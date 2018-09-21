package com.lnwazg.ui;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lnwazg.kit.executor.ExecMgr;
import com.lnwazg.kit.swing.ui.comp.SmartButton;
import com.lnwazg.kit.validate.Validates;
import com.lnwazg.mq.api.MqRequest;
import com.lnwazg.mq.api.MqRequest.ResultCallback;
import com.lnwazg.mq.entity.Message;
import com.lnwazg.mq.framework.MQFramework;
import com.lnwazg.mq.util.MQHelper;
import com.lnwazg.swing.util.WinMgr;
import com.lnwazg.swing.xmlbuilder.XmlJFrame;
import com.lnwazg.swing.xmlbuilder.anno.XmlBuild;

/**
 * 主窗口<br>
 * 这种异步回调的设计模式真是神器！<br>
 * 终于意识到，压力测试是很有必要的！<br>
 * 因为性能问题往往在压力测试下才能暴露！<br>
 * 性能问题也是一个很重要的问题！<br>
 * 已经支持后台异步通讯！已经实现中国最好用的异步通讯框架！<br>
 * 不经意间，竟然开发出了属于自己的异步通讯框架！<br>
 * 真的是太棒了呢！<br>
 * 异步通讯的特点是，即使所有客户端全部关机了，也可以可靠通讯！
 * @author nan.li
 * @version 2016年8月1日
 */
@XmlBuild("MQ_CLIENT.xml")
public class MainFrame extends XmlJFrame
{
    private static final long serialVersionUID = 416736654918898426L;
    
    /**
     * 默认的服务器地址
     */
    private String DEFAULT_SERVER_ADDR = "127.0.0.1";
    
    /**
     * 默认的服务器端口号
     */
    private int DEFAULT_PORT = 11111;
    
    private JTabbedPane tabPane;
    
    private JTextField sm_node_text;
    
    private JTextField sm_content_text;
    
    private SmartButton sm_btn10000;
    
    private SmartButton sm_btn10000_2;
    
    private SmartButton sm_btn;
    
    private JTextField rm_node_text;
    
    private JTextField rm_num_text;
    
    private SmartButton rm_btn;
    
    private JTextArea sta_text;
    
    private JTextField set_server_addr_text;
    
    private JTextField set_server_port_text;
    
    private SmartButton set_btn;
    
    private JCheckBox cluster_switch_checkbox;
    
    private JTextArea cluster_config_info_text;
    
    private String currentServerAddr = DEFAULT_SERVER_ADDR;
    
    private int currentPort = DEFAULT_PORT;
    
    private boolean clusterSwitch = false;//默认关闭这个集群开关
    
    private String clusterConfigInfo;
    
    @Override
    public void afterUIBind()
    {
        initEnv();
        initListeners();
    }
    
    private void initEnv()
    {
        //首先尝试从配置中加载信息到本地
        if (StringUtils.isNotEmpty(WinMgr.getConfig("currentServerAddr")) && StringUtils.isNotEmpty(WinMgr.getConfig("currentPort")))
        {
            currentServerAddr = WinMgr.getConfig("currentServerAddr");
            currentPort = Integer.valueOf(WinMgr.getConfig("currentPort"));
        }
        if (StringUtils.isNotEmpty(WinMgr.getConfig("clusterSwitch")))
        {
            clusterSwitch = Boolean.valueOf(WinMgr.getConfig("clusterSwitch"));
        }
        if (StringUtils.isNotEmpty(WinMgr.getConfig("clusterConfigInfo")))
        {
            clusterConfigInfo = WinMgr.getConfig("clusterConfigInfo");
        }
        
        //然后先填充到ui的输入框中
        ExecMgr.guiExec.execute(() -> {
            set_server_addr_text.setText(currentServerAddr);
            set_server_port_text.setText(currentPort + "");
            cluster_switch_checkbox.setSelected(clusterSwitch);
            cluster_config_info_text.setText(clusterConfigInfo);
        });
        //最后预先启动相应的服务
        initMqClient();
    }
    
