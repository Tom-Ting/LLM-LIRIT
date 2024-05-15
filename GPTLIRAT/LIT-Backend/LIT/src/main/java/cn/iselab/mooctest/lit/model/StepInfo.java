package cn.iselab.mooctest.lit.model;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Properties;

@Data
public class StepInfo {
    private String serialNo; // 步骤序号
    private String location; // 操作控件的位置，格式为 str(x)+" "+str(y)
    private String replayBaseDirPath; // 回放图片的基底目录
    private String replayScreenshotPath; // 回放图片的目录
    private String recordElementPath; // 录制控件的目录
    private String recordScreenshotPath; // 录制图片的目录
    private String infoPath; // 正确控件的位置（作为参考）
    private int stepIndex; // 步骤序号

    private String hint;

    private StepInfo() {
    }


    /**
     *
     * @param serialNo
     * @param location
     * @param stepIndex
     * @param expirement 输入的路径(location)是否为绝对路径
     */
    public StepInfo(String serialNo, String location, int stepIndex, boolean expirement) {
        this.serialNo = serialNo;
        this.stepIndex = stepIndex;
        String root = "";
        if (!expirement) {
            // 转换成绝对路径
            root = System.getProperty("user.dir") + File.separator;
            this.location = root + location;
            // Constant.replaysPath="replays"
            this.replayBaseDirPath = root + Constant.replaysPath + File.separator + serialNo;
            this.replayScreenshotPath = this.replayBaseDirPath + File.separator + "fullscreen.png";
            this.recordElementPath = this.location + File.separator + "element.png";
            this.recordScreenshotPath = this.location + File.separator + "screenshot.png";
            this.infoPath = this.location + File.separator + "info.json";
        }else{
            this.location = location;
            // Constant.replaysPath="replays"
            this.replayBaseDirPath = root + Constant.replaysPath + File.separator + serialNo;
            this.replayScreenshotPath = this.replayBaseDirPath + File.separator + "fullscreen.png";
            this.recordElementPath = this.location + File.separator + "element.png";
            this.recordScreenshotPath = this.location + File.separator + "screenshot.png";
            this.infoPath = this.location + File.separator + "info.json";
        }
    }

    public static void main(String[] args) {
        System.out.println(System.getProperty("user.dir"));
    }
}
