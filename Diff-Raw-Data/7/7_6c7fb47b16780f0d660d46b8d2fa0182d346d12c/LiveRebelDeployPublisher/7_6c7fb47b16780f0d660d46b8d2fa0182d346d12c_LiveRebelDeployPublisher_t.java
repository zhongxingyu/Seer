 package org.zeroturnaround.jenkins;
 
 /*****************************************************************
 Copyright 2011 ZeroTurnaround OÜ
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
  *****************************************************************/
 import hudson.Extension;
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.BuildListener;
 import hudson.model.Result;
 import hudson.tasks.ArtifactArchiver;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.BuildStepMonitor;
 import hudson.tasks.Notifier;
 import hudson.tasks.Publisher;
 import hudson.util.FormValidation;
 import java.io.IOException;
 import java.io.Serializable;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import net.sf.json.JSONObject;
 
 import javax.servlet.ServletException;
 
 import org.kohsuke.stapler.AncestorInPath;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.StaplerRequest;
 
 import com.zeroturnaround.liverebel.api.CommandCenter;
 import com.zeroturnaround.liverebel.api.CommandCenterFactory;
 import com.zeroturnaround.liverebel.api.ConnectException;
 import com.zeroturnaround.liverebel.api.Forbidden;
 import com.zeroturnaround.liverebel.api.ServerInfo;
 
 /**
  * @author Juri Timoshin
  */
 public class LiveRebelDeployPublisher extends Notifier implements Serializable {
 
   public final String artifacts;
   public final boolean useOfflineUpdateIfCompatibleWithWarnings;
   private final List<ServerCheckbox> servers;
 
   // Fields in config.jelly must match the parameter names in the
   // "DataBoundConstructor"
   @DataBoundConstructor
   public LiveRebelDeployPublisher(String artifacts, List<ServerCheckbox> servers, boolean useOfflineUpdateIfCompatibleWithWarnings) {
     this.artifacts = artifacts;
     this.useOfflineUpdateIfCompatibleWithWarnings = useOfflineUpdateIfCompatibleWithWarnings;
     this.servers = servers;
   }
 
   @Override
   public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException,
       InterruptedException {
     if (!build.getResult().isBetterOrEqualTo(Result.UNSTABLE)) {
       return false;
     }
 
     FilePath[] deployableFiles;
     if (build.getWorkspace().isRemote()) {
       new ArtifactArchiver(artifacts, "", true).perform(build, launcher, listener);
       deployableFiles = new FilePath(build.getArtifactsDir()).list(artifacts);
     }
     else {
       deployableFiles = build.getWorkspace().list(artifacts);
     }
 
     CommandCenterFactory commandCenterFactory = new CommandCenterFactory().setUrl(getDescriptor().getLrUrl()).setVerbose(true).authenticate(getDescriptor().getAuthToken());
 
     if (!new LiveRebelProxy(commandCenterFactory, listener).perform(deployableFiles, getDeployableServers(),
         useOfflineUpdateIfCompatibleWithWarnings))
       build.setResult(Result.FAILURE);
     return true;
   }
 
   // Overridden for better type safety.
   @Override
   public DescriptorImpl getDescriptor() {
     return (DescriptorImpl) super.getDescriptor();
   }
 
   public BuildStepMonitor getRequiredMonitorService() {
     return BuildStepMonitor.BUILD;
   }
 
   public List<String> getDeployableServers() {
     List<String> list = new ArrayList<String>();
    if (servers != null) {
      for (ServerCheckbox server : servers)
        list.add(server.getServer());
     }

     return list;
   }
 
   public List<ServerCheckbox> getServers() {
     CommandCenter commandCenter = getDescriptor().newCommandCenter();
     if (commandCenter != null) {
       Map<String, ServerInfo> lrServers = commandCenter.getServers();
       Map<String, ServerCheckbox> map = new HashMap<String, ServerCheckbox>();
 
       for (ServerCheckbox checkbox : servers) {
         map.put(checkbox.getServer(), checkbox);
       }
 
       servers.clear();
       for (ServerInfo server : lrServers.values()) {
         boolean checked = map.containsKey(server.getId()) ? map.get(server.getId()).isSelected() : false;
         servers.add(new ServerCheckbox(server.getId(), server.getName(), checked, server.isConnected()));
       }
     }
     return servers;
   }
 
   @Extension
   public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
 
     private static final Logger LOGGER = Logger.getLogger(DescriptorImpl.class.getName());
 
     public DescriptorImpl() {
       load();
     }
     private String authToken;
     private String lrUrl;
 
     public String getAuthToken() {
       return authToken;
     }
 
     public String getLrUrl() {
       return lrUrl;
     }
 
     public CommandCenter newCommandCenter() {
       if (getLrUrl() == null || getAuthToken() == null) {
         LOGGER.warning("Please, navigate to Jenkins Configuration to specify running LiveRebel Url and Authentication Token.");
         return null;
       }
 
       try {
         CommandCenter commandCenter = new CommandCenterFactory().setUrl(getLrUrl()).setVerbose(true).authenticate(getAuthToken()).newCommandCenter();
         return commandCenter;
       }
       catch (Forbidden e) {
         LOGGER.warning("ERROR! Access denied. Please, navigate to Jenkins Configuration to specify LiveRebel Authentication Token.");
       }
       catch (ConnectException e) {
         LOGGER.warning("ERROR! Unable to connect to server.");
         LOGGER.log(Level.WARNING, "URL: {0}", e.getURL());
         if (e.getURL().equals("https://")) {
           LOGGER.warning("Please, navigate to Jenkins Configuration to specify running LiveRebel Url.");
         }
         else {
           LOGGER.log(Level.WARNING, "Reason: {0}", e.getMessage());
         }
       }
       return null;
     }
 
     public List<ServerCheckbox> getDefaultServers() {
       List<ServerCheckbox> servers = new ArrayList<ServerCheckbox>();
       CommandCenter commandCenter = newCommandCenter();
       if (commandCenter != null) {
         for (ServerInfo server : commandCenter.getServers().values()) {
           servers.add(new ServerCheckbox(server.getId(), server.getName(), false, server.isConnected()));
         }
       }
 
       return servers;
     }
 
     public FormValidation doCheckLrUrl(@QueryParameter("lrUrl") final String value) throws IOException,
         ServletException {
       if (value != null && value.length() > 0) {
         try {
           new URL(value);
         }
         catch (Exception e) {
           return FormValidation.error("Should be a valid URL.");
         }
       }
       return FormValidation.ok();
     }
 
     public FormValidation doCheckAuthToken(@QueryParameter("authToken") final String value) throws IOException,
         ServletException {
       if (value == null || value.length() != 36) {
         return FormValidation.error("Should be a valid authentication token.");
       }
       return FormValidation.ok();
     }
 
     public FormValidation doTestConnection(@QueryParameter("authToken") final String authToken,
         @QueryParameter("lrUrl") final String lrUrl) throws IOException, ServletException {
       try {
         new CommandCenterFactory().setUrl(lrUrl).setVerbose(false).authenticate(authToken).newCommandCenter();
         return FormValidation.ok("Success");
       }
       catch (Forbidden e) {
         return FormValidation.error("Please, provide right authentication token!");
       }
       catch (ConnectException e) {
         return FormValidation.error("Could not connect to LiveRebel at (%s)", e.getURL());
       }
       catch (Exception e) {
         return FormValidation.error(e.getMessage());
       }
     }
 
     public boolean isApplicable(Class<? extends AbstractProject> aClass) {
       // Indicates that this builder can be used with all kinds of project types
       return true;
     }
 
     /**
      * This human readable name is used in the configuration screen.
      */
     public String getDisplayName() {
       return "Deploy artifacts with LiveRebel";
     }
 
     @Override
     public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
       // To persist global configuration information,
       // set that to properties and call save().
       authToken = formData.getString("authToken");
       lrUrl = "https://" + formData.getString("lrUrl").replaceFirst("http://", "").replaceFirst("https://", "");
       save();
       return super.configure(req, formData);
     }
 
     public FormValidation doCheckArtifacts(@AncestorInPath AbstractProject project, @QueryParameter String value)
         throws IOException, ServletException {
       if (value == null || value.length() == 0) {
         return FormValidation.error("Please, provide at least one artifact.");
       }
       else {
         return FilePath.validateFileMask(project.getSomeWorkspace(), value);
       }
     }
   }
   private static final long serialVersionUID = 1L;
 }
