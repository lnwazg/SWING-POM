package com.lnwazg.ui;

import com.lnwazg.swing.xmlbuilder.XmlJFrame;
import com.lnwazg.swing.xmlbuilder.anno.XmlBuild;

/**
 * 主窗体
 * @author Administrator
 * @version 2016年2月12日
 */
@XmlBuild("SoalClock.xml")
public class MainFrame extends XmlJFrame
{
    private static final long serialVersionUID = 416736654918898426L;
    
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
    
    }
    
    /**
     * 绑定一些容易操作的事件
     * @author Administrator
     */
    private void initListeners()
    {
    
    }
}
