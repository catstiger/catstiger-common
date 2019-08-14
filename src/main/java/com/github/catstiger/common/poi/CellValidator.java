package com.github.catstiger.common.poi;

public interface CellValidator {
  public CellValid validate(org.apache.poi.ss.usermodel.Cell cell, Object cellValue);
}
