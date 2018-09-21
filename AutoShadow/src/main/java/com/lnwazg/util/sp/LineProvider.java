package com.lnwazg.util.sp;

import java.util.HashMap;
import java.util.Map;

import com.lnwazg.bean.ServerConfig;
import com.lnwazg.ws.service.ClientAppService;

/**
 * 线路信息提供商
 * @author nan.li
 * @version 2017年1月13日
 */
public interface LineProvider
{
    static ServerConfig emptyConfig = new ServerConfig("empty", "8989", "", "aes-256-cfb", "此路不通！");
    
    /**
     * 上一次成功访问到的配置信息数据
     */
    static Map<Integer, ServerConfig> lastSuccessConfigMap = new HashMap<Integer, ServerConfig>();
    
    /**
     * 提供某条线路的连接信息
     * @author nan.li
     * @param lineNum  传入的参数分别是：1、2、3
     * @param clientAppService 
     * @return
     */
    ServerConfig provide(int lineNum, ClientAppService clientAppService);
}
