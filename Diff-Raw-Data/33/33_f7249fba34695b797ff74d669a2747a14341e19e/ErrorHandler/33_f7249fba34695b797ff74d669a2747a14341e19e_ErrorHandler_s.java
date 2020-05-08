 package com.mtbaker.client;
 
 public interface ErrorHandler {
 
 	public void numberFormatException(String value, Class<? extends Number> cls);
 }
