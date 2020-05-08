 package com.jexpect;
 
 import org.junit.Test;
 
 import static com.jexpect.Expect.expect;
 
 public class ToBeFloatTest {
 
   @Test
   public void To_Be_Actual() throws Exception {
     expect(10.0f).toBe(10.0f);
   }
 
   @Test(expected = IllegalArgumentException.class)
   public void To_Be_Actual_Fail() throws Exception {
     expect(10.0f).toBe(9.0f);
   }
 
   @Test
   public void To_Be_Less_Than() throws Exception {
     expect(1.0f).toBeLessThan(9.0f);
   }
 
   @Test(expected = IllegalArgumentException.class)
   public void To_Be_Less_Than_Fail() throws Exception {
     expect(11.0f).toBeLessThan(9.0f);
   }
 
   @Test
   public void To_Be_Greater_Than() throws Exception {
    expect(1.0f).toBeLessThan(9.0f);
   }
 
   @Test(expected = IllegalArgumentException.class)
   public void To_Be_Greater_Than_Fail() throws Exception {
    expect(10.0f).toBeLessThan(9.0f);
   }
 }
