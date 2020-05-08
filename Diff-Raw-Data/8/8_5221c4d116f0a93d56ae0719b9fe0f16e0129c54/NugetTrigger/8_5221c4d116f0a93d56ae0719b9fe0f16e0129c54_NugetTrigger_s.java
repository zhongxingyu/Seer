 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.jenkinsci.nuget;
 import antlr.ANTLRException;
 import com.jenkinsci.nuget.Utils.NugetUpdater;
 import hudson.Extension;
 import hudson.model.AbstractProject;
 import hudson.model.Action;
 import hudson.model.Hudson;
 import hudson.model.Node;
 import java.io.File;
 import java.net.MalformedURLException;
 import net.sf.json.JSONObject;
 import org.jenkinsci.lib.xtrigger.AbstractTrigger;
 import org.jenkinsci.lib.xtrigger.XTriggerDescriptor;
 import org.jenkinsci.lib.xtrigger.XTriggerException;
 import org.jenkinsci.lib.xtrigger.XTriggerLog;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 /**
  *
  * @author bgregg
  */
 public class NugetTrigger extends AbstractTrigger {
     @DataBoundConstructor
     public NugetTrigger(String cronTabSpec) throws ANTLRException {
         super(cronTabSpec);
         
     }
     
     @Override
     protected File getLogFile() {
         return new File(job.getRootDir(), "nuget-polling.log");
     }
 
     @Override
     protected boolean requiresWorkspaceForPolling() {
         return true;
     }
 
     @Override
     protected String getName() {
         return "Nuget";
     }
 
     @Override
     protected Action[] getScheduledActions(Node node, XTriggerLog xtl) {
         return new Action[0];
     }
 
     @Override
     protected boolean checkIfModified(Node node, XTriggerLog xtl) throws XTriggerException {
         AbstractProject project = (AbstractProject) job;
         NugetUpdater updater = new NugetUpdater(project.getSomeWorkspace(), getDescriptor().nugetExe, xtl);
         return updater.performUpdate();
     }
 
     @Override
     protected String getCause() {
         return "Package updated.";
     }
     
     @Override
     public NugetTriggerDescriptor getDescriptor() {
        return (NugetTriggerDescriptor) Hudson.getInstance().getDescriptorOrDie(getClass());
     }
 
     @Override
     public Action getProjectAction() {
         return new NugetTriggerAction((AbstractProject)job, getLogFile());
     }
     
     @Extension
     public static final class NugetTriggerDescriptor extends XTriggerDescriptor {
         private String nugetExe;
 
         @Override
         public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
             nugetExe = json.getString("nugetExe");
             save();
             return super.configure(req, json); //To change body of generated methods, choose Tools | Templates.
         }
         
         @Override
         public String getDisplayName() {
             return "Build on Nuget updates";
         }
         
         public String getNugetExe() {
             return nugetExe;
         }
         
     }
 }
