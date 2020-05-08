 package edu.ncsu.csc575.utils.test;
 
 import org.apache.log4j.Logger;
 import org.junit.Test;
 
 public class LoggerTest {
 
 	@Test
 	public void test() {
		Logger logger = Logger.getLogger(getClass());
		//test commit
		logger.info("Trooper");
 	}
 
 }
