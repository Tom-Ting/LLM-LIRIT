package cn.iselab.mooctest.lit.controller.web;

import cn.iselab.mooctest.lit.common.web.ResponseMessage;
import cn.iselab.mooctest.lit.common.web.ResponseResult;
import cn.iselab.mooctest.lit.common.web.StatusCode;
import cn.iselab.mooctest.lit.service.RecordService;
import cn.iselab.mooctest.lit.util.DeviceServiceUtil;
import cn.iselab.mooctest.lit.vo.ResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Handling script recording requests.
 */
@RestController
@RequestMapping("/api/record")
public class RecordController {
    private RecordService recordService;

    @Autowired
    public RecordController(RecordService recordService) {
        this.recordService = recordService;
    }

    /**
     * Perform a tap action and record its info.
     * Use this method to create a new script or continue recording.
     *
     * @return If {@code scriptId} is < 0, a new script will be created and its
     * id will be returned. Otherwise the {@code scriptId} itself will be
     * returned. Under both conditions, if any error occurs, -1 is returned.
     */
    @GetMapping("/tap")
    public Object recordTap(@RequestParam("serialNo") String serialNo,
                            @RequestParam("appId") String appId,
                            @RequestParam("scriptId") long scriptId,
                            @RequestParam("x") int x,
                            @RequestParam("y") int y) {
        long sid = recordService.tap(serialNo, appId, scriptId, x, y);
        return ResponseResult.responseOk("id", sid);
    }

    @GetMapping(value = "/complete")
    public Map<String, Object> scriptRecordComplete(@RequestParam(name = "scriptId") Long scriptId,
                                                    @RequestParam(name = "serialNo") String serialNo) {
        String serviceName = DeviceServiceUtil.findServiceNameByDevice(serialNo);
        if (serviceName != null) {
            recordService.completeRecord(serialNo, scriptId);
            return ResponseResult.responseOk(ResponseMessage.MSG, "ok");
        } else {
            return ResponseResult.responseError(StatusCode.INNER_ERROR, ResponseMessage.MSG, "device not exist");
        }
    }

    @GetMapping(value = "/playback")
    public Map<String, Object> scriptRecordPlayback(@RequestParam(name = "scriptId") Long scriptId,
                                                    @RequestParam(name = "serialNo") String serialNo,
                                                    @RequestParam(name = "fromStep", defaultValue = "1") int fromStep) {
        String serviceName = DeviceServiceUtil.findServiceNameByDevice(serialNo);
        if (serviceName != null) {
            return ResponseResult.responseOk(ResponseMessage.DATA, recordService.playback(scriptId, serialNo, fromStep));
        } else {
            return ResponseResult.responseError(StatusCode.INNER_ERROR, ResponseMessage.MSG, "device not exist");
        }
    }
    @GetMapping(value="/message")
    public ResponseVO getMessage(){
        return recordService.getMessage();
    }

}
