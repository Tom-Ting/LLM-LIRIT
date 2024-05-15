package cn.iselab.mooctest.lit;

import cn.iselab.mooctest.lit.model.StepInfo;
import cn.iselab.mooctest.lit.service.PlayBackService;
import cn.iselab.mooctest.lit.service.RuleService;
import cn.iselab.mooctest.lit.util.DeviceUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;

import static org.mockito.ArgumentMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class experiment {
    @Autowired
    PlayBackService playBackService;
    @SpyBean
    DeviceUtils deviceUtils;
    @Autowired
    RuleService ruleService;

    /**
     * 运行单步匹配测试
     */
    @Test
    public void runExperiment() {
        // 回放截图(绝对路径)
        String replayScreenShotPath="C:\\MyGraduation\\database\\MapLIRATDatabase\\CookidooApp\\script1\\ios\\step1\\screenshot.png";
        // 录制脚本路径(绝对路径)
        String recordScriptPath="C:\\MyGraduation\\database\\MapLIRATDatabase\\CookidooApp\\script1\\android";
        // 录制脚本总数
        int totalScriptCount=10;
        // 对应录制脚本第几步(从1开始计数)
        int stepIndex=1;
        // 不用管
        String serialNo="test";


        Mockito.doNothing().when(deviceUtils).takeScreenshot(any(),any());
        Mockito.doNothing().when(deviceUtils).performTap(anyString(),anyInt(),anyInt());
        Mockito.doNothing().when(deviceUtils).swipe(anyString(), anyString());


        String[] dirsLocation=initDirsLocation(recordScriptPath,totalScriptCount);
        String location = dirsLocation[stepIndex - 1];
        StepInfo stepInfo=new StepInfo(serialNo,location,stepIndex,true);
        stepInfo.setReplayScreenshotPath(replayScreenShotPath);


        playBackService.singleStepReplay(stepInfo,dirsLocation);
    }

    private String[] initDirsLocation(String recordScriptPath,int totalScriptCount){
        String[] res=new String[totalScriptCount];
        for(int i=1;i<=totalScriptCount;i++){
            res[i-1]=recordScriptPath+ File.separator+"step"+i;
        }
        return res;
    }


}
