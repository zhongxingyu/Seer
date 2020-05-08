 package org.citest.ci_test;
 
 import org.citest.ci_test.App;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 /**
  * Unit test for simple App.
  */
 public class AppTest 
     extends TestCase
 {
     /**
      * Create the test case
      *
      * @param testName name of the test case
      */
     public AppTest( String testName )
     {
         super( testName );
     }
 
     /**
      * @return the suite of tests being tested
      */
     public static Test suite()
     {
         return new TestSuite( AppTest.class );
     }
 
     public void testSquear ()
     {
     	double result = App.squear(2);
     	assertEquals(4.0, result);
     }
 
     public void testSum ()
     {
     	double result = App.sum(2);
     	assertEquals(4.0, result);
     }
 
     public void testSumFailed ()
     {
    	double result = App.sum(2);
    	assertEquals(5.0, result);
     }
 }
