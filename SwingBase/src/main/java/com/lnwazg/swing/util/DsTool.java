package com.lnwazg.swing.util;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import javax.swing.JLabel;
import javax.swing.JTextPane;

import org.apache.commons.lang3.StringUtils;

import com.lnwazg.dbkit.jdbc.MyJdbc;
import com.lnwazg.dbkit.tools.dbcache.tablemap.DBConfigHelper;
import com.lnwazg.dbkit.utils.DbKit;
import com.lnwazg.kit.executor.ExecMgr;
import com.lnwazg.kit.log.Logs;
import com.lnwazg.kit.singleton.B;

/**
 * 数据源工具类<br>
 * 既可以从本地获取数据，也可以从远程数据库获取数据
 * @author nan.li
 * @version 2018年3月6日
 */
public class DsTool
{
    /**
     * 仅供同步线程使用
     */
    private static List<Runnable> runnables = new CopyOnWriteArrayList<>();
    
    /**
     * 写缓存<br>
     * 不会立即写入，而是在下一次同步更新前先写入！
     */
    static Map<String, String> writeCache = new ConcurrentHashMap<>();
    
    /**
     * 是否联网工作模式<br>
     * 默认为离线模式
     */
    public static boolean networkMode = false;
    
    static MyJdbc myJdbc = null;
    
    /**
     * 存入的间隔秒数
     */
    private static final int PUSH_INVERVAL_SECONDS = 10;
    
    /**
     * 拉取最新的间隔秒数
     */
    private static final int PULL_INVERVAL_SECONDS = 60 * 5;
    
    /**
     * 网络数据同步
     * @author nan.li
     */
    public static void initSync()
    {
        if (networkMode)
        {
            Logs.i("网络模式，开启同步Daemon线程...");
            //push线程
            new Thread(
                () -> {
                    while (true)
                    {
                        try
                        {
                            Logs.i("push sleep...");
                            TimeUnit.SECONDS.sleep(PUSH_INVERVAL_SECONDS);
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                        if (networkMode)
                        {
                            if (writeCache.size() > 0)
                            {
                                Logs.i("push...");
                                Logs.i(writeCache);
                                try
                                {
                                    if (myJdbc == null)
                                    {
                                        myJdbc = DbKit.getDefaultJdbc();
                                        B.s(MyJdbc.class, myJdbc);
                                    }
                                    DBConfigHelper dbConfigHelper = B.g(DBConfigHelper.class);
                                    for (String key : writeCache.keySet())
                                    {
                                        dbConfigHelper.set(key, writeCache.get(key));
                                    }
                                    writeCache.clear();
                                }
                                catch (Exception e)
                                {
                                    //数据库操作异常了，那就不清除数据
                                    Logs.e("数据库连接异常，下次再存..." + e.getMessage());
                                }
                            }
                        }
                    }
                }).start();
                
            //pull线程
            new Thread(
                () -> {
                    while (true)
                    {
                        try
                        {
                            Logs.i("pull sleep...");
                            TimeUnit.SECONDS.sleep(PULL_INVERVAL_SECONDS);
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                        if (networkMode)
                        {
                            Logs.i("check write cache...");
                            if (writeCache.size() > 0)
                            {
                                Logs.i("writeCache not empty! Write...");
                                Logs.i(writeCache);
                                try
                                {
                                    if (myJdbc == null)
                                    {
                                        myJdbc = DbKit.getDefaultJdbc();
                                        B.s(MyJdbc.class, myJdbc);
                                    }
                                    DBConfigHelper dbConfigHelper = B.g(DBConfigHelper.class);
                                    for (String key : writeCache.keySet())
                                    {
                                        dbConfigHelper.set(key, writeCache.get(key));
                                        
                                    }
                                    writeCache.clear();
                                    Logs.i("writeCache sync and cleared!");
                                }
                                catch (Exception e)
                                {
                                    //数据库操作异常了，那就不清除数据
                                    Logs.e("数据库连接异常，下次再存..." + e.getMessage());
                                }
                            }
                            else
                            {
                                Logs.i("no writecache to sync");
                            }
                            Logs.i("pull...");
                            for (Runnable runnable : runnables)
                            {
                                ExecMgr.cachedExec.execute(runnable);
                            }
                        }
                    }
                }).start();
        }
        else
        {
            Logs.i("本地模式，忽略网络同步线程");
        }
    }
    
    /**
     * 存数据
     * @author nan.li
     * @param key
     * @param value
     */
    public static void saveDataToDs(String key, String value)
    {
        if (networkMode)
        {
            //网络模式
            //先放入高速写缓存中，随后由同步线程去定期存入，以免降低前台性能
            writeCache.put(key, value);
        }
        else
        {
            //本地模式，直接存入
            WinMgr.setConfig(key, value);
        }
    }
    
    /**
     * 取数据，并按需同步（联网则同步，不联网不同步）
     * @author nan.li
     * @param key
     * @param dataNullShowFix
     * @param component
     */
    public static void getAndSyncDataFromDs(String key, String dataNullShowFix, Object component)
    {
        Runnable runnable = () -> {
            ExecMgr.guiExec.execute(() -> {
                try
                {
                    if (component instanceof JTextPane)
                    {
                        ((JTextPane)component).setText(getFixDataFromDs(key, dataNullShowFix));
                    }
                    else if (component instanceof JLabel)
                    {
                        ((JLabel)component).setText(getFixDataFromDs(key, dataNullShowFix));
                    }
                }
                catch (Exception e)
                {
                    //数据库操作异常了，那就不取数据了
                    Logs.e("数据库连接异常，下次再取..." + e.getMessage());
                }
            });
        };
        //先运行一次
        ExecMgr.singleExec.execute(runnable);
        
        //然后加入轮询列表中，若网络模式，则会定期再次拉取；否则，本地模式会直接忽略，因为同步线程在本地模式会不执行
        runnables.add(runnable);
    }
    
    /**
     * 从数据源获取数据，启用替补值（若替补值可用）
     * @author nan.li
     * @param key
     * @param dataNullShowFix
     * @return
     * @throws SQLException 
     */
    public static String getFixDataFromDs(String key, String dataNullShowFix)
        throws SQLException
    {
        String value = null;
        if (networkMode)
        {
            if (myJdbc == null)
            {
                myJdbc = DbKit.getDefaultJdbc();
                B.s(MyJdbc.class, myJdbc);
            }
            DBConfigHelper dbConfigHelper = B.g(DBConfigHelper.class);
            value = dbConfigHelper.getAsString(key);
        }
        else
        {
            value = WinMgr.getConfig(key);
        }
        if (StringUtils.isEmpty(value) && StringUtils.isNotEmpty(dataNullShowFix))
        {
            //若空，且替补值非空，则采用替补值
            value = dataNullShowFix;
        }
        return value;
    }
}
