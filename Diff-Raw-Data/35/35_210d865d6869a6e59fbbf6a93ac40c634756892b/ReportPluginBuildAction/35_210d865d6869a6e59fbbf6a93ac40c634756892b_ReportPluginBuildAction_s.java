 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.redhat.engineering.jenkins.report.plugin;
 
 import com.redhat.engineering.jenkins.testparser.Parser;
 import com.redhat.engineering.jenkins.testparser.results.Filter;
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
     
     
     public ReportPluginBuildAction(AbstractBuild<?, ?> build, MatrixBuildTestResults results){
 	this.results = results;
 	this.build = build;
 	results.setOwner(this.build);
 	
 	/* TODO: speed up by caching, this would work but we need to update 
 	 * these fields every time we add MatrixRunTestResults to MatrixBuildTestResults
 	 * results
 	 * Example:
 	 * this.passedTestCount = results.getPassedTestCount();
 	 */
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
 	// TODO: speed up via caching
 	// return this.passedTestCount;
 	return results.getPassedTestCount();
     }
 
     public int getFailedTestCount() {
 	// TODO: speed up via caching
 	//return this.failedTestCount;
 	return results.getFailedTestCount();
     }
 
     public int getSkippedTestCount() {
 	// TODO: speed up via caching
 	//return this.skippedTestCount;
 	return results.getSkippedTestCount();
     }
     
     
     public void addFilter(Filter filter){
 	results.addFilter(filter);
     }
     
     public void removeFilter(){
 	results.removeFilter();
     }
 }
