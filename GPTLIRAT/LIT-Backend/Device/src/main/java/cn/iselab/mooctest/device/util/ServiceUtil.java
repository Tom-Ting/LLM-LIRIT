package cn.iselab.mooctest.device.util;

import cn.iselab.mooctest.device.service.MiniToolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ServiceUtil {

    private static Logger log = LoggerFactory.getLogger(ServiceUtil.class);

    private static Map<String, MiniToolService> miniServiceMap = new HashMap<>();

    private ServiceUtil() {

    }

    public static void addMiniService(String deviceSerialNo, MiniToolService service) {
        miniServiceMap.put(deviceSerialNo, service);
    }

    public static void removeMiniService(String deviceSerialNo) {
        miniServiceMap.remove(deviceSerialNo);
    }

    public static MiniToolService getMiniServiceBySerialNo(String deviceSerialNo) {
        return miniServiceMap.get(deviceSerialNo);
    }
}
