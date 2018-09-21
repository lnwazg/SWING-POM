package com.lnwazg.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.lnwazg.bean.ServerConfig;
import com.lnwazg.kit.executor.ExecMgr;
import com.lnwazg.kit.http.ProxyState;
import com.lnwazg.kit.http.ProxyUtils;
import com.lnwazg.kit.io.StreamUtils;
import com.lnwazg.kit.log.Logs;
import com.lnwazg.kit.swing.ui.comp.NonBorderButton;
import com.lnwazg.swing.util.WinMgr;
import com.lnwazg.swing.util.uiloader.LocalUiLoader;
import com.lnwazg.util.IconMgr;
import com.lnwazg.util.VbsManager;
import com.lnwazg.util.WsManager;

/**
 * 主窗口
 * 将翻墙自适应化！
 * 这个类，应该就是进化了一半的类吧！这个类，虽然用了swingbase，却还没有用到注解！这是个发展中的过渡品哦，挺有意思的！
 * @author nan.li
 * @version 2014-12-10
 */
public class MainFrame extends JFrame
{
    private static final long serialVersionUID = -1L;
    
    /**
     * 几个小容器
     */
    JPanel textPanel, handlePanel, statusPanel;
    
    /**
     * 显示最新线路信息的文本区
     */
    JTextPane text;
    
    /**
     * 12个按钮
     */
    JButton btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9, btn10, btn11, btn12;
    
    /**
     * 自动切换的开关按钮
     */
    JButton autoSwitchBtn;
    
    /**
     * 12个按钮的数组
     */
    JButton[] switchBtns;
    
    /**
     * 文本区的滚动条
     */
    JScrollPane paneScrollPane;
    
    /**
     * 标签
     */
    JLabel statusLabel, autoSwitchLabel;
    
    /**
     * 当前选择的线路
     */
    public String curSelLine = "-1";
    
    public String lastSelLine = "-1";
    
    /**
     * 任务栏图标
     */
    TrayIcon trayIcon;
    
    /**
     * 是否自动切换线路
     */
    boolean autoSwitchLine = false;
    
    /**
     * ss检测连续成功次数
     */
    AtomicInteger totalSuccessCount = new AtomicInteger(0);
    
    /**
     * ss检测连续失败次数
     */
    AtomicInteger continuousFailCount = new AtomicInteger(0);
    
    /**
     * 自动切换的线路数组
     */
    private String[] autoSwitchLineSeq;
    
    /**
     * 是否首次启动状态
     */
    private boolean firstTime = true;
    
    /**
     * 安装目录
     */
    public String installBaseDir;
    
    File installBaseDirFile;
    
