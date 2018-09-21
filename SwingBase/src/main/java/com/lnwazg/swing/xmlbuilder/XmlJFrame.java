package com.lnwazg.swing.xmlbuilder;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRootPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.lnwazg.kit.log.Logs;
import com.lnwazg.kit.map.Maps;
import com.lnwazg.kit.swing.anno.DataBinding;
import com.lnwazg.kit.swing.anno.ShortCutKey;
import com.lnwazg.swing.util.DsTool;
import com.lnwazg.swing.util.WinMgr;
import com.lnwazg.swing.xmlbuilder.iface.XmlBuildSupport;

/**
 * 需要实现XML构建支持的JFrame
 * @author Administrator
 * @version 2015年11月3日
 */
public abstract class XmlJFrame extends JFrame implements XmlBuildSupport
{
    private static final long serialVersionUID = -1915997321640851876L;
    
    /**
     * 当前的JFrame所绑定的注册表对象
     */
    protected CompRegistry $;
    
    /**
     * 框架的额外操作<br>
     * 该操作要先于afterUIBind()方法的执行<br>
     * @see XmlLayoutBuilder.startBuild(XmlBuild xmlBuildInfo, JFrame targetFrame)
     * @author nan.li
     */
    protected void frameworkExtra()
    {
        initDsWorkMode();
        initDataBinding();
    }
    
    /**
     * 初始化数据源工作模式
     * @author nan.li
     */
    private void initDsWorkMode()
    {
        //是否联网工作
        //默认是离线模式
        String switchResult = WinMgr.getConfig("networkSwitch");
        if (StringUtils.isEmpty(switchResult) || "false".equals(switchResult))
        {
            DsTool.networkMode = false;
        }
        else
        {
            DsTool.networkMode = true;
        }
        DsTool.initSync();
    }
    
