from AlgorithmServer.utils.canny_ocr import extract
from AlgorithmServer.utils.ocr import ocr
from AlgorithmServer.utils.rule_utils import find_semantic_words_in_screen_words, cut, boundings_sorting, my_resize, \
    image_type_match
from AlgorithmServer.utils.vgg16 import pred
import cv2
import os

# 适用于该匹配规则的类型
from AlgorithmServer.views.rules.match.layout import process_bounding

types = ["arrow_left","add"]


def rule_widget_type(record_widget_path, replay_screen_path, record_screen_path, record_x, record_y):
    print("开始图标类型匹配规则匹配,调用rule_widget_type方法")

    widget = cv2.imread(record_widget_path)
    widget_type = pred(widget)

    try:
        parent_dir = os.path.dirname(os.path.abspath(replay_screen_path))
        with open(parent_dir+"\\widget_type.text",'w', encoding="utf-8") as file:
            file.write(widget_type)
        print("成功识别图标类型，并写入文件！")
    except Exception as e:
        print(e)
        print("成功识别图标类型，但写入文件失败！")
    print(widget_type)
    if widget_type not in types:
        return None

    res = image_type_match(replay_screen_path, widget_type, record_screen_path, record_x, record_y)
    if res is None:
        print("图标类型匹配规则匹配失败")
    else:
        print("图标类型匹配规则匹配成功")
    return res
