 package lv.vitalijs.shakels.microlending.spring.config;
 
 import org.springframework.context.annotation.ComponentScan;
 import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
 
 @Configuration
@EnableWebMvc
@ComponentScan({"lv.vitalijs.shakels.microlending.facade", "lv.vitalijs.shakels.microlending.services"})
 public class ApplicationContextConfig {
 
 }
