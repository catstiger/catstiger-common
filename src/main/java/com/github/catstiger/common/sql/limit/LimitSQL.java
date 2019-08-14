package com.github.catstiger.common.sql.limit;

/**
 * 子类将一个普通的SQL加工成一个限制提取（fetch）范围的SQL，子类需要根据不同的数据库实现不同的算法。
 * @author catstiger
 *
 */
public interface LimitSQL {
  /**
   * 将一个普通的SQL加工成限制抓取范围的SQL
   * @param sql 给定一个SQL 
   * @param start 提取位置，对于MySQL，第一条数据为0
   * @param limit 抓取数量
   * @return 被limit子句加工过的SQL
   */
  String getLimitSql(String sql, int start, int limit);
}
