package com.lnwazg.ui;

import javax.swing.JTextField;

import com.lnwazg.swing.xmlbuilder.XmlJFrame;
import com.lnwazg.swing.xmlbuilder.anno.XmlBuild;

@XmlBuild("PictureViewer.xml")
public class MainFrame2 extends XmlJFrame
{
    private static final long serialVersionUID = 5198363906417130690L;
    
    @Override
    public void afterUIBind()
    {
        setLocationRelativeTo(null);
        pack();
        //        btnBad2.setText("xxxxxxxxxx");
        //        setSize(1000, 700);
        ((JTextField)$.get("editLoginName")).setText("haohaohao");
        ((JTextField)$.get("editLoginPsw")).setText("xxxxxxxxxxxxx");
    }
    
}
