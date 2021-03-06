 package org.jenkinsci.plugins.clamav;
 
 import hudson.Extension;
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.Util;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.BuildListener;
 import hudson.model.Descriptor.FormException;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.BuildStepMonitor;
 import hudson.tasks.Publisher;
 import hudson.tasks.Recorder;
 import hudson.util.FormValidation;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.logging.Logger;
 import net.sf.json.JSONObject;
 import org.jenkinsci.plugins.clamav.scanner.ClamAvScanner;
 import org.jenkinsci.plugins.clamav.scanner.ScanResult;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.StaplerRequest;
 import static org.jenkinsci.plugins.clamav.scanner.ScanResult.Status.*;
 
 /**
  * ClamAvRecorder
  * 
  * @author Seiji Sogabe
  */
 public class ClamAvRecorder extends Recorder {
 
     private final String artifacts;
 
     public String getArtifacts() {
         return artifacts;
     }
 
     @DataBoundConstructor
     public ClamAvRecorder(String artifacts) {
         this.artifacts = artifacts;
     }
 
     @Override
     public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
             throws InterruptedException, IOException {
 
         PrintStream logger = listener.getLogger();
 
         FilePath ws = build.getWorkspace();
         if (ws == null) {
             return true;
         }
 
         DescriptorImpl d = (DescriptorImpl) getDescriptor();
        ClamAvScanner scanner = new ClamAvScanner(d.getHost(), d.getPort(), 1000);
 
         long start = System.currentTimeMillis();
         FilePath[] targets = ws.list(artifacts, null);
         for (FilePath target : targets) {
             ScanResult r = scanner.scan(target.read());
             StringBuilder msg = new StringBuilder("[ClamAv] Scanned " + target.getRemote() + " ");
             switch (r.getStatus()) {
                 case ERROR:
                     msg.append("ERROR : ").append(r.getMessage());
                     break;
                 case FAILED:
                     msg.append("FAILED : ").append(r.getMessage());
                 default:
                     msg.append("PASSED");
             }
             logger.println(msg.toString());
         }
         logger.println("[ClamAv] " + (System.currentTimeMillis() - start) + "ms took.");    
         
         return true;
     }
 
     public BuildStepMonitor getRequiredMonitorService() {
         return BuildStepMonitor.NONE;
     }
     
     @Extension
     public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
 
         private String host;
 
         private int port = 3310;
 
         public String getHost() {
             return host;
         }
 
         public int getPort() {
             return port;
         }
 
         public DescriptorImpl() {
             load();
         }
         
         @Override
         public String getDisplayName() {
             return "Check for viruses";
         }
 
         @Override
         public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
             return req.bindJSON(ClamAvRecorder.class, formData);
         }
 
         @Override
         public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
             host = Util.fixEmptyAndTrim(json.getString("host"));
             port = json.optInt("port", 3310);
             save();
             return super.configure(req, json);
         }
 
         /**
          * Check ClamAV host.
          * 
          * exposed to global.jelly.
          * 
          * @param host host name or IP address of ClamAV Host.
          * @param port port of ClamAv host. 
          * @return {@link FormValidation} 
          */
         public FormValidation doCheckHost(@QueryParameter String host, @QueryParameter int port) {
             host = Util.fixEmptyAndTrim(host);
             if (host == null) {
                 return FormValidation.ok();
             }
             if (port < 0 || port > 65535) {
                 return FormValidation.error("Port should be in the range from 0 to 65535");
             }
             ClamAvScanner scanner = new ClamAvScanner(host, port);
             if (!scanner.ping()) {
                 return FormValidation.error("No response from " + host + ":" + port);
             }
             return FormValidation.ok();
         }
 
         /**
          * Check artifacts and host.
          * 
          * exposed to config.jelly.
          * 
          * @param artifacts
          * @return {@link FormValidation} 
          */
         public FormValidation doCheck(StaplerRequest req, @QueryParameter String artifacts) {
             if (host == null) {
                 return FormValidation.errorWithMarkup(Messages.ClamAvRecorder_NotHostConfigured(req.getContextPath()));
             }
             return FormValidation.validateRequired(artifacts);
         }
 
         @Override
         public boolean isApplicable(Class<? extends AbstractProject> type) {
             return true;
         }
     }
 
     private static final Logger LOGGER = Logger.getLogger(ClamAvRecorder.class.getName());
 }
