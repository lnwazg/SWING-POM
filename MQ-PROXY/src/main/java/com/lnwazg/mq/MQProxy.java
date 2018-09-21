package com.lnwazg.mq;

import org.zeromq.ZMQ;

import com.lnwazg.kit.executor.ExecMgr;
import com.lnwazg.kit.http.net.IpHostUtils;
import com.lnwazg.kit.log.Logs;
import com.lnwazg.kit.map.Maps;
import com.lnwazg.kit.security.SecurityUtils;
import com.lnwazg.kit.singleton.BeanMgr;
import com.lnwazg.myzoo.framework.MyZooClient;
import com.lnwazg.ui.MainFrame;

/**
 * MQ的代理服务器<br>
 * 这里的MQ Server同样是起着一个服务，但是对收到的字节不作任何解压缩，直接转发到更早就建立好的mq-client里面去。。
 * @author Administrator
 * @version 2016年7月29日
 */
public class MQProxy
{
    private static MQProxy INSTANCE = new MQProxy();
    
    public static MQProxy getInstance()
    {
        return INSTANCE;
    }
    
    private MQProxy()
    {
    }
    
    ZMQ.Context context;
    
    ZMQ.Socket socket;
    
    boolean continueExec = true;
    
    String nodeBaseName = "mq";//节点基础名称
    
    String groupName = "mq";//组名称
    
    String server;//当前的服务器地址
    
    String nodeName;//当前的节点名称
    
    /**
     * 开启MQ代理服务器
     * @author nan.li
     * @param proxyIp
     * @param proxyPort
     * @param localPort
     */
    public void start(String proxyIp, int proxyPort, int localPort)
    {
        server = IpHostUtils.getLocalHostIP();
        nodeName = String.format("%s-%s", nodeBaseName, SecurityUtils.md5Encode(String.format("%s_%s", server, localPort)));
        
        continueExec = true;
        Transponder.init(proxyIp, proxyPort);
        new Thread(() -> {
            context = ZMQ.context(4);
            socket = context.socket(ZMQ.REP); //创建一个response类型的socket，他可以接收request发送过来的请求，其实可以将其简单的理解为服务端
            socket.bind("tcp://*:" + localPort); //绑定端口
            Logs.i(String.format("MQ服务已启动，端口号: %d", localPort));
            ExecMgr.guiExec.execute(() -> {
                BeanMgr.get(MainFrame.class).statusLabel.setText(String.format("服务已启动，端口号 %d", localPort));
            });
            while (continueExec && !Thread.currentThread().isInterrupted())
            {
                //扔到线程池里面去执行响应
                try
                {
                    byte[] request = socket.recv();
                    //System.out.println(String.format("收到一个请求:%s", new String(request)));
                    //获取request发送过来的数据
                    //这个方法会在这里阻塞
                    
                    //消息转发，并照常接收
                    byte[] response = Transponder.forward(request);
                    
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
        
        if (MainFrame.myZooInitSuccess)
        {
            //向myzookeeper注册mq服务
            MyZooClient.registerService(Maps.asStrHashMap("node", nodeName, "group", groupName, "server", server, "port", localPort + ""));
        }
    }
    
    /**
     * 关闭MQ
     * @author Administrator
     */
    public void shutdown()
    {
        continueExec = false;//可以保证当调用完close()之后，线程内的下次循环的时候，自然停止掉。那么整个线程也就停止执行了
        Transponder.shutdown();
        socket.close();
        context.term();
        
        if (MainFrame.myZooInitSuccess)
        {
            //向myzookeeper解除注册mq服务
            MyZooClient.unregisterService(Maps.asStrHashMap("node", nodeName, "group", groupName));
        }
    }
}
