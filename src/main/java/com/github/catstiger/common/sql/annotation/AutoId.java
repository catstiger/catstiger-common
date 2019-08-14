package com.github.catstiger.common.sql.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 如果在属性或者属性对应的getter方法上加入@AutoId标注，该属性在创建表的时候，对应的字段 为自增字段。
 * 
 * @author catstiger
 *
 */
@Target(value = { ElementType.FIELD, ElementType.METHOD })
@Retention(value = RetentionPolicy.RUNTIME)
public @interface AutoId {
}
