package cn.iselab.mooctest.lit.service.impl;

import cn.iselab.mooctest.lit.dao.ScriptDao;
import cn.iselab.mooctest.lit.model.Script;
import cn.iselab.mooctest.lit.model.StepInfo;
import cn.iselab.mooctest.lit.service.PlayBackService;
import cn.iselab.mooctest.lit.service.RuleService;
import cn.iselab.mooctest.lit.util.DeviceUtils;
import cn.iselab.mooctest.lit.util.MatchUtils;
import cn.iselab.mooctest.lit.util.OSUtil;
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

import static cn.iselab.mooctest.lit.util.PythonInvokeUtil.imageInvoke;

@Service
@Slf4j
public class PlayBackServiceImpl implements PlayBackService {
    @Autowired
    ScriptDao scriptDao;
    @Autowired
    DeviceUtils deviceUtils;
    @Autowired
    RuleService ruleService;
    @Autowired
    MatchUtils matchUtils;
    @Autowired
    OSUtil osUtil;
    @Value("${replay.sleep.seconds}")
    private int replaySleepSeconds;
    @Value("${replay.max.trials}")
    private int maxTrials;
    @Value("${replay.weight.x}")
    private double xWeight;
    @Value("${replay.weight.y}")
    private double yWeight;
    private final static Pattern LOCATION_PATTERN = Pattern.compile("(\\d+) (\\d+)");
    private final static Pattern THREE_D_COORDINATE_PATTERN = Pattern.compile("(\\d+) (\\d+) (\\d+)");
    /**
     *
     * @param scriptId
     * @param serialNo
     * @param fromStep
     * @return
     */
    @Override
    public JSONObject doReplay(Long scriptId, String serialNo, int fromStep) {
        Script script = scriptDao.get(scriptId);
        String[] dirsLocation = script.getDirsLocation().split(","); // Step info path.
        int stepIndex = fromStep;
        boolean isSuccessful = true;
        for (; stepIndex <= dirsLocation.length; stepIndex++) {
            String location = dirsLocation[stepIndex - 1];
            StepInfo stepInfo = new StepInfo(serialNo, location, stepIndex,false);
            // 用nextstep来控制循环
            // 由于nextstep未来会被先+1再使用，故此处实际为 下一次匹配的序号-1
            int nextstep = singleStepReplay(stepInfo, dirsLocation);
            if (nextstep == -1) {
                isSuccessful = false;
                break;
            } else {
                // 每轮循环结束后，stepIndex++。因此在下一轮循环中stepIndex = nextstep+1
                stepIndex = nextstep;
            }
        }
        return new JSONObject()
                .fluentPut("success", isSuccessful)
                .fluentPut("next_step", isSuccessful ? 0 : stepIndex);
    }