    private void initMqClient()
    {
        if (clusterSwitch)
        {
            //如果集群打开了，就启用集群的配置
            MqRequest.setClusterConfigInfo(clusterConfigInfo);
            MqRequest.initClusterConfigInfo();
        }
        else
        {
            //启用单机的配置
            MqRequest.setSingletonServerAddrAndPort(currentServerAddr, currentPort);
            MqRequest.initSingletonServerAddrAndPort();
        }
        
        //                initSub("#1");
        //                initSub("#2");
        //                initSub("#3");
        //        initAll();
        
        //        MQFramework.myselfAddress = "Tom";//指定你的地址
        //        MQFramework.eachTimeLimit = 1000;
        //        MQFramework.pullIntervalSeconds = 5;
        //        MQFramework.initMqController("Tom");//指定你要监听的目标地址
        
        String targetNode = "demoWeb";
        String meNode = "ccc";
        MQFramework.myselfAddress = meNode;//指定你的地址
        ExecMgr.scheduledExec.scheduleAtFixedRate(new Runnable()
        {
            public void run()
            {
                //                MQUtils.sendAsyncMsg(targetNode, "/inbox/readMessage", "message", RandomUtils.nextInt(1, 100) + "");
                for (int i = 0; i < 10000; i++)
                {
                    MQHelper.sendAsyncMsg(targetNode, "/inbox/choujiang", "buyNums", generateBuyNums());
                }
            }
            
        }, 0, 1, TimeUnit.SECONDS);
    }
    
    private String generateBuyNums()
    {
        //随机买1到3000注
        int buyCount = RandomUtils.nextInt(2994, 2995);
        //1-5998元
        List<Integer> list = new LinkedList<>();
        for (int i = 0; i < 5998; i++)
        {
            list.add(i + 1);
        }
        List<Integer> ll = new LinkedList<>();
        for (int i = 0; i < buyCount; i++)
        {
            ll.add(list.remove(RandomUtils.nextInt(0, list.size())));
        }
        return StringUtils.join(ll.toArray(new Integer[ll.size()]), ",");
    }
    
    private void initAll()
    {
        MQFramework.myselfAddress = "#All";
        MQFramework.eachTimeLimit = 1000;
        MQFramework.pullIntervalSeconds = 10;
        //        MQFramework.initMqController("Tom", "Jerry");
        MQFramework.initMqController("#All");
    }
    
    private void initSub(String meNode)
    {
        MQFramework.myselfAddress = meNode;//指定我自己的邮箱地址
        ExecMgr.scheduledExec.scheduleAtFixedRate(new Runnable()
        {
            public void run()
            {
                MQHelper.sendAsyncMsg("#All", "/stat/count", meNode, RandomUtils.nextInt(1, 100) + "");
            }
        }, 0, 5, TimeUnit.SECONDS);
    }
    
