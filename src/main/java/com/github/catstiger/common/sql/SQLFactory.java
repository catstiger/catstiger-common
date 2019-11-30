package com.github.catstiger.common.sql;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.DigestUtils;
import org.springframework.util.ReflectionUtils;

import com.github.catstiger.common.sql.limit.LimitSQL;
import com.github.catstiger.common.util.Exceptions;
import com.github.catstiger.common.util.IDUtil;
import com.github.catstiger.common.util.ReflectUtil;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

public final class SQLFactory {
  private static Map<String, String> sqlCache = new ConcurrentHashMap<String, String>();
  
  private SQLFactory() {
  }

  private static volatile SQLFactory instance;

  /**
   * 返回{@code SQLFactory}的实例
   */
  public static SQLFactory getInstance() {
    if (instance == null) {
      synchronized(SQLFactory.class) {
        if (instance == null) {
          instance = new SQLFactory();
        }
      }
    }

    return instance;
  }

  /**
   * 根据实体类的属性，构造一个SELECT SQL语句：
   * <ul>
   * <li>表名根据类名取得，如果被@Table标注，则取@Table规定的类名。</li>
   * <li>字段名根据类的Field确定，如果Field的Reader方法被@Column,@JoinColumn标注，则取标注的字段名。</li>
   * <li>如果Field的Reader方法被@Transient标注，则忽略此字段。</li>
   * <li>如果Field是一个指向其他实体类的属性，则取@JoinColumn作为类名，如果没有@JoinColumn作为标注，则取引用的表名+id作为字段名，例如：User.getDept(),对应的字段为dept_id。</li>
   * <li>字段别名就是实体类中的字段名，表的别名，就是实体类的类名各个单词的字头小写+下划线</li>
   * <li>表名，字段名，全部小写，关键字大写。</li>
   * </ul>
   * 
   * @param sqlRequest SQL请求对象 
   * @return SQL
   */
  public SQLReady select(SQLRequest sqlRequest) {
    String key = sqlKey(sqlRequest, "select");
    // 从缓存中取得SQL
    String sqlObj = sqlCache.get(key);
    if (sqlObj != null) {
      return new SQLReady(sqlObj, new Object[] {}, sqlRequest.getLimitSql());
    }
    //缓存中木有
    Collection<ColField> colFields = columns(sqlRequest);
    String tablename = sqlRequest.getNamingStrategy().tablename(sqlRequest.getEntityClass());
    String tableAlias = this.buildTableAlias(sqlRequest); //表名别名
    
    final StringBuilder sqlBuf = new StringBuilder(1000).append("SELECT ");
    List<String> sqlSegs = new ArrayList<String>(colFields.size()); // 一个一个SQL片段，最后join为完整的字段列表
   
    // SELECT后的字段列表
    for (Iterator<ColField> itr = colFields.iterator(); itr.hasNext();) {
      ColField colField = itr.next();
      String sqlSeg = selectCols(sqlRequest, tableAlias, colField);
      sqlSegs.add(sqlSeg.toString());
    }
    
    sqlBuf.append(Joiner.on(",\n ").join(sqlSegs));

    // FROM后面的主表
    sqlBuf.append(" \nFROM ").append(tablename).append(" ").append(tableAlias).append(" \n");

    ColField primary = findPrimary(colFields);

    if (sqlRequest.isById()) {
      sqlBuf.append(selectById(sqlRequest, primary, tableAlias)); //where id=?
    }
    sqlBuf.append("\n");
    sqlCache.put(key, sqlBuf.toString()); // 装入缓存

    return new SQLReady(sqlBuf.toString(), new Object[] {}, sqlRequest.getLimitSql());
  }
  
