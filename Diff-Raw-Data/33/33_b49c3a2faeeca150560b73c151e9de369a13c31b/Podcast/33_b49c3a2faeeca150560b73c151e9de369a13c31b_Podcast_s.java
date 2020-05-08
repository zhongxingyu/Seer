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
 /**
  *  This is the class used to store details about the rss feed
  */
 package com.mimpidev.podsalinan.data;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Map;
 import java.util.Vector;
 
 import com.mimpidev.dev.sql.field.BooleanType;
 import com.mimpidev.dev.sql.field.StringType;
 import com.mimpidev.podsalinan.DataStorage;
 import com.mimpidev.podsalinan.Downloader;
 import com.mimpidev.podsalinan.Podsalinan;
 import com.mimpidev.podsalinan.XmlReader;
 
 /**
  * @author bugman
  *
  */
 public class Podcast extends DownloadDetails{
 
 	/**
 	 * 
 	 */
 	private Vector<Episode> episodeList = new Vector<Episode>(); // Used to store the the downloads, seperate from the DownloadList
 	/**
 	 * 
 	 */
 	private DateFormat df;
 	/**
 	 * 
 	 */
 	private String settingsDir = null;
 	/**
 	 * 
 	 */
 	public Podcast() {
 		super();
 		fields.put("image", new StringType());
 		fields.put("automaticQueue", new BooleanType());
 		df = new SimpleDateFormat(Episode.getDateFormat());
 	}
 	/**This is used to create a new Podcast with only a url.
 	 * 
 	 * @param newURL
 	 */
 	public Podcast(String newURL){
 		this();
 		
 		this.setURL(newURL);
 	}
 	
 	/** Used to create a Podcast from the information in the systems database. 
 	 * 
 	 * @param newName
 	 * @param newURL
 	 * @param newDirectory
 	 * @param newDatastore
 	 */
 	public Podcast(String newName, String newURL, String newDirectory, String newDatastore){
 		this(newURL);
 		
 		setName(newName);
 		setDirectory(newDirectory);
 		setDatafile(newDatastore);
 	}
 	
 	public Podcast(String newName, String newURL, String newDirectory, String newDatastore, boolean autoDownload){
 		this(newName,newURL,newDirectory,newDatastore);
 		setAutomaticQueue(autoDownload);
 	}
 	/**
 	 * This constructor will take the map array of values, and
 	 * create a new Podcast object with those values
 	 * @param record
 	 */
 	public Podcast(Map<String, String> record) {
 		this();
 		populateFromRecord(record);
 		if ((!getDatabaseRecord().containsKey("localFile"))&&
 				(record.containsKey("localFile"))&&
 				(fields.get("datafile").getValue().equals("")||fields.get("datafile").getValue()!=null)){
 				fields.get("datafile").setValue(record.get("localFile"));
 				setUpdated(true);
 		}
 	}
 	
 	public Vector<Episode> getEpisodes(){
 		return episodeList;
 	}
 	
 	public void setURL(String url){
 		super.setURL(url);
 	}
 	
 	public String getURL(){
 		return super.getURL();
 	}
 
 	public void setDirectory(String directory) {
 		fields.get("directory").setValue(directory);
 	}
 
 	public String getDirectory() {
 		return fields.get("directory").getValue();
 	}
 
 	/**
 	 * @return the image
 	 */
 	public String getImage() {
 		return fields.get("image").getValue();
 	}
 
 	/**
 	 * @param image the image to set
 	 */
 	public void setImage(String image) {
 		fields.get("image").setValue(image);
 	}
 
 	/**
 	 * 
 	 * @param string
 	 */
 	public void setSettingsDir(String newSettingsDir) {
 		settingsDir = newSettingsDir;
 	}
 	
 	public String getSettingsDir() {
 		return settingsDir;
 	}
 
 	/**
 	 * @return the automaticQueue
 	 */
 	public boolean isAutomaticQueue() {
 		return fields.get("automaticQueue").equals("TRUE");
 	}
 
 	/**
 	 * @param automaticQueue the automaticQueue to set
 	 */
 	public void setAutomaticQueue(boolean automaticQueue) {
 		fields.get("automaticQueue").setValue((automaticQueue?"TRUE":"FALSE"));
 	}	
 	
 	/**
 	 * This method calls updateList(String,boolean), but automatically sets manualUpdate to false so
 	 * that the autoUpdater in the program will not have to be rewritten.
 	 * @param tempDir
 	 */
 	public void updateList(String tempDir){
 		updateList(tempDir,false);
 	}
 	
 	/**
 	 * This method is used when the user manually prompts to update the podcast
 	 * @param tempDir
 	 * @param manualUpdate
 	 */
 	public void updateList(String tempDir, boolean manualUpdate){
 		if (tempDir!=null){
 			File outputFile;
 			if (!manualUpdate)
 				outputFile = new File(tempDir+"/temp.xml");
 			else
 				outputFile = new File(tempDir+"/manualtemp.xml");
 			
 			if ((manualUpdate)&&(outputFile.exists()))
 				outputFile.delete();
 			
 			/* Added this, so that if an update occurs while another update is already happening,
 			 * the requested update wont occur. If the system is already in the middle of a full
 			 * update of the podcasts, it wont matter if the user has requested for an update, as
 			 * one will either have already occurred, or will occur shortly. 
 			 */
 			if (!outputFile.exists()){
 				try {
 					Downloader downloader = new Downloader(new URL(fields.get("url").getValue()), outputFile);
 					int result = downloader.getFile();
 					if (result==Downloader.DOWNLOAD_COMPLETE){
 						XmlReader xmlfile = new XmlReader();
 						
 						// Read the episode list from the xml file.
 						Vector<Episode> newEpisodeList = xmlfile.parseEpisodes(new FileInputStream(outputFile));
 						synchronized(episodeList){
 							if (episodeList!=null){
 								for (Episode newEpisode : newEpisodeList){
 									boolean foundEpisode=false;
 									int episodeCount=0;
 									// Using a while loop here, because we don't want to continue looking for an episode
 									// if it is already found
 									while ((!foundEpisode)&&(episodeCount<episodeList.size())){
 										// This code does not seem to be working, as it should be finding the url :(
 										Episode currentEpisode = episodeList.get(episodeCount);
 										if (newEpisode.getURL().toString().equalsIgnoreCase(currentEpisode.getURL().toString()))
 											foundEpisode=true;
 										else
 											episodeCount++;
 									}
 									if (!foundEpisode){
 										addEpisode(newEpisode);
 										//System.out.println("Not Found Episode: "+newEpisode.getURL().toString());
 									}// else
 									//	System.out.println("Found Episode: "+newEpisode.getURL().toString());
 								}
 							}
 						}
 					}
 					// Delete the temp file from the filesystem
 					outputFile.delete();
 				} catch (MalformedURLException e) {
 				} catch (FileNotFoundException e) {
 				}
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * @param episodeCount
 	 * @return 0 if successfully deleted, -1 if error occurred
 	 */
 	public int deleteEpisodeFromDrive(int episodeCount) {
 		synchronized(episodeList){
 			if (episodeList.size()>0)
 				if ((episodeCount<episodeList.size())&&
 					(episodeCount>=0)){
 					return deleteEpisodeFromDrive(episodeList.get(episodeCount));
 				}
 		}
 		
 		return -1;
 	}
 	
 	/**
 	 * 
 	 * @param selectedEpisode
 	 * @return
 	 */
 	public int deleteEpisodeFromDrive(Episode selectedEpisode){
 		/* 
 		 * The reason this method is not in episode is because the destination directory is stored this class
 		 */
 		if (selectedEpisode!=null)
 			synchronized(selectedEpisode){
 				File destinationFile=null;
 				try{
 					if (System.getProperty("os.name").equalsIgnoreCase("linux"))
 						destinationFile = new File(getDirectory()+"/"+selectedEpisode.getFilename());
 					else if (System.getProperty("os.name").startsWith("Windows"))
 						destinationFile = new File(getDirectory()+"\\"+selectedEpisode.getFilename());
 				} catch (MalformedURLException e){
 					Podsalinan.debugLog.logError("Invalid URL");
 					Podsalinan.debugLog.printStackTrace(e.getStackTrace());
 				}
 				if (destinationFile!=null){
 					if (destinationFile.exists()){
 						destinationFile.delete();
 						selectedEpisode.setStatus(Episode.NOT_QUEUED);
 						return 0;
 					}
 				}
 				
 			}
 		
 		return -1;
 	}
 	
 	/**
 	 * This Method is used to add the episodes to the array, sorted by date. Newest first.
 	 * @param newEpisode
 	 * @return
 	 */
 	public int addEpisode(Episode newEpisode){
 		if (episodeList!=null)
 			synchronized(episodeList){
 				if (episodeList.size()>0){
 					try {
 						if (debug) Podsalinan.debugLog.logInfo(getClass(),"Date in new episode: "+newEpisode.getDate());
 						Date newEpisodeDate = df.parse(newEpisode.getDate());
 						boolean found=false;
 						int epCount=0;
 						while ((!found)&&(epCount<episodeList.size())){
 							Date currentEpisodeDate = df.parse(episodeList.get(epCount).getDate());
 							if (newEpisodeDate.after(currentEpisodeDate))
 								found=true;
 							else
 								epCount++;
 						}
 						if (found)
 							episodeList.add(epCount, newEpisode);
 						else
 							episodeList.add(newEpisode);
 						return 0;
 					} catch (ParseException e) {
 						e.printStackTrace();
 					}
 					
 				} else {
 					episodeList.add(newEpisode);
 					return 0;
 				}
 			}
 		
 		return -1;
 	}
 
 	public Episode getEpisodeByURL(String string) {
 		for (Episode currentEpisode : episodeList)
 			if (currentEpisode.getURL().toString().equalsIgnoreCase(string))
 				return currentEpisode;
 		
 		return null;
 	}
 	
 	public Vector<Episode> getEpisodesByDate(Date searchDate){
 		Vector<Episode> searchResults = new Vector<Episode>();
 		
 		for (Episode currentEpisode : episodeList){
 			if (currentEpisode.dateEquals(searchDate))
 				searchResults.add(currentEpisode);
 		}
 		
 		return searchResults;
 	}
 
 	public int getEpisodeId(Episode episode) {
 		for (int episodeCount=0; episodeCount< episodeList.size(); episodeCount++)
 			if (episodeList.get(episodeCount).equals(episode))
 				return episodeCount;
 		
 		return -1;
 	}
 	
 	public Vector<Episode> getEpisodesByStatus(int statusValue){
 		Vector<Episode> searchResults = new Vector<Episode>();
 		
 		for (Episode episode : episodeList){
 			if (episode.getStatus()==statusValue)
 				searchResults.add(episode);
 		}
 		
 		return searchResults;
 	}
 	
 	/** scanDirectory will check the current directory for episode downloads that have completed
 	 * 
 	 */
 	public void scanDirectory(DataStorage data){
 		ArrayList<File> filesInDir = new ArrayList<File>();
         //int count=0;
 		String directoryToScan;
 		String directory = fields.get("directory").getValue();
 		try {
 			directoryToScan=directory.substring(0, directory.indexOf("Download"));
 		} catch (StringIndexOutOfBoundsException e){
 			directoryToScan = directory;
 		}
 		File directoryFile = new File (directoryToScan);
 		data.scanDirectory(directoryFile, filesInDir);
 		for (Episode episode : episodeList)
 			if (episode.getStatus()==URLDetails.FINISHED){
 				String filename = episode.getURL().toString().split("/")[episode.getURL().toString().split("/").length-1];
 				boolean found=false;
 				int fileCount=0;
 				File file=null;
 				while ((fileCount<filesInDir.size())&&(!found)){
 					file = filesInDir.get(fileCount);
 					if (file.getName().equalsIgnoreCase(filename))
 						found=true;
 					fileCount++;
 				}
 				if (!found)
 					episode.setStatus(URLDetails.NOT_QUEUED);
 			} else if (episode.getStatus()!=URLDetails.FINISHED){
 				String filename = episode.getURL().toString().split("/")[episode.getURL().toString().split("/").length-1];
 				boolean found=false;
 				int fileCount=0;
 				File file=null;
 				while ((fileCount<filesInDir.size())&&(!found)){
 					file = filesInDir.get(fileCount);
 					if ((file.getName().equalsIgnoreCase(filename))&&
 						(file.length()>=Long.parseLong(episode.getSize()))){
 						episode.setStatus(URLDetails.FINISHED);
 						found=true;
 					} else
 						fileCount++;
 				}
 				// The reason for removing the file from the filesInDir array, is it means 1 less record for the system to check against.
 				if (found)
 					filesInDir.remove(file);
 			}
 	}
 	
 }
