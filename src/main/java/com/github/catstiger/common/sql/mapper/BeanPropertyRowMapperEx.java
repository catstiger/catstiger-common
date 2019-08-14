package com.github.catstiger.common.sql.mapper;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.NotWritablePropertyException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import com.github.catstiger.common.util.ReflectUtil;

public class BeanPropertyRowMapperEx<T> extends BeanPropertyRowMapper<T> {
  /**
   * Create a new {@code BeanPropertyRowMapper} for bean-style configuration.
   * 
   * @see #setMappedClass
   * @see #setCheckFullyPopulated
   */
  public BeanPropertyRowMapperEx() {
    super();
  }

  /**
   * Create a new {@code BeanPropertyRowMapper}, accepting unpopulated properties in the target bean.
   * 
   * <p>
   * Consider using the {@link #newInstance} factory method instead, which allows for specifying the mapped type once only.
   * </p>
   * 
   * @param mappedClass the class that each row should be mapped to
   */
  public BeanPropertyRowMapperEx(Class<T> mappedClass) {
    super(mappedClass);
  }

  /**
   * Create a new {@code BeanPropertyRowMapper}.
   * 
   * @param mappedClass the class that each row should be mapped to
   * @param checkFullyPopulated whether we're strictly validating that all bean properties have been mapped from corresponding database
   *          fields
   */
  public BeanPropertyRowMapperEx(Class<T> mappedClass, boolean checkFullyPopulated) {
    super(mappedClass, checkFullyPopulated);
  }

  /**
   * 可以将外键装入对应的Bean中，例如如下的Mapping class分别映射t_user表和t-dept表。其中t_user表的dept_id字段 对应t_dept表的主键字段id.
   * <p/>
   * 
   * <pre>
   * {@link Entity @Entity} {@link Table @Table}("t_user")
   * class User extends BaseEntity {
   *   private String username;
   *   {@link ManyToOne @ManyToOne} {@link JoinColumn @JoinColumn}(name = "dept_id")
   *   private Dept dept;
   *   //getters and setters...
   * }
   * {@link Entity @Entity} {@link Table @Table}("t_dept")
   * class Dept extends BaseEntity {
   *    {@link Id @Id}
   *    private Long id;
   *    private String deptName;
   *    //getters and setters...
   * }
   * 
   * List users = jdbcTemplate.query("select id,username,dept_id from t_users", new BeanPropertyRowMapperEx(User.class))
   * Assert.isTrue(users != null && users.size() > 0);
   * Assert.isTrue(users.get(0).getDept().getId() != null); //自动将dept_id装入到Dept中。
   * </pre>
   * 
   * <p>
   * 注意：只能装入引用表的ID字段
   * </p>
   * 
   * @see BeanPropertyRowMapper#mapRow(ResultSet, int)
   * @see java.sql.ResultSetMetaData
   */
  @Override
  public T mapRow(ResultSet rs, int rowNumber) throws SQLException {
    Assert.state(this.getMappedClass() != null, "Mapped class was not specified");
    T mappedObject = BeanUtils.instantiateClass(this.getMappedClass());
    BeanWrapper bw = new BeanWrapperImplEx(mappedObject); // PropertyAccessorFactory.forBeanPropertyAccess(mappedObject);
    initBeanWrapper(bw);

    ResultSetMetaData rsmd = rs.getMetaData();
    int columnCount = rsmd.getColumnCount();
    Set<String> populatedProperties = (isCheckFullyPopulated() ? new HashSet<String>() : null);

    for (int index = 1; index <= columnCount; index++) {
      String column = JdbcUtils.lookupColumnName(rsmd, index);
      String field = lowerCaseName(column.replaceAll(" ", ""));
      PropertyDescriptor pd = this.getMappedFields().get(field);
      // 如果没有找到PropertyDescriptor，并且字段名称为**_id(表示外键，_id前面是snake_case格式的field名)，则取_id前面的部分作为field
      if (pd == null && field.endsWith("_id")) {
        field = field.substring(0, field.lastIndexOf("_id"));
        pd = this.getMappedFields().get(field);
      }
      if (pd != null) {
        Object value = getColumnValue(rs, index, pd);
        mapColumn(value, bw, pd, column, rowNumber);
        if (populatedProperties != null) {
          populatedProperties.add(pd.getName());
        }
      } else {
        // No PropertyDescriptor found
        if (rowNumber == 0) {
          logger.debug("No property found for column '" + column + "' mapped to field '" + field + "'");
        }
      }
    }

    if (populatedProperties != null && !populatedProperties.equals(this.getMappedProperties())) {
      throw new InvalidDataAccessApiUsageException("Given ResultSet does not contain all fields "
          + "necessary to populate object of class [" + this.getMappedClass().getName() + "]: " + this.getMappedProperties());
    }

    return mappedObject;
  }
  
  private void mapColumn(Object value, BeanWrapper bw, PropertyDescriptor pd, String column, int rowNumber) {
    try {
      try {
        bw.setPropertyValue(pd.getName(), value);
      } catch (TypeMismatchException ex) {
        if (value == null && this.isPrimitivesDefaultedForNullValue()) {
          logger.debug("Intercepted TypeMismatchException for row " + rowNumber + " and column '" + column
              + "' with null value when setting property '" + pd.getName() + "' of type '"
              + ClassUtils.getQualifiedName(pd.getPropertyType()), ex);

        } else {
          throw ex;
        }
      }
    } catch (NotWritablePropertyException ex) {
      throw new DataRetrievalFailureException("Unable to map column '" + column + "' to property '" + pd.getName() + "'", ex);
    }
  }

  @SuppressWarnings("unchecked")
  protected Map<String, PropertyDescriptor> getMappedFields() {
    Field field = ReflectUtil.findField(getClass(), "mappedFields");
    field.setAccessible(true);

    return (Map<String, PropertyDescriptor>) ReflectUtil.getField(field, this);
  }

  @SuppressWarnings("unchecked")
  protected Set<String> getMappedProperties() {
    Field field = ReflectUtil.findField(getClass(), "mappedProperties");
    field.setAccessible(true);
    return (Set<String>) ReflectUtil.getField(field, this);
  }
}
