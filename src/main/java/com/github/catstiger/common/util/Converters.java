package com.github.catstiger.common.util;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;

public final class Converters {
  /**
   * 将一个字符串转换为Date类型，可以根据各种pattern自动匹配，目前支持的格式包括: <br>
   * <ul>
   * <li>yyyy-MM-dd HH:mm:ss</li>
   * <li>yyyy-MM-dd HH:mm</li>
   * <li>yyyy-MM-dd HH</li>
   * <li>yyyy-MM-dd</li>
   * </ul>
   * 
   * @param datetime 字符串表达的日期、时间
   * @return Date实例，如果不能解析，返回{@code false}
   */
  public static Date parseDate(String datetime) {
    if (StringUtils.isBlank(datetime)) {
      return null;
    }
    DateTimeParser[] parsers = { DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").getParser(),
        DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").getParser(),
        DateTimeFormat.forPattern("yyyy-MM-dd HH").getParser(), DateTimeFormat.forPattern("yyyy-MM-dd").getParser(),
        DateTimeFormat.forPattern("yyyy/MM/dd").getParser() };
    DateTimeFormatter formatter = new DateTimeFormatterBuilder().append(null, parsers).toFormatter();
    DateTime dt = formatter.parseDateTime(datetime);
    return dt == null ? null : dt.toDate();
  }

  /**
   * 将 date类型转化为 固定格式
   * 
   * @param date 日期
   * @return
   */
  public static Date parseDate(Date date) {
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    return parseDate(df.format(date));
  }

  /**
   * 获取当前时间
   */
  public static Date nowTime() {
    Date date = new Date();
    String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    Timestamp now = Timestamp.valueOf(nowTime);
    return now;
  }

  /**
   * 获取当前时间不带连接符的字符串 例如:20180101
   */
  public static String nowTimeString() {
    Date date = new Date();
    String nowTime = new SimpleDateFormat("yyyyMMddHHmmss").format(date);
    return nowTime;
  }

  /**
   * 获取当前时间不带连接符的字符串 例如:2018-01-01
   */
  public static String nowTimeToString() {
    Date date = new Date();
    String nowTime = new SimpleDateFormat("yyyy-MM-dd").format(date);
    return nowTime;
  }

  /**
   * 判断当前时间是am 还是 pm
   * 
   * @return
   */
  public static String amPm() {
    SimpleDateFormat sdf = new SimpleDateFormat("HH");
    int hour = Integer.valueOf(sdf.format(new Date()));
    if (hour < 13) {
      return "am";
    } else {
      return "pm";
    }
  }

  private Converters() {

  }
}
