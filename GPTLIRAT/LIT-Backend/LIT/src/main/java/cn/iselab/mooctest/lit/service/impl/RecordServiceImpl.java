package cn.iselab.mooctest.lit.service.impl;

import cn.iselab.mooctest.lit.common.constant.ApiConstants;
import cn.iselab.mooctest.lit.dao.ScriptDao;
import cn.iselab.mooctest.lit.model.Script;
import cn.iselab.mooctest.lit.service.PlayBackService;
import cn.iselab.mooctest.lit.service.RecordService;
import cn.iselab.mooctest.lit.util.DeviceServiceUtil;
import cn.iselab.mooctest.lit.util.FileUtil;
import cn.iselab.mooctest.lit.util.DeviceUtils;
import cn.iselab.mooctest.lit.vo.ResponseVO;
import com.alibaba.fastjson.JSONObject;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class RecordServiceImpl implements RecordService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecordServiceImpl.class);
    /**
     * Pattern used in replay.
     */

    @Autowired
    private ScriptDao scriptDao;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private DeviceUtils deviceUtils;
    @Autowired
    private PlayBackService playBackService;
    @Value("${script.save.path}")
    private String componentInfoSavePath;

    private static final Queue<String> messages=new LinkedList<>();

    @Override
    public long tap(String serialNo, String appId, long scriptId, int x, int y) {
        long sid = saveInfo(scriptId, serialNo, appId, x, y);
        deviceUtils.performTap(serialNo, x, y);
        return sid;
    }

    @Override
    public void completeRecord(String serialNo, long scriptId) {
        Map<String, Object> params = new HashMap<>();
        params.put("serialNo", serialNo);
        params.put("scriptId", scriptId);
        String serviceName = DeviceServiceUtil.findServiceNameByDevice(serialNo);
        String url = ApiConstants.HTTP + serviceName + ApiConstants.DEVICE_RECORD_COMPLETE;
        byte[] recordInfo = restTemplate.getForObject(url, byte[].class, params);
        saveFile(recordInfo, scriptId);
    }

    /**
     * Save the info of a clicked component of an application.
     *
     * @param scriptId       The id of the script to record this action.
     *                       A negative number for creating a new script.
     * @param deviceSerialNo The serial number of the connected device.
     * @param appId          The id of current application.
     * @return If a new script is created, its id is returned. Otherwise
     * the return value is the same as the parameter. Under both
     * circumstances, -1 is returned if any error occurs.
     */
    private Long saveInfo(Long scriptId, String deviceSerialNo, String appId, int x, int y) {
        // Save the device udid and scripts into a folder named `udid_scriptId`.
        // Each step is in its own folder like `step1` under the script folder.
        Script script;
        int stepIndex = 1;
        if (scriptId < 0) {
            // Save script record into database.
            script = new Script();
            script.setDeviceUdid(deviceSerialNo);
            script.setAppId(appId);
            scriptId = scriptDao.save(script).getScriptId();
            String scriptName = deviceSerialNo + "_" + scriptId.toString();
            script.setName(scriptName);
            String baseDirPath = componentInfoSavePath + scriptName + "/step" + stepIndex;
            script.setDirsLocation(baseDirPath);
        } else {
            // Create new step folder and update the database record.
            script = scriptDao.get(scriptId);
            stepIndex = script.getCurrentStep() + 1;
            String nextDirPath = componentInfoSavePath + script.getName() + "/step" + stepIndex;
            script.setCurrentStep(stepIndex);
            script.setDirsLocation(script.getDirsLocation() + "," + nextDirPath);
        }
        if (recordTap(deviceSerialNo, script.getName(), stepIndex, x, y)) {
            scriptDao.save(script);
        } else {
            scriptId = -1L;
        }
        return scriptId;
    }

    /**
     * Save the steps of the given script into a local .zip file and unzip to the same directory.
     */
    private void saveFile(byte[] bytes, long scriptId) {
        if (bytes != null) {
            Script script = scriptDao.get(scriptId);
            String zipInfoPath = componentInfoSavePath + script.getName() + ".zip";
            FileUtil.saveFile(bytes, zipInfoPath);
            script.setScriptUrl(zipInfoPath);
            try {
                ZipFile zipFile = new ZipFile(zipInfoPath);
                zipFile.extractAll(zipFile.getFile().getParent());
                scriptDao.save(script);
            } catch (ZipException e) {
                e.printStackTrace();
            }
        }
    }



    private boolean recordTap(String serialNo, String scriptName, int stepIndex, int x, int y) {
        String serviceName = DeviceServiceUtil.findServiceNameByDevice(serialNo);
        if (serviceName != null) {
            Map<String, String> params = new HashMap<>();
            params.put("serialNo", serialNo);
            params.put("scriptName", scriptName);
            params.put("stepIndex", String.valueOf(stepIndex));
            params.put("x", String.valueOf(x));
            params.put("y", String.valueOf(y));
            String url = ApiConstants.HTTP + serviceName;

            url += ApiConstants.DEVICE_RECORD_CLICK;


            Boolean response = restTemplate.getForObject(url, Boolean.class, params);
            return response != null && response;
        } else {
            return false;
        }
    }

    /**
     * @param scriptId   Script id saved in the database.
     * @param deviceUdid Serial number of the device to replay the script.
     * @param fromStep   The step from which to start replaying.
     * @return A json object of the following structure:
     * {@literal {"success": true|false, "next_step": <int>}},
     * where success means the replay completes to the end of the script,
     * and next_step means the failed step index or 0 if success is true.
     */
    @Override
    public JSONObject playback(Long scriptId, String deviceUdid, int fromStep) {
        return playBackService.doReplay(scriptId,deviceUdid,fromStep);
    }

    @Override
    public ResponseVO getMessage() {
        List<String> res=new ArrayList<>();
        while(!messages.isEmpty()){
            res.add(messages.poll());
        }
        return ResponseVO.buildSuccess(res);
    }
    @Override
    public void setMessage(String message){
        messages.offer(message);
    }

}

