 
 /* ===========================================================================
  *  Copyright (c) 2007 Serena Software. All rights reserved.
  *
  *  Use of the Sample Code provided by Serena is governed by the following
  *  terms and conditions. By using the Sample Code, you agree to be bound by
  *  the terms contained herein. If you do not agree to the terms herein, do
  *  not install, copy, or use the Sample Code.
  *
  *  1.  GRANT OF LICENSE.  Subject to the terms and conditions herein, you
  *  shall have the nonexclusive, nontransferable right to use the Sample Code
  *  for the sole purpose of developing applications for use solely with the
  *  Serena software product(s) that you have licensed separately from Serena.
  *  Such applications shall be for your internal use only.  You further agree
  *  that you will not: (a) sell, market, or distribute any copies of the
  *  Sample Code or any derivatives or components thereof; (b) use the Sample
  *  Code or any derivatives thereof for any commercial purpose; or (c) assign
  *  or transfer rights to the Sample Code or any derivatives thereof.
  *
  *  2.  DISCLAIMER OF WARRANTIES.  TO THE MAXIMUM EXTENT PERMITTED BY
  *  APPLICABLE LAW, SERENA PROVIDES THE SAMPLE CODE AS IS AND WITH ALL
  *  FAULTS, AND HEREBY DISCLAIMS ALL WARRANTIES AND CONDITIONS, EITHER
  *  EXPRESSED, IMPLIED OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY
  *  IMPLIED WARRANTIES OR CONDITIONS OF MERCHANTABILITY, OF FITNESS FOR A
  *  PARTICULAR PURPOSE, OF LACK OF VIRUSES, OF RESULTS, AND OF LACK OF
  *  NEGLIGENCE OR LACK OF WORKMANLIKE EFFORT, CONDITION OF TITLE, QUIET
  *  ENJOYMENT, OR NON-INFRINGEMENT.  THE ENTIRE RISK AS TO THE QUALITY OF
  *  OR ARISING OUT OF USE OR PERFORMANCE OF THE SAMPLE CODE, IF ANY,
  *  REMAINS WITH YOU.
  *
  *  3.  EXCLUSION OF DAMAGES.  TO THE MAXIMUM EXTENT PERMITTED BY APPLICABLE
  *  LAW, YOU AGREE THAT IN CONSIDERATION FOR RECEIVING THE SAMPLE CODE AT NO
  *  CHARGE TO YOU, SERENA SHALL NOT BE LIABLE FOR ANY DAMAGES WHATSOEVER,
  *  INCLUDING BUT NOT LIMITED TO DIRECT, SPECIAL, INCIDENTAL, INDIRECT, OR
  *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, DAMAGES FOR LOSS OF
  *  PROFITS OR CONFIDENTIAL OR OTHER INFORMATION, FOR BUSINESS INTERRUPTION,
  *  FOR PERSONAL INJURY, FOR LOSS OF PRIVACY, FOR NEGLIGENCE, AND FOR ANY
  *  OTHER LOSS WHATSOEVER) ARISING OUT OF OR IN ANY WAY RELATED TO THE USE
  *  OF OR INABILITY TO USE THE SAMPLE CODE, EVEN IN THE EVENT OF THE FAULT,
  *  TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY, OR BREACH OF CONTRACT,
  *  EVEN IF SERENA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.  THE
  *  FOREGOING LIMITATIONS, EXCLUSIONS AND DISCLAIMERS SHALL APPLY TO THE
  *  MAXIMUM EXTENT PERMITTED BY APPLICABLE LAW.  NOTWITHSTANDING THE ABOVE,
  *  IN NO EVENT SHALL SERENA'S LIABILITY UNDER THIS AGREEMENT OR WITH RESPECT
  *  TO YOUR USE OF THE SAMPLE CODE AND DERIVATIVES THEREOF EXCEED US$10.00.
  *
  *  4.  INDEMNIFICATION. You hereby agree to defend, indemnify and hold
  *  harmless Serena from and against any and all liability, loss or claim
  *  arising from this agreement or from (i) your license of, use of or
  *  reliance upon the Sample Code or any related documentation or materials,
  *  or (ii) your development, use or reliance upon any application or
  *  derivative work created from the Sample Code.
  *
  *  5.  TERMINATION OF THE LICENSE.  This agreement and the underlying
  *  license granted hereby shall terminate if and when your license to the
  *  applicable Serena software product terminates or if you breach any terms
  *  and conditions of this agreement.
  *
  *  6.  CONFIDENTIALITY.  The Sample Code and all information relating to the
  *  Sample Code (collectively "Confidential Information") are the
  *  confidential information of Serena.  You agree to maintain the
  *  Confidential Information in strict confidence for Serena.  You agree not
  *  to disclose or duplicate, nor allow to be disclosed or duplicated, any
  *  Confidential Information, in whole or in part, except as permitted in
  *  this Agreement.  You shall take all reasonable steps necessary to ensure
  *  that the Confidential Information is not made available or disclosed by
  *  you or by your employees to any other person, firm, or corporation.  You
  *  agree that all authorized persons having access to the Confidential
  *  Information shall observe and perform under this nondisclosure covenant.
  *  You agree to immediately notify Serena of any unauthorized access to or
  *  possession of the Confidential Information.
  *
  *  7.  AFFILIATES.  Serena as used herein shall refer to Serena Software,
  *  Inc. and its affiliates.  An entity shall be considered to be an
  *  affiliate of Serena if it is an entity that controls, is controlled by,
  *  or is under common control with Serena.
  *
  *  8.  GENERAL.  Title and full ownership rights to the Sample Code,
  *  including any derivative works shall remain with Serena.  If a court of
  *  competent jurisdiction holds any provision of this agreement illegal or
  *  otherwise unenforceable, that provision shall be severed and the
  *  remainder of the agreement shall remain in full force and effect.
  * ===========================================================================
  */
 
 /*
  * This experimental plugin extends Hudson support for Dimensions SCM repositories
  *
  * @author Tim Payne
  *
  */
 
 // Package name
 package hudson.plugins.dimensionsscm;
 
 // Dimensions imports
 import hudson.plugins.dimensionsscm.DimensionsAPI;
 import hudson.plugins.dimensionsscm.DimensionsSCMRepositoryBrowser;
 import hudson.plugins.dimensionsscm.Logger;
 import hudson.plugins.dimensionsscm.DimensionsChangeLogParser;
 import hudson.plugins.dimensionsscm.DimensionsBuildWrapper;
 import hudson.plugins.dimensionsscm.DimensionsBuildNotifier;
 import hudson.plugins.dimensionsscm.DimensionsChecker;
 import hudson.plugins.dimensionsscm.CheckOutAPITask;
 import hudson.plugins.dimensionsscm.CheckOutCmdTask;
 import hudson.plugins.dimensionsscm.GetHostDetailsTask;
 
 
 // Hudson imports
 import hudson.Extension;
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.Util;
 import hudson.model.Hudson;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.BuildListener;
 import hudson.model.ModelObject;
 import hudson.model.Run;
 import hudson.model.TaskListener;
 import hudson.scm.ChangeLogParser;
 import hudson.scm.RepositoryBrowsers;
 import hudson.scm.SCM;
 import hudson.scm.SCMDescriptor;
 import hudson.util.FormFieldValidator;
 import hudson.util.Scrambler;
 import hudson.util.VariableResolver;
 import hudson.FilePath;
 import hudson.FilePath.FileCallable;
 import hudson.model.Node;
 import hudson.model.Computer;
 import hudson.model.Hudson.MasterComputer;
 import hudson.remoting.Callable;
 import hudson.remoting.DelegatingCallable;
 import hudson.remoting.Channel;
 import hudson.remoting.VirtualChannel;
 
 // General imports
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.Serializable;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.TimeZone;
 import java.util.Vector;
 import java.net.InetSocketAddress;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 
 import javax.servlet.ServletException;
 
 import net.sf.json.JSONObject;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 import org.apache.commons.lang.StringUtils;
 
 /*
  * Hudson requires the following functions to be implemented
  *
  *   public boolean checkout(AbstractBuild build, Launcher launcher, FilePath workspace, BuildListener listener, File changelogFile)
  *                           throws IOException, InterruptedException;
  *   public boolean pollChanges(AbstractProject project, Launcher launcher, FilePath workspace, TaskListener listener)
  *                           throws IOException, InterruptedException;
  *   public ChangeLogParser createChangeLogParser();
  *   public SCMDescriptor<?> getDescriptor();
  *
  * For this experimental plugin, only the main ones will be implemented
  *
  */
 
 /*
  * Main Dimensions SCM class which creates the plugin logic
  */
 public class DimensionsSCM extends SCM implements Serializable
 {
     // Hudson details
     private String project;
     private String directory;
     private String workarea;
 
     private String jobUserName;
     private String jobPasswd;
     private String jobServer;
     private String jobDatabase;
 
     private String[] folders = new String[0];
 
     private String jobTimeZone;
     private String jobWebUrl;
 
     private boolean canJobUpdate;
     private boolean canJobDelete;
     private boolean canJobForce;
     private boolean canJobRevert;
 
     DimensionsAPI dmSCM;
     DimensionsSCMRepositoryBrowser browser;
 
     public DimensionsSCM getSCM() {
         return this;
     }
 
     public DimensionsAPI getAPI() {
         return this.dmSCM;
     }
 
     /*
      * Gets the project ID for the connection.
      * @return the project ID
      */
     public String getProject() {
         return this.project;
     }
 
     /*
      * Gets the project path.
      * @return the project path
      */
     public String getDirectory() {
         return this.directory;
     }
 
 
     /*
      * Gets the project paths.
      * @return the project paths
      */
     public String[] getFolders() {
         return this.folders;
     }
 
     /*
      * Gets the workarea path.
      * @return the workarea path
      */
     public String getWorkarea() {
         return this.workarea;
     }
 
     /*
      * Gets the job user ID for the connection.
      * @return the job user ID
      */
     public String getJobUserName() {
         return this.jobUserName;
     }
 
     /*
      * Gets the job passwd for the connection.
      * @return the project ID
      */
     public String getJobPasswd() {
         return Scrambler.descramble(jobPasswd);
     }
 
     /*
      * Gets the server ID for the connection.
      * @return the server ID
      */
     public String getJobServer() {
         return this.jobServer;
     }
 
     /*
      * Gets the job database ID for the connection.
      * @return the job database ID
      */
     public String getJobDatabase() {
         return this.jobDatabase;
     }
 
     /*
      * Gets the job timezone for the connection.
      * @return the job timezone
      */
     public String getJobTimeZone() {
         return this.jobTimeZone;
     }
 
     /*
      * Gets the job weburl ID for the connection.
      * @return the job weburl
      */
     public String getJobWebUrl() {
         return this.jobWebUrl;
     }
 
     /*
      * Gets the update .
      * @return the update
      */
     public boolean isCanJobUpdate() {
         return this.canJobUpdate;
     }
 
     /*
      * Gets the delete .
      * @return the delete
      */
     public boolean isCanJobDelete() {
         return this.canJobDelete;
     }
 
     /*
      * Gets the force .
      * @return the force
      */
     public boolean isCanJobForce() {
         return this.canJobForce;
     }
 
     /*
      * Gets the revert .
      * @return the force
      */
     public boolean isCanJobRevert() {
         return this.canJobRevert;
     }
 
     @Extension
     public static final DescriptorImpl DM_DESCRIPTOR = new DescriptorImpl();
 
     /*
      *-----------------------------------------------------------------
      *  FUNCTION SPECIFICATION
      *  Name:
      *      requiresWorkspaceForPolling
      *  Description:
      *      Does this SCM plugin require a workspace for polling?
      * Parameters:
      *  Return:
      *      @return boolean
      *-----------------------------------------------------------------
      */
     @Override
     public boolean requiresWorkspaceForPolling() {
         return false;
     }
 
     /*
      *-----------------------------------------------------------------
      *  FUNCTION SPECIFICATION
      *  Name:
      *      supportsPolling
      *  Description:
      *      Does this SCM plugin support polling?
      * Parameters:
      *  Return:
      *      @return boolean
      *-----------------------------------------------------------------
      */
     @Override
     public boolean supportsPolling() {
         return true;
     }
 
 
     /*
      *-----------------------------------------------------------------
      *  FUNCTION SPECIFICATION
      *  Name:
      *      buildEnvVars
      *  Description:
      *      Build up environment variables for build support
      * Parameters:
      *  Return:
      *-----------------------------------------------------------------
      */
     @Override
     public void buildEnvVars(AbstractBuild build, Map<String, String> env)
     {
         // To be implemented when build support put in
         super.buildEnvVars(build, env);
         return;
     }
 
     /*
      *-----------------------------------------------------------------
      *  FUNCTION SPECIFICATION
      *  Name:
      *      Constructor
      *  Description:
      *      Default constructor for the plugin
      * Parameters:
      *      @param String project
      *      @param String workspaceName
      *      @param String workarea
      *      @param String jobServer
      *      @param String jobUserName
      *      @param String jobPasswd
      *      @param String jobDatabase
      *  Return:
      *      @return void
      *-----------------------------------------------------------------
      */
     public DimensionsSCM(String project,
                          String directory,
                          String workarea,
                          boolean canJobDelete,
                          boolean canJobForce,
                          boolean canJobRevert,
                          String jobUserName,
                          String jobPasswd,
                          String jobServer,
                          String jobDatabase,
                          boolean canJobUpdate,
                          String jobTimeZone,
                          String jobWebUrl)
     {
         this(project,null,workarea,canJobDelete,
              canJobForce,canJobRevert,
              jobUserName,jobPasswd,
              jobServer,jobDatabase,
              canJobUpdate,jobTimeZone,
              jobWebUrl,directory);
     }
 
     /*
      *-----------------------------------------------------------------
      *  FUNCTION SPECIFICATION
      *  Name:
      *      Constructor
      *  Description:
      *      Default constructor for the plugin
      * Parameters:
      *      @param String project
      *      @param String[] folderNames
      *      @param String workspaceName
      *      @param String workarea
      *      @param String jobServer
      *      @param String jobUserName
      *      @param String jobPasswd
      *      @param String jobDatabase
      *      @param String directory
      *  Return:
      *      @return void
      *-----------------------------------------------------------------
      */
     @DataBoundConstructor
     public DimensionsSCM(String project,
                          String[] folders,
                          String workarea,
                          boolean canJobDelete,
                          boolean canJobForce,
                          boolean canJobRevert,
                          String jobUserName,
                          String jobPasswd,
                          String jobServer,
                          String jobDatabase,
                          boolean canJobUpdate,
                          String jobTimeZone,
                          String jobWebUrl,
                          String directory)
     {
         // Check the folders specified have data specified
         if (folders != null) {
             Logger.Debug("Folders are populated");
             Vector<String> x = new Vector<String>();
             for(int t=0;t<folders.length;t++) {
                 if (StringUtils.isNotEmpty(folders[t]))
                     x.add(folders[t]);
             }
             this.folders = (String[])x.toArray(new String[1]);
         }
         else {
             if (directory != null)
                 this.folders[0] = directory;
         }
 
         // If nothing specified, then default to '/'
         if (this.folders.length < 2) {
             if (this.folders[0] == null || this.folders[0].length() < 1)
                 this.folders[0] = "/";
         }
 
         // Copying arguments to fields
         this.project = (Util.fixEmptyAndTrim(project) == null ? "${JOB_NAME}" : project);
         this.workarea = (Util.fixEmptyAndTrim(workarea) == null ? null : workarea);
         this.directory = (Util.fixEmptyAndTrim(directory) == null ? null : directory);
 
         this.jobServer = (Util.fixEmptyAndTrim(jobServer) == null ? getDescriptor().getServer() : jobServer);
         this.jobUserName = (Util.fixEmptyAndTrim(jobUserName) == null ? getDescriptor().getUserName() : jobUserName);
         this.jobDatabase = (Util.fixEmptyAndTrim(jobDatabase) == null ? getDescriptor().getDatabase() : jobDatabase);
         String passwd = (Util.fixEmptyAndTrim(jobPasswd) == null ? getDescriptor().getPasswd() : jobPasswd);
         this.jobPasswd = Scrambler.scramble(passwd);
 
         this.canJobUpdate = canJobUpdate;
         this.canJobDelete = canJobDelete;
         this.canJobForce = canJobForce;
         this.canJobRevert = canJobRevert;
 
         this.jobTimeZone = (Util.fixEmptyAndTrim(jobTimeZone) == null ? getDescriptor().getTimeZone() : jobTimeZone);
         this.jobWebUrl = (Util.fixEmptyAndTrim(jobWebUrl) == null ? getDescriptor().getWebUrl() : jobWebUrl);
 
         String dmS = this.jobServer + "-" + this.jobUserName + ":" + this.jobDatabase;
         Logger.Debug("Starting job for project '" + this.project + "' ('" + this.folders.length + "')" +
                      ", connecting to " + dmS);
     }
 
 
     /*
      *-----------------------------------------------------------------
      *  FUNCTION SPECIFICATION
      *  Name:
      *      checkout
      *  Description:
      *      Checkout method for the plugin
      * Parameters:
      *      @param AbstractBuild build
      *      @param Launcher launcher
      *      @param FilePath workspace
      *      @param BuildListener listener
      *      @param File changelogFile
      *  Return:
      *      @return boolean
      *-----------------------------------------------------------------
      */
     @Override
     public boolean checkout(final AbstractBuild build, final Launcher launcher,
                             final FilePath workspace, final BuildListener listener,
                             final File changelogFile)
                             throws IOException, InterruptedException
     {
         boolean bRet = false;
 
         if (!isCanJobUpdate()) {
             Logger.Debug("Skipping checkout - " + this.getClass().getName());
         }
 
         Logger.Debug("Invoking checkout - " + this.getClass().getName());
 
         try {
             // Load other Dimensions plugins if set
             DimensionsBuildWrapper.DescriptorImpl bwplugin = (DimensionsBuildWrapper.DescriptorImpl)
                                 Hudson.getInstance().getDescriptor(DimensionsBuildWrapper.class);
             DimensionsBuildNotifier.DescriptorImpl bnplugin = (DimensionsBuildNotifier.DescriptorImpl)
                                 Hudson.getInstance().getDescriptor(DimensionsBuildNotifier.class);
 
             if (DimensionsChecker.isValidPluginCombination(build)) {
                 Logger.Debug("Plugins are ok");
             } else {
                 listener.fatalError("\n[DIMENSIONS] The plugin combinations you have selected are not valid.");
                 listener.fatalError("\n[DIMENSIONS] Please review online help to determine valid plugin uses.");
                 return false;
             }
 
             if (isCanJobUpdate()) {
                 // Get the details of the master
                 InetAddress netAddr = InetAddress.getLocalHost();
                 byte[] ipAddr = netAddr.getAddress();
                 String hostname = netAddr.getHostName();
 
                 boolean master = false;
                 GetHostDetailsTask buildHost = new GetHostDetailsTask(hostname);
                 master = workspace.act(buildHost);
 
                 if (master) {
                     // Running on master...
                     listener.getLogger().println("[DIMENSIONS] Running checkout on master...");
                     listener.getLogger().flush();
 
                     // Using Java API because this allows the plugin to work on platforms
                     // where Dimensions has not been ported, e.g. MAC OS, which is what
                     // I use
                     CheckOutAPITask task = new CheckOutAPITask(build,this,workspace,listener);
                     bRet = workspace.act(task);
                 } else {
                     // Running on slave... Have to use the command line as Java API will not
                     // work on remote hosts. Cannot serialise it...
                     int version = 2009;
                     long key = dmSCM.login(getJobUserName(),getJobPasswd(),
                                            getJobDatabase(),getJobServer());
 
                     if (key>0) {
                         // Get the server version
                         Logger.Debug("Login worked.");
                         version = dmSCM.getDmVersion();
                         if (version == 0) {
                             version = 2009;
                         }
                         dmSCM.logout(key);
                     }
 
                     {
                         // VariableResolver does not appear to be serialisable either, so...
                         VariableResolver<String> myResolver = build.getBuildVariableResolver();
 
                         String baseline = myResolver.resolve("DM_BASELINE");
                         String requests = myResolver.resolve("DM_REQUEST");
 
                         listener.getLogger().println("[DIMENSIONS] Running checkout on slave...");
                         listener.getLogger().flush();
 
                         CheckOutCmdTask task = new CheckOutCmdTask(getJobUserName(), getJobPasswd(),
                                                                    getJobDatabase(), getJobServer(),
                                                                    getProject(), baseline, requests,
                                                                    isCanJobDelete(),
                                                                    isCanJobRevert(), isCanJobForce(),
                                                                    (build.getPreviousBuild() == null),
                                                                    getFolders(),version,
                                                                    workspace,listener);
                         bRet = workspace.act(task);
                     }
                 }
             } else {
                 bRet = true;
             }
 
             if (bRet) {
                 bRet = generateChangeSet(build,listener,changelogFile);
             }
         }
         catch(Exception e)
         {
             String errMsg = e.getMessage();
             if (errMsg == null) {
                 errMsg = "An unknown error occurred. Please try the operation again.";
             }
             listener.fatalError("Unable to run checkout callout - " + errMsg);
             // e.printStackTrace();
             //throw new IOException("Unable to run checkout callout - " + e.getMessage());
             bRet = false;
         }
         return bRet;
     }
 
 
     /*
      *-----------------------------------------------------------------
      *  FUNCTION SPECIFICATION
      *  Name:
      *      generateChangeSet
      *  Description:
      *      Generate the changeset
      * Parameters:
      *      @param AbstractProject build
      *      @param BuildListener listener
      *      @param File changelogFile
      *  Return:
      *      @return boolean
      *-----------------------------------------------------------------
      */
     private boolean generateChangeSet(final AbstractBuild build, final BuildListener listener,
                             final File changelogFile)
                             throws IOException, InterruptedException {
         long key = -1;
         boolean bRet = false;
         DimensionsAPI dmSCM = new DimensionsAPI();
 
         try
         {
             // When are we building files for?
             // Looking for the last successful build and then going forward from there - could use the last build as well
             //
             // Calendar lastBuildCal = (build.getPreviousBuild() != null) ? build.getPreviousBuild().getTimestamp() : null;
             Calendar lastBuildCal = (build.getPreviousNotFailedBuild() != null) ? build.getPreviousNotFailedBuild().getTimestamp() : null;
             Calendar nowDateCal = Calendar.getInstance();
 
             TimeZone tz = (getJobTimeZone() != null && getJobTimeZone().length() > 0) ? TimeZone.getTimeZone(getJobTimeZone()) : TimeZone.getDefault();
             if (getJobTimeZone() != null && getJobTimeZone().length() > 0)
                 Logger.Debug("Job timezone setting is " + getJobTimeZone());
 
             Logger.Debug("Log updates between " + ((lastBuildCal != null) ? DateUtils.getStrDate(lastBuildCal,tz) : "0") +
                             " -> " + DateUtils.getStrDate(nowDateCal,tz) + " (" + tz.getID() + ")");
 
             dmSCM.setLogger(listener.getLogger());
 
             // Connect to Dimensions...
             key = dmSCM.login(getJobUserName(),getJobPasswd(),
                             getJobDatabase(),getJobServer());
 
             if (key>0)
             {
                 Logger.Debug("Login worked.");
                 VariableResolver<String> myResolver = build.getBuildVariableResolver();
 
                 String baseline = myResolver.resolve("DM_BASELINE");
                 String requests = myResolver.resolve("DM_REQUEST");
 
                 if (baseline != null) {
                     baseline = baseline.trim();
                     baseline = baseline.toUpperCase();
                 }
                 if (requests != null) {
                     requests = requests.replaceAll(" ","");
                     requests = requests.toUpperCase();
                 }
 
                 Logger.Debug("Extra parameters - " + baseline + " " + requests);
                 String[] folders = getFolders();
 
                 if (baseline != null && baseline.length() == 0)
                     baseline = null;
                 if (requests != null && requests.length() == 0)
                     requests = null;
 
                 bRet = true;
 
                 // Iterate through the project folders and process them in Dimensions
                 for (int ii=0;ii<folders.length; ii++) {
                     if (!bRet)
                         break;
 
                     String folderN = folders[ii];
                     File fileName = new File(folderN);
                     FilePath dname = new FilePath(fileName);
 
                     Logger.Debug("Looking for changes in '" + folderN + "'...");
 
                     // Checkout the folder
                     bRet = dmSCM.createChangeSetLogs(key,getProject(),dname,
                                           lastBuildCal,nowDateCal,
                                           changelogFile, tz,
                                           jobWebUrl,
                                           baseline,requests);
                 }
 
                 // Close the changes log file
                 {
                     FileWriter logFile = null;
                     try {
                         logFile = new FileWriter(changelogFile,true);
                         PrintWriter fmtWriter = new PrintWriter(logFile);
                         fmtWriter.println("</changelog>");
                         logFile.flush();
                         bRet=true;
                     } catch (Exception e) {
                         throw new IOException("Unable to write change log - " + e.getMessage());
                     } finally {
                         logFile.close();
                     }
                 }
             }
         }
         catch(Exception e)
         {
             String errMsg = e.getMessage();
             if (errMsg == null) {
                 errMsg = "An unknown error occurred. Please try the operation again.";
             }
            listener.fatalError("Unable to run checkout callout - " + errMsg);
             // e.printStackTrace();
            //throw new IOException("Unable to run checkout callout - " + e.getMessage());
             bRet = false;
         }
         finally
         {
             dmSCM.logout(key);
         }
         return bRet;
     }
 
     /*
      *-----------------------------------------------------------------
      *  FUNCTION SPECIFICATION
      *  Name:
      *      pollChanges
      *  Description:
      *      Has the repository had any changes?
      * Parameters:
      *      @param AbstractProject project
      *      @param Launcher launcher
      *      @param FilePath workspace
      *      @param TaskListener listener
      *  Return:
      *      @return boolean
      *-----------------------------------------------------------------
      */
     @Override
     public boolean pollChanges(final AbstractProject project, final Launcher launcher,
                                final FilePath workspace, final TaskListener listener)
                               throws IOException, InterruptedException
     {
         boolean bChanged = false;
 
         Logger.Debug("Invoking pollChanges - " + this.getClass().getName() );
         Logger.Debug("Checking job - " + project.getName());
         long key = -1;
 
         if (getProject() == null || getProject().length() == 0)
             return false;
 
         if (project.getLastBuild() == null)
             return true;
 
         try
         {
             Calendar lastBuildCal = null;
             if (project.getLastSuccessfulBuild() != null && project.getLastSuccessfulBuild().getTimestamp() != null)
                 lastBuildCal = project.getLastSuccessfulBuild().getTimestamp();
             else
                 lastBuildCal = project.getLastBuild().getTimestamp();
 
             Calendar nowDateCal = Calendar.getInstance();
             TimeZone tz = (getJobTimeZone() != null && getJobTimeZone().length() > 0) ? TimeZone.getTimeZone(getJobTimeZone()) : TimeZone.getDefault();
             if (getJobTimeZone() != null && getJobTimeZone().length() > 0)
                 Logger.Debug("Job timezone setting is " + getJobTimeZone());
 
             Logger.Debug("Checking for any updates between " + ((lastBuildCal != null) ? DateUtils.getStrDate(lastBuildCal,tz) : "0") +
                             " -> " + DateUtils.getStrDate(nowDateCal,tz) + " (" + tz.getID() + ")");
 
             if (dmSCM == null)
             {
                 Logger.Debug("Creating new API interface object");
                 dmSCM = new DimensionsAPI();
             }
 
             dmSCM.setLogger(listener.getLogger());
 
             // Connect to Dimensions...
             key = dmSCM.login(jobUserName,
                             getJobPasswd(),
                             jobDatabase,
                             jobServer);
             if (key>0)
             {
                 String[] folders = getFolders();
                 // Iterate through the project folders and process them in Dimensions
                 for (int ii=0;ii<folders.length; ii++) {
                     if (bChanged)
                         break;
 
                     String folderN = folders[ii];
                     File fileName = new File(folderN);
                     FilePath dname = new FilePath(fileName);
 
                     Logger.Debug("Polling using key "+key);
                     Logger.Debug("Polling '" + folderN + "'...");
 
                     bChanged = dmSCM.hasRepositoryBeenUpdated(key,getProject(),
                                                               dname,lastBuildCal,
                                                               nowDateCal, tz);
                 }
             }
         }
         catch(Exception e)
         {
             String errMsg = e.getMessage();
             if (errMsg == null) {
                 errMsg = "An unknown error occurred. Please try the operation again.";
             }
             listener.fatalError("Unable to run pollChanges callout - " + errMsg);
             // e.printStackTrace();
             //throw new IOException("Unable to run pollChanges callout - " + e.getMessage());
             bChanged = false;
         }
         finally
         {
             dmSCM.logout(key);
         }
 
         return bChanged;
     }
 
     /*
      *-----------------------------------------------------------------
      *  FUNCTION SPECIFICATION
      *  Name:
      *      createChangeLogParser
      *  Description:
      *      Create a log parser object
      * Parameters:
      *  Return:
      *      @return ChangeLogParser
      *-----------------------------------------------------------------
      */
     @Override
     public ChangeLogParser createChangeLogParser()
     {
         Logger.Debug("Invoking createChangeLogParser - " + this.getClass().getName());
         return new DimensionsChangeLogParser();
     }
 
     /*
      *-----------------------------------------------------------------
      *  FUNCTION SPECIFICATION
      *  Name:
      *      SCMDescriptor
      *  Description:
      *      Return an SCM descriptor
      * Parameters:
      *  Return:
      *      @return DescriptorImpl
      *-----------------------------------------------------------------
      */
     @Override
     public DescriptorImpl getDescriptor()
     {
         return DM_DESCRIPTOR;
     }
 
 
     /*
      * Implementation class for Dimensions plugin
      */
     public static class
     DescriptorImpl extends SCMDescriptor<DimensionsSCM> implements ModelObject {
 
         DimensionsAPI connectionCheck = null;
 
         private String server;
         private String userName;
         private String passwd;
         private String database;
 
         private String timeZone;
         private String webUrl;
 
         private boolean canUpdate;
 
         /*
          * Loads the SCM descriptor
          */
         public DescriptorImpl()
         {
             super(DimensionsSCM.class, DimensionsSCMRepositoryBrowser.class);
             load();
             Logger.Debug("Loading " + this.getClass().getName());
         }
 
         public String getDisplayName()
         {
             return "Dimensions";
         }
 
         /*
          * Save the SCM descriptor configuration
          */
         @Override
         public boolean configure(StaplerRequest req, JSONObject jobj) throws FormException
         {
             // Get the values and check them
             userName = req.getParameter("dimensionsscm.userName");
             passwd = req.getParameter("dimensionsscm.passwd");
             server = req.getParameter("dimensionsscm.server");
             database = req.getParameter("dimensionsscm.database");
 
             timeZone = req.getParameter("dimensionsscm.timeZone");
             webUrl = req.getParameter("dimensionsscm.webUrl");
 
             if (userName != null)
                 userName = Util.fixNull(req.getParameter("dimensionsscm.userName").trim());
 
             if (passwd != null)
                 passwd = Util.fixNull(req.getParameter("dimensionsscm.passwd").trim());
 
             if (server != null)
                 server = Util.fixNull(req.getParameter("dimensionsscm.server").trim());
 
             if (database != null)
                 database = Util.fixNull(req.getParameter("dimensionsscm.database").trim());
 
             if (timeZone != null)
                 timeZone = Util.fixNull(req.getParameter("dimensionsscm.timeZone").trim());
 
             if (webUrl != null)
                 webUrl = Util.fixNull(req.getParameter("dimensionsscm.webUrl").trim());
 
             req.bindJSON(DM_DESCRIPTOR, jobj);
 
             this.save();
             return super.configure(req, jobj);
         }
 
         @Override
         public SCM newInstance(StaplerRequest req, JSONObject formData) throws FormException {
             // Get variables and then construct a new object
             String[] folders = req.getParameterValues("dimensionsscm.folders");
 
             String project = req.getParameter("dimensionsscm.project");
             String directory = req.getParameter("dimensionsscm.directory");
             String workarea = req.getParameter("dimensionsscm.workarea");
 
             Boolean canJobDelete = Boolean.valueOf("on".equalsIgnoreCase(req.getParameter("dimensionsscm.canJobDelete")));
             Boolean canJobForce = Boolean.valueOf("on".equalsIgnoreCase(req.getParameter("dimensionsscm.canJobForce")));
             Boolean canJobRevert = Boolean.valueOf("on".equalsIgnoreCase(req.getParameter("dimensionsscm.canJobRevert")));
             Boolean canJobUpdate = Boolean.valueOf("on".equalsIgnoreCase(req.getParameter("dimensionsscm.canJobUpdate")));
 
             String jobUserName = req.getParameter("dimensionsscm.jobUserName");
             String jobPasswd = req.getParameter("dimensionsscm.jobPasswd");
             String jobServer = req.getParameter("dimensionsscm.jobServer");
             String jobDatabase = req.getParameter("dimensionsscm.jobDatabase");
             String jobTimeZone = req.getParameter("dimensionsscm.jobTimeZone");
             String jobWebUrl = req.getParameter("dimensionsscm.jobWebUrl");
 
             DimensionsSCM scm = new DimensionsSCM(project,folders,workarea,canJobDelete,
                                                   canJobForce,canJobRevert,
                                                   jobUserName,jobPasswd,
                                                   jobServer,jobDatabase,
                                                   canJobUpdate,jobTimeZone,
                                                   jobWebUrl,directory);
 
             scm.browser = RepositoryBrowsers.createInstance(DimensionsSCMRepositoryBrowser.class,req,formData,"browser");
             if (scm.dmSCM == null)
                 scm.dmSCM = new DimensionsAPI();
             return scm;
         }
 
         /*
          * Gets the timezone for the connection.
          * @return the timezone
          */
         public String getTimeZone() {
             return this.timeZone;
         }
 
         /*
          * Gets the weburl ID for the connection.
          * @return the weburl
          */
         public String getWebUrl() {
             return this.webUrl;
         }
 
         /*
          * Gets the user ID for the connection.
          * @return the user ID of the user as whom to connect
          */
         public String getUserName() {
             return this.userName;
         }
 
         /*
          * Gets the base database for the connection (as "NAME@CONNECTION").
          * @return the name of the base database to connect to
          */
         public String getDatabase() {
             return this.database;
         }
 
         /*
          * Gets the server for the connection.
          * @return the name of the server to connect to
          */
         public String getServer() {
             return this.server;
         }
 
         /*
          * Gets the password .
          * @return the password
          */
         public String getPasswd() {
             return Scrambler.descramble(passwd);
         }
 
         /*
          * Gets the update .
          * @return the update
          */
         public boolean isCanUpdate() {
             return this.canUpdate;
         }
 
         /*
          * Sets the update .
          */
         public void setCanUpdate(boolean x) {
             this.canUpdate = x;
         }
 
         /*
          * Sets the user ID for the connection.
          */
         public void setUserName(String userName) {
             this.userName = userName;
         }
 
         /*
          * Sets the base database for the connection (as "NAME@CONNECTION").
          */
         public void setDatabase(String database) {
             this.database = database;
         }
 
         /*
          * Sets the server for the connection.
          */
         public void setServer(String server) {
             this.server = server;
         }
 
         /*
          * Sets the password .
          */
         public void setPasswd(String password) {
             this.passwd = Scrambler.scramble(password);
         }
 
         /*
          * Sets the timezone for the connection.
          */
         public void setTimeZone(String x) {
             this.timeZone = x;
         }
 
         /*
          * Sets the weburl ID for the connection.
          */
         public void setWebUrl(String x) {
             this.webUrl = x;
         }
 
         private void doCheck(StaplerRequest req, StaplerResponse rsp)
                             throws IOException, ServletException
         {
             new FormFieldValidator(req, rsp, false)
             {
                 @Override
                 protected void check() throws IOException, ServletException
                 {
                     String value = Util.fixEmpty(request.getParameter("value"));
                     String nullText = null;
                     if (value == null)
                     {
                         if (nullText == null)
                             ok();
                         else
                             error(nullText);
 
                         return;
                     }
                     else
                     {
                         ok();
                         return;
                     }
                 }
             }.process();
         }
 
         public void domanadatoryFieldCheck(StaplerRequest req, StaplerResponse rsp)
                             throws IOException, ServletException
         {
             new FormFieldValidator(req, rsp, false)
             {
                 @Override
                 protected void check() throws IOException, ServletException
                 {
                     String value = Util.fixEmpty(request.getParameter("value"));
                     String errorTxt = "This value is manadatory.";
                     if (value == null)
                     {
                         error(errorTxt);
                         return;
                     }
                     else
                     {
                         // Some processing
                         ok();
                         return;
                     }
                 }
             }.process();
         }
 
         public void domanadatoryJobFieldCheck(StaplerRequest req, StaplerResponse rsp)
                             throws IOException, ServletException
         {
             new FormFieldValidator(req, rsp, false)
             {
                 @Override
                 protected void check() throws IOException, ServletException
                 {
                     String value = Util.fixEmpty(request.getParameter("value"));
                     String errorTxt = "This value is manadatory.";
                     // Some processing in the future
                     ok();
                     return;
                 }
             }.process();
         }
 
         /*
          * Check if the specified Dimensions server is valid
          */
         public void docheckTz(StaplerRequest req, StaplerResponse rsp,
                                 @QueryParameter("dimensionsscm.timeZone") final String timezone,
                                 @QueryParameter("dimensionsscm.jobTimeZone") final String jobtimezone)
                             throws IOException, ServletException
         {
             new FormFieldValidator(req, rsp, false)
             {
                 @Override
                 protected void check() throws IOException, ServletException
                 {
                     try
                     {
                         String xtz = (jobtimezone != null) ? jobtimezone : timezone;
                         Logger.Debug("Invoking docheckTz - " + xtz);
                         TimeZone ctz = TimeZone.getTimeZone(xtz);
                         String  lmt = ctz.getID();
                         if (lmt.equalsIgnoreCase("GMT") && !(xtz.equalsIgnoreCase("GMT") ||
                                              xtz.equalsIgnoreCase("Greenwich Mean Time") ||
                                              xtz.equalsIgnoreCase("UTC") ||
                                              xtz.equalsIgnoreCase("Coordinated Universal Time")))
                             error("Timezone specified is not valid.");
                         else
                             ok("Timezone test succeeded!");
 
                         return;
                     }
                     catch (Exception e)
                     {
                         error("timezone check error:" + e.getMessage());
                     }
                 }
             }.process();
         }
 
         /*
          * Check if the specified Dimensions server is valid
          */
         public void docheckServer(StaplerRequest req, StaplerResponse rsp,
                                 @QueryParameter("dimensionsscm.userName") final String user,
                                 @QueryParameter("dimensionsscm.passwd") final String passwd,
                                 @QueryParameter("dimensionsscm.server") final String server,
                                 @QueryParameter("dimensionsscm.database") final String database,
                                 @QueryParameter("dimensionsscm.jobUserName") final String jobuser,
                                 @QueryParameter("dimensionsscm.jobPasswd") final String jobPasswd,
                                 @QueryParameter("dimensionsscm.jobServer") final String jobServer,
                                 @QueryParameter("dimensionsscm.jobDatabase") final String jobDatabase)
                             throws IOException, ServletException
         {
             new FormFieldValidator(req, rsp, false)
             {
                 @Override
                 protected void check() throws IOException, ServletException
                 {
                     if (connectionCheck == null)
                         connectionCheck = new DimensionsAPI();
 
                     try
                     {
                         String xserver = (jobServer != null) ? jobServer : server;
                         String xuser = (jobuser != null) ? jobuser : user;
                         String xpasswd = (jobPasswd != null) ? jobPasswd : passwd;
                         String xdatabase = (jobDatabase != null) ? jobDatabase : database;
                         long key = -1;
                         String dmS = xserver + "-" + xuser + ":" + xdatabase;
                         Logger.Debug("Invoking serverCheck - " + dmS);
                         key = connectionCheck.login(xuser,
                                                   xpasswd,
                                                   xdatabase,
                                                   xserver);
                         if (key<1)
                         {
                             error("Connection test failed");
                         }
                         else
                         {
                             ok("Connection test succeeded!");
                             connectionCheck.logout(key);
                         }
                         return;
                     }
                     catch (Exception e)
                     {
                         error("Server connection error:" + e.getMessage());
                     }
                 }
             }.process();
         }
     }
 }
 
 
