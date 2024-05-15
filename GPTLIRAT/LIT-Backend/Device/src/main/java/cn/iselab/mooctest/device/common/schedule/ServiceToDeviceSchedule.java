package cn.iselab.mooctest.device.common.schedule;

import cn.iselab.mooctest.device.model.Device;
import cn.iselab.mooctest.device.service.MoocEnterpriseFeignService;
import cn.iselab.mooctest.device.util.DeviceManagementUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by zsx on 2018/11/30.
 */
@Component
@EnableScheduling
public class ServiceToDeviceSchedule {

    private final MoocEnterpriseFeignService moocEnterpriseFeignService;
    private Logger log = LoggerFactory.getLogger(getClass());
    @Value("${spring.application.name}")
    private String serviceName;

    public ServiceToDeviceSchedule(MoocEnterpriseFeignService moocEnterpriseFeignService) {
        this.moocEnterpriseFeignService = moocEnterpriseFeignService;
    }

    /**
     * Send service - device mapping info to {@code lit} service.
     */
    @Scheduled(cron = "0/2 * *  * * ? ")
    public void sendService2DevicesInfo() {
        try {
            List<Device> devices = new ArrayList<>();
            for (Map.Entry<String, Device> deviceEntry : DeviceManagementUtil.getDevices().entrySet()) {
                devices.add(deviceEntry.getValue());
            }
            for (Map.Entry<String, Device> deviceEntry : DeviceManagementUtil.getIosDevices().entrySet()) {
                devices.add(deviceEntry.getValue());
            }
            if (!moocEnterpriseFeignService.sendService2DevicesInfo(serviceName, devices)) {
                log.error("send service2device error");
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("service2device schedule error");
        }
    }
}
