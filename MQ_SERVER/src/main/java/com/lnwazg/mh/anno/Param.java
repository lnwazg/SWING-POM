package com.lnwazg.mh.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Param
{
    /**
     * 该字段的参数名
     * 默认情况下无须设置，那么这个值就采用默认值（这个参数字段的名称）
     * 但是这个字段还是有保留的必要的（适用于保留字冲突导致的json序列化失败的场景，这种情况下就必须用到别名了！），虽说实际使用率可能会很低！
     * @return
     */
    String value() default "";
    
    /**
     * 该字段的文档描述
     * @return
     */
    String desc() default "";
    
    /**
     * 该参数是否强制需要
     * @author nan.li
     * @return
     */
    boolean required() default true;
    
}
