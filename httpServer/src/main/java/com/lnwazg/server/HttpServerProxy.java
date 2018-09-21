package com.lnwazg.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.lnwazg.httpkit.Constants;
import com.lnwazg.httpkit.server.HttpServer;

/**
 * 服务器代理类
 * @author nan.li
 * @version 2016年11月28日
 */
public class HttpServerProxy
{
    private Integer localPort = 0;
    
    private HttpServer server;
    
    /**
     * 构造函数<br>
     * 初始化一些绑定的数据
     * @param port
     */
    private HttpServerProxy(Integer port)
    {
        localPort = port;
    }
    
    public void start()
    {
        try
        {
            server = HttpServer.bind(localPort);
            server.setContextPath("root");
//                        server.packageSearchAndInit("com.lnwazg.main.ctrl");
            server.autoSearchThenAddWatchResourceDirRoute();
            server.addFreemarkerPageDirRoute("web", Constants.DEFAULT_WEB_RESOURCE_BASE_PATH);
            //监听在这个端口处
            server.listen();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public void shutdown()
    {
        if (server != null)
        {
            server.shutdown();
        }
    }
    
    /**
     * 实例表
     */
    private static Map<Integer, HttpServerProxy> INSTANCE_MAP = new HashMap<>();
    
    public static synchronized HttpServerProxy getInstance(Integer port)
    {
        if (INSTANCE_MAP.get(port) == null)
        {
            INSTANCE_MAP.put(port, new HttpServerProxy(port));
        }
        return INSTANCE_MAP.get(port);
    }
}
