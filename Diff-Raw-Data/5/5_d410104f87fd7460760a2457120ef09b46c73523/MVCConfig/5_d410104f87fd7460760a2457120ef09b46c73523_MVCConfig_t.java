 package edu.upc.dsbw.spring.web;
 
 import java.util.Properties;
 
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.web.multipart.MultipartResolver;
 import org.springframework.web.servlet.HandlerExceptionResolver;
 import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
 import org.springframework.web.servlet.view.InternalResourceViewResolver;
 import org.springframework.web.servlet.view.JstlView;
 
 @Configuration
 public class MVCConfig {
 	
 	@Bean
 	public InternalResourceViewResolver viewResolver(){
 		InternalResourceViewResolver result = new InternalResourceViewResolver();
 		result.setPrefix("/WEB-INF/jsp/");
 		result.setSuffix(".jsp");
 		result.setViewClass(JstlView.class);
 		return result;
 	}
 	
 	@Bean
 	public MultipartResolver multipartResolver() {
 		return new org.springframework.web.multipart.commons.CommonsMultipartResolver();
 	}
  /*
   @Bean
   public HandlerExceptionResolver exceptionResolver() {
     SimpleMappingExceptionResolver excpResolver = new SimpleMappingExceptionResolver();
     
     excpResolver.setDefaultErrorView("general-error");
     
     return excpResolver;
   }
	*/
 }
