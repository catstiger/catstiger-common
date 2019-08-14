package com.github.catstiger.common.sql.sync.mysql;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import com.github.catstiger.common.sql.NamingStrategy;
import com.github.catstiger.common.sql.ORMHelper;
import com.github.catstiger.common.sql.annotation.AutoId;
import com.github.catstiger.common.sql.sync.ColumnCreator;
import com.github.catstiger.common.sql.sync.DDLExecutor;
import com.github.catstiger.common.sql.sync.DatabaseInfo;
import com.github.catstiger.common.sql.sync.IndexCreator;

public class MySqlColumnCreator implements ColumnCreator {
  private static Logger logger = LoggerFactory.getLogger(MySqlColumnCreator.class);
  private Boolean strongReferences = false;
  private DatabaseInfo databaseInfo;
  private IndexCreator indexCreator;
  private NamingStrategy namingStrategy;
  private DDLExecutor executor;

  @Override
  public void addColumnIfNotExists(Class<?> entityClass, String field) {
    ORMHelper ormHelper = ORMHelper.getInstance(namingStrategy);
    if (ormHelper.isFieldIgnore(entityClass, field)) {
      return;
    }

    if (!isColumnExists(entityClass, field)) {
      StringBuilder sqlBuilder = new StringBuilder(100).append("alter table ").append(ormHelper.tableNameByEntity(entityClass)).append(" add column (")
          .append(getColumnSqlFragment(entityClass, field)).append(");");
      logger.debug("新增字段 : {}", sqlBuilder);
      executor.execute(sqlBuilder.toString());
    }
  }

  @Override
  public String getColumnSqlFragment(Class<?> entityClass, String fieldName) {
    Objects.requireNonNull(entityClass);
    Objects.requireNonNull(fieldName);

    ORMHelper ormHelper = ORMHelper.getInstance(namingStrategy);
    if (!ormHelper.isEntity(entityClass)) {
      throw new RuntimeException(entityClass.getName() + " 不是实体类！");
    }
    Field field = ReflectionUtils.findField(entityClass, fieldName); // 属性
    if (ormHelper.isFieldIgnore(field)) {
      return StringUtils.EMPTY;
    }

    String name = ormHelper.columnNameByField(entityClass, fieldName); // 对应的字段名
    Method getter = ormHelper.getAccessMethod(entityClass, fieldName); // 对应的getter方法

    Column colAnn = getter.getAnnotation(Column.class); // Column标注
    JoinColumn joinColAnn = getter.getAnnotation(JoinColumn.class); // 外键标注
    Lob lobAnn = getter.getAnnotation(Lob.class); // Lob标注
   
    Entity refEntityAnn = field.getType().getAnnotation(Entity.class); // 外键

    int length = 255;
    int precision = 0;
    int scale = 0;
    boolean nullable = true;
    boolean unique = false;
    String columnDef = "";

    if (colAnn != null) {
      length = colAnn.length();
      precision = colAnn.precision();
      scale = colAnn.scale();
      nullable = colAnn.nullable();
      unique = colAnn.unique();
      columnDef = colAnn.columnDefinition();
    } else if (joinColAnn != null) {
      nullable = joinColAnn.nullable();
      unique = joinColAnn.unique();
      columnDef = joinColAnn.columnDefinition();
    }

    StringBuilder sql = new StringBuilder(100).append(name).append(" ");
    Class<?> type = field.getType();
    //处理定义部分
    doDefinition(sql, columnDef, type, precision, scale, lobAnn, refEntityAnn, length);

    if (!nullable) {
      sql.append(" not null ");
    }

    if (unique) {
      sql.append(" unique ");
    }
    AutoId autoId = getter.getAnnotation(AutoId.class); // 是否自增
    if (autoId != null) {
      sql.append(" auto_increment ");
    }

    Id id = getter.getAnnotation(Id.class); // 是否主键
    if (id != null) {
      sql.append(" primary key ");
    }

    return sql.toString();
  }
  
  private void doDefinition(StringBuilder sql, String columnDef, Class<?> type, int precision, int scale, Lob lobAnn, Entity refEntityAnn, int length) {
    if (StringUtils.isNotBlank(columnDef)) { // 字符串类型
      sql.append(columnDef);
    } else if (type == String.class) {
      doString(sql, lobAnn, length);
    } else if (isFloat(type)) { // 浮点类型
      doFloat(sql, precision, scale, type);
    } else if (isLong(type)) { // 长整型
      doLong(sql, precision);
    } else if (isInteger(type)) { // 整型
      doInteger(sql, precision);
    } else if (isShort(type)) { // 短整型
      doShort(sql, precision);
    } else if (isBoolean(type)) { // boolean
      sql.append("tinyint(1)");
    } else if (type == Date.class || type == Timestamp.class) { // 日期
      sql.append("datetime");
    } else if (refEntityAnn != null) { // 外键
      sql.append("bigint");
    }
  }
  