    protected void initDataBinding()
    {
        Logs.i("初始化双向数据绑定框架...");
        
        //说是双向绑定，其实只要实现单向绑定就能满足要求了...
        //无他，因为单向绑定实现起来更加容易而已，并且对性能的损耗最低
        Field[] fields = getClass().getDeclaredFields();
        for (Field field : fields)
        {
            field.setAccessible(true);
            if (field.isAnnotationPresent(DataBinding.class))
            {
                DataBinding dataBinding = field.getAnnotation(DataBinding.class);
                String dataBindingValue = dataBinding.value();
                if (StringUtils.isEmpty(dataBindingValue))
                {
                    dataBindingValue = field.getName();
                }
                final String key = dataBindingValue;
                final String dataNullShowFix = dataBinding.dataNullShowFix();
                Logs.i(String.format("开始为%s-%s做数据绑定...", field.getType().getSimpleName(), field.getName()));
                try
                {
                    Object fieldObj = field.get(this);
                    
                    if (field.getType() == JTextPane.class)
                    {
                        JTextPane jTextPane = (JTextPane)fieldObj;
                        //load数据
                        //jTextPane.setText(DsTool.getFixDataFromDs(key, dataNullShowFix));
                        //自动数据绑定，网络一旦刷新了，本地也将会很快同步更新！
                        DsTool.getAndSyncDataFromDs(key, dataNullShowFix, jTextPane);
                        
                        //写数据
                        
                        //这个监听器会连锁反应，当手动setText()时也会被触发，这样就无法区分是按键盘修改的，还是网络同步的了！
                        //                        jTextPane.getDocument().addDocumentListener(new DocumentListener()
                        //                        {
                        //                            @Override
                        //                            public void removeUpdate(DocumentEvent e)
                        //                            {
                        //                                DsTool.saveDataToDs(key, jTextPane.getText());
                        //                            }
                        //                            
                        //                            @Override
                        //                            public void insertUpdate(DocumentEvent e)
                        //                            {
                        //                                DsTool.saveDataToDs(key, jTextPane.getText());
                        //                            }
                        //                            
                        //                            @Override
                        //                            public void changedUpdate(DocumentEvent e)
                        //                            {
                        //                                DsTool.saveDataToDs(key, jTextPane.getText());
                        //                            }
                        //                        });
                        
                        //按键监听，才能真正区分是我做的还是机器同步的！
                        jTextPane.addKeyListener(new KeyListener()
                        {
                            @Override
                            public void keyTyped(KeyEvent e)
                            {
                                //                                System.out.println("111");
                                DsTool.saveDataToDs(key, jTextPane.getText());
                            }
                            
                            @Override
                            public void keyReleased(KeyEvent e)
                            {
                                //                                System.out.println("2222");
                                DsTool.saveDataToDs(key, jTextPane.getText());
                            }
                            
                            @Override
                            public void keyPressed(KeyEvent e)
                            {
                                //                                System.out.println("3333");
                                DsTool.saveDataToDs(key, jTextPane.getText());
                            }
                        });
                    }
                    else if (field.getType() == JLabel.class)
                    {
                        JLabel jLabel = (JLabel)fieldObj;
                        //load数据
                        //jLabel.setText(DsTool.getFixDataFromDs(key, dataNullShowFix));
                        //自动数据绑定，网络一旦刷新了，本地也将会很快同步更新！
                        DsTool.getAndSyncDataFromDs(key, dataNullShowFix, jLabel);
                    }
                }
                catch (IllegalArgumentException e)
                {
                    e.printStackTrace();
                }
                catch (IllegalAccessException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
    
    @Override
    protected JRootPane createRootPane()
    {
        //绑定快捷键
        JRootPane rootPane = new JRootPane();
        
        Logs.i("扫描键绑定map...");
        //轮询map，依次绑定全局监听器事件
        if (Maps.isNotEmpty(getGlobalKeyEventMap()))
        {
            Logs.i("键绑定map非空，开始设置绑定map...");
            Map<Integer, ActionListener> globalKeyEventMap = getGlobalKeyEventMap();
            for (Integer keyCode : globalKeyEventMap.keySet())
            {
                rootPane.registerKeyboardAction(globalKeyEventMap.get(keyCode), KeyStroke.getKeyStroke(keyCode, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
            }
        }
        
        Logs.i("扫描键绑定注解...");
        //这个才是神器般的超级方便的功能！
        //自动检测注解上的键绑定
        Field[] fields = getClass().getDeclaredFields();
        for (Field field : fields)
        {
            ShortCutKey[] shortCutKeys = field.getAnnotationsByType(ShortCutKey.class);
            if (ArrayUtils.isNotEmpty(shortCutKeys))
            {
                Logs.i(String.format("在%s字段上发现键绑定注解，开始设置...", field.getName()));
                for (ShortCutKey shortCutKey : shortCutKeys)
                {
                    rootPane.registerKeyboardAction(e -> {
                        try
                        {
                            field.setAccessible(true);
                            //触发一次该按钮的 点击事件
                            ((AbstractButton)field.get(XmlJFrame.this)).doClick();
                        }
                        catch (Exception e1)
                        {
                            e1.printStackTrace();
                        }
                    } , KeyStroke.getKeyStroke(shortCutKey.value(), 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
                }
            }
        }
        Logs.i("键绑定解析完毕...");
        return rootPane;
    }
    
    /**
     * 获取绑定监听器的Map<br>
     * 覆写此方法将改变全局键盘监听器的使用<br>
     * 这个方法可以覆盖，示例代码见下面的注释掉的代码
     * @author nan.li
     * @return
     */
    protected Map<Integer, ActionListener> getGlobalKeyEventMap()
    {
        return new HashMap<>();
    }
    
    //全局快捷键绑定的例子
    //    @Override
    //    protected Map<Integer, ActionListener> getGlobalKeyEventMap()
    //    {
    //        Map<Integer, ActionListener> map = new HashMap<>();
    
    //        //关机监听器
    //        map.put(KeyEvent.VK_F1, e -> {
    //            shutdownBtn.doClick();
    //        });
    //        map.put(KeyEvent.VK_F8, e -> {
    //            shutdownBtn.doClick();
    //        });
    //        map.put(KeyEvent.VK_F9, e -> {
    //            shutdownBtn.doClick();
    //        });
    
    //今天明天后天按钮
    //        map.put(KeyEvent.VK_1, e -> {
    //            todayTask.doClick();
    //        });
    //        map.put(KeyEvent.VK_2, e -> {
    //            tomorrowTask.doClick();
    //        });
    //        map.put(KeyEvent.VK_3, e -> {
    //            afterTomorrowTask.doClick();
    //        });
    //        
    //        //打呵欠
    //        map.put(KeyEvent.VK_Q, e -> {
    //            yawnRecord.doClick();
    //        });
    //        return map;
    //    }
}
