package com.lnwazg.util.sp.impl;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.lnwazg.bean.ServerConfig;
import com.lnwazg.kit.http.HttpUtils;
import com.lnwazg.util.sp.LineProvider;
import com.lnwazg.ws.service.ClientAppService;

public class ArukasProvider implements LineProvider
{
    static String url = "https://superss.arukascloud.io/2ac56b41-0592-403e-9948-25faef4bc124";
    
    @Override
    public ServerConfig provide(int lineNum, ClientAppService clientAppService)
    {
        ServerConfig resultConfig = emptyConfig;
        //尝试从远程去获取配置信息
        String jsonStr = HttpUtils.doGet(url);
        if (StringUtils.isNotEmpty(jsonStr))
        {
            try
            {
                JSONArray jsonArray = new JSONArray(jsonStr);
                if (jsonArray != null && jsonArray.length() > 0)
                {
                    JSONObject jsonObject = jsonArray.getJSONObject(lineNum - 1);
                    if (jsonObject != null)
                    {
                        //一旦获取到了，就将其存起来
                        resultConfig = new ServerConfig(jsonObject.getString("server"), jsonObject.getString("server_port"), jsonObject.getString("password"), jsonObject.getString("method"), new Date().toLocaleString());
                        lastSuccessConfigMap.put(lineNum, resultConfig);
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        //由于种种原因，这边依然有可能获取不到在线的配置信息（由于服务超时啊、网络断开啊等等原因）
        if (resultConfig == emptyConfig)
        {
            //那么，就沿用最后一次的有效配置
            if (lastSuccessConfigMap.get(lineNum) != null)
            {
                resultConfig = lastSuccessConfigMap.get(lineNum);
            }
        }
        return resultConfig;
    }
    
}
