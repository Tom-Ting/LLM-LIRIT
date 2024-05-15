package cn.iselab.mooctest.lit;

import cn.iselab.mooctest.lit.model.StepInfo;
import cn.iselab.mooctest.lit.service.PlayBackService;
import cn.iselab.mooctest.lit.service.RuleService;
import cn.iselab.mooctest.lit.util.DeviceUtils;
import cn.iselab.mooctest.lit.util.OSUtil;
import cn.iselab.mooctest.lit.util.PythonInvokeUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static cn.iselab.mooctest.lit.util.PythonInvokeUtil.imageInvoke;
import static org.mockito.ArgumentMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class experimentMultipleTest {
    @Autowired
    PlayBackService playBackService;
    @SpyBean
    DeviceUtils deviceUtils;
    @Autowired
    RuleService ruleService;
    @Autowired
    OSUtil osUtil;
    private static final String serialNo = "test";

    @Test
    public void run() {
        init();
        // (绝对路径)
        String androidScriptPath = "C:\\MyGraduation\\MyDataset\\08_Outlook\\script5\\android";
        // (绝对路径)
        String iosScriptPath = "C:\\MyGraduation\\MyDataset\\08_Outlook\\script5\\ios";
        int[] androidStep = new int[]{1,2,3,4,5,6,7,8,9,10,11,12};
        int[] iosStep = new int[]    {1,2,3,4,5,6,7,8,9,10,11,12};
        int androidTotalScriptCount = 12;
        int iosTotalScriptCount = 12;

        long startTime = System.currentTimeMillis();
//        // 安卓录制，ios回放
        System.out.println("开始  安卓---->ios 放回实验---------------------------------------------");
        for (int i = 0; i < iosStep.length; i++) {
            System.out.printf("开始   安卓第%d步--->ios第%d步   回放%n", androidStep[i], iosStep[i]);
            String replayScreenShotPath = iosScriptPath + "\\step" + iosStep[i] + "\\screenshot.png";
            String[] dirsLocation = initDirsLocation(androidScriptPath, androidTotalScriptCount);
            singleExperiment(replayScreenShotPath, dirsLocation, androidStep[i]);
            String replayInfoPath = iosScriptPath + "\\step" + iosStep[i] + "\\info.json";
            referenceCoordinate(replayInfoPath);
            osUtil.sleep(1);
        }
        long middleTime = System.currentTimeMillis();
        System.out.println("用时" + (middleTime - startTime) / 1000 + "s");
        System.out.println("结束！---------------------------------------------------------");

        // ios录制，安卓回放
        System.out.println("开始  ios---->安卓 放回实验---------------------------------------------");
        for (int i = 0; i < androidStep.length; i++) {
            // 控制台打印提示信息
            System.out.printf("开始   ios第%d步--->安卓第%d步   回放%n", iosStep[i], androidStep[i]);
            // 回放图片路径拼装
            String replayScreenShotPath = androidScriptPath + "\\step" + androidStep[i] + "\\screenshot.png";
            /*  录制图片路径拼装  [根路径]//...//[stepN]
                拼装好后，组装成数组
             */
            String[] dirsLocation = initDirsLocation(iosScriptPath, iosTotalScriptCount);
            // 单步匹配
            singleExperiment(replayScreenShotPath, dirsLocation, iosStep[i]);
            String replayInfoPath = androidScriptPath + "\\step" + androidStep[i] + "\\info.json";
            referenceCoordinate(replayInfoPath);
            osUtil.sleep(1);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("用时" + (endTime - middleTime) / 1000 + "s");
        System.out.println("结束！---------------------------------------------------------");

    }

    /**
     * 运行单步匹配测试
     * @param replayScreenShotPath 回放图片地址
     * @param dirsLocation 录制文件的路径数组
     * @param stepIndex 整型。录制步骤序号
     * return: 无。仅完成操作即可
     */
    public void singleExperiment(String replayScreenShotPath, String[] dirsLocation, int stepIndex) {
        String location = dirsLocation[stepIndex - 1];
        // 将回放图片的路径与录制信息拼在一起，作为StepInfo类型的变量，便于后续操作
        StepInfo stepInfo = new StepInfo(serialNo, location, stepIndex, true);
        stepInfo.setReplayScreenshotPath(replayScreenShotPath);
        playBackService.singleStepReplay(stepInfo, dirsLocation);
    }

    private String[] initDirsLocation(String recordScriptPath, int totalScriptCount) {
        String[] res = new String[totalScriptCount];
        for (int i = 1; i <= totalScriptCount; i++) {
            res[i - 1] = recordScriptPath + File.separator + "step" + i;
        }
        return res;
    }

    private void init() {
        Mockito.doNothing().when(deviceUtils).takeScreenshot(any(), any());
        Mockito.doNothing().when(deviceUtils).performTap(anyString(), anyInt(), anyInt());
        Mockito.doNothing().when(deviceUtils).swipe(anyString(), anyString());

    }

    private void referenceCoordinate(String path) {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            JSONObject info = JSONObject.parseObject(reader.lines().collect(Collectors.joining()));
            int recordX = info.getIntValue("x");
            int recordY = info.getIntValue("y");
            log.info("参考坐标为      [{},{}]", recordX, recordY);
        } catch (IOException e) {
            log.error("Failed to open script info file.", e);
            log.info("出错的文件路径："+path);
        }
    }
}
