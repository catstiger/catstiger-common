package com.github.catstiger.common.web.converter;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToLongConverter implements Converter<String, Long> {

  @Override
  public Long convert(String source) {
    return (NumberUtils.isCreatable(source)) ? NumberUtils.createNumber(source).longValue() : null;
  }

}
