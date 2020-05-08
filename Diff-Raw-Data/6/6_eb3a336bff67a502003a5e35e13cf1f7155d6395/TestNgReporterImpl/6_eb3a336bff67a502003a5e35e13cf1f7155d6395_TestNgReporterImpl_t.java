 package framework.testng.report;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import org.testng.IClass;
 import org.testng.IReporter;
 import org.testng.IResultMap;
 import org.testng.ISuite;
 import org.testng.ISuiteResult;
 import org.testng.ITestResult;
 import org.testng.xml.XmlSuite;
 
 import framework.testng.report.xml.SummaryReport;
 import framework.testng.report.xml.TestReport;
 import framework.testng.report.xml.TestsReport;
 import framework.utils.LogUtils;
 
 /**
  * @author moran
  */
 public class TestNgReporterImpl implements IReporter {
 
     private static final LogFetcher logFetcher = new LogFetcher();
     private static final Comparator<IClass> CLASS_COMPARATOR = new TestClassComparator();
     private static final Comparator<ITestResult> RESULT_COMPARATOR = new TestResultComparator();
 
     private enum TestStatus {
         FAILED_CONFIG,
         SKIPPED_CONFIG,
         FAILED_TEST,
         SKIPPED_TEST,
         PASSED_TEST,
     }
 
     @Override
     public void generateReport(java.util.List<XmlSuite> xmlSuites, java.util.List<ISuite> suites, java.lang.String outputDirectory) {
         try {
             List<TestReport> testReports = createTestReports(suites);
             TestsReport testsReport = new TestsReport(testReports);
             testsReport.setSuiteName(getSuiteName(suites));
 
             String fileName = getFileName(suites);
             TestsReportFileStream fileStream = new TestsReportFileStream();
             fileStream.writeToFile(outputDirectory, fileName, testsReport);
 
             LogUtils.log("generating report to: " + outputDirectory + "/" + fileName);
             LogUtils.log(new SummaryReport(testsReport).toString());
 
         } catch (Exception e) {
             throw new RuntimeException("could not generate report; " + e, e);
         }
     }
 
     private List<TestReport> createTestReports(List<ISuite> suites) throws Exception {
         List<TestReport> list = new ArrayList<TestReport>();
         for (ISuite suite : suites) {
             for (ISuiteResult result : suite.getResults().values()) {
                 list.addAll(createTestReport(TestStatus.FAILED_CONFIG, sortByTestClass(result.getTestContext().getFailedConfigurations())));
                 list.addAll(createTestReport(TestStatus.SKIPPED_CONFIG, sortByTestClass(result.getTestContext().getSkippedConfigurations())));
                 list.addAll(createTestReport(TestStatus.FAILED_TEST, sortByTestClass(result.getTestContext().getFailedTests())));
                 list.addAll(createTestReport(TestStatus.SKIPPED_TEST, sortByTestClass(result.getTestContext().getSkippedTests())));
                 list.addAll(createTestReport(TestStatus.PASSED_TEST, sortByTestClass(result.getTestContext().getPassedTests())));
             }
         }
 
         return list;
     }
 
     private List<TestReport> createTestReport(TestStatus testStatus,
                                               SortedMap<IClass, List<ITestResult>> sortByTestClass) {
         boolean isSuspect = false;
         List<TestReport> list = new ArrayList<TestReport>();
         for (List<ITestResult> testResult : sortByTestClass.values()) {
             for (ITestResult iTestResult : testResult) {
 
                 //do not add configurations as tests. Only add them if they failed.
                 if (!iTestResult.getMethod().isTest() && !(iTestResult.getStatus() == ITestResult.FAILURE)) {
                     continue; //is configuration && not failed
                 }
                 isSuspect = isTestSuspect(iTestResult);
                 TestReport testReport = new TestReport(iTestResult.getTestClass().getName() + "." + iTestResult.getName());
                 testReport.setDuration(iTestResult.getEndMillis() - iTestResult.getStartMillis());
                 testReport.setLogs(logFetcher.getLogs(iTestResult));
 
                 switch (testStatus) {
                     case FAILED_CONFIG:
                     case FAILED_TEST:
                         if (isSuspect) {
                             testReport.setSuspected(true);
                         }else{
                             testReport.setFailed(true);
                         }
                         if (iTestResult.getThrowable() != null) {
                             testReport.setCause(String.valueOf((iTestResult.getThrowable())));
                         }
                         break;
                     case SKIPPED_CONFIG:
                     case SKIPPED_TEST:
                         testReport.setSkipped(true);
                         break;
                     case PASSED_TEST:
                         if (isSuspect) {
                             testReport.setSuspected(true);
                         }else{
                             testReport.setFailed(false);
                         }
                         break;
                 }
                 list.add(testReport);
             }
         }
 
         return list;
 
     }
 
     private boolean isTestSuspect(ITestResult iTestResult) {
         if(iTestResult.getMethod().getGroups().length == 0){
             return false;
         }
         for (String group : iTestResult.getMethod().getGroups()) {
             if (group.equals("SUSPECTED")) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Group test methods by class and sort alphabetically.
      */
     private SortedMap<IClass, List<ITestResult>> sortByTestClass(IResultMap results) {
         SortedMap<IClass, List<ITestResult>> sortedResults = new TreeMap<IClass, List<ITestResult>>(CLASS_COMPARATOR);
         for (ITestResult result : results.getAllResults()) {
             List<ITestResult> resultsForClass = sortedResults.get(result.getTestClass());
             if (resultsForClass == null) {
                 resultsForClass = new ArrayList<ITestResult>();
                 sortedResults.put(result.getTestClass(), resultsForClass);
             }
             int index = Collections.binarySearch(resultsForClass, result, RESULT_COMPARATOR);
             if (index < 0) {
                 index = Math.abs(index + 1);
             }
             resultsForClass.add(index, result);
         }
         return sortedResults;
     }
 
     public String getFileName(List<ISuite> suites) {
         String fileName = "sgtest-result-" + getSuiteName(suites) + ".xml";
         return fileName;
     }
 
     private String getSuiteName(List<ISuite> suites) {
         if (!suites.isEmpty()) {
            return System.getProperty("sgtest.suiteName") + System.getProperty("sgtest.suiteId");
         } else {
             return "";
         }
     }
 
}
