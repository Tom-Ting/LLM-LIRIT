package cn.iselab.mooctest.lit.service;

import cn.iselab.mooctest.lit.vo.ResponseVO;
import com.alibaba.fastjson.JSONObject;

public interface RecordService {

    /**
     * Perform a tap action on the given device and record this action in the
     * given script.
     *
     * @param serialNo Serial number of the device on which to perform
     *                 this tap action.
     * @param appId    Id of the application under testing.
     * @param scriptId Id of the script to record this action. A negative
     *                 number for creating a new script.
     * @param x        The x coordinate of the tapped point.
     * @param y        The y coordinate of the tapped point.
     * @return The script id. If a new script is created, its id is returned.
     * Otherwise the param {@code scriptId} is returned. -1 if any
     * error occurs.
     */
    long tap(String serialNo, String appId, long scriptId, int x, int y);

    /**
     * Save the script steps into a zip file and unzip it.
     */
    void completeRecord(String serialNo, long scriptId);

    JSONObject playback(Long scriptId, String deviceUdid, int fromStep);

    ResponseVO getMessage();

    void setMessage(String message);
}
