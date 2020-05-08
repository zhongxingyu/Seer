 package com.endava.andonescu.demos.springfreemarker.config;
 
 import org.springframework.context.MessageSource;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.ComponentScan;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.support.ReloadableResourceBundleMessageSource;
 import org.springframework.web.servlet.LocaleResolver;
 import org.springframework.web.servlet.ViewResolver;
 import org.springframework.web.servlet.config.annotation.EnableWebMvc;
 import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
 import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
 import org.springframework.web.servlet.i18n.FixedLocaleResolver;
 import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;
 import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
 import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 @Configuration
 @EnableWebMvc
 @ComponentScan("com.endava.andonescu.demos.springfreemarker.controllers")
 public class ConfigMVC extends WebMvcConfigurerAdapter {
 
     @Bean
     public LocaleResolver fixedLocalResolverBean() {
         return new FixedLocaleResolver(new Locale("ro"));
     }
 
     @Bean
     public FreeMarkerConfig freemarkerConfigBean() {
         FreeMarkerConfigurer fConfig = new FreeMarkerConfigurer();
         fConfig.setConfiguration(freemarkerConfiguration());
         return fConfig;
     }
 
     private freemarker.template.Configuration freemarkerConfiguration() {
         freemarker.template.Configuration freeMarkerConfig = new freemarker.template.Configuration(
                 freemarker.template.Configuration.VERSION_2_3_21
         );
 
        List<String> templates = new ArrayList<String>();
         templates.add("lib/implicit.ftl");
 
         freeMarkerConfig.setDateTimeFormat("dd.MM.yyyy");
         freeMarkerConfig.setNumberFormat("#");
         freeMarkerConfig.setWhitespaceStripping(true);
         freeMarkerConfig.setAutoIncludes(templates);
         freeMarkerConfig.setDefaultEncoding("UTF-8");
         return freeMarkerConfig;
     }
 
     @Bean
     public ViewResolver viewResolverBean() {
         FreeMarkerViewResolver viewResolver = new FreeMarkerViewResolver();
         viewResolver.setCache(true);
         viewResolver.setPrefix("");
         viewResolver.setSuffix(".ftl");
         viewResolver.setExposeSpringMacroHelpers(true);
         viewResolver.setContentType("text/html;charset=UTF-8");
         viewResolver.setOrder(1);
         return viewResolver;
     }
 
     @Bean
     public MessageSource messageSourceBean() {
         ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
         messageSource.setBasename("classpath:messages/messages");
         messageSource.setCacheSeconds(5);
         return messageSource;
     }
 
     @Override
     public void addResourceHandlers(ResourceHandlerRegistry registry) {
         registry.addResourceHandler("/resources/**").addResourceLocations(
                 "/resources/");
     }
 
 }
