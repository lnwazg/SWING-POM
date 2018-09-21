package com.lnwazg.bean;

/**
 * 服务器配置信息
 * @author nan.li
 * @version 2015-9-4
 */
public class ServerConfig
{
    /**
     * 服务器IP或者host
     */
    private String server;
    
    /**
     * 服务器端口号
     */
    private String server_port;
    
    /**
     * 服务器密码
     */
    private String password;
    
    /**
     * 验证方式
     */
    private String method;
    
    /**
     * 备注
     */
    private String remarks;
    
    public ServerConfig(String server, String server_port, String password, String method, String remarks)
    {
        this.server = server;
        this.server_port = server_port;
        this.password = password;
        this.method = method;
        this.remarks = remarks;
    }
    
    @Override
    public String toString()
    {
        return "ServerConfig \n[server=" + server + ",\n server_port=" + server_port + ",\n password=" + password + ",\n method=" + method + ",\n remarks=" + remarks + "]";
    }
    
    public String getServer()
    {
        return server;
    }
    
    public void setServer(String server)
    {
        this.server = server;
    }
    
    public String getServer_port()
    {
        return server_port;
    }
    
    public void setServer_port(String server_port)
    {
        this.server_port = server_port;
    }
    
    public String getPassword()
    {
        return password;
    }
    
    public void setPassword(String password)
    {
        this.password = password;
    }
    
    public String getMethod()
    {
        return method;
    }
    
    public void setMethod(String method)
    {
        this.method = method;
    }
    
    public String getRemarks()
    {
        return remarks;
    }
    
    public void setRemarks(String remarks)
    {
        this.remarks = remarks;
    }
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((method == null) ? 0 : method.hashCode());
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + ((server == null) ? 0 : server.hashCode());
        result = prime * result + ((server_port == null) ? 0 : server_port.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ServerConfig other = (ServerConfig)obj;
        if (method == null)
        {
            if (other.method != null)
                return false;
        }
        else if (!method.equals(other.method))
            return false;
        if (password == null)
        {
            if (other.password != null)
                return false;
        }
        else if (!password.equals(other.password))
            return false;
        if (server == null)
        {
            if (other.server != null)
                return false;
        }
        else if (!server.equals(other.server))
            return false;
        if (server_port == null)
        {
            if (other.server_port != null)
                return false;
        }
        else if (!server_port.equals(other.server_port))
            return false;
        return true;
    }
}
