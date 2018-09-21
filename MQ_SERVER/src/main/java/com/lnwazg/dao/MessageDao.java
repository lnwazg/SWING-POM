package com.lnwazg.dao;

import java.util.List;
import java.util.Map;

import com.lnwazg.dbkit.anno.dao.handletype.Select;
import com.lnwazg.dbkit.jdbc.MyJdbc;
import com.lnwazg.mq.entity.Message;

/**
 * 最优秀的设计，是可以“消灭”DAO层的！<br>
 * 通过精巧的封装，减少DAO层的复杂性！<br>
 * 最妙的设计，是约定大于配置的！即，用最少的核心配置信息，去完成最复杂的业务
 * @author nan.li
 * @version 2016年5月20日
 */
public interface MessageDao extends MyJdbc
{
    /**
     * 按照发送的顺序顺序查询
     * @author nan.li
     * @param map
     * @return
     */
    @Select("select * from MESSAGE where 1=1 and node=#{node} limit ${limit}")
    List<Message> queryByLimit(Map<String, Object> map);
    
    @Select("select * from MESSAGE where 1=1 and node=#{node}")
    List<Message> queryAll(Map<String, Object> map);
    
    @Select("select count(1) from MESSAGE where 1=1 and node=#{node}")
    int queryAvailableMsgNumByNode(Map<String, Object> map);
    
    @Select("select count(1) from MESSAGE where 1=1")
    int queryAvailableMsgNum();
    
    @Select("select node,count(content) count from MESSAGE where 1=1 group by node order by node")
    List<Map<String, Object>> queryAvailableMsgNumGroupByNode();
    
    //    @Select("select * from city_info")
    //    List<CityInfo> queryAllCities();
    //    
    //    @Select("select * from insurance_items")
    //    List<InsuranceItem> queryInsuranceItems();
    //    
    //    @Select("select * from city_info where city_id=#{city_id}")
    //    CityInfo queryOneCity(@Param("city_id") String city_id);
    //    
    //    @Select("select * from city_info where city_id=#{city_id}")
    //    CityInfo queryOneCity(Map<String, Object> paramMap);
    //    
    //    @Select("select count(1) from city_info")
    //    int queryCityCount();
    //    
    //    @Select("select * from insurance_items limit ${start},${limit}")
    //    List<InsuranceItem> queryInsuranceItemsByPage(@Param("start") String start, @Param("limit") String limit);
    //    
    //    @Select("select * from insurance_items limit ${start},${limit}")
    //    List<InsuranceItem> queryInsuranceItemsByPage(Map<String, Object> paramMap);
    //    
    //    @Select("select * from insurance_order")
    //    List<InsuranceOrder> queryOrderInfos();
    //    
    //    @Select("select count(1) from insurance_order")
    //    int queryOrderInfosCount();
    //    
    //    @Select("select count(1) from test")
    //    int queryTestCount();
    //    
    //    @Update("update test set aaaa=#{aaaa} where id=#{id}")
    //    int updateTest(Map<String, Object> paramMap);
}
