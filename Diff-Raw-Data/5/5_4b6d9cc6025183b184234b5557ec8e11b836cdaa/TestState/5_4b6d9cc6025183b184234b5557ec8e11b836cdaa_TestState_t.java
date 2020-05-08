 /*******************************************************************************
  * Copyright (c) 2010 Oracle.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * and Apache License v2.0 which accompanies this distribution. 
  * The Eclipse Public License is available at
  *     http://www.eclipse.org/legal/epl-v10.html
  * and the Apache License v2.0 is available at 
  *     http://www.opensource.org/licenses/apache2.0.php.
  * You may elect to redistribute this code under either of these licenses.
  *
  * Contributors:
  *     mkeith - Gemini JPA tests 
  ******************************************************************************/
 package test;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.junit.runner.Result;
 
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 
 /**
  * Test state class used to keep a memory of the tests that get executed
  * in the face of the test cases continually being refreshed.
  * 
  * NOTE: This bundle must not require any other test packages or bundles
  *       so it doesn't get refreshed when test punits get refreshed.
  * 
  * @author mkeith
  */
 public class TestState implements BundleActivator {
 
     public static String GEMINI_TEST_CLASSES = "GEMINI_TESTS";
 
     public static boolean isDsfOnline = false;
     public static Set<Class<?>> dsfQueuedTests = new HashSet<Class<?>>();
 
     static Set<String> incompletedTests = new HashSet<String>();
     static Map<String,Result> completedTests = new HashMap<String,Result>();
     static boolean initialized = initTests();
     
     static boolean initTests() {
         incompletedTests = new HashSet<String>();
         completedTests = new HashMap<String,Result>();
 
         // If test property provided then just run comma-separated list 
         // of unqualified JpaTest subclasses in org.eclipse.gemini.jpa.tests
         String tests = System.getProperty(GEMINI_TEST_CLASSES, null);
         if (tests != null) {
             incompletedTests.addAll(Arrays.asList(tests.split(",")));
         } else {
             // Enumerate the tests to run - Comment out tests to disable them.
             // Each test is the name of a JpaTest subclass in the 
             // org.eclipse.gemini.jpa.tests package
             /*   */
             incompletedTests.add("TestMongo");
            incompletedTests.add("TestStaticPersistence");
             incompletedTests.add("TestEMFService");
             incompletedTests.add("TestEMFBuilderService");
             incompletedTests.add("TestEMFBuilderServiceProperties");
             incompletedTests.add("TestEMFBuilderExternalDataSource");
             incompletedTests.add("TestEmbeddedPUnit");
             incompletedTests.add("TestOrmMappingFile");
             incompletedTests.add("TestMappingFileElement");
             incompletedTests.add("TestEmptyPersistence");
             incompletedTests.add("TestEmptyPersistenceWithProps");
             incompletedTests.add("TestWeaving");
             incompletedTests.add("TestEmbeddedJdbc");
        }
         return true;
     }
     
     public static void resetTests() { 
         initTests(); 
     }
 
     public static void startTest(String s) { 
         incompletedTests.remove(s);
     }
     
     public static void completedTest(String s, Result r) { 
         completedTests.put(s, r);
     }
 
     public static Set<String> getIncompletedTests() { 
         return incompletedTests;
     }
 
     public static boolean isTested(String s) { 
         return !incompletedTests.contains(s); 
     }
 
     public static Map<String,Result> getAllTestResults() { 
         return completedTests; 
     }
 
     public void start(BundleContext context) throws Exception {
         System.out.println("TestState active");
         System.out.println("Tests in run list: ");
         System.out.println("" + incompletedTests);
     }
 
     public void stop(BundleContext context) throws Exception {}
 }
