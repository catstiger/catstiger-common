package com.github.catstiger.common.lazy.aop;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import com.github.catstiger.common.lazy.annotation.LazyField;
import com.github.catstiger.common.sql.BaseEntity;
import com.github.catstiger.common.sql.JdbcTemplateProxy;
import com.github.catstiger.common.sql.Page;
import com.github.catstiger.common.sql.SQLReady;
import com.github.catstiger.common.sql.SQLRequest;

/**
 * 根据<code>@LazyField</code>标注，加载BaseEntity的子类中需要延迟加载的类。
 * @see com.github.catstiger.common.lazy.annotation.LazyField
 * @author samlee
 *
 */
@Service
public class LazyLoadingService {
  private static Logger logger = LoggerFactory.getLogger(LazyLoadingService.class);
  
  private static final int MIN_METHOD_NAME = 4;
  private static final int GETTER_LENGTH = 3;
  
  @Autowired
  private ApplicationContext applicationContext;
  @Autowired
  private JdbcTemplateProxy jdbcTemplate;
  
  /**
   * 根据BaseEntity子类中的<code>@LazyField</code>标注，加载相关的field
   * @param entity 给出<code>BaseEntity</code>的实例
   */
  public void load(BaseEntity entity) {
    if (entity == null) {
      return;
    }
    Class<?> type = entity.getClass();
    Method[] methods = ReflectionUtils.getAllDeclaredMethods(type);
    
    for (Method method : methods) {
      if (isGetter(method)) {
        LazyField lazyField = method.getAnnotation(LazyField.class);
        if (lazyField == null) {
          if (BaseEntity.class.isAssignableFrom(method.getReturnType())) {
            //处理 没有用LazyField注解，但是返回值是Entity的情况，此种情况自动调用对应的Service，填充此属性
            loadEntity(entity, method);
          }
        } else {
          if (!lazyField.service().equals(Object.class) && StringUtils.isNotBlank(lazyField.method())) {
            // 调用指定Bean，指定方法的情况
            loadField(entity, method, lazyField);
          } else {
            // 未指定Bean和方法的情况
            logger.warn("必须指定service和method");
          }
        }
      }
    }
  }
  
  /**
   * 加载Page中的对象里面的 {@link Page#getRows()} 需要异步加载的数据
   */
  public void load(Page page) {
    if (page == null || CollectionUtils.isEmpty(page.getRows())) {
      return;
    }
    load(page.getRows());
  }
  
  /**
   * 加载Collection中的Entity中需要异步加载的数据
   */
  public void load(Collection<?> entities) {
    if (CollectionUtils.isEmpty(entities)) {
      return;
    }
    for (Iterator<?> itr = entities.iterator(); itr.hasNext();) {
      Object entity = itr.next();
      if (entity != null && entity instanceof BaseEntity) {
        load((BaseEntity) entity);
      }
    }
  }
  
  /**
   * 处理没有@LazyField注解的情况下，加载完整的Entity的情况：
   * 
   * <pre>
   * class Employee extends BaseEntity {
   *   public Department getDepartment() {
   *     return this.department;
   *   }
   * }
   * </pre>
   */
  private void loadEntity(BaseEntity owner, Method getter) {
    Object returnObject = ReflectionUtils.invokeMethod(getter, owner);
    if (returnObject == null || !(returnObject instanceof BaseEntity)) {
      return;
    }
    BaseEntity relateEntity = (BaseEntity) returnObject;
    if (relateEntity.getId() == null) {
      return;
    }
    //猜测service的bean name
    String beanName = relateEntity.getClass().getSimpleName() + "Service";
    beanName = StringUtils.uncapitalize(beanName);
    //获取service实例
    Object service = null;
    try {
      service = applicationContext.getBean(beanName);
    } catch (Exception e) {
      logger.debug("未得到service {}", beanName);
    }
    
    Object fullEntity = null;
    //没有service，直接查询jdbc
    if (service == null) {
      logger.debug("Loading entity by jdbc");
      fullEntity = loadByJdbc(relateEntity.getClass(), relateEntity.getId());
    } else { // 有Service的情况
      logger.debug("Loading entity by {}", service.getClass().getName());
      fullEntity = loadByService(service, relateEntity.getId());
    }
    Method setter = findSetterByGetter(getter);
    if (setter != null && fullEntity != null) {
      ReflectionUtils.invokeMethod(setter, owner, fullEntity);
    }
  }
  
