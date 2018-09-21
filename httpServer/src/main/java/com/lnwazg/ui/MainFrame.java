package com.lnwazg.ui;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import org.apache.commons.lang3.StringUtils;

import com.lnwazg.kit.executor.ExecMgr;
import com.lnwazg.kit.log.Logs;
import com.lnwazg.kit.singleton.B;
import com.lnwazg.kit.swing.ui.comp.SmartButton;
import com.lnwazg.kit.validate.Validates;
import com.lnwazg.server.HttpServerProxy;
import com.lnwazg.swing.util.WinMgr;
import com.lnwazg.swing.util.ui.IOS7SwitchButton;
import com.lnwazg.swing.xmlbuilder.XmlJFrame;
import com.lnwazg.swing.xmlbuilder.anno.XmlBuild;

/**
 * 主窗体<br>
 * @author Administrator
 * @version 2016年2月12日
 */
@XmlBuild("httpServer.xml")
public class MainFrame extends XmlJFrame
{
    private static final long serialVersionUID = 416736654918898426L;
    
    private IOS7SwitchButton switchBtn;
    
    private JTextField portInputText;
    
    private JTextPane logScreen;
    
    public JLabel statusLabel;
    
    Integer localPortInteger = 7777;
    
    Boolean switchBoolean = false;
    
    private SmartButton clearLog;
    
    /**
     * 我的zooKeeper是否初始化成功了
     */
    public static boolean myZooInitSuccess = false;
    
    @Override
    public void afterUIBind()
    {
        initEnv();
        initListeners();
    }
    
    private void initEnv()
    {
        logScreen.setContentType("text/html");
        Logs.addLogDest(logScreen);
        
        //首先尝试从配置中加载信息到本地
        if (StringUtils.isNotEmpty(WinMgr.getConfig("localPort")))
        {
            localPortInteger = Integer.valueOf(WinMgr.getConfig("localPort"));
        }
        if (StringUtils.isNotEmpty(WinMgr.getConfig("switch")))
        {
            switchBoolean = Boolean.valueOf(WinMgr.getConfig("switch"));
        }
        //然后先填充到ui的输入框中
        ExecMgr.guiExec.execute(() -> {
            portInputText.setText(localPortInteger + "");
        });
        B.put(this);
        //zookeeper组件
        //        myZooInitSuccess = MyZooKeeper.initDefaultConfig();
    }
    
    private void initListeners()
    {
        switchBtn.setOnCallback(() -> {
            //验证
            if (StringUtils.isBlank(portInputText.getText()))
            {
                JOptionPane.showMessageDialog(this, "端口号不能为空！");
                return;
            }
            if (StringUtils.isNotBlank(portInputText.getText()))
            {
                if (!Validates.isInteger(portInputText.getText().trim()))
                {
                    JOptionPane.showMessageDialog(this, "端口号必须是整数！");
                    return;
                }
            }
            
            //首先从界面上取数
            localPortInteger = Integer.valueOf(portInputText.getText().trim());
            
            //存到配置文件中
            WinMgr.setConfig("localPort", localPortInteger + "");
            WinMgr.setConfig("switch", "true");
            
            // 开启代理MQ
            HttpServerProxy.getInstance(localPortInteger).start();
        });
        switchBtn.setOffCallback(() -> {
            // 关闭代理MQ
            WinMgr.setConfig("switch", "false");
            HttpServerProxy.getInstance(localPortInteger).shutdown();
            Logs.i("服务器已经关闭...");
            ExecMgr.guiExec.execute(() -> {
                statusLabel.setText("服务器已经关闭");
            });
        });
        
        clearLog.addActionListener(e -> {
            ExecMgr.guiExec.execute(() -> {
                logScreen.setText(null);
            });
        });
        
        //手动开启服务器
        ExecMgr.guiExec.execute(() -> {
            switchBtn.setStatus(switchBoolean);
        });
    }
}
