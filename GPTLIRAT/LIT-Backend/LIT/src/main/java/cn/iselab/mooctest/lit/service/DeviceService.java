package cn.iselab.mooctest.lit.service;

import cn.iselab.mooctest.lit.common.server.AndroidControlServer;
import cn.iselab.mooctest.lit.model.Device;

import java.util.List;
import java.util.Map;

public interface DeviceService {

    void saveService2DevicesMap(String serviceName, List<Device> devices);

    Object invokeDeviceServiceAndReturnRes(String url, Map<String, Object> param);

    boolean invokeDeviceService(String url, Map<String, Object> param);

    Map<String, Object> getTransferSocketHostAndPort();

    /**
     * Keep screenshots receiver socket and corresponding websocket alive.
     *
     * @see AndroidControlServer#startSocket()
     * @see AndroidControlServer#startWebSocket()
     */
    void keepAndroidControlServerAlive();

    List<Device> getDevices();

    Device getDeviceBySerialNo(String serialNo);

    void updateDeviceStatus(String serviceName, String serialNo, int status);


}
