package com.github.catstiger.common.poi;

import org.apache.poi.ss.usermodel.Cell;

/**
 * 不做验证，总返回验证通过标记
 * @author Think
 *
 */
public class NothingValidator implements CellValidator {

  @Override
  public CellValid validate(Cell cell, Object value) {
    return CellValid.SUCCESSED;
  }

}
