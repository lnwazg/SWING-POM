package com.lnwazg.ws.module.impl;

import java.util.Map;

import javax.annotation.Resource;

import com.google.gson.JsonObject;
import com.lnwazg.ws.service.ClientAppService;
import com.lnwazg.ws.sim.IService;
import com.lnwazg.ws.sim.anno.Component;
import com.lnwazg.ws.sim.anno.Note;
import com.lnwazg.ws.sim.anno.WsRequestParam;

@Component("S00001")
@Note("配置各个APP的运行参数的服务")
public class S00001 implements IService
{
    @Resource
    private ClientAppService clientAppService;
    
    @WsRequestParam(desc = "应用ID")
    int appId;
    
    @WsRequestParam(desc = "要执行的操作   1:返回所有的参数map   2：根据key返回值       3:设置key的value")
    int handle;
    
    @WsRequestParam(desc = "参数名称", required = false)
    String key;
    
    @WsRequestParam(desc = "参数值", required = false)
    String value;
    
    @Override
    public void execute(JsonObject responseJsonObject)
        throws Exception
    {
        switch (handle)
        {
            case 1:
                Map<String, String> m = clientAppService.getConfigMap(appId);
                responseJsonObject.add("resultMap", gson.toJsonTree(m));
                break;
            case 2:
                responseJsonObject.addProperty("value", clientAppService.getConfig(appId, key));
                break;
            case 3:
                clientAppService.setConfig(appId, key, value);
                break;
            default:
                break;
        }
    }
}
