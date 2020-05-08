 package com.sb;
 
 import static org.junit.Assert.*;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class TestSomething {
 	Something abstractSomething = null;
 	
 	@Before
 	public void setUp() throws Exception {
 		abstractSomething = new Something();
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void testDoSomething() {
		assertTrue(abstractSomething.doSomething() == 4);
 	}
 
 }
