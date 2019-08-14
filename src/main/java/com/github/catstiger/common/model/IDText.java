package com.github.catstiger.common.model;

import java.io.Serializable;

public class IDText implements Serializable {
  private static final long serialVersionUID = -2433644241304015949L;
  
  private String id;
  private Object text;
  
  public IDText() {
    
  }
  
  public IDText(String id, Object text) {
    this.id = id;
    this.text = text;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Object getText() {
    return text;
  }

  public void setText(Object text) {
    this.text = text;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((text == null) ? 0 : text.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    IDText other = (IDText) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (text == null) {
      if (other.text != null)
        return false;
    } else if (!text.equals(other.text))
      return false;
    return true;
  }

}
