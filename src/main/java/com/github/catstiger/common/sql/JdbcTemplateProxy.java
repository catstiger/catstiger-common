package com.github.catstiger.common.sql;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import com.github.catstiger.common.model.KeyValue;
import com.github.catstiger.common.sql.mapper.BeanPropertyRowMapperEx;
import com.google.common.base.Preconditions;

@Component
public class JdbcTemplateProxy {
  @Autowired
  private JdbcTemplate jdbcTemplate;

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#batchUpdate(java.lang.String[])
   */
  public int[] batchUpdate(String... sql) throws DataAccessException {
    return jdbcTemplate.batchUpdate(sql);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#batchUpdate(java.lang.String,
   *      org.springframework.jdbc.core.BatchPreparedStatementSetter)
   */
  public int[] batchUpdate(String sql, BatchPreparedStatementSetter pss) throws DataAccessException {
    return jdbcTemplate.batchUpdate(sql, pss);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#batchUpdate(java.lang.String,
   *      java.util.Collection, int,
   *      org.springframework.jdbc.core.ParameterizedPreparedStatementSetter)
   */
  public <T> int[][] batchUpdate(String sql, Collection<T> batchArgs, int batchSize,
      ParameterizedPreparedStatementSetter<T> pss) throws DataAccessException {
    return jdbcTemplate.batchUpdate(sql, batchArgs, batchSize, pss);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#batchUpdate(java.lang.String,
   *      java.util.List)
   */
  public int[] batchUpdate(String sql, List<Object[]> batchArgs) throws DataAccessException {
    return jdbcTemplate.batchUpdate(sql, batchArgs);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#batchUpdate(java.lang.String,
   *      java.util.List, int[])
   */
  public int[] batchUpdate(String sql, List<Object[]> batchArgs, int[] argTypes) throws DataAccessException {
    return jdbcTemplate.batchUpdate(sql, batchArgs, argTypes);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#call(org.springframework.jdbc.core.CallableStatementCreator,
   *      java.util.List)
   */
  public Map<String, Object> call(CallableStatementCreator csc, List<SqlParameter> declaredParameters)
      throws DataAccessException {
    return jdbcTemplate.call(csc, declaredParameters);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#execute(org.springframework.jdbc.core.CallableStatementCreator,
   *      org.springframework.jdbc.core.CallableStatementCallback)
   */
  public <T> T execute(CallableStatementCreator csc, CallableStatementCallback<T> action) throws DataAccessException {
    return jdbcTemplate.execute(csc, action);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#execute(org.springframework.jdbc.core.ConnectionCallback)
   */
  public <T> T execute(ConnectionCallback<T> action) throws DataAccessException {
    return jdbcTemplate.execute(action);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#execute(org.springframework.jdbc.core.PreparedStatementCreator,
   *      org.springframework.jdbc.core.PreparedStatementCallback)
   */
  public <T> T execute(PreparedStatementCreator psc, PreparedStatementCallback<T> action) throws DataAccessException {
    return jdbcTemplate.execute(psc, action);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#execute(org.springframework.jdbc.core.StatementCallback)
   */
  public <T> T execute(StatementCallback<T> action) throws DataAccessException {
    return jdbcTemplate.execute(action);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#execute(java.lang.String)
   */
  public void execute(String sql) throws DataAccessException {
    jdbcTemplate.execute(sql);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#execute(java.lang.String,
   *      org.springframework.jdbc.core.CallableStatementCallback)
   */
  public <T> T execute(String callString, CallableStatementCallback<T> action) throws DataAccessException {
    return jdbcTemplate.execute(callString, action);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#execute(java.lang.String,
   *      org.springframework.jdbc.core.PreparedStatementCallback)
   */
  public <T> T execute(String sql, PreparedStatementCallback<T> action) throws DataAccessException {
    return jdbcTemplate.execute(sql, action);
  }

  /**
   * @see org.springframework.jdbc.support.JdbcAccessor#getDataSource()
   */
  public DataSource getDataSource() {
    return jdbcTemplate.getDataSource();
  }

  /**
   * @see org.springframework.jdbc.support.JdbcAccessor#getExceptionTranslator()
   */
  public SQLExceptionTranslator getExceptionTranslator() {
    return jdbcTemplate.getExceptionTranslator();
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#getFetchSize()
   */
  public int getFetchSize() {
    return jdbcTemplate.getFetchSize();
  }

  @Autowired
  public JdbcTemplate getJdbcTemplate() {
    return jdbcTemplate;
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#getMaxRows()
   */
  public int getMaxRows() {
    return jdbcTemplate.getMaxRows();
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#getQueryTimeout()
   */
  public int getQueryTimeout() {
    return jdbcTemplate.getQueryTimeout();
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#isIgnoreWarnings()
   */
  public boolean isIgnoreWarnings() {
    return jdbcTemplate.isIgnoreWarnings();
  }

  /**
   * @see org.springframework.jdbc.support.JdbcAccessor#isLazyInit()
   */
  public boolean isLazyInit() {
    return jdbcTemplate.isLazyInit();
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#isResultsMapCaseInsensitive()
   */
  public boolean isResultsMapCaseInsensitive() {
    return jdbcTemplate.isResultsMapCaseInsensitive();
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#isSkipResultsProcessing()
   */
  public boolean isSkipResultsProcessing() {
    return jdbcTemplate.isSkipResultsProcessing();
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#isSkipUndeclaredResults()
   */
  public boolean isSkipUndeclaredResults() {
    return jdbcTemplate.isSkipUndeclaredResults();
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#query(org.springframework.jdbc.core.PreparedStatementCreator,
   *      org.springframework.jdbc.core.PreparedStatementSetter,
   *      org.springframework.jdbc.core.ResultSetExtractor)
   */
  public <T> T query(PreparedStatementCreator psc, PreparedStatementSetter pss, ResultSetExtractor<T> rse)
      throws DataAccessException {
    return jdbcTemplate.query(psc, pss, rse);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#query(org.springframework.jdbc.core.PreparedStatementCreator,
   *      org.springframework.jdbc.core.ResultSetExtractor)
   */
  public <T> T query(PreparedStatementCreator psc, ResultSetExtractor<T> rse) throws DataAccessException {
    return jdbcTemplate.query(psc, rse);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#query(org.springframework.jdbc.core.PreparedStatementCreator,
   *      org.springframework.jdbc.core.RowCallbackHandler)
   */
  public void query(PreparedStatementCreator psc, RowCallbackHandler rch) throws DataAccessException {
    jdbcTemplate.query(psc, rch);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#query(org.springframework.jdbc.core.PreparedStatementCreator,
   *      org.springframework.jdbc.core.RowMapper)
   */
  public <T> List<T> query(PreparedStatementCreator psc, RowMapper<T> rowMapper) throws DataAccessException {
    return jdbcTemplate.query(psc, rowMapper);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#query(java.lang.String,
   *      java.lang.Object[], int[],
   *      org.springframework.jdbc.core.ResultSetExtractor)
   */
  public <T> T query(String sql, Object[] args, int[] argTypes, ResultSetExtractor<T> rse) throws DataAccessException {
    return jdbcTemplate.query(sql, args, argTypes, rse);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#query(java.lang.String,
   *      java.lang.Object[], int[],
   *      org.springframework.jdbc.core.RowCallbackHandler)
   */
  public void query(String sql, Object[] args, int[] argTypes, RowCallbackHandler rch) throws DataAccessException {
    jdbcTemplate.query(sql, args, argTypes, rch);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#query(java.lang.String,
   *      java.lang.Object[], int[], org.springframework.jdbc.core.RowMapper)
   */
  public <T> List<T> query(String sql, Object[] args, int[] argTypes, RowMapper<T> rowMapper)
      throws DataAccessException {
    return jdbcTemplate.query(sql, args, argTypes, rowMapper);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#query(java.lang.String,
   *      java.lang.Object[], org.springframework.jdbc.core.ResultSetExtractor)
   */
  public <T> T query(String sql, Object[] args, ResultSetExtractor<T> rse) throws DataAccessException {
    return jdbcTemplate.query(sql, args, rse);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#query(java.lang.String,
   *      java.lang.Object[], org.springframework.jdbc.core.RowCallbackHandler)
   */
  public void query(String sql, Object[] args, RowCallbackHandler rch) throws DataAccessException {
    jdbcTemplate.query(sql, args, rch);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#query(java.lang.String,
   *      java.lang.Object[], org.springframework.jdbc.core.RowMapper)
   */
  public <T> List<T> query(String sql, Object[] args, RowMapper<T> rowMapper) throws DataAccessException {
    return jdbcTemplate.query(sql, args, rowMapper);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#query(java.lang.String,
   *      org.springframework.jdbc.core.PreparedStatementSetter,
   *      org.springframework.jdbc.core.ResultSetExtractor)
   */
  public <T> T query(String sql, PreparedStatementSetter pss, ResultSetExtractor<T> rse) throws DataAccessException {
    return jdbcTemplate.query(sql, pss, rse);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#query(java.lang.String,
   *      org.springframework.jdbc.core.PreparedStatementSetter,
   *      org.springframework.jdbc.core.RowCallbackHandler)
   */
  public void query(String sql, PreparedStatementSetter pss, RowCallbackHandler rch) throws DataAccessException {
    jdbcTemplate.query(sql, pss, rch);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#query(java.lang.String,
   *      org.springframework.jdbc.core.PreparedStatementSetter,
   *      org.springframework.jdbc.core.RowMapper)
   */
  public <T> List<T> query(String sql, PreparedStatementSetter pss, RowMapper<T> rowMapper) throws DataAccessException {
    return jdbcTemplate.query(sql, pss, rowMapper);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#query(java.lang.String,
   *      org.springframework.jdbc.core.ResultSetExtractor)
   */
  public <T> T query(String sql, ResultSetExtractor<T> rse) throws DataAccessException {
    return jdbcTemplate.query(sql, rse);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#query(java.lang.String,
   *      org.springframework.jdbc.core.ResultSetExtractor, java.lang.Object[])
   */
  public <T> T query(String sql, ResultSetExtractor<T> rse, Object... args) throws DataAccessException {
    return jdbcTemplate.query(sql, rse, args);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#query(java.lang.String,
   *      org.springframework.jdbc.core.RowCallbackHandler)
   */
  public void query(String sql, RowCallbackHandler rch) throws DataAccessException {
    jdbcTemplate.query(sql, rch);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#query(java.lang.String,
   *      org.springframework.jdbc.core.RowCallbackHandler, java.lang.Object[])
   */
  public void query(String sql, RowCallbackHandler rch, Object... args) throws DataAccessException {
    jdbcTemplate.query(sql, rch, args);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#query(java.lang.String,
   *      org.springframework.jdbc.core.RowMapper)
   */
  public <T> List<T> query(String sql, RowMapper<T> rowMapper) throws DataAccessException {
    return jdbcTemplate.query(sql, rowMapper);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#query(java.lang.String,
   *      org.springframework.jdbc.core.RowMapper, java.lang.Object[])
   */
  public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) throws DataAccessException {
    return jdbcTemplate.query(sql, rowMapper, args);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#queryForList(java.lang.String)
   */
  public List<Map<String, Object>> queryForList(String sql) throws DataAccessException {
    return jdbcTemplate.queryForList(sql);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#queryForList(java.lang.String,
   *      java.lang.Class)
   */
  public <T> List<T> queryForList(String sql, Class<T> elementType) throws DataAccessException {
    return jdbcTemplate.queryForList(sql, elementType);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#queryForList(java.lang.String,
   *      java.lang.Class, java.lang.Object[])
   */
  public <T> List<T> queryForList(String sql, Class<T> elementType, Object... args) throws DataAccessException {
    return jdbcTemplate.queryForList(sql, elementType, args);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#queryForList(java.lang.String,
   *      java.lang.Object[])
   */
  public List<Map<String, Object>> queryForList(String sql, Object... args) throws DataAccessException {
    return jdbcTemplate.queryForList(sql, args);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#queryForList(java.lang.String,
   *      java.lang.Object[], java.lang.Class)
   */
  public <T> List<T> queryForList(String sql, Object[] args, Class<T> elementType) throws DataAccessException {
    return jdbcTemplate.queryForList(sql, args, elementType);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#queryForList(java.lang.String,
   *      java.lang.Object[], int[])
   */
  public List<Map<String, Object>> queryForList(String sql, Object[] args, int[] argTypes) throws DataAccessException {
    return jdbcTemplate.queryForList(sql, args, argTypes);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#queryForList(java.lang.String,
   *      java.lang.Object[], int[], java.lang.Class)
   */
  public <T> List<T> queryForList(String sql, Object[] args, int[] argTypes, Class<T> elementType)
      throws DataAccessException {
    return jdbcTemplate.queryForList(sql, args, argTypes, elementType);
  }

  /**
   */
  public Map<String, Object> queryForMap(String sql) throws DataAccessException {
    return jdbcTemplate.queryForMap(sql);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#queryForMap(java.lang.String,
   *      java.lang.Object[])
   */
  public Map<String, Object> queryForMap(String sql, Object... args) throws DataAccessException {
    return jdbcTemplate.queryForMap(sql, args);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#queryForMap(java.lang.String,
   *      java.lang.Object[], int[])
   */
  public Map<String, Object> queryForMap(String sql, Object[] args, int[] argTypes) throws DataAccessException {
    return jdbcTemplate.queryForMap(sql, args, argTypes);
  }

  /**
   * 查询指定类型的单个记录，如果不存在返回{@code null}， 如果存在多个，返回第一个。
   * @see org.springframework.jdbc.core.JdbcTemplate#queryForObject(java.lang.String,
   *      java.lang.Class)
   */
  public <T> T queryForObject(String sql, Class<T> requiredType) throws DataAccessException {
    return this.queryForObject(sql, requiredType, new Object[] {});
  }

  /**
   * 查询指定类型的单个记录，如果不存在返回{@code null}， 如果存在多个，返回第一个。
   * @see org.springframework.jdbc.core.JdbcTemplate#queryForObject(java.lang.String,
   *      java.lang.Class, java.lang.Object[])
   */
  public <T> T queryForObject(String sql, Class<T> requiredType, Object... args) throws DataAccessException {
    List<T> list = jdbcTemplate.queryForList(sql, requiredType, args);
    
    return getSingleObject(list);// jdbcTemplate.queryForObject(sql, requiredType, args);
  }

  /**
   * 查询指定类型的单个记录，如果不存在返回{@code null}， 如果存在多个，返回第一个。
   * @see org.springframework.jdbc.core.JdbcTemplate#queryForObject(java.lang.String,
   *      java.lang.Object[], java.lang.Class)
   */
  public <T> T queryForObject(String sql, Object[] args, Class<T> requiredType) throws DataAccessException {
    List<T> list = jdbcTemplate.queryForList(sql, args, requiredType);
    return getSingleObject(list); // jdbcTemplate.queryForObject(sql, args, requiredType);
  }

  /**
   * 查询指定类型的单个记录，如果不存在返回{@code null}， 如果存在多个，返回第一个。
   * @see org.springframework.jdbc.core.JdbcTemplate#queryForObject(java.lang.String,
   *      java.lang.Object[], int[], java.lang.Class)
   */
  public <T> T queryForObject(String sql, Object[] args, int[] argTypes, Class<T> requiredType)
      throws DataAccessException {
    List<T> list = jdbcTemplate.queryForList(sql, args, argTypes, requiredType);
    return getSingleObject(list);
  }

  /**
   * 查询指定类型的单个记录，如果不存在返回{@code null}， 如果存在多个，返回第一个。
   * @see org.springframework.jdbc.core.JdbcTemplate#queryForObject(java.lang.String,
   *      java.lang.Object[], int[], org.springframework.jdbc.core.RowMapper)
   */
  public <T> T queryForObject(String sql, Object[] args, int[] argTypes, RowMapper<T> rowMapper)
      throws DataAccessException {
    List<T> results = query(sql, args, argTypes, new RowMapperResultSetExtractor<T>(rowMapper, 1));
    return getSingleObject(results);
  }

  /**
   * 查询指定类型的单个记录，如果不存在返回{@code null}， 如果存在多个，返回第一个。
   * @see org.springframework.jdbc.core.JdbcTemplate#queryForObject(java.lang.String,
   *      java.lang.Object[], org.springframework.jdbc.core.RowMapper)
   */
  public <T> T queryForObject(String sql, Object[] args, RowMapper<T> rowMapper) throws DataAccessException {
    List<T> results = query(sql, args, new RowMapperResultSetExtractor<T>(rowMapper, 1));
    return getSingleObject(results);
  }

  /**
   * 查询单个对象，如果不存在返回{@code null}, 如果有多个匹配的结果，返回第一个。
   * @see org.springframework.jdbc.core.JdbcTemplate#queryForObject(java.lang.String,
   *      org.springframework.jdbc.core.RowMapper)
   */
  public <T> T queryForObject(String sql, RowMapper<T> rowMapper) throws DataAccessException {
    List<T> results = query(sql, rowMapper);
    return getSingleObject(results);
  }

  /**
   * 根据参数查询单个对象，如果不存在返回{@code null}, 如果有多个匹配的结果，返回第一个。
   * @see org.springframework.jdbc.core.JdbcTemplate#queryForObject(java.lang.String,
   *      org.springframework.jdbc.core.RowMapper, java.lang.Object[])
   */
  public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) throws DataAccessException {
    List<T> results = query(sql, args, new RowMapperResultSetExtractor<T>(rowMapper, 1));
    return getSingleObject(results);
  }
  
  /**
   * 根据SQLReady查询单个实体对象
   * @param sqlReady {@link SQLReady}实例，包括SQL和查询参数
   * @param requiredType 要求的实体类类型
   * @return 查询单个实体类，如果不存在，返回{@code null}, 如果有多个符合条件的结果，返回第一个。
   */
  public <T> T queryForObject(SQLReady sqlReady, Class<T> requiredType) {
    return this.queryForObject(sqlReady.getSql(), new BeanPropertyRowMapperEx<T>(requiredType), sqlReady.getArgs());
  }
  
  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#queryForRowSet(java.lang.String)
   */
  public SqlRowSet queryForRowSet(String sql) throws DataAccessException {
    return jdbcTemplate.queryForRowSet(sql);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#queryForRowSet(java.lang.String,
   *      java.lang.Object[])
   */
  public SqlRowSet queryForRowSet(String sql, Object... args) throws DataAccessException {
    return jdbcTemplate.queryForRowSet(sql, args);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#queryForRowSet(java.lang.String,
   *      java.lang.Object[], int[])
   */
  public SqlRowSet queryForRowSet(String sql, Object[] args, int[] argTypes) throws DataAccessException {
    return jdbcTemplate.queryForRowSet(sql, args, argTypes);
  }

  /**
   * @see org.springframework.jdbc.support.JdbcAccessor#setDatabaseProductName(java.lang.String)
   */
  public void setDatabaseProductName(String dbName) {
    jdbcTemplate.setDatabaseProductName(dbName);
  }

  /**
   * @see org.springframework.jdbc.support.JdbcAccessor#setDataSource(javax.sql.DataSource)
   */
  public void setDataSource(DataSource dataSource) {
    jdbcTemplate.setDataSource(dataSource);
  }

  /**
   * @see org.springframework.jdbc.support.JdbcAccessor#setExceptionTranslator(org.springframework.jdbc.support.SQLExceptionTranslator)
   */
  public void setExceptionTranslator(SQLExceptionTranslator exceptionTranslator) {
    jdbcTemplate.setExceptionTranslator(exceptionTranslator);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#setFetchSize(int)
   */
  public void setFetchSize(int fetchSize) {
    jdbcTemplate.setFetchSize(fetchSize);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#setIgnoreWarnings(boolean)
   */
  public void setIgnoreWarnings(boolean ignoreWarnings) {
    jdbcTemplate.setIgnoreWarnings(ignoreWarnings);
  }

  public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  /**
   * @see org.springframework.jdbc.support.JdbcAccessor#setLazyInit(boolean)
   */
  public void setLazyInit(boolean lazyInit) {
    jdbcTemplate.setLazyInit(lazyInit);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#setMaxRows(int)
   */
  public void setMaxRows(int maxRows) {
    jdbcTemplate.setMaxRows(maxRows);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#setQueryTimeout(int)
   */
  public void setQueryTimeout(int queryTimeout) {
    jdbcTemplate.setQueryTimeout(queryTimeout);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#setResultsMapCaseInsensitive(boolean)
   */
  public void setResultsMapCaseInsensitive(boolean resultsMapCaseInsensitive) {
    jdbcTemplate.setResultsMapCaseInsensitive(resultsMapCaseInsensitive);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#setSkipResultsProcessing(boolean)
   */
  public void setSkipResultsProcessing(boolean skipResultsProcessing) {
    jdbcTemplate.setSkipResultsProcessing(skipResultsProcessing);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#setSkipUndeclaredResults(boolean)
   */
  public void setSkipUndeclaredResults(boolean skipUndeclaredResults) {
    jdbcTemplate.setSkipUndeclaredResults(skipUndeclaredResults);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#update(org.springframework.jdbc.core.PreparedStatementCreator)
   */
  public int update(PreparedStatementCreator psc) throws DataAccessException {
    return jdbcTemplate.update(psc);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#update(org.springframework.jdbc.core.PreparedStatementCreator,
   *      org.springframework.jdbc.support.KeyHolder)
   */
  public int update(PreparedStatementCreator psc, KeyHolder generatedKeyHolder) throws DataAccessException {
    return jdbcTemplate.update(psc, generatedKeyHolder);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#update(java.lang.String)
   */
  public int update(String sql) throws DataAccessException {
    return jdbcTemplate.update(sql);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#update(java.lang.String,
   *      java.lang.Object[])
   */
  public int update(String sql, Object... args) throws DataAccessException {
    return jdbcTemplate.update(sql, args);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#update(java.lang.String,
   *      java.lang.Object[], int[])
   */
  public int update(String sql, Object[] args, int[] argTypes) throws DataAccessException {
    return jdbcTemplate.update(sql, args, argTypes);
  }

  /**
   * @see org.springframework.jdbc.core.JdbcTemplate#update(java.lang.String,
   *      org.springframework.jdbc.core.PreparedStatementSetter)
   */
  public int update(String sql, PreparedStatementSetter pss) throws DataAccessException {
    return jdbcTemplate.update(sql, pss);
  }
  
  /**
   * 根据{@link SQLReady}查询指定类型的实体类集合
   * @param sqlReady 给出{@link SQLReady}的实例，包括SQL和查询参数
   * @param requiredType 要求的Bean类型
   * @return requiredType 要求的Bean类型
   */
  public <T> List<T> queryBySqlReady(SQLReady sqlReady, Class<T> requiredType) {
    return  jdbcTemplate.query(sqlReady.getSql(), new BeanPropertyRowMapperEx<T>(requiredType), sqlReady.getArgs());
  }
  
  /**
   * 根据{@link SQLReady}和{@link Page}执行limit查询
   * @param sqlReady 给出{@link SQLReady}的实例，包括SQL和查询参数
   * @param page 给出分页参数，如果为null, 则查询全部匹配的数据
   * @param requiredType requiredType 要求的Bean类型
   * @return requiredType 要求的Bean类型
   */
  public <T> List<T> queryBySqlReady(SQLReady sqlReady, Page page, Class<T> requiredType) {
    if (page == null) {
      return queryBySqlReady(sqlReady, requiredType);
    }
    
    return jdbcTemplate.query(sqlReady.limitSql(page.getStart(), page.getLimit()), new BeanPropertyRowMapperEx<T>(requiredType), sqlReady.getArgs());
  }
  
  /**
   * 查询符合{@link SQLReady}的行的数量，通常用于分页查询
   * @param sqlReady  给出{@link SQLReady}的实例，包括SQL和查询参数
   * @return number of total rows.
   */
  public Long queryTotal(SQLReady sqlReady) {
    return jdbcTemplate.queryForObject(sqlReady.countSql(), Long.class, sqlReady.getArgs());
  }
  
  /**
   * 根据ID查询单个实体对象，如果不存在，返回{@code null}
   * @param requiredType 需要的实体类类型
   * @param id Identifier / Primary key
   * @return 符合条件的实例，如果没有，返回{@code null}
   */
  public <T> T get(Class<T> requiredType, Long id) {
    SQLReady sqlReady = new SQLRequest(requiredType).usingAlias(true).byId(true).select().addArg(id);
    return queryForObject(sqlReady, requiredType);
  }
  
  /**
   * 根据指定的字段，查询（精确匹配）单个记录
   * @param requiredType 需要的对象类型
   * @param column 字段名称（注意不是属性名称）
   * @param expectValue 期望值
   * @return
   */
  public <T> T get(Class<T> requiredType, String column, Object expectValue) {
    SQLReady sqlReady = new SQLRequest(requiredType).usingAlias(true).select().append(" WHERE " + column + " =?", expectValue);
    return queryForObject(sqlReady, requiredType);
  }
  
  private <T> T getSingleObject(List<T> results) {
    if (results == null || results.isEmpty()) {
      return null;
    }
    return results.get(0);
  }
  
  /**
   * 判断表中的字段值是否存在重复
   * @param table 表名，要有一个主键名称为id
   * @param id 表的主键字段，如果不为空，则此记录被排除在外
   * @param keyValues 被判断的字段名和字段值
   * @return
   */
  public boolean exists(String table, Long id, List<KeyValue<String, Object>> keyValues) {
    Preconditions.checkNotNull(keyValues);
    Preconditions.checkState(keyValues.size() > 0);
    
    SQLReady sqlReady = new SQLReady("select count(*) from ").append(table).append(" where 1=1")
        .appendIfExists(" and id<>? ", id);
    for (KeyValue<String, Object> kv : keyValues) {
      sqlReady = sqlReady.append(" and " + kv.getKey() + "=?", kv.getValue());
    }
    Long count = this.queryForObject(sqlReady.getSql(), Long.class, sqlReady.getArgs());
    
    return count > 0L;
  }
  
}
