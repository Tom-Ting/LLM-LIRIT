package cn.iselab.mooctest.device.common.constant;

import java.io.File;

public class MiniToolConstants {

    public static final String MINICAP_BIN = "minicap";
    public static final String MINITOUCH_BIN = "minitouch";
    public static final String MINICAP_NOPIE = "minicap-nopie";
    public static final String MINITOUCH_NOPIE = "minitouch-nopie";
    public static final String MINICAP_SO = "minicap.so";
    private static final String ROOT = System.getProperty("user.dir")+"/sdk";

    private MiniToolConstants() {
    }

    public static File getMinicapBin() {
        return new File(ROOT, "android-minicap/bin");
    }

    public static File getMiniTouchBin() {
        return new File(ROOT, "android-minitouch");
    }

    public static File getMinicapSo() {
        return new File(ROOT, "android-minicap/shared");
    }
}