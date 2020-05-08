 package org.jenkinsci.plugins;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import hudson.model.*;
 import hudson.util.RemotingDiagnostics;
 import jenkins.model.Jenkins;
 import org.jenkinsci.plugins.ReincarnateFailedJobsConfiguration.RegEx;
 
 import antlr.ANTLRException;
 
 import hudson.AbortException;
 import hudson.Extension;
 import hudson.scheduler.CronTab;
 import hudson.util.IOUtils;
 import hudson.util.RunList;
 
 /**
  * Main class of the periodic reincarnation plug-in. Method execute is called
  * every minute but further functionality and restart of failed jobs happens
  * only when the time specified in the cron tab overlaps with the current
  * minute.
  *
  * @author yboev
  *
  */
 @Extension
 public class PeriodicReincarnation extends AsyncPeriodicWork {
 
     /**
      * Logger for PeriodicReincarnation.
      */
     private static final Logger LOGGER = Logger
             .getLogger(PeriodicReincarnation.class.getName());
 
     /**
      * Constructor.
      */
     public PeriodicReincarnation() {
         super("PeriodicReincarnation");
     }
 
     /**
      * This method is called every minute.
      *@param taskListener TaskListener 
      */
     @Override
     protected void execute(TaskListener taskListener) {
         final ReincarnateFailedJobsConfiguration config = ReincarnateFailedJobsConfiguration
                 .get();
         if (config == null) {
             LOGGER.info("No configuration available.");
             return;
         }
 
         final String cron = config.getCronTime();
         final boolean isActive = config.isActive();
         if (!isActive) {
             return;
         }
         if (cron != null) {
             try {
 
                 final CronTab cronTab = new CronTab(cron);
                 final long currentTime = System.currentTimeMillis();
                 RegEx regEx;
 
                 if ((cronTab.ceil(currentTime).getTimeInMillis() - currentTime) == 0
                         && isActive) {
 
                     final List<?> projectList = Hudson.getInstance()
                             .getProjects();
                     for (final Iterator<?> i = projectList.iterator(); i
                             .hasNext();) {
                         final Project<?, ?> project = (Project<?, ?>) i.next();
                         //TODO: jeff race condition with last build
                         if (project != null
                                 && project instanceof BuildableItem
                                 && project.getLastBuild() != null
                                 && project.getLastBuild().getResult() != null
                                 && project.getLastBuild().getResult()
                                 .isWorseOrEqualTo(Result.FAILURE)
                                 && !project.isBuilding()
                                 && !project.isInQueue())
                         {
                             regEx = checkBuild(project.getLastBuild());
                             if (regEx != null) {
                                 this.restart(project, config, "Restarting due to this regex: " + regEx.getValue()
                                         + " was found in the build output");
                                 this.execAction(project, config, regEx.getNodeAction(), regEx.getMasterAction());
                             } else if (config.isRestartUnchangedJobsEnabled()
                                     && qualifyForUnchangedRestart(project)) {
                                 this.restart(project, config,
                                         "no difference between last builds");
                             }
                         }
 
                     }
                 }
 
             } catch (ANTLRException e) {
                 LOGGER.warning("Could not parse the given cron tab. Check for type errors: "
                         + e.getMessage());
             } catch (InterruptedException e) {
                 LOGGER.warning("Could not parse the given cron tab. Check for type errors: "
                         + e.getMessage());
             } catch (IOException e) {
                 LOGGER.warning("Could not parse the given cron tab. Check for type errors: "
                         + e.getMessage());
             }
         } else {
             LOGGER.warning("Cron time is not configured.");
         }
     }
 
 
     private void execAction(Project<?, ?> project, ReincarnateFailedJobsConfiguration config, String nodeAction,
                             String masterAction) throws IOException, InterruptedException
     {
         Node node = project.getLastBuild().getBuiltOn();
         Computer slave = node.toComputer();
 
         LOGGER.info("executing script " + nodeAction + " in node: " + slave.getName());
         try {
             RemotingDiagnostics.executeGroovy(nodeAction, slave.getChannel());
         } catch (IOException e) {
             String message = "Error: " + e.getMessage() + "there were problems executing script in "
                     + slave.getName()
                     + " script: " + nodeAction;
             LOGGER.warning(message);
         } catch (InterruptedException e) {
             String message = "Error: " + e.getMessage() + "there were problems executing script in "
                     + slave.getName()
                     + " script: " + nodeAction;
             LOGGER.warning(message);
         }
 
        masterAction = "slave_name = " + "'" + slave.getName() + "'" + "; \n" + masterAction;
        LOGGER.info("executing this script in master: \n " + masterAction + " in master.");
         try {
             RemotingDiagnostics.executeGroovy(masterAction, Jenkins.MasterComputer.localChannel);
         } catch (IOException e) {
             String message = "Error: " + e.getMessage() + "there were problems executing script in "
                     + "the master node. Script: " + masterAction;
             LOGGER.warning(message);
         } catch (InterruptedException e) {
             String message = "Error: " + e.getMessage() + "there were problems executing script in "
                     + "the master node. Script: " + masterAction;
             LOGGER.warning(message);
         }
     }
 
     /**
      * Recurrence will occur every minute, but action will be taken according to
      * the cron time set in the configuration. Only when this minute and the
      * cron tab time overlap.
      *
      * @return The recurrence period in ms.
      */
     @Override
     public long getRecurrencePeriod() {
         return MIN;
     }
 
     /**
      * Returns this AsyncTask.
      *
      * @return the instance of PeriodicReincarnation which is currently running
      */
     public static PeriodicReincarnation get() {
         return AsyncPeriodicWork.all().get(PeriodicReincarnation.class);
     }
 
     /**
      * Determines whether or not there were changes between two builds.
      *
      * @param build1
      *            First build.
      * @param build2
      *            Second build.
      * @return true if there is at least one chage, false otherwise.
      */
     private boolean changesBetweenTwoBuilds(Run<?, ?> build1, Run<?, ?> build2) {
         return ((AbstractBuild<?, ?>) build1).getChangeSet().equals(
                 ((AbstractBuild<?, ?>) build2).getChangeSet());
     }
 
     /**
      * If there were no changes between the last 2 builds of a project and the
      * last build failed but the previous didn't, then this project is being
      * restarted if this unchanged restart option is enabled.
      *
      * @param project
      *            the project.
      * @return true if it qualifies, false otherwise.
      */
     private boolean qualifyForUnchangedRestart(Project<?, ?> project) {
         // proves if a build WAS stable and if there are changes between the
         // last two builds.
         // WAS stable means: last build was worse or equal to FAILURE, second
         // last was better than FAILURE
         if (project.getBuilds() != null && project.getBuilds().size() >= 2) {
             final RunList<?> builds = project.getBuilds();
             final Run<?, ?> lastBuild = builds.getLastBuild();
             final Run<?, ?> secondLastBuild = builds.get(builds.size() - 2);
             if (lastBuild != null && lastBuild.getResult() != null
                     && lastBuild.getResult().isWorseOrEqualTo(Result.FAILURE)
                     && secondLastBuild != null
                     && secondLastBuild.getResult() != null
                     && secondLastBuild.getResult().isBetterThan(Result.FAILURE)
                     && !changesBetweenTwoBuilds(lastBuild, secondLastBuild)) {
                 // last build failed, but second one didn't and there were no
                 // changes between the two builds
                 // in this case we restart the build
                 return true;
             }
             // last build was not a failure or 2nd last was.
             // no restart
             return false;
         }
         // less than 2 builds
         // no restart
         return false;
     }
 
     /**
      * Helper method for restarting a project.
      *
      * @param project
      *            the project.
      * @param config
      *            instance of periodic reincarnation configuration.
      * @param cause
      *            the cause for the restart.
      */
     private void restart(Project<?, ?> project,
                          ReincarnateFailedJobsConfiguration config, String cause) {
         project.scheduleBuild(new ReincarnateFailedBuildsCause(cause));
         if (config.isLogInfoEnabled()) {
 
             LOGGER.info("Restarting failed build(" + cause + ")........"
                     + project.getDisplayName());
         }
     }
 
     /**
      * Checks if a certain build matches any of the given regular expressions.
      *
      * @param build
      *            the build.
      * @return RegEx object if at least one match, null otherwise.
      */
     private RegEx checkBuild(Run<?, ?> build) {
         final ReincarnateFailedJobsConfiguration config = new ReincarnateFailedJobsConfiguration();
         final List<RegEx> regExprs = config.getRegExprs();
         if (regExprs == null || regExprs.size() == 0) {
             return null;
         }
         for (final Iterator<RegEx> i = regExprs.iterator(); i.hasNext();) {
             final RegEx currentRegEx = i.next();
             try {
                 if (checkFile(build.getLogFile(), currentRegEx.getPattern(),
                         true)) {
                     return currentRegEx;
                 }
             } catch (AbortException e) {
                 e.printStackTrace();
             }
         }
         return null;
     }
 
     /**
      * Searches for a given pattern in a given file.
      *
      * @param file
      *            the current file being checked.
      * @param pattern
      *            the reg ex we are checking with.
      * @param abortAfterFirstHit
      *            normally true, can be set to false in order to continue
      *            searching.
      *
      * @return True if reg ex was found in the file, false otherwise.
      */
     private boolean checkFile(File file, Pattern pattern,
                               boolean abortAfterFirstHit) {
         if (pattern == null) {
             return false;
         }
         boolean rslt = false;
         BufferedReader reader = null;
         try {
             reader = new BufferedReader(new FileReader(file));
             String line;
             while ((line = reader.readLine()) != null) {
                 final Matcher matcher = pattern.matcher(line);
                 if (matcher.find()) {
                     // we have a hit
                     rslt = true;
                     if (abortAfterFirstHit) {
                         return true;
                     }
                 }
             }
         } catch (IOException e) {
             LOGGER.info("File could not be read!");
         } finally {
             IOUtils.closeQuietly(reader);
         }
         return rslt;
     }
 
 }
