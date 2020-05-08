 /**
  * 
  */
 package com.chinarewards.qqgpvn.main.test;
 
 import org.junit.Test;
 
 /**
  * Simple test to make sure the Java logging does work.
  * 
  * @author Cyril
  * @since 0.1.0
  */
 public class JavaLoggingTest extends BaseTest {
 
 	/**
 	 * After running this, see your Eclipse console and make sure the
 	 * corresponding output does appear.
 	 */
 	@Test
 	public void testAllLogLevels() {
 
 		System.out.println("System.out.println print this");
 
 		log.trace("Hello world!");
 		log.debug("Hello world!");
 		log.info("Hello world!");
 		log.warn("Hello world!");
 		log.error("Hello world!");
 
 	}
 
 }
