package cn.iselab.mooctest.lit.common.constant;

public class ApiConstants {

    public static final String HTTP = "http://";
    public static final String HEART_BEAT = "/deviceService/heartbeat";
    public static final String DEVICE_CONNECT_START = "/deviceService/device/start?serialNo={serialNo}&host={host}&port={port}";
    public static final String DEVICE_CONNECT_RESTART = "/deviceService/device/restart?serialNo={serialNo}";
    public static final String DEVICE_CONNECT_CLOSE = "/deviceService/device/close?serialNo={serialNo}";
    public static final String DEVICE_RECORD_CLICK = "/deviceService/device/record/click" +
            "?serialNo={serialNo}&scriptName={scriptName}&stepIndex={stepIndex}&x={x}&y={y}";
    public static final String DEVICE_RECORD_COMPLETE = "/deviceService/device/record/complete?serialNo={serialNo}&scriptId={scriptId}";
    public static final String DEVICE_RECORD_PLAYBACK = "/deviceService/device/record/playback?serialNo={serialNo}";
    public static final String DEVICE_TAP = "/deviceService/device/execute/tap?serialNo={serialNo}&x={x}&y={y}";
    public static final String DEVICE_SWIPE = "/deviceService/device/execute/swipe?serialNo={serialNo}&direction={direction}";

    private ApiConstants() {
    }
}
