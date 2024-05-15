import cv2, sys
import numpy as np


def exec(w, s):
    widget_path = w
    screenshot_path = s
    widget = cv2.imread(widget_path)
    screenshot = cv2.imread(screenshot_path)

    # SIFT feature points extraction.
    widget_grey = cv2.cvtColor(widget, cv2.COLOR_BGR2GRAY)
    screenshot_grey = cv2.cvtColor(screenshot, cv2.COLOR_BGR2GRAY)

    sift = cv2.SIFT_create()
    widget_keypoint, widget_descriptor = sift.detectAndCompute(widget_grey, None)
    screenshot_keypoint, screenshot_descriptor = sift.detectAndCompute(screenshot_grey, None)

    widget = cv2.drawKeypoints(widget_grey, widget_keypoint, widget)
    screenshot = cv2.drawKeypoints(screenshot_grey, screenshot_keypoint, screenshot)

    # FLANN Matching.
    FLANN_INDEX_KDTREE = 0
    index_params = dict(algorithm=FLANN_INDEX_KDTREE, trees=5)
    search_params = dict(checks=50)

    flann = cv2.FlannBasedMatcher(index_params, search_params)
    matches = flann.knnMatch(widget_descriptor, screenshot_descriptor, k=2)

    goodMatch = []
    for m, n in matches:
        if m.distance < 0.50 * n.distance:
            goodMatch.append(m)

    try:
        src_pts = np.float32([widget_keypoint[m.queryIdx].pt for m in goodMatch]).reshape(-1, 1, 2)
        dst_pts = np.float32([screenshot_keypoint[m.trainIdx].pt for m in goodMatch]).reshape(-1, 1, 2)
        M, mask = cv2.findHomography(src_pts, dst_pts, cv2.RANSAC, 5.0)
        matchesMask = mask.ravel().tolist()
        h, w = widget_grey.shape
        pts = np.float32([[0, 0], [0, h - 1], [w - 1, h - 1], [w - 1, 0]]).reshape(-1, 1, 2)
        dst = cv2.perspectiveTransform(pts, M)
        center_x = center_y = 0.0
        for i in range(len(dst)):
            center_x = center_x + dst[i][0][0]
            center_y = center_y + dst[i][0][1]

        screenshot = cv2.polylines(screenshot, [np.int32(dst)], True, 255, 3, cv2.LINE_AA)

        goodMatch = np.expand_dims(goodMatch, 1)

        img_out = cv2.drawMatchesKnn(widget, widget_keypoint, screenshot, screenshot_keypoint, goodMatch, None, flags=2)

        return int(round(center_x / 4)), int(round(center_y / 4))
    except:
        import traceback
        traceback.print_exc()
        return None


if __name__ == '__main__':
    print(exec(sys.argv[1], sys.argv[2]))
