 package yeti.environments.java;
 
 import java.io.ByteArrayOutputStream;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Vector;
 
 import yeti.Yeti;
 import yeti.YetiLog;
 import yeti.YetiLogProcessor;
 
 /**
  * Class that represents a log processor for Java. 
  * <code>processLog</code> generates test cases in each cell of the array.
  * 
  * @author Manuel Oriol (manuel@cs.york.ac.uk)
  * @date Jun 22, 2009
  *
  */
 public class YetiJavaLogProcessor extends YetiLogProcessor {
 
 	/**
 	 * A constructor for the YetiJavaLogProcessor.
 	 */
 	public YetiJavaLogProcessor() {	
 
 	}
 
 	/* (non-Javadoc)
 	 * Adds a timestamp on the log
 	 * 
 	 * @see yeti.YetiLogProcessor#appendToCurrentLog(java.lang.String)
 	 */
 	@Override
 	public void appendToCurrentLog(String newLog) {
 		// substantific gains (2-3x) in execution time can be done by NOT adding the timestamp
 		// super.appendToCurrentLog(newLog);
 		super.appendToCurrentLog(newLog+" // time:"+(new Date()).getTime());
 	}
 
 	/* 
 	 * A nicer printer for Yeti and Java
 	 * 
 	 * (non-Javadoc)
 	 * @see yeti.YetiLogProcessor#appendFailureToCurrentLog(java.lang.String)
 	 */
 	public void appendFailureToCurrentLog(String newLog){
 		String log = this.getCurrentLog();
 		log=log+"\n"+newLog;
 		this.setCurrentLog(log);
 		this.numberOfErrors++;
 	}	
 
 	/**
 	 * The number of errors in last Logs processed.
 	 */
 	private static int lastLogTotalSize=0;
 
 	/**
 	 * The number of non-unique bugs in last logs.
 	 */
 	private static int lastNumberOfNonUniqueBugs=0;
 
 	/**
 	 * Generates a Vector<String> that a test case for each cell.
 	 * 
 	 * @see yeti.YetiLogProcessor#processLogs()
 	 */
 	@Override
 	public Vector<String> processLogs() {		
 		Vector<String> tmp = YetiJavaLogProcessor.sliceStatically(this.getCurrentLog());
 		Vector<String> result = new Vector<String>();
 		int i = 0;
 		for (String tc: tmp) {
 			i++;
 			result.add("public static void test_"+i+"() throws Exception {\n"+tc+"\n}");
 		}
 		result.add("/** Non-Unique bugs: "+lastNumberOfNonUniqueBugs+", Unique Bugs: "+result.size()+", Logs size (locs): "+lastLogTotalSize+"**/");
 		return result;
 	}
 
 
 	/**
 	 * Generates the kill value for this line.
 	 * 
 	 * @param loc the line of code to treat.
 	 * @return the String value for the variable to kill.
 	 */
 	public static String kill(String loc){
 		boolean isAssignment = (loc.indexOf("=")>0);
 		int indexOfSpace = loc.indexOf(" ");
 
 
 		YetiLog.printDebugLog("loc: "+loc, YetiJavaLogProcessor.class);
 		if (isAssignment){
 			YetiLog.printDebugLog("kill: "+ loc.substring(indexOfSpace+1,loc.indexOf("=")), YetiJavaLogProcessor.class);
 			return loc.substring(indexOfSpace+1,loc.indexOf("="));
 		}else {
 			YetiLog.printDebugLog("no kill", YetiJavaLogProcessor.class);
 			return null;
 		}
 	}
 
 	/**
 	 * Generates the vector of variables that are used by this line of code.
 	 * 
 	 * @param loc the line of code to treat.
 	 * @return a vector containing all the variables that should be added to the 
 	 * list of values that matter.
 	 */
 	public static Vector<String> gen(String loc){
 
 		boolean isAssignment = (loc.indexOf("=")>0);
 		boolean isCreation = (loc.indexOf("new ")>0);
 		boolean isMethodCall = (loc.indexOf("(")>0);
 		boolean isComment = loc.startsWith("/**");
 
 		// if this is a comment we return no gen
 		if (isComment)
 			return new Vector<String>();
 
 		// we initialize the values
 		String localLoc = loc;
 		Vector<String> valuesThatMatter = new Vector<String>();
 		YetiLog.printDebugLog("loc: "+loc, YetiJavaLogProcessor.class);
 
 		// if it is not a creation method but it is a method call
 		if (!isCreation&&isMethodCall) {
 			String target;
 			// we find the target
 			if (isAssignment)
 				target = loc.substring(loc.indexOf("=")+1,loc.lastIndexOf('.'));
 			else
 				target = loc.substring(0,loc.lastIndexOf('.'));
 			YetiLog.printDebugLog("target: "+target, YetiJavaLogProcessor.class);
 
 			// we add it to the values that matter
 			valuesThatMatter.add(target);
 		}
 		// for all method calls we extract arguments
 		if (isMethodCall) {
 			int indexOfAfterOpenParenthesis = loc.indexOf("(")+1;
 			int indexOfCloseParenthesis = loc.indexOf(")");
 			localLoc = localLoc.substring(indexOfAfterOpenParenthesis, indexOfCloseParenthesis);
 
 			// we add all arguments one after he other
 			if (localLoc.length()>0)
 				for (String var: localLoc.split(",")){
 					YetiLog.printDebugLog("arg: "+var, YetiJavaLogProcessor.class);
 					if (!var.equals("null")) 
 						valuesThatMatter.add(var);
 				}
 		}
 		// we return the result
 		return valuesThatMatter;
 	}
 
 	/**
 	 * Checks whether the line contains kills or gen that matter for the variables
 	 * 
 	 * @param loc the line of code to consider
 	 * @param varNames the variable names
 	 * @return <code>true</code> it it contains a gen or a kill, <code>false</code> otherwise.
 	 */
 	public static boolean containsKillsOrGens(String loc, Vector<String> varNames){
 		Vector<String> gen0 = gen(loc);
 		String kill0=kill(loc);
 
 		// we iterate through all names
 		for (String var: varNames) {
 			if (kill0!=null)
 				if (kill0.equals(var)) return true;
 			for (String geni: gen0) {
 				if (geni.equals(var)) return true;
 			}
 
 		}
 		return false;
 
 	}
 
 	/**
 	 * Slices the code of the test case statically and conervatively.
 	 * 
 	 * Does not make any assumption on command-query separation.
 	 * 
 	 * @param log the log to slice
 	 * @return a vector with all generated test cases.
 	 */
 	public static Vector<String> sliceStatically(String log){
 		Vector<String> testCases = new Vector<String>();
 
 		// we split the lines of code
 		String []linesOfTest = log.split("\n");
 
 		// for logging purposes
 		lastLogTotalSize=linesOfTest.length;
 
 		// for logging purposes, we want to know how many errors we found
 		int numberOfErrorsParsed = 0;
 
 		// we make the list of errors
 		HashMap<String,Integer> listOfErrors= new HashMap<String,Integer>();
 		// we look for all errors up
 		for (int i = 0; i<linesOfTest.length; i++){
 			String exceptionTrace="";
 			if (linesOfTest[i].startsWith("/**BUG")||linesOfTest[i].startsWith("/**POSSIBLE BUG")){
 				// we aggregate the results and give some output
 				int k=i+1;
				
				// just in case the trace is unfinished
				if (k>=linesOfTest.length)
					continue;
 
 				// logging purposes
 				numberOfErrorsParsed++;
 
 				// the exception starts with a comment
 				if (linesOfTest[k].startsWith("/**")) {
 					// will be used to filter the yeti exception stack
 					boolean isInYetiExceptions=false;
 
 					// we continue until the end of the exception trace
 					while (k<linesOfTest.length && !linesOfTest[k].contains("**/")){
 						// if we arrive to the reflexive call, we cut
 						if (!isInYetiExceptions&&linesOfTest[k].contains("sun.reflect.")) {
 							isInYetiExceptions=true;
 						}
 						if (!isInYetiExceptions)
 							exceptionTrace=exceptionTrace+"\n"+linesOfTest[k++];
 						else
 							k++;
 					}
 				}
 				// we add the error if it is unique
 				if (!listOfErrors.containsKey(exceptionTrace)&&Yeti.testModule.isThrowableInModule(exceptionTrace))
 					listOfErrors.put(exceptionTrace,i-1);
 			}
 		}
 
 		// for logging purposes:
 		lastNumberOfNonUniqueBugs=numberOfErrorsParsed;
 
 		// for each error:
 		for(int i: listOfErrors.values()){
 			int finalLength = 0;
 			String currentTestCase = linesOfTest[i]+"\n"+linesOfTest[i+1];
 			Vector<String> variables = gen(linesOfTest[i]);
 			boolean ignoreNext = false;
 			// for all lines previously executed:
 			for (int j = i-1; j>=0 ; j--){
 				// if there is no active variable we stop here
 				if (variables.isEmpty()) break;
 
 				// if there is an error, we ignore the call
 				if (ignoreNext) {
 					if (linesOfTest[j+1].startsWith("/**"))
 						ignoreNext = false;
 					continue;
 				}
 				if (linesOfTest[j].endsWith("**/")) {
 					ignoreNext=true;
 					continue;
 				}
 
 
 				// if the line contains meaningful kills or gen 
 				// then we include it in the trace
 				// Note we cannot take an aggressive stance on command-query 
 				// separation with Java
 				if (containsKillsOrGens(linesOfTest[j], variables)) {
 					String kill0 = kill(linesOfTest[j]);
 					// we remove the kill
 					if (kill0!=null)
 						for (int k=0;k<variables.size();k++){
 							if (variables.get(k).equals(kill0)){
 								variables.remove(k--);
 							}
 						}
 					// we add the gens
 					variables.addAll(gen(linesOfTest[j]));
 					// we add the line to the test case
 					currentTestCase = linesOfTest[j] +"\n"+ currentTestCase;
 					finalLength++;
 				}
 			}
 			// we aggregate the results and give some output
 			String exceptionTrace="";
 			int k=i+2;
 			// the exception starts with a comment
 			if (linesOfTest[k].startsWith("/**")) {
 				// will be used to filter the yeti exception stack
 				boolean isInYetiExceptions=false;
 
 				// we continue until the end of the exception trace
 				while (k<linesOfTest.length &&!linesOfTest[k].contains("**/")){
 					// if we arrive to the reflexive call, we cut
 					if (!isInYetiExceptions&&linesOfTest[k].contains("sun.reflect.")) {
 						isInYetiExceptions=true;
 					}
 					if (!isInYetiExceptions)
 						exceptionTrace=exceptionTrace+"\n"+linesOfTest[k++];
 					else
 						k++;
 				}
 				// we add the comment at the end
 				exceptionTrace=exceptionTrace+"\n"+linesOfTest[k++];				
 			}
 			currentTestCase=currentTestCase+exceptionTrace;
 			currentTestCase=currentTestCase+"\n/** original locs: "+i+" minimal locs: "+(finalLength+1)+"**/";
 			testCases.add(currentTestCase);
 		}
 
 		YetiLog.printDebugLog("Number of Errors: "+listOfErrors.size()+" Number of test cases: "+testCases.size(), YetiJavaLogProcessor.class);
 		//testCases.add("Number of Errors: "+listOfErrors.size()+" Number of test cases: "+testCases.size());
 
 		return testCases;
 
 	}
 	/**
 	 * Printer for raw logs
 	 * 
 	 * @parameter message the message log to print.
 	 */
 	public void printMessageRawLogs(String message) {
 		System.err.println("YETI LOG: "+message);
 	}
 
 	/**
 	 * A list of traces for relevant detected errors. 
 	 */
 	public HashMap<String,Object> listOfErrors=new HashMap<String, Object>();
 
 
 	/**
 	 * Printer for throwables in raw logs
 	 * 
 	 * @parameter t the throwable log to print.
 	 */
 	public void printThrowableRawLogs(Throwable t) {
 		System.err.print("YETI EXCEPTION - START ");
 		OutputStream os=new ByteArrayOutputStream();
 		PrintStream ps = new PrintStream(os);
 		if (t!=null) 
 			t.printStackTrace(ps);
 		else 
 			ps.println("Thread killed by Yeti!");
 		String throwableLog = os.toString();
 		// we split the lines of code
 		String []linesOfTest = throwableLog.split("\n");
 		// we continue until the end of the exception trace
 		int k = 0;
 		String exceptionTrace = "";
 		while (k<linesOfTest.length){
 
 			// if we arrive to the reflexive call, we cut
 			if (linesOfTest[k].contains("sun.reflect.")) {
 				break;
 			}
 			exceptionTrace=exceptionTrace+"\n"+linesOfTest[k++];
 		}
 		// if the trace is actually relevant for the considered module...
 		if (Yeti.testModule.isThrowableInModule(exceptionTrace)) {
 			// we print the exception trace
 			System.err.println(exceptionTrace);
 			String s0=exceptionTrace.substring(exceptionTrace.indexOf('\t'));
 			if (!listOfErrors.containsKey(s0)) {
 				listOfErrors.put(s0,this);
 			}
 		}
 		else 
 			System.err.println("- NOT IN TESTED MODULE"+exceptionTrace);
 		System.err.println("YETI EXCEPTION - END ");
 
 
 
 	}
 
 	/**
 	 * Printer for throwables in no logs
 	 * 
 	 * @parameter t the throwable log not to print.
 	 */
 	public void printThrowableNoLogs(Throwable t) {
 		OutputStream os=new ByteArrayOutputStream();
 		PrintStream ps = new PrintStream(os);
 		if (t!=null) 
 			t.printStackTrace(ps);
 		String throwableLog = os.toString();
 		// we split the lines of code
 		String []linesOfTest = throwableLog.split("\n");
 		// we continue until the end of the exception trace
 		int k = 0;
 		String exceptionTrace = "";
 		while (k<linesOfTest.length){
 
 			// if we arrive to the reflexive call, we cut
 			if (linesOfTest[k].contains("sun.reflect.")) {
 				break;
 			}
 			exceptionTrace=exceptionTrace+"\n"+linesOfTest[k++];
 		}
 		// if the trace is actually relevant for the considered module...
 		if (Yeti.testModule.isThrowableInModule(exceptionTrace)) {
 			String s0=exceptionTrace.substring(exceptionTrace.indexOf('\t'));
 			if (!listOfErrors.containsKey(s0)) {
 				listOfErrors.put(s0,new Date());
 			}
 		}
 	}
 
 
 
 }
