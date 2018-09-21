package com.lnwazg;

import com.lnwazg.kit.testframework.TF;
import com.lnwazg.kit.testframework.anno.TestCase;
import com.lnwazg.mq.api.MqRequest;

public class Test2
{
    @TestCase
    void test0()
    {
        MqRequest request = new MqRequest("StatisticsAll");
        for (int i = 0; i < 100000; i++)
        {
            request.sendAsync();
            //            if (response.isOk())
            //            {
            //                System.out.println(response.getContent());
            //            }
            System.out.println(i);
        }
    }
    
    //    @TestCase
    void test1()
    {
        for (int i = 0; i < 10000; i++)
        {
            new MqRequest("SendMessage").addParam("message", "aaa").addParam("node", "bbbb").sendAsync();
            System.out.println(i);
        }
    }
    
    public static void main(String[] args)
    {
        TF.l(TestCase.class);
    }
}
