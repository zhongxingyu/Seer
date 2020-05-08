 /*
  * Team : AGF AM / OSI / SI / BO
  *
  * Copyright (c) 2001 AGF Asset Management.
  */
 package net.codjo.test.common.depend;
 import junit.framework.AssertionFailedError;
 import junit.framework.TestCase;
 /**
  * Classe de test de {@link PackageDependencyTestCase}.
  */
 public class PackageDependencyTestCaseTest extends TestCase {
     public void test_dependency() throws Exception {
         MyPackageDependencyTestCase testCase = new MyPackageDependencyTestCase();
         try {
             testCase.test_dependency();
             throw new Error("should have failed");
         }
         catch (AssertionFailedError ex) {}
         try {
             testCase.test_dependencyTest();
             throw new Error("should have failed");
         }
         catch (AssertionFailedError ex) {}
     }
 
 
     @Override
     protected void setUp() throws Exception {}
 
     private static class MyPackageDependencyTestCase extends PackageDependencyTestCase {
         public void test_dependency() throws Exception {
             Dependency dependency = createDependency();
            dependency.assertDependency("dependencyTest_ok.txt");
             dependency.assertNoCycle();
         }
 
 
         public void test_dependencyTest() throws Exception {
             Dependency dependency = createTestDependency();
            dependency.assertDependency("dependencyTest_ok.txt");
             dependency.assertNoCycle();
         }
     }
 }
