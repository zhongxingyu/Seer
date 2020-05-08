 /*******************************************************************************
  * Copyright (c) 2010, Cloudsmith Inc.
  * The code, documentation and other materials contained herein have been
  * licensed under the Eclipse Public License - v 1.0 by the copyright holder
  * listed above, as the Initial Contributor under such license. The text of
  * such license is available at www.eclipse.org.
  ******************************************************************************/
 
 package org.eclipse.b3.beelang.junit;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.b3.BeeLangStandaloneSetup;
 import org.eclipse.b3.backend.evaluator.b3backend.IFunction;
 import org.eclipse.b3.build.BeeModel;
 import org.eclipse.b3.build.BuildUnit;
 import org.eclipse.b3.build.IBuilder;
 import org.eclipse.b3.build.core.B3BuildConstants;
 import org.eclipse.b3.build.core.B3BuildEngine;
 import org.eclipse.b3.build.core.SharedScope;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.ContentHandler;
 import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
 import org.eclipse.xtext.diagnostics.AbstractDiagnostic;
 import org.eclipse.xtext.resource.XtextResource;
 import org.eclipse.xtext.resource.XtextResourceSet;
 import org.junit.runner.Description;
 import org.junit.runner.Runner;
 import org.junit.runner.notification.Failure;
 import org.junit.runner.notification.RunNotifier;
 import org.junit.runners.ParentRunner;
 import org.junit.runners.model.InitializationError;
 import org.junit.runners.model.Statement;
 import org.osgi.framework.BundleReference;
 
 import com.google.inject.Injector;
 
 /**
  * A factory class for building JUnit 4 runners capable of executing B3 language functions as JUnit tests. A separate
  * runner is built for each of the <code>{@link B3TestFiles}</code> defined for the class passed to the factory's
  * constructor. Each runner built by this factory executes all B3 functions from the B3 file it was built for which
  * names start with the prefix of "test" as separate JUnit tests.
  * <p>
  * When test are executed, the property <code>$test.argv</code> is bound to a {@link List} containing an instance of {@link B3BuildEngine} in the
  * first position, and subsequent positions containing references to any {@link BuildUnit} instances defined in the executed b3 file.
  * </p>
  * 
  * @see B3TestFiles
  * @see JUnitB3TestRunner
  */
 class JUnitB3FileRunnerFactory {
 
 	protected static class ErrorReportingRunner extends Runner {
 
 		private final Description testDescription;
 
 		private final Throwable error;
 
 		private String errorMessage;
 
 		public ErrorReportingRunner(Description description, Throwable t, String message) {
 			testDescription = description;
 			error = t;
 			errorMessage = message;
 		}
 
 		@Override
 		public Description getDescription() {
 			return testDescription;
 		}
 
 		@Override
 		public void run(RunNotifier notifier) {
 			Description description = getDescription();
 
 			notifier.fireTestStarted(description);
 			try {
 				throw new Exception(errorMessage, error);
 			}
 			catch(Throwable t) {
 				notifier.fireTestFailure(new Failure(description, t));
 				Throwable cause = t.getCause();
 				if(cause != null && cause instanceof MultiProblemException) {
 					for(Throwable problem : ((MultiProblemException) cause).getProblems())
 						notifier.fireTestFailure(new Failure(description, problem));
 				}
 			}
 			finally {
 				notifier.fireTestFinished(description);
 			}
 		}
 
 	}
 
 	protected class JUnitB3FileRunner extends ParentRunner<JUnitB3FileRunner.TestFunctionDescriptor> {
 		protected class TestFunctionDescriptor {
 
 			protected String testFunctionName;
 
 			protected Description testDescription;
 
 			public TestFunctionDescriptor(String functionName) {
 				testFunctionName = functionName;
 				// We call Description.createSuiteDescription despite this is really no test suite. This is because the
 				// other Description.create*Description methods take a Class<?> argument which we can't provide since
 				// the tests are actually B3 functions not wrapped by any Java class.
 				testDescription = Description.createSuiteDescription(functionName + '(' + b3FilePath + ')');
 			}
 
 			public Description getDescription() {
 				return testDescription;
 			}
 
 			public String getFunctionName() {
 				return testFunctionName;
 			}
 
 		}
 
 		protected String b3FilePath;
 
 		protected ArrayList<TestFunctionDescriptor> testFunctionDescriptors;
 
 		protected B3BuildEngine engine;
 
 		private SharedScope resolutionScope;
 
 		public JUnitB3FileRunner(String b3File) throws Exception {
 			super(definitionClass);
 
 			b3FilePath = (b3File.charAt(0) != '/')
 					? '/' + b3File
 					: b3File;
 
 			initializeFunctionTests();
 		}
 
 		private void afterChildren() throws Exception {
 			resolutionScope.exit();
 		}
 
 		private void beforeChildren() throws Exception {
 			resolutionScope = engine.getInjector().getInstance(B3BuildConstants.KEY_RESOLUTION_SCOPE);
 			resolutionScope.enter();
 			IStatus status = engine.resolveAllUnits();
 			if(!status.isOK()) {
				// accept warning, but not error or cancel
				if(!status.matches(IStatus.WARNING))
					throw new Exception(status.toString());
 			}
 
 		}
 
 		@Override
 		protected Statement childrenInvoker(final RunNotifier notifier) {
 			final Statement original = super.childrenInvoker(notifier);
 			Statement s = new Statement() {
 
 				@Override
 				public void evaluate() throws Throwable {
 					beforeChildren();
 					original.evaluate();
 					afterChildren();
 				}
 
 			};
 			return s;
 		}
 
 		@Override
 		protected Description describeChild(TestFunctionDescriptor child) {
 			return child.getDescription();
 		}
 
 		@Override
 		protected List<TestFunctionDescriptor> getChildren() {
 			return testFunctionDescriptors;
 		}
 
 		@Override
 		protected String getName() {
 			return b3FilePath;
 		}
 
 		protected void initializeFunctionTests() throws Exception {
 			URI b3FileURI = URI.createPlatformPluginURI(containingBundleName + b3FilePath, true);
 			XtextResource resource = (XtextResource) beeLangResourceSet.createResource(
 				b3FileURI, ContentHandler.UNSPECIFIED_CONTENT_TYPE);
 
 			resource.load(null);
 			// List<org.eclipse.emf.common.util.Diagnostic> syntaxErrors = resource.validateConcreteSyntax();
 
 			EList<Diagnostic> errors = resource.getErrors();
 			if(errors.size() > 0 /* || syntaxErrors.size() > 0 */) {
 				ArrayList<Throwable> problems = new ArrayList<Throwable>(errors.size());
 
 				for(Diagnostic error : errors) {
 					try {
 						if(error instanceof AbstractDiagnostic)
 							throw new Exception("Error at line: " + error.getLine() + ": " + error.getMessage());
 						throw new Exception("Error at unspecified location: " + error.getMessage());
 					}
 					catch(Throwable t) {
 						problems.add(t);
 					}
 				}
 				// for(org.eclipse.emf.common.util.Diagnostic error : syntaxErrors) {
 				// try {
 				// if(error instanceof AbstractDiagnostic)
 				// throw new Exception("Error at line: " + ((AbstractDiagnostic) error).getLine() + ": " +
 				// error.getMessage());
 				// throw new Exception("Error at unspecified location: " + error.getMessage());
 				// }
 				// catch(Throwable t) {
 				// problems.add(t);
 				// }
 				// }
 
 				throw new MultiProblemException("There were parse errors in the file", problems);
 			}
 
 			// TODO: Use an Engine with test bindings for repositories
 			BeeModel beeModel = (BeeModel) resource.getParseResult().getRootASTElement();
 			engine = new B3BuildEngine();
 			engine.defineBeeModel(beeModel);
 			final List<Object> argv = new ArrayList<Object>();
 			argv.add(engine);
 			// ctx.defineFinalValue("${test.engine}", engine, B3BuildEngine.class);
 
 			testFunctionDescriptors = new ArrayList<TestFunctionDescriptor>();
 
 			// Define all functions and create descriptors of test functions
 			for(IFunction function : beeModel.getFunctions()) {
 				if(function instanceof IBuilder)
 					continue; // skip builders that happen to be named starting with TEST_FUNCTION_PREFIX
 
 				String functionName = function.getName();
 
 				if(functionName.length() > TEST_FUNCTION_PREFIX.length() &&
 						functionName.startsWith(TEST_FUNCTION_PREFIX) && function.getParameters().size() == 0)
 					testFunctionDescriptors.add(new TestFunctionDescriptor(functionName));
 			}
 
 			if(testFunctionDescriptors.isEmpty())
 				throw new Exception("No test functions");
 		}
 
 		@Override
 		protected void runChild(TestFunctionDescriptor child, RunNotifier notifier) {
 			Description testDescription = child.getDescription();
 
 			notifier.fireTestStarted(testDescription);
 			try {
 				engine.callFunction(child.getFunctionName(), EMPTY_PARAMETER_ARRAY, EMPTY_TYPE_ARRAY);
 			}
 			catch(Throwable t) {
 				notifier.fireTestFailure(new Failure(testDescription, t));
 			}
 			finally {
 				notifier.fireTestFinished(testDescription);
 			}
 		}
 
 	}
 
 	public static final String TEST_FUNCTION_PREFIX = "test";
 
 	protected static final Object[] EMPTY_PARAMETER_ARRAY = new Object[] {};
 
 	protected static final Type[] EMPTY_TYPE_ARRAY = new Type[] {};
 
 	protected XtextResourceSet beeLangResourceSet;
 
 	protected final Class<?> definitionClass;
 
 	protected final String containingBundleName;
 
 	protected List<Runner> b3FileRunners;
 
 	public JUnitB3FileRunnerFactory(Class<?> klass) throws InitializationError {
 		ClassLoader classLoader = klass.getClassLoader();
 
 		if(!(classLoader instanceof BundleReference))
 			throw new InitializationError("Failed to find out bundle containing class: " + klass.getName());
 
 		definitionClass = klass;
 		containingBundleName = ((BundleReference) classLoader).getBundle().getSymbolicName();
 
 		Annotation[] testClassAnnotations = klass.getAnnotations();
 
 		for(Annotation annotation : testClassAnnotations) {
 			if(annotation instanceof B3TestFiles) {
 				createB3FileRunners(((B3TestFiles) annotation).value());
 				return;
 			}
 		}
 
 		throw new InitializationError("No @" + B3TestFiles.class.getSimpleName() + " annotation specified for class: " +
 				klass.getName());
 	}
 
 	protected Runner createB3FileRunner(String b3File) throws Exception {
 		return new JUnitB3FileRunner(b3File);
 	}
 
 	protected void createB3FileRunners(String[] b3Files) {
 		createResourceSet();
 
 		ArrayList<Runner> runners = new ArrayList<Runner>(b3Files.length);
 
 		for(String b3File : b3Files) {
 			try {
 				runners.add(createB3FileRunner(b3File));
 			}
 			catch(Throwable t) {
 				runners.add(createErrorReportingRunner(b3File, t));
 			}
 		}
 
 		b3FileRunners = runners;
 	}
 
 	protected Runner createErrorReportingRunner(String b3File, Throwable t) {
 		return new ErrorReportingRunner(
 			Description.createSuiteDescription(b3File), t, "Test initialization failed for: " + b3File);
 	}
 
 	protected void createResourceSet() {
 		Injector beeLangInjector = new BeeLangStandaloneSetup().createInjectorAndDoEMFRegistration();
 
 		beeLangResourceSet = beeLangInjector.getProvider(XtextResourceSet.class).get();
 		beeLangResourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);
 	}
 
 	public List<Runner> getB3FileRunners() {
 		return b3FileRunners;
 	}
 
 }
