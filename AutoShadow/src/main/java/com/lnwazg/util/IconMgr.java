package com.lnwazg.util;

import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.lnwazg.ui.MainFrame;

/**
 * 图标管理器
 * @author nan.li
 * @version 2016年8月29日
 */
public class IconMgr
{
    public static Image iconOk1 = Toolkit.getDefaultToolkit().createImage(MainFrame.class.getClassLoader().getResource("icons/ss_ok1.ico"));
    
    public static Image iconOk2 = Toolkit.getDefaultToolkit().createImage(MainFrame.class.getClassLoader().getResource("icons/ss_ok2.ico"));
    
    public static Image iconOk3 = Toolkit.getDefaultToolkit().createImage(MainFrame.class.getClassLoader().getResource("icons/ss_ok3.ico"));
    
    public static Image iconErr = Toolkit.getDefaultToolkit().createImage(MainFrame.class.getClassLoader().getResource("icons/ss_err.ico"));
    
    public static Image icon = Toolkit.getDefaultToolkit().createImage(MainFrame.class.getClassLoader().getResource("icons/ss.ico"));
    
    public static Icon iconSwitchOn = new ImageIcon(Toolkit.getDefaultToolkit().createImage(MainFrame.class.getClassLoader().getResource("icons/on.png")));
    
    public static Icon iconSwitchOff = new ImageIcon(Toolkit.getDefaultToolkit().createImage(MainFrame.class.getClassLoader().getResource("icons/off.png")));
    
}
