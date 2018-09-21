package com.lnwazg.ui;

import javax.swing.JLabel;
import javax.swing.JTextPane;

import org.apache.commons.lang3.StringUtils;

import com.lnwazg.kit.date.DateUtils;
import com.lnwazg.kit.shell.ScriptKit;
import com.lnwazg.kit.swing.anno.DataBinding;
import com.lnwazg.kit.swing.ui.comp.SmartButton;
import com.lnwazg.swing.util.DsTool;
import com.lnwazg.swing.util.WinMgr;
import com.lnwazg.swing.util.ui.IOS7SwitchButton;
import com.lnwazg.swing.xmlbuilder.XmlJFrame;
import com.lnwazg.swing.xmlbuilder.anno.XmlBuild;

/**
 * 主窗体
 * @author Administrator
 * @version 2016年2月12日
 */
@XmlBuild("Will.xml")
public class MainFrame extends XmlJFrame
{
    private static final long serialVersionUID = 416736654918898426L;
    
    @DataBinding("1")
    private JTextPane wish1;
    
    @DataBinding("2")
    private JTextPane wish2;
    
    @DataBinding("3")
    private JTextPane wish3;
    
    @DataBinding("4")
    private JTextPane wish4;
    
    @DataBinding("5")
    private JTextPane wish5;
    
    @DataBinding("6")
    private JTextPane wish6;
    
    @DataBinding("7")
    private JTextPane wish7;
    
    @DataBinding("8")
    private JTextPane wish8;
    
    @DataBinding("9")
    private JTextPane wish9;
    
    @DataBinding("10")
    private JTextPane wish10;
    
    @DataBinding("11")
    private JTextPane wish11;
    
    @DataBinding("12")
    private JTextPane wish12;
    
    @DataBinding
    private JTextPane action1;
    
    @DataBinding
    private JTextPane action2;
    
    @DataBinding
    private JTextPane action3;
    
    @DataBinding
    private JTextPane action4;
    
    @DataBinding
    private JTextPane action5;
    
    @DataBinding
    private JTextPane action6;
    
    @DataBinding
    private JTextPane action7;
    
    @DataBinding
    private JTextPane action8;
    
    @DataBinding
    private JTextPane action9;
    
    @DataBinding
    private JTextPane action10;
    
    @DataBinding
    private JTextPane action11;
    
    @DataBinding
    private JTextPane action12;
    
    @DataBinding
    private JTextPane wish13;
    
    @DataBinding
    private JTextPane wish14;
    
    @DataBinding
    private JTextPane wish15;
    
    @DataBinding
    private JTextPane wish16;
    
    @DataBinding
    private JTextPane wish17;
    
    @DataBinding
    private JTextPane wish18;
    
    @DataBinding
    private JTextPane wish19;
    
    @DataBinding
    private JTextPane wish20;
    
    @DataBinding
    private JTextPane action13;
    
    @DataBinding
    private JTextPane action14;
    
    @DataBinding
    private JTextPane action15;
    
    @DataBinding
    private JTextPane action16;
    
    @DataBinding
    private JTextPane action17;
    
    @DataBinding
    private JTextPane action18;
    
    @DataBinding
    private JTextPane action19;
    
    @DataBinding
    private JTextPane action20;
    
    @DataBinding(dataNullShowFix = "0")
    private JLabel number;
    
    @DataBinding(dataNullShowFix = "0")
    private JLabel breakNumber;
    
    private SmartButton add;
    
    private SmartButton minus;
    
    private SmartButton breakAdd;
    
    private SmartButton breakMinus;
    
    private SmartButton openWordPad;
    
    //    @ShortCutKey(KeyEvent.VK_S)
    //快捷键与文本编辑框冲突了...
    private IOS7SwitchButton networkSwitchBtn;
    
    @Override
    public void afterUIBind()
    {
        initEnv();
        initListeners();
    }
    
    /**
     * 初始化工作环境
     * @author Administrator
     */
    private void initEnv()
    {
        setTitle(String.format("%s碎碎念，一定会实现！", DateUtils.getCurrentCalendarDesc().getYear()));
        
        String switchResult = WinMgr.getConfig("networkSwitch");
        if (StringUtils.isEmpty(switchResult) || "false".equals(switchResult))
        {
            networkSwitchBtn.setStatus(false);
        }
        else
        {
            networkSwitchBtn.setStatus(true);
        }
    }
    
    /**
     * 绑定一些容易操作的事件
     * @author Administrator
     */
    private void initListeners()
    {
        add.addActionListener((e) -> {
            number.setText((Integer.valueOf(number.getText()) + 1) + "");
            DsTool.saveDataToDs("number", number.getText());
        });
        minus.addActionListener((e) -> {
            number.setText((Integer.valueOf(number.getText()) - 1) + "");
            DsTool.saveDataToDs("number", number.getText());
        });
        
        breakAdd.addActionListener((e) -> {
            breakNumber.setText((Integer.valueOf(breakNumber.getText()) + 1) + "");
            DsTool.saveDataToDs("breakNumber", breakNumber.getText());
        });
        breakMinus.addActionListener((e) -> {
            breakNumber.setText((Integer.valueOf(breakNumber.getText()) - 1) + "");
            DsTool.saveDataToDs("breakNumber", breakNumber.getText());
        });
        
        networkSwitchBtn.addActionListener(e -> {
            if (networkSwitchBtn.isSwitchStatus())
            {
                //改成关
                WinMgr.setConfig("networkSwitch", "false");
            }
            else
            {
                //改成开
                WinMgr.setConfig("networkSwitch", "true");
            }
        });
        openWordPad.addActionListener(e -> {
            String cmd = String.format("E:\\2012\\nppp\\nppp.exe E:\\2012\\5年计划纲要与年度总结\\%d.txt", DateUtils.getCurrentCalendarDesc().getYear());
            ScriptKit.execute(cmd);
        });
    }
}
