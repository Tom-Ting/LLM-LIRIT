import cv2, sys
import os
import numpy as np
from django.http import HttpResponse
import json

from aip import AipOcr


def ocr(image_bytes, lang='CHN_ENG', show_char=False):
    # Use your own id and keys below.
    # Request your app id and keys on https://cloud.baidu.com.
    # APP_ID = '45051593'
    # API_KEY = 'yMHg2emGscvcvdcGSvPlNibN'
    # SECRET_KEY = 'Gdg8WeygRgkRemDiMsGYfMYQNp9Ii4gH'
    APP_ID = '57754781'
    API_KEY = 'OSksEmNrLT0QQuVHpuQiZUMm'
    SECRET_KEY = 'EoKDnLz4JIN1CJSK8G5SQLUTUDF0lpet'

    os.environ["HTTP_PROXY"] = "http://localhost:7890"
    os.environ["HTTPS_PROXY"] = "http://localhost:7890"

    client = AipOcr(APP_ID, API_KEY, SECRET_KEY)
    options = {
        'language-type': lang,
        'recognize_granularity': 'small' if show_char else 'big',
        'probability': 'true'
    }
    return client.general(image_bytes, options)

# 斜率阈值，1/threshold是水平方向阈值,区域为[1,无穷]
slope_threshold = 3.0


def slope_filter(points):
    for i in range(len(points)):
        x1 = points[i][0]
        y1 = points[i][1]
        x2 = points[i + 1][0] if i + 1 < len(points) else points[0][0]
        y2 = points[i + 1][1] if i + 1 < len(points) else points[0][1]
        if x1 == x2:
            continue
        else:
            k = abs((y1 - y2) / (x1 - x2))
            if 1 / slope_threshold < k < slope_threshold:
                return False
    return True


def sift(w, s):
    print("开始调用sift方法")
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
    if len(goodMatch) < 4:
        # 无匹配项
        print("sift匹配失败")
        return None
    try:
        src_pts = np.float32([widget_keypoint[m.queryIdx].pt for m in goodMatch]).reshape(-1, 1, 2)
        dst_pts = np.float32([screenshot_keypoint[m.trainIdx].pt for m in goodMatch]).reshape(-1, 1, 2)
        M, mask = cv2.findHomography(src_pts, dst_pts, cv2.RANSAC, 5.0)
        matchesMask = mask.ravel().tolist()
        h, w = widget_grey.shape
        pts = np.float32([[0, 0], [0, h - 1], [w - 1, h - 1], [w - 1, 0]]).reshape(-1, 1, 2)
        dst = cv2.perspectiveTransform(pts, M)
        center_x = center_y = 0.0
        fill_con = []
        for i in range(len(dst)):
            center_x = center_x + dst[i][0][0]
            center_y = center_y + dst[i][0][1]
            fill_con.append([int(dst[i][0][0]), int(dst[i][0][1])])

        # 面积
        single_area = cv2.contourArea(np.array(fill_con))
        if single_area < 100:
            print("sift面积过小")
            return None
        # 通过斜率来筛选
        if not slope_filter([(dst[0][0][0], dst[0][0][1]), (dst[1][0][0], dst[1][0][1]), (dst[2][0][0], dst[2][0][1]),
                             (dst[3][0][0], dst[3][0][1])]):
            print("sift匹配失败,斜率无法通过")
            return None
        # screenshot = cv2.polylines(screenshot, [np.int32(dst)], True, (255, 0, 0), 3, cv2.LINE_AA)
        # goodMatch = np.expand_dims(goodMatch, 1)
        # img_out = cv2.drawMatchesKnn(widget, widget_keypoint, screenshot, screenshot_keypoint, goodMatch, None, flags=2)
        # cv2.imshow("1",img_out)
        # cv2.waitKey(0)

        return [int(round(center_x / 4)), int(round(center_y / 4))]
    except:
        import traceback
        print("sift匹配失败")
        traceback.print_exc()
        return None


