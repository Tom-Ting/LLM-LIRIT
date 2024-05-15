package cn.iselab.mooctest.device.service.impl;

import cn.iselab.mooctest.device.common.constant.ADBCommandConstants;
import cn.iselab.mooctest.device.common.constant.IOSCommandConstants;
import cn.iselab.mooctest.device.model.Device;
import cn.iselab.mooctest.device.service.ComponentService;
import cn.iselab.mooctest.device.service.DeviceService;
import cn.iselab.mooctest.device.util.DeviceManagementUtil;
import cn.iselab.mooctest.device.util.HttpClientUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class DeviceServiceImpl implements DeviceService {

    @Autowired
    ComponentService componentService;

    private static int duration=1000;

    @Override
    public void swipe(String serialNo, String direction) {
        Set<String> iosDeviceUdids = DeviceManagementUtil.getIosDeviceUdids();
        if (iosDeviceUdids.contains(serialNo)) {
            // ios
            iosSwipe(serialNo,direction);
        } else {
            // android
            androidSwipe(serialNo,direction);
        }
    }

    private void androidSwipe(String serialNo, String direction){
        Device device=DeviceManagementUtil.getDevices().get(serialNo);
        int resolutionWidth = Integer.parseInt(device.getResolution().split("x")[0]);
        int resolutionHeight = Integer.parseInt(device.getResolution().split("x")[1]);
        int topY=resolutionHeight/3;
        int bottomY=resolutionHeight*2/3;
        int X=resolutionWidth/2;
        String command=null;
        if(direction.equals("down"))
            command= String.format(ADBCommandConstants.SWIP, X,topY,X,bottomY,duration);
        else
            command= String.format(ADBCommandConstants.SWIP,X,bottomY, X,topY,duration);
        componentService.executeCommand(serialNo, command);
    }


    /**
     * ios 因为设备问题，暂未测试，代码参考python库"wda"中的swipe方法
     * @param serialNo
     * @param direction
     */
    private void iosSwipe(String serialNo,String direction){
        Device device=DeviceManagementUtil.getDevices().get(serialNo);
        String sessionId=device.getWdaSession();
        int resolutionWidth = Integer.parseInt(device.getResolution().split("x")[0]);
        int resolutionHeight = Integer.parseInt(device.getResolution().split("x")[1]);
        int topY=resolutionHeight/3;
        int bottomY=resolutionHeight*2/3;
        int X=resolutionWidth/2;

        JSONObject tapLoc = new JSONObject();
        if(direction.equals("down")){
            tapLoc.put("fromX",X);
            tapLoc.put("fromY",topY);
            tapLoc.put("toX",X);
            tapLoc.put("toY",bottomY);
            tapLoc.put("duration",duration);
        }else{
            tapLoc.put("fromX",X);
            tapLoc.put("fromY",bottomY);
            tapLoc.put("toX",X);
            tapLoc.put("toY",topY);
            tapLoc.put("duration",duration);
        }
        String swipeResult = HttpClientUtil.sendHttpPostJson(IOSCommandConstants.getWdaSwipeCommand(sessionId, device.getForwardWdaPort()), tapLoc.toString());
    }
}
