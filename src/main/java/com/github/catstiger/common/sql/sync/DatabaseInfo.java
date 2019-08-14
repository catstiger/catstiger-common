package com.github.catstiger.common.sql.sync;

public interface DatabaseInfo {
  String getSchema();

  Boolean isTableExists(String table);
  
  Boolean isViewExists(String view);

  Boolean isColumnExists(String table, String column);

  Boolean isForeignKeyExists(String table, String column, String refTable, String refColumn);

  Boolean isIndexExists(String tableName, String indexName, boolean unique);
}
