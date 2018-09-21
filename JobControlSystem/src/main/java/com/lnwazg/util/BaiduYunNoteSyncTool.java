package com.lnwazg.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.lnwazg.bean.NoteBook;
import com.lnwazg.bean.NoteRecord;
import com.lnwazg.kit.charset.CharsetKit;
import com.lnwazg.kit.date.DateUtils;
import com.lnwazg.kit.date.DateUtils.CalendarDesc;
import com.lnwazg.kit.gson.GsonKit;
import com.lnwazg.kit.http.HttpUtils;
import com.lnwazg.kit.list.Lists;
import com.lnwazg.kit.log.Logs;
import com.lnwazg.kit.map.Maps;

/**
 * 从百度云记事本到我的“代码碎片汇总”的整合工具<br>
 * 每次执行自动执行一次整合
 * @author nan.li
 * @version 2018年1月11日
 */
public class BaiduYunNoteSyncTool
{
    /**
     * 数据整合
     * @author nan.li
     */
    public static void process()
    {
        Logs.i("开始百度云笔记数据整合...");
        
        //首先拉取笔记内容
        String url =
            "http://note.baidu.com/api/note?method=select&limit=0-100&t=1515637718725&channel=chunlei&web=1&app_id=250528&bdstoken=417d4df34be9971be08bdbe1f4560d21&logid=MTUxNTYzNzcxODcyNjAuNzA3MTU5MjM4MzYzMjMyNg==&clienttype=0";
        String result = HttpUtils.doPost(url,
            new HashMap<>(),
            Maps.asStrHashMap("Cookie", "BAIDUID=70DA5791665EF397B850693E2C0881E5:FG=1; PSTM=1499820478; BIDUPSID=78722458D4F982F0E5688249DF647F04; BDUSS=TRPQno3Ry1UUVZITHBKU0k3U3FNfmVSR2ZYakFIU21oTXkyc0VMMk9hV3BYNVZaSVFBQUFBJCQAAAAAAAAAAAEAAAAkDaAFbG53YXpnAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAKnSbVmp0m1ZW; MCITY=-%3A; H_PS_PSSID=1440_21119_22159; BDSFRCVID=YoIsJeC62Z-eYGTAoMvecaE-weDDZ4rTH6aoF89jwkaZnFgDNxDOEG0PqU8g0Kub2VqXogKKL2OTHmoP; H_BDCLCKID_SF=tR-JoDDMJDL3qPTuKITaKDCShUFst-7W-2Q-5KL-fCjEEDJFLJbm3UkS-t_L--QeJTbi3MbdJJjoHI8GqtR2y-bQLlbyJx_La2TxoUJh5DnJhhvG-4PKjtCebPRiWTj9QgbLWMtLtD85bKt4D5A35n-Wql3KbtoQaITQQ5rJabC3oJ7VKU6qLT5XWmc-Xxbn0COuLJ5JKlKVfn5eQhCMKl0njxQy0jk82jvW3xc83J3U84neXUonDh8L3H7MJUntKJrpQxnO5hvv8KoO3M7VhpOh-p52f6KDJnAD3H; BDRCVFR[feWj1Vr5u3D]=I67x6TjHwwYf0; PSINO=3; locale=zh; STOKEN=bf5069e91a0c2054c79cf2cbe12d14b7ae50719151111cc90389f9792cf2536a; Hm_lvt_c6f51662a6f72f83745b8724a21a80b0=1515636064; Hm_lpvt_c6f51662a6f72f83745b8724a21a80b0=1515636255; PANPSC=9403124002264100157%3Acvzr9Jxhf5BQi%2FqJDCUQ%2BSOAoXNFwReXE%2BGQ%2Fklt7TtkpM38qW%2FTrD9hnwPONS7CfPb0JMZPTIw%2FyWEPBP8bbBm1oZaG%2B7VpVb5kimJikYQ%2BMIHUP9XFjCpOBrl9L%2Fv6kj8ekCcZNBwW3yjGOz7YfEdPbuUCqdp8F26SqGuKC9wQwTXW6KH7t9E%2FrpzBjbZx"));
        NoteBook noteBook = GsonKit.parseString2Object(result, NoteBook.class);
        if (noteBook != null)
        {
            List<NoteRecord> records = noteBook.getRecords();
            if (Lists.isNotEmpty(records))
            {
                Logs.i("拉取到" + records.size() + "条笔记...");
                
                CalendarDesc calendarDesc = DateUtils.getCurrentCalendarDesc();
                int year = calendarDesc.getYear();
                int month = calendarDesc.getMonth() + 1;
                String filePath = String.format("E:\\2012\\d心理碎片trunk\\d代码碎片汇总_%s0101\\%s年%s月\\系统说明_%s%02d01.txt", year, year, month, year, month);
                Logs.i("代码碎片汇总文件路径: " + filePath);
                File targetFile = new File(filePath);
                
                //最后一条记录的编号
                String encoding = CharsetKit.getFileCharset(targetFile);
                int lastNum = getLatestRecordNum(targetFile, encoding);
                Logs.i("最新记录编号：" + lastNum);
                
                List<String> lines = new ArrayList<>();
                
                for (int i = records.size() - 1; i >= 0; i--)
                {
                    NoteRecord noteRecord = records.get(i);
                    System.out.println(noteRecord.getContent());
                    
                    lines.add(String.format("\r\n\r\n%s.%s", (++lastNum), noteRecord.getContent()));
                    
                    //                    Logs.i("删除该笔记...");
                    String delUrl = "http://note.baidu.com/api/note?method=delete&t=1515638671302&channel=chunlei&web=1&app_id=250528&bdstoken=417d4df34be9971be08bdbe1f4560d21&logid=MTUxNTYzODY3MTMwMzAuMjA";
                    result = HttpUtils.doPost(delUrl,
                        Maps.asStrHashMap("param", GsonKit.parseObject2String(
                            Maps.asMap("_key", Lists.asList(noteRecord.get_key())))),
                        Maps.asStrHashMap("Cookie", "BAIDUID=70DA5791665EF397B850693E2C0881E5:FG=1; PSTM=1499820478; BIDUPSID=78722458D4F982F0E5688249DF647F04; BDUSS=TRPQno3Ry1UUVZITHBKU0k3U3FNfmVSR2ZYakFIU21oTXkyc0VMMk9hV3BYNVZaSVFBQUFBJCQAAAAAAAAAAAEAAAAkDaAFbG53YXpnAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAKnSbVmp0m1ZW; MCITY=-%3A; H_PS_PSSID=1440_21119_22159; BDSFRCVID=YoIsJeC62Z-eYGTAoMvecaE-weDDZ4rTH6aoF89jwkaZnFgDNxDOEG0PqU8g0Kub2VqXogKKL2OTHmoP; H_BDCLCKID_SF=tR-JoDDMJDL3qPTuKITaKDCShUFst-7W-2Q-5KL-fCjEEDJFLJbm3UkS-t_L--QeJTbi3MbdJJjoHI8GqtR2y-bQLlbyJx_La2TxoUJh5DnJhhvG-4PKjtCebPRiWTj9QgbLWMtLtD85bKt4D5A35n-Wql3KbtoQaITQQ5rJabC3oJ7VKU6qLT5XWmc-Xxbn0COuLJ5JKlKVfn5eQhCMKl0njxQy0jk82jvW3xc83J3U84neXUonDh8L3H7MJUntKJrpQxnO5hvv8KoO3M7VhpOh-p52f6KDJnAD3H; BDRCVFR[feWj1Vr5u3D]=I67x6TjHwwYf0; PSINO=3; locale=zh; STOKEN=bf5069e91a0c2054c79cf2cbe12d14b7ae50719151111cc90389f9792cf2536a; Hm_lvt_c6f51662a6f72f83745b8724a21a80b0=1515636064; Hm_lpvt_c6f51662a6f72f83745b8724a21a80b0=1515636255; PANPSC=9403124002264100157%3Acvzr9Jxhf5BQi%2FqJDCUQ%2BSOAoXNFwReXE%2BGQ%2Fklt7TtkpM38qW%2FTrD9hnwPONS7CfPb0JMZPTIw%2FyWEPBP8bbBm1oZaG%2B7VpVb5kimJikYQ%2BMIHUP9XFjCpOBrl9L%2Fv6kj8ekCcZNBwW3yjGOz7YfEdPbuUCqdp8F26SqGuKC9wQwTXW6KH7t9E%2FrpzBjbZx"));
                    //                    Logs.i("删除结果:" + result);
                }
                
                Logs.i("开始追加到本地日记本中...");
                //                String linesStr = String.join("", lines);
                
                try
                {
                    FileUtils.writeLines(targetFile, encoding, lines, IOUtils.LINE_SEPARATOR_WINDOWS, true);
                    //                    FileUtils.write(targetFile, encoding, linesStr, true);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                Logs.i("本地日记本追加完毕");
            }
            else
            {
                Logs.i("未能拉取到任何笔记");
            }
        }
        Logs.i("百度云笔记数据整合完毕...");
    }
    
    /**
     * 获取最新的记录编号
     * @author nan.li
     * @param targetFile
     * @return
     */
    private static int getLatestRecordNum(File file, String encoding)
    {
        try
        {
            List<String> lines = FileUtils.readLines(file, encoding);
            if (Lists.isNotEmpty(lines))
            {
                for (int i = lines.size() - 1; i >= 0; i--)
                {
                    String line = lines.get(i);
                    if (DiaryKit.matchParagraphStart(line))
                    {
                        return Integer.valueOf(line.substring(0, line.indexOf(".")));
                    }
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return 0;
    }
    
    public static void main(String[] args)
    {
        process();
    }
}
