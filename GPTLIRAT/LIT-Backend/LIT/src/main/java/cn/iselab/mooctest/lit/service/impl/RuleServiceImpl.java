package cn.iselab.mooctest.lit.service.impl;

import cn.iselab.mooctest.lit.model.StepInfo;
import cn.iselab.mooctest.lit.service.RecordService;
import cn.iselab.mooctest.lit.service.RuleService;
import cn.iselab.mooctest.lit.util.DeviceUtils;
import cn.iselab.mooctest.lit.util.OSUtil;
import cn.iselab.mooctest.lit.util.PythonInvokeUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.io.File;

@Service
@Slf4j
public class RuleServiceImpl implements RuleService {

    private final static Pattern LOCATION_PATTERN = Pattern.compile("(\\d+) (\\d+)");
    private final static Pattern THREE_D_COORDINATE_PATTERN = Pattern.compile("(\\d+) (\\d+) (\\d+)");

    private final static Double SEMANTIC_THRESHOLD = 0.8;
    @Value("${replay.max.redundancy}")
    private int maxRedundancy;

    @Value("${experiment}")
    private boolean isExperiment;

    @Autowired
    PlayBackServiceImpl playBackService;

    @Autowired
    DeviceUtils deviceUtils;

    @Autowired
    OSUtil osUtil;

    @Autowired
    RecordService recordService;

    @Value("${replay.sleep.seconds}")
    private int replaySleepSeconds;

    @Override
    public int[] executeMatchRules(StepInfo stepInfo) {
        osUtil.logAndMessage("开始匹配类规则");
        // 先截个图
        deviceUtils.takeScreenshot(stepInfo.getReplayScreenshotPath(), stepInfo.getSerialNo());
        int[] point = null;
        // 图标类型匹配
        point = point == null ? widgetTypeMatch(stepInfo) : point;
        // 布局匹配
        point = point == null ? layout(stepInfo) : point;
        // 语义匹配
        point = point == null ? semanticMatch(stepInfo) : point;
        if (point != null) {
            osUtil.logAndMessage("匹配类规则执行成功");
            return point;
        }
        osUtil.logAndMessage("匹配类规则执行失败");
        return null;
    }

    /**
     * 丁自民的图像匹配（第二部分）
     *
     * @param stepInfo
     * @return
     */
    @Override
    public int[] imageRulesMatch(StepInfo stepInfo){
        // 先截个图
        deviceUtils.takeScreenshot(stepInfo.getReplayScreenshotPath(), stepInfo.getSerialNo());
        int[] point = null;
        // 图标类型匹配
        point = point == null ? widgetTypeMatch(stepInfo) : point;
        // 布局匹配
        point = point == null ? layout(stepInfo) : point;
        if (point != null) {
            osUtil.logAndMessage("图像规则匹配执行成功");
            return point;
        }
        osUtil.logAndMessage("图像规则匹配执行失败");
        return null;
    }

    /**
     * 丁自民的文本匹配
     *
     * @param stepInfo
     * @return
     */
    @Override
    public int[] textRulesMatch(StepInfo stepInfo){
        // 先截个图
//        deviceUtils.takeScreenshot(stepInfo.getReplayScreenshotPath(), stepInfo.getSerialNo());
        // 语义匹配
        int[] point = semanticMatch(stepInfo);
        if (point != null) {
            osUtil.logAndMessage("文本匹配执行成功");
            return point;
        }
        osUtil.logAndMessage("文本匹配执行失败");
        return null;
    }

    /**
     * 场景匹配
     *
     * @param stepInfo
     * @return
     */
    @Override
    public boolean executeSceneRules(StepInfo stepInfo) {
        osUtil.logAndMessage("开始场景类规则");
        int[] target = null;
        target = target == null ? authorizationScene(stepInfo) : target;
        target = target == null ? policyScene(stepInfo) : target;
        target = target == null ? loginScene(stepInfo) : target;
        target = target == null ? guideScene(stepInfo) : target;
        target = target == null ? discountScene(stepInfo) : target;
        target = target == null ? closeScene(stepInfo) : target;
        if (target != null) {
            osUtil.logAndMessage("场景类规则成功，操作场景转跳目标");
            deviceUtils.performTap(stepInfo.getSerialNo(), target[0], target[1]);
            osUtil.sleep(replaySleepSeconds);
            return true;
        } else {
            osUtil.logAndMessage("场景类规则失败");
            return false;
        }
    }

