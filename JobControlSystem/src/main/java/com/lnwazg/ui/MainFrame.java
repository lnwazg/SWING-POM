package com.lnwazg.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;

import com.lnwazg.dbkit.jdbc.MyJdbc;
import com.lnwazg.dbkit.tools.db.collection.DbHashMap;
import com.lnwazg.entity.ExtraRemindThings;
import com.lnwazg.kit.executor.ExecMgr;
import com.lnwazg.kit.list.Lists;
import com.lnwazg.kit.log.Logs;
import com.lnwazg.kit.shell.CmdUtils;
import com.lnwazg.kit.shell.CmdUtils.FailCallback;
import com.lnwazg.kit.shell.CmdUtils.SuccessCallback;
import com.lnwazg.kit.swing.SwingDialogKit;
import com.lnwazg.kit.swing.anno.ShortCutKey;
import com.lnwazg.kit.swing.ui.comp.SmartButton;
import com.lnwazg.swing.util.WinMgr;
import com.lnwazg.swing.util.ui.IOS7SwitchButton;
import com.lnwazg.swing.util.ui.IOS7SwitchButton.OffCallBack;
import com.lnwazg.swing.util.ui.IOS7SwitchButton.OnCallBack;
import com.lnwazg.swing.xmlbuilder.XmlJFrame;
import com.lnwazg.swing.xmlbuilder.anno.XmlBuild;
import com.lnwazg.util.Constant;
import com.lnwazg.util.ExtraRemindKit;
import com.lnwazg.util.BaiduYunNoteSyncTool;

/**
 * 主窗体
 * @author Administrator
 * @version 2016年2月12日
 */
@XmlBuild("JobControlSystem.xml")
public class MainFrame extends XmlJFrame
{
    private static final long serialVersionUID = 416736654918898426L;
    
    /**
     * 文件夹同步按钮
     */
    public SmartButton syncFolderBtn;
    
    /**
     * 更新E盘svn代码的按钮
     */
    public SmartButton updateSvnBtn;
    
    /**
     * 提交E盘svn代码的按钮
     */
    public SmartButton commitSvnBtn;
    
    /**
     * 一键执行全部按钮
     */
    private SmartButton doAllBtn;
    
    /**
     * 工作开关按钮
     */
    private IOS7SwitchButton switchBtn;
    
    /**
     * 智能提醒的开关
     */
    @ShortCutKey(KeyEvent.VK_A)
    public IOS7SwitchButton popupMsgSwitchBtn;
    
    /**
     * 一句话箴言的提醒开关<br>
     * 默认就是打开着的，这个开关的记录也不需要记录到本地文件中，每次软件启动中默认为true。只是当不想要提醒的时候，才关闭之，并且是瞬时的<br>
     * 软件下次重启的时候依然会是打开的！
     */
    @ShortCutKey(KeyEvent.VK_S)
    public IOS7SwitchButton realWordsSwitchBtn;
    
    /**
     * 状态栏
     */
    private JLabel status;
    
    /**
     * 所有任务的按钮列表
     */
    private List<JButton> allTaskBtns = new ArrayList<JButton>();
    
    /**
     * 打呵欠记录下来
     */
    @ShortCutKey(KeyEvent.VK_Q)
    private SmartButton yawnRecord;
    
    /**
     * 今天提醒
     */
    @ShortCutKey(KeyEvent.VK_1)
    private SmartButton todayTask;
    
    /**
     * 明天提醒
     */
    @ShortCutKey(KeyEvent.VK_2)
    private SmartButton tomorrowTask;
    
    /**
     * 后天提醒
     */
    @ShortCutKey(KeyEvent.VK_3)
    private SmartButton afterTomorrowTask;
    
    /**
     * 关机按钮
     */
    @ShortCutKey(KeyEvent.VK_F1)
    @ShortCutKey(KeyEvent.VK_F2)
    @ShortCutKey(KeyEvent.VK_F9)
    private SmartButton shutdownBtn;
    
    @ShortCutKey(KeyEvent.VK_N)
    private SmartButton syncBaiduNotes;
    
