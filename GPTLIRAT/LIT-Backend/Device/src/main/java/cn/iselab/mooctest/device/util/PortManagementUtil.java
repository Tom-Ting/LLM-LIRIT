package cn.iselab.mooctest.device.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class PortManagementUtil {

    private static Set<Integer> miniCapPortSet = new HashSet<>();

    private static Set<Integer> miniTouchPortSet = new HashSet<>();

    private static Set<Integer> iosMiniCapPortSet = new HashSet<>();

    private static Set<Integer> iosWdaPortSet = new HashSet<>();

    private PortManagementUtil() {

    }

    public static void init() {
        for (int i = 1300; i < 1400; i++) {
            miniCapPortSet.add(i);
            miniTouchPortSet.add(i + 100);
            iosMiniCapPortSet.add(i + 200);
            //wda port: 8100-8200
            iosWdaPortSet.add(i + 6800);
        }
    }

    public static int useFirstMiniCapPort() {
        int port = new ArrayList<>(miniCapPortSet).get(0);
        miniCapPortSet.remove(port);
        return port;
    }

    public static void recoverMiniCapPort(int port) {
        miniCapPortSet.add(port);
    }

    public static int useFirstMiniTouchPort() {
        int port = new ArrayList<>(miniTouchPortSet).get(0);
        miniTouchPortSet.remove(port);
        return port;
    }

    public static void recoverMiniTouchPort(int port) {
        miniTouchPortSet.add(port);
    }

    public static int useFirstIOSMiniCapPort() {
        int port = new ArrayList<>(iosMiniCapPortSet).get(0);
        iosMiniCapPortSet.remove(port);
        return port;
    }

    public static void recoverIOSMiniCapPort(int port) {
        iosMiniCapPortSet.add(port);
    }

    public static int useFirstIOSWdaPort() {
        int port = new ArrayList<>(iosWdaPortSet).get(0);
        iosWdaPortSet.remove(port);
        return port;
    }

    public static void recoverIOSWdaPort(int port) {
        iosWdaPortSet.add(port);
    }
}


