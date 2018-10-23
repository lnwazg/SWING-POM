package com.lnwazg.mq;

import org.apache.commons.codec.CharEncoding;
import org.zeromq.ZMQ;

import com.lnwazg.kit.compress.GzipBytesUtils;
import com.lnwazg.kit.executor.ExecMgr;
import com.lnwazg.kit.http.net.IpHostUtils;
import com.lnwazg.kit.log.Logs;
import com.lnwazg.kit.map.Maps;
import com.lnwazg.kit.security.PasswordKit;
import com.lnwazg.kit.security.SecurityUtils;
import com.lnwazg.kit.singleton.B;
import com.lnwazg.kit.singleton.BeanMgr;
import com.lnwazg.mh.MessageHandler;
import com.lnwazg.myzoo.framework.MyZooClient;
import com.lnwazg.ui.MainFrame;
import com.lnwazg.util.Utils;

/**
 * MQ的主驱动程序
 * @author Administrator
 * @version 2016年7月29日
 */
public class MQ
{
    /**
     * 我的zooKeeper是否初始化成功了
     */
    public static boolean myZooInitSuccess = false;
    
    private static MQ INSTANCE = new MQ();
    
    public static MQ getInstance()
    {
        return INSTANCE;
    }
    
    private MQ()
    {
        myZooInitSuccess = MyZooClient.initDefaultConfig();
    }
    
    ZMQ.Context context;
    
    ZMQ.Socket socket;
    
    boolean continueExec = true;
    
    //跟MyZoo相关的参数
    String nodeBaseName = "mq";//节点基础名称
    
    String groupName = "mq";//组名称
    
    String server;//当前的服务器地址
    
    String nodeName;//当前的节点名称
    
    /**
     * 开启MQ
     * @author Administrator
     * @param port
     */
    public void start(int port)
    {
        //启动的时候，取出一些节点的基本信息
        server = IpHostUtils.getLocalHostIP();
        nodeName = String.format("%s-%s", nodeBaseName, SecurityUtils.md5Encode(String.format("%s_%s", server, port)));
        
        continueExec = true;
        new Thread(() -> {
            context = ZMQ.context(4);
            socket = context.socket(ZMQ.REP); //创建一个response类型的socket，他可以接收request发送过来的请求，其实可以将其简单的理解为服务端
            socket.bind("tcp://*:" + port); //绑定端口
            Logs.i(String.format("MQ服务已启动，端口号: %d", port));
            Utils.showInLogScreen(String.format("MQ服务已启动，端口号: %d", port));
            if (B.q(MainFrame.class) != null)
            {
                ExecMgr.guiExec.execute(() -> {
                    BeanMgr.get(MainFrame.class).statusLabel.setText(String.format("服务已启动，端口号 %d", port));
                });
            }
            while (continueExec && !Thread.currentThread().isInterrupted())
            {
                //扔到线程池里面去执行响应
                try
                {
                    byte[] request = socket.recv();
                    //System.out.println(String.format("收到一个请求:%s", new String(request)));
                    //获取request发送过来的数据
                    //这个方法会在这里阻塞
                    request = SecurityUtils.aesDecode(request, PasswordKit.PASSWORD);//解密
                    request = GzipBytesUtils.unzip(request);//首先进行解压缩
                    String reqStr = new String(request, CharEncoding.UTF_8);
                    String rspStr = MessageHandler.handle(reqStr);
                    byte[] response = rspStr.getBytes(CharEncoding.UTF_8);
                    response = GzipBytesUtils.zip(response);//然后对数据进行压缩处理，减少传输量的消耗
                    response = SecurityUtils.aesEncode(response, PasswordKit.PASSWORD);//数据加密
                    socket.send(response, 0);
                    
                    MyZooClient.monitorInvokeOnce(nodeName);
                    
                    //睡眠1ms，很核心的一个小功能，可有效防止服务器瘫痪！
                    Thread.sleep(1);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Logs.i("MQ-Server已停止！");
                }
            }
        }).start();
        if (myZooInitSuccess)
        {
            //向myzookeeper注册mq服务
            MyZooClient.registerService(Maps.asStrHashMap("node", nodeName, "group", groupName, "server", server, "port", port + ""));
        }
    }
    
    /**
     * 关闭MQ
     * @author Administrator
     */
    public void shutdown()
    {
        continueExec = false;//可以保证当调用完close()之后，线程内的下次循环的时候，自然停止掉。那么整个线程也就停止执行了
        socket.close();
        context.term();
        if (myZooInitSuccess)
        {
            //向myzookeeper解除注册mq服务
            MyZooClient.unregisterService(Maps.asStrHashMap("node", nodeName, "group", groupName));
        }
    }
}