    /**
     * 探索匹配
     *
     * @param stepInfo
     * @return
     */
    @Override
    public boolean executeSearchRules(StepInfo stepInfo) {
        osUtil.logAndMessage("开始探索类规则");
        int[] target = null;
        // 探索，寻找options和下拉箭头
        target = target == null ? optionSearch(stepInfo) : target;
        target = target == null ? downHideSearch(stepInfo) : target;

        if (target != null) {
            osUtil.logAndMessage("探索类规则成功，操作场景转跳目标");
            deviceUtils.performTap(stepInfo.getSerialNo(), target[0], target[1]);
            osUtil.sleep(replaySleepSeconds);
            return true;
        }

        // 滑动，无需操作
        boolean res = swipeToSearch(stepInfo);
        if (res) {
            osUtil.logAndMessage("探索类规则成功，已进行屏幕滑动，并暴露目标");
            return true;
        }
        osUtil.logAndMessage("探索类规则失败");
        return false;
    }

    /**
     * 判断是否存在冗余步骤，若存在，则返回正确的下一步index，若不存在，返回-1
     *
     * @param dirsLocation
     * @param ind 录制步骤序号
     * @param stepInfo 录制步骤信息
     * @return
     */
    public int redundantStepDetermination(String[] dirsLocation, int ind, StepInfo stepInfo) {
        // 对后续的maxRedundancy步骤进行验证
        osUtil.logAndMessage("开始冗余步骤检测");
        // boolean absolute = isExperiment;
        boolean absolute = true;
        for (int i = ind + 1; i <= ind + maxRedundancy && i <= dirsLocation.length; i++) {
            StepInfo curStepInfo = new StepInfo(stepInfo.getSerialNo(), dirsLocation[i - 1], i, absolute);
            if(isExperiment){
                // 为了做实验
                curStepInfo.setReplayScreenshotPath(stepInfo.getReplayScreenshotPath());
            }
            int[] res = playBackService.imageMatch(curStepInfo);
            if (res != null) {
                osUtil.logAndMessage(String.format("存在冗余步骤, 转跳至步骤%d", i));
                return i;
            }
        }
        osUtil.logAndMessage("无冗余步骤");
        return -1;
    }


    private int[] semanticMatch(StepInfo stepInfo) {
        // osUtil.logAndMessage("开始语义匹配");
        log.info("匹配类规则——调用semanticMatch方法");
        String result = PythonInvokeUtil.semanticInvoke(stepInfo.getRecordElementPath(), stepInfo.getReplayScreenshotPath(), SEMANTIC_THRESHOLD);
        Matcher locationMatcher = LOCATION_PATTERN.matcher(result);
        if (locationMatcher.matches()) {
            osUtil.logAndMessage(String.format("文本匹配成功，找到坐标点为(%s,%s).", locationMatcher.group(1), locationMatcher.group(2)));
            return new int[]{
                    Integer.parseInt(locationMatcher.group(1)),
                    Integer.parseInt(locationMatcher.group(2))
            };
        } else {
            osUtil.logAndMessage("匹配类规则——调用semanticMatch方法未匹配到结果");
            return null;
        }
    }

    private int[] layout(StepInfo stepInfo) {
        // osUtil.logAndMessage("开始布局匹配");
        log.info("匹配类规则——调用layout方法");
        try (BufferedReader reader = new BufferedReader(new FileReader(stepInfo.getInfoPath()))) {
            JSONObject info = JSONObject.parseObject(reader.lines().collect(Collectors.joining()));
            int recordX = info.getIntValue("x");
            int recordY = info.getIntValue("y");
            String result = PythonInvokeUtil.layoutInvoke(stepInfo.getRecordScreenshotPath(), stepInfo.getReplayScreenshotPath(),
                    String.valueOf(recordX), String.valueOf(recordY));
            Matcher locationMatcher = LOCATION_PATTERN.matcher(result);
            if (locationMatcher.matches()) {
//                osUtil.logAndMessage(String.format("布局匹配成功，找到坐标点为(%s,%s).", locationMatcher.group(1), locationMatcher.group(2)));
                osUtil.logAndMessage(String.format("图像匹配成功，找到坐标点为(%s,%s).", locationMatcher.group(1), locationMatcher.group(2)));
                return new int[]{
                        Integer.parseInt(locationMatcher.group(1)),
                        Integer.parseInt(locationMatcher.group(2))
                };
            } else {
                osUtil.logAndMessage("匹配类规则——调用layout方法未匹配到结果");
            }

            return null;
        } catch (IOException e) {
            log.error("Failed to open script info file.", e);
        }
        return null;
    }

