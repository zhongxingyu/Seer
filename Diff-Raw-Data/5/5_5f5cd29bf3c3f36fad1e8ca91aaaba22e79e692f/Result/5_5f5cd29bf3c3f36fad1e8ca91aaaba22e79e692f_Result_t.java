 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package de.schlueters.phpttestrunner.results;
 
 import java.io.File;
 import java.util.List;
 import java.util.LinkedList;
 
 /**
  *
  * @author johannes
  */
 public abstract class Result {
     protected List<Test> executedTests;
     protected List<Test> failedTests;
     protected List<Test> expectedToFailedTests;
     protected List<Test> succesfulTests;
     protected List<Test> skippedTests;
     
     protected Result() {
         executedTests         = new LinkedList<Test>();
         failedTests           = new LinkedList<Test>();
         expectedToFailedTests = new LinkedList<Test>();
         succesfulTests        = new LinkedList<Test>();
         skippedTests          = new LinkedList<Test>(); 
     }
     
     public Result(File file) throws Exception {
         this();
         parse(file);
     }
     
     protected abstract void parse(File file) throws Exception;
     
     protected void testExecuted(Test test) {
        if (test == null || test.getResult() == null) {
            // TODO this should never happen ...
            return;
        }

         executedTests.add(test);
         switch (test.getResult()) {
             case PASS:
                 succesfulTests.add(test);
                 break;
             case FAIL:
                 failedTests.add(test);;
                 break;
             case XFAIL:
                 expectedToFailedTests.add(test);
                 break;
             case SKIP:
                 skippedTests.add(test);
                 break;
         }
     }
     
     public List<Test> getExecutedTests() {
         if (executedTests == null) {
             throw new NullPointerException();
         }
         return executedTests;
     }
     public List<Test> getFailedTests() {
         if (failedTests == null) {
             throw new NullPointerException();
         }
         return failedTests;
     }
     
     public List<Test> getExpectedToFailedTests() {
         if (expectedToFailedTests == null) {
             throw new NullPointerException();
         }
         return expectedToFailedTests;
     }
     public List<Test> getSuccesfulTests() {
         if (succesfulTests == null) {
             throw new NullPointerException();
         }
         return succesfulTests;
     }
     public List<Test> getSkippedTests() {
         if (skippedTests == null) {
             throw new NullPointerException();
         }
         return skippedTests;
     }
 }
