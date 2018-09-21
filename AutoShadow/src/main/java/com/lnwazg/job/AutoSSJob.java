package com.lnwazg.job;

import java.util.Date;

import org.quartz.JobExecutionContext;

import com.lnwazg.bean.ServerConfig;
import com.lnwazg.kit.job.Scheduled;
import com.lnwazg.kit.log.Logs;
import com.lnwazg.swing.util.WinMgr;
import com.lnwazg.swing.util.quartz.ControlableJob;
import com.lnwazg.ui.MainFrame;
import com.lnwazg.util.VbsManager;
import com.lnwazg.util.WsManager;
import com.lnwazg.ws.service.ClientAppService;

/**
 * 定时检测远程的SS账号密码是否有更新
 * @author Administrator
 * @version 2016年1月17日
 */
@Scheduled(cron = "0 * * * * ?")
public class AutoSSJob extends ControlableJob
{
    ClientAppService clientAppService = new ClientAppService();
    
    @Override
    public void executeCustom(JobExecutionContext context)
    {
        //获取窗体的实例
        MainFrame m = WinMgr.win(MainFrame.class);
        //获取窗体实例中的某个字段的信息
        String curSelLine = m.curSelLine;
        String lineProvider = clientAppService.getConfig("lineProvider");
        
        //只有1\2\3号线路需要检测，其余的均忽略检测
        //并且只有当lineProvider是0号或者1号的时候才需要检测，2号是随机lineProvider，不可以检测
        if (("1".equals(curSelLine) || "2".equals(curSelLine) || "3".equals(curSelLine)) && ("0".equals(lineProvider) || "1".equals(lineProvider)))
        {
            Logs.i("定期检查网站上的最新配置信息在" + new Date().toLocaleString() + "...");
            //读取本地现有的配置信息
            ServerConfig fileConfig = m.readFromSSConfigFile();
            //读取当前远程网络里的配置信息
            ServerConfig netConfig = WsManager.readConfigFromNet(curSelLine);
            //两者进行比较，若不相同
            if (!netConfig.equals(fileConfig))
            {
                Logs.i("检测到网站上的配置已经变更，即将下载最新配置到本地...");
                
                Logs.d("fileConfig: " + fileConfig);
                Logs.d("netConfig: " + netConfig);
                
                //先将最新的网络配置追加显示
                m.showNewConfigInfo(netConfig);
                //然后将最新的配置信息应用到本地的ss软件中
                m.saveSSConfigFile(netConfig);
                //重启本地的ss客户端，令新配置生效
                VbsManager.restartSSClientSoftware(m.installBaseDir);
            }
        }
    }
}
