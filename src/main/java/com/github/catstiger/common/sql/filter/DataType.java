package com.github.catstiger.common.sql.filter;

/**
 * DynaSpec支持的数据类型
 * @author samlee
 *
 */
public enum DataType {
  /**
   * 字符串类型，Java String， SQL varchar.
   */
  S, 
  
  /**
   * 长整型， Java Long， SQL Bigint
   */
  L, 
  
  /**
   * 布尔，Java Boolean， SQL tinyint
   */
  B, 
  
  /**
   * 整形，Java Integer， SQL integer
   */
  N, 
  
  /**
   * BigDecimal
   */
  BD, 
  
  /**
   * Double
   */
  DBL, 
  
  /**
   * Float
   */
  FT,
  
  /**
   * Short
   */
  SN, 
  
  /**
   * Date
   */
  D
}
