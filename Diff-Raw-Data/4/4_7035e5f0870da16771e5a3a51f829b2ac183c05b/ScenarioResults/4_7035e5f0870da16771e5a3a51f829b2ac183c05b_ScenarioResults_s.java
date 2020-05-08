 /*******************************************************************************
  * Copyright (c) 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.test.performance.ui;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Iterator;
 
 import junit.framework.AssertionFailedError;
 
 import org.eclipse.test.internal.performance.data.Dim;
 import org.eclipse.test.internal.performance.db.Scenario;
 import org.eclipse.test.internal.performance.db.TimeSeries;
 import org.eclipse.test.performance.ui.Utils.ConfigDescriptor;
 
 public class ScenarioResults {
 	private Scenario[] scenarios;
 	private String baseline;
 	private String baselinePrefix = null;
 	private String current;
 	private ArrayList pointsOfInterest;
 	private ArrayList buildIDStreamPatterns;
 	private Hashtable scenarioComments;
 	private Hashtable variabilityData;
 	private ConfigDescriptor configDescriptor;
 	/**
 	 * Summary of results for a scenario for a given build compared to a
 	 * reference.
 	 * 
 	 * @param scenarios -
 	 *            the array of Scenario objects for which to generate results.
 	 * @param reference -
 	 *            the reference build ID
 	 * @param current -
 	 *            the current buildID
 	 * @param configDescriptor -
 	 *            a ConfigDescriptor object.
 	 * @param pointsOfInterest -
 	 *            an ArrayList of buildId's to highlight on line graphs.
 	 */
 	public ScenarioResults(Utils.ConfigDescriptor configDescriptor, Scenario[] scenarios, String baseline, String baselinePrefix, String current, 
 			ArrayList pointsOfInterest, Hashtable scenarioComments, ArrayList buildIDPatterns, Hashtable variabilityTable) {
 		
 		this.scenarios = scenarios;
 		this.baseline = baseline;
 		this.baselinePrefix = baselinePrefix;
 		this.pointsOfInterest = pointsOfInterest;
 		this.scenarioComments = scenarioComments;
 		this.configDescriptor=configDescriptor;
 		buildIDStreamPatterns=buildIDPatterns;
 		this.current = current;
 		variabilityData=variabilityTable;
 
 		printSummary();
 		
 		printDetails();
 	}
 
 	private void printSummary() {
 		String outFile = null;
 		PrintStream ps = null;
 
 		for (int s = 0; s < scenarios.length; s++) {
 			ArrayList pointsOfInterest = new ArrayList();
 			Scenario t = scenarios[s];
 
 			// get latest points of interest matching
 			if (this.pointsOfInterest != null) {
 				Iterator iterator = this.pointsOfInterest.iterator();
 				while (iterator.hasNext()) {
 					String buildIdPattern = iterator.next().toString();
 
 					if (buildIdPattern.endsWith("*")) {
 						pointsOfInterest.addAll(getAllMatchingBuildIds(t, buildIdPattern.substring(0, buildIdPattern.length() - 1)));
 					} else {
 						String match = getMostRecentMatchingBuildID(t, buildIdPattern);
 						if (match != null)
 							pointsOfInterest.add(match);
 					}
 				}
 			}
 
 			int[] buildNameIndeces = { -1, -1 };
 			buildNameIndeces[0] = Utils.getBuildNameIndex(t.getTimeSeriesLabels(), current);
 			buildNameIndeces[1] = Utils.getBuildNameIndex(t.getTimeSeriesLabels(), baseline);
 			// don't produce result if none exists for current build
 			if (Utils.getBuildNameIndex(t.getTimeSeriesLabels(), current) == -1) {
 				continue;
 			}
 
 			String scenarioFileName = t.getScenarioName().replace('#', '.').replace(':', '_').replace('\\', '_');
 			outFile = configDescriptor.outputDir + "/" + scenarioFileName + ".html";
 			String rawDataFile=scenarioFileName+"_raw.html";
 			if (outFile != null) {
 				try {
 					new File(outFile).getParentFile().mkdirs();
 					ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(outFile)));
 				} catch (FileNotFoundException e) {
 					System.err.println("can't create output file" + outFile); //$NON-NLS-1$
 				}
 			}
 			if (ps == null)
 				ps = System.out;
 			ps.println(Utils.HTML_OPEN);
 			ps.println(Utils.HTML_DEFAULT_CSS);
 			ps.println("<title>" + t.getScenarioName() + "(" + configDescriptor.description + ")" + "</title></head>"); //$NON-NLS-1$
 			ps.println("<h4>Scenario: " + t.getScenarioName() + " (" + configDescriptor.description + ")</h4><br>"); //$NON-NLS-1$ //$NON-NLS-2$
 
 			if (scenarioComments.containsKey(t.getScenarioName())) {
 				ps.println("<b>Notes</b><br>\n");
 				ps.println("<table><tr><td>" + scenarioComments.get(t.getScenarioName()) + "</td></tr></table><br>\n");
 			}
 
 			ps.println("<b>Click measurement name to view line graph of measured values over builds.</b><br><br>\n");
 			ps.println("<table border=\"1\">"); //$NON-NLS-1$ //$NON-NLS-2$
 
 			Dim[] dimensions = filteredDimensions(t.getDimensions());
 			try {
 				ps.println("<tr><td>Build Id</td>"); //$NON-NLS-1$
 				for (int i = 0; i < dimensions.length; i++) {
 					Dim dim = dimensions[i];
 					String dimName = dim.getName();
 					ps.println("<td><a href=\"#" + configDescriptor.name + "_" + scenarioFileName + "_" + dimName + "\">" + dimName + "</a></td>");
 				}
 				ps.print("</tr>\n");
 
 				// store current and reference values for diff later
 				double[][] diffValues = new double[2][dimensions.length];
 				// to determine if diff is possible
 				boolean[] refValueExistance = new boolean[dimensions.length];
 
 				for (int j = 0; j < 2; j++) {
 					String referenceIndicator = (j == 1) ? "(reference)" : "";
 
 					String buildName = ((buildNameIndeces[j] == -1) ? "n/a" : t.getTimeSeriesLabels()[buildNameIndeces[j]]) + referenceIndicator;
 					ps.print("<tr><td>" + buildName + "</td>");
 
 					for (int i = 0; i < dimensions.length; i++) {
 						Dim dim = dimensions[i];
 
 						TimeSeries ts = t.getTimeSeries(dim);
 						if (j == 1 && buildNameIndeces[j] != -1)
 							refValueExistance[i] = true;
 
 						if (buildNameIndeces[j] != -1)
 							diffValues[j][i] = ts.getValue(buildNameIndeces[j]);
 
 						String displayValue = (buildNameIndeces[j] == -1) ? "n/a" : dim.getDisplayValue(ts.getValue(buildNameIndeces[j]));
 						String stddev = (buildNameIndeces[j] == -1) ? "0" : dim.getDisplayValue(ts.getStddev(buildNameIndeces[j]));
 
 						if (stddev.startsWith("0 ") || stddev.equals("0"))
 							ps.println("<td>" + displayValue + "</td>");
 						else
 							ps.println("<td>" + displayValue + " [" + stddev + "]" + "</td>");
 					}
 					ps.print("</tr>");
 				}
 				// get diffs and print results
 				ps.println(getDiffs(diffValues, dimensions, refValueExistance));
 				ps.println();
 				ps.println("</font></table>");
 				ps.println("*Delta values in red and green indicate degradation > 10% and improvement > 10%,respectively.<br><br>");
 				ps.println("<br><hr>\n\n");
 
 				// print text legend.
 				ps.println("Black and yellow points plot values measured in integration and last seven nightly builds.<br>\n" + "Magenta points plot the repeated baseline measurement over time.<br>\n"
 						+ "Boxed points represent previous releases, milestone builds, current reference and current build.<br><br>\n" 
 						+ "Hover over any point for build id and value.\n");
 
 				//print link to raw data.
 				ps.println("<br><br><b>Click <a href=\""+rawDataFile+"\">here</a> to view raw data and stats.</b>" +
 						"<br>The raw data and stats include data for all current stream builds and all baseline test runs.<br>\n");
 
 				// print image maps of historical
 				for (int i = 0; i < dimensions.length; i++) {
 					Dim dim = dimensions[i];
 					String dimName = dim.getName();
 
 					TimeLineGraph lg = Utils.getLineGraph(t, dim.getName(), baseline, baselinePrefix, current, pointsOfInterest,buildIDStreamPatterns);
 
 					String lgImg = configDescriptor.outputDir + "/graphs/" + scenarioFileName + "_" + dimName + ".gif";
 					Utils.printLineGraphGif(lg, lgImg);
 					ps.println("<br><a name=\"" + configDescriptor.name + "_" + scenarioFileName + "_" + dimName + "\"></a>");
 					ps.println("<br><b>" + dimName + "</b><br>");
 					ps.println(Utils.getDimensionDescription(dimName) + "<br><br>\n");
 					ps.println(Utils.getImageMap(lg, "graphs/" + scenarioFileName + "_" + dimName + ".gif",scenarioFileName+"_raw.html"));
 					}
 				ps.println("<br><br></body>");
 				ps.println(Utils.HTML_CLOSE); //$NON-NLS-1$
 				if (ps != System.out)
 					ps.close();
 
 			} catch (AssertionFailedError e) {
 				e.printStackTrace();
 				continue;
 			}
 		}
 	}
 
 	private void printDetails() {
 
 		String outFile = null;
 		PrintStream ps = null;
 
 		for (int s = 0; s < scenarios.length; s++) {
 			Scenario t = scenarios[s];
 			// don't produce result if none exists for current build
 			if (Utils.getBuildNameIndex(t.getTimeSeriesLabels(), current) == -1) {
 				continue;
 			}
 			Dim[] dimensions = filteredDimensions(t.getDimensions());
 			RawDataTable currentResultsTable=new RawDataTable(t,dimensions,buildIDStreamPatterns,current);
 			
 			//create table for baseline data
 			RawDataTable baselineResultsTable=new RawDataTable(t,dimensions,baselinePrefix,baseline);
 			
 			//store cv for aggregate data
 			Hashtable data=(Hashtable)variabilityData.get(t.getScenarioName());
 			if (data==null){
 				data=new Hashtable();
 				variabilityData.put(t.getScenarioName(),data);
 			}
 			data.put("cConfig-"+configDescriptor.name,currentResultsTable.getCV());
 			data.put("bConfig-"+configDescriptor.name,baselineResultsTable.getCV());
 
 			String scenarioFileName = t.getScenarioName().replace('#', '.').replace(':', '_').replace('\\', '_');
 			outFile = configDescriptor.outputDir + "/" + scenarioFileName + "_raw.html";
 			if (outFile != null) {
 				try {
 					new File(outFile).getParentFile().mkdirs();
 					ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(outFile)));
 				} catch (FileNotFoundException e) {
 					System.err.println("can't create output file" + outFile); //$NON-NLS-1$
 				}
 			}
 			if (ps == null)
 				ps = System.out;
 			ps.println(Utils.HTML_OPEN);
 			ps.println(Utils.HTML_DEFAULT_CSS);
			ps.println("<title>" + t.getScenarioName() + "(" + configDescriptor.name + ")" + " - Details</title></head>"); //$NON-NLS-1$
			ps.println("<h4>Scenario: " + t.getScenarioName() + " (" + configDescriptor.name + ")</h4>"); //$NON-NLS-1$
 			ps.println("<a href=\""+scenarioFileName+".html\">VIEW GRAPH</a><br><br>"); //$NON-NLS-1$
 			ps.println("<table><td><b>Current Stream Test Runs</b></td><td><b>Baseline Test Runs</b></td></tr>\n");
 			ps.println("<tr valign=\"top\">" +
 					"<td>"+currentResultsTable.toHtmlString()+"</td>"
 					+"<td>"+baselineResultsTable.toHtmlString()+"</td></tr>");
 			ps.println("</table>");
 			ps.close();
 		}
 	}
 
 	private String getMostRecentMatchingBuildID(Scenario scenario, String buildIdPrefix) {
 		String[] buildIds = scenario.getTimeSeriesLabels();
 		for (int i = buildIds.length - 1; i > -1; i--) {
 			if (buildIds[i].startsWith(buildIdPrefix))
 				return buildIds[i];
 		}
 		return null;
 	}
 
 	private ArrayList getAllMatchingBuildIds(Scenario scenario, String buildIdPrefix) {
 		ArrayList result = new ArrayList();
 		String[] buildIds = scenario.getTimeSeriesLabels();
 		for (int i = buildIds.length - 1; i > -1; i--) {
 			if (buildIds[i].startsWith(buildIdPrefix) && !buildIds[i].equals(baseline))
 				result.add(buildIds[i]);
 		}
 		return result;
 	}
 
 	private String getDiffs(double[][] values, Dim[] dimensions, boolean[] refValueExistance) {
 		String diffRow = "<tr><td>*Delta</td>";
 		for (int j = 0; j < dimensions.length; j++) {
 			Dim dim = dimensions[j];
 
 			double diffValue = values[0][j] - values[1][j];
 			double diffPercentage = 0;
 			if (values[1][j] != 0)
 				diffPercentage = ((int) (((diffValue / Math.abs(values[1][j])) * 1000))) / 10.0;
 			String diffDisplayValue = dim.getDisplayValue(diffValue);
 			// green
 			String fontColor = "";
 			if ((diffPercentage < -10 && !dim.largerIsBetter()) || (diffPercentage > 10 && dim.largerIsBetter()))
 				fontColor = "#006600";
 			if ((diffPercentage < -10 && dim.largerIsBetter()) || (diffPercentage > 10 && !dim.largerIsBetter()))
 				fontColor = "#FF0000";
 
 			diffPercentage = Math.abs(diffPercentage);
 			String percentage = (diffPercentage == 0) ? "" : "<br>" + diffPercentage + " %";
 
 			if (diffPercentage > 10 || diffPercentage < -10) {
 				diffRow = diffRow.concat("<td><FONT COLOR=\"" + fontColor + "\"><b>" + diffDisplayValue + percentage + "</b></FONT></td>");
 			} else if (refValueExistance[j]) {
 				diffRow = diffRow.concat("<td>" + diffDisplayValue + percentage + "</td>");
 			} else {
 				diffRow = diffRow.concat("<td>n/a</td>");
 			}
 		}
 		diffRow = diffRow.concat("</tr>");
 		return diffRow;
 	}
 
 	private Dim[] filteredDimensions(Dim[] dimensions) {
 		ArrayList list = new ArrayList();
 		ArrayList filtered = new ArrayList();
 		list.add(0, "Elapsed Process");
 		list.add(1, "CPU Time");
 		list.add(2, "Invocation Count");
 		list.add(3, "Kernel time");
 		list.add(4, "Data Size");
 		list.add(5, "Library Size");
 		list.add(6, "GDI Objects");
 		list.add(7, "Text Size");
 
 		// list.add(8,"Used Java Heap");
 		// list.add(9,"Committed");
 		// list.add(10,"Page Faults");
 		// list.add(11,"Hard Page Faults");
 		// list.add(12,"Soft Page Faults");
 
 		for (int i = 0; i < dimensions.length; i++) {
 			String dimName = dimensions[i].getName();
 			if (list.contains(dimName))
 				list.set(list.indexOf(dimName), dimensions[i]);
 		}
 		Iterator iterator = list.iterator();
 		while (iterator.hasNext()) {
 			Object tmp = iterator.next();
 			try {
 				if ((Dim) tmp instanceof Dim)
 					filtered.add(tmp);
 			} catch (ClassCastException e) {
 				//silently ignore
 			}
 		}
 		return (Dim[]) filtered.toArray(new Dim[filtered.size()]);
 	}
 
 }
