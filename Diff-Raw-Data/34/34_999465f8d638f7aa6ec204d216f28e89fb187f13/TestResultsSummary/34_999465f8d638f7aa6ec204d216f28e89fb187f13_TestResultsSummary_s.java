 /*
  * JBoss, Home of Professional Open Source.
  * See the COPYRIGHT.txt file distributed with this work for information
  * regarding copyright ownership.  Some portions may be licensed
  * to Red Hat, Inc. under one or more contributor license agreements.
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * 
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
  * 02110-1301 USA.
  */
 
 package org.teiid.test.client;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.teiid.test.framework.ConfigPropertyLoader;
 import org.teiid.test.framework.TestLogger;
 
 public class TestResultsSummary  {
     
     private static final String PROP_SUMMARY_PRT_DIR="summarydir";
     private static final SimpleDateFormat FILE_NAME_DATE_FORMATER = new SimpleDateFormat(
 	    "yyyyMMdd_HHmmss"); //$NON-NLS-1$
 
     private static final String NL = System.getProperty("line.separator"); //$NON-NLS-1$
     
     // totals for scenario
     private int total_queries = 0;
     private int total_pass = 0;
     private int total_fail = 0;
     private int total_querysets = 0;
     private List<String> failed_queries = new ArrayList<String>();
     private List<String> query_sets = new ArrayList<String>(10);
     
     private Map<String, Collection<TestResult>> testResults = Collections.synchronizedMap(new HashMap<String, Collection<TestResult>>());
     
     public void cleanup() {
 	failed_queries.clear();
 	query_sets.clear();
 	testResults.clear();
     }
 
     public synchronized void addTestResult(String querySetID, TestResult result) {
 	Collection<TestResult> results = null;
 	if (this.testResults.containsKey(querySetID)) {
 	    results = this.testResults.get(querySetID);
 	} else {
 	    results = new ArrayList<TestResult>();
 	    this.testResults.put(querySetID, results);
 	}
 	results.add(result);
 	
     }
 
 
 
     public Collection<TestResult> getTestResults(String querySetID) {
 	return this.testResults.get(querySetID);
     }
     
  
     private static PrintStream getSummaryStream(String outputDir,
 	    String summaryName) throws IOException {
 	File summaryFile = createSummaryFile(outputDir, summaryName);
 	OutputStream os = new FileOutputStream(summaryFile);
 	os = new BufferedOutputStream(os);
 	return new PrintStream(os);
     }
 
     /**
      * Overloaded to overwrite the already existing files
      */
     private static PrintStream getSummaryStream(String outputDir,
 	    String summaryName, boolean overwrite) throws IOException {
 
 	// Check Extension is already specified for the file, if not add the
 	// .txt
 	if (summaryName.indexOf(".") == -1) { //$NON-NLS-1$
 	    summaryName = summaryName + ".txt"; //$NON-NLS-1$
 	}
 
 	File summaryFile = new File(outputDir, summaryName);
 	if (summaryFile.exists() && !overwrite) {
 	    throw new IOException(
 		    "Summary file already exists: " + summaryFile.getName()); //$NON-NLS-1$
 	}
 	summaryFile.createNewFile();
 	OutputStream os = new FileOutputStream(summaryFile);
 	os = new BufferedOutputStream(os);
 	return new PrintStream(os);
     }
 
     private static File createSummaryFile(String outputDir, String summaryName)
 	    throws IOException {
 	File summaryFile = new File(outputDir, summaryName + ".txt"); //$NON-NLS-1$
 	if (summaryFile.exists()) {
 	    System.err
 		    .println("Summary file already exists: " + summaryFile.getName()); //$NON-NLS-1$
 	    throw new IOException(
 		    "Summary file already exists: " + summaryFile.getName()); //$NON-NLS-1$
 	}
 
 	try {
 	    summaryFile.createNewFile();
 	} catch (IOException e) {
 	    System.err
 		    .println("Failed to create summary file at: " + summaryFile.getAbsolutePath()); //$NON-NLS-1$
 	    throw new IOException(
 		    "Failed to create summary file at: " + summaryFile.getAbsolutePath() + ": " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
 	}
 	return summaryFile;
     }
 
     private static void printQueryTestResults(PrintStream outputStream,
 	    long testStartTS, long endTS, int numberOfClients,
 	    SimpleDateFormat formatter, Collection results) {
 	outputStream.println("Query Test Results"); //$NON-NLS-1$
 	outputStream.println("=================="); //$NON-NLS-1$
 	outputStream.println("Start        Time: " + new Date(testStartTS)); //$NON-NLS-1$
 	outputStream.println("End          Time: " + new Date(endTS)); //$NON-NLS-1$
 	outputStream
 		.println("Elapsed      Time: " + ((endTS - testStartTS) / 1000) + " seconds"); //$NON-NLS-1$ //$NON-NLS-2$
 	outputStream.println("Number of Clients: " + numberOfClients); //$NON-NLS-1$
 
 	Map passFailGenMap = getPassFailGen(results);
 	outputStream
 		.println("Number of Queries: " + passFailGenMap.get("queries")); //$NON-NLS-1$ //$NON-NLS-2$
 	outputStream
 		.println("Number Passed    : " + passFailGenMap.get("pass")); //$NON-NLS-1$ //$NON-NLS-2$
 	outputStream
 		.println("Number Failed    : " + passFailGenMap.get("fail")); //$NON-NLS-1$ //$NON-NLS-2$
	outputStream.println("Number Generated : " + passFailGenMap.get("gen")); //$NON-NLS-1$ //$NON-NLS-2$
 
 	ResponseTimes responseTimes = calcQueryResponseTimes(results);
 	outputStream.println("QPS              : " + responseTimes.qps); //$NON-NLS-1$
 
 	outputStream.println("Ave First Resp   : " + responseTimes.first); //$NON-NLS-1$
 
 	outputStream.println("Ave Full Resp    : " + responseTimes.full); //$NON-NLS-1$
 
 	Iterator resultItr = results.iterator();
 	while (resultItr.hasNext()) {
 	    TestResult stat = (TestResult) resultItr.next();
 	    writeQueryResult(outputStream, formatter, stat);
 	}
 
     }
 
     private static Map getPassFailGen(Collection results) {
 	Map passFailGenMap = new HashMap();
 	int queries = 0;
 	int pass = 0;
 	int fail = 0;
 	int gen = 0;
 
 	for (Iterator resultsItr = results.iterator(); resultsItr.hasNext();) {
 	    TestResult stat = (TestResult) resultsItr.next();
 	    ++queries;
 	    switch (stat.getStatus()) {
 	    case TestResult.RESULT_STATE.TEST_EXCEPTION:
 		++fail;
 		break;
 	    case TestResult.RESULT_STATE.TEST_SUCCESS:
 		++pass;
 		break;
 	    case TestResult.RESULT_STATE.TEST_EXPECTED_EXCEPTION:
 		++pass;
 		break;
 	    }
 	}
 	passFailGenMap.put("queries", Integer.toString(queries)); //$NON-NLS-1$
 	passFailGenMap.put("pass", Integer.toString(pass)); //$NON-NLS-1$
 	passFailGenMap.put("fail", Integer.toString(fail)); //$NON-NLS-1$
 	//       passFailGenMap.put("gen", Integer.toString(gen)); //$NON-NLS-1$
 	return passFailGenMap;
     }
     
     private void addTotalPassFailGen(String scenario_name, Collection results) {
 	int queries = 0;
 	int pass = 0;
 	int fail = 0;
 	
 	String queryset = null;
 
 	total_querysets++;
 	for (Iterator resultsItr = results.iterator(); resultsItr.hasNext();) {
 	    TestResult stat = (TestResult) resultsItr.next();
 	    
 	    if (queryset == null){
 		queryset = stat.getQuerySetID();
 	    }
 	    
 	    ++queries;
 	    switch (stat.getStatus()) {
 	    case TestResult.RESULT_STATE.TEST_EXCEPTION:
 		++fail;
 		
 		this.failed_queries.add(stat.getQueryID());
 		break;
 	    case TestResult.RESULT_STATE.TEST_SUCCESS:
 		++pass;
 		break;
 	    case TestResult.RESULT_STATE.TEST_EXPECTED_EXCEPTION:
 		++pass;
 		break;
 	    }
 	}
 	
 	this.query_sets.add("\t" + queryset + "\t\t" + pass + "\t" + fail + "\t" + queries);
 	
 	total_fail = total_fail + fail;
 	total_pass = total_pass + pass;
 	total_queries = total_queries + queries;
 
     }
     
     public void printResults(QueryScenario scenario, String querySetID,
 	    long beginTS,
 	    long endTS) throws Exception {
 
     
             TestLogger.logDebug("Print results for Query Set [" + querySetID
         	    + "]");
         
             try {
         	printResults(scenario, querySetID, beginTS, endTS, 1, 1);
             } catch (Exception e) {
         	// TODO Auto-generated catch block
         	e.printStackTrace();
             }
     }
 
     /**
      * Print test results.
      * 
      * @param testStartTS
      *            The test start time.
      * @param endTS
      *            The test end time.
      * @throws Exception
      */
     public void printResults(QueryScenario scenario, String querySetID,
 	    long testStartTS,
 	    long endTS, int numberOfClients, int runNumber) throws Exception {
 	
 	String testname = scenario.getQueryScenarioIdentifier();
 	Collection<TestResult> testResults = getTestResults(querySetID);
 //	Properties props = scenario.getProperties();
 	String outputDir = scenario.getResultsGenerator().getOutputDir();
 	
 	//       CombinedTestClient.log("Calculating and printing result statistics"); //$NON-NLS-1$
 	if (testResults.size() > 0) {
 	    // Create output file
 	    String outputFileName = generateFileName(querySetID, System
 		    .currentTimeMillis(), runNumber);
 	    //           CombinedTestClient.log("Creating output file: " + outputFileName); //$NON-NLS-1$
 	    PrintStream outputStream = null;
 	    PrintStream overwriteStream = null;
 	    try {
 		outputStream = getSummaryStream(outputDir, outputFileName);
 		overwriteStream = getSummaryStream(outputDir, querySetID, true); //$NON-NLS-1$
 	    } catch (IOException e) {
 		//              logError("Unable to get output stream for file: " + outputFileName); //$NON-NLS-1$
 		throw e;
 	    }
 	    addTotalPassFailGen(testname, testResults);
 	    // Text File output
 	    printQueryTestResults(outputStream, testStartTS, endTS,
 		    numberOfClients, TestClient.TSFORMAT, testResults);
 	    printQueryTestResults(overwriteStream, testStartTS, endTS,
 		    numberOfClients, TestClient.TSFORMAT, testResults);
 
 	    // HTML Vesion of output
 	    PrintStream htmlStream = getSummaryStream(outputDir, querySetID
 		    + ".html", true); //$NON-NLS-1$
 	    printHtmlQueryTestResults(htmlStream, testStartTS, endTS,
 		    numberOfClients, TestClient.TSFORMAT, testResults);
 	    htmlStream.close();
 
 	    // Wiki Update
 	    //       	CombinedTestUtil.publishResultsToWiki(props, outputDir+File.separator+querySetID+".html", testStartTS, endTS, numberOfClients, testResults); //$NON-NLS-1$ //$NON-NLS-2$
 
 	    // Print results according to test type
 	    // switch (CombinedTestClient.TEST_TYPE) {
 	    // case CombinedTestClient.TEST_TYPE_QUERY:
 	    // // Text File output
 	    // printQueryTestResults(outputStream, testStartTS, endTS,
 	    // numberOfClients, TestClientTransaction.TSFORMAT, testResults);
 	    // printQueryTestResults(overwriteStream, testStartTS, endTS,
 	    // numberOfClients, TestClientTransaction.TSFORMAT, testResults);
 	    //                	
 	    // // HTML Vesion of output
 	    //                	PrintStream htmlStream = getSummaryStream(outputDir, CONFIG_ID+".html", true); //$NON-NLS-1$
 	    // CombinedTestUtil.printHtmlQueryTestResults(htmlStream,
 	    // testStartTS, endTS, numberOfClients,
 	    // TestClientTransaction.TSFORMAT, testResults);
 	    // htmlStream.close();
 	    //                	
 	    // // Wiki Update
 	    //                	CombinedTestUtil.publishResultsToWiki(props, outputDir+File.separator+CONFIG_ID+".html", testStartTS, endTS, numberOfClients, testResults); //$NON-NLS-1$ //$NON-NLS-2$
 	    // break;
 	    // case CombinedTestClient.TEST_TYPE_LOAD:
 	    // CombinedTestUtil.printLoadTestResults(outputStream, testStartTS,
 	    // endTS, numberOfClients, TestClientTransaction.TSFORMAT,
 	    // testResults);
 	    // CombinedTestUtil.printLoadTestResults(overwriteStream,
 	    // testStartTS, endTS, numberOfClients,
 	    // TestClientTransaction.TSFORMAT, testResults);
 	    // break;
 	    // case CombinedTestClient.TEST_TYPE_PERF:
 	    // CombinedTestUtil.printPerfTestResults(outputStream, testStartTS,
 	    // endTS, numberOfClients, CONF_LVL, TestClientTransaction.TSFORMAT,
 	    // testResults);
 	    // CombinedTestUtil.printPerfTestResults(overwriteStream,
 	    // testStartTS, endTS, numberOfClients, CONF_LVL,
 	    // TestClientTransaction.TSFORMAT, testResults);
 	    // break;
 	    // case CombinedTestClient.TEST_TYPE_PROF:
 	    // CombinedTestUtil.printProfTestResults();
 	    // break;
 	    // default:
 	    // break;
 	    // }
 
 	    //        CombinedTestClient.log("Closing output stream"); //$NON-NLS-1$
 	    outputStream.close();
 	    overwriteStream.close();
 	} else {
 	    //          logError("No results to print."); //$NON-NLS-1$
 	}
     }
     
     public void printTotals(QueryScenario scenario ) throws Exception {
 	    String outputDir = scenario.getResultsGenerator().getOutputDir(); 
 	    String scenario_name = scenario.getQueryScenarioIdentifier();
 	    String querysetname = scenario.getQuerySetName();
 
 	
 	String summarydir = ConfigPropertyLoader.getInstance().getProperty(PROP_SUMMARY_PRT_DIR);
 	if (summarydir != null) {
 	    outputDir = summarydir;
 	}
 
 	    PrintStream outputStream = null;
 	    try {
 		outputStream = getSummaryStream(outputDir, "Summary_" + querysetname + "_" + scenario_name, true); //$NON-NLS-1$
 	    } catch (IOException e) {
 		//              logError("Unable to get output stream for file: " + outputFileName); //$NON-NLS-1$
 		throw e;
 	    }
 	    
 	    	
 		outputStream.println("Scenario " + scenario_name + " Summary"); //$NON-NLS-1$
 		outputStream.println("Query Set Name " + querysetname); //$NON-NLS-1$
 		outputStream.println("=================="); //$NON-NLS-1$
 		
 		outputStream
 		.println("Number of Test Query Sets: " + total_querysets); //$NON-NLS-1$ //$NON-NLS-2$
 		
 		outputStream.println("=================="); //$NON-NLS-1$
 		outputStream.println("Test Query Set"); //$NON-NLS-1$
 		outputStream.println("\t" + "Name" + "\t\t" + "Pass" + "\t" + "Fail" + "\t" + "Total"); //$NON-NLS-1$
 
 		if (!this.query_sets.isEmpty()) {
 		    // sort so that like failed queries are show together
 			Collections.sort(this.query_sets);
 
 			
 			for (Iterator<String> it=this.query_sets.iterator(); it.hasNext();) {
 				outputStream
 				.println(it.next()); //$NON-NLS-1$ //$NON-NLS-2$
 		    
 			}
 
 		}	
 		outputStream.println("=================="); //$NON-NLS-1$
 		
 		
 		outputStream
 		.println("\t" + "Totals" + "\t\t" + total_pass + "\t" + total_fail + "\t" + total_queries);
 		
 //		outputStream
 //			.println("Number of Queries: " + total_queries); //$NON-NLS-1$ //$NON-NLS-2$
 //		outputStream
 //			.println("Number Passed    : " + total_pass); //$NON-NLS-1$ //$NON-NLS-2$
 //		outputStream
 //			.println("Number Failed    : " + total_fail); //$NON-NLS-1$ //$NON-NLS-2$
 		
 		if (!this.failed_queries.isEmpty()) {
 		    // sort so that like failed queries are show together
 			Collections.sort(this.failed_queries);
 		    
 			outputStream.println("\n\n=================="); //$NON-NLS-1$
 			outputStream.println("Failed Queries"); //$NON-NLS-1$		    
 			
 			for (Iterator<String> it=this.failed_queries.iterator(); it.hasNext();) {
 				outputStream
 				.println("\t - " + it.next()); //$NON-NLS-1$ //$NON-NLS-2$
 		    
 			}
 				    
 			outputStream.println("=================="); //$NON-NLS-1$
 		    
 		}
 
 		    outputStream.close();
 
 	
     }
 
     private static String generateFileName(String configName, long timestamp,
 	    int runNumber) {
 	return configName
 		+ "_" + FILE_NAME_DATE_FORMATER.format(new Date(timestamp)) + "_Run-" + runNumber; //$NON-NLS-1$ //$NON-NLS-2$
     }
 
     private static void printHtmlQueryTestResults(PrintStream outputStream,
 	    long testStartTS, long endTS, int numberOfClients,
 	    SimpleDateFormat formatter, Collection results) {
 
 	StringBuffer htmlCode = new StringBuffer("<html>").append(NL); //$NON-NLS-1$
 	htmlCode.append("<HEAD>").append(NL); //$NON-NLS-1$
 	htmlCode.append("<TITLE>Query Test Results</TITLE>").append(NL); //$NON-NLS-1$
 	htmlCode.append("<STYLE TYPE=\"text/css\">").append(NL); //$NON-NLS-1$
 	htmlCode
 		.append(
 			"td { font-family: \"New Century Schoolbook\", Times, serif  }").append(NL); //$NON-NLS-1$
 	htmlCode.append("td { font-size: 8pt }").append(NL); //$NON-NLS-1$
 	htmlCode.append("</STYLE>").append(NL); //$NON-NLS-1$
 	htmlCode.append("<SCRIPT type=\"text/javascript\">").append(NL); //$NON-NLS-1$
 	htmlCode.append("var scriptWin = null;").append(NL); //$NON-NLS-1$
 	htmlCode.append("function show(msg){").append(NL); //$NON-NLS-1$
 	//htmlCode.append("alert(msg);").append(nl);       //$NON-NLS-1$
 	htmlCode
 		.append("if (scriptWin == null || scriptWin.closed){").append(NL); //$NON-NLS-1$
 	htmlCode
 		.append(
 			"scriptWin = window.open(\"\", \"script\", \"width=800,height=50,resizable\");").append(NL); //$NON-NLS-1$
 	htmlCode.append("scriptWin.document.open(\"text/plain\");").append(NL); //$NON-NLS-1$
 	htmlCode.append("}").append(NL); //$NON-NLS-1$
 	htmlCode.append("scriptWin.focus();").append(NL); //$NON-NLS-1$
 	htmlCode.append("msg = msg.replace(/#/g, '\"');").append(NL); //$NON-NLS-1$
 	htmlCode.append("scriptWin.document.writeln(msg);").append(NL); //$NON-NLS-1$        
 	htmlCode.append("}").append(NL); //$NON-NLS-1$        
 	htmlCode.append("</SCRIPT>").append(NL); //$NON-NLS-1$        
 	htmlCode.append("</HEAD>").append(NL); //$NON-NLS-1$
 	htmlCode.append("<body>").append(NL); //$NON-NLS-1$
 	htmlCode.append("<h1>Query Test Results</h1>").append(NL); //$NON-NLS-1$
 	htmlCode.append("<table border=\"1\">").append(NL); //$NON-NLS-1$
 
 	addTableRow(htmlCode, "StartTime", new Date(testStartTS).toString()); //$NON-NLS-1$
 	addTableRow(htmlCode, "EndTime", new Date(endTS).toString()); //$NON-NLS-1$
 	addTableRow(htmlCode,
 		"Elapsed Time", ((endTS - testStartTS) / 1000) + " seconds"); //$NON-NLS-1$ //$NON-NLS-2$
 	addTableRow(htmlCode,
 		"Number Of Clients", String.valueOf(numberOfClients)); //$NON-NLS-1$
 
 	Map passFailGenMap = getPassFailGen(results);
 	addTableRow(htmlCode,
 		"Number of Queries:", passFailGenMap.get("queries")); //$NON-NLS-1$ //$NON-NLS-2$
 	addTableRow(htmlCode, "Number Passed    :", passFailGenMap.get("pass")); //$NON-NLS-1$ //$NON-NLS-2$
 	addTableRow(htmlCode, "Number Failed    :", passFailGenMap.get("fail")); //$NON-NLS-1$ //$NON-NLS-2$
 	//       addTableRow(htmlCode, "Number Generated :", passFailGenMap.get("gen")); //$NON-NLS-1$ //$NON-NLS-2$
 
 	ResponseTimes responseTimes = calcQueryResponseTimes(results);
 	addTableRow(htmlCode, "QPS :", Double.toString(responseTimes.qps)); //$NON-NLS-1$ 
 	//        addTableRow(htmlCode, "Ave First Resp   :", Double.toString(responseTimes.first)); //$NON-NLS-1$ 
 	//        addTableRow(htmlCode, "Ave Full Resp    :", Double.toString(responseTimes.full)); //$NON-NLS-1$ 
 
 	htmlCode.append("</table> <p>").append(NL); //$NON-NLS-1$
 	htmlCode.append("<table border=\"1\">").append(NL); //$NON-NLS-1$
 
 	// Add table headers
 	htmlCode.append("<tr style=\"background: #C0C0C0 \">"); //$NON-NLS-1$
 
 	addTableData(htmlCode, "QueryId"); //$NON-NLS-1$
 	addTableData(htmlCode, "Result"); //$NON-NLS-1$
 	addTableData(htmlCode, "First Response"); //$NON-NLS-1$
 	addTableData(htmlCode, "Total Seconds"); //$NON-NLS-1$
 	addTableData(htmlCode, "Exception"); //$NON-NLS-1$
 	addTableData(htmlCode, "Error File (if any)"); //$NON-NLS-1$
 	htmlCode.append("</tr>").append(NL); //$NON-NLS-1$
 
 	Iterator resultItr = results.iterator();
 	while (resultItr.hasNext()) {
 	    TestResult stat = (TestResult) resultItr.next();
 	    htmlCode.append("<tr>").append(NL); //$NON-NLS-1$            
 	    addTableDataLink(htmlCode, stat.getQueryID(),
 		    "show('" + scrub(stat.getQuery()) + "')"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 	    addTableData(htmlCode, stat.getResultStatusString(),
 		    "fail".equalsIgnoreCase(stat.getResultStatusString())); //$NON-NLS-1$
 	    addTableData(htmlCode, new Date(stat.getBeginTS()).toString());
 		    
 	//	    Long.toString(stat.getBeginTS()));
 	    addTableData(htmlCode, Long.toString(  (stat.getEndTS() - stat.getBeginTS() / 1000 )));
 		    //Long.toString(stat.getEndTS()));
 	    if (stat.getStatus() == TestResult.RESULT_STATE.TEST_EXCEPTION) {
 		addTableData(htmlCode, stat.getExceptionMsg());
 		if (stat.getErrorfile() != null
 			&& !stat.getErrorfile().equals("null")) { //$NON-NLS-1$
 		    addTableDataLink(htmlCode, stat.getErrorfile(), ""); //$NON-NLS-1$ 
 		} else {
 		    addTableData(htmlCode, ""); //$NON-NLS-1$
 		}
 	    } else {
 		addTableData(htmlCode, ""); //$NON-NLS-1$
 		addTableData(htmlCode, ""); //$NON-NLS-1$                                
 	    }
 	    htmlCode.append("</tr>").append(NL); //$NON-NLS-1$
 	}
 	htmlCode.append("</table>").append(NL); //$NON-NLS-1$
 	outputStream.print(htmlCode.toString());
     }
 
     private static void addTableRow(StringBuffer table, String column,
 	    Object msg) {
 	table.append("<tr>").append(NL); //$NON-NLS-1$        
 	addTableData(table, column); //$NON-NLS-1$
 	addTableData(table, msg.toString());
 	table.append("</tr>").append(NL); //$NON-NLS-1$        
     }
 
     private static void addTableData(StringBuffer table, String msg) {
 	addTableData(table, msg, false);
     }
 
     private static void addTableDataLink(StringBuffer table, String link,
 	    String jsEvent) {
 	if (link.indexOf(".") == -1) //$NON-NLS-1$
 	    table
 		    .append("<td>").append("<a href=\"#" + link + "\" onclick=\"" + jsEvent + "\">" + link + "</a>").append("</td>").append(NL); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
 	else
 	    table
 		    .append("<td>").append("<a href=\"" + link + "\" onclick=\"" + jsEvent + "\">" + link + "</a>").append("</td>").append(NL); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$         
     }
 
     private static void addTableData(StringBuffer table, String msg,
 	    boolean error) {
 	if (error)
 	    table
 		    .append("<td style=\"background: #ffccff \">").append(msg).append("</td>").append(NL); //$NON-NLS-1$ //$NON-NLS-2$
 	else
 	    table.append("<td>").append(msg).append("</td>").append(NL); //$NON-NLS-1$ //$NON-NLS-2$
     }
 
     /**
      * @param results
      * @return
      * @since 4.2
      */
     private static ResponseTimes calcQueryResponseTimes(Collection queryResults) {
 	ResponseTimes responseTimes = new ResponseTimes();
 	int nQueries = 0;
 	double startTS;
 	// double firstResponseTimeStamp;
 	double fullResponseTimeStamp;
 	double totalSecs = 0.0;
 	double totalFullMilliSecs = 0.0;
 	// double totalFirstMilliSecs = 0.0;
 
 	for (Iterator resultItr = queryResults.iterator(); resultItr.hasNext();) {
 	    TestResult result = (TestResult) resultItr.next();
 	    ++nQueries;
 
 	    startTS = result.getBeginTS();
 	    // firstResponseTimeStamp = result.getBeginTS();
 	    fullResponseTimeStamp = result.getEndTS();
 	    totalSecs += ((fullResponseTimeStamp - startTS) / 1000);
 
 	    // totalFirstMilliSecs += (firstResponseTimeStamp - startTS);
 	    totalFullMilliSecs += (fullResponseTimeStamp - startTS);
 	}
 
 	responseTimes.qps = (totalSecs > 0 ? nQueries / totalSecs : -1.0);
 	// responseTimes.first = (nQueries > 0 ? totalFirstMilliSecs / nQueries
 	// : -1.0);
 	responseTimes.full = (nQueries > 0 ? totalFullMilliSecs / nQueries
 		: -1.0);
 	return responseTimes;
     }
 
     private static String scrub(String str) {
 	// Scrub the query
 	if (str != null) {
 	    str = str.replace('"', '#');
 	    str = str.replace('\'', '#');
 	}
 	return str;
     }
 
     /**
      * @param outputStream
      * @param formatter
      * @param stat
      */
     private static void writeQueryResult(PrintStream outputStream,
 	    SimpleDateFormat formatter, TestResult stat) {
 	outputStream.print(stat.getQueryID());
 	outputStream.print(","); //$NON-NLS-1$
 	outputStream.print(stat.getResultStatusString());
 	outputStream.print(","); //$NON-NLS-1$
 	outputStream.print(stat.getBeginTS());
 	outputStream.print(","); //$NON-NLS-1$
 	outputStream.print(stat.getEndTS());
 	outputStream.print(","); //$NON-NLS-1$
 	outputStream.print(getFormattedTimestamp(formatter, stat.getBeginTS()));
 	//        outputStream.print(","); //$NON-NLS-1$
 	// outputStream.print(getFormattedTimestamp(formatter,
 	// stat.getFirstRepsonseTimeStamp()));
 	outputStream.print(","); //$NON-NLS-1$
 	outputStream.print(getFormattedTimestamp(formatter, stat.getEndTS()));
 	outputStream.print(","); //$NON-NLS-1$
 	outputStream
 		.println((stat.getStatus() != TestResult.RESULT_STATE.TEST_SUCCESS ? stat
 			.getExceptionMsg()
 			: "")); //$NON-NLS-1$
     }
 
     private static String getFormattedTimestamp(SimpleDateFormat format,
 	    long millis) {
 	return format.format(new Date(millis));
     }
 
     private static class ResponseTimes {
 	double first; // millis
 	double full; // millis
 	double qps; // secs
     }
 
 }
