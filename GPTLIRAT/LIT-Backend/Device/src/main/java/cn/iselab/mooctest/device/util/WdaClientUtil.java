package cn.iselab.mooctest.device.util;

import cn.iselab.mooctest.device.common.constant.IOSCommandConstants;
import cn.iselab.mooctest.device.model.Device;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WdaClientUtil {

    private static final Logger logger = LoggerFactory.getLogger(WdaClientUtil.class);

    public void backHome(String deviceUdid) {
        String result = HttpClientUtil.sendHttpPost(IOSCommandConstants
                .getWdaHomeCommand(DeviceManagementUtil.getIosDeviceBySerialNo(deviceUdid).getForwardWdaPort()));
        logger.info("Wda back home command result:{}", result);
    }

    public static void tapByCoords(String deviceUdid, String x, String y) {
        Device device = DeviceManagementUtil.getIosDeviceBySerialNo(deviceUdid);
        if (device != null) {
            String sessionId = device.getWdaSession();
            if (sessionId == null) {
                sessionId = getSessionId(deviceUdid);
                DeviceManagementUtil.getIosDeviceBySerialNo(deviceUdid).setWdaSession(sessionId);
            }

            String windowSizeJsonString = HttpClientUtil.sendHttpGet(IOSCommandConstants.getWdaWindowSizeCommand(device.getForwardWdaPort(), sessionId));
            JSONObject windowSize = new JSONObject(windowSizeJsonString);
            int windowWidth = windowSize.getJSONObject("value").getInt("width");
            int windowHeight = windowSize.getJSONObject("value").getInt("height");
            int resolutionWidth = Integer.parseInt(device.getResolution().split("x")[0]);
            int resolutionHeight = Integer.parseInt(device.getResolution().split("x")[1]);
            int tapX = Integer.parseInt(x) * windowWidth / resolutionWidth;
            int tapY = Integer.parseInt(y) * windowHeight / resolutionHeight;

            JSONObject tapLoc = new JSONObject();
            tapLoc.put("x", tapX);
            tapLoc.put("y", tapY);
            String tapResult = HttpClientUtil.sendHttpPostJson(IOSCommandConstants.getWdaTapCommand(sessionId, device.getForwardWdaPort()), tapLoc.toString());
            if (null == tapResult) {
                logger.error("Wda is no running, tap error");
            }
            if (tapResult.contains("Session does not exist")) {
                sessionId = getSessionId(deviceUdid);
                DeviceManagementUtil.getIosDeviceBySerialNo(deviceUdid).setWdaSession(sessionId);
                HttpClientUtil.sendHttpPostJson(IOSCommandConstants.getWdaTapCommand(sessionId, device.getForwardWdaPort()), tapLoc.toString());
            }
        }
    }

    public static String getSessionId(String deviceId) {
        String sessionId = null;
        try {
            logger.warn(IOSCommandConstants.getWdaStatusCommand(DeviceManagementUtil.getIosDeviceBySerialNo(deviceId).getForwardWdaPort()));
            String status = HttpClientUtil.sendHttpGet(IOSCommandConstants.getWdaStatusCommand(DeviceManagementUtil.getIosDeviceBySerialNo(deviceId).getForwardWdaPort()));
            JSONObject statusJson = new JSONObject(status);
            sessionId = statusJson.get("sessionId").toString();
        } catch (JSONException e) {
            logger.error("wda get session json parse error, cause:{}", e.getMessage());
        }
        return sessionId;
    }

}
