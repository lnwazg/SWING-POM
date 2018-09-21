package com.lnwazg.server;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.HashMap;
import java.util.Map;

import com.lnwazg.cache.rmi.IRemoteCache;
import com.lnwazg.cache.rmi.impl.RemoteCacheImpl;
import com.lnwazg.kit.executor.ExecMgr;
import com.lnwazg.kit.http.net.IpHostUtils;
import com.lnwazg.kit.log.Logs;
import com.lnwazg.kit.map.Maps;
import com.lnwazg.kit.security.SecurityUtils;
import com.lnwazg.kit.singleton.BeanMgr;
import com.lnwazg.myzoo.framework.MyZooClient;
import com.lnwazg.ui.MainFrame;

/**
 * RMI服务器<br>
 * 通过不同端口号的切换，即可实现切换不同缓存服务器切换的功能！
 * @author lnwazg@126.com
 * @version 2016年10月16日
 */
public class RMIServer
{
    /**
     * 实例表<br>
     * key为端口号，value为对应的实例对象
     */
    private static Map<Integer, RMIServer> INSTANCE_MAP = new HashMap<>();
    
    Integer localPort = 0;
    
    IRemoteCache remoteCache;
    
    public static synchronized RMIServer getInstance(Integer port)
    {
        if (INSTANCE_MAP.get(port) == null)
        {
            INSTANCE_MAP.put(port, new RMIServer(port));
        }
        return INSTANCE_MAP.get(port);
    }
    
    /**
     * 构造函数<br>
     * 初始化一些绑定的数据
     * @param port
     */
    private RMIServer(Integer port)
    {
        localPort = port;
        try
        {
            //创建一个远程对象 
            remoteCache = new RemoteCacheImpl(port);
            //本地主机上的远程对象注册表Registry的实例，并指定端口为8888，这一步必不可少（Java默认端口是1099），必不可缺的一步，缺少注册表创建，则无法绑定对象到远程注册表上 
            LocateRegistry.createRegistry(localPort);
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * 创建RMI注册表，启动RMI服务，并将远程对象注册到RMI注册表中。 
     * @author lnwazg@126.com
     * @param port
     */
    public void start()
    {
        try
        {
            //把远程对象注册到RMI注册服务器上，并命名为RHello 
            //绑定的URL标准格式为：rmi://host:port/name(其中协议名可以省略，下面两种写法都是正确的） 
            Naming.bind(String.format("rmi://127.0.0.1:%s/REMOTE_CACHE", localPort), remoteCache);
            Logs.i(String.format("Remote Cache服务已启动，端口号: %d", localPort));
            ExecMgr.guiExec.execute(() -> {
                BeanMgr.get(MainFrame.class).statusLabel.setText(String.format("服务已启动，端口号 %d", localPort));
            });
            if (MainFrame.myZooInitSuccess)
            {
                //向myzookeeper注册mq服务
                server = IpHostUtils.getLocalHostIP();
                nodeName = String.format("%s-%s", nodeBaseName, SecurityUtils.md5Encode(String.format("%s_%s", server, localPort)));
                //心跳控制存活
                //如果没有了心跳，就主动将其删除掉
                MyZooClient.registerService(Maps.asStrHashMap("node", nodeName, "group", groupName, "server", server, "port", localPort + ""));
            }
        }
        catch (RemoteException e)
        {
            System.out.println("创建远程对象发生异常！");
            e.printStackTrace();
        }
        catch (AlreadyBoundException e)
        {
            System.out.println("发生重复绑定对象异常！");
            e.printStackTrace();
        }
        catch (MalformedURLException e)
        {
            System.out.println("发生URL畸形异常！");
            e.printStackTrace();
        }
    }
    
    String nodeBaseName = "remoteCache";//节点基础名称
    
    String groupName = "remoteCache";//组名称
    
    String server;//当前的服务器地址
    
    String nodeName;//当前的节点名称
    
    /**
     * 关闭RMI服务
     * @author lnwazg@126.com
     */
    public void shutdown()
    {
        try
        {
            Naming.unbind(String.format("rmi://127.0.0.1:%s/REMOTE_CACHE", localPort));
            if (MainFrame.myZooInitSuccess)
            {
                //向myzookeeper解除注册mq服务
                MyZooClient.unregisterService(Maps.asStrHashMap("node", nodeName, "group", groupName));
            }
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (NotBoundException e)
        {
            e.printStackTrace();
        }
    }
}
