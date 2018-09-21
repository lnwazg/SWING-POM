package com.lnwazg.mq;

import org.zeromq.ZMQ;

import com.lnwazg.kit.log.Logs;

/**
 * 消息转发器
 * @author nan.li
 * @version 2016年10月8日
 */
public class Transponder
{
    static ZMQ.Context context = null;
    
    static ZMQ.Socket socket = null;
    
    static String currentServerAddr = "127.0.0.1";
    
    static int currentPort = 11111;
    
    /**
     * 初始化转发器
     * @author nan.li
     * @param proxyIp
     * @param proxyPort
     */
    public static void init(String proxyIp, int proxyPort)
    {
        currentServerAddr = proxyIp;
        currentPort = proxyPort;
        context = ZMQ.context(1); //创建一个I/O线程的上下文
        socket = context.socket(ZMQ.REQ); //创建一个request类型的socket，这里可以将其简单的理解为客户端，用于向response端发送数据
        socket.connect(String.format("tcp://%s:%d", currentServerAddr, currentPort)); //默认连接的地址以及端口号 
    }
    
    /**
     * 转发字节请求到被代理的服务器去
     * @author nan.li
     * @param request
     * @return
     */
    public static byte[] forward(byte[] request)
    {
        byte[] response = null;
        try
        {
            long now = System.currentTimeMillis();
            int len1 = request.length;
            Logs.d(String.format("转发%d字节", len1));
            socket.send(request, 0);
            //向reponse端发送数据
            response = socket.recv(); //接收response发送回来的数据  正在request/response模型中，send之后必须要recv之后才能继续send，这可能是为了保证整个request/response的流程走完
            int len2 = response.length;
            Logs.d(String.format("接收到%d字节", len2));
            long after = System.currentTimeMillis();
            Logs.i(String.format("消息转发花费了 %d ms", after - now));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return response;
    }
    
    /**
     * 关闭转发器
     * @author nan.li
     */
    public static void shutdown()
    {
        if (socket != null)
        {
            socket.close();
            socket = null;
        }
        if (context != null)
        {
            context.term();
            context = null;
        }
    }
    
}
