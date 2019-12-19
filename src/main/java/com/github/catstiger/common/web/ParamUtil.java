package com.github.catstiger.common.web;

import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.alibaba.fastjson.JSON;
import com.github.catstiger.common.sql.Page;
import com.github.catstiger.common.web.controller.BaseController.Sort;

public final class ParamUtil {
  /**
   * 页面传递Page No的参数名称
   */
  public static final String PARAM_PAGE_NO = "page";

  /**
   * 页面传递的Page Size的参数名称
   */
  public static final String PARAM_PAGE_SIZE = "limit";

  /**
   * 页面传递的排序字段的名称
   */
  public static final String PARAM_SORT_NAME = "sort";

  /**
   * 向页面传递总数量的参数名称
   */
  public static final String ATTR_TOTAL_ROWS = "total";

  /**
   * 向页面传递数据块的参数名称
   */
  public static final String ATTR_PAGE_DATA = "rows";

  /**
   * 向页面传递的当前页码得参数名称
   */
  public static final String ATTR_PAGE_NO = "page";

  /**
   * 页面传递的排序方向的字段名称
   */
  public static final String PARAM_SORT_ORDER = "order";

  /**
   * 页面传递的第一条记录的参数
   */
  public static final String PARAM_START = "start";

  /**
   * 缺省Page Size
   */
  public static final int DEFAULT_PAGE_SIZE = 15;

  /**
   * 得到分页页码
   */
  public static int getPageNo() {
    String pageNo = WebUtil.getRequest().getParameter(PARAM_PAGE_NO);

    if (StringUtils.isBlank(pageNo)) {
      // bootstrap table 使用offset作为分页参数
      String offset = WebUtil.getRequest().getParameter("offset");
      if (StringUtils.isNotBlank(offset) && StringUtils.isNumeric(offset)) {
        Integer start = Integer.valueOf(offset);
        return new Page(start, getPageSize()).getPageNo();
      } else {
        return Page.FIRST_PAGE_INDEX;
      }
    }
    return Integer.valueOf(pageNo);
  }
  
  /**
   * 得到分页Page Size
   * 
   * @return
   */
  public static int getPageSize() {
    String pageSize = WebUtil.getRequest().getParameter(PARAM_PAGE_SIZE);

    if (!NumberUtils.isCreatable(pageSize)) {
      return DEFAULT_PAGE_SIZE;
    }

    return Integer.valueOf(pageSize);
  }
  
  /**
   * 首行的索引，0是第一个
   */
  public static int getStart() {
    String start = WebUtil.getRequest().getParameter(PARAM_START);

    if (StringUtils.isBlank(start)) {
      return 0;
    }

    return Integer.valueOf(start);
  }
  
  /**
   * 得到排序字段名，如果参数中没有数据，返回<code>null</code>
   */
  public static String getSortName() {
    String sortJson = WebUtil.getRequest().getParameter(PARAM_SORT_NAME);
    if (sortJson == null) {
      return null;
    }
    // bootstrap table 直接传的排序名称
    if (sortJson.indexOf("{") < 0) {
      return sortJson;
    }
    try {
      if (sortJson != null) {
        Sort[] sorts = JSON.parseObject(sortJson, Sort[].class);
        if (sorts != null && sorts.length > 0) {
          return sorts[sorts.length - 1].getProperty();
        }
      }
    } catch (Exception e) {
      // Nothing need to do
    }
    return null;
  }
  
  /**
   * 得到排序方向（asc,desc）,如果参数中没有数据返回空字符串
   */
  public static String getSortOrder() {
    // 直接传递order,bootstrap table
    String order = WebUtil.getRequest().getParameter(PARAM_SORT_ORDER);
    if (StringUtils.isNotBlank(order)) {
      return order;
    }

    String sortJson = WebUtil.getRequest().getParameter(PARAM_SORT_NAME);
    if (StringUtils.isBlank(sortJson)) {
      return StringUtils.EMPTY;
    }
    try {
      Sort[] sorts = JSON.parseObject(sortJson, Sort[].class);
      if (sorts != null && sorts.length > 0) {
        return sorts[sorts.length - 1].getDirection();
      }
    } catch (Exception e) {
      // Nothing need to do
    }
    return StringUtils.EMPTY;
  }
  
  /**
   * 返回一个根据当前分页条件而创建的Page对象，该对象常用于pageQuery的参数
   */
  public static Page page() {
    Page page;
    try {
      page = new Page(getStart(), getPageSize());
      page.setRows(Collections.emptyList());
    } catch (Exception e) {
      //测试状态
      page = Page.createPage(0, Page.DEFAULT_PAGE_SIZE);
    }
    return page;
  }
  
  private ParamUtil() {
    
  }
}
