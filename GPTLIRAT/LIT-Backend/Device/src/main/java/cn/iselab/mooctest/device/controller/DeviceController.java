package cn.iselab.mooctest.device.controller;

import cn.iselab.mooctest.device.model.Banner;
import cn.iselab.mooctest.device.service.*;
import cn.iselab.mooctest.device.util.DeviceManagementUtil;
import cn.iselab.mooctest.device.util.ServiceUtil;
import cn.iselab.mooctest.device.util.WdaClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping(value = "/deviceService")
public class DeviceController {

    private static final int IOS_UDID_LEN = 40;

    private ComponentService componentService;

    @Autowired
    DeviceService deviceService;

    @Autowired
    public DeviceController(ComponentService componentService) {
        this.componentService = componentService;
    }

    @RequestMapping(value = "/heartbeat", method = RequestMethod.GET)
    public boolean heartBeat() {
        return true;
    }

    /**
     * Start a {@code MiniToolService} with the given device, host and port
     * if not yet started.
     *
     * @see MiniToolService
     */
    @RequestMapping(value = "/device/start", method = RequestMethod.GET)
    public Banner connectDevice(@RequestParam(name = "serialNo") String serialNo,
                                @RequestParam(name = "host") String host,
                                @RequestParam(name = "port") int port) {
        MiniToolService miniToolService = ServiceUtil.getMiniServiceBySerialNo(serialNo);
        Set<String> iosDeviceUdids = DeviceManagementUtil.getIosDeviceUdids();
        if (iosDeviceUdids.contains(serialNo)) {
            if (miniToolService == null) {
                miniToolService = new MiniToolService(serialNo, host, port);
                miniToolService.initIOSMiniCap();
                ServiceUtil.addMiniService(serialNo, miniToolService);
            }
        } else {
            if (miniToolService == null) {
                miniToolService = new MiniToolService(DeviceManagementService.getInstance().getIDevice(serialNo), host, port);
                miniToolService.initMiniTool();
                ServiceUtil.addMiniService(serialNo, miniToolService);
            }
        }
        return miniToolService.getBanner();
    }


    @RequestMapping(value = "/device/restart", method = RequestMethod.GET)
    public void reconnectDevice(@RequestParam(name = "serialNo") String serialNo) {
        if (ServiceUtil.getMiniServiceBySerialNo(serialNo) != null) {
            ServiceUtil.getMiniServiceBySerialNo(serialNo).reStartMiniTool();
        }
    }

    /**
     * Close the corresponding {@code MiniToolService}.
     *
     * @see MiniToolService
     */
    @RequestMapping(value = "/device/close", method = RequestMethod.GET)
    public void closeDeviceConnect(@RequestParam(name = "serialNo") String serialNo) {
        if (ServiceUtil.getMiniServiceBySerialNo(serialNo) != null) {
            if (serialNo.length() == IOS_UDID_LEN) {
                ServiceUtil.getMiniServiceBySerialNo(serialNo).closeIOSMiniCap();
            } else {
                ServiceUtil.getMiniServiceBySerialNo(serialNo).closeMiniTool();
            }
        }
        ServiceUtil.removeMiniService(serialNo);
    }

    /**
     * @param serialNo   Serial number of the clicked device.
     * @param scriptName Actually, it is the script id.
     * @param stepIndex  The number of current step.
     * @param prefix     The prefix of output path to save the info.
     * @return Whether recording is successful.
     */
    @GetMapping("/device/record/click")
    public boolean recordClick(@RequestParam("serialNo") String serialNo,
                               @RequestParam("scriptName") String scriptName,
                               @RequestParam("stepIndex") int stepIndex,
                               @RequestParam("x") int x,
                               @RequestParam("y") int y,
                               @RequestParam(name = "prefix", required = false) String prefix) {
        if (prefix == null) {
            return componentService.recordClick(serialNo, scriptName, stepIndex, x, y);
        } else {
            return componentService.recordClick(serialNo, scriptName, stepIndex, x, y, prefix);
        }
    }

    @RequestMapping(value = "/device/record/complete", method = RequestMethod.GET)
    public byte[] scriptRecordComplete(@RequestParam(name = "serialNo") String serialNo,
                                       @RequestParam(name = "scriptId") long scriptId) {
        return componentService.zipAndGet(serialNo, scriptId);
    }

    @RequestMapping(value = "/device/record/playback", method = RequestMethod.GET)
    public byte[] getCurrentScreenshot(@RequestParam(name = "serialNo") String serialNo) {
        return componentService.getCurScreenShot(serialNo);
    }

    @RequestMapping(value = "/device/execute/tap", method = RequestMethod.GET)
    public void executeTapCommand(@RequestParam(name = "serialNo") String serialNo,
                                  @RequestParam(name = "x") String tap_x,
                                  @RequestParam(name = "y") String tap_y) {
        Set<String> iosDeviceUdids = DeviceManagementUtil.getIosDeviceUdids();
        if (iosDeviceUdids.contains(serialNo)) {
            WdaClientUtil.tapByCoords(serialNo, tap_x, tap_y);
        } else {
            String command = "input tap " + tap_x + " " + tap_y;
            componentService.executeCommand(serialNo, command);
        }
    }


    /**
     *
     * @param serialNo
     * @param direction  down是从上往下划，up是从下往上划
     */
    @RequestMapping(value="/device/execute/swipe",method = RequestMethod.GET)
    public void executeSwipeCommand(@RequestParam(name = "serialNo") String serialNo, @RequestParam(name = "direction") String direction){
        deviceService.swipe(serialNo,direction);
    }
}
