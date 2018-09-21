package com.lnwazg.bean;

public class NoteRecord
{
    String content;
    
    String _key;
    
    public String get_key()
    {
        return _key;
    }
    
    public NoteRecord set_key(String _key)
    {
        this._key = _key;
        return this;
    }
    
    public String getContent()
    {
        return content;
    }
    
    public NoteRecord setContent(String content)
    {
        this.content = content;
        return this;
    }
}
