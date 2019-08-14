package com.github.catstiger.common.sql.mapper;

import java.lang.reflect.Field;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.AbstractNestablePropertyAccessor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.util.ReflectionUtils;

import com.github.catstiger.common.sql.BaseEntity;
import com.github.catstiger.common.util.ReflectUtil;

public class BeanWrapperImplEx extends BeanWrapperImpl {
  private static Logger logger = LoggerFactory.getLogger(BeanWrapperImplEx.class);

  public BeanWrapperImplEx() {
    super();
  }

  public BeanWrapperImplEx(boolean registerDefaultEditors) {
    super(registerDefaultEditors);
  }

  public BeanWrapperImplEx(Class<?> clazz) {
    super(clazz);
  }

  public BeanWrapperImplEx(Object object, String nestedPath, Object rootObject) {
    super(object, nestedPath, rootObject);
  }

  public BeanWrapperImplEx(Object object) {
    super(object);
  }

  /**
   * @see AbstractNestablePropertyAccessor#convertForProperty(String, Object, Object, TypeDescriptor)
   */
  @Override
  protected Object convertForProperty(String propertyName, Object oldValue, Object newValue, TypeDescriptor td)
      throws TypeMismatchException {

    if (td.hasAnnotation(ManyToOne.class) || td.hasAnnotation(JoinColumn.class)) { // 外键，注入ID
      // 如果被注入的值为空或者不是数字，则直接跳过
      if (newValue == null || !NumberUtils.isCreatable(newValue.toString())) {
        return null;
      }
      Class<?> rawClass = td.getResolvableType().getRawClass(); // 外键指向的对象的类
      if (rawClass.getAnnotation(Entity.class) != null || rawClass.getAnnotation(Table.class) != null) {
        Object wrappedInstance = this.getWrappedInstance(); // Object contains the foreign object.
        if (wrappedInstance == null) {
          throw new IllegalStateException("Wrapped instance is null.");
        }

        // 获取外键对象
        Field propertyField = ReflectUtil.findField(this.getWrappedClass(), propertyName);
        ReflectionUtils.makeAccessible(propertyField);
        Object entity = ReflectUtil.getField(propertyField, wrappedInstance);
        // 如果外键对象为null, 则创建一个
        if (entity == null) {
          entity = BeanUtils.instantiateClass(rawClass);
        }
        // 设置外键对象的主键
        if (entity instanceof BaseEntity) {
          logger.debug("装入外键 {} {} {}", propertyName, wrappedInstance.getClass().getSimpleName(), newValue);
          ((BaseEntity) entity).setId(Long.valueOf(newValue.toString()));
        }
        return entity;
      }
      throw new IllegalStateException("无法实现外键转换 " + propertyName + " class: " + td);
    } else {
      return super.convertForProperty(propertyName, oldValue, newValue, td);
    }
  }
}
