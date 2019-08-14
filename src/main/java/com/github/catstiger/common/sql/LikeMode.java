package com.github.catstiger.common.sql;

/**
 * 用于实现 “like”查询匹配策略 
 */
public enum LikeMode {
  /**
   * Match the entire string to the pattern
   */
  EXACT {
    public String matching(String pattern) {
      return pattern;
    }
  },

  /**
   * Match the start of the string to the pattern
   */
  START {
    public String matching(String pattern) {
      return pattern + '%';
    }
  },

  /**
   * Match the end of the string to the pattern
   */
  END {
    public String matching(String pattern) {
      return '%' + pattern;
    }
  },

  /**
   * Match the pattern anywhere in the string
   */
  FULL {
    public String matching(String pattern) {
      return '%' + pattern + '%';
    }
  };

  /**
   * convert the pattern, by appending/prepending "%"
   */
  public abstract String matching(String pattern);
}
