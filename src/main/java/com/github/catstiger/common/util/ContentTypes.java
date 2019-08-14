package com.github.catstiger.common.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 根据扩展名，取得ContentType,例如：<br>
 * 
 * <pre>
 * // 下面的代码返回image/jpeg
 * ContentTypes.get("jpg");
 * 
 * </pre>
 * 
 * @author catstiger@gmail.com
 *
 */
public abstract class ContentTypes {
  
  public static final String TYPE_DEFAULT = "application/octet-stream";
  public static final String TYPE_DOC = "application/msword";
  public static final String TYPE_DOCX = "application/msword";
  public static final String TYPE_XLS = "application/vndtype.ms-excel";
  public static final String TYPE_XLSX = "application/vndtype.ms-excel";
  public static final String TYPE_PPT = "type.ppt=application/x-ppt";
  public static final String TYPE_PPTX = "type.ppt=application/x-ppt";
  public static final String TYPE_PNG = "image/png";
  public static final String TYPE_JPEG = "image/jpeg";
  public static final String TYPE_JPG = "image/jpeg";
  public static final String TYPE_GIF = "image/gif";
  public static final String TYPE_XHTML = "text/html";
  public static final String TYPE_HTML = "text/html";
  public static final String TYPE_HTM = "text/html";
  public static final String TYPE_XML = "text/xml";
  public static final String TYPE_CSV = "application/csv";
  
  private static final Map<String, String> CONTENT_TYPES = new ConcurrentHashMap<>();
  
  static {
    CONTENT_TYPES.put("doc", TYPE_DOC);
    CONTENT_TYPES.put("docx", TYPE_DOCX);
    CONTENT_TYPES.put("xls", TYPE_XLS);
    CONTENT_TYPES.put("xlsx", TYPE_XLSX);
    CONTENT_TYPES.put("ppt", TYPE_PPT);
    CONTENT_TYPES.put("pptx", TYPE_PPTX);
    CONTENT_TYPES.put("xhtml", TYPE_XHTML);
    CONTENT_TYPES.put("html", TYPE_HTML);
    CONTENT_TYPES.put("htm", TYPE_HTM);
    CONTENT_TYPES.put("xml", TYPE_XML);
    CONTENT_TYPES.put("csv", TYPE_CSV);
    CONTENT_TYPES.put("jpg", TYPE_JPG);
    CONTENT_TYPES.put("jpeg", TYPE_JPEG);
    CONTENT_TYPES.put("gif", TYPE_GIF);
    CONTENT_TYPES.put("png", TYPE_PNG);
  }
  
  /**
   * 根据扩展名，返回content type, 例如，输入jpg, 返回 image/jpeg
   */
  public static String get(String key) {
    if (key == null || key.length() == 0) {
      return TYPE_DEFAULT;
    }
    
    if (CONTENT_TYPES.containsKey(key.toLowerCase())) {
      return CONTENT_TYPES.get(key.toLowerCase());
    }
    return TYPE_DEFAULT;
  }
}
