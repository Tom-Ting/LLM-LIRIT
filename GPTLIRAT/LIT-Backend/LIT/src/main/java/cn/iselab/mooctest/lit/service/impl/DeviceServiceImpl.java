package cn.iselab.mooctest.lit.service.impl;

import cn.iselab.mooctest.lit.common.server.AndroidControlServer;
import cn.iselab.mooctest.lit.model.Device;
import cn.iselab.mooctest.lit.service.DeviceService;
import cn.iselab.mooctest.lit.util.DeviceServiceUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DeviceServiceImpl implements DeviceService {

    private final RestTemplate restTemplate;
    @Value("${netty.server.host}")
    private String host;
    @Value("${mini.server.port}")
    private int miniPort;
    @Value("${log.server.port}")
    private int logPort;

    public DeviceServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void saveService2DevicesMap(String serviceName, List<Device> devices) {
        List<Device> oldDevices = DeviceServiceUtil.getDevicesByService(serviceName);
        if (oldDevices != null) {
            for (Device device : devices) {
                for (Device oldDevice : oldDevices) {
                    if (device.getSerialNumber().equals(oldDevice.getSerialNumber())
                            && device.getStatus() != oldDevice.getStatus()) {
                        device.setStatus(oldDevice.getStatus());
                        break;
                    }
                }
            }
        }
        DeviceServiceUtil.putOrUpdateService2Devices(serviceName, devices);
    }

    @Override
    public Object invokeDeviceServiceAndReturnRes(String url, Map<String, Object> param) {
        try {
            return restTemplate.getForObject(url, Object.class, param);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean invokeDeviceService(String url, Map<String, Object> param) {
        try {
            if (param != null) {
                restTemplate.getForObject(url, Boolean.class, param);
            } else {
                restTemplate.getForObject(url, Boolean.class);
            }
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    @Override
    public Map<String, Object> getTransferSocketHostAndPort() {
        Map<String, Object> param = new HashMap<>();
        param.put("host", host);
        param.put("port", miniPort);
        return param;
    }

    @Override
    public void keepAndroidControlServerAlive() {
        if (!AndroidControlServer.getInstance().isSocketStart()) {
            AndroidControlServer.getInstance().startSocket();
        }
        if (!AndroidControlServer.getInstance().isWebSocketStart()) {
            AndroidControlServer.getInstance().startWebSocket();
        }
    }

    @Override
    public List<Device> getDevices() {
        List<Device> devices = new ArrayList<>();
        for (List<Device> deviceList : DeviceServiceUtil.getService2DevicesMap().values()) {
            devices.addAll(deviceList);
        }
        return devices;
    }

    @Override
    public Device getDeviceBySerialNo(String serialNo) {
        for (List<Device> devices : DeviceServiceUtil.getService2DevicesMap().values()) {
            for (Device device : devices) {
                if (serialNo.equals(device.getSerialNumber())) {
                    return device;
                }
            }
        }
        return null;
    }


    @Override
    public void updateDeviceStatus(String serviceName, String serialNo, int status) {
        Device device = DeviceServiceUtil.getDeviceByServiceAndSerialNo(serviceName, serialNo);
        if (device != null) {
            device.setStatus(status);
        }
    }
}
