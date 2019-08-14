package com.github.catstiger.common.sql.naming;

import java.beans.PropertyDescriptor;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.CaseFormat;

/**
 * 字段名全小写，作为ColumnLabel
 * 
 * @author catstiger
 *
 */
public class SimpleNamingStrategy extends AbstractNamingStrategy {

  @Override
  public String columnLabel(PropertyDescriptor propDesc) {
    if (propDesc == null || StringUtils.isBlank(propDesc.getName())) {
      return null;
    }

    return propDesc.getName().toLowerCase();
  }

  @Override
  public String columnLabel(ResultSet rs, int columnIndex) {
    try {
      ResultSetMetaData metaData = rs.getMetaData();
      String label = metaData.getColumnLabel(columnIndex);

      if (StringUtils.isBlank(label)) {
        return null;
      }

      return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, label.toLowerCase()).toLowerCase();
    } catch (SQLException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  @Override
  public String columnLabel(String column) {
    if (StringUtils.isBlank(column)) {
      return StringUtils.EMPTY;
    }
    return column.toLowerCase();
  }

  @Override
  public String simpleAlias(Class<?> entityClass) {
    return null;
  }

}