    /**
     * 额外提醒事项<br>
     * key为日期字符串，值为事务列表
     */
    //    public static Map<String, Set<String>> extraRemindThings = new HashMap<>();
    public static Map<String, Set<String>> extraRemindThings = null;
    
    @Override
    public void afterUIBind()
    {
        //必须在afterUIBind()之后初始化，防止配置文件没有被加载好就去读配置
        //初始化数据库连接
        MyJdbc myJdbc = WinMgr.getDefaultSqliteMyJdbc();
        //根据数据库连接创建DbHashMap对象，然后就可以像操作HashMap一样操作数据库
        extraRemindThings = new DbHashMap<>(myJdbc, ExtraRemindThings.class, String.class, Set.class);
        
        initEnv();
        initListeners();
        //启动的时候就需要点击一次“更新E盘svn代码”，目的是为了一到家打开电脑，就保证本地的代码已经自动同步到最新！
        //软件一启动便自动更新到最新，就像是一个幽灵，很给力很有力！
        updateSvnBtn.doClick();
    }
    
    /**
     * 初始化工作环境
     * @author Administrator
     */
    private void initEnv()
    {
        //每添加一个任务，就在此地注册一下新任务按钮即可
        allTaskBtns.addAll(Lists.asList(syncFolderBtn, updateSvnBtn, commitSvnBtn));
    }
    
