package com.github.catstiger.common.sql.sync.mysql;

import java.io.IOException;

import org.springframework.jdbc.core.JdbcTemplate;

import com.github.catstiger.common.sql.NamingStrategy;
import com.github.catstiger.common.sql.sync.DDLExecutor;
import com.github.catstiger.common.sql.sync.DatabaseInfo;
import com.github.catstiger.common.sql.sync.ModelClassLoader;
import com.zaxxer.hikari.HikariDataSource;

/**
 * 执行数据库DDL同步操作
 * 
 * @author catstiger
 *
 */
public abstract class MySqlSyncOperator {

  /**
   * 将数据库结构与系统中的实体类同步
   * 
   * @param connInfo 数据库连接参数，包括driver, url, username, password
   * @param executor DDL SQL执行器，不同的执行器可以决定是直接执行SQL还是打印SQL
   * @param namingStrategy 命名策略
   * @param strongReferences 是否强关联，如果为{@code true}, 则创建外键关联，否则只创建一个索引。
   * @param packagesToScan 实体类所在包，例如：com.github.catstiger.**.model
   */
  public static void sync(DbConnInfo connInfo, DDLExecutor executor, NamingStrategy namingStrategy, boolean strongReferences,
      String... packagesToScan) throws IOException {
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setDriverClassName(connInfo.driver);
    dataSource.setJdbcUrl(connInfo.url);
    dataSource.setUsername(connInfo.user);
    dataSource.setPassword(connInfo.password);
    dataSource.setMaximumPoolSize(5);

    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    // 把DDLSQL写在文件中

    DatabaseInfo dbInfo = new MySqlDatabaseInfo(jdbcTemplate, connInfo.url);

    MySqlIndexCreator indexCreator = new MySqlIndexCreator();
    indexCreator.setDatabaseInfo(dbInfo);
    indexCreator.setExecutor(executor);
    indexCreator.setNamingStrategy(namingStrategy);
    indexCreator.setStrongReferences(strongReferences);

    MySqlColumnCreator columnCreator = new MySqlColumnCreator();
    columnCreator.setDatabaseInfo(dbInfo);
    columnCreator.setIndexCreator(indexCreator);
    columnCreator.setExcecutor(executor);
    columnCreator.setStrongReferences(strongReferences);

    MySqlManyToManyCreator m2mCreator = new MySqlManyToManyCreator();
    m2mCreator.setDatabaseInfo(dbInfo);
    m2mCreator.setExcecutor(executor);
    m2mCreator.setNamingStrategy(namingStrategy);
    m2mCreator.setStrongReferences(strongReferences);

    MySqlTableCreator tableCreator = new MySqlTableCreator();
    tableCreator.setColumnCreator(columnCreator);
    tableCreator.setDatabaseInfo(dbInfo);
    tableCreator.setExcecutor(executor);
    tableCreator.setNamingStrategy(namingStrategy);

    MySqlSync mySqlSync = new MySqlSync();
    mySqlSync.setColumnCreator(columnCreator);
    mySqlSync.setDatabaseInfo(dbInfo);
    mySqlSync.setIndexCreator(indexCreator);
    mySqlSync.setM2mCreator(m2mCreator);
    mySqlSync.setModelClassLoader(new ModelClassLoader());
    mySqlSync.setNamingStrategy(namingStrategy);
    mySqlSync.setTableCreator(tableCreator);
    mySqlSync.setPackagesToScan(packagesToScan);

    try {
      mySqlSync.afterPropertiesSet();
      mySqlSync.sync();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      dataSource.close();
    }
  }

  public static class DbConnInfo {
    private String driver;
    private String url;
    private String user;
    private String password;

    public DbConnInfo() {

    }

    public String getDriver() {
      return driver;
    }

    public void setDriver(String driver) {
      this.driver = driver;
    }

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }

    public String getUser() {
      return user;
    }

    public void setUser(String user) {
      this.user = user;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }
  }
}
