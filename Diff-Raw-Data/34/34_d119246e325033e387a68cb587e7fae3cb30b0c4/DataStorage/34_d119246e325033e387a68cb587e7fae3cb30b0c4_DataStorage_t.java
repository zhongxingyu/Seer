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
 package podsalinan;
 
 import java.io.File;
 import java.util.Vector;
 
 import com.almworks.sqlite4java.SQLiteConnection;
 import com.almworks.sqlite4java.SQLiteException;
 import com.almworks.sqlite4java.SQLiteJob;
 import com.almworks.sqlite4java.SQLiteQueue;
 import com.almworks.sqlite4java.SQLiteStatement;
 
 /**
  * @author bugman
  *
  */
 public class DataStorage {
 	private String settingsDir;
 	private final String CREATE_PODCAST = "CREATE TABLE IF NOT EXISTS podcasts (" +
 										  "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
 										  "name TEXT, " +
 										  "localFile TEXT, " +
 										  "url TEXT, " +
 										  "directory TEXT);",
 						CREATE_SETTINGS = "CREATE TABLE IF NOT EXISTS settings (" +
 										  "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
 										  "name TEXT, " +
 										  "value TEXT);",
 						CREATE_SHOWS = "CREATE TABLE IF NOT EXISTS shows(" +
 								 	   "id INTEGER PRIMARY KEY AUTOINCREMENT," +
 								 	   "published TEXT," +
 								 	   "title TEXT," +
 								 	   "url TEXT," +
 								 	   "size INTEGER," +
 								 	   "description TEXT," +
 								 	   "status INTEGER);",
 						CREATE_DOWNLOADS = "CREATE TABLE IF NOT EXISTS downloads(" +
 				  								"id INTEGER PRIMARY KEY AUTOINCREMENT," +
 				  								"url TEXT," +
 				  								"size TEXT," +
 				  								"destination TEXT," +
 				  								"priority INTGEGER," +
 				  								"podcastSource INTEGER," +
 				  								"status INTEGER);",
 						SELECT_ALL_PODCASTS = "SELECT * from podcasts;",
 						SELECT_ALL_SETTINGS = "SELECT * from settings;",
 						SELECT_ALL_DOWNLOADS = "SELECT * from downloads;",
 						CLEAR_ALL_SETTINGS = "DELETE from settings;" +
 								 			 "DELETE from sqlite_sequence WHERE name='settings';";
 	
 	/**
 	 * 
 	 */
 	public DataStorage(){
 		
 		// This if Block checks to see if it's windows or linux, and sets the
 		// settings directory appropriately.
 		if (System.getProperty("os.name").equalsIgnoreCase("linux"))
 			settingsDir = System.getProperty("user.home").concat("/.podsalinan");
 		else if (System.getProperty("os.name").startsWith("Windows"))
 			settingsDir = System.getProperty("user.home").concat("\\appdata\\local\\podsalinan");
 
 		// Check if the settings directory exists, and if not, create it.
 		File settings = new File(settingsDir);
 		if (!settings.exists()){
 			settings.mkdir();
 		}
 	}
 	
 	/** loadSettings loads the settings from the database.
 	 * 
 	 * @return
 	 */
 	public int loadSettings(Vector<Podcast> podcasts,
 							URLDownloadList downloads,
 							ProgSettings settings){
 		boolean firstRun = true;
 		SQLiteStatement sql;
 		
 		File podsalinanDBFile = new File(settingsDir.concat("/podsalinan.db"));
 		if (podsalinanDBFile.exists()){
 			SQLiteConnection podsalinanDB = new SQLiteConnection(podsalinanDBFile);
 			try {
 				podsalinanDB.open(true);
 				sql = podsalinanDB.prepare(SELECT_ALL_DOWNLOADS);
 				while (sql.step()){
 					if (sql.hasRow()){
 						URLDownload newDownload = new URLDownload(sql.columnString(1),
 																  sql.columnString(2),
 																  sql.columnString(3),
 																  sql.columnString(5),
 																  sql.columnInt(6));
 						newDownload.setAdded(true);
 						downloads.addDownload(newDownload, sql.columnInt(4));
 					}
 				}
 				sql.dispose();
 				sql = podsalinanDB.prepare(SELECT_ALL_SETTINGS);
 				while (sql.step()){
 					if (sql.hasRow()){
 						settings.addSetting(sql.columnString(1),
 								            sql.columnString(2));
 					}
 				}
 				sql.dispose();
 				sql = podsalinanDB.prepare(SELECT_ALL_PODCASTS);
 				while (sql.step()){
 					if (sql.hasRow()){
 						Podcast newPodcast = new Podcast(sql.columnString(1),
 													  	 sql.columnString(3),
 													  	 sql.columnString(4),
 													  	 sql.columnString(2).replaceAll("&apos;", "\'"));
 						newPodcast.setAdded(true);
 						podcasts.add(newPodcast);
 					}
 				}
 				sql.dispose();
 				podsalinanDB.dispose();
 			} catch (SQLiteException e) {
 				
 			}
 		} else {
 			/* This area is for the older version of the data files, so we can easily migrate to
 			 * the updated version. For instance, the downloads will now be held in a table
 			 * in the main data file. Where as in this code, the downloads we seperate.
 			 */
 			
 			// Path to the downloads database
 			File downloadsDBFile = new File(settingsDir.concat("/downloads.db"));
 			// does the download file exist
 			if (downloadsDBFile.exists()){
 				SQLiteConnection downloadDB = new SQLiteConnection(downloadsDBFile);
 				
 				try {
 					downloadDB.open(true);
 					sql = downloadDB.prepare("SELECT * FROM downloads;");
 					
 					while (sql.step()){
 						if (sql.hasRow()){
 							downloads.addDownload(sql.columnString(1),
 												  sql.columnString(3),
 												  sql.columnString(2),
 												  true);
 						}
 					}
 				} catch (SQLiteException e) {
 					e.printStackTrace();
 				}
 				
 			}
 			
 			String configFile = settingsDir.concat("/podcast.db");
 			
 			if (new File(configFile).exists()){
 				firstRun = false;
 				SQLiteConnection settingsDB = new SQLiteConnection(new File(configFile));
 			
 				try {
 					settingsDB.open(true);
 					
 					
 					if (!firstRun){
 						// Do a search in the podcasts table for podcasts stored in the system
 						sql = settingsDB.prepare(SELECT_ALL_PODCASTS);
 						while (sql.step()){
 							if (sql.hasRow()){
 								Podcast newPodcast = new Podcast(sql.columnString(1),
 															  	 sql.columnString(3),
 															  	 sql.columnString(4),
 															  	 sql.columnString(2).replaceAll("&apos;", "\'"));
 								newPodcast.setAdded(false);
 								podcasts.add(newPodcast);
 							}
 						}
 						sql.dispose();
 						sql = settingsDB.prepare(SELECT_ALL_SETTINGS);
 						while (sql.step()){
 							if (sql.hasRow()){
 								settings.addSetting(sql.columnString(1),
 													sql.columnString(2));
 							}
 						}
 						sql.dispose();
 						settingsDB.dispose();
 					}
 				} catch (SQLiteException e) {
 					return 1;
 				}
 			}
 
 		}
 		return 0;
 	}
 
 	/**
 	 * @param downloads 
 	 * 
 	 */
 	public void saveSettings(Vector<Podcast> podcasts,
 							 URLDownloadList downloads,
 							 ProgSettings settings) {
 		SQLiteStatement sql;
 		
 		File podsalinanDBFile = new File(settingsDir.concat("/podsalinan.db"));
 		boolean dataFileExists = podsalinanDBFile.exists(); 
 		SQLiteConnection podsalinanDB = new SQLiteConnection(podsalinanDBFile);
 		if (!dataFileExists){
 			try {
 				podsalinanDB.open(true);
 				sql = podsalinanDB.prepare(CREATE_DOWNLOADS);
 				sql.stepThrough();
 				sql.dispose();
 			} catch (SQLiteException e) {
 			}
 			try {
 				sql = podsalinanDB.prepare(CREATE_PODCAST);
 				sql.stepThrough();
 				sql.dispose();
 			} catch (SQLiteException e) {
 			}
 			try {
 				sql = podsalinanDB.prepare(CREATE_SETTINGS);
 				sql.stepThrough();
 				sql.dispose();
 			} catch (SQLiteException e) {
 			}
 		} else {
 			try {
 				podsalinanDB.open();
 			} catch (SQLiteException e) {
 			}
 		}
 		//podsalinanDB.dispose();
 		int downloadCount=0;
 		for (URLDownload download : downloads.getDownloads()){
 			//System.out.println("download: "+download.getURL());
 			try {
 				sql = null;
 				int sqlType=0;
 				if (!download.isAdded()){
 					//System.out.println("New download being added to database");
 					sql = podsalinanDB.prepare("INSERT INTO downloads(url,size,destination,priority,podcastSource,status) " +
 											   "VALUES ('"+download.getURL().toString()+"',"+
 											           "'"+download.getSize()+"',"+ 
 											   		   "'"+download.getDestination()+"',"+
 											           "'"+downloadCount+"',"+
 											   		   "'"+download.getPodcastId()+"',"+
 											           "'"+download.getStatus()+"');");
 					sqlType=1;
 				} else if (download.isRemoved()){
					System.out.println("URL= "+download.getURL().toString());
					System.out.println("Destination= "+download.getDestination());
 					sql = podsalinanDB.prepare("DELETE FROM downloads " +
 											   "WHERE url='"+download.getURL().toString()+"'"+
 											   "AND destination='"+download.getDestination()+"';");
 					//System.out.println("download being removed to database");
 				} else if (download.isUpdated()){
 					sql = podsalinanDB.prepare("UPDATE downloads " +
 											   "SET destination='"+download.getDestination()+"',"+
 											   	   "size='"+download.getSize()+"',"+
 											       "priority='"+downloadCount+"',"+
 											       "podcastSource='"+download.getPodcastId()+"',"+
 											       "status='"+download.getStatus()+"'"+
 											   "WHERE url='"+download.getURL()+"';");
 					sqlType=3;
 					//System.out.println("download being updated to database");
 				}
 				if (sql!=null){
 					//System.out.println(sql.toString());
 					//podDBQueue.execute(new SQLiteJob());
 					sql.stepThrough();
 					sql.dispose();
 					switch (sqlType){
 					case 1:
 						download.setAdded(true);
 						break;
 					case 3:
 						download.setUpdated(false);
 						break;
 				}
 
 				}
 			} catch (SQLiteException e) {
 				e.printStackTrace();
 			}
 			downloadCount++;
 		}
 		// clear the settings
 		try {
 			sql = podsalinanDB.prepare(CLEAR_ALL_SETTINGS);
 			sql.stepThrough();
 			sql.dispose();
 		} catch (SQLiteException e){
 		}
 		// add settings back into the database
 		for (Setting setting : settings.getArray()){
 			try {
 				sql = podsalinanDB.prepare("INSERT INTO settings(name,value)" +
 						 				   "VALUES ('"+setting.name+"'," +
 						 				           "'"+setting.value+"');");
 				sql.stepThrough();
 				sql.dispose();
 			} catch (SQLiteException e) {
 			} 
 		}
 		
 		// update the podcast list in the database
 		for (Podcast podcast : podcasts){
 			sql = null;
 			int sqlMethod=0;
 			try {
 				if ((!podcast.isAdded())&&(!podcast.isRemoved())){
 					sql = podsalinanDB.prepare("INSERT INTO podcasts(name, localFile, url, directory)" +
 							 				   "VALUES ('"+podcast.getName()+"',"+
 							 				   "'"+podcast.getDatafile()+"'," +
 							 				   "'"+podcast.getURL()+"'," +
 							 				   "'"+podcast.getDirectory()+"');");
 					sqlMethod=1;
 				} else if (podcast.isRemoved()){
 					sql = podsalinanDB.prepare("DELETE FROM podcasts " +
 											   "WHERE localfile='"+podcast.getDatafile()+"',");
 				} else if (podcast.isChanged()){
 					sql = podsalinanDB.prepare("UPDATE podcasts " +
 											   "SET name='"+podcast.getName()+"',"+
 											       "directory='"+podcast.getDirectory()+"',"+
 											       "url='"+podcast.getURL()+"'"+
 											   "WHERE localFile='"+podcast.getDatafile()+"';");
 					sqlMethod=3;
 				}
 				if (sql!=null){
 					sql.stepThrough();
 					sql.dispose();
 					switch (sqlMethod){
 						case 1:
 							podcast.setAdded(true);
 							break;
 						case 3:
 							podcast.setChanged(false);
 							break;
 					}
 				}
 			} catch (SQLiteException e) {
 			}
 		}
 	}
 
 	public void loadPodcast(Podcast podcast){
 		String feedFilename=settingsDir.concat("/"+podcast.getDatafile()+".pod");
 		SQLiteConnection feedDb = new SQLiteConnection (new File(feedFilename));
 		
 		try {
 			feedDb.open();
 			
 			/* The following 13 lines are used to see if the status field is found in the shows table
 			 * if it is not, it is added before reading the information from the podcast data file.
 			 */
 			SQLiteStatement sql = feedDb.prepare("PRAGMA table_info(shows);");
 			boolean statusFound=false;
 			
 			// While loop jumps through the table description
 			while (sql.step())
 				for (int columnCounter=0; columnCounter<sql.columnCount(); columnCounter++)
 					// does the column name equal status?
 					if ((sql.getColumnName(columnCounter).equalsIgnoreCase("name"))&&
 						(sql.columnString(columnCounter).equalsIgnoreCase("status")))
 						statusFound=true;
 					
 			sql.dispose();
 			
 			// If status is not found change the table to add it.
 			if (!statusFound){
 				sql = feedDb.prepare("ALTER TABLE shows ADD status INTEGER;");
 				sql.stepThrough();
 				sql.dispose();
 			}
 			
 			// Read the episodes from the podcast database.
 			sql = feedDb.prepare("SELECT * FROM shows;");
 			while (sql.step()){
 				Episode ep = new Episode(sql.columnString(1),
 										 sql.columnString(2).replaceAll("&apos;", "\'"),
 										 sql.columnString(3).replaceAll("&apos;", "\'"),
 										 sql.columnString(4),
 										 sql.columnString(5).replaceAll("&apos;", "\'"),
 										 sql.columnInt(6));
 				ep.setAdded(true);
 				//podcast.getEpisodes().add(ep);
 				podcast.addEpisode(ep);
 			}
 			sql.dispose();
 		} catch (SQLiteException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * 
 	 * @param episodes
 	 * @param feedDBName
 	 */
 	public void savePodcast(Podcast savedPodcast){
 		boolean feedDBExists=false;
 		SQLiteStatement sql;
 		
 		String feedFilename=settingsDir.concat("/"+savedPodcast.getDatafile()+".pod");
 		
 		if (new File(feedFilename).exists())
 			feedDBExists = true;
 		
 		SQLiteConnection feedDB = new SQLiteConnection(new File(feedFilename));
 		try {
 			feedDB.open(true);
 			
 			if (!feedDBExists){
 				sql = feedDB.prepare(CREATE_SHOWS);
 				sql.stepThrough();
 				sql.dispose();
 			}
 			for (int epCount=0; epCount< savedPodcast.getEpisodes().size(); epCount++){
 				Episode currentEpisode = savedPodcast.getEpisodes().get(epCount);
 				if (!currentEpisode.isAdded()){
 					sql = feedDB.prepare("INSERT INTO shows(published,title,url,size,description,status)" +
 							"						VALUES ('"+currentEpisode.getDate()+"'," +
 							 							   "'"+currentEpisode.getTitle().replaceAll("\'", "&apos;")+"'," +
 							 							   "'"+currentEpisode.getURL().toString().replaceAll("\'", "&apos;")+"'," +
 							 							   "'"+currentEpisode.getSize()+"'," +
 							 							   "'"+currentEpisode.getDescription().replaceAll("\'", "&apos;")+"'," +
 							 							   	   currentEpisode.getStatus()+");");
 					sql.stepThrough();
 					sql.dispose();
 					currentEpisode.setAdded(true);
 				} else if (currentEpisode.isUpdated()) {
 					// If the episode has been updated. it will up updated in the database.
 					sql = feedDB.prepare("UPDATE shows " +
 					 		   			 "SET status="+currentEpisode.getStatus()+", " +
 					 		   			     "description='"+currentEpisode.getDescription().replaceAll("\'", "&apos;")+"' " +
 					 		   			 "WHERE url='"+currentEpisode.getURL().toString().replaceAll("\'", "&apos;")+"';");
 					sql.stepThrough();
 					sql.dispose();
 					currentEpisode.setUpdated(false);
 				}
 			}
 			feedDB.dispose();
 		} catch (SQLiteException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public String getSettingsDir(){
 		return settingsDir;
 	}
 }
