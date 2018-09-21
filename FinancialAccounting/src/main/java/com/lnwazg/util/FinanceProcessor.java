package com.lnwazg.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;

import com.lnwazg.bean.AccountLine;
import com.lnwazg.kit.charset.CharsetKit;
import com.lnwazg.kit.date.DateUtils;
import com.lnwazg.kit.list.Lists;
import com.lnwazg.kit.log.Logs;
import com.lnwazg.kit.validate.Validates;

/**
 * 财务处理器
 * @author nan.li
 * @version 2017年4月12日
 */
public class FinanceProcessor
{
    public static Map<String, Integer> operatorSummap = new HashMap<>();
    
    /**
     * 处理财务并生成总结报表
     * @author nan.li
     * @param file
     */
    public static void handle(File file)
    {
        operatorSummap = new HashMap<>();
        List<AccountLine> list = readToAccountLines(file);
        if (Lists.isNotEmpty(list))
        {
            Logs.i("开始财务统计...");
            //根据操作人进行分组统计
            for (AccountLine accountLine : list)
            {
                String operator = accountLine.getOperator();
                int account = accountLine.getAmount();
                if (operatorSummap.get(operator) == null)
                {
                    operatorSummap.put(operator, account);
                }
                else
                {
                    operatorSummap.put(operator, operatorSummap.get(operator) + account);
                }
            }
            int sum = 0;
            for (String key : operatorSummap.keySet())
            {
                sum += operatorSummap.get(key);
            }
            if (!operatorSummap.isEmpty())
            {
                try
                {
                    Logs.i("开始生成汇总文件...");
                    StringBuilder sBuilder = new StringBuilder();
                    String originalContent = FileUtils.readFileToString(file, CharsetKit.getFileCharset(file));
                    sBuilder.append(originalContent).append("\r\n");
                    sBuilder.append("----------------------------------\r\n");
                    sBuilder.append("操作人").append("\t").append("金额汇总").append("\r\n");
                    sBuilder.append("----------------------------------\r\n");
                    for (String key : operatorSummap.keySet())
                    {
                        sBuilder.append(key).append("\t").append(operatorSummap.get(key)).append("\r\n");
                    }
                    sBuilder.append("----------------------------------\r\n");
                    sBuilder.append("合计").append("\t").append(sum).append("\r\n");
                    String prefix = FilenameUtils.getBaseName(file.getName());
                    String extension = FilenameUtils.getExtension(file.getName());
                    
                    FileUtils.writeStringToFile(new File(file.getParentFile(), String.format("%s_统计结果.%s", prefix, extension)),
                        sBuilder.toString(),
                        CharEncoding.UTF_8);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * 将文件解析成财务表数据
     * @author nan.li
     * @param file
     * @return
     */
    private static List<AccountLine> readToAccountLines(File file)
    {
        try
        {
            List<String> lines = FileUtils.readLines(file, CharsetKit.getFileCharset(file));
            List<AccountLine> ret = new ArrayList<>();
            for (String line : lines)
            {
                if (StringUtils.isNotEmpty(line))
                {
                    String[] fields = StringUtils.splitByWholeSeparator(line, null);
                    if (ArrayUtils.isNotEmpty(fields) && fields.length >= 3)
                    {
                        if (!DateUtils.isDateStr(fields[0]))
                        {
                            continue;
                        }
                        if (!Validates.isInteger(fields[1]))
                        {
                            continue;
                        }
                        AccountLine accountLine = new AccountLine();
                        accountLine.setCreateTime(fields[0]);
                        accountLine.setAmount(Integer.valueOf(fields[1]));
                        accountLine.setOperator(fields[2]);
                        if (fields.length >= 4)
                        {
                            accountLine.setContent(fields[3]);
                        }
                        if (fields.length >= 5)
                        {
                            accountLine.setRemark(fields[4]);
                        }
                        ret.add(accountLine);
                    }
                }
            }
            return ret;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
}
