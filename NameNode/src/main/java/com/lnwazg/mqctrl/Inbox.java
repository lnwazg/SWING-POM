package com.lnwazg.mqctrl;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.lnwazg.bean.DateNodeInfo;
import com.lnwazg.bean.HandleResult;
import com.lnwazg.kit.controllerpattern.Controller;
import com.lnwazg.kit.log.Logs;
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
     * 客户端上线通知的消息，也可以用于心跳上报
     * @author nan.li
     */
    void clientOnline()
    {
        //当前上线的客户端的名称
        String clientName = paramMap.get("clientName");
        //该客户端不为空，则将其加入到上线列表中
        if (StringUtils.isNotEmpty(clientName))
        {
            if (!MainFrame.onlineDataNodeInfoMap.containsKey(clientName))
            {
                Logs.i(clientName + " online!");
                //首次上线
                //将该客户端添加到在线列表中
                MainFrame.onlineDataNodeInfoMap.put(clientName, new DateNodeInfo().setLastHeartbeatTime(new Date()));
                
                //给该客户端补发遗漏掉的消息
                //                WinMgr.win(MainFrame.class).addSendMissingMsg(clientName);
                //任务执行中不允许节点上下线！否则会造成数据错乱！
                
                //刷新公告板上的在线客户端列表
                WinMgr.win(MainFrame.class).refreshOnlineList();
            }
            else
            {
                Logs.i(clientName + " heartbeat...");
                //非首次上线，那么就需要更新心跳信息
                DateNodeInfo oldDataNodeInfo = MainFrame.onlineDataNodeInfoMap.get(clientName);
                //以收到该节点的消息的时间为准，作为该节点的上次心跳时间
                oldDataNodeInfo.setLastHeartbeatTime(new Date());
                MainFrame.onlineDataNodeInfoMap.put(clientName, oldDataNodeInfo);
            }
        }
    }
    
    /**
     * 某个客户端任务执行完毕了
     * @author nan.li
     */
    void end()
    {
        String clientName = paramMap.get("clientName");
        //该客户端不为空，则将其加入到上线列表中
        if (StringUtils.isNotEmpty(clientName))
        {
            if (!MainFrame.jobDoneDataNodeList.contains(clientName))
            {
                MainFrame.jobDoneDataNodeList.add(clientName);
            }
        }
    }
    
    /**
     * 接收客户端上送的计算结果数据，并定制化处理<br>
     * 同样调用远程jar包进行处理，将处理结果封装成对象
     * @author nan.li
     */
    void reportKeyValues()
    {
        /**
         * 客户端可能会上报任意的参数，例如:<br>
         * report("sum", sum);
         * 
         * 但是有一个基础参数一定会上报，那就是DataNode的节点号nodeNum
         */
        //节点号
        String nodeNum = paramMap.get("nodeNum");
        
        //调用Jar包服务器端的指定的处理方法，对客户端运算过程中上送的参数进行处理
        HandleResult handleResult = WinMgr.win(MainFrame.class).mapper(paramMap);
        //处理结果中增加节点号数据
        handleResult.setNodeNum(nodeNum);
        //储存处理结果
        WinMgr.win(MainFrame.class).saveHandleResults(handleResult);
    }
}
