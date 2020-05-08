 package org.amplafi.flow.utils;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.Properties;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import static org.amplafi.flow.utils.AdminToolCommandLineOptions.*;
 import org.amplafi.dsl.ParameterValidationException;
import org.amplafi.dsl.ParameterUsge;
 import org.amplafi.dsl.ScriptRunner;
 import org.amplafi.dsl.ScriptDescription;
 import org.amplafi.flow.definitions.FarReachesServiceInfo;
 import org.amplafi.json.JSONArray;
 import org.amplafi.json.JSONObject;
 import java.util.Map;
 
 /**
  * Command line interface for running scripts to communicate with the
  * Farreach.es wire server. Please read AdminTool.md
  * for more details
  */
 public class AdminTool extends UtilParent {
     private static final String AUTO_OBTAIN_KEY = "Auto obtain key";
     private static final String PUBLIC_API = "public";
     private boolean verbose = false;
 
     /**
      * Main entry point for tool.
      * @param args
      */
     public static void main(String[] args) {
         AdminTool adminTool = new AdminTool();
         for (String arg : args) {
             adminTool.getLog().debug("arg: " + arg);
         }
         adminTool.processCommandLine(args);
     }
 
     /**
      * Process command line.
      * @param args
      */
     public void processCommandLine(String[] args) {
         // Process command line options.
         AdminToolCommandLineOptions cmdOptions = null;
         try {
             cmdOptions = new AdminToolCommandLineOptions(args);
         } catch (ParseException e) {
             getLog().error("Could not parse passed arguments, message:", e);
             return;
         }
         // Print help if there has no args.
         if (args.length == 0) {
             cmdOptions.printHelp();
             return;
         }
 
 
         String list = cmdOptions.getOptionValue(LIST);
         // Obtain a list of script descriptions from the script runner
         // this will also check for basic script compilation errors or lack of
         // description lines in script.
         ScriptRunner runner = new ScriptRunner(null,null);
         Map<String, ScriptDescription> scriptLookup = getScriptLookup(runner);
         if (cmdOptions.hasOption(LIST) || cmdOptions.hasOption(LISTDETAILED)) {
             // If user has asked for a list of commands then list the good
             // scripts with their
             // descriptions.
             for (ScriptDescription sd : runner.getGoodScripts()) {
                 if (cmdOptions.hasOption(LIST)) {
                     emitOutput("     " + sd.getName() + "       - "
                             + sd.getDescription());
                 } else {
                     emitOutput("     " + sd.getName() + "       - "
                             + sd.getDescription() + "       - "
                             + getRelativePath(sd.getPath()));
                 }
             }
             // List scripts that have errors if there are any
             if (!runner.getScriptsWithErrors().isEmpty()) {
                 emitOutput("The following scripts have errors: ");
             }
             for (ScriptDescription sd : runner.getScriptsWithErrors()) {
                 emitOutput("  " + getRelativePath(sd.getPath()) + "       - "
                         + sd.getErrorMesg());
             }
         } else if (cmdOptions.hasOption(HELP)) {
             //  print usage if has option help
             if (args.length == 1) {
                 cmdOptions.printHelp();
             } else {
                 for (int i = 1; i < args.length; i++) {
                     String scriptName = args[i];
                     for (ScriptDescription sd : runner.getGoodScripts()) {
                         if (sd.getName().equals(scriptName)) {
                             printScriptUsage(sd, scriptName);
                             /**if (sd.getUsage() != null && !sd.getUsage().equals("")) {
                                 emitOutput("Script Usage: " + sd.getUsage());
                             } else {
                                 emitOutput("Script " + scriptName +
                                            " does not have usage information");
                             }*/
                         }
                     }
                 }
             }
         } else if (cmdOptions.hasOption(FILE_PATH)) {
             // run an ad-hoc script from a file
             String filePath = cmdOptions.getOptionValue(FILE_PATH);
             runScript(filePath, scriptLookup, cmdOptions);
         } else {
             runScript(null, scriptLookup, cmdOptions);
         }
         // If the config properties were loaded, save them here.
         saveProperties();
         return;
     }
 
     /**
      * Runs the named script.
      * @param filePath is the full path to the script
      * @param scriptLookup is the map of ScriptDescription
      * @param cmdOptions is instance of AdminToolCommandLineOptions
      */
     private void runScript(String filePath,
                            Map<String, ScriptDescription> scriptLookup,
                            AdminToolCommandLineOptions cmdOptions) {
         verbose = cmdOptions.hasOption(VERBOSE);
         List<String> remainder = cmdOptions.getRemainingOptions();
         try {
             // Get script options if needed
             String host = getOption(cmdOptions, HOST, DEFAULT_HOST);
             String port = getOption(cmdOptions, PORT, DEFAULT_PORT);
             String apiVersion = getOption(cmdOptions, API_VERSION,
                     DEFAULT_API_VERSION);
             final FarReachesServiceInfo service = new FarReachesServiceInfo(host, port, apiVersion);
 
             String key = getOption(cmdOptions, API_KEY, AUTO_OBTAIN_KEY);
 
             if (!PUBLIC_API.equals(apiVersion)){
 
                 if ( AUTO_OBTAIN_KEY.equals(key)){
                     key = getPermApiKey(service,null, verbose);
                 }
             } else {
                 key = null;
             }
 
             if (cmdOptions.hasOption(FLOWS)){
                 listFlows(cmdOptions,key,service);
                 return;
             }
 
             if (cmdOptions.hasOption(DESCRIBE)){
                 String flow = cmdOptions.getOptionValue(DESCRIBE);
                 descFlow(cmdOptions,key,flow,service);
                 return;
             }
 
             String scriptName = filePath;
             // Check if we are running and ad-hoc script
             if (filePath == null) {
                 if (!remainder.isEmpty()) {
                     scriptName = remainder.get(0);
                     if (scriptLookup.containsKey(scriptName)) {
                         ScriptDescription sd = scriptLookup.get(scriptName);
                         filePath = sd.getPath();
                     }
                     remainder.remove(0);
                 }
             }
             // Get the parameter for the script itself.
             Map<String, String> parammap = getParamMap(remainder);
             // Is verbose switched on?
             // run the script
 
             ScriptRunner runner2 = new ScriptRunner(service, key, parammap, verbose);
             runner2.setScriptLookup(scriptLookup);
             if (filePath != null) {
                 System.out.println("call loadAndRunOneScript filePath = " + filePath);
                 try {
                     runner2.loadAndRunOneScript(filePath);
                 } catch (ParameterValidationException pve) {
                     printScriptUsage(scriptLookup.get(scriptName),scriptName);
                 }
             } else {
                 getLog().error("No script to run or not found.");
             }
         } catch (IOException ioe) {
             if(verbose) {
                 getLog().error("Error: " + ioe);
             } else {
                 getLog().error("Error: " + ioe.getMessage());
             }
         }
     }
 
     public void printScriptUsage(ScriptDescription sd,String scriptName){
         if (sd != null && sd.getUsage() != null && !sd.getUsage().equals("")) {
             //emitOutput("Script Usage: ant AdminTool " + sd.getUsage());
             StringBuffer sb = new StringBuffer();
             sb.append("Script Usage: ant FAdmin -Dargs=\"" + scriptName);
             if(sd.getUsageList() != null){
                for (ParameterUsge pu : sd.getUsageList()){
                     sb.append(" " + pu.getName() + "=<" + pu.getDescription() + "> ");
                 }
                 sb.append("\"");
                 emitOutput(sb.toString());
             }
             emitOutput(sd.getUsage());
          } else {
             emitOutput("Script " + scriptName +" does not have usage information");
          }
     }
 
     public void listFlows( AdminToolCommandLineOptions cmdOptions,String key, FarReachesServiceInfo service ){
         emitOutput("Using Key  >" + key);
         GeneralFlowRequest request = new GeneralFlowRequest(service, key, null);
         JSONArray<String> flows = request.listFlows();
         if(verbose){
             emitOutput("");
             emitOutput(" Sent Request: " + request.getRequestString() );
             emitOutput(" With key: " + request.getApiKey() );
             emitOutput("");
         }
         emitOutput(flows.toString());
 
     }
 
     public void descFlow( AdminToolCommandLineOptions cmdOptions,String key, String flowName, FarReachesServiceInfo service ){
         GeneralFlowRequest request = new GeneralFlowRequest(service, key, flowName);
         JSONObject flows = request.describeFlow();
         if(verbose){
             emitOutput("");
             emitOutput(" Sent Request: " + request.getRequestString() );
             emitOutput(" With key: " + request.getApiKey() );
             emitOutput("");
         }
         emitOutput(flows.toString(4));
     }
 
 }
