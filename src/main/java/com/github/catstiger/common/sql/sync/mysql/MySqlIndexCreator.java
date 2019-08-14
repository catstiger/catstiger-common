package com.github.catstiger.common.sql.sync.mysql;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.persistence.JoinColumn;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import com.github.catstiger.common.sql.NamingStrategy;
import com.github.catstiger.common.sql.ORMHelper;
import com.github.catstiger.common.sql.annotation.Index;
import com.github.catstiger.common.sql.sync.DDLExecutor;
import com.github.catstiger.common.sql.sync.DatabaseInfo;
import com.github.catstiger.common.sql.sync.IndexCreator;
import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;

public class MySqlIndexCreator implements IndexCreator {
  private static Logger logger = LoggerFactory.getLogger(MySqlIndexCreator.class);

  private Boolean strongReferences = false;
  private DDLExecutor executor;
  private DatabaseInfo databaseInfo;
  private NamingStrategy namingStrategy;

  public MySqlIndexCreator() {

  }
  
  /**
   * 如果{@code entityClass}和{@code fieldName} 所代表的表和字段索引不存在则创建
   */
  @Override
  public void addIndexIfNotExists(Class<?> entityClass, String fieldName) {
    ORMHelper ormHelper = ORMHelper.getInstance(namingStrategy);
    if (ormHelper.isFieldIgnore(entityClass, fieldName)) {
      return;
    }
    
    Field field = ReflectionUtils.findField(entityClass, fieldName);
    Method getter = ReflectionUtils.findMethod(entityClass, "get" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, fieldName));
    Index index = getter.getAnnotation(Index.class);
    if (index == null) {
      index = field.getAnnotation(Index.class);
    }
    String table = ormHelper.tableNameByEntity(entityClass);
    String column = ormHelper.columnNameByField(entityClass, field.getName());

    if (index == null) { //没有@Index标注， 并且是外键，为外键创建索引
      if (!strongReferences) {
        // 如果不创建外键约束，则引用字段建立索引
        if (getter.getAnnotation(JoinColumn.class) != null || field.getAnnotation(JoinColumn.class) != null) {
          createIndexForFk(table, column);
        }
      }
    } else { //根据@Index创建
      createIndexForAnn(index, table, column);
    }
  }
  
  /**
   * 根据@Index创建索引
   */
  private void createIndexForAnn(Index index, String table, String column) {
    String[] columns = index.columnNames();
    if (columns == null || columns.length == 0) {
      columns = new String[] { column };
    }
    String name = index.name();
    if (StringUtils.isBlank(name)) {
      name = new StringBuilder(200).append("idx_").append(table).append("_").append(Joiner.on("_").join(columns)).toString();
      name = simpleIndexName(name, table); // 让索引名称符合MYSQL规定
      // 当索引不存在的时候创建
      if (!databaseInfo.isIndexExists(table, name, index.unique())) {
        String sql = new StringBuilder(200).append("create ").append(index.unique() ? "unique " : "").append("index ").append(name).append(" on ")
            .append(table).append("(").append(Joiner.on(",").join(columns)).append(");").toString();
        logger.info("创建索引 {}, {} on {}", name, table, Joiner.on(",").join(columns));
        try {
          executor.execute(sql);
        } catch (Exception e) {
          logger.error(e.getMessage());
        }
      }
    }
  }
  
  /**
   * 为外键创建索引
   */
  private void createIndexForFk(String table, String column) {
    String name = "idx_fk_" + table + "_" + column.toLowerCase();
    name = simpleIndexName(name, table); // 让索引名称符合MYSQL规定
    if (!databaseInfo.isIndexExists(table, name, false)) { // 当索引不存在的时候创建
      String sql = new StringBuilder(100).append("create index ")
          .append(name).append(" on ").append(table).append("(").append(column.toLowerCase())
          .append(");").toString();
      logger.info("创建索引 {}, {} on {}", name, table, column);
      try {
        executor.execute(sql);
      } catch (Exception e) {
        logger.error(e.getMessage());
      }
    }
  }

  private static String simpleIndexName(String name, String table) {
    if (name == null || name.length() == 0) {
      throw new IllegalArgumentException("索引名称不可为空");
    }
    final int maxLengthOfIndex = 64;
    final String splitter = "_";

    if (name.length() < maxLengthOfIndex) {
      return name;
    }

    StringBuilder builder = new StringBuilder(name.length()).append(name.substring(0, name.indexOf(table) + table.length())).append(splitter);
    String[] sub = StringUtils.split(name.substring(name.indexOf(table) + table.length(), name.length()), splitter);
    if (sub == null) {
      return name;
    }
    for (String s : sub) {
      builder.append(s.charAt(0));
    }
    return builder.toString();
  }

  public void setNamingStrategy(NamingStrategy namingStrategy) {
    this.namingStrategy = namingStrategy;
  }

  public void setStrongReferences(Boolean strongReferences) {
    this.strongReferences = strongReferences;
  }

  public void setExecutor(DDLExecutor executor) {
    this.executor = executor;
  }

  public void setDatabaseInfo(DatabaseInfo databaseInfo) {
    this.databaseInfo = databaseInfo;
  }
  
}
