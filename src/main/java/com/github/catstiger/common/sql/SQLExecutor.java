package com.github.catstiger.common.sql;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import com.github.catstiger.common.sql.mapper.BeanPropertyRowMapperEx;
import com.github.catstiger.common.sql.mapper.Mappers;

@Component
public class SQLExecutor {
  private JdbcTemplate jdbcTemplate;

  /**
   * 返回{@link JdbcTemplate的实例}
   */
  public JdbcTemplate getJdbcTemplate() {
    return jdbcTemplate;
  }
  
  @Autowired
  public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }
  
  /**
   * 返回查询结果中第一条数据，如果没有查询结果，返回{@code null}
   * @param sql 要执行的SQL query
   * @param rowMapper 将每一行数据映射为一个对象的回调
   * @param args 绑定的查询参数
   * @return 映射的单个对象，如果给出的{@code RowMapper}返回{@code null}, 或者没有任何可用数据，返回{@code null}
   */
  @Nullable
  public <T> T first(String sql, RowMapper<T> rowMapper, @Nullable Object... args) {
    List<T> list = getJdbcTemplate().query(sql, rowMapper, args);
    return getSingleObject(list);
  }
  
  /**
   * 根据给定的SQL和绑定的参数执行查询，返回期望的结果对象。
   * <p> 该查询应该是单行/单列查询; 返回的结果直接映射到对应的对象类型</p>
   * @param sql 要执行的SQL query
   * @param requiredType 结果对象期望匹配的类型
   * @param args 绑定的查询参数
   * @return 所需类型的结果对象，或者在SQL NULL的情况下为{@code null}
   */
  @Nullable
  public <T> T first(String sql, Class<T> requiredType, @Nullable Object... args) {
    List<T> list = getJdbcTemplate().queryForList(sql, requiredType, args);
    return getSingleObject(list);
  }
  
  /**
   * 根据给定的SQL以及绑定到查询的参数列表，返回结果。
   * <p>结果将映射到Map（每列一个条目，使用列名作为键）。</p>
   * @param sql  要执行的SQL query
   * @param args 绑定的查询参数
   * @return 结果映射的Map，如果没有查询到结果，返回{@code null}
   */
  public Map<String, Object> first(String sql, @Nullable Object... args) {
    List<Map<String, Object>> list = getJdbcTemplate().queryForList(sql, args);
    return getSingleObject(list);
  }
  
  /**
   * 根据指定的{@code SQLReady}的实例执行查询，返回期望的结果对象。
   * @param sqlReady 给出{@code SQLReady}的实例
   * @param requiredType 期望的查询结果的类型，通常是一个{@code EntityBean}的子类，不支持primitive的包装类型。
   * @return 所需类型的结果对象，或者没有任何可用数据，返回{@code null}
   */
  public <T> T first(SQLReady sqlReady, Class<T> requiredType) {
    // 既然期望得到第一行数据，因此限制抓取size，减少性能损耗
    return first(sqlReady.limitSql(0, 1), new BeanPropertyRowMapperEx<T>(requiredType), sqlReady.getArgs());
  }
  
  /**
   * 执行给定的SQL Query和绑定的参数，将查询结果，根据{@code RowMapper}映射为给定的类型
   * @param sql 要执行的SQL query
   * @param rowMapper 将每一行数据映射为一个对象的回调
   * @param args 绑定的查询参数
   * @return 映射的单个对象，
   * <ul>
   * <li>如果给出的{@code RowMapper}返回{@code null}, </li>
   * <li>如果查询结果集为空，则抛出{@code EmptyResultDataAccessException}</li>
   * <li>如果查询结果集超过1，则抛出{@code IncorrectResultSizeDataAccessException}</li>
   * </ul>
   */
  @Nullable
  public <T> T one(String sql, RowMapper<T> rowMapper, @Nullable Object... args) {
    return getJdbcTemplate().queryForObject(sql, rowMapper, args);
  }
  
  /**
   * 根据给定的SQL和绑定的参数执行查询，返回期望的结果对象。
   * <p> 该查询应该是单行/单列查询; 返回的结果直接映射到对应的对象类型</p>
   * @param sql 要执行的SQL query
   * @param requiredType 结果对象期望匹配的类型
   * @param args 绑定的查询参数
   * @return 所需类型的结果对象
   * <ul>
   * <li>如果查询结果集为空，则抛出{@code EmptyResultDataAccessException}</li>
   * <li>如果查询结果集超过1，则抛出{@code IncorrectResultSizeDataAccessException}</li>
   * </ul>
   */
  @Nullable
  public <T> T one(String sql, Class<T> requiredType, @Nullable Object... args) {
    return getJdbcTemplate().queryForObject(sql, requiredType, args);
  }
  
  /**
   * 根据给定的SQL以及绑定到查询的参数列表，返回结果。如果没有域模型的时候，可以使用这个方法。
   * <p>结果将映射到Map（每列一个条目，使用列名作为键）。</p>
   * @param sql  要执行的SQL query
   * @param args 绑定的查询参数
   * @return 结果映射的Map，如果没有查询到结果，返回{@code null}
   * <ul>
   * <li>如果查询结果集为空，则抛出{@code EmptyResultDataAccessException}</li>
   * <li>如果查询结果集超过1，则抛出{@code IncorrectResultSizeDataAccessException}</li>
   * </ul>
   */
  public Map<String, Object> one(String sql, @Nullable Object... args) {
    return getJdbcTemplate().queryForMap(sql, args);
  }
  
  /**
   * 根据指定的{@code SQLReady}的实例执行查询，返回期望的结果对象。
   * @param sqlReady 给出{@code SQLReady}的实例
   * @param requiredType 期望的查询结果的类型，通常是一个{@code EntityBean}的子类，不支持primitive的包装类型。
   * @return 所需类型的结果对象
   * <ul>
   * <li>如果给出的{@code RowMapper}返回{@code null}, </li>
   * <li>如果查询结果集为空，则抛出{@code EmptyResultDataAccessException}</li>
   * <li>如果查询结果集超过1，则抛出{@code IncorrectResultSizeDataAccessException}</li>
   * </ul>
   */
  public <T> T one(SQLReady sqlReady, Class<T> requiredType) {
    return one(sqlReady.getSql(), new BeanPropertyRowMapperEx<T>(requiredType), sqlReady.getArgs());
  }
  
  /**
   * 根据ID查询单个实体对象，如果不存在，返回{@code null}
   * @param requiredType 需要的实体类类型
   * @param id Identifier / Primary key
   * @return 符合条件的实例，如果没有，返回{@code null}
   */
  public <T> T get(Class<T> requiredType, Long id) {
    SQLReady sqlReady = new SQLRequest(requiredType).usingAlias(true).byId(true).select().addArg(id);
    return first(sqlReady, requiredType);
  }
  
  /**
   * 根据给定的{@code SQLReady}实例执行查询，给出查询结果的总数量。
   * @param sqlReady 给出的{@code SQLReady}实例
   * @return count of the results size.
   */
  public Long total(SQLReady sqlReady) {
    return one(sqlReady.countSql(), Long.class, sqlReady.getArgs());
  }
  
  /**
   * 根据给出的SQL Query，执行查询，得到查询结果的总数量
   * @param sql 给出的SQL Query
   * @param args 绑定的查询参数
   * @return count of the results size.
   */
  public Long total(String sql, @Nullable Object... args) {
    String countSql = SQLFactory.getInstance().countSql(sql);
    return one(countSql, Long.class, args);
  }
  
  /**
   * {@code JdbcTemplate#update(String, Object...)}的快捷方式，使用给定的SQL和绑定的参数，执行更新操作（insert、update、delete等）
   * @param sql 给出SQL
   * @param args 绑定的参数
   * @return 影响到的行数
   */
  public int update(String sql, @Nullable Object... args) {
    return getJdbcTemplate().update(sql, args);
  }
  
  /**
   * 执行给定的SQL Query以及绑定的参数，将结果集中的每一行根据给出的{@code RowMapper}，映射为期望的结果。
   * @param sql 给出要执行的SQL
   * @param rowMapper 映射单个结果集为Object
   * @param args 绑定的参数
   * @return 结果List，包含映射后的对象
   * @see {@link JdbcTemplate#query(String, RowMapper, Object...)}
   */
  public <T> List<T> query(String sql, RowMapper<T> rowMapper, @Nullable Object... args) {
    return getJdbcTemplate().query(sql, rowMapper, args);
  }
  
  /**
   * 执行给定的SQL Query以及绑定的参数，以期得到相应的结果。
   * <p>该查询应该是单列查询; 返回的结果直接映射到对应的对象类型</p>
   * @param sql 给出要执行的SQL
   * @param elementType 期望List中的元素类型
   * @param args 绑定的参数
   * @return 与指定元素类型匹配的对象列表
   * @see {@link JdbcTemplate#queryForList(String, Class, Object...)}
   */
  public <T> List<T> query(String sql, Class<T> elementType, @Nullable Object... args) {
    return getJdbcTemplate().queryForList(sql, elementType, args);
  }
  
  /**
   * 执行给定的SQL Query以及绑定的参数，以期得到期望的结果。
   * 结果将映射到以Map组成的List（每行一个条目）（每列一个键，使用列名作为键）
   * @param sql 给出要执行的SQL
   * @param args 绑定的参数
   * @return a List that contains a Map per row
   * @see {@link JdbcTemplate#queryForList(String, Object...)}
   */
  public List<Map<String, Object>> query(String sql, @Nullable Object... args) {
    return getJdbcTemplate().queryForList(sql, args);
  }
  
  /**
   * 根据给定的{@code SQLReady}对象，执行SQL Query，以期得到期望的结果
   * @param sqlReady 给出{@code SQLReady}
   * @param requiredType 期望得到的元素的类型
   * @return a List contains the required object.
   */
  public <T> List<T> query(SQLReady sqlReady, Class<T> requiredType) {
    return this.query(sqlReady.getSql(), Mappers.byClass(requiredType), sqlReady.getArgs());
  }
  
  private <T> T getSingleObject(List<T> list) {
    if (list == null || list.isEmpty()) {
      return null;
    }
    return list.get(0);
  }
}
