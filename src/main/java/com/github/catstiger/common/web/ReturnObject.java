package com.github.catstiger.common.web;

import java.io.Serializable;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;

/**
 * 用于构建返回到客户端的数据，可以直接转换为JSON，render到Response；
 * 也可以直接返回利用<code>ResponseBody</code>转换为JSON。支持链式操作：
 * <pre>
 * return ReturnObject.success().data(yourData);
 * return ReturnObject.error().message("Error Message");
 * </pre>
 * 注意，{@code ReturnObject} 不是线程安全的。
 * @author samlee
 */
public class ReturnObject implements Serializable {
  private static final long serialVersionUID = 3760070610839678501L;
 
  private Boolean success = true;
  private String errorMessage = StringUtils.EMPTY;
  private Object data;
  /**
   * 创建一个新的ReturnObject, 并设置状态“success”为{@code true}
   * @return new instance of ReturnObject
   */
  public static ReturnObject success() {
    ReturnObject returnObject = new ReturnObject();
    returnObject.success = true;
    return returnObject;
  }
  
  /**
   * 创建一个新的ReturnObject, 并设置状态“success”为{@code false}
   * @return new instance of ReturnObject
   */
  public static ReturnObject error() {
    ReturnObject returnObject = new ReturnObject();
    returnObject.success = false;
    return returnObject;
  }
  
  /**
   * 设置{@code ReturnObject}的data属性，这个属性的数据通常是成功处理请求之后的结果。
   * @param data object to set.
   * @return this instance.
   */
  public ReturnObject data(Object data) {
    this.setData(data);
    return this;
  }
  
  /**
   * 设置失败或者成功的相关信息文本
   * @param message 给出相关信息文本
   * @return this instance.
   */
  public ReturnObject message(String message) {
    this.errorMessage = message;
    return this;
  }
  
  /**
   * 序列化当前对象为JSON
   */
  public String toJson() {
    return JSON.toJSONString(this);
  }
  
  /**
   * Render当前对象的JSON，到当前请求对应的<code>HttpServletResponse</code>
   */
  public void writeJson() {
    WebUtil.writeJson(this);
  }
  
  /**
   * Render当前对象到指定的<code>HttpServletResponse</code>
   */
  public void writeJson(HttpServletResponse response) {
    WebUtil.writeJson(response, this);
  }
  
  private ReturnObject() {
    super();
  }

  public Boolean getSuccess() {
    return success;
  }

  public void setSuccess(Boolean success) {
    this.success = success;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public Object getData() {
    return data;
  }

  public void setData(Object data) {
    this.data = data;
  }
}
