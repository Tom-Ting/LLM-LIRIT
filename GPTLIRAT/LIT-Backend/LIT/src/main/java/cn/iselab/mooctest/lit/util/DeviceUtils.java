package cn.iselab.mooctest.lit.util;

import cn.iselab.mooctest.lit.common.constant.ApiConstants;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DeviceUtils {
    @Autowired
    PythonInvokeUtil pythonInvokeUtil;

    @Autowired
    RestTemplate restTemplate;

    public void takeScreenshot(String path, String serialNo) {
        String serviceName = DeviceServiceUtil.findServiceNameByDevice(serialNo);
        Map<String, Object> params = new HashMap<>();
        params.put("serialNo", serialNo);
        String url = ApiConstants.HTTP + serviceName + ApiConstants.DEVICE_RECORD_PLAYBACK;
        byte[] data = restTemplate.getForObject(url, byte[].class, params);
        FileUtil.saveFile(data, path);
    }
    /**
     * 将坐标绑定到步骤信息上。未修改
     *
     * @param   serialNo The id of the script to record this action.
     *                   A negative number for creating a new script.
     * @param   x        x坐标
     * @param   y        y坐标
     */
    public void performTap(String serialNo, int x, int y) {
        log.info("performTap函数 serviceName : " + serialNo);
        String serviceName = DeviceServiceUtil.findServiceNameByDevice(serialNo);
        log.info("performTap函数 serviceName : " + serviceName);
        if (serviceName != null) {
            String url = ApiConstants.HTTP + serviceName + ApiConstants.DEVICE_TAP;
            Map<String, Object> params = new HashMap<>();
            params.put("serialNo", serialNo);
            params.put("x", x);
            params.put("y", y);
            log.info("performTap函数 x : " + x);
            log.info("performTap函数 y : " + y);
            restTemplate.getForObject(url, Boolean.TYPE, params);
        } else {
            log.warn("No device service for device: {}, skip tapping.", serialNo);
        }
    }

    public void swipe(String serialNo, String direction){
        String serviceName = DeviceServiceUtil.findServiceNameByDevice(serialNo);
        if (serviceName != null) {
            String url = ApiConstants.HTTP + serviceName + ApiConstants.DEVICE_SWIPE;
            Map<String, Object> params = new HashMap<>();
            params.put("serialNo", serialNo);
            params.put("direction", direction);
            restTemplate.getForObject(url, Boolean.TYPE, params);
        } else {
            log.warn("No device service for device: {}, skip swipe.", serialNo);
        }
    }
}