package com.github.catstiger.common.web;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 保存HttpServletRequest和HttpServletResponse对象，这样可以在任何地方使用这两个变量（Web环境中）。
 * <pre>
 * WebObjectsHolder.getRequest();
 * WebObjectsHolder.getResponse();
 * </pre>
 * 需要将{@code WebObjectsHolderFilter}的顺序配置为第一位
 * <pre>
 * public class WebConfiguration {
 *    public FilterRegistrationBean<WebObjectsHolderFilter>  webObjectsHolderFilter() {
 *      FilterRegistrationBean<WebObjectsHolderFilter> registration = new FilterRegistrationBean<>(new WebObjectsHolderFilter());
 *      registration.addUrlPatterns("/*");
 *      registration.setOrder(0); //顺序为第一位
 *      return registration;
 *    }
 * }
 * </pre>
 * @author samlee
 *
 */
public class WebObjectsHolderFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      WebObjectsHolder.putRequest(request);
      WebObjectsHolder.putResponse(response);
      filterChain.doFilter(request, response);
    } finally {
      WebObjectsHolder.clear();
    }
  }

}
