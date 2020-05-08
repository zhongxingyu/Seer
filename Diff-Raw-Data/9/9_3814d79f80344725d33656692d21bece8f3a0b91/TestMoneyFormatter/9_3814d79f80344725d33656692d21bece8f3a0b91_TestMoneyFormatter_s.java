 package com.acmetelecom;
 
 import java.math.BigDecimal;
 
 import junit.framework.TestCase;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class TestMoneyFormatter extends TestCase {
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void testPenceToPounds() {
		assertEquals("0.10", MoneyFormatter.penceToPounds(new BigDecimal(10.1)));
 	}
 
 }
