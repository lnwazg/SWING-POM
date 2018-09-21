package com.lnwazg.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import org.jb2011.lnf.beautyeye.ch3_button.BEButtonUI;
import org.jb2011.swing9patch.toast.Toast;
import org.jdesktop.swingworker.SwingWorker;

import com.eva.epc.common.util.CommonUtils;
import com.eva.epc.widget.HardLayoutPane;
import com.lnwazg.sdk.MyChatBaseEventImpl;
import com.lnwazg.sdk.MyChatTransDataEventImpl;
import com.lnwazg.sdk.MyMessageQoSEventImpl;
import com.lnwazg.swing.xmlbuilder.XmlJFrame;
import com.lnwazg.swing.xmlbuilder.anno.XmlBuild;

import net.openmob.mobileimsdk.java.ClientCoreSDK;
import net.openmob.mobileimsdk.java.core.LocalUDPDataSender;
import net.openmob.mobileimsdk.java.utils.Log;

@XmlBuild("MobileIMSDKDemo-layout.xml")
public class MainFrame extends XmlJFrame
{
    private static final long serialVersionUID = 5198363906417130690L;
    
    private static final String TAG = MainFrame.class.getSimpleName();
    
    private JTextField editServerIp = null;
    
    private JTextField editServerPort = null;
    
    private JTextField editLoginName = null;
    
    private JPasswordField editLoginPsw = null;
    
    private JButton btnLogin = null;
    
    private JButton btnLogout = null;
    
    private JTextField editId = null;
    
    private JTextField editContent = null;
    
    private JButton btnSend = null;
    
    private JLabel viewMyid = null;
    
    private JTextPane debugPane;
    
    private JTextPane imInfoPane;
    
    private SimpleDateFormat hhmmDataFormat = new SimpleDateFormat("HH:mm:ss");
    
    //        public MainFrame()
    //        {
    //                    initViews();
    //            //原有的初始化工作，现在由xml解析器自动去完成！
    //                    afterUIBind();
    //        }
//    CompRegistry $ = XS.get(MainFrame.class);
    
    @Override
    public void afterUIBind()
    {
        setLocationRelativeTo(null);
        setSize(1000, 700);
        Log.getInstance().setLogDest(this.debugPane);
        initListeners();
        initOthers();
        ((JTextField)$.get("editId2")).setText("haohaohao");
        ((JTextField)$.get("editContent2")).setText("xxxxxxxxxxxxx");
    }
    
