package com.github.catstiger.common;

import java.sql.SQLException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.catstiger.DataSourceProp;
import com.github.catstiger.TestApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class)
//@ActiveProfiles("")
public class PropertiesTest {
  @Autowired
  private AppProps prop;
  
  @Autowired
  private DataSourceProp dsProp;
  
  @Autowired
  private JdbcTemplate jdbcTemplate;
  
  @Test
  public void test() throws SQLException {
    System.out.println(prop.cdnImage());
    System.out.println(dsProp.getDriverClassName());
    System.out.println(jdbcTemplate.getDataSource().getConnection().getMetaData().getDriverName());
  }
}
