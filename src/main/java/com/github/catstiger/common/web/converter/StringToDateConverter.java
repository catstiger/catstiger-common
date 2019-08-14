package com.github.catstiger.common.web.converter;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * 支持各种格式的字符串转换为Date类型，Spring MVC会自动将这个Convert装入ConversionService.
 * <p>
 * StringToDateConverter给出了单例的实现模式，可以通过调用{@link #getInstance()}，返回一个StringToDateConverter的实例， 通常用于在非Spring环境中使用
 * </p>
 * 
 * @author samlee
 *
 */
@Component
public class StringToDateConverter implements Converter<String, Date> {
  private static DateTimeFormatter formatter;
  private static StringToDateConverter instance = new StringToDateConverter();

  public static StringToDateConverter getInstance() {
    return instance;
  }

  static {
    DateTimeParser[] parsers = { DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").getParser(),
        DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").getParser(), DateTimeFormat.forPattern("yyyy-MM-dd HH").getParser(),
        DateTimeFormat.forPattern("yyyy-MM-dd").getParser(), DateTimeFormat.forPattern("yyyyMMdd").getParser(),
        DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss").getParser(), DateTimeFormat.forPattern("yyyy/MM/dd  h:m:s").getParser() };
    formatter = new DateTimeFormatterBuilder().append(null, parsers).toFormatter();
  }

  @Override
  public Date convert(String source) {
    if (StringUtils.isBlank(source)) {
      return null;
    }
    if (source.indexOf("T") > 0) {
      source = StringUtils.replace(source, "T", StringUtils.SPACE);
    }
    source = StringUtils.trim(source);
    if (NumberUtils.isCreatable(source) && source.length() > 8) {
      return new Date(NumberUtils.createNumber(source).longValue());
    }

    DateTime dateTime = formatter.parseDateTime(source);
    return dateTime.toDate();
  }

}
