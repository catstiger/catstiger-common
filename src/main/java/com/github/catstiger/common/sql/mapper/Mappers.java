package com.github.catstiger.common.sql.mapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

/**
 * 用于简化创建BeanPropertyRowMapperEx的过程，这个类创建的非常频繁...
 * @author leesam
 *
 */
public final class Mappers {
  private static Logger logger = LoggerFactory.getLogger(Mappers.class);
  
  @SuppressWarnings("rawtypes")
  private static Map<String, RowMapper> mapperCache = new ConcurrentHashMap<>();
  
  /**
   * 创建一个{@link BeanPropertyRowMapperEx}的实例（泛型的），如果根据<code>mappingClass</code>创建
   * 的实例已经缓存，则不会重复创建。
   * @param mappingClass 给出mappingClass
   * @return
   */
  @SuppressWarnings("unchecked")
  public static <T> RowMapper<T> byClass(Class<T> mappingClass) {
    String className = mappingClass.getName();
    if (mapperCache.containsKey(className)) {
      return mapperCache.get(className);
    }
    RowMapper<T> rowMapper = new BeanPropertyRowMapperEx<T>(mappingClass);
    logger.debug("创建 BeanPropertyRowMapperEx {}", className);
    mapperCache.put(className, rowMapper);
    return rowMapper;
  }
  
  private Mappers() {
  }
}
