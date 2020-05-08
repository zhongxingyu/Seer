 package org.hackystat.sensor.eclipse.addon;
 
 import java.net.URI;
 import java.util.Date;
import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.Map;
 
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

 import org.eclipse.jdt.junit.ITestRunListener;
 import org.hackystat.sensor.eclipse.EclipseSensor;
 
 
 /**
  * Provides a JUnit listener implementing class to get JUnit result information. 
  * A client must add this implementing class to the JUnitPlugin class in such a way that:
  * 
  * Note that this class extend the extension point defined in the JUnit plugin.
  *
  * @author Hongbing Kou 
  */
 public class TestRunnerSensor implements ITestRunListener {
   /** The EclipseSensor. */
   private EclipseSensor sensor;
   
   /** Run time. */
  private XMLGregorianCalendar runTime;
   /** Number of tests in one test run. */
   private int testCount;
   /** Indice of the test in one test run. */
   private int testIndice;
   
   /** The starting time of each test case. */
   private Date startTime;
   /** The failure stack trace message. */
   private String failureMessage;
   /** The error stack trace message. */
   private String errorMessage;
  
   /**
    * Constructor for the EclipseJUnitListener object. Adds this lister instance to the
    * <code>org.eclipse.jdt.internal.junit.ui.JUnitPlugin</code> instance.
    *
    */
   public TestRunnerSensor() {
     this(EclipseSensor.getInstance());
   }
   
   /**
    * Constructor for the EclipseJUnitListener object. Adds this lister instance to the
    * <code>org.eclipse.jdt.internal.junit.ui.JUnitPlugin</code> instance.
    *
    * @param sensor The EclipseSensor instance.
    */
   public TestRunnerSensor(EclipseSensor sensor) {
     this.sensor = sensor;
     init();
   }
 
   /**
    * Initializes all the instance fields related to the fields in the JUnitResource instance.
    */
   private void init() {
     this.failureMessage = "";
     this.errorMessage = "";
   }
 
   /**
    * A single test was reran.
    *
    * @param testClass the name of the test class.
    * @param testName the name of the test.
    * @param status the status of the run
    * @param trace the stack trace in the case of a failure.
    *
    */
   public void testReran(String testClass, String testName, int status, String trace) {
   }
 
   /**
    * A test run has started.
    *
    * @param testCount the number of tests that will be run.
    *
    * @see org.eclipse.jdt.junit.ITestRunListener#testRunStarted(int)
    */
   public void testRunStarted(int testCount) {
    this.runTime = makeTimestamp();
     this.testCount = testCount;
     this.testIndice = 1;
   }
   
  /**
   * Converts the specified time in milliseconds into a
   * javax.xml.datatype.XMLGregorianCalendar.
   * @param timeInMillis the specified time in milliseconds to convert.
   * @return A new instance of a javax.xml.datatype.XmlGregorianCalendar
   */
  private XMLGregorianCalendar makeTimestamp() {
    DatatypeFactory factory = null;
    try {
      factory = DatatypeFactory.newInstance();
      GregorianCalendar calendar = new GregorianCalendar();
      calendar.setTimeInMillis(new Date().getTime());
      return factory.newXMLGregorianCalendar(calendar);
    }
    catch (DatatypeConfigurationException e) {
      throw new RuntimeException("Can not get date of XMLGregorianCalendar", e);
    }
  }
  
    /**
    * A test run ended.
    *
    * @param elapsedTime the elapsed time of the test run.
    *
    * @see org.eclipse.jdt.junit.ITestRunListener#testRunEnded(long)
    */
   public void testRunEnded(long elapsedTime) {
   }
 
   /**
    * A test run was stopped before it ended.
    *
    * @param elapsedTime the elapsed time of the test run.
    *
    * @see org.eclipse.jdt.junit.ITestRunListener#testRunStopped(long)
    */
   public void testRunStopped(long elapsedTime) {
   }
 
   /**
    * The test runner VM has terminated.
    *
    * @see org.eclipse.jdt.junit.ITestRunListener#testRunTerminated()
    */
   public void testRunTerminated() {
   }
 
   /**
    * Gets the fully qualified package class name from the testName string. For example, returns
    * "org.hackystat.sensor.eclipse.junit.EclipseJUnitListener".
    *
    * @param testName The test name string to be passed.
    *
    * @return The fully qualified package class name.
    */
   private String getTestNameOf(String testName) {
     int startIndex = testName.indexOf('(');
     int endIndex = testName.indexOf(')');
 
     // There is no test case
     if (startIndex == -1 || endIndex == -1) {
       return "";
     }
     
     return testName.substring(startIndex + 1, endIndex);
   }
 
   /**
    * Gets the test case name (method name) from the testName string. For example, returns
    * "testMethod".
    *
    * @param test The test name string to be passed.
    *
    * @return The test case name (method name).
    */
   private String getTestCaseOf(String test) {
     int endIndex = test.indexOf('(');
     
     // There is no test but test case name.
     if (endIndex == -1) {
       return test;
     }
     
     return test.substring(0, endIndex);
   }
 
   /**
    * An individual test has started. Supports the method for the 2.1 stream from RC2.
    *
    * @param testId a unique Id identifying the test
    * @param test the name of the test that started
    *
    * @see org.eclipse.jdt.junit.ITestRunListener#testStarted(java.lang.String, java.lang.String)
    * @since 2.1 RC2
    **/
   public void testStarted(String testId, String test) {
     this.startTime = new Date();
     init();
   }
 
   /**
    * An individual test has ended. Supports the method for the 2.1 stream from RC2.
    *
    * @param testId a unique Id identifying the test
    * @param test the name of the test that ended
    *
    * @see org.eclipse.jdt.junit.ITestRunListener#testEnded(java.lang.String, java.lang.String)
    * @since 2.1 RC2
    **/
   public void testEnded(String testId, String test) {
     Date endTime = new Date();
     String testName = getTestNameOf(test);
     String testCase = getTestCaseOf(test);
     
     String name = testName + "." + testCase;
     // Incase there is compilation error so that there is not test name
     if ("".equals(testName)) {
       name = testCase;
     }
     
     // Run test
     Map<String, String> keyValueMap = new HashMap<String, String>();
    keyValueMap.put("Runtime", String.valueOf(this.runTime.toString()));
 
     // Elapsed time
     long elapsedTime = endTime.getTime() - this.startTime.getTime();
     elapsedTime = (elapsedTime >= 0) ? elapsedTime : 0;
     keyValueMap.put("ElapsedTime", String.valueOf(elapsedTime));
     
     String result = "pass";
     if (this.failureMessage.length() > 0) {
       result = "fail";
       keyValueMap.put("FailureString", this.failureMessage);      
     }
     else if (this.errorMessage.length() > 0) {
       result = "fail";
       keyValueMap.put("ErrorString" , this.errorMessage);      
     }
     keyValueMap.put("TestCase", testCase); // Test case
     keyValueMap.put("TestName", testName); // Test name
     
     keyValueMap.put("TestCount", String.valueOf(this.testCount));
     keyValueMap.put("TestIndice", String.valueOf(this.testIndice));
 
     URI testFileResource = this.sensor.getObjectFile(testName);
     if (testFileResource == null) {
       testFileResource = this.sensor.getActiveFile();
     }
     
     // Increment indice number by one. 
     this.testIndice++;
     // Process UnitTest
     this.sensor.addDevEvent("Test", testFileResource, keyValueMap, name + " : " + result);
   }
 
   /**
    * An individual test has failed with a stack trace.
    * Supports the method for the 2.1 stream from RC2.
    *
    * @param testId a unique Id identifying the test
    * @param test the name of the test that failed
    * @param status the outcome of the test; one of
    * {@link #STATUS_ERROR STATUS_ERROR} or
    * {@link #STATUS_FAILURE STATUS_FAILURE}
    * @param trace the stack trace
    *
    * @see org.eclipse.jdt.junit.ITestRunListener#testFailed(int, java.lang.String,
    * java.lang.String, java.lang.String)
    * @since 2.1 RC2
    */
   public void testFailed(int status, String testId, String test, String trace) {
     if (status == ITestRunListener.STATUS_FAILURE) {
       this.failureMessage = trace;
     }
     else if (status == ITestRunListener.STATUS_ERROR) {
       this.errorMessage = trace;
     }
   }
 
   /**
    * An individual test has been rerun.
    * Supports the method for the 2.1 stream from RC2.
    *
    * @param testId a unique Id identifying the test
    * @param testClass the name of the test class that was rerun
    * @param test the name of the test that was rerun
    * @param status the outcome of the test that was rerun; one of
    * {@link #STATUS_OK STATUS_OK}, {@link #STATUS_ERROR STATUS_ERROR},
    * or {@link #STATUS_FAILURE STATUS_FAILURE}
    * @param trace the stack trace in the case of abnormal termination,
    * or the empty string if none
    *
    * @see org.eclipse.jdt.junit.ITestRunListener#testReran(java.lang.String, java.lang.String,
    *  java.lang.String, int, java.lang.String)
    * @since 2.1 RC2
    */
   public void testReran(String testId, String testClass,
                         String test, int status, String trace) {
     testReran(testClass, test, status, trace);
   }
 }
