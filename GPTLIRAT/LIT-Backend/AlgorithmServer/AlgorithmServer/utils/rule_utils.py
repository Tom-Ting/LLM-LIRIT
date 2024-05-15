import cv2

from AlgorithmServer.utils.canny_ocr import extract
from AlgorithmServer.utils.ocr import ocr
from AlgorithmServer.utils.semantic_model import sentence_semantic_multiply_match
from AlgorithmServer.utils.vgg16 import pred
from AlgorithmServer.views.rules.match.layout import process_bounding


def run_rule_scene_by_text(imagePath, identify_keywords, action_keywords, threshold):
    screen = open(imagePath, 'rb').read()
    s_ocr_json = ocr(screen)
    s_ocr_res = list(s_ocr_json['words_result'])
    screen_words = [n['words'] for n in s_ocr_res]
    print("开始场景验证")
    # 判断是否存在关键词
    ind = find_semantic_words_in_screen_words(identify_keywords, screen_words, threshold)
    if ind == -1:
        print("验证失败")
        return None
    print("验证成功，验证对象为'{}'".format(screen_words[ind]))
    print("开始寻找操作对象")
    # 走到这步说明验证场景成功，接着获取需要操作的目标
    ind = find_semantic_words_in_screen_words(action_keywords, screen_words, threshold, 5)
    if ind == -1:
        print("操作失败")
        return None
    print("成功找到操作目标,匹配目标为'{}'".format(screen_words[ind]))
    # 转换为坐标
    match_word_data = s_ocr_res[ind]
    return match_word_data['location']['left'] + match_word_data['location']['width'] / 2, match_word_data['location'][
        'top'] + match_word_data['location']['height'] / 2


def index_contain_in_list(s, l, length):
    for i in range(0, len(l)):
        if s in l[i] and len(l[i]) < length:
            return i
    return -1


def index_in_list(s, l):
    for i in range(0, len(l)):
        if s == l[i]:
            return i
    return -1


def find_semantic_words_in_screen_words(keywords, screen_words, threshold, length=99):
    ind = -1
    for x in keywords:
        if index_contain_in_list(x, screen_words, length) != -1:
            ind = index_contain_in_list(x, screen_words, length)
            break
    if ind == -1:
        # 语义匹配
        semantic_res = sentence_semantic_multiply_match(keywords, screen_words, threshold)
        if sum([i for i in semantic_res]) == 0 - len(semantic_res):
            return -1
        else:
            for x in semantic_res:
                if x != -1:
                    ind = x
                    break
    return ind


def cut(path, left, upper, right, lower):
    img = cv2.imread(path)  # 打开图像
    cropped = img[upper:lower, left:right]
    return cropped


def coordinate_sorting(corrdinates: list):
    """
    用于排序坐标，从左上到右下
    :param corrdinates:
    :return:
    """
    return sorted(corrdinates, key=lambda x: (x[1], x[0]))


def boundings_sorting(boundings: list):
    """
    用于排序外边框，从左上到右下
    :param boundings:
    :return:
    """
    return sorted(boundings, key=lambda x: (x[1], x[0]))


def boundings_sorting_by_x_y(boundings: list, cal_x, cal_y):
    """
    用于排序外边框，从左上到右下
    :param boundings:
    :return:
    """
    return sorted(boundings, key=lambda x: ((x[0] + x[2] / 2 - cal_x) ** 2 + (x[1] + x[3] / 2 - cal_y) ** 2))


def my_resize(im, target_height, target_width):
    height, width = im.shape[:2]  # 取彩色图片的长、宽。

    ratio_h = height / target_height
    ration_w = width / target_width

    ratio = max(ratio_h, ration_w)

    # 缩小图像  resize(...,size)--size(width，height)
    size = (int(width / ratio), int(height / ratio))
    shrink = cv2.resize(im, size, interpolation=cv2.INTER_AREA)  # 双线性插值
    tianchong = [255, 255, 255]

    a = (target_width - int(width / ratio)) / 2
    b = (target_height - int(height / ratio)) / 2

    constant = cv2.copyMakeBorder(shrink, int(b), int(b), int(a), int(a), cv2.BORDER_CONSTANT, value=tianchong)
    constant = cv2.resize(constant, (target_width, target_height), interpolation=cv2.INTER_AREA)
    return constant


def image_type_match(replay_screen_path, widget_type, record_screen_path=None, record_x=0, record_y=0):
    """
    从图片中找出对应类型的图标
    :param replay_screen_path:
    :param widget_type:
    :return:
    """
    # 图像匹配
    screen_img = cv2.imread(replay_screen_path)
    transformer_x = 0
    transformer_y = 0
    # 计算相对坐标，进行排序
    if record_screen_path is not None:
        record_h, record_w, _ = cv2.imread(record_screen_path).shape
        replay_h, replay_w, _ = cv2.imread(replay_screen_path).shape
        transformer_x = float(record_x) / float(record_w) * replay_w
        transformer_y = float(record_y) / float(record_h) * replay_h
    boundings = boundings_sorting_by_x_y(process_bounding(screen_img, extract(replay_screen_path)), transformer_x,
                                         transformer_y)
    for boundings in boundings:
        left, upper, right, lower = boundings[0], boundings[1], boundings[2] + boundings[0], boundings[3] + boundings[1]
        cropped = cut(replay_screen_path, left, upper, right, lower)  # 不用把图保存下来，直接做参数就好
        # 填充到正方形
        new_size = max(right - left, upper - lower)
        cropped = my_resize(cropped, new_size, new_size)
        ty = pred(cropped)
        res = [int((left + right) / 2), int((upper + lower) / 2)]
        if (ty == widget_type):
            print(res)
            return res
    return None


if __name__ == '__main__':
    image_type_match(r"C:\Users\zpc\Desktop\MapLIRATDatabase\hemaApp\script5\ios\step9\screenshot.png", "add")
