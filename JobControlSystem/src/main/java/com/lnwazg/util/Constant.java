package com.lnwazg.util;

public class Constant
{
    public static final String JobControlSystem = "JobControlSystem";
    
    /**
     * 移动硬盘的盘符路径文件
     */
    public static final String REMOVEABLE_HARD_DISK_DRIVE_DIR = "O:/";
    
    /**
     * 行程管理的基础文件夹路径，优先读取本地硬盘的数据
     */
    public static final String TASK_REMIND_BASEPATH_1 = "E:/2012";
    
    /**
     * 行程管理的基础文件夹路径，本地硬盘里的数据不存在，才failOver尝试读取移动硬盘里面的数据
     */
    public static final String TASK_REMIND_BASEPATH_2 = "O:/2012";
    
    /**
     * svn同步的文件夹
     */
    public static final String SVN_WORK_BASEPATH = TASK_REMIND_BASEPATH_1;
    
    /**
     * 日志文件的基础目录，是动态拼接的<br>
     * 例如： O:/2012/d心理碎片trunk
     */
    public static String DIARY_BASE_PATH;
    
}
