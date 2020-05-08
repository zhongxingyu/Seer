 /*******************************************************************************
  * Copyright (c) 2010 Thales Corporate Services SAS                             *
  *                                                                              *
  * Permission is hereby granted, free of charge, to any person obtaining a copy *
  * of this software and associated documentation files (the "Software"), to deal*
  * in the Software without restriction, including without limitation the rights *
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell    *
  * copies of the Software, and to permit persons to whom the Software is        *
  * furnished to do so, subject to the following conditions:                     *
  *                                                                              *
  * The above copyright notice and this permission notice shall be included in   *
  * all copies or substantial portions of the Software.                          *
  *                                                                              *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR   *
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,     *
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  *
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER       *
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,*
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN    *
  * THE SOFTWARE.                                                                *
  *******************************************************************************/
 
 package com.thalesgroup.sonar.plugins.tusar.sensors;
 
 import com.thalesgroup.sonar.lib.model.v4.Sonar;
 import com.thalesgroup.sonar.lib.model.v4.TestsComplexType;
 import com.thalesgroup.sonar.plugins.tusar.TUSARResource;
 import org.apache.commons.lang.StringEscapeUtils;
 import org.sonar.api.batch.SensorContext;
 import org.sonar.api.measures.CoreMetrics;
 import org.sonar.api.measures.Measure;
 import org.sonar.api.resources.Project;
 import org.sonar.api.utils.ParsingUtils;
 
 import javax.xml.stream.XMLStreamException;
 import java.text.ParseException;
 import java.util.*;
 
 
 /**
  * Contains methods to extract TUSAR tests data.
  */
 public class TUSARTestsDataExtractor {
 
 //	private Logger logger = LoggerFactory.getLogger(TUSARTestsDataExtractor.class);	
 
     public static Collection<TestSuiteReport> extractTestsData(Sonar model, SensorContext context) throws XMLStreamException {
         Map<String, TestSuiteReport> reportsPerPath = new HashMap<String, TestSuiteReport>();
 
         for (TestsComplexType.Testsuite testSuite : model.getTests().getTestsuite()) {
 
             //testSuite.getName();
             //testSuite.getSkipped();
             //testSuite.getTests();
             //testSuite.getTime();
 
             for (TestsComplexType.Testsuite.Testcase testCase : testSuite.getTestcase()) {
 
                 String testCasePath = "";
                 if (testCase.getFilepath() != null) {
                     testCasePath = testCase.getFilepath();
                 } 
                 else if (testCase.getFulltestname() != null){
                     testCasePath = testCase.getFulltestname();
                 }
                 
                 //Fix bug : if testCasePath is empty, set it with the testname
                 if(testCasePath.trim().isEmpty()){
                 	testCasePath = testCase.getTestname();
                 }
 
                 //Filter testCasePath
                 testCasePath = testCasePath.replaceAll("\r", "");
                 testCasePath = testCasePath.replaceAll("\\n", "");
                 testCasePath = testCasePath.trim();
 
                 TestSuiteReport testSuiteReport = reportsPerPath.get(testCasePath);
                 if (testSuiteReport == null) {
                     testSuiteReport = new TestSuiteReport(testCasePath);
                     reportsPerPath.put(testCasePath, testSuiteReport);
                 }
 
                 TestCaseDetails testCaseDetails = new TestCaseDetails();
 
                 testCaseDetails.setName(testCase.getTestname());
                if (testCase.getTime() == null){
                	testCaseDetails.setTimeMS(0);
                }
                else {
                	testCaseDetails.setTimeMS(getTimeAttributeInMS(testCase.getTime()).intValue());
                }
 
                 //testCase.getAssertions();
                 //testCase.getFilename();
 
                 String testCaseStatus = TestCaseDetails.STATUS_OK;
 
                 if (testCase.getFailure() != null) {
                     testCaseStatus = TestCaseDetails.STATUS_FAILURE;
                     testCaseDetails.setErrorMessage(testCase.getFailure().getMessage());
                     testCaseDetails.setStackTrace(testCase.getFailure().getContent());
 
                     // cumulate data for test suite
                     testSuiteReport.setFailures(testSuiteReport.getFailures() + 1);
                 } 
                 else if (testCase.getError() != null) {
                     testCaseStatus = TestCaseDetails.STATUS_ERROR;
                     testCaseDetails.setErrorMessage(testCase.getError().getMessage());
                     testCaseDetails.setStackTrace(testCase.getError().getContent());
 
                     // cumulate data for test suite
                     testSuiteReport.setErrors(testSuiteReport.getErrors() + 1);
                 }
                 else if (testCase.getSkipped() != null) {
                 	testCaseStatus = TestCaseDetails.STATUS_SKIPPED;
                 	
                 	// cumulate data for test suite
                     testSuiteReport.setSkipped(testSuiteReport.getSkipped() + 1);
                 }
 
                 testCaseDetails.setStatus(testCaseStatus);
 
                 /* TODO check what to do with that
                     if (testCase.getSystemOut() != null) {
                         systemOut.getContent();
                     }
                     if (testCase.getSystemErr() != null) {
                         systemErr.getContent();
                     }
                     */
 
                 testSuiteReport.setTests(testSuiteReport.getTests() + 1);
                 testSuiteReport.setTimeMS(testSuiteReport.getTimeMS() + testCaseDetails.getTimeMS());
                 testSuiteReport.getDetails().add(testCaseDetails);
             }
         }
 
         return reportsPerPath.values();
     }
 
     public static void saveToSonarTestsData(Collection<TestSuiteReport> testSuiteReports, SensorContext context, Project project) {
 
         for (TestSuiteReport testSuiteReport : testSuiteReports) {
 
             TUSARResource resource = TUSARResource.fromAbsOrRelativePath(testSuiteReport.getPath(), project, true);
 
             double testsCount = testSuiteReport.getTests() - testSuiteReport.getSkipped();
             context.saveMeasure(resource, CoreMetrics.TESTS, testsCount);
             context.saveMeasure(resource, CoreMetrics.TEST_ERRORS, (double) testSuiteReport.getErrors());
             context.saveMeasure(resource, CoreMetrics.TEST_FAILURES, (double) testSuiteReport.getFailures());
             context.saveMeasure(resource, CoreMetrics.SKIPPED_TESTS, (double) testSuiteReport.getSkipped());
             context.saveMeasure(resource, CoreMetrics.TEST_EXECUTION_TIME, (double) testSuiteReport.getTimeMS());
 
             double passedTests = testSuiteReport.getTests() - testSuiteReport.getErrors() - testSuiteReport.getFailures();
             if (testsCount > 0) {
                 double percentage = ParsingUtils.scaleValue(passedTests * 100d / testsCount);
                 context.saveMeasure(resource, CoreMetrics.TEST_SUCCESS_DENSITY, percentage);
             }
 
             String testCaseDetails = computeTestsDetails(context, testSuiteReport);
             context.saveMeasure(resource, new Measure(CoreMetrics.TEST_DATA, testCaseDetails));
         }
     }
 
     private static String computeTestsDetails(SensorContext context, TestSuiteReport fileReport) {
         StringBuilder testCaseDetails = new StringBuilder(256);
         testCaseDetails.append("<tests-details>");
         List<TestCaseDetails> details = fileReport.getDetails();
 
         for (TestCaseDetails detail : details) {
             testCaseDetails.append("<testcase status=\"").append(detail.getStatus())
                     .append("\" time=\"").append(detail.getTimeMS())
                     .append("\" name=\"").append(detail.getName()).append("\"");
 
             boolean isError = detail.getStatus().equals(TestCaseDetails.STATUS_ERROR);
             if (isError || detail.getStatus().equals(TestCaseDetails.STATUS_FAILURE)) {
                 testCaseDetails.append(">")
                         .append(isError ? "<error message=\"" : "<failure message=\"")
                         .append(StringEscapeUtils.escapeXml(detail.getErrorMessage())).append("\">")
                         .append("<![CDATA[").append(StringEscapeUtils.escapeXml(detail.getStackTrace())).append("]]>")
                         .append(isError ? "</error>" : "</failure>").append("</testcase>");
             } else {
                 testCaseDetails.append("/>");
             }
         }
         testCaseDetails.append("</tests-details>");
         return testCaseDetails.toString();
     }
 
 
     private static Double getTimeAttributeInMS(String stringTime) throws XMLStreamException {
         // hardcoded to Locale.ENGLISH see http://jira.codehaus.org/browse/SONAR-602
         try {
             Double time = ParsingUtils.parseNumber(stringTime, Locale.ENGLISH);
             return !Double.isNaN(time) ? ParsingUtils.scaleValue(time * 1000, 3) : 0;
         } catch (ParseException e) {
             throw new XMLStreamException(e);
         }
     }
 }
