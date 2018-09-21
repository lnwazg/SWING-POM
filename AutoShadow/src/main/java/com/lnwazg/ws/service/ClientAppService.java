package com.lnwazg.ws.service;

import java.util.Map;

import com.lnwazg.kit.property.PropertyUtils;
import com.lnwazg.swing.util.uiloader.LocalUiLoader;

public class ClientAppService
{
    /**
     * appId没有被用到，仅仅作为适配使用
     * @author nan.li
     * @param appId
     * @return
     */
    public Map<String, String> getConfigMap(int appId)
    {
        return PropertyUtils.load(LocalUiLoader.CONFIG_FILE_DIR + LocalUiLoader.CONFIG_FILE_NAME);
    }
    
    public String getConfig(int appId, String key)
    {
        return PropertyUtils.get(LocalUiLoader.CONFIG_FILE_DIR + LocalUiLoader.CONFIG_FILE_NAME, key);
    }
    
    public String getConfig(String key)
    {
        return PropertyUtils.get(LocalUiLoader.CONFIG_FILE_DIR + LocalUiLoader.CONFIG_FILE_NAME, key);
    }
    
    public void setConfig(int appId, String key, String value)
    {
        PropertyUtils.set(LocalUiLoader.CONFIG_FILE_DIR + LocalUiLoader.CONFIG_FILE_NAME, key, value);
    }
}
