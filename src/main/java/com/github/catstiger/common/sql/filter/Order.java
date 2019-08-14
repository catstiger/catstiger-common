package com.github.catstiger.common.sql.filter;

import java.io.Serializable;

/**
 * Represents an order imposed upon a <tt>Criteria</tt> result set
 * 
 * @author Gavin King
 * @author Lukasz Antoniak (lukasz dot antoniak at gmail dot com)
 * @author Brett Meyer
 */
@SuppressWarnings("serial")
public class Order implements Serializable {
  private boolean ascending;
  private boolean ignoreCase;
  private String propertyName;

  public String toString() {
    return propertyName + ' ' + (ascending ? "asc" : "desc");
  }

  public Order ignoreCase() {
    ignoreCase = true;
    return this;
  }

  /**
   * Constructor for Order.
   */
  protected Order(String propertyName, boolean ascending) {
    this.propertyName = propertyName;
    this.ascending = ascending;
  }

  /**
   * Render the SQL fragment
   *
   */
  public String toSqlString() {
    return new StringBuilder(30).append(" ").append(propertyName).append(" ").append(ascending ? "asc" : "desc").toString();
  }

  public String getPropertyName() {
    return propertyName;
  }

  public boolean isAscending() {
    return ascending;
  }

  public boolean isIgnoreCase() {
    return ignoreCase;
  }

  /**
   * Ascending order
   *
   * @param propertyName 属性名称
   * @return Order
   */
  public static Order asc(String propertyName) {
    return new Order(propertyName, true);
  }

  /**
   * Descending order
   *
   * @param propertyName 属性名称
   * @return Order
   */
  public static Order desc(String propertyName) {
    return new Order(propertyName, false);
  }

}
