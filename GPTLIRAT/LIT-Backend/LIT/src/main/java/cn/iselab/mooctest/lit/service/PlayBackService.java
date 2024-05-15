package cn.iselab.mooctest.lit.service;

import cn.iselab.mooctest.lit.model.StepInfo;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

public interface PlayBackService {
    JSONObject doReplay(Long scriptId, String serialNo, int fromStep);

    int singleStepReplay(StepInfo stepInfo, String[] dirsLocation);
}
