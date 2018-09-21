package com.lnwazg.sdk;

import java.util.ArrayList;

import com.lnwazg.ui.MainFrame;

import net.openmob.mobileimsdk.java.event.MessageQoSEvent;
import net.openmob.mobileimsdk.java.utils.Log;
import net.openmob.mobileimsdk.server.protocal.Protocal;

public class MyMessageQoSEventImpl implements MessageQoSEvent
{
    private static final String TAG = MyMessageQoSEventImpl.class.getSimpleName();
    
    private MainFrame ____temp = null;
    
    public void messagesLost(ArrayList<Protocal> lostMessages)
    {
        Log.d(TAG, "【DEBUG_UI】收到系统的未实时送达事件通知，当前共有" + lostMessages.size() + "个包QoS保证机制结束，判定为【无法实时送达】！");
        
        if (this.____temp == null)
            return;
        this.____temp.showIMInfo_brightred("[消息未成功送达]共" + lostMessages.size() + "条!(网络状况不佳或对方id不存在)");
    }
    
    public void messagesBeReceived(String theFingerPrint)
    {
        if (theFingerPrint == null)
            return;
        Log.d(TAG, "【DEBUG_UI】收到对方已收到消息事件的通知，fp=" + theFingerPrint);
        
        if (this.____temp == null)
            return;
        this.____temp.showIMInfo_blue("[收到对方消息应答]fp=" + theFingerPrint);
    }
    
    public MyMessageQoSEventImpl set____temp(MainFrame ____temp)
    {
        this.____temp = ____temp;
        return this;
    }
    
    public MainFrame get____temp()
    {
        return this.____temp;
    }
}