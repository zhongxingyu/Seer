 /**
  *
  *
  *  To Do: Do TCP Gateway,
  *
  *
  *
  *
  *
  *
  *
  */
 package org.mxunit.ant;
 
 import org.apache.tools.ant.Task;
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Project;
 import java.util.*;
 import java.util.Date;
 import java.net.*;
 import java.io.*;
 import java.text.DecimalFormat;
 
 
 /**
  * @author Billy
  *
  */
 public class MXUnitAntTask extends Task {
 
     String version = "1.0.6";
     //private Project project;
 
     String server;
     public void setServer(String server) {
       this.server = server;
     }
 
     int port = 80;
     public void setPort(int port) {
       this.port = port;
     }
 
     String defaultRunner = "/mxunit/runner/HttpAntRunner.cfc";
     public void setDefaultRunner(String defaultRunner) {
       if(defaultRunner.equals("")){
         return;//in case the defaultrunner is specified as an empty string.
       }
       this.defaultRunner = defaultRunner;
     }
 
     String connectionMethod = "http";
     public void setConnectionMethod(String connectionMethod) {
       this.connectionMethod = connectionMethod;
     }
 
     String outputdir;
     public void setOutputdir(String outputdir) {
       this.outputdir = outputdir;
     }
 
     String username;
     public void setUsername(String username) {
       this.username = username;
     }
 
     String password;
     public void setPassword(String password) {
       this.password = password;
     }
 
     String authMethod = "no_auth";
     public void setAuthMethod(String authMethod) {
       this.authMethod = authMethod;
     }
 
     String mxunitHome = "mxunit";
     public void setMxunitHome(String mxunitHome) {
       this.mxunitHome = mxunitHome;
     }
 
     boolean verbose = false;
     public void setVerbose(boolean verbose) {
       this.verbose = verbose;
     }
 
 
     boolean haltOnError = false;
     public void setHaltOnError(boolean haltOnError) {
       this.haltOnError = haltOnError;
     }
 
     boolean haltOnFailure = false;
     public void setHaltOnFailure(boolean haltOnFailure) {
       this.haltOnFailure = haltOnFailure;
     }
 
 
     //Name of the summary file that hold info about all tests that were run
     String testResultsSummary = "testresults.properties";
     public void setTestResultsSummary(String testResultsSummary) {
       this.testResultsSummary = testResultsSummary;
     }
 
 
     //Not implemented
     //StringTokenizer params;
     String params;
     public void addText(String text){
       //this.params = new StringTokenizer(text,";", true);
       this.params = text.trim();
     }
 
     Vector testcases = new Vector();
     public Testcase createTestcase(){
       Testcase testCase = new Testcase();
       testcases.add(testCase);
       return testCase;
 
     }
 
     Vector directories = new Vector();
     public Directory createDirectory(){
       Directory directory = new Directory();
       directories.add(directory);
       return directory;
 
     }
 
     
     /////////////////////////////
     private String failureProperty;
     public String getFailureProperty() {
         return failureProperty;
     }
     public void setFailureProperty(String failureProperty) {
         this.failureProperty = failureProperty;
     }
 
    ///////////////////////    
     private String errorProperty;
     public String getErrorProperty() {
         return errorProperty;
     }
 
     public void setErrorProperty(String errorProperty) {
         this.errorProperty = errorProperty;
         
     }
     
     
     
 
   private HttpHelper helper;
 
   public MXUnitAntTask() {
 
     if(this.authMethod.equals("no_auth") ){
      this.helper = new HttpHelper();
     }
     else{
       this.helper = new HttpHelper(this.username, this.password, this.authMethod);
     }
 
 
 
 
   }
 
   public void execute() throws BuildException {
 
     DecimalFormat df = new DecimalFormat(".00");
 
     log("Greetings, earth being ...");
     log("Running MXUnitAntTask version : " + version);
 
     if(outputdir != null){
       log("Outputting results to: " + outputdir);
     }
 
     if(verbose){
       log("Verbose: " + verbose);
       log("MXunit home :"+ mxunitHome);
       log("Using server:port : " + server + ":" + port);
       log("AuthMethod: " + authMethod);
       log("Default runner : " + defaultRunner);
     }
     
    
 
     runIndividualTestCases();
     runDirectoryTests();
 
 
     helper.close();
 
     log("Total testruns: " + helper.getTotalTestRuns());
     log("Total errors: " + helper.getTotalErrors());
     log("Total failures: " + helper.getTotalFailures());
     log("Total time: " + helper.getTotalTime());
     log("Failure ratio: " + df.format(helper.getFailureRatio()) );
     log("Error ratio: " + df.format(helper.getErrorRatio()) );
     log("Success ratio: " + df.format(helper.getSuccessRatio()) );
 
 
     if(outputdir != null){
       // Can be used by Ant to conditionally process other tasks
       Properties properties = new Properties();
       properties.setProperty("total.runs", String.valueOf(helper.getTotalTestRuns()));
       properties.setProperty("total.errors", String.valueOf(helper.getTotalErrors()));
       properties.setProperty("total.failures", String.valueOf(helper.getTotalFailures()));
       properties.setProperty("total.time", String.valueOf(helper.getTotalTime()));
       properties.setProperty("failure.ratio", df.format(helper.getFailureRatio()));
       properties.setProperty("error.ratio", df.format(helper.getErrorRatio()));
       properties.setProperty("success.ratio", df.format(helper.getSuccessRatio()));
 
       try {
         properties.store(new FileOutputStream(this.outputdir + "/" + this.testResultsSummary), null);
        }
        catch (IOException e) {
         e.printStackTrace();
        }
     }
 
 
     log("Fare thee well, human.");
 
   }
 
 
 
 private void runIndividualTestCases() {
 
   int counter = 0;
 
   for(Iterator i = testcases.iterator();i.hasNext();){
       Testcase test = (Testcase)i.next();
 
       log("Loading Testcase : " + test.getName());
       //Use the default runner if not specified
 
       String runner = this.defaultRunner;
 
       if(!test.getRunner().equals("")){
         runner = test.getRunner();
       }
 
       String qs = "method=" + test.getRemoteMethod() +"&type=testcase&value=" + test.getName() +
                   "&recurse=false&packageName=" +  test.getPackageName();
       String outputFile = outputdir + "/mxunittestcase_" + ++counter + ".xml";
       doTest(qs, outputFile, runner);
 
   }//endfor
 }// end run
 
 
 
 private void runDirectoryTests() {
 
     int counter = 0;
 
     for(Iterator i = directories.iterator();i.hasNext();){
       Directory d = (Directory)i.next();
 
       //Use the default runner if not specified
       String runner = this.defaultRunner;
       if(!d.getRunner().equals("")){
         runner = d.getRunner();
       }
       
       if(d.getComponentPath() == null || d.getComponentPath().equals("")){
       	log("WARNING: optional componentPath id deprecated. Please specify the componentPath from now on.");
       }
 
       String qs = "method=" + d.getRemoteMethod() +"&type=dir&value=" + d.getPath() +
                   "&recurse=" + d.getRecurse() + "&excludes=" + d.getExcludes() + "&packageName=" +  d.getPackageName() +
                   "&componentPath=" + d.getComponentPath();
       String outputFileName = outputdir + "/mxunitdirectorytestsuite_" + ++counter + ".xml";
 
 
        doTest(qs, outputFileName, runner);
 
    }//end for
 
 }
 
 
 private void doTest(String queryString, String fileName, String runner) throws BuildException {
 
   try{
       int status = helper.runTest(this.server, this.port, runner, queryString );
       String summary = helper.getTestResultSummary();
 
       log("[HttpStatus] " + status);
 
       if (status > 304){
     	log("[HttpStatus] -- error. Output was: " + helper.getHttpResponseString());
     	if(this.haltOnError) {
     		throw new BuildException("Http Status returned an error. HttpStatus code: " + status);
     	}
       }
 
       if(helper.getTotalFailures() > 0 && this.getFailureProperty() != null){
     	   getProject().setProperty(this.getFailureProperty(), "true"); 
       }
       
       if(helper.getTotalErrors() > 0 && this.getErrorProperty() != null){
     	 if(this.getFailureProperty() != null){
     	   getProject().setProperty(this.getFailureProperty(), "true");
     	 }
     	 getProject().setNewProperty(this.getErrorProperty(), "true");
       }
       
       if(this.haltOnFailure && (helper.getTotalFailures() > 0) ){
         
     	throw new BuildException("haltonFailure property set to TRUE and one or more FAILURES occured. No files generated. Set the haltonfailure property to true in the MXUnit Ant task to view test result details");
       }
 
       if(this.haltOnError && (helper.getTotalErrors() > 0) ){
         throw new BuildException("haltonError property set to TRUE and one or more ERRORS occured. No files generated. Set the haltonerror property to true in the MXUnit Ant task to view test result details");
       }
 
       System.out.println("[Testresults] " + summary );
 
       if(outputdir != null){
         File file = new File(fileName);
         file.createNewFile();
         BufferedInputStream bis = new BufferedInputStream(helper.getHttpInputStream());
         log("writing file : " + fileName);
         writeFile(bis,file);
         bis.close();
       }
     }
	catch (java.net.ConnectException ce) {
	  System.out.println("[mxunit error] Error connecting");
	  ce.printStackTrace();
	  throw new BuildException(ce);
	}
  	catch(java.io.IOException ioe){
       System.out.println("[mxunit error] Error trying to write to : " + outputdir + ". Please make sure this output dircetory exists.");
       System.out.println("[mxunit error] Exiting ... see stacktrace for details.");
       ioe.printStackTrace();
     }
     catch(BuildException be){
       throw new BuildException(be);
     }
     catch(Exception e){
      //e.printStackTrace();
      throw new BuildException(e);
     }//end catch
 
 }
 
 
 
 
 private void writeFile(InputStream inStream, File file) throws IOException {
     final int bufferSize = 1000;
     FileOutputStream fout = new FileOutputStream(file);
     byte[] buffer = new byte[bufferSize];
     int readCount = 0;
     while ((readCount = inStream.read(buffer)) != -1) {
       if (readCount < bufferSize) {
         fout.write(buffer, 0, readCount);
       } else {
         fout.write(buffer);
       }
     }
     fout.close();
   }
 
 
 
 
   /**
    * Represents a nested Directory element
    *
    * */
   public class Directory {
     public Directory(){}
  //////////////////////////////////////
     String path;
     public void setPath(String path){
       this.path = path;
     }
     public String getPath(){
       return this.path;
     }
  //////////////////////////////////////
     String runner = ""; //Default runner for directories
     public void setRunner(String runner){
       this.runner = runner;
     }
 
     public String getRunner(){
       return this.runner;
     }
  //////////////////////////////////////
      String remoteMethod = "run";
      public void setRemoteMethod(String remoteMethod){
        this.remoteMethod = remoteMethod;
      }
 
      public String getRemoteMethod(){
        return this.remoteMethod;
     }
  //////////////////////////////////////
    String packageName = "mxunit.testresults";
    public void setPackageName(String packageName){
        this.packageName = packageName;
    }
    public String getPackageName(){
        return this.packageName;
     }
  //////////////////////////////////////
    String recurse = "false";
    public void setRecurse(String recurse){
       this.recurse = recurse;
    }
     public String getRecurse(){
        return this.recurse;
     }
  //////////////////////////////////////
     String componentPath = ""; //optional with dirrunner ... prefix of component org.foo.bar.MyComponent
     public void setComponentPath(String componentPath){
        this.componentPath = componentPath;
     }
     public String getComponentPath(){
        return this.componentPath;
     }
  //////////////////////////////////////
     String includes; //to do
     public void setIncludes(String includes){
        this.includes = includes;
     }
     public String getIncludes(){
        return this.includes;
     }
     //////////////////////////////////////
     String excludes; //to do
     public void setExcludes(String excludes){
        this.excludes = excludes;
      }
     public String getExcludes(){
        return this.excludes;
     }
 
   }//End Directory inner class
 
 
 
   /**
    * Represents a nested testcase element
    *
    * */
   public class Testcase {
     public Testcase(){}
 
     String name;
     public void setName(String name){
       this.name = name;
     }
 
     public String getName(){
       return this.name;
     }
 
     //////////////////////////////////////
         String runner = "";
         public void setRunner(String runner){
           this.runner = runner;
         }
 
         public String getRunner(){
           return this.runner;
         }
      //////////////////////////////////////
          String remoteMethod = "run";
          public void setRemoteMethod(String remoteMethod){
            this.remoteMethod = remoteMethod;
          }
 
          public String getRemoteMethod(){
            return this.remoteMethod;
     }
   //////////////////////////////////////
      String packageName = "mxunit.testresults";
      public void setPackageName(String packageName){
          this.packageName = packageName;
      }
      public String getPackageName(){
          return this.packageName;
       }
 
     }//end TestCase inner class
 
 
 
 }// end class