    private int[] widgetTypeMatch(StepInfo stepInfo) {
        // osUtil.logAndMessage("开始图标类型匹配");
        log.info("匹配类规则——调用widgetTypeMatch方法");
        try (BufferedReader reader = new BufferedReader(new FileReader(stepInfo.getInfoPath()))) {
            JSONObject info = JSONObject.parseObject(reader.lines().collect(Collectors.joining()));
            int recordX = info.getIntValue("x");
            int recordY = info.getIntValue("y");
            String result = PythonInvokeUtil.widgetTypeInvoke(stepInfo.getRecordElementPath(), stepInfo.getReplayScreenshotPath(),stepInfo.getRecordScreenshotPath(),String.valueOf(recordX),String.valueOf(recordY));
            System.out.println(result);
            Matcher locationMatcher = LOCATION_PATTERN.matcher(result);
            if (locationMatcher.matches()) {
//                osUtil.logAndMessage(String.format("图标类型成功，找到坐标点为(%s,%s).", locationMatcher.group(1), locationMatcher.group(2)));
                osUtil.logAndMessage(String.format("widgetTypeMatch成功，找到坐标点为(%s,%s).", locationMatcher.group(1), locationMatcher.group(2)));
                return new int[]{
                        Integer.parseInt(locationMatcher.group(1)),
                        Integer.parseInt(locationMatcher.group(2))
                };
            } else {
                // osUtil.logAndMessage("图标类型匹配失败");
                log.info("匹配类规则——widgetTypeMatch方法未匹配到结果");
                return null;
            }
        }catch (IOException e){
            log.error("Failed to open script info file.", e);
        }
        return null;
    }

    public int[] chatGPTServerMatch(StepInfo stepInfo) {
        // osUtil.logAndMessage("开始语义匹配");
        log.info("chatGPT辅助测试模块——调用chatGPTServerMatch方法");
        log.info("chatGPT模块数据预处理！");
        String recordElementPath = stepInfo.getRecordElementPath();
        String replayStepPath = stepInfo.getReplayScreenshotPath().replace("\\screenshot.png","");
        PythonInvokeUtil.preChatGPTInvoke(recordElementPath, replayStepPath);
        String result = PythonInvokeUtil.chatgptInvoke(stepInfo.getRecordElementPath(), stepInfo.getReplayScreenshotPath());
        log.info("chatGPT服务返回： " + result);
        if (result.equals("-1 -1")){
            osUtil.logAndMessage("ChatGPT辅助匹配模块——录制冗余，即跳过当前录制步骤");
            return null;
        }
        Matcher locationMatcher = LOCATION_PATTERN.matcher(result);
        if (locationMatcher.matches()) {
            osUtil.logAndMessage(String.format("ChatGPT辅助匹配成功，找到坐标点为(%s,%s).", locationMatcher.group(1), locationMatcher.group(2)));
            return new int[]{
                    Integer.parseInt(locationMatcher.group(1)),
                    Integer.parseInt(locationMatcher.group(2))
            };
        } else {
            osUtil.logAndMessage("ChatGPT辅助匹配模块——调用chatGPTServerMatch方法未匹配到结果");
            return null;
        }
    }

    private int[] authorizationScene(StepInfo stepInfo) {
        String result = PythonInvokeUtil.ruleSceneAuthorization(stepInfo.getReplayScreenshotPath());
        Matcher locationMatcher = LOCATION_PATTERN.matcher(result);
        if (locationMatcher.matches()) {
            osUtil.logAndMessage(String.format("授权场景匹配成功，找到坐标点为(%s,%s).", locationMatcher.group(1), locationMatcher.group(2)));
            return new int[]{
                    Integer.parseInt(locationMatcher.group(1)),
                    Integer.parseInt(locationMatcher.group(2))
            };
        } else {
            osUtil.logAndMessage("授权场景匹配失败");
        }
        return null;
    }

    private int[] policyScene(StepInfo stepInfo) {
        String result = PythonInvokeUtil.ruleScenePolicy(stepInfo.getReplayScreenshotPath());
        Matcher locationMatcher = LOCATION_PATTERN.matcher(result);
        if (locationMatcher.matches()) {
            osUtil.logAndMessage(String.format("政策场景匹配成功，找到坐标点为(%s,%s).", locationMatcher.group(1), locationMatcher.group(2)));
            return new int[]{
                    Integer.parseInt(locationMatcher.group(1)),
                    Integer.parseInt(locationMatcher.group(2))
            };
        } else {
            osUtil.logAndMessage("政策场景匹配失败");
        }
        return null;
    }

    private int[] loginScene(StepInfo stepInfo) {
        String result = PythonInvokeUtil.ruleSceneLogin(stepInfo.getReplayScreenshotPath());
        Matcher locationMatcher = LOCATION_PATTERN.matcher(result);
        if (locationMatcher.matches()) {
            osUtil.logAndMessage(String.format("登录场景匹配成功，找到坐标点为(%s,%s).", locationMatcher.group(1), locationMatcher.group(2)));
            return new int[]{
                    Integer.parseInt(locationMatcher.group(1)),
                    Integer.parseInt(locationMatcher.group(2))
            };
        } else {
            osUtil.logAndMessage("登录场景匹配失败");
        }
        return null;
    }

