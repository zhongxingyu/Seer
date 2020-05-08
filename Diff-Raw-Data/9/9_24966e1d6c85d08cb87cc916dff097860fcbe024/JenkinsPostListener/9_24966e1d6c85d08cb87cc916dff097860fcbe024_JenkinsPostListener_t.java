 package org.jenkins.plugins.jenkinspost;
 
 import hudson.Extension;
 import hudson.model.TaskListener;
 import hudson.model.Run;
 import hudson.model.listeners.RunListener;
 
 @Extension
 public class JenkinsPostListener extends RunListener<Run> {
 	
 	public JenkinsPostListener() {
 		super(Run.class);
 	}
 	
 	@Override
 	public void onCompleted(Run r, TaskListener listener) {
 		if(JenkinsPost.getSendGlobalPosts()) {
 			String postString = JenkinsPost.getGlobalPostString();
			postString = postString.replace("%DATETIME%", r.getTime().toString());
 			postString = postString.replace("%BUILD_ID%", r.getId());
 			postString = postString.replace("%JOB_NAME%", r.getParent().getDisplayName());
 			postString = postString.replace("%BUILD_MESSAGE%", 
 								r.getBuildStatusSummary().message);
 			JenkinsPost.sendPost(postString);
 			JenkinsPost.LOG.info(postString);
 			JenkinsPost.LOG.info("Post request was sent");
 		}
 	}
 
 }
