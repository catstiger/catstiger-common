package com.github.catstiger.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileChannelUtil {
  private static Logger logger = LoggerFactory.getLogger(FileChannelUtil.class);

  public static final int BUFFER_SIZE = 10240;

  /**
   * 写文件
   * @param dest 要写入的文件
   * @param in 要写入的内容
   * @return
   */
  public int write(File dest, InputStream in) {
    int byteCount = 0;
    FileOutputStream out = null;
    FileChannel channel = null;
    try {
      out = new FileOutputStream(dest);
    } catch (FileNotFoundException e) {
      logger.error("File '{}' is a directory or can not be open.", dest.getAbsolutePath());
      throw Exceptions.unchecked(e);
    }
    try {
      channel = out.getChannel();

      byte[] buffer = new byte[BUFFER_SIZE];
      int bytesRead = -1;
      while ((bytesRead = in.read(buffer)) != -1) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, bytesRead);
        channel.write(byteBuffer);
        byteCount += bytesRead;
      }

    } catch (IOException e) {
      throw Exceptions.unchecked(e);
    } finally {

      try {
        if (channel != null) {
          channel.close();
        }
        if (in != null) {
          in.close();
        }
        if (out != null) {
          out.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return byteCount;
  }

  /**
   * 读取文件到OutputStream
   * @param src 要读取的文件
   * @param dest 读取的内容存放到这里
   * @return
   */
  public long read(File src, OutputStream dest) {
    FileChannel channel = null;
    FileInputStream in = null;
    int bytesRead = -1;
    long bytes = 0L;
    try {
      in = new FileInputStream(src);
      channel = in.getChannel();
      ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE * 1024);
      while ((bytesRead = channel.read(buffer)) != -1) {
        buffer.flip();
        dest.write(buffer.array(), 0, bytesRead);
        dest.flush();
        buffer.clear();
        bytes += bytesRead;
      }
    } catch (Exception e) {
      throw Exceptions.unchecked(e);
    } finally {
      try {
        if (dest != null) {
          dest.flush();
          dest.close();
        }
        if (channel != null) {
          channel.close();
        }
        if (in != null) {
          in.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return bytes;
  }
}
