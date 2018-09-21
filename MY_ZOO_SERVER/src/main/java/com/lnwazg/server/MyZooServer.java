package com.lnwazg.server;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.lnwazg.dbkit.jdbc.MyJdbc;
import com.lnwazg.dbkit.utils.DbKit;
import com.lnwazg.kit.executor.ExecMgr;
import com.lnwazg.kit.log.Logs;
import com.lnwazg.kit.random.RandomStrUtils;
import com.lnwazg.kit.singleton.B;
import com.lnwazg.myzoo.bean.Msg;
import com.lnwazg.myzoo.entity.NodeInvokeInfo;
import com.lnwazg.myzoo.framework.ZooFramework;
import com.lnwazg.myzoo.util.KyroResigterClassKit;
import com.lnwazg.swing.util.uiloader.LocalUiLoader;
import com.lnwazg.zooctrl.ServerController;

/**
 * 我的ZooKeeper服务器<br>
 * 支持的功能：注册服务、查询服务(按名称、按名称以...开头、按分组信息)<br>
 * 
 * 调用关系说明：

0. 服务容器负责启动，加载，运行服务提供者。
1. 服务提供者在启动时，向注册中心注册自己提供的服务。
2. 服务消费者在启动时，向注册中心订阅自己所需的服务。
3. 注册中心返回服务提供者地址列表给消费者，如果有变更，注册中心将基于长连接推送变更数据给消费者。
4. 服务消费者，从提供者地址列表中，基于软负载均衡算法，选一台提供者进行调用，如果调用失败，再选另一台调用。
5. 服务消费者和提供者，在内存中累计调用次数和调用时间，定时每分钟发送一次统计数据到监控中心。

(1) 连通性：

注册中心负责服务地址的注册与查找，相当于目录服务，服务提供者和消费者只在启动时与注册中心交互，注册中心不转发请求，压力较小
监控中心负责统计各服务调用次数，调用时间等，统计先在内存汇总后每分钟一次发送到监控中心服务器，并以报表展示
服务提供者向注册中心注册其提供的服务，并汇报调用时间到监控中心，此时间不包含网络开销
服务消费者向注册中心获取服务提供者地址列表，并根据负载算法直接调用提供者，同时汇报调用时间到监控中心，此时间包含网络开销
注册中心，服务提供者，服务消费者三者之间均为长连接，监控中心除外
注册中心通过长连接感知服务提供者的存在，服务提供者宕机，注册中心将立即推送事件通知消费者
注册中心和监控中心全部宕机，不影响已运行的提供者和消费者，消费者在本地缓存了提供者列表
注册中心和监控中心都是可选的，服务消费者可以直连服务提供者

(2) 健状性：

监控中心宕掉不影响使用，只是丢失部分采样数据
数据库宕掉后，注册中心仍能通过缓存提供服务列表查询，但不能注册新服务
注册中心对等集群，任意一台宕掉后，将自动切换到另一台
注册中心全部宕掉后，服务提供者和服务消费者仍能通过本地缓存通讯
服务提供者无状态，任意一台宕掉后，不影响使用
服务提供者全部宕掉后，服务消费者应用将无法使用，并无限次重连等待服务提供者恢复

(3) 伸缩性：

注册中心为对等集群，可动态增加机器部署实例，所有客户端将自动发现新的注册中心
服务提供者无状态，可动态增加机器部署实例，注册中心将推送新的服务提供者信息给消费者

(4) 升级性：

当服务集群规模进一步扩大，带动IT治理结构进一步升级，需要实现动态部署，进行流动计算，现有分布式服务架构不会带来阻力
 * 
 * @author lnwazg@126.com
 * @version 2016年10月30日
 */
public class MyZooServer
{
    Integer localPort = 0;
    
    private static Map<Integer, MyZooServer> INSTANCE_MAP = new HashMap<>();
    
    /**
     * 默认的刷新公告板间隔时间-秒数
     */
    public static final int DEFAULT_REFRESH_INTERVAL_SECONDS = 20;
    
    /**
     * 连接表
     */
    Map<String, Connection> connMap = new ConcurrentHashMap<>();
    
    Server server;
    
    /**
     * 数据库连接文件
     */
    static MyJdbc jdbc = DbKit.getJdbc("jdbc:sqlite://" + LocalUiLoader.CONFIG_FILE_DIR + "/myzooserver.db", "", "");
    
