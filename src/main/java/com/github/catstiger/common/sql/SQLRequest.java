package com.github.catstiger.common.sql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;

import com.github.catstiger.common.sql.limit.LimitSQL;
import com.github.catstiger.common.sql.limit.MySqlLimitSQL;
import com.github.catstiger.common.sql.naming.CamelCaseNamingStrategy;
import com.google.common.base.Joiner;

/**
 * 用于存放生成SQL所需的参数
 * 
 * @author catstiger
 *
 */
public final class SQLRequest {
  /**
   * 缺省的命名策略--字段名，表名使用下划线命名法，别名使用驼峰命名法
   */
  public static final NamingStrategy DEFAULT_NAME_STRATEGY = new CamelCaseNamingStrategy();
  public static final LimitSQL DEFAULT_LIMIT_SQL = new MySqlLimitSQL();

  private Class<?> entityClass;
  private Object entity;
  private List<String> includes = new ArrayList<String>(10);
  private List<String> excludes = new ArrayList<String>(10);
  private boolean usingAlias = false;
  
  private boolean namedParams = false;
  private NamingStrategy namingStrategy = DEFAULT_NAME_STRATEGY;
  private boolean includesNull = false;
  private boolean byId = false;
  private LimitSQL limitSql = DEFAULT_LIMIT_SQL;
  private Map<String, String> sorts = new LinkedHashMap<>(5);

  /**
   * 根据实体类构建一个SQLRequest，使用缺省的命名规则，并且不生成别名
   */
  public SQLRequest(Class<?> entityClass) {
    this.entity = null;
    this.entityClass = entityClass;
    usingAlias = false;
    namingStrategy = DEFAULT_NAME_STRATEGY;
    includesNull = false;
    byId = false;
    namedParams = false;
  }

  /**
   * 根据实体类构建SQLRequest
   * @param entityClass 实体类
   * @param usingAlias 是否使用别名
   */
  public SQLRequest(Class<?> entityClass, boolean usingAlias) {
    this.entity = null;
    this.entityClass = entityClass;
    this.usingAlias = usingAlias;
    namingStrategy = DEFAULT_NAME_STRATEGY;
    includesNull = false;
    byId = false;
    namedParams = false;
  }

  /**
   * 根据实体类的实例构建一个SQLRequest，使用缺省的命名规则，并且不生成别名
   */
  public SQLRequest(BaseEntity entity) {
    if (entity == null) {
      throw new RuntimeException("实体不可为null.");
    }
    this.entity = entity;
    this.entityClass = entity.getClass();
    usingAlias = false;
    namingStrategy = DEFAULT_NAME_STRATEGY;
    includesNull = false;
    byId = false;
    namedParams = false;
  }

  /**
   * 根据实体类的实例构建SQLRequest
   * @param entity 实体类
   * @param usingAlias 是否使用别名
   */
  public SQLRequest(BaseEntity entity, boolean usingAlias) {
    if (entity == null) {
      throw new RuntimeException("实体不可为null.");
    }
    this.entity = entity;
    this.entityClass = entity.getClass();
    this.usingAlias = usingAlias;
    namingStrategy = DEFAULT_NAME_STRATEGY;
    includesNull = false;
    byId = false;
    namedParams = false;
  }

  /**
   * 设置SQL对应的实体类的Class
   * 
   * @param entityCls 实体类
   * @return 支持链式操作
   */
  public SQLRequest entityClass(Class<?> entityCls) {
    this.entityClass = entityCls;
    return this;
  }

  /**
   * 设置SQL对应的实体对象，通常用于生成SQL
   * 
   * @param entityInstance 实体对象
   * @return 支持链式操作
   */
  public SQLRequest entity(Object entityInstance) {
    if (entityInstance == null) {
      throw new RuntimeException("实体不可为null.");
    }
    this.entity = entityInstance;
    this.entityClass = entityInstance.getClass();

    return this;
  }

  /**
   * 设置必须包含的属性名
   * 
   * @param includeFieldnames 属性名
   * @return 支持链式操作
   */
  public SQLRequest includes(String... includeFieldnames) {
    if (includeFieldnames != null && includeFieldnames.length > 0) {
      this.includes.addAll(Arrays.asList(includeFieldnames));
    }
    return this;
  }

  /**
   * 设置必须排除的属性名
   * 
   * @param excludeFieldnames 属性名
   * @return 支持链式操作
   */
  public SQLRequest excludes(String... excludeFieldnames) {
    if (excludeFieldnames != null && excludeFieldnames.length > 0) {
      this.excludes.addAll(Arrays.asList(excludeFieldnames));
    }
    return this;
  }

  /**
   * 是否将属性名作为字段的别名，或者，在insert和update的时候，用属性名代替?
   * 
   * @param isUsingAlias 如果为<code>true</code>，则将属性名作为字段名的别名，例如 user_id AS userId， 缺省为<code>false</code>
   * @return 支持链式操作
   */
  public SQLRequest usingAlias(boolean isUsingAlias) {
    this.usingAlias = isUsingAlias;
    return this;
  }

