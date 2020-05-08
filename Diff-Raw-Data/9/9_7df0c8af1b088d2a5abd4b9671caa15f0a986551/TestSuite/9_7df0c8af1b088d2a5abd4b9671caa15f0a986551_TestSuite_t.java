 package com.m91snik.code_gen.test.generator;
 
 
 /**
  * User: m91snik
  * Date: 11.10.13
  * Time: 0:08
  */
 
 import org.junit.runner.RunWith;
 import org.junit.runners.Suite;
 
 @RunWith(Suite.class)
@Suite.SuiteClasses({JavassistCodeGenTest.class, AsmCodeGenTest.class, CglibCodeGenTest.class})
 public class TestSuite {
 
 }
