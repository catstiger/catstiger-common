package com.github.catstiger.common.sql.filter;

import com.github.catstiger.common.sql.Page;

public interface DynaSpec {
  /**
   * 添加一个QueryPart对象
   * 
   * @param queryPart 给定QueryPart对象
   * @return DynaSpec对象，便于编写链式语法
   */
  public abstract DynaSpec add(QueryPart queryPart);

  /**
   * 添加一个排序
   * 
   * @param sortName 排序字段名
   * @param sortOrder 排序方向（asc, desc）
   * @return DynaSpec对象，便于编写链式语法
   */
  public abstract DynaSpec addSort(String sortName, String sortOrder);

  /**
   * 设置查询的其实行索引，第一行，为0
   */
  public abstract DynaSpec setFirstResult(Integer firstResult);

  /**
   * Set a limit upon the number of objects to be retrieved.
   *
   * @param maxResults the maximum number of results
   * @return this (for method chaining)
   */
  public abstract DynaSpec setMaxResults(Integer maxResults);

  /**
   * 设置Root Entity Class
   */
  public abstract <T> DynaSpec setRoot(Class<T> entityClass);

  /**
   * 返回Root Entity Class
   */
  public abstract <T> Class<T> getRoot();

  /**
   * 构建Query String
   * 
   * @param entityClass 查询对应的实体类
   */
  public abstract <T> String buildQueryString(Class<T> entityClass);

  /**
   * 构建Query String
   * 
   * @param tableName 查询对应的表名
   */
  public abstract String buildQueryString(String tableName);

  /**
   * 构建Query String，不考虑表名或者别名
   */
  public abstract String buildQueryString();

  /**
   * 返回查询参数
   * 
   * @return
   */
  public abstract Object[] getQueryParams();

  /**
   * 设置Page对象
   */
  public DynaSpec setPage(Page page);

  /**
   * 返回Page对象
   */
  public Page getPage();
}
