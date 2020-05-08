 package org.jenkinsci.plugins.vs_code_metrics;
 
 import java.io.IOException;
 
 import org.jenkinsci.plugins.vs_code_metrics.util.Constants;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.Action;
import hudson.model.Result;
 
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
 
    public VsCodeMetricsBuildAction getLastResult() {
        for( AbstractBuild<?,?> b = project.getLastBuild(); b!=null; b=b.getPreviousBuild()) {
            if(b.getResult()== Result.FAILURE)
                continue;
            VsCodeMetricsBuildAction r = b.getAction(VsCodeMetricsBuildAction.class);
            if(r!=null)
                return r;
         }
        return null;
     }
 
    public void doIndex(final StaplerRequest request, final StaplerResponse response) {
        // TODO:最後のビルドを表示
     }
 
     public void doGraph(StaplerRequest req, StaplerResponse rsp) throws IOException {
        if (getLastResult() != null)
           getLastResult().doGraph(req, rsp);
     }
 }
