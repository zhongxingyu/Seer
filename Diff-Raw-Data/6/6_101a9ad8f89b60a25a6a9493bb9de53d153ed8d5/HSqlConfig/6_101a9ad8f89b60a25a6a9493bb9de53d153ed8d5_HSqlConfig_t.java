 package com.freeroom.projectci.beans;
 
 import com.freeroom.di.annotations.Bean;
 import com.freeroom.persistence.DBConfig;
 
 import java.util.Properties;
 
 @Bean
 public class HSqlConfig implements DBConfig {
     @Override
     public Properties getDbProperties()
     {
         final Properties properties = new Properties();
        properties.setProperty("url", "jdbc:mysql://host.mysql.iinet:3306/iinet_project_status");
        properties.setProperty("username", "root");
        properties.setProperty("password", "123456");
         properties.setProperty("blockSize", "10");
         return properties;
     }
 }
