 
 package org.easetech.easytest.runner;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import org.easetech.easytest.exceptions.ParamAssertionError;
 import org.easetech.easytest.internal.EasyAssignments;
 import org.easetech.easytest.loader.Loader;
 import org.easetech.easytest.reports.data.TestMethodDuration;
 import org.easetech.easytest.reports.data.TestResultBean;
 import org.easetech.easytest.util.CommonUtils;
 import org.junit.Assert;
 import org.junit.experimental.theories.PotentialAssignment;
 import org.junit.experimental.theories.internal.Assignments;
 import org.junit.internal.AssumptionViolatedException;
 import org.junit.internal.runners.model.EachTestNotifier;
 import org.junit.runner.notification.RunNotifier;
 import org.junit.runners.model.FrameworkMethod;
 import org.junit.runners.model.Statement;
 import org.junit.runners.model.TestClass;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * An internal class that holds the logic of running a given Test method. This class contains the common code for both
  * {@link DataDrivenTestRunner} and SpringTestRunner that is present in the easytest-spring module.
  * 
  * @author Anuj Kumar
  * 
  */
 public class InternalParameterizedStatement extends Statement {
 
     /**
      * An instance of logger associated with the test framework.
      */
     protected final Logger LOG = LoggerFactory.getLogger(InternalParameterizedStatement.class);
 
 
     /**
      * an instance of {@link FrameworkMethod} identifying the method to be tested.
      */
     private EasyFrameworkMethod fTestMethod;
 
     /**
      * A List of {@link Assignments}. Each member in the list corresponds to a single set of test data to be passed to
      * the test method. For eg. If the user has specified the test data in the CSV file as:<br>
      * <br>
      * <B>testGetItems,LibraryId,itemType,searchText</B> <br>
      * ,4,journal,batman <br>
      * ,1,ebook,potter <br>
      * where: <li>testGetItems is the name of the method</li> <li>
      * LibraryId,itemType,searchText are the names of the parameters that the test method expects</li> and <li>
      * ,4,journal,batman</li> <li>,1,ebook,potter</li> are the actual test data <br>
      * then this list will consists of TWO {@link Assignments} instances with values: <li>[[{LibraryId=4,
      * itemType=journal, searchText=batman}]]</li> AND <li>[[{LibraryId=1, itemType=ebook, searchText=potter}]]
      * 
      */
     private List<EasyAssignments> listOfAssignments;
 
     /**
      * List of Invalid parameters
      */
     private List<AssumptionViolatedException> fInvalidParameters = new ArrayList<AssumptionViolatedException>();
 
     /**
      * An instance of {@link TestClass} identifying the class under test
      */
     private TestClass fTestClass;
 
     /**
      * The actual instance of the test class. This is extremely handy in cases where we want to reflectively set
      * instance fields on a test class.
      */
     private Object testInstance;
 
     public InternalParameterizedStatement(EasyFrameworkMethod fTestMethod,
         
         TestClass testClass, Object testInstance) {
         this.fTestMethod = fTestMethod;
         this.listOfAssignments = new ArrayList<EasyAssignments>();
         this.fTestClass = testClass;
         this.testInstance = testInstance;
 
     }
 
     private TestClass getTestClass() {
         return fTestClass;
     }
 
     public void evaluate() throws Throwable {
         runWithAssignment(EasyAssignments.allUnassigned(fTestMethod.getMethod(), getTestClass()));
         
     }
 
     /**
      * This method encapsulates the actual change in behavior from the traditional JUnit Theories way of populating and
      * supplying the test data to the test method. This method creates a list of {@link Assignments} identified by
      * {@link #listOfAssignments} and then calls {@link #runWithCompleteAssignment(EasyAssignments)} for each
      * {@link Assignments} element in the {@link #listOfAssignments}
      * 
      * @param parameterAssignment an instance of {@link Assignments} identifying the parameters that needs to be
      *            supplied test data
      * @throws Throwable if any exception occurs.
      */
     protected void runWithAssignment(EasyAssignments parameterAssignment) throws Throwable {
         while (!parameterAssignment.isComplete()) {
             List<PotentialAssignment> potentialAssignments = parameterAssignment.potentialsForNextUnassigned(fTestMethod);
             boolean isFirstSetOfArguments = listOfAssignments.isEmpty();
             for (int i = 0; i < potentialAssignments.size(); i++) {
                 if (isFirstSetOfArguments) {
                     EasyAssignments assignments = EasyAssignments
                         .allUnassigned(fTestMethod.getMethod(), getTestClass());
                     listOfAssignments.add(assignments.assignNext(potentialAssignments.get(i)));
                 } else {
                     EasyAssignments assignments = listOfAssignments.get(i);
                     try {
                         listOfAssignments.set(i, assignments.assignNext(potentialAssignments.get(i)));
                     } catch (IndexOutOfBoundsException e) {
                         listOfAssignments.add(assignments.assignNext(potentialAssignments.get(i)));
                     }
                 }
 
             }
             parameterAssignment = parameterAssignment.assignNext(null);
         }
         if (listOfAssignments.isEmpty()) {
             LOG.debug("The list of Assignments is null. It normally happens when the user has not supplied any parameters to the test.");
             LOG.debug(" Creating an instance of Assignments object with all its value unassigned.");
             listOfAssignments.add(EasyAssignments.allUnassigned(fTestMethod.getMethod(), getTestClass()));
         }
         for (EasyAssignments assignments : listOfAssignments) {
             runWithCompleteAssignment(assignments);
         }
     }
 
     /**
      * Run the test data with complete Assignments
      * 
      * @param complete the {@link Assignments}
      * @throws InstantiationException if an error occurs while instantiating the method
      * @throws IllegalAccessException if an error occurs due to illegal access to the test method
      * @throws InvocationTargetException if an error occurs because the method is not invokable
      * @throws NoSuchMethodException if an error occurs because no such method with the given name exists.
      * @throws Throwable any other error
      */
     protected void runWithCompleteAssignment(final EasyAssignments complete) throws InstantiationException,
         IllegalAccessException, InvocationTargetException, NoSuchMethodException, Throwable {
         
         methodCompletesWithParameters(fTestMethod, complete, testInstance);
     }
 
     /**
      * This method is responsible for actually executing the test method as well as capturing the test data returned by
      * the test method. The algorithm to capture the output data is as follows:
      * <ol>
      * After the method has been invoked explosively, the returned value is checked. If there is a return value:
      * <li>We get the name of the method that is currently executing,
      * <li>We find the exact place in the test input data for which this method was executed,
      * <li>We put the returned result in the map of input test data. The entry in the map has the key :
      * {@link Loader#ACTUAL_RESULT} and the value is the returned value by the test method.
      * <li>If expected result{@link Loader#EXPECTED_RESULT} exist in user input data then we compare it with actual
      * result and put the test status either passed/failed. The entry in the map has the key :
      * {@link Loader#TEST_STATUS} and the value is the either PASSED or FAILED.
      * 
      * We finally write the test data to the file.
      * 
      * @param method an instance of {@link FrameworkMethod} that needs to be executed
      * @param complete an instance of {@link Assignments} that contains the input test data values
      * @param freshInstance a fresh instance of the class for which the method needs to be invoked.
      * @throws Throwable 
      */
     private void methodCompletesWithParameters(final EasyFrameworkMethod method, final EasyAssignments complete,
         final Object freshInstance) throws Throwable{
 
         final RunNotifier testRunNotifier = new RunNotifier();
         final TestRunDurationListener testRunDurationListener = new TestRunDurationListener();
         testRunNotifier.addListener(testRunDurationListener);
         final EachTestNotifier eachRunNotifier = new EachTestNotifier(testRunNotifier, null);
         
         String currentMethodName = method.getMethod().getName();
         TestResultBean testResult = method.getTestResult();
         Map<String, Object> writableRow = method.getTestData();
         Object returnObj = null;
         try {
             final Object[] values = complete.getMethodArguments(true);
 
             testResult.setInput(method.getTestData());
             // invoke test method
             eachRunNotifier.fireTestStarted();
             LOG.debug("Calling method {} with values {}", method.getName(), values);
             returnObj = method.invokeExplosively(freshInstance, values);
             eachRunNotifier.fireTestFinished();
             
             TestMethodDuration testItemDurationBean = new TestMethodDuration(currentMethodName,
                 testRunDurationListener.getStartInNano(), testRunDurationListener.getEndInNano());
             testResult.addTestItemDurationBean(testItemDurationBean);
             testResult.setOutput((returnObj == null) ? "void" : returnObj);
             testResult.setPassed(Boolean.TRUE);
             
             if (writableRow != null) {
                 if (returnObj != null) {
                     LOG.debug("Data returned by method {} is {} :", method.getName(), returnObj);
                     writableRow.put(Loader.ACTUAL_RESULT, returnObj); 
                     Object expectedResult = writableRow.get(Loader.EXPECTED_RESULT);
                     // if expected result exist in user input test data,
                     // then compare that with actual output result
                     // and write the status back to writable map data.
                     if (expectedResult != null) {
                         LOG.debug("Expected result exists");
                         if (expectedResult.toString().equals(returnObj.toString())) {
                             writableRow.put(Loader.TEST_STATUS, Loader.TEST_PASSED);
                         } else {
                             writableRow.put(Loader.TEST_STATUS, Loader.TEST_FAILED);
 
                         }
                     }
 
                 }
                 LOG.debug("testItemDurationBean:" + testItemDurationBean);
                 if (testItemDurationBean != null) {
                     Double testDuration = CommonUtils.getRounded(testItemDurationBean.getRoundedMsDifference()
                         .doubleValue(), 3);
                     LOG.debug("testItemDurationBean.getRoundedMsDifference():" + testDuration);
                     writableRow.put(Loader.DURATION, testDuration);
                 }
             }
         } catch (AssumptionViolatedException e) {
             eachRunNotifier.addFailedAssumption(e);
             handleAssumptionViolation(e);
         } catch (Throwable e) {
 
             if (e instanceof AssertionError) { // Assertion error
                 testResult.setPassed(Boolean.FALSE);
                 testResult.setResult(e.getMessage());                       
 
             } else { // Exception
                 testResult.setException(Boolean.TRUE);
                 testResult.setExceptionResult(e.toString());
 
             }
             eachRunNotifier.addFailure(e);
             reportParameterizedError(e, complete.getArgumentStrings(true));
         } finally {
             eachRunNotifier.fireTestFinished();
         }
         //The test should fail in case the Actual Result returned by the test method did
         //not match the Expected result specified for the method in the test data file.
         if (writableRow != null && writableRow.get(Loader.TEST_STATUS) != null
             && writableRow.get(Loader.TEST_STATUS).equals(Loader.TEST_FAILED)) {
             Assert.fail("Actual Result returned by the method : [" + returnObj
                 + "] did not match the expected result : [" + writableRow.get(Loader.EXPECTED_RESULT) + "]");
         }
 
     }
 
     protected void handleAssumptionViolation(AssumptionViolatedException e) {
         fInvalidParameters.add(e);
     }
 
     protected void reportParameterizedError(Throwable e, Object... params) throws Throwable {
         if (params.length == 0)
             throw e;
         throw new ParamAssertionError(e, fTestMethod.getName(), params);
     }
 
     
 }
