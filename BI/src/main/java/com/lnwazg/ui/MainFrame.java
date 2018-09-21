package com.lnwazg.ui;

import java.io.File;
import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import org.apache.commons.lang3.StringUtils;

import com.lnwazg.kit.executor.ExecMgr;
import com.lnwazg.kit.log.Logs;
import com.lnwazg.kit.swing.JOptionPaneHelper;
import com.lnwazg.kit.swing.SwingUtils;
import com.lnwazg.kit.swing.ui.comp.SmartButton;
import com.lnwazg.swing.util.WinMgr;
import com.lnwazg.swing.xmlbuilder.XmlJFrame;
import com.lnwazg.swing.xmlbuilder.anno.XmlBuild;
import com.lnwazg.tool.Processor;

@XmlBuild("Frame.xml")
public class MainFrame extends XmlJFrame
{
    private static final long serialVersionUID = 911994927255462705L;
    
    private SmartButton chooseFile;
    
    private SmartButton again;
    
    private JLabel lastFileLabel;
    
    File lastFileDirectory = null;
    
    private File lastFile;
    
    private JTextPane logScreen;
    
    private SmartButton clearLog;
    
    JScrollPane logScroller;
    
    @Override
    public void afterUIBind()
    {
        logScreen.setContentType("text/html");
        Logs.addLogDest(logScreen);
        
        String lastFileDirPath = WinMgr.getConfig("lastFileDirectory");
        if (StringUtils.isNotEmpty(lastFileDirPath))
        {
            lastFileDirectory = new File(lastFileDirPath);
        }
        String lastFilePath = WinMgr.getConfig("lastFile");
        if (StringUtils.isNotEmpty(lastFilePath))
        {
            lastFile = new File(lastFilePath);
            try
            {
                lastFileLabel.setText(lastFile.getCanonicalPath());
            }
            catch (IOException e1)
            {
                e1.printStackTrace();
            }
        }
        chooseFile.addActionListener((e) -> {
            File file = SwingUtils.chooseFile(WinMgr.win(MainFrame.class), "请选择BI配置文件（*.json）", lastFileDirectory, "json");
            if (file == null)
            {
                JOptionPaneHelper.showMessageDialog(WinMgr.win(MainFrame.class), "文件格式不对！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            lastFileDirectory = file.getParentFile();
            lastFile = file;
            try
            {
                WinMgr.saveConfig("lastFileDirectory", lastFileDirectory.getCanonicalPath());
                WinMgr.saveConfig("lastFile", lastFile.getCanonicalPath());
                try
                {
                    lastFileLabel.setText(lastFile.getCanonicalPath());
                }
                catch (IOException e1)
                {
                    e1.printStackTrace();
                }
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
            //所有的界面操作都不宜处理耗时过长的任务，都应该立即响应。而真正耗时的任务都应该在线程中去处理，这样界面的响应就可以极快！
            ExecMgr.singleExec.execute(() -> {
                Processor.handle(file);
                ExecMgr.guiExec.execute(() -> {
                    JOptionPaneHelper.showMessageDialog(WinMgr.win(MainFrame.class), "处理完毕！");
                });
            });
        });
        again.addActionListener((e) -> {
            if (lastFile == null)
            {
                JOptionPaneHelper.showMessageDialog(WinMgr.win(MainFrame.class), "无上次处理文件！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            ExecMgr.singleExec.execute(() -> {
                Processor.handle(lastFile);
                ExecMgr.guiExec.execute(() -> {
                    JOptionPaneHelper.showMessageDialog(WinMgr.win(MainFrame.class), "处理完毕！");
                });
            });
        });
        
        clearLog.addActionListener(e -> {
            ExecMgr.guiExec.execute(() -> {
                logScreen.setText(null);
            });
        });
    }
}
