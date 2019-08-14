package com.github.catstiger.common.sql.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 在SQL中执行Match查询的时候，如果要求全匹配（即%target%），则无法利用索引带来的性能提升。 标注为@FullMatches的字段，在生成查询SQL的时候，会使用一些其他方法代替LIKE查询。例如，在
 * MySQL数据库中使用locate函数的性能，要比LIKE快许多。
 * 
 * @author catstiger
 *
 */
@Target(value = { ElementType.METHOD, ElementType.FIELD })
@Retention(value = RetentionPolicy.RUNTIME)
public @interface FullMatches {

}
