 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import logs.Log;
 import serviceLayer.Services;
 import tests.Inventory;
 import tests.SingleTest;
 import xml.XmlWriter;
 import annotations.Test;
 
 public class ClassTester {
 
 	static int intPassed = 0;
 	static int intFailed = 0;
 	static Log myLog;
 	static Inventory testInventory = new Inventory();
 	static boolean boolXML = false;
 
 	protected static void createTestsConfiguration(String className) {
 		createTestsConfiguration(className, "", false);
 	}
 
 	protected static void createTestsConfiguration(String className,
 			String fileName, boolean XMLEncoding) {
 
 		myLog = Services.getOutputMethod(className, fileName);
 		boolXML = XMLEncoding;
 		loadTests(className);
 	}
 
 	protected static void lauchTests() throws IllegalArgumentException,
 			SecurityException, InvocationTargetException,
 			NoSuchMethodException, ClassNotFoundException 
 	{
 		XmlWriter xmlWriter = new XmlWriter();
 		long RunTestsSuiteStartTime = System.currentTimeMillis();
 		long RunTestsSuiteStopTime = 0l;
 		long SetupStartTime = 0l;
 		long SetupStopTime = 0l;
 		long TestStartTime = 0l;
 		long TestStopTime = 0l;
 		long TearDownStartTime = 0l;
 		long TearDownStopTime = 0l;
 		
 		xmlWriter.StartXMLEntry();
 
 		for (SingleTest s : testInventory) 
 		{
 			try 
 			{
 				SetupStartTime = System.currentTimeMillis();
 				Services.ExecuteMethod(s.getClassName(), Class.forName(
 						s.getClassName()).getMethod("setup"));
 				SetupStopTime = System.currentTimeMillis();
 				
 				TestStartTime = System.currentTimeMillis();
 				Services.ExecuteMethod(s.getClassName(), s.getMethod());
 				TestStopTime = System.currentTimeMillis();
 
 				s.setTestResultMessage("Success");
 				intPassed++;
 
 			} catch (Throwable ex) {
 				TestStopTime = System.currentTimeMillis();
 				s.setTestResultMessage(ex.getCause().toString());
 				intFailed++;
 
 			} finally {
 				TearDownStartTime = System.currentTimeMillis();
 				Services.ExecuteMethod(s.getClassName(), Class.forName(
 						s.getClassName()).getMethod("tearDown"));
 				TearDownStopTime = System.currentTimeMillis();
				xmlWriter.AddXMLTest(s.getMethodName(),
 						SetupStopTime - SetupStartTime,
 						TestStopTime - TestStartTime,
 						TearDownStopTime - TearDownStartTime);
 
 			}
 
 		}
		RunTestsSuiteStopTime = System.currentTimeMillis();
		xmlWriter.FinishXMLEntry(RunTestsSuiteStopTime - RunTestsSuiteStartTime);
 
 		displayResults();
 
 	}
 
 	protected static void loadTests(String className) {
 
 		try {
 
 			for (Method methodToTest : Class.forName(className).getMethods()) {
 
 				if (methodToTest.isAnnotationPresent(Test.class)
 						&& methodToTest.getName() != "setup"
 						&& methodToTest.getName() != "tearDown") {
 
 					SingleTest aTest = new SingleTest(Class.forName(className)
 							.toString().substring(6), methodToTest);
 
 					testInventory.add(aTest);
 				}
 
 			}
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 	protected static void addTestsSuite(String className) {
 		loadTests(className);
 	}
 
 	private static void displayResults() {
 
 		if (boolXML) {
 			myLog.log("<ListOfTests>");
 		}
 
 		for (SingleTest s : testInventory) {
 
 			if (boolXML) {
 
 				myLog.log("<classname>" + s.getClassName() + "</classname>");
 				myLog.log("<method>" + s.getMethodName() + "</method>");
 				myLog.log("<result>" + s.getTestResultMessage() + "</result>");
 
 			} else {
 
 				myLog.log(s.getClassName() + "." + s.getMethodName());
 				myLog.log("Result:" + s.getTestResultMessage());
 			}
 
 		}
 
 		if (boolXML) {
 
 			myLog.log("<GlobalResults>");
 			myLog.log("<Passed>" + intPassed + "</Passed>");
 			myLog.log("<Failed>" + intFailed + "</Failed>");
 			myLog.log("</GlobalResults>");
 			myLog.log("</ListOfTests>");
 
 		} else {
 
 			myLog.skipline();
 			myLog.log("**** Passed:" + intPassed + " Failed " + intFailed);
 		}
 
 		myLog.close();
 
 	}
 
 }
