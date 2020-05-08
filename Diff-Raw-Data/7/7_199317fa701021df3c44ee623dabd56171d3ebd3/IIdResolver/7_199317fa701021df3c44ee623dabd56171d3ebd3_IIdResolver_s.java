 package com.swtxml.swt.properties;
 
 public interface IIdResolver {
 
	@Deprecated
 	public <T> T getById(String id, Class<T> clazz);
 
 }
