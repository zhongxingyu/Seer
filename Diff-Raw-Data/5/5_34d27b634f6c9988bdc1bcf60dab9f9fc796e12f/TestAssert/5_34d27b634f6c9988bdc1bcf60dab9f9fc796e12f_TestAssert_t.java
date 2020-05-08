 /*
  * Copyright (c) 2000
  *      Jon Schewe.  All rights reserved
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
  * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
  * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  *
  * I'd appreciate comments/suggestions on the code jpschewe@mtu.net
  */
 package net.mtu.eggplant.dbc.test;
 
 import net.mtu.eggplant.dbc.AssertTools;
 import net.mtu.eggplant.dbc.AssertionViolation;
 
 import java.io.IOException;
 import java.util.Vector;
 
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import junit.textui.TestRunner;
 
 /**
  * This is a test class for testing my assertions.  Tests all kinds of things.
  * Must be called with ASSERT_BEHAVIOR_CONDITIONS set to EXCEPTION.
  *  
  * @invariant (_invariant), "This is an invariant";
 * @version $Revision: 1.5 $
  */
 public class TestAssert extends TestCase {
 
   private boolean _invariant = true;
   
   static public void main(final String[] args) {
     final TestSuite suite = new TestSuite();
     suite.addTest(suite());
     TestRunner.run(suite);
     
   }
 
   public TestAssert(final String name) {
     super(name);
     if(!"EXCEPTION".equalsIgnoreCase(AssertTools.ASSERT_BEHAVIOR)) {
       fail("ASSERT_BEHAVIOR must be set to EXCEPTION");
     }
   }
 
   /**
      @pre (j > 10)
   **/
   public TestAssert(final int j) {
     super("null");
   }
 
   
   static public TestSuite suite() {
     return new TestSuite(TestAssert.class);
   }
   
   /**
      Just see if it runs.  This is in response to a bug from John Maloney.
   */
   public void testDefaultPackage() {
     final DefaultPackage dp = new DefaultPackage();
     dp.foo(new Object());
   }
   
   /**
      Check to see if an invariant fails when it should.
   **/
   public void testFailInvariant() {
     boolean exception = false;
     try {
       failInvariant();
     }
     catch(final AssertionViolation av) {
       exception = true;
     }
     finally {
       _invariant = true;
     }
     assertTrue("a1:This should throw an assertion violation", exception); 
           
   }
 
   public void failInvariant() {
     _invariant = false;
   }
 
   /**
      Check if constructor preconditions work.
   **/
   public void testConstructorPreCondition() {
     boolean exception = false;
     try {
       final TestAssert ta = new TestAssert(9);
     }
     catch(final AssertionViolation av) {
       exception = true;
     }
     assertTrue("a1:This should throw an assertion violation", exception); 
 
     exception = false;
     try {
       final TestAssert ta = new TestAssert(12);
     }
     catch(final AssertionViolation av) {
       exception=true;
     }
     assertTrue("a2:This should not throw an assertion violation", !exception);
     
   }
   
   /**
      Check if general preconditions work.
   **/
   public void testPrecondition() {
     boolean exception = false;
     try {
       preCond(-5); // should fail
     }
     catch(final AssertionViolation av) {
       exception = true;
     }
     assertTrue("a1:This should throw an assertion violation", exception);
 
     exception = false;
     try {
       preCond(10); // should pass
     }
     catch(final AssertionViolation av) {
       exception = true;
     }
     assertTrue("a2:This not should throw an assertion violation", !exception);
   }
 
   /**
      Check if post conditions work.
   **/
   public void testPostcondition() {
     boolean exception = false;
     try {
       postCond(4); // should pass
     }
     catch(final AssertionViolation av) {
       exception = true;
     }
     assertTrue("a3:This should not throw an assertion violation", !exception); 
 
     exception = false;
     try {
       postCond(10); // should fail
     }
     catch(final AssertionViolation av) {
       exception = true;
     }
     assertTrue("a4:This should throw an assertion violation", exception); 
       
   }
 
   /**
      @pre (foo(i) > 0)
   **/
   public boolean preCond(final int i) {
     return false;
   }
 
   /**
      @post ($return < 10), "Post condition";
   **/
   public int postCond(final int i) {
     return i+5;
   }
 
   /**
      Make sure that inline assertions work.
   **/
   public void testInlineAssertFail() {
     boolean exception = false;
     try {
       /**
          @assert (false)
       **/
       ;
     }
     catch(final AssertionViolation av) {
       exception = true;
     }
     assertTrue("a1:This should throw an assertion violation", exception); 
 
   }
 
   /**
      Test assertions on abstract methods.
   **/
   public void testAbstractMethod() {
     boolean exception = false;
     final AbstractClass ac = new ConcreteClass();
 
     /**
        @assert (ac != null)
     **/
     try {
       ac.preCond(-5); // should fail
     }
     catch(final AssertionViolation av) {
       exception = true;
     }
     assertTrue("a1:This should throw an assertion violation", exception); 
 
     exception = false;
     try {
       ac.preCond(10); // should pass
     }
     catch(final AssertionViolation av) {
       exception = true;
     }
     assertTrue("a2:This should not throw an assertion violation", !exception); 
 
     exception = false;
     try {
       ac.postCond(5); // should pass
     }
     catch(final AssertionViolation av) {
       exception = true;
     }
     assertTrue("a3:This should not throw an assertion violation", !exception); 
 
     exception = false;
     try {
       ac.postCond(10); // should fail
     }
     catch(final AssertionViolation av) {
       exception = true;
     }
     assertTrue("a4:This should throw an assertion violation", exception); 
     
     
   }
 
   /**
      Test interface assertions.
   **/
   public void testInterface() {
     boolean exception = false;
     final Interface it = new InterfaceClass();
 
     try {
       it.preCond(-5); // should fail
     }
     catch(final AssertionViolation av) {
       exception = true;
     }
     assertTrue("a1:This should throw an assertion violation", exception); 
 
     exception = false;
     try {
       it.preCond(10); // should pass
     }
     catch(final AssertionViolation av) {
       exception = true;
     }
     assertTrue("a2:This should not throw an assertion violation", !exception); 
 
     exception = false;
     try {
       it.postCond(5); // should pass
     }
     catch(final AssertionViolation av) {
       exception = true;
     }
     assertTrue("a3:This should not throw an assertion violation", !exception); 
 
     exception = false;
     try {
       it.postCond(10); // should fail
     }
     catch(final AssertionViolation av) {
       exception = true;
     }
     assertTrue("a4:This should throw an assertion violation", exception); 
     
 
   }
 
   private int foo(final int i) {
     return i;
   }
 
   /**
      Test assertions on anonymous classes.
   **/
   public void testAnonymousClass() {
     final AnonymousClass ac = new AnonymousClass();
     boolean exception = false;
     try {
       ac.pass();
     } catch(final AssertionViolation av) {
       exception = true;
     }
     assertTrue("a1:This should not throw an assertion violation", !exception); 
 
     exception = false;
     try {
       ac.fail();
     } catch(final AssertionViolation av) {
       exception = true;
     }
     assertTrue("a2:This should throw an assertion violation", exception); 
     
   }
 
   /**
      Test assertions on named inner classes.
   **/
   public void testNamedInnerClass() {
     final NamedInnerClass nic = new NamedInnerClass();
     boolean exception = false;
     try {
       nic.pass();
     } catch(final AssertionViolation av) {
       exception = true;
     }
     assertTrue("a1:This should not throw an assertion violation", !exception); 
 
     exception = false;
     try {
       nic.fail();
     } catch(final AssertionViolation av) {
       exception = true;
     }
     assertTrue("a2:This should throw an assertion violation", exception); 
   }
 
   /**
      Test assertions on private methods.
   **/
   public void testPrivateMethod() {
     final PrivateMethodTest nic = new PrivateMethodTest();
     boolean exception = false;
     try {
       nic.pass();
     } catch(final AssertionViolation av) {
       exception = true;
     }
     assertTrue("a1:This should not throw an assertion violation", !exception); 
 
     exception = false;
     try {
       nic.fail();
     } catch(final AssertionViolation av) {
       exception = true;
     }
     assertTrue("a2:This should throw an assertion violation", exception); 
   }  
 
   /**
      Test assertions on static methods.
   **/
   public void testStaticMethod() {
     boolean exception = false;
     try {
       StaticMethod.pass();
     } catch(final AssertionViolation av) {
       exception = true;
     }
     assertTrue("a1:This should not throw an assertion violation", !exception); 
 
     exception = false;
     try {
       StaticMethod.fail();
     } catch(final AssertionViolation av) {
       exception = true;
     }
     assertTrue("a2:This should throw an assertion violation", exception); 
   }
 
   /**
      Test that exceptions are properly handled.  ie. the post condition
      doesn't get checked.
   **/
   public void testPostConditionException() {
     boolean exception = false;
     boolean ioexception = false;
     try {
       final Node n = new Node();
       n.exceptionMethod(10);
     } catch(final AssertionViolation av) {
       exception = true;
     } catch(final java.io.IOException ioe) {
       ioexception = true;
     }
     assertTrue("a1:This should not throw an assertion violation", !exception); 
     assertTrue("a3:This shuld throw an IOException", ioexception);
 
     exception = false;
     ioexception = false;
     try {
       final Node n = new Node();
       n.exceptionMethod(-10);
     } catch(final AssertionViolation av) {
       exception = true;
     } catch(final IOException ioe) {
       ioexception = true;
     }
     assertTrue("a2:This should not throw an assertion violation", !exception);
     assertTrue("a4:This should not throw an IOException", !ioexception);
   }
 
   /**
      This test checks to make sure that superclass assertions are not checked
      on private methods and only checked on classes within the same package
      for package methods.
   **/
   public void testExtraConditionChecks() {
     final net.mtu.eggplant.dbc.test.sub.CheckWrongPreconditionsSubClass c = new net.mtu.eggplant.dbc.test.sub.CheckWrongPreconditionsSubClass();
     boolean exception = false;
     try {
       c.testPrivateMethod(0);
     } catch(final AssertionViolation av) {
       exception = true;
     }
     assertTrue("a1:Preconditions are being checked on superclass of private method", !exception);
 
     exception = false;
     try {
       c.testPackageMethod(0);
     } catch(final AssertionViolation av) {
       exception = true;
     }
     assertTrue("a2:Precondditions are being checked on superclass of package method with subclass in different package", !exception);
     
   }
 
 }
