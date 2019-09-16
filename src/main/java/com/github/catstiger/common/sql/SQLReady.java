package com.github.catstiger.common.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

import com.github.catstiger.common.sql.limit.LimitSQL;
import com.google.common.base.Joiner;

/**
 * 用于存放生成的SQL，以及对应的参数。SQLReady可以简化“SQL拼接”，使得代码更加清爽简洁。下面是一个场景：
 * 
 * <pre>
 * SQLReady sqlReady = new SQLReady("select * from users where 1=1")
 * .appendIfExists(" and name like ? ", name)
 * .append(" and degree &gt; ? ", () -&gt; {return degree != null}, degree)
 * .orderBy("birth", SQLReady.DESC);
 * jdbcTemplate.query(sqlReady.limitSQL(), sqlRead.getArgs());
 * </pre>
 */
public final class SQLReady {
  
  /**
   * SQL对应的参数列表
   */
  private List<Object> args = new ArrayList<>(10);
  
  /**
   * 命名SQL对应的参数列表
   */
  private Map<String, Object> namedParameters = new HashMap<>(0);
  
  /**
   * {@code LimitSQL}的实现类，用于生成范围抓取代码
   */
  private LimitSQL limitSql = SQLRequest.DEFAULT_LIMIT_SQL;
  
  /**
   * SQL片段，所有的元素组合成一个完整的SQL
   */
  private List<String> appended = new ArrayList<>(10);
 
  /**
   * 组合SQL使用的分隔符
   */
  private static final String SQL_SPLITTER = StringUtils.SPACE;

  /**
   * 排序常量
   */
  private static final String ORDER_BY = " ORDER BY ";

  /**
   * 排序方向常量
   */
  public static final String DESC = "desc";
  
  /**
   * 排序方向常量
   */
  public static final String ASC = "asc";
  
  /**
   * SQL AND
   */
  public static final String AND = "AND";
  
  /**
   * SQL OR
   */
  public static final String OR = "OR";
  
  private static final String WHERE_TRUE = "WHERE 1=1";
  
  private static final String WHERE = "WHERE";

  /**
   * 使用SQL和对应的参数，构建一个{@code SQLReady}的实例
   * 
   * @param sql SQL语句
   * @param args 对应的参数，与SQL语句中的占位符数量相同，顺序一致。
   */
  public SQLReady(String sql, Object... args) {
    appended.add(sql);
    if (args != null) {
      for (Object arg : args) {
        this.args.add(arg);
      }
    }
  }
  
  /**
   * 使用SQL和对应的命名参数，构建一个{@code SQLReady}的实例
   * 
   * @param sql SQL语句
   * @param namedParameters {@code Map}装载的命名参数，key为名称，value为参数值。key值必须与SQL中的命名占位符一致。
   * @deprecated 暂不支持命名参数
   */
  @Deprecated
  public SQLReady(String sql, Map<String, Object> namedParameters) {
    if (sql == null) {
      throw new IllegalArgumentException("SQL must not be null.");
    }
    appended.add(sql);
    this.namedParameters = namedParameters;
  }

  /**
   * 使用SQL和对应的参数以及{@code LimitSql}，构建一个{@code SQLReady}的实例
   * 
   * @param sql SQL语句
   * @param args 对应的参数，与SQL语句中的占位符数量相同，顺序一致。
   */
  public SQLReady(String sql, Object[] args, LimitSQL limitSql) {
    if (sql == null) {
      throw new IllegalArgumentException("SQL must not be null.");
    }

    appended.add(sql);
    if (args != null) {
      for (Object arg : args) {
        this.args.add(arg);
      }
    }
    this.limitSql = limitSql;
  }

  /**
   * 使用SQL和对应的命名参数以及{@code LimitSql}，构建一个{@code SQLReady}的实例
   * 
   * @param sql SQL语句
   * @param namedParameters {@code Map}装载的命名参数，key为名称，value为参数值。key值必须与SQL中的命名占位符一致。
   * @param limitSql {@code LimitSQL}的实例
    * @deprecated 暂不支持命名参数
   */
  @Deprecated
  public SQLReady(String sql, Map<String, Object> namedParameters, LimitSQL limitSql) {
    appended.add(sql);
    this.namedParameters = namedParameters;
    this.limitSql = limitSql;
  }
  
