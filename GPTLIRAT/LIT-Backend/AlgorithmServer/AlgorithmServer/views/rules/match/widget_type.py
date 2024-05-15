from django.http import HttpResponse

from AlgorithmServer.servers.rules.match.widget_type import rule_widget_type
from AlgorithmServer.servers.rules.scene.authorization import rule_authorization


def rule_widget_type_views(request):
    record_widget_path = request.GET["recordWidgetPath"]
    record_screen_path = request.GET['recordScreenPath']
    replay_screen_path = request.GET["replayScreenPath"]
    x = int(request.GET['x'])
    y = int(request.GET['y'])
    try:
        res = rule_widget_type(record_widget_path,replay_screen_path,record_screen_path,x,y)
        if res is None:
            return HttpResponse("Fail")
        return HttpResponse(str(int(res[0])) + " " + str(int(res[1])))
    except Exception as e:
        print(e)
        return HttpResponse("Fail")