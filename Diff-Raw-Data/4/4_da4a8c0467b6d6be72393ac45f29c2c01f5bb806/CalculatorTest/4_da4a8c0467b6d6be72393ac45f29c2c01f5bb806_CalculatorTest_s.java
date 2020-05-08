 package com.packtpub;
 
 import junit.framework.Assert;
 import junit.framework.TestCase;
 
 public class CalculatorTest extends TestCase {
 
     public void testSum() throws Exception {
         Calculator calculator = new Calculator();
         int sum = calculator.sum(1, 2);
         Assert.assertEquals(3, sum);
     }
 
    public void testBad() {
        fail("Oops!");
     }
 }
 
