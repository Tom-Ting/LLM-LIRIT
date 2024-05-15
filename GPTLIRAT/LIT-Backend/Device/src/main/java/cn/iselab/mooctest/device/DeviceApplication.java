package cn.iselab.mooctest.device;

import cn.iselab.mooctest.device.common.listener.ApplicationStartup;
import org.springframework.beans.factory.support.DefaultSingletonBeanRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@EnableFeignClients
@SpringBootApplication
public class DeviceApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(DeviceApplication.class);
        application.addListeners(new ApplicationStartup());
        application.run(args);
        ApplicationContext context;
        DefaultSingletonBeanRegistry registry;
    }

}
