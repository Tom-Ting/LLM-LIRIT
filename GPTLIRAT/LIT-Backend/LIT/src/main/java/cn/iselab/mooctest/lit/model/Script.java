package cn.iselab.mooctest.lit.model;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "script")
public class Script {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scriptId;

    @Column(name = "device_udid")
    private String deviceUdid;

    @Column(name = "name")
    private String name;

    @Column(name = "current_step")
    private int currentStep = 1;

    @Column(name = "dirs_location")
    private String dirsLocation;

    @Column(name = "create_time_millis")
    private Timestamp createTimeMills = new Timestamp(System.currentTimeMillis());

    @Column(name = "script_url")
    private String scriptUrl;

    @Column(name = "script_step_list")
    private String scriptStepList;

    @Column(name = "app_id")
    private String appId;

    public Long getScriptId() {
        return scriptId;
    }

    public void setScriptId(Long scriptId) {
        this.scriptId = scriptId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
    }

    public String getDirsLocation() {
        return dirsLocation;
    }

    public void setDirsLocation(String dirsLocation) {
        this.dirsLocation = dirsLocation;
    }

    public Timestamp getCreateTimeMills() {
        return createTimeMills;
    }

    public void setCreateTimeMills(Timestamp createTimeMills) {
        this.createTimeMills = createTimeMills;
    }

    public String getDeviceUdid() {
        return deviceUdid;
    }

    public void setDeviceUdid(String deviceUdid) {
        this.deviceUdid = deviceUdid;
    }

    public String getScriptUrl() {
        return scriptUrl;
    }

    public void setScriptUrl(String scriptUrl) {
        this.scriptUrl = scriptUrl;
    }

    public String getScriptStepList() {
        return scriptStepList;
    }

    public void setScriptStepList(String scriptStepList) {
        this.scriptStepList = scriptStepList;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }
}
