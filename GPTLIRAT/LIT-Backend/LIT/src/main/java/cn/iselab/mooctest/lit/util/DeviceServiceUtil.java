package cn.iselab.mooctest.lit.util;

import cn.iselab.mooctest.lit.model.Device;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DeviceServiceUtil {

    private static Map<String, List<Device>> service2DevicesMap = new ConcurrentHashMap<>();

    private DeviceServiceUtil() {

    }

    public static Map<String, List<Device>> getService2DevicesMap() {
        return service2DevicesMap;
    }

    public static void setService2DevicesMap(Map<String, List<Device>> service2DevicesMap) {
        DeviceServiceUtil.service2DevicesMap = service2DevicesMap;
    }

    public static void putOrUpdateService2Devices(String serviceName, List<Device> devices) {
        service2DevicesMap.put(serviceName, devices);
    }

    public static void removeService2Devices(String serviceName) {
        service2DevicesMap.remove(serviceName);
    }

    public static String findServiceNameByDevice(String deviceSerialNo) {
        for (Map.Entry<String, List<Device>> entry : service2DevicesMap.entrySet()) {
            for (Device device : entry.getValue()) {
                if (deviceSerialNo.equals(device.getSerialNumber())) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    public static List<Device> getDevicesByService(String serviceName) {
        return service2DevicesMap.get(serviceName);
    }

    public static Device getDeviceByServiceAndSerialNo(String serviceName, String serialNo) {
        for (Device device : service2DevicesMap.get(serviceName)) {
            if (serialNo.equals(device.getSerialNumber())) {
                return device;
            }
        }
        return null;
    }

}
