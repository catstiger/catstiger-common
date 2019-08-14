package com.github.catstiger.common.poi;

public class NothingConverter implements CellConverter {

  @Override
  public Object convert(org.apache.poi.ss.usermodel.Cell cell) {
    return null;
  }

}
