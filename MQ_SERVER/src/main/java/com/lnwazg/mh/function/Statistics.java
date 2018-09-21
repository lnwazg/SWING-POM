package com.lnwazg.mh.function;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.lnwazg.kit.map.Maps;
import com.lnwazg.mh.anno.Note;
import com.lnwazg.mh.anno.Param;
import com.lnwazg.mh.spi.IFunction;
import com.lnwazg.util.Utils;

@Note("统计消息数量。可以统计某个节点的消息情况，也可以统计所有的节点的消息情况")
public class Statistics implements IFunction
{
    @Param(desc = "消息要被发送到的节点", required = false)
    String node;
    
    @Override
    public void execute(Map<String, Object> outMap)
        throws Exception
    {
        // 统计内容
        Utils.showInLogScreen(String.format("统计消息数量，节点为：%s", (StringUtils.isEmpty(node) ? "所有节点" : node)));
        outMap.put("node", node);
        int num = 0;
        if (StringUtils.isNotEmpty(node))
        {
            //查出该节点下的消息数
            num = msgDao.queryAvailableMsgNumByNode(Maps.asMap("node", node));
        }
        else
        {
            //查出所有节点下的消息数
            num = msgDao.queryAvailableMsgNum();
        }
        outMap.put("num", num);
    }
}
