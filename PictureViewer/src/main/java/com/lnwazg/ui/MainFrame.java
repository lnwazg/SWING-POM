package com.lnwazg.ui;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;

import com.lnwazg.dbkit.jdbc.ConnectionManager;
import com.lnwazg.dbkit.resolver.ResultSetResolve;
import com.lnwazg.dbkit.utils.DbKit;
import com.lnwazg.kit.compress.GzipBytesUtils;
import com.lnwazg.kit.executor.ExecMgr;
import com.lnwazg.kit.swing.R;
import com.lnwazg.kit.swing.SwingUtils;
import com.lnwazg.kit.swing.ui.comp.ImageScroller;
import com.lnwazg.kit.swing.ui.comp.SmartButton;
import com.lnwazg.swing.util.WinMgr;
import com.lnwazg.swing.xmlbuilder.XmlJFrame;
import com.lnwazg.swing.xmlbuilder.anno.XmlBuild;

@XmlBuild("PictureViewer.xml")
public class MainFrame extends XmlJFrame
{
    private static final long serialVersionUID = 911994927255462705L;
    
    SmartButton btnSwitch, btnSwitch2;
    
    ImageScroller imageScroller;
    
    int totalPics = 79;
    
    ConnectionManager jdbc = null;
    
    //    DruidDataSource datasource = null;
    
    @Override
    public void afterUIBind()
    {
        initDs(false);
        btnSwitch.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ExecMgr.guiExec.execute(new Runnable()
                {
                    public void run()
                    {
                        imageScroller.setImageContent(R.icon(String.format("images/%d.jpg", RandomUtils.nextInt(79) + 1)));
                        pack();
                        if (WinMgr.win(MainFrame.class).getWidth() > 1600)
                        {
                            WinMgr.win(MainFrame.class).setSize(new java.awt.Dimension(1600, 900));
                        }
                    }
                });
            }
        });
        btnSwitch2.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                //查询到数据库中的任意一张图片，并将其字节显示出来
                //                select gzipBytes from PhotoStore where id=1
                //查询的时候无需单线程池控制，只有写入的时候才需要控制
                ExecMgr.cachedExec.execute(new Runnable()
                {
                    public void run()
                    {
                        try
                        {
                            int total = 40085;
                            byte[] gzipBytes;
                            gzipBytes = jdbc.findOne(new ResultSetResolve<byte[]>()
                            {
                                @Override
                                public byte[] exec(ResultSet rs)
                                    throws SQLException
                                {
                                    return rs.getBytes("gzipBytes");
                                }
                            }, "select gzipBytes from PhotoStore where id=" + (RandomUtils.nextInt(total - 1) + 1));
                            byte[] imgData = GzipBytesUtils.unzip(gzipBytes);
                            Image nextImage = Toolkit.getDefaultToolkit().createImage(imgData);
                            ExecMgr.guiExec.execute(new Runnable()
                            {
                                public void run()
                                {
                                    imageScroller.setImageContent(new ImageIcon(nextImage));
                                    pack();
                                    if (WinMgr.win(MainFrame.class).getWidth() > 1600)
                                    {
                                        WinMgr.win(MainFrame.class).setSize(new java.awt.Dimension(1600, 900));
                                    }
                                }
                            });
                        }
                        catch (SQLException e1)
                        {
                            e1.printStackTrace();
                            initDs(true);
                        }
                    }
                });
            }
        });
        regHotKey();
    }
    
    /**
     * 初始化数据源
     * @author Administrator
     */
    private void initDs(boolean forceInit)
    {
        //        datasource = new DruidDataSource();
        //        jdbc:sqlite://dirA/dirB/dbfile
        //            jdbc:sqlite://DRIVE:/dirA/dirB/dbfile
        //            jdbc:sqlite://COMPUTERNAME/shareA/dirB/dbfile
        //        datasource.setUrl("jdbc:sqlite://n:/PhotoWall2.db");
        //        datasource.setUrl("jdbc:sqlite://N:\\PhotoWall.db");
        String ds = WinMgr.cfg("ds");
        if (forceInit)
        {
            //不管是否为空，都要重新选择！
            File file = SwingUtils.chooseFile(WinMgr.win(MainFrame.class), "数据库读取失败！请重新选择数据库文件（PhotoWall.db）");
            ds = String.format("jdbc:sqlite://%s", file.getAbsolutePath());
            WinMgr.cfg("ds", ds);
        }
        else
        {
            //只有为空的时候，才再次选择
            if (StringUtils.isEmpty(ds))
            {
                File file = SwingUtils.chooseFile(WinMgr.win(MainFrame.class), "初始化数据库配置信息，请选择数据库文件（PhotoWall.db）");
                ds = String.format("jdbc:sqlite://%s", file.getAbsolutePath());
                WinMgr.cfg("ds", ds);
            }
        }
        //        datasource.setUrl(ds);
        //        datasource.setUsername("");
        //        datasource.setPassword("");
        //        try
        //        {
        //            datasource.init();
        //        }
        //        catch (SQLException e)
        //        {
        //            e.printStackTrace();
        //        }
        // create NewbieJdbc object
        //        jdbc = new NewbieJdbcSupport(datasource);
        jdbc = DbKit.getJdbc(ds, "", "");
    }
    
    //定义热键标识，用于在设置多个热键时，在事件处理中区分用户按下的热键
    public static final int FUNC_KEY_LEFT = 1;
    
    public static final int FUNC_KEY_RIGHT = 0;
    
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
                btnSwitch.doClick();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        //注册按键监听器
        //F1发音
        rootPane.registerKeyboardAction(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                btnSwitch2.doClick();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        return rootPane;
    }
    
    /**
     * 注册唤醒热键
     * @author nan.li
     */
    private void regHotKey()
    {
        //系统级的快捷键毕竟不是所有软件都配享有的！只有googleTranslate可以享受！所以，此处还是用用户级的吧！
        //        try
        //        {
        //            //第一步：注册热键，第一个参数表示该热键的标识，第二个参数表示组合键，如果没有则为0，第三个参数为定义的主要热键
        //            JIntellitype.getInstance().registerHotKey(FUNC_KEY_LEFT, 0, KeyEvent.VK_LEFT);
        //            JIntellitype.getInstance().registerHotKey(FUNC_KEY_RIGHT, 0, KeyEvent.VK_RIGHT);
        //            //第二步：添加热键监听器
        //            JIntellitype.getInstance().addHotKeyListener(new HotkeyListener()
        //            {
        //                @Override
        //                public void onHotKey(int markCode)
        //                {
        //                    switch (markCode)
        //                    {
        //                        case FUNC_KEY_LEFT:
        //                            btnSwitch.doClick();
        //                            break;
        //                        case FUNC_KEY_RIGHT:
        //                            btnSwitch2.doClick();
        //                            break;
        //                        default:
        //                            break;
        //                    }
        //                }
        //            });
        //        }
        //        catch (Exception e)
        //        {
        //            e.printStackTrace();
        //            //            WinMgr.win(MainFrame.class).setVisible(false);
        //            //重复打开该辞典，会导致热键注册失败，因此后打开的应该退出
        //            JOptionPane.showMessageDialog(WinMgr.win(MainFrame.class), "重复打开会导致热键注册失败哦！");
        //            //            System.exit(0);
        //        }
    }
}
