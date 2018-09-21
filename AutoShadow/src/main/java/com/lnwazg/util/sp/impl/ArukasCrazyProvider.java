package com.lnwazg.util.sp.impl;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.lnwazg.bean.ServerConfig;
import com.lnwazg.kit.http.HttpUtils;
import com.lnwazg.kit.log.Logs;
import com.lnwazg.util.sp.LineProvider;
import com.lnwazg.ws.service.ClientAppService;

public class ArukasCrazyProvider implements LineProvider
{
    static String[] urls = {"https://superss.arukascloud.io/2ac56b41-0592-403e-9948-25faef4bc124",
        "https://superss.arukascloud.io/f2349484-5846-4f55-af57-4e84047186a3", "https://superss.arukascloud.io/e7531e04-3c5b-49a7-b124-4208efb7abf6"};
    
    @Override
    public ServerConfig provide(int lineNum, ClientAppService clientAppService)
    {
        ServerConfig resultConfig = emptyConfig;
        //尝试从远程去获取配置信息
        int choosedUrlNum = RandomUtils.nextInt(urls.length);
        Logs.i(String.format("随机选中%d号 URL线路...", choosedUrlNum));
        String jsonStr = HttpUtils.doGet(urls[choosedUrlNum]);
        if (StringUtils.isNotEmpty(jsonStr))
        {
            try
            {
                JSONArray jsonArray = new JSONArray(jsonStr);
                if (jsonArray != null && jsonArray.length() > 0)
                {
                    int length = jsonArray.length();
                    Logs.i(String.format("当前共有%d条翻墙账号可供选则...", length));
                    int choosedAccountNum = RandomUtils.nextInt(length);
                    Logs.i(String.format("选定第%d条翻墙账号...", choosedAccountNum));
                    JSONObject jsonObject = jsonArray.getJSONObject(choosedAccountNum);
                    if (jsonObject != null)
                    {
                        //一旦获取到了，就将其存起来
                        resultConfig = new ServerConfig(jsonObject.getString("server"), jsonObject.getString("server_port"), jsonObject.getString("password"),
                            jsonObject.getString("method"), String.format("随机到线路%s-账号%s/%s", choosedUrlNum + 1, choosedAccountNum + 1, length));
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
