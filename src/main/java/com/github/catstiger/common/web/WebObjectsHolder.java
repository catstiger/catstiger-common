package com.github.catstiger.common.web;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 保存<code>HttpServletRequest</code>和<code>HttpServletResponse</code>对象，这样
 * 可以在应用程序的任何地方使用这些对象。
 * <p>需要首先配置WebObjectsHolderFilter</p>
 * @author samlee
 *
 */
public final class WebObjectsHolder {
  private static final ThreadLocal<HttpServletRequest> requestHolder = new InheritableThreadLocal<HttpServletRequest>();
  private static final ThreadLocal<HttpServletResponse> responseHolder = new InheritableThreadLocal<HttpServletResponse>();
  
  static void putRequest(HttpServletRequest request) {
    requestHolder.set(request);
  }
  
  static void putResponse(HttpServletResponse response) {
    responseHolder.set(response);
  }
  
  /**
   * 清空当前线程绑定的{@code HttpServletRequest}对象和{@code HttpServletResponse}对象
   */
  public static void clear() {
    requestHolder.remove();
    responseHolder.remove();
  }
  
  /**
   * 返回当前请求的HttpServletRequest对象
   * @return 当前请求的HttpServletRequest对象
   * @throws IllegalStateException 如果没有任何对象，通常发生在错误配置WebSecurityFilter的情况下
   */
  public static HttpServletRequest getRequest() {
    HttpServletRequest request = requestHolder.get();
    if (request == null) {
      throw new IllegalStateException("当前线程未绑定HttpServletRequest对象。");
    }
    return request;
  }
  
  /**
   * 返回当前请求的HttpServletResponse对象
   * @return 当前请求的HttpServletResponse对象
   */
  public static HttpServletResponse getResponse() {
    HttpServletResponse response = responseHolder.get();
    if (response == null) {
      throw new IllegalStateException("当前线程未绑定HttpServletResponse对象。");
    }
    return response;
  }
  
  public static ServletContext getContext() {
    return getRequest().getServletContext();
  }
  
  private WebObjectsHolder() {
    
  }
}
