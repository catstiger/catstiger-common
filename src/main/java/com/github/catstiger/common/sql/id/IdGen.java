package com.github.catstiger.common.sql.id;

/**
 * {@code IdGen}的实现类，用于实现生成全局唯一ID。当数据库需要做多阶段负载平衡、读写分离、分库分表的情况下。
 * 自增字段往往不能满足要求，因此需要某种算法生成全局唯一的ID，{@code IdGen}的子类用于实现这种算法。{@code IdGen}
 * 接口为这些算法提供统一的调用方式。
 * @author catstiger
 *
 */
public interface IdGen {
  /**
   * 生成一个全局唯一的ID
   * @return Id generated.
   */
  public Long nextId();
  
}
