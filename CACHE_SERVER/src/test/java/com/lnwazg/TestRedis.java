package com.lnwazg;

import java.util.Map;

import com.lnwazg.cache.client.RemoteCacheServer;
import com.lnwazg.cache.proxy.Cache;
import com.lnwazg.kit.testframework.TF;
import com.lnwazg.kit.testframework.anno.TestCase;
import com.lnwazg.myzoo.framework.MyZooClient;

public class TestRedis
{
    public static void main(String[] args)
    {
        boolean success = MyZooClient.initDefaultConfig();
        System.out.println(success);
        TF.l(TestRedis.class);
    }
    
    @TestCase
    void test1()
    {
        Map<String, String> m = MyZooClient.queryServiceConfigByNodeNameStartWithThenChooseOne("remoteCache");
        Cache cache = RemoteCacheServer.initConfig(m.get("server"), Integer.valueOf(m.get("port")));
        
        //        cache.put(5, 100.78D);
        //        System.out.println(cache.get(5));
        
//                        cache.put(6, 100);
//        System.out.println(cache.incr(6));
        System.out.println(cache.decr(6));
        System.out.println(cache.get(6));
    }
    
}