    /**
     * 把每个step的匹配抽出来，方便后续做实验。匹配到的坐标值使用deviceUtils.performTap方法进行保存
     * 控制台输出匹配结果
     *
     * @param stepInfo 录制信息，数据类型为StepInfo
     * @param dirsLocation 某个数据的地址数组
     * @return 返回值当前步骤信息
     *          -1 代表匹配失败
     */
    public int singleStepReplay(StepInfo stepInfo, String[] dirsLocation) {
        osUtil.logAndMessage(String.format("开始第%d步脚本的回放",stepInfo.getStepIndex()));
        // finalPoint : [x, y] 二维数组，表示匹配到的最终结果。如果为null，则表示未匹配到结果
        int[] finalPoint = null;
        int trial = 0;
        // *****匹配*****
        // 执行图像匹配（第一部分）
        /*
            这里是一个循环。循环的跳出条件有2个
            一是，匹配的结果finalPoint为null
            二是，匹配的次数大于最大尝试次数maxTrials
        */
        osUtil.logAndMessage("## 图象匹配模块开始 ##");
        while (finalPoint == null && trial < maxTrials) {
            trial++;
            finalPoint = imageMatch(stepInfo);
            osUtil.sleep(replaySleepSeconds);
        }
        if (finalPoint == null) {
            // 执行图像匹配（第二部分）
            finalPoint = ruleService.imageRulesMatch(stepInfo);
        }
        if (finalPoint == null) {
            // 执行文本匹配
            osUtil.logAndMessage("## 文本匹配模块开始 ##");
            finalPoint = ruleService.textRulesMatch(stepInfo);
        }
        // 对匹配的结果进行判断
        if (finalPoint != null) {
            // 将匹配结果固定下来
            try {
                osUtil.logAndMessage("匹配结果为[" + finalPoint[0] +", " + finalPoint[1] +"]");
                deviceUtils.performTap(stepInfo.getSerialNo(), finalPoint[0], finalPoint[1]);
                osUtil.sleep(replaySleepSeconds);
            } catch (Exception e) {
                osUtil.logAndMessage("结果固定捕捉到了异常！");
                osUtil.logAndMessage(String.valueOf(finalPoint.length));
                e.printStackTrace();
            }
            return stepInfo.getStepIndex();
        }

        osUtil.logAndMessage("## ChatGPT辅助匹配模块启动 ##");
        // 此处加入数据预处理的服务
        finalPoint = ruleService.chatGPTServerMatch(stepInfo);
        if (Arrays.equals(finalPoint, new int[]{-1, -1})){
            // 根据返回数据的格式化信息，此为录制冗余
            log.info("ChatGPT辅助匹配模块:录制冗余，跳过当前录制步骤");
            return stepInfo.getStepIndex();
        }
        if (finalPoint != null){
            log.info("ChatGPT辅助匹配模块:回放冗余。在回放界面无对应控件，可操作[{}, {}]进入后续流程", finalPoint[0], finalPoint[1]);
        }



        /**
        // *****多对少的冗余抛弃*****
        int nextStep = ruleService.redundantStepDetermination(dirsLocation, stepInfo.getStepIndex(), stepInfo);
        if (nextStep != -1) {
            // 返回正确的录制步骤。由于后面要+1使用，故此处-1预处理
            return nextStep - 1;
        }

        boolean find = false;
        // *****少对多的场景和探索匹配*****
        // 场景匹配
        find = ruleService.executeSceneRules(stepInfo);
        //探索匹配
        if (!find) {
            find = ruleService.executeSearchRules(stepInfo);
        }
        if (find) {
            // 重新执行该步脚本
            return stepInfo.getStepIndex() - 1;
        } else {
            return -1;
        }
         */
        return -1;
    }

    /**
     * 综合匹配
     *
     * @param
     * @return
     */
    public int[] imageMatch(StepInfo stepInfo) {
        String replayScreenshotPath = stepInfo.getReplayScreenshotPath();
        String serialNo = stepInfo.getSerialNo();
        deviceUtils.takeScreenshot(replayScreenshotPath, serialNo);
        String recordElementPath = stepInfo.getRecordElementPath();
        String infoPath = stepInfo.getInfoPath();
        String recordScreenshotPath = stepInfo.getRecordScreenshotPath();
        int[] pointByImage = null;

        try (BufferedReader reader = new BufferedReader(new FileReader(infoPath))) {
            JSONObject info = JSONObject.parseObject(reader.lines().collect(Collectors.joining()));
            int recordX = info.getIntValue("x");
            int recordY = info.getIntValue("y");
            // 返回图像匹配的结果
            String result = imageInvoke(recordElementPath, recordScreenshotPath, replayScreenshotPath, recordX, recordY);
//            osUtil.logAndMessage(String.format("imageMatch方法: imageInvoke返回值为 " + result));
            Matcher m = LOCATION_PATTERN.matcher(result);
            if (m.matches()) {
                int x = Integer.parseInt(m.group(1));
                int y = Integer.parseInt(m.group(2));
                pointByImage = new int[]{x, y};
            }
        } catch (IOException e) {
            log.info("Error File Path: "+ infoPath);
            log.error("Failed to open script info file.", e);
        }
        if (pointByImage != null) {
            osUtil.logAndMessage(String.format("imageMatch方法: 匹配结果为[%d,%d]",pointByImage[0], pointByImage[1]));
        }
        return pointByImage;
    }
}
