package com.github.catstiger.common.util;

import org.springframework.util.StringUtils;

public abstract class FileHelper {
  public static final String[] PIC_EXTS = new String[] { "jpg", "png", "gif" };

  public static final String[] SWF_EXTS = new String[] { "swf" };

  public static final String[] PDF_EXTS = new String[] { "pdf" };

  public static final String[] OFFICE_EXTS = new String[] { "doc", "xls", "ppt", "docx", "xlsx", "pptx" };

  /**
   * 得到文件扩展名
   */
  public static String getExt(String filename) {
    if (!StringUtils.hasText(filename)) {
      return null;
    }
    return StringUtils.getFilenameExtension(filename);
  }

  /**
   * 判断文件名是否是图片
   * 
   * @param url 给出文件名或者URL
   */
  public static boolean isPic(String url) {
    String ext = StringUtils.getFilenameExtension(url);
    if (!StringUtils.hasText(ext)) {
      return false;
    }
    return contains(PIC_EXTS, ext);
  }

  /**
   * 判断是否是SWF文件
   */
  public static boolean isSwf(String url) {
    String ext = StringUtils.getFilenameExtension(url);
    if (!StringUtils.hasText(ext)) {
      return false;
    }
    return contains(SWF_EXTS, ext);
  }

  /**
   * 判断是否是PDF文件
   */
  public static boolean isPdf(String url) {
    String ext = StringUtils.getFilenameExtension(url);
    if (!StringUtils.hasText(ext)) {
      return false;
    }
    return contains(PDF_EXTS, ext);
  }

  /**
   * 判断是否是Office文件
   */
  public static boolean isOffice(String url) {
    String ext = StringUtils.getFilenameExtension(url);
    if (!StringUtils.hasText(ext)) {
      return false;
    }
    return contains(OFFICE_EXTS, ext);
  }

  /**
   * 删除文件扩展（如：删除 .docx）
   * @return
   */
  public static String delExt(String filePath) {
    return filePath.substring(0, filePath.lastIndexOf("."));
  }

  private static boolean contains(String[] array, String target) {
    if (array == null || array.length == 0) {
      return false;
    }

    if (!StringUtils.hasText(target)) {
      return false;
    }

    for (String a : array) {
      if (target.toLowerCase().equals(a)) {
        return true;
      }
    }
    return false;
  }

}
