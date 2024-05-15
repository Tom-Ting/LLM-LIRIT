package cn.iselab.mooctest.device.wrapper;

import cn.iselab.mooctest.device.common.constant.PathConstants;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AndroidDebugBridgeWrapper {

    private static Logger log = LoggerFactory.getLogger(AndroidDebugBridgeWrapper.class);

    private AndroidDebugBridge androidDebugBridge;

    public void init(boolean isClientSupport) {
        AndroidDebugBridge.init(isClientSupport);
        log.info("The android path is {}", PathConstants.ANDROID_PATH);
        androidDebugBridge = AndroidDebugBridge.
                createBridge(PathConstants.ANDROID_PATH + "adb", false);
    }

    public void addDeviceListener(AndroidDebugBridge.IDeviceChangeListener deviceChangeListener) {
        AndroidDebugBridge.addDeviceChangeListener(deviceChangeListener);
    }

    public void removeDeviceListener(AndroidDebugBridge.IDeviceChangeListener deviceChangeListener) {
        AndroidDebugBridge.removeDeviceChangeListener(deviceChangeListener);
    }

    public void terminate() {
        AndroidDebugBridge.terminate();
    }

    public void disconnectBridge() {
        AndroidDebugBridge.disconnectBridge();
    }

    public IDevice[] getIDevices() {
        IDevice[] deviceList = null;
        if (androidDebugBridge != null) {
            deviceList = androidDebugBridge.getDevices();
        }
        return deviceList;
    }
}