    private void initListeners()
    {
        set_btn.addActionListener(e -> {
            //数据检测
            if (StringUtils.isBlank(set_server_addr_text.getText()) || StringUtils.isBlank(set_server_port_text.getText()))
            {
                JOptionPane.showMessageDialog(this, "地址和端口号均不能为空！");
                return;
            }
            if (StringUtils.isNotBlank(set_server_port_text.getText()))
            {
                if (!Validates.isInteger(set_server_port_text.getText().trim()))
                {
                    JOptionPane.showMessageDialog(this, "端口号必须是整数！");
                    return;
                }
            }
            if (cluster_switch_checkbox.isSelected() && StringUtils.isBlank(cluster_config_info_text.getText()))
            {
                JOptionPane.showMessageDialog(this, "您打开了集群开关，因此集群配置信息不能为空！");
                return;
            }
            
            ExecMgr.cachedExec.execute(() -> {
                //首先从界面上取数
                currentServerAddr = set_server_addr_text.getText().trim();
                currentPort = Integer.valueOf(set_server_port_text.getText().trim());
                clusterSwitch = cluster_switch_checkbox.isSelected();
                clusterConfigInfo = cluster_config_info_text.getText().trim();
                
                //存到配置文件中
                WinMgr.setConfig("currentServerAddr", currentServerAddr);
                WinMgr.setConfig("currentPort", currentPort + "");
                WinMgr.setConfig("clusterSwitch", String.valueOf(clusterSwitch));
                WinMgr.setConfig("clusterConfigInfo", clusterConfigInfo);
                
                //更新客户端状态
                initMqClient();
                
                //消息通知
                JOptionPane.showMessageDialog(this, "设置成功！");
            });
        });
        //发送消息
        sm_btn.addActionListener(e -> {
            //数据检测
            if (StringUtils.isBlank(sm_node_text.getText()) || StringUtils.isBlank(sm_content_text.getText()))
            {
                JOptionPane.showMessageDialog(this, "节点和内容不能为空！");
                return;
            }
            //消息发送
            ExecMgr.cachedExec.execute(() -> {
                new MqRequest("SendMessage").addParam("message", sm_content_text.getText().trim()).addParam("node", sm_node_text.getText().trim()).sendAsync(r -> {
                    if (r.isOk())
                    {
                        JOptionPane.showMessageDialog(this, "消息发送成功！");
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(this, "消息发送失败！");
                    }
                });
            });
        });
        
        sm_btn10000.addActionListener(e -> {
            //数据检测
            if (StringUtils.isBlank(sm_node_text.getText()) || StringUtils.isBlank(sm_content_text.getText()))
            {
                JOptionPane.showMessageDialog(this, "节点和内容不能为空！");
                return;
            }
            //消息发送
            ExecMgr.cachedExec.execute(() -> {
                List<Message> list = new ArrayList<>();
                for (int i = 0; i < 10000; i++)
                {
                    list.add(new Message().setNode(sm_node_text.getText().trim()).setContent(sm_content_text.getText().trim()));
                }
                new MqRequest("BatchSendMessage").addParam("msgs", list).sendAsync();
                JOptionPane.showMessageDialog(this, "1w条消息已加入发送队列！");
            });
        });
        
        sm_btn10000_2.addActionListener(e -> {
            //数据检测
            if (StringUtils.isBlank(sm_node_text.getText()) || StringUtils.isBlank(sm_content_text.getText()))
            {
                JOptionPane.showMessageDialog(this, "节点和内容不能为空！");
                return;
            }
            //消息发送
            ExecMgr.cachedExec.execute(() -> {
                for (int i = 0; i < 1000000; i++)
                {
                    new MqRequest("SendMessage").addParam("message", sm_content_text.getText().trim()).addParam("node", sm_node_text.getText().trim()).sendAsync();
                }
                JOptionPane.showMessageDialog(this, "100w条消息已加入发送队列(消息智能合并)！");
            });
        });
        
        //接收消息
        rm_btn.addActionListener(e -> {
            //数据检测
            if (StringUtils.isBlank(rm_node_text.getText()))
            {
                JOptionPane.showMessageDialog(this, "节点不能为空！");
                return;
            }
            if (StringUtils.isNotBlank(rm_num_text.getText()))
            {
                if (!Validates.isInteger(rm_num_text.getText().trim()))
                {
                    JOptionPane.showMessageDialog(this, "数量必须是整数！");
                    return;
                }
            }
            ExecMgr.cachedExec.execute(() -> {
                ResultCallback callback = response -> {
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();//输出的时候，进行格式美化
                    if (response.isOk())
                    {
                        JOptionPane.showMessageDialog(this, "接收消息成功！" + gson.toJson(response.getContent()));
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(this, "接收消息失败！");
                    }
                };
                //消息发送
                if (StringUtils.isNotBlank(rm_num_text.getText()))
                {
                    //不为空，则发送limit字段。因为limit字段是可选的，所以并不一定要发送！
                    new MqRequest("ReceiveMessage").addParam("node", rm_node_text.getText().trim()).addParam("limit", rm_num_text.getText().trim()).sendAsync(callback);
                }
                else
                {
                    new MqRequest("ReceiveMessage").addParam("node", rm_node_text.getText().trim()).sendAsync(callback);
                }
            });
        });
        
        //tab切换到最后一个的时候，取出相应的数据
        tabPane.addChangeListener(e -> {
            ExecMgr.cachedExec.execute(() -> {
                int selectedIndex = ((JTabbedPane)e.getSource()).getSelectedIndex();
                switch (selectedIndex)
                {
                    case 2:
                        //选中了最后一个标签
                        //主动计算一次统计数据，并显示到对应的文本区内
                        new MqRequest("StatisticsAll").sendAsync(response -> {
                            String text = "";//统计的结果
                            if (response.isOk())
                            {
                                StringBuilder sBuilder = new StringBuilder();
                                String allCount = response.getAsString("allCount");
                                sBuilder.append("总数：").append(allCount).append("\n\n");
                                JsonArray jsonArray = response.getAsJsonArray("detailList");
                                //                        sBuilder.append("\t明细：").append("\n");
                                for (int i = 0; i < jsonArray.size(); i++)
                                {
                                    JsonObject jo = jsonArray.get(i).getAsJsonObject();
                                    String node = jo.get("node").getAsString();
                                    String count = jo.get("count").getAsString();
                                    sBuilder.append("节点：").append(node).append(" 数量：").append(count).append("\n");
                                }
                                text = sBuilder.toString();
                            }
                            else
                            {
                                text = "通讯出错";
                            }
                            String showTxt = text;
                            ExecMgr.guiExec.execute(() -> {
                                sta_text.setText(showTxt);
                                sta_text.setCaretPosition(0);
                            });
                        });
                        break;
                    default:
                        break;
                }
            });
        });
    }
}
