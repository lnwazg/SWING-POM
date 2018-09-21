package com.lnwazg.bean;

import java.util.Date;

/**
 * 数据节点的信息对象
 * @author nan.li
 * @version 2017年7月19日
 */
public class DateNodeInfo
{
    /**
     * 最后一次心跳时间
     */
    private Date lastHeartbeatTime;
    
    /**
     * 最后一次心跳的描述备注信息
     */
    private String lastHeartbeatRemark;
    
    /**
     * 是否正常在线
     */
    private boolean isAlive = true;
    
    /**
     * 状态文本
     */
    private String status;
    
    public boolean isAlive()
    {
        return isAlive;
    }
    
    public DateNodeInfo setAlive(boolean isAlive)
    {
        this.isAlive = isAlive;
        return this;
    }
    
    public String getStatus()
    {
        return status;
    }
    
    public DateNodeInfo setStatus(String status)
    {
        this.status = status;
        return this;
    }
    
    public Date getLastHeartbeatTime()
    {
        return lastHeartbeatTime;
    }
    
    public DateNodeInfo setLastHeartbeatTime(Date lastHeartbeatTime)
    {
        this.lastHeartbeatTime = lastHeartbeatTime;
        return this;
    }
    
    public String getLastHeartbeatRemark()
    {
        return lastHeartbeatRemark;
    }
    
    public DateNodeInfo setLastHeartbeatRemark(String lastHeartbeatRemark)
    {
        this.lastHeartbeatRemark = lastHeartbeatRemark;
        return this;
    }
    
}
