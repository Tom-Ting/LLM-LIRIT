from AlgorithmServer.utils.rule_utils import run_rule_scene_by_text

identify_keywords = ["政策"]
action_keywords = ["同意"]
threshold = 0.75
name="政策"

# 弹出相关政策提醒，需要点击“知道了”,"同意”
def rule_policy(imagePath):
    print("开始{}场景规则匹配".format(name))
    res = run_rule_scene_by_text(imagePath, identify_keywords, action_keywords, threshold)
    if res is None:
        print("{}场景规则匹配失败".format(name))
    else:
        print("{}场景规则匹配成功".format(name))
    return res

if __name__ == '__main__':
    for i in range(1, 15):
        print(rule_policy(
            r"C:\Users\zpc\Desktop\MapLIRATDatabase\meituanApp\script1\android\step{}\screenshot.png".format(i)))


