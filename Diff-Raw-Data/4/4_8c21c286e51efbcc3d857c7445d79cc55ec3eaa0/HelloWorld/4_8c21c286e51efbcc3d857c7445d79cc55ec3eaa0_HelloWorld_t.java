 /**
  * 
  */
 package com.ft.hack.cobweb;
 
 import org.apache.log4j.Logger;
 
 /**
  * @author anurag.kapur
  *
  */
 public class HelloWorld {
 	
 	private static final Logger LOGGER = Logger.getLogger(HelloWorld.class);
 	
	public String getGreeting() {
		return "Hello World!";
	}
	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		LOGGER.debug("Hello World");
 	}
 
 }
