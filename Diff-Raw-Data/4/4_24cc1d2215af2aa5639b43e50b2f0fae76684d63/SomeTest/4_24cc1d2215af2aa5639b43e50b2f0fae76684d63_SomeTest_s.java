 import junit.framework.TestCase;
 
 public class SomeTest extends TestCase
 {
     public void testOne() throws Exception
     {
//..    new ThisShouldCompile();
      fail("one"); 
     }
 
     public void testTwo() throws Exception
     {
         new ThisShouldCompile();
 //      fail("two"); 
     }
 
     public void testThree() throws Exception
     {
         new ThisShouldCompile();
 //        fail("three"); 
     }
 }
