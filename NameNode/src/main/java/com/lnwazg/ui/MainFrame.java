package com.lnwazg.ui;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;

import org.apache.commons.io.FileUtils;

import com.lnwazg.bean.DateNodeInfo;
import com.lnwazg.bean.HandleResult;
import com.lnwazg.httpkit.server.HttpServer;
import com.lnwazg.kit.date.DateUtils;
import com.lnwazg.kit.executor.ExecMgr;
import com.lnwazg.kit.gson.GsonKit;
import com.lnwazg.kit.http.net.IpHostUtils;
import com.lnwazg.kit.log.Logs;
import com.lnwazg.kit.reflect.RemoteJarKit;
import com.lnwazg.kit.sort.MapSort;
import com.lnwazg.kit.swing.SwingDialogKit;
import com.lnwazg.kit.swing.SwingUtils;
import com.lnwazg.kit.swing.ui.comp.SmartButton;
import com.lnwazg.mq.framework.MQConfig;
import com.lnwazg.mq.util.MQHelper;
import com.lnwazg.myzoo.framework.MyZooClient;
import com.lnwazg.swing.util.WinMgr;
import com.lnwazg.swing.xmlbuilder.XmlJFrame;
import com.lnwazg.swing.xmlbuilder.anno.XmlBuild;
import com.lnwazg.util.Constant;

/**
 * NameNode的主窗体<br>
 * 指挥官节点，用于分发任务、分发可执行jar包、接收工人节点的上报结果并处理等等<br>
 * 基于MQ的通信方式，可用于异构的网络的数据通信
 * @author nan.li
 * @version 2017年7月2日
 */
@XmlBuild("NameNode.xml")
public class MainFrame extends XmlJFrame
{
    private static final long serialVersionUID = 416736654918898426L;
    
    /**
     * 主类属性配置文件
     */
    private static final String MAIN_CLASS_PROPERTIES_FILE_NAME = "MainClass.properties";
    
    /**
     * DateNode客户端下线超时时间<br>
     * 该值设置地越小，对客户端超时越灵敏；越大，则越不灵敏<br>
     * 然而也有可能是因为客户端过于繁忙而造成的上报延迟，因此该值应该适当增大，以防任务执行期间客户端被误识别为已掉线<br>
     * 这应该是个权衡值！<br>
     * 这个值可能会经常调整，建议做到界面上，可以实时修改生效！
     */
    private static final long TIME_TO_OFFLINE = 30;
    
    /**
     * 已完成工作任务的DataNode客户端列表
     */
    public static List<String> jobDoneDataNodeList = Collections.synchronizedList(new LinkedList<>());//写性能较好  读性能（采用了synchronized关键字的方式）较差
    
    /**
     * 在线DataNode客户端列表<br>
     * 待监听的邮箱地址<br>
     * 这个地址列表是动态变化的
     */
    //    public static List<String> onlineDataNodeList = Collections.synchronizedList(new LinkedList<>());//写性能较好  读性能（采用了synchronized关键字的方式）较差
    
    /**
     * 在线DataNode客户端列表<br>
     * 待监听的邮箱地址<br>
     * 这个地址列表是动态变化的
     */
    public static Map<String, DateNodeInfo> onlineDataNodeInfoMap = new ConcurrentHashMap<>();
    
    /**
     * 判断是否当前所有的任务都做完了<br>
     * 有些客户端可能是后上线的，所以判断条件放宽松
     * @author nan.li
     * @return
     */
    public static boolean isAllTasksDone()
    {
        //实时统计在线客户端数量
        int onlineSize = 0;
        for (String nodeName : onlineDataNodeInfoMap.keySet())
        {
            if (onlineDataNodeInfoMap.get(nodeName).isAlive())
            {
                onlineSize++;
            }
        }
        //若已完成数量不小于在线数量，说明所有在线的任务都上报完毕了！
        return jobDoneDataNodeList.size() >= onlineSize;
    }
    
    /**
     * 我的MQ邮箱
     */
    String myselfAddress = "NameNode";
    
    private JTextPane logScreen;
    
    /**
     * 更新客户端的按钮
     */
    private SmartButton updateClient;
    
    /**
     * 上传可执行jar包的按钮
     */
    private SmartButton uploadJarToExec;
    
    /**
     * 清屏按钮
     */
    private SmartButton clearLogScreen;
    
    /**
     * 状态栏信息
     */
    private JLabel statusLabel;
    
    /**
     * 在线NameNode客户端列表
     */
    private JTextPane onlineInfo;
    
    /**
     * 本次发送的mq消息对象
     */
    //    MqMsg thisTimeMqMsg;
    
