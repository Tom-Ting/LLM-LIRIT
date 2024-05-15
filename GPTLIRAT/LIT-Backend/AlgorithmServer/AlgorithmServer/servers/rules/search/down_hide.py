import cv2

from AlgorithmServer.utils.canny_ocr import extract
from AlgorithmServer.utils.ocr import ocr
from AlgorithmServer.utils.rule_utils import find_semantic_words_in_screen_words, cut, boundings_sorting, \
    image_type_match
from AlgorithmServer.utils.vgg16 import pred
from AlgorithmServer.views.rules.match.layout import process_bounding


def rule_down_hide(replay_screen_path):
    print("开始下拉隐藏搜索规则匹配")
    res = image_type_match(replay_screen_path, 'arrow_down')
    if res is None:
        print("下拉隐藏搜索规则匹配失败")
    else:
        print("下拉隐藏搜索规则匹配成功")
    return res
