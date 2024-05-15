package cn.iselab.mooctest.lit.controller.web;

import cn.iselab.mooctest.lit.common.constant.ApiConstants;
import cn.iselab.mooctest.lit.common.enums.DeviceState;
import cn.iselab.mooctest.lit.common.web.ResponseMessage;
import cn.iselab.mooctest.lit.common.web.ResponseResult;
import cn.iselab.mooctest.lit.common.web.StatusCode;
import cn.iselab.mooctest.lit.model.Device;
import cn.iselab.mooctest.lit.model.UINodeVO;
import cn.iselab.mooctest.lit.service.DeviceService;
import cn.iselab.mooctest.lit.util.DeviceChannelUtil;
import cn.iselab.mooctest.lit.util.DeviceServiceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/device")
public class DeviceController {

    private final DeviceService deviceService;
    @Value("${netty.server.host}")
    private String host;

    @Autowired
    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    /**
     * Connect the device screenshots receiver socket with the sender socket.
     * The receiver socket will then send the data to a websocket on
     * {@code ws://localhost:1801/ws} to be connected to by any client.
     * After invoking this method, the {@link Device#getStatus()} will become
     * {@link DeviceState#BUSY}.
     *
     * @see #closeDeviceConnect(String)
     * @see #reconnectDevice(String)
     */
    @GetMapping(value = "/start")
    public Map<String, Object> connectDevice(@RequestParam(name = "serialNo") String serialNo) {
        String serviceName = DeviceServiceUtil.findServiceNameByDevice(serialNo);
        deviceService.keepAndroidControlServerAlive();
        if (serviceName != null) {
            deviceService.updateDeviceStatus(serviceName, serialNo, DeviceState.BUSY.getStatus());
            Map<String, Object> param = deviceService.getTransferSocketHostAndPort();
            param.put("serialNo", serialNo);
            return ResponseResult.responseOk(ResponseMessage.DATA, deviceService.invokeDeviceServiceAndReturnRes(
                    ApiConstants.HTTP + serviceName + ApiConstants.DEVICE_CONNECT_START, param));
        } else {
            return ResponseResult.responseError(StatusCode.INNER_ERROR, ResponseMessage.MSG, "device not exist");
        }
    }

    /**
     * @see #connectDevice(String)
     * @see #closeDeviceConnect(String)
     */
    @GetMapping(value = "/restart")
    public Map<String, Object> reconnectDevice(@RequestParam(name = "serialNo") String serialNo) {
        String serviceName = DeviceServiceUtil.findServiceNameByDevice(serialNo);
        deviceService.keepAndroidControlServerAlive();
        if (serviceName != null) {
            Map<String, Object> param = new HashMap<>();
            param.put("serialNo", serialNo);
            return ResponseResult.responseOk(ResponseMessage.MSG, deviceService.invokeDeviceService(
                    ApiConstants.HTTP + serviceName + ApiConstants.DEVICE_CONNECT_RESTART, param));
        } else {
            return ResponseResult.responseError(StatusCode.INNER_ERROR, ResponseMessage.MSG, "device not exist");
        }
    }

    /**
     * Disconnect all sockets and close all channels that are related to the
     * device.
     *
     * @see #connectDevice(String)
     * @see #reconnectDevice(String)
     */
    @GetMapping(value = "/close")
    public Map<String, Object> closeDeviceConnect(@RequestParam(name = "serialNo") String serialNo) {
        String serviceName = DeviceServiceUtil.findServiceNameByDevice(serialNo);
        if (serviceName != null) {
            deviceService.updateDeviceStatus(serviceName, serialNo, DeviceState.IDLE.getStatus());
            DeviceChannelUtil.removeDevice2Channel(serialNo);
            DeviceChannelUtil.removeDevice2WSChannel(serialNo);
            DeviceChannelUtil.removeDevice2LogWSChannel(serialNo);
            Map<String, Object> param = new HashMap<>();
            param.put("serialNo", serialNo);
            return ResponseResult.responseOk(ResponseMessage.MSG, deviceService.invokeDeviceService(
                    ApiConstants.HTTP + serviceName + ApiConstants.DEVICE_CONNECT_CLOSE, param));
        } else {
            return ResponseResult.responseError(StatusCode.INNER_ERROR, ResponseMessage.MSG, "device not exist");
        }
    }

    @GetMapping(value = "/list")
    public Map<String, Object> getRemoteDeviceList() {
        try {
            return ResponseResult.responseOk(ResponseMessage.List_RESULT, deviceService.getDevices());
        } catch (Exception e) {
            return ResponseResult.responseError(StatusCode.INNER_ERROR, ResponseMessage.MSG, "get devices error");
        }
    }

    @GetMapping(value = "/get/{serialNo}")
    public Map<String, Object> getRemoteDeviceBySerialNo(@PathVariable("serialNo") String serialNo) {
        try {
            return ResponseResult.responseOk(ResponseMessage.DATA, deviceService.getDeviceBySerialNo(serialNo));
        } catch (Exception e) {
            return ResponseResult.responseError(StatusCode.INNER_ERROR, ResponseMessage.MSG, "get device error");
        }
    }


}
