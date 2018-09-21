package com.lnwazg.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import com.lnwazg.kit.charset.CharsetKit;

/**
 * 金句回顾学习包<br>
 * 这个小小的包，竟然是一个高效的数据挖掘机！真是大大出乎了我当初的意料！
 * @author nan.li
 * @version 2017年2月28日
 */
public class DiaryKit
{
    /**
     * 随机获取一篇日志的片段
     * @author nan.li
     * @return  文件名  行号  内容<br>
     * 例如：系统说明_20170701.txt      20170759        今天我以这个为荣！
     */
    public static ImmutableTriple<String, String, String> getRandomDiaryFragment()
    {
        /**
         * 1.根据现在时间，随机挑选一个日志文件
         * 2.获取该文件的行数，随机获取一行
         * 3.找到该行最近的一篇片段，返回之
         */
        //1. 随机选取文件
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        
        List<ImmutablePair<Integer, Integer>> yearMonthList = new ArrayList<>();
        for (int i = 2010; i <= currentYear; i++)
        {
            int jMax = 12;
            if (i == currentYear)
            {
                jMax = currentMonth;
            }
            for (int j = 1; j <= jMax; j++)
            {
                yearMonthList.add(new ImmutablePair<Integer, Integer>(i, j));
            }
        }
        ImmutablePair<Integer, Integer> selectedYearMonth = yearMonthList.get(RandomUtils.nextInt(yearMonthList.size()));
        
        //basePathDir    O:\2012\d心理碎片trunk
        //motherDir      O:\2012\d心理碎片trunk\d代码碎片汇总_20120101
        //monthDir       O:\2012\d心理碎片trunk\d代码碎片汇总_20120101\2012年9月
        //file           O:\2012\d心理碎片trunk\d代码碎片汇总_20120101\2012年9月\系统说明*
        File dir = new File(String.format("%s/d代码碎片汇总_%d0101/%d年%d月",
            Constant.DIARY_BASE_PATH,
            selectedYearMonth.getLeft(),
            selectedYearMonth.getLeft(),
            selectedYearMonth.getRight()));
            
        if (dir.exists())
        {
            File[] files = dir.listFiles();
            for (File file : files)
            {
                if (!file.isDirectory() && file.getName().startsWith("系统说明"))
                {
                    //                    Logs.d("选取文件: " + file.getPath());
                    //                    String content;
                    try
                    {
                        //2.读取该文件
                        String encoding = CharsetKit.getFileCharset(file);
                        List<String> lines = FileUtils.readLines(file, encoding);
                        
                        //3.片段处理
                        int selectedLine = RandomUtils.nextInt(lines.size());
                        //                        Logs.d("selectedLine line: " + selectedLine);
                        int startLine = 0;
                        int endLine = 0;
                        //当前选的行的内容
                        String selectedLineContent = lines.get(selectedLine);
                        
                        //获取起始行
                        if (matchParagraphStart(selectedLineContent))
                        {
                            startLine = selectedLine;
                        }
                        else
                        {
                            startLine = selectedLine;
                            while (true)
                            {
                                startLine = startLine - 1;
                                if (startLine < 0)
                                {
                                    startLine = 0;
                                    break;
                                }
                                if (matchParagraphStart(lines.get(startLine)))
                                {
                                    break;
                                }
                            }
                        }
                        //                        Logs.d("start line: " + startLine);
                        //获取结束行
                        endLine = selectedLine;
                        while (true)
                        {
                            endLine = endLine + 1;
                            if (endLine > lines.size() - 1)
                            {
                                endLine = lines.size() - 1;
                                break;
                            }
                            if (matchParagraphStart(lines.get(endLine)))
                            {
                                break;
                            }
                        }
                        //                        Logs.d("end line: " + endLine);
                        //trim后输出
                        StringBuilder result = new StringBuilder();
                        //                        result.append("" + file.getName() + "\n");
                        if (startLine < endLine)
                        {
                            for (int i = startLine; i < endLine; i++)
                            {
                                result.append(lines.get(i)).append("\r\n");
                            }
                            //                            return result.toString();
                            return new ImmutableTriple<String, String, String>(file.getName(), getNumber(file.getName(), result.toString()),
                                result.toString().substring(result.indexOf(".") + 1));
                            //                            System.out.println(result.toString());
                        }
                        
                        //                        System.out.println("==================");
                        //                        content = FileUtils.readFileToString(file, encoding);
                        //                        System.out.println(content);
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * 获取编号
     * @author nan.li
     * @param fileName 系统说明_20130401.txt
     * @param paragraph  20.讲一下心理学。
     * @return
     */
    private static String getNumber(String fileName, String paragraph)
    {
        //        Logs.e(paragraph);
        return String.format("%s%s", fileName.substring("系统说明_".length(), "系统说明_201304".length()), paragraph.substring(0, paragraph.indexOf(".")));
    }
    
    /**
     * 判断是否以某个特殊的结构开头<br>
     * 正则表达式真好用！<br>
     * 正则表达式真是强大的神器
     * @author nan.li
     * @param line
     * @return
     */
    public static boolean matchParagraphStart(String line)
    {
        Pattern pattern = Pattern.compile("\\d{1,3}\\.{1}.*");
        Matcher matcher = pattern.matcher(line);
        return matcher.matches();
    }
    
    public static void main(String[] args)
    {
        ImmutableTriple<String, String, String> diary = getRandomDiaryFragment();
        System.out.println(diary.getLeft());
        System.out.println();
        //        System.out.println(diary.getMiddle());
        System.out.println(diary.getMiddle().substring(6) + "." + diary.getRight());
        //        System.out.println(matchParagraphStart("1.抓住事物中最关键的20%,并以此作为自己解决问题的法宝!"));
        //        System.out.println(matchParagraphStart("112."));
        //        System.out.println(matchParagraphStart("112"));
        //        System.out.println(matchParagraphStart("113."));
        //        System.out.println(matchParagraphStart("112123w."));
    }
}
