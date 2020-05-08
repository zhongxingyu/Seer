 package com.hazelcast.annotation;
 
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
 
import org.data4j.type.DistributedObjectTypeEnum;
 
 @Retention(RetentionPolicy.RUNTIME)
 @Target(ElementType.TYPE)
 public @interface EntryListener {
 
	public DistributedObjectTypeEnum type();
	public String distributedObjectName();
 	
 }
