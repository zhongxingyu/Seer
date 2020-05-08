 /*  Copyright (C) 2011  Nicholas Wright
 	
 	part of 'Aid', an imageboard downloader.
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package app;
 
 import gui.Aid;
 import gui.BlockList;
 import gui.BlockListDataModel;
 import gui.BoardListDataModel;
 import gui.Filterlist;
 import gui.Log;
 import gui.Stats;
 import io.BoneConnectionPool;
 import io.ConnectionPool;
 import io.FileWriter;
 import io.ImageLoader;
 import io.MySQL;
 import io.MySQLaid;
 import io.SchemaUpdateException;
 import io.SchemaUpdater;
 import io.ThumbnailLoader;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Properties;
 import java.util.logging.LogManager;
 import java.util.logging.Logger;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JOptionPane;
 import javax.swing.UIManager;
 
 import thread.WorkQueue;
 import board.Board;
 import board.Page;
 import board.PageFactory;
 import config.DefaultAppSettings;
 import config.DefaultLoggerSettings;
 import config.DefaultMySQLconnection;
 
 /**
  * This is a Image board downloader that will check for new files at regular intervals
  */
 public class Main implements ActionListener{
 	//TODO add a setting file (window pos, ect.)
 	//TODO add favorite tag to folders (keywords)
 
 	String saveDirectory = "";				// Default Drive
 	String defaultDirectory = null;
 
 	final int STARTUP_DELAY = 7; // delay between board starts when using "Start All", in minutes
 
 	boolean SkipLogEnabled = false;
 
 	private static Logger logger = Logger.getLogger(Main.class.getName());
 	private PropertyChangeSupport change = new PropertyChangeSupport(this);
 
 	private io.FileWriter fileWriter;
 	private filter.Filter filter;
 	private ImageLoader imageLoader;
 
 	private Aid aid;
 	private Filterlist filterlist;
 	private BlockList blockList;
 	private WorkQueue pageQueue;
 	private BlockListDataModel blockListModel;
 	private ThumbnailLoader thumbLoader;
 	private ConnectionPool connPool;
 	private MySQLaid mySQL;
 
 	private BoardListDataModel boards = new BoardListDataModel();
 	Properties appSettings = new DefaultAppSettings();
 	Properties loggerSettings = new DefaultLoggerSettings();
 	Properties sqlProps = new DefaultMySQLconnection();
 
 	private final String PWD = System.getProperty("user.dir");
 	private final String LOGGER_CFG_FILENAME = "logging.ini";
 	private final String MYSQL_CFG_FILENAME = "mysql.ini";
 	private final String APP_CFG_FILENAME = "config.ini";
 	private final String FILTER_DATA_FILENAME = "filter.dat";
 	
 	private final String DEFAULT_PAGE_THREADS = "1";
 	private final String DEFAULT_IMAGE_THREADS = "1";
 	private final String DEFAULT_WRITE_BLOCKED = "false";
 	private final String DEFAULT_BASE_URL = "http://boards.4chan.org/";
 	private final String DEFAULT_SUB_PAGES = "a;15,w;15,wg;15";
 	
 	public static void main(String[] args) {
 		new Main().init();
 	}
 
 	/**
 	 * This method constructs all Objects
 	 */
 	final private void build(){
 		String page, image, writeBlocked, baseUrl = "", subPages = "";
 		int pageThreads = 1, imageThreads = 1;
 		boolean writeBlock = false;
 		
 		//  -------------- Configuration loading starts here --------------
 		appSettings = loadAppConfig(APP_CFG_FILENAME);
 
 		if(! SettingValidator.validateAppSettings(appSettings)){
 			String message = "One or more program settings are invalid. Please correct them and restart the program.\n"
 					+ " to reset to the default values, delete "+APP_CFG_FILENAME+" and restart the program.";
 			logger.severe(message);
 			dieWithError(message, 1);
 		}
 		
 		page = appSettings.getProperty("page_threads",DEFAULT_PAGE_THREADS);
 		image = appSettings.getProperty("image_threads",DEFAULT_IMAGE_THREADS);
 		writeBlocked = appSettings.getProperty("write_Blocked",DEFAULT_WRITE_BLOCKED);
 		baseUrl = appSettings.getProperty("base_url",DEFAULT_BASE_URL);
 		subPages = appSettings.getProperty("sub_pages",DEFAULT_SUB_PAGES);
 
 		if(page != null){pageThreads = Integer.parseInt(page);}
 		if(image != null){imageThreads = Integer.parseInt(image);}
 		if(writeBlocked != null){writeBlock = Boolean.parseBoolean(writeBlocked);}
 
 		defaultDirectory = appSettings.getProperty("default_directory",null);
 
 		if(defaultDirectory == null){
 			saveDirectory = (String)JOptionPane.showInputDialog(
 					aid,
 					"Enter a path save path",
 					"Select Drive",
 					JOptionPane.PLAIN_MESSAGE
 					);
 			if(saveDirectory != null){
 				new File(saveDirectory).mkdirs();
 				appSettings.setProperty("default_directory", saveDirectory);
 			}
 		}else{
 			saveDirectory = defaultDirectory;
 		}
 
 		if((saveDirectory == null)||(saveDirectory.equals(""))){
 			String message = saveDirectory+" is an invalid save location. Quitting...";
 			dieWithError(message, 2);
 		}
 
 		File basePath = new File(saveDirectory);
 		if(! basePath.exists()){
 			String message = saveDirectory + " is an invalid save location. Quitting...";
 			dieWithError(message, 3);
 		}
 
 		sqlProps = loadMySqlConfig(MYSQL_CFG_FILENAME);
 
 		if(sqlProps == null){
 			String message = "Unable to load MySQL config.\nCheck settings and restart the Programm.";
 			dieWithError(message, 4);
 		}
 
 		//  -------------- Class instantiation starts here --------------  //
 		pageQueue = new WorkQueue(pageThreads, pageThreads, 100);
 		connPool = new BoneConnectionPool(sqlProps,10); // connection pool for database connections
 		try {
 			connPool.startPool();
 		} catch (Exception e) {
			String message = "Unable to connect to database.";
 			dieWithError(message, 7);
 		}
 		blockListModel = new BlockListDataModel();
 		mySQL = new MySQLaid(connPool);
 		thumbLoader = new ThumbnailLoader(mySQL);
 		DefaultListModel<String> fileNameModel = new DefaultListModel<>();
 		DefaultListModel<String> postContentModel = new DefaultListModel<>();
 		filter = new filter.Filter(mySQL,blockListModel,fileNameModel, postContentModel, thumbLoader); // filter handler
 		filterlist = new Filterlist(filter, fileNameModel, postContentModel); // filter GUI
 		fileWriter = new FileWriter(filter); // disk IO
 
 		imageLoader = new ImageLoader(fileWriter, filter, basePath,imageThreads);
 		logger.info("Saving files to the basePath "+basePath.toString());
 		blockList = new BlockList(filter,blockListModel);
 
 		// parse subpages
 		String[] subP = subPages.split(",");
 
 		for(String s : subP){
 			String[] param = s.split(";");
 			try{
 				ArrayList<Page> pList = PageFactory.makePages(baseUrl+param[0], Integer.parseInt(param[1]), filter, imageLoader);
 				Board b = new Board(pList, pageQueue,param[0]);
 				boards.addElement(b);
 			}catch(IndexOutOfBoundsException oob){
 				logger.warning("Sub_pages is not configured correctly");
 			}
 		}
 
 		aid = new Aid(boards,this);
 		Stats.addStatListener(aid);
 		fileWriter.setWriteBlocked(writeBlock);
 	}
 
 	/**
 	 * This Method initializes the Program
 	 */
 	private void init(){
 		// load the logger settings
 		loggerSettings = loadLoggerConfig(LOGGER_CFG_FILENAME);
 		
 		String mysqljdbc = "mysql-connector-java-5.1.15-bin.jar";
 		if(! (new File(mysqljdbc)).canRead()) {
 			String message = "Required library file " + mysqljdbc + " could not be found.";
 			dieWithError(message, 5);
 		}
 
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (Exception e){
 			logger.warning("Unable to load look and feel: "+e.getMessage());
 		}
 
 		// shutdown Thread will be called on exit
 		Runtime.getRuntime().addShutdownHook(new Shutdown());
 
 		// so java will not be appended to http user-agent field
 		System.setProperty("http.agent", ""); 
 
 		logger.info("Working directory is: " + PWD);
 
 		// create all needed classes
 		build();
 		try {
 			SchemaUpdater.update(new MySQL(connPool), new InternalSetting());
 		} catch (SchemaUpdateException e) {
 			String message = "Schema update failed: "+e.getMessage();
 			dieWithError(message, 6);
 		}
 		
 		InputStream is = null;
 		try{
 			is = new FileInputStream(FILTER_DATA_FILENAME);
 			if(is != null){
 				filter.loadFilter(is); // load list of filtered items
 				is.close();
 			}
 		}catch(IOException ioe){
 			logger.warning("Error accessing file " + ioe.getMessage());
 		}
 
 		filter.startUpdater();
 		aid.setVisible(true);
 
 		logger.info("Initialization complete");
 		Log.add("Initialization complete");
 	}
 	
 	private void dieWithError(String message, int errorCode){
 		logger.severe(message);
 		JOptionPane.showMessageDialog(null, message, "Fatal Error", JOptionPane.ERROR_MESSAGE);
 		System.exit(errorCode);
 	}
 
 
 	/**
 	 * Load the logger settings from the file
 	 */
 	public Properties loadLoggerConfig(String filepath){
 		// Load logger Configuration
 		try{
 			InputStream is = new FileInputStream(filepath);
 			Properties loggerSettings = new Properties();
 			loggerSettings.load(is);
 			is.close();
 
 			is = new FileInputStream(filepath);
 			LogManager.getLogManager().readConfiguration(is);
 			logger.info("Logger config Loaded");
 			is.close();
 
 			return loggerSettings;
 		}catch(IOException ioe){
 			logger.info("Error accessing file "+ioe.getMessage());
 			return new DefaultLoggerSettings();
 		}
 	}
 
 	/**
 	 * Load the mySql settings
 	 */
 	public Properties loadMySqlConfig(String filepath){
 		try {
 			Properties sqlProps = new DefaultMySQLconnection(); 
 			InputStream is = new FileInputStream(filepath);
 			if(is != null)
 				sqlProps.load(is);
 			return sqlProps;
 		} catch (IOException ioe) {
 			logger.warning("Error accessing file "+ ioe.getMessage());
 			return new DefaultMySQLconnection();
 		}
 	}
 	
 	/**
 	 * Loads the Application configuration from file.
 	 * If an error occurs whilst loading the configuration, a default config
 	 * is returned.
 	 * @return Property Object containing the configuration.
 	 */
 	public Properties loadAppConfig(String filepath){
 		try {
 			// try to load from file
 			Properties appSetting = new DefaultAppSettings();
 			InputStream is = new FileInputStream(filepath);
 			if(is != null)
 				appSetting.load(is);
 			
 			
 			
 			return appSetting;
 		} catch (IOException ioe) {
 			logger.warning("Error accessing file "+ ioe.getMessage());
 			return new DefaultAppSettings();
 		}
 	}
 	
 	public void actionPerformed(ActionEvent e){
 		if("Clear ImageQueue".equals(e.getActionCommand())){
 			imageLoader.clearQueue();
 		}
 
 		if("Clear PageQueue".equals(e.getActionCommand())){
 			pageQueue.clearQueue();
 		}
 
 		if("Prune cache".equals(e.getActionCommand())){
 			filter.pruneCache();
 		}
 
 		if("Filter".equals(e.getActionCommand())){
 			blockList.setVisible(true);
 		}
 
 		if("Filterlist".equals(e.getActionCommand())){
 			filterlist.setVisible(true);
 		}
 
 		if ("Clear stats".equals(e.getActionCommand())){
 			fileWriter.clearStats();
 			Log.add("Stats cleared");
 		}
 	}
 	
 	public void addPropertyChangeListener(PropertyChangeListener listener){
 		change.addPropertyChangeListener(listener);
 	}
 
 	/**
 	 * Thread for the Shutdown hook.
 	 * This Thread will be called when the Program is shut down.
 	 */
 	class Shutdown extends Thread{
 		@Override
 		public void run(){
 			//FIXME Logger shutdown hook seems to be executed too, closing the LogHandlers. 
 			logger.info("Shutting down...");
 
 			// stop boards
 			for(Object o : boards.toArray()){
 				((Board)o).stop();
 			}
 
 			logger.info("Clearing queue...");
 			// clear queue
 			if(pageQueue != null)
 				pageQueue.clearQueue();
 
 			// shutdown file downloading
 			if(imageLoader != null)
 				imageLoader.shutdown();
 
 			// shutdown writing to disk
 			try {
 				if(fileWriter != null){
 					fileWriter.shutdown();
 					fileWriter.join();
 				}
 			} catch (InterruptedException e) {}
 
 			// close all DB connections
 			if(connPool != null)
 				connPool.stopPool();
 
 			// save the thread filter
 			if(filter != null)
 				filter.saveFilter(new File(PWD,FILTER_DATA_FILENAME));
 
 			// save program settings
 			try {
 				appSettings.store(new FileOutputStream(APP_CFG_FILENAME), "General application settings");
 			} catch (IOException e) {
 				logger.warning("Unable to save configuration "+e.getMessage());
 			}
 
 			// save logger settings
 			try {
 				loggerSettings.store(new FileOutputStream(LOGGER_CFG_FILENAME), "Java logger settings");
 			} catch (IOException e) {
 				logger.warning("Unable to save configuration "+e.getMessage());
 			}
 
 			// save mysql settings
 			try {
 				sqlProps.store(new FileOutputStream(MYSQL_CFG_FILENAME), "MySQL connection settings");
 			} catch (IOException e) {
 				logger.warning("Unable to save configuration "+e.getMessage());
 			}
 
 			logger.info("Shutdown complete.");
 		}
 	}	
 }
