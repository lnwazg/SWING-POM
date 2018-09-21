package com.lnwazg.job;

import org.quartz.JobExecutionContext;

import com.lnwazg.kit.job.Scheduled;
import com.lnwazg.swing.util.WinMgr;
import com.lnwazg.swing.util.quartz.ControlableJob;
import com.lnwazg.ui.MainFrame;

/**
 * 自动提交SVN<br>
 * 每20分钟自动提交一次，避免遗漏而导致的工作内容无法有效同步
 * @author nan.li
 * @version 2017年5月8日
 */
@Scheduled(cron = "0 0/20 0-23 * * ?")
public class CommitSvnJob extends ControlableJob
{
    @Override
    public void executeCustom(JobExecutionContext context)
    {
        WinMgr.win(MainFrame.class).commitSvnBtn.doClick();
    }
}
