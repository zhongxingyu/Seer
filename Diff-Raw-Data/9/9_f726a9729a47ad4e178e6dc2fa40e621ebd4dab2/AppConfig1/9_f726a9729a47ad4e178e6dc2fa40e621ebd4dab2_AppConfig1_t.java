 package org.mybatis.spring.sample.config;
 
 import org.mybatis.spring.mapper.annotation.EnableMyBatisMapperScanner;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.ImportResource;
 
 /**
  * Test the {@link EnableMyBatisMapperScanner} annotation using
  * {@link EnableMyBatisMapperScanner#value()}.
  * 
  * @author lanyonm
  */
 @Configuration
@ImportResource("classpath:org/mybatis/spring/sample/config/applicationContext-infrastructure.xml")
 @EnableMyBatisMapperScanner("org.mybatis.spring.sample.dao")
 public class AppConfig1 {
 
 }
