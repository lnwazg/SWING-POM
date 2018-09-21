package com.lnwazg;

import java.util.List;
import java.util.Map;

import com.lnwazg.dao.MessageDao;
import com.lnwazg.dbkit.jdbc.MyJdbc;
import com.lnwazg.dbkit.proxy.DaoProxy;
import com.lnwazg.dbkit.utils.DbKit;
import com.lnwazg.kit.random.RandomStrUtils;
import com.lnwazg.kit.testframework.TF;
import com.lnwazg.kit.testframework.anno.AfterFinalOnce;
import com.lnwazg.kit.testframework.anno.PrepareStartOnce;
import com.lnwazg.kit.testframework.anno.TestCase;
import com.lnwazg.mq.entity.Message;

/**
 * 无须羡慕c3p0连接池，因此已经采用了更屌的DruidDataSource
 * @author nan.li
 * @version 2016年5月20日
 */
public class SQliteTest
{
    MyJdbc jdbc = null;
    
    MessageDao msgDao = null;
    
    boolean isMysql = false;
    
    @PrepareStartOnce
    void prepareStartOnce()
    {
        String password = "";
        String url = "";
        String username = "";
        if (isMysql)
        {
            url =
                "jdbc:mysql://127.0.0.1:3306/MQ?useUnicode=true&generateSimpleParameterMetadata=true&characterEncoding=UTF-8&autoReconnect=true&autoReconnectForPools=true";
            username = "root";
            jdbc = DbKit.getJdbc(url, username, password);
        }
        else
        {
            jdbc = DbKit.getDefaultJdbc();
        }
        msgDao = DaoProxy.proxyDaoInterface(MessageDao.class, jdbc);//根据接口生成动态代理类
    }
    
    @AfterFinalOnce
    void afterFinalOnce()
    {
    }
    
    @TestCase
    void test0()
    {
        //测试100,0000条数据
        //内存内ehcache检索，花费：query cost 17781 ms【插入速度倒是最快的，毕竟内存操作嘛】
        //mysql全文检索，花费0.002秒【文件+内存型数据库，插入速度也很可以，大概十几秒钟】
        //sqlite全文检索，花费0.503秒，也还可以！          【毕竟是文件型数据库，因此插入速度最慢，大概几分钟时间】
        try
        {
            //索引技术很重要！可以大幅度降低查询耗时！
            msgDao.createTable(Message.class);
            System.out.println("begin...");
            for (int i = 0; i < 1000000; i++)
            {
                msgDao.insert(new Message().setNode("小燕").setContent("love" + RandomStrUtils.generateRandomString(30)).setCreateTime(new java.util.Date()));
            }
            System.out.println("Ok...");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    //    @TestCase
    void testSelect2()
    {
        try
        {
            Message message = jdbc.load(Message.class, 49953);
            System.out.println(message);
            Boolean delAfterRead = null;
            //            message.setSent(true);
            //            message.setCreateTime(new Date());
            //            message.setDeleted(delAfterRead);
            //            message.setDeleted(false);
            jdbc.updateEntity(message);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    //    @TestCase
    void test3()
    {
        List<Map<String, Object>> list = msgDao.queryAvailableMsgNumGroupByNode();
        System.out.println(list);
    }
    
    public static void main(String[] args)
    {
        TF.l(SQliteTest.class);
    }
}
