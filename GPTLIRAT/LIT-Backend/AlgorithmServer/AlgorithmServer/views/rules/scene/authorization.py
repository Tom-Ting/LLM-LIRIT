from django.http import HttpResponse

from AlgorithmServer.servers.rules.scene.authorization import rule_authorization


def rule_authorization_views(request):
    replay_screen_path = request.GET["replayScreenPath"]
    try:
        res = rule_authorization(replay_screen_path)
        if res is None:
            return HttpResponse("Fail")
        return HttpResponse(str(int(res[0])) + " " + str(int(res[1])))
    except Exception as e:
        print(e)
        return HttpResponse("Fail")