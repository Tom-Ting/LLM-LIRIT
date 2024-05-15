import os
from django.http import HttpResponse

from AlgorithmServer.utils.ocr import ocr_for_step
from AlgorithmServer.utils.all_widgets_type import image_type_match, widget_type_match


# 先OCR，element.png和screenshot.png
def pre_chatgpt(record_element_path, replay_step_path):
    '''
    ChatGPT辅助模块前，数据预处理:
        录制文件ocr处理
        录制控件类型识别
        回放step路径下，将element.png和screenshot.png文件进行ocr处理，并本地保存
        回放截图类型识别
    :param record_element_path:
    :param replay_step_path:
    :return:
    '''
    widget_type_match(record_element_path)
    record_step_path = os.path.dirname(record_element_path)
    ocr_for_step(record_step_path)
    ocr_for_step(replay_step_path)
    screen_path = replay_step_path + r"\screenshot.png"
    image_type_match(screen_path)


def entrance(request, debug=False):
    """
    作为服务的对外接口
    :param request: 请求信息。应包含step_path
    :param debug: 是否在本地调试/由于本地不是Django服务，所以不能使用Django框              架的Http相关功能
    :return: 返回成功处理信息
    """
    record_element_path = request.GET["recordElementPath"]
    step_path = request.GET["stepPath"]
    pre_chatgpt(record_element_path, step_path)
    print("ChatGPT模块：数据预处理完毕！")
    if not debug:
        return HttpResponse("ChatGPT模块：数据预处理完毕！")
    else:
        print("ChatGPT模块：数据预处理完毕！")


if __name__ == "__main__":
    class request:
        GET = {"stepPath": r"C:\MyGraduation\database_test\MapLIRATDatabase\dianpingApp\script1\android\step2"}


    http_request = entrance(request, True)
