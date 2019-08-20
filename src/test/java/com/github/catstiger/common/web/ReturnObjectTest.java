package com.github.catstiger.common.web;

import org.junit.Test;

public class ReturnObjectTest {
  @Test
  public void testToJson() {
    String json = ReturnObject.success().data("HelloWorld").toJson();
    System.out.println(json);
    json = ReturnObject.error().message("Hello world").toJson();
    System.out.println(json);
  }
}
