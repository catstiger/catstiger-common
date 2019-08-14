package com.github.catstiger.common.lazy.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用<code>@LazyLoading</code>标注的方法，会被AOP拦截，处理其返值中被<code>@LazyField</code>
 * 标注的方法，加载关联的数据。
 * <strong>目前可以处理的返值类型为</strong>
 * <ul>
 *     <li>返值为Page对象，处理其中的Collection rows字段，并且其中的元素为BaseEntity的子类</li>
 *     <li>返值为Collection, 并且其中的元素为BaseEntity的子类</li>
 *     <li>BaseEntity的子类</li>
 * </ul>
 * @author samlee
 *
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LazyLoading {
 
}