  /**
   * 根据给定的<code>Class</code>, 生成一个select SQL，例如：
   * select id, username from user
   * @param entityClass 给出entity class，entity class采用JPA标注。
   * @return new SQLReady instance.
   */
  public static SQLReady select(Class<?> entityClass) {
    return new SQLRequest(entityClass).select();
  }
  
  /**
   * 根据给定的<code>Class</code>，生成一个select SQL语句。
   * @param entityClass 给出使用JPA标注的entity class
   * @param useAlias 是否使用别名，如果为{@code true}，那么生成的SQL别名遵照如下规则：
   * <ul>
   *     <li>表名别名由构成表名的各个单词（下划线分割）的首字母构成，如果只有一个单词，则由该单词前两位字母构成</li>
   *     <li>字段别名与对应的实体类的属性名字相同</li>
   * </ul>
   * @return new SQLReady instance.
   */
  public static SQLReady select(Class<?> entityClass, boolean useAlias) {
    return new SQLRequest(entityClass, useAlias).select();
  }
  
  /**
   * 根据给定的<code>Class</code>，生成一个select SQL语句。
   * @param entityClass 给出使用JPA标注的entity class
   * @param tableAlias 给出表名别名，字段名别名与对应的实体类的属性名字相同。
   * @return new SQLReady instance.
   */
  public static SQLReady select(Class<?> entityClass, String tableAlias) {
    return new SQLRequest(entityClass, tableAlias).select();
  }
  
  /**
   * 根据给出的实体类的实例，构建一个insert SQL，以及根据实体类属性解析得到的绑定的参数
   * @param entity 给出实体类的实例
   * @return new SQLReady instance.
   */
  public static SQLReady insert(BaseEntity entity) {
    return new SQLRequest(entity).insert();
  }
  
  /**
   * 根据给出的实体类的实例，构建一个update SQL，以及根据实体类属性解析得到的绑定的参数。构建的update 以ID(主键)
   * 作为过滤条件：
   * <pre>
   * update users set username=? where id=?
   * <pre>
   * @param entity 给出实体类的实例
   * @return new SQLReady instance.
   */
  public static SQLReady update(BaseEntity entity) {
    return new SQLRequest(entity).updateById();
  }
  
  /**
   * 在SQL的末尾，添加一个where 1=1子句:
   * <pre>
   * // select id, name from student where 1=1
   * SQLReady sqlReady = SQLReady.select(Sutdent.class).where()
   * </pre>
   * 
   * @return
   */
  public SQLReady where() {
    if (appended.contains(WHERE_TRUE)) {
      return this;
    }
    return this.append(WHERE_TRUE);
  }
  
  /**
   * 在SQL的末尾，追加一个Where子句
   * <pre>
   * // select id, name from student where name=?
   * SQLReady sqlReady = SQLReady.select(Student.class).where("name = ?", "小明");
   * </pre>
   * @param sqlSegment SQL子句，如果不以where/WHERE开头，则自动前缀一个WHERE
   * @param args 绑定的查询参数
   * @return this instance
   */
  public SQLReady where(String sqlSegment, @Nullable Object... args) {
    if (StringUtils.isBlank(sqlSegment)) {
      return this;
    }
    boolean whereStart = StringUtils.trim(sqlSegment).toUpperCase().startsWith(WHERE);
    
    if (whereStart) {
      append(sqlSegment, args);
    } else {
      append(WHERE).append(sqlSegment, args);
    }
    return this;
  }
  
  /**
   * 根据条件，在SQL的末尾，追加一个Where子句
   * <pre>
   * // select id, name from student where name=?
   * String name = //get name...
   * SQLReady sqlReady = SQLReady.select(Student.class).where("name = ?", name != null, name);
   * </pre>
   * @param sqlSegment sqlSegment SQL子句，如果不以where/WHERE开头，则自动前缀一个WHERE
   * @param condition 追加条件，如果为{@code true}，则执行
   * @param args 绑定的参数
   * @return   this instance
   */
  public SQLReady where(String sqlSegment, Boolean condition, @Nullable Object... args) {
    if (condition) {
      where(sqlSegment, args);
    }
    return this;
  }
  
