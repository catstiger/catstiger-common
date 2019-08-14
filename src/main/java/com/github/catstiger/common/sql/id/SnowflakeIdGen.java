package com.github.catstiger.common.sql.id;

/**
 * 用单机版Snow flake实现IdGen，仅支持一个datacenter和一个worker
 * 
 * @author catstiger
 *
 */
@Deprecated
public class SnowflakeIdGen implements IdGen {

  private long workerIdBits = 5L;
  // 31
  protected long maxWorkerId = -1L ^ (-1L << workerIdBits);
  protected long datacenterId = 0L;
  protected SnowflakeIDWorker worker = null;

  @Override
  @Deprecated
  public Long nextId() {
    if (worker == null) {
      long workerId = getWorkderId(datacenterId);
      worker = new SnowflakeIDWorker(workerId, datacenterId);
    }
    return worker.nextId();
  }

  protected long getWorkderId(long dataCenterId) {
    return 1L;
  }

  protected void setDatacenterId(long datacenterId) {
    this.datacenterId = datacenterId;
  }

}
