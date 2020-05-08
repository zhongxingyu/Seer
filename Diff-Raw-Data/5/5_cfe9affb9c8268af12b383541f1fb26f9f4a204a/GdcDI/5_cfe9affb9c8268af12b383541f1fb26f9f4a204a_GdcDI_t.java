 package com.gooddata.processor;
 
 import java.io.*;
 import java.util.*;
 
 import com.gooddata.connector.*;
 import com.gooddata.connector.backend.DerbyConnectorBackend;
 import com.gooddata.connector.backend.MySqlConnectorBackend;
 import com.gooddata.processor.parser.DIScriptParser;
 import com.gooddata.processor.parser.ParseException;
 
 import com.gooddata.exception.*;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.Options;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 import org.gooddata.connector.Connector;
 
 import com.gooddata.util.FileUtil;
 import org.gooddata.connector.backend.ConnectorBackend;
 
 /**
  * The GoodData Data Integration CLI processor.
  *
  * @author jiri.zaloudek
  * @author Zdenek Svoboda <zd@gooddata.org>
  * @version 1.0
  */
 public class GdcDI implements Executor {
 
     private static Logger l = Logger.getLogger(GdcDI.class);
 
     private String projectId = null;
     private Connector connector = null;
     private CliParams cliParams = null;
     private Connector[] connectors = null;
 
     private ProcessingContext context = new ProcessingContext();
 
     private static long  LOCK_EXPIRATION_TIME = 1000 * 3600; // 1 hour
 
     private GdcDI(CommandLine ln) {
         try {
             cliParams = CliParams.create(ln);
             connectors = instantiateConnectors();
             String execute = cliParams.get(CliParams.CLI_PARAM_EXECUTE[0]);
             String scripts = cliParams.get(CliParams.CLI_PARAM_SCRIPT);
             if(execute!= null && scripts != null && execute.length()>0 && scripts.length()>0) {
                 l.error("You can't execute a script and use the -e command line parameter at the same time.");
             }
             if(execute!= null && execute.length() > 0) {
                 execute(execute);
             }
             if(scripts!= null && scripts.length() > 0) {
                 String[] sas = scripts.split(",");
                 for(String script : sas)
                     execute(new File(script));
             }
         }
         catch (InvalidArgumentException e) {
             l.error(e.getMessage());
             l.info(cliParams.commandsHelp());
         }
         catch (IOException e) {
             l.error(e.getMessage());
         }
 
     }
 
     /**
      * Prints an err message, help and exits with status code 1
      * @param err the err message
      */
    public static void printErrorAndExit(String err) {
         l.error("ERROR: " + err);
         System.exit(1);
     }
 
 
     public void execute(final String commandsStr) {
         List<Command> cmds = new ArrayList<Command>();
         cmds.addAll(parseCmd(commandsStr));
         for(Command command : cmds) {
             boolean processed = false;
             for(int i=0; i<connectors.length && !processed; i++) {
                 processed = connectors[i].processCommand(command, cliParams, context);
             }
             if(!processed)
                 this.processCommand(command, cliParams, context);
         }
     }
 
     public void execute(final File scriptFile) throws IOException {
         List<Command> cmds = new ArrayList<Command>();
         cmds.addAll(parseCmd(FileUtil.readStringFromFile(scriptFile.getAbsolutePath())));
         for(Command command : cmds) {
             boolean processed = false;
             for(int i=0; i<connectors.length && !processed; i++) {
                 processed = connectors[i].processCommand(command, cliParams, context);
             }
             if(!processed)
                 processed = this.processCommand(command, cliParams, context);
             if(!processed)
                 throw new InvalidCommandException("Unknown command '"+command.getCommand()+"'");
         }
     }
 
     /**
      * The main CLI processor
      * @param args command line argument
      * @throws Exception any issue
      */
     public static void main(String[] args) {
 
         PropertyConfigurator.configure(System.getProperty("log4j.configuration"));
         try {
             Options o = CliParams.getOptions();
             CommandLineParser parser = new GnuParser();
             new GdcDI(parser.parse(o, args));
         } catch (org.apache.commons.cli.ParseException e) {
             l.error("Error parsing command line parameters",e);
         }
     }
 
     /**
      * Parses the commands
      * @param cmd commands string
      * @return array of commands
      * @throws InvalidArgumentException in case there is an invalid command
      */
     protected static List<Command> parseCmd(String cmd) throws InvalidArgumentException {
         try {
             if(cmd != null && cmd.length()>0) {
                 Reader r = new StringReader(cmd);
                 DIScriptParser parser = new DIScriptParser(r);
                 List<Command> commands = parser.parse();
                 l.debug("Running "+commands.size()+" commands.");
                 for(Command c : commands) {
                     l.debug("Command="+c.getCommand()+" params="+c.getParameters());
                 }
                 return commands;
             }
         }
         catch(ParseException e) {
             throw new InvalidArgumentException("Can't parse command '" + cmd + "'");
         }
         throw new InvalidArgumentException("Can't parse command (empty command).");
     }
 
 
     
 
     /**
      * Processes single command
      * @param c command to be processed
      * @param cli parameters (commandline params)
      * @param ctx processing context
      * @return true if the command has been processed, false otherwise
      */
     public boolean processCommand(Command c, CliParams cli, ProcessingContext ctx) throws ProcessingException {
         try {
             if(c.match("CreateProject")) {
                 createProject(c, cli, ctx);
             }
             else if(c.match("OpenProject")) {
                 ctx.setProjectId(c.getParamMandatory("id"));
             }
             else if(c.match("StoreProject")) {
                 storeProject(c, cli, ctx);
             }
             else if(c.match("RetrieveProject")) {
                 retrieveProject(c, cli, ctx);
             }
             else if(c.match( "Lock")) {
                 lock(c, cli, ctx);
             }
             else
                 return false;
         }
         catch (IOException e) {
             throw new ProcessingException(e);
         }
         return true;
     }
 
 
     private void createProject(Command c, CliParams p, ProcessingContext ctx) {
         try {
             String name = c.getParamMandatory("name");
             ctx.setProjectId(ctx.getRestApi(p).createProject(name, name));
             String pid = ctx.getProjectId();
             l.info("Project id = '"+pid+"' created.");
         }
         catch (GdcRestApiException e) {
             l.error("Can't create project. You are most probably over the project count quota. " +
                     "Please try deleting few projects.");            
         }
     }
 
      private void storeProject(Command c, CliParams p, ProcessingContext ctx) throws IOException {
         String fileName = c.getParamMandatory("fileName");
         String pid = ctx.getProjectId();
         FileUtil.writeStringToFile(pid, fileName);
     }
 
     private void retrieveProject(Command c, CliParams p, ProcessingContext ctx) throws IOException {
         String fileName = c.getParamMandatory("fileName");
         ctx.setProjectId(FileUtil.readStringFromFile(fileName).trim());
     }
     
     private void lock(Command c, CliParams p, ProcessingContext ctx) throws IOException {
     	final String path = c.getParamMandatory( "path");
     	final File lock = new File(path);
     	if (!lock.createNewFile()) {
     		if (System.currentTimeMillis() - lock.lastModified() > LOCK_EXPIRATION_TIME) {
     			lock.delete();
     			if (!lock.exists()) {
     				lock(c, p, ctx); // retry
     			}
     		}
    		printErrorAndExit("A concurrent process found using the " + path + " lock file.");	
     	}
     	lock.deleteOnExit();
     }
 
     private Connector[] instantiateConnectors() throws IOException {
         String b = cliParams.get(CliParams.CLI_PARAM_BACKEND[0]);
         ConnectorBackend backend = null;
         if("mysql".equalsIgnoreCase(b))
             backend = MySqlConnectorBackend.create(cliParams.get(CliParams.CLI_PARAM_DB_USERNAME[0]),
                     cliParams.get(CliParams.CLI_PARAM_DB_PASSWORD[0]));
         else if("derby".equalsIgnoreCase(b))
             backend = DerbyConnectorBackend.create();
         return new Connector[] {
             CsvConnector.createConnector(backend),
             GaConnector.createConnector(backend),
             SfdcConnector.createConnector(backend),
             JdbcConnector.createConnector(backend),
             TimeDimensionConnector.createConnector()    
         };
     }
 
 }
