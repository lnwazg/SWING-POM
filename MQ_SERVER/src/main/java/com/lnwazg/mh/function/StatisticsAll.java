package com.lnwazg.mh.function;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;

import com.lnwazg.kit.list.Lists;
import com.lnwazg.mh.anno.Note;
import com.lnwazg.mh.spi.IFunction;
import com.lnwazg.util.Utils;

@Note("分组全部统计消息的数量。返回所有的值以及每个组的值")
public class StatisticsAll implements IFunction
{
    @Override
    public void execute(Map<String, Object> outMap)
        throws Exception
    {
        // 统计内容
        Utils.showInLogScreen(String.format("分组统计消息数量"));
        //查出该节点下的消息数
        List<Map<String, Object>> list = msgDao.queryAvailableMsgNumGroupByNode();
        int allNum = 0;
        if (Lists.isNotEmpty(list))
        {
            for (Map<String, Object> map : list)
            {
                //                String node = ObjectUtils.toString(map.get("node"));
                int count = Integer.valueOf(ObjectUtils.toString(map.get("count")));
                allNum += count;
            }
        }
        outMap.put("allCount", allNum);
        outMap.put("detailList", list);
    }
}
