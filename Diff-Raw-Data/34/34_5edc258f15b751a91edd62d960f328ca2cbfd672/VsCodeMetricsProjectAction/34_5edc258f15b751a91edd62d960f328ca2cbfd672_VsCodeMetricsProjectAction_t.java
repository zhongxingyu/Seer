 package org.jenkinsci.plugins.vs_code_metrics;
 
 import java.io.IOException;
 
 import org.jenkinsci.plugins.vs_code_metrics.util.Constants;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.Action;
 
 /**
  * @author Yasuyuki Saito
  */
 public class VsCodeMetricsProjectAction implements Action  {
 
     public final AbstractProject<?,?> project;
 
     public VsCodeMetricsProjectAction(final AbstractProject<?, ?> project) {
         this.project = project;
     }
 
     public String getIconFileName() {
         return Constants.ACTION_ICON;
     }
 
     public String getDisplayName() {
         return Messages.VsCodeMetricsProjectAction_DisplayName();
     }
 
     public String getUrlName() {
         return Constants.ACTION_URL;
     }
 
    public AbstractBuild<?, ?> getLastFinishedBuild() {
        AbstractBuild<?, ?> lastBuild = project.getLastBuild();
        while (lastBuild != null && (lastBuild.isBuilding() || lastBuild.getAction(VsCodeMetricsBuildAction.class) == null)) {
            lastBuild = lastBuild.getPreviousBuild();
         }
        return lastBuild;
     }
 
    public VsCodeMetricsBuildAction getLastFinishedBuildAction() {
        AbstractBuild<?, ?> lastBuild = getLastFinishedBuild();
        return (lastBuild != null) ? (lastBuild.getAction(VsCodeMetricsBuildAction.class)) : (null);
    }

    public void doIndex(final StaplerRequest request, final StaplerResponse response) throws IOException {
        AbstractBuild<?, ?> build = getLastFinishedBuild();
        if (build != null) {
            response.sendRedirect2(String.format("../%d/%s", build.getNumber(), Constants.ACTION_URL));
        }
     }
 
     public void doGraph(StaplerRequest req, StaplerResponse rsp) throws IOException {
        if (getLastFinishedBuildAction() != null)
            getLastFinishedBuildAction().doGraph(req, rsp);
     }
 }
