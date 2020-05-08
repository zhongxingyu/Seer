 package com.cloudbees.sdk.commands;
 
 import com.cloudbees.sdk.CommandServiceImpl;
 import com.cloudbees.sdk.GAV;
 import com.cloudbees.sdk.cli.BeesCommand;
 import com.cloudbees.sdk.cli.CLICommand;
 import com.cloudbees.sdk.cli.CommandService;
 import com.cloudbees.sdk.utils.Helper;
 
 import javax.inject.Inject;
 
 /**
  * @author Fabian Donze
  */
@CLICommand("plugin:list")
@BeesCommand(group="SDK", description = "List CLI plugins")
 public class PluginDeleteCommand extends Command {
     private Boolean force;
 
     @Inject
     CommandService commandService;
 
     public void setForce(Boolean force) {
         this.force = force;
     }
 
     public PluginDeleteCommand() {
         setArgumentExpected(1);
     }
 
     @Override
     protected String getUsageMessage() {
         return "PLUGIN_NAME";
     }
 
     @Override
     protected boolean preParseCommandLine() {
         // add the Options
        addOption( "f", "force", false, "force update to newest version without prompting" );
 
         return true;
     }
 
     @Override
     protected boolean execute() throws Exception {
         String name = getParameters().get(0);
         if (force == null || !force.booleanValue()) {
             if (!Helper.promptMatches("Are you sure you want to delete this plugin [" + name + "]: (y/n) ", "[yY].*")) {
                 return true;
             }
         }
         CommandServiceImpl service = (CommandServiceImpl) commandService;
         GAV gav = service.deletePlugin(name);
         if (gav != null) {
             System.out.println("Plugin deleted: " + gav);
         } else {
             System.out.println("Plugin not found: " + name);
         }
         return true;
     }
 
 }
 
