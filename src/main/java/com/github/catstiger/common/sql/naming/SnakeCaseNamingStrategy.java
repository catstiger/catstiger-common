package com.github.catstiger.common.sql.naming;

import java.beans.PropertyDescriptor;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.CaseFormat;

public class SnakeCaseNamingStrategy extends AbstractNamingStrategy {

  @Override
  public String columnLabel(PropertyDescriptor propDesc) {
    if (propDesc == null || StringUtils.isBlank(propDesc.getName())) {
      return null;
    }
    return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, propDesc.getName());
  }

  @Override
  public String columnLabel(ResultSet rs, int columnIndex) {
    try {
      ResultSetMetaData metaData = rs.getMetaData();
      String label = metaData.getColumnLabel(columnIndex);

      if (StringUtils.isBlank(label)) {
        return null;
      }

      return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, label);
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

    return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, column);
  }
}
