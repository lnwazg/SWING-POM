package com.lnwazg.mh;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.lnwazg.dbkit.jdbc.MyJdbc;
import com.lnwazg.kit.log.Logs;
import com.lnwazg.kit.servlet.HttpServletResponseCode;
import com.lnwazg.kit.singleton.BeanMgr;
import com.lnwazg.mh.anno.Param;
import com.lnwazg.mh.spi.IFunction;
import com.lnwazg.mh.util.MQConstants;
import com.lnwazg.mh.util.MQUtils;
import com.lnwazg.ws.sim.WsBusinessException;

/**
 * 消息处理器
 * @author Administrator
 * @version 2016年7月29日
 */
public class MessageHandler
{
    /**
     * 批量提交接口<br>
     * 这是一种系统服务，不可被嵌套调用！<br>
     * 即，这种接口内部不允许再调用自己！
     */
    private static final String BATCH_CALL_SERVICE = "BatchCall";
    
    static Gson gson = new Gson();
    
    static MyJdbc myJdbc = BeanMgr.get(MyJdbc.class);
    
    /**
     * 处理代码<br>
     * 支持批量执行接口
     * @author nan.li
     * @param reqStr
     * @return
     */
    public static String handle(String reqStr)
    {
        if (StringUtils.isEmpty(reqStr))
        {
            return MQUtils.errorMsg(HttpServletResponseCode.SC_BAD_REQUEST, "invalid request: request param is empty!");
        }
        /**
         * 从请求map中取出服务码,然后执行服务
         */
        try
        {
            JsonParser parser = new JsonParser();
            JsonObject requestJsonObject = parser.parse(reqStr).getAsJsonObject();
            if (null == requestJsonObject)
            {
                return MQUtils.errorMsg(HttpServletResponseCode.SC_BAD_REQUEST, "invalid request: the request string cannot be converted to a JsonObject!");
            }
            JsonElement serviceCodeJsonElement = requestJsonObject.get(MQConstants.SERVICE_CODE);
            if (null == serviceCodeJsonElement)
            {
                return MQUtils.errorMsg(HttpServletResponseCode.SC_BAD_REQUEST, "invalid request: serviceCodeJsonElement is null!");
            }
            String serviceCode = serviceCodeJsonElement.getAsString();
            if (StringUtils.isEmpty(serviceCode))
            {
                return MQUtils.errorMsg(HttpServletResponseCode.SC_BAD_REQUEST, "invalid request: serviceCode should not be empty!");
            }
            //找到了服务对象，接下来就要记录该次访问的日志了
            //记录访问日志入库
            //记录访问日志，是对性能的一种浪费！因此完全可以考虑舍弃掉日志！
            //            BatchInsertDBDaemonThread.vector.add(new VisitLog().setServiceCode(serviceCode).setVisitTime(new Date()));
            
            //假如是批量提交接口，那么此时必定要批量调用
            if (BATCH_CALL_SERVICE.equals(serviceCode))
            {
                JsonArray jsonArray = requestJsonObject.getAsJsonArray("list");
                List<String> responses = new ArrayList<>();
                for (int i = 0; i < jsonArray.size(); i++)
                {
                    String request = jsonArray.get(i).getAsString();
                    //注意，此处的request不可以再是BATCH_CALL_SERVICE，否则会造成死循环调用！
                    String response = handle(request);
                    responses.add(response);
                }
                
                //事先构造出需要返回回去的对象
                JsonObject responseJsonObject = new JsonObject();
                responseJsonObject.add("list", gson.toJsonTree(responses));
                //统一增加一个默认的成功状态值！以最大化地降低业务代码的累赘性
                JsonElement resultCodeElement = responseJsonObject.get(MQConstants.RESULT_CODE);
                if (resultCodeElement == null || StringUtils.isEmpty(resultCodeElement.getAsString()))
                { //如果在响应中不声明RESULT_CODE字段
                 //那么默认该字段的结果为：WsStatusCode.SC_OK
                    responseJsonObject.addProperty(MQConstants.RESULT_CODE, HttpServletResponseCode.SC_OK);
                }
                return responseJsonObject.toString();
            }
            else
            {
                //否则，就是单个处理的逻辑
                //根据服务码取出相应的服务类
                IFunction functionObj = (IFunction)BeanMgr.getBeanByClassName("com.lnwazg.mh.function", serviceCode);//此处做了缓存，获取的时候是极速获取，无性能瓶颈
                if (null == functionObj)
                {
                    return MQUtils.errorMsg(HttpServletResponseCode.SC_BAD_REQUEST,
                        "invalid request: the service '" + serviceCode + "' is not available now! The ENV could not find a bean which is named: " + serviceCode
                            + "!");
                }
                
                //验证service参数
                Class<?> serviceClass = functionObj.getClass();
                
                //验证必传参数并将结果填充到相应的字段中
                Field[] fields = serviceClass.getDeclaredFields();
                if (fields != null && fields.length > 0)
                {
                    for (Field field : fields)
                    {
                        //该注解直接加在字段上面，因此可以直接通过字段去获取注解对象
                        //将所有的必传参数（加入了RequiredParam注解的参数）的信息（这些信息也是对象，数据源为json字符串所还原得到的对象）填充到serviceObj中
                        Param param = field.getAnnotation(Param.class);
                        if (param != null)
                        {
                            //则该字段是一个必传参数
                            String paramNameAnno = param.value();//该注解的value值一般都没有设置，默认为“”
                            //                        requiredParam.desc()
                            //如果该必传字段的字段名注解为空，则采用该字段的声明名称
                            if (StringUtils.isEmpty(paramNameAnno))
                            {
                                paramNameAnno = field.getName();
                            }
                            try
                            {
                                field.setAccessible(true);
                                //利用字段的名称，字段的Class所属的Type类型（field对象提供了该属性），requestJsonObject数据对象，以及Gson框架，去还原出一个对象
                                Object inflatedObj = inflateField(paramNameAnno, requestJsonObject, field);
                                //利用json中的字段参数信息还原得到的该字段对应的对象
                                if (inflatedObj == null && param.required() == true)
                                {
                                    return MQUtils.errorMsg(HttpServletResponseCode.SC_BAD_REQUEST, "invalid request: missing param element: " + paramNameAnno);
                                }
                                //妙就妙在，上面这段代码配合以下的代码，实际上消除了（显式）对象强制转换的语句声明！
                                field.set(functionObj, inflatedObj);//将还原出的对象直接注入到service对象的该参数字段中
                            }
                            catch (JsonSyntaxException e)
                            {
                                //字段填充失败
                                Logs.e("inflating the required param failed! The param name is: " + paramNameAnno, e);
                                return MQUtils.errorMsg(HttpServletResponseCode.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                            }
                            catch (Exception e)
                            {
                                //字段填充失败
                                Logs.e("inflating the required param failed! The param name is: " + paramNameAnno, e);
                                return MQUtils.errorMsg(HttpServletResponseCode.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                            }
                        }
                    }
                }
                //事先构造出需要返回回去的对象
                JsonObject responseJsonObject = new JsonObject();
                //执行webService服务，将待返回的对象作为参数传递给待执行的方法体，以便进行返回参数的“搭车式”填充
                //“搭车式”填充的好处是：可以在这个方法体内统一初始化，而具体的业务类仅需专注于业务即可，省去了一步初始化返回对象的操作
                Map<String, Object> outMap = new HashMap<>();
                functionObj.execute(outMap);
                
                //            String resultStr = gson.toJson(outMap);
                //            responseJsonObject = new JsonParser().parse(resultStr).getAsJsonObject();
                responseJsonObject = gson.toJsonTree(outMap).getAsJsonObject();
                
                //统一增加一个默认的成功状态值！以最大化地降低业务代码的累赘性
                JsonElement resultCodeElement = responseJsonObject.get(MQConstants.RESULT_CODE);
                if (resultCodeElement == null || StringUtils.isEmpty(resultCodeElement.getAsString()))
                { //如果在响应中不声明RESULT_CODE字段
                 //那么默认该字段的结果为：WsStatusCode.SC_OK
                    responseJsonObject.addProperty(MQConstants.RESULT_CODE, HttpServletResponseCode.SC_OK);
                }
                return responseJsonObject.toString();
            }
        }
        catch (WsBusinessException e)
        {
            Logs.e(e);
            return MQUtils.errorMsg(HttpServletResponseCode.SC_BAD_REQUEST, e.getMessage());
        }
        catch (JsonSyntaxException e)
        {
            Logs.e(e);
            return MQUtils.errorMsg(HttpServletResponseCode.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
        catch (Exception e)
        {
            Logs.e(e);
            return MQUtils.errorMsg(HttpServletResponseCode.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
    
    /**
     * 根据字段的类型，去填充该字段
     * @param paramName
     * @param requestJsonObject
     * @param field
     * @return
     */
    private static Object inflateField(String paramNameAnno, JsonObject requestJsonObject, Field field)
        throws Exception
    {
        JsonElement jsonElement = requestJsonObject.get(paramNameAnno);
        if (jsonElement == null)
        {
            //json中不含该字段，则返回null
            return null;
        }
        //若含这个字段，则尝试进行转换
        //根据field的字段类型信息（包括了泛型的信息）进行转换
        //return gson.fromJson(jsonElement, TypeToken.get(field.getGenericType()).getType());
        return gson.fromJson(jsonElement, field.getGenericType());//更简单而有效的写法！！！！modified @2013-07-13 01:12:02
        //无论该field是一个参数化的类型，还是一个普通的class类型，均可使用field.getGenericType()去获取它的实际的类型
    }
}
