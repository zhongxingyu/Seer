 /** Test.java
  *
  * Base class for deriving test classes. A derived test class commonly
  * contains concrete <code>name()</code>, <code>plan()</code> and
  * <code>test()</code> methods producing, respectively, the name of the test
  * class, the planned number of tests, and the results of running the tests
  * themselves.
  *
  * The method <code>ok(boolean, String)</code> defined in this class is used
  * every time a test is performed by the derived class. That is, a derived
  * class that plans five tests (say) will also call the <code>ok</code>
  * method five times.
  *
  * Copyright 2005-2006 Jonathan Alvarsson, Carl Masak
  */
 
 public abstract class Test {
     protected int testsRun = 0;
     protected int successfulTests = 0;
 
     protected String name;
     protected int testsPlanned;
 
     public Test( String name, int testsPlanned ) {
 
         this.name = name;
         this.testsPlanned = testsPlanned;
     }
     
     private void nextTest() {
 
         System.out.printf( "%03d - ", ++this.testsRun );
     }
    
     public void ok( boolean success, String description ) {
 
         nextTest();
 
         if (success) {
             ++this.successfulTests;
             System.out.println("[  ok  ] " + description.toLowerCase());
         }
         else {
             System.out.println("[NOT OK] " + description.toLowerCase());
         }
     }
    
     public void ok( boolean success ) {
         ok( success, "" );
     }
 
     public void test() {
       runTests();
 
       if (testsRun != testsPlanned)
         System.out.printf( "Warning: %d tests planned but %d run.\n",
                            testsPlanned, testsRun );
 
       System.out.printf( "%d tests successful (%d%%)",
                         successfulTests,
                         (int)(100.0*successfulTests/testsPlanned + .5) );
 
       if (successfulTests < testsRun)
         System.out.printf( ", %d failed (%d%%)",
                            testsRun-successfulTests,
                           (int)(100.0*(testsRun-successfulTests)
                                 / testsPlanned + .5) );
 
       System.out.println( "." );
     }
 
     protected abstract void runTests();
 }
