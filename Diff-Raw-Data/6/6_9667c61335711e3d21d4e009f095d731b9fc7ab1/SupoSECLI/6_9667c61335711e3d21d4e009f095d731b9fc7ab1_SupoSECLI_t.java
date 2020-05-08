 package com.soebes.cli.cli_test;
 
 import java.io.IOException;
 import java.util.Properties;
 
 import org.apache.log4j.Logger;
 
 import com.beust.jcommander.ParameterException;
 
 public class SupoSECLI {
     private static Logger LOGGER = Logger.getLogger(SupoSECLI.class);
 
     private int returnCode = 0;
 
     public SupoSECLI() {
     }
 
     public void run(String[] args) {
         setReturnCode(0);
         SupoSECommands commands = null;
         try {
             commands = new SupoSECommands(args);
         } catch (ParameterException e) {
             LOGGER.warn("");
             LOGGER.warn("It looks like you used a wrong command or used wrong options or a combination of this.");
             LOGGER.warn("");
             LOGGER.warn("Message: " + e.getMessage());
             LOGGER.warn("");
             LOGGER.warn("To get help about all existing commands please type:");
             LOGGER.warn("");
             LOGGER.warn("    supose --help");
             LOGGER.warn("");
             LOGGER.warn("If you like to get help about a particular command:");
             LOGGER.warn("");
             LOGGER.warn("    supose command --help");
             LOGGER.warn("");
             return;
         }
 
         SupoSECommands.SupoSEExistingCommands command = commands.getCommand();
         if (commands.isHelpForCommand()) {
             commands.getCommander().usage(command.getCommandName());
             return;
         }
 
         if (commands.getMainCommand().isVersion()) {
             printVersion();
             return;
         }
 
         if (command == null || commands.getMainCommand().isHelp()
                 || (args.length == 0)) {
             commands.getCommander().usage();
             return;
         }
 
         switch (command) {
         case MERGE:
             mergeCommand(commands.getMergeCommand());
             break;
         case SCAN:
             scanCommand(commands.getScanCommand());
             break;
         case SEARCH:
             searchCommand(commands.getSearchCommand());
             break;
         default:
             LOGGER.error("Unknown command in switch.");
             setReturnCode(1);
             break;
         }
     }
 
     /**
     * This is the <code>supose --version</code>
      */
     private void printVersion() {
         Properties properties = new Properties();
         try {
            properties.load(this.getClass().getResourceAsStream("/version.properties"));
         } catch (IOException e) {
             LOGGER.error("Can't read the version.properties file.", e);
             return;
         }
         String version = properties.getProperty("version");
         String buildNumber = properties.getProperty("buildNumber");
         System.out.println("Version:" + version);
         System.out.println("Build Number:" + buildNumber);
     }
 
     public void setReturnCode(int returnCode) {
         this.returnCode = returnCode;
     }
 
     public int getReturnCode() {
         return returnCode;
     }
 
     private void mergeCommand(MergeCommand command) {
         System.out.println("The merge command");
     }
 
     private void scanCommand(ScanCommand command) {
         System.out.println("The scan command");
     }
 
     private void searchCommand(SearchCommand command) {
         System.out.println("The search command");
     }
 }
