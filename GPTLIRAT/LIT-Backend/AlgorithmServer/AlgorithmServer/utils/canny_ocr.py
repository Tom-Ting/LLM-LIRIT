import os
import sys
import cv2
import json
import numpy as np

from AlgorithmServer.utils.ocr import ocr

cache_dir = '../cache'


def draw_rectangle_show_save(src, bboxs, output_path, show=False, show_title='image', line=3, color=(0, 255, 255)):
    image = src.copy()
    for bbox in bboxs:
        x, y, w, h = bbox
        cv2.rectangle(image, (x, y), (x + w, y + h), color, line)
    os.makedirs(cache_dir, exist_ok=True)
    cv2.imwrite(cache_dir + output_path, image)
    if show:
        cv2.imshow(show_title, image)
        cv2.waitKey(0)


def print_group(src, groups, line=3, color=(0, 0, 255)):
    output_path = "/group_image.png"
    image = src.copy()
    for group in groups:
        for row in group[0]:
            for col in row[0]:
                cv2.rectangle(image, (col[0], row[1]), (col[1], row[2]), color, line)
    os.makedirs(cache_dir, exist_ok=True)
    cv2.imwrite(cache_dir + output_path, image)


def canny_boundings(image, canny_sigma=0.33, dilate_count=4):
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)  # 色彩空间转换
    v = np.median(gray)
    # 修改阈值，用于识别关闭符号，屏蔽背景的影响
    # todo 需要一个更好地方法来选择阈值
    # lower_threshold = int(max(0, (1 - canny_sigma) * v))
    upper_threshold = int(min(255, (1 + canny_sigma) * v))
    img_binary = cv2.Canny(gray, 120, upper_threshold, -1)
    # cv2.imshow("1",img_binary)
    # cv2.waitKey(0)
    img_dilated = cv2.dilate(img_binary, None, iterations=dilate_count)
    # cv2.imshow("2", img_dilated)
    # cv2.waitKey(0)
    contours, _ = cv2.findContours(img_dilated, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)
    # Find bounding using contours.
    boundings = []
    for c in contours:
        boundings.append(cv2.boundingRect(c))
    return boundings


def deep_ocr(image_path, lang='CHN_ENG', prob=0.90, space_ratio=0.7):
    cache_ocr_dir = cache_dir + "/ocr"
    os.makedirs(cache_ocr_dir, exist_ok=True)
    cache_file = os.path.join(cache_ocr_dir, '%s.json' %
                              os.path.basename(image_path)[0: os.path.basename(image_path).index(".")])
    if os.path.isfile(cache_file):
        result = json.load(open(cache_file, 'r'))
        load_flag = True
    else:
        result = ocr(open(image_path, 'rb').read(), lang)
        load_flag = False

    image = cv2.imread(image_path)
    assert image is not None, 'Cannot read the image file %s.' % image_path
    if 'words_result' not in result:
        print('OCR failed: %s' % str(result), file=sys.stderr)
        return []

    text_boxes = []
    for words in result['words_result']:
        left = words['location']['left']
        top = words['location']['top']
        width = words['location']['width']
        height = words['location']['height']
        cropped = cv2.imencode('.jpg', image[top:top + height, left:left + width, :])[1].tobytes()
        if load_flag:
            details = words['details']
        else:
            words['details'] = details = ocr(cropped, show_char=True)
        if 'words_result' not in details:
            continue
        for detailed_words in details['words_result']:
            if detailed_words['probability']['average'] < prob:
                continue
            split_idx = []
            for i, (p, q) in enumerate(zip(detailed_words['chars'][:-1], detailed_words['chars'][1:])):
                distance = q['location']['left'] - p['location']['left'] - p['location']['width']
                threshold = space_ratio * min(p['location']['height'], q['location']['height'])
                if distance > threshold:
                    split_idx.append(i + 1)
            for i, j in zip([0] + split_idx, split_idx + [len(detailed_words['chars'])]):
                texts = detailed_words['words'][i:j]
                q_loc = detailed_words['chars'][i]['location']
                box_x = q_loc['left'] + left
                box_y = q_loc['top'] + top
                r_loc = detailed_words['chars'][j - 1]['location']
                box_w = r_loc['left'] + r_loc['width'] - q_loc['left']
                box_h = q_loc['height']
                text_boxes.append((box_x, box_y, box_w, box_h))
    # if not load_flag:
    #    json.dump(result, open(cache_file, 'w'), ensure_ascii=False, indent=4)
    return text_boxes


def intersect(rect_a, rect_b):
    a_x, a_y, a_w, a_h = rect_a
    b_x, b_y, b_w, b_h = rect_b
    dx = max(0, min(a_x + a_w, b_x + b_w) - max(a_x, b_x))
    dy = max(0, min(a_y + a_h, b_y + b_h) - max(a_y, b_y))
    S_i = dx * dy
    S_a = a_w * a_h
    S_b = b_w * b_h
    return S_i / S_a, S_i / S_b, S_i / (S_a + S_b - S_i)


def extract(image_path, use_ocr=False, threshold=.70):
    image = cv2.imread(image_path)
    assert image is not None, 'Cannot read the image file %s.' % image_path
    boundings = canny_boundings(image)
    ocr_res = deep_ocr(image_path) if use_ocr else []
    mods = []
    for rect_o in ocr_res:
        best_match = (None, 0, 0, 0)
        for rect_c in boundings:
            ratio_o, ratio_c, ratio = intersect(rect_o, rect_c)
            if ratio > best_match[3]:
                best_match = (rect_c, ratio_o, ratio_c, ratio)
        rect_c, _, ratio_c, _ = best_match
        if rect_c is not None:
            if ratio_c > threshold:
                mods.append(('replace', rect_c, rect_o))
                for rect_cc in boundings:
                    if rect_cc == rect_c:
                        continue
                    _, ratio_cc, _ = intersect(rect_o, rect_cc)
                    if ratio_cc > threshold:
                        mods.append(('delete', rect_cc))
            else:
                mods.append(('add', rect_o))
        else:
            mods.append(('add', rect_o))
    for mod in set(mods):
        if mod[0] == 'replace':
            boundings[boundings.index(mod[1])] = mod[2]
        elif mod[0] == 'add':
            boundings.append(mod[1])
        elif mod[0] == 'delete':
            boundings.remove(mod[1])
    draw_rectangle_show_save(image, boundings, "/extract_image.png")
    return boundings

if __name__ == '__main__':
    extract(r"C:\Users\zpc\Desktop\MapLIRATDatabase\dianpingApp\script3\ios\step12\screenshot.png")