package cn.iselab.mooctest.lit.common.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

public class ApplicationStartup implements ApplicationListener<ContextRefreshedEvent> {

    private static boolean isStart = false;
    private Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        if (!isStart) {
            startMiniSocket();
            startMiniWSSocket();
            startLogSocket();
            startLogWSSocket();
            isStart = true;
        }
    }

    private void startMiniSocket() {
        log.info("mini socket start");
        new Thread(AndroidControlServer.getInstance():: startSocket).start();
    }

    private void startMiniWSSocket() {
        log.info("mini websocket start");
        new Thread(AndroidControlServer.getInstance()::startWebSocket).start();
    }

    private void startLogSocket() {
        log.info("log socket start");
        new Thread(AndroidControlServer.getInstance()::startLogSocket).start();
    }

    private void startLogWSSocket() {
        log.info("log websocket start");
        new Thread(AndroidControlServer.getInstance()::startLogWebSocket).start();
    }
}
