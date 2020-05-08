 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.test.performance.ui;
 
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.StringTokenizer;
 
 import org.eclipse.test.internal.performance.PerformanceTestPlugin;
 import org.eclipse.test.internal.performance.data.Dim;
 import org.eclipse.test.internal.performance.db.DB;
 import org.eclipse.test.internal.performance.db.Scenario;
 import org.eclipse.test.internal.performance.db.SummaryEntry;
 import org.eclipse.test.internal.performance.db.TimeSeries;
 import org.eclipse.test.internal.performance.db.Variations;
 import org.eclipse.test.performance.Dimension;
 
 public class ScenarioStatusTable {
 	
 	private Hashtable configMaps=null;
 	private Variations variations;
 	private String scenarioPattern;
 	private ArrayList configNames=new ArrayList();
 	private Hashtable scenarioComments;
 	private SummaryEntry[] fingerprintEntries;
 	private final String baseline;
 	
 	private class ScenarioStatus{
 		Hashtable statusMap;
 		String name, shortName;
 		Hashtable configStatus;
 		Hashtable resultsMap;
 		boolean hasSlowDownExplanation=false;
 		boolean fingerprint = false;
 		boolean hasBaseline = true;
 						
 		public ScenarioStatus(String scenarioName){
 			name = scenarioName;
 			statusMap = new Hashtable();
 			configStatus = new Hashtable();
 			resultsMap = new Hashtable();
 		}
 
 	}
 	
 	/**
 	 * Creates an HTML table of red x/green check for a scenario for each
 	 * configuration.
 	 * 
 	 * @param variations
 	 * @param scenarioPattern
 	 * @param configDescriptors
 	 */
 	public ScenarioStatusTable(Variations variations,String scenarioPattern,Hashtable configDescriptors,Hashtable scenarioComments, SummaryEntry[] fpSummaries, String baseline){
 		configMaps=configDescriptors;
 		this.variations=variations;
 		this.scenarioPattern=scenarioPattern;
 		this.scenarioComments=scenarioComments;
 		this.baseline= baseline;
 		this.fingerprintEntries = fpSummaries;
 	}
 
 	/**
 	 * Prints the HTML representation of scenario status table into the given stream.
 	 */
 	public void print(PrintStream stream, boolean filter) {
 		String OS="config";
         Scenario[] scenarios= DB.queryScenarios(variations, scenarioPattern, OS, null);
 	
 		if (scenarios != null && scenarios.length > 0) {
 			ArrayList scenarioStatusList=new ArrayList();
 
 			for (int i= 0; i < scenarios.length; i++) {
 				Scenario scenario= scenarios[i];
 				String scenarioName=scenario.getScenarioName();
 				if (filter && !Utils.matchPattern(scenarioName, scenarioPattern)) continue;
 				// returns the config names. Making assumption that indices in
 				// the configs array map to the indices of the failure messages.
 				String[] configs=scenario.getTimeSeriesLabels();
 				String[] failureMessages= scenario.getFailureMessages();
 				ScenarioStatus scenarioStatus=new ScenarioStatus(scenarioName);
 				scenarioStatus.fingerprint = Utils.hasSummary(this.fingerprintEntries, scenarioName);
 
 				String scenarioComment= (String)scenarioComments.get(scenarioName);
 				if (scenarioComment != null)
 					scenarioStatus.hasSlowDownExplanation= true;
 
 				int confsLength = configs.length;
 				for (int j=0; j<confsLength; j++){
 					if (!configNames.contains(configs[j]))
 						configNames.add(configs[j]);
 
 					// Compute confidence level and store it in scenario status
 					Variations v = (Variations) variations.clone();
 					v.put(PerformanceTestPlugin.CONFIG, configs[j]);
 //    				double[] resultStats = Utils.resultStats(v, scenarioName, baseline, configs[j]);
 					String current = (String) v.get(PerformanceTestPlugin.BUILD);
 					Dim significanceDimension = (Dim) Dimension.ELAPSED_PROCESS;
 					Scenario newScenario= DB.getScenarioSeries(scenarioName, v, PerformanceTestPlugin.BUILD, baseline, current, new Dim[] { significanceDimension });
 			        String[] timeSeriesLabels= newScenario.getTimeSeriesLabels();
 			        TimeSeries timeSeries = newScenario.getTimeSeries(significanceDimension);
 			        boolean hasBaseline = timeSeriesLabels.length == 2 && timeSeriesLabels[0].equals(baseline);
 			        double[] resultStats = Utils.resultsStatistics(timeSeries);
 					if (resultStats == null) continue;
 					if (resultStats != null && resultStats[1] < 0 && scenarioStatus.hasBaseline) scenarioStatus.hasBaseline = false;
					int confidenceLevel = Utils.confidenceLevel(resultStats);
					scenarioStatus.configStatus.put(configs[j], new Integer(confidenceLevel));
 					
 					// Store failure message in scenario status
 					boolean hasScenarioFailure = failureMessages[j] != null && failureMessages[j].indexOf(configs[j]) != -1; // ensure correct failure message relates to config
 					StringBuffer buffer = new StringBuffer();
 					if (hasScenarioFailure) {
 						buffer.append(failureMessages[j]);
 						if (scenarioStatus.hasSlowDownExplanation) {
 							buffer.append(" - Explanation comment: ");
 							buffer.append(scenarioComment);
 						}
 						confidenceLevel |= Utils.DEV;
 					}
 					scenarioStatus.statusMap.put(configs[j], buffer.toString());
 
 					// Store text for numbers in scenario status
 					String text = Utils.failureMessage(resultStats, false);
 					scenarioStatus.resultsMap.put(configs[j], text);
 					
 					// Store scenario short name in scenario status (for table column)
 					if (scenarioStatus.shortName == null) {
 						if (hasBaseline) { // baseline is OK
 							scenarioStatus.shortName = Utils.getScenarioShortName(scenarioName, -1);
 						} else {
 							StringBuffer shortName = new StringBuffer("*");
 							shortName.append(Utils.getScenarioShortName(scenarioName, -1));
 							shortName.append(" <small>(vs.&nbsp;");
 							shortName.append(timeSeriesLabels[0]);
 							shortName.append(")</small>");
 							scenarioStatus.shortName = shortName.toString();
 						}
 					}
 
 					// Store scenario status
 					if (!scenarioStatusList.contains(scenarioStatus)) {
 						scenarioStatusList.add(scenarioStatus);
 					}
 				}
 			}
 			
 			String label=null;
 			stream.println("<br><h4>Scenario Status</h4>");
 			stream.println("The following table gives a complete but compact view of performance results for the component.<br>");
 			stream.println("Each line of the table shows the results for one scenario on all machines.<br><br>");
 			stream.println("The name of the scenario is in <b>bold</b> when its results are also displayed in the fingerprints<br>");
 			stream.println("and starts with an '*' when the scenario has no results in the last baseline run.<br><br>");
 			stream.println("Here are information displayed for each test (ie. in each cell):");
 			stream.println("<ul>");
 			stream.println("<li>an icon showing whether the test fails or passes and whether it's reliable or not.<br>");
 			stream.println("The legend for this icon is:");
 			stream.println("<ul>");
 			stream.print("<li>Green (<img src=\"");
 			stream.print(Utils.OK_IMAGE);
 			stream.print("\">): mark a <b>successful result</b>, which means this test has neither significant performance regression nor significant standard error</li>");
 			stream.print("<li>Red (<img src=\"");
 			stream.print(Utils.FAIL_IMAGE);
 			stream.println("\">): mark a <b>failing result</b>, which means this test shows a significant performance regression (more than 10%)</li>");
 			stream.print("<li>Gray (<img src=\"");
 			stream.print(Utils.FAIL_IMAGE_EXPLAINED);
 			stream.println("\">): mark a <b>failing result</b> (see above) with a comment explaining this degradation.</li>");
 			stream.print("<li>Yellow (<img src=\"");
 			stream.print(Utils.FAIL_IMAGE_WARN);
 			stream.print("\"> or <img src=\"");
 			stream.print(Utils.OK_IMAGE_WARN);
 			stream.print("\">): mark a <b>failing or successful result</b> with a significant standard error (more than ");
 			stream.print(Utils.STANDARD_ERROR_THRESHOLD_STRING);
 			stream.println(")</li>");
 			stream.print("<li>Black (<img src=\"");
 			stream.print(Utils.UNKNOWN_IMAGE);
 			stream.print("\">): mark an <b>undefined result</b>, which means that deviation on this test is not a number (<code>NaN</code>) or is infinite (happens when the reference value is equals to 0!)</li>");
 			stream.println("<li>\"n/a\": mark a test for with <b>no</b> performance results</li>");
 			stream.println("</ul></li>");
 			stream.println("<li>the value of the deviation from the baseline as a percentage (ie. formula is: <code>(build_test_time - baseline_test_time) / baseline_test_time</code>)</li>");
 			stream.println("<li>the value of the standard error of this deviation as a percentage (ie. formula is: <code>sqrt(build_test_stddev^2 / N + baseline_test_stddev^2 / N) / baseline_test_time</code>)<br>");
 			stream.println("When test only has one measure, the standard error cannot be computed and is replaced with a '<font color=\"#CCCC00\">[n/a]</font>'.</li>");
 			stream.println("</ul>");
 			stream.println("<u>Hints</u>:<ul>");
 			stream.println("<li>fly over image of failing tests to see the complete error message</li>");
 			stream.println("<li>to look at the complete and detailed test results, click on its image</li>");
 			stream.println("</ul>");
 			stream.println();
 			stream.println("<table border=\"1\">");
 			stream.println("<tr>");
 			stream.print("<td><h4>All ");
 			stream.print(scenarios.length);
 			stream.println(" scenarios</h4></td>");
 
 			for (int i= 0; i < configNames.size(); i++){
 				label = configNames.get(i).toString();
 				String columnTitle = label;
 				if (configMaps!=null) {
 					Utils.ConfigDescriptor configDescriptor= (Utils.ConfigDescriptor)configMaps.get(label);
 					if (configDescriptor != null) {
 						int idx = configDescriptor.description.indexOf('(');
 						if (idx < 0) {
 							columnTitle = configDescriptor.description;
 						} else {
 							// first line
     						StringTokenizer tokenizer = new StringTokenizer(configDescriptor.description.substring(0, idx).trim(), " ");
     						StringBuffer buffer = new StringBuffer(tokenizer.nextToken());
     						while (tokenizer.hasMoreTokens()) {
     							buffer.append("&nbsp;");
     							buffer.append(tokenizer.nextToken());
     						}
     						buffer.append(' ');
     						// second line
     						tokenizer = new StringTokenizer(configDescriptor.description.substring(idx).trim(), " ");
     						buffer.append(tokenizer.nextToken());
     						while (tokenizer.hasMoreTokens()) {
     							buffer.append("&nbsp;");
     							buffer.append(tokenizer.nextToken());
     						}
     						columnTitle = buffer.toString();
 						}
 					}
 				}
 				stream.print("<td><h5>");
 				stream.print(columnTitle);
 				stream.println("</h5>");
 			}
 			
 			// counter for js class Id's
 			int jsIdCount=0;
 			for (int j= 0; j < scenarioStatusList.size(); j++) {
 				
 				ScenarioStatus status=(ScenarioStatus)scenarioStatusList.get(j);
 
 				stream.println("<tr>");
 				stream.print("<td>");
 				if (status.fingerprint) stream.print("<b>");
 				if (!status.hasBaseline) stream.print("*");
 //				stream.print(status.name.substring(status.name.indexOf(".",status.name.indexOf(".test")+1)+1));
 				stream.print(status.shortName);
 				if (!status.hasBaseline) stream.print("</i>");
 				if (status.fingerprint) stream.print("</b>");
 				stream.println();
 				for (int i=0;i<configNames.size();i++){
 					String configName=configNames.get(i).toString();
 					String aUrl=configName;
 					if (status.statusMap.containsKey(configName)){
 						String message = (String) status.statusMap.get(configName);
 						int confidence = ((Integer) status.configStatus.get(configName)).intValue();
 						String image = Utils.getImage(confidence, status.hasSlowDownExplanation);
 						stream.print("<td><a ");
 						if ((confidence & Utils.DEV) == 0 || (confidence & Utils.NAN) != 0 || message.length() == 0){
 							// write deviation with error in table when test pass
 							stream.print("href=\"");
 							stream.print(aUrl);
 							stream.print('/');
 							stream.print(status.name.replace('#', '.').replace(':', '_').replace('\\', '_'));
 							stream.println(".html\">");
 							stream.print("<img hspace=\"10\" border=\"0\" src=\"");
 							stream.print(image);
 							stream.println("\"/></a>");
 						} else {
 							// create message with tooltip text including deviation with error plus failure message
 							jsIdCount+=1;
 							stream.print("class=\"tooltipSource\" onMouseover=\"show_element('toolTip");
 							stream.print(jsIdCount);
 							stream.print("')\" onMouseout=\"hide_element('toolTip");
 							stream.print(jsIdCount);
 							stream.print("')\" \nhref=\"");
 							stream.print(aUrl);
 							stream.print('/');
 							stream.print(status.name.replace('#', '.').replace(':', '_').replace('\\', '_'));
 							stream.println(".html\">");
 							stream.print("<img hspace=\"10\" border=\"0\" src=\"");
 							stream.print(image);
 							stream.println("\"/>");
 							stream.print("<span class=\"hidden_tooltip\" id=\"toolTip");
 							stream.print(jsIdCount);
 							stream.print("\">");
 							stream.print(message);
 							stream.println("</span></a>");
 						}
 						String result = (String) status.resultsMap.get(configName);
 						stream.println(result);
 					}else{
 						stream.println("<td> n/a");
 					}
 				}
 				stream.flush();
 			}
 			stream.println("</table>");
 		}
     }
 }
