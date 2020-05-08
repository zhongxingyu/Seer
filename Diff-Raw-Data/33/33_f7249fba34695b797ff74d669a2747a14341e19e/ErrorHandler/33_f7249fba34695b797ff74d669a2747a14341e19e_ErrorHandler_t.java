 package com.mtbaker.client;
 
 public interface ErrorHandler {
 
	public void exception(String namespace, String key, Throwable error);

 	public void numberFormatException(String value, Class<? extends Number> cls);
 }
