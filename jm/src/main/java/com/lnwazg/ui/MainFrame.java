package com.lnwazg.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.lnwazg.kit.swing.ui.comp.SmartButton;
import com.lnwazg.swing.xmlbuilder.XmlJFrame;
import com.lnwazg.swing.xmlbuilder.anno.XmlBuild;
import com.lnwazg.util.EncrypUtils;

/**
 * 主窗体
 * @author Administrator
 * @version 2016年2月12日
 */
@XmlBuild("jm.xml")
public class MainFrame extends XmlJFrame
{
    private static final long serialVersionUID = 416736654918898426L;
    
    private JTextField src_text;
    
    private JTextField target_text;
    
    private SmartButton btn_encrypt;
    
    private SmartButton btn_decrypt;
    
    private JTextArea result;
    
    @Override
    public void afterUIBind()
    {
        initListeners();
    }
    
    /**
     * 绑定一些容易操作的事件
     * @author Administrator
     */
    private void initListeners()
    {
        btn_encrypt.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                result.setText(EncrypUtils.encrypt(src_text.getText()));
            }
        });
        btn_decrypt.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                result.setText(EncrypUtils.decrypt(target_text.getText()));
            }
        });
    }
}