    private void initViews()
    {
        this.editServerIp = new JTextField(16);
        this.editServerPort = new JTextField(5);
        this.editServerIp.setForeground(new Color(13, 148, 252));
        this.editServerPort.setForeground(new Color(13, 148, 252));
        this.editServerIp.setText("rbcore.openmob.net");
        this.editServerPort.setText("7901");
        this.btnLogin = new JButton("登陆");
        this.btnLogin.setUI(new BEButtonUI().setNormalColor(BEButtonUI.NormalColor.blue));
        this.btnLogin.setForeground(Color.white);
        this.editLoginName = new JTextField(22);
        this.editLoginPsw = new JPasswordField(22);
        this.btnLogout = new JButton("退出");
        this.viewMyid = new JLabel();
        this.viewMyid.setForeground(new Color(255, 0, 255));
        this.viewMyid.setText("未登陆");
        
        this.btnSend = new JButton("发送消息");
        this.btnSend.setUI(new BEButtonUI().setNormalColor(BEButtonUI.NormalColor.green));
        this.btnSend.setForeground(Color.white);
        this.editId = new JTextField(20);
        
        this.editContent = new JTextField(20);
        
        this.debugPane = new JTextPane();
        this.debugPane.setBackground(Color.black);
        
        this.debugPane.setCaretColor(Color.white);
        
        Log.getInstance().setLogDest(this.debugPane);
        
        this.imInfoPane = new JTextPane();
        
        HardLayoutPane authPanel = new HardLayoutPane();
        JPanel serverInfoPane = new JPanel(new BorderLayout());
        JPanel portInfoPane = new JPanel(new BorderLayout());
        portInfoPane.add(new JLabel("："), "West");
        portInfoPane.add(this.editServerPort, "Center");
        serverInfoPane.add(this.editServerIp, "Center");
        serverInfoPane.add(portInfoPane, "East");
        authPanel.addTo(serverInfoPane, 2, true);
        authPanel.nextLine();
        authPanel.addTo(new JLabel("用户名："), 1, true);
        authPanel.addTo(this.editLoginName, 1, true);
        authPanel.nextLine();
        authPanel.addTo(new JLabel("密  码："), 1, true);
        authPanel.addTo(this.editLoginPsw, 1, true);
        authPanel.nextLine();
        authPanel.addTo(this.btnLogin, 1, true);
        authPanel.addTo(this.btnLogout, 1, true);
        authPanel.nextLine();
        authPanel.addTo(new JLabel("我的id："), 1, true);
        JPanel idAndVerPanel = new JPanel();
        idAndVerPanel.setLayout(new BoxLayout(idAndVerPanel, 2));
        JLabel lbVer = new JLabel("v2.1b151012.1O");
        lbVer.setForeground(new Color(184, 184, 184));
        idAndVerPanel.add(this.viewMyid);
        idAndVerPanel.add(Box.createHorizontalGlue());
        idAndVerPanel.add(lbVer);
        authPanel.addTo(idAndVerPanel, 1, true);
        authPanel.nextLine();
        
        HardLayoutPane toPanel = new HardLayoutPane();
        toPanel.addTo(new JLabel("对方ID号："), 1, true);
        toPanel.addTo(this.editId, 1, true);
        toPanel.nextLine();
        toPanel.addTo(new JLabel("发送内容："), 1, true);
        toPanel.addTo(this.editContent, 1, true);
        toPanel.nextLine();
        toPanel.addTo(this.btnSend, 4, true);
        toPanel.nextLine();
        
        HardLayoutPane oprPanel = new HardLayoutPane();
        oprPanel.addTitledLineSeparator("登陆认证");
        oprPanel.addTo(authPanel, 1, true);
        oprPanel.addTitledLineSeparator("消息发送");
        oprPanel.addTo(toPanel, 1, true);
        oprPanel.addTitledLineSeparator();
        
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(oprPanel, "North");
        JScrollPane imInfoSc = new JScrollPane(this.imInfoPane);
        imInfoSc.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 7, 0, 7), imInfoSc.getBorder()));
        imInfoSc.setHorizontalScrollBarPolicy(31);
        leftPanel.add(imInfoSc, "Center");
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(leftPanel, "West");
        JScrollPane sc = new JScrollPane(this.debugPane);
        sc.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(4, 0, 0, 2), sc.getBorder()));
        sc.setHorizontalScrollBarPolicy(31);
        getContentPane().add(sc, "Center");
        
        setTitle("MobileIMSDK演示工程 - (作者:Jack Jiang, 讨论区:openmob.net, QQ群:215891622)");
        setLocationRelativeTo(null);
        setSize(1000, 700);
    }
    
    public void showToast(String text)
    {
        Toast.showTost(3000, text, new Point((int)getLocationOnScreen().getX() + 50, (int)getLocationOnScreen().getY() + 400));
    }
    
    private void initListeners()
    {
        this.btnLogin.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                String serverIP = MainFrame.this.editServerIp.getText();
                String serverPort = MainFrame.this.editServerPort.getText();
                if ((!(CommonUtils.isStringEmpty(serverIP, true))) && (!(CommonUtils.isStringEmpty(serverPort, true))))
                {
                    net.openmob.mobileimsdk.java.conf.ConfigEntity.serverIP = serverIP.trim();
                    try
                    {
                        net.openmob.mobileimsdk.java.conf.ConfigEntity.serverUDPPort = Integer.parseInt(serverPort.trim());
                    }
                    catch (Exception e2)
                    {
                        MainFrame.this.showToast("请输入合法的端口号！");
                        Log.e(MainFrame.class.getSimpleName(), "请输入合法的端口号！");
                        return;
                    }
                }
                else
                {
                    MainFrame.this.showToast("请确保服务端地址和端口号都不为空！");
                    Log.e(MainFrame.class.getSimpleName(), "请确保服务端地址和端口号都不为空！");
                    return;
                }
                
                if (MainFrame.this.editLoginName.getText().toString().trim().length() > 0)
                {
                    new LocalUDPDataSender.SendLoginDataAsync(MainFrame.this.editLoginName.getText(), MainFrame.this.editLoginPsw.getText())
                    {
                        protected void fireAfterSendLogin(int code)
                        {
                            if (code == 0)
                            {
                                MainFrame.this.showToast("数据发送成功！");
                                Log.i(MainFrame.class.getSimpleName(), "登陆信息已成功发出！");
                            }
                            else
                            {
                                MainFrame.this.showToast("数据发送失败。错误码是：" + code + "！");
                                Log.w(MainFrame.class.getSimpleName(), "数据发送失败。错误码是：" + code + "！");
                            }
                        }
                    }.execute();
                }
                else
                    Log.e(MainFrame.class.getSimpleName(), "登陆名长度=" + MainFrame.this.editLoginName.getText().toString().trim().length());
            }
        });
        this.btnLogout.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                new SwingWorker()
                {
                    protected Integer doInBackground()
                    {
                        int code = LocalUDPDataSender.getInstance().sendLoginout();
                        return Integer.valueOf(code);
                    }
                    
                    protected void done()
                    {
                        int code = -1;
                        try
                        {
                            code = ((Integer)get()).intValue();
                        }
                        catch (Exception e)
                        {
                            Log.w(MainFrame.TAG, e.getMessage());
                        }
                        onPostExecute(Integer.valueOf(code));
                    }
                    
                    protected void onPostExecute(Integer code)
                    {
                        MainFrame.this.setMyid(-1);
                        if (code.intValue() == 0)
                            Log.i(MainFrame.class.getSimpleName(), "注销登陆请求已完成！");
                        else
                            MainFrame.this.showToast("注销登陆请求发送失败。错误码是：" + code + "！");
                    }
                }.execute();
            }
        });
        this.btnSend.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                String msg = MainFrame.this.editContent.getText().toString().trim();
                if (msg.length() > 0)
                {
                    int friendId = Integer.parseInt(MainFrame.this.editId.getText().toString().trim());
                    MainFrame.this.showIMInfo_black("我对" + friendId + "说：" + msg);
                    
                    new LocalUDPDataSender.SendCommonDataAsync(msg, friendId, true)
                    {
                        protected void onPostExecute(Integer code)
                        {
                            if (code.intValue() == 0)
                            {
                                Log.i(MainFrame.class.getSimpleName(), "2数据已成功发出！");
                            }
                            else
                                MainFrame.this.showToast("数据发送失败。错误码是：" + code + "！");
                        }
                    }.execute();
                }
                else
                {
                    Log.e(MainFrame.class.getSimpleName(), "消息内容长度=" + msg.length());
                }
            }
        });
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                ClientCoreSDK.getInstance().release();
                
                System.exit(0);
            }
        });
    }
    
    private void initOthers()
    {
        net.openmob.mobileimsdk.java.conf.ConfigEntity.appKey = "5418023dfd98c579b6001741";
        ClientCoreSDK.getInstance().setChatTransDataEvent(new MyChatTransDataEventImpl().set____temp(this));
        ClientCoreSDK.getInstance().setChatBaseEvent(new MyChatBaseEventImpl().set____temp(this));
        ClientCoreSDK.getInstance().setMessageQoSEvent(new MyMessageQoSEventImpl().set____temp(this));
    }
    
    public void setMyid(int myid)
    {
        this.viewMyid.setText(String.format("当前登陆的用户ID:%d", myid));
    }
    
    public void showIMInfo_black(String txt)
    {
        showIMInfo(new Color(0, 0, 0), txt);
    }
    
    public void showIMInfo_blue(String txt)
    {
        showIMInfo(new Color(0, 0, 255), txt);
    }
    
    public void showIMInfo_brightred(String txt)
    {
        showIMInfo(new Color(255, 0, 255), txt);
    }
    
    public void showIMInfo_red(String txt)
    {
        showIMInfo(new Color(255, 0, 0), txt);
    }
    
    public void showIMInfo_green(String txt)
    {
        showIMInfo(new Color(0, 128, 0), txt);
    }
    
    public void showIMInfo(Color c, String txt)
    {
        try
        {
            Log.append(c, "[" + this.hhmmDataFormat.format(new Date()) + "]" + txt + "\r\n", this.imInfoPane);
            this.imInfoPane.setCaretPosition(this.imInfoPane.getDocument().getLength());
        }
        catch (Exception localException)
        {
        }
    }
    
}
