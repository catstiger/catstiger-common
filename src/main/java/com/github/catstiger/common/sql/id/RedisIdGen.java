package com.github.catstiger.common.sql.id;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;

import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.catstiger.common.AppProps;

@Service
public class RedisIdGen implements IdGen {
  private static Logger logger = LoggerFactory.getLogger(RedisIdGen.class);

  public static final int DEFAULT_STEP = 3000;
  public static final String DEFAULT_KEY = AppProps.APP_PREFIX + "_catstiger_high_";

  private static Lock lock = new ReentrantLock(false);
  private static AtomicLong max;
  private static AtomicLong id;
  private Integer step = DEFAULT_STEP;
  private String key = DEFAULT_KEY;
  @Autowired
  private RedissonClient redis;

  public RedisIdGen() {

  }

  public RedisIdGen(RedissonClient redis) {
    this.redis = redis;
  }

  /**
   * 初始化，从Redis中得到High value, 并申请下一个high value
   */
  @PostConstruct
  public synchronized void init() {
    RAtomicLong ratomicLong = redis.getAtomicLong(key);
    if (!ratomicLong.isExists()) {
      ratomicLong.set(10000000L);
    }
    id = new AtomicLong(ratomicLong.getAndAdd(step));
    max = new AtomicLong(ratomicLong.get());
  }

  @Override
  public Long nextId() {
    try {
      lock.lock();
      if (id.longValue() >= max.longValue()) {
        RAtomicLong ratomicLong = redis.getAtomicLong(key);
        id.set(ratomicLong.getAndAdd(step) + 1L); // 得到新的下限，并且设置新的上线，+1是为了确保唯一性
        max.set(ratomicLong.get());
        logger.debug("增加高位值 {} {}", id.longValue(), max.longValue());
      }
    } finally {
      lock.unlock();
    }
    return id.incrementAndGet();
  }

  public void setStep(Integer step) {
    this.step = step;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }
}
