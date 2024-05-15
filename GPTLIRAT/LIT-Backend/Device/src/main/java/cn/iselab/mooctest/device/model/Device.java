package cn.iselab.mooctest.device.model;

public class Device {

    private String serialNumber;
    private String marketingName;
    private String brand;
    private String model;
    private String version;
    private String resolution;
    private String abi;
    private String sdk;
    private int forwardMiniCapPort = 0;
    private int forwardMiniTouchPort = 0;
    private int forwardWdaPort = 0;
    private String wdaSession;
    private int status;

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getMarketingName() {
        return marketingName;
    }

    public void setMarketingName(String marketingName) {
        this.marketingName = marketingName;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getAbi() {
        return abi;
    }

    public void setAbi(String abi) {
        this.abi = abi;
    }

    public String getSdk() {
        return sdk;
    }

    public void setSdk(String sdk) {
        this.sdk = sdk;
    }

    public int getForwardMiniCapPort() {
        return forwardMiniCapPort;
    }

    public void setForwardMiniCapPort(int forwardMiniCapPort) {
        this.forwardMiniCapPort = forwardMiniCapPort;
    }

    public int getForwardMiniTouchPort() {
        return forwardMiniTouchPort;
    }

    public void setForwardMiniTouchPort(int forwardMiniTouchPort) {
        this.forwardMiniTouchPort = forwardMiniTouchPort;
    }

    public int getForwardWdaPort() {
        return forwardWdaPort;
    }

    public void setForwardWdaPort(int forwardWdaPort) {
        this.forwardWdaPort = forwardWdaPort;
    }

    public String getWdaSession() {
        return wdaSession;
    }

    public void setWdaSession(String wdaSession) {
        this.wdaSession = wdaSession;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
