package com.lnwazg.mh.function;

import java.util.Date;
import java.util.Map;

import com.lnwazg.dbkit.utils.BatchInsertDBDaemonThread;
import com.lnwazg.mh.anno.Note;
import com.lnwazg.mh.anno.Param;
import com.lnwazg.mh.spi.IFunction;
import com.lnwazg.mq.entity.Message;
import com.lnwazg.util.Utils;

@Note("发消息的服务")
public class SendMessage implements IFunction
{
    @Param(desc = "消息要被发送到的节点")
    String node;
    
    @Param(desc = "消息的内容")
    String message;
    
    @Override
    public void execute(Map<String, Object> outMap)
        throws Exception
    {
        Utils.showInLogScreen(String.format("MQ系统收到消息！\r\n目标邮箱：%s，\r\n消息内容：%s", node, message));
//        Logs.i(String.format("MQ系统收到消息！\r\n目标邮箱：%s，\r\n消息内容：%s", node, message));
        Message m = new Message().setNode(node).setContent(message).setCreateTime(new Date());
        BatchInsertDBDaemonThread.vector.add(m);
    }
}
