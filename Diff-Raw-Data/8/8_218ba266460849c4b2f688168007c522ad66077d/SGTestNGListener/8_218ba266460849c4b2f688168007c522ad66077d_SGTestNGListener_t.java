 package framework.testng;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 
 import org.testng.ITestContext;
 import org.testng.ITestNGMethod;
 import org.testng.ITestResult;
 import org.testng.Reporter;
 import org.testng.TestListenerAdapter;
 
 import framework.utils.DumpUtils;
 import framework.utils.LogUtils;
 import framework.utils.TestNGUtils;
 import framework.utils.ZipUtils;
 
 
 public class SGTestNGListener extends TestListenerAdapter {
 
     protected String testMethodName;
     protected String suiteName;
     
     @Override
     public void onStart(ITestContext iTestContext) {
    	suiteName = System.getProperty("sgtest.suiteName");
     }
 
     @Override
     public void onConfigurationSuccess(ITestResult iTestResult) {
         super.onConfigurationSuccess(iTestResult);
         String testName = iTestResult.getTestClass().getName();
         String configurationName = iTestResult.getMethod().toString().split("\\(|\\)")[0];
         if (isAfter(iTestResult)) {
         	DumpUtils.copyBeforeConfigurationsLogToTestDir(testName, suiteName);
         	testName = testMethodName;
         }
         if (suiteName == null) { // this is in case the suite has a @BeforeSuite method. which is invoked before the onStart is.
        	suiteName = System.getProperty("sgtest.suiteName");
         }
         LogUtils.log("Configuration Succeeded: " + configurationName);
         ZipUtils.unzipArchive(testMethodName, suiteName);
         write2LogFile(iTestResult, DumpUtils.createTestFolder(testName, suiteName));
     }
 
 	@Override
     public void onConfigurationFailure(ITestResult iTestResult) {
         super.onConfigurationFailure(iTestResult);
         String testName = iTestResult.getTestClass().getName();
         String configurationName = iTestResult.getMethod().toString().split("\\(|\\)")[0];
         if (isAfter(iTestResult)) {
         	DumpUtils.copyBeforeConfigurationsLogToTestDir(testName, suiteName);
         	testName = testMethodName;
         }
         if (suiteName == null) { // this is in case the suite has a @BeforeSuite method. which is invoked before the onStart is.
        	suiteName = System.getProperty("sgtest.suiteName");
         }
         LogUtils.log("Configuration Failed: " + configurationName, iTestResult.getThrowable());
         ZipUtils.unzipArchive(testMethodName, suiteName);
         write2LogFile(iTestResult, DumpUtils.createTestFolder(testName, suiteName));
     }
 	
 	@Override
     public void onConfigurationSkip(ITestResult iTestResult) {
         super.onConfigurationFailure(iTestResult);
         String testName = iTestResult.getTestClass().getName();
         String configurationName = iTestResult.getMethod().toString().split("\\(|\\)")[0];
         if (isAfter(iTestResult)) {
         	DumpUtils.copyBeforeConfigurationsLogToTestDir(testName, suiteName);
         	testName = testMethodName;
         }
         LogUtils.log("Configuration Skipped: " + configurationName, iTestResult.getThrowable());
         ZipUtils.unzipArchive(testMethodName, suiteName);
         write2LogFile(iTestResult, DumpUtils.createTestFolder(testName, suiteName));
     }
 
 
     @Override
     public void onTestStart(ITestResult iTestResult) {
     	super.onTestStart(iTestResult);
         String testName = TestNGUtils.constructTestMethodName(iTestResult);
         LogUtils.log("Test Start: " + testName);
     }
 
     @Override
     public void onTestFailure(ITestResult iTestResult) {
         super.onTestFailure(iTestResult);
         testMethodName = TestNGUtils.constructTestMethodName(iTestResult);
         LogUtils.log("Test Failed: " + testMethodName, iTestResult.getThrowable());
         write2LogFile(iTestResult, DumpUtils.createTestFolder(testMethodName, suiteName));
     }
 
     @Override
 	public void onTestSkipped(ITestResult iTestResult) {
 		super.onTestSkipped(iTestResult);
 		testMethodName = TestNGUtils.constructTestMethodName(iTestResult);
 		LogUtils.log("Test Skipped: " + testMethodName, iTestResult.getThrowable());
         write2LogFile(iTestResult, DumpUtils.createTestFolder(testMethodName, suiteName));
 	}
 
 	@Override
     public void onTestSuccess(ITestResult iTestResult) {
         super.onTestSuccess(iTestResult);
         testMethodName = TestNGUtils.constructTestMethodName(iTestResult);
         LogUtils.log("Test Passed: " + testMethodName);
         write2LogFile(iTestResult, DumpUtils.createTestFolder(testMethodName, suiteName));
     }
 
     @Override
     public void onFinish(ITestContext testContext) {
     	
         super.onFinish(testContext);
     }
 
     private void write2LogFile(ITestResult iTestResult, File testFolder) {
         try {
             if(testFolder == null){
                 LogUtils.log("Can not write to file test folder is null");
                 return;
             }
             String parameters = TestNGUtils.extractParameters(iTestResult);
             File testLogFile = new File(testFolder.getAbsolutePath() + "/" + iTestResult.getName() + "(" + parameters + ").log");
             if (!testLogFile.createNewFile()) {
                 new RuntimeException("Failed to create log file [" + testLogFile + "];\n log output: " + Reporter.getOutput());
             }
             FileWriter fstream = new FileWriter(testLogFile);
             BufferedWriter out = new BufferedWriter(fstream);
             String output = SGTestNGReporter.getOutput();
             out.write(output);
             out.close();
         } catch (Exception e) {
             new RuntimeException(e);
         } finally {
             SGTestNGReporter.reset();
         }
     }
     
     private boolean isAfter(ITestResult iTestResult) {
     	ITestNGMethod method = iTestResult.getMethod();
     	return (
     			method.isAfterClassConfiguration() || 
     			method.isAfterMethodConfiguration() || 
     			method.isAfterSuiteConfiguration() || 
     			method.isAfterTestConfiguration()
     	);
     }
 }
