package com.github.catstiger.common.sql.sync.executor;

import java.io.Writer;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.catstiger.common.sql.sync.DDLExecutor;
import com.github.catstiger.common.util.Exceptions;

public class WriterDDLExecutor implements DDLExecutor {
  private static Logger logger = LoggerFactory.getLogger(WriterDDLExecutor.class);
  private Writer writer;

  public WriterDDLExecutor(Writer writer) {
    this.writer = writer;
  }

  @Override
  public synchronized void execute(String sql) {
    if (StringUtils.isBlank(sql)) {
      throw new IllegalArgumentException("SQL 不可为空。");
    }
    try {
      sql = StringUtils.trim(sql);
      writer.write(sql);
      if (!sql.endsWith(";")) {
        writer.write(";");
      }
      writer.write("\n\n");
      writer.flush();
      logger.info("Write [{}]", sql);
    } catch (Exception e) {
      e.printStackTrace();
      throw Exceptions.unchecked(e);
    }
  }

}
