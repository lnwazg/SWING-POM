package com.lnwazg.mh.spi;

import java.util.Map;

import com.lnwazg.dao.MessageDao;
import com.lnwazg.dbkit.jdbc.MyJdbc;
import com.lnwazg.kit.singleton.BeanMgr;

/**
 * 消息中间件所提供的功能的通用的待实现接口
 * @author Administrator
 * @version 2016年7月29日
 */
public interface IFunction
{
    static MyJdbc myJdbc = BeanMgr.get(MyJdbc.class);
    
    static MessageDao msgDao = BeanMgr.get(MessageDao.class);
    
    public void execute(Map<String, Object> outMap)
        throws Exception;
}