  /**
   * 根据条件，在SQL的末尾，追加一个Where子句
   * <pre>
   * // select id, name from student where name=?
   * String name = //get name...
   * SQLReady sqlReady = SQLReady.select(Student.class).where("name = ?", () -> {name != null}, name);
   * </pre>
   * @param sqlSegment sqlSegment SQL子句，如果不以where/WHERE开头，则自动前缀一个WHERE
   * @param condition 追加条件，如果为{@code true}，则执行
   * @param args 绑定的参数
   * @return   this instance
   */
  public SQLReady where(String sqlSegment, BooleanSupplier action, @Nullable Object... args) {
    if (action.getAsBoolean()) {
      where(sqlSegment, args);
    }
    return this;
  }
  
  /**
   * 追加一个SQL AND字句，例如：
   * <pre>
   * 如果原SQLReady的内容为“select id from user where 1=1”
   * 执行and("username='admin')之后，
   * SQLReady的内容为"select id from user where 1=1 and username='admin'"
   * </pre>
   * @param sqlSegment 追加的SQL片段，可以以AND或者OR关键字开头，也可以不用，此时，{@code #and(String, Object...)}
   * 方法会自动加一个“AND ”前缀
   * @param args 绑定的参数
   * @return this object.
   */
  public SQLReady and(String sqlSegment, @Nullable Object... args) {
    return andOr(sqlSegment, AND, args);
  }
  
  /**
   * 根据给定的条件，追加一个SQL AND子句。
   * @param sqlSegment 追加的SQL片段，可以以AND或者OR关键字开头，也可以不用，此时，{@code #and(String, Boolean, Object...)}
   * 方法会自动加一个“AND ”前缀
   * @param condition 如果为{@code true}在执行SQL追加，否则，什么都不做
   * @param args 绑定的参数
   * @return this object.
   */
  public SQLReady and(String sqlSegment, Boolean condition, @Nullable Object... args) {
    if (condition) {
      return and(sqlSegment, args);
    }
    return this;
  }
  
  /**
   * 根据给定的条件，追加一个SQL AND子句。
   * @param sqlSegment 追加的SQL片段，可以以AND或者OR关键字开头，也可以不用，此时，{@code #andIf(String, BooleanSupplier, Object...)}
   * 方法会自动加一个“AND ”前缀
   * @param condition 如果返回值为{@code true}在执行SQL追加，否则，什么都不做
   * @param args 绑定的参数
   * @return this object.
   */
  public SQLReady and(String sqlSegment, BooleanSupplier action, @Nullable Object... args) {
    if (action.getAsBoolean()) {
      return this.and(sqlSegment, args);
    }
    return this;
  }
  
  /**
   * 追加一个SQL OR字句，例如：
   * <pre>
   * 如果原SQLReady的内容为“select id from user where 1=1”
   * 执行or("username='admin')之后，
   * SQLReady的内容为"select id from user where 1=1 or username='admin'"
   * </pre>
   * @param sqlSegment 追加的SQL片段，可以以AND或者OR关键字开头，也可以不用，此时，{@code #or(String, Object...)}
   * 方法会自动加一个“OR ”前缀
   * @param args 绑定的参数
   * @return this object.
   */
  public SQLReady or(String sqlSegment, @Nullable Object... args) {
    return andOr(sqlSegment, OR, args);
  }
  
  /**
   * 根据给定的条件，追加一个SQL OR子句。
   * @param sqlSegment 追加的SQL片段，可以以AND或者OR关键字开头，也可以不用，此时，{@code #or(String, Boolean, Object...)}
   * 方法会自动加一个“OR ”前缀
   * @param condition 如果为{@code true}在执行SQL追加，否则，什么都不做
   * @param args 绑定的参数
   * @return this object.
   */
  public SQLReady or(String sqlSegment, Boolean condition, @Nullable Object... args) {
    if (condition) {
      return this.or(sqlSegment, args);
    }
    return this;
  }
  