    public MainFrame()
    {
        //将项目所需要的软件全部拷贝到指定目录
        prepareSoftwareEnv();
        
        //自定义测试地址
        ProxyUtils.CUSTOM_PING_URL = WinMgr.configs.get("CUSTOM_PING_URL");
        
        //设置主窗体的布局
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        //主窗体的图标
        setIconImage(IconMgr.icon);
        
        //文本区的容器
        textPanel = new JPanel();
        textPanel.setBorder(BorderFactory.createTitledBorder("ss状态"));
        
        text = new JTextPane();
        text.setEditable(false);
        text.setAutoscrolls(true);
        paneScrollPane = new JScrollPane(text);
        
        paneScrollPane.setPreferredSize(new Dimension(250, 175));
        textPanel.add(paneScrollPane);
        add(textPanel);
        
        //从配置中读取出12个按钮的title
        String[] btnTitles = WinMgr.configs.get("BTN_TITLES").trim().split(",");
        if (btnTitles == null || btnTitles.length != 12)
        {
            String errMsg = "Invalid BTN_TITLES config! Length not correct!";
            Logs.e(errMsg);
            throw new RuntimeException(errMsg);
        }
        
        //分别设置按钮的标题以及绑定的事件
        btn1 = new JButton(btnTitles[0]);
        btn1.setActionCommand("1");
        btn1.addActionListener(lineButtonListener);
        
        btn2 = new JButton(btnTitles[1]);
        btn2.setActionCommand("2");
        btn2.addActionListener(lineButtonListener);
        
        btn3 = new JButton(btnTitles[2]);
        btn3.setActionCommand("3");
        btn3.addActionListener(lineButtonListener);
        
        btn4 = new JButton(btnTitles[3]);
        btn4.setActionCommand("4");
        btn4.addActionListener(lineButtonListener);
        
        btn5 = new JButton(btnTitles[4]);
        btn5.setActionCommand("5");
        btn5.addActionListener(lineButtonListener);
        
        btn6 = new JButton(btnTitles[5]);
        btn6.setActionCommand("6");
        btn6.addActionListener(lineButtonListener);
        
        btn7 = new JButton(btnTitles[6]);
        btn7.setActionCommand("7");
        btn7.addActionListener(lineButtonListener);
        
        btn8 = new JButton(btnTitles[7]);
        btn8.setActionCommand("8");
        btn8.addActionListener(lineButtonListener);
        
        btn9 = new JButton(btnTitles[8]);
        btn9.setActionCommand("9");
        btn9.addActionListener(lineButtonListener);
        
        btn10 = new JButton(btnTitles[9]);
        btn10.setActionCommand("10");
        btn10.addActionListener(lineButtonListener);
        
        btn11 = new JButton(btnTitles[10]);
        btn11.setActionCommand("11");
        btn11.addActionListener(lineButtonListener);
        
        btn12 = new JButton(btnTitles[11]);
        btn12.setActionCommand("12");
        btn12.addActionListener(lineButtonListener);
        
        handlePanel = new JPanel();
        handlePanel.setPreferredSize(new Dimension(0, 120));
        
        handlePanel.add(btn1);
        handlePanel.add(btn2);
        handlePanel.add(btn3);
        
        handlePanel.add(btn4);
        handlePanel.add(btn5);
        handlePanel.add(btn6);
        
        handlePanel.add(btn7);
        handlePanel.add(btn8);
        handlePanel.add(btn9);
        
        handlePanel.add(btn10);
        handlePanel.add(btn11);
        handlePanel.add(btn12);
        
        add(handlePanel);
        
        //将初始化好的这12个按钮放置到按钮数组中，便于后续的引用
        switchBtns = new JButton[] {btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9, btn10, btn11, btn12};//所有待切换的button均放在这里
        
        //状态栏
        statusPanel = new JPanel();
        statusPanel.setBorder(BorderFactory.createTitledBorder("线路状态"));
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
        statusLabel = new JLabel();
        statusLabel.setText("正在启动，连接中...");
        statusPanel.add(Box.createHorizontalStrut(10));
        statusPanel.add(statusLabel);
        
        //初始化为false
        autoSwitchBtn = new NonBorderButton(IconMgr.iconSwitchOff);
        autoSwitchBtn.setToolTipText("自动切换线路");
        
        //启动时加载自动切换线路的信息。
        //如果初始化的状态和配置的状态不一致，则需要手动触发一次！
        autoSwitchBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                JButton source = (JButton)(e.getSource());
                Icon currentIcon = source.getIcon();
                if (currentIcon == IconMgr.iconSwitchOn)
                {
                    source.setIcon(IconMgr.iconSwitchOff);
                    autoSwitchLine = false;
                    //从打开的那一刻起，应该开始重新计数！
                    totalSuccessCount.set(0);
                    continuousFailCount.set(0);
                }
                else
                {
                    source.setIcon(IconMgr.iconSwitchOn);
                    autoSwitchLine = true;
                }
                saveNetConfig("AUTO_SWITCH_LINE_TOGGLE", autoSwitchLine + "");
            }
        });
        
        //自动切换的线路顺序的配置的初始化
        autoSwitchLineSeq = WinMgr.configs.get("AUTO_SWITCH_LINE_STRATEGY").trim().split(",");
        if (autoSwitchLineSeq.length < 2)
        {
            String errMsg =
                "invalid config AUTO_SWITCH_LINE_STRATEGY: " + WinMgr.configs.get("AUTO_SWITCH_LINE_STRATEGY") + " ,seq length should be more than 2!";
            Logs.e(errMsg);
            throw new RuntimeException(errMsg);
        }
        //是否自动切换的按钮初始化
        boolean autoSwitchLineConfig = Boolean.valueOf(WinMgr.configs.get("AUTO_SWITCH_LINE_TOGGLE"));
        if (autoSwitchLineConfig != autoSwitchLine)
        {
            autoSwitchBtn.doClick();
        }
        
        statusPanel.add(Box.createHorizontalGlue());
        autoSwitchLabel = new JLabel("自动切换：");
        statusPanel.add(autoSwitchLabel);
        statusPanel.add(autoSwitchBtn);
        statusPanel.add(Box.createHorizontalStrut(10));
        add(statusPanel);
        
        //设置托盘图标所需要的事件
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                setExtendedState(JFrame.ICONIFIED);//最小化，并且不可见了
            }
            
            @Override
            public void windowIconified(WindowEvent e)
            {
                setVisible(false);
            }
        });
        
        //设置托盘图标所需要的事件
        loadSystemTray();
        
        setResizable(false);//设置该窗体大小不可以修改
        
        pack();//打包！这一步很重要！否则很可能无法正常显示！
        
        //触发当前线路按钮的点击事件
        String curLine = WsManager.readCurLine();
        clickLineBtn(curLine);//主动触发一次点击
    }
    
    /**
     * 显示线路的ping状态<br>
     * 若线路状态不佳，则尝试切换到下一条预先指派的线路
     * @author nan.li
     */
    public void showLineState()
    {
        ExecMgr.cachedExec.execute(new Runnable()
        {
            @Override
            public void run()
            { //检测线路状态的同时，还要计算出是否有必要切换线路
                final ProxyState proxyState = ProxyUtils.checkLocalSSEazyDetail();
                String result = "";
                if (proxyState.isSuccess())
                {
                    result = "PING: " + proxyState.getCostTime() + " 毫秒";
                    if (autoSwitchLine)
                    {
                        //一旦成功，就将连续失败清零，连续成功+1
                        totalSuccessCount.addAndGet(1);
                        continuousFailCount.set(0);
                        Logs.d(String.format("totalSuccessCount:%s, continuousFailCount:%s", totalSuccessCount, continuousFailCount));
                    }
                    else
                    {
                        //Logs.d("autoSwitchLine OFF!");
                    }
                }
                else
                {
                    result = "服务暂时不可用";
                    if (autoSwitchLine)
                    {
                        continuousFailCount.addAndGet(1);
                        Logs.d(String.format("totalSuccessCount:%s, continuousFailCount:%s", totalSuccessCount, continuousFailCount));
                        //偶尔一次失败，不计入成功的总次数
                        boolean needSwitch = judge(totalSuccessCount, continuousFailCount);
                        Logs.d(String.format("needSwitch:%s", needSwitch));
                        if (needSwitch)
                        {
                            autoSwitchToNextLine();
                        }
                    }
                    else
                    {
                        //Logs.d("autoSwitchLine OFF!");
                    }
                }
                final String statusShowStr = result;
                ExecMgr.guiExec.execute(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        statusLabel.setText(statusShowStr);
                        long costTime = proxyState.getCostTime();
                        String stateText = "";
                        if (costTime >= 0 && costTime <= 1500)
                        {
                            setIconImage(IconMgr.iconOk3);
                            trayIcon.setImage(IconMgr.iconOk3);
                            stateText = "极佳";
                        }
                        else if (costTime > 1500 && costTime <= 5000)
                        {
                            setIconImage(IconMgr.iconOk2);
                            trayIcon.setImage(IconMgr.iconOk2);
                            stateText = "较好";
                        }
                        else if (costTime > 5000)
                        {
                            setIconImage(IconMgr.iconOk1);
                            trayIcon.setImage(IconMgr.iconOk1);
                            stateText = "一般";
                        }
                        else if (costTime < 0)
                        {
                            stateText = "未连接";
                            setIconImage(IconMgr.iconErr);
                            trayIcon.setImage(IconMgr.iconErr);
                        }
                        trayIcon.setToolTip(String.format("线路%s【%s】 %s", curSelLine, stateText, statusShowStr));
                    }
                });
            }
        });
    }
    
    /**
     * 判断是否需要切换线路了
     * @author nan.li
     * @param continuousSuccessCount
     * @param continuousFailCount
     * @return
     */
    private boolean judge(AtomicInteger continuousSuccessCount, AtomicInteger continuousFailCount)
    {
        /**
                        只要累计超过5次成功，就可以变成longWork状态！
         longwork状态下，需要连续失败5次，才需要切换到下一个！
                       非longwork状态下，连续失败2次，就需要切换到下一个！
                       此法可避免频繁无意义切换
        */
        if (continuousSuccessCount.get() >= 5)
        {
            //long work state
            if (continuousFailCount.get() >= Integer.valueOf(WinMgr.configs.get("LONG_WORK_FAIL_TO_SWITCH_COUNT")))
            {
                System.out.println(String.format("Longwork状态下连续失败指定次数：【%s】，需要切换到下一个线路！", WinMgr.configs.get("LONG_WORK_FAIL_TO_SWITCH_COUNT")));
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            //non long work state
            if (continuousFailCount.get() >= Integer.valueOf(WinMgr.configs.get("NON_LONG_WORK_FAIL_TO_SWITCH_COUNT")))
            {
                System.out.println(String.format("Non longwork状态下连续失败指定次数:【%s】，需要切换到下一个线路！", WinMgr.configs.get("NON_LONG_WORK_FAIL_TO_SWITCH_COUNT")));
                return true;
            }
            else
            {
                return false;
            }
        }
    }
    
    /**
     * 自动切换到下一条线路
     * @author nan.li
     */
    private void autoSwitchToNextLine()
    {
        for (int i = 0; i < autoSwitchLineSeq.length; i++)
        {
            if (autoSwitchLineSeq[i].equals(curSelLine))
            {
                //找到了！
                int targetIndex = 0;
                if (i == autoSwitchLineSeq.length - 1)
                {
                    //到达最后了
                    targetIndex = 0;
                }
                else
                {
                    targetIndex = i + 1;
                }
                clickLineBtn(autoSwitchLineSeq[targetIndex]);
                return;
            }
        }
        //没找到
        //那就切换到策略的第一个线路
        clickLineBtn(autoSwitchLineSeq[0]);
    }
    
    /**
     * 点击某一条线路的按钮
     * @author Administrator
     * @param lineNum
     */
    private void clickLineBtn(final String lineNum)
    {
        switchBtns[Integer.valueOf(lineNum) - 1].doClick();
    }
    
    /**
     * 12个按钮的监听器
     */
    ActionListener lineButtonListener = new ActionListener()
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            String cmd = e.getActionCommand();
            if (cmd.equals(curSelLine))
            {
                //如果当前选中的按钮未变化，那么就没必要做任何重复的操作
                return;
            }
            //记录下上次和本次点击的线路
            lastSelLine = curSelLine;
            curSelLine = cmd;
            WsManager.writeCurLine(curSelLine);
            
            refreshBtnSelectionAndScreen();
            switchLine();
        }
    };
    
    /**
     * 显示线路的信息
     * @author Administrator
     * @param msg
     */
    protected void showLineMessage(final String msg)
    {
        ExecMgr.guiExec.execute(new Runnable()
        {
            @Override
            public void run()
            {
                Document doc = text.getDocument();
                try
                {
                    SimpleAttributeSet attrSet = new SimpleAttributeSet();
                    StyleConstants.setFontSize(attrSet, 16);
                    StyleConstants.setFontFamily(attrSet, "楷体");
                    doc.insertString(doc.getLength(), "\n" + msg, attrSet);
                    text.setSelectionStart(text.getText().length());//滚动到最后
                }
                catch (BadLocationException e)
                {
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * 显示新线路的信息
     * @author Administrator
     * @param netConfig
     */
    public void showNewConfigInfo(final ServerConfig netConfig)
    {
        ExecMgr.guiExec.execute(new Runnable()
        {
            @Override
            public void run()
            {
                Document doc = text.getDocument();
                try
                {
                    if (StringUtils.isBlank(netConfig.getPassword()))
                    {
                        SimpleAttributeSet attrSet = new SimpleAttributeSet();
                        StyleConstants.setForeground(attrSet, Color.RED);
                        doc.insertString(doc.getLength(), "\n" + netConfig.toString(), attrSet);
                    }
                    else
                    {
                        doc.insertString(doc.getLength(), "\n" + netConfig.toString(), null);
                    }
                    text.setSelectionStart(text.getText().length());//滚动到最后
                }
                catch (BadLocationException e)
                {
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * 选择按钮
     * @author Administrator
     */
    private void refreshBtnSelectionAndScreen()
    {
        final String con = String.format("切换到%s号线路...", curSelLine);
        ExecMgr.guiExec.execute(new Runnable()
        {
            @Override
            public void run()
            {
                //将选中状态进行一次刷新
                for (int i = 0; i < switchBtns.length; i++)
                {
                    if (curSelLine.equals(String.valueOf(i + 1)))
                    {
                        switchBtns[i].setSelected(true);
                    }
                    else
                    {
                        switchBtns[i].setSelected(false);
                    }
                }
                //刷新显示区域
                Document doc = text.getDocument();
                try
                {
                    doc.remove(0, doc.getLength());//先清空、
                    doc.insertString(0, con, null);//再插入
                }
                catch (BadLocationException e)
                {
                    e.printStackTrace();
                }
                text.setCaretPosition(0);
            }
        });
    };
    
    /**
     * 将远程的配置信息回写到配置文件中
     * @author nan.li
     * @param netConfig
     */
    public void saveSSConfigFile(ServerConfig netConfig)
    {
        try
        {
            File configFile = new File(installBaseDirFile, "gui-config.json");
            JSONObject targetConfigJsonObject = new JSONObject(FileUtils.readFileToString(configFile, "UTF-8"));
            JSONObject tt = targetConfigJsonObject.getJSONArray("configs").getJSONObject(0);
            tt.put("server", netConfig.getServer());
            tt.put("server_port", netConfig.getServer_port());
            tt.put("password", netConfig.getPassword());
            tt.put("method", netConfig.getMethod());
            tt.put("remarks", netConfig.getRemarks());//加上时间戳
            FileUtils.writeStringToFile(configFile, targetConfigJsonObject.toString());
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        Logs.d("配置文件更新成功！");
    }
    
    /**
     * 准备软件环境
     * 从服务器中读取出软件安装的基础目录（例如D:\），然后将所有的资料拷贝过去
     * 拷贝前依次检查相应的文件是否已经存在，若已经存在了则忽略
     * @author nan.li
     */
    private void prepareSoftwareEnv()
    {
        installBaseDir = LocalUiLoader.CONFIG_FILE_DIR;
        installBaseDirFile = new File(installBaseDir);
        if (!installBaseDirFile.exists())
        {
            installBaseDirFile.mkdirs();
        }
        boolean newFlag = false;
        
        //检测目标文件们是否存在，若不存在，则拷贝过去
        String[] resArray = {"gui-config.json", "pac.txt", "restart.vbs", "Shadowsocks.exe", "stop.vbs", "start.vbs"};
        for (int i = 0; i < resArray.length; i++)
        {
            File targetFile = new File(installBaseDirFile, resArray[i]);
            if (!targetFile.exists())
            {
                newFlag = true;
                //拷贝过去
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream(String.format("ss/%s", resArray[i]));
                FileOutputStream fileOutputStream = null;
                try
                {
                    fileOutputStream = new FileOutputStream(targetFile);
                    IOUtils.copy(inputStream, fileOutputStream);
                }
                catch (FileNotFoundException e)
                {
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    StreamUtils.close(fileOutputStream, inputStream);
                }
            }
        }
        //初始化restart.vbs里面执行的脚本的真正的路径
        //假如该路径不对，则更新之
        if (newFlag)
        {
            try
            {
                String content = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(String.format("ss/%s", "restart.vbs")));
                String resultCon = StringUtils.replace(content, "$path$", installBaseDir);
                FileUtils.writeStringToFile(new File(installBaseDirFile, "restart.vbs"), resultCon);
                
                content = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(String.format("ss/%s", "start.vbs")));
                resultCon = StringUtils.replace(content, "$path$", installBaseDir);
                FileUtils.writeStringToFile(new File(installBaseDirFile, "start.vbs"), resultCon);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 从那个配置文件中将关键的几个配置项的信息读取并加载到这个配置信息对象里
     * @author nan.li
     * @return
     */
    public ServerConfig readFromSSConfigFile()
    {
        try
        {
            JSONObject targetConfigJsonObject = new JSONObject(FileUtils.readFileToString(new File(installBaseDirFile, "gui-config.json"), "UTF-8"));
            JSONObject resultJsonObject = targetConfigJsonObject.getJSONArray("configs").getJSONObject(0);
            return new ServerConfig(resultJsonObject.getString("server"), resultJsonObject.getString("server_port"), resultJsonObject.getString("password"),
                resultJsonObject.getString("method"), resultJsonObject.getString("remarks"));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 加载系统托盘图标设置
     */
    private void loadSystemTray()
    {
        try
        {
            if (SystemTray.isSupported())
            {
                // 判断当前平台是否支持系统托盘
                SystemTray st = SystemTray.getSystemTray();
                trayIcon = new TrayIcon(IconMgr.icon);
                String toolTips = "自动SS！开启你的未来！";
                trayIcon.setToolTip(toolTips);//托盘图标提示
                //左击该托盘图标，则打开窗体
                trayIcon.addMouseListener(new MouseAdapter()
                {
                    public void mouseClicked(MouseEvent e)
                    {
                        //当左击窗口时
                        if (e.getButton() == MouseEvent.BUTTON1)
                        {
                            setVisible(true);//设置窗口可见
                            setExtendedState(JFrame.NORMAL);//正常显示窗口
                        }
                    }
                });
                PopupMenu popupMenu = new PopupMenu();
                MenuItem exitSubMenu = new MenuItem("Exit");
                exitSubMenu.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        System.exit(0);
                    }
                });
                popupMenu.add(exitSubMenu);
                trayIcon.setPopupMenu(popupMenu); // 为托盘添加右键弹出菜单
                st.add(trayIcon);//将托盘图标加入到系统托盘中
            }
        }
        catch (Exception e)
        {
            Logs.e(e);
        }
    }
    
    /**
     * 切换到某一条线路
     * @author nan.li
     * @param cmd 线路1、2、3
     */
    private void switchLine()
    {
        ExecMgr.singleExec.execute(new Runnable()
        {
            @Override
            public void run()
            {
                //如果是GOGO定制线路
                if ("9".equals(curSelLine))
                {
                }
                else if ("10".equals(curSelLine))
                {
                }
                else if ("11".equals(curSelLine))
                {
                }
                else if ("12".equals(curSelLine))
                {
                }
                else
                {
                    List<String> frontEightLines = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8");
                    ServerConfig netConfig = WsManager.readConfigFromNet(curSelLine);
                    showNewConfigInfo(netConfig);//切换了之后，立即将新的配置进行展示
                    ServerConfig fileConfig = readFromSSConfigFile();
                    
                    //假如ss的配置发生了变化，或者说线路是由非1-8号线路切换过来的，那么需要手动重启一次
                    if (!netConfig.equals(fileConfig) || !frontEightLines.contains(lastSelLine) || firstTime)
                    {
                        saveSSConfigFile(netConfig);
                        VbsManager.restartSSClientSoftware(installBaseDir);
                        firstTime = false;
                    }
                }
                //切换线路后，成功失败次数都得清零
                totalSuccessCount.set(0);
                continuousFailCount.set(0);
                //最后要再次刷新线路状态
                showLineState();
            }
        });
    }
    
    /**
     * 将配置信息回传回网络
     * @author nan.li
     * @param key
     * @param value
     */
    protected void saveNetConfig(final String key, final String value)
    {
        ExecMgr.singleExec.execute(new Runnable()
        {
            @Override
            public void run()
            {
                WsManager.saveConfig(key, value);
            }
        });
    }
}
