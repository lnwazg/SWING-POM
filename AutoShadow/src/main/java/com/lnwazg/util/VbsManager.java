package com.lnwazg.util;

import com.lnwazg.kit.shell.CmdUtils;

/**
 * VBS脚本执行管理器
 * @author nan.li
 * @version 2016年8月30日
 */
public class VbsManager
{
    /**
     * 启动代理工具
     * @author nan.li
     * @param installBaseDir
     */
    public static void startSSClientSoftware(String installBaseDir)
    {
        CmdUtils.execute(String.format("cscript %sstart.vbs", installBaseDir));
    }
    
    /**
     * 重启一次客户端软件<br>
     * 自适应能力更强，可以替代startSSClientSoftware()
     * @author nan.li
     */
    public static void restartSSClientSoftware(String installBaseDir)
    {
        CmdUtils.execute(String.format("cscript %srestart.vbs", installBaseDir));
    }
    
    /**
     * 先杀死某个exe进程
     * @author Administrator
     * @param installBaseDir
     */
    public static void stopSSClientSoftware(String installBaseDir)
    {
        CmdUtils.execute(String.format("cscript %sstop.vbs", installBaseDir));
    }
}
