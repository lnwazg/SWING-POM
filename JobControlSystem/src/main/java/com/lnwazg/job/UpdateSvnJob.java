package com.lnwazg.job;

import org.quartz.JobExecutionContext;

import com.lnwazg.kit.job.Scheduled;
import com.lnwazg.swing.util.WinMgr;
import com.lnwazg.swing.util.quartz.ControlableJob;
import com.lnwazg.ui.MainFrame;

/**
 * 更新SVN
 * @author nan.li
 * @version 2017年5月8日
 */
@Scheduled(cron = "0 0/20 8,9 * * ?")
public class UpdateSvnJob extends ControlableJob
{
    @Override
    public void executeCustom(JobExecutionContext context)
    {
        WinMgr.win(MainFrame.class).updateSvnBtn.doClick();
    }
}
