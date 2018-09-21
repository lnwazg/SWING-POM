package com.lnwazg;

import com.lnwazg.swing.util.uiloader.LocalUiLoader;

/**
 * 用maven导出的互相依赖的项目，每次皆须从根POM开始构建，方能构建出最新的代码！
 * @version 2015-9-1
 */
public class LocalMain extends LocalUiLoader
{
    public static void main(String[] args)
    {
        new LocalMain();
    }
}