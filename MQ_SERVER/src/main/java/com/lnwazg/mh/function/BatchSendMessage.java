package com.lnwazg.mh.function;

import java.util.List;
import java.util.Map;

import com.lnwazg.dbkit.utils.BatchInsertDBDaemonThread;
import com.lnwazg.mh.anno.Note;
import com.lnwazg.mh.anno.Param;
import com.lnwazg.mh.spi.IFunction;
import com.lnwazg.mq.entity.Message;
import com.lnwazg.util.Utils;

@Note("发消息的服务")
public class BatchSendMessage implements IFunction
{
    @Param(desc = "消息数组")
    List<Message> msgs;
    
    @Override
    public void execute(Map<String, Object> outMap)
        throws Exception
    {
        Utils.showInLogScreen(String.format("收到%d条批量提交的消息", msgs.size()));
        BatchInsertDBDaemonThread.vector.addAll(msgs);
    }
}
