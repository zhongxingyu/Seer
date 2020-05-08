 /**
  * 
  */
 package com.chinarewards.qqgbvpn.main;
 
 import java.net.InetSocketAddress;
 import java.util.Properties;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.configuration.Configuration;
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.mina.core.service.IoAcceptor;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.chinarewards.qqgbvpn.common.HomeDirLocator;
 import com.chinarewards.qqgbvpn.config.ConfigReader;
 import com.chinarewards.qqgbvpn.config.DatabaseProperties;
 import com.chinarewards.qqgbvpn.config.HardCodedConfigModule;
 import com.chinarewards.qqgbvpn.core.jpa.JpaPersistModuleBuilder;
 import com.chinarewards.qqgbvpn.main.guice.AppModule;
 import com.chinarewards.qqgbvpn.main.protocol.ServiceHandlerModule;
 import com.chinarewards.qqgbvpn.main.protocol.ServiceMapping;
 import com.chinarewards.qqgbvpn.main.protocol.ServiceMappingConfigBuilder;
 import com.chinarewards.qqgbvpn.main.protocol.guice.ServiceHandlerGuiceModule;
 import com.chinarewards.utils.appinfo.AppInfo;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.Module;
 import com.google.inject.persist.PersistService;
 import com.google.inject.persist.jpa.JpaPersistModule;
 import com.google.inject.util.Modules;
 
 /**
  * Bootstrap class contains bootstrapping code.
  * <p>
  * 
  * The important methods are the {@link BootStrap#run()} and {@link #shutdown()}
  * , which should be called during application startup and termination
  * respectively.
  * 
  * @author cyril
  * @since 0.1.0
  */
 public class BootStrap {
 
 	Logger log = LoggerFactory.getLogger(getClass());
 
 	private static final String APP_NAME = "POSv2 Server";
 
 	/**
 	 * Container for all dependency injection required objects.
 	 */
 	Injector injector;
 
 	/**
 	 * Command line arguments (raw).
 	 */
 	final String[] args;
 
 	/**
 	 * Contains parsed command line arguments, from <code>args</code>.
 	 */
 	CommandLine cl;
 
 	/**
 	 * socket server address
 	 */
 	InetSocketAddress serverAddr;
 
 	/**
 	 * acceptor
 	 */
 	IoAcceptor acceptor;
 
 	/**
 	 * The configuration object.
 	 */
 	Configuration configuration;
 
 	private String rootConfigFilename = "posnet.ini";
 
 	/**
 	 * Creates an instance of BootStrap.
 	 * 
 	 * @param args
 	 *            command line arguments
 	 */
 	public BootStrap(String... args) {
 		this.args = args;
 	}
 
 	/**
 	 * Obtains the Guice injector created.
 	 * 
 	 * @return
 	 */
 	public Injector getInjector() {
 		return injector;
 	}
 
 	/**
 	 * @return the rootConfigFilename
 	 */
 	public String getRootConfigFilename() {
 		return rootConfigFilename;
 	}
 
 	protected void printAppVersion() {
 		// print application version.
 		AppInfo appInfo = AppInfo.getInstance();
		System.out.println(APP_NAME + ": version " + appInfo.getVersionString()
				+ "(build: " + appInfo.getBuildNumber() + ")");
 
 	}
 
 	/**
 	 * Starts the bootstrap sequence.
 	 * <p>
 	 * 
 	 * After the bootstrapping, all Guice modules should be properly configured,
 	 * and the Guice injector can be retrieved via {@link #getInjector()}
 	 * method.
 	 */
 	public void run() throws Exception {
 
 		// print application version.
 		printAppVersion();
 
 		// parse command line arguments.
 		parseCmdArgs();
 
 		// build the configuration object.
 		buildConfiguration();
 
 		// print some text.
 		log.info("Bootstrapping...");
 
 		// create the dependency injection environment.
 		createGuice();
 
 		// start mina server
 		log.info("Bootstrapping completed");
 
 	}
 
 	/**
 	 * Call this method to shutdown the system, including any system initialized
 	 * in the {@link #run()} method.
 	 * <p>
 	 */
 	public void shutdown() {
 
 		log.info("Shutdown sequence began");
 
 		// shut down in reverse order as found in method run()!
 
 		shutdownJpa();
 
 		log.info("Shutdown sequence done");
 	}
 
 	protected void shutdownJpa() {
 
 		PersistService ps = injector.getInstance(PersistService.class);
 		try {
 			log.info("Shutting down persistence service");
 			ps.stop();
 		} catch (Throwable t) {
 			log.warn("Error occurred when shutting down persistence service", t);
 		}
 
 	}
 
 	/**
 	 * Parse command line arguments.
 	 */
 	protected void parseCmdArgs() {
 
 		Options opts = buildCmdArgOpts();
 		CommandLineParser parser = new SimpleParser();
 		try {
 			cl = parser.parse(opts, args);
 		} catch (ParseException e) {
 			System.err.println(e.getMessage());
 			printHelp(opts);
 			System.exit(1);
 		}
 
 		// print help message and quit.
 		if (cl.hasOption("help")) {
 			printHelp(opts);
 			System.exit(0);
 		}
 
 		// print version and quit.
 		if (cl.hasOption("version")) {
 			AppInfo appInfo = AppInfo.getInstance();
 			System.out.println(BootStrap.APP_NAME + " version "
 					+ appInfo.getVersionString());
 			System.exit(0);
 		}
 
 	}
 
 	/**
 	 * United way to print command usage.
 	 * 
 	 * @param options
 	 */
 	protected void printHelp(Options options) {
 		HelpFormatter f = new HelpFormatter();
 		f.printHelp("java -jar posnet.jar", options, true);
 	}
 
 	protected void buildConfiguration() throws ConfigurationException {
 
 		// check if the directory is given via command line.
 		String homedir = cl.getOptionValue("d"); // TODO better API call?
 		HomeDirLocator homeDirLocator = new HomeDirLocator(homedir);
 		ConfigReader cr = new ConfigReader(homeDirLocator);
 
 		log.info("Home directory: {}", homeDirLocator.getHomeDir());
 
 		// read the configuration
 		Configuration conf = cr.read(this.getRootConfigFilename());
 		configuration = conf;
 
 		if (this.configuration == null) {
 			// no configuration is found, throw exception
 			throw new RuntimeException(
 					"No configuration is found. Please specify "
 							+ "POSNET_HOME environment variable for the home directory, or -d <home_dir> ");
 		}
 
 	}
 
 	@SuppressWarnings("static-access")
 	protected Options buildCmdArgOpts() {
 
 		// for usage, see link: http://commons.apache.org/cli/usage.html
 
 		Options main = new Options();
 		
 
 		// optional parameters
 
 		{
 			// --verbose
 			main.addOption(OptionBuilder.withLongOpt("verbose").hasArg()
 					.withDescription("详细级别.支持0,1,小数等调试信息。默认为0").create());
 		}
 
 		{
 			// -d : Home directory
 			main.addOption(OptionBuilder.withArgName("homedir")
 					.withLongOpt("home-dir").hasArg()
 					.withDescription("Home directory").create('d'));
 		}
 
 		// help message
 		{
 			Option help = OptionBuilder.withArgName("help").withLongOpt("help")
 					.withDescription("打印消息").create('h');
 			Options options = main;
 			options.addOption(help);
 		}
 
 		// print version
 		{
 			main.addOption(OptionBuilder.withLongOpt("version")
 					.withDescription("显示版本").create());
 		}
 
 		return main;
 	}
 
 	/**
 	 * Create the guice injector environment.
 	 */
 	protected void createGuice() {
 
 		log.info("Initializing dependency injection environment...");
 
 		// prepare the JPA persistence module
 		JpaPersistModule jpaModule = buildJpaPersistModule();
 		
 		Module serviceHandlerModule = buildServiceHandlerModule();
 
 		// prepare Guice injector
 		log.debug("Bootstraping Guice injector...");
 		injector = Guice.createInjector(new AppModule(), new ServerModule(),
 				new HardCodedConfigModule(configuration), jpaModule, serviceHandlerModule);
 
 	}
 	
 	protected Module buildServiceHandlerModule() {
 		
 		// XXX improve this.
 		ServiceMappingConfigBuilder mappingBuilder = new ServiceMappingConfigBuilder();
 		ServiceMapping mapping = mappingBuilder.buildMapping(configuration);
 		
 		return Modules.override(new ServiceHandlerModule(configuration)).with(new ServiceHandlerGuiceModule(mapping));
 		
 	}
 
 	protected JpaPersistModule buildJpaPersistModule() {
 
 		// TODO make it not a builder.
 		JpaPersistModuleBuilder builder = new JpaPersistModuleBuilder();
 
 		JpaPersistModule jpaModule = new JpaPersistModule("posnet");
 		builder.configModule(jpaModule, configuration, "db");
 
 		return jpaModule;
 	}
 
 }
