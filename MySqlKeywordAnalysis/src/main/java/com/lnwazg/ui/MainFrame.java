package com.lnwazg.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.lnwazg.swing.util.WinMgr;
import com.lnwazg.swing.xmlbuilder.XmlJFrame;
import com.lnwazg.swing.xmlbuilder.anno.XmlBuild;

@XmlBuild("main.xml")
public class MainFrame extends XmlJFrame
{
    private static final long serialVersionUID = 5198363906417130690L;
    
    final String[] keywords = WinMgr.configs.get("KEYWORDS").toLowerCase().split(",");
    
    private JTextField paramName;
    
    private JTextPane resultContent;
    
    @Override
    public void afterUIBind()
    {
        initListeners();
    }
    
    /**
     * 初始化监听器
     * @author nan.li
     */
    private void initListeners()
    {
        resultContent.setContentType("text/html");
        // 输入的字段要立即将所读取到的内容全部匹配到
        paramName.getDocument().addDocumentListener(new DocumentListener()
        {
            @Override
            public void removeUpdate(DocumentEvent e)
            {
                handleInput();
            }
            
            @Override
            public void insertUpdate(DocumentEvent e)
            {
                handleInput();
            }
            
            @Override
            public void changedUpdate(DocumentEvent e)
            {
                handleInput();
            }
        });
        handleInput();//主动触发一次内容
    }
    
    protected void handleInput()
    {
        String param = paramName.getText().toLowerCase();
        if (StringUtils.isEmpty(param))
        {
            resultContent.setText(showAllKeywords());
        }
        else
        {
            if (ArrayUtils.contains(keywords, param))
            {
                resultContent.setText(String.format(
                    "<div></div><div></div><div align='center'><h2><font color=red><b>%s</b></font>是<font color=red><b>关键字</b></font>，<br>不可以使用哦！</h2></div>",
                    param));
            }
            else
            {
                String similarShowStr = getSimilarShowStr(param);
                String result = new StringBuilder("<div></div><div></div><div align='center'><h2><font color=green>恭喜你，您输入的参数名可以使用！</font></h2></div>")
                    .append(similarShowStr).toString();
                resultContent.setText(result);
            }
        }
        resultContent.setCaretPosition(0);
    }
    
    /**
     * 相似的单词
     * @author nan.li
     * @param param
     * @param keywords2
     * @return
     */
    private String getSimilarShowStr(String param)
    {
        if (StringUtils.isEmpty(param) || param.length() < 2)
        {
            return "";
        }
        StringBuilder sb = new StringBuilder("<span><h4>类似的关键字有:<br>");
        //从1位到最长位数，分别去匹配，将匹配结果放置到结果集中
        boolean exists = false;//是否存在可匹配到的相似的关键字
        int totalCount = 0;
        int pLen = param.length();
        for (String s : keywords)
        {
            for (int i = 2; i <= pLen; i++)
            {
                String matchParam = param.substring(0, 0 + i);
                if (StringUtils.startsWith(s, matchParam))
                {
                    totalCount++;
                    sb.append(s).append("&nbsp;&nbsp;&nbsp;").append(totalCount % 3 == 0 ? "<br>" : "");
                    exists = true;
                    break;
                }
            }
        }
        if (!exists)
        {
            return "";
        }
        else
        {
            sb.append("</h4></span>");
        }
        return sb.toString();
    }
    
    /**
     * 显示所有的关键字
     * @author nan.li
     * @param keywords
     * @return
     */
    protected String showAllKeywords()
    {
        StringBuilder sb = new StringBuilder("所有的关键字如下：<br>");
        for (String s : keywords)
        {
            sb.append(s).append("  ");
        }
        return sb.toString();
    }
    
    /**
     * 获取匹配到的关键字列表
     * @author nan.li
     * @param param
     * @param keywords
     * @return
     */
    protected List<String> getMatchList(String param, String[] keywords)
    {
        List<String> ret = new ArrayList<String>();
        for (String s : keywords)
        {
            if (s.equals(param))
            {
                ret.add(s);
            }
        }
        return ret;
    }
    
    @Override
    protected JRootPane createRootPane()
    {
        //添加点击esc之后清空输入框的事件
        KeyStroke escStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        JRootPane rootPane = new JRootPane();
        //注册按键监听器
        rootPane.registerKeyboardAction(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                paramName.setText("");//清空查询输入框
            }
        }, escStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
        return rootPane;
    }
}