  /**
   * 根据给定的条件，追加一个SQL OR子句。
   * @param sqlSegment 追加的SQL片段，可以以AND或者OR关键字开头，也可以不用，此时{@code #or(String, BooleanSupplier, Object...)}
   * 方法会自动加一个“OR ”前缀
   * @param condition 如果返回值为{@code true}在执行SQL追加，否则，什么都不做
   * @param args 绑定的参数
   * @return this object.
   */
  public SQLReady or(String sqlSegment, BooleanSupplier action, @Nullable Object... args) {
    if (action.getAsBoolean()) {
      return or(sqlSegment, args);
    }
    return this;
  }
  
  /**
   * 返回完整的SQL语句，即本SQLReady实例历次append的结果，按照顺序组合成一个完整的SQL
   * 
   * @return SQL
   */
  public String getSql() {
    return Joiner.on(SQL_SPLITTER).join(appended);
  }

  /**
   * 返回所有的SQL参数，返回的顺序与SQL中占位符的顺序相同，数量相同
   */
  public Object[] getArgs() {
    Object[] objs = new Object[args.size()];
    return args.toArray(objs);
  }

  /**
   * 设置查询参数，元素的顺序与SQL中占位符顺序一致。此操作会在原有参数上追加参数。
   * 
   * @param args 查询参数。
   */
  public void setArgs(Object[] args) {
    if (args != null) {
      for (Object arg : args) {
        this.args.add(arg);
      }
    }
  }

  /**
   * 新增一个SQL参数，SQL参数的数量必须与SQL语句中占位符的数量一致
   * 
   * @param arg SQL参数
   * @return this instance.
   */
  public SQLReady addArg(Object arg) {
    this.args.add(arg);
    return this;
  }

  /**
   * 返回所有命名参数，命名参数用于带有命名的SQL语句，例如：insert into my_table(id,name)values(:id,:name)
   * 
   * @return 一个装载命名参数的{@code Map}实例，key为名称，value为参数值
   */
  public Map<String, Object> getNamedParameters() {
    return namedParameters;
  }

  /**
   * 设置初始的命名参数，注意，这个操作会抹除原有测命名参数。
   * 
   * @param namedParameters to be set.
   */
  public void setNamedParameters(Map<String, Object> namedParameters) {
    this.namedParameters = namedParameters;
  }

  /**
   * 追加一段SQL，及其参数
   * 
   * @param sqlSegment SQL片段
   * @param appendArgs 此段SQL所args 涉及的参数
   */
  public SQLReady append(String sqlSegment, @Nullable Object... appendArgs) {
    if (sqlSegment == null) {
      throw new IllegalArgumentException("Sql Segment must not be null.");
    }
    appended.add(sqlSegment);
    if (appendArgs != null && appendArgs.length > 0) {
      for (Object arg : appendArgs) {
        this.args.add(arg);
      }
    }
    return this;
  }

  /**
   * 根据表达式的结果，判断是否追加SQL和参数
   * 
   * @param sqlSegment SQL片段
   * @param expression 表达式，可以是一个boolean类型的语句，如果为true, 则追加，否则，直接返回SQLReady对象
   * @param appendArgs 此段SQL所args 涉及的参数
   * @return
   */
  public SQLReady append(String sqlSegment, Boolean expression, @Nullable Object... appendArgs) {
    return this.append(sqlSegment, () -> expression, appendArgs);
  }

  /**
   * 根据booleanSupplier的返值，决定是否追加一段SQL
   * 
   * @param sqlSegment 要追加的SQL
   * @param action Instance of {@link java.util.function.BooleanSupplier}, 可以是一个lambda
   * @param appendArgs 此段SQL所args 涉及的参数
   * @return This instance.
   */
  public SQLReady append(String sqlSegment, BooleanSupplier action, @Nullable Object... appendArgs) {
    if (action == null || !action.getAsBoolean()) {
      return this;
    }
    return append(sqlSegment, appendArgs);
  }

  /**
   * 追加一段SQL和一个命名参数以及参数值
   * 
   * @param sqlSegment SQL片段
   * @param name 参数名称
   * @param value 参数值
   * @deprecated 暂不支持命名参数
   */
  @Deprecated
  public SQLReady append(String sqlSegment, String name, Object value) {
    if (sqlSegment == null) {
      throw new IllegalArgumentException("Sql Segment must not be null.");
    }
    appended.add(sqlSegment);
    if (name != null) {
      this.namedParameters.put(name, value);
    }
    return this;
  }

