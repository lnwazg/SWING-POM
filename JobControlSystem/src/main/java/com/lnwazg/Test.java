package com.lnwazg;

import java.util.HashMap;
import java.util.List;

import com.lnwazg.bean.NoteBook;
import com.lnwazg.bean.NoteRecord;
import com.lnwazg.kit.gson.GsonKit;
import com.lnwazg.kit.http.HttpUtils;
import com.lnwazg.kit.list.Lists;
import com.lnwazg.kit.map.Maps;

public class Test
{
    public static void main(String[] args)
    {
        String url =
            "http://note.baidu.com/api/note?method=select&limit=0-100&t=1515637718725&channel=chunlei&web=1&app_id=250528&bdstoken=417d4df34be9971be08bdbe1f4560d21&logid=MTUxNTYzNzcxODcyNjAuNzA3MTU5MjM4MzYzMjMyNg==&clienttype=0";
        String result = HttpUtils.doPost(url,
            new HashMap<>(),
            Maps.asStrHashMap("Cookie", "BAIDUID=70DA5791665EF397B850693E2C0881E5:FG=1; PSTM=1499820478; BIDUPSID=78722458D4F982F0E5688249DF647F04; BDUSS=TRPQno3Ry1UUVZITHBKU0k3U3FNfmVSR2ZYakFIU21oTXkyc0VMMk9hV3BYNVZaSVFBQUFBJCQAAAAAAAAAAAEAAAAkDaAFbG53YXpnAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAKnSbVmp0m1ZW; MCITY=-%3A; H_PS_PSSID=1440_21119_22159; BDSFRCVID=YoIsJeC62Z-eYGTAoMvecaE-weDDZ4rTH6aoF89jwkaZnFgDNxDOEG0PqU8g0Kub2VqXogKKL2OTHmoP; H_BDCLCKID_SF=tR-JoDDMJDL3qPTuKITaKDCShUFst-7W-2Q-5KL-fCjEEDJFLJbm3UkS-t_L--QeJTbi3MbdJJjoHI8GqtR2y-bQLlbyJx_La2TxoUJh5DnJhhvG-4PKjtCebPRiWTj9QgbLWMtLtD85bKt4D5A35n-Wql3KbtoQaITQQ5rJabC3oJ7VKU6qLT5XWmc-Xxbn0COuLJ5JKlKVfn5eQhCMKl0njxQy0jk82jvW3xc83J3U84neXUonDh8L3H7MJUntKJrpQxnO5hvv8KoO3M7VhpOh-p52f6KDJnAD3H; BDRCVFR[feWj1Vr5u3D]=I67x6TjHwwYf0; PSINO=3; locale=zh; STOKEN=bf5069e91a0c2054c79cf2cbe12d14b7ae50719151111cc90389f9792cf2536a; Hm_lvt_c6f51662a6f72f83745b8724a21a80b0=1515636064; Hm_lpvt_c6f51662a6f72f83745b8724a21a80b0=1515636255; PANPSC=9403124002264100157%3Acvzr9Jxhf5BQi%2FqJDCUQ%2BSOAoXNFwReXE%2BGQ%2Fklt7TtkpM38qW%2FTrD9hnwPONS7CfPb0JMZPTIw%2FyWEPBP8bbBm1oZaG%2B7VpVb5kimJikYQ%2BMIHUP9XFjCpOBrl9L%2Fv6kj8ekCcZNBwW3yjGOz7YfEdPbuUCqdp8F26SqGuKC9wQwTXW6KH7t9E%2FrpzBjbZx"));
        //        System.out.println(result);
        NoteBook noteBook = GsonKit.parseString2Object(result, NoteBook.class);
        if (noteBook != null)
        {
            List<NoteRecord> records = noteBook.getRecords();
            if (Lists.isNotEmpty(records))
            {
                for (NoteRecord noteRecord : records)
                {
                    System.out.println(noteRecord.getContent());
                    
                    String delUrl = "http://note.baidu.com/api/note?method=delete&t=1515638671302&channel=chunlei&web=1&app_id=250528&bdstoken=417d4df34be9971be08bdbe1f4560d21&logid=MTUxNTYzODY3MTMwMzAuMjA";
                    result = HttpUtils.doPost(delUrl,
                        Maps.asStrHashMap("param", GsonKit.parseObject2String(
                            Maps.asMap("_key", Lists.asList(noteRecord.get_key())))),
                        Maps.asStrHashMap("Cookie", "BAIDUID=70DA5791665EF397B850693E2C0881E5:FG=1; PSTM=1499820478; BIDUPSID=78722458D4F982F0E5688249DF647F04; BDUSS=TRPQno3Ry1UUVZITHBKU0k3U3FNfmVSR2ZYakFIU21oTXkyc0VMMk9hV3BYNVZaSVFBQUFBJCQAAAAAAAAAAAEAAAAkDaAFbG53YXpnAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAKnSbVmp0m1ZW; MCITY=-%3A; H_PS_PSSID=1440_21119_22159; BDSFRCVID=YoIsJeC62Z-eYGTAoMvecaE-weDDZ4rTH6aoF89jwkaZnFgDNxDOEG0PqU8g0Kub2VqXogKKL2OTHmoP; H_BDCLCKID_SF=tR-JoDDMJDL3qPTuKITaKDCShUFst-7W-2Q-5KL-fCjEEDJFLJbm3UkS-t_L--QeJTbi3MbdJJjoHI8GqtR2y-bQLlbyJx_La2TxoUJh5DnJhhvG-4PKjtCebPRiWTj9QgbLWMtLtD85bKt4D5A35n-Wql3KbtoQaITQQ5rJabC3oJ7VKU6qLT5XWmc-Xxbn0COuLJ5JKlKVfn5eQhCMKl0njxQy0jk82jvW3xc83J3U84neXUonDh8L3H7MJUntKJrpQxnO5hvv8KoO3M7VhpOh-p52f6KDJnAD3H; BDRCVFR[feWj1Vr5u3D]=I67x6TjHwwYf0; PSINO=3; locale=zh; STOKEN=bf5069e91a0c2054c79cf2cbe12d14b7ae50719151111cc90389f9792cf2536a; Hm_lvt_c6f51662a6f72f83745b8724a21a80b0=1515636064; Hm_lpvt_c6f51662a6f72f83745b8724a21a80b0=1515636255; PANPSC=9403124002264100157%3Acvzr9Jxhf5BQi%2FqJDCUQ%2BSOAoXNFwReXE%2BGQ%2Fklt7TtkpM38qW%2FTrD9hnwPONS7CfPb0JMZPTIw%2FyWEPBP8bbBm1oZaG%2B7VpVb5kimJikYQ%2BMIHUP9XFjCpOBrl9L%2Fv6kj8ekCcZNBwW3yjGOz7YfEdPbuUCqdp8F26SqGuKC9wQwTXW6KH7t9E%2FrpzBjbZx"));
                    System.out.println(result);
                }
            }
        }
    }
}
