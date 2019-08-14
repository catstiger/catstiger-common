package com.github.catstiger.common.util;

import org.apache.commons.lang3.StringUtils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public final class CNHelper {
  /**
   * 将汉字转换为拼音全拼
   * 
   * @param cn 汉字字符串
   */
  public static String pinyin(String cn) {
    if (StringUtils.isBlank(cn)) {
      return cn;
    }
    char[] soruce = null;
    soruce = cn.toCharArray();

    HanyuPinyinOutputFormat pinyinOutputFormat = new HanyuPinyinOutputFormat();
    pinyinOutputFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
    pinyinOutputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    pinyinOutputFormat.setVCharType(HanyuPinyinVCharType.WITH_V);
    String dest = "";
    int length = soruce.length;
    String[] buf = new String[soruce.length];
    try {
      for (int i = 0; i < length; i++) {
        // 判断是否为汉字字符
        if (java.lang.Character.toString(soruce[i]).matches("[\\u4E00-\\u9FA5]+")) {
          buf = PinyinHelper.toHanyuPinyinStringArray(soruce[i], pinyinOutputFormat);
          dest += buf[0];
        } else {
          dest += java.lang.Character.toString(soruce[i]);
        }
      }
      // System.out.println(t4);
      return dest;
    } catch (BadHanyuPinyinOutputFormatCombination e) {
      e.printStackTrace();
    }
    return dest;
  }

  /**
   * 得到汉字拼音字头
   * 
   * @param cn 汉字字符串
   */
  public static String pinyinHeadChar(String cn) {
    if (StringUtils.isBlank(cn)) {
      return cn;
    }
    String temp = "";
    String result = "";
    String convert = "";
    for (int j = 0; j < cn.length(); j++) {
      char word = cn.charAt(j);
      String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(word);
      if (pinyinArray != null && pinyinArray.length > 0) {
        convert += pinyinArray[0].charAt(0);
      } else {
        convert += word;
      }
    }
    for (int i = 0; i < convert.length(); i++) { // convert目前为小写首字母,下面是将小写首字母转化为大写
      if (convert.charAt(i) >= 'a' && convert.charAt(i) <= 'z') {
        temp = convert.substring(i, i + 1).toUpperCase();
        result += temp;
      }
    }
    return result;
  }

  private CNHelper() {

  }
}
