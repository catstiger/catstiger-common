package com.github.catstiger.common.sql.filter;
/**
 * 操作符，包含SQL中的=，<>, <, >, <=, >=, LIKE, NOT LIKE, IS NULL, IS NOT NULL
 */
public enum Operator {
  /**
   * Equals, 表示相等，SQL =
   */
  EQ, 
  
  /**
   * Not equals, 表示不等， SQL &lt;&gt;
   */
  NE,
  
  /**
   * Less than, 小于， SQL &lt;
   */
  LT, 
  
  /**
   * Great than, 打印， SLQ &gt;
   */
  GT, 
  
  /**
   * Equals or less than, 小于或者等于， SQL &lt;=
   */
  LE, 
  
  /**
   * Equals or great than, 大于或者等于， SQL &gt;=
   */
  GE, 
  
  /**
   * Like, 完全匹配， %xxx%
   */
  LK, 
  
  /**
   * Left Like, 左匹配， %xxx
   */
  LLK, 
  
  /**
   * Right like, 右匹配， xxx%
   */
  RLK, 
  
  /**
   * Not Like
   */
  NLK, 
  
  /**
   * NOT left like, eg: NOT LIKE '%xxx'
   */
  NLLK,
  
  /**
   * NOT right like, eg: NOT LIKE 'xxx%'
   */
  NRLK,
  
  /**
   * IS NULL
   */
  NULL, 
  
  /**
   * IS NOT NULL
   */
  NOTNULL, 
  
  /**
   * IN
   */
  IN, 
  
  /**
   * NOT IN
   */
  NIN
}
