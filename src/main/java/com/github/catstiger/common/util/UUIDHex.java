package com.github.catstiger.common.util;

import java.net.InetAddress;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;

public final class UUIDHex {
  private static final int IP;
  
  static {
    int ipadd;
    try {
      ipadd = toInt(InetAddress.getLocalHost().getAddress());
    } catch (Exception e) {
      ipadd = 0;
    }
    IP = ipadd;
  }
  
  private static short counter = (short) 0;
  private static final int JVM = (int) (System.currentTimeMillis() >>> 8);

  private UUIDHex() {
  }

  protected String format(int intval) {
    String formatted = Integer.toHexString(intval);
    StringBuffer buf = new StringBuffer("00000000");
    buf.replace(8 - formatted.length(), 8, formatted);
    return buf.toString();
  }

  protected String format(short shortval) {
    String formatted = Integer.toHexString(shortval);
    StringBuffer buf = new StringBuffer("0000");
    buf.replace(4 - formatted.length(), 4, formatted);
    return buf.toString();
  }

  protected String generate() {
    return new StringBuffer(36).append(format(getIP())).append(format(getJVM())).append(format(getHiTime())).append(format(getLoTime()))
        .append(format(getCount())).toString();
  }

  public static String gen() {
    return new UUIDHex().generate();
  }

  /**
   * 根据auto_inc_code表中得数据，得到一个唯一的数字字符串
   */
  public static String genNum(JdbcTemplate jdbcTemplate) {
    String uniqueNumber = null;

    jdbcTemplate.update("INSERT INTO auto_inc_code (memo) VALUES ('')"); // 自动生成一个唯一数
    List<Long> numbers = jdbcTemplate.queryForList("SELECT id FROM auto_inc_code ORDER BY id DESC", Long.class);

    if (!CollectionUtils.isEmpty(numbers)) {
      Long num = numbers.get(0);
      if (num != null) {
        uniqueNumber = num.toString();
      }
      jdbcTemplate.execute("DELETE FROM auto_inc_code WHERE id < " + uniqueNumber); // 清空唯一数，只保留最后一个
    }
    return uniqueNumber;
  }

  /**
   * Unique across JVMs on this machine (unless they load this class in the same quater second - very unlikely)
   */
  protected int getJVM() {
    return JVM;
  }

  /**
   * Unique in a millisecond for this JVM instance (unless there are > Short.MAX_VALUE instances created in a millisecond)
   */
  protected short getCount() {
    synchronized (UUIDHex.class) {
      if (counter < 0) {
        counter = 0;
      }
      return counter++;
    }
  }

  /**
   * Unique in a local network
   */
  protected int getIP() {
    return IP;
  }

  /**
   * Unique down to millisecond
   */
  protected short getHiTime() {
    return (short) (System.currentTimeMillis() >>> 32);
  }

  protected int getLoTime() {
    return (int) System.currentTimeMillis();
  }

  private static int toInt(byte[] bytes) {
    int result = 0;
    for (int i = 0; i < 4; i++) {
      result = (result << 8) - Byte.MIN_VALUE + (int) bytes[i];
    }
    return result;
  }
}

