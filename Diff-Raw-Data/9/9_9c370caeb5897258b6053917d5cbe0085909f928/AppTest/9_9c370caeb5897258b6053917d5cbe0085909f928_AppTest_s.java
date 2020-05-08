 package org.zezutom;
 
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  * Unit test for simple App.
  */
 public class AppTest 
 {
     @Test
     public void sayHello()
     {
	assertEquals("Hello Test!", new App().sayHello("Test"));
     }
 }
