package com.github.catstiger.common.sql.sync.mysql;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.persistence.JoinColumn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.github.catstiger.common.sql.NamingStrategy;
import com.github.catstiger.common.sql.ORMHelper;
import com.github.catstiger.common.sql.sync.ColumnCreator;
import com.github.catstiger.common.sql.sync.DatabaseInfo;
import com.github.catstiger.common.sql.sync.DbSync;
import com.github.catstiger.common.sql.sync.IndexCreator;
import com.github.catstiger.common.sql.sync.ManyToManyCreator;
import com.github.catstiger.common.sql.sync.ModelClassLoader;
import com.github.catstiger.common.sql.sync.TableCreator;
import com.github.catstiger.common.util.ReflectUtil;

public class MySqlSync implements DbSync, InitializingBean {
  private static Logger logger = LoggerFactory.getLogger(MySqlSync.class);

  private ModelClassLoader modelClassLoader;
  private DatabaseInfo databaseInfo;
  private TableCreator tableCreator;
  private ColumnCreator columnCreator;
  private IndexCreator indexCreator;
  private ManyToManyCreator m2mCreator;
  private String[] packagesToScan;
  private NamingStrategy namingStrategy;

  public MySqlSync() {

  }

  @Override
  public void afterPropertiesSet() throws Exception {
    modelClassLoader.scanPackages(packagesToScan);
  }

  /**
   * 设置Model类所在的包，支持ant格式的匹配，例如com.github.catstiger.**.model
   * 
   * @param packagesToScan 要扫描的package
   */
  public void setPackagesToScan(String[] packagesToScan) {
    this.packagesToScan = packagesToScan;
  }

  @Override
  public void sync() {
    ORMHelper ormHelper = ORMHelper.getInstance(namingStrategy);
    Set<String> tables = new HashSet<String>();
    Iterator<Class<?>> entityItr = modelClassLoader.getEntityClasses();
    // 创建所有表和字段
    while (entityItr.hasNext()) {
      Class<?> entityClass = entityItr.next();

      if (ormHelper.isEntity(entityClass)) {
        String table = ormHelper.tableNameByEntity(entityClass);
        if (tables.contains(table)) {
          throw new RuntimeException("表名重复，请检查实体类 " + entityClass.getName());
        }
        logger.debug("同步字段 {}", entityClass);
        syncEntity(entityClass);
        tables.add(table);
      }
    }

    // 创建所有表的外键
    entityItr = modelClassLoader.getEntityClasses();
    while (entityItr.hasNext()) {
      Class<?> entityClass = entityItr.next();

      if (ormHelper.isEntity(entityClass)) {
        logger.debug("同步外键 {}", entityClass);
        syncForeignKey(entityClass);
      }
    }

    // 创建索引
    entityItr = modelClassLoader.getEntityClasses();
    while (entityItr.hasNext()) {
      Class<?> entityClass = entityItr.next();

      if (ormHelper.isEntity(entityClass)) {
        logger.debug("同步索引 {}", entityClass);
        syncIndexes(entityClass);
      }
    }
    // 多对多
    entityItr = modelClassLoader.getEntityClasses();
    while (entityItr.hasNext()) {
      Class<?> entityClass = entityItr.next();

      if (ormHelper.isEntity(entityClass)) {
        logger.debug("many2many {}", entityClass);
        syncM2m(entityClass);
      }
    }
  }

  private void syncM2m(Class<?> entityClass) {

    Field[] fields = ReflectUtil.getFields(entityClass); // .getFields();
    for (Field field : fields) {
      m2mCreator.createCrossTable(entityClass, field.getName());
    }
  }

  private void syncEntity(Class<?> entityClass) {
    ORMHelper ormHelper = ORMHelper.getInstance(namingStrategy);
    String table = ormHelper.tableNameByEntity(entityClass);
    if (!databaseInfo.isTableExists(table)) {
      tableCreator.createTableIfNotExists(entityClass);
    } else {
      tableCreator.updateTable(entityClass);
    }
  }

  /**
   * 创建外键，多对一
   */
  private void syncForeignKey(Class<?> entityClass) {
    ORMHelper ormHelper = ORMHelper.getInstance(namingStrategy);
    Field[] fields = ReflectUtil.getFields(entityClass); // .getFields();
    for (Field field : fields) {
      if (ormHelper.isFieldIgnore(field)) {
        continue;
      }
      JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
      if (joinColumn == null) {
        Method getter = ormHelper.getAccessMethod(entityClass, field.getName());
        joinColumn = getter.getAnnotation(JoinColumn.class);
      }
      if (joinColumn == null) {
        continue;
      }

      columnCreator.addForeignKeyIfNotExists(entityClass, field.getName(), field.getType(), "id");
    }
  }

  private void syncIndexes(Class<?> entityClass) {
    ORMHelper ormHelper = ORMHelper.getInstance(namingStrategy);
    Field[] fields = ReflectUtil.getFields(entityClass);
    for (Field field : fields) {
      if (ormHelper.isFieldIgnore(field)) {
        continue;
      }
      JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
      if (joinColumn == null) {
        Method getter = ormHelper.getAccessMethod(entityClass, field.getName());
        joinColumn = getter.getAnnotation(JoinColumn.class);
      }
      if (joinColumn != null) { // 外键不重复建立索引
        continue;
      }
      indexCreator.addIndexIfNotExists(entityClass, field.getName());
    }
  }

  public void setNamingStrategy(NamingStrategy namingStrategy) {
    this.namingStrategy = namingStrategy;
  }

  public void setModelClassLoader(ModelClassLoader modelClassLoader) {
    this.modelClassLoader = modelClassLoader;
  }

  public void setDatabaseInfo(DatabaseInfo databaseInfo) {
    this.databaseInfo = databaseInfo;
  }

  public void setTableCreator(TableCreator tableCreator) {
    this.tableCreator = tableCreator;
  }

  public void setColumnCreator(ColumnCreator columnCreator) {
    this.columnCreator = columnCreator;
  }

  public void setIndexCreator(IndexCreator indexCreator) {
    this.indexCreator = indexCreator;
  }

  public void setM2mCreator(ManyToManyCreator m2mCreator) {
    this.m2mCreator = m2mCreator;
  }
}
