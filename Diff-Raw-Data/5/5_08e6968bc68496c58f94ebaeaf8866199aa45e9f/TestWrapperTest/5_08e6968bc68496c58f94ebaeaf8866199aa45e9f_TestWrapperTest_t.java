 /*
  * This file is covered by the terms of the Common Public License v1.0.
  *
  * Copyright (c) SZEDER Gábor
  *
  * Parts of this software were developed within the JEOPARD research
  * project, which received funding from the European Union's Seventh
  * Framework Programme under grant agreement No. 216682.
  */
 
 package de.fzi.cjunit.jpf.inside;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.*;
 
 import org.junit.Test;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 
 import de.fzi.cjunit.testexceptions.*;
 
 
 public class TestWrapperTest {
 
 	String className = "java.lang.String";
 	String methodName = "toString";
 	String exceptionName = "java.lang.AssertionError";
 
 	public void throwNothing() { };
 	public void throwTestException() throws TestException {
 		throw new TestException();
 	}
 	public void throwOtherTestException() throws OtherTestException {
 		throw new OtherTestException();
 	}
 
 
 	@Test
 	public void parseArgsTestClass() {
 		TestWrapper tw = new TestWrapper(new String[] {
 				"--testclass=" + className });
 		assertThat(tw.testClassName, equalTo(className));
 	}
 
 	@Test
 	public void parseArgsTesMethod() {
 		TestWrapper tw = new TestWrapper(new String[] {
 				"--testmethod=" + methodName });
 		assertThat(tw.testMethodName, equalTo(methodName));
 	}
 
 	@Test
 	public void parseArgsBeforeMethods() {
 		TestWrapper tw = new TestWrapper(new String[] {
 				"--beforemethod=toString",
 				"--beforemethod=hashCode" });
 		assertThat("number of method names",
 				tw.beforeMethodNames.size(), equalTo(2));
 		assertThat(tw.beforeMethodNames,
 				hasItems("toString", "hashCode"));
 	}
 
 	@Test
 	public void parseArgsAfterMethods() {
 		TestWrapper tw = new TestWrapper(new String[] {
 				"--aftermethod=wait",
 				"--aftermethod=notifyAll" });
 		assertThat("number of method names",
 				tw.afterMethodNames.size(), equalTo(2));
 		assertThat(tw.afterMethodNames,
 				hasItems("wait", "notifyAll"));
 	}
 
 	@Test
 	public void parseArgsExpectedException() {
 		TestWrapper tw = new TestWrapper(new String[] {
 				"--expectedexception=" + exceptionName });
 		assertThat(tw.expectedExceptionName, equalTo(exceptionName));
 	}
 
 	@Test(expected=RuntimeException.class)
 	public void parseArgsWrongArgument() {
 		new TestWrapper(new String[] { "asdf" });
 	}
 
 	@Test(expected=RuntimeException.class)
 	public void parseArgsNoValue() {
 		new TestWrapper(new String[] { "--testclass=" });
 	}
 
 	@Test(expected=RuntimeException.class)
 	public void parseArgsNoArguments() {
 		new TestWrapper((String[]) null);
 	}
 
 	@Test
 	public void createTestObject() throws Throwable {
 		TestWrapper tw = new TestWrapper();
 		tw.testClassName = String.class.getName();
 		tw.createTestObject();
 		assertThat(tw.target, instanceOf(String.class));
 	}
 
 	@Test
 	public void createMethod() throws Throwable {
 		TestWrapper tw = new TestWrapper();
 		tw.testClassName = String.class.getName();
 		tw.createTestObject();
 		Method m = tw.createMethod(methodName);
 		assertThat(m.getName(), equalTo(methodName));
 		assertThat(m.getDeclaringClass().getName(), equalTo(className));
 	}
 
 	@Test
 	public void createTestMethod() throws Throwable {
 		TestWrapper tw = new TestWrapper();
 		tw.testClassName = String.class.getName();
 		tw.testMethodName = methodName;
 		tw.createTestObject();
 		tw.createTestMethod();
 		assertThat(tw.method.getName(), equalTo(methodName));
 		assertThat(tw.method.getDeclaringClass().getName(),
 				equalTo(className));
 	}
 
 	@Test
 	public void createBeforeMethods() throws Throwable {
 		TestWrapper tw = new TestWrapper(new String[] {
 				"--testclass=" + className,
 				"--beforemethod=toString",
 				"--beforemethod=hashCode" });
 		tw.createTestObject();
 		tw.createBeforeMethods();
 		assertThat("number of methods",
				tw.beforeMethods.size(), equalTo(2));
 	}
 
 	@Test
 	public void createAfterMethods() throws Throwable {
 		TestWrapper tw = new TestWrapper(new String[] {
 				"--testclass=" + className,
 				"--aftermethod=wait",
 				"--aftermethod=notifyAll" });
 		tw.createTestObject();
 		tw.createAfterMethods();
 		assertThat("number of methods",
				tw.afterMethods.size(), equalTo(2));
 	}
 
 	@Test
 	public void isExpectedExceptionTrue() throws Throwable {
 		TestWrapper tw = new TestWrapper();
 		tw.expectedExceptionName = TestException.class.getName();
 		assertThat(tw.isExpectedException(new TestException()),
 				equalTo(true));
 	}
 
 	@Test
 	public void isExpectedExceptionFalse() throws Throwable {
 		TestWrapper tw = new TestWrapper();
 		tw.expectedExceptionName = TestException.class.getName();
 		assertThat(tw.isExpectedException(new OtherTestException()),
 				equalTo(false));
 	}
 
 	@Test
 	public void isExpectedExceptionTrueOnSubclass() throws Throwable {
 		TestWrapper tw = new TestWrapper();
 		tw.expectedExceptionName = ParentTestException.class.getName();
 		assertThat(tw.isExpectedException(new ChildTestException()),
 				equalTo(true));
 	}
 
 	@Test(expected=InvocationTargetException.class)
 	public void testInvokeTestMethodThrowsInvocationTargetException()
 			throws Throwable {
 		TestWrapper tw = new TestWrapper();
 		tw.target = this;
 		tw.method = this.getClass().getMethod("throwTestException");
 
 		tw.invokeTestMethod();
 	}
 
 	@Test(expected=TestException.class)
 	public void testInvokeMethodUnchainingExceptionThrowsTestException()
 			throws Throwable {
 		TestWrapper tw = new TestWrapper();
 		tw.target = this;
 		tw.method = this.getClass().getMethod("throwTestException");
 
 		tw.invokeMethodUnchainingException(tw.method);
 	}
 
 	@Test(expected=AssertionError.class)
 	public void testExpectingExceptionButNoneIsThrown() throws Throwable {
 		TestWrapper tw = new TestWrapper();
 		tw.target = this;
 		tw.method = this.getClass().getMethod("throwNothing");
 		tw.expectedExceptionName = TestException.class.getName();
 
 		tw.runTestMethod();
 	}
 
 	@Test
 	public void testExpectedExceptionIsCatched() throws Throwable {
 		TestWrapper tw = new TestWrapper();
 		tw.target = this;
 		tw.method = this.getClass().getMethod("throwTestException");
 		tw.expectedExceptionName = TestException.class.getName();
 
 		tw.runTestMethod();
 	}
 
 	@Test(expected=Exception.class)
 	public void testUnexpectedExceptionIsThrown() throws Throwable {
 		TestWrapper tw = new TestWrapper();
 		tw.target = this;
 		tw.method = this.getClass().getMethod("throwTestException");
 		tw.expectedExceptionName = OtherTestException.class.getName();
 
 		tw.runTestMethod();
 	}
 
 	@Test
 	public void testRunBeforeMethods() throws Throwable {
 		final List<String> invokedMethodNames = new ArrayList<String>();
 		TestWrapper tw = new TestWrapper() {
 			protected void invokeMethodUnchainingException(Method m)
 					throws IllegalArgumentException,
 					IllegalAccessException, Throwable {
 				invokedMethodNames.add(m.getName());
 			}
 		};
 		tw.beforeMethods.add(this.getClass().getMethod("throwNothing"));
 		tw.beforeMethods.add(this.getClass().getMethod(
 				"throwTestException"));
 
 		tw.runBeforeMethods();
 
 		assertThat("number of invoked methods",
 				invokedMethodNames.size(), equalTo(2));
 		assertThat("invoked method names", invokedMethodNames,
 				hasItems("throwNothing", "throwTestException"));
 	}
 
 	@Test(expected=TestException.class)
 	public void testExceptionInBeforeMethods() throws Throwable {
 		TestWrapper tw = new TestWrapper();
 		tw.target = this;
 		tw.beforeMethods.add(this.getClass().getMethod(
 				"throwTestException"));
 
 		tw.runBeforeMethods();
 	}
 
 	// Can't put it inside the method, because it has to be final to be
 	// accessible from the inner class, but if it's final, then it can't be
 	// modified.  Catch-22.
 	boolean testMethodInvoked = false;
 
 	@Test
 	public void testMethodIsNotInvokedIfABeforeMethodFails()
 			throws Throwable {
 		TestWrapper tw = new TestWrapper() {
 			protected void invokeTestMethod() {
 				testMethodInvoked = true;
 			}
 		};
 		tw.target = this;
 		tw.beforeMethods.add(this.getClass().getMethod(
 				"throwTestException"));
 
 		try {
 			tw.runTest();
 		} catch (TestException te) {}
 
 		assertThat(testMethodInvoked, equalTo(false));
 	}
 
 	@Test
 	public void testRunAllAfterMethodsEvenIfOneThrows() throws Throwable {
 		final List<String> invokedMethodNames = new ArrayList<String>();
 		TestWrapper tw = new TestWrapper() {
 			protected void invokeMethodUnchainingException(Method m)
 					throws IllegalArgumentException,
 					IllegalAccessException, Throwable {
 				invokedMethodNames.add(m.getName());
 				super.invokeMethodUnchainingException(m);
 			}
 		};
 		tw.target = this;
 		tw.afterMethods.add(this.getClass().getMethod(
 				"throwTestException"));
 		tw.afterMethods.add(this.getClass().getMethod("throwNothing"));
 
 		tw.runAfterMethods();
 
 		assertThat("number of invoked methods",
 				invokedMethodNames.size(), equalTo(2));
 		assertThat("exception-thrower method is invoked first",
 				invokedMethodNames.get(0),
 				equalTo("throwTestException"));
 		assertThat("other method is invoked after", invokedMethodNames,
 				hasItem("throwNothing"));
 	}
 
 	@Test(expected=TestException.class)
 	public void testSingleError() throws Throwable {
 		TestWrapper tw = new TestWrapper();
 		tw.errors.add(new TestException());
 
 		tw.handleErrors();
 	}
 
 	@Test(expected=Exception.class)
 	public void testExceptionOnMultipleError() throws Throwable {
 		TestWrapper tw = new TestWrapper();
 		tw.errors.add(new TestException());
 		tw.errors.add(new OtherTestException());
 
 		tw.handleErrors();
 	}
 
 	@Test(expected=TestException.class)
 	public void testFirstErrorOnMultipleError() throws Throwable {
 		TestWrapper tw = new TestWrapper();
 		tw.errors.add(new TestException());
 		tw.errors.add(new OtherTestException());
 
 		try {
 			tw.handleErrors();
 		} catch (Exception e) {
 			throw e.getCause();
 		}
 	}
 
 	// this also implicitly tests that @After methods are invoked even if
 	// the test method throws an exception
 	@Test
 	public void testErrorsAreCollectedInOrder() throws Throwable {
 		TestWrapper tw = new TestWrapper();
 		tw.target = this;
 		tw.method = this.getClass().getMethod("throwTestException");
 		tw.afterMethods.add(this.getClass().getMethod(
 				"throwOtherTestException"));
 
 		try {
 			tw.runTest();
 		} catch (Exception e) {}
 
 		assertThat("number of exceptions", tw.errors.size(),
 				equalTo(2));
 		assertThat("test method's exception is the first",
 				tw.errors.get(0),
 				instanceOf(TestException.class));
 		assertThat("@After method's exception is second",
 				tw.errors.get(1),
 				instanceOf(OtherTestException.class));
 	}
 }
