package cn.iselab.mooctest.lit.controller.remote;

import cn.iselab.mooctest.lit.model.Device;
import cn.iselab.mooctest.lit.service.DeviceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/remote")
public class DeviceServiceController {

    private final DeviceService deviceService;

    public DeviceServiceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping(value = "/serviceToDevice")
    public boolean getService2DevicesInfo(@RequestParam(name = "serviceName") String serviceName, @RequestBody List<Device> devices) {
        try {
            deviceService.saveService2DevicesMap(serviceName, devices);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
