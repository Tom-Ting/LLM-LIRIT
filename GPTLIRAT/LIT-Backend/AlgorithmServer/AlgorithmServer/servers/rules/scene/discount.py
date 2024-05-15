from AlgorithmServer.utils.rule_utils import run_rule_scene_by_text

identify_keywords = ["优惠"]
action_keywords = ["放弃"]
threshold = 0.75
name = "优惠"


# 弹出的红包，需要进行"放弃优惠"
def rule_discount(imagePath):
    print("开始{}场景规则匹配".format(name))
    res = run_rule_scene_by_text(imagePath, identify_keywords, action_keywords, threshold)
    if res is None:
        print("{}场景规则匹配失败".format(name))
    else:
        print("{}场景规则匹配成功".format(name))
    return res


if __name__ == '__main__':
    for i in range(1, 10):
        print(rule_discount(
            r"C:\Users\zpc\Desktop\MapLIRATDatabase\meituanApp\script1\android\step{}\screenshot.png".format(i)))
