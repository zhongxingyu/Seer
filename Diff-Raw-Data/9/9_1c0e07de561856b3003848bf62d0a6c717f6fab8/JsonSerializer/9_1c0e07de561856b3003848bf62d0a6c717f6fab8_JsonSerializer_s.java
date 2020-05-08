package com.albergopadrone.web;
 
 
 import org.codehaus.jackson.map.ObjectMapper;
 
 public class JsonSerializer {
     private final ObjectMapper objectMapper;
 
     public JsonSerializer() {
         objectMapper = ObjectMapperFactory.objectMapper();
     }
 
     public String serialize(Object o) {
         try {
             return objectMapper.writeValueAsString(o);
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     public <T> T deserialize(String string, Class<T> type) {
         try {
             return objectMapper.readValue(string, type);
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 }
