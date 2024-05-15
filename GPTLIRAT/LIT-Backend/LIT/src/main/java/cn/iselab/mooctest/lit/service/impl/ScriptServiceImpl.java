package cn.iselab.mooctest.lit.service.impl;

import cn.iselab.mooctest.lit.dao.ScriptDao;
import cn.iselab.mooctest.lit.model.Script;
import cn.iselab.mooctest.lit.service.ScriptService;
import cn.iselab.mooctest.lit.vo.QueryData;
import cn.iselab.mooctest.lit.vo.ResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ScriptServiceImpl implements ScriptService {
    @Autowired
    ScriptDao scriptDao;

    @Override
    public QueryData getAllScript(int pageIndex, int pageSize) {
        List<Script> all=scriptDao.findAll();
        all.sort(new Comparator<Script>() {
            @Override
            public int compare(Script o1, Script o2) {
                if(o1.getScriptId()-o2.getScriptId()>0){
                    return -1;
                }else{
                    return 1;
                }
            }
        });
        int fromIndex = (pageIndex-1)*pageSize;
        int toIndex = pageIndex*pageSize;
        List<Script> resultPaging=new ArrayList<>();
        if (all.size() >= toIndex){
            resultPaging = all.subList(fromIndex,toIndex);
        }else{
            resultPaging = all.subList(fromIndex,all.size());
        }
        QueryData res=new QueryData();
        res.list=resultPaging;
        res.pageTotal=all.size();
        return res;
    }

    @Override
    public ResponseVO updateName(long scriptId, String name) {
        Optional<Script> scriptOptional=scriptDao.findById(scriptId);
        if(scriptOptional.isPresent()){
            Script script=scriptOptional.get();
            script.setName(name);
            scriptDao.save(script);
            return ResponseVO.buildSuccess();
        }
        return ResponseVO.buildFailure("未找到用例");
    }

    @Override
    public ResponseVO deleteScript(long scriptId) {
        scriptDao.deleteById(scriptId);
        return ResponseVO.buildSuccess();
    }
}
