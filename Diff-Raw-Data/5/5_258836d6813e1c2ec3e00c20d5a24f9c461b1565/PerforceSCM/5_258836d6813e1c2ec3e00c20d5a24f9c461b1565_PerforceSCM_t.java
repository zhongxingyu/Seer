 package hudson.plugins.perforce;
 
 import com.tek42.perforce.Depot;
 import com.tek42.perforce.PerforceException;
 import com.tek42.perforce.model.Changelist;
 import com.tek42.perforce.model.Counter;
 import com.tek42.perforce.model.Label;
 import com.tek42.perforce.model.Workspace;
 import com.tek42.perforce.parse.Counters;
 import com.tek42.perforce.parse.Workspaces;
 
 import hudson.AbortException;
 import hudson.EnvVars;
 import hudson.Extension;
 import hudson.FilePath;
 import hudson.Util;
 import hudson.FilePath.FileCallable;
 import hudson.Launcher;
 import static hudson.Util.fixNull;
 import hudson.matrix.MatrixBuild;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.BuildListener;
 import hudson.model.Computer;
 import hudson.model.Hudson;
 import hudson.model.Node;
 import hudson.model.Run;
 import hudson.model.TaskListener;
 import hudson.remoting.VirtualChannel;
 import hudson.scm.ChangeLogParser;
 import hudson.scm.SCM;
 import hudson.scm.SCMDescriptor;
 import hudson.util.FormValidation;
 
 import hudson.util.StreamTaskListener;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.StaplerRequest;
 
 import javax.servlet.ServletException;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.net.InetAddress;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Extends {@link SCM} to provide integration with Perforce SCM repositories.
  *
  * @author Mike Wille
  * @author Brian Westrich
  * @author Victor Szoltysek
  */
 public class PerforceSCM extends SCM {
 
     String p4User;
     String p4Passwd;
     String p4Port;
     String p4Client;
     String projectPath;
     /* This is better for build than original options: noallwrite noclobber nocompress unlocked nomodtime normdir */
     String projectOptions;
     String p4Label;
     String p4Counter;
 
     String p4Exe = "C:\\Program Files\\Perforce\\p4.exe";
     String p4SysDrive = "C:";
     String p4SysRoot = "C:\\WINDOWS";
 
     PerforceRepositoryBrowser browser;
 
     private static final Logger LOGGER = Logger.getLogger(PerforceSCM.class.getName());
 
     /**
      * This is being removed, including it as transient to fix exceptions on startup.
      */
     transient int lastChange;
     /**
      * force sync is a one time trigger from the config area to force a sync with the depot.
      * it is reset to false after the first checkout.
      */
     boolean forceSync = false;
     /**
      * If true, we will manage the workspace view within the plugin.  If false, we will leave the
      * view alone.
      */
     boolean updateView = true;
     /**
      * If false we add the slave hostname to the end of the client name when
      * running on a slave.  Defaulting to true so as not to change the behavior
      * for existing users.
      */
     boolean dontRenameClient = true;
     /**
      * If true we update the named counter to the last changelist value after the sync operation.
      * If false the counter will be used as the changelist value to sync to.
      * Defaulting to false since the counter name is not set to begin with.
      */
     boolean updateCounterValue = false;
     /**
      * If true the environment value P4PASSWD will be set to the value of p4Passwd.
      */
     boolean exposeP4Passwd = false;
 
     /**
      * If > 0, then will override the changelist we sync to for the first build.
      */
     int firstChange = -1;
 
     /**
      * If a ticket was issued we can use it instead of the password in the environment.
      */
     private String p4Ticket = null;
 
     @DataBoundConstructor
     public PerforceSCM(String p4User, String p4Passwd, String p4Client, String p4Port, String projectPath, String projectOptions,
                        String p4Exe, String p4SysRoot, String p4SysDrive, String p4Label, String p4Counter, boolean updateCounterValue,
                        boolean forceSync, boolean updateView, boolean dontRenameClient, boolean exposeP4Passwd,
                        int firstChange, PerforceRepositoryBrowser browser) {
 
         this.p4User = p4User;
         this.setP4Passwd(p4Passwd);
         this.exposeP4Passwd = exposeP4Passwd;
         this.p4Client = p4Client;
         this.p4Port = p4Port;
         this.projectOptions = (projectOptions != null)
                 ? projectOptions
                 : "noallwrite clobber nocompress unlocked nomodtime rmdir";
 
         // Make it backwards compatible with the old way of specifying a label
         Matcher m = Pattern.compile("(@\\S+)\\s*").matcher(projectPath);
         if (m.find()) {
             p4Label = m.group(1);
             projectPath = projectPath.substring(0,m.start(1))
                 + projectPath.substring(m.end(1));
         }
 
         if (this.p4Label != null && p4Label != null) {
             Logger.getLogger(PerforceSCM.class.getName()).warning(
                     "Label found in views and in label field.  Using: "
                     + p4Label);
         }
         this.p4Label = Util.fixEmptyAndTrim(p4Label);
 
         this.p4Counter = Util.fixEmptyAndTrim(p4Counter);
         this.updateCounterValue = updateCounterValue;
 
         this.projectPath = projectPath;
 
         if (p4Exe != null)
             this.p4Exe = p4Exe;
 
         if (p4SysRoot != null && p4SysRoot.length() != 0)
             this.p4SysRoot = p4SysRoot;
 
         if (p4SysDrive != null && p4SysDrive.length() != 0)
             this.p4SysDrive = p4SysDrive;
 
         // Get systemDrive,systemRoot computer environment variables from
         // the current machine.
         String systemDrive = null;
         String systemRoot = null;
         if (Hudson.isWindows()) {
             try {
                 EnvVars envVars = Computer.currentComputer().getEnvironment();
                 systemDrive = envVars.get("SystemDrive");
                 systemRoot = envVars.get("SystemRoot");
             } catch (Exception ex) {
                 LOGGER.log(Level.WARNING, ex.getMessage(), ex);
             }
         }
         if (p4SysRoot != null && p4SysRoot.length() != 0) {
             this.p4SysRoot = p4SysRoot;
         } else {
             if (systemRoot != null && !systemRoot.trim().equals("")) {
                 this.p4SysRoot = systemRoot;
             }
         }
         if (p4SysDrive != null && p4SysDrive.length() != 0) {
             this.p4SysDrive = p4SysDrive;
         } else {
             if (systemDrive != null && !systemDrive.trim().equals("")) {
                 this.p4SysDrive = systemDrive;
             }
         }
 
         this.forceSync = forceSync;
         this.browser = browser;
         this.updateView = updateView;
         this.dontRenameClient = dontRenameClient;
         this.firstChange = firstChange;
     }
 
     /**
      * This only exists because we need to do initialization after we have been brought
      * back to life.  I'm not quite clear on stapler and how all that works.
      * At any rate, it doesn't look like we have an init() method for setting up our Depot
      * after all of the setters have been called.  Someone correct me if I'm wrong...
      *
      * UPDATE: With the addition of PerforceMailResolver, we now have need to share the depot object.  I'm making
      * this protected to enable that.
      *
      * Always create a new Depot to reflect any changes to the machines that
      * P4 actions will be performed on.
      */
     protected Depot getDepot(Launcher launcher, FilePath workspace) {
 
         HudsonP4ExecutorFactory p4Factory = new HudsonP4ExecutorFactory(launcher,workspace);
 
         Depot depot = new Depot(p4Factory);
         depot.setUser(p4User);
 
         PerforcePasswordEncryptor encryptor = new PerforcePasswordEncryptor();
         depot.setPassword(encryptor.decryptString(p4Passwd));
 
         depot.setPort(p4Port);
         depot.setClient(p4Client);
 
         depot.setExecutable(p4Exe);
         depot.setSystemDrive(p4SysDrive);
         depot.setSystemRoot(p4SysRoot);
 
         return depot;
     }
 
     /**
      * Override of SCM.buildEnvVars() in order to setup the last change we have
      * sync'd to as a Hudson
      * environment variable: P4_CHANGELIST
      *
      * @param build
      * @param env
      */
     @Override
     public void buildEnvVars(AbstractBuild build, Map<String, String> env) {
         super.buildEnvVars(build, env);
         env.put("P4PORT", p4Port);
         env.put("P4USER", p4User);
 
         // if we want to allow p4 commands in script steps this helps
         if (exposeP4Passwd) {
             // this may help when tickets are used since we are
             // not storing the ticket on the client during login
             if (p4Ticket != null) {
                 env.put("P4PASSWD", p4Ticket);
             } else {
                 PerforcePasswordEncryptor encryptor = new PerforcePasswordEncryptor();
                 env.put("P4PASSWD", encryptor.decryptString(p4Passwd));
             }
         }
 
         env.put("P4CLIENT", getEffectiveClientName(build));
         PerforceTagAction pta = getMostRecentTagAction(build);
         if (pta != null) {
             if (pta.getChangeNumber() > 0) {
                 int lastChange = pta.getChangeNumber();
                 env.put("P4_CHANGELIST", Integer.toString(lastChange));
             }
             else if (pta.getTag() != null) {
                 String label = pta.getTag();
                 env.put("P4_LABEL", label);
             }
         }
     }
 
     /**
      * Perform some manipulation on the workspace URI to get a valid local path
      * <p>
      * Is there an issue doing this?  What about remote workspaces?  does that happen?
      *
      * @param path
      * @return
      * @throws IOException
      * @throws InterruptedException
      */
     private String getLocalPathName(FilePath path, boolean isUnix) throws IOException, InterruptedException {
         String uriString = path.toURI().toString();
         // Get rid of URI prefix
         // NOTE: this won't handle remote files, is that a problem?
         uriString = uriString.replaceAll("file:/", "");
         // It seems there is a /./ to denote the root in the path on my test instance.
         // I don't know if this is in production, or how it works on other platforms (non win32)
         // but I am removing it here because perforce doesn't like it.
         uriString = uriString.replaceAll("/./", "/");
         // The URL is also escaped.  We need to unescape it because %20 in path names isn't cool for perforce.
         uriString = URLDecoder.decode(uriString, "UTF-8");
 
         // Last but not least, we need to convert this to local path separators.
         if (isUnix) {
             // on unixen we need to prepend with /
             uriString = "/" + uriString;
         } else {
             //just replace with sep doesn't work because java's foobar regexp replaceAll
             uriString = uriString.replaceAll("/", "\\\\");
         }
 
         return uriString;
     }
 
     /*
      * @see hudson.scm.SCM#checkout(hudson.model.AbstractBuild, hudson.Launcher, hudson.FilePath, hudson.model.BuildListener, java.io.File)
      */
     @Override
     public boolean checkout(AbstractBuild build, Launcher launcher,
             FilePath workspace, BuildListener listener, File changelogFile) throws IOException, InterruptedException {
 
         PrintStream log = listener.getLogger();
 
         //keep projectPath local so any modifications for slaves don't get saved
         String projectPath = this.projectPath;
         Depot depot = getDepot(launcher,workspace);
 
         // If the 'master' MatrixBuild runs on the same node as any of the 'child' MatrixRuns,
         // and it syncs to its own root, then the child's workspace won't get updated.
         // http://bugs.sun.com/view_bug.do?bug_id=6548436  (bug 1022)
         boolean dontChangeRoot = ((Object)build instanceof MatrixBuild);
 
         try {
             Workspace p4workspace = getPerforceWorkspace(depot, build.getBuiltOn(), launcher, workspace, listener, dontChangeRoot);
 
             if (p4workspace.isNew()) {
                 log.println("Saving new client " + p4workspace.getName());
                 depot.getWorkspaces().saveWorkspace(p4workspace);
             }
             else if (p4workspace.isDirty()) {
                 log.println("Saving modified client " + p4workspace.getName());
                 depot.getWorkspaces().saveWorkspace(p4workspace);
             }
 
             //Get the list of changes since the last time we looked...
             String p4WorkspacePath = "//" + p4workspace.getName() + "/...";
             final int lastChange = getLastChange((Run)build.getPreviousBuild());
             log.println("Last sync'd change: " + lastChange);
 
             List<Changelist> changes;
             int newestChange = lastChange;
             if (p4Label != null) {
                 changes = new ArrayList<Changelist>(0);
             } else {
                 String counterName;
                 if (p4Counter != null && !updateCounterValue)
                     counterName = p4Counter;
                 else
                     counterName = "change";
 
                 Counter counter = depot.getCounters().getCounter(counterName);
                 newestChange = counter.getValue();
 
                 if (lastChange >= newestChange) {
                     changes = new ArrayList<Changelist>(0);
                 } else {
                     List<Integer> changeNumbersTo = depot.getChanges().getChangeNumbersInRange(p4workspace, lastChange+1, newestChange);
                     changes = depot.getChanges().getChangelistsFromNumbers(changeNumbersTo);
                 }
             }
 
             if (changes.size() > 0) {
                 // Save the changes we discovered.
                 PerforceChangeLogSet.saveToChangeLog(
                         new FileOutputStream(changelogFile), changes);
                 newestChange = changes.get(0).getChangeNumber();
             }
             else {
                 // No new changes discovered (though the definition of the workspace or label may have changed).
                 createEmptyChangeLog(changelogFile, listener, "changelog");
                 // keep the newestChange to the same value except when changing
                 // definitions from label builds to counter builds
                 if (lastChange != -1)
                     newestChange = lastChange;
             }
 
             // Now we can actually do the sync process...
             StringBuilder sbMessage = new StringBuilder("Sync'ing workspace to ");
             StringBuilder sbSyncPath = new StringBuilder(p4WorkspacePath);
             sbSyncPath.append("@");
 
             if (p4Label != null) {
                 sbMessage.append("label ");
                 sbMessage.append(p4Label);
                 sbSyncPath.append(p4Label);
             }
             else {
                 sbMessage.append("changelist ");
                 sbMessage.append(newestChange);
                 sbSyncPath.append(newestChange);
             }
 
             if (forceSync)
                 sbMessage.append(" (forcing sync of unchanged files).");
             else
                 sbMessage.append(".");
 
             log.println(sbMessage.toString());
             String syncPath = sbSyncPath.toString();
 
             long startTime = System.currentTimeMillis();
 
             depot.getWorkspaces().syncTo(syncPath, forceSync);
 
             long endTime = System.currentTimeMillis();
             long duration = endTime - startTime;
 
             log.println("Sync complete, took " + duration + " ms");
 
             // reset one time use variables...
             forceSync = false;
             firstChange = -1;
 
             if (p4Label != null) {
                 // Add tagging action that indicates that the build is already
                 // tagged (you can't label a label).
                 build.addAction(new PerforceTagAction(
                         build, depot, p4Label, projectPath));
             }
             else {
                 // Add tagging action that enables the user to create a label
                 // for this build.
                 build.addAction(new PerforceTagAction(
                         build, depot, newestChange, projectPath));
             }
 
             if (p4Counter != null && updateCounterValue) {
                 // Set or create a counter to mark this change
                 Counter counter = new Counter();
                 counter.setName(p4Counter);
                 counter.setValue(newestChange);
                 log.println("Updating counter " + p4Counter + " to " + newestChange);
                 depot.getCounters().saveCounter(counter);
             }
 
             //save the one time use variables...
             build.getParent().save();
 
             // remember the p4Ticket if we were issued one
             p4Ticket = depot.getP4Ticket();
 
             return true;
 
         } catch (PerforceException e) {
             log.print("Caught exception communicating with perforce. " + e.getMessage());
             StringWriter sw = new StringWriter();
             PrintWriter pw = new PrintWriter(sw, true);
             e.printStackTrace(pw);
             pw.flush();
             log.print(sw.toString());
             throw new AbortException(
                     "Unable to communicate with perforce. " + e.getMessage());
 
         } catch (InterruptedException e) {
             throw new IOException(
                     "Unable to get hostname from slave. " + e.getMessage());
         }
     }
 
     /**
      * compute the path(s) that we search on to detect whether the project
      * has any unsynched changes
      *
      * @param p4workspace the workspace
      * @return a string of path(s), e.g. //mymodule1/... //mymodule2/...
      */
     private String getChangesPaths(Workspace p4workspace) {
         return PerforceSCMHelper.computePathFromViews(p4workspace.getViews());
     }
 
     @Override
     public PerforceRepositoryBrowser getBrowser() {
        return browser;
     }
 
     /*
      * @see hudson.scm.SCM#createChangeLogParser()
      */
     @Override
     public ChangeLogParser createChangeLogParser() {
         return new PerforceChangeLogParser();
     }
 
     /*
      * @see hudson.scm.SCM#pollChanges(hudson.model.AbstractProject, hudson.Launcher, hudson.FilePath, hudson.model.TaskListener)
      *
      * When *should* this method return true?
      *
      * 1) When there is no previous build (might be the first, or all previous
      *    builds might have been deleted).
      *
      * 2) When the previous build did not use Perforce, in which case we can't
      *    be "sure" of the state of the files.
      *
      * 3) If the clientspec's views have changed since the last build; we don't currently
      *    save that info, but we should!  I (James Synge) am not sure how to save it;
      *    should it be:
      *         a) in the build.xml file, and if so, how do we save it there?
      *         b) in the change log file (which actually makes a fair amount of sense)?
      *         c) in a separate file in the build directory (not workspace),
      *            along side the change log file?
      *
      * 4) p4Label has changed since the last build (either from unset to set, or from
      *    one label to another).
      *
      * 5) p4Label is set AND unchanged AND the set of file-revisions selected
      *    by the label in the p4 workspace has changed.  Unfortunately, I don't
      *    know of a cheap way to do this.
      *
      * There may or may not have been a previous build.  That build may or may not
      * have been done using Perforce, and if with Perforce, may have been done
      * using a label or latest, and may or may not be for the same view as currently
      * defined.  If any change has occurred, we'll treat that as a reason to build.
      *
      * Note that the launcher and workspace may operate remotely (as of 2009-06-21,
      * they correspond to the node where the last build occurred, if any; if none,
      * then the master is used).
      *
      * Note also that this method won't be called while the workspace (not job)
      * is in use for building or some other polling thread.
      */
     @Override
     public boolean pollChanges(AbstractProject project, Launcher launcher,
             FilePath workspace, TaskListener listener) throws IOException, InterruptedException {
 
         PrintStream logger = listener.getLogger();
         logger.println("Looking for changes...");
 
         Depot depot = getDepot(launcher,workspace);
 
         try {
             Workspace p4workspace = getPerforceWorkspace(depot, project.getLastBuiltOn(), launcher, workspace, listener, false);
             if (p4workspace.isNew())
                 return true;
 
             Boolean needToBuild = needToBuild(p4workspace, project, depot, logger);
             if (needToBuild == null) {
                 needToBuild = wouldSyncChangeWorkspace(project, depot, logger);
             }
 
             if (needToBuild == Boolean.FALSE) {
                 return false;
             }
             else {
                 logger.println("Triggering a build.");
                 return true;
             }
         } catch (PerforceException e) {
             System.out.println("Problem: " + e.getMessage());
             logger.println("Caught Exception communicating with perforce." + e.getMessage());
             e.printStackTrace();
             throw new IOException("Unable to communicate with perforce.  Check log file for: " + e.getMessage());
         }
     }
 
     private Boolean needToBuild(Workspace p4workspace, AbstractProject project, Depot depot,
             PrintStream logger) throws IOException, InterruptedException, PerforceException {
 
         /*
          * Don't bother polling if we're already building, or soon will.
          * Ideally this would be a policy exposed to the user, perhaps for all
          * jobs with all types of scm, not just those using Perforce.
          */
 //        if (project.isBuilding() || project.isInQueue()) {
 //            logger.println("Job is already building or in the queue; skipping polling.");
 //            return Boolean.FALSE;
 //        }
 
         Run lastBuild = project.getLastBuild();
         if (lastBuild == null) {
             logger.println("No previous build exists.");
             return null;    // Unable to determine if there are changes.
         }
 
         PerforceTagAction action = lastBuild.getAction(PerforceTagAction.class);
         if (action == null) {
             logger.println("Previous build doesn't have Perforce info.");
             return null;
         }
 
         int lastChangeNumber = action.getChangeNumber();
         String lastLabelName = action.getTag();
 
         if (lastChangeNumber <= 0 && lastLabelName != null) {
             logger.println("Previous build was based on label " + lastLabelName);
             // Last build was based on a label, so we want to know if:
             //      the definition of the label was changed;
             //      or the view has been changed;
             //      or p4Label has been changed.
             if (p4Label == null) {
                 logger.println("Job configuration changed to build from head, not a label.");
                 return Boolean.TRUE;
             }
 
             if (!lastLabelName.equals(p4Label)) {
                 logger.println("Job configuration changed to build from label " + p4Label + ", not from head");
                 return Boolean.TRUE;
             }
 
             // No change in job definition (w.r.t. p4Label).  Don't currently
             // save enough info about the label to determine if it changed.
             logger.println("Assuming that the workspace and label definitions have not changed.");
             return Boolean.FALSE;
         }
 
         if (lastChangeNumber > 0) {
             logger.println("Last sync'd change was " + lastChangeNumber);
             if (p4Label != null) {
                 logger.println("Job configuration changed to build from label " + p4Label + ", not from head.");
                 return Boolean.TRUE;
             }
 
             // Has any new change been submitted since then (that is selected
             // by this workspace).
 
             String root = "//" + p4workspace.getName() + "/...";
             List<Integer> changeNumbers = depot.getChanges().getChangeNumbers(root, -1, 2);
             if (changeNumbers.isEmpty()) {
                 // Wierd, this shouldn't be!  I suppose it could happen if the
                 // view selects no files (e.g. //depot/non-existent-branch/...).
                 // Just in case, let's try to build.
                 return Boolean.TRUE;
             }
 
             int highestSelectedChangeNumber = changeNumbers.get(0);
             logger.println("Latest submitted change selected by workspace is " + highestSelectedChangeNumber);
             if (lastChangeNumber >= highestSelectedChangeNumber) {
                 // Note, can't determine with currently saved info
                 // whether the workspace definition has changed.
                 logger.println("Assuming that the workspace definition has not changed.");
                 return Boolean.FALSE;
             }
             else {
                 return Boolean.TRUE;
             }
         }
 
         return null;
     }
 
     // TODO Handle the case where p4Label is set.
     private boolean wouldSyncChangeWorkspace(AbstractProject project, Depot depot,
             PrintStream logger) throws IOException, InterruptedException, PerforceException {
 
         Workspaces workspaces = depot.getWorkspaces();
         String result = workspaces.syncDryRun().toString();
 
         if (result.startsWith("File(s) up-to-date.")) {
             logger.println("Workspace up-to-date.");
             return false;
         }
         else {
             logger.println("Workspace not up-to-date.");
             return true;
         }
     }
 
     public int getLastChange(Run build) {
         // If we are starting a new hudson project on existing work and want to skip the prior history...
         if (firstChange > 0)
             return firstChange;
 
         // If we can't find a PerforceTagAction, we will default to 0.
 
         PerforceTagAction action = getMostRecentTagAction(build);
         if (action == null)
             return 0;
 
         //log.println("Found last change: " + action.getChangeNumber());
         return action.getChangeNumber();
     }
 
     private PerforceTagAction getMostRecentTagAction(Run build) {
         if (build == null)
             return null;
 
         PerforceTagAction action = build.getAction(PerforceTagAction.class);
         if (action != null)
             return action;
 
         // if build had no actions, keep going back until we find one that does.
         return getMostRecentTagAction(build.getPreviousBuild());
     }
 
     private Workspace getPerforceWorkspace(
             Depot depot, Node buildNode,
             Launcher launcher, FilePath workspace, TaskListener listener, boolean dontChangeRoot)
         throws IOException, InterruptedException, PerforceException
     {
         PrintStream log = listener.getLogger();
 
         // If we are building on a slave node, and each node is supposed to have
         // its own unique client, then adjust the client name accordingly.
         // make sure each slave has a unique client name by adding it's
         // hostname to the end of the client spec
 
         String p4Client = this.p4Client;
         p4Client = getEffectiveClientName(buildNode, workspace);
 
         if (!nodeIsRemote(buildNode)) {
             log.print("Using master perforce client: ");
             log.println(p4Client);
         }
         else if (dontRenameClient) {
             log.print("Using shared perforce client: ");
             log.println(p4Client);
         }
         else {
             log.println("Using remote perforce client: " + p4Client);
         }
 
 
         depot.setClient(p4Client);
 
         // Get the clientspec (workspace) from perforce
 
         Workspace p4workspace = depot.getWorkspaces().getWorkspace(p4Client);
         assert p4workspace != null;
         boolean creatingNewWorkspace = p4workspace.isNew();
 
         // Ensure that the clientspec (workspace) name is set correctly
         // TODO Examine why this would be necessary.
 
         p4workspace.setName(p4Client);
 
         // Set the workspace options according to the configuration
         if (projectOptions != null)
             p4workspace.setOptions(projectOptions);
 
         // Ensure that the root is appropriate (it might be wrong if the user
         // created it, or if we previously built on another node).
 
         String localPath = getLocalPathName(workspace, launcher.isUnix());
         if (!localPath.equals(p4workspace.getRoot()) && !dontChangeRoot) {
             log.println("Changing P4 Client Root to: " + localPath);
             p4workspace.setRoot(localPath);
         }
 
         // If necessary, rewrite the views field in the clientspec;
 
         // TODO If dontRenameClient==false, and updateView==false, user
         // has a lot of work to do to maintain the clientspecs.  Seems like
         // we could copy from a master clientspec to the slaves.
 
         if (updateView || creatingNewWorkspace) {
             List<String> mappingPairs = parseProjectPath(projectPath, p4Client);
             if (!equalsProjectPath(mappingPairs, p4workspace.getViews())) {
                 log.println("Changing P4 Client View from:\n" + p4workspace.getViewsAsString());
                 log.println("Changing P4 Client View to: ");
                 p4workspace.clearViews();
                 for (int i = 0; i < mappingPairs.size(); ) {
                     String depotPath = mappingPairs.get(i++);
                     String clientPath = mappingPairs.get(i++);
                     p4workspace.addView(" " + depotPath + " " + clientPath);
                     log.println("  " + depotPath + " " + clientPath);
                 }
             }
         }
 
         // If we use the same client on multiple hosts (e.g. master and slave),
         // erase the host field so the client isn't tied to a single host.
         if (dontRenameClient) {
             p4workspace.setHost("");
         }
 
         // NOTE: The workspace is not saved.
         return p4workspace;
     }
 
     private String getEffectiveClientName(AbstractBuild build) {
         Node buildNode = build.getBuiltOn();
         FilePath workspace = build.getWorkspace();
         String p4Client = this.p4Client;
         try {
             p4Client = getEffectiveClientName(buildNode, workspace);
         } catch (Exception e){
             new StreamTaskListener(System.out).getLogger().println(
                     "Could not get effective client name: " + e.getMessage());
         } finally {
             return p4Client;
         }
     }
 
     private String getEffectiveClientName(Node buildNode, FilePath workspace)
             throws IOException, InterruptedException {
 
         String nodeSuffix = "";
         String p4Client = this.p4Client;
 
         if (nodeIsRemote(buildNode) && !dontRenameClient) {
             //use the 1st part of the hostname as the node suffix
             String host = workspace.act(new GetHostname());
             if (host.contains(".")) {
                 nodeSuffix = "-" + host.subSequence(0, host.indexOf('.'));
             } else {
                 nodeSuffix = "-" + host;
             }
             p4Client += nodeSuffix;
         }
         return p4Client;
     }
 
 
     private boolean nodeIsRemote(Node buildNode) {
         return buildNode != null && buildNode.getNodeName().length() != 0;
     }
 
     @Extension
     public static final class PerforceSCMDescriptor extends SCMDescriptor<PerforceSCM> {
         public PerforceSCMDescriptor() {
             super(PerforceSCM.class, PerforceRepositoryBrowser.class);
             load();
         }
 
         public String getDisplayName() {
             return "Perforce";
         }
 
         public String isValidProjectPath(String path) {
             if (!path.startsWith("//")) {
                 return "Path must start with '//' (Example: //depot/ProjectName/...)";
             }
             if (!path.endsWith("/...")) {
                 if (!path.contains("@")) {
                     return "Path must end with Perforce wildcard: '/...'  (Example: //depot/ProjectName/...)";
                 }
             }
             return null;
         }
 
         protected Depot getDepotFromRequest(StaplerRequest request) {
             String port = fixNull(request.getParameter("port")).trim();
             String exe = fixNull(request.getParameter("exe")).trim();
             String user = fixNull(request.getParameter("user")).trim();
             String pass = fixNull(request.getParameter("pass")).trim();
 
             if (port.length() == 0 || exe.length() == 0) { // Not enough entered yet
                 return null;
             }
             Depot depot = new Depot();
             depot.setUser(user);
             PerforcePasswordEncryptor encryptor = new PerforcePasswordEncryptor();
             if (encryptor.appearsToBeAnEncryptedPassword(pass)) {
                 depot.setPassword(encryptor.decryptString(pass));
             }
             else {
                 depot.setPassword(pass);
             }
             depot.setPort(port);
             depot.setExecutable(exe);
             try {
                 Counter counter = depot.getCounters().getCounter("change");
                 if (counter != null)
                     return depot;
             } catch (PerforceException e) {
             }
 
             return null;
         }
 
         /**
          * Checks if the perforce login credentials are good.
          */
         public FormValidation doValidatePerforceLogin(StaplerRequest req) {
             Depot depot = getDepotFromRequest(req);
             if (depot != null) {
                 try {
                     depot.getStatus().isValid();
                 } catch (PerforceException e) {
                     return FormValidation.error(e.getMessage());
                 }
             }
             return FormValidation.ok();
         }
 
         /**
          * Checks to see if the specified workspace is valid.
          */
         public FormValidation doValidateP4Client(StaplerRequest req) {
             Depot depot = getDepotFromRequest(req);
             if (depot == null) {
                 return FormValidation.error(
                         "Unable to check workspace against depot");
             }
             String workspace = Util.fixEmptyAndTrim(req.getParameter("client"));
             if (workspace == null) {
                 return FormValidation.error("You must enter a workspaces name");
             }
             try {
                 Workspace p4Workspace =
                     depot.getWorkspaces().getWorkspace(workspace);
 
                 if (p4Workspace.getAccess() == null ||
                         p4Workspace.getAccess().equals(""))
                     return FormValidation.warning("Workspace does not exist. " +
                             "If \"Let Hudson Manage Workspace View\" is check" +
                             " the workspace will be automatically created.");
             } catch (PerforceException e) {
                 return FormValidation.error(
                         "Error accessing perforce while checking workspace");
             }
 
             return FormValidation.ok();
         }
 
         /**
          * Performs syntactical check on the P4Label
           */
         public FormValidation doValidateP4Label(StaplerRequest req, @QueryParameter String label) throws IOException, ServletException {
             label = Util.fixEmptyAndTrim(label);
             if (label == null)
                 return FormValidation.ok();
 
             Depot depot = getDepotFromRequest(req);
             if (depot != null) {
                 try {
                     Label p4Label = depot.getLabels().getLabel(label);
                     if (p4Label.getAccess() == null || p4Label.getAccess().equals(""))
                         return FormValidation.error("Label does not exist");
                 } catch (PerforceException e) {
                     return FormValidation.error(
                             "Error accessing perforce while checking label");
                 }
             }
             return FormValidation.ok();
         }
 
         /**
          * Performs syntactical and permissions check on the P4Counter
           */
         public FormValidation doValidateP4Counter(StaplerRequest req, @QueryParameter String counter) throws IOException, ServletException {
             counter= Util.fixEmptyAndTrim(counter);
             if (counter == null)
                 return FormValidation.ok();
 
             Depot depot = getDepotFromRequest(req);
             if (depot != null) {
                 try {
                     Counters counters = depot.getCounters();
                     Counter p4Counter = counters.getCounter(counter);
                     // try setting the counter back to the same value to verify permissions
                     counters.saveCounter(p4Counter);
                 } catch (PerforceException e) {
                     return FormValidation.error(
                             "Error accessing perforce while checking counter: " + e.getLocalizedMessage());
                 }
             }
             return FormValidation.ok();
         }
 
         /**
          * Checks if the value is a valid Perforce project path.
          */
         public FormValidation doCheckProjectPath(@QueryParameter String value) throws IOException, ServletException {
             String view = Util.fixEmptyAndTrim(value);
             if (view != null) {
                 for (String mapping : view.split("\n")) {
                     if (!DEPOT_ONLY.matcher(mapping).matches() &&
                         !DEPOT_AND_WORKSPACE.matcher(mapping).matches() &&
                         !DEPOT_ONLY_QUOTED.matcher(mapping).matches() &&
                         !DEPOT_AND_WORKSPACE_QUOTED.matcher(mapping).matches() &&
                         !COMMENT.matcher(mapping).matches())
                         return FormValidation.error("Invalid mapping:" + mapping);
                 }
             }
             return FormValidation.ok();
         }
 
         /**
          * Checks if the change list entered exists
          */
         public FormValidation doCheckChangeList(StaplerRequest req) {
             Depot depot = getDepotFromRequest(req);
             String change = fixNull(req.getParameter("change")).trim();
 
             if (change.length() == 0) { // nothing entered yet
                 return FormValidation.ok();
             }
             if (depot != null) {
                 try {
                     int number = Integer.parseInt(change);
                     Changelist changelist = depot.getChanges().getChangelist(number);
                     if (changelist.getChangeNumber() != number)
                         throw new PerforceException("broken");
                 } catch (Exception e) {
                     return FormValidation.error("Changelist: " + change + " does not exist.");
                 }
             }
             return FormValidation.ok();
         }
     }
 
     /* Regular expressions for parsing view mappings.
      */
     private static final Pattern COMMENT = Pattern.compile("^$|^#.*$");
     private static final Pattern DEPOT_ONLY = Pattern.compile("^[+-]?//\\S+?(/\\S+)$");
     private static final Pattern DEPOT_ONLY_QUOTED = Pattern.compile("^\"[+-]?//\\S+?(/[^\"]+)\"$");
     private static final Pattern DEPOT_AND_WORKSPACE =
             Pattern.compile("^([+-]?//\\S+?/\\S+)\\s+//\\S+?(/\\S+)$");
     private static final Pattern DEPOT_AND_WORKSPACE_QUOTED =
             Pattern.compile("^\"([+-]?//\\S+?/[^\"]+)\"\\s+\"//\\S+?(/[^\"]+)\"$");
 
     /**
      * Parses the projectPath into a list of pairs of strings representing the depot and client
      * paths. Even items are depot and odd items are client.
      * <p>
      * This parser can handle quoted or non-quoted mappings, normal two-part mappings, or one-part
      * mappings with an implied right part. It can also deal with +// or -// mapping forms.
      */
     static List<String> parseProjectPath(String projectPath, String p4Client) {
         List<String> parsed = new ArrayList<String>();
         for (String line : projectPath.split("\n")) {
             Matcher depotOnly = DEPOT_ONLY.matcher(line);
             if (depotOnly.find()) {
                 // add the trimmed depot path, plus a manufactured client path
                 parsed.add(line.trim());
                 parsed.add("//" + p4Client + depotOnly.group(1));
             } else {
                 Matcher depotOnlyQuoted = DEPOT_ONLY_QUOTED.matcher(line);
                 if (depotOnlyQuoted.find()) {
                     // add the trimmed quoted depot path, plus a manufactured quoted client path
                     parsed.add(line.trim());
                     parsed.add("\"//" + p4Client + depotOnlyQuoted.group(1) + "\"");
                 } else {
                     Matcher depotAndWorkspace = DEPOT_AND_WORKSPACE.matcher(line);
                     if (depotAndWorkspace.find()) {
                         // add the found depot path and the clientname-tweaked client path
                         parsed.add(depotAndWorkspace.group(1));
                         parsed.add("//" + p4Client + depotAndWorkspace.group(2));
                     } else {
                         Matcher depotAndWorkspaceQuoted = DEPOT_AND_WORKSPACE_QUOTED.matcher(line);
                         if (depotAndWorkspaceQuoted.find()) {
                            // add the found depot path and the clientname-tweaked client path
                             parsed.add("\"" + depotAndWorkspaceQuoted.group(1) + "\"");
                             parsed.add("\"//" + p4Client + depotAndWorkspaceQuoted.group(2) + "\"");
                         }
                         // Assume anything else is a comment and ignore it
                     }
                 }
             }
         }
         return parsed;
     }
 
     /**
      * Compares a parsed project path pair list against a list of view
      * mapping lines from a client spec.
      */
      static boolean equalsProjectPath(List<String> pairs, List<String> lines) {
         Iterator<String> pi = pairs.iterator();
         for (String line : lines) {
             if (!pi.hasNext())
                 return false;
             String p1 = pi.next();
             String p2 = pi.next();  // assuming an even number of pair items
             if (!line.trim().equals(p1 + " " + p2))
                 return false;
         }
         return !pi.hasNext(); // equals iff there are no more pairs
     }
 
     /**
      * @return the projectPath
      */
     public String getProjectPath() {
         return projectPath;
     }
 
     /**
      * @param projectPath the projectPath to set
      */
     public void setProjectPath(String projectPath) {
         this.projectPath = projectPath;
     }
 
     /**
      * @return the p4User
      */
     public String getP4User() {
         return p4User;
     }
 
     /**
      * @param user the p4User to set
      */
     public void setP4User(String user) {
         p4User = user;
     }
 
     /**
      * @return the p4Passwd
      */
     public String getP4Passwd() {
         return p4Passwd;
     }
 
     /**
      * @param passwd the p4Passwd to set
      */
     public void setP4Passwd(String passwd) {
         PerforcePasswordEncryptor encryptor = new PerforcePasswordEncryptor();
         if (encryptor.appearsToBeAnEncryptedPassword(passwd))
             p4Passwd = passwd;
         else
             p4Passwd = encryptor.encryptString(passwd);
     }
 
     /**
      * @return the p4Port
      */
     public String getP4Port() {
         return p4Port;
     }
 
     /**
      * @param port the p4Port to set
      */
     public void setP4Port(String port) {
         p4Port = port;
     }
 
     /**
      * @return the p4Client
      */
     public String getP4Client() {
         return p4Client;
     }
 
     /**
      * @param client the p4Client to set
      */
     public void setP4Client(String client) {
         p4Client = client;
     }
 
     /**
      * @return the p4SysDrive
      */
     public String getP4SysDrive() {
         return p4SysDrive;
     }
 
     /**
      * @param sysDrive the p4SysDrive to set
      */
     public void setP4SysDrive(String sysDrive) {
         p4SysDrive = sysDrive;
     }
 
     /**
      * @return the p4SysRoot
      */
     public String getP4SysRoot() {
         return p4SysRoot;
     }
 
     /**
      * @param sysRoot the p4SysRoot to set
      */
     public void setP4SysRoot(String sysRoot) {
         p4SysRoot = sysRoot;
     }
 
     /**
      * @return the p4Exe
      */
     public String getP4Exe() {
         return p4Exe;
     }
 
     /**
      * @param exe the p4Exe to set
      */
     public void setP4Exe(String exe) {
         p4Exe = exe;
     }
 
     /**
      * @return the p4Label
      */
     public String getP4Label() {
         return p4Label;
     }
 
     /**
      * @param label the p4Label to set
      */
     public void setP4Label(String label) {
         p4Label = label;
     }
 
     /**
      * @return the p4Counter
      */
     public String getP4Counter() {
         return p4Counter;
     }
 
     /**
      * @param counter the p4Counter to set
      */
     public void setP4Counter(String counter) {
         p4Counter = counter;
     }
 
     /**
      * @return True if the plugin should update the counter to the last change
      */
     public boolean isUpdateCounterValue() {
         return updateCounterValue;
     }
 
     /**
      * @param updateCounterValue True if the plugin should update the counter to the last change
      */
     public void setUpdateCounterValue(boolean updateCounterValue) {
         this.updateCounterValue = updateCounterValue;
     }
 
     /**
      * @return True if the P4PASSWD value must be set in the environment
      */
     public boolean isExposeP4Passwd() {
         return exposeP4Passwd;
     }
 
     /**
      * @param exposeP4Passwd True if the P4PASSWD value must be set in the environment
      */
     public void setExposeP4Passwd(boolean exposeP4Passwd) {
         this.exposeP4Passwd = exposeP4Passwd;
     }
 
     /**
      * The current perforce option set for the view.
      * @return current perforce view options
      */
     public String getProjectOptions() {
         return projectOptions;
     }
 
     /**
      * Set the perforce options for view creation.
      * @param projectOptions the effective perforce options.
      */
     public void setProjectOptions(String projectOptions) {
         this.projectOptions = projectOptions;
     }
 
     /**
      * @param update    True to let the plugin manage the view, false to let the user manage it
      */
     public void setUpdateView(boolean update) {
         this.updateView = update;
     }
 
     /**
      * @return  True if the plugin manages the view, false if the user does.
      */
     public boolean isUpdateView() {
         return updateView;
     }
 
     /**
      * @return  True if we are performing a one-time force sync
      */
     public boolean isForceSync() {
         return forceSync;
     }
 
     /**
      * @param force True to perform a one time force sync, false to perform normal sync
      */
     public void setForceSync(boolean force) {
         this.forceSync = force;
     }
 
     /**
      * @return  True if we are using a label
      */
     public boolean isUseLabel() {
         return p4Label != null;
     }
 
     /**
      * @param dontRenameClient  False if the client will rename the client spec for each
      * slave
      */
     public void setDontRenameClient(boolean dontRenameClient) {
         this.dontRenameClient = dontRenameClient;
     }
 
     /**
      * @return  True if the client will rename the client spec for each slave
      */
     public boolean isDontRenameClient() {
         return dontRenameClient;
     }
 
     /**
      * This is only for the config screen.  Also, it returns a string and not an int.
      * This is because we want to show an empty value in the config option if it is not being
      * used.  The default value of -1 is not exactly empty.  So if we are set to default of
      * -1, we return an empty string.  Anything else and we return the actual change number.
      *
      * @return  The one time use variable, firstChange.
      */
     public String getFirstChange() {
         if (firstChange <= 0)
             return "";
         return Integer.valueOf(firstChange).toString();
     }
 
     /**
      * Get the hostname of the client to use as the node suffix
      */
     private static final class GetHostname implements FileCallable<String> {
         public String invoke(File f, VirtualChannel channel) throws IOException {
             return InetAddress.getLocalHost().getHostName();
         }
         private static final long serialVersionUID = 1L;
     }
 
     /**
      * With Perforce the server keeps track of files in the workspace.  We never
      * want files deleted without the knowledge of the server so we disable the
      * cleanup process.
      *
      * @param project
      *      The project that owns this {@link SCM}. This is always the same
      *      object for a particular instanceof {@link SCM}. Just passed in here
      *      so that {@link SCM} itself doesn't have to remember the value.
      * @param workspace
      *      The workspace which is about to be deleted. Never null. This can be
      *      a remote file path.
      * @param node
      *      The node that hosts the workspace. SCM can use this information to
      *      determine the course of action.
      *
      * @return
      *      true if {@link SCM} is OK to let Hudson proceed with deleting the
      *      workspace.
      *      False to veto the workspace deletion.
      */
     @Override
     public boolean processWorkspaceBeforeDeletion(AbstractProject<?,?> project, FilePath workspace, Node node) {
         Logger.getLogger(PerforceSCM.class.getName()).info(
                 "Veto workspace cleanup");
         return false;
     }
 
     @Override public boolean requiresWorkspaceForPolling() {
        return true;
     }
 
 }