def ocr_match(w, s):
    print("开始调用ocr_match方法")
    res = []

    widget = open(w, 'rb').read()
    screen = open(s, 'rb').read()

    screenshot_ocr_result_path = os.path.dirname(s) + "\\screenshot_ocr_result.json"
    widget_ocr_result_path = os.path.dirname(s) + "\\widget_ocr_result.json"

    # 对screenshot进行OCR
    if os.path.exists(screenshot_ocr_result_path):
        with open(screenshot_ocr_result_path, 'r', encoding='utf-8') as file:
            s_ocr_json = json.load(file)
    else:
        s_ocr_json = ocr(screen)
        with open(screenshot_ocr_result_path, "w", encoding='utf-8') as file:
            json.dump(s_ocr_json, file, ensure_ascii=False)

    # 对widget进行OCR
    if os.path.exists(widget_ocr_result_path):
        with open(widget_ocr_result_path, 'r', encoding='utf-8') as file:
            w_ocr_json = json.load(file)
    else:
        w_ocr_json = ocr(widget)
        if w_ocr_json['words_result_num'] == 0:
            print("未检测到部件中的文字")
            return None
        with open(widget_ocr_result_path, "w", encoding='utf-8') as file:
            json.dump(w_ocr_json, file, ensure_ascii=False)

    w_ocr_res = list(w_ocr_json['words_result'])
    s_ocr_res = list(s_ocr_json['words_result'])

    w_ocr_res = [n for n in filter(lambda x: len(x['words']) > 1, w_ocr_res)]
    if len(w_ocr_res) == 0:
        print("没有符合规则的文字")
        return None

    # 选部件中可能性最高的文字
    w_ocr_res.sort(key=lambda x: x['probability']['average'], reverse=True)
    w_text = w_ocr_res[0]['words']
    print("部件中检测到文字: {}".format(w_text))

    # 进行匹配
    for word_res in s_ocr_res:
        if w_text in word_res['words']:
            res.append([int(word_res['location']['left'] + word_res['location']['width'] / 2),
                        int(word_res['location']['top'] + word_res['location']['height'] / 2)])
    if len(res) != 0:
        print("image文件：ocr 匹配结果为： ", res)
        return res
    print("image文件：ocr 匹配失败")
    return None


def template_cal(component_path, screen_image_path):
    print("开始进行模板匹配，template_cal")
    res = []
    component_image = cv2.imread(component_path)
    screen_image = cv2.imread(screen_image_path)
    c_h, c_w = component_image.shape[:2]
    component_gray = cv2.cvtColor(component_image, cv2.COLOR_BGR2GRAY)
    screen_gray = cv2.cvtColor(screen_image, cv2.COLOR_BGR2GRAY)
    h, w = component_gray.shape[:2]
    # 归一化平方差匹配
    template_res = cv2.matchTemplate(screen_gray, component_gray, cv2.TM_CCOEFF_NORMED)
    min_val, max_val, min_loc, max_loc = cv2.minMaxLoc(template_res)
    loc = np.where(template_res >= 0.9)
    for pt in zip(*loc[::-1]):
        res.append((pt[0] + c_w / 2,
                    pt[1] + c_h / 2))
    return res


def exec(record_widget_path, record_screen_path, replay_screen_path, x, y):
    res = []
    # 模板匹配
    tem_res = template_cal(record_widget_path, replay_screen_path)
    if len(tem_res) != 0:
        print("模板匹配的结果为：", tem_res)
        res = tem_res
    # 进行sift图像匹配，然后进行ocr匹配，最后根据坐标，筛选出结果
    sift_res = sift(record_widget_path, replay_screen_path)
    if sift_res is not None:
        print("sift匹配的结果为：", sift_res)
        res.append(sift_res)
    # 进行ocr匹配
    # ocr_res = ocr_match(record_widget_path, replay_screen_path)
    # if ocr_res is not None:
    #     res.extend(ocr_res)
    # 判断是否有待选结果
    if len(res) == 0:
        print("image 匹配失败")
        return None

    # 进行坐标匹配
    # 对不同分辨率进行转换
    replay_screen = cv2.imread(replay_screen_path)
    record_screen = cv2.imread(record_screen_path)
    rp_h, rp_w, _ = replay_screen.shape
    rd_h, rd_w, _ = record_screen.shape
    new_x = float(x) / rd_w * rp_w
    new_y = float(y) / rd_h * rp_h
    # print('转换后的坐标为{},{}'.format(new_x,new_y))
    # print(res)
    res.sort(key=lambda x: ((x[0] - new_x) ** 2 + (x[1] - new_y) ** 2) ** 0.5)
    print("imageMatch匹配的结果为：", res)
    return res[0]


def image(request):
    record_widget_path = request.GET["recordWidgetPath"]
    record_screen_path = request.GET['recordScreenPath']
    replay_screen_path = request.GET["replayScreenPath"]
    x = request.GET["x"]
    y = request.GET["y"]
    try:
        res = exec(record_widget_path, record_screen_path, replay_screen_path, x, y)
        if res is None:
            return HttpResponse("Fail")
        return HttpResponse(str(int(res[0])) + " " + str(int(res[1])))
    except Exception as e:
        print(e)
        return HttpResponse("Fail")


if __name__ == '__main__':
    # 测试用
    print(exec(r"C:\MyGraduation\database\MapLIRATDatabase\kfcApp\script2\\android\step4\element.png",
               r"C:\MyGraduation\database\MapLIRATDatabase\kfcApp\script2\ios\step2\screenshot.png"))
    print(exec(r"C:\MyGraduation\database\MapLIRATDatabase\kfcApp\script2\android\step3\element.png",
               r"C:\MyGraduation\database\MapLIRATDatabase\kfcApp\script2\android\step3\screenshot.png"))
