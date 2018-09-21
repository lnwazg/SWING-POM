package com.lnwazg.tool;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.CharEncoding;

import com.lnwazg.dbkit.tools.bi.BiEngine;
import com.lnwazg.dbkit.tools.bi.bean.BiMetaInfo;
import com.lnwazg.kit.gson.GsonKit;
import com.lnwazg.kit.log.Logs;

/**
 * 处理器
 * @author nan.li
 * @version 2017年12月21日
 */
public class Processor
{
    /**
     * 业务逻辑处理
     * @author nan.li
     * @param file
     */
    public static void handle(File file)
    {
        try
        {
            Logs.i("开始处理BI计算...\n==============================\n");
            
            //读取
            BiMetaInfo biMetaInfo = GsonKit.prettyGson.fromJson(FileUtils.readFileToString(file, CharEncoding.UTF_8), BiMetaInfo.class);
            //解析这个对象，自上而下去解析执行
            BiEngine.process(biMetaInfo);
        }
        catch (IOException e)
        {
            Logs.e(e);
        }
    }
}
