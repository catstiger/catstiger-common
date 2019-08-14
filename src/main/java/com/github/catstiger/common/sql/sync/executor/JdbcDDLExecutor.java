package com.github.catstiger.common.sql.sync.executor;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.github.catstiger.common.sql.sync.DDLExecutor;
import com.github.catstiger.common.util.Exceptions;

/**
 * 直接在JDBC中执行DDL SQLs
 * @author leesam
 *
 */
public class JdbcDDLExecutor implements DDLExecutor {
  private static Logger logger = LoggerFactory.getLogger(JdbcDDLExecutor.class);
  private DataSource dataSource;
  private JdbcTemplate jdbcTemplate;
  
  public JdbcDDLExecutor(DataSource dataSource) {
    this.dataSource = dataSource;
    jdbcTemplate = new JdbcTemplate(this.dataSource);
  }
  
  @Override
  public void execute(String sql) {
    try {
      jdbcTemplate.execute(sql);
      logger.info("Execute [{}]", sql);
    } catch (Exception e) {
      e.printStackTrace();
      throw Exceptions.unchecked(e);
    }
  }

}
