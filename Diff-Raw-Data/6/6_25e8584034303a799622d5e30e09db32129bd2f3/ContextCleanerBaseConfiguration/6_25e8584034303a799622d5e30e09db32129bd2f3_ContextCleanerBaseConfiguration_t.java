 package com.stresstest.cleaners.configuration;
 
import javax.inject.Singleton;

 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.ImportAware;
 import org.springframework.core.annotation.AnnotationAttributes;
 import org.springframework.core.type.AnnotationMetadata;
 
 import com.stresstest.cleaners.aop.CleanerSpringAdvisor;
 import com.stresstest.cleaners.context.CleanerContext;
 import com.stresstest.spring.listener.TestContextListenerRegistrator;
 
 @Configuration
 public class ContextCleanerBaseConfiguration implements ImportAware {
 
     protected AnnotationAttributes enableContextCleaner;
 
     private CleanerContext cleanerContext = new CleanerContext();
     private ContextCleanerTestExecutionListener cleanerTestExecutionListener = new ContextCleanerTestExecutionListener(cleanerContext);
     private TestContextListenerRegistrator contextListenerRegistrator = new TestContextListenerRegistrator(cleanerTestExecutionListener);

     @Bean
     @Singleton
     public CleanerSpringAdvisor cleanerSpringAdvisor() {
         if (enableContextCleaner != null) {
             return new CleanerSpringAdvisor(enableContextCleaner.getStringArray("packages"));
         } else {
             return new CleanerSpringAdvisor(new String[0]);
         }
     }

     @Bean
     @Singleton
     public CleanerContext cleanerContext() {
         return cleanerContext;
     }
 
     @Bean
     @Singleton
     public TestContextListenerRegistrator contextListenerRegistrator() {
         return contextListenerRegistrator;
     }
 
     @Bean
     @Singleton
     public ContextCleanerTestExecutionListener cleanerTestExecutionListener() {
         return cleanerTestExecutionListener;
     }
 
     @Override
     public void setImportMetadata(AnnotationMetadata importMetadata) {
         this.enableContextCleaner = AnnotationAttributes.fromMap(importMetadata.getAnnotationAttributes(EnableContextCleaner.class.getName(), false));
     }
 }
