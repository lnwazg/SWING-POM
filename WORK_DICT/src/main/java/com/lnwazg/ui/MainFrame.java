package com.lnwazg.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.lnwazg.kit.swing.ui.comp.SmartButton;
import com.lnwazg.swing.util.ui.IOS7SwitchButton;
import com.lnwazg.swing.xmlbuilder.XmlJFrame;
import com.lnwazg.swing.xmlbuilder.anno.XmlBuild;

/**
 * 主窗体<br>
 * @author Administrator
 * @version 2016年2月12日
 */
@XmlBuild("WorkDict.xml")
public class MainFrame extends XmlJFrame
{
    private static final long serialVersionUID = 416736654918898426L;
    
    private JTextField queryWord;
    
    private JTextPane logScreen;
    
    private SmartButton delThisWord;
    
    private IOS7SwitchButton switchBtn;
    
    private JList<String> availWordList;
    
    private JScrollPane availWordListPane;
    
    private JScrollPane explainPane;
    
    @Override
    public void afterUIBind()
    {
        initEnv();
        initListeners();
        initExtra();
    }
    
    /**
     * 初始化其他内容
     * @author nan.li
     */
    private void initExtra()
    {
    }
    
    private void initEnv()
    {
        DefaultListModel<String> listModel = new DefaultListModel<String>();
        listModel.addElement("Debbie Scott");
        listModel.addElement("Scott Hommel");
        listModel.addElement("Sharon Zakhour");
        availWordList.setModel(listModel);
        availWordList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        availWordList.addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (e.getValueIsAdjusting() == false)
                {
                    System.out.println("not adjust");
                    //                    availWordList.setVisible(false);
                    availWordListPane.setVisible(false);
                    explainPane.setVisible(true);
                }
            }
        });
    }
    
    private void initListeners()
    {
    }
    
    @Override
    protected JRootPane createRootPane()
    {
        //添加点击esc之后清空输入框的事件
        JRootPane rootPane = new JRootPane();
        //注册按键监听器
        rootPane.registerKeyboardAction(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                queryWord.setText(null);
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        return rootPane;
    }
}
