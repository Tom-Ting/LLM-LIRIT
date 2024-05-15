from AlgorithmServer.utils.rule_utils import run_rule_scene_by_text

identify_keywords = ["升级", "新增", "下一步", "体验", "开始"]
action_keywords = ["下一步", "体验", "开始"]
threshold = 0.75
name = "教程"


# 首次打开的功能出现教程
# 需要几次点击下一步的操作才能进入页面
def rule_guide(imagePath):
    print("开始{}场景规则匹配".format(name))
    res = run_rule_scene_by_text(imagePath, identify_keywords, action_keywords, threshold)
    if res is None:
        print("{}场景规则匹配失败".format(name))
    else:
        print("{}场景规则匹配成功".format(name))
    return res


if __name__ == '__main__':
    for i in range(1, 15):
        print(rule_guide(
            r"C:\Users\zpc\Desktop\MapLIRATDatabase\DingTalkApp\script2\ios\step{}\screenshot.png".format(i)))
