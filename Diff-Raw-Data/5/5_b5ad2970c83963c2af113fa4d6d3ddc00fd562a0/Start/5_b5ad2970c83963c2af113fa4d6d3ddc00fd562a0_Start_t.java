 /*
  * Copyright (C) 2009  Lars Pötter <Lars_Poetter@gmx.de>
  * All Rights Reserved.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License version 2
  * as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, see <http://www.gnu.org/licenses/>
  *
  */
 package org.FriendsUnited;
 
 import java.io.File;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.security.Security;
 import java.util.Locale;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 import javax.crypto.BadPaddingException;
 import javax.crypto.Cipher;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.NoSuchPaddingException;
 import javax.crypto.SecretKey;
 import javax.crypto.spec.SecretKeySpec;
 
 import org.FriendsUnited.ConfigurationCreator.Main;
 import org.FriendsUnited.NetworkLayer.Supervisor;
 import org.FriendsUnited.UserInterface.Startup;
 import org.FriendsUnited.Util.File.FileSystem;
 import org.FriendsUnited.Util.Option.BooleanOption;
 import org.FriendsUnited.Util.Option.IntegerOption;
 import org.FriendsUnited.Util.Option.OptionCollection;
 import org.FriendsUnited.Util.Option.StringOption;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 import org.apache.log4j.Appender;
 import org.apache.log4j.ConsoleAppender;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.SimpleLayout;
 import org.apache.log4j.xml.DOMConfigurator;
 import org.bouncycastle.jce.provider.BouncyCastleProvider;
 
 /** Start FriendsUnited.
  * @author Lars P&ouml;tter
  * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
  */
 public final class Start
 {
     private boolean startNetwork;
     private boolean startGui;
     private boolean startConfig;
     private Options options;
     private Logger rootlog = null;
     private final Logger log;
     private FileSystem fs;
 
     /**
      *
      * @param cmd
      * @param cfg
      */
     private void evaluateGuiCommandLine(final CommandLine cmd, final OptionCollection cfg)
     {
         // Mode settings
         if(cmd.hasOption("s"))
         {
             log.debug("found Option s");
             // user specified the Server to connect the GUI to
             final String ServerUrl = cmd.getOptionValue("s");
             if(null != ServerUrl)
             {
                 if(true == ServerUrl.startsWith("TCP:"))
                 {
                     final String host = ServerUrl.substring(ServerUrl.indexOf(":") + 1);
                     try
                     {
                         InetAddress.getByName(host);
                         final StringOption ip = new StringOption("ServerIP", host);
                         cfg.add(ip);
                         final IntegerOption port = new IntegerOption("ServerPort",
                                                                      Defaults.TCP_CONTROL_INTERFACE_PORT);
                         cfg.add(port);
                         log.debug("configured IP and Port TCP");
                     }
                     catch(final UnknownHostException e)
                     {
                         // Invalid URL
                         System.err.println("Invalid Server of " + host);
                     }
                 }
                 if(true == ServerUrl.startsWith("SSL:"))
                 {
                     final String host = ServerUrl.substring(ServerUrl.indexOf(":") + 1);
                     try
                     {
                         InetAddress.getByName(host);
                         final StringOption ip = new StringOption("ServerIP", host);
                         cfg.add(ip);
                         final IntegerOption port = new IntegerOption("ServerPort",
                                                                      Defaults.SSL_CONTROL_INTERFACE_PORT);
                         cfg.add(port);
                         log.debug("configured IP and Port SSL");
                     }
                     catch(final UnknownHostException e)
                     {
                         // Invalid URL
                         System.err.println("Invalid Server of " + host);
                     }
                 }
             }
         }
 
         if(cmd.hasOption("a"))
         {
             log.debug("found Option a");
             // User wants to use the GUI in Administrator mode
             final BooleanOption isAdmin = new BooleanOption("isAdmin", true);
             cfg.add(isAdmin);
         }
     }
 
     /**
      *
      */
     private Start()
     {
         // Start Log4J Logger
         initLogging();
         log = Logger.getLogger("org.FriendsUnited.Start");
        fs = new FileSystem(System.getProperty("user.dir"));
     }
 
     /** Initialize the Logging Framework(Log4J).
      */
     private void initLogging()
     {
         // Log4J
         // Start Log4J Logger
         if(true == (new File(Defaults.LOG4J_CONFIG_FILENAME)).canRead())
         {
             DOMConfigurator.configureAndWatch(Defaults.LOG4J_CONFIG_FILENAME);
         }
         else
         {
             // Fall Back - No log4j.xml to configure Logging
             rootlog = Logger.getRootLogger();
             rootlog.setLevel(Level.INFO);
             final SimpleLayout layout = new SimpleLayout();
             final Appender app = new ConsoleAppender(layout);
             rootlog.addAppender(app);
         }
     }
 
     /**
      *
      * @param args
      */
     public void run(final String[] args)
     {
         // Parse command line
         final CommandLineParser parser = new PosixParser();
         initOptions();
         try
         {
             final CommandLine cmd = parser.parse(options, args);
             evaluateCommandLine(cmd);
             final OptionCollection root = new OptionCollection("root");
             final OptionCollection cfg = root.getSubSectionCreateIfAbsent("cfg");
             cfg.importSettingsFromXmlFile(Defaults.XML_CONFIG_FILENAME);
             log.debug("Configuration file loaded");
             if(true == startNetwork)
             {
                 // Start the Network Layer
                 startNetworkLayer(cmd, cfg, root);
             }
             if(true == startGui)
             {
                 // Start the User Interface
                 startGui(cmd, cfg);
             }
             if(true == startConfig)
             {
                 // Start the Configuration Creator
                 startConfigurationCreator(cfg);
             }
         }
         catch (final ParseException e)
         {
             System.err.println(e.getLocalizedMessage());
             printHelp();
             System.exit(3);
         }
     }
 
     /** create Options object.
      */
     private void initOptions()
     {
         options = new Options();
         // Connect gui to URL
         options.addOption("d",
                           "directory",
                           true,
                           "use the specified Directory instead of the current directory");
         // add admin Option
         options.addOption("a",
                           "admin",
                           false,
                           "switch gui to Administrator mode");
         // add help Option
         options.addOption("h",
                           "help",
                           false,
                           "print this message");
         // Network only - Headless mode
         options.addOption("n",
                           "network",
                           false,
                           "starts only the Network Layer");
         // GUI only - use remote Network Layer
         options.addOption("g",
                           "gui",
                           false,
                           "starts only the GUI\n"
                           + "for use with another Network Layer\n"
                           + "use -s to specify the Network Layer");
         // Configuration creator only
         // - create a new FriendsUnited Network Definition
         options.addOption("c",
                           "config",
                           false,
                           "starts Configuration creator");
         // GUI and Network Layer - normal Operation
         options.addOption("u",
                           "use",
                           false,
                           "starts GUI and the Network Layer\n"
                           + "this is the default behaviour !");
         // Connect GUI to URL
         options.addOption("s",
                           "server",
                           true,
                           "connect gui to URL Url has to be\n" +
                           "TCP:1.2.3.4 or SSL:1.2.3.4");
         // Command line Interface only - no GUI
         options.addOption("m",
                           "commandline",
                           false,
                           "enable the command line Interface");
     }
 
     /** set state variables according to command line parameters.
      *
      * @param cmd Command Line Parameters
      */
     private void evaluateCommandLine(final CommandLine cmd)
     {
         if(cmd.hasOption("d"))
         {
             log.debug("found Option d");
         // working directory specified
            final String workingDirectory = cmd.getOptionValue("d");
             fs = new FileSystem(workingDirectory);
         }
 
         // Check which Modules should be started
         if(true == cmd.hasOption("h"))
         {
             log.debug("found Option h");
             // Usage info
             startConfig  = false;
             startGui     = false;
             startNetwork = false;
             printHelp();
         }
         else if(cmd.hasOption("n"))
         {
             log.debug("found Option n");
         // Network Layer only
             startConfig  = false;
             startGui     = false;
             startNetwork = true;
         }
         else if(cmd.hasOption("g"))
         {
             log.debug("found Option g");
         // Gui only
             startConfig  = false;
             startGui     = true;
             startNetwork = false;
         }
         else if(cmd.hasOption("c"))
         {
             log.debug("found Option c");
          // Config only
             startConfig  = true;
             startGui     = false;
             startNetwork = false;
         }
         else if(cmd.hasOption("u"))
         {
             log.debug("found Option u");
         // normal usage
             startConfig  = false;
             startGui     = true;
             startNetwork = true;
         }
         else if(cmd.hasOption("m"))
         {
             log.debug("found Option m");
         // command Line Interface
             startConfig  = false;
             startGui     = false;
             startNetwork = true;
         }
         else
         {
             // set the appropriate default values
             if(true == testIfThisIsTheFirstStart())
             {
                 log.debug("default for first Start");
                 startConfig  = true;
                 startGui     = false;
                 startNetwork = false;
             }
             else
             {
                 log.debug("default for not first start");
                 startConfig  = false;
                 startGui     = true;
                 startNetwork = true;
             }
         }
     }
 
     /** test if a valid configuration is present.
      */
     private boolean testIfThisIsTheFirstStart()
     {
         final File cfgDir = fs.getFile(Defaults.CONFIGURATION_FOLDER);
         if((true == cfgDir.exists()) && (true == cfgDir.isDirectory()))
         {
             log.debug("Configuration Directory exists !");
             // The Configuration Directory exists, ...
             final File cfgfile = fs.getFile(Defaults.XML_CONFIG_FILENAME);
             if((true == cfgfile.exists()) && (true == cfgfile.isFile()) && (true == cfgfile.canRead()))
             {
                 // .. and the configuration file exists,..
                 // so we are already configured and this it the 2nd or 3rd or... start
                 return false;
             }
             else
             {
                 // no configuration from previous start available
                 return true;
             }
         }
         else
         {
             cfgDir.mkdirs();
             log.info("This is the First start of FriendsUnited !");
             return true;
         }
     }
 
 
     /** start the Friends United Network Node.
      */
     private void startNetworkLayer(final CommandLine cmd, final OptionCollection cfg, final OptionCollection root)
     {
         log.info("Start of Log Network Layer");
         if(false == checkEnvironmentAndPrerequisites())
         {
             // Prerequisites not fulfilled !
             System.exit(2);
         }
         else
         {
             log.debug("All Prerequisities are fulfilled");
             evaluateNetworkCommandLine(cmd, cfg);
             // Start the Network layer Supervisor with the configuration
             final Supervisor su = new Supervisor(root, fs);
             su.start();
             // This tread may now stop.
             // When Supervisor stops - Then everything is over.
             // And Thats how it's supposed to be
         }
     }
 
     private ResourceBundle createResourceBundle(final OptionCollection cfg)
     {
         // Get default Locale
         final String LocalLanguage  = cfg.getString("LocalLanguage", "en");
         final String LocalCountry   = cfg.getString("LocalCountry", "US");
         final Locale currentLocale = new Locale(LocalLanguage, LocalCountry);
 
         ResourceBundle messages = null;
         String prefix = null;
         try
         {
             prefix = Defaults.MESSAGE_BUNDLE_BASE_NAME;
             messages = ResourceBundle.getBundle(prefix, currentLocale);
         }
         catch(final MissingResourceException e )
         {
             System.out.println("ERROR : Did not find the ressouce bundle !("
                                + prefix + "_"
                                + currentLocale.toString() + ".properties)");
             System.exit(1);
         }
         return messages;
     }
 
     /** start the Friends United Graphic User Interface.
      */
     private void startGui(final CommandLine cmd, final OptionCollection cfg)
     {
         log.info("Start of Log GUI");
         evaluateGuiCommandLine(cmd, cfg);
         final ResourceBundle msg = createResourceBundle(cfg);
         // Start the User Interface with the configuration
         final Startup su = new Startup(cfg, msg, fs);
         // Schedule a job for the event-dispatching thread:
         // creating and showing this application's GUI.
         javax.swing.SwingUtilities.invokeLater(su);
     }
 
     /** Start the Friends United Configuration Creator Wizard.
      */
     private void startConfigurationCreator(final OptionCollection cfg)
     {
         final ResourceBundle msg = createResourceBundle(cfg);
         final Main ConfigCreator = new Main(msg, fs);
         ConfigCreator.execute();
     }
 
     /** print out the Help Statement.
      *
      * The Help statement explains all the command line switches.
      */
     private void printHelp()
     {
         // automatically generate the help statement
         final HelpFormatter formatter = new HelpFormatter();
         formatter.printHelp("java -jar FriendsUnited.jar [options]", options);
     }
 
     /** check if all prerequisites are fulfilled.
      *
      * @return
      */
     private boolean checkEnvironmentAndPrerequisites()
     {
         // Check if I have really strong cryptography available
         // If this fails download the unrestricted policy files
         try
         {
             // create a 192 bit secret key from raw bytes
             final byte[] data = {0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07 };
             final SecretKey key192 = new SecretKeySpec(new byte[]
                            {0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
                             0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f,
                             0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17 },
                             "Blowfish");
 
             // now try encrypting with the larger key
             final Cipher c = Cipher.getInstance("Blowfish/ECB/NoPadding");
             c.init(Cipher.ENCRYPT_MODE, key192);
             c.doFinal(data);
             log.debug("192 bit test: passed");
         }
         catch(final NoSuchAlgorithmException e)
         {
             log.error("No Strong cryptography available ! - download the unrestricted policy files\n"
                     + "http://java.sun.com/\n" + "Downloads\n" + "Other Downloads\n"
                     + "Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files");
             return false;
         }
         catch(final NoSuchPaddingException e)
         {
             log.error("No Strong cryptography available ! - download the unrestricted policy files\n"
                     + "http://java.sun.com/\n" + "Downloads\n" + "Other Downloads\n"
                     + "Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files");
             return false;
         }
         catch(final InvalidKeyException e)
         {
             log.error("No Strong cryptography available ! - download the unrestricted policy files\n"
                     + "http://java.sun.com/\n" + "Downloads\n" + "Other Downloads\n"
                     + "Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files");
             return false;
         }
         catch(final IllegalBlockSizeException e)
         {
             log.error("No Strong cryptography available ! - download the unrestricted policy files\n"
                     + "http://java.sun.com/\n" + "Downloads\n" + "Other Downloads\n"
                     + "Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files");
             return false;
         }
         catch(final BadPaddingException e)
         {
             log.error("No Strong cryptography available ! - download the unrestricted policy files\n"
                     + "http://java.sun.com/\n" + "Downloads\n" + "Other Downloads\n"
                     + "Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files");
             return false;
         }
 
         // Check that Bouncy Castle is available
         // If this fails install bcprov-*.jar
         // Check jre/lib/security/java.security
         // it has to contain the following line( <N> = any Number)
         // security.provider.<N>=org.bouncycastle.jce.provider.BouncyCastleProvider
         if(null == Security.getProvider("BC"))
         {
             Security.addProvider(new BouncyCastleProvider());
             if(null == Security.getProvider("BC"))
             {
                 log.error("Bouncy Castle not installed !\n"
                           + "install bcprov-*.jar\n"
                           + "Check jre/lib/security/java.security "
                           + "it has to contain the following line( N = any Number)\n"
                           + "security.provider.<N>=org.bouncycastle.jce.provider.BouncyCastleProvider");
                 return false;
             }
             else
             {
                 log.info("Could install Bouncy Castle");
             }
         }
         else
         {
             log.info("Bouncy Castle is installed !");
         }
 
         // all checks are ok
         return true;
     }
 
     private void evaluateNetworkCommandLine(final CommandLine cmd, final OptionCollection cfg)
     {
         if(cmd.hasOption("m"))
         {
             log.debug("found Option m");
             // User wants to use the Command Line Interface
             final BooleanOption CommandLine = new BooleanOption("CommandLine", true);
             final OptionCollection cifo = cfg.getSubSectionCreateIfAbsent("ControlInterface");
             cifo.add(CommandLine);
         }
     }
 
     /** The Main Routine.
      * @param args Command Line Arguments
      */
     public static void main(final String[] args)
     {
         // configure Environment
         System.setProperty("file.encoding", "UTF-8");
 
         // TODO Remove the next line
         System.setProperty("java.net.preferIPv4Stack", "true");  // work around for
         // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6206527
 
         final Start st = new Start();
         st.run(args);
     }
 
 }