  /**
   * 根据给定的实体类，构造一个SQL INSERT语句，
   * 如果给出的SQLRequest对象中，namedParams为<code>true</code>，则返回带有参数的SQL，数据使用MAP封装，否则返回带有?的SQL，数据采用数组封装。
   * 
   * @param sqlRequest 给定SQLRequest
   * @return SQLReady 包括SQL和参数，如果SQLRequest.namedParams为<code>true</code>,
   *         SQL语句使用属性名作为字段别名和占位符，参数采用Map存储，Key为属性名。 否则，SQL语句采用?作为占位符，参数用数组保存。
   */
  @SuppressWarnings("deprecation")
  public SQLReady insert(SQLRequest sqlRequest) {
    if (sqlRequest.getEntity() == null) {
      throw new java.lang.IllegalArgumentException("给出的实体类不可为空。");
    }

    Object entity = sqlRequest.getEntity();
    if ((entity instanceof BaseEntity) && ((BaseEntity) entity).getId() == null) {
      ((BaseEntity) entity).setId(IDUtil.nextId());
    }

    Class<?> entityClass = sqlRequest.getEntityClass();
    String tablename = sqlRequest.getNamingStrategy().tablename(entityClass);
    Collection<ColField> colFields = columns(sqlRequest);

    List<Object> args = new ArrayList<Object>(colFields.size()); // 使用？做占位符
    Map<String, Object> namedParams = new LinkedHashMap<>(colFields.size()); // 使用别名做占位符
    // 字段列表
    List<String> prevValues = new ArrayList<String>(colFields.size());
    // 占位符
    List<String> afterValues = new ArrayList<String>(colFields.size());

    for (Iterator<ColField> itr = colFields.iterator(); itr.hasNext();) {
      ColField colField = itr.next();
      Object argumentValue = getField(colField, entity); // 可以加载字段值或者外键指向的实体类的主键值

      if (argumentValue == null && !sqlRequest.isIncludesNull()) { // 不包括NULL字段
        continue;
      }
      prevValues.add(colField.col);

      if (sqlRequest.isNamedParams()) { // 使用别名，参数存放在Map中，别名为KEY
        namedParams.put(colField.fieldname, argumentValue);
        afterValues.add(":" + colField.fieldname);
      } else { // 使用？
        args.add(argumentValue);
        afterValues.add("?");
      }
    }
    if (prevValues.isEmpty() || afterValues.isEmpty()) {
      throw new IllegalStateException("无法构造有效的INSERT语句。");
    }

    String cols = Joiner.on(",").join(prevValues);
    String values = Joiner.on(",").join(afterValues);

    StringBuilder sqlBuf = new StringBuilder(200).append("INSERT INTO ").append(tablename).append(" (\n").append(cols)
        .append(") VALUES (\n").append(values).append(")");

    if (sqlRequest.isNamedParams()) {
      return new SQLReady(sqlBuf.toString(), namedParams, sqlRequest.getLimitSql());
    } else {
      return new SQLReady(sqlBuf.toString(), args.toArray(new Object[] {}), sqlRequest.getLimitSql());
    }
  }

  /**
   * 生成Insert SQL，忽略为<code>null</code>的字段。
   * 如果给出的SQLRequest对象中，namedParams为<code>true</code>，则返回带有参数的SQL，数据使用MAP封装，否则返回带有?的SQL，数据采用数组封装。
   * 
   * @param sqlRequest 给定SQLRequest对象，包含生成SQL的各种条件
   * @return SQLReady contains sql and parameters.
   */
  public SQLReady insertNonNull(SQLRequest sqlRequest) {
    sqlRequest = sqlRequest.includesNull(false);
    return insert(sqlRequest);
  }

