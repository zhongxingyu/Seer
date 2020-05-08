 package org.mybatis.spring.sample.config;
 
 import org.mybatis.spring.mapper.annotation.EnableMyBatisMapperScanner;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.ImportResource;
 
 /**
  * Test the {@link EnableMyBatisMapperScanner} annotation using
  * {@link EnableMyBatisMapperScanner#basePackages()}.
  * 
  * @author lanyonm
  */
 @Configuration
@ImportResource("classpath:org/mybatis/spring/sample/applicationContext-infrastructure.xml")
 @EnableMyBatisMapperScanner(basePackages = {"org.mybatis.spring.sample.dao"})
 public class AppConfig2 {
 
 }
