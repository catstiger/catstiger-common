package com.github.catstiger.common.sql.limit;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;



/**
 * 用于探测数据库类型
 */
@Component
public class DatabaseDetector {
  @Autowired
  private DataSource dataSource;
  private String vender;
  
  public DatabaseDetector() {
    
  }
  
  public DatabaseDetector(DataSource dataSource) {
    this.dataSource = dataSource;
  }
  
  void init() {
    if (StringUtils.isBlank(vender)) {
      Connection conn = null;
      try {
        conn = dataSource.getConnection();
        vender = conn.getMetaData().getDatabaseProductName();
      } catch (SQLException e) {
        throw new RuntimeException(e);
      } finally {
        try {
          conn.close();
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      }
      
      if (StringUtils.isBlank(vender)) {
        throw new RuntimeException("Do not find database name!");
      }
    }
  }
  
  public String getVender() {
    init();
    return vender;
  }
  
  public Boolean isH2() {
    init();
    return vender.toUpperCase().indexOf("H2") >= 0;
  }
  
  public Boolean isMySql() {
    init();
    return vender.toUpperCase().indexOf("MYSQL") >= 0;
  }
  
  public Boolean isOracle() {
    init();
    return vender.toUpperCase().indexOf("ORACLE") >= 0;
  }

  public void setDataSource(DataSource ds) {
    this.dataSource = ds;
  }
}
