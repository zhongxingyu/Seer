 package com.mutation.runner;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import com.mutation.runner.IClassSetResolver.ClassDescription;
 import com.mutation.runner.events.IEvent;
 import com.mutation.runner.events.IEventListener;
 import com.mutation.runner.events.ProcessingClassUnderTest;
 import com.mutation.runner.events.ProcessingClassUnderTestFinished;
 import com.mutation.runner.events.ProcessingMutant;
 import com.mutation.runner.events.ProcessingMutationOperator;
 import com.mutation.runner.utililties.ByteCodeFileReader;
 import com.mutation.testrunner.JUnitRunner;
 
 public class MutationRunner {
 
 	private ITestRunner testRunner;
 
 	private IMutantGenerator mutantGenerator;
 
 	private File originalClassPath;
 
 	private IEventListener eventListener;
 
 	/**
 	 * 
 	 * Integration interface method for direct access using complex datatypes.
 	 * 
 	 * @param operators
 	 * @param byteCodeFileReader
 	 * @param classUnderTestDescription
 	 * @param testNames
 	 * @throws IOException
 	 */
 	public void performMutations(List<EMutationOperator> operators, ByteCodeFileReader byteCodeFileReader,
 			ClassDescription classUnderTestDescription, Set<String> testNames) throws IOException {
 
 		eventListener.notifyEvent(new ProcessingClassUnderTest(classUnderTestDescription));
 
 		String fqClassName;
 
 		if (classUnderTestDescription.getPackageName() == null
 				|| classUnderTestDescription.getPackageName().trim().equals("")) {
 			fqClassName = classUnderTestDescription.getClassName();
 		} else {
 			fqClassName = classUnderTestDescription.getPackageName() + "." + classUnderTestDescription.getClassName();
 		}
 
 		byte[] byteCode = loadClass(byteCodeFileReader, fqClassName);
 
 		for (EMutationOperator operator : operators) {
 
 			eventListener.notifyEvent(new ProcessingMutationOperator(operator.name()));
 
			List<Mutant> mutants = mutantGenerator.generateMutants(classUnderTestDescription.getPackageName() + "."
					+ classUnderTestDescription.className, byteCode, operator, eventListener);
 
 			for (Mutant mutant : mutants) {
 
 				eventListener.notifyEvent(new ProcessingMutant(mutant));
 
 				testRunner.execute(mutant, testNames, eventListener);
 			}
 		}
 
 		eventListener.notifyEvent(new ProcessingClassUnderTestFinished());
 
 	}
 
 	/**
 	 * 
 	 * Integration interface method for external access using simple datatypes.
 	 * 
 	 * @param operators
 	 * @param classUnderTest
 	 * @param classUnderTestFile
 	 * @param testNames
 	 * @throws IOException
 	 */
 	private void performMutations(List<String> operators, String classUnderTest, String classUnderTestFile,
 			Set<String> testNames) throws IOException {
 
 		this.eventListener = new IEventListener() {
 
 			public void notifyEvent(IEvent event) {
 			};
 		};
 
 		this.mutantGenerator = new com.mutation.transform.bcel.MutantGenerator();
 
 		List<EMutationOperator> convertedOperators = new ArrayList<EMutationOperator>();
 		convertedOperators.add(EMutationOperator.ROR);
 
 		ClassDescription classUnderTestDescription = new ClassDescription();
 		classUnderTestDescription.setClassFile(classUnderTestFile);
 
 		int lastDotIndex = classUnderTest.lastIndexOf(".");
 
 		if (lastDotIndex > 0) {
 			classUnderTestDescription.setPackageName(classUnderTest.substring(0, lastDotIndex - 1));
 			classUnderTestDescription.setClassName(classUnderTest.substring(lastDotIndex));
 		} else {
 			classUnderTestDescription.setPackageName("");
 			classUnderTestDescription.setClassName(classUnderTest);
 
 		}
 
 		performMutations(convertedOperators, new ByteCodeFileReader(), classUnderTestDescription, testNames);
 
 	}
 
 	/**
 	 * Public main method for integration via creation of a new JVM (e.g. from
 	 * eclipse).
 	 * 
 	 * @param argv
 	 */
 	public static void main(String[] argv) {
 
 		JUnitRunner testRunner = new JUnitRunner();
 		testRunner.setStopOnFirstFailedTest(false);
 		try {
 			testRunner.setTestClassesLocations(new URL[] { new URL(
 					"file:/Users/raua/Documents/workspace/SampleProjectUnderTest/target/test-classes/") });
 		} catch (MalformedURLException e1) {
 			e1.printStackTrace();
 			return;
 		}
 
 		MutationRunner runner = new MutationRunner();
 		runner.setTestRunner(testRunner);
 
 		List<String> operators = new ArrayList<String>();
 		operators.add(EMutationOperator.ROR.name());
 
 		String classUnderTest = "com.mutation.test.Sample";
 
 		String classUnderTestFile = "com/mutation/test/Sample.class";
 
 		Set<String> testNames = new HashSet<String>();
 		testNames.add("com.mutation.test.SampleTestCase");
 
 		runner.setOriginalClassPath(new File("../../../SampleProjectUnderTest/target/classes"));
 
 		try {
 			runner.performMutations(operators, classUnderTest, classUnderTestFile, testNames);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private byte[] loadClass(ByteCodeFileReader byteCodeFileReader, String classUnderTestName) throws IOException {
 
 		String path = originalClassPath.getAbsolutePath() + "/" + classUnderTestName.replace('.', '/') + ".class";
 
 		File originalClassFile = new File(path);
 
 		return byteCodeFileReader.readByteCodeFromDisk(originalClassFile);
 	}
 
 	public void setMutantGenerator(IMutantGenerator mutantGenerator) {
 		this.mutantGenerator = mutantGenerator;
 	}
 
 	public void setTestRunner(ITestRunner testRunner) {
 		this.testRunner = testRunner;
 	}
 
 	public File getOriginalClassPath() {
 		return originalClassPath;
 	}
 
 	public void setOriginalClassPath(File originalClassPath) {
 		this.originalClassPath = originalClassPath;
 	}
 
 	public void setEventListener(IEventListener eventListener) {
 		this.eventListener = eventListener;
 	}
 
 }
