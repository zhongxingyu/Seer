 package polly;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.lang.management.ManagementFactory;
 import java.nio.channels.FileLock;
 import java.util.Arrays;
 
 import javax.naming.ConfigurationException;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 import polly.commandline.AbstractArgumentParser;
 import polly.commandline.ParameterException;
 import polly.commandline.PollyArgumentParser;
 import polly.configuration.ConfigurationProviderImpl;
 import polly.core.ModuleBootstrapper;
 import polly.core.ShutdownManagerImpl;
 import polly.moduleloader.AbstractProvider;
 import polly.moduleloader.DefaultModuleLoader;
 import polly.moduleloader.ModuleLoader;
 import polly.moduleloader.SetupException;
 import polly.moduleloader.annotations.Module;
 import polly.moduleloader.annotations.Provide;
 import polly.util.FileUtil;
 import polly.util.ProxyClassLoader;
 import de.skuzzle.polly.sdk.Configuration;
 import de.skuzzle.polly.sdk.Version;
 
 
 
 public class Polly {
 
     public static Version getPollyVersion() {
         String version = Polly.class.getPackage().getImplementationVersion();
         if (version == null) {
             return new Version(DEVELOP_VERSION);
         } else {
             return new Version(version);
         }
     }
 
 
 
     public static String getPid() {
         String[] parts = ManagementFactory.getRuntimeMXBean().getName()
             .split("@");
         return parts[0];
     }
 
 
 
     public static File getPollyPath() {
         return new File(".");
     }
 
     private static String commandLine = "";
     private static String[] commandLineArgs = {};
 
 
 
     public static String getCommandLine() {
         return commandLine;
     }
     
     
     
     public static String[] getCommandLineArgs() {
         return commandLineArgs;
     }
     
     
 
     public final static String DEVELOP_VERSION = "0.9.1 - dev";
     public final static String CONFIG_FULL_PATH = "cfg/polly.cfg";
     public final static String CONFIG_FILE_NAME = "polly.cfg";
     public final static String CONFIG_DIR = "./cfg";
     
     /**
      * The polly plugin folder. That folder is not supposed to change
      */
     public final static String PLUGIN_FOLDER = "plugins";
 
     private static Logger logger = Logger.getLogger(Polly.class.getName());
 
 
 
     /**
      * Start polly.
      * 
      * @param args
      *            The commandline arguments passed to polly. This array may be
      *            empty
      */
     public synchronized static void main(String[] args) throws Exception {
         commandLineArgs = args;
         for (String arg : args) {
             if (arg.indexOf(' ') != -1) {
                 commandLine += "\"" + arg + "\" ";
             } else {
                 commandLine += arg + " ";
             }
         }
         commandLine = commandLine.trim();
 
         ProxyClassLoader parentCl = (ProxyClassLoader) 
                     Thread.currentThread().getContextClassLoader();
         
         new Polly(args, parentCl);
     }
 
 
 
     public Polly(String[] args, ProxyClassLoader parentCl) {
         if (!this.lockInstance(new File("polly.lck"))) {
             System.out.println("Polly already running");
             return;
         }
         if (!FileUtil.waitFor("polly.installer.jar")) {
             System.out.println("Updates still running. Exiting!");
             return;
         }
         
         
         ConfigurationProviderImpl configurationProvider = 
             new ConfigurationProviderImpl(new File(CONFIG_DIR));
         
         Configuration pollyCfg = null;
         try {
             pollyCfg = configurationProvider.open(CONFIG_FILE_NAME, true, 
                     ConfigurationProviderImpl.NOP_VALIDATOR);
             PropertyConfigurator.configure(
                 pollyCfg.readString(Configuration.LOG_CONFIG_FILE));
         } catch (FileNotFoundException e) {
             System.out.println("Error while opening the main configuration file");
             System.out.println("File '" + CONFIG_FILE_NAME + "' not found");
             e.printStackTrace();
         } catch (IOException e) {
             System.out.println("Error while opening the main configuration file");
             System.out.println("IO Exception: " + e.getMessage());
             e.printStackTrace();
         } catch (ConfigurationException e) {
             System.out.println("Error while opening the main configuration file");
             System.out.println("The configuration could not be validated. Reason: " + 
                     e.getMessage());
             e.printStackTrace();
         }
         
         logger.info("");
         logger.info("");
         logger.info("");
         this.parseArguments(args, pollyCfg);
 
         logger.info("----------------------------------------------");
         logger.info("new polly session started!");
         logger.info("----------------------------------------------");
         logger.info("");
         logger.info("Config file read from '" + CONFIG_FULL_PATH + "'");
         logger.info("Polly command line arguments: " + Arrays.toString(args));
         logger.info("(Canonical: " + getCommandLine() + ")");
         Version version = getPollyVersion();
         logger.info("Version info: " + version);
         logger.info("Java Version: " + System.getProperty("java.version"));
         logger.info("Java Home: " + System.getProperty("java.home"));
 
 
         /*
          * POLLY INITIALIZATION:
          * 
          * Each component to be set up has its own Module subclass which does
          * all the set up things for this component. While setting up, this
          * module can provide all components that it has initialized to the
          * other modules that will be setup next. This process is executed by
          * the ModuleBlackboard.
          * 
          * Each module must report its dependencies and provided components in the
          * constructor. Given that information for every single module, the module
          * loader determines the order in which the modules are loaded so all 
          * dependencies can be satisfied.
          * 
          * The modules to be loaded must be specified in the modules.cfg as pointed to
          * by Configuration#MODULES_CONFIG.
          */
 
         // Setup Module loader
         ModuleLoader loader = new DefaultModuleLoader();
 
         // Modules:
         // Register all the modules. The order in which they are registered does not
         // matter as the setup order will be automatically determined by the 
         // ModuleLoader. Just make sure your module properly reports all its 
         // dependencies!
         
         
         try {
             // This parses the modules.cfg and instantiates all listed modules
             File modulesCfg = new File(pollyCfg.readString(Configuration.MODULE_CONFIG));
             ModuleBootstrapper.prepareModuleLoader(loader, modulesCfg);
             
             // add startup module
             new AnonymousModule(loader, configurationProvider, parentCl);
             
             loader.runSetup();
             loader.runModules();
             
             if (pollyCfg.readBoolean(Configuration.DEBUG_MODE)) {
                 loader.exportToDot(new File("modules.dot"));
             }
         } catch (Exception e) {
             logger.fatal("Crucial component failed to load!", e);
             
             ShutdownManagerImpl s = loader.requireNow(ShutdownManagerImpl.class);
             s.shutdown(true);
         } finally {
             loader.dispose();
         }
 
         logger.info("Polly succesfully set up.");
     }
 
     
     
        @Module(startUp = true, provides = {
         @Provide(component = ShutdownManagerImpl.class),
         @Provide(component = ConfigurationProviderImpl.class),
         @Provide(component = ProxyClassLoader.class),
     })
     private static class AnonymousModule extends AbstractProvider {
         
         private ConfigurationProviderImpl configurationProvider;
         private ProxyClassLoader parentCl;
         
         
         public AnonymousModule(ModuleLoader loader, 
                 ConfigurationProviderImpl configurationProvider,  
                 ProxyClassLoader parentCl) {
             super("ROOT_PROVIDER", loader, true);
             this.parentCl = parentCl;
             this.configurationProvider = configurationProvider;
         }
 
         
         
         @Override
         public void setup() throws SetupException {
             this.provideComponent(this.configurationProvider);
             this.provideComponent(this.parentCl);
             this.provideComponent(new ShutdownManagerImpl());
         }
         
     }
 
 
 
     private boolean lockInstance(final File file) {
         try {
             final boolean existed = file.exists();
             if (!existed) {
                 file.createNewFile();
             }
             final RandomAccessFile randomAccessFile = new RandomAccessFile(
                 file, "rw");
             final FileLock fileLock = randomAccessFile.getChannel().tryLock();
             if (fileLock != null) {
                 Runtime.getRuntime().addShutdownHook(new Thread() {
 
                     public void run() {
                         try {
                             fileLock.release();
                             randomAccessFile.close();
                             if (!existed) {
                                 file.delete();
                             }
                         } catch (Exception e) {
                             e.printStackTrace();
                         }
                     }
                 });
                 return true;
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return false;
     }
 
 
 
     private void parseArguments(String[] args, Configuration config) {
         try {
             AbstractArgumentParser parser = new PollyArgumentParser(config);
             parser.parse(args);
             commandLine = parser.getCanonicalArguments();
         } catch (ParameterException e) {
             logger.warn(e.getMessage(), e);
             this.printHelp();
             System.exit(0);
         }
     }
 
 
 
     private void printHelp() {
         System.out.println("Polly Parameterbersicht:");
         System.out.println("Name: \t\tParameter:\t Beschreibung:");
         System.out.println("(-log | -l) \t<'on'|'off'> \t Schaltet IRC "
             + "log ein/aus.");
         System.out
             .println("(-nick | -n) \t<nickname> \t Nickname fr den Bot.");
         System.out
             .println("(-ident | -i) \t<ident> \t Passwort fr den Nickserv.");
         System.out
             .println("(-server | -s) \t<server> \t Server zu dem sich der Bot "
                 + "verbindet. Nutzt 6669 als Port wenn nicht anders angegeben.");
         System.out
             .println("(-port | -p) \t<port> \t\t Port fr den IRC-Server.");
         System.out.println("(-join | -j) \t<#c1,..#cn> \t Joined nur die/den "
             + "angegebenen Channel.");
         System.out
             .println("(-update | -u) \t<'on'|'off'> \t Auto update ein/aus.");
         System.out
             .println("-telnet \t<'on'|'off'>\t Telnet-Server aktivieren.");
         System.out.println("-telnetport \t<port>\t\t Telnet-Port setzen.");
         System.out.println("(-help | -?) \t\t\t Diese bersicht anzeigen.");
         System.out.println("Beliebige Taste drcken zum Beenden.");
         try {
             System.in.read();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 }
