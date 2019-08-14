package com.github.catstiger.common.sql.limit;

public class MySqlLimitSQL implements LimitSQL {

  @Override
  public String getLimitSql(String sql, int start, int limit) {
    return new StringBuilder(sql.length() + 20).append(sql)
        .append(" limit ").append(start).append(",").append(limit).toString();
  }

}
