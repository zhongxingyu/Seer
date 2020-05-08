 /*
  * Acacia - GS-FLX & Titanium read error-correction and de-replication software.
  * Copyright (C) <2011>  <Lauren Bragg and Glenn Stone - CSIRO CMIS & University of Queensland>
  * 
  * 	This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *  
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *  
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package pyromaniac;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.WindowEvent;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Properties;
 import java.util.TreeSet;
 import java.util.jar.Attributes;
 import java.util.jar.JarFile;
 import java.util.jar.Manifest;
 
 
 import javax.swing.ImageIcon;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.OptionGroup;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 
 import pyromaniac.DataStructures.MIDPrimerCombo;
 import pyromaniac.GUI.CustomDialog;
 import pyromaniac.GUI.GridBagUtility;
 import pyromaniac.GUI.TagInputPanel;
 import pyromaniac.IO.AcaciaLogger;
 
 // TODO: Auto-generated Javadoc
 /**
  * AcaciaMain contains the main method for the Acacia software package. It gets the run paramters from either the command line,
  * config file or GUI as indicated by the user, and then starts the AcaciaEngine.
  */
 public class AcaciaMain 
 {
 	
 	/** The settings flag. */
 	private Object settingsFlag;
 	
 	/** The input frame. */
 
 	AcaciaFrame inputFrame;
 	
 	/** The program status. */
 	private String programStatus;
 	
 	/** The Constant STATUS_USER_INTERACTING. */
 	protected static final String STATUS_USER_INTERACTING = "USER";
 	
 	/** The Constant STATUS_USER_SUBMITTED. */
 	protected static final String STATUS_USER_SUBMITTED = "SUBMITTED";
 	
 	/** The Constant STATUS_USER_EXITED. */
 	protected static final String STATUS_USER_EXITED = "EXITED";
 	
 	/** The Constant FRAME_BACKGROUND_COLOUR. */
 	protected static final Color FRAME_BACKGROUND_COLOUR = Color.decode("#F4E17A");
 	
 	/** The Constant WATTLE_LOC. */
 	protected static final String WATTLE_LOC;
 	
 	/** The Constant ACACIA_LOGO. */
 	public static final String ACACIA_LOGO;
 	
 	/** The Constant DATE_FORMAT_NOW. */
 	protected static final String DATE_FORMAT_NOW = "yyyyMMddHHmmss";
 	
 	
 	/** The Constant MENU_STRING_EXIT. */
 	protected static final String MENU_STRING_EXIT = "Quit";
 	
 	/** The Constant MENU_PROGRAM_INFO. */
 	protected static final String MENU_PROGRAM_INFO = "Program Info";
 
 	//need to be changed to relative to install.
 
 	/** The Constant STANDARD_OUT_NAME. */
 	static final String STANDARD_OUT_NAME;
 	
 	/** The Constant STANDARD_ERR_NAME. */
 	static final String STANDARD_ERR_NAME;
 	
 	/** The Constant STANDARD_DEBUG_NAME. */
 	static final String STANDARD_DEBUG_NAME;
 	
 	static final String STANDARD_OUT_SUFFIX;
 	static final String STANDARD_ERR_SUFFIX;
 	
 	static final String STANDARD_DEBUG_SUFFIX;
 	
 	static 
 	{
 		WATTLE_LOC = "/images/acacia_small_wattle_icon.png";
 		ACACIA_LOGO = "/images/Acacia_logo2.png";
 		
 		STANDARD_OUT_NAME = "acacia_standard_output.txt";
 		STANDARD_ERR_NAME = "acacia_standard_error.txt";
 		STANDARD_DEBUG_NAME = "acacia_standard_debug.txt";
 		
 		STANDARD_OUT_SUFFIX = "stdout.txt";
 		STANDARD_ERR_SUFFIX = "stderr.txt";
 		STANDARD_DEBUG_SUFFIX = "stddebug.txt";
 	}
 
 	/**
 	 * The main method.
 	 *
 	 * @param args the arguments
 	 */
 	@SuppressWarnings("static-access")
 	public static void main(String[] args) 
 	{		
 		AcaciaMain am = new AcaciaMain();				
 		Options options = new Options();
 		OptionGroup runType = new OptionGroup();
 	
 		Option genConfigFile = OptionBuilder.withArgName("file").hasArg().withDescription("Write default config to this file").create("g");
 		Option runFromConfig = OptionBuilder.withArgName("file").hasArg().withDescription("Run Acacia with this config").create("c");
 		Option runGUI = OptionBuilder.withDescription("Run Acacia GUI").create("u");
 		Option help = OptionBuilder.withDescription("Show this help message").create("h");
 		Option version = OptionBuilder.withDescription("Version").create("v");
 		Option property  = OptionBuilder.withArgName( "property=value" )
         .hasArgs(2)
         .withValueSeparator()
         .withDescription( "use value for given property [when running from command line]" )
         .create( "D" );
 		
 		runType.addOption(genConfigFile);
 		runType.addOption(runFromConfig);
 		runType.addOption(runGUI);
 		runType.addOption(help);
 		runType.addOption(version);
 		runType.addOption(property); //this indicates the user is running from the command line without a config.
 		options.addOptionGroup(runType);
 	
 		try
 		{
 			CommandLineParser parser = new PosixParser(); 
 			CommandLine clObj = parser.parse(options, args);
 			
 			if(!(clObj.hasOption('g') ^ clObj.hasOption('c') ^ clObj.hasOption('u') ^ clObj.hasOption('D')^ clObj.hasOption('v')))
 			{
 				usage(options);
 			}
 			
 			if(clObj.hasOption('g'))
 			{
 				String config = clObj.getOptionValue('g');
 				am.generateConfig(config);
 				System.exit(0);
 			}
 			else if(clObj.hasOption('c'))
 			{
 				String config = clObj.getOptionValue('c');
 				
 				HashMap <String, String> settings = am.loadConfigFromFile(config);
 				
 				am.runAcacia(settings);
 			}
 			else if(clObj.hasOption('u'))
 			{
 				am.runAcacia(null);				
 			}
 			else if(clObj.hasOption('v'))
 			{
 				System.out.println("Acacia version: " + AcaciaEngine.getVersion());
 				System.exit(0);
 			}
 			else if(clObj.hasOption('D'))
 			{
 				//running from command line...
 				HashMap <String, String> settings = am.populateSettingsFromCommandLine(clObj);
 				
 				am.runAcacia(settings);
 			}
 			else
 			{
 				usage(options);
 			}
 		}
 		catch(ParseException pe)
 		{
 			System.out.println(pe.getMessage());
 			pe.printStackTrace();
 			usage(options);
 		}
 		catch(Exception e)
 		{
 			
 			e.printStackTrace();
 			am.cleanExit(null, e);
 		}
 	}
 	
 
 	/**
 	 * Instantiates a new acacia main.
 	 */
 	public AcaciaMain() 
 	{
 		this.settingsFlag = new Object();
 		this.programStatus = AcaciaMain.STATUS_USER_INTERACTING;
 	}
 
 
 	/**
 	 * Load user-configurations from file.
 	 *
 	 * @param configLocation the config file location
 	 * @return the runtime settings, with default values overridden by user-specified values
 	 * @throws Exception any exception that occurs while loading the config file
 	 */
 	private HashMap <String, String> loadConfigFromFile(String configLocation) throws Exception
 	{
 			HashMap <String, String> settings = AcaciaEngine.getEngine().getDefaultSettings();
 		
 			File f = new File(configLocation);
 
 			if (!f.exists()) 
 			{
 				throw new IOException("Config file does not exist: "
 						+ configLocation);
 			}
 
 			BufferedReader in = new BufferedReader(new FileReader(f));
 
 			String line = in.readLine();
 			while (line != null) 
 			{
 				String[] keyValue = line.split(AcaciaConstants.CONFIG_DELIMITER);
 				if(settings.containsKey(keyValue[0]))
 				{
 					if(keyValue.length == 2) //user has specified something
 					{
 						System.out.println("Loading from specified config: " + keyValue[0] + AcaciaConstants.CONFIG_DELIMITER + keyValue[1]);
 						
 						if(keyValue[1].trim().length() == 0)
 							settings.put(keyValue[0], null);
 						else
 							settings.put(keyValue[0], keyValue[1]);
 					}
 					else if (keyValue.length > 2)
 					{
 						throw new IOException(
 								"Configuration file is incorrectly formatted: expecting key=value, found "
 										+ line);		
 					}
 					else
 					{
 						//put null for the key.
 						settings.put(keyValue[0], null);
 					}
 				}
 				else
 				{
 					throw new Exception("Invalid parameter in configuration file: " + line);
 				}
 				line = in.readLine();
 			}
 			
 			System.out.println("Finished loading config");
 			
 			
 			return settings;
 	}
 	
 	/**
 	 * Populate settings from command line.
 	 *
 	 * @param clObj object containing command line parameters
 	 * @return the Acacia run settings, with defaults overriden by user-specified values from the command line
 	 * @throws Exception any exception that occurs while populating settings from the command line
 	 */
 	private HashMap <String, String> populateSettingsFromCommandLine(CommandLine clObj) throws Exception
 	{
 		HashMap <String, String> settings = AcaciaEngine.getEngine().getDefaultSettings();
 		
 		//we are only interested in -D
 		
 		Properties p = clObj.getOptionProperties("D");
 		
 		for(Object key : p.keySet())
 		{
 			String keyS = (String)key;
 			
 			Object val = p.get(key);
 			String valS = (String) val;
 			if(settings.containsKey(keyS))
 			{
 				//only way to set parameter to null using command line.
 				if(valS.equals("null"))
 				{
 					valS = null;
 				}
 				
 				
 				System.out.println("Loading from commandline: " + keyS + "=" + valS);
 				settings.put(keyS, valS);
 			}
 			else
 			{
 				throw new Exception("No such parameter: " + keyS);
 			}	
 		}
 
 		return settings;
 	}
 
 	/**
 	 * Generates configuration file with default settings.
 	 *
 	 * @param string the string
 	 */
 	private void generateConfig(String string) 
 	{
 		try 
 		{	
 			File textFile = new File(string);
 			if (!textFile.createNewFile()) 
 			{
 				throw new IOException("File already exists: " + string
 						+ ". Exiting");
 			}
 
 			BufferedWriter out = new BufferedWriter(new FileWriter(textFile));
 			HashMap <String, String> defaultSettings = AcaciaEngine.getEngine().getDefaultSettings();
 			TreeSet<String> keys = new TreeSet<String>(defaultSettings.keySet());
 			
 			//need to sort alphabetically, sick of the inconsistency...
 			for(String setting: keys)
 			{
 				String defaultVal = defaultSettings.get(setting);
 				
 				out.write(setting + AcaciaConstants.CONFIG_DELIMITER
 						+ defaultVal + System.getProperty("line.separator"));
 			}
 			
 			out.close();
 			System.out.println("Default config successfully written to "
 					+ string);
 		} 
 		catch (IOException ie) 
 		{
 			System.err.println(ie.getMessage());
 			System.exit(1);
 		}
 	}
 
 	/**
 	 * Usage.
 	 *
 	 * @param options the command line object
 	 */
 	private static void usage(Options options) 
 	{
 		HelpFormatter formatter = new HelpFormatter();
 		formatter.printHelp( "Acacia", options);
 		System.exit(0);
 	}
 	
 
 
 	/**
 	 * The Class GUIRunnable.
 	 */
 	public class GUIRunnable implements Runnable 
 	{
 		
 		/** The Acacia Main instance. */
 		private AcaciaMain am;
 		
 		/**
 		 * Instantiates a new gUI runnable.
 		 *
 		 * @param am the am
 		 */
 		public GUIRunnable(AcaciaMain am) 
 		{
 			super();
 			this.am = am;
 		}
 
 		/* (non-Javadoc)
 		 * @see java.lang.Runnable#run()
 		 */
 		public void run()
 		{
 			try
 			{
 				new AcaciaFrame().setVisible(true); //starts the GUI thread running
 			}
 			catch(Exception e)
 			{
 				//don't have access to the logger here, but most errors should be caught by the Acacia engine
 				System.out.println("An error occurred: " + e.getMessage());
 				System.exit(1);
 			}
 		}
 	}
 	
 	//after this method, program status has changed from interacting to either submitted or exiting.
 	/**
 	 * Run the Acacia GUI to capture user input.
 	 */
 	private void runGUI()
 	{
 		System.out.println("Running from GUI");
 		GUIRunnable mine = this.new GUIRunnable(this);
 		javax.swing.SwingUtilities.invokeLater(mine);	
 	}
 	
 	/**
 	 * Run from command line.
 	 *
 	 * @param settings the run time settings
 	 * @throws Exception the exception
 	 */
 	private void runFromCommandLine(HashMap <String, String> settings) throws Exception
 	{
 		this.checkSettings(settings);
 		
 		boolean errorOccurred = false;
 		
 		AcaciaLogger logger = new AcaciaLogger();
 		try
 		{	
 			AcaciaEngine engine = AcaciaEngine.getEngine();
 			
 			//System.out.println("Initialising the log files");
 			engine.initLogFiles(settings, logger, false, null);
 			
 			LinkedList <MIDPrimerCombo> validTags = null;
 			
			//System.out.println("Settings is <" + settings.get(AcaciaConstants.OPT_MID) + ">");
			
 			if(settings.get(AcaciaConstants.OPT_MID).equals(AcaciaConstants.OPT_LOAD_MIDS))
 			{
				System.out.println("Running valid tags");
 				validTags = engine.loadMIDS(settings.get(AcaciaConstants.OPT_MID_FILE), logger);
 			}
 			else
 			{
 				validTags = new LinkedList <MIDPrimerCombo>();
 				validTags.add(AcaciaConstants.NO_MID_GROUP);
 			}
 			
 			System.out.println("Running acacia");
 			engine.runAcacia(settings, validTags, logger, null, AcaciaEngine.getVersion());
 				
 		}
 		catch(OutOfMemoryError error)
 		{
 			errorOccurred = true;
 			logger.writeLog(error.getMessage(), AcaciaLogger.LOG_ERROR);
 			
 			StackTraceElement [] trace = error.getStackTrace();
 			
 			for(int i = 0; i < trace.length; i++)
 			{
 				logger.writeLog(trace[i].toString(), AcaciaLogger.LOG_ERROR);
 			}
 		}
 		catch(Exception e)
 		{
 			errorOccurred = true;
 			System.out.println(e.getMessage());
 			logger.writeLog(e.getMessage(), AcaciaLogger.LOG_ERROR);
 			
 			StackTraceElement [] trace = e.getStackTrace();
 			
 			for(int i = 0; i < trace.length; i++)
 			{
 				logger.writeLog(trace[i].toString(), AcaciaLogger.LOG_ERROR);
 			}
 		}
 		finally
 		{
 			try
 			{
 				logger.closeLogger();
 			}
 			catch(Exception e)
 			{
 				e.printStackTrace();
 			}	
 			
 			if(errorOccurred)
 			{
 				System.exit(1);
 			}
 			else
 			{
 				System.exit(0);
 			}
 		}
 	}
 	
 /* TODO: need to add more parameter checks here.
 
 *
 *
 */	
 	/**
 	 * Check settings.
 	 *
 	 * @param settings the run time settings
 	 * @throws Exception the exception
 	 */
 	private void checkSettings(HashMap<String, String> settings) throws Exception 
 	{
 		if(!(settings.get(AcaciaConstants.OPT_FASTA).equals("TRUE") ^ settings.get(AcaciaConstants.OPT_FASTQ).equals("TRUE")))
 		{
 			throw new Exception("One, and only one of FASTA or FASTQ must be specified");
 		}
 		
 		if(settings.get(AcaciaConstants.OPT_FASTA).equals("TRUE") && (settings.get(AcaciaConstants.OPT_FASTA_LOC) == null || ! new File(settings.get(AcaciaConstants.OPT_FASTA_LOC)).exists()))
 		{
 			throw new Exception("FASTA file does not exist: " + settings.get(AcaciaConstants.OPT_FASTA_LOC));
 		}
 		
 		if(settings.get(AcaciaConstants.OPT_FASTQ).equals("TRUE") && (settings.get(AcaciaConstants.OPT_FASTQ_LOC) == null || ! new File(settings.get(AcaciaConstants.OPT_FASTQ_LOC)).exists()))
 		{
 			throw new Exception("FASTQ file does not exist: " + settings.get(AcaciaConstants.OPT_FASTQ_LOC));
 		}
 		
 		int minQual = Integer.parseInt(settings.get(AcaciaConstants.OPT_MIN_AVG_QUALITY));
 		
 		if(minQual < 0 || minQual > 40) //magic numbers to be fixed 
 		{
 			throw new Exception("Invalid avg. quality value, needs to be integer in range 0-40: " + minQual);
 		}
 		
 		if(! new File(settings.get(AcaciaConstants.OPT_OUTPUT_DIR)).isDirectory())
 		{
 			throw new Exception("Output directory does not exist: " + settings.get(AcaciaConstants.OPT_OUTPUT_DIR));
 		}
 		
 		String midOption = settings.get(AcaciaConstants.OPT_MID);
 		
 		if(!(midOption.equals(AcaciaConstants.OPT_LOAD_MIDS) || 
 				midOption.equals(AcaciaConstants.OPT_NO_MID) || 
 				midOption.equals(AcaciaConstants.OPT_ROCHE_10MID)|| 
 				midOption.equals(AcaciaConstants.OPT_ROCHE_5MID)))
 		{
 			throw new Exception("Invalid value for " + AcaciaConstants.OPT_MID +". Value must be in " + 
 					AcaciaConstants.OPT_LOAD_MIDS +", " + AcaciaConstants.OPT_NO_MID + ", " + AcaciaConstants.OPT_ROCHE_10MID + 
 					", " + AcaciaConstants.OPT_ROCHE_5MID);
 		}
 		
		if(midOption.equals(AcaciaConstants.OPT_LOAD_MIDS) &! new File(settings.get(AcaciaConstants.OPT_MID_FILE)).exists())
 		{
 			throw new Exception("Specified MID file does not exist: " + settings.get(AcaciaConstants.OPT_MID_FILE));
 		}
 		
 		try
 		{
 			if(settings.get(AcaciaConstants.OPT_TRIM_TO_LENGTH) != null && settings.get(AcaciaConstants.OPT_TRIM_TO_LENGTH).length() > 0)
 			{
 				int trimLength = Integer.parseInt(settings.get(AcaciaConstants.OPT_TRIM_TO_LENGTH));
 				
 			}
 		}
 		catch(NumberFormatException nfe)
 		{
 			throw new Exception("Specified trim length is not an integer: " + settings.get(AcaciaConstants.OPT_TRIM_TO_LENGTH));
 		}
 		
 		if(! isValidFileName(settings.get(AcaciaConstants.OPT_OUTPUT_PREFIX)))
 		{
 			throw new Exception("Specified prefix is not valid for this OS");
 		}
 		
 		if(! new File(settings.get(AcaciaConstants.OPT_OUTPUT_DIR)).isDirectory())
 		{
 			throw new Exception("The output directory does not exist, or is not a directory: " + settings.get(AcaciaConstants.OPT_OUTPUT_DIR));
 		}
 		
 		int manhattanDist = 0;
 		
 		try
 		{
 			manhattanDist = Integer.parseInt(settings.get(AcaciaConstants.OPT_MAXIMUM_MANHATTAN_DIST));
 		}
 		catch(NumberFormatException nfe)
 		{
 			throw new Exception("Manhattan distance was not an integer:" + settings.get(AcaciaConstants.OPT_MAXIMUM_MANHATTAN_DIST));
 			
 		}
 		
 		if(manhattanDist < 0)
 		{
 			throw new Exception("Manhattan distance is less than zero");
 		}
 		
 		String significanceLevel = settings.get(AcaciaConstants.OPT_SIGNIFICANCE_LEVEL);
 		if(! significanceLevel.equals(AcaciaConstants.SIGN_THRESHOLD_ZERO))
 		{
 			try
 			{
 				int sigLevel = Integer.parseInt(significanceLevel);
 			}
 			catch(NumberFormatException nfe)
 			{
 				throw new Exception("Significance level is not an integer: " + settings.get(AcaciaConstants.OPT_SIGNIFICANCE_LEVEL));
 			}
 		}
 		
 		String representative = settings.get(AcaciaConstants.OPT_REPRESENTATIVE_SEQ);
 		
 		if(!(representative.equals(AcaciaConstants.OPT_MODE_REPRESENTATIVE) ||
 				representative.equals(AcaciaConstants.OPT_MEDIAN_REPRESENTATIVE) ||
 				representative.equals(AcaciaConstants.OPT_MAX_REPRESENTATIVE) ||
 				representative.equals(AcaciaConstants.OPT_MIN_REPRESENTATIVE)))
 				{
 					throw new Exception("Representative sequence option invalid, must be in " +
 							AcaciaConstants.OPT_MODE_REPRESENTATIVE + ", " + AcaciaConstants.OPT_MEDIAN_REPRESENTATIVE + ", " +
 							AcaciaConstants.OPT_MAX_REPRESENTATIVE + ", " + AcaciaConstants.OPT_MIN_REPRESENTATIVE);
 				}
 			
 		
 		String split = settings.get(AcaciaConstants.OPT_SPLIT_ON_MID);
 		split = split.trim();
 		
 		if(!( split.equals("TRUE") || split.equals("FALSE")))
 		{
 			throw new Exception("Split on MID needs to be TRUE/FALSE, not: " + settings.get(AcaciaConstants.OPT_SPLIT_ON_MID));
 		}
 		
 		try
 		{
 			int value = Integer.parseInt(settings.get(AcaciaConstants.OPT_MAX_STD_DEV_LENGTH));
 			
 			if(value <= 0)
 			{
 				throw new Exception("Standard deviations needs to be a positive integer, not " + value);
 			}
 		}
 		catch(NumberFormatException nfe)
 		{
 			throw new Exception("Standard deviations is not an integer: " + settings.get(AcaciaConstants.OPT_SIGNIFICANCE_LEVEL));
 		}
 
 		String errorModel = settings.get(AcaciaConstants.OPT_ERROR_MODEL);
 		
 		if(! (errorModel.equals(AcaciaConstants.OPT_ACACIA_TITANIUM_ERROR_MODEL) || errorModel.equals(AcaciaConstants.OPT_FLOWSIM_ERROR_MODEL) || errorModel.equals(AcaciaConstants.OPT_PYRONOISE_ERROR_MODEL)))
 		{
 			throw new Exception("Incorrect error model specified, must be one of: ");
 		}
 		
 		String validNucs = "ATGC";
 		String flowKey = settings.get(AcaciaConstants.OPT_FLOW_KEY);
 		for(int i = 0; i < flowKey.length(); i++)
 		{
 			if(! validNucs.contains((flowKey.charAt(i) + "")))
 			{
 				throw new Exception ("Invalid character in specified flow key: " + flowKey.charAt(i));
 			}
 		}
 		
 		try
 		{
 			int recurse = Integer.parseInt(settings.get(AcaciaConstants.OPT_MAX_RECURSE_DEPTH));
 			
 			if(recurse < 0)
 			{
 				throw new Exception("Recurse depth needs to be a positive integer");
 			}
 		}
 		catch(NumberFormatException nfe)
 		{
 			throw new Exception("Specified recurse depth is not an integer: " + settings.get(AcaciaConstants.OPT_MAX_RECURSE_DEPTH));
 		}
 		
 		if(settings.containsKey(AcaciaConstants.OPT_TRUNCATE_READ_TO_FLOW) && settings.get(AcaciaConstants.OPT_TRUNCATE_READ_TO_FLOW) != null && settings.get(AcaciaConstants.OPT_TRUNCATE_READ_TO_FLOW).trim().length() > 0)
 		{
 			try
 			{
 				System.out.println("Truncate consensus to flow contains < " + settings.get(AcaciaConstants.OPT_TRUNCATE_READ_TO_FLOW) + ">");
 				
 				int truncateToFlow = Integer.parseInt(settings.get(AcaciaConstants.OPT_TRUNCATE_READ_TO_FLOW));
 			
 				if(truncateToFlow <= 0)
 				{
 					throw new Exception("Truncate to flow must be a positive integer");
 				}
 			}
 			catch(NumberFormatException nfe)
 			{
 				throw new Exception("Specified truncate to flow is not a positive integer: " + settings.get(AcaciaConstants.OPT_TRUNCATE_READ_TO_FLOW));
 			}
 		}
 		
 		if(settings.containsKey(AcaciaConstants.OPT_MIN_READ_REP_BEFORE_TRUNCATION) && settings.get(AcaciaConstants.OPT_MIN_READ_REP_BEFORE_TRUNCATION) != null && settings.get(AcaciaConstants.OPT_MIN_READ_REP_BEFORE_TRUNCATION).trim().length() > 0)
 		{
 			try
 			{
 				double minReadRep = Double.parseDouble(settings.get(AcaciaConstants.OPT_MIN_READ_REP_BEFORE_TRUNCATION));
 				
 				if(minReadRep < 0 || minReadRep > 1)
 				{
 					throw new Exception("Specified min read rep before truncation between be a double between 0 and 1.");
 				}
 			}
 			catch(NumberFormatException nfe)
 			{
 				throw new Exception("Specified min read rep before truncation is not a double: " + settings.get(AcaciaConstants.OPT_MIN_READ_REP_BEFORE_TRUNCATION));
 			}
 		}
 		
 		if(settings.containsKey(AcaciaConstants.OPT_MIN_FLOW_TRUNCATION) && settings.get(AcaciaConstants.OPT_MIN_FLOW_TRUNCATION) != null)	
 		{
 			try
 			{
 				int minFlowTruncation = Integer.parseInt(settings.get(AcaciaConstants.OPT_MIN_FLOW_TRUNCATION));
 				
 				if(minFlowTruncation < 0)
 				{
 					throw new Exception("Min flow truncation must be a positive integer, if specified");
 				}
 			}
 			catch(NumberFormatException nfe)
 			{
 				throw new Exception("Specified min flow truncation is not an integer: " + settings.get(AcaciaConstants.OPT_MIN_FLOW_TRUNCATION));
 			}
 		}
 		
 		if(settings.containsKey(AcaciaConstants.OPT_FILTER_READS_WITH_N_BEFORE_POS) && settings.get(AcaciaConstants.OPT_FILTER_READS_WITH_N_BEFORE_POS) != null)
 		{
 			try
 			{
 				int position = Integer.parseInt(settings.get(AcaciaConstants.OPT_FILTER_READS_WITH_N_BEFORE_POS));
 				if(position < 0)
 				{
 					throw new Exception ("Specified read position (FILTER_N_BEFORE_POS) must be a positive integer");
 				}
 			}
 			catch(NumberFormatException nfe)
 			{
 				throw new Exception("Specified read position (FILTER_N_BEFORE_POS) is not an integer: "+ settings.get(AcaciaConstants.OPT_FILTER_READS_WITH_N_BEFORE_POS));
 			}
 		}
 
 		if(!( settings.get(AcaciaConstants.OPT_SIGNIFICANT_WHEN_TWO).toUpperCase().equals("TRUE") || settings.get(AcaciaConstants.OPT_SIGNIFICANT_WHEN_TWO).toUpperCase().equals("FALSE")))
 		{
 			throw new Exception("ANY_SIGNIFICANT_DIFF_FOR_TWO_SEQS must be TRUE or FALSE, not: " + settings.get(AcaciaConstants.OPT_SIGNIFICANT_WHEN_TWO));
 		}
 	}
 	
 	/**
 	 * Checks if is valid file name.
 	 *
 	 * @param aFileName the a file name
 	 * @return true, if is valid file name
 	 */
 	public boolean isValidFileName(final String aFileName) //this prevents people from re-writing their runs? 
 	{
 	    final File aFile = new File(aFileName);
 	    boolean isValid = true;
 	    
 	    if(aFile.exists())
 	    {
 	    	return true;
 	    }
 	    
 	    try {
 	        if (aFile.createNewFile()) 
 	        {
 	            aFile.delete();
 	        }
 	    } catch (IOException e) {
 	        isValid = false;
 	    }
 	    return isValid;
 	}
 
 	/**
 	 * Starts the Acacia Engine.
 	 *
 	 * @param settings the run time settings (GUI or command line)
 	 * @throws Exception the exception
 	 */
 	private void runAcacia(HashMap <String, String> settings) throws Exception
 	{	
 		//run GUI
 		if (settings == null) 
 		{
 			runGUI();
 		}
 		else
 		{
 			runFromCommandLine(settings);
 		}
 	}
 
 	/**
 	 * Clean exit.
 	 *
 	 * @param message the error message
 	 * @param e the exception
 	 */
 	protected void cleanExit(String message, Exception e)
 	{
 		this.inputFrame.setVisible(false);
 		System.exit(0);
 	}
 
 	/**
 	 * Gets the platform specific path divider.
 	 *
 	 * @return the platform specific path divider
 	 */
 	public static String getPlatformSpecificPathDivider() 
 	{
 		String pathSep = System.getProperty("file.separator");
 		return pathSep;
 	}
 
 	/* Logic for handling 'help' and 'version' info */
 	/**
 	 * The listener interface for receiving acaciaMenu events.
 	 * The class that is interested in processing a acaciaMenu
 	 * event implements this interface, and the object created
 	 * with that class is registered with a component using the
 	 * component's <code>addAcaciaMenuListener<code> method. When
 	 * the acaciaMenu event occurs, that object's appropriate
 	 * method is invoked.
 	 *
 	 * @see AcaciaMenuEvent
 	 */
 	protected class AcaciaMenuListener implements ActionListener, ItemListener 
 	{
 		
 		/** The frame. */
 		AcaciaFrame frame;
 		
 		/**
 		 * Instantiates a new acacia menu listener.
 		 *
 		 * @param frame the parent frame
 		 */
 		public AcaciaMenuListener(AcaciaFrame frame) 
 		{	
 			this.frame = frame;
 		}
 
 		/* (non-Javadoc)
 		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 		 */
 		public void actionPerformed(ActionEvent e) 
 		{
 			// ...Get information from the action event...
 			// ...Display it in the text area...
 			System.out.println(e.getActionCommand());
 			
 			if(e.getActionCommand().equals(AcaciaMain.MENU_STRING_EXIT))
 			{
 				this.frame.promptForQuit();
 			}
 			else if(e.getActionCommand().equals(AcaciaMain.MENU_PROGRAM_INFO));
 			{
 				String ABOUT_MESSAGE = "Acacia was developed by Lauren Bragg and Glenn Stone (2010). The Acacia logo is a composition by Lauren Bragg which uses the " +
 						"<a href=http://upload.wikimedia.org/wikipedia/commons/6/68/Acacia_genistifolia.jpg> Acacia_Genistifolia.jpg </a> image from Wikimedia.";
 				JDialog dialog = new CustomDialog(this.frame, false, ABOUT_MESSAGE);
 				dialog.setTitle("About");
 				dialog.setVisible(true);
 				
 			}
 		}
 
 		/* (non-Javadoc)
 		 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
 		 */
 		public void itemStateChanged(ItemEvent e) 
 		{
 			// ...Get information from the item event...
 			// ...Display it in the text area...
 		}
 	}
 	
 	/**
 	 * The listener interface for receiving mainFrame events.
 	 * The class that is interested in processing a mainFrame
 	 * event implements this interface, and the object created
 	 * with that class is registered with a component using the
 	 * component's <code>addMainFrameListener<code> method. When
 	 * the mainFrame event occurs, that object's appropriate
 	 * method is invoked.
 	 *
 	 * @see MainFrameEvent
 	 */
 	protected class MainFrameListener extends java.awt.event.WindowAdapter
 	{
 		
 		/** The frame. */
 		private AcaciaFrame frame;
 		
 		/**
 		 * Instantiates a new main frame listener.
 		 *
 		 * @param frame the frame
 		 */
 		public MainFrameListener( AcaciaFrame frame)
 		{
 			this.frame = frame;
 		}
 		
 		/* (non-Javadoc)
 		 * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
 		 */
 		public void windowClosing(WindowEvent e)
 		{
 			this.frame.promptForQuit();
 		}
 	}
 	
 	/**
 	 * The Class AcaciaFrame.
 	 */
 	private class AcaciaFrame extends JFrame
 	{
 		
 		/** The TagInputPanel for getting user input. */
 		private TagInputPanel tp;
 		
 		/**
 		 * Instantiates a new acacia frame.
 		 *
 		 * @throws Exception the exception
 		 */
 		public AcaciaFrame () throws Exception
 		{
 			super("Acacia - pyrosequencing error-correction and de-replication");
 			init();
 		}
 		
 		/**
 		 * Prompt for quit.
 		 */
 		public void promptForQuit()
 		{
 			int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to quit?", "Exit Acacia",
 			        JOptionPane.YES_NO_OPTION, 
 			        JOptionPane.QUESTION_MESSAGE);
 			
 			if (response == JOptionPane.NO_OPTION) 
 			{
 				//do nothing;
 			}
 			else
 			{	
 				System.exit(0);
 				//think this needs to be consolidated but no time for it now.
 			}		
 		}
 
 		/**
 		 * Inits the JFrame for the TagInputPanel.
 		 *
 		 * @throws Exception the exception
 		 */
 		public void init() throws Exception
 		{
 			JFrame.setDefaultLookAndFeelDecorated(true);
 			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 			this.setPreferredSize(new Dimension(1064, 800));
 			this.setMinimumSize(new Dimension(1064, 800));
 			
 			URL wattleLoc = getClass().getResource(WATTLE_LOC);
 			
 			if(wattleLoc == null)
 			System.out.println("File could not be found");
 			
 			this.setIconImage(new ImageIcon(wattleLoc).getImage());
 			this.addWindowListener(new MainFrameListener(this));
 					
 			// menu bar for help
 			AcaciaMenuListener listener = new AcaciaMenuListener(this);
 
 			JMenuBar menuBar = new JMenuBar();
 			JMenu menu = new JMenu("Options");
 			menuBar.add(menu);
 
 			JMenuItem quit = new JMenuItem(MENU_STRING_EXIT);
 			JMenuItem info = new JMenuItem(MENU_PROGRAM_INFO);
 			quit.addActionListener(listener);
 			info.addActionListener(listener);
 
 			menu.add(info);
 			menu.add(quit);
 
 			this.setJMenuBar(menuBar);
 
 			GridBagUtility u = new GridBagUtility();
 			// Create and set up the content pane.
 			this.tp = new TagInputPanel(u, this);
 			tp.setOpaque(true);
 
 			this.getContentPane().setLayout(new BorderLayout());
 			this.getContentPane().add(tp, BorderLayout.CENTER);
 		}
 		
 		/**
 		 * Clear fields.
 		 */
 		public void clearFields()
 		{
 			this.tp.clearInterface();
 		}
 	}
 
 }
