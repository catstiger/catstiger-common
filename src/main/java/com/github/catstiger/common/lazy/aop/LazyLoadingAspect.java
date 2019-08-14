package com.github.catstiger.common.lazy.aop;

import java.util.Collection;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.catstiger.common.sql.BaseEntity;
import com.github.catstiger.common.sql.Page;

/**
 * Spring AOP，拦截所有被<code>@LazyLoading</code>标注的方法，如果方法返值中有
 * {@link BaseEntity}的子类，则尝试加载实体类中关联的字段。
 *
 */
@Component
@Aspect
public class LazyLoadingAspect {
  @Autowired
  private LazyLoadingService lazyLoadingService;
  
  @Pointcut(value = "@annotation(com.github.catstiger.common.lazy.annotation.LazyLoading)")
  public void lazyLoad() {
    
  }
  /**
   * 被LazyLoading标注的方法，会自动加载相关的数据。
   */
  @AfterReturning(pointcut = "lazyLoad()", returning = "returnObject")
  public void afterReturning(JoinPoint joinPoint, Object returnObject) {
      if (returnObject != null) {
        if (returnObject instanceof Page) {
          lazyLoadingService.load((Page) returnObject);
        } 
        if (returnObject instanceof Collection) {
          lazyLoadingService.load((Collection<?>) returnObject);
        }
        if (returnObject instanceof BaseEntity) {
          lazyLoadingService.load((BaseEntity) returnObject);
        }
      }
  }
}
