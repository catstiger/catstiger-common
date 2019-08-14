package com.github.catstiger.common.sql.limit;

public class H2LimitSQL implements LimitSQL {

  @Override
  public String getLimitSql(String sql, int start, int limit) {
    return new StringBuffer(sql.length() + 20) // hasOffset ? " limit ? offset ?" : " limit ?" 
    .append(sql)
    .append(" limit ").append(limit).append(" offset ").append(start)
    .toString();
  }

}
