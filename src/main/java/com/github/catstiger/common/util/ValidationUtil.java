package com.github.catstiger.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * 验证工具类
 * 
 * @author Sam
 *
 */
public final class ValidationUtil {

  /**
   * 验证Email地址是否合法
   * 
   * @param emailAddr  给定的Email地址.
   * @return 如果合法，返回<code>true</code>,否则，返回<code>false</code>
   */
  public static boolean isValidEmail(String emailAddr) {
    String emailAddressPattern = "\\b(^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@([A-Za-z0-9-])"
        + "+(\\.[A-Za-z0-9-]+)*((\\.[A-Za-z0-9]{2,})|(\\.[A-Za-z0-9]{2,}\\.[A-Za-z0-9]{2,}))$)\\b";
    return validateRegex(emailAddr, emailAddressPattern, true);
  }
  
  /**
   * 域名合法验证
   */
  public static boolean isValidDomain(String domain) {
    String pattern = "^((http://)|(https://))?([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,6}";
    return validateRegex(domain, pattern, true);
  }
  
  public static void main(String[]args) {
    System.out.println("http://www.sina.com".replaceAll("^((http://)|(https://))", ""));
    System.out.println("https://www.sina.com".replaceAll("^((http://)|(https://))", ""));
    System.out.println("www.sina.com".replaceAll("^((http://)|(https://))", ""));
    
  }
  
  /**
   * 正则表达式验证。
   * 
   * @param value 被验证字符串， 如果为空字符串或{@code null},则认为不匹配。
   * @param expression 正则表达式， 如果为空字符串或{@code null},则认为不匹配。
   * @param isCaseSensitive 是否忽略大小写
   * @return 如果匹配，返回{@code true},否则返回{@code false}
   */
  public static boolean validateRegex(String value, String expression, boolean isCaseSensitive) {
    if (StringUtils.isBlank(value) || StringUtils.isBlank(expression)) {
      return false;
    }

    String compare = ((String) value).trim();
    if (compare.length() == 0) {
      return false;
    }

    Pattern pattern;
    if (isCaseSensitive) {
      pattern = Pattern.compile(expression);
    } else {
      pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
    }

    Matcher matcher = pattern.matcher(compare);
    return matcher.matches();
  }

  /**
   * 验证IP是否合法
   * 
   */
  public static boolean isValidIp(String ip) {
    Pattern patt = Pattern.compile("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");
    Matcher matcher = patt.matcher(ip);
    return matcher.matches();
  }
  
  /**
   * 验证是否是正确的手机号码
   * @param mobile 给出手机号码
   * @return
   */
  public static boolean isValidMobile(String mobile) {
    return mobile.matches("^1[3456789]\\d{9}$");
  }
  
  public enum TelecomProviders {
    Mobile {
      @Override
      public boolean matches(String mobile) {
        return mobile.matches("^134[0-8]\\d{7}$|^(?:13[5-9]|147|15[0-27-9]|178|18[2-478])\\d{8}$");
      }
    }, 
    
    ChinaUnicom {
      @Override
      public boolean matches(String mobile) {
        return mobile.matches("^(?:13[0-2]|145|15[56]|176|175|166|18[56])\\d{8}$"); //添加166
      }
    }, 
    
    Telecom {
      @Override
      public boolean matches(String mobile) {
        return mobile.matches("^(?:199|173|133|153|177|18[019])\\d{8}$"); //添加173 199
      }
    };
    
    public abstract boolean matches(String mobile);
  }

  /**
   * 防止初始化
   */
  private ValidationUtil() {
  }
}
