 /*
  * Copyright 2012 Luis Rodero-Merino.
  * 
  * This file is part of Onodrim.
  * 
  * Onodrim is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Onodrim is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Onodrim.  If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 
 package org.onodrim;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * It organizes jobs results in a table (configured using {@link ResultsTableConf} ). 
  * @author Luis Rodero-Merino
  * @since 1.0
  */
 public class ResultsTable {
 
     private static Logger logger = Logger.getLogger(ResultsTable.class.getCanonicalName());
     
     /**
      * The contents of the table itself, both labels and values.
      */
     String[][] cells = null;
     /**
      * 'Header rows' is the subset of rows whose cells are meant to contain 'labels' and not values, typically those including labels and not values.
      */
     Set<Integer> headerRows = new HashSet<Integer>();
     /**
      * Similar to {@link #headerRows}, but referred to columns
      */
     Set<Integer> headerColumns = new HashSet<Integer>();
 
     /**
      * Prints the table as an HTML table, cells belonging to header rows and columns (labels) will be printed using {@code \<th\>}, while
      * values will be printed using {@code \<td\>}.   
      * @return The table contents as an HTML table.
      */
     public String toHTML() {
         
         if(cells == null)
             return null;
 
         StringBuilder tableBuilder = new StringBuilder();
         tableBuilder.append("<table border=\"0\">\n");
         for(int i = 0; i < cells.length; i++) {
             tableBuilder.append("<tr border=\"0\">");
             for(int j = 0; j < cells[0].length; j++) {
                 if(headerRows.contains(i) || headerColumns.contains(j))
                     tableBuilder.append("<th align=\"right\" border=\"0\" bgcolor=\"#AAAAAA\">" + cells[i][j] + "</th>"); 
                 else
                     tableBuilder.append("<td align=\"right\" border=\"0\">" + cells[i][j] + "</td>");      
             }
             tableBuilder.append("</tr>\n");
         }
         tableBuilder.append("</table>");
 
         return tableBuilder.toString();
     }
 
     /**
      * Returns the results table as a single string, cells in a row separated by tabs '\t', rows separated by new line '\n'.
      */
     @Override
     public String toString() {
         
         if(cells == null)
             return null;
         
         StringBuilder tableBuilder = new StringBuilder();
 
         for(int i = 0; i < cells.length; i++) {
             tableBuilder.append(cells[i][0]);
             for(int j = 1; j < cells[i].length; j++)
                 tableBuilder.append("\t" + cells[i][j]);
             tableBuilder.append("\n");
         }
         
         return null;
     }
     
     /**
      * This method filters out failed jobs, then reads the configuration of the remaining ones
      * to prepare the parameters to call iteratively to {@link #buildResultsTable(ResultsTableConf, Map, Map)}
      * (once per table configuration in the list).
      * @param jobs
      * @param tablesConfs
     * @return The {@link ResultsTable}s created from the results of the jobs, one per each {@link ResultsTableConf}
      * in the list passed as parameter.
      */
     public static List<ResultsTable> buildResultsTables(List<Job> jobs, List<ResultsTableConf> tablesConfs) {
         
         List<ResultsTable> tables = new ArrayList<ResultsTable>();
 
         // First, filtering successful jobs
         List<Job> successfulJobs = new ArrayList<Job>();
         for(Job job: jobs)
             if(!job.discarded() && !job.executionFailed())
                 successfulJobs.add(job);
         
         if(successfulJobs.isEmpty())
             return tables;
             
         // Second, we build a map that associates each configuration parameter to a list with its values; this
         // is made for convenience, as it will be an useful construction for the next step
         Map<String, Collection<String>> paramsValues = new HashMap<String, Collection<String>>();
         // Also, we must organize results in a map that associates
         // each experiment results with their configuration
         Map<Properties, Collection<Properties>> resultsMap = new HashMap<Properties, Collection<Properties>>();
         for(Job job: successfulJobs) {
             Configuration jobConf = job.getConfiguration();
             for(String parameterName: jobConf.getParameterNames()) {
                 Collection<String> values = paramsValues.get(parameterName);
                 if(values == null) {
                     values = new LinkedHashSet<String>();
                     paramsValues.put(parameterName, values);
                 }
                 values.add(jobConf.getParameter(parameterName));
             }
             Properties resultsProperties = new Properties();
             for(String resultName: job.getResults().keySet())
                 resultsProperties.put(resultName, job.getResults().get(resultName));
             
             Collection<Properties> resultsForConfig = resultsMap.get(job.getConfiguration());
             if(resultsForConfig == null) {
                 resultsForConfig = new ArrayList<Properties>();
                 resultsMap.put(job.getConfiguration(), resultsForConfig);
             }
             resultsForConfig.add(resultsProperties);
         }
 
         // Finally, building tables
         for(ResultsTableConf tableConf: tablesConfs)
             tables.add(buildResultsTable(tableConf, paramsValues, resultsMap));
         
         return tables;
 
     }
     
     /**
      * This method just creates a list to store the table configuration instance and then
      * calls to {@link #buildResultsTables(List, List)}. That call will return a list with
      * a single table, which will be returned to the caller.
      * @param jobs
      * @param resultsTableConf
     * @return A {@link ResultsTable}s created from the results of the jobs, giving the instructions
      * in the {@link ResultsTableConf} instance passed as parameter.
      */
     public static ResultsTable buildResultsTable(List<Job> jobs, ResultsTableConf resultsTableConf) {
     	List<ResultsTableConf> tablesConfs = new ArrayList<ResultsTableConf>();
     	tablesConfs.add(resultsTableConf);
     	List<ResultsTable> tables = buildResultsTables(jobs, tablesConfs);
     	return tables.get(0);
     }
     
     /**
      * It creates a {@link ResultsTable} instance that contains the results passed as parameter. This
      * factory builds the matrix of Strings once, making it available for subsequent calls to print
      * functions (e.g. {@link #toHTML()}).
      * @param resultsTableConf Configuration of the table to print
      * @param paramsValues All possible values of the parameters used
      * @param resultsMap Association of parameters to results
      * @return An instance containing all the info required to build the table, or null if the table
      *         cannot be built, for example there are no values associated to any of the rows or columns given.
      */
     public static ResultsTable buildResultsTable(ResultsTableConf resultsTableConf,
                                                  Map<String, Collection<String>> paramsValues,
                                                  Map<Properties, Collection<Properties>> resultsMap) {
         
         if(resultsTableConf == null)
             throw new IllegalArgumentException("Cannot build " + ResultsTable.class.getName() + " instance without the proper configuration");
         
         if(paramsValues == null)
             throw new IllegalArgumentException("Cannot build " + ResultsTable.class.getName() + " instance without a mapping of params names and their values");
 
         if(resultsMap == null)
             throw new IllegalArgumentException("Cannot build " + ResultsTable.class.getName() + " instance without a mapping of results for each experiment");
         
         List<String[]> rowParamsNamesList = new ArrayList<String[]>();
         List<String[][]> rowParamsCombinedValuesList = new ArrayList<String[][]>();
         for(String[] rowParamsByConf: resultsTableConf.getRowParamsSets()) {
             List<String[]> rowParamsValues = new ArrayList<String[]>();
             for(int rowParamIndex = 0; rowParamIndex < rowParamsByConf.length; rowParamIndex++) {
                 // We will add to the table only rows that correspond to some parameter in the experiments configurations
                 if(!paramsValues.containsKey(rowParamsByConf[rowParamIndex]))
                     continue;
                 rowParamsValues.add(paramsValues.get(rowParamsByConf[rowParamIndex]).toArray(new String[]{}));
             }
             if(rowParamsValues.isEmpty()) {
                 logger.log(Level.WARNING, "Cannot build table printer, parameters for rows " + Arrays.toString(rowParamsByConf) + " have no corresponding values");
                 return null;
             }
             rowParamsNamesList.add(rowParamsByConf);
             String[][] rowParamsCombinedValues = Util.Collections.product(rowParamsValues.toArray(new String[][]{}), String.class);
             rowParamsCombinedValuesList.add(rowParamsCombinedValues);
         }
 
         List<String[]> columnParamsNamesList = new ArrayList<String[]>();
         List<String[][]> columnParamsCombinedValuesList = new ArrayList<String[][]>();
         for(String[] columnParamsByConf: resultsTableConf.getColumnParamsSets()) {
             List<String[]> columnParamsValues = new ArrayList<String[]>();
             for(int columnParamIndex = 0; columnParamIndex < columnParamsByConf.length; columnParamIndex++) {
                 // We will add to the table only columns that correspond to some parameter in the exps confs
                 if(!paramsValues.containsKey(columnParamsByConf[columnParamIndex]))
                     continue;
                 columnParamsValues.add(paramsValues.get(columnParamsByConf[columnParamIndex]).toArray(new String[]{}));
             }
             if(columnParamsValues.isEmpty()) {
                 logger.log(Level.WARNING, "Cannot build table printer, parameters for columns " + Arrays.toString(columnParamsByConf) + " have no corresponding values");
                 return null;
             }
             columnParamsNamesList.add(columnParamsByConf);
             String[][] columnParamsCombinedValues = Util.Collections.product(columnParamsValues.toArray(new String[][]{}), String.class);
             columnParamsCombinedValuesList.add(columnParamsCombinedValues);
         }
 
         
         //// Build subtable of results for each combination of parameters rows and values
         String[][][][] allResultsSubtables = new String[columnParamsNamesList.size()][rowParamsNamesList.size()][][];
         for(int i = 0; i< columnParamsNamesList.size(); i++)
             for(int j = 0; j < rowParamsNamesList.size(); j++)
                 allResultsSubtables[i][j] = buildResultsSubtable(columnParamsNamesList.get(i), columnParamsCombinedValuesList.get(i),
                                                                  rowParamsNamesList.get(j), rowParamsCombinedValuesList.get(j),
                                                                  resultsTableConf.getResultToStore(), resultsTableConf.getResultsComputationMethod(),
                                                                  resultsMap);
         
         //// Computing total table size
         // First getting top and left headers size
         int topHeaderHeight = 0;
         for(String[] rowParams: rowParamsNamesList)
             if(rowParams.length > topHeaderHeight)
                 topHeaderHeight = rowParams.length;
         int leftHeaderWidth = 0;
         for(String[] columnParams: columnParamsNamesList)
             if(columnParams.length > leftHeaderWidth)
                 leftHeaderWidth = columnParams.length;
         // Now finally getting total size
         int tableHeight = 0;
         for(String[][][] subtablesRow: allResultsSubtables)
             tableHeight += (subtablesRow[0].length+1);
         tableHeight += topHeaderHeight;
         int tableWidth = 0;
         for(String[][] subtable: allResultsSubtables[0])
             tableWidth += (subtable[0].length+1);
         tableWidth += leftHeaderWidth - 1;
 
         //// Instantiating table printer, to start storing cells values into it
         ResultsTable tablePrinter = new ResultsTable();        
         tablePrinter.cells = new String[tableHeight][];
         String[][] cells = tablePrinter.cells;
         for(int i = 0; i < cells.length; i++)
             cells[i] = new String[tableWidth];
         // Initially all cells will contain the empty string (and none a null value)
         for(int i = 0; i < cells.length; i++)
             for(int j = 0; j < cells[0].length; j++)
                 cells[i][j] = ""; 
         // Setting header rows and columns
         for(int i = 0; i < topHeaderHeight; i++)
             tablePrinter.headerRows.add(i);
         for(int j = 0; j < leftHeaderWidth; j++)
             tablePrinter.headerColumns.add(j);
                 
         //// Now building the top header, this is the most complicated part :(
         int startFillingInRow = 0;
         int startFillingInCol = leftHeaderWidth-1;
         for(int rowParamSetIndex = 0; rowParamSetIndex < rowParamsNamesList.size(); rowParamSetIndex++) {
             String[] rowParams = rowParamsNamesList.get(rowParamSetIndex);
             String[][] rowParamsCombinedValues = rowParamsCombinedValuesList.get(rowParamSetIndex);
             startFillingInRow = topHeaderHeight - rowParams.length;
             for(int i = startFillingInRow; i < (startFillingInRow + rowParams.length); i++) {
                 // Writing parameters labels
                 cells[i][startFillingInCol] = rowParams[i-startFillingInRow];
                 tablePrinter.headerColumns.add(startFillingInCol);
                 // Writing parameters combined values
                 for(int j = startFillingInCol + 1; j < (startFillingInCol + 1 + rowParamsCombinedValues.length); j++) 
                     cells[i][j] = rowParamsCombinedValues[j - (startFillingInCol + 1)][i - startFillingInRow];
             }
             startFillingInCol += (rowParamsCombinedValues.length + 1);
         }
         
         //// Turn for the left header
         startFillingInRow = topHeaderHeight;
         startFillingInCol = 0;
         for(int columnParamSetIndex = 0; columnParamSetIndex < columnParamsNamesList.size(); columnParamSetIndex++) {
             String[] columnParams = columnParamsNamesList.get(columnParamSetIndex);
             String[][] columnParamsCombinedValues = columnParamsCombinedValuesList.get(columnParamSetIndex);
             startFillingInCol = leftHeaderWidth - columnParams.length;
             for(int j = startFillingInCol; j < (startFillingInCol + columnParams.length); j++) {
                 // Writing parameters labels
                 cells[startFillingInRow][j] = columnParams[j-startFillingInCol];
                 tablePrinter.headerRows.add(startFillingInRow);
                 // Writing parameters combined values
                 for(int i = startFillingInRow + 1; i < (startFillingInRow + 1 + columnParamsCombinedValues.length); i++)
                     cells[i][j] = columnParamsCombinedValues[i- (startFillingInRow+1)][j-startFillingInCol];
             }
             startFillingInRow += (columnParamsCombinedValues.length + 1);     
         }
         
         //// Printing results subtables, remember they are organized in a matrix themselves
         startFillingInRow = topHeaderHeight+1;
         startFillingInCol = leftHeaderWidth;
         for(int subtableRow = 0; subtableRow < allResultsSubtables.length; subtableRow++ ) {
             for(int subtableColumn = 0; subtableColumn < allResultsSubtables[0].length; subtableColumn++) {
                 String[][] resultsSubtable = allResultsSubtables[subtableRow][subtableColumn];
                 for(int i = startFillingInRow; i < startFillingInRow + resultsSubtable.length; i++)
                     for(int j = startFillingInCol; j < startFillingInCol + resultsSubtable[0].length; j++)
                         cells[i][j] = resultsSubtable[i-startFillingInRow][j-startFillingInCol];
                 startFillingInCol += resultsSubtable[0].length + 1;
             }
             startFillingInRow += allResultsSubtables[subtableRow][0].length+1;
             startFillingInCol = leftHeaderWidth;
         }
         
         return tablePrinter;
     }
     
     /**
      * Build a table containing the results of the jobs with the configurations passed
      * as parameters.
      * @param rowParams
      * @param rowParamsCombinedValues
      * @param colParams
      * @param colParamsCombinedValues
      * @param resultName
      * @param computation
      * @param resultsMap
      * @return Table of results of the experiments, organized in a matrix
      */
     private static String[][] buildResultsSubtable(String[] rowParams, String[][] rowParamsCombinedValues, String[] colParams, String[][] colParamsCombinedValues, String resultName, ResultsTableConf.RESULTS_COMPUTATION computation, Map<Properties, Collection<Properties>> resultsMap) {
         
         for(String[] rowParamsCombination: rowParamsCombinedValues)
             if(rowParamsCombination.length != rowParams.length)
                 throw new Error("There are " + rowParams.length + " row params, but there is a combination with size " + rowParamsCombination.length);
         
         for(String[] colParamsCombination: colParamsCombinedValues)
             if(colParamsCombination.length != colParams.length)
                 throw new Error("There are " + colParams.length + " col params, but there is a combination with size " + colParamsCombination.length);
         
         String[][] resultsTable = new String[rowParamsCombinedValues.length][];
         
         for(int i = 0; i < rowParamsCombinedValues.length; i++) {
             String[] rowParamsCombination = rowParamsCombinedValues[i];
             resultsTable[i] = new String[colParamsCombinedValues.length];
             for(int j = 0; j < colParamsCombinedValues.length; j++) {
                 String[] colParamsCombination = colParamsCombinedValues[j];
                 // Looking for results for this combination of parameters
                 Properties configuration = new Properties();
                 for(int rowParamIndex = 0; rowParamIndex < rowParams.length; rowParamIndex++)
                     configuration.put(rowParams[rowParamIndex], rowParamsCombination[rowParamIndex]);
                 for(int colParamIndex = 0; colParamIndex < colParams.length; colParamIndex++)
                     configuration.put(colParams[colParamIndex], colParamsCombination[colParamIndex]);
                 
                 List<Object> results = new ArrayList<Object>();
                 List<Properties> allResultsWithConf = getResultsOfExpsWithConf(configuration, resultsMap);
                 for(Properties resultsWithConf: allResultsWithConf)
                     if(resultsWithConf.containsKey(resultName))
                         results.add(resultsWithConf.get(resultName));
                 
                 resultsTable[i][j] = ResultsTableConf.computeResults(results, computation);
             }
         }
         
         return resultsTable;
     }
     
     /**
      * Returns all the results obtained by all the jobs executed with the configuration passed as parameter.
      * @param confLookedFor
      * @param paramsResultsMap
      * @return Results obtained by all the jobs executed with the configuration passed as parameter.
      */
     private static List<Properties> getResultsOfExpsWithConf(Properties confLookedFor, Map<Properties, Collection<Properties>> paramsResultsMap) {
 
         List<Properties> resultsWithConf = new ArrayList<Properties>();
         
         for(Properties params: paramsResultsMap.keySet()) {
             boolean paramsFulfillConfig = true;
             for(String paramLookedFor: confLookedFor.stringPropertyNames()) {
                 if(!params.containsKey(paramLookedFor)) {
                     paramsFulfillConfig = false;
                     break;
                 }
                 if(!confLookedFor.get(paramLookedFor).equals(params.get(paramLookedFor))) {
                     paramsFulfillConfig = false;
                     break;
                 }
             }
             if(paramsFulfillConfig)
                 resultsWithConf.addAll(paramsResultsMap.get(params));
         }
         
         return resultsWithConf;
     }
 
 }
