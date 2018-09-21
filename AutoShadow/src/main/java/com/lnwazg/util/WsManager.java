package com.lnwazg.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.lnwazg.bean.ServerConfig;
import com.lnwazg.swing.util.WinMgr;
import com.lnwazg.ws.WsRequest;
import com.lnwazg.ws.WsResponse;

/**
 * webservice的管理器
 * @author Administrator
 * @version 2016年1月17日
 */
public class WsManager
{
    /**
     * 读取当前的线路信息
     * @author Administrator
     * @return
     */
    public static String readCurLine()
    {
        WsResponse response = new WsRequest("A10006").addParam("action", 1).send();
        if (response.isOk())
        {
            String line = response.getAsString("result");
            return line;
        }
        return null;
    }
    
    /**
     * 从服务器中读取出当前要用的线路的信息
     * @author nan.li
     * @param curSelLine
     * @return
     */
    public static ServerConfig readConfigFromNet(String curSelLine)
    {
        WsResponse response = new WsRequest("A10006").addParam("action", 4).addParam("line", curSelLine).send();
        if (response.isOk())
        {
            JsonElement jsonElement = response.get("result");
            ServerConfig result = new Gson().fromJson(jsonElement, new TypeToken<ServerConfig>()
            {
            }.getType());
            return result;
        }
        return null;
    }
    
    /**
     * 写入当前的线路信息
     * @author Administrator
     * @param cmd
     */
    public static void writeCurLine(String cmd)
    {
        new WsRequest("A10006").addParam("action", 2).addParam("line", cmd).send();
    }
    
    /**
     * 保存配置信息
     * @author Administrator
     * @param key
     * @param value
     */
    public static void saveConfig(String key, String value)
    {
        new WsRequest("S00001").addParam("appId", WinMgr.appId).addParam("handle", 3).addParam("key", key).addParam("value", value).send();
    }
}
