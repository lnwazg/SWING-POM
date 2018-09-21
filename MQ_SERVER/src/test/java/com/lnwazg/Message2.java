package com.lnwazg;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.lnwazg.dbkit.anno.entity.AutoIncrement;
import com.lnwazg.dbkit.anno.entity.Id;
import com.lnwazg.dbkit.anno.entity.Index;

/**
 * MQ服务访问日志
 * @author Administrator
 * @version 2016年7月31日
 */
public class Message2
{
    @Id
    @AutoIncrement
    @Index(multiple = true)
    public Integer id;
    
    @Index
    private String node;
    
    private String content;
    
    @Index(multiple = true)
    private Date createTime;
    
    //是否已经发送
    private Boolean sent;
    
    private Boolean deleted;
    
    public Integer getId()
    {
        return id;
    }
    
    public Message2 setId(Integer id)
    {
        this.id = id;
        return this;
    }
    
    public String getNode()
    {
        return node;
    }
    
    public Message2 setNode(String node)
    {
        this.node = node;
        return this;
    }
    
    public String getContent()
    {
        return content;
    }
    
    public Message2 setContent(String content)
    {
        this.content = content;
        return this;
    }
    
    public Date getCreateTime()
    {
        return createTime;
    }
    
    public Message2 setCreateTime(Date createTime)
    {
        this.createTime = createTime;
        return this;
    }
    
    public Boolean getSent()
    {
        return sent;
    }
    
    public Message2 setSent(Boolean sent)
    {
        this.sent = sent;
        return this;
    }
    
    public Boolean getDeleted()
    {
        return deleted;
    }
    
    public Message2 setDeleted(Boolean deleted)
    {
        this.deleted = deleted;
        return this;
    }
    
    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
    
}
