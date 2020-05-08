 /*
 * $Id: TestRunner.java,v 1.11 2003-07-05 13:45:20 o_rossmueller Exp $
  *
  * (c) 2002 Oliver Rossmueller
  *
  *
  */
 
 package org.junitee.runner;
 
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import junit.runner.BaseTestRunner;
 import junit.runner.TestSuiteLoader;
 import junit.framework.*;
 
 
 /**
  * This is the JUnitEE testrunner.
  *
  * @author  <a href="mailto:oliver@oross.net">Oliver Rossmueller</a>
 * @version $Revision: 1.11 $
  * @since   1.5
  */
 public class TestRunner extends BaseTestRunner {
 
   private TestSuiteLoader loader;
   private TestRunnerListener listener;
   private volatile boolean run = false;
   private boolean forkThread;
 
 
   /**
    * Create a new instance and set the classloader to be used to load test classes.
    *
    * @param loader  classloader to load test classes
    * @param listener  test listener to be notfied
    */
   public TestRunner(ClassLoader loader, TestRunnerListener listener, boolean forkThread) {
     this.listener = listener;
     this.loader = new org.junitee.runner.TestSuiteLoader(loader);
     this.forkThread = forkThread;
   }
 
 
   public void stop() {
     run = false;
     // notify the listener immediatley so we can display this information
     listener.setStopped();
   }
 
 
   /**
    * Run all tests in the given test classes.
    *
    * @param testClassNames names of the test classes
    */
   public void run(final String[] testClassNames) {
     Runnable runnable = new Runnable() {
 
       public void run() {
         TestResult result = new TestResult();
         result.addListener(listener);
         listener.start(false);
 
         for (int i = 0; i < testClassNames.length; i++) {
           Test test = getTest(testClassNames[i]);
           if (test != null) {
             test.run(result);
           }
           try {
             Thread.sleep(10);
           } catch (InterruptedException e) {
             // back to work
           }
           if (!run) {
             // if this was not the last test
             if (i != testClassNames.length - 1) {
               runFailed("Execution was stopped");
               break;
             }
           }
         }
         listener.finish();
       }
     };
     run = true;
     if (forkThread) {
       Thread thread = new Thread(runnable, this.toString());
       thread.start();
     } else {
       runnable.run();
     }
   }
 
 
   public void run(String testClassName, String testName) {
     TestResult result = new TestResult();
     result.addListener(listener);
 
     listener.start(true);
 
     Test test = getTest(testClassName, testName);
     if (test != null) {
       test.run(result);
     }
 
     listener.finish();
   }
 
 
   public TestSuiteLoader getLoader() {
     return loader;
   }
 
 
   protected void runFailed(String className) {
     listener.runFailed(className);
   }
 
 
   protected Test getTest(String suiteClassName, String testName) {
     try {
       Class clazz = loadSuiteClass(suiteClassName);
       Constructor constructor = clazz.getConstructor(new Class[]{String.class});
       Test test = (Test)constructor.newInstance(new Object[]{testName});
 
       if (test instanceof RequiresDecoration) {
         test = ((RequiresDecoration)test).decorate();
       }
       return test;
     } catch (ClassNotFoundException e) {
       runFailed("Class not found \"" + suiteClassName + "\"");
     } catch (InstantiationException e) {
       runFailed("Could not create instance of class \"" + suiteClassName + "\" (" + e.getMessage() + ")");
     } catch (IllegalAccessException e) {
       runFailed("Could not create instance of class \"" + suiteClassName + "\" (" + e.getMessage() + ")");
     } catch (InvocationTargetException e) {
       runFailed("Could not create instance of class \"" + suiteClassName + "\" (" + e.getMessage() + ")");
     } catch (NoSuchMethodException e) {
      runFailed("No method \"" + testName + "\" in class \"" + suiteClassName + "\"");
     }
     return null;
   }
 
 
   // TestListener methods; we do nothing here as the events are handled by the listener
   public void addError(Test test, Throwable throwable) {
   }
 
 
   public void addFailure(Test test, AssertionFailedError assertionFailedError) {
   }
 
 
   public void endTest(Test test) {
   }
 
 
   public void startTest(Test test) {
   }
 
 
   public void testStarted(String s) {
   }
 
 
   public void testEnded(String s) {
   }
 
 
   public void testFailed(int i, Test test, Throwable throwable) {
   }
 
 }
