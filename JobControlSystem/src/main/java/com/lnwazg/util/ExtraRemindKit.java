package com.lnwazg.util;

import java.util.HashSet;
import java.util.Set;

import com.lnwazg.kit.date.DateUtils;
import com.lnwazg.ui.MainFrame;

/**
 * 额外提醒的工具包<br>
 * 目前都是直接添加到内存里，重启应用后便失效
 * @author nan.li
 * @version 2017年8月2日
 */
public class ExtraRemindKit
{
    /**
     * 添加一条今日提醒<br>
     * 添加的内容不会重复提醒，因为是加入到一个Set里面的！
     * @author nan.li
     * @param remindContent
     */
    public static void addTodayRemind(String remindContent)
    {
        //今天的日期字符串，格式为： 20170802
        String todayDateStr = DateUtils.getNowDateNoConnectorStr();
        if (!MainFrame.extraRemindThings.containsKey(todayDateStr))
        {
            MainFrame.extraRemindThings.put(todayDateStr, new HashSet<>());
        }
        //因为是操作数据库，所以必须手动操作，而不能指望内存引用自动操作完成
        Set<String> set = MainFrame.extraRemindThings.get(todayDateStr);
        set.add(remindContent);
        MainFrame.extraRemindThings.put(todayDateStr, set);
    }
    
    /**
     * 增加一条明日提醒<br>
     * 添加的内容不会重复提醒，因为是加入到一个Set里面的！
     * @author nan.li
     * @param remindContent
     */
    public static void addTomorrowRemind(String remindContent)
    {
        //明天的日期字符串，格式为： 20170803
        String tomorrowDateStr = DateUtils.getFormattedDateTimeStr(DateUtils.DEFAULT_DATE_FORMAT_PATTERN_NO_CONNECTOR, DateUtils.getTomorrowDate());
        if (!MainFrame.extraRemindThings.containsKey(tomorrowDateStr))
        {
            MainFrame.extraRemindThings.put(tomorrowDateStr, new HashSet<>());
        }
        //因为是操作数据库，所以必须手动操作，而不能指望内存引用自动操作完成
        Set<String> set = MainFrame.extraRemindThings.get(tomorrowDateStr);
        set.add(remindContent);
        MainFrame.extraRemindThings.put(tomorrowDateStr, set);
    }
    
    /**
     * 添加一条后天提醒<br>
     * 添加的内容不会重复提醒，因为是加入到一个Set里面的！
     * @author nan.li
     * @param remindContent
     */
    public static void addAfterTomorrowRemind(String remindContent)
    {
        String dateStr = DateUtils.getFormattedDateTimeStr(DateUtils.DEFAULT_DATE_FORMAT_PATTERN_NO_CONNECTOR, DateUtils.getAfterTomorrowDate());
        if (!MainFrame.extraRemindThings.containsKey(dateStr))
        {
            MainFrame.extraRemindThings.put(dateStr, new HashSet<>());
        }
        //因为是操作数据库，所以必须手动操作，而不能指望内存引用自动操作完成
        Set<String> set = MainFrame.extraRemindThings.get(dateStr);
        set.add(remindContent);
        MainFrame.extraRemindThings.put(dateStr, set);
    }
}
