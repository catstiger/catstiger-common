package com.github.catstiger.common.sql.sync.mysql;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.persistence.ManyToMany;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import com.github.catstiger.common.sql.NamingStrategy;
import com.github.catstiger.common.sql.ORMHelper;
import com.github.catstiger.common.sql.sync.DDLExecutor;
import com.github.catstiger.common.sql.sync.DatabaseInfo;
import com.github.catstiger.common.sql.sync.ManyToManyCreator;

public class MySqlManyToManyCreator implements ManyToManyCreator {
  private static Logger logger = LoggerFactory.getLogger(MySqlManyToManyCreator.class);

  private Boolean strongReferences = false;
  private DatabaseInfo databaseInfo;
  private NamingStrategy namingStrategy;

  private DDLExecutor executor;

  public MySqlManyToManyCreator() {

  }

  @Override
  public void createCrossTable(Class<?> entityClass, String fieldName) {
    ORMHelper ormHelper = ORMHelper.getInstance(namingStrategy);
    Field field = ReflectionUtils.findField(entityClass, fieldName);
    ManyToMany m2m = field.getAnnotation(ManyToMany.class);
    if (m2m == null) {
      Method getter = ormHelper.getAccessMethod(entityClass, fieldName);
      if (getter != null) {
        m2m = getter.getAnnotation(ManyToMany.class);
      }
    }

    if (m2m == null) {
      return;
    }

    Class<?> targetEntity = m2m.targetEntity();
    if (targetEntity == null || targetEntity == void.class) {
      return;
    }
    String mappedBy = m2m.mappedBy();
    if (StringUtils.isBlank(mappedBy)) {
      return;
    }
    String masterTable = ormHelper.tableNameByEntity(targetEntity);
    String masterColumn = masterTable + "_id";
    String slaveTable = ormHelper.tableNameByEntity(entityClass);
    String slaveColumn = slaveTable + "_id";
    String crossTable = masterTable + "_" + slaveTable;

    if (databaseInfo.isTableExists(crossTable)) {
      logger.debug("交叉表已经存在 {}", crossTable);
      return;
    }

    // 创建交叉表
    StringBuilder sqlBuilder = new StringBuilder(100).append("create table ").append(crossTable).append("(").append(masterColumn)
        .append(" bigint,").append(slaveColumn).append(" bigint);");
    logger.debug("创建交叉表 {}, {}", entityClass.getSimpleName(), sqlBuilder);
    executor.execute(sqlBuilder.toString());
    // 交叉表唯一索引
    StringBuilder sqlIndex = new StringBuilder(100).append("ALTER TABLE ").append(crossTable).append("  ADD UNIQUE ( ").append(masterColumn)
        .append(",").append(slaveColumn).append(");");
    logger.debug("创建交叉表索引 {}", sqlIndex);
    executor.execute(sqlIndex.toString());

    String keyMaster = new StringBuilder(20).append("fk_").append(ormHelper.simpleName(crossTable)).append("_")
        .append(ormHelper.simpleName(masterColumn)).append("_").append(ormHelper.simpleName(masterTable)).toString();

    String keySlave = new StringBuilder(20).append("fk_").append(ormHelper.simpleName(crossTable)).append("_")
        .append(ormHelper.simpleName(slaveColumn)).append("_").append(ormHelper.simpleName(slaveTable)).toString();
    // 外键
    if (strongReferences) {
      StringBuilder sqlFk = new StringBuilder(200).append("ALTER TABLE ").append(crossTable).append(" ADD CONSTRAINT ").append(keyMaster)
          .append(" FOREIGN KEY (").append(masterColumn).append(") REFERENCES ").append(masterTable).append("(id);");
      logger.debug("创建交叉表外键 {}", sqlFk);
      executor.execute(sqlFk.toString());

      sqlFk = new StringBuilder(200).append("ALTER TABLE ").append(crossTable).append(" ADD CONSTRAINT ").append(keySlave)
          .append(" FOREIGN KEY (").append(slaveColumn).append(") REFERENCES ").append(slaveTable).append("(id);");
      logger.debug("创建交叉表外键 {}", sqlFk);
      executor.execute(sqlFk.toString());
    }

  }

  public void setNamingStrategy(NamingStrategy namingStrategy) {
    this.namingStrategy = namingStrategy;
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

}
