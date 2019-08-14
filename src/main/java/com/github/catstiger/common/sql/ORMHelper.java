package com.github.catstiger.common.sql;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;

import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import com.github.catstiger.common.sql.annotation.SyncIgnore;
import com.google.common.base.CaseFormat;
import com.google.common.base.Splitter;

public final class ORMHelper {
  private static Map<String, ORMHelper> instances = new ConcurrentHashMap<>(5);

  private NamingStrategy namingStrategy;

  private ORMHelper() {
    namingStrategy = SQLRequest.DEFAULT_NAME_STRATEGY;
  }

  private ORMHelper(NamingStrategy namingStrategy) {
    this.namingStrategy = namingStrategy;
  }

  /**
   * 得到{@code ORMHelper}的实例，并使用缺省的命名策略（数据库使用snake case, java使用camel case）
   * @return
   */
  public static ORMHelper getInstance() {
    String key = SQLRequest.DEFAULT_NAME_STRATEGY.getClass().getSimpleName();
    if (!instances.containsKey(key)) {
      ORMHelper instance = new ORMHelper();
      instances.put(key, instance);
    }

    return instances.get(key);
  }

  /**
   * 得到{@code ORMHelper}的实例，并制定命名策略
   * @param namingStrategy 命名策略
   * @return
   */
  public static ORMHelper getInstance(NamingStrategy namingStrategy) {
    if (namingStrategy == null) {
      return getInstance();
    }
    String key = namingStrategy.getClass().getSimpleName();
    if (!instances.containsKey(key)) {
      ORMHelper instance = new ORMHelper(namingStrategy);
      instances.put(key, instance);
    }

    return instances.get(key);
  }

  /**
   * 判断给定的Class是否是一个实体类
   */
  public Boolean isEntity(Class<?> entityClass) {
    if (entityClass == null) {
      return false;
    }

    return (entityClass.getAnnotation(Entity.class) != null) && (entityClass.getAnnotation(SyncIgnore.class) == null);
  }

  /**
   * 判断一个实体类是否被数据同步忽略，被SyncIgnore标注的类会被忽略
   */
  public Boolean isEntityIgnore(Class<?> entityClass) {
    if (entityClass == null) {
      return true;
    }

    return entityClass.getAnnotation(SyncIgnore.class) != null;
  }

  /**
   * 根据实体类，得到对应的表名：
   * <ul>
   * <li>如果实体类被@javax.persistence.Table标注，并且进行命名，则取@Table命名</li>
   * <li>如果实体类被@org.beetl.sql.core.annotatoin.Table标注，并且进行命名，则取@Table的命名</li>
   * <li>否则，取类名小写，单词之间用下划线分割作为表名</li>
   * </ul>
   * 
   * @param entityClass 给出实体类
   * @return 表名
   */
  public String tableNameByEntity(Class<?> entityClass) {
    return namingStrategy.tablename(entityClass);
  }

  /**
   * 根据实体类的属性，获取字段名：
   * <ul>
   * <li>如果属性被@javax.persistence.Column或者@javax.persistence.JoinColumn标注，并且进行命名，则取其作为字段名</li>
   * <li>否则，取字段名小写，单词之间用下划线分割作为字段名</li>
   * </ul>
   * 
   * @param entityClass 实体类
   * @param fieldName 属性名
   * @return 字段名
   */
  public String columnNameByField(Class<?> entityClass, String fieldName) {
    return namingStrategy.columnName(entityClass, fieldName);
  }

