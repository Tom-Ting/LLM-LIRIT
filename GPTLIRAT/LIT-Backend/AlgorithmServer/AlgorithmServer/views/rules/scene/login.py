from django.http import HttpResponse

from AlgorithmServer.servers.rules.scene.login import rule_login


def rule_login_views(request):
    replay_screen_path = request.GET["replayScreenPath"]
    try:
        res = rule_login(replay_screen_path)
        if res is None:
            return HttpResponse("Fail")
        return HttpResponse(str(int(res[0])) + " " + str(int(res[1])))
    except Exception as e:
        print(e)
        return HttpResponse("Fail")