package com.lnwazg.job;

import org.quartz.JobExecutionContext;

import com.lnwazg.swing.util.WinMgr;
import com.lnwazg.swing.util.quartz.ControlableJob;
import com.lnwazg.ui.MainFrame;

/**
 * 同步文件夹的工作<br>
 * 此处已经增加了对目标磁盘是否存在的判断！
 * @author Administrator
 * @version 2016年1月17日
 */
//@Scheduled(cron = "0 0,20,40 * * * ?")
public class SyncFolderJob extends ControlableJob
{
    @Override
    public void executeCustom(JobExecutionContext context)
    {
        WinMgr.win(MainFrame.class).syncFolderBtn.doClick();
    }
}