    /**
     * 所有的中间的处理结果<br>
     * key为nodeNum，value为该节点的对应的HandleResult列表，并且是按顺序存放的
     */
    Map<String, List<HandleResult>> handleResultsMap = new HashMap<>();
    
    /**
     * 储存处理的中间结果<br>
     * 按nodeNum，按处理顺序存放处理结果
     * @author nan.li
     * @param handleResult
     */
    public void saveHandleResults(HandleResult handleResult)
    {
        String nodeNum = handleResult.getNodeNum();
        if (!handleResultsMap.containsKey(nodeNum))
        {
            handleResultsMap.put(nodeNum, new LinkedList<>());
        }
        List<HandleResult> oldList = handleResultsMap.get(nodeNum);
        oldList.add(handleResult);
        handleResultsMap.put(nodeNum, oldList);
    }
    
    /**
     * 增加发送因为后上线而遗漏的消息<br>
     * 这样可以有效地保障所有的客户端都执行到了那个任务！
     * @author nan.li
     * @param clientName
     */
    //    public void addSendMissingMsg(String targetAddress)
    //    {
    //        if (thisTimeMqMsg != null)
    //        {
    //            //先复制老参数
    //            String[] params = thisTimeMqMsg.getTargetInvokeMethodParams();
    //            Object[] paramsSend = new Object[params.length + 2];
    //            int i = 0;
    //            for (; i < params.length; i++)
    //            {
    //                paramsSend[i] = params[i];
    //            }
    //            //然后补充最后两个参数
    //            // "nodeNum", nodeNum++
    //            paramsSend[i] = "nodeNum";
    //            paramsSend[i + 1] = nodeNum++;
    //            MQHelper.sendAsyncMsg(targetAddress, thisTimeMqMsg.getTargetInvokeMethodFullPath(), paramsSend);
    //        }
    //    }
    
    /**
     * 节点号，每次发送新的可执行任务jar包时都说重新从0开始计数
     */
    int nodeNum = 0;
    
    /**
     * 当前在线的总节点数
     */
    int totalNodes = 0;
    
    /**
     * 当前执行的jar包的url
     */
    String jarUrl;
    
