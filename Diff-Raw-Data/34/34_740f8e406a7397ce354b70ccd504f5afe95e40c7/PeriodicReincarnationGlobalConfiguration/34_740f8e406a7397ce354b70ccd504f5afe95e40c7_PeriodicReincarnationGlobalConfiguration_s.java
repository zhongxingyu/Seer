 package org.jenkinsci.plugins.periodicreincarnation;
 
 import java.util.List;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import hudson.AbortException;
 import hudson.Extension;
 import hudson.scheduler.CronTab;
 import hudson.util.FormValidation;
 import net.sf.json.JSONObject;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 
 import antlr.ANTLRException;
 
 import jenkins.model.GlobalConfiguration;
 
 /**
  * Implements the interface GlobalConfiguration from Jenkins. Gives the option
  * to configure how this plugin works, what should be restarted, when it should
  * be restarted and what should appear in the log file. With the help of the
  * load() and save() method the configuration is loaded when instancing this
  * class and saved when changing it.
  * 
  * @author yboev
  * 
  */
 @Extension
 public class PeriodicReincarnationGlobalConfiguration extends
         GlobalConfiguration {
 
     /**
      * Shows if the cron restart of failed jobs is active or disabled.
      */
     private String activeCron;
     /**
      * Shows if the afterbuild restart of failed jobs is active or disabled.
      */
     private String activeTrigger;
     /**
      * Contains the cron time set in the configuration.
      */
     private String cronTime;
     /**
      * List of all regular expressions.
      */
     private List<RegEx> regExprs;
     /**
      * maximal restart depth for afterbuild restarts.
      */
     private String maxDepth;
     /**
      * Shows if info should be printed to the log or not.
      */
     // private String logInfo;
     /**
      * Shows if the option to restart jobs that have failed in their last build
      * but succeeded in their second last and have no change between these two
      * build should be restarted.
      */
     private String noChange;
 
     /**
      * Constructor. Loads the configuration upon invoke.
      */
     public PeriodicReincarnationGlobalConfiguration() {
         load();
     }
 
     /**
      * Constructor. DataBound because this constructor is used to populate
      * values entered from the user.
      * 
      * @param active
      *            shows if plugin is enabled disabled.
      * @param cronTime
      *            contains the cron time.
      * @param regExprs
      *            list of all regular expressions.
      * @param logInfo
      *            shows if log info is enabled
      * @param noChange
      *            shows if no change option is enabled or disabled.
      */
     @DataBoundConstructor
     public PeriodicReincarnationGlobalConfiguration(String activeTrigger,
             String maxDepth, String activeCron, String cronTime,
             List<RegEx> regExprs, String noChange) {
         this.activeTrigger = activeTrigger;
         this.maxDepth = maxDepth;
         this.activeCron = activeCron;
         this.cronTime = cronTime;
         this.regExprs = regExprs;
         // this.logInfo = logInfo;
         this.noChange = noChange;
     }
 
     /**
      * {@inheritDoc} This method defines what happens when we save to
      * configuration. All values are taken from the json file and set to the
      * variabled in this class.
      */
     @Override
     public boolean configure(StaplerRequest req, JSONObject json)
             throws FormException {
         this.regExprs = req.bindJSON(
                 PeriodicReincarnationGlobalConfiguration.class, json).regExprs;
         this.activeTrigger = json.getString("activeTrigger").trim();
         this.maxDepth = json.getString("maxDepth").trim();
         this.activeCron = json.getString("activeCron").trim();
         this.cronTime = json.getString("cronTime");
         // this.logInfo = json.getString("logInfo");
         this.noChange = json.getString("noChange");
         save();
         return true;
     }
 
     /**
      * Check method for the cron time. Returns an error message if the cron time
      * was invalid.
      * 
      * @return Returns ok if cron time was correct, error message otherwise.
      */
    public FormValidation doCheckCronTime() throws ANTLRException, NullPointerException {
         try {
             new CronTab(cronTime);
             return FormValidation.ok();
         } catch (ANTLRException e) {
             return FormValidation
                     .error("Cron time could not be parsed. Please check for type errors!");
         } catch (NullPointerException e) {
             return FormValidation.error("Cron time was null");
         }
     }
 
     /**
      * Finds and returns the configuration class.
      * 
      * @return the ReincarnateFailedJobsConfiguration.
      */
     public static PeriodicReincarnationGlobalConfiguration get() {
         return GlobalConfiguration.all().get(
                 PeriodicReincarnationGlobalConfiguration.class);
     }
 
     /**
      * Returns the field noChange.
      * 
      * @return the field noChange.
      */
     public String getNoChange() {
         return this.noChange;
     }
 
     /**
      * See {@code noChange}.
      * 
      * @return true if the option is enabled, false otherwise.
      */
     public boolean isRestartUnchangedJobsEnabled() {
         return (this.noChange != null && this.noChange.equals("true"));
     }
 
     /**
      * Returns the field logInfo.
      * 
      * @return logInfo.
      */
     /*
      * public String getLogInfo() { return this.logInfo; }
      */
 
     /**
      * Tells if printing in log is enabled or not.
      * 
      * @return true if enabled, false otherwise.
      */
     /*
      * public boolean isLogInfoEnabled() { return (this.logInfo != null &&
      * this.logInfo.equals("true")); }
      */
 
     /**
      * Returns a list containing all regular expressions.
      * 
      * @return the list with reg exs.
      */
     public List<RegEx> getRegExprs() {
         return this.regExprs;
     }
 
     /**
      * Returns the field cron time.
      * 
      * @return cronTime.
      */
     public String getCronTime() {
         return this.cronTime;
     }
 
     /**
      * Returns the field activeCron.
      * 
      * @return activeCron.
      */
     public String getActiveCron() {
         return this.activeCron;
     }
 
     /**
      * Tells if the cron restart is activated or not.
      * 
      * @return true if activated, false otherwise.
      */
     public boolean isCronActive() {
         return (this.activeCron != null && this.activeCron.equals("true"));
     }
 
     /**
      * Returns the field activeTrigger.
      * 
      * @return activeTrigger.
      */
     public String getActiveTrigger() {
         return this.activeTrigger;
     }
 
     /**
      * Tells if the afterbuild restart is activated or not.
      * 
      * @return true if activated, false otherwise.
      */
     public boolean isTriggerActive() {
         return (this.activeTrigger != null && this.activeTrigger.equals("true"));
     }
 
     /**
      * returns the maximal number of consecutive afterbuild retries.
      * 
      * @return the number from the configuration as String
      */
     public int getMaxDepth() {
         try {
             Integer.parseInt(this.maxDepth);
         } catch (NumberFormatException e) {
             this.maxDepth = "0";
         }
         return Integer.parseInt(this.maxDepth);
     }
 
     /**
      * Class for handling regular expressions.
      * 
      * @author yboev
      * 
      */
     public static class RegEx {
         /**
          * Value of the reg ex as String.
          */
         private String value;
 
         private String nodeAction;
 
         private String masterAction;
 
         /**
          * Constructor. Creates a reg ex.
          * 
          * @param value
          *            the reg ex.
          * @param nodeAction
          * @param masterAction
          */
         @DataBoundConstructor
         public RegEx(String value, String nodeAction, String masterAction) {
             this.value = value;
             this.nodeAction = nodeAction;
             this.masterAction = masterAction;
         }
 
         /**
          * Returns this reg ex.
          * 
          * @return the reg ex value.
          */
         public String getValue() {
             return this.value;
         }
 
         /**
          * Returns the pattern correspondin to this reg ex.
          * 
          * @return the pattern.
          * @throws AbortException
          *             if the pattern could not be compiled.
          */
         public Pattern getPattern() throws AbortException {
             Pattern pattern;
             try {
                 pattern = Pattern.compile(this.value);
             } catch (PatternSyntaxException e) {
                throw new AbortException();
             }
             return pattern;
         }
 
         public String getNodeAction() {
             return nodeAction;
         }
 
         public String getMasterAction() {
             return masterAction;
         }
     }
 
 }
