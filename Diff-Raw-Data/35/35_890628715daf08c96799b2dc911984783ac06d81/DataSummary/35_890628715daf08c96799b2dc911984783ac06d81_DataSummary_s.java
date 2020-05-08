 /* ***** BEGIN LICENSE BLOCK *****
  * 
  * Copyright (c) 2011 Colin J. Fuller
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  * 
  * ***** END LICENSE BLOCK ***** */
 
 package edu.stanford.cfuller.imageanalysistools.frontend;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.util.Hashtable;
 
 import edu.stanford.cfuller.imageanalysistools.parameters.ParameterDictionary;
 
 /**
  * Summarizes the output from analysis routines, combining all output from a directory into a single unified file.
  * If objects have been clustered into groups, summarizes only over those groups, omitting data on individual objects.
  *
  * @author Colin J. Fuller
  *
  */
 
 
 public class DataSummary {
 
     private static double[] newAverages(int numChannels) {
 
         double[] averages = new double[numChannels];
         for (int i = 0; i < numChannels; i++) {
             averages[i] = 0.0;
         }
         return averages;
 
     }
 
 
     /**
      * Creates a summary of output files created by the analysis program.
      *
      * @param directory     The full path to the directory containing the output files to be summarized.
      * @param parameterDirectory       The directory that stores the parameters for the analysis.
      * @throws java.io.IOException      If any problems reading the analysis output files or writing the summary to disk are encountered.
      */
     public static void SummarizeData(String directory, String parameterDirectory) throws java.io.IOException {
 
 
         final String outputFileExtension = ".out.txt";
 
         File dir = new File(directory);
 
         if (! dir.exists()) {
 
             dir.mkdir();
 
         }
 
         File outputFile = new File(directory + File.separator + "summary.txt");
 
 
 
         PrintWriter output = new PrintWriter(new FileOutputStream(outputFile));
 
 
         int numChannels = 0;
 
         boolean headerRowWritten = false;
 
         for (File f : dir.listFiles()) {
 
 
         	ParameterDictionary params = null;
         	
 
             if (! f.getName().matches(".*" + outputFileExtension)) {continue;}
 
            if (! headerRowWritten) {
 
                File parameterFile = new File(parameterDirectory + File.separator + f.getName().replace(outputFileExtension, AnalysisController.PARAMETER_EXTENSION));
 
                params = ParameterDictionary.readParametersFromFile(parameterFile.getAbsolutePath());
 
                numChannels = Integer.parseInt(params.getValueForKey("number_of_channels"));
 
                 // column headers
 
                 output.print("cell_number" + " ");
                 for (int i = 0; i < numChannels; i++) {
 
                     output.print(params.getValueForKey("channel_name").split(" ")[i]);
                     output.print(" ");
                 }
 
                 for (int i = 0; i < numChannels; i++) {
                     output.print(params.getValueForKey("channel_name").split(" ")[i] + "_background ");
 
                 }
 
                 output.print("number_of_regions_in_cluster ");
 
                 output.println("average_region_size");
 
 
 
                 headerRowWritten = true;
 
             }
 
 
 
 
             System.out.println(f.getName());
 
             Hashtable<Integer, double[]> regions = new Hashtable<Integer, double[]>();
             Hashtable<Integer, int[]> counts = new Hashtable<Integer, int[]>();
 
             Hashtable<Integer, Double[]> allBG = new Hashtable<Integer, Double[]>();
             Hashtable<Integer, Double> average_sizes = new Hashtable<Integer, Double>();
 
 
             BufferedReader b = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
 
             String line;
 
             while ((line = b.readLine()) != null) {
 
 
                 String[] splitline = line.split(" ");
 
                 int regionID = (int) Double.parseDouble(splitline[splitline.length - 1]);
                 int regionType = 0;
 
                 if (! regions.containsKey(regionID)){
                     regions.put(regionID, newAverages(numChannels));
                     int[] countsArr = new int[4];
                     countsArr[0] = 0; countsArr[1] = 0; countsArr[2] = 0; countsArr[3] = 0;
                     counts.put(regionID, countsArr);
                     average_sizes.put(regionID, 0.0);
                 }
 
                 double[] averages = regions.get(regionID);
                 int[] averageCounts = counts.get(regionID);
 
                 for (int i = 0; i < numChannels; i++) {
 
                     //averages[i] += Double.parseDouble(splitline[i])/Double.parseDouble(splitline[numChannels]);
                     averages[i] += Double.parseDouble(splitline[i]);
                 }
 
                 averageCounts[regionType] += 1;
                 Double[] tempBg = new Double[numChannels];
                 if (params != null && params.getBooleanValueForKey("background_calculated")) {
 	                for (int i = 0; i < numChannels; i++) {
 	                    tempBg[i] = Double.parseDouble(splitline[numChannels+i+1]);
 	
 	                }
                 } else {
                 	java.util.Arrays.fill(tempBg, 0.0);
                 }
 
                 average_sizes.put(regionID, average_sizes.get(regionID)+ Double.parseDouble(splitline[numChannels]));
 
                 allBG.put(regionID, tempBg);
 
             }
             output.println(f.getName());
 
             for (int key : regions.keySet()) {
 
                 if (key == 0) continue; //things found as centromeres but excluded by clustering have region 0 -- exclude them
 
                 output.print("" + key + " ");
 
                 int totalCounts = counts.get(key)[0] + counts.get(key)[1] + counts.get(key)[2] + counts.get(key)[3];
 
                 for (int i = 0; i < numChannels; i++) {
                     output.print("" + regions.get(key)[i]/totalCounts + " ");
                 }
                 for (int i = 0; i < numChannels; i++) {
                     output.print("" + allBG.get(key)[i] + " ");
                 }
                 //output.println("" + counts.get(key)[0]*1.0/totalCounts + " " + counts.get(key)[1]*1.0/totalCounts + " " + counts.get(key)[2]*1.0/totalCounts + " " + counts.get(key)[3]*1.0/totalCounts);
 
                 output.print("" + totalCounts + " ");
 
                 output.println("" + average_sizes.get(key)/totalCounts);
 
             }
 
         }
 
         output.close();
 
 
     }
 
 
 
 }
