package cn.iselab.mooctest.lit.service;

import cn.iselab.mooctest.lit.model.Script;
import cn.iselab.mooctest.lit.vo.QueryData;
import cn.iselab.mooctest.lit.vo.ResponseVO;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface ScriptService {
    QueryData getAllScript(int pageIndex, int pageSize);
    ResponseVO updateName(long scriptId, String name);
    ResponseVO deleteScript(long scriptId);
}
