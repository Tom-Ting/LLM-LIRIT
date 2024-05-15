package cn.iselab.mooctest.device.common.listener;

import cn.iselab.mooctest.device.common.constant.ADBCommandConstants;
import cn.iselab.mooctest.device.common.constant.DeviceInfoConstants;
import cn.iselab.mooctest.device.model.Device;
import cn.iselab.mooctest.device.util.CommandUtil;
import cn.iselab.mooctest.device.util.DeviceManagementUtil;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


/**
 * Listener for Android devices' connection.
 */
@Service
public class DeviceListener implements AndroidDebugBridge.IDeviceChangeListener {

    private Logger log = LoggerFactory.getLogger(DeviceListener.class);

    @Override
    public void deviceConnected(IDevice iDevice) {
        log.info("Device connect: {}", iDevice.getSerialNumber());

        if (iDevice.isOnline()) {
            saveConnectedDevice(iDevice);
        }
    }

    @Override
    public void deviceDisconnected(IDevice iDevice) {
        log.info("Device disconnect: {}", iDevice.getSerialNumber());
        DeviceManagementUtil.removeDevice(iDevice.getSerialNumber());
    }

    @Override
    public void deviceChanged(IDevice iDevice, int i) {
        if (iDevice.isOnline()) {
            log.info("Device change online: {}", iDevice.getSerialNumber());
            saveConnectedDevice(iDevice);
        } else {
            log.info("Device change offline: {}", iDevice.getSerialNumber());
        }
    }

    private void saveConnectedDevice(IDevice iDevice) {
        Device device = new Device();
        device.setStatus(1000);
        device.setSerialNumber(iDevice.getSerialNumber());
        if (!iDevice.getProperty(DeviceInfoConstants.PRODUCT_BRAND)
                .equals(iDevice.getProperty(DeviceInfoConstants.PRODUCT_MANUFACTURER))) {
            device.setBrand(iDevice.getProperty(DeviceInfoConstants.PRODUCT_MANUFACTURER) + " "
                    + iDevice.getProperty(DeviceInfoConstants.PRODUCT_BRAND));
        } else {
            device.setBrand(iDevice.getProperty(DeviceInfoConstants.PRODUCT_BRAND));
        }

        if (iDevice.getProperty(DeviceInfoConstants.MARKETING_NAME) != null) {
            device.setMarketingName(iDevice.getProperty(DeviceInfoConstants.MARKETING_NAME));
        } else {
            device.setMarketingName(device.getBrand());
        }

        device.setModel(iDevice.getProperty(DeviceInfoConstants.PRODUCT_MODEL));
        device.setVersion(iDevice.getProperty(DeviceInfoConstants.VERSION_RELEASE));

        if (iDevice.getProperty(DeviceInfoConstants.SYS_ROG_WIDTH) == null) {
            /*
              Explanation: the output may be like the following:
                           Physical Size: ***x***
                           Override Size: ***x***
                           When override size exists, it is the actual size of the screen.
             */
            String[] wmSizes = CommandUtil.executeShellCommand(iDevice, ADBCommandConstants.GET_WM_SIZE).split(":");
            device.setResolution(wmSizes[wmSizes.length - 1].trim());
        } else {
            device.setResolution(iDevice.getProperty(DeviceInfoConstants.SYS_ROG_WIDTH) + "x"
                    + iDevice.getProperty(DeviceInfoConstants.SYS_ROG_HEIGHT));
        }

        device.setAbi(iDevice.getProperty(DeviceInfoConstants.PRODUCT_CPU__ABI));
        device.setSdk(iDevice.getProperty(DeviceInfoConstants.VERSION_SDK));
        DeviceManagementUtil.addDevice(device);
    }
}
