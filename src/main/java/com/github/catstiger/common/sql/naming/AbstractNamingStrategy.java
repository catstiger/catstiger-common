package com.github.catstiger.common.sql.naming;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ReflectionUtils;

import com.github.catstiger.common.sql.NamingStrategy;
import com.google.common.base.CaseFormat;
import com.google.common.base.Splitter;

public abstract class AbstractNamingStrategy implements NamingStrategy {
  protected static Map<String, String> tablenameCache = new ConcurrentHashMap<>(64);
  protected static Map<String, String> colnameCache = new ConcurrentHashMap<>(64);
  protected static Map<String, String> aliasCache = new ConcurrentHashMap<>(64);
  protected static Map<String, String> simpleAliasCache = new ConcurrentHashMap<>(64);

  /**
   * 根据@Table标注获取表名，如果没有标注，则取类名的Snake Case作为表名
   */
  @Override
  public String tablename(Class<?> entityClass) {
    if (tablenameCache.containsKey(entityClass.getName())) {
      return tablenameCache.get(entityClass.getName());
    }
    Entity entity = entityClass.getAnnotation(Entity.class);
    if (entity == null) {
      throw new RuntimeException("实体类必须用@Entity标注:" + entityClass.getName());
    }

    String tablename;
    Table table = entityClass.getAnnotation(Table.class);
    if (table != null && StringUtils.isNotBlank(table.name())) {
      tablename = table.name();
    } else {
      tablename = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, entityClass.getSimpleName());
    }

    tablenameCache.put(entityClass.getName(), tablename);
    return tablename;
  }

  /**
   * 表名缩写作为别名
   */
  @Override
  public String tableAlias(Class<?> entityClass) {
    if (aliasCache.containsKey(entityClass.getName())) {
      return aliasCache.get(entityClass.getName());
    } else {
      String alias = entityClass.getSimpleName();
      aliasCache.put(entityClass.getName(), alias);
      return alias;
    }
  }

  @Override
  public String simpleAlias(Class<?> entityClass) {
    if (simpleAliasCache.containsKey(entityClass.getName())) {
      return simpleAliasCache.get(entityClass.getName());
    } else {
      String underScore = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, entityClass.getSimpleName());
      StringBuilder buf = new StringBuilder(20);
      Splitter.on("_").split(underScore).forEach(s -> {
        buf.append(s.charAt(0));
      });

      String alias = buf.toString();
      simpleAliasCache.put(entityClass.getName(), alias);
      return alias;
    }
  }

  @Override
  public String columnName(Class<?> entityClass, String fieldname) {
    if (entityClass == null) {
      throw new IllegalArgumentException("实体类不可为空！");
    }
    if (StringUtils.isBlank(fieldname)) {
      throw new IllegalArgumentException("属性名不可为空！");
    }
    Field field = ReflectionUtils.findField(entityClass, fieldname);
    return this.columnName(entityClass, field);
  }

  @Override
  public String columnName(Class<?> entityClass, Field field) {
    if (field == null) {
      throw new java.lang.IllegalArgumentException("属性不可为空！");
    }
    String key = new StringBuilder(100).append(entityClass.getName()).append("#").append(field.getName()).toString();
    if (colnameCache.containsKey(key)) {
      return colnameCache.get(key);
    }

    Method getter = ReflectionUtils.findMethod(entityClass, "get" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, field.getName()));

    Column colAnn = getColumnAnn(field, getter);
    if (colAnn != null && StringUtils.isNotBlank(colAnn.name())) {
      String colname = colAnn.name().toLowerCase();
      colnameCache.put(key, colname);
      return colname;
    }

    JoinColumn joinColAnn = getJoinColumnAnn(field, getter);
    if (joinColAnn != null && StringUtils.isNotBlank(joinColAnn.name())) {
      String colname = joinColAnn.name().toLowerCase();
      colnameCache.put(key, colname);
      return colname;
    }

    String colname = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName());
    Entity refEntityAnn = field.getType().getAnnotation(Entity.class); // 外键
    if (joinColAnn != null || refEntityAnn != null) { // 外键加_id
      colname = colname + "_id";
    }
    colnameCache.put(key, colname);

    return colname;
  }
  
  private Column getColumnAnn(Field field, Method getter) {
    Column colAnn = field.getAnnotation(Column.class);
    if (colAnn == null) {
      if (getter != null) {
        colAnn = getter.getAnnotation(Column.class);
      }
    }
    return colAnn;
  }
  
  private JoinColumn getJoinColumnAnn(Field field, Method getter) {
    JoinColumn joinColAnn = field.getAnnotation(JoinColumn.class);
    if (joinColAnn == null) {
      if (getter != null) {
        joinColAnn = getter.getAnnotation(JoinColumn.class);
      }
    }
    return joinColAnn;
  }
  
}
