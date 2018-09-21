package com.lnwazg.mqctrl;

import com.lnwazg.kit.controllerpattern.Controller;
import com.lnwazg.kit.date.DateUtils;
import com.lnwazg.kit.reflect.ClassKit;
import com.lnwazg.kit.testframework.TF;
import com.lnwazg.kit.testframework.anno.TestCase;
import com.lnwazg.mq.framework.BaseController;

/**
 * 单线联系是指在地下工作时，为了防止工作人员被俘后背叛导致一大批人员暴露，就采用单线联系的方式。<br>
 * 单线联系就是一个人只有一个上级和一个下级，不与其他人发生工作联系。<br>
 * 这样，即使他被捕，需要转移的也只有2个人而已。<br>
 * 所有的单线最终汇总到一个最大的头目。<br>
 * @author nan.li
 * @version 2016年10月8日
 */
@Controller("/news")
public class Newspaper extends BaseController
{
    void readNews()
    {
        String content = paramMap.get("news");
        System.out.println("朝日新闻:" + content);
        //只要定期目标主机、消息类型、消息内容，即可实现异步通讯！
        //通讯消息的投递，很可靠！
        //        MQUtils.sendAsyncMsg("Jerry", "/news/thankyou", "message", "消息已经收到，灰常感谢" + DateUtils.getCurStandardDateTimeStr());
        reply("/news/thankyou", "message", "消息已经" + "收到，灰常感谢" + DateUtils.getNowDateTimeStr());
    }
    
    void thankyou()
    {
        String content = paramMap.get("message");
        System.out.println(content);
        //        MQUtils.sendAsyncMsg("Tom", "/news/readNews", "news", "不用谢，这是第二条新闻：文曲星黄金英雄坛说正式适配NC3000！" + DateUtils.getCurStandardDateTimeStr());
        reply("/news/readNews", "news", "不用谢，这是第二条新闻：文曲星黄金英雄坛说正式适配NC3000！" + DateUtils.getNowDateTimeStr());
    }
    
    public static void main(String[] args)
    {
        TF.l(Newspaper.class);
    }
    
    @TestCase
    void test()
    {
        Object object = new Newspaper();
        for (int i = 0; i < 10000000; i++)
        {
            ClassKit.invokeMethod(object, "fff");
        }
    }
}
