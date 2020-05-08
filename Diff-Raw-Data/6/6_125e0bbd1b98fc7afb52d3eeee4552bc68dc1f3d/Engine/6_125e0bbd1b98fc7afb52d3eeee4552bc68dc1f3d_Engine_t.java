 /*
  * Japex ver. 0.1 software ("Software")
  * 
  * Copyright, 2004-2005 Sun Microsystems, Inc. All Rights Reserved.
  * 
  * This Software is distributed under the following terms:
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, is permitted provided that the following conditions are met:
  * 
  * Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer.
  * 
  * Redistribution in binary form must reproduce the above copyright notice,
  * this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  * 
  * Neither the name of Sun Microsystems, Inc., 'Java', 'Java'-based names,
  * nor the names of contributors may be used to endorse or promote products
  * derived from this Software without specific prior written permission.
  * 
  * The Software is provided "AS IS," without a warranty of any kind. ALL
  * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
  * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
  * PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS
  * SHALL NOT BE LIABLE FOR ANY DAMAGES OR LIABILITIES SUFFERED BY LICENSEE
  * AS A RESULT OF OR RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THE
  * SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE
  * LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT,
  * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED
  * AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
  * INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGES.
  * 
  * You acknowledge that the Software is not designed, licensed or intended
  * for use in the design, construction, operation or maintenance of any
  * nuclear facility.
  */
 
 package com.sun.japex;
 
 import java.util.*;
 import java.text.*;
 import java.net.*;
 import java.io.File;
 import java.util.concurrent.*;
 
 public class Engine {
     
     public Engine() {
     }
     
     public TestSuiteImpl start(String configFile) {
         TestSuiteImpl testSuite = null;
         Boolean computeResult = null;
                 
         try { 
             // Load config file
             ConfigFileLoader cfl = new ConfigFileLoader(configFile);
             testSuite = cfl.getTestSuite();
 
             // Get number of threads and print it out
             int nOfThreads = testSuite.getIntParam(Constants.NUMBER_OF_THREADS);
             System.out.println("Running using " + nOfThreads + " thread(s) ...");
             if (testSuite.hasParam(Constants.WARMUP_TIME) && 
                     testSuite.hasParam(Constants.RUN_TIME)) 
             {
                 int[] hms = estimateRunningTime(testSuite);
                 System.out.println("Estimated warmup time + run time is " +
                     (hms[0] > 0 ? (hms[0] + " hours ") : "") +
                     (hms[1] > 0 ? (hms[1] + " minutes ") : "") +
                     (hms[2] > 0 ? (hms[2] + " seconds ") : ""));                    
             }
 
             // Allocate a fix pool of nOfThreads threads
             ExecutorService threadPool = Executors.newFixedThreadPool(nOfThreads);
                 
             // Iterate through each driver
             Iterator jdi = testSuite.getDriverInfoList().iterator();
             while (jdi.hasNext()) {                               
                 DriverImpl di = (DriverImpl) jdi.next();
                 
                 // Display driver's name
                 System.out.print("  " + di.getName());
                 
                 int runsPerDriver = testSuite.getIntParam(Constants.RUNS_PER_DRIVER);
                 boolean includeWarmupRun = testSuite.getBooleanParam(Constants.INCLUDE_WARMUP_RUN);
                 
                 // Allocate a matrix of nOfThreads * runPerDriver size and initialize each instance
                 JapexDriverBase drivers[][] = new JapexDriverBase[nOfThreads][runsPerDriver];
                 for (int i = 0; i < nOfThreads; i++) {
                     for (int j = 0; j < runsPerDriver; j++) {
                         drivers[i][j] = di.getJapexDriver();   // returns fresh copy
                         drivers[i][j].setDriver(di);
                         drivers[i][j].setTestSuite(testSuite);
                         drivers[i][j].initializeDriver();
                     }
                 }
                 
                 for (int driverRun = 0; driverRun < runsPerDriver; driverRun++) {
                     if (includeWarmupRun) {
                         System.out.print(driverRun == 0 ? "\n    Warmup run: "
                             : "\n    Run " + driverRun + ": ");
                     }
                     else {
                         System.out.print("\n    Run " + (driverRun + 1) + ": ");                        
                     }
                     
                     // Get list of tests
                     List tcList = di.getTestCases(driverRun);
                     int nOfTests = tcList.size();
                     
                     // geometric mean = (sum{i,n} x_i) / n
                     double geomMeanresult = 1.0;
                     // arithmetic mean = (prod{i,n} x_i)^(1/n)
                     double aritMeanresult = 0.0;
                     // harmonic mean inverse = sum{i,n} 1/(n * x_i)
                     double harmMeanresultInverse = 0.0;
                                 
                     // Iterate through list of test cases
                     Iterator tci = tcList.iterator();
                     while (tci.hasNext()) {
                         long runTime = 0L;
                         TestCaseImpl tc = (TestCaseImpl) tci.next();               
 
                         if (Japex.verbose) {
                             System.out.println(tc.getName());
                         }
                         else {
                             System.out.print(tc.getName() + ",");
                         }
 
                         // If nOfThreads == 1, re-use this thread
                         if (nOfThreads == 1) {
                             // -- Prepare phase --------------------------------------
 
                             drivers[0][driverRun].setTestCase(tc);     // tc is shared!
                             drivers[0][driverRun].prepare();
 
                             // -- Warmup phase ---------------------------------------
 
                             // Start timer 
                             runTime = Util.currentTimeMillis();
 
                             // First time call does warmup
                             drivers[0][driverRun].call();
 
                             // Stop timer
                             runTime = Util.currentTimeMillis() - runTime;
 
                             // Set japex.actualWarmupTime output param
                             tc.setDoubleParam(Constants.ACTUAL_WARMUP_TIME, runTime);  
                             
                             // -- Run phase -------------------------------------------
 
                             // Start timer for run phase
                             runTime = Util.currentTimeMillis();
 
                             // Second time call does run
                             drivers[0][driverRun].call();
                         }
                         else {  // nOfThreads > 1
                             Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
 
                             // -- Prepare phase --------------------------------------
                             
                             // Initialize driver instance with test case object do prepare
                             for (int i = 0; i < nOfThreads; i++) {
                                 drivers[i][driverRun].setTestCase(tc);     // tc is shared!
                                 drivers[i][driverRun].prepare();
                             }
                                                         
                             // -- Warmup phase ---------------------------------------
                             
                             // Start timer for warmup phase 
                             runTime = Util.currentTimeMillis();
 
                             // Fork all threads -- first time drivers will warmup
                             Future<?>[] futures = new Future<?>[nOfThreads];                            
                             for (int i = 0; i < nOfThreads; i++) {
                                 futures[i] = threadPool.submit(drivers[i][driverRun]);
                             }
 
                             // Wait for all threads to finish
                             for (int i = 0; i < nOfThreads; i++) {
                                 futures[i].get();
                             }
                             
                             // Stop timer
                             runTime = Util.currentTimeMillis() - runTime;
 
                             // Set japex.actualWarmupTime output param
                             tc.setDoubleParam(Constants.ACTUAL_WARMUP_TIME, runTime);  
                             
                             // -- Run phase -------------------------------------------
                             
                             // Start timer for run phase
                             runTime = Util.currentTimeMillis();
 
                             // Fork all threads -- second time drivers will run
                             for (int i = 0; i < nOfThreads; i++) {
                                 futures[i] = threadPool.submit(drivers[i][driverRun]);
                             }
 
                             // Wait for all threads to finish
                             for (int i = 0; i < nOfThreads; i++) {
                                 futures[i].get();
                             }
                         }
 
                         // Stop timer
                         runTime = Util.currentTimeMillis() - runTime;
 
                         // Set japex.actualRunTime output param
                         tc.setDoubleParam(Constants.ACTUAL_RUN_TIME, runTime);  
 
                         // Finish phase                         
                         for (int i = 0; i < nOfThreads; i++) {
                             drivers[i][driverRun].finish();
                         }                    
                         
                         double result = 0.0;
 
                         if (computeResult == null) {
                             if (!tc.hasParam(Constants.RESULT_VALUE)) {
                                 result = 
                                     tc.getIntParam(Constants.ACTUAL_RUN_ITERATIONS) / 
                                         (runTime / 1000.0);
                                 testSuite.setParam(Constants.RESULT_UNIT, "TPS");
 
                                 computeResult = Boolean.TRUE;
                                 tc.setDoubleParam(Constants.RESULT_VALUE, result);
                             }
                             else {
                                 result = tc.getDoubleParam(Constants.RESULT_VALUE);
                                 computeResult = Boolean.FALSE;
                             }                        
                         }
                         else if (computeResult == Boolean.TRUE
                                  && !tc.hasParam(Constants.RESULT_VALUE))  
                         {
                             result = 
                                 tc.getIntParam(Constants.ACTUAL_RUN_ITERATIONS) / 
                                     (runTime / 1000.0);
                             tc.setDoubleParam(Constants.RESULT_VALUE, result);
                         }
                         else if (computeResult == Boolean.FALSE
                                  && tc.hasParam(Constants.RESULT_VALUE)) 
                         {
                             result = tc.getDoubleParam(Constants.RESULT_VALUE);
                         }
                         else {
                             throw new RuntimeException(
                                "The output parameter '" + Constants.RESULT_VALUE
                                + "' must be computed by either all or none of "
                                + "the drivers for the results to be comparable");
                         }            
 
                         // Compute running means 
                         aritMeanresult += result / nOfTests;
                         geomMeanresult *= Math.pow(result, 1.0 / nOfTests);
                         harmMeanresultInverse += 1.0 / (nOfTests * result);
                     
                         // Display results for this test
                         if (Japex.verbose) {
                             System.out.println("           " + tc.getParam(Constants.RESULT_VALUE));
                             System.out.print("           ");
                         }
                         else {
                             System.out.print(tc.getParam(Constants.RESULT_VALUE) + ",");
                             System.out.flush();
                         }
                     }               
                 
                     System.out.print(
                         "aritmean," + Util.formatDouble(aritMeanresult) +
                         ",geommean," + Util.formatDouble(geomMeanresult) +
                         ",harmmean," + Util.formatDouble(1.0 / harmMeanresultInverse));
                     
                     // Call terminate on all driver instances
                     for (int i = 0; i < nOfThreads; i++) {
                         drivers[i][driverRun].terminateDriver();
                     }
                 }
                               
                 if (runsPerDriver > 1) {
                     // Print average for all runs
                     System.out.print("\n     Avgs: ");
                     Iterator tci = di.getAggregateTestCases().iterator();
                     while (tci.hasNext()) {
                         TestCaseImpl tc = (TestCaseImpl) tci.next();
                         System.out.print(tc.getName() + ",");                        
                         System.out.print(
                             Util.formatDouble(tc.getDoubleParam(Constants.RESULT_VALUE)) 
                             + ",");
                     }
                     System.out.print(
                         "aritmean," +
                         di.getParam(Constants.RESULT_ARIT_MEAN) + 
                         ",geommean," +
                         di.getParam(Constants.RESULT_GEOM_MEAN) + 
                         ",harmmean," +
                         di.getParam(Constants.RESULT_HARM_MEAN));   
 
                     // Print standardDevs for all runs
                     System.out.print("\n    Stdev: ");
                     tci = di.getAggregateTestCases().iterator();
                     while (tci.hasNext()) {
                         TestCaseImpl tc = (TestCaseImpl) tci.next();
                         System.out.print(tc.getName() + ",");                        
                         System.out.print(
                             Util.formatDouble(tc.getDoubleParam(Constants.RESULT_VALUE_STDDEV)) 
                             + ",");
                     }
                     System.out.println(
                         "aritmean," +
                         di.getParam(Constants.RESULT_ARIT_MEAN_STDDEV) + 
                         ",geommean," +
                         di.getParam(Constants.RESULT_GEOM_MEAN_STDDEV) + 
                         ",harmmean," +
                         di.getParam(Constants.RESULT_HARM_MEAN_STDDEV));   
                 }
                 else {
                     System.out.println("");
                 }
             }
 
 	    // Shutdown thread pool -- all threads must have stopped by now
 	    threadPool.shutdown();
         }
         catch (Exception e) {
             e.printStackTrace();
             throw new RuntimeException(e);
         }
 
         return testSuite;
     }        
     
     /**
      * Calculates the time of the warmup and run phases. Returns an array 
      * of size 3 with hours, minutes and seconds.
      */
     private int[] estimateRunningTime(TestSuiteImpl testSuite) {        
         int nOfDrivers = testSuite.getDriverInfoList().size();
         int nOfTests = ((DriverImpl) testSuite.getDriverInfoList().get(0)).getTestCases(0).size();
     
         String runTime = testSuite.getParam(Constants.RUN_TIME);
         String warmupTime = testSuite.getParam(Constants.WARMUP_TIME);
 
        long seconds = (long)
            (nOfDrivers * nOfTests * (Util.parseDuration(warmupTime) / 1000.0) +
            nOfDrivers * nOfTests * (Util.parseDuration(runTime) / 1000.0)) *
             testSuite.getIntParam(Constants.RUNS_PER_DRIVER);     
         
         int[] hms = new int[3];
         hms[0] = (int) (seconds / 60 / 60);
         hms[1] = (int) ((seconds / 60) % 60);
         hms[2] = (int) (seconds % 60);
         return hms;
     }
 }
