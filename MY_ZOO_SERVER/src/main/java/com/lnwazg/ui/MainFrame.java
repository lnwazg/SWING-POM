package com.lnwazg.ui;

import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import org.apache.commons.lang3.StringUtils;

import com.lnwazg.kit.executor.ExecMgr;
import com.lnwazg.kit.gson.GsonKit;
import com.lnwazg.kit.json.GsonCfgMgr;
import com.lnwazg.kit.log.Logs;
import com.lnwazg.kit.singleton.B;
import com.lnwazg.kit.swing.SwingDialogKit;
import com.lnwazg.kit.swing.ui.comp.SmartButton;
import com.lnwazg.kit.validate.Validates;
import com.lnwazg.myzoo.util.ZooServers;
import com.lnwazg.server.MyZooServer;
import com.lnwazg.swing.util.WinMgr;
import com.lnwazg.swing.util.ui.IOS7SwitchButton;
import com.lnwazg.swing.xmlbuilder.XmlJFrame;
import com.lnwazg.swing.xmlbuilder.anno.XmlBuild;
import com.lnwazg.zooctrl.ServerController;

/**
 * 主窗体<br>
 * @author Administrator
 * @version 2016年2月12日
 */
@XmlBuild("MY_ZOO_SERVER.xml")
public class MainFrame extends XmlJFrame
{
    private static final long serialVersionUID = 416736654918898426L;
    
    private IOS7SwitchButton switchBtn;
    
    private JTextField portInputText;
    
    private JTextPane logScreen;
    
    public JLabel statusLabel;
    
    Integer localPortInteger = 1210;
    
    Boolean switchBoolean = false;
    
    private SmartButton clearLog;
    
    private JTextPane serverConfigInfo;
    
    private SmartButton updateBtn;
    
    private SmartButton clearBtn;
    
    private JScrollPane logScroller;
    
    @Override
    public void afterUIBind()
    {
        initEnv();
        initListeners();
    }
    
    private void initEnv()
    {
        //配置swing日志窗口
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
        
        //初始化服务器配置信息，填充到serverConfigInfo里面
        ZooServers zooServers = GsonCfgMgr.readObject(ZooServers.class);
        if (zooServers != null)
        {
            String configJson = GsonKit.prettyGson.toJson(zooServers);
            ExecMgr.guiExec.execute(() -> {
                serverConfigInfo.setText(configJson);
                serverConfigInfo.setCaretPosition(0);
            });
        }
        
        B.put(this);
        
        //主动注入依赖给框架，便于框架回调刷新已注册的node信息面板
        ServerController.serverConfigInfo = serverConfigInfo;
        ServerController.logScroller = logScroller;
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
            MyZooServer.getInstance(localPortInteger).start();
        });
        switchBtn.setOffCallback(() -> {
            // 关闭代理MQ
            WinMgr.setConfig("switch", "false");
            MyZooServer.getInstance(localPortInteger).shutdown();
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
        
        //更新服务端配置信息的按钮
        updateBtn.addActionListener(e -> {
            //更新的时候，需要检查输入的格式是否合法
            try
            {
                ZooServers zooServers = GsonKit.prettyGson.fromJson(serverConfigInfo.getText(), ZooServers.class);
                if (zooServers != null)
                {
                    //配置信息正确了，才保存！
                    //将数据更新到当前的zookeeper服务器中
                    ServerController.updateCustomServerInfo(zooServers);
                    JOptionPane.showMessageDialog(this, "更新服务器配置信息成功！");
                }
            }
            catch (Exception e1)
            {
                //输入的格式不合法，阻止保存
                Logs.e("您输入的配置信息json格式不正确！", e1);
            }
        });
        
        clearBtn.addActionListener(e -> {
            //更新的时候，需要检查输入的格式是否合法
            Object[] options = {"是(Y)", "否(N)"};
            int result = SwingDialogKit.showOptionDialog(this, "确定要清空所有的在线服务列表信息吗？", "温馨提示", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
            if (result == 0)
            {
                //将配置信息对象清空
                ZooServers zooServers = new ZooServers();
                zooServers.setOnlineGroupNodeInfoMap(new HashMap<>());
                zooServers.setOnlineServerInfoMap(new HashMap<>());
                //持久化配置信息
                GsonCfgMgr.writeObject(zooServers);
                //内置的服务器列表内存更新
                ServerController.updateCustomServerInfo(zooServers);
                //然后还要反向刷新公告板的信息
                String configJson = GsonKit.prettyGson.toJson(zooServers);
                ExecMgr.guiExec.execute(() -> {
                    serverConfigInfo.setText(configJson);
                    serverConfigInfo.setCaretPosition(0);
                });
                //对话框通知
                JOptionPane.showMessageDialog(this, "服务器配置信息已经清空！");
            }
        });
        
        //手动开启服务器
        ExecMgr.guiExec.execute(() -> {
            switchBtn.setStatus(switchBoolean);
        });
        
    }
}
