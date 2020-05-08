 package ui;
 
 import gui.EddieGUI;
 import gui.utilities.PropertyFrame;
 import gui.viewers.file.FileViewerModel;
 
 import java.awt.Container;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Properties;
 
 import javax.swing.JFileChooser;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 
 import javax.swing.KeyStroke;
 
 import cli.EddieCLI;
 import cli.LazyPosixParser;
 import modules.Module;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 import databases.manager.DatabaseManager;
 
 import tools.Tools_Modules;
 import tools.Tools_Array;
 import tools.Tools_File;
 import tools.Tools_String;
 import tools.Tools_System;
 
 
 public class PropertyLoader implements Module{
 
 	//TODO hand over database properties to another class
     private Properties props;
     public static String propertyfilename = new String("eddie.properties");
     public static String infoFile = new String("eddie.info");
     public String rootfolder;
     Options options;
     /* This is actually the 4th iteration of Eddie, 
      * though this one has been written from scratch
      */
     public static int engineversion = 4;
     public static double guiversion = 0.28;
     public static String edition = "Development";
     Level level;
     public static Logger logger;
     public String[] actions;
     private PropertyFrame propsframe;
 	public static String defaultlnf =  "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
 	public String[] args;
 	public String modulename;
 	private String slash;
 	private boolean isLogging;
 	
 	/*
 	 * Current Subproperties, others can be added
 	 * this just for ease 
 	 */
 	//Test subproperty file
 	public static String TestPrefs = "test";
 	public static String DB = "db";
 	//Database subproperty
 	
 
 	
 	
 	//This is a bit of a mess now :( sorry
 	public static String DBHOST = "DBHOST";
 	public static String DBNAME = "DBNAME";
 	public static String DBUSER = "DBUSER";
 	public static String DBTYPE = "DBTYPE";
 	public static String DBDRIVER = "DBDRIVER";
 	
 	public static String[] defaultKeys = new String[]{"AUXILTHREAD","CORETHREAD", "BLAST_BIN_DIR", "BLAST_DB_DIR", "ESTSCAN_BIN", "FILES_XML"};
 	public static String[] defaultKeysUN = new String[]{DBHOST, "DBNAME", "DBUSER", "PREFLNF","MODULES", "VERSION","DBTYPE","DBDRIVER", "TESTDATADIR"};
 	//These should be the index to defaultKeysUN
 	
 	
 	
 	public PropertyLoader() {
 		rootfolder = getEnvirons();
 		level = Level.WARN;
         props = new Properties();
         modulename = this.getClass().getName();
         slash = Tools_System.getFilepathSeparator();
 	}
 	
 	public int loadBasicArguments(String[] args){
 		int retvalue = 0;
 		buildOptions();
 		CommandLineParser parser = new LazyPosixParser();
 		try {
 			CommandLine cmd = parser.parse(options, args);
 			if(cmd.hasOption("h")){
 				retvalue = 1;
 			}
 			else if(cmd.hasOption("about")){
 				retvalue = 6;
 			}
 			else if(cmd.hasOption("lite")){
 				retvalue = 5;
 			}
 			else{
 				if(cmd.hasOption("g")){ 
 					/* GUI */
 					retvalue = 4;					
 				}
 				else{
 					/*If Command Line iNterface*/
 					retvalue = 2;
 					/*
 					 * Store arguments for further parsing by CLI
 					 */
 					this.args=args;
 				}
 			}
 			if(cmd.hasOption("l")){
 				String lev = cmd.getOptionValue("l");
 				level = Level.toLevel(lev, level);
 			}
 		} catch (ParseException e) {
 			System.out.println("Failed To Parse Input Options");
 			e.printStackTrace();
 			retvalue = -1;
 		}
 		return retvalue;
 	}
 	
 	private void buildOptions(){
 		options = new Options();
 		options.addOption(new Option("p", "props", true, "Use this as default properties file"));
 		options.addOption(new Option("g", "gui", false, "Set to GUI rather than CLI (indev)"));
 		//options.addOption(new Option("e", "lite", false, "Eddie Lite, a lighter mode which loads less clunge")); WIP
 		//options.addOption(new Option("persist", false, "If CLI set, this will stop CLI from closing without further args"));
 		options.addOption(new Option("h", "help", false, "Help Menu"));
 		options.addOption(new Option("l", "log", true, "Set Log Level {TRACE,DEBUG,INFO,WARN,ERROR,FATAL}"));
 		options.addOption(new Option("about", false, "About the author"));
 	}
 	
 	public void printHelp(){
 		if(options == null){
 			buildOptions();
 		}
 		HelpFormatter help = new HelpFormatter();
 		help.printHelp("ls", "-- Eddie v"+guiversion+" Help Menu --", options, "-- Share And Enjoy! --");
 		System.out.println();
 		System.out.println("Use -task for list of command line tasks");
 		System.out.println("Use -task taskname -opts for that task's helpmenu");
 	}
 
 	private boolean loadPropertiesInit(){
 		/*
     	 * Create properties file in current folder
     	 */
         File prop = new File(getEnvirons()+propertyfilename);
                
         /*
          * Check to see if it exists, if it does, set rootfolder to current folder
          */
         if(prop.exists()){
         	rootfolder = getEnvirons();
         	/*
         	 * File exists, so try and load it
         	 */
         	this.props = loadPropertyFile(props, prop);
         	return (this.props != null);
         }
         /*
          * If not, then see if file exists in the default user home directory
          * If it does, set rootfolder as user home
          */
         else if(new File(System.getProperty("user.home")+slash+propertyfilename).exists()){
         	rootfolder = System.getProperty("user.home")+slash;
         	this.props = loadPropertyFile(props, new File(System.getProperty("user.home")+slash+propertyfilename));
            	return (this.props != null);
         }
         else{
         	return false;
         }
 	}
 	
 	public void loadPropertiesCLI(){
     	boolean propsbeenloaded = loadPropertiesInit();
         if(!propsbeenloaded){
         	/*
         	 * Alert user that there is properties file 
         	 */
             preLog("Properties File is not found. Creating properties.");
             
             /*
              * Set root folder to environment
              */
             rootfolder = getEnvirons();
             loadDefaultProperties();
             propsbeenloaded = save(rootfolder+propertyfilename, props);
         }
         if(propsbeenloaded){
         	//Get Workspace if not set
 	        if(!props.containsKey("WORKSPACE")){
 	        	setWorkspacePath(rootfolder);
 	        }
 	        startLog();
 	        Logger.getRootLogger().info("Workspace set as root folder " + rootfolder +" to change this change "+ propertyfilename);
         }
 	}
 	
     public void loadPropertiesGUI(Container pane){
     	
     	boolean propsbeenloaded = loadPropertiesInit();
 
         if(!propsbeenloaded){
         	/*
         	 * Alert user that there is properties file 
         	 */
             JOptionPane.showMessageDialog(pane, "Properties File is not found. Creating properties.");
             
             /*
              * Set root folder to environment
              */
             rootfolder = getEnvirons();
            System.out.println("[PRE-LOG] Environment identified as " + rootfolder);
             loadDefaultProperties();
             propsbeenloaded = true;
         }
         if(propsbeenloaded){
         	//Get Workspace if not set
 	        if(!props.containsKey("WORKSPACE")){
 	           	String defaulthome = System.getProperty("user.home")+slash+"eddie";
 	            Object input = JOptionPane.showInputDialog(pane, "Create EddieGUI Workspace folder at default location: ?", "Default EddieGUI Workspace Folder", 1, null, new Object[] {
 	                defaulthome, "Choose Another Location"}, "OK");
 	            if(input != null){
 	                if(input.toString().contentEquals("Choose Another Location")){
 	                    JFileChooser choose = new JFileChooser();
 	                    choose.setFileSelectionMode(1);
 	                    choose.setDialogTitle("Please Choose Folder to Create EddieGUI Workspace in:");
 	                    int opt = choose.showOpenDialog(pane);
 	                    if(opt != 1){
 	                        if(choose.getSelectedFile() != null){
 	                            String path = choose.getSelectedFile().getPath();
 	                            setWorkspacePath(path);
 	                        }
 	                    }
 	                    else{
 	                    	System.out.println("[FATAL] Failed to set workspace");
 	                        System.exit(0);
 	                    }
 	                } 
 	                else{
 	                    setWorkspacePath(defaulthome);
 	                }
 	            }
 	        }
 	        //Start Log if Workspace now exists
 	        if(props.containsKey("WORKSPACE")){
 	           	startLog();
 	           	//Resave to add new workspace
 	           	boolean saved = save(rootfolder+propertyfilename, props);
 	           	if(!saved){
 	           		JOptionPane.showMessageDialog(pane, "Cannot save properties file, do you have permissions for workspace location " +this.getWorkspace() + "?");
 	           	}
 	        }
 	        else{
 	        	JOptionPane.showMessageDialog(pane, "An Error has occured, Log could not be started as workspace no set.");
 	        }
         }
         else{
         	JOptionPane.showMessageDialog(pane, "An Error has occured, properties for this session could not be saved, check folder permissions and restart.");
         }
     }
     
     public Properties loadPropertyFile(Properties prop, File file){
     	 try{
             prop.load(new FileInputStream(file));
             if(isLogging)	logger.info("Trying to load Properties File @"+file.getPath());
             else	preLog("Trying to load Properties File @ "+file.getPath());
         	return prop;
          }
          catch(FileNotFoundException fi) {
         	 if(isLogging){
         		 logger.error(fi);
         	 }
         	 else{
         		 preLog("Property File not found");
         		 fi.printStackTrace();
         	 }
         	 return null;
          }
          catch(IOException io) {
         	 if(isLogging){
         		 logger.error(io);
         	 }
         	 else{
         		 preLog("IOException File not found");
         		 io.printStackTrace();
         	 }
         	 return null;
          }
     }
     
 	private void startLog() {
 		File logfolder = new File(getWorkspace()+ slash + "logs");
 		preLog("Initialising Log...");
 		if (logfolder.isFile()) {
 			System.out.println("Failed To log is standard location!!");
 			int i = 0;
 			while (logfolder.isFile()) {
 				logfolder = new File(getWorkspace()	+ slash + "logs" + i);
 			}
 		}
 		if (!logfolder.exists()) {
 			boolean done = logfolder.mkdir();
 			if(!done)System.out.println("Could not make a log folder @ " + logfolder.getPath());
 		}
 		if(logfolder.isDirectory()){
             File log_properties = new File(logfolder.getPath()+slash+"log4j.properties");
             if(log_properties.isFile()){
                 logger = Logger.getLogger(logfolder.getPath()+slash+"log4j.properties");
                 PropertyConfigurator.configure(log_properties.getPath());
             }
             else{
                 Properties defaults = getDefaultLogProperties(logfolder.getPath()+slash);
                 save(log_properties.getPath(), defaults);
                 logger = Logger.getLogger(logfolder.getPath()+slash+"log4j.properties");
                 PropertyConfigurator.configure(defaults);
                 
             }
             Logger.getRootLogger().setLevel(level);
             Logger.getRootLogger().info("Logger Initialised LVL: "+level.toString()+" @ "+Tools_System.getDateNow());
             isLogging = true;
         } 
 		else{
             preLog("Logging has failed. Can Not Continue.");
             preLog("This is a major issue, that will lead to downstream problems");
         }
 	}
 	
 	private static Properties getDefaultLogProperties(String logfilepath){
 		Properties defaults = new Properties();
 		//Set Log File Properties
 		defaults.setProperty("log4j.appender.rollingFile", "org.apache.log4j.RollingFileAppender");
 		defaults.setProperty("log4j.appender.rollingFile.File", logfilepath+"eddie.log");
 		defaults.setProperty("log4j.appender.rollingFile.MaxFileSize", "10MB");
 		defaults.setProperty("log4j.appender.rollingFile.MaxBackupIndex", "3");
 		defaults.setProperty("log4j.appender.rollingFile.layout", "org.apache.log4j.PatternLayout");
 		defaults.setProperty("log4j.appender.rollingFile.layout.ConversionPattern", "[%5p] %t (%F:%L) - %m%n");
 		//Mirror in console
 		defaults.setProperty("log4j.appender.stdout","org.apache.log4j.ConsoleAppender");
 		defaults.setProperty("log4j.appender.stdout.layout",  "org.apache.log4j.PatternLayout");
 		defaults.setProperty("log4j.appender.stdout.layout.ConversionPattern",  "[%5p] %t (%F:%L) - %m%n");
 		
 		defaults.setProperty("log4j.rootLogger", "WARN, rollingFile, stdout");
 		return defaults;
 	}
 
 	public String getModuleFolder(){
 		return getPropOrSet("MODULES", rootfolder+"Modules"+slash);
 	}
 
 	public static boolean save(String filepath, Properties props1) {
 		boolean success = false;
 		try {
 			FileOutputStream stream = new FileOutputStream(filepath);
 			props1.store(stream, null);
 			stream.close();
 			success = true;
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		if (success) {
 			if(logger == null){
 				System.out.println("[PRE-LOG] Saved Properties @ "+filepath);
 			}
 			else{
 				Logger.getRootLogger().info("Saved Properties @ "+filepath);
 			}
 		}
 		if (!success) {
 			if(logger == null){
 				preLog("[ERROR] failed to Saved Properties @ "+filepath);
 			}
 			else{
 				Logger.getRootLogger().error("Saved Properties @ "+filepath);
 			}
 		}
 		return success;
 	}
 
 	private boolean setWorkspacePath(String path) {
 		preLog("Setting workspace location to " + path);
 		setWorkspace(path);
 		File workspace = new File(path);
 		boolean ret = false;
 		if (workspace.exists()) {
 			ret = true;
 			File eddie = new File(path	+ slash + infoFile);
 			String in = new String("");
 			if (eddie.isFile()) {
 				in = Tools_File.quickRead(eddie);
 				if (in.length() > 0) {
 					int start = 0;
 					if ((start = in.indexOf("VERS:")) != -1) {
 						Double oldvers = Tools_String.parseString2Double(in
 								.substring(start+5, in.length()));
 						if(oldvers != null && oldvers != guiversion){
 							//TODO Compatibilaty
 						}
 					}
 				}
 			} else {
 				saveInfoFile(eddie);
 			}
 		} else {
 			ret = workspace.mkdir();
 			if (ret) {
 				saveInfoFile(new File(path
 						+ System.getProperty("file.separator")
 						+ infoFile));
 			}
 		}
 		save(rootfolder+propertyfilename, props);
 		return ret;
 	}
 
 	private boolean saveInfoFile(File file) {
 		return Tools_File.quickWrite("VERS:" + guiversion, file, false);
 	}
 
	public String getEnvirons() {
		return new File(getClass().getProtectionDomain().getCodeSource().getLocation().getFile()).getParent()+slash;
 	}
 
 	public String getWorkspace() {
 		return props.getProperty("WORKSPACE");
 	}
 
 	public void setWorkspace(String set){
 		props.setProperty("WORKSPACE", set);
 	}
 
 	public Properties getProps() {
 		return props;
 	}
 
 	public void setProps(Properties props) {
 		this.props = props;
 	}
 
 	public void loadDefaultProperties() {
 		String[][] ret = getChangableStats();
 		for(int i =0; i< ret[0].length; i++){
 			props.put(ret[0][i], ret[1][i]);
 		}
 		ret = getUnchangableStats();
 		for(int i =0; i< ret[0].length; i++){
 			props.put(ret[0][i], ret[1][i]);
 		}
 	}
 	
 	public boolean initilaseSubProperty(String name){
 		if(this.props.containsKey(name) && new File(this.getProp(name)).exists()){
 			logger.warn("This property name ["+name+"] already exists!");
 			return true;
 		}
 		else{
 			String path =  this.getWorkspace()+"prefs"+Tools_System.getFilepathSeparator()+name+".properties";
 			File folder = new File(this.getWorkspace()+"prefs");
 			if(!Tools_File.createFolderIfNotExists(folder)){
 				return false;
 			}
 			else{
 				if(save(path, new Properties())){
 					setPropertyValue(name,path);
 					return true;
 				}
 				else{
 					logger.error("Failed to initialise new subproperties ");
 					return false;
 				}
 			}
 		}
 	}
 	
 	public Properties getSubProperty(String property){
 		if(this.props.containsKey(property)){
 			return this.loadPropertyFile(new Properties(), new File(this.getProp(property)));
 		}
 		else{
 			logger.error("Failed to retrieve subproperty file for " + property);
 			return null;
 		}
 	}
 	
 	public String[][] getChangableStats(){
 		//These need to all be the same length
 		String[] stats = defaultKeys;
 		String[] stats_val = new String[]{"5", "1", "/usr/bin/", this.rootfolder+"blas_db"+Tools_System.getFilepathSeparator(),"/usr/bin/ESTscan", rootfolder+FileViewerModel.filename};
 		String[] tool_tips = new String[]{"Max number of auxiliary threads","Max number of primary threads", "Directory that contains blast executables", 
 				"XML file which list current files in project", "Location of the ESTScan executable", "File XML list location"};
 		String[][] ret = new String[3][stats.length];
 		ret[0] = stats;
 		ret[1] = stats_val;
 		ret[2] = tool_tips;
 		return ret;
 	}
 	
 	public String[][] getUnchangableStats(){
 		String[] stats = defaultKeysUN;
 		String[] stats_val = new String[]{"Localhost", DatabaseManager.default_database, System.getProperty("user.name"),defaultlnf, rootfolder+"Modules"+Tools_System.getFilepathSeparator(), guiversion+"","mysql","com.mysql.jdbc.Driver", "/home/user/eddie4/test/"};
 		String[][] ret = new String[2][stats.length];
 		ret[0] = stats;
 		ret[1] = stats_val;
 		return ret;
 	}
 
 	public void setPropertyValue(String prop, String value){
 		props.setProperty(prop, value);
 	}
 	
 	public String getPropOrSet(String prop, String defaultvalue) {
 		if (props.containsKey(prop)) {
 			return props.getProperty(prop);
 		} else {
 			props.setProperty(prop, defaultvalue);
 			return defaultvalue;
 		}
 	}
 	
 	public String getProp(String prop) {
 		if (props.containsKey(prop)) {
 			return props.getProperty(prop);
 		} else {
 			return null;
 		}
 	}
 	
 	public int getPropertiesCount(){
 		return props.size();
 	}
 
 	public String getLastLNF() {
 		return getPropOrSet("PREFLNF",defaultlnf);
 	}
 
 	public void changeDefaultLNF(String lnf) {
 		props.setProperty("PREFLNF", lnf);
 	}
 
 	public String getAuxil() {
 		return getPropOrSet("AUXILTHREAD", "5");
 	}
 
 	public String getCore() {
 		return getPropOrSet("CORETHREAD", "1");
 	}
 	
 	public static void preLog(String str){
 		System.out.println("[PRE-LOG] "+str);
 	}
 	
 	public static double getFullVersion(){
 		return guiversion+engineversion;
 	}
 	
 	public String[] getDBSettings(){
 		String[] s = new String[5];
 		s[0]=this.getProp(DBTYPE);
 		s[1] =this.getProp(DBDRIVER);
 		s[2] =this.getProp(DBHOST);
 		s[3] =this.getProp(DBNAME);
 		s[4] =this.getProp(DBUSER);
 		return s;
 	}
 	
 	public String[][] getFullDBsettings(){
 		String[][] db = new String[3][5];
 		String[] d = new String[]{DBTYPE, DBDRIVER, DBHOST,DBNAME,DBUSER};
 		String[] s = new String[]{"Type of database, currently only mysql supported"
 				,"Database driver, this is unlikely to change"
 				,"Name or ip of computer that hosts database, if local, localhost or 129.0.0.1"
 				,"Name of database to store data",
 				"Your mysql username"};
 		for(int i =0 ; i < 5 ; i++){
 			db[0][i] = d[i];
 			db[1][i] = this.getProp(d[i]);
 			db[2][i] = s[i];
 		}
 		return db;
 	}
 	
 	/****************************************************************************/
 	/*																			*
 	 *																			*
 	 * 						MODULE SPECIFIC METHODS 							*
 	 * 																			*
 	 * 																			*
 	 *																			*/
 	/****************************************************************************/
 	public boolean ownsThisAction(String s) {
 		return Tools_Modules.ownsThisAction(actions, s);
 	}
 
 	public void actOnAction(String s, EddieGUI gui) {
 		Logger.getRootLogger().debug("PropertyLoader acting upon command "+s);
 		if(s.contentEquals(this.modulename)){
 			Logger.getRootLogger().debug("Building General Properties Frame");
 			propsframe = new PropertyFrame();
 			String[][] labels = this.getChangableStats();
 			for(int i =0;i < labels[0].length; i++)labels[1][i]=this.getPropOrSet(labels[0][i], labels[1][i]);
 			actions = Tools_Array.mergeStrings(actions, propsframe.build(this.modulename,"", gui, labels));
 			propsframe.setVisible(true);
 			gui.add2Desktop(propsframe);
 		}
 		else if(s.contentEquals(modulename+"_PROPS_CLOSE")){
 			Logger.getRootLogger().debug("Closing General Properties Without Saving");
 			this.propsframe.dispose();
 			actions = new String[]{actions[0]};
 		}
 		else if(s.contentEquals(modulename+"_PROPS_SAVE")){
 			Logger.getRootLogger().debug("Closing General Properties and Saving");
 			String[][] states = getChangableStats();
 			for(int i =0; i< states[0].length;i++){
 				Logger.getRootLogger().trace("Setting Property "+states[0][i]+" to " + propsframe.getInput(i));
 				setPropertyValue(states[0][i], propsframe.getInput(i));
 			}
 			this.propsframe.dispose();
 			actions = new String[]{actions[0]};
 		} 	
 	}
 
 	public void addToGui(EddieGUI eddiegui) {
 		actions = new String[1]; 
 		JMenuItem menuItem = new JMenuItem("General Properties");
 		menuItem.setMnemonic(KeyEvent.VK_P);
 	    menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.ALT_MASK));
 	    menuItem.setActionCommand(this.modulename);
 	    actions[0] = this.modulename;
 	    menuItem.addActionListener(eddiegui);
 	    Tools_Modules.add2JMenuBar(eddiegui.getMenu(), menuItem, "Properties", true);
 	}
 
 	public String getModuleName() {
 		return this.modulename;
 	}	
 	public void printTasks() {
 		// TODO Auto-generated method stub
 	}
 
 	public void actOnTask(String s, UI ui) {
 		// TODO Auto-generated method stub
 	}
 	
 	public void addToCli(EddieCLI cli) {
 		cli.setArgs(this.args);
 	}
 
 	public boolean isPersistant() {
 		return true;
 	}
 
 	public String[] getTasks() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public String[] getActions() {
 		return actions;
 	}
 	
 	public void resetModuleName(String name){
 		this.modulename = name;
 	}
 
 	public boolean isTest() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 	
 	
 }
