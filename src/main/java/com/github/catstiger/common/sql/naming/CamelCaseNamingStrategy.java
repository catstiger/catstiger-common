package com.github.catstiger.common.sql.naming;

import java.beans.PropertyDescriptor;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.CaseFormat;

public class CamelCaseNamingStrategy extends AbstractNamingStrategy {

  @Override
  public String columnLabel(PropertyDescriptor propDesc) {
    if (propDesc == null || StringUtils.isBlank(propDesc.getName())) {
      return null;
    }
    return propDesc.getName();
  }

  @Override
  public String columnLabel(ResultSet rs, int columnIndex) {
    try {
      ResultSetMetaData metaData = rs.getMetaData();
      String label = metaData.getColumnLabel(columnIndex);

      if (StringUtils.isBlank(label)) {
        return null;
      }
      if (label.toLowerCase().endsWith("_id")) { // 说明是外键
        label = label.substring(0, label.length() - 3);
      }
      return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, label.toLowerCase());
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

    return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, column.toLowerCase());
  }

}
