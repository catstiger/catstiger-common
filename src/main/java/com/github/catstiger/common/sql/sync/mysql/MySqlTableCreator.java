package com.github.catstiger.common.sql.sync.mysql;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.catstiger.common.sql.NamingStrategy;
import com.github.catstiger.common.sql.ORMHelper;
import com.github.catstiger.common.sql.sync.ColumnCreator;
import com.github.catstiger.common.sql.sync.DDLExecutor;
import com.github.catstiger.common.sql.sync.DatabaseInfo;
import com.github.catstiger.common.sql.sync.TableCreator;
import com.github.catstiger.common.util.ReflectUtil;
import com.google.common.base.Joiner;

public class MySqlTableCreator implements TableCreator {
  private Logger logger = LoggerFactory.getLogger(MySqlTableCreator.class);

  private DatabaseInfo databaseInfo;
  private ColumnCreator columnCreator;
  private NamingStrategy namingStrategy;
  private DDLExecutor executor;

  public MySqlTableCreator() {

  }

  @Override
  public void createTableIfNotExists(Class<?> entityClass) {
    ORMHelper ormHelper = ORMHelper.getInstance(namingStrategy);
    String table = ormHelper.tableNameByEntity(entityClass);
    Field[] fields = ReflectUtil.getFields(entityClass);

    if (!this.isTableExists(entityClass)) {
      StringBuilder sqlBuf = new StringBuilder(500).append("create table ").append(table).append("(\n");
      List<String> sqls = new ArrayList<String>(fields.length); // SQL片段
      for (Field field : fields) {
        if (ormHelper.isFieldIgnore(field)) {
          continue;
        }
        String sqlFregment = columnCreator.getColumnSqlFragment(entityClass, field.getName());
        logger.debug("SQL Fregment {}", sqlFregment);
        if (StringUtils.isBlank(sqlFregment)) {
          continue;
        }
        sqls.add(sqlFregment);
      }
      sqlBuf.append(Joiner.on(",\n").join(sqls)).append("); \n");
      logger.debug("创建表{}, {}", entityClass.getName(), sqlBuf);
      try {
        executor.execute(sqlBuf.toString());
      } catch (Exception e) {
        logger.error(e.getMessage());
      }
    }
  }

  @Override
  public Boolean isTableExists(Class<?> entityClass) {
    ORMHelper ormHelper = ORMHelper.getInstance(namingStrategy);
    String table = ormHelper.tableNameByEntity(entityClass);
    return databaseInfo.isTableExists(table);
  }

  @Override
  public void updateTable(Class<?> entityClass) {
    ORMHelper ormHelper = ORMHelper.getInstance(namingStrategy);
    Field[] fields = ReflectUtil.getFields(entityClass);
    if (this.isTableExists(entityClass)) {
      for (Field field : fields) {
        if (ormHelper.isFieldIgnore(field)) {
          continue;
        }
        columnCreator.addColumnIfNotExists(entityClass, field.getName());
      }
    }

  }

  public void setNamingStrategy(NamingStrategy namingStrategy) {
    this.namingStrategy = namingStrategy;
  }

  public void setDatabaseInfo(DatabaseInfo databaseInfo) {
    this.databaseInfo = databaseInfo;
  }

  public void setColumnCreator(ColumnCreator columnCreator) {
    this.columnCreator = columnCreator;
  }

  public void setExcecutor(DDLExecutor ddlExecutor) {
    this.executor = ddlExecutor;
  }

}
