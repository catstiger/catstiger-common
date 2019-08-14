package com.github.catstiger.common.sql.sync;

/**
 * 执行DDL SQL 语句，实现类应该可以直接在JDBC中执行，可以可以将这些SQL打印出来或者存为文件
 * @author leesam
 *
 */
public interface DDLExecutor {
  /**
   * 执行或者打印/输出 SQL文件
   * @param sql 给出SQL语句
   */
  void execute(String sql);
}
