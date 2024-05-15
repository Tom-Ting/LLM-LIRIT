package cn.iselab.mooctest.lit.controller.web;

import cn.iselab.mooctest.lit.service.ScriptService;
import cn.iselab.mooctest.lit.vo.QueryData;
import cn.iselab.mooctest.lit.vo.ResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/script")
public class ScriptController {
    @Autowired
    ScriptService scriptService;

    @GetMapping("/all")
    public ResponseVO getAllScript(@RequestParam("pageIndex") int pageIndex,@RequestParam("pageSize") int pageSize){
        return ResponseVO.buildSuccess(scriptService.getAllScript(pageIndex,pageSize));
    }

    @PostMapping("/updateName")
    public ResponseVO updateName(@RequestParam("scriptId") long scriptId,@RequestParam("name") String name){
        try {
            return scriptService.updateName(scriptId,name);
        }catch (Exception e){
            e.printStackTrace();
            return ResponseVO.buildFailure("失败");
        }
    }
    @GetMapping("/delete")
    public ResponseVO deleteScript(@RequestParam("scriptId") long scriptId){
        return ResponseVO.buildSuccess(scriptService.deleteScript(scriptId));
    }
    
}