  /**
   * 根据表达式，追加一段SQL和一个命名参数以及参数值
   * 
   * @param sqlSegment SQL片段
   * @param expression 如果为true,则追加，否则，直接返回
   * @param name 参数名称
   * @param value 参数值
   * @return This instance.
   * @deprecated 暂不支持命名参数
   */
  @Deprecated
  public SQLReady append(String sqlSegment, Boolean expression, String name, Object value) {
    return append(sqlSegment, () -> expression, name, value);
  }

  /**
   * 根据Lambda表达式，追加一段SQL和一个命名参数以及参数值
   * 
   * @param sqlSegment SQL片段
   * @param action 如果返值为TRUE，则追加，否则返回
   * @param name 参数名称
   * @param value 参数值
   * @return This instance.
   * @deprecated 暂不支持命名参数
   */
  @Deprecated
  public SQLReady append(String sqlSegment, BooleanSupplier action, String name, Object value) {
    if (action == null || !action.getAsBoolean()) {
      return this;
    }
    return append(sqlSegment, name, value);
  }

  /**
   * 追加一段SQL，和命名参数
   * 
   * @param sqlSegment SQL片段
   * @param namedParams 命名参数，KEY为参数名称，Value为参数值
   * @return This instance.
   * @deprecated 暂不支持命名参数
   */
  @Deprecated
  public SQLReady append(String sqlSegment, Map<String, Object> namedParams) {
    if (sqlSegment == null) {
      throw new IllegalArgumentException("Sql Segment must not be null.");
    }
    appended.add(sqlSegment);
    if (namedParams != null) {
      this.namedParameters.putAll(namedParams);
    }
    return this;
  }

  /**
   * 添加一个SQL片段，和对应的命名参数。
   * 
   * @param sqlSegment SQL片段
   * @param expression 如果为{@code true},则追加；否则，忽略
   * @param namedParams 命名参数，key与SQL中对应的名称一致
   * @return This instance.
   * @deprecated 暂不支持命名参数
   */
  @Deprecated
  public SQLReady append(String sqlSegment, Boolean expression, Map<String, Object> namedParams) {
    return this.append(sqlSegment, () -> expression, namedParams);
  }

  /**
   * 添加一个SQL片段，和对应的命名参数。
   * 
   * @param sqlSegment SQL片段
   * @param action 如果返回true,则追加；否则，忽略
   * @param namedParams 命名参数，key与SQL中对应的名称一致
   * @return This instance.
   * @deprecated 暂不支持命名参数
   */
  @Deprecated
  public SQLReady append(String sqlSegment, BooleanSupplier action, Map<String, Object> namedParams) {
    if (action == null || !action.getAsBoolean()) {
      return this;
    }
    return this.append(sqlSegment, namedParams);
  }

  /**
   * 如果给定的参数不为null，且toString不为空字符串，则追加SQL
   */
  public SQLReady appendIfExists(String sqlSegment, Object arg) {
    if (arg == null) {
      return this;
    }
    if (StringUtils.isBlank(arg.toString())) {
      return this;
    }
    return append(sqlSegment, arg);
  }

  /**
   * 当SQL片段是一个like查询的时候，如果参数pattern不为{@code null} 也不为{@code StringUtils#isBlank(CharSequence)}，
   * 则追加一个查询SQL，并根据{@link LikeMode} 构建一个matched
   * arguments
   * 
   * @param sqlSegment 给出SQL片段，通常类似于 some_column LIKE ?
   * @param pattern 匹配的字符串
   * @param likeMode 匹配位置
   * @return this instance
   */
  public SQLReady like(String sqlSegment, String pattern, LikeMode likeMode) {
    if (pattern == null || StringUtils.isBlank(pattern)) {
      return this;
    }

    return append(sqlSegment, likeMode.matching(pattern));
  }

  /**
   * 构建一个full matched like查询条件，如果pattern为空或者<code>null</code>，则忽略
   */
  public SQLReady like(String sqlSegment, String pattern) {
    if (pattern == null || StringUtils.isBlank(pattern)) {
      return this;
    }

    return append(sqlSegment, LikeMode.FULL.matching(pattern));
  }

