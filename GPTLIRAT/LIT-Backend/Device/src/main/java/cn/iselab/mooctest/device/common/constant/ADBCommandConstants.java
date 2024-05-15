package cn.iselab.mooctest.device.common.constant;

public class ADBCommandConstants {

    public static final String GET_WM_SIZE = "wm size";
    public static final String MINICAP_START = "LD_LIBRARY_PATH=/data/local/tmp /data/local/tmp/%s -P %s@%s/%d";
    public static final String MINITOUCH_START = "/data/local/tmp/%s";
    public static final String CHMOD_COMMAND = "chmod 777 %s/%s";
    public static final String MINICAP_INSTALLED = "LD_LIBRARY_PATH=/data/local/tmp /data/local/tmp/minicap -P %s@%s/%d -i";
    public static final String DUMP_UI_XML = "uiautomator dump " + PathConstants.REMOTE_XML_PATH;
    public static final String TAKE_SCREENSHOT = "screencap -p /data/local/tmp/%s";
    public static final String GET_ACTIVITY = "dumpsys activity";
    public static final String SWIP="input swipe %d %d %d %d %d";

    private ADBCommandConstants() {
    }

}
