package com.github.catstiger.common.sql;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.util.Assert;

public class SQLReadyTest {

  @Test
  public void testSelect() {
    SQLReady sqlReady = SQLReady.select(TestEntity.class);
    System.out.println(sqlReady.getSql());
    
    sqlReady = SQLReady.select(TestEntity.class, true);
    System.out.println(sqlReady.getSql());
    
    sqlReady = SQLReady.select(TestEntity.class, "a_");
    System.out.println(sqlReady.getSql());
    
    sqlReady = new SQLRequest(TestEntity.class, true).selectById();
    Assert.isTrue(StringUtils.trim(sqlReady.getSql()).endsWith("id=?"), "Must be end with byID \n" + sqlReady.getSql());
  }

}

@Entity
@Table(name = "t_test")
class TestEntity extends BaseEntity {
  private String title;
  private String content;
  private Date lastModified;
  private TestOut testOut;

  @Column
  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public Date getLastModified() {
    return lastModified;
  }

  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }

  @ManyToOne
  @JoinColumn
  public TestOut getTestOut() {
    return testOut;
  }

  public void setTestOut(TestOut testOut) {
    this.testOut = testOut;
  }
}

@Entity
@Table(name = "t_test_out")
class TestOut extends BaseEntity {
  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
