 /*
  * Created on Jul 19, 2006
  */
 package edu.duke.cabig.catrip.test.report;
 
 import java.io.File;
 import java.io.IOException;
 
 import edu.duke.cabig.catrip.test.report.data.TestCase;
 import edu.duke.cabig.catrip.test.report.data.TestSuite;
 import junit.textui.TestRunner;
 
 /**
  * This is a unit test for testing the JUnitDoclet class, which is used do attach JavaDoc comments
  * to JUnit test results.
  * @testType unit
  * @author MCCON012
  */
 public class JUnitDocletTest
 	extends junit.framework.TestCase
 {
 	public JUnitDocletTest(String name)
 	{
 		super(name);
 	}
 	
 	/**
 	 * This test case tests the doclet functionality of adding class docs and method docs to test results.
 	 * @throws IOException
 	 */
 	public void testDoclet() 
 		throws IOException
 	{
 		TestSuite suite = new TestSuite();
 		suite.name = "edu.duke.cabig.catrip.test.report.JUnitDocletTest";
 		TestCase test = new TestCase();
 		test.className = "edu.duke.cabig.catrip.test.report.JUnitDocletTest";
 		test.name = "testDoclet";
 		suite.testCases.add(test);
 		
 		JUnitDoclet.addDocs(
 			new File[] { 
 				new File("src" + File.separator + "java"), 
 				new File("test" + File.separator + "src" + File.separator + "java"),
 			}, 
 			new TestSuite[] { suite }
 		);
 		
 		assertEquals(
 			"This is a unit test for testing the JUnitDoclet class, which is used do attach JavaDoc comments\n to JUnit test results.",
 			suite.docText
 		);
 		assertEquals(
 			"This test case tests the doclet functionality of adding class docs and method docs to test results.",
 			test.docText
 		);
 		
 		assertEquals("unit", suite.docTags.getProperty("testType"));
 		assertEquals(null, test.docTags.getProperty("testType"));
 	}
 	
 	/**
 	 * This test case tests the doclet functionality of adding class docs and method docs to test results from a Haste-based test.
 	 * @throws IOException
 	 */
 	public void testHaste() 
 		throws IOException
 	{
 		TestSuite suite = new TestSuite();
 		suite.name = "edu.duke.cabig.catrip.test.system.JUnitDocReportTest";
 		
 		File projectDir = new File("..", "test-system");
 		JUnitDoclet.addDocs(
 			new File[] { 
 				new File(projectDir, "src" + File.separator + "java"), 
 				new File(projectDir, "test" + File.separator + "src" + File.separator + "java"),
 			}, 
 			new TestSuite[] { suite }
 		);
 		
 		assertEquals("integration", suite.docTags.getProperty("testType"));
 		assertEquals(0, suite.tests);
 		assertEquals(0, suite.testCases.size());
 		assertEquals(3, suite.testSteps.size());
		assertEquals("JUnitDocReportCreateAntFileStep", suite.testSteps.get(0).name);
		assertEquals("JUnitDocReportRunStep", suite.testSteps.get(1).name);
		assertEquals("JUnitDocReportCheckStep", suite.testSteps.get(2).name);
 	}
 	
 	/**
 	 * Run the tests
 	 * @param args ignored
 	 */
 	public static void main(String[] args)
 	{
 		TestRunner runner = new TestRunner();
 		junit.framework.TestResult result = runner.doRun(new junit.framework.TestSuite(JUnitDocletTest.class));
 		System.exit(result.errorCount() + result.failureCount());
 	}
 }
