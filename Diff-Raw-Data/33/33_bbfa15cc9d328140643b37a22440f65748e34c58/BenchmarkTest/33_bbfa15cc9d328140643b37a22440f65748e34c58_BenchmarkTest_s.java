 /**
  * Bristlecone Test Tools for Databases
  * Copyright (C) 2006-2007 Continuent Inc.
  * Contact: bristlecone@lists.forge.continuent.org
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of version 2 of the GNU General Public License as
  * published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA
  *
  * Initial developer(s): Robert Hodges and Ralph Hannus.
  * Contributor(s):
  */
 
 package com.continuent.bristlecone.benchmark.test;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.util.Properties;
 import java.util.Vector;
 
 import junit.framework.TestCase;
 
 import com.continuent.bristlecone.benchmark.Benchmark;
 import com.continuent.bristlecone.benchmark.impl.ConfigMetadata;
 import com.continuent.bristlecone.benchmark.impl.PropertyManager;
 import com.continuent.bristlecone.benchmark.scenarios.InsertScenario;
 import com.continuent.bristlecone.benchmark.scenarios.QueryAggregatesScenario;
 import com.continuent.bristlecone.benchmark.scenarios.QueryScenario;
 
 /**
  * Basic unit test to confirm that benchmarks work.  Test methods 
  * document specific cases.  <p>
  * 
  * The test by default runs against Hypersonic.  However, you can 
  * override this by supplying a properties file name in the system 
  * property "connection.properties".  This properties file must 
  * define url, user, and password values as shown in the following
  * example: <p>
  * 
  * <code><pre>
  * url=jdbc:postgresql://ubuntu4/mydb
  * user=benchmark
  * password=secret
  * </pre></code>
  * 
  * To include the file invoke the test with -Dconnection.properties=name
  * where 'name' is the name of the file. 
  * 
  * @author rhodges
  */
 public class BenchmarkTest extends TestCase
 {
   private String url = "jdbc:hsqldb:file:testdb/benchmark;shutdown=true";
   private String user = "sa";
   private String password = "";
 
   /** 
    * Write the test case header.  If there are connection properties 
    * load them now.  
    */
   protected void setUp() throws Exception
   {
     super.setUp();
     writeTestHeader();
     
     String connectionPropsName = System.getProperty("connection.properties");
     if (connectionPropsName != null)
     {
       PropertyManager pm = new PropertyManager();
       Properties connectionProperties = pm.loadProperties(new File(connectionPropsName));
       url = connectionProperties.getProperty("url");
       user = connectionProperties.getProperty("user");
       password = connectionProperties.getProperty("password");
     }
   }
 
   /**
    * Does nothing for now... 
    */
   protected void tearDown() throws Exception
   {
     super.tearDown();
   }
   
   /** 
    * Demonstrate that properties instance replication works using a small
    * properties instance with replicated and un-replicated files. 
    */
   public void testPropertyReplication() throws Exception
   {
     // Create properties instance. 
     Properties testProps = new Properties();
     testProps.setProperty("a", "1|2");
     testProps.setProperty("b", "3");
     
     // Generate properties instances using a cross product and print the result.
     PropertyManager pm = new PropertyManager();
     Vector<String> splitPropertyNames = new Vector<String>();
     Vector crossProductList = pm.propertiesCrossProduct(testProps, null, splitPropertyNames);
     writePropertiesVector(crossProductList);
     
     // Ensure the properties list and instance sizes are correct.   
     assertEquals("Incorrect vector size", crossProductList.size(), 2);
     
     for (int i = 0; i < crossProductList.size(); i++)
     {
       Properties p = (Properties) crossProductList.elementAt(i);
       assertEquals("Incorrect properties file size", p.size(), 2);
     }
     
     // Ensure the split properties are as expected. 
     assertEquals("Incorrect split property names size", splitPropertyNames.size(), 1);
     String pname = (String) splitPropertyNames.elementAt(0);
     assertEquals("Invalid split property", pname, "a");
   }
 
   /** 
    * Demonstrate that property loading works by generating a simple file, 
    * loading, and testing values. 
    */
   public void testPropertyLoading() throws Exception
   {
     // Create a properties file.  
     File f = File.createTempFile("propLoad", ".properties");
     FileWriter fw = new FileWriter(f);
     fw.write("prop1=1\n");
     fw.write("prop2=2\n");
     fw.close();
     writeTempFileLocation(f);
     
     // Load this file and confirm the expected properties are present. 
     PropertyManager pm = new PropertyManager();
     Properties p = pm.loadProperties(f);
     writeProperties(p);
   
     assertEquals("properties instance size is incorrect", p.size(), 2);
     assertEquals("prop1 value is incorrect", p.getProperty("prop1"), "1");
     assertEquals("prop2 value is incorrect", p.getProperty("prop2"), "2");
     
     f.delete();
   }
 
   /** 
    * Test that property replication works with include properties by defining
    * a base file with two include file names separated by "|".  Each included
    * file supplies different values for the "a" and "b" properties so we can 
    * ensure that they are present.  
    */
   public void testReplicationAndIncludes() throws Exception
   {
     // Create included properties files.  
     File includeFiles[] = new File[2];
     for (int i = 0; i < includeFiles.length; i++)
     {
       File pi = File.createTempFile("propInclude", ".properties");
       FileWriter fw = new FileWriter(pi);
       fw.write("a=" + i + "\n");
       fw.write("b=" + i + "\n");
       fw.write("c=" + i + "\n");
       fw.close();
       writeTempFileLocation(pi);
       includeFiles[i] = pi;
     }
     
     // Create the base file on which these properties should be loaded. 
     File f = File.createTempFile("propBase", ".properties");
     FileWriter fw = new FileWriter(f);
     fw.write("include=");
     for (int i = 0; i < includeFiles.length; i++)
     {
       if (i > 0)
         fw.write("|");
       fw.write(includeFiles[i].getAbsolutePath());
     }
     fw.write("\n");
     fw.write("c=VALUE\n");
     fw.close();
     writeTempFileLocation(f);
     
     // Generate properties instances using a cross product and print the result.
     PropertyManager pm = new PropertyManager();
     Vector<String> splitPropertyNames = new Vector<String>();
     Vector crossProductList = pm.propertiesCrossProduct(f, splitPropertyNames);
     writePropertiesVector(crossProductList);
     
     // Ensure the properties list and instance sizes are correct.   
     assertEquals("Incorrect vector size", crossProductList.size(), 2);
     
     for (int i = 0; i < crossProductList.size(); i++)
     {
       Properties p = (Properties) crossProductList.elementAt(i);
       assertEquals("Incorrect properties file size", 4, p.size());
       assertEquals("c value is overridden", "VALUE", p.getProperty("c"));
       assertNotNull("a property null value", p.getProperty("a"));
       assertEquals("a and b properties not same", p.getProperty("a"), 
           p.getProperty("b"));
     }
     
     // Ensure the split properties are as expected. 
     assertEquals("Incorrect split property names size", 3, splitPropertyNames.size());
     String[] expectedNames = new String[] {"a", "b", "c"}; 
     for (int i = 0; i < expectedNames.length; i++)
     {
       assertTrue("Could not find expected split property: " + expectedNames[i], 
           splitPropertyNames.contains(expectedNames[i]));
     }
 
     // Clean up property files. 
     f.delete();
     for (int i = 0; i < includeFiles.length; i++)
       includeFiles[i].delete();
   }
 
   /**
    * Demonstrate that property metadata is correctly computed and that the 
    * ConfigMetadata instance can call setters for values on scenario classes.  
    */
   public void testConfigMetadata1() throws Exception
   {
     // Start out easy with SimpleScenario. 
     Properties p = new Properties();
     p.setProperty("simple", "simple value");    
     ConfigMetadata metadata = new ConfigMetadata();
     metadata.initialize(p, SimpleScenario.class);
     
     SimpleScenario simple = new SimpleScenario();
     metadata.setProperties(p, simple);
     
     assertEquals("setSimple() property call", "simple value", simple.getSimple());
   }
   
   /** 
    * Demonstrate that ConfigMetadata can handle all supported setter 
    * argument types
    */
   public void testConfigMetadata2() throws Exception
   {
     // Use complex scenario and check all datatypes. 
     Properties p = new Properties();
     p.setProperty("propString", "propString1");    
     p.setProperty("propLong", "100");    
     p.setProperty("propInt", "13");    
     p.setProperty("propBoolean", "true");    
     p.setProperty("propChar", "b");    
     p.setProperty("propFloat", "2.0");    
     p.setProperty("propDouble", "1000.");    
 
     ConfigMetadata metadata = new ConfigMetadata();
     metadata.initialize(p, ComplexScenario.class);
     
     ComplexScenario complex = new ComplexScenario();
     metadata.setProperties(p, complex);
     
     assertEquals("propString() property call", "propString1", complex.getPropString());
     assertEquals("propLong() property call", 100, complex.getPropLong());
     assertEquals("propInt() property call", (int) 13, complex.getPropInt());
     assertEquals("propBoolean() property call", true, complex.isPropBoolean());
     assertEquals("propChar() property call", 'b', complex.getPropChar());
     assertEquals("propDouble() property call", (float) 2.0, complex.getPropFloat());
     assertEquals("propString() property call", 1000.0, complex.getPropDouble());
   }
 
   /**
    * Show that a test with required methods only and no properties by default 
    * iterates once over the test using a single thread.  
    */
   public void testBenchmarkExecution1() throws Exception
   {
     SimpleScenario.clearCounters();
     Properties p = new Properties();
     p.setProperty("simple", "simple value");
     this.runScenario("Default", SimpleScenario.class, p, true, 2);
     
     // Check that all methods were called once. 
     assertEquals("Init called once", 1, SimpleScenario.calledInitialize);
     assertEquals("Prepare called once", 1, SimpleScenario.calledPrepare);
     assertEquals("Iterate called once", 1, SimpleScenario.calledIterate);
     assertEquals("Cleaned called once", 1, SimpleScenario.calledCleanup);
   }
 
   /**
    * Show that a test with all methods but no properties by default iterates 
    * once over the test using a single thread.  
    */
   public void testBenchmarkExecution2() throws Exception
   {
     ComplexScenario.clearCounters();
     
     this.runScenario("Default", ComplexScenario.class, 
         new Properties(), true, 2);
     
     // Check that all methods were called once. 
     assertEquals("Global init called once", 1, ComplexScenario.calledPrepare);
     assertEquals("Global cleanup called once", 1, ComplexScenario.calledGlobalCleanup);
     assertEquals("Init called once", 1, ComplexScenario.calledInitialize);
     assertEquals("Iterate called once", 1, ComplexScenario.calledIterate);
     assertEquals("Cleaned called once", 1, ComplexScenario.calledCleanup);
   }
 
   /**
    * Show that a test with all methods and with threads=3 and iterations=2
    * calls the instance methods three times and the global methods once.  
    */
   public void testBenchmarkExecution3() throws Exception
   {
     ComplexScenario.clearCounters();
 
     // Set properties and run. 
     Properties props = new Properties(); 
     props.setProperty("threads", "3");
     props.setProperty("iterations", "2");
     this.runScenario("Default", ComplexScenario.class, props, true, 2);
     
     // Check that all methods were called once. 
     assertEquals("Global init called once", 1, ComplexScenario.calledGlobalPrepare);
     assertEquals("Global cleanup called once", 1, ComplexScenario.calledGlobalCleanup);
     assertEquals("Init called once", 3, ComplexScenario.calledInitialize);
     assertEquals("Iterate called once", 6, ComplexScenario.calledIterate);
     assertEquals("Cleaned called once", 3, ComplexScenario.calledCleanup);
   }
   
   /** 
    * Show that a test run using the duration bound for 10 seconds runs for at
    * least that length of time.  This test is single threaded and does not 
    * run multiple scenarios. 
    */
   public void testBenchmarkExecution4() throws Exception
   {
     ComplexScenario.clearCounters();
 
     // Set properties and run. 
     Properties props = new Properties(); 
     props.setProperty("bound", "duration");
     props.setProperty("duration", "10");
     long start = System.currentTimeMillis();
     this.runScenario("Default", ComplexScenario.class, props, true, 2);
     long end = System.currentTimeMillis();
     
     // Check that global methods and start/stop are called once.  
     assertEquals("Global init called once", 1, ComplexScenario.calledGlobalPrepare);
     assertEquals("Global cleanup called once", 1, ComplexScenario.calledGlobalCleanup);
     assertEquals("Init called once", 1, ComplexScenario.calledInitialize);
     assertEquals("Cleaned called once", 1, ComplexScenario.calledCleanup);
     
     // Assert that test lasted at least 10 seconds. 
     assertTrue("Test at least 10 seconds", (end - start / 1000) >= 10);
   }
 
   /** 
    * Show that multiple runs with multiple threads generates calls as follows: 
    * <ul>
    * <li>globalInit, globalCleanup -- Called once for each run. </li>
    * <li>init, cleanup -- Called once for each thread </li>
    * <li>iterate -- Called once for each iteration in each thread</li>
    * </ul>
    */
   public void testBenchmarkExecution5() throws Exception
   {
     ComplexScenario.clearCounters();
 
     // Set properties and run. 
     Properties props = new Properties(); 
     props.setProperty("bound", "iterations");
     props.setProperty("iterations", "10");
     props.setProperty("threads", "1|2");
     this.runScenario("Default", ComplexScenario.class, props, true, 3);
     
     // Check that global methods and start/stop are called once.  
     assertEquals("Global init called twice", 2, ComplexScenario.calledGlobalPrepare);
     assertEquals("Global cleanup called twice", 2, ComplexScenario.calledGlobalCleanup);
     assertEquals("Init called three times", 3, ComplexScenario.calledInitialize);
     assertEquals("Cleanup called three times", 3, ComplexScenario.calledCleanup);
     assertEquals("Iterate called 30 times", 30, ComplexScenario.calledIterate);
   }
 
   /** 
    * Show that multiple runs using time generation result in the following 
    * numbers of calls: 
    * <li>globalInit, globalCleanup -- Called once for each run. </li>
    * <li>init, cleanup -- Called once for each thread </li>
    * </ul>
    */
   public void testBenchmarkExecution6() throws Exception
   {
     ComplexScenario.clearCounters();
 
     // Set properties and run. 
     Properties props = new Properties(); 
     props.setProperty("bound", "duration");
     props.setProperty("duration", "2");
     props.setProperty("threads", "1|2");
     this.runScenario("Default", ComplexScenario.class, props, true, 3);
     
     // Check that global methods and start/stop are called once.  
     assertEquals("Global init called twice", 2, ComplexScenario.calledGlobalPrepare);
     assertEquals("Global cleanup called twice", 2, ComplexScenario.calledGlobalCleanup);
     assertEquals("Init called three times", 3, ComplexScenario.calledInitialize);
     assertEquals("Cleanup called three times", 3, ComplexScenario.calledCleanup);
     
     // Ensure that we iterated at least 1000 times.  This is a fair smaller number
     // than should actually have occurred. 
     assertTrue("Iterations > 1000", ComplexScenario.calledIterate > 1000);
    }
 
   /** 
    * Tests off-the-shelf insert scenario with parameters designed to generate
    * 3 lines of csv output. 
    */
   public void testInsertScenario() throws Exception
   {
     // Set properties and run. 
     Properties props = new Properties(); 
     props.setProperty("bound", "duration");
     props.setProperty("duration", "2");
     props.setProperty("threads", "1|2");
     
     props.setProperty("url", url);
     props.setProperty("user", user);
     props.setProperty("password", password);
     
     props.setProperty("tables", "2");
     props.setProperty("datarows", "10");
     props.setProperty("datawidth", "100");
     props.setProperty("datatype", "varchar");
     
     this.runScenario("Default", InsertScenario.class, props, true, 3);
   }
 
   /** 
    * Tests off-the-shelf query scenario with parameters designed to generate
    * 9 lines of csv output (1 header row + 8 individual scenario runs).   
    */
   public void testQueryScenario() throws Exception
   {
     // Set properties and run. 
     Properties props = new Properties(); 
     props.setProperty("bound", "duration");
     props.setProperty("duration", "2");
     props.setProperty("threads", "1|2");
     
     props.setProperty("url", url);
     props.setProperty("user", user);
     props.setProperty("password", password);
     
     props.setProperty("tables", "2");
     props.setProperty("datarows", "10|100");
     props.setProperty("datawidth", "10|100");
     props.setProperty("datatype", "varchar");
     
     this.runScenario("Default", QueryScenario.class, props, true, 9);
   }
 
   /** 
    * Tests off-the-shelf query aggregates scenario with parameters to generate
    * 5 lines of csv output (1 header row + 4 individual scenario runs).   
    */
   public void testQueryAggregatesScenario() throws Exception
   {
     // Set properties and run. 
     Properties props = new Properties(); 
     props.setProperty("bound", "duration");
     props.setProperty("duration", "2");
     props.setProperty("threads", "2");
     
     props.setProperty("url", url);
     props.setProperty("user", user);
     props.setProperty("password", password);
     
     props.setProperty("tables", "2");
     props.setProperty("datarows", "10|100");
     props.setProperty("datawidth", "10|100");
     props.setProperty("datatype", "varchar");
     
     this.runScenario("Default", QueryAggregatesScenario.class, props, true, 5);
   }
 
   // Write a case header. 
   private void writeTestHeader()
   {
     // "this" is a test case hence should know its own name...
     System.out.println("#########################################");
     System.out.println("# TEST CASE: " + this.getName());
     System.out.println("#########################################");
   }
 
   // Dump a properties vector.
   private void writePropertiesVector(Vector pv)
   {
     System.out.println("Dumping properties instance list...");
     for (int i = 0; i < pv.size(); i++)
     {
       Properties p = (Properties) pv.elementAt(i);
       writeProperties(p);
     }
   }
   
   // Dump a properties instance. 
   private void writeProperties(Properties p)
   {
     System.out.println("Properties instance: " + p.toString());
   }
   
   // Write out the location of a temporary file created for a case. 
   private void writeTempFileLocation(File f)
   {
     System.out.println("Created temp file: " + f.getAbsolutePath());
   }
   
   /**
    * Run a test on a benchmark scenario.  You can overwrite by providing values
    * in a Properties instance for anything that should be different.
    * @param name Name of the scenario
    * @param scenarioClass 
    * @param props Property values to configure scenario
    * @param consoleOutput True if console output is desired
    * @param expectedLines Number of CSV lines we expect to see
    */ 
   public void runScenario(String name, Class scenarioClass, Properties props,
       boolean consoleOutput, int expectedLines) throws Exception
   {
     // Write properties definition to a file. 
     File benchprops = File.createTempFile(name, ".properties");
     FileOutputStream os = new FileOutputStream(benchprops);
     props.setProperty("scenario", scenarioClass.getName());
     props.store(os, "# Test properties file for BenchMark unit test");
     os.close();
     writeTempFileLocation(benchprops);
     
     // Create an output file for comma separated values.  
     File csv = File.createTempFile(name, ".csv");
     writeTempFileLocation(csv);
 
     // Create an output file for text report.  
     File text = File.createTempFile(name, ".txt");
     writeTempFileLocation(text);
 
     // Create an output file for HTML report.  
     File html = File.createTempFile(name, ".html");
     writeTempFileLocation(html);
 
     // Run the benchmark.  
     Benchmark benchmark = new Benchmark(); 
     benchmark.setProps(benchprops.getAbsolutePath());
     benchmark.setText(text.getAbsolutePath());
     benchmark.setCsv(csv.getAbsolutePath());
     benchmark.setHtml(html.getAbsolutePath());
     benchmark.go();
     
     // Ensure the csv file has the indicated number of lines.  
     FileReader fr = new FileReader(csv);
     BufferedReader br = new BufferedReader(fr);
     int actualLines = 0;
     while (br.readLine() != null)
       actualLines++;
     
     assertEquals("Checking CSV output lines from benchmark run", expectedLines, actualLines);
     
     // Clean up property files. 
     benchprops.delete();
     csv.delete();
   }
 }
