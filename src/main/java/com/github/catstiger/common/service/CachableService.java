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
import com.google.common.base.Preconditions;

public class CachableService<T extends BaseEntity> {
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
  
  /**
   * 根据ID，获取单个记录。会抓取所有的字段，并支持以ID为key的缓存
   * @param id 主键
   * @return
   */
  @Transactional(readOnly = true)
  //@Cacheable(value = Constants.CACHE_NAME_COMMON, key = "#p0")
  public T get(Long id) {
    Preconditions.checkArgument(id != null, "ID是必须的。");
    return jdbcTemplate.get(getEntityClass(), id);
  }
  
  /**
   * 新增一条记录，自动分配主键，无论ID是否有值，都会为他分配一个值。
   * @param model 实体对象，承载各个字段的值。
   * @return 保存后的Model，ID不为空。
   */
  @Transactional
  public T create(T model) {
    Preconditions.checkArgument(model != null, "model不可为空。");
    
    model.setId(idGen.nextId());
    SQLReady sqlReady = new SQLRequest(entityClass).entity(model).insertNonNull();
    jdbcTemplate.update(sqlReady.getSql(), sqlReady.getArgs());
    
    return model;
  }
  
  
  /**
   * 根据ID，更新一条记录。
   * @param model 装载各个字段值的实体对象，ID不可为空
   * @return 就是传入的那个model.
   */
  @Transactional
  //@CacheEvict(value = Constants.CACHE_NAME_COMMON, key = "#p0.id")
  public T update(T model) {
    Preconditions.checkArgument(model != null, "model不可为空。");
    Preconditions.checkArgument(model.getId() != null, "ID不可为空。");
    
    getEntityClass();
    SQLReady sqlReady = new SQLRequest(entityClass).entity(model).updateById();
    jdbcTemplate.update(sqlReady.getSql(), sqlReady.getArgs());
    
    return model;
  }
  
  /**
   * 根据ID，删除一条记录
   * @param id 给出ID
   */
  @Transactional
  //@CacheEvict(value = Constants.CACHE_NAME_COMMON, key = "#p0")
  public void delete(Long id) {
    Preconditions.checkArgument(id != null, "ID不可为空。");
    getEntityClass();
    String tablename = new SQLRequest(entityClass).getNamingStrategy().tablename(entityClass);
    jdbcTemplate.update("delete from " + tablename + " where id=?", id);
  }
  
  /**
   * 取得entityClass的函数
   */
  @SuppressWarnings("unchecked")
  protected Class<T> getEntityClass() {
    if (entityClass == null) {
      entityClass = GenericsUtil.getGenericClass(getClass());
    }
    return entityClass;
  }
}
