package com.github.catstiger;

import java.io.IOException;
import java.io.InputStream;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.spring.starter.RedissonProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

import com.github.catstiger.common.AppProps;

@SpringBootApplication(scanBasePackages = "com.github.catstiger")
@EnableConfigurationProperties({AppProps.class, DataSourceProp.class})
public class TestApplication {
  @Autowired
  private RedissonProperties redissonProperties; // 这个定义在redisson-spring-boot-starter

  @Autowired
  private ApplicationContext ctx;

  @Bean(destroyMethod = "shutdown")
  public RedissonClient redisson() throws IOException {
    InputStream is = getConfigStream();
    Config config = Config.fromYAML(is);
    
    return Redisson.create(config);
  }
  
  private InputStream getConfigStream() throws IOException {
    Resource resource = ctx.getResource(redissonProperties.getConfig());
    InputStream is = resource.getInputStream();
    return is;
  }
  
  public static void main(String[] args) {
    SpringApplication.run(TestApplication.class, args);
  }
}