  /**
   * 根据SQLRequest构造一个SQL UPDATE语句及其对应的参数数组。
   * <ul>
   * <li>在SET子句中忽略主键字段</li>
   * <li>根据sqlRequest的设置，可以只处理不为空的字段</li>
   * <li>根据SQLRequest中的NamingStrategy，构造各个字段的名字</li>
   * <li>根据sqlRequest的设置，如果byId为true，并且，SQLRequest#entity的主键不为空，则自动追加WHERE
   * id=?子句，并且在参数中加入ID值</li>
   * </ul>
   * 
   */
  @SuppressWarnings("deprecation")
  public SQLReady update(SQLRequest sqlRequest) {
    Preconditions.checkNotNull(sqlRequest.getEntity(), "给出的实体类不可为空。");
    
    Collection<ColField> colFields = columns(sqlRequest);
    if (colFields == null || colFields.isEmpty()) {
      throw new java.lang.IllegalArgumentException("无法获取实体类的属性。");
    }
    List<Object> args = new ArrayList<>(colFields.size()); // 存放SQL对应的参数
    Map<String, Object> namedParams = new LinkedHashMap<>(colFields.size()); // 使用别名做占位符
    List<String> sqls = new ArrayList<>(colFields.size()); // 存放col=?

    for (ColField colField : colFields) {
      buildUpdateBody(colField, sqlRequest, namedParams, sqls, args);
    }
    if (sqls.isEmpty()) {
      throw new IllegalStateException("无法构造有效的UPDATE语句。");
    }
    Class<?> entityClass = sqlRequest.getEntityClass();
    String tablename = sqlRequest.getNamingStrategy().tablename(entityClass);

    final StringBuilder sqlBuf = new StringBuilder(100).append("UPDATE ").append(tablename).append(" SET ");
    sqlBuf.append(Joiner.on(",").join(sqls));
    // ByID更新
    if (sqlRequest.isById()) {
      if (!(sqlRequest.getEntity() instanceof BaseEntity) || sqlRequest.getEntity() == null) {
        throw Exceptions.unchecked("给定的映射类，不是BaseEntity的子类，无法使用byId更新。");
      }
      Object entity = sqlRequest.getEntity();
      Long id = ((BaseEntity) entity).getId();

      ColField primary = findPrimary(colFields);
      String idCol = (primary != null ? primary.col : "id");
      sqlBuf.append(" WHERE ").append(idCol).append("=");

      if (sqlRequest.isNamedParams()) {
        String idField = (primary != null ? primary.fieldname : "id");
        sqlBuf.append(":").append(idField);
      } else {
        sqlBuf.append("?");
      }
      args.add(id);
    }

    if (sqlRequest.isNamedParams()) {
      return new SQLReady(sqlBuf.toString(), namedParams, sqlRequest.getLimitSql());
    } else {
      return new SQLReady(sqlBuf.toString(), args.toArray(new Object[] {}), sqlRequest.getLimitSql());
    }
  }
  
  private void buildUpdateBody(ColField colField, SQLRequest sqlRequest, Map<String, Object> namedParams, List<String> sqls, List<Object> args) {
    ORMHelper ormHelper = ORMHelper.getInstance(sqlRequest.getNamingStrategy());
    Field field = colField.getField();
    if (ormHelper.isPrimaryKey(field)) { // 主键忽略
      return;
    }
    Object v = getField(colField, sqlRequest.getEntity()); // 取得field值或者，如果是外键，取得外键对应的实体类的ID
    if (v == null && !sqlRequest.isIncludesNull()) {
      return;
    }
    if (sqlRequest.isNamedParams()) {
      namedParams.put(field.getName(), v);
      sqls.add(sqlRequest.getNamingStrategy().columnName(sqlRequest.getEntityClass(), field) + "=:" + field.getName());
    } else {
      args.add(v);
      sqls.add(sqlRequest.getNamingStrategy().columnName(sqlRequest.getEntityClass(), field) + "=?");
    }
  }

  /**
   * 根据SQLRequest构造一个SQL UPDATE语句及其对应的参数数组。
   * <ul>
   * <li>只处理不为空的字段，并且，在SET子句中忽略主键字段</li>
   * <li>根据SQLRequest中的NamingStrategy，构造各个字段的名字</li>
   * <li>如果SQLRequest#entity的主键不为空，则自动追加WHERE id=?子句，并且在参数中加入ID值</li>
   * </ul>
   * 
   * @param sqlRequest {@link SQLRequest}的实例，包含了生成SQL所需的条件。
   * @return {@link SQLReady}包含了生成的SQL和对应的参数。
   */
  public SQLReady updateById(SQLRequest sqlRequest) {
    sqlRequest.byId(true);
    return this.update(sqlRequest);
  }

  /**
   * 根据给定的SQLRequest，获取对应的列名-字段名列表
   * 
   * @param sqlRequest 给定实SQLRequest
   * @return List of {@link ColField}
   */
  public Collection<ColField> columns(SQLRequest sqlRequest) {
    Collection<ColField> colFields = getColFields(sqlRequest);
    return colFields;
  }

