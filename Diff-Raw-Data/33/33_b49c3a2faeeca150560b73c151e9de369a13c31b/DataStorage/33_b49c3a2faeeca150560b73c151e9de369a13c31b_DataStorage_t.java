 /*******************************************************************************
  * Copyright (c) 2011 Sam Bell.
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published
  * by the Free Software Foundation, either version 3 of the License,
  * or  any later version.
  * 
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
  * the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  * Contributors:
  *     Sam Bell - initial API and implementation
  ******************************************************************************/
 package com.mimpidev.podsalinan;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.mimpidev.podsalinan.data.PodcastList;
 import com.mimpidev.podsalinan.data.ProgSettings;
 import com.mimpidev.podsalinan.data.URLDownloadList;
 import com.mimpidev.podsalinan.data.loader.DownloadsLoader;
 import com.mimpidev.podsalinan.data.loader.PodcastLoader;
 import com.mimpidev.podsalinan.data.loader.SettingsLoader;
 import com.mimpidev.podsalinan.data.loader.TableLoader;
 import com.mimpidev.sql.sqlitejdbc.Database;
 import com.mimpidev.sql.sqlitejdbc.exceptions.SqliteException;
 
 /**
  * @author bugman
  *
  */
 public class DataStorage {
     /**
      * URLDownloadList is storage area for all url downloads 
      */
 	private URLDownloadList urlDownloads;
 	/**
 	 * PodcastList is the storage area for all of the podcasts
 	 */
 	private PodcastList podcasts;
 	/**
 	 * settings is where the program settings are listed
 	 */
 	private ProgSettings settings;
 	private String settingsDir;
 	private Object finishWait= new Object();
 	/**
 	 * 
 	 */
 	private ArrayList<TableLoader> tableLoaders;
 	/**
 	 *  Used to Define the current File system slash.
 	 */
 	private String fileSystemSlash = "/";
 	
 	public DataStorage(){
 		podcasts = new PodcastList();
 		urlDownloads = new URLDownloadList(podcasts);
 		settings = new ProgSettings();
 		tableLoaders = new ArrayList<TableLoader>();
 		
 		checkSettingsDirectory();
 	}
 	
 	public DataStorage(PodcastList podcasts, URLDownloadList urlDownloads,
 			ProgSettings settings) {
 		setPodcasts(podcasts);
 		setUrlDownloads(urlDownloads);
 		urlDownloads.setPodcasts(podcasts.getList());
 		setSettings(settings);
 		tableLoaders = new ArrayList<TableLoader>();
 		
 		checkSettingsDirectory();
 	}
 	
 	/**
 	 *  This is used to set settingsDir and fileSystemSlash for the current operating
 	 *  system.
 	 */
 	public void checkSettingsDirectory(){
 		// This if Block checks to see if it's windows or linux, and sets the
 		// settings directory appropriately.
 		if (System.getProperty("os.name").equalsIgnoreCase("linux")){
 			settingsDir = System.getProperty("user.home").concat("/.podsalinan");
 			fileSystemSlash = "/";
 		}else if (System.getProperty("os.name").startsWith("Windows")){
 			settingsDir = System.getProperty("user.home").concat("\\appdata\\local\\podsalinan");
 			fileSystemSlash = "\\";
 		}
 
 		// Check if the settings directory exists, and if not, create it.
 		File settingsLocation = new File(settingsDir);
 		if (!settingsLocation.exists()){
 			settingsLocation.mkdir();
 		}
 		
 		File tempXMLFile = new File (settingsDir+fileSystemSlash+"temp.xml");
 		if (tempXMLFile.exists())
 			tempXMLFile.delete();
 
 	}
 
 	/**
 	 * Used to load this object's podcasts, downloads, and settings. 
 	 * @return
 	 */
 	public int loadSettings(){
 		return loadSettings(podcasts, urlDownloads, settings);
 	}
 	
 	/**
  	 * Used to load the passed in podcast Vector, download array and settings.
 	 * @param podcasts The Vector list used to store the values being read.
 	 * @param downloads The download list used to store the values being read.
 	 * @param settings The settings list used to store the values being read.
 	 * @return Success status -1 is failure 0 is success
 	 */
 	public int loadSettings(PodcastList podcasts,
 							URLDownloadList downloads,
 							ProgSettings settings){
 		
 		File podsalinanDBFile = new File(settingsDir.concat(fileSystemSlash+"podsalinan.db"));
 		if (podsalinanDBFile.exists()){
 			Database podsalinanDB=null;
 			try {
 				podsalinanDB = new Database(podsalinanDBFile.getAbsolutePath());
 			} catch (SqliteException e) {
 				Podsalinan.debugLog.printStackTrace(e.getStackTrace());
 			} catch (ClassNotFoundException e) {
				Podsalinan.debugLog.logError("JDBC library not found. Exiting");
				System.exit(1);
				//Podsalinan.debugLog.printStackTrace(e.getStackTrace());
 			}
 			
 			PodcastLoader podcastHandler=null;
 			DownloadsLoader downloadHandler=null;
 			SettingsLoader settingsHandler=null;
 			if (podsalinanDB!=null){
 				try {
 					podcastHandler = new PodcastLoader(podcasts,podsalinanDB);
 					downloadHandler = new DownloadsLoader(downloads,podsalinanDB);
 					settingsHandler = new SettingsLoader(settings,podsalinanDB);
 				} catch (ClassNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				tableLoaders.add(podcastHandler);
 				tableLoaders.add(downloadHandler);
 				tableLoaders.add(settingsHandler);
 				for (TableLoader loader : tableLoaders){
 					try {
 						loader.readTable();
 					} catch (ClassNotFoundException e) {
 						Podsalinan.debugLog.printStackTrace(e.getStackTrace());
 					}
 				}
 			}
 
 			
 			/*
 			try {
 				podsalinanDB.close();
 			} catch (SqlJetException e) {
 				Podsalinan.debugLog.printStackTrace(e.getStackTrace());
 				return -1;
 			}
 			*/
 		}
 		if (!settings.isValidSetting("defaultDirectory")){
 			if (System.getProperty("os.name").startsWith("Windows"))
 			    settings.addSetting("defaultDirectory", System.getProperty("user.home").concat("\\Download"));
 			else
 			    settings.addSetting("defaultDirectory", System.getProperty("user.home").concat("/Download"));
 		}
 		return 0;
 	}
 	
 	/**
 	 * This is used to save the locally stored information to the databases
 	 */
 	public void saveSettings(){
 		saveSettings(podcasts,urlDownloads,settings);
 	}
 
     /**
      * This is used to save the passed in information to the databases 
      * @param podcasts The podcast Vector
      * @param downloads The downloads Array
      * @param settings The settings Array
      */
 	public void saveSettings(PodcastList podcasts,
 							 URLDownloadList downloads,
 							 ProgSettings settings) {
 
 	    for (TableLoader loader : tableLoaders){
 	    	loader.updateDatabase();
 	    }
 	}
 	
 	/**
 	 * 
 	 * @return String for settings directory 
 	 */
 	public String getSettingsDir(){
 		return settingsDir;
 	}
 
 	/**
 	 * @return Download List
 	 */
 	public URLDownloadList getUrlDownloads() {
 		return urlDownloads;
 	}
 
 	/**
 	 * @param urlDownloads Array of Downloads
 	 */
 	public void setUrlDownloads(URLDownloadList urlDownloads) {
 		this.urlDownloads = urlDownloads;
 	}
 
 	/**
 	 * @return Array list of Podcasts
 	 */
 	public PodcastList getPodcasts() {
 		return podcasts;
 	}
 
 	/**
 	 * @param podcasts Array of Podcasts
 	 */
 	public void setPodcasts(PodcastList podcasts) {
 		this.podcasts = podcasts;
 	}
 
 	/**
 	 * @return Array list of Settings
 	 */
 	public ProgSettings getSettings() {
 		return settings;
 	}
 
 	/**
 	 * @param settings Array of Program Settings
 	 */
 	public void setSettings(ProgSettings settings) {
 		this.settings = settings;
 	}
 
 	/**
 	 * @return the type of file system slash
 	 */
 	public String getFileSystemSlash() {
 		return fileSystemSlash;
 	}
 
 	/**
 	 * @param fileSystemSlash Defines the current file system slash
 	 */
 	public void setFileSystemSlash(String fileSystemSlash) {
 		this.fileSystemSlash = fileSystemSlash;
 	}
 
 	/**
 	 * @return object for waiting till the reading is finished
 	 */
 	public Object getFinishWait() {
 		return finishWait;
 	}
 
 	/**
 	 * @param finishWait the finishWait to set
 	 */
 	public void setFinishWait(Object finishWait) {
 		this.finishWait = finishWait;
 	}
 	
 	/**
 	 * This is a directory scanning method, which will create a list of files recursive
 	 * in the directory passed in.
 	 * @param directory Directory to be scanned for files
 	 * @param fileList Array of File objects found 
 	 */
 	public void scanDirectory(File directory, List<File> fileList){
 		if (directory == null)
 			return;
 		if ((directory.exists())&&
 			(directory.isDirectory())){
 			File[] files = directory.listFiles();
 			for (File file : files){
 				if (file.isDirectory())
 					scanDirectory(file,fileList);
 				else
 					fileList.add(file);
 			}
 		}
 	}
 }
