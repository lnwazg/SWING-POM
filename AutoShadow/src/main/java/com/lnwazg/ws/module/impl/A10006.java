package com.lnwazg.ws.module.impl;

import java.util.Date;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonObject;
import com.lnwazg.bean.ServerConfig;
import com.lnwazg.swing.util.WinMgr;
import com.lnwazg.util.sp.LineProvider;
import com.lnwazg.util.sp.impl.ArukasCrazyProvider;
import com.lnwazg.util.sp.impl.ArukasProvider;
import com.lnwazg.util.sp.impl.IshadowLineProvider;
import com.lnwazg.ws.service.ClientAppService;
import com.lnwazg.ws.sim.IService;
import com.lnwazg.ws.sim.anno.Component;
import com.lnwazg.ws.sim.anno.Note;
import com.lnwazg.ws.sim.anno.WsRequestParam;

@Component("A10006")
@Note("AutoSS支撑服务")
public class A10006 implements IService
{
    @Resource
    private ClientAppService clientAppService;
    
    @WsRequestParam(desc = "要做的操作  1:读取当前线路  2:设置当前线路  3:读取ss资源安装目录  4:到远程去读取参数line的ss配置信息（1-3线路，4-6本地，7-12其他）")
    int action;
    
    @WsRequestParam(desc = "要设置的当前线路的值", required = false)
    String line;
    
    /**
     * 当前需要操作的应用的ID
     */
    int appId = 1;
    
    @Override
    public void execute(JsonObject responseJsonObject)
        throws Exception
    {
        switch (action)
        {
            case 1:
                responseJsonObject.addProperty("result", clientAppService.getConfig(appId, "CUR_LINE"));
                break;
            case 2:
                clientAppService.setConfig(appId, "CUR_LINE", line);
                break;
            case 3:
                responseJsonObject.addProperty("result", clientAppService.getConfig(appId, "INSTALL_PATH"));
                break;
            case 4:
                ServerConfig sc = readRemoteConfig();
                responseJsonObject.add("result", gson.toJsonTree(sc));
                break;
            default:
                break;
        }
    }
    
    /**
     * 线路提供者列表
     */
    LineProvider[] lineProviders = {new IshadowLineProvider(), new ArukasProvider(), new ArukasCrazyProvider()};
    
    /**
     * 初始化为第0个服务提供商
     */
    LineProvider lineProvider = lineProviders[0];
    
    ServerConfig emptyConfig = new ServerConfig("empty", "8989", "", "aes-256-cfb", "此路不通！");
    
    /**
     * 获取指定线路的远程配置信息
     * @author nan.li
     * @param line
     * @return
     */
    private ServerConfig readRemoteConfig()
    {
        ServerConfig resultConfig = emptyConfig;
        if (StringUtils.isNotEmpty(WinMgr.configs.get("lineProvider")))
        {
            //从配置文件中挑选线路
            lineProvider = lineProviders[Integer.valueOf(clientAppService.getConfig(appId, "lineProvider"))];
        }
        int lineInt = Integer.parseInt(line);
        switch (lineInt)
        {
            case 1:
            case 2:
            case 3:
            case 4:
                resultConfig = lineProvider.provide(lineInt, clientAppService);
                break;
            case 5:
            case 6:
            case 7:
            case 8:
                //本地的
                String localLineKey = String.format("LOCAL_LINE%d", lineInt - 4);
                String jsonStr = clientAppService.getConfig(appId, localLineKey);
                if (StringUtils.isNotBlank(jsonStr))
                {
                    try
                    {
                        JSONObject jsonObject = new JSONObject(jsonStr);
                        resultConfig = new ServerConfig(jsonObject.getString("server"), jsonObject.getString("server_port"), jsonObject.getString("password"),
                            jsonObject.getString("method"), new Date().toLocaleString());
                    }
                    catch (JSONException e)
                    {
                        resultConfig = emptyConfig;
                        e.printStackTrace();
                    }
                }
                else
                {
                    resultConfig = emptyConfig;
                }
                break;
            case 9:
            case 10:
            case 11:
            case 12:
                resultConfig = emptyConfig;
                break;
            default:
                break;
        }
        return resultConfig;
    }
}