  /**
   * 删除SQL语句中的SELECT部分，例如，SELECT ID FROM MY_TABLE，会变成 FROM MY_TABLE
   * 
   * @param sql 原始SQL
   * @return 修改之后的SQL
   */
  public String removeSelect(String sql) {
    int beginPos = sql.toLowerCase().indexOf("from");
    Assert.isTrue(beginPos != -1, " hql : " + sql + " must has a keyword 'from'");
    return sql.substring(beginPos);
  }

  /**
   * 去除SQL的order by 子句
   */
  public String removeOrders(String sql) {
    Pattern p = Pattern.compile("order\\s*by[\\w|\\W|\\s|\\S]*", Pattern.CASE_INSENSITIVE);
    Matcher m = p.matcher(sql);
    StringBuffer sb = new StringBuffer();
    while (m.find()) {
      m.appendReplacement(sb, "");
    }
    m.appendTail(sb);
    return sb.toString();
  }

  /**
   * 删除SQL语句中limit子句
   * 
   * @param sql SQL to be fixed
   * @return
   */
  public String removeLimit(String sql) {
    String regEx = "\\s*limit\\s*[0-9]\\d*\\s*,\\s*[0-9]\\d*|\\s*limit\\s*[0-9]\\d*";
    Pattern p = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
    Matcher matcher = p.matcher(sql);
    if (matcher.find(0)) {
      int index = matcher.start();
      if (index > 0) {
        return sql.substring(0, index);
      }
    }
    
    return sql;
  }
 
  /**
   * 将一个普通的SQL，转换为COUNT查询的SQL，去掉Select中的字段列表，和ORDER子句。
   * 
   * @param querySql 普通的SQL
   */
  public String countSql(String querySql) {
    return new StringBuilder(150).append("SELECT COUNT(*) FROM (").append(removeOrders(removeLimit(querySql)))
        .append(") table_ ").toString();
  }

  /**
   * 返回基于MySQL limit语法的Limt SQL
   * 
   * @param sql 原始的SQL
   * @param start Start index of the rows, first is 0.
   * @param limit Max results
   * @return SQL with limit
   */
  public String limitSql(String sql, int start, int limit) {
    return limitSql(sql, start, limit, null);
  }

  /**
   * 返回基于LimitSql对象的Limit SQL
   * 
   * @param sql 原始的SQL
   * @param start Start index of the rows, first is 0.
   * @param limit Max results
   * @return SQL with limit
   */
  public String limitSql(String sql, int start, int limit, LimitSQL limitSql) {
    if (limitSql == null) {
      limitSql = SQLRequest.DEFAULT_LIMIT_SQL;
    }
    return limitSql.getLimitSql(sql, start, limit);
  }

  /**
   * 使用下划线命名法，取得实体类对应的表名
   */
  public String getTablename(Class<?> entityClass) {
    return SQLRequest.DEFAULT_NAME_STRATEGY.tablename(entityClass);
  }

  /**
   * 获取SQL语句的缓存键值
   * 
   * @param sqlRequest SQLRequest对象
   * @param type SQL类型
   * @return 缓存Key
   */
  private String sqlKey(SQLRequest sqlRequest, String type) {
    StringBuilder keyBuilder = new StringBuilder(200).append(type).append(sqlRequest.toString());
    return DigestUtils.md5DigestAsHex(keyBuilder.toString().getBytes(Charsets.UTF_8));
  }

  private List<ColField> getColFields(SQLRequest sqlRequest) {
    PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(sqlRequest.getEntityClass());
    if (propertyDescriptors == null) {
      throw new RuntimeException("无法获取PropertyDescriptor " + sqlRequest.getEntityClass().getName());
    }

    List<ColField> colFields = new ArrayList<ColField>(propertyDescriptors.length);
    for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
      if (propertyDescriptor == null || StringUtils.isBlank(propertyDescriptor.getName())) {
        continue;
      }
      // Read方法
      Method readMethod = propertyDescriptor.getReadMethod();
      Field field = ReflectUtil.findField(sqlRequest.getEntityClass(), propertyDescriptor.getName());
      String fieldname = propertyDescriptor.getName();
      if (this.ignore(propertyDescriptor, readMethod, field, sqlRequest, fieldname)) {
        continue;
      }
      
      // 列名，根据字段名转换得到，与表生成的规则相同
      String columnName = sqlRequest.getNamingStrategy().columnName(sqlRequest.getEntityClass(), fieldname);
      boolean isPrimary = (getAnnotation(field, Id.class) != null);
      Class<?> type = field.getType();

      boolean isForeign = ((getAnnotation(field, JoinColumn.class) != null
          || getAnnotation(field, ManyToOne.class) != null) && type.getAnnotation(Entity.class) != null);

      ColField colField = new ColField(columnName, field, isPrimary, isForeign, type, sqlRequest.getEntityClass());
      colField.alias = sqlRequest.getNamingStrategy().tableAlias(colField.ownerClass);
      colField.simpleAlias = sqlRequest.getNamingStrategy().simpleAlias(colField.ownerClass);

      if (sqlRequest.getEntity() != null) {
        colField.setOwnerValue(sqlRequest.getEntity());
      }
      colFields.add(colField);
    }

