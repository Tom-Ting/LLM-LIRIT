from django.http import HttpResponse

from AlgorithmServer.servers.rules.search.option import rule_option


def rule_option_views(request):
    replay_screen_path = request.GET["replayScreenPath"]
    threshold= float(request.GET["threshold"])
    try:
        res = rule_option(replay_screen_path,threshold)
        if res is None:
            return HttpResponse("Fail")
        return HttpResponse(str(int(res[0])) + " " + str(int(res[1])))
    except Exception as e:
        print(e)
        return HttpResponse("Fail")