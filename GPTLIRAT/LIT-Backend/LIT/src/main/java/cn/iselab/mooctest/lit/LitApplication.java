package cn.iselab.mooctest.lit;

import cn.iselab.mooctest.lit.common.server.ApplicationStartup;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;

@EnableAsync
@SpringBootApplication
public class LitApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(LitApplication.class);
        application.addListeners(new ApplicationStartup());
        application.run(args);
    }

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
