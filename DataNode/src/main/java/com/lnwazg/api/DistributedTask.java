package com.lnwazg.api;

import java.util.List;
import java.util.Map;

import com.lnwazg.bean.HandleResult;
import com.lnwazg.kit.log.Logs;
import com.lnwazg.swing.util.WinMgr;
import com.lnwazg.ui.MainFrame;

/**
 * 分布式任务，抽象类，便于具体任务去实现
 * @author nan.li
 * @version 2017年7月6日
 */
public abstract class DistributedTask
{
    /**
     * 当前的节点号，该值在服务端分发任务时指定。节点编号从0开始计算，第一个节点的编号为0<br>
     * 服务端主要发过来两个核心参数：<br>
     * jarUrl: 用于远程加载jar包并执行(远程加载jar包需要依赖http服务，该服务由NameNode提供)<br>
     * nodeNum：在线的DataNode的任务节点序号，从0开始计数。
     */
    protected int nodeNum = 0;
    
    /**
     * 当前在线干活的总节点数量<br>
     * 属于服务端发过来的新增参数
     */
    protected int totalNodes = 0;
    
    /**
     * 当前节点的请求序号
     */
    protected int nodeNumReqNum = 0;
    
    /**
     * 总任务数
     */
    protected int totalTaskNum = -1;
    
    /**
     * DataNode的统一执行调用入口，核心的执行方法<br>
     * 服务端主要发过来两个核心参数：<br>
     * jarUrl: 用于远程加载jar包并执行(远程加载jar包需要依赖http服务，该服务由NameNode提供)<br>
     * nodeNum：在线的DataNode的任务节点序号，从0开始计数。
     * @author nan.li
     * @param map
     */
    public void execute(Map<String, Object> map)
    {
        //获取当前的节点号
        nodeNum = Integer.valueOf(map.get("nodeNum").toString());
        
        //当前在线干活的总节点数量
        totalNodes = Integer.valueOf(map.get("totalNodes").toString());
        
        //总任务数量
        totalTaskNum = Integer.valueOf(map.get("totalTaskNum").toString());
        
        Logs.i("当前节点编号:" + nodeNum + " 总节点数：" + totalNodes + " 总任务数量:" + totalTaskNum);
        
        //定制的执行内容
        executeCustom(map);
        
        //计算结束
        end();
    }
    
    /**
     * 待客户端实现的自定义方法
     * @author nan.li
     */
    public abstract void executeCustom(Map<String, Object> map);
    
    /**
     * mapper，服务端的映射处理，存储请求参数
     * @author nan.li
     * @param paramMap  参数表，通过mq所发送出去的参数
     * @return  处理结果对象
     */
    public HandleResult mapper(Map<String, String> paramMap)
    {
        //节点号
        String nodeNum = paramMap.get("nodeNum");
        
        //节点的请求序号
        String nodeNumReqNum = paramMap.get("nodeNumReqNum");
        
        //单步的处理结果
        return new HandleResult().setNodeNum(nodeNum).setNodeNumReqNum(nodeNumReqNum).setParamMap(paramMap);
    }
    
    /**
     * reducer，服务端的汇总处理<br>
     * 所有的结果都要在这里一步汇总！
     * @author nan.li
     * @param handleResultsMap  参数表，每一步处理数据的结果集
     * @return  最终的汇总结果对象
     */
    public HandleResult reducer(Map<String, List<HandleResult>> handleResultsMap)
    {
        return null;
    }
    
    /**
     * 获取任务的描述信息
     * @author nan.li
     * @return
     */
    public abstract String getTaskDescription();
    
    /**
     * 获取当前的客户端名称
     * @author nan.li
     * @return
     */
    public String getCurrentDataNodeName()
    {
        return WinMgr.win(MainFrame.class).myselfAddress;
    }
    
    /**
     * 计算过程的数据上报<br>
     * 会自定上报当前的节点号
     * @author nan.li
     * @param key
     * @param value
     */
    public void report(Object... keyvalues)
    {
        Object[] paramsSend = new Object[keyvalues.length + 2 + 2];
        int i = 0;
        for (; i < keyvalues.length; i++)
        {
            paramsSend[i] = keyvalues[i];
        }
        // "nodeNum", nodeNum++
        //节点号
        paramsSend[i] = "nodeNum";
        paramsSend[i + 1] = nodeNum;
        
        //该节点的请求序号
        paramsSend[i + 2] = "nodeNumReqNum";
        paramsSend[i + 3] = nodeNumReqNum++;
        
        WinMgr.win(MainFrame.class).report(paramsSend);
    }
    
    /**
     * 获取总任务数<br>
     * 默认为-1，该方法可以被重载
     * @author nan.li
     * @return
     */
    protected int getTotalTaskNum()
    {
        return -1;
    }
    
    /**
     * 计算当前的任务
     * @author nan.li
     * @param taskNum
     * @return
     */
    protected Object calcTask(long taskNum)
    {
        Logs.i("正在计算任务编号:" + taskNum);
        return calcTaskCustom(taskNum);
    }
    
    /**
     * 真正的计算当前任务的代码
     * @author nan.li
     * @param taskNum
     * @return
     */
    protected Object calcTaskCustom(long taskNum)
    {
        return null;
    }
    
    /**
     * 任务执行完毕了
     * @author nan.li
     */
    public void end()
    {
        WinMgr.win(MainFrame.class).endTask();
    }
}
