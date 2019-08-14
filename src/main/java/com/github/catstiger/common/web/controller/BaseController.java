package com.github.catstiger.common.web.controller;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.github.catstiger.common.sql.ORMHelper;
import com.github.catstiger.common.sql.Page;
import com.github.catstiger.common.web.WebObjectsHolder;
import com.github.catstiger.common.web.WebUtil;
import com.google.common.base.CaseFormat;

public abstract class BaseController {
  protected Logger logger = LoggerFactory.getLogger(getClass());
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
   * TypeAhead形式的combo传递的参数名称
   */
  public static final String PARAM_QUERY = "query";

  /**
   * 缺省显示的页码数量(Google风格)
   */
  public static final int DEFAULT_STEPS = 10;

  protected HttpServletRequest getRequest() {
    return WebObjectsHolder.getRequest();
  }

  protected HttpServletResponse getResponse() {
    return WebObjectsHolder.getResponse();
  }

  /**
   * 得到分页页码
   */
  protected int getPageNo() {
    String pageNo = getRequest().getParameter(PARAM_PAGE_NO);

    if (StringUtils.isBlank(pageNo)) {
      // bootstrap table 使用offset作为分页参数
      String offset = getRequest().getParameter("offset");
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
  protected int getPageSize() {
    String pageSize = getRequest().getParameter(PARAM_PAGE_SIZE);

    if (StringUtils.isBlank(pageSize)) {
      return DEFAULT_PAGE_SIZE;
    }

    return Integer.valueOf(pageSize);
  }

  /**
   * 首行的索引，0是第一个
   */
  protected int getStart() {
    String start = getRequest().getParameter(PARAM_START);

    if (StringUtils.isBlank(start)) {
      return 0;
    }

    return Integer.valueOf(start);
  }

  /**
   * 得到排序字段名，如果参数中没有数据，返回<code>null</code>
   */
  protected String getSortName() {
    String sortJson = getRequest().getParameter(PARAM_SORT_NAME);
    if (sortJson == null) {
      return null;
    }
    // bootstrap table 直接传的排序名称
    if (sortJson.indexOf("{") < 0) {
      return sortJson;
    }
    try {
      if (sortJson != null) {
        Sort[] sorts = fromJson(sortJson, Sort[].class);
        if (sorts != null && sorts.length > 0) {
          return sorts[sorts.length - 1].property;
        }
      }
    } catch (Exception e) {
      // Nothing need to do
    }
    return null;
  }

  /**
   * 返回排序的字段名，根据排序的属性名称，找到对应的数据库的字段名。
   * 
   * @param entityType 映射的实体类
   * @return 数据库字段名，如果没有，返回实体属性名，如果客户端没有传递任何字段名，返回{@code null}
   */
  protected <T> String getSortName(Class<T> entityType) {
    String fieldName = getSortName();
    if (StringUtils.isBlank(fieldName)) {
      return null;
    }

    return ORMHelper.getInstance().columnNameByField(entityType, fieldName);
  }

  /**
   * 得到排序方向（asc,desc）,如果参数中没有数据返回空字符串
   */
  protected String getSortOrder() {
    // 直接传递order,bootstrap table
    String order = getRequest().getParameter(PARAM_SORT_ORDER);
    if (StringUtils.isNotBlank(order)) {
      return order;
    }

    String sortJson = getRequest().getParameter(PARAM_SORT_NAME);
    if (StringUtils.isBlank(sortJson)) {
      return StringUtils.EMPTY;
    }
    try {
      Sort[] sorts = fromJson(sortJson, Sort[].class);
      if (sorts != null && sorts.length > 0) {
        return sorts[sorts.length - 1].direction;
      }
    } catch (Exception e) {
      // Nothing need to do
    }
    return StringUtils.EMPTY;
  }

  /**
   * 返回一个根据当前分页条件而创建的Page对象，该对象常用于pageQuery的参数
   */
  protected Page page() {
    Page page = new Page(getStart(), getPageSize());
    page.setRows(Collections.emptyList());
    return page;
  }

  /**
   * ExtJS4, 排序对象 (sort = [{"property":"name","direction":"DESC"}])
   */
  public static class Sort {

    private String property;
    private String direction;

    public Sort() {
    }

    public Sort(String property, String direction) {
      this.property = property;
      this.direction = direction;
    }

    public String getProperty() {
      return property;
    }

    public String getDirection() {
      return direction;
    }

    public String getColumn() {
      return CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, property);
    }

    public void setProperty(String property) {
      this.property = property;
    }

    public void setDirection(String direction) {
      this.direction = direction;
    }
  }

  /**
   * 将Json转换为Object
   */
  protected <T> T fromJson(String json, Class<T> cls) {
    return JSON.parseObject(json, cls);
  }

  /**
   * 直接输出数据，而不是通过MVC框架重新定向.
   * 
   * @param response The HttpServletResponse object to be write in
   * @param text Content to be writed.
   * @param contentType MIME content type.
   */
  public static void render(HttpServletResponse response, String text, String contentType) {
    WebUtil.setNoCacheHeader(response);
    try {
      response.setContentType(contentType);
      response.setCharacterEncoding("UTF-8");
      response.getWriter().write(text);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void renderText(String text) {
    render(WebObjectsHolder.getResponse(), text, "text/plain");
  }

  public static void renderHTML(String html) {
    render(WebObjectsHolder.getResponse(), html, "text/html");
  }

  /**
   * 直接输出Json，如果是jsonp请求，则处理为符合要求的格式
   */
  public static void renderJson(HttpServletRequest request, HttpServletResponse response, String text) {
    render(response, jsonp(request, text), "application/json");
  }

  /**
   * 直接输出Json，如果是jsonp请求，则处理为符合要求的格式
   */
  public static void renderJson(String text) {
    HttpServletRequest request = WebObjectsHolder.getRequest();
    HttpServletResponse response = WebObjectsHolder.getResponse();

    renderJson(request, response, text);
  }

  /**
   * 渲染一个符合ExtJS Form load()规范的JSON数据
   * 
   * @param data 需要返回到客户端的数据
   * @param success 是否成功
   */
  protected Map<String, Object> forExt(Object data, boolean success) {
    Map<String, Object> model = new HashMap<String, Object>(2);
    model.put("data", data);
    model.put("success", success);

    return model;
  }

  protected Map<String, Object> forExt(boolean success) {
    Map<String, Object> model = new HashMap<String, Object>(2);
    model.put("data", StringUtils.EMPTY);
    model.put("success", success);

    return model;
  }

  /**
   * 渲染一个符合ExtJS Form load()错误规范的JSON数据
   */
  protected Map<String, Object> forExt(String msg) {
    Map<String, Object> model = new HashMap<String, Object>(2);
    model.put("success", false);
    model.put("errorMessage", msg);

    return model;
  }

  /**
   * 将json数据处理为符合jsonp要求的格式：根据callback参数，为原始json加上前后缀
   */
  public static String jsonp(HttpServletRequest request, String json) {
    if (StringUtils.isEmpty(request.getParameter("callback"))) {
      return json;
    }
    return new StringBuilder(json.length()).append(request.getParameter("callback")).append("(").append(json).append(")").toString();
  }

}
