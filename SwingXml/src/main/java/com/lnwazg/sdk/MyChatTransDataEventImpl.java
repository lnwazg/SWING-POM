package com.lnwazg.sdk;

import com.lnwazg.ui.MainFrame;

import net.openmob.mobileimsdk.java.event.ChatTransDataEvent;
import net.openmob.mobileimsdk.java.utils.Log;

public class MyChatTransDataEventImpl implements ChatTransDataEvent
{
    private static final String TAG = MyChatTransDataEventImpl.class.getSimpleName();
    
    private MainFrame ____temp = null;
    
    public void onTransBuffer(String fingerPrintOfProtocal, int dwUserid, String dataContent)
    {
        Log.d(TAG, "【DEBUG_UI】收到来自用户" + dwUserid + "的消息:" + dataContent);
        
        if (this.____temp == null)
        {
            return;
        }
        this.____temp.showToast(dwUserid + "说：" + dataContent);
        this.____temp.showIMInfo_black(dwUserid + "说：" + dataContent);
    }
    
    public MyChatTransDataEventImpl set____temp(MainFrame ____temp)
    {
        this.____temp = ____temp;
        return this;
    }
    
    public MainFrame get____temp()
    {
        return this.____temp;
    }
    
    public void onErrorResponse(int errorCode, String errorMsg)
    {
        Log.d(TAG, "【DEBUG_UI】收到服务端错误消息，errorCode=" + errorCode + ", errorMsg=" + errorMsg);
        this.____temp.showIMInfo_red("Server反馈错误码：" + errorCode + ",errorMsg=" + errorMsg);
    }
}