    @Override
    public void afterUIBind()
    {
        /**
         * 当从指挥官提交一个jar包的时候，将其拷贝到D:\DistributedTasks目录下，jar包生成的名称为:20170702220512.jar这种样子，精确到秒
         * 然后，NameNode给MQ发送N条消息，N为当前DataNode的在线数量！
         * 消息格式为:
         *      收件人: DataNode-aswsr2er4r234
         *      内容：     xxxTask
         *      url:   http://192.168.1.100/tasks/20170702220512.jar（启动的时候就需要启动httpServer作为jar包伺服器） 
         * 有多少个DataNode在线，就给对应的DataNode发送多少条消息
         * 
         * DataNode上线时，首先发送一条消息给NameNode，用于注册自己。自己的名字，就叫做DataNode-aswsr2er4r234，反正是个随机数
         * 消息格式为:
         *      收件人: NameNode
         *      内容：     启动注册信息
         *      
         * 此时，NameNode本地有一个OnlineDataNodeList：[DataNode-aswsr2er4r234,DataNode-aswsr2er4r235,DataNode-aswsr2er4r236]，
         * 那么发MQ消息时候，发送给这几个邮箱即可
         */
        
        //配置swing日志窗口
        logScreen.setContentType("text/html");
        Logs.addLogDest(logScreen);
        
        //        Logs.TIMESTAMP_LOG_SWITCH = true;
        //        Logs.FILE_LOG_SWITCH = true;
        
        WinMgr.reg(MainFrame.class);
        
        initMqClient();
        initHttpServer();
        
        //上传按钮事件绑定，重命名jar包，并拷贝到D:\DistributedTasks\20170702220512.jar。
        uploadJarToExec.addActionListener((e) -> {
            //选择待上传的jar包
            File file = SwingUtils.chooseFile(this, "请上传分布式任务jar包(*.jar)", "jar");
            if (file != null && file.exists())
            {
                //将其拷贝到指定目录
                File destFile = new File(String.format("%s\\%s.jar", Constant.UPLOAD_TASK_JAR_DIR, DateUtils.getNowFileDateTimeStr()));
                destFile.getParentFile().mkdirs();
                try
                {
                    FileUtils.copyFile(file, destFile);
                }
                catch (Exception e1)
                {
                    e1.printStackTrace();
                }
                SwingDialogKit.showMessageDialog(this, "可执行任务Jar包上传成功，即将开始执行...", "温馨提示", JOptionPane.PLAIN_MESSAGE);
                
                //重新初始化已完成的节点列表
                jobDoneDataNodeList.clear();
                
                //然后分别给在线的DataNode发送一条分布式计算任务的MQ消息，通知各个客户端立即加载网络jar包并执行
                jarUrl = String.format("http://%s:%s/%s/%s", IpHostUtils.getLocalHostIP(), Constant.HTTP_SERVER_PORT, Constant.HTTP_SERVER_CONTEXT_JARTASK, destFile.getName());
                Logs.d("上传的jar包url为:" + jarUrl);
                
                //计数器清理
                nodeNum = 0;
                handleResultsMap = new HashMap<>();
                
                //总在线节点数
                //这块代码严重依赖：任务执行过程中，客户端节点不可以随便失联下线！
                Logs.i("当前在线的总节点数为:" + totalNodes + "，任务运行期间工作节点不可以随便上下线！");
                
                //总任务数量
                int totalTaskNum = (int)RemoteJarKit.invokeRemoteObjectByPropertyFile(jarUrl, MAIN_CLASS_PROPERTIES_FILE_NAME, "getTotalTaskNum");
                Logs.i("总任务数量:" + totalTaskNum);
                
                for (String targetAddress : onlineDataNodeInfoMap.keySet())
                {
                    if (onlineDataNodeInfoMap.get(targetAddress).isAlive())
                    {
                        //仅对还存活着的节点发送任务消息
                        //每发送一次，计数器+1
                        MQHelper.sendAsyncMsg(targetAddress, "/inbox/runDistrJarTask", "jarUrl", jarUrl, "nodeNum", nodeNum++, "totalNodes", totalNodes, "totalTaskNum", totalTaskNum);
                    }
                }
                
                //本次执行的任务记录下来，所有后加入客户端的都应该再执行一次
                //后续的不要继续执行，防止干扰！
                //                thisTimeMqMsg = new MqMsg().setTargetInvokeMethodFullPath("/inbox/runDistrJarTask").setTargetInvokeMethodParams(new String[] {"jarUrl", jarUrl});
                
                //继续监听着。直到onlineDataNodeList里面的每个DataNode都发完了end()指令，那么代表着客户端全部计算完毕，就可以在NameNode公布最终的计算结果了！
                new Thread(() -> {
                    String taskName = (String)RemoteJarKit.invokeRemoteObjectByPropertyFile(jarUrl, MAIN_CLASS_PROPERTIES_FILE_NAME, "getTaskDescription");
                    
                    Logs.i(String.format("开始执行分布式任务【%s %s】！", destFile.getName(), taskName));
                    ExecMgr.guiExec.execute(() -> {
                        //任务详细信息可以从任务描述中读取到
                        statusLabel.setText(String.format("开始执行任务【%s】", destFile.getName()));
                    });
                    while (true)
                    {
                        //当所有任务计算完毕之后，退出死循环
                        if (isAllTasksDone())
                        {
                            // 所有任务执行完毕了，那么就可以汇总所有中间环节的执行结果了！
                            HandleResult atLastResult = reducer();
                            //以下是对最终结果的展示
                            if (atLastResult == null)
                            {
                                Logs.i("任务无汇总结果");
                            }
                            else
                            {
                                Logs.i(String.format("任务汇总结果为：%s", atLastResult.getResult()));
                            }
                            break;
                        }
                        //否则，1秒后继续检查
                        try
                        {
                            TimeUnit.SECONDS.sleep(1);
                        }
                        catch (Exception e1)
                        {
                            e1.printStackTrace();
                        }
                    }
                    Logs.i(String.format("分布式任务【%s %s】计算完毕！", destFile.getName(), taskName));
                    ExecMgr.guiExec.execute(() -> {
                        statusLabel.setText(String.format("任务【%s】计算完毕", destFile.getName()));
                    });
                }).start();
            }
        });
        
        updateClient.addActionListener(e -> {
            //首先下发最新客户端的任务
            //然后各个客户端先将客户端下载到D://DistributedClients
            //接下来，用vbs脚本拉起新客户端，然后结束掉自己
            
            //选择待上传的jar包
            File file = SwingUtils.chooseFile(this, "请上传DataNode客户端Jar包(*.jar)", "jar");
            if (file != null && file.exists())
            {
                //将其拷贝到指定目录
                File destFile = new File(String.format("%s\\DataNode_%s.jar", Constant.UPLOAD_CLIENT_JAR_DIR, DateUtils.getNowFileDateTimeStr()));
                destFile.getParentFile().mkdirs();
                try
                {
                    FileUtils.copyFile(file, destFile);
                }
                catch (Exception e1)
                {
                    e1.printStackTrace();
                }
                SwingDialogKit.showMessageDialog(this, "客户端Jar包上传成功，即将开始更新...", "温馨提示", JOptionPane.PLAIN_MESSAGE);
                
                //重新初始化已完成的节点列表
                jobDoneDataNodeList.clear();
                
                //然后分别给在线的DataNode发送一条分布式计算任务的MQ消息，通知各个客户端立即加载网络jar包并执行
                jarUrl = String.format("http://%s:%s/%s/%s", IpHostUtils.getLocalHostIP(), Constant.HTTP_SERVER_PORT, Constant.HTTP_SERVER_CONTEXT_CLIENT_UPDATES, destFile.getName());
                Logs.d("上传的jar包url为:" + jarUrl);
                
                //计数器清理
                nodeNum = 0;
                handleResultsMap = new HashMap<>();
                
                for (String targetAddress : onlineDataNodeInfoMap.keySet())
                {
                    if (onlineDataNodeInfoMap.get(targetAddress).isAlive())
                    {
                        //仅对还存活着的节点发送任务消息
                        //每发送一次，计数器+1
                        MQHelper.sendAsyncMsg(targetAddress, "/inbox/updateDataNodeClient", "jarUrl", jarUrl, "nodeNum", nodeNum++);
                    }
                }
                
                //本次执行的任务记录下来，所有后加入客户端的都应该再执行一次
                //                thisTimeMqMsg = new MqMsg().setTargetInvokeMethodFullPath("/inbox/updateDataNodeClient").setTargetInvokeMethodParams(new String[] {"jarUrl", jarUrl});
                //                thisTimeMqMsg = null;
                //必须置为null，否则刚更新的客户端上线后，会再次收到升级通知，导致无限循环死更新
                
                //继续监听着。直到onlineDataNodeList里面的每个DataNode都发完了end()指令，那么代表着客户端全部计算完毕，就可以在NameNode公布最终的计算结果了！
                new Thread(() -> {
                    Logs.i(String.format("开始分布式更新NameNode客户端！"));
                    ExecMgr.guiExec.execute(() -> {
                        //任务详细信息可以从任务描述中读取到
                        statusLabel.setText("开始分布式更新NameNode客户端！");
                    });
                    while (true)
                    {
                        //当所有任务计算完毕之后，退出死循环
                        if (isAllTasksDone())
                        {
                            break;
                        }
                        //否则，1秒后继续检查
                        try
                        {
                            TimeUnit.SECONDS.sleep(1);
                        }
                        catch (Exception e1)
                        {
                            e1.printStackTrace();
                        }
                    }
                    Logs.i(String.format("分布式更新NameNode客户端完毕！"));
                    ExecMgr.guiExec.execute(() -> {
                        statusLabel.setText("分布式更新NameNode客户端完毕！");
                    });
                }).start();
            }
        });
        
        //清屏按钮
        clearLogScreen.addActionListener(e -> {
            logScreen.setText(null);
        });
        
        //开启后台线程，每10秒刷新显示最后一次客户端的心跳时间
        ExecMgr.startDaemenThread(() -> {
            while (true)
            {
                //刷新在线列表
                refreshOnlineList();
                
                //间歇一段时间之后再试
                try
                {
                    TimeUnit.SECONDS.sleep(2);
                }
                catch (Exception e1)
                {
                    e1.printStackTrace();
                }
            }
        });
    }
    
