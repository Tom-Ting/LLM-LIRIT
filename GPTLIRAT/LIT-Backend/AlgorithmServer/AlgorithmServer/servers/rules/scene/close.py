import cv2
from AlgorithmServer.utils.canny_ocr import extract
from AlgorithmServer.utils.rule_utils import cut, find_semantic_words_in_screen_words
from AlgorithmServer.utils.vgg16 import pred
from tensorflow.python.keras.models import load_model
from AlgorithmServer.utils.ocr import ocr
from AlgorithmServer.utils.semantic_model import sentence_semantic_match

keywords = ["关闭", "跳过"]


def rule_close(pic_path, threshold):
    print("开始广告场景规则匹配")
    # 语义匹配
    screen = open(pic_path, 'rb').read()
    s_ocr_json = ocr(screen)
    s_ocr_res = list(s_ocr_json['words_result'])
    screen_words = [n['words'] for n in s_ocr_res]
    ind = find_semantic_words_in_screen_words(keywords, screen_words, threshold, 5)
    if ind == -1:
        print("语义匹配失败")
    else:
        print("成功找到目标,匹配目标为'{}'".format(screen_words[ind]))
        match_word_data = s_ocr_res[ind]
        print("广告场景规则匹配成功")
        return match_word_data['location']['left'] + match_word_data['location']['width'] / 2, \
               match_word_data['location'][
                   'top'] + match_word_data['location']['height'] / 2
    # 图像匹配
    boundings = extract(pic_path)
    i = 0
    for boundings in boundings:
        left, upper, right, lower = boundings[0], boundings[1], boundings[2] + boundings[0], boundings[3] + boundings[1]
        cropped = cut(pic_path, left, upper, right, lower)  # 不用把图保存下来，直接做参数就好
        i = i + 1
        ty = pred(cropped)
        res = [int((left + right) / 2), int((upper + lower) / 2)]
        if (ty == 'close'):
            print(res)
            print("广告场景规则匹配成功")
            return res
    print("广告场景匹配失败")


if __name__ == '__main__':
    pic_path = 'C:/Users/zpc/Desktop/MapLIRATDatabase/LarkApp/script3/ios/step12/screenshot.png'
    # pic_path='C:/Users/zpc/Desktop/MapLIRATDatabase/hemaApp/script1/ios/step13/screenshot.png'
    rule_close(pic_path)
