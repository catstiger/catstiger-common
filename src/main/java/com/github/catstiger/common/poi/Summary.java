package com.github.catstiger.common.poi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Summary implements Serializable {
  private static final long serialVersionUID = 3775961455560709610L;

  private boolean success = true;
  private int total;
  private int errors;
  private List<RowValid> rowValids = new ArrayList<>(100);

  /**
   * 总数量
   */
  public int getTotal() {
    return total;
  }

  public void setTotal(int total) {
    this.total = total;
  }

  /**
   * 错误的数量
   * @return
   */
  public int getErrors() {
    return errors;
  }

  public void setErrors(int errors) {
    this.errors = errors;
  }

  /**
   * 返回所有错误的导入
   */
  public List<RowValid> getRowValids() {
    return rowValids;
  }

  public void setRowValids(List<RowValid> rowValids) {
    this.rowValids = rowValids;
  }
  
  /**
   * 添加一个错误的导入结果
   * @param rowValid
   */
  public void addInvalid(RowValid rowValid) {
    if (!rowValid.isValid()) {
      this.rowValids.add(rowValid);
    }
  }

  public boolean getSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }
}
