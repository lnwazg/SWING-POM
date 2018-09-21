package com.lnwazg.bean;

import java.util.List;

/**
 * 百度记事本对象
 * @author nan.li
 * @version 2018年1月11日
 */
public class NoteBook
{
    int count;
    
    List<NoteRecord> records;
    
    long request_id;
    
    public int getCount()
    {
        return count;
    }
    
    public NoteBook setCount(int count)
    {
        this.count = count;
        return this;
    }
    
    public List<NoteRecord> getRecords()
    {
        return records;
    }
    
    public NoteBook setRecords(List<NoteRecord> records)
    {
        this.records = records;
        return this;
    }
    
    public long getRequest_id()
    {
        return request_id;
    }
    
    public NoteBook setRequest_id(long request_id)
    {
        this.request_id = request_id;
        return this;
    }
}
