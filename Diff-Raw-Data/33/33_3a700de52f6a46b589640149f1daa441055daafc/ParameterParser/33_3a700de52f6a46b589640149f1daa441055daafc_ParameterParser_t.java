 /*  	CASi Context Awareness Simulation Software
  *   Copyright (C) 2012 2012  Moritz Bürger, Marvin Frick, Tobias Mende
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
 
 import java.security.InvalidParameterException;
 
 /**
  * This class is used to interpret the command line options
  * 
  * @author Tobias Mende
  * 
  */
 public class ParameterParser {
 	/** possible log file formats */
 	public enum LOG_FORMAT {
 		/** logfiles are text only */
 		TEXT,
 		/** logfiles are in xml format */
 		XML,
 		/** logfiles are in html format */
 		HTML;
 	}
 	/** Was development mode detected? */
 	private boolean devMode;
 	/** Was verbose mode detected? */
 	private boolean verboseMode;
 	/** Was asked for help? */
 	private boolean helpRequest;
 	/** Should the gui be disabled? */
 	private boolean disableGui;
 	/** A factor for the simulation speed */
 	private double speedFactor;
 	/** The configuration file for the network if one was connected */
 	private String networkConfigFile;
 	/** The format for log files */
 	private LOG_FORMAT logFormat = LOG_FORMAT.TEXT;
 
 	/**
 	 * Creates a new parser
 	 * 
 	 * @param args
 	 *            the arguments to parse
 	 */
 	public ParameterParser(String[] args) {
 		for (int i = 0; i < args.length; i++) {
 			if (parseFlags(args[i])) {
 				continue;
 			}
 
 			if (args[i].matches("--network-config") && args.length > i + 1) {
 				networkConfigFile = args[++i];
 			} else if (args[i].matches("--speed") && args.length > i + 1) {
 				speedFactor = Double.parseDouble(args[++i]);
 			} else if (args[i].matches("--help")) {
 				helpRequest = true;
 			} else {
 				throw new InvalidParameterException("Unknown parameter " + i
 						+ ": " + args[i]);
 			}
 		}
 	}
 
 	/**
 	 * Tries to parse flags
 	 * 
 	 * @param parameter
 	 *            the parameter to parse
 	 * @return {@code true} if a flag was detected and successfully parsed,
 	 *         {@code false} otherwise.
 	 */
 	private boolean parseFlags(String parameter) {
 		boolean success = false;
 		if (parameter.matches("-[^-]*v[^-]*")) {
 			verboseMode = true;
 			success = true;
 		}
 		if (parameter.matches("-[^-]*d[^-]*")) {
 			devMode = true;
 			success = true;
 		}
 		if (parameter.matches("-[^-]*n[^-]*")) {
 			System.out.println("Disable GUI.");
 			disableGui = true;
 			success = true;
 		}
 		/* Log format: */
 		if (parameter.matches("-[^-]*x[^-]*")) {
 			System.out.println("Use XML for logging.");
 			logFormat = LOG_FORMAT.XML;
 			success = true;
 		}
 		if (parameter.matches("-[^-]*h[^-]*")) {
 			System.out.println("Use HTML for logging.");
 			logFormat = LOG_FORMAT.HTML;
 			success = true;
 		}
 		return success;
 	}
 
 	/**
 	 * Checks whether the dev mode flag was detected
 	 * 
 	 * @return {@code true} if dev mode should be activated
 	 */
 	public boolean isDevMode() {
 		return devMode;
 	}
 
 	/**
 	 * Checks whether the verbose mode flag was detected
 	 * 
 	 * @return {@code true} if verbose mode should be activated
 	 */
 	public boolean isVerboseMode() {
 		return verboseMode;
 	}
 
 	/**
 	 * Checks whether help was requested
 	 * 
 	 * @return {@code true} if help mode should be activated
 	 */
 	public boolean isHelpRequest() {
 		return helpRequest;
 	}
 
 	/**
 	 * Checks whether a network config was provided
 	 * 
 	 * @return {@code true} if a config path was set.
 	 */
 	public boolean networkConfigProvided() {
 		return networkConfigFile != null;
 	}
 	
 	/**
 	 * Checks whether the gui should be disabled
 	 * @return {@code true} if the gui should be disabled
 	 */
 	public boolean isGuiDisabled() {
 		return disableGui;
 	}
 
 	/**
 	 * Getter for a network config file
 	 * 
 	 * @return the networkConfigFile the path to the network config or
 	 *         {@code null} if no network config was provided
 	 */
 	public String getNetworkConfigFile() {
 		return networkConfigFile;
 	}
 	
 	/**
 	 * Getter for the format which should be used for log files
 	 * @return the format
 	 */
 	public LOG_FORMAT getLogFormat() {
 		return logFormat;
 	}
 	
 	public double getSpeedFactor() {
 		return speedFactor;
 	}
 
 }
