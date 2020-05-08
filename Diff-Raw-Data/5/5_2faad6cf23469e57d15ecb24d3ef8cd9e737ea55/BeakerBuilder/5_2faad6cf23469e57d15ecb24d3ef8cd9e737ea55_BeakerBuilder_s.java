 package org.jenkinsci.plugins.beakerbuilder;
 
 import hudson.Extension;
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.model.BuildListener;
 import hudson.model.Result;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.Builder;
 import hudson.util.FormValidation;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.Timer;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.sf.json.JSONObject;
 
 import org.apache.xmlrpc.XmlRpcException;
 import org.fedorahosted.beaker4j.beaker.BeakerServer;
 import org.fedorahosted.beaker4j.client.BeakerClient;
 import org.fedorahosted.beaker4j.remote_model.BeakerJob;
 import org.fedorahosted.beaker4j.remote_model.BeakerTask;
 import org.fedorahosted.beaker4j.remote_model.Identity;
 import org.fedorahosted.beaker4j.remote_model.TaskResult;
 import org.fedorahosted.beaker4j.remote_model.TaskStatus;
 import org.jenkinsci.plugins.beakerbuilder.utils.ConsoleLogger;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.StaplerRequest;
 
 /**
  * Builder which provides possibility to schedule external Beaker job, waits for its completion and sets up job result
  * according to Beaker job status.
  * 
  * @author vjuranek
  * 
  */
 public class BeakerBuilder extends Builder {
 
     /**
      * Beaker job XML
      */
     private final JobSource jobSource;
     private final boolean downloadFiles;
 
     @DataBoundConstructor
     public BeakerBuilder(JobSource jobSource, boolean downloadFiles) {
         this.jobSource = jobSource;
         this.downloadFiles = downloadFiles;
         System.out.println("download is " + downloadFiles);
     }
 
     public JobSource getJobSource() {
         return jobSource;
     }
     
     public boolean getDownloadFiles() {
         return downloadFiles;
     }
 
     /**
      * Prepares job XML, schedule the job in Beaker, waits for the result and
      */
     @Override
     public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
             throws InterruptedException {
         ConsoleLogger console = new ConsoleLogger(listener);
 
         // prepare job XML file
         File jobFile = prepareJob(build, console);
         if (!verifyFile(jobFile, build, console))
             return false;
         // TODO cleanup before leave - delete temp job XML file
         
         log(console, "[Beaker] INFO: Job XML file prepared");
         String jobXml = readJobFile(jobFile, build);
         if (jobXml == null) {
             log(console, "[Beaker] ERROR: Cannot read job source file " + jobFile.getPath());
             return false;
         }
 
         // schedule job
         LOGGER.fine("Scheduling Beaker job from file " + jobFile.getPath());
         BeakerJob job = scheduleJob(jobXml, build, console); 
         if (job == null) {
             log(console, "[Beaker] ERROR: Something went wrong when submitting job to Beaker, got NULL from Beaker, see console and Jenkins log for details");
             return false;
         }
         //TODO cancel job in Beaker if running before leaving
         
         // job exists in Beaker, we can create an action pointing to it
         int jobNum = getJobNumber(job);
         BeakerBuildAction bba = new BeakerBuildAction(jobNum, getDescriptor().getBeakerURL());
         build.addAction(bba);
         log(console, "[Beaker] INFO: Job successfuly submitted to Beaker, job ID is " + job.getJobId());
         
         // wait for job completion
         if (!waitForJobCompletion(job, console))
             return false;
 
         // set build result according to Beaker result
         setBuildResult(job, build, console);
         
         //try to download job files into workspace
         if(downloadFiles)
             downloadJobFiles(job, build, console);
 
         return true;
     }
 
     /**
      * Prepares job XML file in workspace
      * 
      * @param build
      * @param listener
      * @return True if job is prepared.
      * @throws InterruptedException
      */
     private File prepareJob(AbstractBuild<?, ?> build, ConsoleLogger console) throws InterruptedException {
         File jobFile = null;
         // create temporary file with Beaker job
         try {
             jobFile = jobSource.createJobFile(build, console.getListener());
         } catch (IOException ioe) {
             log(console, "[Beaker] ERROR: Could not get canonical path to workspace:" + ioe);
             ioe.printStackTrace();
             build.setResult(Result.FAILURE);
         }
         return jobFile;
     }
     
     private boolean verifyFile(File jobFile, AbstractBuild<?, ?> build, ConsoleLogger console) {
         if(jobFile == null)
             return false;
         
         FilePath fp = new FilePath(build.getWorkspace(), jobFile.getPath());
         try {
             if (!fp.exists()) {
                 log(console, "[Beaker] ERROR: Job file " + fp.getName() + " doesn't exists on channel" + fp.getChannel() + "!");
                 //build.setResult(Result.FAILURE);
                 return false;
             }
         } catch (IOException e) {
             log(console, "[Beaker] ERROR: failed to verify that " + fp.getName() + " exists on channel" + fp.getChannel()
                     + "! IOException cought, check Jenkins log for more details");
             LOGGER.log(Level.INFO,
                     "Beaker error: failed to verify that " + fp.getName() + " exists on channel" + fp.getChannel()
                             + "!", e);
             //build.setResult(Result.FAILURE);
             return false;
         } catch(InterruptedException e) {
             //TODO log exception
         }
         
         //TODO verify it's valid XML file
         
         return true;
     }
     
     private String readJobFile(File jobFile, AbstractBuild<?, ?> build) {
         String jobXml = null;
         try {
             FilePath fp = new FilePath(build.getWorkspace(), jobFile.getPath());
             jobXml = fp.readToString();
             System.out.println("job XML: " + jobXml);
         } catch (IOException e) {
             LOGGER.log(Level.INFO, "Beaker error: failed to read Beaker job XML file "
                     + jobFile.getPath(), e);
         }
         return jobXml;
     }
 
     /**
      * Schedules job on Beaker server. If scheduling is successful, add {@link BeakerBuildAction} to the job.
      * 
      * @param build
      * @return True if job scheduling is successful.
      */
     private BeakerJob scheduleJob(String jobXml, AbstractBuild<?, ?> build, ConsoleLogger console) {
         LOGGER.fine("Job XML is: \n" + jobXml);
         BeakerJob job = null;
         try {
             //client credentials can expire after some time, renew them before scheduling of each build
             getDescriptor().getIdentity().authenticate();
             job = getDescriptor().getBeakerClient().scheduleJob(jobXml);
         } catch(XmlRpcException e) {
             log(console, "[Beaker] ERROR: Job scheduling has failed, reason: " + e.getMessage());
         }
         return job;
     }
     
     private int getJobNumber(BeakerJob job) {
         Integer jobNum = new Integer(0);
         try {
             jobNum = new Integer(job.getJobId().substring(2, job.getJobId().length()));
         } catch (NumberFormatException e) {
             LOGGER.log(Level.INFO, "Beaker error: cannot convert job ID " + job.getJobId() + " to int");
         }
         return jobNum.intValue();
     }
 
     /**
      * Starts {@link TaskWatchdog} and waits until job finishes. If job status has changes, sends notification to the
      * console log.
      * 
      * @return True if waiting for job finishes normally.
      */
     private boolean waitForJobCompletion(BeakerJob job, ConsoleLogger console) {
         BeakerTask jobTask = new BeakerTask(job.getJobId(), job.getBeakerClient());
         TaskWatchdog watchdog = new TaskWatchdog(jobTask, TaskStatus.NEW);
         Timer timer = new Timer();
         timer.scheduleAtFixedRate(watchdog, TaskWatchdog.DEFAULT_DELAY, TaskWatchdog.DEFAULT_PERIOD);
         synchronized (watchdog) {
             while (!watchdog.isFinished()) {
                 try {
                     watchdog.wait(); // TODO timeout
                 } catch (InterruptedException e) {
                     timer.cancel();
                     log(console, "[Beaker] INFO: Job aborted");
                     return false;
                 }
                 log(console, "[Beaker] INFO: Job has changed state from " + watchdog.getOldStatus() + " state to state "
                         + watchdog.getStatus());
             }
         }
         timer.cancel();
 
         log(console, "[Beaker] INFO: Job finished");
         return true;
     }
 
     /**
      * Sets the build result according to Beaker job result.
      * 
      * @param build
      */
     private void setBuildResult(BeakerJob job, AbstractBuild<?, ?> build, ConsoleLogger console) {
         BeakerTask jobTask = new BeakerTask(job.getJobId(), job.getBeakerClient());
         TaskResult result = null;
         try {
             result = jobTask.getInfo().getResult();
         } catch (XmlRpcException e) {
             LOGGER.log(Level.INFO, "Beaker error: cannot get result from Beaker ", e);
             log(console, "[Beaker] ERROR: Cannot get job result from Beaker, check Jenkins logs for more details");
         }
 
         if (result == null) {
             log(console, "[Beaker] ERROR: Cannot get job result from Beaker, got NULL");
             build.setResult(Result.FAILURE);
         }
 
         // Beaker <---> Jenkins result mapping
         // TODO check, if this is correct
         // TODO add ABORTED result
         // TODO add CANCELED result
         switch (result) {
         case FAIL:
             build.setResult(Result.FAILURE);
             break;
         case PANIC:
             build.setResult(Result.FAILURE);
             break;
         case WARN:
             build.setResult(Result.UNSTABLE);
             break;
         case PASS:
             build.setResult(Result.SUCCESS);
             break;
         default:
             build.setResult(Result.UNSTABLE);
             log(console, "[Beaker] INFO: Unknow job result, setting build result to UNSTABLE");
             break;
         }
 
     }
 
     private void downloadJobFiles(BeakerJob job, AbstractBuild<?, ?> build, ConsoleLogger console) {
         FilePath beakerFileDir = new FilePath(build.getWorkspace(),"beaker/" + job.getJobId().replaceAll(":", "_"));
         try {
             beakerFileDir.mkdirs();
         } catch(IOException e) {
             LOGGER.log(Level.INFO, "Beaker error: cannot create dir for Beaker files", e);
             log(console, "[Beaker] ERROR: Cannot create dir for Beaker files: " + e.getMessage());
         } catch(InterruptedException e) {
             LOGGER.log(Level.INFO, "Beaker error: creating dir for Beaker files interrupted", e);
             log(console, "[Beaker] ERROR: Creating dir for Beaker files interrupted: " + e.getMessage());
         }
         
         log(console, "[Beaker] INFO: Trying to download job file into " + beakerFileDir.getRemote());
         try {
             ArrayList<Map<String, String>> files = job.getFiles();
             for(Map<String, String> f : files) {
                FilePath fp = new FilePath(beakerFileDir, f.get("filename"));
                log(console, "[Beaker] INFO: Downloading " + f.get("filename") + " into " + fp.getRemote());
                try {
                    fp.copyFrom(new URL(f.get("url")));
                } catch(IOException e) {
                   log(console, "[Beaker] ERROR: Something went wronf when downloading " + f.get("filename") + ", check Jenkins log for details.");
                    LOGGER.log(Level.INFO, "Beaker error: cannot donwload file " + f.get("filename"), e);
                } catch(InterruptedException e) {
                   log(console, "[Beaker] ERROR: Something went wronf when downloading " + f.get("filename") + ", check Jenkins log for details.");
                    LOGGER.log(Level.INFO, "Beaker error: cannot donwload file " + f.get("filename"), e);
                }   
             }
         } catch(XmlRpcException e) {
             LOGGER.log(Level.INFO, "Beaker error: cannot download files from Beaker ", e);
             log(console, "[Beaker] ERROR: Cannot download job files from Beaker: " + e.getMessage()); 
         }
         
     }
     
     /**
      * Logs messages into Jenkins console.
      * 
      * @param message
      *            Message to be logges
      */
     private void log(ConsoleLogger console, String message) {
         console.logAnnot(message);
     }
 
     @Override
     public DescriptorImpl getDescriptor() {
         return (DescriptorImpl) super.getDescriptor();
     }
 
     @Extension
     public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
 
         /**
          * Beaker server URL
          */
         private String beakerURL;
 
         // TODO provide a way to store the credential in Jenkisn authentication center.
         /**
          * Beaker login
          */
         private String login;
 
         /**
          * Beaker password
          */
         private String password;
 
         private transient BeakerClient beakerClient;
         private transient Identity identity;
 
         /**
          * Constructor creates Beaker client for communication with Beaker server and does the authentication so that we
          * have valid (authenticated) session for futher requests.
          */
         public DescriptorImpl() {
             load();
             setupClient(); 
         }
 
         @Override
         public boolean isApplicable(@SuppressWarnings("rawtypes")Class<? extends AbstractProject> aClass) {
             return true;
         }
 
         public String getDisplayName() {
             return "Execute Beaker task";
         }
 
         @Override
         public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
             req.bindJSON(this, formData);
             if (beakerURL.endsWith("/"))
                 beakerURL = beakerURL.substring(0, beakerURL.length() - 1);
             setupClient();
             save();
             return super.configure(req, formData);
         }
 
         private void setupClient() {
             if (beakerURL != null && !"".equals(beakerURL.trim())) {
                 beakerClient = BeakerServer.getXmlRpcClient(beakerURL);
                 //beakerClient.authenticate(login, password);
                 identity = new Identity(login, password, beakerClient);
                 identity.authenticate();
             }
         }
         
         /**
          * Tries to connect to Beaker server and verify that provided credential works.
          * 
          * @param beakerURL
          * @param login
          * @param password
          * @return
          */
         public FormValidation doTestConnection(@QueryParameter("beakerURL") final String beakerURL,
                 @QueryParameter("login") final String login, @QueryParameter("password") final String password) {
             System.out.println("Trying to get client for " + beakerURL);
             BeakerClient bc = BeakerServer.getXmlRpcClient(beakerURL);
             Identity ident = new Identity(login, password, bc);
             try {
                 if (!ident.authenticate())
                     // TODO localization
                     return FormValidation.error("Cannot connect to " + beakerURL + " as " + login);
                 return FormValidation.ok("Connected as " + ident.whoAmI());
             } catch (Exception e) {
                 e.printStackTrace();
                 return FormValidation.error("Somethign went wrong, cannot connect to " + beakerURL + ", cause: "
                         + e.getCause());
             }
         }
         
         public String getBeakerURL() {
             return beakerURL;
         }
 
         public void setBeakerURL(String beakerURL) {
             this.beakerURL = beakerURL;
         }
 
         public String getLogin() {
             return login;
         }
 
         public void setLogin(String login) {
             this.login = login;
         }
 
         public String getPassword() {
             return password;
         }
 
         public void setPassword(String password) {
             this.password = password;
         }
 
         public BeakerClient getBeakerClient() {
             return beakerClient;
         }
         
         public Identity getIdentity() {
             return identity;
         }
 
     }
 
     private static final Logger LOGGER = Logger.getLogger(BeakerBuilder.class.getName());
 
 }
