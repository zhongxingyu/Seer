 package com.github.funreco.service;
 
 import java.net.UnknownHostException;
 
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import com.github.funreco.domain.Action;
 import com.google.code.morphia.Morphia;
 import com.mongodb.Mongo;
 import com.mongodb.MongoException;
 
 @Configuration
 public class SpringConfig {
     @Bean
     public Datastore datastore() throws MongoException, UnknownHostException {
         Morphia morphia = new Morphia();
 
         morphia.map(Action.class);
 
         return morphia.createDatastore(new Mongo(), "funreco");
     }
 }