    colFields.sort(new Comparator<ColField>() {
      @Override
      public int compare(ColField cf1, ColField cf2) {
        if (cf1 == null && cf2 == null) {
          return 0;
        }

        if (cf1 == null && cf2 != null) {
          return -1;
        }

        if (cf1 != null && cf2 == null) {
          return 1;
        }

        if ("id".equalsIgnoreCase(cf1.col)) {
          return -1;
        }

        if (cf1.col != null) {
          return cf1.col.compareToIgnoreCase(cf2.col);
        }

        throw new NullPointerException("字段名不可为空。");
      }
    });

    return colFields;
  }
  
  /**
   * 判断一个字段是否可以忽略（或者必须忽略）
   * @param propertyDescriptor 字段描述
   * @param readMethod 字段对应的getter
   * @param field 字段对应的Field
   * @param sqlRequest SQLRequest，忽略的或者必须包含的字段
   * @param fieldname 字段名
   * @return
   */
  private Boolean ignore(PropertyDescriptor propertyDescriptor, Method readMethod, Field field, SQLRequest sqlRequest, String fieldname) {
    if (readMethod == null || field == null) {
      return true;
    }
    // 如果标注为Transient,则忽略
    if (getAnnotation(field, Transient.class) != null) {
      return true;
    }
    if (getAnnotation(field, java.beans.Transient.class) != null) {
      return true;
    }

    // 如果是集合类或者数组，则忽略
    if (ClassUtils.isAssignable(Collection.class, propertyDescriptor.getPropertyType())
        || propertyDescriptor.getPropertyType().isArray()) {
      return true;
    }
    
    // 必须包含
    if (!CollectionUtils.isEmpty(sqlRequest.getIncludes()) && !sqlRequest.getIncludes().contains(fieldname)) {
      return true;
    }
    // 必须排除
    if (!CollectionUtils.isEmpty(sqlRequest.getExcludes()) && sqlRequest.getExcludes().contains(fieldname)) {
      return true;
    }
    return false;
  }

  private Object getField(Field field, Object entity) {
    Method getter = ORMHelper.getInstance().getAccessMethod(field);
    Object value;
    if (getter != null) {
      value = ReflectUtil.invokeMethod(getter, entity);
    } else {
      ReflectionUtils.makeAccessible(field);
      value = ReflectUtil.getField(field, entity);
    }

    return value;
  }

  private Object getField(ColField colField, Object entity) {
    Field field = colField.getField();
    Object value = getField(field, entity);
    // 外键字段加载getId()
    if (colField.isForeign && value != null && (value instanceof BaseEntity)) {
      return ((BaseEntity) value).getId();
    }

    return value;
  }

  private static <T extends Annotation> T getAnnotation(Field field, Class<T> annotationClass) {
    Objects.requireNonNull(field);
    ORMHelper ormHelper = ORMHelper.getInstance();

    T ann = field.getAnnotation(annotationClass);
    if (ann == null) {
      Method getter = ormHelper.getAccessMethod(field);
      ann = getter.getAnnotation(annotationClass);
    }

    return ann;
  }

  private ColField findPrimary(Collection<ColField> colFields) {
    if (colFields == null || colFields.isEmpty()) {
      return null;
    }
    for (Iterator<ColField> itr = colFields.iterator(); itr.hasNext();) {
      ColField cf = itr.next();
      if (cf.isPrimary) {
        return cf;
      }
    }
    return null;
  }
  

  private String selectCols(SQLRequest sqlRequest, String tableAlias, ColField colField) {
    StringBuilder sqlSeg = new StringBuilder(100);
    // 属性名作为别名
    if (sqlRequest.isUsingAlias()) {
      sqlSeg.append(tableAlias).append(".").append(colField.col).append(" as ").append(colField.fieldname);
    } else { // 不使用别名
      sqlSeg.append(colField.col);
    }
    
    return sqlSeg.toString();
  }
  
  private String selectById(SQLRequest sqlRequest, ColField primary, String tableAlias) {
    StringBuilder sqlBuf = new StringBuilder(100);
    String idCol = (primary != null ? primary.col : "id");
    sqlBuf.append("WHERE ").append(tableAlias).append(".").append(idCol).append("=");
    if (sqlRequest.isNamedParams()) {
      String idField = (primary != null ? tableAlias + "." + primary.fieldname : tableAlias + ".id");
      sqlBuf.append(":").append(idField);
    } else {
      sqlBuf.append("?");
    }
    
    return sqlBuf.toString();
  }
  
  //构建表名别名
  private String buildTableAlias(SQLRequest sqlRequest) {
    String mainAlias = StringUtils.EMPTY;
    
    if (sqlRequest.isUsingAlias()) {
      if (StringUtils.isBlank(sqlRequest.getTableAlias())) {
        mainAlias = sqlRequest.getNamingStrategy().simpleAlias(sqlRequest.getEntityClass()); // 字段所在表别名
      } else {
        mainAlias = sqlRequest.getTableAlias();
      }
    }
    return mainAlias;
  }


  /**
   * 用于装载数据库字段col, 和实体类属性field的对应关系
   * 
   * @author leesam
   *
   */
  @SuppressWarnings("serial")
	public static final class ColField implements java.io.Serializable {
    private Object ownerValue;
    //private String fkPrimaryColumn;
    private String col;
    private String fieldname;
    private Field field;
    private boolean isPrimary = false;
    private boolean isForeign = false;
    private Class<?> ownerClass;

    private String alias;
    private String simpleAlias;

    public ColField() {

    }
    
    /**
     * 构建一个ColFiel对象，用以表达字段和属性的对应关系
     * @param col 字段名
     * @param fieldname 属性名
     * @param isPrimary 是否主键
     * @param isForeign 是否外键
     * @param type 类型
     * @param ownerClass 属性归属的类
     */
    public ColField(String col, String fieldname, boolean isPrimary, boolean isForeign, Class<?> type,
        Class<?> ownerClass) {
      this.col = col;
      this.fieldname = fieldname;
      this.isPrimary = isPrimary;
      this.isForeign = isForeign;
      this.ownerClass = ownerClass;
      if (ownerClass != null) {
        field = ReflectUtil.findField(ownerClass, fieldname);
      }
      if (type == null && field != null) {
        type = field.getType();
      }
    }

    /**
     * 构建一个ColFiel对象，用以表达字段和属性的对应关系
     * @param col 字段名
     * @param field 属性对象
     * @param isPrimary 是否主键
     * @param isForeign 是否外键
     * @param type 类型
     * @param ownerClass 属性归属的类
     */
    public ColField(String col, Field field, boolean isPrimary, boolean isForeign, Class<?> type, Class<?> ownerClass) {
      this.col = col;
      this.field = field;
      this.isForeign = isForeign;
      this.ownerClass = ownerClass;
      if (field != null) {
        this.fieldname = field.getName();
        if (ownerClass == null) {
          ownerClass = field.getDeclaringClass();
        }
      }
      if (type == null && field != null) {
        type = field.getType();
      }
      this.isPrimary = isPrimary;
    }

    /**
     * 从指定的class中获取指定名称的field对象
     */
    public Field getField() {
      if (this.field == null && this.ownerClass != null) {
        this.field = ReflectUtil.findField(this.ownerClass, this.fieldname);
      }
      return field;
    }
    
    public String getSimpleAlias() {
      return simpleAlias;
    }
    
    public String getAlias() {
      return this.alias;
    }

    public Object getOwnerValue() {
      return ownerValue;
    }

    public void setOwnerValue(Object ownerValue) {
      this.ownerValue = ownerValue;
    }

  }

}
