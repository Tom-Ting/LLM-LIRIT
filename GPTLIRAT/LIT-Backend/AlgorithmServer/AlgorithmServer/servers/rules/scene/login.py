from AlgorithmServer.utils.rule_utils import run_rule_scene_by_text

identify_keywords = ["登录"]
action_keywords = ["跳过"]
threshold = 0.75
name="登录"

# 可能会出现登录界面
# 不属于测试流程，需要进行“跳过”操作。
def rule_login(imagePath):
    print("开始{}场景规则匹配".format(name))
    res = run_rule_scene_by_text(imagePath, identify_keywords, action_keywords, threshold)
    if res is None:
        print("{}场景规则匹配失败".format(name))
    else:
        print("{}场景规则匹配成功".format(name))
    return res

if __name__ == '__main__':
    for i in range(1, 15):
        print(rule_login(
            r"C:\Users\zpc\Desktop\MapLIRATDatabase\dianpingApp\script1\android\step{}\screenshot.png".format(i)))


