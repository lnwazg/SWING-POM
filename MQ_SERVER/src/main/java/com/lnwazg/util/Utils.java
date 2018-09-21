package com.lnwazg.util;

import com.lnwazg.kit.executor.ExecMgr;
import com.lnwazg.kit.singleton.B;
import com.lnwazg.kit.singleton.BeanMgr;
import com.lnwazg.ui.MainFrame;

public class Utils
{
    public static void showInLogScreen(String text)
    {
        if (B.q(MainFrame.class) != null)
        {
            ExecMgr.guiExec.execute(() -> {
                if (BeanMgr.get(MainFrame.class).logScreen.getText().length() >= 10000)
                {
                    BeanMgr.get(MainFrame.class).logScreen.setText("");
                }
                BeanMgr.get(MainFrame.class).logScreen.append(String.format("%s\n", text));
                BeanMgr.get(MainFrame.class).logScreen.setCaretPosition(BeanMgr.get(MainFrame.class).logScreen.getText().length() - 1);
            });
        }
    }
}
