package com.lnwazg.mh.function;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.lnwazg.kit.executor.ExecMgr;
import com.lnwazg.kit.map.Maps;
import com.lnwazg.mh.anno.Note;
import com.lnwazg.mh.anno.Param;
import com.lnwazg.mh.spi.IFunction;
import com.lnwazg.mq.entity.Message;
import com.lnwazg.util.Utils;

@Note("收消息的服务，可以分页，也可以全部收取")
public class ReceiveMessage implements IFunction
{
    @Param(desc = "消息要被发送到的节点")
    String node;
    
    @Param(desc = "限制的条数", required = false)
    Integer limit;
    
    //    @Param(desc = "是否阅后即焚", required = false)
    //    Boolean delAfterRead;
    
    @Override
    public void execute(Map<String, Object> outMap)
        throws Exception
    {
        Utils.showInLogScreen(String.format("收取消息：【Node】 %s 【limit】 %s", node, limit));
        //查询出来，并作出相应的处理
        List<Message> resultList = null;
        if (limit != null)
        {
            //分页查询
            resultList = msgDao.queryByLimit(Maps.asMap("limit", limit, "node", node));
        }
        else
        {
            //全部查询。慎用！
            resultList = msgDao.queryAll(Maps.asMap("node", node));
        }
        
        List<Map<String, Object>> list = new ArrayList<>();
        for (Message message : resultList)
        {
            list.add(Maps.asMap("content", message.getContent()));
        }
        outMap.put("list", list);
        //查完之后标记这些消息已经读完毕
        //如果阅后即焚，则还要标记这些标记已经焚烧掉
        final List<Message> updateList = resultList;
        
        //        //只有这个参数存在的时候，并且为true的时候，才有必要存取
        //        if (delAfterRead != null && delAfterRead)
        //        {
        //阅后即焚的话，要将其删除掉
        ExecMgr.singleExec.execute(() -> {
            try
            {
                if (updateList != null && updateList.size() > 0)
                {
                    for (Message message : updateList)
                    {
                        msgDao.delete(message);
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }
}
