package com.github.catstiger.common.poi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD })
public @interface Cell {
  /**
   * 对应Excel的标题列
   * @return
   */
  String title() default "";

  /**
   * 是否存入数据库，如果为true，则表示忽略，不会存入数据库
   */
  boolean ignore() default false;

  /**
   * 对应的数据库的列名，缺省为空，此时按照underline命名法推定数据库列名
   */
  String columnName() default "";

  /**
   * 转换器类名，CellConverter的实现类
   */
  Class<?> converter() default NothingConverter.class;
  
  Class<?> validator() default NothingValidator.class;
}
