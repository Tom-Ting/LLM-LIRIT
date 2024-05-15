package cn.iselab.mooctest.device.util;

import cn.iselab.mooctest.device.model.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DeviceManagementUtil {

    private static final Logger logger = LoggerFactory.getLogger(DeviceManagementUtil.class);

    private static Map<String, Device> devices = new ConcurrentHashMap<>();

    private static Map<String, Device> iosDevices = new ConcurrentHashMap<>();

    private DeviceManagementUtil() {
    }

    public static Map<String, Device> getDevices() {
        return devices;
    }

    public static void setDevices(Map<String, Device> devices) {
        DeviceManagementUtil.devices = devices;
    }

    public static void addDevice(Device device) {
        devices.put(device.getSerialNumber(), device);
    }

    public static void removeDevice(String deviceSerialNumber) {
        devices.remove(deviceSerialNumber);
    }

    public static Device getDeviceBySerialNo(String deviceSerialNumber) {
        return devices.get(deviceSerialNumber);
    }

    public static Map<String, Device> getIosDevices() {
        return iosDevices;
    }

    public static void setIosDevices(Map<String, Device> iosDevices) {
        DeviceManagementUtil.iosDevices = iosDevices;
    }

    public static void addIosDevice(Device device) {
        iosDevices.put(device.getSerialNumber(), device);
        logger.info("add IOS device:{}", device.getSerialNumber() + device.getMarketingName());
    }

    public static void removeIosDevice(String deviceSerialNumber) {
        iosDevices.remove(deviceSerialNumber);
        logger.info("remove IOS device:{}", deviceSerialNumber);
    }

    public static Device getIosDeviceBySerialNo(String deviceSerialNumber) {
        return iosDevices.get(deviceSerialNumber);
    }

    public static Set<String> getIosDeviceUdids() {
        return iosDevices.keySet();
    }

    /**
     * Find a device no matter it is an Android or Ios device.
     */
    public static Device find(String serialNo) {
        Device device;
        if ((device = getDeviceBySerialNo(serialNo)) != null) {
            return device;
        } else {
            return getIosDeviceBySerialNo(serialNo);
        }
    }
}