  private boolean isBoolean(Class<?> type) {
    return type == Boolean.class || type == boolean.class;
  }
  
  private boolean isShort(Class<?> type) {
    return type == Short.class || type == short.class;
  }
  
  private boolean isInteger(Class<?> type) {
    return type == Integer.class || type == int.class;
  }
  
  private boolean isLong(Class<?> type) {
    return (type == Long.class || type == long.class);
  }
  
  private boolean isFloat(Class<?> type) {
    return type == Double.class || type == double.class || type == Float.class || type == float.class;
  }
  
  private void doString(StringBuilder sql, Lob lobAnn, int length) {
    if (lobAnn != null) {
      sql.append("text");
    } else {
      sql.append("varchar(").append(length).append(")");
    }
  }
  
  private void doShort(StringBuilder sql, int precision) {
    if (precision > 0) {
      sql.append("numeric(").append(precision).append(")");
    } else {
      sql.append("tinyint");
    }
  }

  private void doInteger(StringBuilder sql, int precision) {
    if (precision > 0) {
      sql.append("numeric(").append(precision).append(")");
    } else {
      sql.append("int");
    }
  }

  private void doLong(StringBuilder sql, int precision) {
    if (precision > 0) {
      sql.append("numeric(").append(precision).append(")");
    } else {
      sql.append("bigint");
    }
  }
  
  private void doFloat(StringBuilder sql, int precision, int scale, Class<?> type) {
    if (precision > 0 && scale == 0) {
      sql.append("numeric(").append(precision).append(")");
    } else if (precision > 0 && scale > 0) {
      sql.append("numeric(").append(precision).append(",").append(scale).append(")");
    } else if (type == double.class || type == Double.class) {
      sql.append("double");
    } else if (type == Float.class || type == float.class) {
      sql.append("float");
    }
  }

  @Override
  public void addForeignKeyIfNotExists(Class<?> entityClass, String field, Class<?> refClass, String refField) {
    ORMHelper ormHelper = ORMHelper.getInstance(namingStrategy);
    if (ormHelper.isEntityIgnore(entityClass)) {
      return;
    }

    if (ormHelper.isFieldIgnore(entityClass, field)) {
      return;
    }

    if (!this.strongReferences) { // 非强关联，不建立外键
      indexCreator.addIndexIfNotExists(entityClass, field); // 外键需要创建索引
      return;
    }
    String table = ormHelper.tableNameByEntity(entityClass);
    String refTable = ormHelper.tableNameByEntity(refClass);
    String column = ormHelper.columnNameByField(entityClass, field);
    String refColumn = ormHelper.columnNameByField(refClass, refField);

    if (!databaseInfo.isForeignKeyExists(table, column, refTable, refColumn)) {
      String fkName = new StringBuilder(30).append("fk_").append(table).append("_").append(column).append("_").append(refTable).toString();
      StringBuilder sqlBuilder = new StringBuilder(200).append("ALTER TABLE ").append(table).append(" ADD CONSTRAINT ").append(fkName).append(" FOREIGN KEY (")
          .append(column).append(") REFERENCES ").append(refTable).append("(").append(refColumn).append(");");
      logger.debug("新增外键 {}", sqlBuilder.toString());
      executor.execute(sqlBuilder.toString());
    }
  }

  @Override
  public Boolean isColumnExists(Class<?> entityClass, String field) {
    ORMHelper ormHelper = ORMHelper.getInstance(namingStrategy);
    String table = ormHelper.tableNameByEntity(entityClass);
    String colname = ormHelper.columnNameByField(entityClass, field);
    return databaseInfo.isColumnExists(table, colname);
  }

  public NamingStrategy getNamingStrategy() {
    return namingStrategy;
  }

  public void setStrongReferences(Boolean strongReferences) {
    this.strongReferences = strongReferences;
  }

  public void setExcecutor(DDLExecutor ddlExecutor) {
    this.executor = ddlExecutor;
  }

  public void setDatabaseInfo(DatabaseInfo databaseInfo) {
    this.databaseInfo = databaseInfo;
  }

  public void setIndexCreator(IndexCreator indexCreator) {
    this.indexCreator = indexCreator;
  }
}