  /**
   * 根据实体类，和field，获取对应的GETTER方法。被如下Annotation标注的字段或者对应的Getter方法，会被忽略
   * <ul>
   * <li>javax.persistence.Transient</li>
   * <li>java.beans.Transient</li>
   * <li>javax.persistence.ManyToMany</li>
   * </ul>
   * 
   * @param entityClass 实体类
   * @param fieldName 属性名
   * @return Getter Meth
   */
  public Method getAccessMethod(Class<?> entityClass, String fieldName) {
    Method getter = ReflectionUtils.findMethod(entityClass, "get" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, fieldName));
    return getter;
  }

  public Method getAccessMethod(Field field) {
    return getAccessMethod(field.getDeclaringClass(), field.getName());
  }

  /**
   * 字段是否被忽略
   * 
   * @param field 给出字段对象
   * @return
   */
  public Boolean isFieldIgnore(Field field) {
    SyncIgnore syncIgnore = field.getAnnotation(SyncIgnore.class);
    if (syncIgnore != null) {
      return true;
    }
    javax.persistence.Transient transientAnn = field.getAnnotation(javax.persistence.Transient.class);
    if (transientAnn != null) {
      return true;
    }

    java.beans.Transient trAnn = field.getAnnotation(java.beans.Transient.class);
    if (trAnn != null) {
      return true;
    }

    Method getter = getAccessMethod(field.getDeclaringClass(), field.getName());
    if (getter == null) {
      return true;
    }
    syncIgnore = field.getAnnotation(SyncIgnore.class);
    if (syncIgnore != null) {
      return true;
    }
    transientAnn = getter.getAnnotation(javax.persistence.Transient.class);
    if (transientAnn != null) {
      return true;
    }

    trAnn = getter.getAnnotation(java.beans.Transient.class);
    if (trAnn != null) {
      return true;
    }

    ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
    if (manyToMany != null) {
      return true;
    }
    manyToMany = getter.getAnnotation(ManyToMany.class);
    if (manyToMany != null) {
      return true;
    }

    return false;
  }

  /**
   * 属性是否被忽略
   */
  public Boolean isFieldIgnore(Class<?> entityClass, String fieldName) {
    try {
      Field field = ReflectionUtils.findField(entityClass, fieldName);
      return isFieldIgnore(field);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return false;
  }

  /**
   * 判断字段是否在UPDATE，INSERT中被忽略，标注为Transient的字段，集合类或者数组
   * 
   * @param field 给出字段对象
   * @return
   */
  public Boolean isUpdateIgnore(Field field) {
    // 如果标注为Transient,则忽略
    if (field.getAnnotation(Transient.class) != null || field.getAnnotation(java.beans.Transient.class) != null) {
      return true;
    }
    // 如果是集合类或者数组，则忽略
    if (ClassUtils.isAssignable(Collection.class, field.getType()) || field.getType().isArray()) {
      return true;
    }

    Method getter = this.getAccessMethod(field);
    if (getter.getAnnotation(Transient.class) != null || getter.getAnnotation(java.beans.Transient.class) != null) {
      return true;
    }
    return false;
  }

  /**
   * 判断一个字段是否在Update SQL 中忽略
   * @param entityClass 给出对应的实体类
   * @param fieldName 字段名
   * @return
   */
  public Boolean isUpdateIgnore(Class<?> entityClass, String fieldName) {
    try {
      Field field = ReflectionUtils.findField(entityClass, fieldName);
      return isUpdateIgnore(field);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return false;
  }

  /**
   * 判断一个属性是否是主键
   */
  public Boolean isPrimaryKey(Field field) {
    if (isFieldIgnore(field)) {
      return false;
    }

    Id idAnn = field.getAnnotation(Id.class);
    if (idAnn != null) {
      return true;
    }

    Method getter = this.getAccessMethod(field);
    idAnn = getter.getAnnotation(Id.class);
    if (idAnn != null) {
      return true;
    }

    if ("id".equals(field.getName()) && Long.class == field.getType()) {
      return true;
    }
    return false;
  }

  /**
   * 返回缩写
   */
  public String simpleName(String name) {
    StringBuilder strBuilder = new StringBuilder();
    Splitter.on("_").split(name).forEach(new Consumer<String>() {
      @Override
      public void accept(String t) {
        if (t != null && t.length() > 1) {
          strBuilder.append(t.charAt(0));
        }
      }
    });

    return strBuilder.toString();
  }
}
