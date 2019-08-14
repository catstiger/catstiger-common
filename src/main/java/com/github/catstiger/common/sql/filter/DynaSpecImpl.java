package com.github.catstiger.common.sql.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.github.catstiger.common.sql.Page;
import com.github.catstiger.common.sql.filter.QueryPart.Operator;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DynaSpecImpl implements DynaSpec {
  private List<QueryPart> parts = new ArrayList<QueryPart>();
  private List<Order> orders = new ArrayList<Order>();
  private int firstResult = 0;
  private int maxResults = 0;
  private Class rootEntityClass;

  public DynaSpecImpl() {

  }

  public DynaSpecImpl(List<QueryPart> parts) {
    this.parts.addAll(parts);
  }

  public DynaSpecImpl(List<QueryPart> parts, List<Order> orders) {
    this.parts.addAll(parts);
    this.orders.addAll(orders);
  }

  /**
   * 用QueryPart，Order， Page对象，创建一个完整的DynaSpecImpl
   */
  public DynaSpecImpl(List<QueryPart> parts, List<Order> orders, Page pageInfo) {
    this.parts.addAll(parts);
    this.orders.addAll(orders);
    this.firstResult = pageInfo.getStart();
    this.maxResults = pageInfo.getLimit();
  }

  @Override
  public DynaSpec add(QueryPart queryPart) {
    parts.add(queryPart);
    return this;
  }

  @Override
  public DynaSpec addSort(String sortName, String sortOrder) {
    if (sortOrder == null || (!"ASC".equalsIgnoreCase(sortOrder) && !"DESC".equalsIgnoreCase(sortOrder))) {
      sortOrder = "ASC";
    }
    Order order = "ASC".equalsIgnoreCase(sortOrder) ? Order.asc(sortName) : Order.desc(sortName);
    orders.add(order);
    return this;
  }

  @Override
  public DynaSpec setFirstResult(Integer first) {
    this.firstResult = first;
    return this;
  }

  @Override
  public DynaSpec setMaxResults(Integer maxRows) {
    this.maxResults = maxRows;
    return this;
  }

  @Override
  public DynaSpec setPage(Page page) {
    firstResult = page.getStart();
    maxResults = page.getLimit();

    return this;
  }

  @Override
  public Page getPage() {
    return new Page(firstResult, maxResults);
  }

  @Override
  public <T> DynaSpec setRoot(Class<T> entityClass) {
    this.rootEntityClass = entityClass;
    return this;
  }

  @Override
  public <T> Class<T> getRoot() {
    return rootEntityClass;
  }

  @Override
  public <T> String buildQueryString(Class<T> entityClass) {
    throw new UnsupportedOperationException("尚未实现！");
  }

  @Override
  public String buildQueryString(String tableName) {
    StringBuilder sql = new StringBuilder(200);
    for (Iterator<QueryPart> itr = parts.iterator(); itr.hasNext();) {
      QueryPart queryPart = itr.next();
      // 如果查询参数为null，则说明不必考虑这个参数
      if (queryPart.getValue() == null && !queryPart.getOperator().equals(Operator.NULL)
          && !queryPart.getOperator().equals(Operator.NOTNULL)) {
        continue;
      }
      sql.append(" AND ").append(whereSnippet(queryPart, tableName));
    }

    return sql.toString();
  }

  @Override
  public String buildQueryString() {
    return buildQueryString(StringUtils.EMPTY);
  }

  @Override
  public Object[] getQueryParams() {
    List<Object> params = new ArrayList<Object>();

    for (QueryPart queryPart : parts) {
      Object value = queryPart.getValue();
      // 如果查询参数为null，则说明不必考虑这个参数
      if (queryPart.getValue() == null || queryPart.getOperator().equals(Operator.NULL)
          || queryPart.getOperator().equals(Operator.NOTNULL)) {
        continue;
      }

      params.add(value);
    }
    return params.toArray();
  }

  private String whereSnippet(QueryPart queryPart, String table) {
    String prefix = StringUtils.EMPTY;
    if (StringUtils.isNotBlank(table)) {
      prefix = table + ".";
    }
    
    SQLDecorator sqlDecorator = SQLDecorators.get(queryPart.getOperator());
    if (sqlDecorator != null) {
      return sqlDecorator.decorate(queryPart.getFieldName(), prefix);
    }
    return StringUtils.EMPTY;
  }

  public static interface SQLDecorator {
    String decorate(String fieldName, String prefix);
  }

  private static final Map<Operator, SQLDecorator> SQLDecorators = new HashMap<>(18);

  static {
    // 处理EQ（等于） SQL
    SQLDecorators.put(Operator.EQ, new SQLDecorator() {
      @Override
      public String decorate(String fieldName, String prefix) {
        return new StringBuilder(100).append(prefix).append(fieldName).append("= ? ").toString();
      }
    });
    // 处理 GE(大于等于) SQL
    SQLDecorators.put(Operator.GE, new SQLDecorator() {
      @Override
      public String decorate(String fieldName, String prefix) {
        return new StringBuilder(100).append(prefix).append(fieldName).append(">= ?").toString();
      }
    });

    // 处理 GE(大于) SQL
    SQLDecorators.put(Operator.GT, new SQLDecorator() {
      @Override
      public String decorate(String fieldName, String prefix) {
        return new StringBuilder(100).append(prefix).append(fieldName).append(" > ?").toString();
      }
    });

    // 处理 IN(不支持) SQL
    SQLDecorators.put(Operator.IN, new SQLDecorator() {
      @Override
      public String decorate(String fieldName, String prefix) {
        return StringUtils.EMPTY;
      }
    });

    // 处理 LE(小于等于) SQL
    SQLDecorators.put(Operator.LE, new SQLDecorator() {
      @Override
      public String decorate(String fieldName, String prefix) {
        return new StringBuilder(100).append(prefix).append(fieldName).append(" <= ? ").toString();
      }
    });

    // 处理 LLK (Like 左匹配) SQL
    SQLDecorators.put(Operator.LLK, new SQLDecorator() {
      @Override
      public String decorate(String fieldName, String prefix) {
        return new StringBuilder(100).append("LOCATE(reverse(?), reverse(").append(prefix).append(fieldName).append(")) = 1").toString();
      }
    });
    
    // 处理 LK(Like) SQL
    SQLDecorators.put(Operator.LK, new SQLDecorator() {
      @Override
      public String decorate(String fieldName, String prefix) {
        return new StringBuilder(100).append("LOCATE(?, ").append(prefix).append(fieldName).append(") > 0").toString();
      }
    });
    
    // 处理 NLLK(与LLK相反) SQL
    SQLDecorators.put(Operator.NLLK, new SQLDecorator() {
      @Override
      public String decorate(String fieldName, String prefix) {
        return new StringBuilder(100).append("LOCATE(reverse(?), reverse(").append(prefix).append(fieldName).append(")) != 1").toString();
      }
    });
    
    // 处理 NLK(Not Like) SQL
    SQLDecorators.put(Operator.NLK, new SQLDecorator() {
      @Override
      public String decorate(String fieldName, String prefix) {
        return new StringBuilder(100).append("LOCATE(?, ").append(prefix).append(fieldName).append(") = 0").toString();
      }
    });

    // 处理 NRLK(Not右边匹配) SQL
    SQLDecorators.put(Operator.NRLK, new SQLDecorator() {
      @Override
      public String decorate(String fieldName, String prefix) {
        return new StringBuilder(100).append("LOCATE(?, ").append(prefix).append(fieldName).append(") != 1").toString();
      }
    });
    
    // 处理 RLK(右边匹配) SQL
    SQLDecorators.put(Operator.RLK, new SQLDecorator() {
      @Override
      public String decorate(String fieldName, String prefix) {
        return new StringBuilder(100).append("LOCATE(?, ").append(prefix).append(fieldName).append(") = 1").toString();
      }
    });
    
    // 处理 LT(小于) SQL
    SQLDecorators.put(Operator.LT, new SQLDecorator() {
      @Override
      public String decorate(String fieldName, String prefix) {
        return new StringBuilder(100).append(prefix).append(fieldName).append(" < ? ").toString();
      }
    });
    
    // 处理 NE(不等于) SQL
    SQLDecorators.put(Operator.NE, new SQLDecorator() {
      @Override
      public String decorate(String fieldName, String prefix) {
        return new StringBuilder(100).append(prefix).append(fieldName).append(" <> ? ").toString();
      }
    });
    
    // 处理 NIN(不支持) SQL
    SQLDecorators.put(Operator.NIN, new SQLDecorator() {
      @Override
      public String decorate(String fieldName, String prefix) {
        return StringUtils.EMPTY;
      }
    });
    
    // 处理 NOTNULL(Is not null) SQL
    SQLDecorators.put(Operator.NOTNULL, new SQLDecorator() {
      @Override
      public String decorate(String fieldName, String prefix) {
        return new StringBuilder(100).append(prefix).append(fieldName).append(" is not null ").toString();
      }
    });
    
    // 处理 NULL(Is null) SQL
    SQLDecorators.put(Operator.NULL, new SQLDecorator() {
      @Override
      public String decorate(String fieldName, String prefix) {
        return new StringBuilder(100).append(prefix).append(fieldName).append(" is null ").toString();
      }
    });

  }
}
