 package no.bekk.fagdag.mylyn.utils;
 
 import static junit.framework.Assert.*;
 
 import org.junit.Test;
 
 public class TestCalculator {
 
 	@Test
 	public void sholdCalculateCorrectly() {
 		Calculator c = new Calculator();
		assertEquals(5, c.add(2,3));
 		
 	}
 	
 
 }
