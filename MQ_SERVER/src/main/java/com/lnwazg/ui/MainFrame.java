package com.lnwazg.ui;

import java.sql.SQLException;

import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;

import com.lnwazg.dao.MessageDao;
import com.lnwazg.dbkit.jdbc.MyJdbc;
import com.lnwazg.dbkit.proxy.DaoProxy;
import com.lnwazg.dbkit.utils.DbKit;
import com.lnwazg.kit.executor.ExecMgr;
import com.lnwazg.kit.singleton.BeanMgr;
import com.lnwazg.kit.validate.Validates;
import com.lnwazg.mq.MQ;
import com.lnwazg.mq.entity.Message;
import com.lnwazg.mq.entity.VisitLog;
import com.lnwazg.swing.util.WinMgr;
import com.lnwazg.swing.util.ui.IOS7SwitchButton;
import com.lnwazg.swing.util.uiloader.LocalUiLoader;
import com.lnwazg.swing.xmlbuilder.XmlJFrame;
import com.lnwazg.swing.xmlbuilder.anno.XmlBuild;
import com.lnwazg.util.Utils;

/**
 * 主窗体<br>
 * 压力测试，是检验一样产品性能好坏的神器！<br>
 * @author Administrator
 * @version 2016年2月12日
 */
@XmlBuild("MQ_SERVER.xml")
public class MainFrame extends XmlJFrame
{
    private static final long serialVersionUID = 416736654918898426L;
    
    private IOS7SwitchButton switchBtn;
    
    public JTextArea logScreen;
    
    private JTextField portInputText;
    
    public JLabel statusLabel;
    
    /**
     * 默认的端口号
     */
    public static int DEFAULT_PORT = 11111;
    
    MyJdbc jdbc = null;
    
    MessageDao messageDao = null;
    
    @Override
    public void afterUIBind()
    {
        initEnv();
        initListeners();
    }
    
    boolean isMysql = false;
    
    private void initEnv()
    {
        //加载上次的端口号
        ExecMgr.guiExec.execute(() -> {
            portInputText.setText(WinMgr.getConfig("port") + "");
        });
        
        BeanMgr.put(this);
        //数据库初始化
        String password = "";
        String url = "";
        String username = "";
        if (isMysql)
        {
            url =
                "jdbc:mysql://127.0.0.1:3306/MQ?useUnicode=true&generateSimpleParameterMetadata=true&characterEncoding=UTF-8&autoReconnect=true&autoReconnectForPools=true";
            username = "root";
        }
        else
        {
            url = "jdbc:sqlite://" + LocalUiLoader.CONFIG_FILE_DIR + "/myMQ.db";
            username = "";
        }
        
        jdbc = DbKit.getJdbc(url, username, password);
        //准备好必要的表
        try
        {
            jdbc.createTable(VisitLog.class);
            jdbc.createTable(Message.class);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        BeanMgr.put(MyJdbc.class, jdbc);//注册jdbc连接器
        messageDao = DaoProxy.proxyDaoInterface(MessageDao.class, jdbc);//根据接口生成动态代理类
        BeanMgr.put(MessageDao.class, messageDao);
    }
    
    private void initListeners()
    {
        switchBtn.setOnCallback(() -> {
            // 开启MQ
            int port = MainFrame.DEFAULT_PORT;
            //如果自定义了port，那么就取出自定义的值
            if (StringUtils.isNoneBlank(portInputText.getText()))
            {
                if (Validates.isInteger(portInputText.getText().trim()))
                {
                    port = Integer.parseInt(portInputText.getText().trim());
                }
                else
                {
                    ExecMgr.guiExec.execute(() -> {
                        portInputText.setText(MainFrame.DEFAULT_PORT + "");
                    });
                }
            }
            WinMgr.saveConfig("port", port + "");//使用过后的端口号，要记录到配置文件中
            MQ.getInstance().start(port);
        });
        switchBtn.setOffCallback(() -> {
            // 关闭MQ
            MQ.getInstance().shutdown();
            Utils.showInLogScreen("服务器已经关闭...");
            ExecMgr.guiExec.execute(() -> {
                statusLabel.setText("服务器已经关闭");
            });
        });
        
        //手动开启服务器
        switchBtn.setStatus(true);
    }
}
