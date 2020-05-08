 package com.castlemon.jenkins.performance;
 
 import hudson.FilePath;
 import hudson.model.Action;
 import hudson.model.AbstractBuild;
 import hudson.model.DirectoryBrowserSupport;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 
 /*
  * This class provides the link to the reports at a build level
  */
 
 public class CucumberBuildAction implements Action {
 
 	private final AbstractBuild<?, ?> build;
 
 	public CucumberBuildAction(AbstractBuild<?, ?> build) {
 		super();
 		this.build = build;
 	}
 
 	public String getDisplayName() {
 		return "Cucumber Build Performance Reports";
 	}
 
 	public String getIconFileName() {
		return "/plugins/cucumber-perf/performance.png";
 	}
 
 	public String getUrlName() {
 		return "cucumber-perf-reports";
 	}
 
 	public void doDynamic(StaplerRequest req, StaplerResponse rsp)
 			throws IOException, ServletException {
 		DirectoryBrowserSupport dbs = new DirectoryBrowserSupport(this,
 				new FilePath(this.dir()), "title", null, false);
 		dbs.setIndexFileName("basicview.html");
 		dbs.generateResponse(req, rsp, this);
 	}
 
 	protected File dir() {
 		return new File(build.getRootDir(), "cucumber-perf-reports");
 	}
 
 }
