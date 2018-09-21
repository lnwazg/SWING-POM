package com.lnwazg.ui;

import com.lnwazg.swing.xmlbuilder.XmlJFrame;
import com.lnwazg.swing.xmlbuilder.anno.XmlBuild;

@XmlBuild("MobileNews.xml")
public class MainFrame extends XmlJFrame
{
    private static final long serialVersionUID = 4694458255727326227L;
    
    @Override
    public void afterUIBind()
    {
    }
}
