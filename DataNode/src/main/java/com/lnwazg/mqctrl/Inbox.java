package com.lnwazg.mqctrl;

import java.io.File;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.lnwazg.api.DistributedTask;
import com.lnwazg.kit.controllerpattern.Controller;
import com.lnwazg.kit.date.DateUtils;
import com.lnwazg.kit.executor.ExecMgr;
import com.lnwazg.kit.http.DownloadKit;
import com.lnwazg.kit.log.Logs;
import com.lnwazg.kit.reflect.RemoteJarKit;
import com.lnwazg.kit.shell.CmdUtils;
import com.lnwazg.mq.framework.BaseController;
import com.lnwazg.swing.util.WinMgr;
import com.lnwazg.ui.MainFrame;

/**
 * 消息收件箱
 * @author nan.li
 * @version 2017年7月9日
 */
@Controller("/inbox")
public class Inbox extends BaseController
{
    /**
     * 运行分布式任务<br>
     * 服务端主要发过来两个核心参数：<br>
     * jarUrl: 用于远程加载jar包并执行(远程加载jar包需要依赖http服务，该服务由NameNode提供)<br>
     * nodeNum：在线的DataNode的任务节点序号，从0开始计数。
     * @author nan.li
     */
    void runDistrJarTask()
    {
        //这个任务可能非常耗时，因此异步执行。执行完毕后会自动上报结果的
        //有些地方可以异步，有些地方却不能异步。
        //若处理不好这一点，就会造成奇怪的系统异常！
        //这就考验高级架构书的素养了！
        
        //不能在发消息的入口处做异步，因为那会影响到所有mq消息通知逻辑的时效性！
        //所以，正确的处理方法为：在需要异步的地方异步！仅在对性能产生瓶颈的地方进行异步！
        
        ExecMgr.cachedExec.execute(() -> {
            //当前需要运行的jar包任务的url
            String jarUrl = paramMap.get("jarUrl");
            
            //该客户端不为空，则将其加入到上线列表中
            if (StringUtils.isNotEmpty(jarUrl))
            {
                //加载远程jar包，并执行分布式任务。
                //什么时候任务执行结束，由具体的执行任务类自己去处理
                //根据该URL进行加载jar包，并执行事先约定好的方法名称，该执行的时候执行，该上报的时候上报。当完毕之后，要发送一条end()指令。
                
                //http://10.13.69.28:45555/jartask/20170712105135.jar
                String jarName = jarUrl.substring(jarUrl.lastIndexOf("/") + 1);
                WinMgr.win(MainFrame.class).showStatus("开始执行" + jarName);
                
                //jar包中的属性表配置文件
                //            Map<String, String> propMap = RemoteJarKit.loadRemotePropertyMap(jarUrl, "MainClass.properties");
                //            if (propMap == null || propMap.isEmpty())
                //            {
                //                Logs.e("远程jar包中的入口类配置文件不存在或者信息为空！因此无法执行远程jar包任务！");
                //                return;
                //            }
                //            String mainClassFullPath = propMap.get("Main");
                //            if (StringUtils.isEmpty(mainClassFullPath))
                //            {
                //                Logs.e("远程jar包中的入口类配置文件中缺少主类配置信息！因此无法执行远程jar包任务！");
                //                return;
                //            }
                //            RemoteJarKit.invokeRemoteObject(jarUrl, mainClassFullPath, "execute", new Class[] {Map.class}, paramMap);
                
                //调用远程jar包的指定配置文件的主类的指定方法，传入指定的参数表
                RemoteJarKit.invokeRemoteObjectByPropertyFile(jarUrl, "MainClass.properties", "execute", new Class[] {Map.class}, paramMap);
                
                WinMgr.win(MainFrame.class).showStatus(jarName + "执行完毕");
            }
        });
    }
    
    /**
     * DATANODE的更新目录
     */
    String DATANODE_UPDATE_DIR = "D://DataNodeUpdates";
    
