 /*
  * This file is covered by the terms of the Common Public License v1.0.
  *
  * Copyright (c) SZEDER Gábor
  *
  * Parts of this software were developed within the JEOPARD research
  * project, which received funding from the European Union's Seventh
  * Framework Programme under grant agreement No. 216682.
  */
 
 package de.fzi.cjunit.runners;
 
 import java.lang.annotation.Annotation;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.internal.runners.model.ReflectiveCallable;
 import org.junit.internal.runners.statements.Fail;
 import org.junit.runners.BlockJUnit4ClassRunner;
 import org.junit.runners.model.FrameworkMethod;
 import org.junit.runners.model.InitializationError;
 import org.junit.runners.model.Statement;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import de.fzi.cjunit.ConcurrentTest;
 import de.fzi.cjunit.ConcurrentTest.None;
 import de.fzi.cjunit.runners.model.ConcurrentFrameworkMethod;
 import de.fzi.cjunit.runners.statements.ConcurrentStatement;
 
 public class ConcurrentRunner extends BlockJUnit4ClassRunner {
 
 	List<FrameworkMethod> testMethods;
 
 	public ConcurrentRunner(Class<?> klass) throws InitializationError {
 		super(klass);
 	}
 
 	@Override
 	protected void collectInitializationErrors(List<Throwable> errors) {
 		super.collectInitializationErrors(errors);
 		validateConcurrentTestMethods(errors);
 		validateNoMethodsWithAnnotation(BeforeClass.class, errors);
 		validateNoMethodsWithAnnotation(AfterClass.class, errors);
 	}
 
 	@Override
 	protected List<FrameworkMethod> computeTestMethods() {
 		if (testMethods == null) {
 			testMethods = new ArrayList<FrameworkMethod>(
 					super.computeTestMethods());
 			testMethods.addAll(computeConcurrentTestMethods());
 		}
 		return testMethods;
 	}
 
 	protected List<FrameworkMethod> computeConcurrentTestMethods() {
 		List<FrameworkMethod> methods
 				= getTestClass().getAnnotatedMethods(
 						ConcurrentTest.class);
 		List<FrameworkMethod> concurrentMethods
 				= new ArrayList<FrameworkMethod>();
 		for (FrameworkMethod eachMethod : methods) {
 			concurrentMethods.add(new ConcurrentFrameworkMethod(
 						eachMethod.getMethod()));
 		}
 		return concurrentMethods;
 	}
 
 	protected void validateConcurrentTestMethods(List<Throwable> errors) {
 		validatePublicVoidNoArgMethods(ConcurrentTest.class, false,
 				errors);
 
 		List<FrameworkMethod> methods
 				= getTestClass().getAnnotatedMethods(
 						ConcurrentTest.class);
 		for (FrameworkMethod eachMethod : methods) {
 			if (eachMethod.getAnnotation(Test.class) != null) {
 				String gripe = "@Test and @ConcurrentTest " +
 						"annotations are mutually " +
 						"exclusive";
 				errors.add(new Exception(gripe));
 			}
 		}
 	}
 
 	private void validateNoMethodsWithAnnotation(
 			Class<? extends Annotation> annotationClass,
 			List<Throwable> errors) {
 		if (!getTestClass().getAnnotatedMethods(annotationClass)
 				.isEmpty()) {
			String gripe = "@" + annotationClass.getSimpleName() +
					" annotation in a test class with " +
					"concurrent tests is not allowed";
 			errors.add(new Exception(gripe));
 		}
 	}
 
 	@Override
 	protected Statement methodBlock(FrameworkMethod method) {
 		if (!(method instanceof ConcurrentFrameworkMethod)) {
 			return super.methodBlock(method);
 		}
 
 		Object test;
 		try {
 			test = createTestObject();
 		} catch (Throwable e) {
 			return new Fail(e);
 		}
 		return buildStatements(method, test);
 	}
 
 	protected Object createTestObject() throws Throwable {
 		return new ReflectiveCallable() {
 			@Override
 			protected Object runReflectiveCall() throws Throwable {
 				return createTest();
 			}
 		}.run();
 	}
 
 	protected Statement buildStatements(FrameworkMethod method,
 			Object test) {
 		ConcurrentStatement statement = concurrentMethodInvoker(
 				method, test);
 		statement = concurrentPossiblyExpectingExceptions(method,
 				test, statement);
 		return statement;
 	}
 
 	protected ConcurrentStatement concurrentMethodInvoker(
 			FrameworkMethod method, Object test) {
 		return new ConcurrentStatement(method, test);
 	}
 
 	protected ConcurrentStatement concurrentPossiblyExpectingExceptions(
 			FrameworkMethod method, Object test,
 			ConcurrentStatement statement) {
 		ConcurrentTest annotation = method.getAnnotation(
 				ConcurrentTest.class);
 		if (annotation != null && annotation.expected() != None.class) {
 			statement.expectException(annotation.expected());
 		}
 		return statement;
 	}
 }
