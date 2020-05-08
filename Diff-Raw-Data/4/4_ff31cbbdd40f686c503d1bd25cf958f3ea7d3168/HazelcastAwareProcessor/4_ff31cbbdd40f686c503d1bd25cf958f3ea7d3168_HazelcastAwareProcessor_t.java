 package com.hazelcast.annotation.processor;
 
 import java.lang.annotation.Annotation;
 import java.util.List;
 
 import com.hazelcast.annotation.builder.HazelcastAnnotationProcessor;
 import com.hazelcast.annotation.builder.HazelcastFieldAnnotationProcessor;
 import com.hazelcast.common.AnnotatedField;
 import com.hazelcast.common.Annotations;
 import com.hazelcast.srv.IHazelcastService;
 
 /**
  * Date: 23/03/2013 16:40
  * Author Yusuf Soysal
  */
 public class HazelcastAwareProcessor implements HazelcastAnnotationProcessor {
 
     @Override
     public boolean canBeProcessedMoreThanOnce() {
         return true;
     }
 
     @Override
     public void process(IHazelcastService hazelcastService, Object obj, Annotation annotation) {
         List<AnnotatedField> supportedAnnotationsList = Annotations.SupportedFieldAnnotation.getSupportedAnnotations(obj.getClass());
 
         for (AnnotatedField supported : supportedAnnotationsList) {
             HazelcastFieldAnnotationProcessor processor = supported.getSupportedAnnotatiton().getProcessor();
             processor.process(hazelcastService, obj, supported.getField(), supported.getAnnotation());
         }
     }
 
     @Override
    public void process(IHazelcastService hazelcastService, Class<?> clazz, Annotation annotation) {
    	
     }
 }
