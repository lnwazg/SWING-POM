package com.lnwazg.util.smart.contentprocess;

import org.apache.commons.lang3.StringUtils;

import com.lnwazg.kit.robot.turing.RobotKit;
import com.lnwazg.kit.rss.GuokeRss;
import com.lnwazg.util.BaiduYunNoteSyncTool;

/**
 * 内容处理器扩展<br>
 * 超智能处理总线，轮询法，并且还能超轻量快速调整，一个定时器搞定一切！
 * @author nan.li
 * @version 2017年8月15日
 */
public class SmartContentProcessor
{
    /**
     * 对笔记的原始内容进行转换<br>
     * 可定制多重子处理器，增强备忘录的功能，也可以加入特殊处理指令，实现自定义的自动化工作
     * @author nan.li
     * @param line
     * @return
     */
    public static String transferAndHandleContent(String line)
    {
        String result = line;
        switch (line)
        {
            case "[南京天气]":
                //查询南京天气
                result = RobotKit.talk("南京天气");
                break;
            case "[果壳新闻]":
                result = StringUtils.join(GuokeRss.getNowRssList(), "\n");
                break;
            case "[发送邮件|张三|下午一起去打牌！]":
                result = StringUtils.join(GuokeRss.getNowRssList(), "\n");
                break;
            case "[百度云记事合并]":
                BaiduYunNoteSyncTool.process();
                result = line;
                break;
                
            default:
                //正则匹配也要走进这里！
                break;
        }
        return result;
    }
}
