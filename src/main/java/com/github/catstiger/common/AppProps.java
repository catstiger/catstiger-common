package com.github.catstiger.common;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 系统全局配置
 * @author samlee
 */
@ConfigurationProperties(prefix = "app", ignoreInvalidFields = true)
public class AppProps implements BeanClassLoaderAware {
  private ClassLoader classLoader;
  
  private CDN cdn;
  
  private Time time;
  
  private Date date;

  public CDN getCdn() {
    return cdn;
  }

  public void setCdn(CDN cdn) {
    this.cdn = cdn;
  }

  public Time getTime() {
    return time;
  }

  public void setTime(Time time) {
    this.time = time;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }
  
  /**
   * 图片存储CDN
   */
  public String cdnImage() {
    if (this.cdn != null) {
      return cdn.image;
    }
    return null;
  }
  
  /**
   * 图片存储CDN
   */
  public String cdnStatic() {
    if (this.cdn != null) {
      return cdn.image;
    }
    return null;
  }
  
  /**
   * 返回全局日期格式
   */
  public String dateFormat() {
    if (this.date != null) {
      return this.date.getFormat();
    }
    return null;
  }
  
  /**
   * 返回全局时间格式
   */
  public String timeFormat() {
    if (this.time != null) {
      return this.time.getFormat();
    }
    return null;
  }

  
  /**
   * 时间格式
   */
  public static class Time {
    private String format;

    public String getFormat() {
      return format;
    }

    public void setFormat(String format) {
      this.format = format;
    }
  }
  
  /**
   * 日期格式
   */
  public static class Date {
    private String format;

    public String getFormat() {
      return format;
    }

    public void setFormat(String format) {
      this.format = format;
    }
  }


  /**
   * CDN configruations
   */
  public static class CDN {
    private String statics;
    private String image;
    
    /**
     * 静态文件CDN
     */
    public String getStatic() {
      return statics;
    }

    public void setStatic(String statics) {
      this.statics = statics;
    }

    /**
     * 图片文件CDN
     */
    public String getImage() {
      return image;
    }

    public void setImage(String image) {
      this.image = image;
    }
  }


  @Override
  public void setBeanClassLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }
  
  public ClassLoader getBeanClassLoader() {
    return classLoader;
  }

}
