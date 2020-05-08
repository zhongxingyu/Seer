 package com.inertia.solutions.spring.rest.web.config;
 
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.ComponentScan;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.web.servlet.config.annotation.EnableWebMvc;
 import org.springframework.web.servlet.view.InternalResourceViewResolver;
 import org.springframework.web.servlet.view.JstlView;
 
 @Configuration
 @EnableWebMvc
@ComponentScan(basePackages = {"com.inertia.solutions.spring.mvc.web.controller"})
 public class WebConfiguration {
 	
 	@Bean
 	public InternalResourceViewResolver jspViewResolver() {
 		InternalResourceViewResolver returnVal = new InternalResourceViewResolver();
 		returnVal.setViewClass(JstlView.class);
 		returnVal.setPrefix("/WEB-INF/jsp/");
 		returnVal.setSuffix(".jsp");
 		return returnVal;
 	}
 
 }
