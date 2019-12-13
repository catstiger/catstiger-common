package com.github.catstiger.common.sql;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.ibatis.session.RowBounds;

import com.github.catstiger.common.util.Exceptions;
import com.github.catstiger.common.web.WebObjectsHolder;
import com.github.catstiger.common.web.controller.BaseController;

/**
 * 分页对象.包含数据及分页信息.
 * 
 * @author Sam
 */
public class Page implements Serializable {

  private static final long serialVersionUID = 231152607479172128L;

  public static final int DEFAULT_PAGE_SIZE = 15;

  public static final int DEFAULT_STEPS = 10;

  public static final int FIRST_PAGE_INDEX = 0;

  private int start;

  private int limit;

  private long total;

  private Collection<?> rows;

  private Collection<?> summaryData;

  public Page() {
    this.start = 0;
    this.limit = DEFAULT_PAGE_SIZE;
  }

  /**
   * 根据起始行和PageSize构造
   * @param start 起始行，第一行是0
   * @param limit page size
   */
  public Page(int start, int limit) {
    this.start = start;
    if (this.start < FIRST_PAGE_INDEX) {
      this.start = FIRST_PAGE_INDEX;
    }
    this.limit = limit;
    if (this.limit <= 0) {
      this.limit = DEFAULT_PAGE_SIZE;
    }
  }

  /**
   * 用于实现Google风格的分页
   */
  public Integer[] getSteps() {
    int startPage = calcStartPage();
    int stepSize = calcStepPageSize();
    List<Integer> steps = new ArrayList<Integer>();
    for (int i = startPage; i < startPage + stepSize; i++) {
      steps.add(i);
    }
    return steps.toArray(new Integer[] {});
  }

  /**
   * calculate fist page No of fast step.
   */
  private int calcStartPage() {
    if (getPageNo() < (DEFAULT_STEPS / 2) || (getPageNo() - (DEFAULT_STEPS / 2)) < 1) {
      return 1;
    } else {
      return getPageNo() - (DEFAULT_STEPS / 2);
    }
  }

  private int calcStepPageSize() {
    if ((calcStartPage() + DEFAULT_STEPS) > getPages()) {
      return getPages() - calcStartPage() + 1;
    } else {
      return DEFAULT_STEPS;
    }
  }

  public int getPageNo() {
    return (start / limit) + 1;
  }

  /**
   * 取总页数
   */
  public int getPages() {
    if (((int) total) % limit == 0) {
      return ((int) total) / limit;
    } else {
      return ((int) total) / limit + 1;
    }
  }

  public int getStart() {
    return start;
  }

  public void setStart(int start) {
    this.start = start;
  }

  public int getOffset() {
    return start;
  }

  public void setOffset(int offset) {
    this.start = offset;
  }

  public int getLimit() {
    return limit;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }

  public long getTotal() {
    return total;
  }

  public void setTotal(long total) {
    this.total = total;
  }

  public Collection<?> getRows() {
    return rows;
  }

  public void setRows(Collection<?> rows) {
    this.rows = rows;
  }
  
  /**
   * 转换为RowBounds对象，方便Mybatis使用、
   */
  public RowBounds toRowBounds() {
    return new RowBounds(start, limit);
  }

  /**
   * 创建一个Page实例，使用给定的start和limit参数
   */
  public static Page createPage(Integer start, Integer limit) {
    if (start != null && limit != null) {
      return new Page(start, limit);
    }

    try {
      String paramStart = WebObjectsHolder.getRequest().getParameter(BaseController.PARAM_START);
      if (NumberUtils.isCreatable(paramStart)) {
        start = Integer.valueOf(paramStart);
      } else {
        start = 0;
      }

      String paramLimit = WebObjectsHolder.getRequest().getParameter(BaseController.PARAM_PAGE_SIZE);
      if (NumberUtils.isCreatable(paramLimit)) {
        limit = Integer.valueOf(paramLimit);
      } else {
        limit = BaseController.DEFAULT_PAGE_SIZE;
      }
      return new Page(start, limit);
    } catch (Exception e) {
      throw Exceptions.unchecked(e);
    }
  }

  public static Page createPage() {
    return createPage(null, null);
  }

  public Collection<?> getSummaryData() {
    return summaryData;
  }

  public void setSummaryData(Collection<?> summaryData) {
    this.summaryData = summaryData;
  }
  
  /**
   * 相当于{@code #setStart(int)} 
   * @param start offset of the result set;
   * @return this page
   */
  public Page start(int start) {
    this.start = start;
    return this;
  }
  
  /**
   * 相当于{@code #setLimit(int)}
   * @param limit
   * @return this page
   */
  public Page limit(int limit) {
    this.limit = limit;
    return this;
  }
  
  /**
   * 相当于{@code #getTotal()}
   * @param c 
   * @return this page
   */
  public Page total(long c) {
    this.total = c;
    return this;
  }
  
  /**
   * 相当于{@code #rows}
   * @param rows
   * @return
   */
  public Page rows(Collection<?> rows) {
    this.rows = rows;
    return this;
  }
  
  /**
   * 简化版{@code #getStart()}
   * @return
   */
  public int start() {
    return this.start;
  }
  
  /**
   * 简化版{@code #getLimit()}
   * @return
   */
  public int limit() {
    return this.limit;
  }
}
