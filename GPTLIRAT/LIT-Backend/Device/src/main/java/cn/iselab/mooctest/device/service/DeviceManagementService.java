package cn.iselab.mooctest.device.service;

import cn.iselab.mooctest.device.common.listener.DeviceListener;
import cn.iselab.mooctest.device.wrapper.AndroidDebugBridgeWrapper;
import com.android.ddmlib.IDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DeviceManagementService {

    private Logger log = LoggerFactory.getLogger(getClass());

    private AndroidDebugBridgeWrapper androidDebugBridgeWrapper;

    private DeviceListener deviceListener;

    private DeviceManagementService() {

    }

    public static DeviceManagementService getInstance() {
        return DMServiceInstance.INSTANCE;
    }

    public void start() {
        androidDebugBridgeWrapper = new AndroidDebugBridgeWrapper();
        deviceListener = new DeviceListener();
        androidDebugBridgeWrapper.addDeviceListener(deviceListener);
        androidDebugBridgeWrapper.init(false);
        log.info("Device manager start successful.");
    }

    public void destroy() {
        if (androidDebugBridgeWrapper == null) {
            return;
        }
        androidDebugBridgeWrapper.removeDeviceListener(deviceListener);
        androidDebugBridgeWrapper.terminate();
    }

    public IDevice getIDevice(String serialNo) {
        IDevice[] iDevices = androidDebugBridgeWrapper.getIDevices();
        if (iDevices == null) {
            return null;
        } else {
            for (IDevice device : iDevices) {
                if (serialNo.equals(device.getSerialNumber())) {
                    return device;
                }
            }
            return null;
        }
    }

    private static class DMServiceInstance {
        private static final DeviceManagementService INSTANCE = new DeviceManagementService();
    }
}
