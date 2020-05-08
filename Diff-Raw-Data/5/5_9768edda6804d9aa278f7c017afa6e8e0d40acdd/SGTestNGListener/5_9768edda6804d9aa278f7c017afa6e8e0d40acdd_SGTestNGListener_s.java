 package iTests.framework.testng.report;
 
 import iTests.framework.tools.SGTestHelper;
 import iTests.framework.utils.DeploymentUtils;
 import iTests.framework.utils.DumpUtils;
 import iTests.framework.utils.IOUtils;
 import iTests.framework.utils.LogUtils;
 import iTests.framework.utils.ZipUtils;
 import iTests.framework.utils.TestNGUtils;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.vfs2.FileObject;
 import org.apache.commons.vfs2.FileSystemException;
 import org.apache.commons.vfs2.FileSystemManager;
 import org.apache.commons.vfs2.VFS;
 import org.apache.commons.vfs2.impl.DefaultFileMonitor;
 import org.jruby.embed.ScriptingContainer;
 import org.testng.*;
 
 import java.io.*;
 import java.util.concurrent.TimeUnit;
 
 
 public class SGTestNGListener extends TestListenerAdapter {
 
     protected String testMethodName;
     protected String suiteName;
     protected String buildNumber;
     protected String version;
     protected ScriptingContainer container;
     private Process process = null;
     private Process process2 = null;
     private String confFilePath;
     private String confFilePath2;
     private String backupFilePath;
     private String backupFilePath2;
     private String logstashLogPath;
     private String logstashLogPath2;
     private static final boolean enableLogstash = Boolean.parseBoolean(System.getProperty("iTests.enableLogstash"));
 
     @Override
     public void onStart(ITestContext iTestContext) {
     	suiteName = System.getProperty("iTests.suiteName");
         LogUtils.log("suite number is now (on start) - " + buildNumber);
 
         if(enableLogstash){
             buildNumber = System.getProperty("iTests.buildNumber");
             LogUtils.log("build number is now (on start) - " + buildNumber);
             version = System.getProperty("cloudifyVersion");
         }
     }
 
     private void initLogstash2(ITestResult tr) {
 
         String simpleTestName = tr.getTestClass().getRealClass().getSimpleName();
         String pathToLogstash = SGTestHelper.getSGTestRootDir().replace("\\", "/") + "/src/main/resources/logstash";
         confFilePath2 = pathToLogstash + "/logstash-shipper-client-2.conf";
 
         if(process2 == null && !isAfter(tr)){
 
             try {
                 backupFilePath2 = IOUtils.backupFile(confFilePath2);
                 IOUtils.replaceTextInFile(confFilePath2, "<path_to_build>", SGTestHelper.getBuildDir());
                 IOUtils.replaceTextInFile(confFilePath2, "<path_to_test_class_folder>", SGTestHelper.getSGTestRootDir().replace("\\", "/") + "/../" + suiteName + "/" + tr.getTestClass().getName());
                 IOUtils.replaceTextInFile(confFilePath2, "<suite_name>", suiteName);
                 IOUtils.replaceTextInFile(confFilePath2, "<test_name>", simpleTestName);
                 IOUtils.replaceTextInFile(confFilePath2, "<build_number>", buildNumber);
                 IOUtils.replaceTextInFile(confFilePath2, "<version>", version);
 
 
                String logstashJarPath = DeploymentUtils.getLocalRepository() + "repository/net/logstash/1.1.13/logstash-1.1.13.jar";
                 logstashLogPath2 = pathToLogstash + "/logstash-" + simpleTestName + "-2.txt";
                 String cmdLine = "java -jar " + logstashJarPath + " agent -f " + confFilePath2 + " -l " + logstashLogPath2;
 
                 final String[] parts = cmdLine.split(" ");
                 final ProcessBuilder pb = new ProcessBuilder(parts);
                 LogUtils.log("Executing Command line: " + cmdLine);
 
                 process2 = pb.start();
 
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
 
     private void initLogstash(String testName) {
         String pathToLogstash = SGTestHelper.getSGTestRootDir().replace("\\", "/") + "/src/main/resources/logstash";
         confFilePath = pathToLogstash + "/logstash-shipper-client.conf";
 
         if(process == null){
 
             try {
                 backupFilePath = IOUtils.backupFile(confFilePath);
                 IOUtils.replaceTextInFile(confFilePath, "<path_to_test_folder>", SGTestHelper.getSGTestRootDir().replace("\\", "/") + "/../" + suiteName + "/" + testName);
                 IOUtils.replaceTextInFile(confFilePath, "<suite_name>", suiteName);
                 IOUtils.replaceTextInFile(confFilePath, "<test_name>", testName);
                 IOUtils.replaceTextInFile(confFilePath, "<build_number>", buildNumber);
                 IOUtils.replaceTextInFile(confFilePath, "<version>", version);
 
                String logstashJarPath = DeploymentUtils.getLocalRepository() + "repository/net/logstash/1.1.13/logstash-1.1.13.jar";
                 logstashLogPath = pathToLogstash + "/logstash-" + testName + ".txt";
                 String cmdLine = "java -jar " + logstashJarPath + " agent -f " + confFilePath + " -l " + logstashLogPath;
 
                 final String[] parts = cmdLine.split(" ");
                 final ProcessBuilder pb = new ProcessBuilder(parts);
                 LogUtils.log("Executing Command line: " + cmdLine);
 
                 TimeUnit.SECONDS.sleep(1);
                 process = pb.start();
 
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     }
 
     @Override
     public void beforeConfiguration(ITestResult tr) {
         if(enableLogstash){
             super.beforeConfiguration(tr);
             if (suiteName == null) { // this is in case the suite has a @BeforeSuite method. which is invoked before the onStart is.
                 suiteName = System.getProperty("iTests.suiteName");
                 buildNumber = System.getProperty("iTests.buildNumber");
                 LogUtils.log("build number is now - " + buildNumber);
                 version = System.getProperty("cloudifyVersion");
             }
 
             initLogstash2(tr);
         }
     }
 
     @Override
     public void onConfigurationSuccess(ITestResult iTestResult) {
         super.onConfigurationSuccess(iTestResult);
         String testName = iTestResult.getTestClass().getName();
         String configurationName = iTestResult.getMethod().toString().split("\\(|\\)")[0];
         if (isAfter(iTestResult) && !enableLogstash) {
         	DumpUtils.copyBeforeConfigurationsLogToTestDir(testName, suiteName);
         	testName = testMethodName;
         }
         if (suiteName == null) { // this is in case the suite has a @BeforeSuite method. which is invoked before the onStart is.
         	suiteName = System.getProperty("iTests.suiteName");
         }
         LogUtils.log("Configuration Succeeded: " + configurationName);
         ZipUtils.unzipArchive(testMethodName, suiteName);
 
         if (enableLogstash && isAfter(iTestResult) && !iTestResult.getMethod().isAfterClassConfiguration() && !iTestResult.getMethod().isAfterSuiteConfiguration()) {
             testName = testMethodName;
         }
 
         write2LogFile(iTestResult, DumpUtils.createTestFolder(testName, suiteName));
 
         if (isAfter(iTestResult)) {
             DumpUtils.copyBeforeConfigurationsLogToTestDir(testName, suiteName);
             if(enableLogstash){
                 if(process != null){
                     killLogstashAgent(1, logstashLogPath);
                 }
                 if(process2 != null && iTestResult.getMethod().isAfterClassConfiguration()){
                     killLogstashAgent(2, logstashLogPath2);
                 }
             }
         }
     }
 
 	@Override
     public void onConfigurationFailure(ITestResult iTestResult) {
         super.onConfigurationFailure(iTestResult);
         String testName = iTestResult.getTestClass().getName();
         String configurationName = iTestResult.getMethod().toString().split("\\(|\\)")[0];
         if (!enableLogstash && isAfter(iTestResult)) {
         	DumpUtils.copyBeforeConfigurationsLogToTestDir(testName, suiteName);
         	testName = testMethodName;
         }
         if (suiteName == null) { // this is in case the suite has a @BeforeSuite method. which is invoked before the onStart is.
         	suiteName = System.getProperty("iTests.suiteName");
         }
         LogUtils.log("Configuration Failed: " + configurationName, iTestResult.getThrowable());
         ZipUtils.unzipArchive(testMethodName, suiteName);
 
         if (enableLogstash && isAfter(iTestResult) && !iTestResult.getMethod().isAfterClassConfiguration() && !iTestResult.getMethod().isAfterSuiteConfiguration()) {
             testName = testMethodName;
         }
         write2LogFile(iTestResult, DumpUtils.createTestFolder(testName, suiteName));
 
         if (isAfter(iTestResult)) {
             DumpUtils.copyBeforeConfigurationsLogToTestDir(testName, suiteName);
             if(enableLogstash){
                 if(process != null){
                     killLogstashAgent(1, logstashLogPath);
                 }
                 if(process2 != null && iTestResult.getMethod().isAfterClassConfiguration()){
                     killLogstashAgent(2, logstashLogPath2);
                 }
             }
         }
     }
 	
 	@Override
     public void onConfigurationSkip(ITestResult iTestResult) {
         super.onConfigurationFailure(iTestResult);
         String testName = iTestResult.getTestClass().getName();
         String configurationName = iTestResult.getMethod().toString().split("\\(|\\)")[0];
         if (!enableLogstash && isAfter(iTestResult)) {
         	DumpUtils.copyBeforeConfigurationsLogToTestDir(testName, suiteName);
         	testName = testMethodName;
         }
         LogUtils.log("Configuration Skipped: " + configurationName, iTestResult.getThrowable());
         ZipUtils.unzipArchive(testMethodName, suiteName);
 
         if (enableLogstash && isAfter(iTestResult) && !iTestResult.getMethod().isAfterClassConfiguration() && !iTestResult.getMethod().isAfterSuiteConfiguration()) {
             testName = testMethodName;
         }
         write2LogFile(iTestResult, DumpUtils.createTestFolder(testName, suiteName));
 
         if (isAfter(iTestResult)) {
             DumpUtils.copyBeforeConfigurationsLogToTestDir(testName, suiteName);
             if(enableLogstash){
                 if(process != null){
                     killLogstashAgent(1, logstashLogPath);
                 }
                 if(process2 != null && iTestResult.getMethod().isAfterClassConfiguration()){
                     killLogstashAgent(2, logstashLogPath2);
                 }
             }
         }
     }
 
 
     @Override
     public void onTestStart(ITestResult iTestResult) {
     	super.onTestStart(iTestResult);
         String testName = TestNGUtils.constructTestMethodName(iTestResult);
         LogUtils.log("Test Start: " + testName);
         if(enableLogstash){
             initLogstash(testName);
         }
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
 
     private void killLogstashAgent(int logAgentNumber, String logstashLogPath) {
 
         LogUtils.log("logstash, kill yourself");
 
         FileObject listendir;
         CustomFileListener listener = new CustomFileListener();
         long TIMEOUT_BETWEEN_FILE_QUERYING = 1000;
         long LOOP_TIMEOUT_IN_MILLIS = 20 * 1000;
 
         try {
             FileSystemManager fileSystemManager = VFS.getManager();
             listendir = fileSystemManager.resolveFile(logstashLogPath);
         } catch (FileSystemException e) {
             e.printStackTrace();
             return;
         }
 
         DefaultFileMonitor fm = new DefaultFileMonitor(listener);
         fm.setRecursive(true);
         fm.addFile(listendir);
         fm.setDelay(TIMEOUT_BETWEEN_FILE_QUERYING);
         fm.start();
 
         LogUtils.log("waiting to destroy logger");
 
         while(true){
 
             if(!listener.isProcessUp()){
                 break;
             }
 
             listener.setProcessUp(false);
 
             try {
                 TimeUnit.MILLISECONDS.sleep(LOOP_TIMEOUT_IN_MILLIS);
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
 
         }
 
         LogUtils.log("destroying logstash agent " + logAgentNumber);
 
         fm.stop();
 
         File logstashOutputFile = new File(logstashLogPath);
 
         if(logAgentNumber == 1){
 
             process.destroy();
             process = null;
         }
         else{
 
             process2.destroy();
             process2 = null;
         }
 
         try {
             TimeUnit.SECONDS.sleep(5);
 
             LogUtils.log("returning logstash config file to initial state");
             if(logAgentNumber == 1){
                 IOUtils.replaceFileWithMove(new File(confFilePath), new File(backupFilePath));
             }
             else{
                 IOUtils.replaceFileWithMove(new File(confFilePath2), new File(backupFilePath2));
             }
         } catch (IOException e) {
             e.printStackTrace();
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
 
         if(logstashOutputFile.exists()){
             FileUtils.deleteQuietly(logstashOutputFile);
         }
     }
 
 //    public static void main(String[] args) throws IOException {
 //
 //        WikiReporter.getTestLogstashLogs("100-101", "Ec2ExamplesTest", null);
 //
 //    }
 
 }
