package com.github.catstiger;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.datasource")
public class DataSourceProp {
  private String driverClassName;
  private String url;

  public String getDriverClassName() {
    return driverClassName;
  }

  public void setDriverClassName(String driverClassName) {
    this.driverClassName = driverClassName;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }
}