  /**
   * 设置命名规则
   * 
   * @param namingStrgy 命名规则的实例
   * @return 支持链式操作
   */
  public SQLRequest namingStrategy(NamingStrategy namingStrgy) {
    this.namingStrategy = namingStrgy;
    return this;
  }

  /**
   * 在Update和Insert的时候，是否包含为<code>null</code>的字段，缺省值为<code>false</code>
   */
  public SQLRequest includesNull(boolean isIncludesNull) {
    this.includesNull = isIncludesNull;
    return this;
  }

  /**
   * 在Update和SELECT的情形下，是否生成WHERE id=?，缺省为false
   */
  public SQLRequest byId(boolean isById) {
    this.byId = isById;
    return this;
  }

  /**
   * 是否命名参数，如果为<code>false</code>,生成的SQL使用?作为占位符， 否则使用属性名作为占位符。
   */
  public SQLRequest namedParams(boolean isNamedParams) {
    this.namedParams = isNamedParams;
    return this;
  }

  /**
   * 设定本SQLRequest使用的LimitSql对象
   */
  public SQLRequest withLimitSql(LimitSQL limitSQL) {
    if (limitSQL != null) {
      this.limitSql = limitSQL;
    }
    return this;
  }

  /**
   * 新增一组排序，Key为字段名，Value为desc/asc
   */
  public SQLRequest setSorts(Map<String, String> sortMap) {
    if (sortMap != null && !sortMap.isEmpty()) {
      this.sorts.putAll(sortMap);
    }
    return this;
  }

  /**
   * 新增一个排序条件
   * 
   * @param columnName 字段名、属性名
   * @param direction 方向，asc\desc
   */
  public SQLRequest addSort(String columnName, String direction) {
    this.sorts.put(columnName, direction);
    return this;
  }

  /**
   * 生成INSERT SQL及其对应的参数，不包括值为<code>null</code>的字段
   * 
   * @return Instance of SQLReady.
   */
  public SQLReady insert() {
    SQLFactory sqlFactory = SQLFactory.getInstance();
    return sqlFactory.insert(this);
  }

  /**
   * 生成INSERT SQL及其对应的参数
   * 
   * @return Instance of SQLReady.
   */
  public SQLReady insertNonNull() {
    SQLFactory sqlFactory = SQLFactory.getInstance();
    this.includesNull(false);
    return sqlFactory.insert(this);
  }

  /**
   * 生成UPDATE SQL
   */
  public SQLReady update() {
    SQLFactory sqlFactory = SQLFactory.getInstance();
    return sqlFactory.update(this);
  }

  /**
   * 根据SQLRequest构造一个SQL UPDATE语句及其对应的参数数组。 只处理不为空的字段，并且，在SET子句中忽略主键字段根据SQLRequest中的NamingStrategy，构造各个字段的名字 如果参数byId为true，并且，SQLRequest#entity的主键不为空，则自动追加WHERE
   * id=?子句，并且在参数中加入ID值
   */
  public SQLReady updateById() {
    SQLFactory sqlFactory = SQLFactory.getInstance();
    return sqlFactory.update(this.byId(true));
  }

  /**
   * 生成SQL语，如果{@link #byId}为false，则不生成WHERE及WHERE以后的部分，否则生成的语句包括WHERE id=?
   * 
   * @return SQLReady, 只有SQL，没有参数
   */
  public SQLReady select() {
    return SQLFactory.getInstance().select(this);
  }

  /**
   * 生成根据ID查询的SQL语句
   */
  public SQLReady selectById() {
    return SQLFactory.getInstance().select(this.byId(true));
  }

  /**
   * 返回当前使用的命名策略ß
   */
  public NamingStrategy getNamingStrategy() {
    return this.namingStrategy;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(200);
    if (entity != null) {
      entityClass = entity.getClass();
    }
    buf.append(entityClass.getName()).append(usingAlias);

    if (!CollectionUtils.isEmpty(includes)) {
      buf.append(Joiner.on("_").join(includes));
    }
    if (!CollectionUtils.isEmpty(excludes)) {
      buf.append(Joiner.on("_").join(excludes));
    }

    buf.append(includesNull).append(usingAlias).append(byId);
    buf.append(namingStrategy.getClass().getSimpleName());

    return buf.toString();
  }

  public Class<?> getEntityClass() {
    return entityClass;
  }

  public Object getEntity() {
    return entity;
  }

  public List<String> getIncludes() {
    return includes;
  }

  public List<String> getExcludes() {
    return excludes;
  }

  public boolean isUsingAlias() {
    return usingAlias;
  }

  public boolean isNamedParams() {
    return namedParams;
  }

  public boolean isIncludesNull() {
    return includesNull;
  }

  public LimitSQL getLimitSql() {
    return limitSql;
  }

  public Map<String, String> getSorts() {
    return sorts;
  }

  public boolean isById() {
    return byId;
  }
}
