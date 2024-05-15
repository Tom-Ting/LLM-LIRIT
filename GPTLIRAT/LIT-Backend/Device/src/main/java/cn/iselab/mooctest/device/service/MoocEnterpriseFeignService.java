package cn.iselab.mooctest.device.service;

import cn.iselab.mooctest.device.model.Device;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Service
@FeignClient(value = "lit",url="${lirat.lit.url}")
public interface MoocEnterpriseFeignService {

    @RequestMapping(value = "/remote/serviceToDevice", method = RequestMethod.POST)
    boolean sendService2DevicesInfo(@RequestParam(name = "serviceName") String serviceName, @RequestBody List<Device> devices);

    @RequestMapping(value = "/remote/data", method = RequestMethod.GET)
    String getMoocEnterpriseHost(@RequestParam(name = "data") byte[] data);
}
