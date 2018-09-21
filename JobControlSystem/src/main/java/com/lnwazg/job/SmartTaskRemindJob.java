package com.lnwazg.job;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.MutablePair;
import org.quartz.JobExecutionContext;

import com.lnwazg.kit.charset.CharsetKit;
import com.lnwazg.kit.date.DateUtils;
import com.lnwazg.kit.date.DateUtils.CalendarDesc;
import com.lnwazg.kit.job.Scheduled;
import com.lnwazg.kit.list.Lists;
import com.lnwazg.kit.log.Logs;
import com.lnwazg.kit.testframework.TF;
import com.lnwazg.kit.testframework.anno.TestCase;
import com.lnwazg.kit.validate.Validates;
import com.lnwazg.swing.util.WinMgr;
import com.lnwazg.swing.util.quartz.ControlableJob;
import com.lnwazg.ui.MainFrame;
import com.lnwazg.util.Constant;
import com.lnwazg.util.DiaryKit;
import com.lnwazg.util.smart.contentprocess.SmartContentProcessor;

/**
 * 智能任务导航系统<br>
 * 现在可以智能识别出本地硬盘与移动硬盘<br>
 * 优先识别出本地硬盘，其次才是移动硬盘<br>
 * 注释很重要，因为再nb的代码，长久不阅读，也会遗忘，也会变得难以理解！
 * @author nan.li
 * @version 2016年8月18日
 */
@Scheduled(cron = "0 * * * * ?")
public class SmartTaskRemindJob extends ControlableJob
{
    
    /**
     * 任务开始标记
     */
    private static final String TASKS_START_FLAG = ">>>>>>>>>>>>>>>>>>TASKS>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>";
    
    /**
     * 任务结束标记
     */
    private static final String TASKS_FINISH_FLAG = "<<<<<<<<<<<<<<<<<<TASKS<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<";
    