    /**
     * 绑定一些容易操作的事件
     * @author Administrator
     */
    private void initListeners()
    {
        switchBtn.setOnCallback(new OnCallBack()
        {
            @Override
            public void call()
            {
                WinMgr.saveConfig("JOB_SWITCH", "true");
                WinMgr.jobExecSwitch = true;
            }
        });
        switchBtn.setOffCallback(new OffCallBack()
        {
            @Override
            public void call()
            {
                WinMgr.saveConfig("JOB_SWITCH", "false");
                WinMgr.jobExecSwitch = false;
            }
        });
        //switchBtn默认就是true的状态
        switchBtn.setStatus(Boolean.valueOf(WinMgr.configs.get("JOB_SWITCH")));
        
        //任务按钮
        syncFolderBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                //点击按钮的事件
                //工作文件夹U盘同步
                //（预先拷贝过去）首先检查软件是否安装了，如果没安装则提示安装，当确定后，弹出安装程序。
                //如果已经安装了，则执行一次cmd命令，进行一次文件夹同步
                //换成singleExec之后，便完美解决了每次只能执行一次同步的问题
                ExecMgr.singleExec.execute(() -> {
                    String syncInstallPath = WinMgr.configs.get("syncInstallPath");
                    File syncInstallPathFile = new File(syncInstallPath);
                    if (!syncInstallPathFile.exists())
                    {
                        Logs.i("同步软件尚未安装，开启安装程序...");
                        //找到用户目录中的那个文件
                        String line = WinMgr.getUserDirFilePath("SyncToySetupPackage_v21_x86.exe");
                        CmdUtils.execute(line);
                    }
                    
                    //同步之前先做一个移动硬盘的检测工作，否则同步没有意义
                    if (!new File(Constant.REMOVEABLE_HARD_DISK_DRIVE_DIR).exists())
                    {
                        status.setText("移动硬盘不存在，同步任务已取消。");
                        return;
                    }
                    
                    ExecMgr.guiExec.execute(new Runnable()
                    {
                        public void run()
                        {
                            status.setText("开始执行文件夹同步任务...");
                        }
                    });
                    
                    //已经安装了，则执行一次cmd命令，进行一次文件夹同步
                    String line = String.format("%s -R", syncInstallPathFile);//run all syncs
                    CmdUtils.execute(line, new int[] {0, 2}, new SuccessCallback()
                    {
                        @Override
                        public void execute()
                        {
                            ExecMgr.guiExec.execute(new Runnable()
                            {
                                public void run()
                                {
                                    status.setText("Sync OK @ " + new Date().toLocaleString());
                                }
                            });
                        }
                    }, new FailCallback()
                    {
                        @Override
                        public void execute(final Exception e)
                        {
                            ExecMgr.guiExec.execute(new Runnable()
                            {
                                public void run()
                                {
                                    status.setText("Sync Fail @ " + new Date().toLocaleString());
                                    Logs.e("Sync Fail @ " + new Date().toLocaleString(), e, true);
                                }
                            });
                        }
                    });
                });
            }
        });
        updateSvnBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ExecMgr.singleExec.execute(() -> {
                    CmdUtils.execute(Constant.SVN_WORK_BASEPATH + "/update.bat", new int[] {0}, new SuccessCallback()
                    {
                        @Override
                        public void execute()
                        {
                            ExecMgr.guiExec.execute(new Runnable()
                            {
                                public void run()
                                {
                                    status.setText("SVN更新成功@" + new Date().toLocaleString());
                                    //                                    WinMgr.showTrayMessage("SVN更新成功");
                                    //应该静默通知，不干扰正常的金句提醒
                                }
                            });
                        }
                    }, new FailCallback()
                    {
                        @Override
                        public void execute(final Exception e)
                        {
                            ExecMgr.guiExec.execute(new Runnable()
                            {
                                public void run()
                                {
                                    status.setText("SVN更新失败@" + new Date().toLocaleString());
                                    Logs.e("SVN更新失败@" + new Date().toLocaleString(), e, true);
                                    //                                    WinMgr.showTrayMessage("SVN更新失败");
                                    //应该静默通知，不干扰正常的金句提醒
                                }
                            });
                        }
                    }, (errMsg) -> {
                        if (StringUtils.isNotEmpty(errMsg) && errMsg.indexOf("'svn' 不是内部或外部命令") != -1)
                        {
                            ExecMgr.guiExec.execute(new Runnable()
                            {
                                public void run()
                                {
                                    String msg = "未安装svn命令行客户端！SVN提交失败！";
                                    status.setText(msg);
                                    WinMgr.showTrayMessage(msg);
                                    //但是如果更新失败了的时候冒泡提醒，还是很有必要的！
                                }
                            });
                        }
                    });
                });
            }
        });
        commitSvnBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ExecMgr.singleExec.execute(() -> {
                    CmdUtils.execute(Constant.SVN_WORK_BASEPATH + "/commit.bat", new int[] {0}, new SuccessCallback()
                    {
                        @Override
                        public void execute()
                        {
                            ExecMgr.guiExec.execute(new Runnable()
                            {
                                public void run()
                                {
                                    status.setText("SVN提交成功@" + new Date().toLocaleString());
                                    //                                    WinMgr.showTrayMessage("SVN提交成功");
                                    //因为提交的频率太高，因此不适合总是冒泡通知，而是应该静默通知
                                }
                            });
                        }
                    }, new FailCallback()
                    {
                        @Override
                        public void execute(final Exception e)
                        {
                            ExecMgr.guiExec.execute(new Runnable()
                            {
                                public void run()
                                {
                                    status.setText("SVN提交失败@" + new Date().toLocaleString());
                                    Logs.e("SVN提交失败@" + new Date().toLocaleString(), e, true);
                                    //                                    WinMgr.showTrayMessage("SVN提交失败");
                                    //因为提交的频率太高，因此不适合总是通知，而是应该静默通知
                                }
                            });
                        }
                    }, (errMsg) -> {
                        if (StringUtils.isNotEmpty(errMsg) && errMsg.indexOf("'svn' 不是内部或外部命令") != -1)
                        {
                            ExecMgr.guiExec.execute(new Runnable()
                            {
                                public void run()
                                {
                                    String msg = "未安装svn命令行客户端！SVN提交失败！";
                                    status.setText(msg);
                                    WinMgr.showTrayMessage(msg);
                                    //但是提交失败了的时候冒泡提醒，还是很有必要的！
                                }
                            });
                        }
                    });
                });
            }
        });
        
        //一键执行全部按钮
        doAllBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                for (JButton jButton : allTaskBtns)
                {
                    jButton.doClick();
                }
            }
        });
        
        //记录下打呵欠
        yawnRecord.addActionListener(e -> {
            //如果今天打呵欠了，那么要提醒今晚早睡！
            //添加到一个全局变量中，让SmartTaskRemindJob去自动加入
            //全局变量的结构：Map<String, Set<String>>     key为日期字符串，也就是每天只对应一个Set
            //应用重启后内存数据消失
            //既然是Set，那么就不用担心Set里面的数据重复了！
            ExtraRemindKit.addTodayRemind("今晚必须深度睡眠！");
            ExtraRemindKit.addTomorrowRemind("今晚必须深度睡眠！");
            SwingDialogKit.showMessageDialog(this, "系统已记录本次点击，会在合适的时候提醒你。下次记得要早睡哦！", "记录打呵欠", JOptionPane.PLAIN_MESSAGE);
        });
        
        todayTask.addActionListener(e -> {
            String title = "添加今天提醒";
            String content = SwingDialogKit.showInputDialog(this, "请输入你的提醒内容：", title, JOptionPane.PLAIN_MESSAGE);
            if (StringUtils.isEmpty(content))
            {
                // SwingDialogKit.showMessageDialog(this, "内容不能为空！", title, JOptionPane.ERROR_MESSAGE);
            }
            else
            {
                ExtraRemindKit.addTodayRemind(content);
                SwingDialogKit.showMessageDialog(this, "提醒已添加", title, JOptionPane.PLAIN_MESSAGE);
            }
        });
        tomorrowTask.addActionListener(e -> {
            String title = "添加明天提醒";
            String content = SwingDialogKit.showInputDialog(this, "请输入你的提醒内容：", title, JOptionPane.PLAIN_MESSAGE);
            if (StringUtils.isEmpty(content))
            {
                // SwingDialogKit.showMessageDialog(this, "内容不能为空！", title, JOptionPane.ERROR_MESSAGE);
            }
            else
            {
                ExtraRemindKit.addTomorrowRemind(content);
                SwingDialogKit.showMessageDialog(this, "提醒已添加", title, JOptionPane.PLAIN_MESSAGE);
            }
        });
        afterTomorrowTask.addActionListener(e -> {
            String title = "添加后天提醒";
            String content = SwingDialogKit.showInputDialog(this, "请输入你的提醒内容：", title, JOptionPane.PLAIN_MESSAGE);
            if (StringUtils.isEmpty(content))
            {
                // SwingDialogKit.showMessageDialog(this, "内容不能为空！", title, JOptionPane.ERROR_MESSAGE);
            }
            else
            {
                ExtraRemindKit.addAfterTomorrowRemind(content);
                SwingDialogKit.showMessageDialog(this, "提醒已添加", title, JOptionPane.PLAIN_MESSAGE);
            }
        });
        syncBaiduNotes.addActionListener(e -> {
            String title = "百度云笔记同步";
            BaiduYunNoteSyncTool.process();
            SwingDialogKit.showMessageDialog(this, "同步完毕！", title, JOptionPane.PLAIN_MESSAGE);
        });
        
        shutdownBtn.addActionListener(e -> {
            int result = SwingDialogKit.showConfirmDialog(this, "即将为你提交代码并关机，确定继续吗？", "请注意！", JOptionPane.WARNING_MESSAGE);
            if (JOptionPane.OK_OPTION == result)
            {
                Logs.i("即将为你提交代码并关机！");
                commitSvnBtn.doClick();
                Logs.i("正在关机请稍后...");
                //调用脚本关机
                new Thread(() -> {
                    try
                    {
                        //5秒后自动关机。。。
                        TimeUnit.SECONDS.sleep(5);
                        Runtime.getRuntime().exec("shutdown -s -t 0");
                        
                    }
                    catch (Exception e1)
                    {
                        e1.printStackTrace();
                    }
                }).start();
                
                //因为这个对话框会阻塞后续代码的执行，所以将其放到最后面！
                SwingDialogKit.showMessageDialog(this, "正在为你提交代码并关机...", "关机提示", JOptionPane.PLAIN_MESSAGE);
            }
            else
            {
                //已取消，啥都不要做！
            }
        });
    }
}
