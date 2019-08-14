package com.github.catstiger.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Java调用外部命令的工具类
 * 
 * @author catstiger@gmail.com
 *
 */
public final class ExecUtil {
  private static Logger logger = LoggerFactory.getLogger(ExecUtil.class);

  /**
   * 执行外部程序
   * 
   * @param workDir 外部命令的的工作目录，如果为null,则是Java当前工作目录
   * @param cmd 外部命令名称以及传递给外部命令的参数
   * @return
   */
  public Integer execute(File workDir, String... cmd) {
    Reader reader = null;

    ProcessBuilder procBuilder = new ProcessBuilder();
    if (workDir != null && workDir.exists() && workDir.isDirectory()) {
      procBuilder.directory(workDir); // 设置执行目录
    }
    procBuilder.redirectErrorStream(true); // 合并输出子进程的standard和error inputstream
    procBuilder.command(cmd);

    Process process;
    Integer exit = null;
    try {
      process = procBuilder.start();
      reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      readString(reader); // 清空输出信息
      exit = process.waitFor();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return exit;
  }

  /**
   * 执行外部命令，并返回第一行输出
   * 
   * @param workDir Sets this process working directory
   * @param cmd 执行的命令及其参数
   * @return
   */
  public String exeAndReadLine(File workDir, String... cmd) {
    BufferedReader reader = null;

    ProcessBuilder procBuilder = new ProcessBuilder();
    if (workDir != null && workDir.exists() && workDir.isDirectory()) {
      procBuilder.directory(workDir); // 设置执行目录
    }
    procBuilder.redirectErrorStream(true); // 合并输出子进程的standard和error inputstream
    procBuilder.command(cmd);

    Process process;
    try {
      process = procBuilder.start();
      reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line = reader.readLine();
      int exit = process.waitFor();
      if (exit == 0 || exit == 1) {
        return line;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * 执行外部程序,并返回所有外部命令的输出
   * 
   * @param workDir 外部命令的的工作目录，如果为null,则是Java当前工作目录
   * @param cmds 外部命令名称以及传递给外部命令的参数
   * @return
   */
  public String exeAndReadAllOutput(File workDir, List<String> cmds) {
    BufferedReader reader = null;
    StringBuffer buf = new StringBuffer();
    ProcessBuilder procBuilder = new ProcessBuilder();
    if (workDir != null && workDir.exists() && workDir.isDirectory()) {
      procBuilder.directory(workDir); // 设置执行目录
    }
    procBuilder.redirectErrorStream(true); // 合并输出子进程的standard和error inputstream
    procBuilder.command(cmds);

    Process process;
    try {
      process = procBuilder.start();
      reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line = null;
      while ((line = reader.readLine()) != null) {
        buf.append(line + "\n");
      }
      int exit = process.waitFor();
      if (exit == 0 || exit == 1) {
        return buf.toString();
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        reader.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return null;
  }

  private void readString(final Reader reader) throws Exception {
    new Thread(new Runnable() {
      public void run() {
        try {
          char[] buf = new char[300];
          while (reader.read(buf) >= 0) {
            if (logger.isDebugEnabled()) {
              System.out.println(buf);
            }
          }
        } catch (IOException e) {
          //Nothing
        }
      }
    }).start();
  }
}