    /**
     * 调用Jar包服务器端的指定的处理方法，对客户端运算过程中上送的参数进行处理
     * @author nan.li
     * @param paramMap
     * @return
     */
    public HandleResult mapper(Map<String, String> paramMap)
    {
        //调用远程jar包的指定配置文件的主类的指定方法，传入指定的参数表
        return (HandleResult)RemoteJarKit.invokeRemoteObjectByPropertyFile(jarUrl, MAIN_CLASS_PROPERTIES_FILE_NAME, "mapper", new Class[] {Map.class}, paramMap);
    }
    
    /**
     * 调用Jar包服务器端的指定的处理方法，对所有的处理结果进行汇总处理
     * @author nan.li
     * @param handleResultsMap2
     * @return
     */
    private HandleResult reducer()
    {
        //调用远程jar包的指定配置文件的主类的指定方法，传入指定的参数表
        return (HandleResult)RemoteJarKit.invokeRemoteObjectByPropertyFile(jarUrl, MAIN_CLASS_PROPERTIES_FILE_NAME, "reducer", new Class[] {Map.class}, handleResultsMap);
    }
    
    /**
     * 初始化http服务器
     * @author nan.li
     */
    private void initHttpServer()
    {
        HttpServer server;
        try
        {
            server = HttpServer.bind(Constant.HTTP_SERVER_PORT);
            //强制新建目录
            FileUtils.forceMkdir(new File(Constant.UPLOAD_TASK_JAR_DIR));
            FileUtils.forceMkdir(new File(Constant.UPLOAD_CLIENT_JAR_DIR));
            
            server.addWatchResourceDirRoute(Constant.HTTP_SERVER_CONTEXT_JARTASK, new File(Constant.UPLOAD_TASK_JAR_DIR));
            server.addWatchResourceDirRoute(Constant.HTTP_SERVER_CONTEXT_CLIENT_UPDATES, new File(Constant.UPLOAD_CLIENT_JAR_DIR));
            //        http://127.0.0.1:45555/jartasks/20170709101349.jar
            //        http://127.0.0.1:45555/clientupdates/DataNode_20170709101349.jar
            server.listen();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * 初始化MQ客户端
     * @author nan.li
     */
    private void initMqClient()
    {
        boolean success = MyZooClient.initDefaultConfig();
        if (success)
        {
            Map<String, String> m = MyZooClient.queryServiceConfigByNodeGroupNameThenChooseOne("mq");
            if (m == null)
            {
                Logs.e("MyZooKeeper查询MQ失败！");
                return;
            }
            //监听自己的邮箱的邮件
            boolean result = MQConfig.initMq(m.get("server"), Integer.valueOf(m.get("port")), myselfAddress, myselfAddress);
            if (result)
            {
                Logs.i("MQ初始化成功！");
            }
        }
        else
        {
            Logs.e("MyZooKeeper集群初始化失败！");
        }
    }
    
    /**
     * 记录上次刷新时间
     */
    Date lastRefresh = new Date();
    
    /**
     * 刷新在线服务器列表信息
     * @author nan.li
     */
    public void refreshOnlineList()
    {
        if (DateUtils.timeDiffAbs(new Date(), lastRefresh, TimeUnit.SECONDS) < 2)
        {
            //防止瞬时频繁刷新导致的UI性能降低、计算量暴增
            //            Logs.d("刷新瞬时量太大保护...");
            return;
        }
        int totalNodeOnline = 0;
        
        //先加工在线信息
        for (String nodeName : onlineDataNodeInfoMap.keySet())
        {
            DateNodeInfo dataNodeInfo = onlineDataNodeInfoMap.get(nodeName);
            Date lastHeartbeatTime = dataNodeInfo.getLastHeartbeatTime();
            //时间描述信息完善
            dataNodeInfo.setLastHeartbeatRemark(DateUtils.getDateTimeDiffersNowRemark(lastHeartbeatTime));
            //计算时间差
            long timeDiff = DateUtils.timeDiffAbs(new Date(), lastHeartbeatTime, TimeUnit.SECONDS);
            
            //不使超时的客户端强制下线，意味着暂时超时离线的客户端还是有机会重新上线的！
            if (timeDiff >= TIME_TO_OFFLINE)
            {
                dataNodeInfo.setAlive(false);
                dataNodeInfo.setStatus("超时离线");
            }
            else
            {
                totalNodeOnline++;
                dataNodeInfo.setAlive(true);
                dataNodeInfo.setStatus("状态正常");
            }
            onlineDataNodeInfoMap.put(nodeName, dataNodeInfo);
        }
        //刷新在线节点数的值
        totalNodes = totalNodeOnline;
        
        //然后还要刷新公告板的信息
        String onlineJson = GsonKit.prettyGson.toJson(MapSort.sort(onlineDataNodeInfoMap, new Comparator<Map.Entry<String, DateNodeInfo>>()
        {
            public int compare(Map.Entry<String, DateNodeInfo> o1, Map.Entry<String, DateNodeInfo> o2)
            {
                //优先根据状态字符串排序
                int statusResult = o1.getValue().getStatus().compareTo(o2.getValue().getStatus());
                if (statusResult == 0)
                {
                    //然后根据时间戳倒序排序
                    statusResult = o2.getValue().getLastHeartbeatTime().compareTo(o1.getValue().getLastHeartbeatTime());
                }
                return statusResult;
            }
        }));
        
        if (onlineInfo != null)
        {
            ExecMgr.guiExec.execute(() -> {
                onlineInfo.setText(onlineJson);
                onlineInfo.setCaretPosition(0);
                statusLabel.setText("当前在线节点数：" + totalNodes);
            });
        }
        
        lastRefresh = new Date();
    }
    
}
