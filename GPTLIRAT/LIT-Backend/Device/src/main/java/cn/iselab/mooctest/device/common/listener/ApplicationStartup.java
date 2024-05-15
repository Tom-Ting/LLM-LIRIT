package cn.iselab.mooctest.device.common.listener;

import cn.iselab.mooctest.device.service.DeviceManagementService;
import cn.iselab.mooctest.device.util.PortManagementUtil;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

public class ApplicationStartup implements ApplicationListener<ContextRefreshedEvent> {

    private static boolean isStart = false;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        if (!isStart) {
            startDeviceManager();
            PortManagementUtil.init();
            isStart = true;
        }
    }

    private void startDeviceManager() {
        new Thread(() -> DeviceManagementService.getInstance().start()).start();
    }
}
