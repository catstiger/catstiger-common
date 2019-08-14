package com.github.catstiger.common.poi;

public interface CellConverter {
  Object convert(org.apache.poi.ss.usermodel.Cell cell);
}
