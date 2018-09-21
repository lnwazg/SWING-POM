package com.lnwazg.util.sp.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.lnwazg.bean.ServerConfig;
import com.lnwazg.util.sp.LineProvider;
import com.lnwazg.ws.service.ClientAppService;

public class IshadowLineProvider implements LineProvider
{
    @Override
    public ServerConfig provide(int lineNum, ClientAppService clientAppService)
    {
        lineNum = (lineNum % 3 == 0 ? 3 : lineNum % 3);//取余，让所有的结果都落在1、2、3这里面
        
        ServerConfig resultConfig = emptyConfig;
        String url = "http://www.ishadowsocks.org/";
        int TIMEOUT_MILLSECONDS = Integer.parseInt(clientAppService.getConfig("JSOUP_TIMEOUT"));
        try
        {
            Document doc = Jsoup.connect(url.trim())
                .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2062.124 Safari/537.36")
                .timeout(TIMEOUT_MILLSECONDS)
                .get();
            Element ele = doc.select("#free div.col-sm-4.text-center").get(lineNum - 1);
            Map<String, String> m = new HashMap<String, String>();
            m.put("1", "A");
            m.put("2", "B");
            m.put("3", "C");
            resultConfig = new ServerConfig(ele.child(0).text().substring((m.get(lineNum + "") + "服务器地址:").length()),
                ele.child(1).text().substring("端口:".length()), ele.child(2).text().substring((m.get(lineNum + "") + "密码:").length()),
                ele.child(3).text().substring("加密方式:".length()), new Date().toLocaleString());
            lastSuccessConfigMap.put(lineNum, resultConfig);
        }
        catch (Exception e)
        {
            //这边极有可能因为网络的问题而导致并不是每一次都能成功访问到配置项
            //因此，必须要考虑到异常情况下的处理
            if (lastSuccessConfigMap.get(lineNum) != null)
            {
                resultConfig = lastSuccessConfigMap.get(lineNum);
            }
            else
            {
                resultConfig = emptyConfig;
            }
            e.printStackTrace();
        }
        return resultConfig;
    }
    
}
