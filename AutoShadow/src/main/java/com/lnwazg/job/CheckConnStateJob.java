package com.lnwazg.job;

import org.quartz.JobExecutionContext;

import com.lnwazg.kit.job.Scheduled;
import com.lnwazg.swing.util.WinMgr;
import com.lnwazg.swing.util.quartz.ControlableJob;
import com.lnwazg.ui.MainFrame;

/**
 * 检查代理的连通性
 * @author nan.li
 * @version 2015-10-5
 */
@Scheduled(cron = "0/15 * * * * ?")
public class CheckConnStateJob extends ControlableJob
{
    @Override
    public void executeCustom(JobExecutionContext context)
    {
        WinMgr.win(MainFrame.class).showLineState();
    }
}
