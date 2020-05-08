 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 package mindtree.jmeterplugin;
 
 import java.io.File;
 import java.io.Serializable;
 import java.util.Arrays;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.apache.jmeter.engine.event.LoopIterationEvent;
 import org.apache.jmeter.samplers.SampleEvent;
 import org.apache.jmeter.samplers.SampleListener;
 import org.apache.jmeter.samplers.SampleResult;
 import org.apache.jmeter.testelement.AbstractTestElement;
 import org.apache.jmeter.testelement.TestListener;
 import org.apache.jmeter.testelement.property.JMeterProperty;
 import org.apache.jmeter.testelement.property.PropertyIterator;
 import org.apache.jmeter.util.JMeterUtils;
 import org.apache.jorphan.logging.LoggingManager;
 import org.apache.log.Logger;
 
 /**
  * Save Error responseData and JMeter state(properties/variables) 
  * to a set of files called Error Response file and Property file
  * when error occurs during execution.
  * 
  * This functions both in GUI and non-GUI runs.
  * 
  * For multiple errors with same response the plugin will create only one error response file.
  * Whereas,property file will log the details of every single error that occured.
  * JMeter properties/variables to be logged can be defined by the user.
  */
 
 public class ErrorListener extends AbstractTestElement implements Serializable, SampleListener,TestListener {
 	static final Logger log = LoggingManager.getLoggerForClass();
 	private static final long serialVersionUID = 240L;
 
 	/** To synchronize the process of logging errors and properties to files*/
 	static Object syncObject =new Object();
 
 	/** Base Properties referenced by plugin to log errors and store the files*/
 	static String BASEPATH;
 	static String ERRORFILE;
 	static String PROPFILE;
 	static String FILEEXT;
 	static String VARIABLE_NAME;
 	static boolean ERRORS_ONLY =true;
 	static boolean SUCCESS_ONLY =false;
 	static  boolean SKIP_SUFFIX =false; //$NON-NLS-1$
 	static String propertyFileExtension=".csv";
 
 	/** Holds the count of properties defined by the user*/
 	static int PropertyCount=0;
 	//Not used currently
 	static String JPropCount = JMeterUtils.getPropDefault("JmeterPropertiesCountToLog", "5");
 
 	/** Holds the property file name and error file name*/
 	static Map<String, String> ErrorFileSampleMap = new ConcurrentHashMap<String, String>();
 	//Not used currently
 	static Map<String, String> PropertyFileSampleMap = new ConcurrentHashMap<String, String>();
 
 	/** Options whether to log only properties or both errors and properties */
 	static String LOG_PROPERTIES = "false";
 	static String LOG_ERRORS_PROPERTIES ="true";
 	static boolean ONLYPROPS = false;
 	static boolean ERRORSANDPROPS =true;
 
 	/** Variables used intermediately to identify the status of execution 
 	 *  and execution of specific functions
 	 */
 
 	static boolean flag=false;
 	static boolean warning=false;
 	static boolean headersCreated=false;
 	static String reportPath=null;
 	static String propertyPath=null;
 	static boolean directoryCreated=false;
 	static boolean updateAllowed=true;
 
 	/**
 	 * Constructor is initially called once for each occurrence in the test plan
 	 * For GUI, several more instances are created Then clear is called at start
 	 * of test Called several times during test startup The name will not
 	 * necessarily have been set at this point.
 	 */
 	public ErrorListener() {
 		super();
 	}
 
 	private static void initialize() {
 		flag=false;
 		directoryCreated=false;
 		headersCreated=false;
 		updateAllowed=false;
 		PropertyCount=0;
 		Arrays.fill(ErrorListenerGui.propertyHeaderArray,null);
 		synchronized (PropertyFileSampleMap) {
 			PropertyFileSampleMap.clear();}
 		synchronized (ErrorFileSampleMap) {
 			ErrorFileSampleMap.clear();}
		reportPath="ErrorListener"+"_"+"TimeStamp-"+GetTimeStampInHMS()+"_"+GetTimeStampInMs()+File.separator;
 		propertyPath="_TimeStamp-"+GetTimeStampInHMS()+"_"+GetTimeStampInMs();
 	}
 
 	/**
 	 * Constructor for use during startup (intended for non-GUI use) @param name
 	 * of summariser
 	 */
 	public ErrorListener(String name) {
 		this();
 		setName(name);
 	}
 
 	/**
 	 * This is called once for each occurrence in the test plan, before the
 	 * start of the test. The super.clear() method clears the name (and all
 	 * other properties), so it is called last.
 	 */
 	@Override
 	public void clear() {
 		super.clear();
 	}
 
 
 
 	/**
 	 * Saves the error response and properties of sample error request in files
 	 *
 	 * @see org.apache.jmeter.samplers.SampleListener#sampleOccurred(org.apache.jmeter.samplers.SampleEvent)
 	 */
 	public void sampleOccurred(SampleEvent e) {
 		if(!e.getResult().isSuccessful())
 		{
 			synchronized(ErrorListener.syncObject)
 			{
 				if(flag==false)
 				{
 					/** Fetch and initialize the base properties*/
 
 					BASEPATH=getPropertyAsString("FileBasePath").trim();
 					if(null==BASEPATH || BASEPATH.isEmpty())
 					{
 						BASEPATH= JMeterUtils.getJMeterBinDir();	
 					}
 					ERRORFILE=getPropertyAsString("ErrorFilePrefix").trim();
 					if(null==ERRORFILE || ERRORFILE.isEmpty())
 					{
 						ERRORFILE="Error";
 					}
 					PROPFILE=getPropertyAsString("PropertyFilePrefix").trim();
 					if(null==PROPFILE || PROPFILE.isEmpty())
 					{
 						PROPFILE="Property";
 					}
 					FILEEXT=getPropertyAsString("FileExtensionName").trim();
 					VARIABLE_NAME=getPropertyAsString("VariableName").trim();
 					if(null==VARIABLE_NAME || VARIABLE_NAME.isEmpty())
 					{
 						VARIABLE_NAME="JVariable";
 					}
 
 					ERRORSANDPROPS=getPropertyAsBoolean("ErrorsAndProperties");
 					ONLYPROPS=getPropertyAsBoolean("OnlyProperties");
 
 					/** Construct the directory path to store the error and property data*/
 					StringBuilder sb = new StringBuilder(ErrorListener.BASEPATH);
					if(!ErrorListener.BASEPATH.endsWith(File.separator))
 					{
						sb.append(File.separator);    		    	
 					}
 					BASEPATH=sb.append(reportPath).toString();
 					PROPFILE=BASEPATH+PROPFILE+propertyPath+propertyFileExtension;
 
 					/** Fetch and initialize the user defined properties*/
 					PropertyIterator iter = propertyIterator();
 					while (iter.hasNext()) {
 						JMeterProperty prop = iter.next();
 						if(prop.getName().startsWith("JmeterProperty"))
 						{
 							ErrorListenerGui.propertyHeaderArray[PropertyCount]=prop.getStringValue().trim();
 							PropertyCount++;
 						}
 					}
 					flag=true;
 				}
 
 				/** Create the directory where the files will be stored*/	
 				File createDir = new File(ErrorListener.BASEPATH);
 				if(!createDir.exists())
 				{
 					directoryCreated=createDir.mkdirs();
 				}
 
 				if(directoryCreated==true)
 				{
 					warning=false;
 					processSample(e.getResult(), new Counter());
 				}
 				else
 				{  
 					if(warning=false)
 					{
 						System.out.println("Directory could not be created.Hence,not proceeding with logging errors...");
 					}warning=true;
 				} 
 			}
 		}
 	}
 
 
 
 	/**
 	 * Recurse the whole (sub)result hierarchy.
 	 *
 	 * @param s Sample result
 	 * @param c sample counter
 	 */
 	private void processSample(SampleResult s, Counter c) {
 
 		SaveResult saveResult=new SaveResult();
 		saveResult.SaveSampleResult(s,c.num++);
 		SampleResult[] sr = s.getSubResults();
 		for (int i = 0; i < sr.length; i++) {
 			processSample(sr[i], c);
 		}
 	}
 
 	/**
 	 * Get the current timestamp in human readable format
 	 * 
 	 */
 	private static String GetTimeStampInHMS() {
 
 		long epoch = System.currentTimeMillis()/1000;
 		String date = new java.text.SimpleDateFormat("dd-MM-yyyy_HH-mm-ss").format(new java.util.Date (epoch*1000));
 		return date; 
 
 	}
 
 	/**
 	 * Get the current timestamp in milliseconds
 	 * 
 	 */
 	private static long GetTimeStampInMs() {
 		return System.currentTimeMillis();
 	}
 
 	// Mutable int to keep track of sample count
 	private static class Counter{
 		int num;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void sampleStarted(SampleEvent e) {
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void sampleStopped(SampleEvent e) {
 		// not used
 	}
 
 	@Override
 	public void testEnded() {
 		updateAllowed=true;
 	}
 
 	@Override
 	public void testEnded(String arg0) {
 		updateAllowed=true;
 	}
 
 	@Override
 	public void testIterationStart(LoopIterationEvent arg0) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void testStarted() {
 		initialize();
 
 	}
 
 	@Override
 	public void testStarted(String arg0) {
 		initialize();
 
 	}
 
 	/*@Override
 	public void processBatch(List<SampleEvent> arg0) throws RemoteException {
 		// TODO Auto-generated method stub
 	}*/
 }
 
 
