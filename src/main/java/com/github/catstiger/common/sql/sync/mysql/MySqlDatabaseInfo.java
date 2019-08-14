package com.github.catstiger.common.sql.sync.mysql;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import com.github.catstiger.common.sql.sync.DatabaseInfo;
import com.google.common.base.Joiner;

public class MySqlDatabaseInfo implements DatabaseInfo {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private JdbcTemplate jdbcTemplate;
  private String url;
  private String schema = null;

  // public static final String CATALOG_NAME = "def";
  private Set<String> tables = new HashSet<String>(100);
  private Set<String> views = new HashSet<String>(50);
  private Set<String> columns = new HashSet<String>(500);
  private Set<String> fks = new HashSet<String>(100);
  private Set<String> indexes = new HashSet<String>(100);

  public MySqlDatabaseInfo() {

  }

  public MySqlDatabaseInfo(JdbcTemplate jdbcTemplate, String url) {
    this.jdbcTemplate = jdbcTemplate;
    this.url = url;
  }

  @Override
  public String getSchema() {
    if (schema == null) {
      List<Map<String, Object>> list = jdbcTemplate.query(
          "select SCHEMA_NAME from information_schema.SCHEMATA " + " where locate(lower(concat('/', SCHEMA_NAME)), lower(?)) > 0", new Object[] { url },
          new ColumnMapRowMapper());

      if (list == null || list.isEmpty() || StringUtils.isBlank(list.get(0).get("SCHEMA_NAME").toString())) {
        throw new IllegalStateException("Can't get schema from information_schema");
      }
      schema = list.get(0).get("SCHEMA_NAME").toString();
      logger.info("SCHEMA : {}, {}", schema, url);
    }

    return schema;
  }

  @Override
  public Boolean isTableExists(String table) {
    if (StringUtils.isBlank(table)) {
      return false;
    }
    if (tables.contains(table.toLowerCase())) {
      return true;
    }
    List<Map<String, Object>> list = jdbcTemplate.query("select table_name from information_schema.tables " + " where table_schema=? and lower(table_name)=?",
        new Object[] { getSchema(), table.toLowerCase() }, new ColumnMapRowMapper());

    String tableName = null;
    if (list != null && !list.isEmpty() && StringUtils.isNotBlank(list.get(0).get("table_name").toString())) {
      tableName = list.get(0).get("table_name").toString().toLowerCase();
      tables.add(tableName.toLowerCase());
    }

    return tableName != null;
  }
  
  @Override
  public Boolean isViewExists(String view) {
    if (StringUtils.isBlank(view)) {
      return false;
    }
    if (views.contains(view.toLowerCase())) {
      return true;
    }
    Long count = jdbcTemplate.queryForObject("select count(*) from information_schema.views " + " where table_schema=? and lower(table_name)=?", Long.class,
        new Object[] { getSchema(), view.toLowerCase() });
    if (count > 0L) {
      views.add(view.toLowerCase());
    }
    return count > 0L;
  }

  @Override
  public Boolean isColumnExists(String table, String column) {
    if (StringUtils.isBlank(table) || StringUtils.isBlank(column)) {
      return false;
    }
    String tableColumn = (table + "/" + column).toLowerCase();
    if (columns.contains(tableColumn)) {
      return true;
    }
    List<Map<String, Object>> list = jdbcTemplate.query(
        "select column_name from information_schema.columns where " + " TABLE_SCHEMA=? and lower(TABLE_NAME)=? and lower(COLUMN_NAME)=?",
        new Object[] { getSchema(), table.toLowerCase(), column.toLowerCase() }, new ColumnMapRowMapper());

    if (list != null && !list.isEmpty() && StringUtils.isNotBlank(list.get(0).get("column_name").toString())) {
      columns.add((table + "/" + column).toLowerCase());
      return true;
    }

    return false;
  }

  @Override
  public Boolean isForeignKeyExists(String table, String column, String refTable, String refColumn) {
    String[] args = { "FK", table, column, refTable, refColumn };
    String key = Joiner.on("_").join(args);
    if (fks.contains(key.toLowerCase())) {
      return true;
    }
    Long c = jdbcTemplate.queryForObject(
        "select count(*) from information_schema.REFERENTIAL_CONSTRAINTS a," + " information_schema.INNODB_SYS_FOREIGN_COLS b where "
            + " concat(?,'/',a.CONSTRAINT_NAME)=b.id and lower(b.for_col_name)=? "
            + " and lower(b.REF_COL_NAME)=? and lower(a.table_name)=? and lower(a.REFERENCED_TABLE_NAME)=?",
        new Object[] { getSchema(), column.toLowerCase(), refColumn.toLowerCase(), table.toLowerCase(), refTable.toLowerCase() }, Long.class);

    if (c != null && c > 0L) {
      fks.add(key.toLowerCase());
      return true;
    }
    return false;
  }

  @Override
  public Boolean isIndexExists(String tableName, String indexName, boolean unique) {
    Objects.requireNonNull(tableName);
    Objects.requireNonNull(indexName);
    String key = (tableName + "/" + indexName).toLowerCase();
    if (indexes.contains(key)) {
      return true;
    }

    Long c = jdbcTemplate.queryForObject(
        "select count(*) from information_schema.INNODB_SYS_TABLES a,information_schema.INNODB_SYS_INDEXES b "
            + " where b.table_id=a.table_id and lower(b.name)=? and lower(a.name)=concat(?, '/', ?)",
        new Object[] { indexName.toLowerCase(), getSchema(), tableName.toLowerCase() }, Long.class);

    if (c != null && c > 0L) {
      indexes.add(key);
      return true;
    }
    return false;
  }

  public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public void setUrl(String url) {
    this.url = url;
  }

}