  /**
   * 新增排序子句，如果原始SQL中有order by子句，则仅追加字段名和排序方向，否则会首先追加{@code #ORDER_BY}
   * 
   * @param column 排序字段名
   * @param direction 排序方向
   * @param expression 表达式，为true才执行添加动作
   * @return This instance.
   */
  public SQLReady orderBy(String column, String direction, Boolean expression) {
    if (direction == null) {
      direction = StringUtils.EMPTY;
    }
    if (expression == null || !expression) {
      return this;
    }

    String orderBy = getSql().indexOf(ORDER_BY) > 0 ? " , " : ORDER_BY;
    appended.add(new StringBuilder(20).append(orderBy).append(column).append(" ").append(direction).toString());
    return this;
  }

  /**
   * 新增排序，方向为ASC, 如果原始SQL中有order by子句，则仅追加字段名和排序方向，否则会首先追加{@code #ORDER_BY}
   * 
   * @param column 字段名
   * @param expression 表达式，决定是否添加排序字段
   * @return This instance.
   */
  public SQLReady orderBy(String column, Boolean expression) {
    return orderBy(column, null, expression);
  }

  /**
   * 新增排序子句，如果原始SQL中有order by子句，则仅追加字段名和排序方向，否则会首先追加{@code #ORDER_BY}
   * 
   * @param column 字段名
   * @param direction 排序方向
   * @return This instance.
   */
  public SQLReady orderBy(String column, String direction) {
    return orderBy(column, direction, true);
  }

  /**
   * 新增排序，如果原始SQL中有order by子句，则仅追加字段名和排序方向，否则会首先追加{@code #ORDER_BY}
   * 
   * @param column 字段名
   * @return This instance.
   */
  public SQLReady orderBy(String column) {
    return orderBy(column, null, true);
  }
  
  /**
   * 追加排序方向，ASC，通常与{@link #orderBy(String)}结合使用
   * @return this instance
   */
  public SQLReady asc() {
    return this.append(ASC);
  }
  
  /**
   * 追加排序方向，DESC，通常与{@link #orderBy(String)}结合使用
   * @return this instance
   */
  public SQLReady desc() {
    return this.append(DESC);
  }

  /**
   * 将原始的SQL转换为一个用于count查询的SQL，会去掉原始SQL中的order， limit等子句，然后在外围包装一个SELECT count(*) FROM..
   * 
   * @return 用于count查询的SQL
   */
  public String countSql() {
    return SQLFactory.getInstance().countSql(getSql());
  }

  /**
   * 将一个普通的SQL转换为限制查询抓取范围的SQL
   * 
   * @param start 抓取结果集的起始记录为准，第一行为0
   * @param limit 抓取的行数
   * @return 带有limit功能的SQL
   */
  public String limitSql(int start, int limit) {
    return SQLFactory.getInstance().limitSql(getSql(), start, limit, limitSql);
  }

  /**
   * 设定本{@code SQLReady}实例所使用的{@code LimitSql}的实例，根据不同的数据库，{@code LimitSql}会不同。
   * 
   * @param usingLimitSql Instance of {@code LimitSql}
   * @return this instance.
   */
  public SQLReady withLimitSql(LimitSQL usingLimitSql) {
    this.limitSql = usingLimitSql;
    return this;
  }

  @Override
  public String toString() {
    return getSql();
  }
  
  private boolean andStart(String str) {
    if (StringUtils.isBlank(str)) {
      return false;
    }
    return StringUtils.trimToEmpty(str).toLowerCase().startsWith("and ");
  }
  
  private boolean orStart(String str) {
    if (StringUtils.isBlank(str)) {
      return false;
    }
    return StringUtils.trimToEmpty(str).toLowerCase().startsWith("or ");
  }
  
  private SQLReady andOr(String sqlSegment, String andOr, Object... args) {
    if (sqlSegment == null) {
      throw new IllegalArgumentException("Sql Segment must not be null.");
    }
    
    if (!andStart(sqlSegment) && !orStart(sqlSegment)) {
      append(andOr).append(sqlSegment, args);
    } else {
      append(sqlSegment, args);
    }
    
    return this;
  }
}
