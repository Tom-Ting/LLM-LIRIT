package cn.iselab.mooctest.lit.common.schedule;

import cn.iselab.mooctest.lit.common.constant.ApiConstants;
import cn.iselab.mooctest.lit.model.Device;
import cn.iselab.mooctest.lit.service.DeviceService;
import cn.iselab.mooctest.lit.util.DeviceServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@EnableScheduling
public class ServiceToDeviceHandleSchedule {

    private final DeviceService deviceService;
    private Logger log = LoggerFactory.getLogger(getClass());

    public ServiceToDeviceHandleSchedule(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Scheduled(cron = "0/8 * *  * * ? ")
    public void clearUnusedService2Devices() {
        try {
            for (Map.Entry<String, List<Device>> entry : DeviceServiceUtil.getService2DevicesMap().entrySet()) {
                if (!deviceService.invokeDeviceService("http://" + entry.getKey() + ApiConstants.HEART_BEAT, null)) {
                    try {
                        Thread.sleep(4000);
                    } catch (InterruptedException e) {
                        log.error("thread sleep error, cause:{}", e.getMessage());
                    }
                    if (!deviceService.invokeDeviceService("http://" + entry.getKey() + ApiConstants.HEART_BEAT, null)) {
                        DeviceServiceUtil.removeService2Devices(entry.getKey());
                        log.info("------------remove service:{}", entry.getKey());
                    }
                }
            }
        } catch (Exception e) {
            log.error("ServiceToDevicesHandle schedule error:", e);
        }
    }
}
