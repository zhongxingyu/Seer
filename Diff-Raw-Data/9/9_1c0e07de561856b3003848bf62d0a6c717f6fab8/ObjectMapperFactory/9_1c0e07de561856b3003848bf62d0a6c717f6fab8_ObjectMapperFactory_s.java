package com.albergopadrone.web;
 
 import org.codehaus.jackson.map.DeserializationConfig;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
 
 public class ObjectMapperFactory {
     private static ObjectMapper objectMapper;
 
     public static ObjectMapper objectMapper() {
         if (objectMapper == null) {
             objectMapper = new ObjectMapper();
             objectMapper.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
             objectMapper.enable(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
             objectMapper.setAnnotationIntrospector(new JaxbAnnotationIntrospector());
         }
         return objectMapper;
     }
 }
