package com.lnwazg.mqctrl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.lnwazg.kit.controllerpattern.Controller;
import com.lnwazg.kit.describe.D;
import com.lnwazg.mq.framework.BaseController;

@Controller("/stat")
public class Statistics extends BaseController
{
    static Map<String, Integer> allMap = new HashMap<>();
    
    /**
     * 汇总统计
     * @author nan.li
     * @param paramMap
     */
    void count()
    {
        Entry<String, String> entry = paramMap.entrySet().iterator().next();
        String key = entry.getKey();
        int value = Integer.valueOf(entry.getValue());
        
        if (!allMap.containsKey(key))
        {
            allMap.put(key, 0);
        }
        allMap.put(key, allMap.get(key) + value);
        System.out.println("当前的统计结果是： ");
        D.d(allMap);
    }
    
}
