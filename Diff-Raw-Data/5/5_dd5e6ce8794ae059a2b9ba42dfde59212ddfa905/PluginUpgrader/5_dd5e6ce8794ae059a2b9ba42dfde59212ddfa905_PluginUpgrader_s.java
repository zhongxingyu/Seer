 package com.atlassian.sal.core.upgrade;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import com.atlassian.plugin.Plugin;
 import com.atlassian.sal.api.message.Message;
 import com.atlassian.sal.api.pluginsettings.PluginSettings;
 import com.atlassian.sal.api.upgrade.PluginUpgradeTask;
 import com.atlassian.sal.core.message.DefaultMessage;
 
 /**
  * Performs an upgrade of a plugin.  Originally copied from Confluence's AbstractUpgradeManager.
  */
 public class PluginUpgrader
 {
 
     public static final String BUILD = ":build";
     protected List<PluginUpgradeTask> upgradeTasks = new ArrayList<PluginUpgradeTask>();
     private static final Logger log = Logger.getLogger(PluginUpgrader.class);
     protected Plugin plugin;
     protected PluginSettings pluginSettings;
     protected List<Message> errors = new ArrayList<Message>();
 
     private static final Comparator<PluginUpgradeTask> UPGRADE_TASK_COMPARATOR = new Comparator<PluginUpgradeTask>()
     {
         public int compare(PluginUpgradeTask t1, PluginUpgradeTask t2)
         {
             if (t1 == null)
             {
                 return -1;
             }
             if (t2 == null)
             {
                 return 1;
             }
             if (t1.getBuildNumber() > t2.getBuildNumber())
             {
                 return 1;
             }
 
             return (t1.getBuildNumber() < t2.getBuildNumber()) ? -1 : 0;
         }
     };
 
     protected PluginUpgrader(Plugin plugin, PluginSettings pluginSettings, List<PluginUpgradeTask> upgradeTasks)
     {
         this.plugin = plugin;
         this.pluginSettings = pluginSettings;
         this.upgradeTasks = upgradeTasks;
         Collections.sort(this.upgradeTasks, UPGRADE_TASK_COMPARATOR);
     }
 
     protected List<Message> upgrade()
     {
     	if (needUpgrade())
         {
         	doUpgrade();
         } 
         return errors;
     }
 
     protected void doUpgrade()
     {
         try
         {
             log.info("Upgrading plugin " + plugin.getKey());
             for (PluginUpgradeTask upgradeTask : upgradeTasks)
             {
 
                 if (upgradeTask.getBuildNumber() <= getDataBuildNumber())
                 {
                     // Current buildnumber of data is higher or equal to this upgrade task. No need to run
                     continue;
                 }
 
                 Collection<Message> messages = upgradeTask.doUpgrade();
 
                 if (messages == null || messages.isEmpty())
                 {
                     upgradeTaskSucceeded(upgradeTask);
                 }
                 else
                 {
                     upgradeTaskFailed(upgradeTask, messages);
                 }
             }
         }
         catch (Throwable e)
         {
             errors.add(new DefaultMessage("upgrade.unexpected.exception", e));
             log.error("Upgrade failed: " + e.getMessage(), e);
         }
         finally
         {
             postUpgrade();
         }
     }
 
 
     protected void upgradeTaskSucceeded(PluginUpgradeTask upgradeTask)
     {
         setDataBuildNumber(upgradeTask.getBuildNumber());
         log.info("Upgraded plugin " + upgradeTask.getPluginKey() + " to version " + upgradeTask.getBuildNumber() + " - " + upgradeTask.getShortDescription());
     }
 
     protected void upgradeTaskFailed(PluginUpgradeTask upgradeTask, Collection<Message> messages)
     {
         errors.addAll(messages);
         StringBuilder msg = new StringBuilder();
         msg.append("Plugin upgrade failed for ").append(upgradeTask.getPluginKey());
         msg.append(" to version ").append(upgradeTask.getBuildNumber());
         msg.append(" - ").append(upgradeTask.getShortDescription());
         msg.append("\n");
         for (Message message : messages)
         {
             msg.append("\t* ").append(message.getKey()).append(" ").append(Arrays.toString(message.getArguments()));
         }
 
         log.warn(msg.toString());
     }
 
     protected List<Message> getErrors()
     {
         return errors;
     }
 
     protected boolean needUpgrade()
     {
     	final PluginUpgradeTask lastUpgradeTask = this.upgradeTasks.get(this.upgradeTasks.size()-1);
    	log.info("Plugin: " +plugin.getKey() + ", current version: " + getDataBuildNumber() + ", highest upgrade task found: " + lastUpgradeTask.getBuildNumber() + ".");
    	return lastUpgradeTask.getBuildNumber() > getDataBuildNumber();
     }
 
 
     /**
      * This is the build number of the current version that the user is running under.
      */
     protected int getDataBuildNumber()
     {
         String val = (String) pluginSettings.get(plugin.getKey() + BUILD);
         if (val != null)
         {
             return Integer.parseInt(val);
         }
         else
         {
             return 0;
         }
     }
 
     protected void setDataBuildNumber(int buildNumber)
     {
         pluginSettings.put(plugin.getKey() + BUILD, String.valueOf(buildNumber));
     }
 
     protected void postUpgrade()
     {
         log.info("Plugin " + plugin.getKey() + " upgrade completed. Current version is: "+ getDataBuildNumber());
     }
 }
