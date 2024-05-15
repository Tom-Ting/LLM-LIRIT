package cn.iselab.mooctest.lit.common.web;

import java.util.HashMap;

public class ResponseResult extends HashMap<String, Object> {

    public static ResponseResult responseError(int statusCode, String key, Object value) {
        ResponseResult result = new ResponseResult();
        result.put(ResponseMessage.CODE, statusCode);
        result.put(key, value);
        return result;
    }

    public static ResponseResult responseOk(String key, Object value) {
        ResponseResult result = new ResponseResult();
        result.put(ResponseMessage.CODE, StatusCode.SUCCESS);
        result.put(key, value);
        return result;
    }

}
