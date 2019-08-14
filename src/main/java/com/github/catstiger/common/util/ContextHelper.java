package com.github.catstiger.common.util;

import java.net.URL;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public abstract class ContextHelper {
  private static Logger logger = LoggerFactory.getLogger(ContextHelper.class);

  private static ServletContext servletContext = null;
  private static WebApplicationContext webApplicationContext = null;

  /**
   * 初始化，在Spring应用程序启动的时候调用，传入ServerletContext
   */
  public static void init(ServletContext servletCtx) {
    logger.debug("Initialize ContextHelper...");
    servletContext = servletCtx;
    if (servletContext != null) {
      webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
    }
  }

  public static WebApplicationContext getWebApplicationContext() {
    return webApplicationContext;
  }

  public static <T> T getBean(Class<T> beanClass) {
    return webApplicationContext.getBean(beanClass);
  }

  /**
   * 判断是否是Ms Windows操作系统
   */
  public static boolean isWindows() {
    String os = System.getProperty("os.name");
    return (os != null && os.toLowerCase().indexOf("windows") >= 0);
  }

  /**
   * 返回操作系统名称
   */
  public static String getOs() {
    return System.getProperty("os.name");
  }

  /**
   * @return WebApplication real path
   */
  public static String getRealPath(String path) {
    if (servletContext != null) {
      return servletContext.getRealPath(path);
    }
    return null;
  }
  
  /**
   * @return WebApplication real path of "/"
   */
  public static String getRealPath() {
    if (servletContext != null) {
      return servletContext.getRealPath("/");
    }
    return null;
  }

  /**
   * 返回Context path
   * 
   * @return
   */
  public static String getContextPath() {
    return servletContext.getContextPath();
  }

  public static String getClassPath() {
    URL resource = ContextHelper.class.getResource("/");
    return resource.getPath();
  }
}
