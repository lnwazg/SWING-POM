package com.lnwazg;

import com.lnwazg.kit.log.Logs;
import com.lnwazg.swing.util.uiloader.LocalUiLoader;

public class LocalMain extends LocalUiLoader
{
    public static void main(String[] args)
    {
        new LocalMain();
        Logs.TIMESTAMP_LOG_SWITCH = true;
    }
}