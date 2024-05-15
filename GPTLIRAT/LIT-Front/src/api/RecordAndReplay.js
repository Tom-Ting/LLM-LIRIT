import request from '../utils/request';
import urls from '@api/globalUrls';
export default{
    getData: (query) =>{
        return request({
            url:urls.Host+"/script/all",
            method: 'get',
            params:query
        });
    },
    deleteScript: (query) =>{
        return request({
            url:urls.Host+"/script/delete",
            method: 'get',
            params:query
        });
    },
    updateName:(params)=>{
        return request({
            url:urls.Host+"/script/updateName",
            method: 'post',
            params:params
        });
    },
    recordTap:(params)=>{
        return request({
            url:urls.Host+"/api/record/tap",
            method: 'get',
            params:params
        });
    },
    listAllDevices:()=>{
        return request({
            url:urls.Host+"/api/device/list",
            method: 'get',
        });
    },
    connect:(params)=>{
        return request({
            url:urls.Host+"/api/device/start",
            method: 'get',
            params:params
        });
    },
    reconnect:(params)=>{
        return request({
            url:urls.Host+"/api/device/restart",
            method: 'get',
            params:params
        });
    },
    disconnect:(params)=>{
        return request({
            url:urls.Host+"/api/device/close",
            method: 'get',
            params:params
        });
    },
    complete:(params)=>{
        return request({
            url:urls.Host+"/api/record/complete",
            method: 'get',
            params:params
        });
    },
    getMessage:()=>{
        return request({
            url:urls.Host+"/api/record/message",
            method: 'get',
        });
    },
    replay:(params)=>{
        return request({
            url:urls.Host+"/api//record/playback",
            method: 'get',
            params:params
        });
    }
}
