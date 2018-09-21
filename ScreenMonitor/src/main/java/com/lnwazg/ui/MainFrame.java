package com.lnwazg.ui;

import javax.swing.JLabel;

import com.lnwazg.kit.monitor.MonitorModule;
import com.lnwazg.swing.xmlbuilder.XmlJFrame;
import com.lnwazg.swing.xmlbuilder.anno.XmlBuild;

/**
 * 主窗体<br>
 * @author Administrator
 * @version 2016年2月12日
 */
@XmlBuild("ScreenMonitor.xml")
public class MainFrame extends XmlJFrame
{
    private static final long serialVersionUID = 416736654918898426L;
    
    public JLabel statusLabel;
    
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
        //启用监控模块功能
        MonitorModule.init();
    }
    
    private void initEnv()
    {
    }
    
    private void initListeners()
    {
    }
}
