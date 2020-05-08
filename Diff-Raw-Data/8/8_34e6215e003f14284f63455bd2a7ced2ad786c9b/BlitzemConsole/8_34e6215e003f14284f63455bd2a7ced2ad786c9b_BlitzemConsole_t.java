 package org.blitzem.console;
 
 import groovy.lang.GroovyShell;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Properties;
 
 import org.blitzem.TaggedItemRegistry;
 import org.blitzem.commands.BaseCommand;
 import org.blitzem.commands.Command;
 import org.blitzem.commands.DownCommand;
 import org.blitzem.commands.HelpCommand;
 import org.blitzem.commands.PerLoadBalancerCommand;
 import org.blitzem.commands.PerNodeCommand;
 import org.blitzem.commands.StatusCommand;
 import org.blitzem.commands.UpCommand;
 import org.blitzem.commands.WholeEnvironmentCommand;
 import org.blitzem.model.Defaults;
 import org.blitzem.model.ExecutionContext;
 import org.blitzem.model.LoadBalancer;
 import org.blitzem.model.Node;
 import org.codehaus.groovy.control.CompilationFailedException;
 import org.jclouds.compute.ComputeServiceContext;
 import org.jclouds.loadbalancer.LoadBalancerServiceContext;
 import org.slf4j.LoggerFactory;
 
 import ch.qos.logback.classic.Level;
 import ch.qos.logback.classic.Logger;
 
 import com.google.common.base.Throwables;
 
 /**
  * Main Blitzem console entry point.
  * 
  * @author Richard North <rich.north@gmail.com>
  * 
  */
 public class BlitzemConsole {
 
 	private static final Logger CONSOLE_LOG = (Logger) LoggerFactory.getLogger(BlitzemConsole.class);
 	public static final Class<? extends Command>[] SUPPORTED_COMMANDS = new Class[] {UpCommand.class, DownCommand.class, StatusCommand.class};
 	public static ExecutionContext executionContext;
 
 	/**
 	 * @param args
 	 *            command line args.
 	 * @throws Exception
 	 */
 	public static void main(String[] args) throws Exception {
 
 		/*
 		 * Parse command line arguments into a Command object for subsequent
 		 * execution.
 		 */
 		BaseCommand command = (BaseCommand) new CommandArgsParser(SUPPORTED_COMMANDS).useDefault(
 				HelpCommand.class).parse(args);
 
 		/*
 		 * Use the --verbose or --superVerbose command line flags to change the
 		 * Logback log level.
 		 */
 		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
 		Logger jclouds = (Logger) LoggerFactory.getLogger("jclouds");
 		if (command.isVerbose()) {
 			root.setLevel(Level.DEBUG);
 			jclouds.setLevel(Level.INFO);
 		} else if (command.isSuperVerbose()) {
 			root.setLevel(Level.TRACE);
 			jclouds.setLevel(Level.TRACE);
 		}
 
 		loadEnvironmentFile(command);
 
 		try {
 			loadContext();
 
 			if (command instanceof WholeEnvironmentCommand) {
 				CONSOLE_LOG.info("Applying command {} to whole environment", command.getClass().getSimpleName());
 				((WholeEnvironmentCommand) command).execute(executionContext.getDriver());
 			} else if (command instanceof PerNodeCommand) {
 
 				for (Node node : TaggedItemRegistry.getInstance().findMatching(command.getNoun(), Node.class)) {
 					CONSOLE_LOG.info("Applying command {} to node '{}'", command.getClass().getSimpleName(), node.getName());
 					((PerNodeCommand) command).execute(node, executionContext.getDriver());
 				}
 
 				for (LoadBalancer loadBalancer : TaggedItemRegistry.getInstance().findMatching(command.getNoun(), LoadBalancer.class)) {
 					CONSOLE_LOG.info("Applying command {} to load balancer '{}'", command.getClass().getSimpleName(),
 							loadBalancer.getName());
 					((PerLoadBalancerCommand) command).execute(loadBalancer, executionContext.getDriver());
 				}
 
 				// always display status
 				new StatusCommand().execute(executionContext.getDriver());
 			}
 
 		} catch (RuntimeException e) {
 			CONSOLE_LOG.error("An unexpected error occurred: \n{}", e.getMessage());
 			// Have to call exit in case another thread is holding the app open (e.g. JClouds spawns some)
 			System.exit(1);
 		} finally {
 			if (executionContext != null) {
 				executionContext.close();
 			}
 		}
 		
 		// Have to call exit in case another thread is holding the app open (e.g. JClouds spawns some)
 		System.exit(0);
 	}
 
 	/**
 	 * Load cloud provider config files, connect, and instantiate a
 	 * {@link ComputeServiceContext} and {@link LoadBalancerServiceContext} for
 	 * further work to be carried out on.
 	 * 
 	 * @return
 	 * @throws IOException
 	 * @throws FileNotFoundException
 	 */
 	private static void loadContext() throws IOException {
 		File cloudConfigFile = new File(System.getProperty("user.home") + "/.blitzem/config.properties");
 		if (!cloudConfigFile.exists() && !cloudConfigFile.isFile()) {
			CONSOLE_LOG.error("Could not find required cloud configuration properties file - expected at: " + cloudConfigFile.getCanonicalPath());
 			throw new RuntimeException();
 		}
 
 		Properties cloudConfigProperties = new Properties();
 		FileInputStream fileInputStream = null;
 		try {
 			fileInputStream = new FileInputStream(cloudConfigFile);
 			cloudConfigProperties.load(fileInputStream);
 		} finally {
 			if (fileInputStream != null) {
 				fileInputStream.close();
 			}
 		}
 
 		executionContext = new ExecutionContext(cloudConfigProperties, cloudConfigFile);
 
 		CONSOLE_LOG.info("Connected to Cloud API ");
 	}
 
 	/**
 	 * Load an environment spec file (expected to be a groovy file, referenced
 	 * in the command).
 	 * 
 	 * Execution of the spec file will update the singleton
 	 * {@link TaggedItemRegistry}.
 	 * 
 	 * @param command
 	 */
 	private static void loadEnvironmentFile(BaseCommand command) {
 
 		File sourceFile = new File(command.getSource());
 		try {
 			final GroovyShell groovyShell = new GroovyShell();
 			groovyShell.setVariable("defaults", Defaults.DEFAULTS);
 			groovyShell.evaluate(sourceFile);
 		} catch (CompilationFailedException e) {
 			CONSOLE_LOG.error("Failed to parse environment definition file: {} with error message:\n{}", sourceFile, e.getMessage());
 			throw new RuntimeException();
 		} catch (IOException e) {
			CONSOLE_LOG.error("Could not open environment definition file expected at {}. Please ensure you are " +
					"running the blitzem command from the directory containing the environment file, or use " +
					"the --source=path argument to specify an alternative path", sourceFile);
 			throw new RuntimeException();
 		}
 	}
 
 }
