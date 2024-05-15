import cv2
import json
import canny_ocr
import numpy as np


def flat_bounding(image, canny_sigma=0.33, dilate_count=4):
    """
    Find external bounding of widgets in a screenshot.

    :param image: Input image of type `ndarray`.
    :param canny_sigma: Sigma parameter for canny to control the thresholds.
    :param dilate_count: Number of iterations to perform dilation.
    :return: A list of widget bounding.
        Each bounding is represented as (x, y, w, h) where (x, y) stands for the position
        of the top-left vertex of the bounding, and (w, h) stands for the width and height of the bounding.
    """
    v = np.median(image)
    img_binary = cv2.Canny(image, int(max(0, (1 - canny_sigma) * v)), int(min(255, (1 + canny_sigma) * v)))
    img_dilated = cv2.dilate(img_binary, None, iterations=dilate_count)
    _, contours, _ = cv2.findContours(img_dilated, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    bounding = []
    for c in contours:
        bounding.append(cv2.boundingRect(c))
    return bounding


def process_bounding(img, bounding_list):
    non_blanks = []
    for bounding in bounding_list:
        x, y, w, h = bounding
        node = img[y:y + h, x:x + w, :]
        if not np.count_nonzero(node) == 0 and not np.count_nonzero(255 - node) == 0:
            non_blanks.append(bounding)

    enlarge_width = 5
    img_h, img_w, _ = img.shape
    enlarged_bounding = []
    for x, y, w, h in bounding_list:
        enlarged_x = max(0, x - enlarge_width)
        enlarged_y = max(0, y - enlarge_width)
        enlarged_w = min(w + 2 * enlarge_width, img_w - enlarged_x)
        enlarged_h = min(h + 2 * enlarge_width, img_h - enlarged_y)
        enlarged_bounding.append((enlarged_x, enlarged_y, enlarged_w, enlarged_h))
    return enlarged_bounding


def layout(bounding, resolution):
    """
        Data Structure Generation
    """
    # Group Generation
    groups = group_generation(basic_row_generation(bounding), resolution)

    # Row & Column Generation.

    line_merge_threshold = 1.5
    column_merge_threshold = 1.5

    for group in groups:
        group[0][0][1] = group[1]
        group[0][-1][2] = group[2]
        #lines = sorted(set([row[1] for row in group[0]] + [row[2] for row in group[0]]))
        nodes = [(y, h) for node_row in group[0] for _, y, _, h in node_row[0]]
        lines = sorted(set([y for y, _ in nodes] + [y + h for y, h in nodes] + [group[1], group[2]]))
        merge_close_lines(lines, line_merge_threshold * resolution[1] / 100)
        lines[0] = group[1]
        lines[-1] = group[2]

        rows = []
        for top, bottom in zip(lines[:-1], lines[1:]):
            filtered_basic_rows = [row for row in group[0] if not (bottom <= row[1] or top >= row[2])]
            filtered_nodes = [(x, w) for row in filtered_basic_rows
                              for x, y, w, h in row[0] if not (y + h <= top or y >= bottom)]
            cols = sorted(set([x for x, _ in filtered_nodes] + [x + w for x, w in filtered_nodes]))
            if len(cols) == 0 or not cols[0] == 0:
                cols = [0] + cols
            if not cols[-1] == resolution[0]:
                cols.append(resolution[0])
            if len(cols) > 0:
                merge_close_lines(cols, column_merge_threshold * resolution[0] / 100)
                cols[0] = 0
                cols[-1] = resolution[0]
                cols = [[left, right] for left, right in zip(cols[:-1], cols[1:])]
            rows.append([cols, top, bottom])
        group[0] = rows

    return groups


def basic_row_generation(bounding):
    basic_rows = []
    for bounding in sorted(bounding, key=lambda b: b[1] + b[3] / 2):
        x, y, w, h = bounding
        center_y = y + h / 2
        found = False
        for row in basic_rows:
            ceiling = row[1]
            ground = row[2]
            if ceiling <= center_y <= ground:
                row[0].append(bounding)
                row[1] = min(ceiling, y)
                row[2] = max(ground, y + h)
                found = True
                break
        if not found:
            basic_rows.append([[bounding], y, y + h])
    return basic_rows


def group_generation(basic_rows, resolution):
    # Initial Group generation.
    groups = [[[row], row[1], row[2]] for row in basic_rows]
    surviving = [True] * len(groups)
    group_count = 0
    while not len(groups) == group_count:
        group_count = len(groups)
        for i, group_i in enumerate(groups):
            for j, group_j in enumerate(groups):
                if not i == j and surviving[j] and \
                        group_j[1] <= (group_i[1] + group_i[2]) / 2 <= group_j[2]:
                    group_j[0] += group_i[0]
                    group_j[1] = min(group_j[1], group_i[1])
                    group_j[2] = max(group_j[2], group_i[2])
                    surviving[i] = False
                    break
    groups = [group for i, group in enumerate(groups) if surviving[i]]

    # Group separation.
    for i, group_i in enumerate(groups):
        for group_j in groups[i + 1:]:
            if group_j[1] < group_i[2] < group_j[2]:
                group_i[2] = int((group_i[2] + group_j[1]) / 2)
                group_j[1] = group_i[2]
            elif group_j[1] < group_i[1] < group_j[2]:
                group_i[1] = int((group_i[1] + group_j[2]) / 2)
                group_j[2] = group_i[1]

    # Group simplification.
    if len(groups) > 0:
        groups[0][1] = 0  # The first group should be at the top.
        groups[-1][2] = resolution[1]  # The last group should be at the bottom.
    for prev, cur in zip(groups[:-1], groups[1:]):
        if prev[2] < cur[1]:
            cur[1] = int((prev[2] + cur[1]) / 2)
            prev[2] = cur[1]
    g_threshold = 1.5 * resolution[1] / 100
    surviving = [True] * len(groups)
    for i in range(len(groups)):
        if groups[i][2] - groups[i][1] < g_threshold:
            if i - 1 < 0 and i + 1 < len(groups):
                groups[i + 1][0] += groups[i][0]
                groups[i + 1][1] = groups[i][1]
            elif i + 1 >= len(groups) and i - 1 >= 0:
                groups[i - 1][0] += groups[i][0]
                groups[i - 1][2] = groups[i][2]
            elif i - 1 >= 0 and i + 1 < len(groups):
                height_a = groups[i - 1][2] - groups[i - 1][1]
                height_b = groups[i + 1][2] - groups[i + 1][1]
                if height_a < height_b:
                    groups[i - 1][0] += groups[i][0]
                    groups[i - 1][2] = groups[i][2]
                else:
                    groups[i + 1][0] += groups[i][0]
                    groups[i + 1][1] = groups[i][1]
            surviving[i] = False
    return [group for i, group in enumerate(groups) if surviving[i]]


def merge_close_lines(lines, threshold=5):
    i = 0
    if len(lines) < 2:
        return
    first = lines[0]
    last = lines[-1]
    while i + 1 < len(lines):
        if lines[i + 1] - lines[i] < threshold:
            lines[i] = int((lines[i] + lines[i + 1]) / 2)
            lines.pop(i + 1)
        else:
            i += 1
    # In case it cannot form a row or a column.
    if len(lines) < 2:
        lines.clear()
        lines.extend([first, last])


def exec(record, path, coordinate):
    img = cv2.imread(path)
    groups = layout(process_bounding(img, canny_ocr.extract(path)), (img.shape[1], img.shape[0]))
    if record:
        x = int(coordinate[0])
        y = int(coordinate[1])
        group_no, group = next((i, g) for i, g in enumerate(groups) if g[1] <= y < g[2])
        row_no, row = next((i, r) for i, r in enumerate(group[0]) if r[1] <= y < r[2])
        col_no = next(i for i, c in enumerate(row[0]) if c[0] <= x < c[1])
        return groups, (group_no, row_no, col_no)
    else:
        group_no = int(coordinate[0])
        row_no = int(coordinate[1])
        col_no = int(coordinate[2])
        if group_no < len(groups):
            group = groups[group_no]
            if row_no < len(group[0]):
                row = group[0][row_no]
            else:
                lines = [line for g in groups for line in g[0]]
                diff = row_no - len(group[0])
                while diff >= 0:
                    group_no += 1
                    if diff < len(groups[group_no][0]):
                        row = groups[group_no][0][diff]
                        break
                    else:
                        diff -= len(groups[group_no][0])
            if col_no >= len(row[0]):
                # Find the nearest line which has enough columns.
                lines = [line for g in groups for line in g[0]]
                valid_lines = [line for line in lines if len(line[0]) > col_no]
                row = min(valid_lines, key=lambda line: abs(lines.index(line) - lines.index(row)))
            col = row[0][col_no]
            return groups, (int((col[0] + col[1]) / 2), int((row[1] + row[2]) / 2))
        else:
            raise RuntimeError('Group number out of range.')


def group_json(groups):
    gson = [{
        'rows': [{
            'cols': [{
                'left': col[0],
                'right': col[1]
            } for col in row[0]],
            'top': row[1],
            'bottom': row[2]
        } for row in group[0]],
        'top': group[1],
        'bottom': group[2]
    } for group in groups]
    return json.dumps(gson)


if __name__ == '__main__':
    import sys
    print(exec(sys.argv[1] == 'record', sys.argv[2], tuple(sys.argv[3:]))[-1])
