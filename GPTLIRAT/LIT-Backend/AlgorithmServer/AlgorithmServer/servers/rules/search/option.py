import cv2

from AlgorithmServer.utils.canny_ocr import extract
from AlgorithmServer.utils.ocr import ocr
from AlgorithmServer.utils.rule_utils import find_semantic_words_in_screen_words, cut, image_type_match
from AlgorithmServer.utils.vgg16 import pred
from AlgorithmServer.views.rules.match.layout import process_bounding

keywords = ["更多"]


def rule_option(replay_screen_path, threshold):
    print("开始选项搜索规则")
    # 语义匹配
    screen = open(replay_screen_path, 'rb').read()
    s_ocr_json = ocr(screen)
    s_ocr_res = list(s_ocr_json['words_result'])
    screen_words = [n['words'] for n in s_ocr_res]
    ind = find_semantic_words_in_screen_words(keywords, screen_words, threshold,5)
    if ind == -1:
        print("语义匹配失败")
    else:
        print("成功找到目标,匹配目标为'{}'".format(screen_words[ind]))
        match_word_data = s_ocr_res[ind]
        print("选项搜索规则匹配成功")
        return match_word_data['location']['left'] + match_word_data['location']['width'] / 2, \
               match_word_data['location'][
                   'top'] + match_word_data['location']['height'] / 2

    res = image_type_match(replay_screen_path, 'menu')
    if res is None:
        print("选项搜索规则匹配失败")
    else:
        print("选项搜索规则匹配成功")
    return res

