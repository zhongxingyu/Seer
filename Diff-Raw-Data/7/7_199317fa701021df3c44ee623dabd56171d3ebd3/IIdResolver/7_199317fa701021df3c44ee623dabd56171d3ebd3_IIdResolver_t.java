 package com.swtxml.swt.properties;
 
 public interface IIdResolver {
 
 	public <T> T getById(String id, Class<T> clazz);
 
 }