  /**
   * 根据LazyField中定义的Service和Method，加载相关属性
   */
  private void loadField(BaseEntity owner, Method getter, LazyField lazyField) {
    if (owner == null) {
      logger.debug("给出的实体对象为null");
      return;
    }
    Method setter = findSetterByGetter(getter);
    if (setter == null) {
      logger.debug("没有对应的setter方法 {}#{}", owner.getClass().getSimpleName(), getter.getName());
      return;
    }
    if (!lazyField.service().equals(Object.class)) {
      Object service = null;
      try {
        service = applicationContext.getBean(lazyField.service()); //Service Bean
      } catch (Exception e) {
        logger.warn("指定的Bean无法获取 {}", lazyField.service());
        return;
      }
      
      String methodName = (StringUtils.isBlank(lazyField.method()) ? "get" : lazyField.method()); // method name
      // 被调用的方法参数类型
      Class<?>[] paramTypes = getFieldTypes(owner.getClass(), lazyField.paramFields()); 
      Method method = null;
      try {
        method = ReflectionUtils.findMethod(lazyField.service(), methodName, paramTypes);
      } catch (Exception e) {
        logger.warn("指定的方法无法获取 {} # {}", lazyField.service().getSimpleName(), methodName);
        return;
      }
      
      if (method != null) {
        //调用Service中的方法
        Object value = ReflectionUtils.invokeMethod(method, service, getFieldValues(owner, lazyField.paramFields()));
        ReflectionUtils.invokeMethod(setter, owner, value);
        logger.debug("Loading field by service {}", service.getClass().getName());
      }
    }
  }
  
  private Class<?>[] getFieldTypes(Class<?> ownerClass, String[] fields) {
    if (fields == null || fields.length == 0) {
      return new Class<?>[] {};
    }
    List<Class<?>> types = new ArrayList<Class<?>>(fields.length);
    for (String field : fields) {
      Field f = ReflectionUtils.findField(ownerClass, field);
      types.add(f.getType());
    }
    
    return types.toArray(new Class<?>[fields.length]);
  }
  
  private Object[] getFieldValues(BaseEntity owner, String[] fields) {
    if (fields == null || fields.length == 0) {
      return new Object[] {};
    }
    
    List<Object> values = new ArrayList<Object>(fields.length);
    for (String field : fields) {
      Field f = ReflectionUtils.findField(owner.getClass(), field);
      ReflectionUtils.makeAccessible(f);
      Object value = ReflectionUtils.getField(f, owner);
      values.add(value);
    }
    return values.toArray();
  }
  
  /**
   * 根据要求的EntityClass, 和ID，查询完整的Entity
   */
  private Object loadByJdbc(Class<?> entityClass, Object id) {
    SQLReady sqlReady = new SQLRequest(entityClass).selectById().addArg(id);
    return jdbcTemplate.queryForObject(sqlReady, entityClass);
  }
  
  private Object loadByService(Object service, Object id) {
    if (id == null) {
      return null;
    }
    Method get = ReflectionUtils.findMethod(service.getClass(), "get", id.getClass());
    if (get == null) {
      logger.debug("未找到{} # get方法，请用@LazyField 注明调用的方法名", service.getClass());
      return null;
    }
    return ReflectionUtils.invokeMethod(get, service, id);
  }
  
  private static boolean isGetter(Method method) {
    String name = method.getName();
    int modifiers = method.getModifiers();
    
    
    return (!ReflectionUtils.isObjectMethod(method)
        && name.length() >= MIN_METHOD_NAME && name.startsWith("get") 
        && 'A' <= name.charAt(GETTER_LENGTH) && name.charAt(GETTER_LENGTH) <= 'Z' //确保大写字母
        && method.getParameterCount() == 0 
        && method.getReturnType() != Void.TYPE 
        && Modifier.isPublic(modifiers)
        && !Modifier.isStatic(modifiers)
        && !Modifier.isAbstract(modifiers));
  }
  
  private static boolean isSetter(Method method) {
    String name = method.getName();
    int modifiers = method.getModifiers();
    
    return (name.length() >= MIN_METHOD_NAME && name.startsWith("set") 
        && 'A' <= name.charAt(GETTER_LENGTH) && name.charAt(GETTER_LENGTH) <= 'Z' 
        && method.getParameterCount() == 1 
        && Void.TYPE.equals(method.getReturnType())
        && Modifier.isPublic(modifiers)
        && !Modifier.isStatic(modifiers)
        && !Modifier.isAbstract(modifiers));
  }
  
  private static Method findSetterByGetter(Method getter) {
    Class<?> type = getter.getDeclaringClass();
    String name = "set" + getter.getName().substring(GETTER_LENGTH);
    Class<?> returnType = getter.getReturnType();
    Method setter;
    try {
      setter = type.getMethod(name, returnType);
    } catch (NoSuchMethodException | SecurityException e) {
      logger.error("未指定对应的Setter方法 {}", name);
      e.printStackTrace();
      return null;
    }
    
    return isSetter(setter) ? setter : null;
  }
}