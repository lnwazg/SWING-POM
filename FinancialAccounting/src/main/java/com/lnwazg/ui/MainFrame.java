package com.lnwazg.ui;

import java.io.File;
import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;

import com.lnwazg.kit.swing.JOptionPaneHelper;
import com.lnwazg.kit.swing.SwingUtils;
import com.lnwazg.kit.swing.ui.comp.SmartButton;
import com.lnwazg.swing.util.WinMgr;
import com.lnwazg.swing.xmlbuilder.XmlJFrame;
import com.lnwazg.swing.xmlbuilder.anno.XmlBuild;
import com.lnwazg.util.FinanceProcessor;

@XmlBuild("Frame.xml")
public class MainFrame extends XmlJFrame
{
    private static final long serialVersionUID = 911994927255462705L;
    
    private SmartButton chooseFile;
    
    private SmartButton again;
    
    private JLabel lastFileLabel;
    
    File lastFileDirectory = null;
    
    private File lastFile;
    
    @Override
    public void afterUIBind()
    {
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
            File file = SwingUtils.chooseFile(WinMgr.win(MainFrame.class), "请选择记账文件（*.txt）", lastFileDirectory, "txt");
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
            FinanceProcessor.handle(file);
            JOptionPaneHelper.showMessageDialog(WinMgr.win(MainFrame.class), "账务处理完毕！");
        });
        again.addActionListener((e) -> {
            if (lastFile == null)
            {
                JOptionPaneHelper.showMessageDialog(WinMgr.win(MainFrame.class), "无上次处理文件！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            FinanceProcessor.handle(lastFile);
            JOptionPaneHelper.showMessageDialog(WinMgr.win(MainFrame.class), "账务处理完毕！");
        });
    }
}
