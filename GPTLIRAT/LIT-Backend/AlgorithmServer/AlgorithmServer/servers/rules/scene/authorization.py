from AlgorithmServer.utils.rule_utils import run_rule_scene_by_text

identify_keywords = ["申请", "权限", "授权", "通知", "位置", "照片", "设置"]
action_keywords = ["同意", "确定", "允许", "授权"]
threshold = 0.75
name = "授权"


# 可能需要点击“授权”、“允许”，当权限被限制，需要点击“确定”
def rule_authorization(imagePath):
    print("开始{}场景规则匹配".format(name))
    res = run_rule_scene_by_text(imagePath, identify_keywords, action_keywords, threshold)
    if res is None:
        print("{}场景规则匹配失败".format(name))
    else:
        print("{}场景规则匹配成功".format(name))
    return res


if __name__ == '__main__':
    for i in range(1, 15):
        print(rule_authorization(
            r"C:\Users\zpc\Desktop\MapLIRATDatabase\hemaApp\script1\ios\step{}\screenshot.png".format(i)))
