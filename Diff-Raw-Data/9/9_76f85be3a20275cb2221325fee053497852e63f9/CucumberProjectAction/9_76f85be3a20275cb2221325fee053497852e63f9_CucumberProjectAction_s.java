 package com.castlemon.jenkins.performance;
 
 import hudson.FilePath;
 import hudson.model.ProminentProjectAction;
 import hudson.model.AbstractItem;
 import hudson.model.AbstractProject;
 import hudson.model.DirectoryBrowserSupport;
 import hudson.model.Run;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 
 public class CucumberProjectAction implements ProminentProjectAction {
 
 	private final AbstractItem project;
 
 	public CucumberProjectAction(AbstractItem project) {
 		super();
 		this.project = project;
 	}
 
 	public String getDisplayName() {
 		return "Cucumber Project Performance Report";
 	}
 
 	public String getIconFileName() {
		return "/plugin/cucumber-perf/cuke.png";
 	}
 
 	public String getUrlName() {
 		return "cucumber-perf-reports";
 	}
 
 	public void doDynamic(StaplerRequest req, StaplerResponse rsp)
 			throws IOException, ServletException {
 		DirectoryBrowserSupport dbs = new DirectoryBrowserSupport(this,
 				new FilePath(this.dir()), "title", null, false);
 		dbs.setIndexFileName("projectview.html");
 		dbs.generateResponse(req, rsp, this);
 	}
 
 	protected File dir() {
 		if (this.project instanceof AbstractProject) {
 			AbstractProject abstractProject = (AbstractProject) this.project;
 
 			Run run = abstractProject.getLastCompletedBuild();
 			if (run != null) {
 				File javadocDir = getBuildArchiveDir(run);
 
 				if (javadocDir.exists()) {
 					return javadocDir;
 				}
 			}
 		}
 
 		return getProjectArchiveDir(this.project);
 	}
 
 	private File getProjectArchiveDir(AbstractItem project) {
 		return new File(project.getRootDir(), "cucumber-perf-reports");
 	}
 
 	private File getBuildArchiveDir(Run run) {
 		return new File(run.getRootDir(), "cucumber-perf-reports");
 	}
 }
