 package hudson.plugins.trackingsvn;
 
 import hudson.Extension;
 import hudson.model.AbstractBuild;
 import hudson.model.Hudson;
 import hudson.model.Job;
 import hudson.model.Run;
 import hudson.model.TaskListener;
 import hudson.model.listeners.RunListener;
 import hudson.plugins.trackingsvn.TrackingSVNProperty.ToTrack;
 import hudson.scm.RevisionParameterAction;
 import hudson.scm.SubversionTagAction;
 import hudson.scm.SubversionSCM.SvnInfo;
 
 import java.util.ArrayList;
 
 @Extension
 public class TrackingSVNRunListener extends RunListener<AbstractBuild> {
 
 	public TrackingSVNRunListener() {
 		super(AbstractBuild.class);
 	}
 
 	@Override
 	public void onStarted(AbstractBuild r, TaskListener listener) {
 		TrackingSVNProperty property = ((AbstractBuild<?, ?>) r).getProject()
 				.getProperty(TrackingSVNProperty.class);
 		if (property == null) {
 			return;
 		}
 
		listener.getLogger().println("Tracking SVN of " + r.getDisplayName());
 
 		String sourceProject = property.getSourceProject();
 		ToTrack toTrack = property.getToTrack();
 
 		Job<?, ?> job = (Job<?, ?>) Hudson.getInstance().getItem(sourceProject);
 		if (job == null)
 			throw new TrackingSVNException(
 					"Unknown source project for tracking-svn : "
 							+ sourceProject);
 		Run<?, ?> run = toTrack.getBuild(job);
 		if (run == null)
 			throw new TrackingSVNException(toTrack + " not found for project "
 					+ sourceProject);
 
 		SubversionTagAction tagAction = run
 				.getAction(SubversionTagAction.class);
 		if (tagAction == null) {
 			throw new TrackingSVNException("Project " + sourceProject
 					+ " is not an SVN project");
 		}
 
 		ArrayList<SvnInfo> revisions = new ArrayList<SvnInfo>(tagAction
 				.getTags().keySet());
 		RevisionParameterAction action = new RevisionParameterAction(revisions);
 		r.addAction(action);
 
		r.addAction(new TrackingSVNAction(r));
 
 	}
 
 }
