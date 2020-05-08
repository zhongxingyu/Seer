 package com.kpro.main;
 
 import java.io.File;			//for configuration file functionality
 import java.io.FileInputStream;		//for configuration file functionality and reading serialized objects
 import java.io.FileOutputStream;	//for writing the new weights config file
 import java.io.IOException;		//for configuration file functionality
 import java.io.InputStream;		//for configuration file functionality
 import java.lang.reflect.InvocationTargetException;
 import java.util.Date;
 import java.util.Properties;		//for configuration file functionality
 import java.util.logging.*;		//for logger functionality
 import org.apache.commons.cli.*;	//for command line options
 
 import com.kpro.algorithm.CBR;
 import com.kpro.parser.P3PParser;
 
 import com.kpro.dataobjects.*;
 import com.kpro.datastorage.*;
 import com.kpro.ui.*;
 
 
 
 /*  to load a new database from a folder, but not use cbr on a new object. overwrites old db (-n option)
  *  ./PrivacyAdvisor -b -f -n ./new_policy_history [-c config_file_location][-w weight_config_file_loc][-d db_file_location]
  *  to compare policy stored in p.txt, assuming config in default location is valid and used
  *  ./PrivacyAdvisor -T p.txt
  */
 
 
 /* Thinking:
  * 
  * user init > commandline > config file
  * 
  * so assume config at general location (load with loadgen(defaultloc))
  * check to see if commandline config (load with loadgen(newloc))
  * handle other commandline options
  * handle userinit
  * 
  */
 
 public class Gio {
 
 
 	private static Logger logger = Logger.getLogger("");	/**create logger object*/
 	private FileHandler fh = null;							/**creates filehandler for logging*/
 	private Properties genProps = new Properties(); 		/**holds all the property values*/
 	private Properties origWeights = null;					/**the loaded weights file.*/
 	private Properties newWeights = null;					/**the revised weights, following LearnAlgorithm. 
 															written to disk by shutdown().
 															also used in loading weights during init()*/
 	private UserIO userInterface = null;					/**means of interacting with the user*/
 	private PolicyDatabase pdb;								/**Policy database object*/
 	private NetworkR nr;									/** Network Resource (community advice database)*/
 
 	
 	/**
 	 * Constructor fo gio class. There should only be one. Consider this a singleton instance to call I/O messages on.
 	 * Constructs and parses command line arguements as well.
 	 * 
 	 * @throws Exception Mostly from loadWeights, but should also happen for loadFromConfig
 	 */
 	public Gio(String[] args) throws Exception 
 	{
 		this(args,null);
 	}
 
 	
 	
 
 	/**
 	 * A constructor permitting a user interface class to launch everything and be in control.
 	 * 
 	 * @param args any commandline arguements
 	 * @param ui the known UserIO object
 	 * @throws Exception Mostly from loadWeights, but should also happen for loadFromConfig
 	 */
 	public Gio(String[] args, UserIO ui) throws Exception
 	{
 		
 		userInterface = ui;
 		genProps = loadFromConfig("./PrivacyAdviser.cfg");
 		loadCLO(args);
 		//TODO add method to check validity of genProps (after each file load, clo load, and ui load).
 
 		if((genProps.getProperty("genConfig")!=null) &&(genProps.getProperty("genConfig")!="./PrivacyAdvisor.cfg"))
 		{
 			System.err.println("clo config call");
 			genProps = loadFromConfig(genProps.getProperty("genConfig")); //TODO merge, not override
 			loadCLO(args);
 		}
 
 		//start the logger
 		logger = startLogger(genProps.getProperty("loglocation","./LOG.txt"),genProps.getProperty("loglevel","INFO"));
 		if(userInterface ==null)
 		{
 			selectUI(genProps.getProperty("UserIO"));
 		}
 		if(Boolean.parseBoolean(genProps.getProperty("userInit","false")) && !(userInterface==null))
 		{
 			genProps = userInterface.user_init(genProps);
 		}
 		selectPDB(genProps.getProperty("policyDB"));
 
 		//load the weights configuration file
 		origWeights = new Properties();
 		origWeights = loadWeights();
 		if(Boolean.parseBoolean(genProps.getProperty("useNet","false")))
 		{
 			startNetwork();
 		}
 		else
 		{
 			nr = null;
 		}
 	}
 
 	/**
 	 * accepts the direct commandline options, then parses & implements them.
 	 * 
 	 * @param args
 	 */
 	//TODO add exception for invalid options
 	private void loadCLO(String[] args) 
 	{
 		Options options = new Options();
 
 		String[][] clolist= 
 			{
				//{"variable/optionname", "has an arguement"- "true"/"false", "description"},
 				{"genConfig","true","general configuration file location"},
 				{"inWeightsLoc", "true", "input weights configuration file location"},
 				{"inDBLoc", "true", "input database file location"},
 				{"outWeightsLoc", "true", "output weights configuration file location"},
 				{"outDBLoc", "true", "output database file location"},
 				{"P3PLocation","true", "adding to DB: single policy file location"},
 				{"P3PDirLocation","true", "adding to DB: multiple policy directory location"},
 				{"newDB","true", "create new database in place of old one (doesn't check for existence of old one"},
 				{"newPolicyLoc","true", "the policy object to process"},
 				{"userResponse","true","response to specified policy"},
 				{"userIO","true","user interface"},
				{"userInit","true","initialization via user interface"},
 				{"policyDB","true","PolicyDatabase backend"},
 				{"cbrV","true","CBR to use"},
 				{"blanketAccept","true","automatically accept the user suggestion"}, 
 				{"loglevel","true","level of things save to the log- see java logging details"},
 				{"policyDB","true","PolicyDatabase backend"},
 				{"networkRType","true","Network Resource type"},
 				{"networkROptions","true","Network Resource options"},
 				{"confidenceLevel","true","Confidence threshold for consulting a networked resource"},
 				{"useNet","true","use networking options"},
 				{"loglocation","true","where to save the log file"},
 				{"loglevel","true","the java logging level to use. See online documentation for enums."}
 			};
 
 		for(String[] i : clolist)
 		{
 			options.addOption(i[0], Boolean.parseBoolean(i[1]),i[2]);
 		}
 
 		CommandLineParser parser = new PosixParser();
 		CommandLine cmd = null;
 		try
 		{
 			cmd = parser.parse( options, args);
 		}
 		catch (ParseException e)
 		{
 			System.err.println("Error parsing commandline arguements.");
 			e.printStackTrace();
 			System.exit(3);
 		}
 
 //		for(String i : args)
 //		{
 //			System.err.println(i);
 //		}
 		for(String[] i : clolist)
 		{
 			if(cmd.hasOption(i[0]))
 			{
 //				System.err.println("found option i: "+i);
 				genProps.setProperty(i[0],cmd.getOptionValue(i[0]));
 			}
 		}
 //		System.err.println(genProps);
 
 
 	}
 
 
 	/**
 	 * converts a string into a valid CBR
 	 * 
 	 * @param string the string to parse
 	 * @return the CBR to use
 	 * @throws Exception 
 	 */
 	private CBR parseCBR(String string) throws Exception {
 
 		try {
 			return (string == null)?(null):(new CBR(this)).parse(string);
 		} catch (Exception e) {
 			System.err.println("error parsing CBR, exiting.");
 			e.printStackTrace();
 			System.exit(5);
 			return null;
 		}
 
 	}
 
 	/**
 	 * Should parse a string to select, initialize, and return one of the policy databases coded
 	 * 
 	 * @param optionValue the string to parse
 	 * @return the policy database being used
 	 */
 	private void selectPDB(String optionValue) {
 		// TODO Add other PolicyDatabase classes, when other classes are made
 		pdb = PDatabase.getInstance(genProps.getProperty("inDBLoc"), genProps.getProperty("outDBLoc",genProps.getProperty("inDBLoc")));
 		if(pdb==null)
 		{
 			System.err.println("pdb null in selectPDB");
 		}
 	}
 
 	/**
 	 * Should parse a string to select, initialize, and return the user interface selected
 	 * 
 	 * @param optionValue the string to parse
 	 * @return the user interface to use
 	 */
 	private void selectUI(String optionValue) {
 		// TODO Add other UserIO classes, when other classes are made
 		userInterface = new UserIO_Simple();
 	}
 
 
 	/**
 	 * Should parse a string to select, initialize, and return one of the actions (result of checking an object) coded.
 	 * 
 	 * @param optionValue the string to parse
 	 * @return the action to apply to the new policy
 	 */
 	private Action parseAct(String optionValue) {
 		// TODO remove this later
 		return (optionValue == null)?(null):(new Action().parse(optionValue));
 	}
 
 	/**
 	 * Loads the general configuration file, either from provided string, or default location (./PrivacyAdviser.cfg)
 	 *
 	 * @param location of configuration file
 	 * @return properties object corresponding to given configuration file
 	 */
 	//TODO add exception for invalid options
 	public Properties loadFromConfig(String fileLoc)
 	{
 		Properties configFile = new Properties();
 
 		try {
 			File localConfig = new File(fileLoc);
 			InputStream is = null;
 			if(localConfig.exists())
 			{
 				is = new FileInputStream(localConfig);
 			}
 			else
 			{
 				System.err.println("No configuration file at "+fileLoc+ ". Please place one in the working directory.");
 				System.exit(3);
 			}
 			configFile.load(is);
 		}
 		catch (IOException e) 
 		{
 			e.printStackTrace();
 			System.err.println("IOException reading first configuration file. Exiting...\n");
 			System.exit(1);
 		}
 
 		return configFile;
 	}
 
 
 
 
 	/**
 	 * Loads the weights configuration file, from the provided location
 	 * 
 	 * @param location of configuration file <---- ????
 	 * @return properties object corresponding to given configuration file
 	 * @throws Exception if there's an issue reading the file (if it doesn't exist, or has an IO error)
 	 */
 	public Properties loadWeights() throws Exception
 	{
 		try 
 		{
 			if(genProps.getProperty("inWeightsLoc") == null)
 			{
 				System.err.println("inWeightsLoc in Gio/LoadWeights is null");
 			}
 			File localConfig = new File(genProps.getProperty("inWeightsLoc"));
 			//			System.out.println(genProps.getProperty("inWeightsLoc"));
 			InputStream is = null;
 			if(localConfig.exists())
 			{
 				is = new FileInputStream(localConfig);
 			}
 			else 
 			{
 				System.err.println("No weights file is available at "+genProps.getProperty("inWeightsLoc")+
 						" . Please place one in the working directory.");
 				throw new Exception("In class Gio.java:loadWeights(), file "+genProps.getProperty("inWeightsLoc")+" doesn't exist.");
 			}
 			origWeights = new Properties();
 			origWeights.load(is);
 		} 
 		catch (IOException e)  
 		{
 			e.printStackTrace();
 			System.err.println("IOException reading the weights configuration file....\n");
 			throw new Exception("In class Gio.java:loadWeights(), IOException loading the weights from file "+genProps.getProperty("inWeightsLoc")+" .");
 		}
 		
 		return origWeights;
 	}
 
 
 	/**
 	 * startLogger initializes and returns a file at logLoc with the results of logging at level logLevel.
 	 *  
 	 * @param logLoc	location of the output log file- a string
 	 * @param logLevel	logging level (is parsed by level.parse())
 	 * @return	Logger object to log to.
 	 */
 	public Logger startLogger(String logLoc, String logLevel)
 	{
 		try 
 		{
 			fh = new FileHandler(logLoc);		//sets output log file at logLoc
 		}
 		catch (SecurityException e) 
 		{
 			e.printStackTrace();
 			System.err.println("SecurityException establishing logger. Exiting...\n");
 			System.exit(1);
 		} 
 		catch (IOException e) 
 		{
 			e.printStackTrace();
 			System.err.println("IOException establishing logger. Exiting...\n");
 			System.exit(1);
 		}			
 		fh.setFormatter(new SimpleFormatter()); 	//format of log is 'human-readable' simpleformat
 		logger.addHandler(fh);						//attach formatter to logger
 		logger.setLevel(Level.parse(logLevel));		//set log level
 		return logger;
 	}
 
 	/**
 	 * Loads the case history into cache. 
 	 * This is where the background database chosen.
 	 * 
 	 * @param dLoc the location of the database
 	 * 
 	 */
 	public void loadDB()
 	{
 		if(!Boolean.parseBoolean(genProps.getProperty("newDB")))
 		{
 			pdb.loadDB();
 		}
 		loadCLPolicies();	
 	}
 
 	/** 
 	 * loads [additional] policies from commandline (either -p or -f)
 	 * 
 	 */
 	private void loadCLPolicies() {
 		//we already checked to make sure we have one of the options avaliable
 		File pLoc = null;
 		PolicyObject p = null;
 
 		if(genProps.getProperty("p3pLocation",null) != null)
 		{
 			pLoc = new File(genProps.getProperty("p3pLocation"));
 			if(!pLoc.exists()){
 				System.err.println("no file found at p3p policy location specified by the -p3p option: "+
 						genProps.getProperty("p3pLocation"));
 				System.err.println("current location is "+System.getProperty("user.dir"));
 				System.exit(1);
 			}
 			try
 			{
 				p = (new P3PParser()).parse(pLoc.getAbsolutePath());
 				if(p.getContext().getDomain()==null)
 				{
 					if(p.getContext().getDomain()==null)
 					{
 						p.setContext(new Context(new Date(System.currentTimeMillis()),new Date(System.currentTimeMillis()),genProps.getProperty("p3pLocation")));
 					}
 					pdb.addPolicy(p);
 				}
 			}
 			catch(Exception e)
 			{
 				System.err.println("Einar needs to fix a parsing error.");
 				e.printStackTrace();
 				//System.exit(5);
 			}
 		}
 		if(genProps.getProperty("p3pDirLocation",null) != null)
 		{
 			pLoc = new File(genProps.getProperty("p3pDirLocation"));
 			File[] pfiles = pLoc.listFiles();
 
 			//System.err.println("pfiles for p3pDirLocation: "+pfiles);
 			for(int i=0;i<pfiles.length;i++)
 			{
 
 				pLoc = (pfiles[i]);
 				if(!pLoc.exists()){
 					System.err.println("no file found at p3p policy location specified by the -p3pDirLocation option, "+ 
 							genProps.getProperty("p3pDirLocation"));
 					System.exit(1);
 				}
 				try
 				{
 
 					p = (new P3PParser()).parse(pLoc.getAbsolutePath());
 					if(p.getContext().getDomain()==null)
 					{
 						p.setContext(new Context(new Date(System.currentTimeMillis()),new Date(System.currentTimeMillis()),pfiles[i].getAbsolutePath()));
 					}
 					pdb.addPolicy(p);
 				}
 				catch(Exception e)
 				{
 					System.err.println("Einar needs to fix this parsing error.");
 					e.printStackTrace();
 				}			
 			}
 		}
 	}
 
 	/**
 	 * returns the only policy database
 	 * 
 	 * @return the policy database
 	 */
 	public PolicyDatabase getPDB()
 	{
 		return pdb;
 	}
 
 	/**
 	 * closes resources and write everything to file
 	 * 
 	 */
 	public void shutdown() {
 		
 		pdb.closeDB(); //save the db
 		if(newWeights == null)
 		{
 			newWeights = origWeights;
 		}
 		writePropertyFile(newWeights,genProps.getProperty("outWeightsLoc",genProps.getProperty("inWeightsLoc")));
 		userInterface.closeResources();
 		System.out.println("preferences saved and closed without error.");
 	}
 
 
 	/**
 	 * writes a property file to disk
 	 * 
 	 * @param wprops the property file to write
 	 * @param wloc	where to write to
 	 */
 	private void writePropertyFile(Properties wprops, String wloc)
 	{
 		if(wprops==null)
 			System.out.println("wrops null in gio/writepropertyfile");
 		if(wloc ==null)
 			System.out.println("wloc null in gio/writepropertyfile");
 		try 
 		{
 			wprops.store(new FileOutputStream(wloc), null);
 		} 
 		catch (IOException e) 
 		{
 			System.err.println("Error writing weights to file.");
 			e.printStackTrace();
 			System.exit(3);
 		}
 	}
 
 
 	/**
 	 * Generates handles response. This is were we would pass stuff to cli or gui, etc
 	 * 
 	 * @param n the processed policy object
 	 * @return the policyObjected as accepted by user (potentially modified
 	 */
 	public PolicyObject userResponse(PolicyObject n) {
 		if((parseAct(genProps.getProperty("userResponse",null)) == null) && 
 				!Boolean.parseBoolean(genProps.getProperty("blanketAccept")))
 		{
 			return userInterface.userResponse(n);
 		}
 		else
 		{
 			if(Boolean.parseBoolean(genProps.getProperty("blanketAccept")))
 			{
 				return n.setAction(n.getAction().setOverride(true));
 			}
 			else
 			{
 				return n.setAction(parseAct(genProps.getProperty("userResponse",null)));
 			}
 		}
 	}
 
 
 	/**
 	 * returns the policy object from the policyObject option
 	 * 
 	 * @return the policy object to be processed
 	 */
 	public PolicyObject getPO() {
 
 		PolicyObject p = null;
 		if(genProps.getProperty("newPolicyLoc",null) == null)
 			System.err.println("newPolLoc == null in gio:getPO");
 		File pLoc = new File(genProps.getProperty("newPolicyLoc",null));
 		if(!pLoc.exists()){
 			System.err.println("no file found at p3p policy location specified by the new policy option");
 			System.exit(1);
 		}
 
 		p = (new P3PParser()).parse(pLoc.getAbsolutePath());
 		//TODO make sure that the context is parsed if avaliable
 		if(p.getContext().getDomain()==null)
 		{
 			p.setContext(new Context(new Date(System.currentTimeMillis()),new Date(System.currentTimeMillis()),genProps.getProperty("newPolicyLoc")));
 		}
 		return p;
 	}
 
 	/**
 	 * returns the -b option if present- whether or not to solely build a database, or build and call CBR.run()
 	 *  
 	 * @return true if a CBR should NOT be run
 	 */
 	public boolean isBuilding() {
 		return (genProps.getProperty("newPolicyLoc",null)==null);
 	}
 
 	/**
 	 * saves the new weights to a buffer variable before writing in the shutdown call
 	 * 
 	 * @param newWeightP the new weights file to save
 	 */
 	public void setWeights(Properties newWeightP) {
 		newWeights = newWeightP;
 
 	}
 
 
 	/**
 	 * returns the CBR to use
 	 * 
 	 * @return the cbr to use
 	 * @throws Exception 
 	 */
 	public CBR getCBR() throws Exception {
 		return parseCBR(genProps.getProperty("cbrV",null));
 	}
 
 	public Properties getWeights() {
 		return origWeights;
 	}
 
 
 	public void showDatabase() {
 		userInterface.showDatabase(pdb);
 
 	}
 
 
 	/**
 	 * GUI classes should use this to ensure the user passes valid files to load.
 	 * 	
 	 * @param filepath path of the file to check
 	 * @return true if the file exists, else false
 	 */
 	public boolean fileExists(String filepath)
 	{
 		return (new File(filepath)).exists(); 
 	}
 
 
 	
 	/**
 	 * Starts the NetworkR specificied by the configuration settings.
 	 * 
 	 * @throws ClassNotFoundException 
 	 * @throws InvocationTargetException 
 	 * @throws IllegalAccessException 
 	 * @throws InstantiationException 
 	 * @throws SecurityException 
 	 * @throws IllegalArgumentException 
 	 */
 	private void startNetwork() throws ClassNotFoundException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException {
 		//System.err.println("startnetwork called");
 		Class<?> cls = Class.forName("com.kpro.datastorage."+genProps.getProperty("NetworkRType"));
 		if(cls == null)
 		{
 			System.err.println("NetworkRType incorrect- null in Gio.startNetwork()");
 		}
 		nr = (NetworkR) cls.getDeclaredConstructors()[0].newInstance(genProps.getProperty("NetworkROptions"));
 		//System.err.println("nr in startNetwork="+nr);
 	}
 	
 	public NetworkR getNR()
 	{
 		return nr;
 	}
 
 
 
 
 
 	public double getConfLevel() {
 		return Double.parseDouble(genProps.getProperty("confidenceLevel","1.0"));
 	}
 
 }
