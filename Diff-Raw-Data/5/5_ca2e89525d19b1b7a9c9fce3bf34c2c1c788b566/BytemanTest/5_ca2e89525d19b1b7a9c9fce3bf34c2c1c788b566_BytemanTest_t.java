 package org.jboss.devconf2013.lab.byteman;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import org.jboss.byteman.contrib.bmunit.BMNGRunner;
 import org.jboss.byteman.contrib.bmunit.BMRule;
 import org.jboss.byteman.contrib.bmunit.BMScript;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 public class BytemanTest extends BMNGRunner {
 
    private BMClass clazz = new BMClass();
 
    @BMRule(
          name = "ruleAnnotation",
          targetClass = "org.jboss.devconf2013.lab.byteman.BMClass",
          targetMethod = "doSomething",
          condition = "true",
          targetLocation = "AT ENTRY",
          action = "throw new java.io.IOException()")
    @Test
    public void bmRuleAnnotationTest() {
       try {
          clazz.doSomething();
       } catch (IOException e) {
          e.printStackTrace();
          return;
       }
       Assert.fail("The java.io.IOException should be thrown at this point.");
    }
 
    @BMScript(value="BytemanTest-bmRuleFileTest.btm")
    @Test
    public void bmRuleFileTest() {
       try {
          clazz.doSomethingElse();
       } catch (FileNotFoundException e) {
          e.printStackTrace();
          return;
       }
       Assert.fail("The java.io.FileNotFoundException should be thrown at this point.");
    }
 
    @Test
    public void doSomethingTest() {
       try {
          clazz.doSomething();
       } catch (IOException e) {
          e.printStackTrace();
         Assert.fail("The java.io.IOException should not be thrown at this point.");
       }
    }
 
    @Test
    public void doSomethingElseTest() {
       try {
          clazz.doSomethingElse();
       } catch (FileNotFoundException e) {
          e.printStackTrace();
         Assert.fail("The java.io.FileNotFoundException should not be thrown at this point.");
       }
    }
 }
