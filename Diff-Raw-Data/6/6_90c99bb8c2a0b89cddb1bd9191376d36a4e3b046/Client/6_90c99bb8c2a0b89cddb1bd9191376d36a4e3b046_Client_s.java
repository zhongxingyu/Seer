 package com.redhat.contentspec.client;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.DataConfiguration;
 import org.apache.commons.configuration.HierarchicalConfiguration;
 import org.apache.commons.configuration.HierarchicalINIConfiguration;
 import org.apache.commons.configuration.SubnodeConfiguration;
 import org.apache.commons.configuration.XMLConfiguration;
 import org.apache.commons.configuration.tree.DefaultConfigurationNode;
 import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
 import org.jboss.resteasy.spi.ResteasyProviderFactory;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.ParameterException;
 import com.redhat.contentspec.client.commands.*;
 import com.redhat.contentspec.client.config.ContentSpecConfiguration;
 import com.redhat.contentspec.client.config.ServerConfiguration;
 import com.redhat.contentspec.client.constants.Constants;
 import com.redhat.contentspec.client.utils.ClientUtilities;
 import com.redhat.contentspec.interfaces.ShutdownAbleApp;
 import com.redhat.contentspec.rest.RESTManager;
 import com.redhat.contentspec.rest.RESTReader;
 import com.redhat.contentspec.utils.logging.ErrorLoggerManager;
 import com.redhat.ecs.commonutils.CollectionUtilities;
 import com.redhat.topicindex.rest.entities.TopicV1;
 import com.redhat.topicindex.rest.entities.UserV1;
 
 @SuppressWarnings("unused")
 public class Client implements BaseCommand, ShutdownAbleApp {
 	
 	private final JCommander parser = new JCommander(this);
 	
 	/**
 	 * A mapping of the sub commands the client uses
 	 */
 	private HashMap<String, BaseCommand> commands = new HashMap<String, BaseCommand>();
 	
 	private final ErrorLoggerManager elm = new ErrorLoggerManager();
 	private RESTManager restManager = null;
 	
 	private BaseCommand command;
 	
 	private File csprocessorcfg = new File("csprocessor.cfg");
 	private ContentSpecConfiguration cspConfig = new ContentSpecConfiguration();
 	
 	private String rootDirectory = "";
 	private String publicanBuildOptions = Constants.DEFAULT_PUBLICAN_OPTIONS;
 	private String publicanPreviewFormat = Constants.DEFAULT_PUBLICAN_FORMAT;
 	private boolean firstRun = false;
 	
 	private HashMap<String, ServerConfiguration> servers = new HashMap<String, ServerConfiguration>();
 	
 	@Parameter(names = {Constants.SERVER_LONG_PARAM, Constants.SERVER_SHORT_PARAM}, metaVar = "<URL>")
 	private String serverUrl;
 	
 	@Parameter(names = {Constants.USERNAME_LONG_PARAM, Constants.USERANME_SHORT_PARAM}, metaVar = "<USERNAME>")
 	private String username;
 	
 	@Parameter(names = Constants.HELP_LONG_PARAM)
 	private Boolean showHelp = false;
 	
 	@Parameter(names = Constants.VERSION_LONG_PARAM)
 	private Boolean showVersion = false;
 	
 	@Parameter(names = Constants.CONFIG_LONG_PARAM, metaVar = "<FILE>")
 	private String configLocation = Constants.DEFAULT_CONFIG_LOCATION;
 	
 	private final AtomicBoolean isShuttingDown = new AtomicBoolean(false);
 	protected final AtomicBoolean shutdown = new AtomicBoolean(false);
 	
 	public static void main(String[] args) {
 		Client client = new Client();
 		Runtime.getRuntime().addShutdownHook(new ShutdownInterceptor(client));
 		client.processArgs(args);
 	}
 	
 	public Client() {
 		// Set the column width
 		try {
 			parser.setColumnSize(Integer.parseInt(System.getenv("COLUMNS")));
 		} catch (Exception e) {
 			parser.setColumnSize(160);
 		}
 		
 		// Set the program name
 		parser.setProgramName(Constants.PROGRAM_NAME);
 		
 		// Load the csprocessor.cfg file from the current directory
 		try {
 			if (csprocessorcfg.exists()) {
 				cspConfig = ClientUtilities.readFromCsprocessorCfg(csprocessorcfg);
 				if (cspConfig.getContentSpecId() == null) {
 					printError(Constants.ERROR_INVALID_CSPROCESSOR_CFG_MSG, false);
 					shutdown(Constants.EXIT_CONFIG_ERROR);
 				}
 			}
 		} catch (Exception e) {
 			// Do nothing if the csprocessor.cfg file couldn't be read
 		}
 		
 		// Setup the commands that are to be used
 		setupCommands(parser, cspConfig);
 	}
 	
 	/**
 	 * Parse the set of arguments passed via the command line and then process those arguments
 	 * 
 	 * @param args The array of arguments from the command line
 	 */
 	public void processArgs(String[] args)
 	{
 		try
 		{
 			parser.parse(args);
 		}
 		catch (ParameterException e)
 		{
 			if (parser.getParsedCommand() != null)
 			{
 				commands.get(parser.getParsedCommand()).printError(Constants.INVALID_ARG_MSG, true);
 			}
 			else
 			{
 				printError(Constants.INVALID_ARG_MSG, true);
 			}
 			shutdown(Constants.EXIT_ARGUMENT_ERROR);
 		}
 		
 		// Get the command used
 		String commandName = parser.getParsedCommand();
 		if (commandName == null)
 		{
 			command = this;
 		}
 		else
 		{
 			command = commands.get(commandName);
 		}
 		
 		// Process the command
 		if (command.isShowHelp() || isShowHelp() || args.length == 0)
 		{
 			command.printHelp();
 		} 
 		else if (command.isShowVersion() || isShowVersion())
 		{
 			// Print the version details
 			printVersionDetails(Constants.BUILD_MSG, Constants.BUILD, false);
 		}
 		else if (command instanceof SetupCommand)
 		{
 			command.process(restManager, elm, null);
 		}
 		else
 		{
 		
 			// Print the version details
 			printVersionDetails(Constants.BUILD_MSG, Constants.BUILD, false);
 			
 			// Good point to check for a shutdown
 			if (isAppShuttingDown()) {
 				shutdown.set(true);
 				return;
 			}
 			
 			// Load the configuration options. If it fails then stop the program
 			if (!setConfigOptions(command.getConfigLocation())) {
 				shutdown(Constants.EXIT_CONFIG_ERROR);
 			}
 			
 			// Good point to check for a shutdown
 			if (isAppShuttingDown()) {
 				shutdown.set(true);
 				return;
 			}
 			
 			// If we are loading from csprocessor.cfg then display a message
 			if (command.loadFromCSProcessorCfg())
 			{
 				JCommander.getConsole().println(Constants.CSP_CONFIG_LOADING_MSG);
 			}
 			
 			// Apply the settings from the csprocessor.cfg, csprocessor.ini & command line.
 			applySettings();
 			
 			// Good point to check for a shutdown
 			if (isAppShuttingDown()) {
 				shutdown.set(true);
 				return;
 			}
 			
 			// Print the server url
 			JCommander.getConsole().println(String.format(Constants.WEBSERVICE_MSG, command.getServerUrl()));
 			
 			// Print a line to separate content
 			JCommander.getConsole().println("");
 			
 			// Test that the server address is valid
 			if (!ClientUtilities.validateServerExists(command.getServerUrl())) {
 				command.printError(Constants.UNABLE_TO_FIND_SERVER_MSG, false);
 				shutdown(Constants.EXIT_NO_SERVER);
 			}
 			
 			// Create the REST Manager
 			restManager = new RESTManager(elm, command.getSkynetServerUrl());
 			
 			// Good point to check for a shutdown
 			if (isAppShuttingDown()) {
 				shutdown.set(true);
 				return;
 			}
 			
 			// Process the commands 
 			UserV1 user = command.authenticate(restManager.getReader());
 			command.process(restManager, elm, user);
 			
 			// Check if the program was shutdown
 			if (isShutdown()) return;
 			
 			// Add a newline just to separate the output
 			JCommander.getConsole().println("");
 		}
 		shutdown(Constants.EXIT_SUCCESS);
 	}
 	
 	/**
 	 * Setup the commands to be used in the client
 	 * 
 	 * @param parser
 	 */
 	protected void setupCommands(final JCommander parser, final ContentSpecConfiguration cspConfig)
 	{
 		AssembleCommand assemble = new AssembleCommand(parser, cspConfig);
 		BuildCommand build = new BuildCommand(parser, cspConfig);
 		CheckoutCommand checkout = new CheckoutCommand(parser, cspConfig);
 		CreateCommand create = new CreateCommand(parser, cspConfig);
 		ChecksumCommand checksum = new ChecksumCommand(parser, cspConfig);
 		InfoCommand info = new InfoCommand(parser, cspConfig);
 		ListCommand list = new ListCommand(parser, cspConfig);
 		PreviewCommand preview = new PreviewCommand(parser, cspConfig);
 		PullCommand pull = new PullCommand(parser, cspConfig);
 		PushCommand push = new PushCommand(parser, cspConfig);
 		RevisionsCommand revisions = new RevisionsCommand(parser, cspConfig);
 		SearchCommand search = new SearchCommand(parser, cspConfig);
 		SetupCommand setup = new SetupCommand(parser, cspConfig);
 		//SnapshotCommand snapshot = new SnapshotCommand(parser, cspConfig);
 		StatusCommand status = new StatusCommand(parser, cspConfig);
 		TemplateCommand template = new TemplateCommand(parser, cspConfig);
 		ValidateCommand validate = new ValidateCommand(parser, cspConfig);
 		
 		parser.addCommand(Constants.ASSEMBLE_COMMAND_NAME, assemble);
 		commands.put(Constants.ASSEMBLE_COMMAND_NAME, assemble);
 		
 		parser.addCommand(Constants.BUILD_COMMAND_NAME, build);
 		commands.put(Constants.BUILD_COMMAND_NAME, build);
 		
 		parser.addCommand(Constants.CHECKOUT_COMMAND_NAME, checkout);
 		commands.put(Constants.CHECKOUT_COMMAND_NAME, checkout);
 		
 		parser.addCommand(Constants.CREATE_COMMAND_NAME, create);
 		commands.put(Constants.CREATE_COMMAND_NAME, create);
 		
 		parser.addCommand(Constants.CHECKSUM_COMMAND_NAME, checksum);
 		commands.put(Constants.CHECKSUM_COMMAND_NAME, checksum);
 		
 		parser.addCommand(Constants.INFO_COMMAND_NAME, info);
 		commands.put(Constants.INFO_COMMAND_NAME, info);
 		
 		parser.addCommand(Constants.LIST_COMMAND_NAME, list);
 		commands.put(Constants.LIST_COMMAND_NAME, list);
 		
 		parser.addCommand(Constants.PREVIEW_COMMAND_NAME, preview);
 		commands.put(Constants.PREVIEW_COMMAND_NAME, preview);
 		
 		parser.addCommand(Constants.PULL_COMMAND_NAME, pull);
 		commands.put(Constants.PULL_COMMAND_NAME, pull);
 		
 		parser.addCommand(Constants.PUSH_COMMAND_NAME, push);
 		commands.put(Constants.PUSH_COMMAND_NAME, push);
 		
 		parser.addCommand(Constants.REVISIONS_COMMAND_NAME, revisions);
 		commands.put(Constants.REVISIONS_COMMAND_NAME, revisions);
 		
 		parser.addCommand(Constants.SEARCH_COMMAND_NAME, search);
 		commands.put(Constants.SEARCH_COMMAND_NAME, search);
 		
 		parser.addCommand(Constants.SETUP_COMMAND_NAME, setup);
 		commands.put(Constants.SETUP_COMMAND_NAME, setup);
 		
 		//parser.addCommand(Constants.SNAPSHOT_COMMAND_NAME, snapshot);
 		//commands.put(Constants.SNAPSHOT_COMMAND_NAME, snapshot);
 		
 		parser.addCommand(Constants.STATUS_COMMAND_NAME, status);
 		commands.put(Constants.STATUS_COMMAND_NAME, status);
 		
 		parser.addCommand(Constants.TEMPLATE_COMMAND_NAME, template);
 		commands.put(Constants.TEMPLATE_COMMAND_NAME, template);
 		
 		parser.addCommand(Constants.VALIDATE_COMMAND_NAME, validate);
 		commands.put(Constants.VALIDATE_COMMAND_NAME, validate);
 	}
 	
 	/**
 	 * Apply the settings of the from the various configuration files and command line parameters to the used command.
 	 */
 	private void applySettings()
 	{
 		// Move the main parameters into the sub command
 		if (getConfigLocation() != null)
 		{
 			command.setConfigLocation(getConfigLocation());
 		}
 		
 		if (getServerUrl() != null)
 		{
 			command.setServerUrl(getServerUrl());
 		}
 		
 		if (getUsername() != null)
 		{
 			command.setUsername(getUsername());
 		}
 		
 		// Good point to check for a shutdown
 		if (isAppShuttingDown())
 		{
 			shutdown.set(true);
 			return;
 		}
 		
 		// Set the URL
 		String url = null;
 		if (command.getServerUrl() != null)
 		{
 			// Check if the server url is a name defined in csprocessor.ini
 			for (String serverName: servers.keySet())
 			{
 				// Ignore the default server for csprocessor.cfg configuration files
 				if (serverName.equals(Constants.DEFAULT_SERVER_NAME))
 				{
 					continue;
 				}
 				else if (serverName.equals(command.getServerUrl()))
 				{
 					command.setServerUrl(servers.get(serverName).getUrl());
 					break;
 				}
 			}
 			
 			url = ClientUtilities.validateHost(command.getServerUrl());
 		}
 		else if (cspConfig != null && cspConfig.getServerUrl() != null && command.loadFromCSProcessorCfg())
 		{
 			for (String serverName: servers.keySet())
 			{
 				// Ignore the default server for csprocessor.cfg configuration files
 				if (serverName.equals(Constants.DEFAULT_SERVER_NAME)) continue;
 				
 				// Compare the urls
 				try
 				{
 					URI serverUrl = new URI(ClientUtilities.validateHost(servers.get(serverName).getUrl()));
 					if (serverUrl.equals(new URI(cspConfig.getServerUrl())))
 					{
 						url = servers.get(serverName).getUrl();
 						break;
 					}
 				}
 				catch (URISyntaxException e)
 				{
 					break;
 				}
 			}
 			
 			// If no URL matched between the csprocessor.ini and csprocessor.cfg then print an error
 			if (url == null && !firstRun)
 			{
 				JCommander.getConsole().println("");
 				printError(String.format(Constants.ERROR_NO_SERVER_FOUND_MSG, cspConfig.getServerUrl()), false);
 				shutdown(Constants.EXIT_CONFIG_ERROR);
 			}
 			else if (url == null)
 			{
 				JCommander.getConsole().println("");
 				printError(Constants.SETUP_CONFIG_MSG, false);
 				shutdown(Constants.EXIT_CONFIG_ERROR);
 			}
 		}
 		else
 		{
 			url = servers.get(Constants.DEFAULT_SERVER_NAME).getUrl();
 		}
 		command.setServerUrl(url);
 		
 		// Good point to check for a shutdown
 		if (isAppShuttingDown())
 		{
 			shutdown.set(true);
 			return;
 		}
 		
 		// Set the username
 		if (command.getUsername() == null)
 		{
 			for (String serverName: servers.keySet())
 			{
 				if (serverName.equals(Constants.DEFAULT_SERVER_NAME)) continue;
 				try
 				{
 					URL serverUrl = new URL(servers.get(serverName).getUrl());
 					if (serverUrl.equals(new URL(url)))
 					{
 						command.setUsername(servers.get(serverName).getUsername());
 					}
 				}
 				catch (MalformedURLException e)
 				{
 					command.setUsername(servers.get(Constants.DEFAULT_SERVER_NAME).getUsername());
 				}
 			}
 			
 			// If none were found for the server then use the default
 			if (command.getUsername() == null || command.getUsername().equals(""))
 			{
 				command.setUsername(servers.get(Constants.DEFAULT_SERVER_NAME).getUsername());
 			}
 		}
 		
 		// Good point to check for a shutdown
 		if (isAppShuttingDown())
 		{
 			shutdown.set(true);
 			return;
 		}
 		
 		// Set the root directory in the csprocessor configuration
 		cspConfig.setRootOutputDirectory(rootDirectory);
 		if (cspConfig.getServerUrl() == null) {
 			cspConfig.setServerUrl(url);
 		}
 		
 		// Set the publican build options for assemble or preview commands
 		if (command instanceof AssembleCommand)
 		{
 			((AssembleCommand)command).setPublicanBuildOptions(publicanBuildOptions);
 			if (command instanceof PreviewCommand)
 			{
 				((PreviewCommand)command).setPreviewFormat(publicanPreviewFormat);
 			}
 		}
 	}
 	
 	/**
 	 * Sets the configuration options from the csprocessor.ini configuration file
 	 * 
 	 * @param location The location of the csprocessor.ini file (eg. /home/.config/)
 	 * @return Returns false if an error occurs otherwise true
 	 */
 	@SuppressWarnings("unchecked")
 	protected boolean setConfigOptions(final String location) {
 		final String fixedLocation = ClientUtilities.validateConfigLocation(location);
 		final HierarchicalINIConfiguration configReader;
 		
 		// Good point to check for a shutdown
 		if (isAppShuttingDown()) {
 			shutdown.set(true);
 			return false;
 		}
 		
 		// Checks if the file exists in the specified location
 		File file = new File(location);
 		if (file.exists() && !file.isDirectory()) {
 			JCommander.getConsole().println(String.format(Constants.CONFIG_LOADING_MSG, location));
 			// Initialise the configuration reader with the skynet.ini content
 			try {
 				configReader = new HierarchicalINIConfiguration(fixedLocation);
 			} catch (ConfigurationException e) {
 				command.printError(Constants.INI_NOT_FOUND_MSG, false);
 				return false;
 			}
 		} else if (location.equals(Constants.DEFAULT_CONFIG_LOCATION)) {
 			JCommander.getConsole().println(String.format(Constants.CONFIG_CREATING_MSG, location));
 			
 			String configFile = "";
 			firstRun = true;
 			
 			// Create the configuration
 			
 			configFile += "[servers]\n";
 			// Create the Default server in the config file
 			configFile += "# Uncomment one of the default servers below based on the server you wish to connect to.\n";
 			configFile += "#" + Constants.DEFAULT_SERVER_NAME + "=production\n";
 			configFile += "#" + Constants.DEFAULT_SERVER_NAME + "=test\n\n";
 			// Create the default.username attribute
 			configFile += "#If you use one username for all servers then uncomment and set-up the below value instead of each servers username\n";
 			configFile += "#default.username=\n\n";
 			// Create the Production server in the config file
 			configFile += "# Production Server settings\n";
 			configFile += Constants.PRODUCTION_SERVER_NAME + ".url=http://skynet.cloud.lab.eng.bne.redhat.com:8080/TopicIndex/\n";
 			configFile += Constants.PRODUCTION_SERVER_NAME + ".username=\n\n";
 			// Create the Test server in the config file
 			configFile += "# Test Server settings\n";
 			configFile += Constants.TEST_SERVER_NAME + ".url=http://skynet.usersys.redhat.com:8080/TopicIndex/\n";
 			configFile += Constants.TEST_SERVER_NAME + ".username=\n\n";
 			
 			// Create the Root Directory
 			configFile += "[directory]\n";
 			configFile += "root=\n\n";
 			
 			// Create the publican options
 			configFile += "[publican]\n";
 			configFile += "build.parameters=" + Constants.DEFAULT_PUBLICAN_OPTIONS + "\n";
 			configFile += "preview.format=" + Constants.DEFAULT_PUBLICAN_FORMAT + "\n";
 			
 			// Save the configuration file
 			try {
 				// Make sure the directory exists
 				if (file.getParentFile() != null) {
 					file.getParentFile().mkdirs();
 				}
 				
 				// Save the config
 				FileOutputStream fos = new FileOutputStream(file);
 				fos.write(configFile.getBytes());
 				fos.flush();
 				fos.close();
 			} catch (IOException e) {
 				printError(Constants.ERROR_FAILED_CREATING_CONFIG_MSG, false);
 				return false;
 			}
 			return setConfigOptions(location);
 		} else {
 			command.printError(Constants.INI_NOT_FOUND_MSG, false);
 			return false;
 		}
 		
 		// Good point to check for a shutdown
 		if (isAppShuttingDown()) {
 			shutdown.set(true);
 			return false;
 		}
 		
 		// Read in and process the servers
 		if (!configReader.getRootNode().getChildren("servers").isEmpty()) {
 			SubnodeConfiguration serversNode = configReader.getSection("servers");
 			for (Iterator<String> it = serversNode.getKeys(); it.hasNext();) {
 				String prefix = "";
 				String key = it.next();
 				
 				// Find the prefix (aka server name) on urls
 				if (key.endsWith(".url")) {
 					prefix = key.substring(0, key.length() - ".url".length());
 					
 					String name = prefix.substring(0, prefix.length() - 1);
 					String url = serversNode.getString(prefix + ".url");
 					String username = serversNode.getString(prefix + ".username");
 					
 					// Check that a url was specified
 					if (url == null) {
 						command.printError(String.format(Constants.NO_SERVER_URL_MSG, name), false);
 						return false;
 					}
 					
 					// Create the Server Configuration
 					ServerConfiguration serverConfig = new ServerConfiguration();
 					serverConfig.setName(name);
 					serverConfig.setUrl(url);
 					serverConfig.setUsername(username);
 					
 					servers.put(name, serverConfig);
 				// Just the default server name
 				} else if (key.equals(Constants.DEFAULT_SERVER_NAME)) {
 					// Create the Server Configuration
 					ServerConfiguration serverConfig = new ServerConfiguration();
 					serverConfig.setName(key);
 					serverConfig.setUrl(serversNode.getString(key));
 					serverConfig.setUsername(serversNode.getString(key + "..username"));
 					
 					servers.put(Constants.DEFAULT_SERVER_NAME, serverConfig);
 				}
 			}
 
 			// Check that a default exists in the configuration files or via command line arguments
 			if (!servers.containsKey(Constants.DEFAULT_SERVER_NAME) && command.getServerUrl() == null) {
 				command.printError(String.format(Constants.NO_DEFAULT_SERVER_FOUND, file.getAbsolutePath()), false);
 				return false;
 			} else if (servers.containsKey(Constants.DEFAULT_SERVER_NAME) && !servers.get(Constants.DEFAULT_SERVER_NAME).getUrl().matches("^(http://|https://).*")) {
 				if (!servers.containsKey(servers.get(Constants.DEFAULT_SERVER_NAME).getUrl())) {
 					command.printError(Constants.NO_SERVER_FOUND_FOR_DEFAULT_SERVER, false);
 					return false;
 				} else {
 					ServerConfiguration defaultConfig = servers.get(Constants.DEFAULT_SERVER_NAME);
 					ServerConfiguration config = servers.get(defaultConfig.getUrl());
 					defaultConfig.setUrl(config.getUrl());
 					if (config.getUsername() != null && !config.getUsername().equals("")) defaultConfig.setUsername(config.getUsername());
 				}
 			}
 		} else {
 			// Add the default server config to the list of server configurations
 			ServerConfiguration config = new ServerConfiguration();
 			config.setName(Constants.DEFAULT_SERVER_NAME);
 			config.setUrl("http://localhost:8080/TopicIndex/");
 			servers.put(Constants.DEFAULT_SERVER_NAME, config);
 		}
 		
 		// Read in the root directory
 		if (!configReader.getRootNode().getChildren("directory").isEmpty()) {
 			// Load the root content specs directory
 			if (configReader.getProperty("directory.root") != null && !configReader.getProperty("directory.root").equals("")) {
 				rootDirectory = ClientUtilities.validateLocation(configReader.getProperty("directory.root").toString());
 			}
 		}
 		
 		// Read in the publican build options
 		if (!configReader.getRootNode().getChildren("publican").isEmpty()) {
 			// Load the publican setup values
 			if (configReader.getProperty("publican.build..parameters") != null && !configReader.getProperty("publican.build..parameters").equals("")) {
 				publicanBuildOptions = configReader.getProperty("publican.build..parameters").toString();
 			}
 			if (configReader.getProperty("publican.preview..format") != null && !configReader.getProperty("publican.preview..format").equals("")) {
 				publicanPreviewFormat = configReader.getProperty("publican.preview..format").toString();
 			}
 		} else {
 			publicanBuildOptions = Constants.DEFAULT_PUBLICAN_OPTIONS;
 			publicanPreviewFormat = Constants.DEFAULT_PUBLICAN_FORMAT;
 		}
 
 		return true;
 	}
 	
 	/**
 	 * Prints the version of the client to the console
 	 */
 	private void printVersionDetails(String msg, String version, boolean printNL) {
 		JCommander.getConsole().println(String.format(msg, version) + (printNL ? "\n" : ""));
 	}
 
 	@Override
 	public String getUsername() {
 		return username;
 	}
 
 	@Override
 	public void setUsername(String username) {
 		this.username = username;
 	}
 
 	@Override
 	public String getServerUrl() {
 		return serverUrl;
 	}
 	
 	@Override
 	public String getSkynetServerUrl() {
 		return serverUrl == null ? null : ((serverUrl.endsWith("/") ? serverUrl : (serverUrl + "/")) + "seam/resource/rest");
 	}
 
 	@Override
 	public void setServerUrl(String serverUrl) {
 		this.serverUrl = serverUrl;
 	}
 
 	@Override
 	public Boolean isShowHelp() {
 		return showHelp;
 	}
 	
 	@Override
 	public void setShowHelp(Boolean showHelp) {
 		this.showHelp = showHelp;
 	}
 
 	@Override
 	public String getConfigLocation() {
 		return configLocation;
 	}
 
 	@Override
 	public void setConfigLocation(String configLocation) {
 		this.configLocation = configLocation;
 	}
 	
 	@Override
 	public Boolean isShowVersion() {
 		return showVersion;
 	}
 
 	@Override
 	public void setShowVersion(Boolean showVersion) {
 		this.showVersion = showVersion;
 	}
 
 	@Override
 	public void printHelp() {
 		parser.usage(false);
 	}
 
 	@Override
 	public void printError(String errorMsg, boolean displayHelp) {
 		JCommander.getConsole().println("ERROR: " + errorMsg);
 		if (displayHelp) {
 			JCommander.getConsole().println("");
 			printHelp();
 		} else {
 			JCommander.getConsole().println("");
 		}
 	}
 
 	@Override
 	public UserV1 authenticate(RESTReader reader) {
 		return null;
 	}
 
 	@Override
 	public void process(final RESTManager restManager, final ErrorLoggerManager elm, final UserV1 user)
 	{	
 	}
 
 	@Override
 	public void shutdown() {
 		this.isShuttingDown.set(true);
 		if (command != null && command != this) command.shutdown();
 	}
 	
 	public void shutdown(int exitStatus) {
 		shutdown.set(true);
 		if (command != null) command.setShutdown(true);
 		System.exit(exitStatus);
 	}
 
 	@Override
 	public boolean isAppShuttingDown() {
 		return isShuttingDown.get();
 	}
 	
 	@Override
 	public void setAppShuttingDown(boolean shuttingDown) {
 		this.isShuttingDown.set(shuttingDown);
 	}
 
 	@Override
 	public boolean isShutdown() {
 		return command == null || command == this ? shutdown.get() : command.isShutdown();
 	}
 	
 	@Override
 	public void setShutdown(boolean shutdown) {
 		this.shutdown.set(shutdown);
 	}
 
 	@Override
 	public boolean loadFromCSProcessorCfg() {
 		return cspConfig != null && csprocessorcfg.exists();
 	}
 }
