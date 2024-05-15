package cn.iselab.mooctest.device.common.constant;

public class IOSCommandConstants {
    public static final String IDEVICE_ID_LIST_COMMAND = "idevice_id -l";
    private static final String IDEVICE_INFO_COMMAND = "ideviceinfo -u ";
    private static final String IDEVICE_INFO_PRODUCT_TYPE_COMMAND = "ideviceinfo -k ProductType -u ";
    private static final String IDEVICE_INSTALLER_COMMAND = "ideviceinstaller -u ";
    private static final String IDEVICE_SCREENSHOT_COMMAND = "idevicescreenshot -u ";
    private static final String IOS_MINICAP_WDAPROXY_PORT_COMMAND = "lsof -i:port |grep LISTEN|awk '{print $2}'";
    private static final String IOS_WDA_PORT_COMMAND = " ps -ef | grep \"id=deviceId\" |head -1|awk '{print $2}'";
    private static final String IOS_WDA_COMMAND = "/usr/bin/xcodebuild test-without-building -project " + PathConstants.IOS_WDA_PATH + " -scheme WebDriverAgentRunner -destination id=deviceId USE_PORT=8100 test";
    private static final String IOS_WDA_PROXY_COMMAND = "wdaproxy -u deviceId -p port";
    private static final String IOS_WDA_SESSION_COMMAND = "http://localhost:port/status";
    private static final String IOS_WDA_TAP_COMMAND = "http://localhost:port/session/sessionId/wda/tap/0";
    private static final String IOS_WDA_HOME_COMMAND = "http://localhost:port/wda/homescreen";
    private static final String IOS_WDA_WINDOW_SIZE_COMMAND = "http://localhost:port/session/sessionId/window/size";
    private static final String IOS_WDA_SWIPE_COMMAND = "http://localhost:port/session/sessionId/wda/dragfromtoforduration";


    private IOSCommandConstants() {
    }

    public static final String ideviceInstaller(String udid, String ipaPath) {
        return IDEVICE_INSTALLER_COMMAND + udid + " -i " + ipaPath;
    }

    public static final String ideviceScreenShot(String udid, String outputPath) {
        return IDEVICE_SCREENSHOT_COMMAND + udid + " " + outputPath;
    }

    public static final String getIOSMatchMinicapOrWdaProxyPort(int port) {
        return IOS_MINICAP_WDAPROXY_PORT_COMMAND.replace("port", String.valueOf(port));
    }

    public static final String getIOSMatchWdaPort(String deviceId) {
        return IOS_WDA_PORT_COMMAND.replace("deviceId", deviceId);
    }

    public static final String getIOSWdaStartCommand(String deviceId) {
        return IOS_WDA_COMMAND.replace("deviceId", deviceId);
    }

    public static final String getIOSWdaProxyStartCommand(String deviceId, int port) {
        return IOS_WDA_PROXY_COMMAND.replace("deviceId", deviceId)
                .replace("port", String.valueOf(port));
    }

    public static final String getWdaStatusCommand(int port) {
        return IOS_WDA_SESSION_COMMAND.replace("port", String.valueOf(port));
    }

    public static final String getWdaTapCommand(String seesionId, int port) {
        return IOS_WDA_TAP_COMMAND.replace("port", String.valueOf(port))
                .replace("sessionId", seesionId);
    }

    public static final String getWdaSwipeCommand(String seesionId, int port) {
        return IOS_WDA_SWIPE_COMMAND.replace("port", String.valueOf(port))
                .replace("sessionId", seesionId);
    }

    public static final String getWdaHomeCommand(int port) {
        return IOS_WDA_HOME_COMMAND.replace("port", String.valueOf(port));
    }

    public static final String getWdaWindowSizeCommand(int port, String sessionId) {
        return IOS_WDA_WINDOW_SIZE_COMMAND
                .replace("port", String.valueOf(port))
                .replace("sessionId", sessionId);
    }

    public static final String ideviceInfo(String udid) {
        return IDEVICE_INFO_COMMAND + udid;
    }

    public static final String ideviceProductType(String udid) {
        return IDEVICE_INFO_PRODUCT_TYPE_COMMAND + udid;
    }

}
