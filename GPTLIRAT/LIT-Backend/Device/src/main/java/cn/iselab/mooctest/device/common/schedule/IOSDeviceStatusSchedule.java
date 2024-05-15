package cn.iselab.mooctest.device.common.schedule;

import cn.iselab.mooctest.device.common.constant.IOSCommandConstants;
import cn.iselab.mooctest.device.common.constant.PathConstants;
import cn.iselab.mooctest.device.model.Device;
import cn.iselab.mooctest.device.service.MoocEnterpriseFeignService;
import cn.iselab.mooctest.device.util.DeviceManagementUtil;
import cn.iselab.mooctest.device.util.OSUtil;
import com.google.common.base.Splitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * This class detects all available IOS mobile devices and registers them
 * into {@link DeviceManagementUtil}.
 */
@Component
@EnableScheduling
public class IOSDeviceStatusSchedule {

    private static final Logger logger = LoggerFactory.getLogger(IOSDeviceStatusSchedule.class);
    private static final Properties iphoneType2Resolution = new Properties();

    static {
        try {
            iphoneType2Resolution.load(new FileReader(PathConstants.IPHONE_TYPE2RESOLUTION_PATH));
            logger.warn("LOAD SUCCESS");
        } catch (IOException e) {
            logger.error("Error on loading IphoneType2Resolution properties.", e);
        }
    }

    @Autowired
    MoocEnterpriseFeignService moocEnterpriseFeignService;
    @Value("${spring.application.name}")
    private String serviceName;

    private static List<String> getIOSDevices() {
        List<String> iosUdidList;
        String deviceUdids = OSUtil.runCommand(IOSCommandConstants.IDEVICE_ID_LIST_COMMAND);
        iosUdidList = Splitter.on("\n").omitEmptyStrings().trimResults().splitToList(deviceUdids);
        Device device;
        Set<String> currentIOSDevices = DeviceManagementUtil.getIosDeviceUdids();
        for (String currentDeviceUdid : currentIOSDevices) {
            if (!iosUdidList.contains(currentDeviceUdid)) {
                DeviceManagementUtil.removeIosDevice(currentDeviceUdid);
            }
        }
        for (String iosUdid : iosUdidList) {
            if (currentIOSDevices.contains(iosUdid)) {
                continue;
            }
            logger.info("Udid:{}", iosUdid);
            device = getDeviceInfo(iosUdid);
            DeviceManagementUtil.addIosDevice(device);
        }
        return iosUdidList;
    }

    private static Device getDeviceInfo(String udid) {
        Device device = new Device();
        device.setSerialNumber(udid);
        device.setStatus(1000);
        // Retrieve device resolution info.
        String productType = OSUtil.runCommand(IOSCommandConstants.ideviceProductType(udid));
        productType = productType.substring(0, productType.length() - 1);
        logger.warn(productType);
        logger.warn(iphoneType2Resolution.getProperty(productType));
        device.setResolution(iphoneType2Resolution.getProperty(productType));

        String iosDeviceInfo = OSUtil.runCommand(IOSCommandConstants.ideviceInfo(udid));
        List<String> iosInfoList = Splitter.on("\n").omitEmptyStrings().splitToList(iosDeviceInfo);
        for (String iosInfo : iosInfoList) {
            String iosInfoTrim = iosInfo.replaceFirst(" ", "");
            if (iosInfo.contains("DeviceName")) {
                device.setMarketingName(iosInfoTrim.substring(iosInfo.indexOf(":") + 1));
            } else if (iosInfo.contains("ProductName")) {
                device.setBrand(iosInfoTrim.substring(iosInfo.indexOf(":") + 1));
            } else if (iosInfo.contains("ProductType")) {
                device.setModel(iosInfoTrim.substring(iosInfoTrim.indexOf(":") + 1, iosInfoTrim.indexOf(",")));
            } else if (iosInfo.contains("ProductVersion")) {
                device.setVersion(iosInfoTrim.substring(iosInfoTrim.indexOf(":") + 1));
            }
        }
        return device;
    }

    @Scheduled(cron = "0/2 * *  * * ? ")
    public void IOSDeviceStatusListener() {
	    //getIOSDevices();
    }
}