    @TestCase
    @Override
    public void executeCustom(JobExecutionContext context)
    {
        //行程提醒&&人生箴言提醒功能，优先从E盘读资料；若E盘中没有资料，才会去移动硬盘（O盘）中去读取
        
        //E:/2012
        String taskRemindBasePath = Constant.TASK_REMIND_BASEPATH_1;
        if (!new File(taskRemindBasePath).exists())
        {
            taskRemindBasePath = Constant.TASK_REMIND_BASEPATH_2;
            if (!new File(taskRemindBasePath).exists())
            {
                Logs.i(String.format("TASK_REMIND_BASEPATH %s 不存在，因此行程提醒功能无法使用", taskRemindBasePath));
                return;
            }
        }
        
        //日志文件的基础目录，是动态拼接的。例如： O:/2012/d心理碎片trunk
        //这个目录的基础目录，由上面的逻辑去决定
        Constant.DIARY_BASE_PATH = String.format("%s/d心理碎片trunk", taskRemindBasePath);
        
        //获取当前日期时间的全部描述信息
        CalendarDesc calendarDesc = DateUtils.getCurrentCalendarDesc();
        
        //私有文件与公有文件的替换关系：
        
        //1.（普通版、经常需要变动的）行程提醒
        //优先  E:\2012\d项目管理之路\TODO.txt
        //如果上述路径不存在，则采用备用方案：     E:\2012\d心理碎片trunk\d代码碎片汇总_20160101\2016年8月\系统说明_20160801.txt
        
        //2.（四季要记、模板化的，沉淀下来的固定不变的）行程提醒 
        //E:\2012\d四季要记\all.txt
        
        //（普通版、经常需要变动的）行程提醒自适应代码开始
        //优先  E:\2012\d项目管理之路\todo.txt 
        String fileFullPath = String.format("%s\\d项目管理之路\\TODO.txt", taskRemindBasePath);
        if (!new File(fileFullPath).exists())
        {
            //优先的路径不能满足，则退而求其次，采用   E:\2012\d心理碎片trunk\d代码碎片汇总_20160101\2016年8月\系统说明_20160801.txt
            fileFullPath = String.format("%s\\d心理碎片trunk\\d代码碎片汇总_%d0101\\%d年%d月\\系统说明_%d%02d01.txt",
                taskRemindBasePath,
                calendarDesc.getYear(),
                calendarDesc.getYear(),
                (calendarDesc.getMonth() + 1),
                calendarDesc.getYear(),
                (calendarDesc.getMonth() + 1));
                
            Logs.i("TODO路径不存在，因此准备检测系统日志信息。FileFullPath为：" + fileFullPath);
            if (!new File(fileFullPath).exists())
            {
                Logs.i(String.format("所有的配置路径均无效，因此无法执行智能提醒功能"));
                return;
            }
        }
        //        Logs.i("TODO路径为： " + fileFullPath);
        //（普通版、经常需要变动的）行程提醒自适应代码结束
        try
        {
            List<String> toShowMsgs = new ArrayList<>();
            
            //待扫描的行程文件数组
            //自上而下依次显示里面的信息
            String[] searchFilePathArray = new String[] {
                fileFullPath,
                String.format("%s\\d四季要记\\all.txt", taskRemindBasePath),
                String.format("%s\\d四季要记\\job.txt", taskRemindBasePath),
            };
            
            //扫面上述的几个文件，并解析出内容
            for (String filePath : searchFilePathArray)
            {
                //行程提醒键值对解析
                Map<String, List<String>> tasks = classifyByConfigFile(new File(filePath));
                //根据时间去提取要展示的数据
                handleTaskAlert(tasks, calendarDesc, toShowMsgs);
            }
            //提取完毕了
            
            //如果最终汇总结果有数据的话
            if (toShowMsgs.size() > 0)
            {
                //拼接今日额外提醒信息
                String todayDateStr = DateUtils.getNowDateNoConnectorStr();
                if (MainFrame.extraRemindThings.containsKey(todayDateStr))
                {
                    //今日有额外提醒事项，一般是呵欠提醒等等
                    toShowMsgs.addAll(MainFrame.extraRemindThings.get(todayDateStr));
                }
                
                //如果有真正的消息提醒存在的话
                //那么就将我的真言加入其中
                //人生箴言在此！
                
                //拼接人生箴言的编号以及内容
                //只有当箴言开关打开的时候，才显示箴言；否则要关闭箴言
                //这个按钮，用于不方便的时候临时关闭箴言。就类似于闹钟贪睡模式
                if (WinMgr.win(MainFrame.class).realWordsSwitchBtn.isSwitchStatus())
                {
                    ImmutableTriple<String, String, String> diaryFragment = DiaryKit.getRandomDiaryFragment();
                    if (diaryFragment != null)
                    {
                        toShowMsgs.add(">>>>>>>>>>>>>" + diaryFragment.getMiddle() + ">>>>>>>>>>>>>");
                        toShowMsgs.add(diaryFragment.getRight());
                    }
                }
                
                //只有当提醒总开关打开的状态下，才进行消息提醒。默认就是打开的，这个模式可用于短期的勿扰模式
                if (WinMgr.win(MainFrame.class).popupMsgSwitchBtn.isSwitchStatus())
                {
                    //mapper操作，智能处理指令自动映射
                    toShowMsgs = toShowMsgs.stream().map(t -> SmartContentProcessor.transferAndHandleContent(t)).collect(Collectors.toList());
                    //最终将消息表合并展示
                    String combinedMsg = StringUtils.join(toShowMsgs, "\n");
                    //在气泡中弹出提示信息
                    WinMgr.showTrayMessage(combinedMsg);
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * 任务处理器<br>
     * 匹配任务时间，以进行正确而合理的处理方法
     * @author Administrator
     * @param tasks
     * @param calendarDesc
     */
    private void handleTaskAlert(Map<String, List<String>> tasks, CalendarDesc calendarDesc, List<String> toShowMsgs)
    {
        for (String key : tasks.keySet())
        {
            //分别处理，对于未能匹配到处理方案的，要给出提示
            List<String> contents = tasks.get(key);
            
            if (Lists.isEmpty(contents))
            {
                //如果提示信息为空，那么就完全没有判断or加入提示列表的必要了
                continue;
            }
            
            switch (key)
            {
                //每日任务提醒
                case "周日":
                case "Sunday":
                case "周一":
                case "Monday":
                case "周二":
                case "Tuesday":
                case "周三":
                case "Wednesday":
                case "周四":
                case "Thursday":
                case "周五":
                case "Friday":
                case "周六":
                case "Saturday":
                    if (matchWeek(calendarDesc, key))
                    {
                        if (matchDailyAlertTime(calendarDesc))
                        {
                            toShowMsgs.addAll(contents);
                        }
                    }
                    break;
                case "全部":
                case "每天":
                case "TODO":
                case "今天":
                case "todo":
                case "Everyday":
                    if (matchDailyAlertTime(calendarDesc))
                    {
                        toShowMsgs.addAll(contents);
                    }
                    break;
                case "今年":
                case "明年":
                case "后年":
                case "大后年":
                case "周年":
                    //今年、明年、后年等周年目标，在每月1、10、20号的9:30需要提醒一次！    
                    if (matchYearAlertTime(calendarDesc))
                    {
                        toShowMsgs.addAll(contents);
                    }
                    break;
                case "早上":
                case "中午":
                case "午间":
                case "下午":
                case "晚上":
                    if (matchDaytimeStage(calendarDesc, key))
                    {
                        toShowMsgs.addAll(contents);
                    }
                    break;
                case "1月":
                case "2月":
                case "3月":
                case "4月":
                case "5月":
                case "6月":
                case "7月":
                case "8月":
                case "9月":
                case "10月":
                case "11月":
                case "12月":
                    if (matchMonthAlertTime(calendarDesc, key))
                    {
                        toShowMsgs.addAll(contents);
                    }
                    break;
                default:
                    if (key.startsWith("今年") || key.startsWith("明年") || key.startsWith("后年") || key.startsWith("大后年") || matchYear(key))
                    {
                        if (matchYearAlertTime(calendarDesc))
                        {
                            toShowMsgs.addAll(contents);
                        }
                    }
                    else if (key.startsWith("以后再说") || key.startsWith("未来规划"))
                    {
                        //这种是无法确定日期的计划，那么只能继续存着，等它转换成有具体日期规划的任务之后再作提醒吧
                    }
                    //9月9日
                    //9月10日
                    else if (matchMonthDay(key))
                    {
                        MutablePair<Integer, Integer> monthDay = getMonthDay(key);
                        //指定日期的目标，提前一个月、一星期、倒数3天每天提醒一次
                        MutablePair<Boolean, String> result = matchMonthDayAlertTime(calendarDesc, monthDay);
                        if (result.getLeft())
                        {
                            for (String content : contents)
                            {
                                toShowMsgs.add((StringUtils.isEmpty(result.getRight()) ? "" : String.format("【%s】", result.getRight())) + content);
                            }
                        }
                    }
                    //10:00-11:00
                    //任务开始的时候提醒一次，任务结束的时候提醒一次，倒数半小时提醒一次，倒数10分钟的时候提醒一次
                    else if (matchTimeRangeFlat(key))
                    {
                        List<Integer> timeRange = getTimeRangeFlat(key);//[10, 0, 11, 0]
                        MutablePair<Boolean, String> result = matchTimeRangeAlertTime(calendarDesc, timeRange);
                        if (result.getLeft())
                        {
                            for (String content : contents)
                            {
                                toShowMsgs.add((StringUtils.isEmpty(result.getRight()) ? "" : String.format("【%s】", result.getRight())) + content);
                                //【5分钟后开始】威力小睡
                                //【         right  】 content
                            }
                        }
                    }
                    //10:00~11:00
                    //任务开始的时候提醒一次，任务结束的时候提醒一次，倒数半小时提醒一次，倒数10分钟的时候提醒一次
                    else if (matchTimeRangeWave(key))
                    {
                        List<Integer> timeRange = getTimeRangeWave(key); //[10, 0, 11, 0]
                        MutablePair<Boolean, String> result = matchTimeRangeAlertTime(calendarDesc, timeRange);
                        if (result.getLeft())
                        {
                            for (String content : contents)
                            {
                                toShowMsgs.add((StringUtils.isEmpty(result.getRight()) ? "" : String.format("【%s】", result.getRight())) + content);
                            }
                        }
                    }
                    //10:30   singleTime
                    //10:30,14:00    multipleTime
                    else if (matchSingleTime(key) || matchMultipleTime(key))
                    {
                        List<Integer> times = getTimes(key);
                        boolean match = matchAnySingleTime(calendarDesc, times);
                        if (match)
                        {
                            toShowMsgs.addAll(contents);
                        }
                    }
                    
                    //对于风险的抑制，就是对幸福的把握！
                    //风险控制【长远战略】
                    //风险控制【*】
                    
                    //key     风险控制【特急】
                    //msg：        【特急！！】 看github
                    
                    //                    风险控制【长远战略】：
                    //                    早中晚各一次
                    //
                    //                    风险控制【一般】：
                    //                    1小时一次
                    //
                    //                    风险控制【紧急】：
                    //                    1小时一次
                    //
                    //                    风险控制【特急】：
                    //                    半小时一次        看github
                    
                    else if (matchRiskManagement(key))
                    {
                        String urgentLevel = getRiskManagementUrgentLevel(key);//特急
                        //12:30   特急        看github
                        MutablePair<Boolean, String> result = matchRiskManagementAlertTime(calendarDesc, urgentLevel);
                        if (result.getLeft())
                        {
                            for (String content : contents)
                            {
                                toShowMsgs.add((StringUtils.isEmpty(result.getRight()) ? "" : String.format("【%s！！】", result.getRight())) + content);
                                //【特急！！】看github
                            }
                        }
                    }
                    //                    风险控制【长远战略】：
                    //
                    //                    风险控制【一般】：
                    //
                    //                    风险控制【紧急】：
                    //
                    //                    风险控制【特急】：
                    
                    //已过期的任务，没必要删除，因为留下存根也很重要
                    else
                    {
                        Logs.w("未能处理" + key);
                    }
                    break;
            }
        }
    }
    
    /**
     * 是否符合风险管理的提醒时间
     * @author Administrator
     * @param calendarDesc
     * @param urgentLevel  紧急
     * @return
     */
    private MutablePair<Boolean, String> matchRiskManagementAlertTime(CalendarDesc calendarDesc, String urgentLevel)
    {
        //        int hour = calendarDesc.getHourOfDay();//现在：10时 
        //        int minute = calendarDesc.getMinute();//现在：30分
        
        //                    风险控制【长远战略】：
        //                    早中晚各一次
        //
        //                    风险控制【一般】：
        //                    1小时一次
        //
        //                    风险控制【紧急】：
        //                    半小时一次
        //
        //                    风险控制【特急】：
        //                    半小时一次        看github
        switch (urgentLevel)
        {
            case "长远战略":
                if (calendarDesc.matchHourMinutes("09:00", "13:30", "21:00"))
                {
                    return new MutablePair<Boolean, String>(true, urgentLevel);
                }
                break;
            case "一般":
                if (calendarDesc.matchHourMinutesRange("09:00", "23:30", 60))
                {
                    return new MutablePair<Boolean, String>(true, urgentLevel);
                }
                break;
            case "紧急":
            case "特急":
                if (calendarDesc.matchHourMinutesRange("09:00", "23:30", 30))
                {
                    return new MutablePair<Boolean, String>(true, urgentLevel);
                }
                break;
            default:
                break;
        }
        return new MutablePair<Boolean, String>(false, null);
    }
    
    private String getRiskManagementUrgentLevel(String key)
    {
        List<String> ret = new ArrayList<>();
        Pattern pat = Pattern.compile("^风险控制【(\\S+)】$");
        Matcher mat = pat.matcher(key);
        while (mat.find())
        {
            for (int i = 1; i <= mat.groupCount(); i++)
            {
                String find = mat.group(i);
                ret.add(find);
            }
        }
        if (ret.size() == 1)
        {
            return ret.get(0);
        }
        return null;
    }
    
    private boolean matchRiskManagement(String key)
    {
        Pattern pattern = Pattern.compile("^风险控制【\\S+】$");
        Matcher matcher = pattern.matcher(key);
        return matcher.matches();
    }
    
    //    @TestCase
    void test4()
    {
        //      风险控制【长远战略】：
        //      风险控制【一般】：
        //      风险控制【紧急】：
        //      风险控制【特急】：
        System.out.println(matchRiskManagement("风险控制【长远战略】"));
        System.out.println(matchRiskManagement("风险控制【一般】"));
        System.out.println(matchRiskManagement("风险控制【紧急】"));
        System.out.println(matchRiskManagement("风险控制【 】"));
        
        System.out.println(getRiskManagementUrgentLevel("风险控制【长远战略】"));
        System.out.println(getRiskManagementUrgentLevel("风险控制【一般】"));
        System.out.println(getRiskManagementUrgentLevel("风险控制【紧急】"));
        System.out.println(getRiskManagementUrgentLevel("1风险控制【1】"));
        
    }
    
    //    @TestCase
    void test5()
    {
        System.out.println(DateUtils.getCurrentCalendarDesc().matchHourMinutesRange("09:00", "23:30", 9));
        System.out.println(DateUtils.getCurrentCalendarDesc().matchHourMinutesRange("10:00", "10:08", 9));
        System.out.println(DateUtils.getCurrentCalendarDesc().matchHourMinutesRange("11:00", "23:30", 9));
    }
    
    /**
     * 每日任务提醒的时间
     * @author Administrator
     * @param calendarDesc
     * @return
     */
    private boolean matchDailyAlertTime(CalendarDesc calendarDesc)
    {
        //        if (calendarDesc.matchHourMinutes("09:00", "11:30", "13:30", "15:00", "17:45", "20:00", "21:00", "22:00"))
        if (calendarDesc.matchHourMinutesRange("09:00", "23:30", 30))
        //        if (calendarDesc.matchHourMinutesRange("09:00", "23:30", 1))
        {
            return true;
        }
        return false;
    }
    
    /**
     * 匹配当日的时间阶段
     * @author Administrator
     * @param calendarDesc
     * @param key
     * @return
     */
    private boolean matchDaytimeStage(CalendarDesc calendarDesc, String key)
    {
        if ("早上".equals(key))
        {
            //            if (calendarDesc.matchHourMinutes("09:30", "10:30", "11:30"))
            if (calendarDesc.matchHourMinutesRange("09:00", "11:30", 30))
            {
                return true;
            }
        }
        if ("中午".equals(key) || "午间".equals(key))
        {
            if (calendarDesc.matchHourMinutes("12:10", "12:30", "12:50"))
            {
                return true;
            }
        }
        else if ("下午".equals(key))
        {
            //            if (calendarDesc.matchHourMinutes("13:30", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30", "17:00", "17:30", "18:00"))
            if (calendarDesc.matchHourMinutesRange("13:30", "17:30", 30))
            {
                return true;
            }
        }
        else if ("晚上".equals(key))
        {
            //            if (calendarDesc.matchHourMinutes("18:30", "19:30", "20:30", "21:30", "22:30", "23:30"))
            if (calendarDesc.matchHourMinutesRange("18:00", "23:30", 30))
            {
                return true;
            }
        }
        return false;
    }
    
    private boolean matchWeek(CalendarDesc calendarDesc, String key)
    {
        int week = calendarDesc.getWeek();
        switch (key)
        {
            case "周日":
            case "Sunday":
                if (week == Calendar.SUNDAY)
                {
                    return true;
                }
                break;
            case "周一":
            case "Monday":
                if (week == Calendar.MONDAY)
                {
                    return true;
                }
                break;
            case "周二":
            case "Tuesday":
                if (week == Calendar.TUESDAY)
                {
                    return true;
                }
                break;
            case "周三":
            case "Wednesday":
                if (week == Calendar.WEDNESDAY)
                {
                    return true;
                }
                break;
            case "周四":
            case "Thursday":
                if (week == Calendar.THURSDAY)
                {
                    return true;
                }
                break;
            case "周五":
            case "Friday":
                if (week == Calendar.FRIDAY)
                {
                    return true;
                }
                break;
            case "周六":
            case "Saturday":
                if (week == Calendar.SATURDAY)
                {
                    return true;
                }
                break;
            default:
                break;
        }
        return false;
    }
    
    private boolean matchAnySingleTime(CalendarDesc calendarDesc, List<Integer> times)
    {
        int hour = calendarDesc.getHourOfDay();//现在：10时 
        int minute = calendarDesc.getMinute();//现在： 05分
        for (int i = 0; i < times.size(); i += 2)
        {
            if (times.get(i) == hour && times.get(i + 1) == minute)
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 任务开始的时候提醒一次，任务结束的时候提醒一次，中场提醒一次，倒数10分钟的时候提醒一次
     * @author Administrator
     * @param calendarDesc  当前时刻
     * @param timeRange   [10, 0, 11, 0]
     * @return
     */
    private MutablePair<Boolean, String> matchTimeRangeAlertTime(CalendarDesc calendarDesc, List<Integer> timeRange)
    {
        int hour = calendarDesc.getHourOfDay();//现在：10时 
        int minute = calendarDesc.getMinute();//现在： 05分
        //        int month = calendarDesc.getMonth();//现在： 8月
        //        int dayOfMonth = calendarDesc.getDayOfMonth();//现在： 20日
        
        int planBeginHour = timeRange.get(0);//10
        int planBeginMinute = timeRange.get(1);//00
        int planEndHour = timeRange.get(2);//11
        int planEndMinute = timeRange.get(3);//00
        
        Calendar calendar = calendarDesc.getCalendar();//当前的年月日
        Date now = calendar.getTime();//当前的时间
        
        //计划开始时间
        Calendar planBegin = Calendar.getInstance();
        planBegin.setTime(now);
        planBegin.set(Calendar.HOUR_OF_DAY, planBeginHour);
        planBegin.set(Calendar.MINUTE, planBeginMinute);
        
        //计划结束时间
        Calendar planEnd = Calendar.getInstance();
        planEnd.setTime(now);
        planEnd.set(Calendar.HOUR_OF_DAY, planEndHour);
        planEnd.set(Calendar.MINUTE, planEndMinute);
        
        //开始时刻
        if (hour == planBeginHour && minute == planBeginMinute)
        {
            long diffMinutes = DateUtils.timeDiffAbs(planBegin, planEnd, TimeUnit.MINUTES);
            return new MutablePair<Boolean, String>(true, String.format("任务开始 [将耗时%s分钟]", diffMinutes));
        }
        //结束时刻
        if (hour == planEndHour && minute == planEndMinute)
        {
            return new MutablePair<Boolean, String>(true, "任务结束");
        }
        //开场前5分钟
        Calendar planCalendar = Calendar.getInstance();
        planCalendar.setTime(now);
        //将其调节到计划的结束时间
        planCalendar.set(Calendar.HOUR_OF_DAY, planBeginHour);
        planCalendar.set(Calendar.MINUTE, planBeginMinute);
        planCalendar.add(Calendar.MINUTE, -5);
        if (planCalendar.get(Calendar.HOUR_OF_DAY) == hour && planCalendar.get(Calendar.MINUTE) == minute)
        {
            return new MutablePair<Boolean, String>(true, "5分钟后开始");
        }
        
        //倒数10分钟的时候
        planCalendar.setTime(now);
        //将其调节到计划的结束时间
        planCalendar.set(Calendar.HOUR_OF_DAY, planEndHour);
        planCalendar.set(Calendar.MINUTE, planEndMinute);
        //往回调10分钟
        planCalendar.add(Calendar.MINUTE, -10);
        //但是如果10分钟前任务还未开始，那么就不能提醒
        if (planCalendar.get(Calendar.HOUR_OF_DAY) == hour && planCalendar.get(Calendar.MINUTE) == minute && planCalendar.after(planBegin))
        {
            return new MutablePair<Boolean, String>(true, "10分钟后结束");
        }
        
        //倒数30分钟的时候
        planCalendar.setTime(now);
        //将其调节到计划的结束时间
        planCalendar.set(Calendar.HOUR_OF_DAY, planEndHour);
        planCalendar.set(Calendar.MINUTE, planEndMinute);
        //往回调
        planCalendar.add(Calendar.MINUTE, -30);
        if (planCalendar.get(Calendar.HOUR_OF_DAY) == hour && planCalendar.get(Calendar.MINUTE) == minute && planCalendar.after(planBegin))
        {
            return new MutablePair<Boolean, String>(true, "半小时后结束");
        }
        
        //中场提示
        //如果开始与结束的间隔超过60分钟，那么中场提示就是有必要的
        long delta = DateUtils.timeDiffAbs(planEnd, planBegin, TimeUnit.MINUTES);
        if (delta > 60)
        {
            //有必要中场提示
            long halfMinutes = delta / 2;
            planCalendar.setTime(now);
            //将其调节到计划的结束时间
            planCalendar.set(Calendar.HOUR_OF_DAY, planEndHour);
            planCalendar.set(Calendar.MINUTE, planEndMinute);
            //往回调
            planCalendar.add(Calendar.MINUTE, (int)(-1 * halfMinutes));
            if (planCalendar.get(Calendar.HOUR_OF_DAY) == hour && planCalendar.get(Calendar.MINUTE) == minute)
            {
                return new MutablePair<Boolean, String>(true, "长时任务—中场提示");
            }
        }
        return new MutablePair<Boolean, String>(false, null);
    }
    
    /**
     * 10:00-11:00
     * @author Administrator
     * @param key
     * @return
     */
    private boolean matchTimeRangeFlat(String key)
    {
        Pattern pattern = Pattern.compile("^\\d{2}:\\d{2}-\\d{2}:\\d{2}$");
        Matcher matcher = pattern.matcher(key);
        return matcher.matches();
    }
    
    /**
     * 10:30
     * @author Administrator
     * @param key
     * @return
     */
    private boolean matchSingleTime(String key)
    {
        Pattern pattern = Pattern.compile("^\\d{2}:\\d{2}$");
        Matcher matcher = pattern.matcher(key);
        return matcher.matches();
    }
    
    /**
     * 10:30,14:00  multipleTime
     * @author Administrator
     * @param key
     * @return
     */
    private boolean matchMultipleTime(String key)
    {
        if (StringUtils.isNotEmpty(key) && key.indexOf(",") != -1)
        {
            String[] splits = key.split(",");
            boolean[] bs = new boolean[splits.length];
            for (int i = 0; i < splits.length; i++)
            {
                bs[i] = matchSingleTime(splits[i]);
            }
            if (bs != null && bs.length >= 2)
            {
                return BooleanUtils.and(bs);
            }
        }
        return false;
    }
    
    private List<Integer> getTimes(String key)
    {
        List<Integer> ret = new ArrayList<>();
        if (matchSingleTime(key))
        {
            ret = getSingleTime(key);
        }
        else
        {
            String[] splits = key.split(",");
            for (int i = 0; i < splits.length; i++)
            {
                List<Integer> temp = getSingleTime(splits[i]);
                if (temp != null)
                {
                    ret.addAll(temp);
                }
            }
        }
        return ret;
    }
    
    //    @TestCase
    void test3()
    {
        System.out.println(matchSingleTime("10:00"));
        System.out.println(matchSingleTime("r10:00"));
        System.out.println(matchSingleTime("110:00"));
        System.out.println(matchMultipleTime("10:00,13:15"));
        System.out.println(matchMultipleTime("10:00,13:15,r"));
        System.out.println(matchMultipleTime("10:00,13:15,r"));
        
        System.out.println(getTimes("10:00"));
        System.out.println(getTimes("10:00,11:00"));
        System.out.println(getTimes("y10:00,11:00,10:00,11:00,10:00,11:00"));
    }
    
    private List<Integer> getSingleTime(String key)
    {
        List<Integer> ret = new ArrayList<>();
        Pattern pat = Pattern.compile("^(\\d{2}):(\\d{2})$");
        Matcher mat = pat.matcher(key);
        while (mat.find())
        {
            for (int i = 1; i <= mat.groupCount(); i++)
            {
                String find = mat.group(i);
                if (Validates.isInteger(find))
                {
                    ret.add(Integer.valueOf(find));
                }
                else
                {
                    break;
                }
            }
        }
        if (ret.size() == 2)
        {
            if (ret.get(0) <= 23 && ret.get(1) <= 59)
            {
                return ret;
            }
        }
        return null;
    }
    
    /**
     * 获取开始时间与结束时间
     * @author Administrator
     * @param key
     * @return
     */
    private List<Integer> getTimeRangeFlat(String key)
    {
        List<Integer> ret = new ArrayList<>();
        Pattern pat = Pattern.compile("^(\\d{2}):(\\d{2})-(\\d{2}):(\\d{2})$");
        Matcher mat = pat.matcher(key);
        while (mat.find())
        {
            for (int i = 1; i <= mat.groupCount(); i++)
            {
                String find = mat.group(i);
                if (Validates.isInteger(find))
                {
                    ret.add(Integer.valueOf(find));
                }
                else
                {
                    break;
                }
            }
        }
        if (ret.size() == 4)
        {
            if (ret.get(0) <= 23 && ret.get(1) <= 59 && ret.get(2) <= 23 && ret.get(3) <= 59)
            {
                return ret;
            }
        }
        return null;
    }
    
    /**
     * 10:00~11:00
     * @author Administrator
     * @param key
     * @return
     */
    private boolean matchTimeRangeWave(String key)
    {
        Pattern pattern = Pattern.compile("^\\d{2}:\\d{2}~\\d{2}:\\d{2}$");
        Matcher matcher = pattern.matcher(key);
        return matcher.matches();
    }
    
    /**
     * 获取开始时间与结束时间
     * @author Administrator
     * @param key
     * @return
     */
    private List<Integer> getTimeRangeWave(String key)
    {
        List<Integer> ret = new ArrayList<>();
        Pattern pat = Pattern.compile("^(\\d{2}):(\\d{2})~(\\d{2}):(\\d{2})$");
        Matcher mat = pat.matcher(key);
        while (mat.find())
        {
            for (int i = 1; i <= mat.groupCount(); i++)
            {
                String find = mat.group(i);
                if (Validates.isInteger(find))
                {
                    ret.add(Integer.valueOf(find));
                }
                else
                {
                    break;
                }
            }
        }
        if (ret.size() == 4)
        {
            if (ret.get(0) <= 23 && ret.get(1) <= 59 && ret.get(2) <= 23 && ret.get(3) <= 59)
            {
                return ret;
            }
        }
        return null;
    }
    
    //    @TestCase
    void test2()
    {
        System.out.println(matchTimeRangeFlat("10:00-11:00"));
        System.out.println(matchTimeRangeFlat("10:110-11:00"));
        System.out.println(matchTimeRangeFlat("r10:00-11:00"));
        System.out.println(matchTimeRangeFlat("10:00~11:00"));
        
        System.out.println(getTimeRangeFlat("10:00-11:00"));
        System.out.println(getTimeRangeFlat("10:110-11:00"));
        System.out.println(getTimeRangeFlat("10:00-11:00a"));
        System.out.println(getTimeRangeFlat("10:00~11:00"));
        
        System.out.println();
        //        System.out.println(matchTimeRangeWave("10:00-11:00"));
        //        System.out.println(matchTimeRangeWave("10:110-11:00"));
        //        System.out.println(matchTimeRangeWave("110:00~11:00"));
        //        System.out.println(matchTimeRangeWave("10:00~11:00"));
        
        System.out.println(getTimeRangeWave("10:00-11:00"));
        System.out.println(getTimeRangeWave("10:110-11:00"));
        System.out.println(getTimeRangeWave("110:00~11:00"));
        System.out.println(getTimeRangeWave("10:00~11:00"));
        
        //        System.out.println(matchMonthDay("5月6日"));
        //        System.out.println(matchMonthDay("9月10日"));
        //        System.out.println(matchMonthDay("9月1日"));
        //        System.out.println(matchMonthDay("9月10日"));
        //        System.out.println(getMonthDay("5月6日"));
        //        System.out.println(getMonthDay("9月10日"));
        //        System.out.println(getMonthDay("15月16日"));
        //        System.out.println(getMonthDay("25月09日"));
    }
    
    /**
     * 2017年
     * @author nan.li
     * @param key
     * @return
     */
    private boolean matchYear(String key)
    {
        Pattern pattern = Pattern.compile("\\d{4}年");
        Matcher matcher = pattern.matcher(key);
        return matcher.matches();
    }
    
    /**
     * 例如5月6日、 9月10日等等
     * @author nan.li
     * @param key
     * @return
     */
    private boolean matchMonthDay(String key)
    {
        Pattern pattern = Pattern.compile("\\d{1,2}月\\d{1,2}日");
        Matcher matcher = pattern.matcher(key);
        return matcher.matches();
    }
    
    /**
     * 获取月日的数据，并加上了数据校验
     * @author nan.li
     * @param key
     * @return
     */
    private MutablePair<Integer, Integer> getMonthDay(String key)
    {
        MutablePair<Integer, Integer> retPair = new MutablePair<>();
        List<Integer> ret = new ArrayList<>();
        Pattern pat = Pattern.compile("(\\d{1,2})月(\\d{1,2})日");
        Matcher mat = pat.matcher(key);
        while (mat.find())
        {
            for (int i = 1; i <= mat.groupCount(); i++)
            {
                String find = mat.group(i);
                if (Validates.isInteger(find))
                {
                    ret.add(Integer.valueOf(find));
                }
                else
                {
                    break;
                }
            }
        }
        if (ret.size() == 2)
        {
            if (ret.get(0) <= 12 && ret.get(1) <= 31)
            {
                retPair.setLeft(ret.get(0));
                retPair.setRight(ret.get(1));
                return retPair;
            }
        }
        return null;
    }
    
    //        @TestCase
    void test1()
    {
        //        System.out.println(matchMonthDay("5月6日"));
        //        System.out.println(matchMonthDay("9月10日"));
        //        System.out.println(matchMonthDay("9月1日"));
        //        System.out.println(matchMonthDay("9月10日"));
        System.out.println(getMonthDay("5月6日"));
        System.out.println(getMonthDay("9月10日"));
        System.out.println(getMonthDay("15月16日"));
        System.out.println(getMonthDay("25月09日"));
    }
    
    public static void main(String[] args)
    {
        TF.l(SmartTaskRemindJob.class);
    }
    
    /**
     * 能否匹配年月的提醒时间
     * @author nan.li
     * @param calendarDesc
     * @param monthDay
     * @return
     */
    private MutablePair<Boolean, String> matchMonthDayAlertTime(CalendarDesc calendarDesc, MutablePair<Integer, Integer> monthDay)
    {
        //指定日期的目标，提前一个月、一星期、倒数3天每天提醒一次
        int month = calendarDesc.getMonth();//现在： 8月
        int dayOfMonth = calendarDesc.getDayOfMonth();//现在： 19日
        
        int planMonth = monthDay.getLeft();//计划的月     8月
        int planDayOfMonth = monthDay.getRight();//计划的日   26日
        
        //将计划日期提前一个月
        Calendar calendar = calendarDesc.getCalendar();//当前的年月日
        Date now = calendar.getTime();//当前的时间
        
        Calendar planCalendar = Calendar.getInstance();
        planCalendar.setTime(now);
        //将其调节到计划的月日
        planCalendar.set(Calendar.MONTH, planMonth - 1);
        planCalendar.set(Calendar.DAY_OF_MONTH, planDayOfMonth);
        //计算计划月日的上个月
        planCalendar.add(Calendar.MONTH, -1);
        if (planCalendar.get(Calendar.MONTH) == month && planCalendar.get(Calendar.DAY_OF_MONTH) == dayOfMonth)
        {
            if (calendarDesc.matchHourMinutes("09:00", "13:30"))
            {
                return new MutablePair<Boolean, String>(true, "一个月后");
            }
        }
        
        //提前一星期
        planCalendar.setTime(now);
        //将其调节到计划的月日
        planCalendar.set(Calendar.MONTH, planMonth - 1);
        planCalendar.set(Calendar.DAY_OF_MONTH, planDayOfMonth);
        planCalendar.add(Calendar.WEEK_OF_YEAR, -1);
        if (planCalendar.get(Calendar.MONTH) == month && planCalendar.get(Calendar.DAY_OF_MONTH) == dayOfMonth)
        {
            if (calendarDesc.matchHourMinutes("09:00", "13:30"))
            {
                return new MutablePair<Boolean, String>(true, "一周后");
            }
        }
        //倒数第三天
        planCalendar.setTime(now);
        //将其调节到计划的月日
        planCalendar.set(Calendar.MONTH, planMonth - 1);
        planCalendar.set(Calendar.DAY_OF_MONTH, planDayOfMonth);
        planCalendar.add(Calendar.DAY_OF_YEAR, -3);
        if (planCalendar.get(Calendar.MONTH) == month && planCalendar.get(Calendar.DAY_OF_MONTH) == dayOfMonth)
        {
            if (calendarDesc.matchHourMinutes("09:00", "13:30"))
            {
                return new MutablePair<Boolean, String>(true, "三天后");
            }
        }
        
        //倒数第二天
        planCalendar.setTime(now);
        //将其调节到计划的月日
        planCalendar.set(Calendar.MONTH, planMonth - 1);
        planCalendar.set(Calendar.DAY_OF_MONTH, planDayOfMonth);
        planCalendar.add(Calendar.DAY_OF_YEAR, -2);
        if (planCalendar.get(Calendar.MONTH) == month && planCalendar.get(Calendar.DAY_OF_MONTH) == dayOfMonth)
        {
            if (calendarDesc.matchHourMinutes("09:00", "13:30"))
            {
                return new MutablePair<Boolean, String>(true, "后天");
            }
        }
        
        //倒数第一天
        planCalendar.setTime(now);
        //将其调节到计划的月日
        planCalendar.set(Calendar.MONTH, planMonth - 1);
        planCalendar.set(Calendar.DAY_OF_MONTH, planDayOfMonth);
        planCalendar.add(Calendar.DAY_OF_YEAR, -1);
        if (planCalendar.get(Calendar.MONTH) == month && planCalendar.get(Calendar.DAY_OF_MONTH) == dayOfMonth)
        {
            if (calendarDesc.matchHourMinutes("09:00", "13:30"))
            {
                return new MutablePair<Boolean, String>(true, "明天");
            }
        }
        
        //当天
        planCalendar.setTime(now);
        //将其调节到计划的月日
        planCalendar.set(Calendar.MONTH, planMonth - 1);
        planCalendar.set(Calendar.DAY_OF_MONTH, planDayOfMonth);
        if (planCalendar.get(Calendar.MONTH) == month && planCalendar.get(Calendar.DAY_OF_MONTH) == dayOfMonth)
        {
            if (calendarDesc.matchHourMinutes("09:00", "13:30"))
            {
                return new MutablePair<Boolean, String>(true, "");
            }
        }
        return new MutablePair<Boolean, String>(false, null);
    }
    
    /**
     * 明年、周年的目标
     * @author Administrator
     * @param calendarDesc
     * @return
     */
    private boolean matchYearAlertTime(CalendarDesc calendarDesc)
    {
        //明年、周年目标，每月1、10、20号的9:30需要提醒一次！    
        int dayOfMonth = calendarDesc.getDayOfMonth();
        if (dayOfMonth == 1 || dayOfMonth == 10 || dayOfMonth == 20)
        {
            //            if (calendarDesc.matchHourMinutes("09:00", "13:30"))
            if (calendarDesc.matchHourMinutesRange("09:00", "23:30", 30))
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 月份是否相符
     * @author nan.li
     * @param calendarDesc
     * @param paramMonthStr
     * @return
     */
    private boolean matchMonthAlertTime(CalendarDesc calendarDesc, String paramMonthStr)
    {
        int currentMonth = calendarDesc.getMonth() + 1;
        int paramMonth = Integer.valueOf(paramMonthStr.substring(0, paramMonthStr.length() - 1));
        if (currentMonth == paramMonth)
        {
            //            if (calendarDesc.matchHourMinutes("09:00", "13:30"))
            if (calendarDesc.matchHourMinutesRange("09:00", "23:30", 30))
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 根据配置文件进行分类任务类型
     * @author Administrator
     * @param targetTaskConfigFile
     * @return
     * @throws IOException
     */
    private Map<String, List<String>> classifyByConfigFile(File targetTaskConfigFile)
        throws IOException
    {
        Map<String, List<String>> retMap = new LinkedHashMap<>();
        List<String> lines = FileUtils.readLines(targetTaskConfigFile, CharsetKit.getFileCharset(targetTaskConfigFile));
        if (lines != null && lines.size() > 0)
        {
            //读取的所有的文件行数据
            boolean shouldRead = false;//是否应该开始读取
            String key = null;
            List<String> values = new ArrayList<>();
            for (int i = 0; i < lines.size(); i++)
            {
                String line = lines.get(i);
                //只有非空的才有价值去研究
                if (StringUtils.isNotBlank(line))
                {
                    //脱水处理
                    line = line.trim();
                    
                    if (line.startsWith(TASKS_START_FLAG))
                    {
                        //可以开始了
                        shouldRead = true;
                        continue;
                    }
                    if (shouldRead)
                    {
                        //应该去读取的
                        //中英文冒号要同时兼容的
                        if (line.endsWith(":") || line.endsWith("："))
                        {
                            //标题潜质的   
                            if (StringUtils.isNotEmpty(key))
                            {
                                //准备开启新的了
                                retMap.put(key, values);
                                //然后将cache重置
                                key = null;
                                values = new ArrayList<>();
                            }
                            //最后为其赋值
                            key = line.substring(0, line.length() - 1);//有可能是空字符串，那么此处有必要修复为“未命名”
                            if (StringUtils.isEmpty(key))
                            {
                                key = "未命名";
                            }
                        }
                        else
                        {
                            //内容潜质的
                            if (line.startsWith("#"))
                            {
                                //注释掉了，被抛弃的，不会被处理了
                                continue;
                            }
                            else if (line.startsWith(TASKS_FINISH_FLAG))
                            {
                                //结束标记的
                                //将最后一组数据放进去
                                retMap.put(key, values);
                                //然后将cache重置
                                key = null;
                                values = new ArrayList<>();
                                break;//最后结束整个循环
                            }
                            else
                            {
                                //未被注释的，会被合理处理
                                //Logs.d("line:" + line);
                                values.add(line);
                            }
                        }
                    }
                }
            }
        }
        return retMap;
    }
}
