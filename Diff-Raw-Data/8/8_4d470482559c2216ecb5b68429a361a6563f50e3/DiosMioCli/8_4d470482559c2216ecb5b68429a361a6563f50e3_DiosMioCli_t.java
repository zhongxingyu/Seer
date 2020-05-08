 package net.alaux.diosmio.ui.cli;
 
 import net.alaux.diosmio.ui.cli.core.CliArtifactManagerJmxActions;
 import net.alaux.diosmio.ui.cli.core.CliMiscJmxActions;
 import net.alaux.diosmio.ui.cli.net.alaux.logging.KissLogger;
 import org.apache.commons.cli.*;
 
 
 import javax.management.*;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 
 
 public class DiosMioCli {
 
     private static Options options;
     private static DiosMioCli diosmioCli;
 
 
 
     private static final String DM_CLI_VERSION = "1.0-SNAPSHOT";
 
     // Technical opt
     private static final String OPT_TECH_HELP = "h";
     private static final String OPT_TECH_HELP_L = "help";
 
     private static final String OPT_TECH_LOG_WARN = "v";
     private static final String OPT_TECH_LOG_INFO = "vv";
     private static final String OPT_TECH_LOG_DEBUG = "vvv";
 
     public static final String OPT_TECH_CONFIG_FILE_L = "conf";
     public static final String OPT_TECH_CONFIG_FILE = "c";
 
     // Misc opt
 //    private static final String OPT_MISC_SHOW_BEANS = "s";
     private static final String OPT_MISC_LIST_BEANS_L = "lsbeans";
 
 //    private static final String OPT_MISC_STATUS = "???";
     private static final String OPT_MISC_SHOW_STATUS_L = "status";
 
     // Artifact Manager opt
 //    private static final String OPT_ARTIF_MNGR_ADD = "a";
     private static final String OPT_ARTIF_MNGR_ADD_L = "add";
 
     //    private static final String OPT_ARTIF_MNGR_LIST = "l";
     private static final String OPT_ARTIF_MNGR_LIST_L = "ls";
 
     private static final String OPT_ARTIF_MNGR_GET_L = "get";
 
 //    private static final String OPT_ARTIF_MNGR_DEL = "d";
     private static final String OPT_ARTIF_MNGR_DEL_L = "rm";
 
     private static KissLogger logger;
 
     // TODO Get this value from config file
     private static final String DIOSMIOCLI_LOG_PATH = "/tmp/diosmio-cli.log";
 
     private static final String DEFAULT_CONF_PATH = "diosmio-cli.conf";
     private static String configFilePath;
     private static Properties properties;
 
 
     public static String getProperty(String key) {
         return properties.getProperty(key);
     }
 
     /**
      *
      * @param filePath
      * @param defaultFilePath
      * @return
      * @throws IOException
      * @throws Exception
      */
     public Properties getDiosMioProperties(String filePath, String defaultFilePath) throws IOException, Exception {
 
         logger.info("Trying to load property file");
 
         InputStream is = null;
         if (filePath != null) {
             File f = new File(filePath);
             if (f.exists() && f.canRead()) {
                 is = new FileInputStream(f);
             } else {
                 logger.error("Cannot access specified configuration file (" + filePath + ")");
             }
         }
 
         if (is == null) {
             logger.info("Falling back to default configuration file (" + defaultFilePath + ")");
             is = ClassLoader.getSystemClassLoader().getResourceAsStream(defaultFilePath);
         }
 
         if (is == null) {
             // TODO Handle this properly
             throw new Exception("cli.no_config_file");
         }
 
         Properties res =  new Properties();
         res.load(is);
         return res;
     }
 
     /**
      *
      * @throws IOException
      * @throws MalformedObjectNameException
      * @throws InstanceNotFoundException
      */
     public DiosMioCli(String[] args) {
 
         try {
             // Parse command line
             this.options = createOptions();
 
             CommandLineParser parser = new PosixParser();
             CommandLine cmd = parser.parse(this.options, args);
 
             // TODO Make some command line basic verification (ie "One OPT at least", "Only one OPT")
 
             executeAction(cmd);
 
         } catch (Exception e) {
             System.err.println(e.getMessage());
             logger.error(e.getMessage());
         } finally {
             if (logger != null) {
                 logger.info("I'm done!");
                 logger.info("----------------------");
                 logger.close();
             }
         }
     }
 
     private Options createOptions() {
 
         Options opt = new Options();
 
         // Technical opt
         opt.addOption(OPT_TECH_HELP, OPT_TECH_HELP_L, false, "give this help");
 
         opt.addOption(OPT_TECH_LOG_WARN, false, "display 'warning' logs");
         opt.addOption(OPT_TECH_LOG_INFO, false, "display 'info' logs");
         opt.addOption(OPT_TECH_LOG_DEBUG, false, "display 'debug' logs");
 
         opt.addOption(OptionBuilder.hasArg()
                 .withArgName("file")
                 .withDescription("use alternate config file")
                 .withLongOpt(OPT_TECH_CONFIG_FILE_L)
                 .create(OPT_TECH_CONFIG_FILE));
 
         // Misc
        opt.addOption(OptionBuilder.withDescription("list available beans")
                .withLongOpt(OPT_MISC_LIST_BEANS_L)
                .create());
        opt.addOption(OptionBuilder.withDescription("show application status")
                .withLongOpt(OPT_MISC_SHOW_STATUS_L)
                .create());
 
         // Artifact Manager
         opt.addOption(OptionBuilder.hasArg()
                 .withArgName("artifact")
                 .withDescription("add artifact to database")
                 .withLongOpt(OPT_ARTIF_MNGR_ADD_L)
                 .create());
 
         opt.addOption(OptionBuilder.hasArg()
                 .withArgName("artifactId")
                 .withType(Long.class)
                 .withDescription("list artifacts in database")
                 .withLongOpt(OPT_ARTIF_MNGR_GET_L)
                 .create());
 
         opt.addOption(OptionBuilder.withDescription("show an artifact")
                 .withLongOpt(OPT_ARTIF_MNGR_LIST_L)
                 .create());
 
         opt.addOption(OptionBuilder.hasArg()
                 .withArgName("artifactId")
                 .withType(Long.class)
                 .withDescription("delete artifact from database")
                 .withLongOpt(OPT_ARTIF_MNGR_DEL_L)
                 .create());
 
         return  opt;
     }
 
     private void executeAction(CommandLine cmd) throws ParseException, IOException, MalformedObjectNameException, Exception {
 
         // First process log level stuff
         if (cmd.hasOption(OPT_TECH_LOG_WARN)) {
             logger = new KissLogger(KissLogger.Level.WARNING, DIOSMIOCLI_LOG_PATH);
         } else if (cmd.hasOption(OPT_TECH_LOG_INFO)) {
             logger = new KissLogger(KissLogger.Level.INFO, DIOSMIOCLI_LOG_PATH);
         } else if (cmd.hasOption(OPT_TECH_LOG_DEBUG)) {
             logger = new KissLogger(KissLogger.Level.DEBUG, DIOSMIOCLI_LOG_PATH);
         } else {
             logger = new KissLogger(KissLogger.Level.SHUT_UP, DIOSMIOCLI_LOG_PATH);
         }
 
         logger.info("Parsing opt");
 
         // Other technical opt ************************
         properties = getDiosMioProperties(cmd.getOptionValue(OPT_TECH_CONFIG_FILE), DEFAULT_CONF_PATH);
 
         // Misc *******************************************
         if (cmd.hasOption(OPT_TECH_HELP)) {
             HelpFormatter formatter = new HelpFormatter();
             formatter.printHelp("diosmio-cli <option> [...]", this.options);
 
         } else if (cmd.hasOption(OPT_MISC_LIST_BEANS_L)) {
             logger.info("Option '" + OPT_MISC_LIST_BEANS_L + "' found");
             CliMiscJmxActions cliMiscJmxActions = new CliMiscJmxActions();
             cliMiscJmxActions.displayMBeanList();
             cliMiscJmxActions.closeJmxConnection();
 
         } else if (cmd.hasOption(OPT_MISC_SHOW_STATUS_L)) {
             logger.info("Option '" + OPT_MISC_SHOW_STATUS_L + "' found");
             CliMiscJmxActions cliMiscJmxActions = new CliMiscJmxActions();
             cliMiscJmxActions.displayStatus();
             cliMiscJmxActions.closeJmxConnection();
 
             // Artifact Manager ***************************
         } else if (cmd.hasOption(OPT_ARTIF_MNGR_LIST_L)) {
             logger.info("Option '" + OPT_ARTIF_MNGR_LIST_L+ "' found");
             CliArtifactManagerJmxActions cliArtifactManagerJmxActions = new CliArtifactManagerJmxActions();
             cliArtifactManagerJmxActions.listAllArtifacts();
             cliArtifactManagerJmxActions.closeJmxConnection();
 
         } else if (cmd.hasOption(OPT_ARTIF_MNGR_GET_L)) {
             logger.info("Option '" + OPT_ARTIF_MNGR_GET_L + "' found with value '" + cmd.getOptionValue(OPT_ARTIF_MNGR_GET_L) + "'");
             CliArtifactManagerJmxActions cliArtifactManagerJmxActions = new CliArtifactManagerJmxActions();
             cliArtifactManagerJmxActions.showArtifact(new Long(cmd.getOptionValue(OPT_ARTIF_MNGR_GET_L)));
             cliArtifactManagerJmxActions.closeJmxConnection();
 
         } else if (cmd.hasOption(OPT_ARTIF_MNGR_ADD_L)) {
             logger.info("Option '" + OPT_ARTIF_MNGR_ADD_L + "' found with value '" + cmd.getOptionValue(OPT_ARTIF_MNGR_ADD_L) + "'");
             CliArtifactManagerJmxActions cliArtifactManagerJmxActions = new CliArtifactManagerJmxActions();
             cliArtifactManagerJmxActions.create(cmd.getOptionValue(OPT_ARTIF_MNGR_ADD_L));
             cliArtifactManagerJmxActions.closeJmxConnection();
 
         } else if (cmd.hasOption(OPT_ARTIF_MNGR_DEL_L)) {
             logger.info("Option '" + OPT_ARTIF_MNGR_DEL_L + "' found with value '" + cmd.getOptionValue(OPT_ARTIF_MNGR_DEL_L) + "'");
             CliArtifactManagerJmxActions cliArtifactManagerJmxActions = new CliArtifactManagerJmxActions();
             cliArtifactManagerJmxActions.delete(new Long(cmd.getOptionValue(OPT_ARTIF_MNGR_DEL_L)));
             cliArtifactManagerJmxActions.closeJmxConnection();
         }
     }
 
     /**
      *
      * @param args
      * @throws Exception
      */
     public static void main(String[] args) {
         diosmioCli = new DiosMioCli(args);
     }
 }
