package com.github.catstiger.common.sql.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用户SyncIgnore标注的类，方法或者属性，在同步DDL的时候，会被自动忽略。
 * 
 * @author catstiger
 *
 */
@Target(value = { ElementType.TYPE, ElementType.METHOD, ElementType.FIELD })
@Retention(value = RetentionPolicy.RUNTIME)
public @interface SyncIgnore {

}
