package com.github.catstiger.common.poi;

import java.io.Serializable;

/**
 * Cell验证，返回数据
 * @author Think
 *
 */
public class CellValid implements Serializable {
  private static final long serialVersionUID = 36056542710263028L;
 
  private boolean isValid;
  private String errorMessage;
 
  public static final CellValid SUCCESSED = new CellValid(true);

  public CellValid(boolean isValid) {
    this.isValid = isValid;
  }
  
  public CellValid(boolean isValid, String errorMessage) {
    this.isValid = isValid;
    this.errorMessage = errorMessage;
  }
  
  public boolean isValid() {
    return isValid;
  }

  public void setValid(boolean isValid) {
    this.isValid = isValid;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}
