"""AlgorithmServer URL Configuration

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/3.2/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('blog/', include('blog.urls'))
"""
from django.contrib import admin
from django.urls import path

from AlgorithmServer.views.rules.match import layout, image, semantic
from AlgorithmServer.views.rules.match.widget_type import rule_widget_type_views
from AlgorithmServer.views.rules.scene import authorization, policy
from AlgorithmServer.views.rules.scene.discount import rule_discount_views
from AlgorithmServer.views.rules.scene.guide import rule_guide_views
from AlgorithmServer.views.rules.scene.login import rule_login_views
from AlgorithmServer.views.rules.scene.close import rule_close_views
from AlgorithmServer.views.rules.search.down_hide import rule_down_hide_views
from AlgorithmServer.views.rules.search.option import rule_option_views

from AlgorithmServer.utils import pre_chatgpt

urlpatterns = [
    path('admin/', admin.site.urls),
    path('image/', image.image),

    path('rule/match/layout/', layout.layout),
    path('rule/match/semantic/', semantic.entrance),
    path('rule/match/widgetType', rule_widget_type_views),

    path('rule/scene/authorization', authorization.rule_authorization_views),
    path('rule/scene/policy', policy.rule_policy_views),
    path('rule/scene/login', rule_login_views),
    path('rule/scene/guide', rule_guide_views),
    path('rule/scene/discount', rule_discount_views),
    path('rule/scene/close', rule_close_views),

    path('rule/search/option', rule_option_views),
    path('rule/search/downHide', rule_down_hide_views),

    path('pre_chatGPT/', pre_chatgpt.entrance),
]
