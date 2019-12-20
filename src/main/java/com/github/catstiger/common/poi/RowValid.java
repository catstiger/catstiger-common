package com.github.catstiger.common.poi;

import java.io.Serializable;

public class RowValid implements Serializable {
  private static final long serialVersionUID = 8447796080212542237L;
  
  private boolean isValid;
  private String errorMessage;
  private int row;
  private Object entity;
  

  public RowValid(boolean isValid, String errorMessage) {
    this.isValid = isValid;
    this.errorMessage = errorMessage;
  }

  public RowValid(int row, Object entity) {
    this.isValid = true;
    this.row = row;
    this.entity = entity;
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

  public int getRow() {
    return row;
  }

  public void setRow(int row) {
    this.row = row;
  }

  public Object getEntity() {
    return entity;
  }

  public void setEntity(Object entity) {
    this.entity = entity;
  }
}
