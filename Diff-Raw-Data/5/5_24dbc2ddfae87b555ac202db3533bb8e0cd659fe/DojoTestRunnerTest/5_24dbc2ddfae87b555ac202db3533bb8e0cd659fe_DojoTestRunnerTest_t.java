 package org.automation.dojo;
 
 import org.automation.dojo.samples.*;
 import org.fest.assertions.ListAssert;
 import org.fest.assertions.StringAssert;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.Description;
 import org.junit.runner.notification.Failure;
 import org.junit.runner.notification.RunListener;
 import org.junit.runner.notification.RunNotifier;
 import org.junit.runners.model.InitializationError;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import static junit.framework.Assert.fail;
 import static org.fest.assertions.Assertions.assertThat;
 import static org.junit.Assert.assertEquals;
 
 public class DojoTestRunnerTest {
 
     private FakeHttpServer server;
     private ArrayList<Failure> failures;
 
     @Before
     public void setUp() throws Exception {
         server = new FakeHttpServer(1111);
         server.start();
         failures = new ArrayList<Failure>();
     }
 
     @After
     public void tearDown() {
         server.stop();
 
     }
 
     @Test
     public void shouldFailToCreateRunnerWhenNoReportAnnotation() {
         try {
             new DojoTestRunner(NotAnnotatedTest.class);
             fail();
         } catch (InitializationError initializationError) {
             String message = initializationError.getCauses().get(0).getMessage();
             assertEquals("Annotation @ReportTo should be defined at a class level", message);
         }
     }
 
     @Test
     public void shouldReportSuccessWhenPassed() throws InitializationError, IOException {
         server.setResponse("scenario1=passed");
 
         runTests(OneSuccessTest.class);
 
         assertRequestContains("scenario1=passed");
     }
 
     private StringAssert assertRequestContains(String expectedSubstring) {
         return assertThat(server.getRequest()).contains(expectedSubstring);
     }
 
     @Test
     public void shouldSendUserWhenTestRun() throws InitializationError {
         server.setResponse("scenario1=passed");
 
         runTests(OneSuccessTest.class);
 
         assertRequestContains("name=Sergey");
     }
 
     @Test
     public void shouldSendSeveralResultsWhenPassed() throws InitializationError {
         server.setResponse("scenario1=passed\nscenario2=passed");
 
         runTests(SeveralPassedTests.class);
 
         assertRequestContains("scenario1=passed");
         assertRequestContains("scenario2=passed");
     }
 
     @Test
     public void shouldSendFailuresWhenTestFails() throws InitializationError {
         server.setResponse("scenario33=failed");
 
         runTests(OneFailedTest.class);
 
         assertRequestContains("scenario33=failed");
     } 
     
     @Test
     public void shouldReportFailureWhenNoScenarioAnnotationGiven() throws InitializationError {
         runTests(NoScenarioAnnotationTest.class);
 
         assertThat(server.getRequest()).isEmpty();
     }
 
     @Test
     public void shouldReportExceptionWhenNonAssertion() throws InitializationError {
         server.setResponse("scenario1=failed");
         runTests(ExceptionTest.class);
 
         assertRequestContains("scenario1=exception");
     }
 
     @Test
     public void shouldReportSeveralResultsWhenSeveralCasesForScenario() throws InitializationError {
         server.setResponse("scenario1=passed");
         runTests(SeveralTestsForOneScenarioTest.class);
 
         assertRequestContains("scenario1=passed");
         assertRequestContains("scenario1=failed");
     }
 
     private void runTests(Class<?> klass) throws InitializationError {
         DojoTestRunner runner = new DojoTestRunner(klass);
         RunNotifier notifier = new RunNotifier();
         notifier.addListener(new RunListener(){
             @Override
             public void testFailure(Failure failure) throws Exception {
                 failures.add(failure);
             }
 
             @Override
             public void testFinished(Description description) throws Exception {
             }
         });
         runner.run(notifier);
     }
 }
