package cn.iselab.mooctest.device.common.constant;

public class PathConstants {

    public static final String REMOTE_XML_PATH = "/data/local/tmp/ui.xml";
    public static final String REMOTE_DEVICE_PATH = "/data/local/tmp";
//    private static final String ROOT = System.getProperty("user.dir");
    private static final String ROOT = System.getProperty("user.dir");
    private static final String SDK_ROOT=ROOT+"/sdk";
    public static final String IOS_MINICAP_PATH = SDK_ROOT + "/ios-minicap";
    public static final String IOS_WDA_PATH = SDK_ROOT + "/ios-WebDriverAgent/WebDriverAgent.xcodeproj";
//    public static final String ANDROID_PATH = ROOT + "/android-sdk-linux/";
    public static final String ANDROID_PATH = SDK_ROOT + "/android-sdk-windows/";

    public static final String COMPONENT_STORAGE_PATH = ROOT + "/scriptStep";
    public static final String IPHONE_TYPE2RESOLUTION_PATH = SDK_ROOT + "/IphoneType2Resolution.txt";

    private PathConstants() {

    }

}
