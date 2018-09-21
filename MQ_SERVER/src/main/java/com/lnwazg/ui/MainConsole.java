package com.lnwazg.ui;

import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;

import com.lnwazg.dao.MessageDao;
import com.lnwazg.dbkit.jdbc.MyJdbc;
import com.lnwazg.dbkit.proxy.DaoProxy;
import com.lnwazg.dbkit.utils.DbKit;
import com.lnwazg.kit.log.Logs;
import com.lnwazg.kit.singleton.BeanMgr;
import com.lnwazg.kit.validate.Validates;
import com.lnwazg.mq.MQ;
import com.lnwazg.mq.entity.Message;
import com.lnwazg.mq.entity.VisitLog;
import com.lnwazg.swing.util.WinMgr;
import com.lnwazg.swing.util.uiloader.LocalUiLoader;

public class MainConsole
{
    /**
     * 默认的端口号
     */
    public static int DEFAULT_PORT = 2233;
    
    MyJdbc jdbc = null;
    
    MessageDao messageDao = null;
    
    /**
     * 端口号
     */
    static int port = DEFAULT_PORT;
    
    public static void main(String[] args)
    {
        if (args.length == 0)
        {
            Logs.i("未指定port参数！将使用默认的端口：" + port);
        }
        else
        {
            if (StringUtils.isNotEmpty(args[0]))
            {
                if (Validates.isInteger(args[0]))
                {
                    port = Integer.valueOf(args[0]);
                    Logs.i("当前指定的端口为：" + port);
                }
            }
        }
        MainConsole mainConsole = new MainConsole();
        mainConsole.initEnv();
        mainConsole.initListeners();
    }
    
    boolean isMysql = false;
    
    private void initEnv()
    {
        LocalUiLoader.initConfigFile();
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
        // 开启MQ
        WinMgr.saveConfig("port", port + "");//使用过后的端口号，要记录到配置文件中
        MQ.getInstance().start(port);
    }
}