    /**
     * DataNode收到更新请求后的处理
     * @author nan.li
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    void updateDataNodeClient()
    {
        String jarUrl = paramMap.get("jarUrl");
        if (StringUtils.isNotEmpty(jarUrl))
        {
            //http://10.13.69.28:45555/clientupdates/DataNode_20170709101349.jar
            WinMgr.win(MainFrame.class).showStatus("开始更新客户端");
            
            //每个文件时间戳都不一样，可以防止多进程操作产生的冲突的问题
            String jarName = "UPDATE_" + DateUtils.getNowFileDateTimeStr() + ".jar";
            
            new DistributedTask()
            {
                @Override
                public String getTaskDescription()
                {
                    return "更新客户端的任务";
                }
                
                @Override
                public void executeCustom(Map<String, Object> map)
                {
                    //然后各个客户端先将客户端下载到D://DatanodeUpdates
                    //下载客户端到本地
                    try
                    {
                        Logs.d("begin to download NameNode update jar...");
                        //即将下载的那个jar包的文件
                        File downloadedJar = new File(DATANODE_UPDATE_DIR, jarName);
                        //父目录提前创建好，防止父目录不存在导致的写入失败！
                        downloadedJar.getParentFile().mkdirs();
                        //下载文件
                        DownloadKit.downloadFile(jarUrl, downloadedJar);
                        Logs.d("NameNode update jar download OK!");
                        
                        Logs.d("write update point file...");
                        //记录下最新客户端的文件指针
                        File pointFile = new File(DATANODE_UPDATE_DIR, "latestClientFile.txt");
                        //记录指针文件
                        FileUtils.writeStringToFile(pointFile, downloadedJar.getPath(), CharEncoding.UTF_8);
                        
                        //接下来，用vbs脚本拉起新客户端
                        Logs.d("generate startup vbs...");
                        //读取vbs脚本模板
                        String vbsTemplateStr = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("runClient.vbs"));
                        //将下载的那个客户端文件的全路径替换到vbs脚本的表达式中
                        String vbsStrContent = StringUtils.replace(vbsTemplateStr, "${path}", downloadedJar.getPath());
                        //之所以每个jar包都得有自己的vbs文件，是为了防止并发操作所引发的冲突
                        //脚本文件的全路径
                        File targetVbsFile = new File(DATANODE_UPDATE_DIR, jarName + ".vbs");
                        //将vbs脚本内容写入到指定路径下
                        FileUtils.writeStringToFile(targetVbsFile, vbsStrContent, CharEncoding.UTF_8);
                        Logs.d("execute startup vbs...");
                        //指定指定全路径下的vbs脚本
                        CmdUtils.execute(String.format("cscript %s", targetVbsFile.getPath()));
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }.execute((Map)paramMap);
            
            WinMgr.win(MainFrame.class).showStatus("客户端更新完毕，即将退出...");
            
            //然后起线程一段时间后结束掉自己
            //一定要起新线程去结束自己，否则end()信息无法发出去，会一直阻塞在这里！
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        int sleepSeconds = 5;
                        Logs.d("Will exit after " + sleepSeconds + " seconds...");
                        TimeUnit.SECONDS.sleep(sleepSeconds);
                        System.exit(0);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
    
    public static void main(String[] args)
    {
        //远程执行jar包有以下几种方式：
        //        System.out.println((Object)RemoteJarKit.invokeRemoteObject("file:\\c:\\1.jar", "com.lnwazg.api.Task", "getTaskDescription"));
        //        System.out.println((Object)RemoteJarKit.invokeRemoteObject("file:\\c:\\1.jar", "com.lnwazg.api.Task", "execute"));
        //        RemoteJarKit.loadRemoteClass("file:\\c:\\2.jar", "com.lnwazg.api.Dep");
        //        System.out.println((Object)RemoteJarKit.invokeRemoteObject("file:\\c:\\2.jar", "com.lnwazg.api.Task", "getTaskDescription"));
        //        System.out.println((Object)RemoteJarKit.invokeRemoteObject("http://10.18.18.148:45555/jartask/20170709122253.jar", "com.lnwazg.api.Task", "getTaskDescription"));
        //        System.out.println((Object)RemoteJarKit.invokeRemoteObject("file:\\c:\\2.jar", "com.lnwazg.api.Task", "execute"));
        
        //具体的测试代码如下：
        //        System.out.println((Object)RemoteJarKit.invokeRemoteObject("file:\\D:\\Documents\\002.jar", "com.lnwazg.Task001", "getTaskDescription"));
        //        System.out.println((Object)RemoteJarKit.invokeRemoteObject("file:\\D:\\Documents\\002.jar", "com.lnwazg.Task002", "getTaskDescription"));
        
        //        String resourceContent = RemoteJarKit.loadRemoteResourceContent("file:\\D:\\Documents\\002.jar", "MainClass.properties");
        //        String resourceContent = RemoteJarKit.loadRemoteResourceContent("file:\\D:\\Documents\\003.jar", "more/2.properties");
        //        System.out.println(resourceContent);
        
        //        Map<String, String> map = RemoteJarKit.loadRemotePropertyMap("file:\\D:\\Documents\\002.jar", "MainClass.properties");
        //        String resourceContent = RemoteJarKit.loadRemoteResourceContent("file:\\D:\\Documents\\003.jar", "more/2.properties");
        //        D.d(map);
    }
}