    private int[] guideScene(StepInfo stepInfo) {
        String result = PythonInvokeUtil.ruleSceneGuide(stepInfo.getReplayScreenshotPath());
        Matcher locationMatcher = LOCATION_PATTERN.matcher(result);
        if (locationMatcher.matches()) {
            osUtil.logAndMessage(String.format("教程场景匹配成功，找到坐标点为(%s,%s).", locationMatcher.group(1), locationMatcher.group(2)));
            return new int[]{
                    Integer.parseInt(locationMatcher.group(1)),
                    Integer.parseInt(locationMatcher.group(2))
            };
        } else {
            osUtil.logAndMessage("教程场景匹配失败");
        }
        return null;
    }

    private int[] discountScene(StepInfo stepInfo) {
        String result = PythonInvokeUtil.ruleSceneDiscount(stepInfo.getReplayScreenshotPath());
        Matcher locationMatcher = LOCATION_PATTERN.matcher(result);
        if (locationMatcher.matches()) {
            osUtil.logAndMessage(String.format("优惠场景匹配成功，找到坐标点为(%s,%s).", locationMatcher.group(1), locationMatcher.group(2)));
            return new int[]{
                    Integer.parseInt(locationMatcher.group(1)),
                    Integer.parseInt(locationMatcher.group(2))
            };
        } else {
            osUtil.logAndMessage("优惠场景匹配失败");
        }
        return null;
    }

    private int[] closeScene(StepInfo stepInfo) {
        String result = PythonInvokeUtil.ruleSceneClose(stepInfo.getReplayScreenshotPath(), SEMANTIC_THRESHOLD);
        Matcher locationMatcher = LOCATION_PATTERN.matcher(result);
        if (locationMatcher.matches()) {
            osUtil.logAndMessage(String.format("广告场景匹配成功，找到坐标点为(%s,%s).", locationMatcher.group(1), locationMatcher.group(2)));
            return new int[]{
                    Integer.parseInt(locationMatcher.group(1)),
                    Integer.parseInt(locationMatcher.group(2))
            };
        } else {
            osUtil.logAndMessage("广告场景匹配失败");
        }
        return null;
    }

    private boolean swipeToSearch(StepInfo stepInfo) {
        osUtil.logAndMessage("开始滑动屏幕进行探索");
        deviceUtils.swipe(stepInfo.getSerialNo(), "up");
        osUtil.sleep(1);
        deviceUtils.takeScreenshot(stepInfo.getReplayScreenshotPath(), stepInfo.getSerialNo());
        int[] res = playBackService.imageMatch(stepInfo);
        if (null == res) {
            deviceUtils.swipe(stepInfo.getSerialNo(), "down");
            osUtil.sleep(1);
            osUtil.logAndMessage("未寻找到目标，探索失败");
            return false;
        } else {
            osUtil.logAndMessage("寻找到目标，探索成功");
            return true;
        }
    }

    private int[] optionSearch(StepInfo stepInfo) {
        String result = PythonInvokeUtil.ruleSearchOption(stepInfo.getReplayScreenshotPath(), SEMANTIC_THRESHOLD);
        Matcher locationMatcher = LOCATION_PATTERN.matcher(result);
        if (locationMatcher.matches()) {
            osUtil.logAndMessage(String.format("选项搜索匹配成功，找到坐标点为(%s,%s).", locationMatcher.group(1), locationMatcher.group(2)));
            return new int[]{
                    Integer.parseInt(locationMatcher.group(1)),
                    Integer.parseInt(locationMatcher.group(2))
            };
        } else {
            osUtil.logAndMessage("选项搜索匹配失败");
        }
        return null;
    }

    private int[] downHideSearch(StepInfo stepInfo) {
        String result = PythonInvokeUtil.ruleSearchDownHide(stepInfo.getReplayScreenshotPath());
        Matcher locationMatcher = LOCATION_PATTERN.matcher(result);
        if (locationMatcher.matches()) {
            osUtil.logAndMessage(String.format("下拉隐藏搜索匹配成功，找到坐标点为(%s,%s).", locationMatcher.group(1), locationMatcher.group(2)));
            return new int[]{
                    Integer.parseInt(locationMatcher.group(1)),
                    Integer.parseInt(locationMatcher.group(2))
            };
        } else {
            osUtil.logAndMessage("下拉隐藏搜索匹配失败");
        }
        return null;
    }
}
