package com.lnwazg.sdk;

import com.lnwazg.ui.MainFrame;

import net.openmob.mobileimsdk.java.event.ChatBaseEvent;
import net.openmob.mobileimsdk.java.utils.Log;

public class MyChatBaseEventImpl implements ChatBaseEvent
{
    private static final String TAG = MyChatBaseEventImpl.class.getSimpleName();
    
    private MainFrame ____temp = null;
    
    public void onLoginMessage(int dwUserId, int dwErrorCode)
    {
        if (dwErrorCode == 0)
        {
            Log.p(TAG, "【DEBUG_UI】登录成功，当前分配的user_id=！" + dwUserId);
            
            if (this.____temp == null)
                return;
            this.____temp.setMyid(dwUserId);
            this.____temp.showIMInfo_green("登录成功,id=" + dwUserId);
        }
        else
        {
            Log.e(TAG, "【DEBUG_UI】登录失败，错误代码：" + dwErrorCode);
            this.____temp.showIMInfo_red("登录失败,code=" + dwErrorCode);
        }
    }
    
    public void onLinkCloseMessage(int dwErrorCode)
    {
        Log.e(TAG, "【DEBUG_UI】网络连接出错关闭了，error：" + dwErrorCode);
        this.____temp.showIMInfo_red("服务器连接已断开,error=" + dwErrorCode);
    }
    
    public MyChatBaseEventImpl set____temp(MainFrame ____temp)
    {
        this.____temp = ____temp;
        return this;
    }
    
    public MainFrame get____temp()
    {
        return this.____temp;
    }
}