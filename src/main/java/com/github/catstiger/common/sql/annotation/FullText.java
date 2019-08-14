package com.github.catstiger.common.sql.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 在MySQL 5.6+中，Clob或者text类型的字段，可以使用全文检索。用@FullText标注的字段， 在构造查询语句的条件的时候，会使用该数据的全文检索语法。
 * 
 * @author catstiger
 *
 */
@Target(value = { ElementType.METHOD, ElementType.FIELD })
@Retention(value = RetentionPolicy.RUNTIME)
public @interface FullText {
  /**
   * 全文检索有时候不能在本字段检索，需要将字段内容编码后存入另外一个字段， relativeColumn用于指出该字段的字段名。
   */
  String relativeColumn() default "";
}
