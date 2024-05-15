import os
import json
from django.http import HttpResponse

from AlgorithmServer.utils.ocr import ocr
from AlgorithmServer.utils.semantic_model import sentence_semantic_match

# w : record_widget_path,即录制图片地址
# s : replay_screen_path,即回放图片地址
def semantic_match(w, s, threshold):
    widget = open(w, 'rb').read()
    screen = open(s, 'rb').read()

    screenshot_ocr_result_path = os.path.dirname(s) + "\\screenshot_ocr_result.json"
    widget_ocr_result_path = os.path.dirname(s) + "\\widget_ocr_result.json"

    # 对screenshot进行OCR
    if os.path.exists(screenshot_ocr_result_path):
        with open(screenshot_ocr_result_path, 'r') as file:
            s_ocr_json = json.load(file)
    else:
        s_ocr_json = ocr(screen)
        with open(screenshot_ocr_result_path, "w") as file:
            json.dump(s_ocr_json, file, ensure_ascii=False)

    # 对widget进行OCR
    if os.path.exists(widget_ocr_result_path):
        with open(widget_ocr_result_path, 'r') as file:
            w_ocr_json = json.load(file)
    else:
        w_ocr_json = ocr(widget)
        if w_ocr_json['words_result_num'] == 0:
            print("未检测到部件中的文字")
            return None
        with open(widget_ocr_result_path, "w") as file:
            json.dump(w_ocr_json, file, ensure_ascii=False)

    if w_ocr_json['words_result_num'] == 0:
        print("未检测到部件中的文字")
        return None
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

    screen_words=[n['words'] for n in s_ocr_res]

    ind, score = sentence_semantic_match(w_text, screen_words)
    print("完成语义匹配，匹配结果为{}:{}".format(s_ocr_res[ind]['words'], score))
    if float(score) < float(threshold):
        return None
    match_word_data = s_ocr_res[ind]
    return match_word_data['location']['left'] + match_word_data['location']['width'] / 2, match_word_data['location'][
        'top'] + match_word_data['location']['height'] / 2


def entrance(request):
    record_widget_path = request.GET["recordWidgetPath"]
    replay_screen_path = request.GET["replayScreenPath"]
    threshold = request.GET["threshold"]
    print("This is semantic.entrance running ! ")
    try:
        res = semantic_match(record_widget_path, replay_screen_path, threshold)
        if res is None:
            return HttpResponse("Fail")
        return HttpResponse(str(int(res[0])) + " " + str(int(res[1])))
    except Exception as e:
        print(e)
        return HttpResponse("Fail")
