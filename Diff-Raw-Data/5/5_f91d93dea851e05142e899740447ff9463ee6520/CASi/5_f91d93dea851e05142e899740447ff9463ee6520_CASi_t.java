 /*  	CASi Context Awareness Simulation Software
  *   Copyright (C) 2012  Moritz Bürger, Marvin Frick, Tobias Mende
  *
  *  This program is free software. It is licensed under the
  *  GNU Lesser General Public License with one clarification.
  *  
  *  You should have received a copy of the 
  *  GNU Lesser General Public License along with this program. 
  *  See the LICENSE.txt file in this projects root folder or visit
  *  <http://www.gnu.org/licenses/lgpl.html> for more details.
  */
 package de.uniluebeck.imis.casi;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Locale;
 import java.util.logging.FileHandler;
 import java.util.logging.Formatter;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.logging.XMLFormatter;
 
 import de.uniluebeck.imis.casi.communication.ICommunicationHandler;
 import de.uniluebeck.imis.casi.communication.comLogger.CommunicationLogger;
 import de.uniluebeck.imis.casi.communication.mack.MACKNetworkHandler;
 import de.uniluebeck.imis.casi.controller.MainController;
 import de.uniluebeck.imis.casi.generator.IWorldGenerator;
 import de.uniluebeck.imis.casi.logging.DevLogFormatter;
 import de.uniluebeck.imis.casi.logging.ExtendedConsoleHandler;
 import de.uniluebeck.imis.casi.logging.HTMLFormatter;
 import de.uniluebeck.imis.casi.logging.SimLogFormatter;
 import de.uniluebeck.imis.casi.simulation.engine.SimulationClock;
 import de.uniluebeck.imis.casi.ui.GuiStub;
 import de.uniluebeck.imis.casi.ui.IMainView;
 import de.uniluebeck.imis.casi.ui.simplegui.MainViewSimpleGui;
 
 /**
  * CASi is the main class of the entire simulator. Modules like the
  * {@link ICommunicationHandler}, {@link IWorldGenerator} or the
  * {@link IMainView} can be exchanged here.
  * 
  * @author Tobias Mende
  * 
  */
 public class CASi {
 	/** The development logger */
 	private static final Logger log = Logger
 			.getLogger("de.uniluebeck.imis.casi");
 	/**
 	 * Default logger for logging simulation information. Should be used from
 	 * the whole project
 	 */
 	public static final Logger SIM_LOG = Logger.getLogger("SimulationLoggger");
 	/** flag for determining whether productive mode is on or off */
 	public static boolean DEV_MODE = true;
 	/** Enables really verbose output */
 	public static boolean VERBOSE = false;
 	/** Handlers for logging in file */
 	private static FileHandler devFileHandler, simFileHandler;
 	/** Handlers for logging on the console */
 	private static ExtendedConsoleHandler devConsoleHandler, simConsoleHandler;
 	/** The command line options */
 	public static ParameterParser commandLineOptions;
 
 	/**
 	 * The starting point for the entire program, changes can be made here to
 	 * customize the simulator
 	 * 
 	 * @param args
 	 *            the command line arguments. Use argument {@code --help} to get
 	 *            further information.
 	 */
 	public static void main(String[] args) {
 		// DON'T REMOVE THESE LINES:
 		configureApplication(args);
 		// DO WHAT YOU WANT:
 		configureSimulation(args);
 	}
 
 	/**
 	 * Configures the simulator and the simulation itself. Change this method to
 	 * provide customized behavior.
 	 * 
 	 * @param args
 	 *            the command line arguments
 	 */
 	private static void configureSimulation(String[] args) {
 		final IWorldGenerator generator = new de.uniluebeck.imis.casi.simulations.mate.generator.java.WorldGenerator();
 		Locale.setDefault(Locale.GERMAN);
 		final ICommunicationHandler networkHandler = generateCommunicationHandler(args);
 		if(commandLineOptions.getSpeedFactor() > 0) {
 			// set the provided scale factor.
 			int scaleFactor = (int)Math.round(1000/commandLineOptions.getSpeedFactor());
 			CASi.SIM_LOG.info("Setting simulation speed factor to "+commandLineOptions.getSpeedFactor()+" ("+scaleFactor+" milliseconds are 1 simulated second.)");
 			SimulationClock.getInstance().setScaleFactor(scaleFactor);
 		}
 		IMainView mainView = null;
 		if (commandLineOptions.isGuiDisabled()) {
 			mainView = new GuiStub();
 		} else {
 			mainView = new MainViewSimpleGui();
 		}
 		final MainController mc = new MainController(generator, networkHandler,
 				mainView);
 		
 		// Call the main controller and let it work:
 		mc.init();
 		mc.start();
 	}
 
 	/**
 	 * Configures the applicaton itself. Don't change this method!
 	 * 
 	 * @param args
 	 *            the command line arguments
 	 */
 	private static void configureApplication(String[] args) {
 		try {
 			commandLineOptions = new ParameterParser(args);
 
 		} catch (Exception e) {
 			System.err.println(e.getMessage());
 			System.err
 					.println("An error occured while parsing the parameter. Please correct your input.");
 			System.exit(0);
 		}
 		if (commandLineOptions.isHelpRequest()) {
 			showHelp();
 			System.exit(0);
 		}
 		setupFlags();
 		setupLogging();
 		showLogHeader(false);
 	}
 
 	/**
 	 * This methods prints some advises for using the simulator
 	 */
 	private static void showHelp() {
 		System.out.print("Welcome to the help mode");
 		showLogHeader(true);
 		System.out
 				.println("You are allowed to use the following parameter to customize the behaviour:");
 		// Single parameter
 		System.out
 				.println("\tFlags which can be used seperated or combined, e.g. '-vd' is equal to '-v -d' is equal to '-dv'.");
 		System.out
 				.println("\t - d\tActivates the development mode. In this mode, all messages are written to the dev-log-file. (optional)");
 		System.out
 		.println("\t - h\tUse fancy html files instead of simple text for logging. (optional, don't use x)");
 		System.out
 				.println("\t - n\tDeactivates the GUI. In this mode, no gui is shown. (optional)");
 
 		System.out
 				.println("\t - v\tActivates the verbose mode with much more output (optional)");
 		
 		System.out
		.println("\t - x\tUse xml files for logging instead of simple text. (optional, don't use h)");
 		// Complete Commands
 		System.out.println("\n\tThese commands can be used as described:");
 		System.out.println("\t --help");
 		System.out
 				.println("\t\tPrints this information. Prevents from starting the simulation. (optional)");
 		System.out.println("\t --network-config <path-to-config-file>");
 		System.out
 				.println("\t\tSimulation uses the provided file to configure the network handler. Should be set. Otherwise, only a simple communication logger is used.");
		System.out.println("\t --speed <double speed-factor>");
 		System.out
 				.println("\t\tCan be used to set an initial factor for the simulation speed. Should be between 0.5 and 100. (optional)");
 		
 		// End of commands
 		System.out.println("\n");
 		System.out.println("\t+++++++++++++++++++++++++++++++++++++++++++++++++++\n");
 		System.out.println("Short Example:");
 		System.out
 				.println("\tjava -jar CASi.jar --network-config network.conf.xml");
 		System.out.println("Further Information:");
 		System.out
 				.println("\tIn normal mode, when the dev and verbose flag arn't set,\n"
 						+ "\tthe simulator devides log outputs in two log files in a 'log'-folder in the execution directory.\n"
 						+ "\tThe sim-log contains information about the behaviour of agents, actuators, sensors itself.\n"
 						+ "\tThe dev-log contains detailed information about the behaviour of the simulator, which can be used for debugging but may be a bit confusing for users.\n"
 						+ "\tIn the normal mode only informations, written to the sim-log with level 'info' or higher are written to the console.\n"
 						+ "\tIf the dev mode is activated, all log outputs are written to the dev-log. No sim-log is created in this case.\n"
 						+ "\tBy default, in dev mode only messages written to dev- or sim-log with level 'info' or higher are printed on the console.");
 		System.out.println("\n");
 		System.out.println("And now: try again and have fun ;-)");
 
 	}
 
 	/**
 	 * Sets the DEV_MODE and VERBOSE flag according to the command line
 	 * arguments
 	 */
 	private static void setupFlags() {
 		VERBOSE = commandLineOptions.isVerboseMode();
 		DEV_MODE = commandLineOptions.isDevMode();
 		if (VERBOSE) {
 			System.out.println("Activating Verbose Mode");
 		}
 		if (DEV_MODE) {
 			System.out.println("Activating Development Mode");
 		}
 	}
 
 	/**
 	 * Generates a new CommunicationHandler depending on the provided arguments
 	 * 
 	 * @param args
 	 *            the command line arguments
 	 * @return a new communication handler
 	 */
 	private static ICommunicationHandler generateCommunicationHandler(
 			String[] args) {
 		final ICommunicationHandler handler;
 		if (commandLineOptions.networkConfigProvided()) {
 			handler = new MACKNetworkHandler(
 					commandLineOptions.getNetworkConfigFile());
 		} else {
 			handler = new CommunicationLogger();
 		}
 		return handler;
 	}
 
 	/**
 	 * Writes the header to the simulation logger
 	 * 
 	 * @param logToOut
 	 *            if {@code true}, the output goes to System.out, if
 	 *            {@code false} it goes to the SIM_LOG
 	 */
 	private static void showLogHeader(boolean logToOut) {
 		StringBuffer buf = new StringBuffer();
 		buf.append("\n\t===================================================\n");
 		buf.append("\tCASi - Context Awareness Simulator\n");
 		buf.append("\tSimulation for testing the MACK Framework\n");
 		buf.append("\tby Moritz Buerger, Marvin Frick and Tobias Mende\n");
 		buf.append("\t---------------------------------------------------\n");
 		buf.append("\t\tsettle back and enjoy the simulation :-)\n");
 		buf.append("\t===================================================\n");
 		if (logToOut) {
 			System.out.println(buf.toString());
 		} else {
 			SIM_LOG.info(buf.toString());
 		}
 	}
 
 	/**
 	 * Sets up the logging
 	 */
 	public static void setupLogging() {
 		// setup logging
 		log.setUseParentHandlers(false);
 		SIM_LOG.setUseParentHandlers(false);
 		log.setLevel(Level.ALL);
 		SIM_LOG.setLevel(Level.ALL);
 		configureDevelopmentLogging();
 		log.addHandler(devConsoleHandler);
 
 		// configure the lowest levels for different loggers
 		if (DEV_MODE) {
 			// use the dev console for sim logging only in dev mode
 			SIM_LOG.addHandler(devConsoleHandler);
 		} else {
 			// Create the sim log only in productive mode
 			configureSimulationLogging();
 			SIM_LOG.addHandler(simConsoleHandler);
 			if (simFileHandler != null) {
 				SIM_LOG.addHandler(simFileHandler);
 			}
 		}
 		// write simulation information to the dev log in every case
 		if (devFileHandler != null) {
 			log.addHandler(devFileHandler);
 			SIM_LOG.addHandler(devFileHandler);
 		}
 
 	}
 
 	/**
 	 * Getter for a log file formatter which fits to the log format, configured by the command line options.
 	 * @throws IOException if the file is not writable.
 	 */
 	private static FileHandler getFileHandler(boolean devLog) throws IOException {
 		FileHandler handler = null;
 		Formatter formatter = null;
 		String suffix = "";
 		switch (commandLineOptions.getLogFormat()) {
 		case HTML:
 			if(devLog) {
 				formatter = new HTMLFormatter(false);
 				suffix = "-dev.html";
 			} else {
 				formatter = new HTMLFormatter(true);
 				suffix = "-sim.html";
 			}
 			break;
 		case XML:
 			formatter = new XMLFormatter();
 			if(devLog) {
 				suffix = "-dev.xml";
 			} else {
 				suffix = "-sim.xml";
 			}
 			break;
 		default:
 			if(devLog) {
 				formatter = new DevLogFormatter();
 				suffix = "-dev.log";
 			} else {
 				formatter = new SimLogFormatter();
 				suffix = "-sim.log";
 			}
 			break;
 		}
 		handler = new FileHandler("log/" + getFileName()
 				+ suffix);
 		handler.setFormatter(formatter);
 		return handler;
 	}
 	
 
 	/**
 	 * Creates a file name for log files depending on the current time
 	 * 
 	 * @return a file name
 	 */
 	private static String getFileName() {
 		Date time = Calendar.getInstance().getTime();
 		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-hhmmss");
 		return formatter.format(time);
 	}
 
 	/**
 	 * Configuring the development logging
 	 * 
 	 * @throws SecurityException
 	 *             if no write permissions are given for the log directory
 	 * @throws IOException
 	 *             if an error occurred while writing the log file
 	 */
 	private static void configureDevelopmentLogging() {
 		// Configure console logger
 		devConsoleHandler = new ExtendedConsoleHandler();
 		devConsoleHandler.setFormatter(new DevLogFormatter());
 	
 		// Configure file logger
 	
 		try {
 			new File("./log").mkdir();
 			devFileHandler = getFileHandler(true);
 	
 			if (DEV_MODE) {
 				// log everything to the log file
 				if(VERBOSE) {
 					devFileHandler.setLevel(Level.ALL);
 				} else {
 					devFileHandler.setLevel(Level.FINE);
 				}
 			} else {
 				// define the behavior of the development handler in productive
 				// mode
 				// log everything important into the dev log file
 				if(VERBOSE) {
 					devFileHandler.setLevel(Level.CONFIG);
 				} else {
 					devFileHandler.setLevel(Level.INFO);
 				}
 			}
 		} catch (Exception e) {
 			System.out.println("Es wird keine Protokolldatei erzeugt: "
 					+ e.getMessage());
 		}
 	
 		if (DEV_MODE) {
 			// log more information on the console
 			if (VERBOSE) {
 				devConsoleHandler.setLevel(Level.ALL);
 			} else {
 				devConsoleHandler.setLevel(Level.INFO);
 			}
 		} else {
 			// define the behavior of the development handler in productive mode
 			devConsoleHandler.setLevel(Level.SEVERE); // show important errors
 			// on the console
 		}
 	}
 
 	/**
 	 * Configuring the simulation logging
 	 * 
 	 * @throws SecurityException
 	 *             if no write permissions are given for the log directory
 	 * @throws IOException
 	 *             if an error occurred while writing the log file
 	 */
 	private static void configureSimulationLogging() {
 		// Configure console logger
 		simConsoleHandler = new ExtendedConsoleHandler();
 		simConsoleHandler.setFormatter(new SimLogFormatter());
 
 		// Configure file logger
 
 		try {
 			new File("./log").mkdir();
 			simFileHandler = getFileHandler(false);
 			if(CASi.VERBOSE) {
 				simFileHandler.setLevel(Level.ALL);
 			} else {
 				simFileHandler.setLevel(Level.FINE);
 			}
 		} catch (Exception e) {
 			System.out.println("Es wird keine Protokolldatei erzeugt: "
 					+ e.getMessage());
 		}
 		// for fancy output
 		simConsoleHandler.setLevel(Level.INFO);
 
 	}
 }
