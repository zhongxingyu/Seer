 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.redhat.engineering.jenkins.report.plugin;
 
 import com.redhat.engineering.jenkins.testparser.Parser;
 import com.redhat.engineering.jenkins.testparser.results.MatrixBuildTestResults;
 import com.redhat.engineering.jenkins.testparser.results.MatrixRunTestResults;
 import com.redhat.engineering.jenkins.testparser.results.TestResults;
 import hudson.FilePath;
 import hudson.matrix.MatrixRun;
 import hudson.model.AbstractBuild;
 import hudson.model.Action;
 import hudson.tasks.Publisher;
 import java.io.PrintStream;
 import java.io.Serializable;
 
 /**
  *
  * @author Jan Rusnacko (jrusnack at redhat.com)
  */
 public class ReportPluginBuildAction implements Action, Serializable{
     MatrixBuildTestResults results;
     AbstractBuild<?, ?> build;
     
   /*
    * Cache test counts to speed up loading of graphs
    */
   private transient int passedTestCount;
   private transient int failedTestCount;
   private transient int skippedTestCount;
     
     public ReportPluginBuildAction(AbstractBuild<?, ?> build, MatrixBuildTestResults results){
 	super();
 	this.results = results;
 	this.build = build;
 	results.setOwner(this.build);
 	
	//initialize the cached values when TestNGBuildAction is instantiated
	this.passedTestCount = results.getPassedTestCount();
	this.failedTestCount = results.getFailedTestCount();
	this.skippedTestCount = results.getSkippedTestCount();
     }
     
 
     public MatrixBuildTestResults getBuildResults(){
 	return results;
     }
     
     public String getIconFileName() {
 	return Definitions.__ICON_FILE_NAME;
     }
 
     public String getDisplayName() {
 	return Definitions.__DISPLAY_NAME;
     }
 
     public String getUrlName() {
 	return Definitions.__URL_NAME;
     }
     
     //FIXME: implement
     static TestResults loadResults(AbstractBuild<?, ?> build, PrintStream logger) {
 	
 	FilePath testngDir = ReportPluginPublisher.getReportDir(build);
 	FilePath[] paths = null;
 	try {
 	    paths = testngDir.list("test-results*.xml");
 	} catch (Exception e) {
 	    //do nothing
 	}
     
 	TestResults tr = null;
 	if (paths == null) {
 	    if(build instanceof MatrixRun){
 		tr = new MatrixRunTestResults("");
 		tr.setOwner(build);
 		return tr;
 	    } else {
 		// TODO: [freestyle]
 		tr = new MatrixRunTestResults("");
 		tr.setOwner(build);
 		return tr;
 		
 	    }
 	}
 	    
 	Parser parser = new Parser(logger);
 	if(build instanceof MatrixRun){
 	    TestResults result = parser.parse(paths, true);
 	    result.setOwner(build);
 	    return result;
 	} else{
 	    // TODO: [freestyle]
 	    tr = new MatrixRunTestResults("");
 	    tr.setOwner(build);
 	    return tr;
 	}
     }
     
     public int getPassedTestCount() {
	return this.passedTestCount;
     }
 
     public int getFailedTestCount() {
	return this.failedTestCount;
     }
 
     public int getSkippedTestCount() {
	return this.skippedTestCount;
     }
     
 }
