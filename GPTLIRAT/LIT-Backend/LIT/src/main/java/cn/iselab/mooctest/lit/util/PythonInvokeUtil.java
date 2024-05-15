package cn.iselab.mooctest.lit.util;

import cn.iselab.mooctest.lit.model.StepInfo;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
@Slf4j
@Component
public class PythonInvokeUtil {

    private static final String pythonUtilHost = "http://127.0.0.1:9999/";
    private static final String imageUrl = pythonUtilHost + "image";

    private static final String layoutUrl = pythonUtilHost + "rule/match/layout";
    private static final String semanticUrl=pythonUtilHost+"rule/match/semantic";
    private static final String widgetTypeUrl=pythonUtilHost+"rule/match/widgetType";

    private static final String RULE_SCENE_AUTHORIZATION=pythonUtilHost+"rule/scene/authorization";
    private static final String RULE_SCENE_POLICY=pythonUtilHost+"rule/scene/policy";
    private static final String RULE_SCENE_LOGIN=pythonUtilHost+"rule/scene/login";
    private static final String RULE_SCENE_GUIDE=pythonUtilHost+"rule/scene/guide";
    private static final String RULE_SCENE_DISCOUNT=pythonUtilHost+"rule/scene/discount";
    private static final String RULE_SCENE_CLOSE=pythonUtilHost+"rule/scene/close";

    private static final String RULE_SEARCH_OPTION=pythonUtilHost+"rule/search/option";
    private static final String RULE_SEARCH_DOWN_HIDE=pythonUtilHost+"rule/search/downHide";

    public static final String CHATGPT_CONVERSATION="http://127.0.0.1:8000/chatGPTServer";
    public static final String CHATGPT_PREPRCESS=pythonUtilHost+"pre_chatGPT";


    public static String imageInvoke(String recordWidgetPath, String recordScreenPath, String replayScreenPath, int x, int y) {
        Map<String, String> param = new HashMap<>();
        param.put("recordWidgetPath", recordWidgetPath);
        param.put("recordScreenPath", recordScreenPath);
        param.put("replayScreenPath", replayScreenPath);
        param.put("x", String.valueOf(x));
        param.put("y", String.valueOf(y));
        // 返回Python-image文件，图像匹配的结果
        return HttpUtils.doGet(imageUrl, param);
    }

    public static String layoutInvoke(String recordScreenPath, String replayScreenPath, String x, String y) {
        Map<String, String> param = new HashMap<>();
        param.put("recordScreenPath", recordScreenPath);
        param.put("replayScreenPath", replayScreenPath);
        param.put("x", x);
        param.put("y", y);
        return HttpUtils.doGet(layoutUrl, param);
    }

    public static String semanticInvoke(String recordWidgetPath, String replayScreenPath, double threshold) {
        Map<String, String> param = new HashMap<>();
        param.put("recordWidgetPath", recordWidgetPath);
        param.put("replayScreenPath", replayScreenPath);
        param.put("threshold", String.valueOf(threshold));
        return HttpUtils.doGet(semanticUrl, param);
    }

    public static String widgetTypeInvoke(String recordWidgetPath, String replayScreenPath,String recordScreenshotPath,String X,String Y) {
        Map<String, String> param = new HashMap<>();
        param.put("recordWidgetPath", recordWidgetPath);
        param.put("replayScreenPath", replayScreenPath);
        param.put("recordScreenPath", recordScreenshotPath);
        param.put("x",X);
        param.put("y",Y);
        return HttpUtils.doGet(widgetTypeUrl, param);
    }

    public static String preChatGPTInvoke(String recordElementPath, String stepPath){
        /**
        * @param recordElementPath: 录制小控件的地址
        * @param replayStepPath: 回放step地址
         */
        Map<String, String> param=new HashMap<>();
        param.put("recordElementPath", recordElementPath);
        param.put("stepPath", stepPath);
        return HttpUtils.doGet(CHATGPT_PREPRCESS,param);
    }

    public static String chatgptInvoke(String recordElementPath, String replayScreenPath){
        /**
         * @param RecordElementPath: 录制小控件的地址
         * @param ReplayScreenshotPath: 回放图片地址
         */
        Map<String, String> param=new HashMap<>();
        param.put("recordElementPath",recordElementPath);
        param.put("replayScreenPath",replayScreenPath);
        return HttpUtils.doGet(CHATGPT_CONVERSATION,param);
    }

    public static String ruleSceneAuthorization(String replayScreenPath){
        Map<String, String> param=new HashMap<>();
        param.put("replayScreenPath",replayScreenPath);
        return HttpUtils.doGet(RULE_SCENE_AUTHORIZATION,param);
    }
    public static String ruleScenePolicy(String replayScreenPath){
        Map<String, String> param=new HashMap<>();
        param.put("replayScreenPath",replayScreenPath);
        return HttpUtils.doGet(RULE_SCENE_POLICY,param);
    }
    public static String ruleSceneLogin(String replayScreenPath){
        Map<String, String> param=new HashMap<>();
        param.put("replayScreenPath",replayScreenPath);
        return HttpUtils.doGet(RULE_SCENE_LOGIN,param);
    }
    public static String ruleSceneGuide(String replayScreenPath){
        Map<String, String> param=new HashMap<>();
        param.put("replayScreenPath",replayScreenPath);
        return HttpUtils.doGet(RULE_SCENE_GUIDE,param);
    }
    public static String ruleSceneDiscount(String replayScreenPath){
        Map<String, String> param=new HashMap<>();
        param.put("replayScreenPath",replayScreenPath);
        return HttpUtils.doGet(RULE_SCENE_DISCOUNT,param);
    }
    public static String ruleSceneClose(String replayScreenPath, double threshold){
        Map<String, String> param=new HashMap<>();
        param.put("replayScreenPath",replayScreenPath);
        param.put("threshold", String.valueOf(threshold));
        return HttpUtils.doGet(RULE_SCENE_CLOSE,param);
    }
    public static String ruleSearchOption(String replayScreenPath, double threshold){
        Map<String, String> param=new HashMap<>();
        param.put("replayScreenPath",replayScreenPath);
        param.put("threshold", String.valueOf(threshold));
        return HttpUtils.doGet(RULE_SEARCH_OPTION,param);
    }

    public static String ruleSearchDownHide(String replayScreenPath){
        Map<String, String> param=new HashMap<>();
        param.put("replayScreenPath",replayScreenPath);
        return HttpUtils.doGet(RULE_SEARCH_DOWN_HIDE,param);
    }
}
