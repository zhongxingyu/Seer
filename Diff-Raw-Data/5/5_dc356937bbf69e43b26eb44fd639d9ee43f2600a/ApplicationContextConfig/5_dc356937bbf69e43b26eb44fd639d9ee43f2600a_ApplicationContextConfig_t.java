 package lv.vitalijs.shakels.microlending.spring.config;
 
 import org.springframework.context.annotation.ComponentScan;
 import org.springframework.context.annotation.Configuration;
 
 @Configuration
@ComponentScan({"lv.vitalijs.shakels.microlending.facade", "lv.vitalijs.shakels.microlending.services, lv.vitalijs.shakels.microlending.repositories"})
 public class ApplicationContextConfig {
 
 }
