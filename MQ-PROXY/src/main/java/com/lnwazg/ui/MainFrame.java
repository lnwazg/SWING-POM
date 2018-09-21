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
import com.lnwazg.mq.MQProxy;
import com.lnwazg.myzoo.framework.MyZooClient;
import com.lnwazg.swing.util.WinMgr;
import com.lnwazg.swing.util.ui.IOS7SwitchButton;
import com.lnwazg.swing.xmlbuilder.XmlJFrame;
import com.lnwazg.swing.xmlbuilder.anno.XmlBuild;

/**
 * @author lnwazg@126.com
 * @version 2016年10月7日
 */
@XmlBuild("MQ_PROXY.xml")
public class MainFrame extends XmlJFrame
{
    private static final long serialVersionUID = 416736654918898426L;
    
    public JTextPane logScreen;
    
    public JLabel statusLabel;
    
    private IOS7SwitchButton switchBtn;
    
    private JTextField localPort;
    
    private SmartButton clearLog;
    
    /**
     * 被代理的主机地址
     */
    private JTextField proxyIp;
    
    /**
     * 被代理的端口号
     */
    private JTextField proxyPort;
    
    //均设有默认值
    String proxyIpString = "192.168.1.10";
    
    Integer proxyPortInteger = 1200, localPortInteger = 1210;
    
    Boolean switchBoolean = false;
    
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
        if (StringUtils.isNotEmpty(WinMgr.getConfig("proxyIp")) && StringUtils.isNotEmpty(WinMgr.getConfig("proxyPort")))
        {
            proxyIpString = WinMgr.getConfig("proxyIp");
            proxyPortInteger = Integer.valueOf(WinMgr.getConfig("proxyPort"));
        }
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
            localPort.setText(localPortInteger + "");
            proxyIp.setText(proxyIpString);
            proxyPort.setText(proxyPortInteger + "");
        });
        B.put(this);
        myZooInitSuccess = MyZooClient.initDefaultConfig();
    }
    
    private void initListeners()
    {
        switchBtn.setOnCallback(() -> {
            //验证
            if (StringUtils.isBlank(proxyIp.getText()) || StringUtils.isBlank(proxyPort.getText()))
            {
                JOptionPane.showMessageDialog(this, "被代理的地址和端口号均不能为空！");
                return;
            }
            if (StringUtils.isNotBlank(proxyPort.getText()))
            {
                if (!Validates.isInteger(proxyPort.getText().trim()))
                {
                    JOptionPane.showMessageDialog(this, "被代理的端口号必须是整数！");
                    return;
                }
            }
            if (StringUtils.isBlank(localPort.getText()))
            {
                JOptionPane.showMessageDialog(this, "本地端口号不能为空！");
                return;
            }
            if (StringUtils.isNotBlank(localPort.getText()))
            {
                if (!Validates.isInteger(localPort.getText().trim()))
                {
                    JOptionPane.showMessageDialog(this, "本地端口号必须是整数！");
                    return;
                }
            }
            
            //首先从界面上取数
            proxyIpString = proxyIp.getText().trim();
            proxyPortInteger = Integer.valueOf(proxyPort.getText().trim());
            localPortInteger = Integer.valueOf(localPort.getText().trim());
            
            //存到配置文件中
            WinMgr.setConfig("proxyIp", proxyIpString);
            WinMgr.setConfig("proxyPort", proxyPortInteger + "");
            WinMgr.setConfig("localPort", localPortInteger + "");
            WinMgr.setConfig("switch", "true");
            
            // 开启代理MQ
            MQProxy.getInstance().start(proxyIpString, proxyPortInteger, localPortInteger);
        });
        switchBtn.setOffCallback(() -> {
            // 关闭代理MQ
            WinMgr.setConfig("switch", "false");
            MQProxy.getInstance().shutdown();
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
