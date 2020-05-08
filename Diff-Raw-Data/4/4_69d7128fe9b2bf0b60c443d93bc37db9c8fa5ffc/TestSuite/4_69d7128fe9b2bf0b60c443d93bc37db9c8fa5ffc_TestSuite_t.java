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
 import java.io.File;
 import com.sun.japex.testsuite.*;
 
 import java.awt.Paint;
 import java.awt.Color;
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JFrame;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.JToolBar;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import org.jfree.chart.*;
 import org.jfree.data.category.DefaultCategoryDataset;
 import org.jfree.ui.ApplicationFrame;
 import org.jfree.ui.RefineryUtilities;
 import org.jfree.chart.plot.CategoryPlot;
 import org.jfree.chart.renderer.category.CategoryItemRenderer;
 import org.jfree.chart.renderer.category.BarRenderer;
 import org.jfree.chart.plot.PlotOrientation;
 
 public class TestSuite extends Params {
     
     String _name;
     List _driverInfo = new ArrayList();
     
     /** 
      * Creates a new instance of TestSuite from a JAXB-generated
      * object. In essence, this constructor implements a mapping
      * between the JAXB object model and the internal object model
      * used in Japex.
      */
     public TestSuite(com.sun.japex.testsuite.TestSuite ts) {
         _name = ts.getName();
         
         // Set global properties by traversing JAXB's model
         List params = ts.getParam();
         final String pathSep = System.getProperty("path.separator");
         List classPathURLs = new ArrayList();
         
         if (params != null) {
             Iterator it = params.iterator();
             while (it.hasNext()) {
                 ParamType pt = (ParamType) it.next();
                 String name = pt.getName();
                 String value = pt.getValue();
                 String oldValue = getParam(name);
                 
                 // If japex.classPath, append to existing value
                 setParam(name, 
                     name.equals(Constants.CLASS_PATH) && oldValue != null ?
                     (oldValue + pathSep + value) : value);
             }
         }
         
         // Set default global params if necessary
         if (!hasParam(Constants.WARMUP_TIME) && 
             !hasParam(Constants.WARMUP_ITERATIONS))
         {
             setParam(Constants.WARMUP_ITERATIONS, 
                      Constants.DEFAULT_WARMUP_ITERATIONS);    
         }
         if (!hasParam(Constants.RUN_TIME) && 
             !hasParam(Constants.RUN_ITERATIONS))
         {
             setParam(Constants.RUN_ITERATIONS, 
                      Constants.DEFAULT_RUN_ITERATIONS);    
         }
         
         // Check number of threads 
         if (!hasParam(Constants.NUMBER_OF_THREADS)) {
             setParam(Constants.NUMBER_OF_THREADS, 
                      Constants.DEFAULT_NUMBER_OF_THREADS);    
         }
         else {
             int nOfThreads = getIntParam(Constants.NUMBER_OF_THREADS);
             if (nOfThreads < 1) {
                 throw new RuntimeException(
                     "Parameter 'japex.numberOfThreads' must be at least 1");
             }
         }
         
         // Set other global params
         setParam(Constants.VERSION, Constants.VERSION_VALUE);
         setParam(Constants.OS_NAME, System.getProperty("os.name"));
         setParam(Constants.OS_ARCHITECTURE, System.getProperty("os.arch"));
         DateFormat df = new SimpleDateFormat("dd MMM yyyy/HH:mm:ss z");
         setParam(Constants.DATE_TIME, df.format(Japex.TODAY));
         setParam(Constants.VM_INFO,
             System.getProperty("java.vendor") + " " + 
             System.getProperty("java.vm.version"));
         
         // Create and populate list of drivers
         Iterator it = ts.getDriver().iterator();
         while (it.hasNext()) {
             TestSuiteType.DriverType dt = (TestSuiteType.DriverType) it.next();
             
             Properties driverParams = new Properties(getParams());
             Iterator driverParamsIt = dt.getParam().iterator();
             while (driverParamsIt.hasNext()) {
                 ParamType pt = (ParamType) driverParamsIt.next();
                 driverParams.setProperty(pt.getName(), pt.getValue());
             }
 
             // If japex.driverClass not specified, use the driver's name
             if (driverParams.getProperty(Constants.DRIVER_CLASS) == null) {
                 driverParams.setProperty(Constants.DRIVER_CLASS, dt.getName());
             }            
             _driverInfo.add(
                 new DriverInfo(dt.getName(), dt.isNormal(), driverParams));
         }
         
         // Create and populate list of test cases
         TestCaseArrayList testCases = new TestCaseArrayList();
         it = ts.getTestCase().iterator();
         while (it.hasNext()) {
             TestSuiteType.TestCaseType tc = 
                 (TestSuiteType.TestCaseType) it.next();
             
             Properties localParams = new Properties(getParams());
             Iterator itParams = tc.getParam().iterator();
             while (itParams.hasNext()) {
                 ParamType pt = (ParamType) itParams.next();
                 localParams.setProperty(pt.getName(), pt.getValue());
             }
             testCases.add(new TestCase(tc.getName(), localParams));
         }
         
         // Set list of test cases on each driver (make sure to clone!)
         it = _driverInfo.iterator();
         while (it.hasNext()) {
             DriverInfo di = (DriverInfo) it.next();
             di.setTestCases((List) testCases.clone());
         }
     }
     
     public String getName() {
         return _name;        
     }
     
     public List getDriverInfoList() {
         return _driverInfo;
     }
     
     public void serialize(StringBuffer report) {
         report.append("<testSuiteReport name=\"" + _name 
             + "\" xmlns=\"http://www.sun.com/japex/testSuiteReport\">\n");      
 
         serialize(report, 2);
         
         // Iterate through each class (aka driver)
         Iterator jdi = _driverInfo.iterator();
         while (jdi.hasNext()) {
             DriverInfo di = (DriverInfo) jdi.next();
             di.serialize(report, 2);
         }
                     
         report.append("</testSuiteReport>\n");
     }
        
     public void generateDriverChart(String fileName) {
         try {
             DefaultCategoryDataset dataset = new DefaultCategoryDataset();
             String resultUnit = getParam(Constants.RESULT_UNIT);
             
             // Find first normalizer driver (if any) and adjust unit
             DriverInfo normalizerDriver = null;            
             Iterator jdi = _driverInfo.iterator();
             while (jdi.hasNext()) {
                 DriverInfo di = (DriverInfo) jdi.next();       
                 if (di.isNormal()) {
                     normalizerDriver = di; 
                     resultUnit = "% of " + resultUnit;
                     break;
                 }
             }
             
             // Generate charts
             jdi = _driverInfo.iterator();
             while (jdi.hasNext()) {
                 DriverInfo di = (DriverInfo) jdi.next();
                               
                 if (normalizerDriver != null) {
                     dataset.addValue(
                         normalizerDriver == di ? 100.0 :
                         (100.0 * di.getDoubleParam(Constants.RESULT_ARIT_MEAN) /
                          normalizerDriver.getDoubleParam(Constants.RESULT_ARIT_MEAN)),
                         di.getName(),
                         "Arithmetic Mean");
                     dataset.addValue(
                         normalizerDriver == di ? 100.0 :
                         (100.0 * di.getDoubleParam(Constants.RESULT_GEOM_MEAN) /
                          normalizerDriver.getDoubleParam(Constants.RESULT_GEOM_MEAN)),
                         di.getName(),
                         "Geometric Mean");
                     dataset.addValue(
                         normalizerDriver == di ? 100.0 :
                         (100.0 * di.getDoubleParam(Constants.RESULT_HARM_MEAN) /
                          normalizerDriver.getDoubleParam(Constants.RESULT_HARM_MEAN)),
                         di.getName(),
                         "Harmonic Mean");                    
                 }
                 else {
                     dataset.addValue(
                         di.getDoubleParam(Constants.RESULT_ARIT_MEAN), 
                         di.getName(),
                         "Arithmetic Mean");
                     dataset.addValue(
                         di.getDoubleParam(Constants.RESULT_GEOM_MEAN), 
                         di.getName(),
                         "Geometric Mean");
                     dataset.addValue(
                         di.getDoubleParam(Constants.RESULT_HARM_MEAN), 
                         di.getName(),
                         "Harmonic Mean");
                 }
             }
             
             JFreeChart chart = ChartFactory.createBarChart3D(
                 "Result Summary (" + resultUnit + ")", 
                 "", resultUnit, 
                 dataset,
                 PlotOrientation.VERTICAL,
                 true, true, false);
             chart.setAntiAlias(true);
             
             ChartUtilities.saveChartAsJPEG(new File(fileName), chart, 600, 450);       
         }
         catch (Exception e) {
             e.printStackTrace();
         }        
     }
     
     public int generateTestCaseCharts(String baseName, String extension) {
         int nOfFiles = 0;
         final int groupSize = 6;
         
         try {            
             String resultUnit = getParam(Constants.RESULT_UNIT);
             
             // Get number of tests from first driver
             final int nOfTests = 
                 ((DriverInfo) _driverInfo.get(0)).getTestCases().size();
             
             // Find first normalizer driver (if any)
             DriverInfo normalizerDriver = null;
             
             Iterator jdi = _driverInfo.iterator();
             while (jdi.hasNext()) {
                 DriverInfo di = (DriverInfo) jdi.next();       
                 if (di.isNormal()) {
                     normalizerDriver = di; 
                     resultUnit = "% of " + resultUnit;
                     break;
                 }
             }
             
             // Generate charts 
             DefaultCategoryDataset dataset = new DefaultCategoryDataset();
             
             int i = 0;
             for (; i < nOfTests; i++) {
                 jdi = _driverInfo.iterator();
                 
                 while (jdi.hasNext()) {
                     DriverInfo di = (DriverInfo) jdi.next();
                     TestCase tc = (TestCase) di.getTestCases().get(i);
             
                     // User normalizer driver if defined
                     if (normalizerDriver != null) {
                         TestCase normalTc = 
                             (TestCase) normalizerDriver.getTestCases().get(i);
                         dataset.addValue(normalizerDriver == di ? 100.0 :
                                 (100.0 * tc.getDoubleParam(Constants.RESULT_VALUE) /
                                  normalTc.getDoubleParam(Constants.RESULT_VALUE)),
                                 di.getName(),
                                 tc.getTestName());                                                
                     }
                     else {
                         dataset.addValue(
                             tc.getDoubleParam(Constants.RESULT_VALUE), 
                             di.getName(),
                             tc.getTestName());                    
                     }
                 }                
                         
                 // Generate chart for this group
                 if (i > 0 && i % groupSize == 0) {
                     JFreeChart chart = ChartFactory.createBarChart3D(
                         "Results per Test (" + resultUnit + ")", 
                         "", resultUnit, 
                         dataset,
                         PlotOrientation.VERTICAL,
                         true, true, false);
                     chart.setAntiAlias(true);
                     ChartUtilities.saveChartAsJPEG(
                         new File(baseName + Integer.toString(nOfFiles) + extension),
                         chart, 600, 450);
                     
                     nOfFiles++;
                     dataset = new DefaultCategoryDataset();
                 }
             }
             
            // Generate first (if exactly groupSize tests) or last chart
            if (i == groupSize || i % groupSize != 0) {
                 JFreeChart chart = ChartFactory.createBarChart3D(
                     "Results per Test (" + resultUnit + ")", 
                     "", resultUnit, 
                     dataset,
                     PlotOrientation.VERTICAL,
                     true, true, false);
                 chart.setAntiAlias(true);
 
                 ChartUtilities.saveChartAsJPEG(
                     new File(baseName + Integer.toString(nOfFiles) + extension),
                     chart, 600, 450);
                 nOfFiles++;
             }
         }
         catch (Exception e) {
             e.printStackTrace();
         }        
         
         return nOfFiles;
     }
 
 }
