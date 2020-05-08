 /*
  * Copyright (c) 2009, GoodData Corporation. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided
  * that the following conditions are met:
  *
  *     * Redistributions of source code must retain the above copyright notice, this list of conditions and
  *        the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
  *        and the following disclaimer in the documentation and/or other materials provided with the distribution.
  *     * Neither the name of the GoodData Corporation nor the names of its contributors may be used to endorse
  *        or promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
  * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
  * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.gooddata.processor;
 
 import com.gooddata.connector.*;
 import com.gooddata.exception.*;
 import com.gooddata.integration.model.Column;
 import com.gooddata.integration.model.SLI;
 import com.gooddata.integration.rest.GdcRESTApiWrapper;
 import com.gooddata.integration.rest.MetadataObject;
 import com.gooddata.integration.rest.configuration.NamePasswordConfiguration;
 import com.gooddata.modeling.model.SourceSchema;
 import com.gooddata.naming.N;
 import com.gooddata.processor.parser.DIScriptParser;
 import com.gooddata.processor.parser.ParseException;
 import com.gooddata.util.DatabaseToCsv;
 import com.gooddata.util.FileUtil;
 import com.gooddata.util.StringUtil;
 import org.apache.commons.cli.*;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 import org.joda.time.DateTimeZone;
 
 import java.io.*;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 /**
  * The GoodData Data Integration CLI processor.
  *
  * @author jiri.zaloudek
  * @author Zdenek Svoboda <zd@gooddata.org>
  * @version 1.0
  */
 public class GdcDI implements Executor {
 
     private static Logger l = Logger.getLogger(GdcDI.class);
 
     //Options data
     public static String[] CLI_PARAM_HELP = {"help", "H"};
 
     public static String[] CLI_PARAM_USERNAME = {"username", "u"};
     public static String[] CLI_PARAM_PASSWORD = {"password", "p"};
 
     public static String[] CLI_PARAM_HOST = {"host", "h"};
     public static String[] CLI_PARAM_FTP_HOST = {"ftphost", "f"};
     public static String[] CLI_PARAM_PROJECT = {"project", "i"};
     public static String[] CLI_PARAM_PROTO = {"proto", "t"};
     public static String[] CLI_PARAM_INSECURE = {"insecure", "s"};
     public static String[] CLI_PARAM_EXECUTE = {"execute", "e"};
     public static String[] CLI_PARAM_VERSION = {"version", "V"};
     public static String[] CLI_PARAM_DEFAULT_DATE_FOREIGN_KEY = {"default-date-fk", "D"};
     public static String[] CLI_PARAM_HTTP_PROXY_HOST = {"proxyhost", "K"};
     public static String[] CLI_PARAM_HTTP_PROXY_PORT = {"proxyport", "L"};
     public static String[] CLI_PARAM_HTTP_PORT = {"port", "S"};
     public static String[] CLI_PARAM_FTP_PORT = {"ftpport", "O"};
     public static String[] CLI_PARAM_HTTP_PROXY_USERNAME = {"proxyusername", "U"};
     public static String[] CLI_PARAM_HTTP_PROXY_PASSWORD = {"proxypassword", "P"};
     public static String[] CLI_PARAM_TIMEZONE = {"timezone", "T"};
     public static String CLI_PARAM_SCRIPT = "script";
 
     private static String DEFAULT_PROPERTIES = "gdi.properties";
 
     // Command line options
     private static Options ops = new Options();
     public static Option[] Options = {
             new Option(CLI_PARAM_HELP[1], CLI_PARAM_HELP[0], false, "Print command reference"),
             new Option(CLI_PARAM_USERNAME[1], CLI_PARAM_USERNAME[0], true, "GoodData username"),
             new Option(CLI_PARAM_PASSWORD[1], CLI_PARAM_PASSWORD[0], true, "GoodData password"),
             new Option(CLI_PARAM_HTTP_PROXY_HOST[1], CLI_PARAM_HTTP_PROXY_HOST[0], true, "HTTP proxy hostname."),
             new Option(CLI_PARAM_HTTP_PROXY_PORT[1], CLI_PARAM_HTTP_PROXY_PORT[0], true, "HTTP proxy port."),
             new Option(CLI_PARAM_HTTP_PORT[1], CLI_PARAM_HTTP_PORT[0], true, "HTTP port."),
             new Option(CLI_PARAM_FTP_PORT[1], CLI_PARAM_FTP_PORT[0], true, "Data stage port"),
             new Option(CLI_PARAM_HTTP_PROXY_USERNAME[1], CLI_PARAM_HTTP_PROXY_USERNAME[0], true, "HTTP proxy username."),
             new Option(CLI_PARAM_HTTP_PROXY_PASSWORD[1], CLI_PARAM_HTTP_PROXY_PASSWORD[0], true, "HTTP proxy password."),
             new Option(CLI_PARAM_HOST[1], CLI_PARAM_HOST[0], true, "GoodData host"),
             new Option(CLI_PARAM_FTP_HOST[1], CLI_PARAM_FTP_HOST[0], true, "GoodData data stage host"),
             new Option(CLI_PARAM_PROJECT[1], CLI_PARAM_PROJECT[0], true, "GoodData project identifier (a string like nszfbgkr75otujmc4smtl6rf5pnmz9yl)"),
             new Option(CLI_PARAM_PROTO[1], CLI_PARAM_PROTO[0], true, "HTTP or HTTPS (deprecated)"),
             new Option(CLI_PARAM_INSECURE[1], CLI_PARAM_INSECURE[0], false, "Disable encryption"),
             new Option(CLI_PARAM_VERSION[1], CLI_PARAM_VERSION[0], false, "Prints the tool version."),
             new Option(CLI_PARAM_TIMEZONE[1], CLI_PARAM_TIMEZONE[0], true, "Specify the default timezone (the computer timezone is the default)."),
             new Option(CLI_PARAM_EXECUTE[1], CLI_PARAM_EXECUTE[0], true, "Commands and params to execute before the commands in provided files"),
             new Option(CLI_PARAM_DEFAULT_DATE_FOREIGN_KEY[1], CLI_PARAM_DEFAULT_DATE_FOREIGN_KEY[0], true, "Foreign key to represent an 'unknown' date")
     };
 
     private CliParams cliParams = null;
     private Connector[] connectors = null;
 
     private ProcessingContext context = new ProcessingContext();
 
     private boolean finishedSucessfuly = false;
 
     private static long LOCK_EXPIRATION_TIME = 1000 * 3600; // 1 hour
 
     private final static String BUILD_NUMBER = "";
 
     private GdcDI(CommandLine ln, Properties defaults) {
         try {
             cliParams = parse(ln, defaults);
 
             if(cliParams.containsKey(CLI_PARAM_TIMEZONE[0])) {
                 String timezone = cliParams.get(CLI_PARAM_TIMEZONE[0]);
                 if(timezone != null && timezone.length()>0) {
                     DateTimeZone.setDefault(DateTimeZone.forID("Europe/London"));
                 }
                 else {
                     throw new InvalidArgumentException("Invalid timezone: '" + timezone+"'.");
                 }
             }
 
            if(cliParams.containsKey(CLI_PARAM_HTTP_PORT)) {
                 String httpPortString = cliParams.get(CLI_PARAM_HTTP_PORT[0]);
                 int httpPort = 0;
                 try {
                     httpPort = Integer.parseInt(httpPortString);
                 }
                 catch(NumberFormatException e) {
                     throw new InvalidArgumentException("Invalid HTTP port value: '" + httpPortString+"'.");
                 }
                 cliParams.setHttpConfig(new NamePasswordConfiguration(
                         cliParams.containsKey(CLI_PARAM_INSECURE[0]) ? "http" : "https",
                         cliParams.get(CLI_PARAM_HOST[0]),
                         cliParams.get(CLI_PARAM_USERNAME[0]), cliParams.get(CLI_PARAM_PASSWORD[0]), httpPort));
             }
             else {
                 cliParams.setHttpConfig(new NamePasswordConfiguration(
                         cliParams.containsKey(CLI_PARAM_INSECURE[0]) ? "http" : "https",
                         cliParams.get(CLI_PARAM_HOST[0]),
                         cliParams.get(CLI_PARAM_USERNAME[0]), cliParams.get(CLI_PARAM_PASSWORD[0])));
             }
 
            if(cliParams.containsKey(CLI_PARAM_FTP_PORT)) {
                 String ftpPortString = cliParams.get(CLI_PARAM_FTP_PORT[0]);
                 int ftpPort = 0;
                 try {
                     ftpPort = Integer.parseInt(ftpPortString);
                 }
                 catch(NumberFormatException e) {
                     throw new InvalidArgumentException("Invalid WebDav port value: '" + ftpPortString+"'.");
                 }
                 cliParams.setFtpConfig(new NamePasswordConfiguration(
                         cliParams.containsKey(CLI_PARAM_INSECURE[0]) ? "http" : "https",
                         cliParams.get(CLI_PARAM_FTP_HOST[0]),
                         cliParams.get(CLI_PARAM_USERNAME[0]), cliParams.get(CLI_PARAM_PASSWORD[0]),ftpPort));
             }
             else {
                 cliParams.setFtpConfig(new NamePasswordConfiguration(
                         cliParams.containsKey(CLI_PARAM_INSECURE[0]) ? "http" : "https",
                         cliParams.get(CLI_PARAM_FTP_HOST[0]),
                         cliParams.get(CLI_PARAM_USERNAME[0]), cliParams.get(CLI_PARAM_PASSWORD[0])));
             }
             connectors = instantiateConnectors();
             String execute = cliParams.get(CLI_PARAM_EXECUTE[0]);
             String scripts = cliParams.get(CLI_PARAM_SCRIPT);
 
             if (execute != null && scripts != null && execute.length() > 0 && scripts.length() > 0) {
                 throw new InvalidArgumentException("You can't execute a script and use the -e command line parameter at the same time.");
             }
             if (execute != null && execute.length() > 0) {
                 l.debug("Executing arg=" + execute);
                 execute(execute);
             }
             if (scripts != null && scripts.length() > 0) {
                 String[] sas = scripts.split(",");
                 for (String script : sas) {
                     l.debug("Executing file=" + script);
                     execute(new File(script));
                 }
             }
             if (cliParams.containsKey(CLI_PARAM_HELP[0]))
                 l.info(commandsHelp());
             finishedSucessfuly = true;
         } catch (InvalidArgumentException e) {
             l.error("Invalid or missing argument: " + e.getMessage());
             l.debug(e);
             Throwable c = e.getCause();
             while (c != null) {
                 l.debug("Caused by: ", c);
                 c = c.getCause();
             }
             HelpFormatter formatter = new HelpFormatter();
             formatter.printHelp("gooddata-cli [<options> ...] -H|--help|<script>|-e <command>", ops);
             finishedSucessfuly = false;
         } catch (InvalidCommandException e) {
             l.error("Invalid command: " + e.getMessage());
             l.debug(e);
             Throwable c = e.getCause();
             while (c != null) {
                 l.debug("Caused by: ", c);
                 c = c.getCause();
             }
             finishedSucessfuly = false;
         } catch (InvalidParameterException e) {
             l.error("Invalid command parameter: " + e.getMessage());
             l.debug(e);
             Throwable c = e.getCause();
             while (c != null) {
                 l.debug("Caused by: ", c);
                 c = c.getCause();
             }
             finishedSucessfuly = false;
         } catch (SfdcException e) {
             l.error("Error communicating with SalesForce: " + e.getMessage());
             l.debug(e);
             Throwable c = e.getCause();
             while (c != null) {
                 l.debug("Caused by: ", c);
                 c = c.getCause();
             }
             finishedSucessfuly = false;
         } catch (ProcessingException e) {
             l.error("Error processing command: " + e.getMessage());
             l.debug(e);
             Throwable c = e.getCause();
             while (c != null) {
                 l.debug("Caused by: ", c);
                 c = c.getCause();
             }
             finishedSucessfuly = false;
         } catch (ModelException e) {
             l.error("Model issue: " + e.getMessage());
             l.debug(e);
             Throwable c = e.getCause();
             while (c != null) {
                 l.debug("Caused by: ", c);
                 c = c.getCause();
             }
             finishedSucessfuly = false;
         } catch (IOException e) {
             l.error("Encountered an IO problem. Please check that all files that you use in your command line arguments and commands exist." + e.getMessage());
             l.debug(e);
             Throwable c = e.getCause();
             while (c != null) {
                 l.debug("Caused by: ", c);
                 c = c.getCause();
             }
             finishedSucessfuly = false;
         } catch (InternalErrorException e) {
             Throwable c = e.getCause();
             if (c != null && c instanceof SQLException) {
                 l.error("Error extracting data. Can't process the incoming data. Please check the CSV file " +
                         "separator and consistency (same number of columns in each row). Also, please make sure " +
                         "that the number of columns in your XML config file matches the number of rows in your " +
                         "data source. Make sure that your file is readable by other users (particularly the mysql user). " +
                         "More info: ", c);
             } else {
                 l.error("Internal error: " + e.getMessage());
                 l.debug(e);
                 c = e.getCause();
                 while (c != null) {
                     l.debug("Caused by: ", c);
                     c = c.getCause();
                 }
             }
             finishedSucessfuly = false;
         } catch (HttpMethodException e) {
             l.debug("Error executing GoodData REST API: " + e);
             Throwable c = e.getCause();
             while (c != null) {
                 l.debug("Caused by: ", c);
                 c = c.getCause();
             }
 
             String msg = e.getMessage();
             String requestId = e.getRequestId();
             if (requestId != null) {
                 msg += "\n\n" +
                         "If you believe this is not your fault, good people from support\n" +
                         "portal (http://support.gooddata.com) may help you.\n\n" +
                         "Show them this error ID: " + requestId;
             }
             l.error(msg);
             finishedSucessfuly = false;
         } catch (GdcRestApiException e) {
             l.error("REST API invocation error: " + e.getMessage());
             l.debug(e, e);
             Throwable c = e.getCause();
             while (c != null) {
                 if (c instanceof HttpMethodException) {
                     HttpMethodException ex = (HttpMethodException) c;
                     String msg = ex.getMessage();
                     if (msg != null && msg.length() > 0 && msg.indexOf("/ldm/manage") > 0) {
                         l.error("Error creating/updating logical data model (executing MAQL DDL).");
                         if (msg.indexOf(".date") > 0) {
                             l.error("Bad time dimension schemaReference.");
                         } else {
                             l.error("You are either trying to create a data object that already exists " +
                                     "(executing the same MAQL multiple times) or providing a wrong reference " +
                                     "or schemaReference in your XML configuration.");
                         }
                     }
                 }
                 l.debug("Caused by: ", c);
                 c = c.getCause();
             }
             finishedSucessfuly = false;
         } catch (GdcException e) {
             l.error("Unrecognized error: " + e.getMessage());
             l.debug(e);
             Throwable c = e.getCause();
             while (c != null) {
                 l.debug("Caused by: ", c);
                 c = c.getCause();
             }
             finishedSucessfuly = false;
         } finally {
             /*
             if (cliParams != null)
                 context.getRestApi(cliParams).logout();
                 */
         }
     }
 
     /**
      * Parse and validate the cli arguments
      *
      * @param ln parsed command line
      * @return parsed cli parameters wrapped in the CliParams
      * @throws InvalidArgumentException in case of nonexistent or incorrect cli args
      */
     protected CliParams parse(CommandLine ln, Properties defaults) throws InvalidArgumentException {
         l.debug("Parsing cli " + ln);
         CliParams cp = new CliParams();
 
         for (Option o : Options) {
             String name = o.getLongOpt();
             if (ln.hasOption(name)) {
                 cp.put(name, ln.getOptionValue(name));
             } else if (defaults.getProperty(name) != null) {
                 cp.put(name, defaults.getProperty(name));
             }
         }
 
         if(cp.containsKey(CLI_PARAM_HTTP_PROXY_HOST[0])) {
             System.setProperty("http.proxyHost", cp.get(CLI_PARAM_HTTP_PROXY_HOST[0]));
         }
         if(cp.containsKey(CLI_PARAM_HTTP_PROXY_PORT[0])) {
             System.setProperty("http.proxyPort", cp.get(CLI_PARAM_HTTP_PROXY_PORT[0]));
         }
         if(cp.containsKey(CLI_PARAM_HTTP_PROXY_USERNAME[0])) {
             System.setProperty("http.proxyUser", cp.get(CLI_PARAM_HTTP_PROXY_USERNAME[0]));
             System.setProperty("http.proxyUserName", cp.get(CLI_PARAM_HTTP_PROXY_USERNAME[0]));
             System.setProperty("http.proxyUsername", cp.get(CLI_PARAM_HTTP_PROXY_USERNAME[0]));
         }
         if(cp.containsKey(CLI_PARAM_HTTP_PROXY_PASSWORD[0])) {
             System.setProperty("http.proxyPassword", cp.get(CLI_PARAM_HTTP_PROXY_PASSWORD[0]));
         }
 
         if (cp.containsKey(CLI_PARAM_VERSION[0])) {
 
             l.info("GoodData CL version 1.2.52" +
                     ((BUILD_NUMBER.length() > 0) ? ", build " + BUILD_NUMBER : "."));
             System.exit(0);
 
         }
 
 
         // use default host if there is no host in the CLI params
         if (!cp.containsKey(CLI_PARAM_HOST[0])) {
             cp.put(CLI_PARAM_HOST[0], Defaults.DEFAULT_HOST);
         }
 
         l.debug("Using host " + cp.get(CLI_PARAM_HOST[0]));
 
         // create default FTP host if there is no host in the CLI params
         if (!cp.containsKey(CLI_PARAM_FTP_HOST[0])) {
             String[] hcs = cp.get(CLI_PARAM_HOST[0]).split("\\.");
             if (hcs != null && hcs.length > 0) {
                 String ftpHost = "";
                 for (int i = 0; i < hcs.length; i++) {
                     if (i > 0)
                         ftpHost += "." + hcs[i];
                     else
                         ftpHost = hcs[i] + N.FTP_SRV_SUFFIX;
                 }
                 cp.put(CLI_PARAM_FTP_HOST[0], ftpHost);
             } else {
                 throw new InvalidArgumentException("Invalid format of the GoodData REST API host: " +
                         cp.get(CLI_PARAM_HOST[0]));
             }
 
         }
 
         l.debug("Using FTP host " + cp.get(CLI_PARAM_FTP_HOST[0]));
 
         // Default to secure protocol if there is no host in the CLI params
         // Assume insecure protocol if user specifies "HTTPS", for backwards compatibility
         if (cp.containsKey(CLI_PARAM_PROTO[0])) {
             String proto = ln.getOptionValue(CLI_PARAM_PROTO[0]).toLowerCase();
             if (!"http".equalsIgnoreCase(proto) && !"https".equalsIgnoreCase(proto)) {
                 throw new InvalidArgumentException("Invalid '" + CLI_PARAM_PROTO[0] + "' parameter. Use HTTP or HTTPS.");
             }
             if ("http".equalsIgnoreCase(proto)) {
                 cp.put(CLI_PARAM_INSECURE[0], "true");
             }
         }
         if (cp.containsKey(CLI_PARAM_INSECURE[0]))
             cp.put(CLI_PARAM_INSECURE[0], "true");
 
         l.debug("Using " + (cp.containsKey(CLI_PARAM_INSECURE[0]) ? "in" : "") + "secure protocols");
 
         if (ln.getArgs().length == 0 && !ln.hasOption(CLI_PARAM_EXECUTE[0]) && !ln.hasOption(CLI_PARAM_HELP[0])) {
             throw new InvalidArgumentException("No command has been given, quitting.");
         }
 
         String scripts = "";
         for (final String arg : ln.getArgs()) {
             if (scripts.length() > 0)
                 scripts += "," + arg;
             else
                 scripts += arg;
         }
         cp.put(CLI_PARAM_SCRIPT, scripts);
         return cp;
     }
 
 
     /**
      * Executes the commands in String
      *
      * @param commandsStr commands string
      */
     public void execute(final String commandsStr) {
         List<Command> cmds = new ArrayList<Command>();
         cmds.addAll(parseCmd(commandsStr));
         for (Command command : cmds) {
             boolean processed = false;
             for (int i = 0; i < connectors.length && !processed; i++) {
                 processed = connectors[i].processCommand(command, cliParams, context);
             }
             if (!processed)
                 this.processCommand(command, cliParams, context);
         }
     }
 
     /**
      * Executes the commands in file
      *
      * @param scriptFile file with commands
      * @throws IOException in case of an IO issue
      */
     public void execute(final File scriptFile) throws IOException {
         List<Command> cmds = new ArrayList<Command>();
         cmds.addAll(parseCmd(FileUtil.readStringFromFile(scriptFile.getAbsolutePath())));
         for (Command command : cmds) {
             boolean processed = false;
             for (int i = 0; i < connectors.length && !processed; i++) {
                 processed = connectors[i].processCommand(command, cliParams, context);
             }
             if (!processed)
                 processed = this.processCommand(command, cliParams, context);
             if (!processed)
                 throw new InvalidCommandException("Unknown command '" + command.getCommand() + "'");
         }
     }
 
     /**
      * Returns the help for commands
      *
      * @return help text
      */
     public static String commandsHelp() {
         try {
             final InputStream is = CliParams.class.getResourceAsStream("/com/gooddata/processor/COMMANDS.txt");
             if (is == null)
                 throw new IOException();
             return FileUtil.readStringFromStream(is);
         } catch (IOException e) {
             l.error("Could not read com/gooddata/processor/COMMANDS.txt");
         }
         return "";
     }
 
 
     private static boolean checkJavaVersion() {
         String version = System.getProperty("java.version");
         if (version.startsWith("1.8") || version.startsWith("1.7") || version.startsWith("1.6") || version.startsWith("1.5"))
             return true;
         l.error("You're running Java " + version + ". Please use Java 1.5 or higher for running this tool. " +
                 "Please refer to http://java.sun.com/javase/downloads/index.jsp for a more recent Java version.");
         throw new InternalErrorException("You're running Java " + version + ". Please use use Java 1.5 or higher for running this tool. " +
                 "Please refer to http://java.sun.com/javase/downloads/index.jsp for a more recent Java version.");
     }
 
     /**
      * The main CLI processor
      *
      * @param args command line argument
      */
     public static void main(String[] args) {
 
         checkJavaVersion();
         Properties defaults = loadDefaults();
 
         for (Option o : Options)
             ops.addOption(o);
 
         try {
             CommandLineParser parser = new GnuParser();
             CommandLine cmdline = parser.parse(ops, args);
             GdcDI gdi = new GdcDI(cmdline, defaults);
             if (!gdi.finishedSucessfuly) {
                 System.exit(1);
             }
         } catch (org.apache.commons.cli.ParseException e) {
             l.error("Error parsing command line parameters: ", e);
             l.debug("Error parsing command line parameters", e);
         }
     }
 
     private void setupHttpProxies() {
         //CredentialsProvider proxyCredentials = new BasicCredentialsProvider ();
     }
 
     /**
      * Parses the commands
      *
      * @param cmd commands string
      * @return array of commands
      * @throws InvalidCommandException in case there is an invalid command
      */
     protected static List<Command> parseCmd(String cmd) throws InvalidCommandException {
         l.debug("Parsing comands: " + cmd);
         try {
             if (cmd != null && cmd.length() > 0) {
                 Reader r = new StringReader(cmd);
                 DIScriptParser parser = new DIScriptParser(r);
                 List<Command> commands = parser.parse();
                 l.debug("Running " + commands.size() + " commands.");
                 for (Command c : commands) {
                     l.debug("Command=" + c.getCommand() + " params=" + c.getParameters());
                 }
                 return commands;
             }
         } catch (ParseException e) {
             throw new InvalidCommandException("Can't parse command '" + cmd + "'");
         }
         throw new InvalidCommandException("Can't parse command (empty command).");
     }
 
 
     /**
      * {@inheritDoc}
      */
     public boolean processCommand(Command c, CliParams cli, ProcessingContext ctx) throws ProcessingException {
         l.debug("Processing command " + c.getCommand());
         try {
             // take project id from command line, may be override in the script
             if (cliParams.get(CLI_PARAM_PROJECT[0]) != null) {
                 ctx.setProjectId(cliParams.get(CLI_PARAM_PROJECT[0]));
             }
             if (c.match("CreateProject")) {
                 createProject(c, cli, ctx);
             } else if (c.match("DropProject") || c.match("DeleteProject")) {
                 dropProject(c, cli, ctx);
             } else if (c.match("OpenProject")) {
                 ctx.setProjectId(c.getParamMandatory("id"));
                 c.paramsProcessed();
 
                 l.debug("Opened project id=" + ctx.getProjectId());
                 l.info("Opened project id=" + ctx.getProjectId());
             } else if (c.match("StoreProject") || c.match("RememberProject")) {
                 storeProject(c, cli, ctx);
             } else if (c.match("ExecuteDml")) {
                 executeDML(c, cli, ctx);
             } else if (c.match("RetrieveProject") || c.match("UseProject")) {
                 retrieveProject(c, cli, ctx);
             } else if (c.match("ExportProject")) {
                 exportProject(c, cli, ctx);
             } else if (c.match("ImportProject")) {
                 importProject(c, cli, ctx);
             } else if (c.match("Lock")) {
                 lock(c, cli, ctx);
             } else if (c.match("GetReports")) {
                 getReports(c, cli, ctx);
             } else if (c.match("CreateUser")) {
                 createUser(c, cli, ctx);
             } else if (c.match("AddUsersToProject")) {
                 addUsersToProject(c, cli, ctx);
             } else if (c.match("DisableUsersInProject")) {
                 disableUsersInProject(c, cli, ctx);
             } else if (c.match("GetProjectUsers")) {
                 getProjectUsers(c, cli, ctx);
             } else if (c.match("InviteUser")) {
                 inviteUser(c, cli, ctx);
             } else if (c.match("ExecuteReports")) {
                 executeReports(c, cli, ctx);
             } else if (c.match("StoreMetadataObject")) {
                 storeMdObject(c, cli, ctx);
             } else if (c.match("DropMetadataObject")) {
                 dropMdObject(c, cli, ctx);
             } else if (c.match("RetrieveMetadataObject")) {
                 getMdObject(c, cli, ctx);
             } else if (c.match("ExportMetadataObjects")) {
                 exportMDObject(c, cli, ctx);
             } else if (c.match("ImportMetadataObjects")) {
                 importMDObject(c, cli, ctx);
             } else if (c.match("ExportJdbcToCsv")) {
                 exportJdbcToCsv(c, cli, ctx);
             } else if (c.match("MigrateDatasets")) {
                 migrateDatasets(c, cli, ctx);
             } else if (c.match("GenerateManifests")) {
                 generateManifests(c, cli, ctx);
             } else {
                 l.debug("No match command " + c.getCommand());
                 return false;
             }
         } catch (IOException e) {
             l.debug("Processing command " + c.getCommand() + " failed", e);
             throw new ProcessingException(e);
         } catch (InterruptedException e) {
             l.debug("Processing command " + c.getCommand() + " failed", e);
             throw new ProcessingException(e);
         }
         l.debug("Command processing " + c.getCommand() + " finished.");
         return true;
     }
 
     /**
      * Executes MAQL DML
      *
      * @param c   command
      * @param p   cli parameters
      * @param ctx current context
      * @throws IOException IO issues
      */
     private void executeDML(Command c, CliParams p, ProcessingContext ctx) throws IOException {
         try {
             l.debug("Executing MAQL DML.");
             String pid = ctx.getProjectIdMandatory();
             final String cmd = c.getParamMandatory("maql");
             c.paramsProcessed();
 
             String taskUri = ctx.getRestApi(p).executeDML(pid, cmd);
             if (taskUri != null && taskUri.length() > 0) {
                 l.debug("Checking MAQL DML execution status.");
                 String status = "";
                 while (!"OK".equalsIgnoreCase(status) && !"ERROR".equalsIgnoreCase(status) && !"WARNING".equalsIgnoreCase(status)) {
                     status = ctx.getRestApi(p).getMigrationStatus(taskUri);
                     l.debug("MAQL DML execution status = " + status);
                     Thread.sleep(500);
                 }
                 l.info("MAQL DML execution finished with status " + status);
                 if ("ERROR".equalsIgnoreCase(status)) {
                     l.error("Error executing the MAQL DML. Check debug log for more details.");
                     throw new GdcRestApiException("Error executing the MAQL DML. Check debug log for more details.");
                 }
             } else {
                 l.error("MAQL DML execution hasn't returned any task URI.");
                 throw new InternalErrorException("MAQL DML execution hasn't returned any task URI.");
             }
             l.debug("Finished MAQL DML execution.");
             l.info("MAQL DML command '" + cmd + "' successfully executed.");
         } catch (InterruptedException e) {
             throw new InternalErrorException(e);
         }
     }
 
     /**
      * Exports project
      *
      * @param c   command
      * @param p   cli parameters
      * @param ctx current context
      * @throws IOException IO issues
      */
     private void exportProject(Command c, CliParams p, ProcessingContext ctx) throws IOException {
         try {
             l.info("Exporting project.");
             String pid = ctx.getProjectIdMandatory();
             final String eu = c.getParamMandatory("exportUsers");
             final boolean exportUsers = (eu != null && "true".equalsIgnoreCase(eu));
             final String ed = c.getParamMandatory("exportData");
             final boolean exportData = (ed != null && "true".equalsIgnoreCase(ed));
             final String fileName = c.getParamMandatory("tokenFile");
             String au = c.getParam("authorizedUsers");
             c.paramsProcessed();
 
             String[] authorizedUsers = null;
             if (au != null && au.length() > 0) {
                 authorizedUsers = au.split(",");
             }
 
             GdcRESTApiWrapper.ProjectExportResult r = ctx.getRestApi(p).exportProject(pid, exportUsers, exportData,
                     authorizedUsers);
             String taskUri = r.getTaskUri();
             String token = r.getExportToken();
             if (taskUri != null && taskUri.length() > 0) {
                 l.debug("Checking project export status.");
                 String status = "";
                 while (!"OK".equalsIgnoreCase(status) && !"ERROR".equalsIgnoreCase(status) && !"WARNING".equalsIgnoreCase(status)) {
                     status = ctx.getRestApi(p).getMigrationStatus(taskUri);
                     l.debug("Project export status = " + status);
                     Thread.sleep(500);
                 }
                 l.info("Project export finished with status " + status);
                 if ("OK".equalsIgnoreCase(status) || "WARNING".equalsIgnoreCase(status)) {
                     FileUtil.writeStringToFile(token, fileName);
                 } else {
                     l.error("Error exporting project. Check debug log for more details.");
                     throw new GdcRestApiException("Error exporting project. Check debug log for more details.");
                 }
             } else {
                 l.error("Project export hasn't returned any task URI.");
                 throw new InternalErrorException("Project export hasn't returned any task URI.");
 
             }
             l.debug("Finished project export.");
             l.info("Project " + pid + " successfully exported. Import token is " + token);
         } catch (InterruptedException e) {
             throw new InternalErrorException(e);
         }
     }
 
     /**
      * Imports project
      *
      * @param c   command
      * @param p   cli parameters
      * @param ctx current context
      * @throws IOException IO issues
      */
     private void importProject(Command c, CliParams p, ProcessingContext ctx) throws IOException {
         try {
             l.info("Importing project.");
             String pid = ctx.getProjectIdMandatory();
             final String tokenFile = c.getParamMandatory("tokenFile");
             c.paramsProcessed();
             String token = FileUtil.readStringFromFile(tokenFile).trim();
             String taskUri = ctx.getRestApi(p).importProject(pid, token);
             if (taskUri != null && taskUri.length() > 0) {
                 l.debug("Checking project import status.");
                 String status = "";
                 while (!"OK".equalsIgnoreCase(status) && !"ERROR".equalsIgnoreCase(status) && !"WARNING".equalsIgnoreCase(status)) {
                     status = ctx.getRestApi(p).getMigrationStatus(taskUri);
                     l.debug("Project import status = " + status);
                     Thread.sleep(500);
                 }
                 l.info("Project import finished with status " + status);
                 if ("ERROR".equalsIgnoreCase(status)) {
                     l.error("Error importing project. Check debug log for more details.");
                     throw new GdcRestApiException("Error importing project. Check debug log for more details.");
                 }
             } else {
                 l.error("Project import hasn't returned any task URI.");
                 throw new InternalErrorException("Project import hasn't returned any task URI.");
 
             }
             l.debug("Finished project import.");
             l.info("Project " + pid + " successfully imported.");
         } catch (InterruptedException e) {
             throw new InternalErrorException(e);
         }
     }
 
 
     /**
      * Exports MD objects
      *
      * @param c   command
      * @param p   cli parameters
      * @param ctx current context
      * @throws IOException IO issues
      */
     private void exportMDObject(Command c, CliParams p, ProcessingContext ctx) throws IOException {
         try {
             l.info("Exporting metadata objects.");
             String token;
             String pid = ctx.getProjectIdMandatory();
             final String fileName = c.getParamMandatory("tokenFile");
             final String idscs = c.getParamMandatory("objectIDs");
             c.paramsProcessed();
             if (idscs != null && idscs.length() > 0) {
                 String[] idss = idscs.split(",");
                 List<Integer> ids = new ArrayList<Integer>();
                 for (String id : idss) {
                     try {
                         ids.add(Integer.parseInt(id));
                     } catch (NumberFormatException e) {
                         l.debug("Invalid metadata object ID " + id, e);
                         l.error("Invalid metadata object ID " + id);
                         throw new InvalidParameterException("Invalid metadata object ID " + id, e);
                     }
                 }
                 GdcRESTApiWrapper.ProjectExportResult r = ctx.getRestApi(p).exportMD(pid, ids);
                 String taskUri = r.getTaskUri();
                 token = r.getExportToken();
                 if (taskUri != null && taskUri.length() > 0) {
                     l.debug("Checking MD export status.");
                     String status = "";
                     while (!"OK".equalsIgnoreCase(status) && !"ERROR".equalsIgnoreCase(status) && !"WARNING".equalsIgnoreCase(status)) {
                         status = ctx.getRestApi(p).getTaskManStatus(taskUri);
                         l.debug("MD export status = " + status);
                         Thread.sleep(500);
                     }
                     l.info("MD export finished with status " + status);
                     if ("OK".equalsIgnoreCase(status) || "WARNING".equalsIgnoreCase(status)) {
                         FileUtil.writeStringToFile(token, fileName);
                     } else {
                         l.error("Error exporting metadata. Check debug log for more details.");
                         throw new GdcRestApiException("Error exporting metadata. Check debug log for more details.");
                     }
                 } else {
                     l.error("MD export hasn't returned any task URI.");
                     throw new InternalErrorException("MD export hasn't returned any task URI.");
                 }
             } else {
                 l.debug("The objectIDs parameter must contain a comma separated list of metadata object IDs!");
                 l.error("The objectIDs parameter must contain a comma separated list of metadata object IDs!");
                 throw new InvalidParameterException("The objectIDs parameter must contain a comma separated list of metadata object IDs!");
             }
             l.debug("Finished MD export.");
             l.info("Project " + pid + " metadata successfully exported. Import token is " + token);
         } catch (InterruptedException e) {
             throw new InternalErrorException(e);
         }
     }
 
     /**
      * Imports MD objects
      *
      * @param c   command
      * @param p   cli parameters
      * @param ctx current context
      * @throws IOException IO issues
      */
     private void importMDObject(Command c, CliParams p, ProcessingContext ctx) throws IOException {
         try {
             l.info("Importing metadata objects.");
             String pid = ctx.getProjectIdMandatory();
             final String tokenFile = c.getParamMandatory("tokenFile");
             String token = FileUtil.readStringFromFile(tokenFile).trim();
             /*
             Currently not supported
             final String ov = c.getParam("overwrite");
             final boolean overwrite = (ov != null && "true".equalsIgnoreCase(ov));
             */
             final String ul = c.getParam("updateLDM");
             final boolean updateLDM = (ul != null && "true".equalsIgnoreCase(ul));
             final boolean overwrite = true;
             c.paramsProcessed();
             String taskUri = ctx.getRestApi(p).importMD(pid, token, overwrite, updateLDM);
             if (taskUri != null && taskUri.length() > 0) {
                 l.debug("Checking MD import status.");
                 String status = "";
                 while (!"OK".equalsIgnoreCase(status) && !"ERROR".equalsIgnoreCase(status) && !"WARNING".equalsIgnoreCase(status)) {
                     status = ctx.getRestApi(p).getTaskManStatus(taskUri);
                     l.debug("MD import status = " + status);
                     Thread.sleep(500);
                 }
                 l.info("MD import finished with status " + status);
                 if ("ERROR".equalsIgnoreCase(status)) {
                     l.error("Error importing MD. Check debug log for more details.");
                     throw new GdcRestApiException("Error importing MD. Check debug log for more details.");
                 }
             } else {
                 l.error("MD import hasn't returned any task URI.");
                 throw new InternalErrorException("MD import hasn't returned any task URI.");
 
             }
             l.debug("Finished metadata import.");
             l.info("Project " + pid + " metadata successfully imported.");
         } catch (InterruptedException e) {
             throw new InternalErrorException(e);
         }
     }
 
 
     /**
      * Creates a new user
      *
      * @param c   command
      * @param p   cli parameters
      * @param ctx current context
      * @throws IOException IO issues
      */
     private void createUser(Command c, CliParams p, ProcessingContext ctx) throws IOException {
         l.info("Creating new user.");
 
         String domain = c.getParamMandatory("domain");
 
         GdcRESTApiWrapper.GdcUser user = new GdcRESTApiWrapper.GdcUser();
         user.setLogin(c.getParamMandatory("username"));
         user.setPassword(c.getParamMandatory("password"));
         user.setVerifyPassword(user.getPassword());
         user.setFirstName(c.getParamMandatory("firstName"));
         user.setLastName(c.getParamMandatory("lastName"));
         user.setCompanyName(c.getParam("company"));
         user.setPosition(c.getParam("position"));
         user.setCountry(c.getParam("country"));
         user.setPhoneNumber(c.getParam("phone"));
         user.setSsoProvider(c.getParam("ssoProvider"));
         String usersFile = c.getParam("usersFile");
         String appnd = c.getParam("append");
         c.paramsProcessed();
 
         final boolean append = (appnd != null && "true".equalsIgnoreCase(appnd));
         String r = ctx.getRestApi(p).createUser(domain, user);
         if (r != null && r.length() > 0 && usersFile != null && usersFile.length() > 0) {
             FileUtil.writeStringToFile(r + "\n", usersFile, append);
         }
         l.info("User " + user.getLogin() + "' successfully created. User URI: " + r);
     }
 
     /**
      * Adds a new user to project
      *
      * @param c   command
      * @param p   cli parameters
      * @param ctx current context
      * @throws IOException IO issues
      */
     private void addUsersToProject(Command c, CliParams p, ProcessingContext ctx) throws IOException {
         l.info("Adding users to project.");
 
         String pid = ctx.getProjectIdMandatory();
         String usersFile = c.getParamMandatory("usersFile");
         List<String> uris = new ArrayList<String>();
         BufferedReader r = FileUtil.createBufferedUtf8Reader(usersFile);
         String uri = r.readLine();
         while (uri != null && uri.trim().length() > 0) {
             uris.add(uri.trim());
             uri = r.readLine();
         }
         String role = c.getParam("role");
         c.paramsProcessed();
 
         ctx.getRestApi(p).addUsersToProject(pid, uris, role);
         l.info("Users " + uris + "' successfully added to project " + pid);
     }
 
 
     /**
      * Adds a new user to project
      *
      * @param c   command
      * @param p   cli parameters
      * @param ctx current context
      * @throws IOException IO issues
      */
     private void disableUsersInProject(Command c, CliParams p, ProcessingContext ctx) throws IOException {
         l.info("Disabling users in project.");
 
         String pid = ctx.getProjectIdMandatory();
         String usersFile = c.getParamMandatory("usersFile");
         c.paramsProcessed();
 
         List<String> uris = new ArrayList<String>();
         BufferedReader r = FileUtil.createBufferedUtf8Reader(usersFile);
         String uri = r.readLine();
         while (uri != null && uri.trim().length() > 0) {
             uris.add(uri.trim());
             uri = r.readLine();
         }
         ctx.getRestApi(p).disableUsersInProject(pid, uris);
         l.info("Users " + uris + "' successfully disabled in project " + pid);
     }
 
 
     /**
      * Adds a new user to project
      *
      * @param c   command
      * @param p   cli parameters
      * @param ctx current context
      * @throws IOException IO issues
      */
     private void getProjectUsers(Command c, CliParams p, ProcessingContext ctx) throws IOException {
         String pid = ctx.getProjectIdMandatory();
         l.info("Getting users from project " + pid);
         String usersFile = c.getParamMandatory("usersFile");
         String field = c.getParamMandatory("field");
         String activeOnlys = c.getParam("activeOnly");
         c.paramsProcessed();
 
         final boolean activeOnly = (activeOnlys != null && "true".equalsIgnoreCase(activeOnlys));
 
         if ("email".equalsIgnoreCase(field) || "uri".equalsIgnoreCase(field)) {
             List<GdcRESTApiWrapper.GdcUser> users = ctx.getRestApi(p).getProjectUsers(pid, activeOnly);
             for (GdcRESTApiWrapper.GdcUser user : users) {
                 if ("email".equalsIgnoreCase(field)) {
                     FileUtil.writeStringToFile(user.getLogin() + "\n", usersFile, true);
                 }
                 if ("uri".equalsIgnoreCase(field)) {
                     FileUtil.writeStringToFile(user.getUri() + "\n", usersFile, true);
                 }
                 l.info("User " + user.getLogin() + "' successfully added. User URI: " + user.getUri());
             }
         } else {
             l.error("Invalid field parameter. Only values 'email' and 'uri' are currently supported.");
         }
     }
 
     /**
      * Create new project command processor
      *
      * @param c   command
      * @param p   cli parameters
      * @param ctx current context
      */
     private void createProject(Command c, CliParams p, ProcessingContext ctx) {
         try {
             String name = c.getParamMandatory("name");
             String desc = c.getParam("desc");
             String pTempUri = c.getParam("templateUri");
             String driver = c.getParam("driver");
             c.paramsProcessed();
 
             if (desc == null || desc.length() <= 0)
                 desc = name;
             ctx.setProjectId(ctx.getRestApi(p).createProject(StringUtil.toTitle(name), StringUtil.toTitle(desc), pTempUri, driver));
             String pid = ctx.getProjectIdMandatory();
             checkProjectCreationStatus(pid, p, ctx);
             l.info("Project id = '" + pid + "' created.");
         } catch (InterruptedException e) {
             throw new InternalErrorException(e);
         }
     }
 
     /**
      * Exports all DB tables to CSV
      *
      * @param c   command
      * @param p   cli parameters
      * @param ctx current context
      */
     private void exportJdbcToCsv(Command c, CliParams p, ProcessingContext ctx) throws IOException {
         try {
             String usr = null;
             if (c.checkParam("username"))
                 usr = c.getParam("username");
             String psw = null;
             if (c.checkParam("password"))
                 psw = c.getParam("password");
             String drv = c.getParamMandatory("driver");
             String url = c.getParamMandatory("url");
             String fl = c.getParamMandatory("dir");
             c.paramsProcessed();
 
             File dir = new File(fl);
             if (!dir.exists() || !dir.isDirectory()) {
                 throw new InvalidParameterException("The dir parameter in the ExportJdbcToCsv command must be an existing directory.");
             }
             DatabaseToCsv d = new DatabaseToCsv(drv, url, usr, psw);
             d.export(dir.getAbsolutePath());
             l.info("All tables successfully exported to " + dir.getAbsolutePath());
         } catch (SQLException e) {
             throw new IOException(e);
         }
     }
 
     /**
      * Checks the project status. Waits till the status is LOADING
      *
      * @param projectId project ID
      * @param p         cli parameters
      * @param ctx       current context
      * @throws InterruptedException internal problem with making file writable
      */
     private void checkProjectCreationStatus(String projectId, CliParams p, ProcessingContext ctx) throws InterruptedException {
         l.debug("Checking project " + projectId + " loading status.");
         String status = "LOADING";
         while ("LOADING".equalsIgnoreCase(status)) {
             status = ctx.getRestApi(p).getProjectStatus(projectId);
             l.debug("Project " + projectId + " loading  status = " + status);
             Thread.sleep(500);
         }
     }
 
     /**
      * Drop project command processor
      *
      * @param c   command
      * @param p   cli parameters
      * @param ctx current context
      */
     private void dropProject(Command c, CliParams p, ProcessingContext ctx) {
         String id = ctx.getProjectId();
         if (id == null) {
             id = c.getParamMandatory("id");
         } else {
             String override = c.getParam("id");
             if (override != null)
                 id = override;
         }
         c.paramsProcessed();
 
         ctx.getRestApi(p).dropProject(id);
         l.info("Project id = '" + id + "' dropped.");
     }
 
     /**
      * Invite user to a project
      *
      * @param c   command
      * @param p   cli parameters
      * @param ctx current context
      */
     private void inviteUser(Command c, CliParams p, ProcessingContext ctx) throws IOException {
         String pid = ctx.getProjectIdMandatory();
         String email = c.getParamMandatory("email");
         String msg = c.getParam("msg");
         String role = c.getParam("role");
         c.paramsProcessed();
 
         ctx.getRestApi(p).inviteUser(pid, email, (msg != null) ? (msg) : (""), role);
         l.info("Successfully invited user " + email + " to the project " + pid);
     }
 
     /**
      * Migrate specified datasets
      *
      * @param c   command
      * @param p   cli parameters
      * @param ctx current context
      */
     private void migrateDatasets(Command c, CliParams p, ProcessingContext ctx) throws IOException, InterruptedException {
         String pid = ctx.getProjectIdMandatory();
         l.info("Migrating project " + pid);
         String configFiles = c.getParamMandatory("configFiles");
         c.paramsProcessed();
 
         if (configFiles != null && configFiles.length() > 0) {
             String[] schemas = configFiles.split(",");
             if (schemas != null && schemas.length > 0) {
                 List<String> manifests = new ArrayList<String>();
                 for (String schema : schemas) {
                     File sf = new File(schema);
                     if (sf.exists()) {
                         SourceSchema srcSchema = SourceSchema.createSchema(sf);
                         String ssn = srcSchema.getName();
                         List<Column> columns = AbstractConnector.populateColumnsFromSchema(srcSchema);
                         SLI sli = ctx.getRestApi(p).getSLIById("dataset." + ssn, pid);
                         String manifest = sli.getSLIManifest(columns);
                         manifests.add(manifest);
                     } else {
                         l.debug("The configFile " + schema + " doesn't exists!");
                         l.error("The configFile " + schema + " doesn't exists!");
                         throw new InvalidParameterException("The configFile " + schema + " doesn't exists!");
                     }
                 }
                 String taskUri = ctx.getRestApi(p).migrateDataSets(pid, manifests);
                 if (taskUri != null && taskUri.length() > 0) {
                     l.debug("Checking migration status.");
                     String status = "";
                     while (!"OK".equalsIgnoreCase(status) && !"ERROR".equalsIgnoreCase(status) && !"WARNING".equalsIgnoreCase(status)) {
                         status = ctx.getRestApi(p).getMigrationStatus(taskUri);
                         l.debug("Migration status = " + status);
                         Thread.sleep(500);
                     }
                     l.info("Migration finished with status " + status);
                 } else {
                     l.info("No migration needed anymore.");
                 }
             } else {
                 l.debug("The configFiles parameter must contain a comma separated list of schema configuration files!");
                 l.error("The configFiles parameter must contain a comma separated list of schema configuration files!");
                 throw new InvalidParameterException("The configFiles parameter must contain a comma separated list of schema configuration files!");
             }
         } else {
             l.debug("The configFiles parameter must contain a comma separated list of schema configuration files!");
             l.error("The configFiles parameter must contain a comma separated list of schema configuration files!");
             throw new InvalidParameterException("The configFiles parameter must contain a comma separated list of schema configuration files!");
         }
     }
 
     /**
      * Generate manifests for specified datasets
      *
      * @param c   command
      * @param p   cli parameters
      * @param ctx current context
      */
     private void generateManifests(Command c, CliParams p, ProcessingContext ctx) throws IOException, InterruptedException {
         String pid = ctx.getProjectIdMandatory();
         l.info("Generating manifests for project " + pid);
         String configFiles = c.getParamMandatory("configFiles");
         String dir = c.getParamMandatory("dir");
         c.paramsProcessed();
         if (dir != null && dir.length() > 0) {
             File targetDir = new File(dir);
             if (targetDir.exists() && targetDir.isDirectory()) {
                 if (configFiles != null && configFiles.length() > 0) {
                     String[] schemas = configFiles.split(",");
                     if (schemas != null && schemas.length > 0) {
                         for (String schema : schemas) {
                             File sf = new File(schema);
                             if (sf.exists()) {
                                 SourceSchema srcSchema = SourceSchema.createSchema(sf);
                                 String ssn = srcSchema.getName();
                                 List<Column> columns = AbstractConnector.populateColumnsFromSchema(srcSchema);
                                 SLI sli = ctx.getRestApi(p).getSLIById("dataset." + ssn, pid);
                                 String manifest = sli.getSLIManifest(columns);
                                 FileUtil.writeStringToFile(manifest, targetDir.getAbsolutePath() +
                                         System.getProperty("file.separator") + ssn + ".json");
                             } else {
                                 l.debug("The configFile " + schema + " doesn't exists!");
                                 l.error("The configFile " + schema + " doesn't exists!");
                                 throw new InvalidParameterException("The configFile " + schema + " doesn't exists!");
                             }
                         }
                     } else {
                         l.debug("The configFiles parameter must contain a comma separated list of schema configuration files!");
                         l.error("The configFiles parameter must contain a comma separated list of schema configuration files!");
                         throw new InvalidParameterException("The configFiles parameter must contain a comma separated list of schema configuration files!");
                     }
                 } else {
                     l.debug("The configFiles parameter must contain a comma separated list of schema configuration files!");
                     l.error("The configFiles parameter must contain a comma separated list of schema configuration files!");
                     throw new InvalidParameterException("The configFiles parameter must contain a comma separated list of schema configuration files!");
                 }
             } else {
                 l.debug("The `dir` parameter must point to a valid directory.");
                 l.error("The `dir` parameter must point to a valid directory.");
                 throw new InvalidParameterException("The `dir` parameter must point to a valid directory.");
             }
         } else {
             l.debug("Please specify a valid `dir` parameter for the GenerateManifests command.");
             l.error("Please specify a valid `dir` parameter for the GenerateManifests command.");
             throw new InvalidParameterException("Please specify a valid `dir` parameter for the GenerateManifests command.");
         }
     }
 
 
     /**
      * Retrieves a MD object
      *
      * @param c   command
      * @param p   cli parameters
      * @param ctx current context
      */
     private void getMdObject(Command c, CliParams p, ProcessingContext ctx) throws IOException {
         String pid = ctx.getProjectIdMandatory();
         String ids = c.getParamMandatory("id");
         String fl = c.getParamMandatory("file");
         c.paramsProcessed();
 
         int id;
         try {
             id = Integer.parseInt(ids);
         } catch (NumberFormatException e) {
             throw new InvalidParameterException("The id in getMetadataObject must be an integer.");
         }
         MetadataObject ret = ctx.getRestApi(p).getMetadataObject(pid, id);
         FileUtil.writeJSONToFile(ret, fl);
         l.info("Retrieved metadata object " + id + " from the project " + pid + " and stored it in file " + fl);
     }
 
     /**
      * Stores a MD object
      *
      * @param c   command
      * @param p   cli parameters
      * @param ctx current context
      */
     private void storeMdObject(Command c, CliParams p, ProcessingContext ctx) throws IOException {
         String pid = ctx.getProjectIdMandatory();
         String fl = c.getParamMandatory("file");
         String ids = c.getParam("id");
         c.paramsProcessed();
 
         if (ids != null && ids.length() > 0) {
             int id;
             try {
                 id = Integer.parseInt(ids);
             } catch (NumberFormatException e) {
                 throw new InvalidParameterException("The id in storeMetadataObject must be an integer.");
             }
             ctx.getRestApi(p).modifyMetadataObject(pid, id, FileUtil.readJSONFromFile(fl));
             l.info("Modified metadata object " + id + " to the project " + pid);
         } else {
             ctx.getRestApi(p).createMetadataObject(pid, FileUtil.readJSONFromFile(fl));
             l.info("Created a new metadata object in the project " + pid);
         }
 
     }
 
     /**
      * Drops a MD object
      *
      * @param c   command
      * @param p   cli parameters
      * @param ctx current context
      */
     private void dropMdObject(Command c, CliParams p, ProcessingContext ctx) throws IOException {
         String pid = ctx.getProjectIdMandatory();
         String ids = c.getParamMandatory("id");
         c.paramsProcessed();
 
         int id;
         try {
             id = Integer.parseInt(ids);
         } catch (NumberFormatException e) {
             throw new InvalidParameterException("The id in dropMetadataObject must be an integer.");
         }
         ctx.getRestApi(p).deleteMetadataObject(pid, id);
         l.info("Dropped metadata object " + id + " from the project " + pid);
     }
 
     /**
      * Enumerate reports
      *
      * @param c   command
      * @param p   cli parameters
      * @param ctx current context
      */
     private void getReports(Command c, CliParams p, ProcessingContext ctx) throws IOException {
         String pid = ctx.getProjectIdMandatory();
         String fileName = c.getParamMandatory("fileName");
         c.paramsProcessed();
 
         List<String> uris = ctx.getRestApi(p).enumerateReports(pid);
         String result = "";
         for (String uri : uris) {
             if (result.length() > 0)
                 result += "\n" + uri;
             else
                 result += uri;
         }
         FileUtil.writeStringToFile(result, fileName);
         l.info("Reports written into " + fileName);
     }
 
     /**
      * Enumerate reports
      *
      * @param c   command
      * @param p   cli parameters
      * @param ctx current context
      */
     private void executeReports(Command c, CliParams p, ProcessingContext ctx) throws IOException, InterruptedException {
         String pid = ctx.getProjectIdMandatory();
         String fileName = c.getParamMandatory("fileName");
         c.paramsProcessed();
 
         String result = FileUtil.readStringFromFile(fileName).trim();
         if (result != null && result.length() > 0) {
             String[] uris = result.split("\n");
             for (String uri : uris) {
                 try {
                     String defUri = ctx.getRestApi(p).getReportDefinition(uri.trim());
                     l.info("Executing report uri=" + defUri);
                     String task = ctx.getRestApi(p).executeReportDefinition(defUri.trim());
                     l.info("Report " + defUri + " execution finished: " + task);
                 } catch (GdcRestApiException e) {
                     l.debug("The report uri=" + uri + " can't be computed!");
                     l.info("The report uri=" + uri + " can't be computed!");
                 }
             }
         } else {
             throw new IOException("There are no reports to execute.");
         }
         l.info("All reports executed.");
     }
 
     /**
      * Store project command processor
      *
      * @param c   command
      * @param p   cli parameters
      * @param ctx current context
      * @throws IOException in case of an IO issue
      */
     private void storeProject(Command c, CliParams p, ProcessingContext ctx) throws IOException {
         String fileName = c.getParamMandatory("fileName");
         c.paramsProcessed();
 
         String pid = ctx.getProjectIdMandatory();
         FileUtil.writeStringToFile(pid, fileName);
         l.debug("Stored project id=" + pid + " to " + fileName);
         l.info("Stored project id=" + pid + " to " + fileName);
     }
 
     /**
      * Retrieve project command processor
      *
      * @param c   command
      * @param p   cli parameters
      * @param ctx current context
      * @throws IOException in case of an IO issue
      */
     private void retrieveProject(Command c, CliParams p, ProcessingContext ctx) throws IOException {
         String fileName = c.getParamMandatory("fileName");
         c.paramsProcessed();
 
         ctx.setProjectId(FileUtil.readStringFromFile(fileName).trim());
         l.debug("Retrieved project id=" + ctx.getProjectId() + " from " + fileName);
         l.info("Retrieved project id=" + ctx.getProjectId() + " from " + fileName);
     }
 
     /**
      * Lock project command processor
      *
      * @param c   command
      * @param p   cli parameters
      * @param ctx current context
      * @throws IOException in case of an IO issue
      */
     private void lock(Command c, CliParams p, ProcessingContext ctx) throws IOException {
         final String path = c.getParamMandatory("path");
         c.paramsProcessed();
 
         final File lock = new File(path);
         if (!lock.createNewFile()) {
             if (System.currentTimeMillis() - lock.lastModified() > LOCK_EXPIRATION_TIME) {
                 lock.delete();
                 if (!lock.exists()) {
                     lock(c, p, ctx); // retry
                 }
             }
             l.debug("A concurrent process found using the " + path + " lock file.");
             throw new IOException("A concurrent process found using the " + path + " lock file.");
         }
         lock.deleteOnExit();
     }
 
     /**
      * Instantiate all known connectors
      * TODO: this should be automated
      *
      * @return array of all active connectors
      * @throws IOException in case of IO issues
      */
     private Connector[] instantiateConnectors() throws IOException {
         return new Connector[]{
                 CsvConnector.createConnector(),
                 GaConnector.createConnector(),
                 SfdcConnector.createConnector(),
                 JdbcConnector.createConnector(),
                 PtConnector.createConnector(),
                 DateDimensionConnector.createConnector(),
                 FacebookConnector.createConnector(),
                 FacebookInsightsConnector.createConnector(),
                 MsDynamicsConnector.createConnector(),
                 SugarCrmConnector.createConnector(),
                 ChargifyConnector.createConnector()
 
         };
     }
 
     /**
      * Loads default values of common parameters from a properties file searching
      * the working directory and user's home.
      *
      * @return default configuration
      */
     private static Properties loadDefaults() {
         final String[] dirs = new String[]{"user.dir", "user.home"};
         final Properties props = new Properties();
         for (final String d : dirs) {
             String path = System.getProperty(d) + File.separator + DEFAULT_PROPERTIES;
             File f = new File(path);
             if (f.exists() && f.canRead()) {
                 try {
                     FileInputStream is = new FileInputStream(f);
                     props.load(is);
                     l.debug("Successfully red the gdi configuration from '" + f.getAbsolutePath() + "'.");
                     return props;
                 } catch (IOException e) {
                     l.warn("Readable gdi configuration '" + f.getAbsolutePath() + "' found be error occurred reading it.");
                     l.debug("Error reading gdi configuration '" + f.getAbsolutePath() + "': ", e);
                 }
             }
         }
         return props;
     }
 
 }
