package com.github.catstiger.common.poi;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.CellType;
import org.joda.time.DateTime;

import com.github.catstiger.common.util.Converters;
import com.google.common.base.Preconditions;

public final class POIUtil {
  /**
   * 从Cell中获取String类型的值， 如果单元格不是STRING类型，则转换为String类型
   * @param cell Cell
   * @return
   */
  public static String getStringValue(org.apache.poi.ss.usermodel.Cell cell) {
    Preconditions.checkArgument(cell != null);
    
    if (cell.getCellType().equals(CellType.STRING)) {
      return StringUtils.trim(cell.getStringCellValue());
    }
    else if (cell.getCellType().equals(CellType.BLANK)) {
      return StringUtils.EMPTY;
    }
    else if (cell.getCellType().equals(CellType.BOOLEAN)) {
      return Boolean.toString(cell.getBooleanCellValue());
    }
    else if (cell.getCellType().equals(CellType.NUMERIC)) {
      Double num = cell.getNumericCellValue();
      return num.toString();
    } else {
      Date date = cell.getDateCellValue();
      return new DateTime(date).toString("yyyy-MM-dd HH:mm:ss");
    }
  }
  
  @SuppressWarnings("unchecked")
  public static <T> T getValue(org.apache.poi.ss.usermodel.Cell cell, Class<T> requiredClass) {
    T val = null;
    if (String.class.equals(requiredClass)) {
      val = (T) POIUtil.getStringValue(cell);
    } else if (Double.class.equals(requiredClass)) {
      val = (T) new Double(cell.getNumericCellValue());
    } else if (Float.class.equals(requiredClass)) {
      Double num = cell.getNumericCellValue();
      val = (T) new Float(num.floatValue());
    } else if (Long.class.equals(requiredClass)) {
      Double num = cell.getNumericCellValue();
      val = (T) new Long(num.longValue());
    } else if (Integer.class.equals(requiredClass)) {
      Double num = cell.getNumericCellValue();
      val = (T) new Integer(num.intValue());
    } else if (Date.class.equals(requiredClass)) {
      String str = POIUtil.getStringValue(cell);
      Date date = Converters.parseDate(str);
      val = (T) date;
    } else {
      val = (T) POIUtil.getStringValue(cell);
    }
    
    return val;
  }
  
  private POIUtil() {
  }
}
