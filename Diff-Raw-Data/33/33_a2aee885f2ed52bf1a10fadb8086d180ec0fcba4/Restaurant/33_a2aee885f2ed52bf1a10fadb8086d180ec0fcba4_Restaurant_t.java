 package com.ex.domain;
 
import org.hibernate.search.annotations.ClassBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
 import org.springframework.data.neo4j.annotation.NodeEntity;
 import org.springframework.roo.addon.javabean.RooJavaBean;
 import org.springframework.roo.addon.jpa.entity.RooJpaEntity;
 import org.springframework.roo.addon.tostring.RooToString;
 
import com.ex.search.TestBridge;

 @RooJavaBean
 @RooToString
 @RooJpaEntity
@Indexed
 @NodeEntity(partial = true)
@ClassBridge(name="test-bridge", index=Index.TOKENIZED, impl=TestBridge.class)
 public class Restaurant {
 
     private String name;
     
 }
