package com.github.catstiger.common.sql;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.JoinColumn;
import javax.persistence.Transient;

import org.apache.commons.collections4.CollectionUtils;

import com.github.catstiger.common.util.GenericsUtil;
import com.github.catstiger.common.util.ReflectUtil;

/**
 * 树形数据结构，匹配Ext Tree等...
 * @author samlee
 *
 */
@SuppressWarnings("serial")
public class TreeEntity<T extends TreeEntity<T>> extends BaseEntity {
  protected T parent;
  protected List<T> children = new ArrayList<>(0);
  
  @JoinColumn(name = "parent_id")
  public T getParent() {
    return parent;
  }
  
  public void setParent(T parent) {
    this.parent = parent;
  }
  
  @Transient
  public List<T> getChildren() {
    return children;
  }
  
  public void setChildren(List<T> children) {
    this.children = children;
  }
  
  @Transient
  public Long getParentId() {
    if (parent != null) {
      return parent.getId();
    }
    return null;
  }
  
  @SuppressWarnings("unchecked")
  public void setParentId(Long id) {
    if (parent == null) {
      Class<?> clz = GenericsUtil.getGenericClass(getClass());
      parent = (T) ReflectUtil.instantiate(clz);
    }
    parent.setId(id);
  }
  
  @Transient
  public Boolean getLeaf() {
    return CollectionUtils.isEmpty(children);
  }
  
  @Transient
  public String getIconCls() {
    return getLeaf() ? "iconfont icon-items" : "iconfont icon-wenjianjia";
  }
  
  @Transient
  public Boolean getExpanded() {
    return !getLeaf();
  }
}