    //建表
    static
    {
        try
        {
            //建立数据库
            jdbc.createTable(NodeInvokeInfo.class);
            
            //最终将MyJdbc注入到单例注册表中
            B.s(MyJdbc.class, jdbc);
            
            //启动一个守护进程，每隔30秒刷新一次各个节点的调用情况并显示
            ExecMgr.startDaemenThread(() -> {
                while (true)
                {
                    //只有当数据库真正初始化完毕之后才进行操作
                    //查询最新的调用次数列表
                    //onlineServerInfoMap的每条记录，都需要刷新出当前的调用次数总和
                    //目前只查询当天的调用次数总和
                    String sql = "select nodeName node,sum(invokeTimes) time from NodeInvokeInfo where date(createTime)=date() group by nodeName";
                    //调用次数目前只支持当天的
                    try
                    {
                        //调用次数列表
                        List<Map<String, Object>> nodeTimesList = jdbc.listMap(sql);
                        for (Map<String, Object> map : nodeTimesList)
                        {
                            //节点
                            String node = ObjectUtils.toString(map.get("node"));
                            //调用次数
                            Integer times = Integer.valueOf(ObjectUtils.toString(map.get("time")));
                            
                            //去在线信息表中进行匹配
                            for (String nodeName : ServerController.onlineServerInfoMap.keySet())
                            {
                                if (nodeName.equals(node))
                                {
                                    ServerController.onlineServerInfoMap.get(nodeName).put("callTimes", times + "");
                                    //只要查找到一次（成功匹配了），那么后面的就没必要再查找了
                                    break;
                                }
                            }
                        }
                    }
                    catch (Exception e1)
                    {
                        e1.printStackTrace();
                    }
                    
                    //更新node信息板
                    ServerController.updateServerConfig();
                    
                    //然后要休眠30秒之后再战
                    try
                    {
                        TimeUnit.SECONDS.sleep(DEFAULT_REFRESH_INTERVAL_SECONDS);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
    
    private MyZooServer(Integer port)
    {
        localPort = port;
    }
    
    public static MyZooServer getInstance(Integer port)
    {
        if (INSTANCE_MAP.get(port) == null)
        {
            INSTANCE_MAP.put(port, new MyZooServer(port));
        }
        return INSTANCE_MAP.get(port);
    }
    
    /**
     * 启动服务
     * @author lnwazg@126.com
     */
    public void start()
    {
        server = new Server();
        server.start();
        try
        {
            server.bind(localPort);
            Logs.i(String.format("Server start ok! Port: %s", localPort));
            Arrays.stream(KyroResigterClassKit.TO_BE_REGISTERED_CLASSES).forEach(server.getKryo()::register);
            server.addListener(new Listener()
            {
                /**
                 * 收到连接发来的数据的时候
                 * @author nan.li
                 * @param connection
                 * @param object
                 */
                public void received(Connection connection, Object object)
                {
                    if (object instanceof Msg)
                    {
                        Msg msg = (Msg)object;
                        Logs.i("服务端收到消息：" + msg + "\n");
                        
                        //统一处理msg信息即可
                        
                        //鉴权，并绑定uuid（如果没有，则返回出去）
                        String token = msg.getToken();//token就是标识完整的会话的重要信息
                        if (StringUtils.isEmpty(token))
                        {
                            //为空，则要新建一个token，并提供给客户端使用
                            token = RandomStrUtils.generateRandomString(64);
                            msg.setToken(token);
                        }
                        else
                        {
                            //维持原有的token不变
                        }
                        //将连接放置到connectionMap里面
                        //每次都调用，即可每次都能更新连接到最新的那个
                        connMap.put(token, connection);
                        
                        ZooFramework.invokeServerController(msg, connection, server, token);
                        
                        //将token返回给客户端
                        //connection.send(msg);
                        // 当客户端重启时候，需要再次用户鉴权。当然如果已经有了uuid，那么可以认为会话未丢失，可以继续运行。
                    }
                }
                
                /**
                 * 初次连接上来的时候
                 * {@inheritDoc}
                 */
                public void connected(Connection connection)
                {
                    ServerController.ALL_CONNECTIONS.add(connection);
                }
                
                /**
                 * 断开连接的时候
                 * {@inheritDoc}
                 */
                public void disconnected(Connection connection)
                {
                    //这种是被动下线，是由服务器端检测到连接已经断开，并将客户端下线掉的
                    //因此，无法通过客户端去发送这种消息！
                    //只能在服务端主动处理掉！
                    ImmutablePair<String, String> immutablePair = ServerController.connectionNodeGroupMap.get(connection);
                    if (immutablePair != null)
                    {
                        //                        ServerController.ALL_CONNECTIONS.remove(connection);
                        connection = null;//将该连接标记为无效，以后通知的时候也不会再通知它！
                        ServerController.customDown(immutablePair);
                    }
                }
                
                /**
                 * 连接空闲的时候
                 * {@inheritDoc}
                 */
                public void idle(Connection connection)
                {
                }
            });
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * 关闭服务
     * @author lnwazg@126.com
     */
    public void shutdown()
    {
        if (server != null)
        {
            server.stop();
        }
    }
    
}
