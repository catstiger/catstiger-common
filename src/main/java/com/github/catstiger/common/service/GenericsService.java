package com.github.catstiger.common.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.github.catstiger.common.sql.BaseEntity;
import com.github.catstiger.common.sql.JdbcTemplateProxy;
import com.github.catstiger.common.sql.SQLReady;
import com.github.catstiger.common.sql.SQLRequest;
import com.github.catstiger.common.sql.id.IdGen;
import com.github.catstiger.common.util.GenericsUtil;

public class GenericsService<T extends BaseEntity> {
  /**
   * Log,子类可以直接使用，不必重新声明
   */
  protected Logger logger = LoggerFactory.getLogger(getClass());
  /**
   * Manager所管理的实体对象类型.
   */
  protected Class<T> entityClass;
  @Autowired
  protected JdbcTemplateProxy jdbcTemplate;
  @Autowired
  protected IdGen idGen;

  @Transactional(readOnly = true)
  public T get(Long id) {
    return jdbcTemplate.get(getEntityClass(), id);
  }

  /**
   * 根据entity的id判断是执行insert还是update，如果id为{@code null} 则生成ID，并执行insert。 如果id不为{@code null} 则执行update
   * 
   * @param entity 给出映射的类
   * @return 原始的entity对象，如果原id为{@code null}, 则此时id不为空。
   */
  @Transactional
  public T merge(T entity) {
    if (entity == null) {
      throw new IllegalArgumentException("entity must not be null");
    }
    getEntityClass();
    Long id = entity.getId();
    SQLReady sqlReady;

    if (id == null) {
      entity.setId(idGen.nextId());
      sqlReady = new SQLRequest(entityClass).entity(entity).insertNonNull();
    } else {
      sqlReady = new SQLRequest(entityClass).entity(entity).updateById();
    }
    if (logger.isDebugEnabled()) {
      System.out.println("MERGE [" + sqlReady.getSql() + "]");
    }
    jdbcTemplate.update(sqlReady.getSql(), sqlReady.getArgs());
    return entity;
  }

  /**
   * 取得entityClass的函数. JDK1.4不支持泛型的子类可以抛开Class entityClass,重载此函数达到相同效果。
   */
  @SuppressWarnings("unchecked")
  protected Class<T> getEntityClass() {
    if (entityClass == null) {
      entityClass = GenericsUtil.getGenericClass(getClass());
    }
    return entityClass;
  }
}
