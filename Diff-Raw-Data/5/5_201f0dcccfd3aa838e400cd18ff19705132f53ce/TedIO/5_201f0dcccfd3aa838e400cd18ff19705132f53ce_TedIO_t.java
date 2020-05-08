 package ted;
 
 /****************************************************
  * IMPORTS
  ****************************************************/
 import java.awt.Color;
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.nio.channels.FileChannel;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.filechooser.FileFilter;
 
 import org.w3c.dom.Element;
 
 import ted.TedTranslateDialog.PropertiesFileFilter;
 import ted.ui.TimedOptionPane;
 import ted.ui.editshowdialog.FeedPopupItem;
 import ted.ui.messaging.GrowlMessenger;
 import ted.ui.messaging.PopupMessenger;
 
 /**
  * TED: Torrent Episode Downloader (2005 - 2006)
  * 
  * This class does all the file reading / writing for ted
  * 
  * @author Roel
  * @author Joost
  *
  * ted License:
  * This file is part of ted. ted and all of it's parts are licensed
  * under GNU General Public License (GPL) version 2.0
  * 
  * for more details see: http://en.wikipedia.org/wiki/GNU_General_Public_License
  *
  */
 public class TedIO 
 {
 	private String XMLurl = "http://ted.sourceforge.net/shows_clean.xml"; //$NON-NLS-1$
 	private String versionUrl = "http://ted.sourceforge.net/version.txt";
 	
 	private static String CONFIG_FILE = TedSystemInfo.getUserDirectory()+"config.ted"; //$NON-NLS-1$
 	public static String SHOWS_FILE = TedSystemInfo.getUserDirectory()+"shows.ted"; //$NON-NLS-1$
 	public static String XML_SHOWS_FILE = TedSystemInfo.getUserDirectory()+"shows_clean.xml"; //$NON-NLS-1$
 	
 	/****************************************************
 	 * CONSTRUCTORS
 	 ****************************************************/
 	
 	/****************************************************
 	 * PUBLIC METHODS
 	 ****************************************************/
 	/**
 	 * Saves the current shows of ted to the harddrive
 	 * @param series Current vector of shows in ted
 	 */
 	public void SaveShows(Vector series)
 	{
 		try
 		{
 			// Write to disk with FileOutputStream
 			FileOutputStream f_out = new FileOutputStream(SHOWS_FILE);
 	
 			// Write object with ObjectOutputStream
 			ObjectOutputStream obj_out = new ObjectOutputStream (f_out);
 	
 			// Write object out to disk
 			obj_out.writeObject ( series );
 			
 			f_out.close();
 			obj_out.close();
 		}
 		catch (Exception e)
 		{
 			TedLog.error(e, "Shows File writing error"); //$NON-NLS-1$
 		}
 	}
 	
 	/**
 	 * Reads the shows from the harddisk
 	 * @return Shows saved on the harddisk
 	 */
 	public Vector GetShows ()
 	{
 		Vector vec = new Vector();
 		try 
 		{
 			// check application folder for all shows file
 			
 			File userdir_shows_file = new File(SHOWS_FILE);
 			// if config does not exist, copy from current dir & delete original
 			if (!userdir_shows_file.exists())
 			{
 				// if config does not exist in current dir either: new ted
 				File shows_file = new File ("shows.ted"); //$NON-NLS-1$
 				
 				if (shows_file.exists())
 				{
 					// copy config_file and delete afterwards
 					try
 					{
 						copyFile(shows_file, userdir_shows_file);
 						if (!shows_file.delete())
 						{
 							TedLog.error("Error deleting shows file from original ted dir"); //$NON-NLS-1$
 						}
 					}
 					catch (Exception e)
 					{
 						TedLog.error(e, "Error copying shows file to user directory"); //$NON-NLS-1$
 					}		
 				}
 			}
 			// Read from disk using FileInputStream
 			FileInputStream f_in;
 			f_in = new FileInputStream(SHOWS_FILE);
 				
 			// Read object using ObjectInputStream
 			ObjectInputStream obj_in = new ObjectInputStream (f_in);
 			
 			// Read an object
 			Object obj = obj_in.readObject();
 	
 			if (obj instanceof Vector)
 			{
 				// Cast object to a Vector
 				vec = (Vector) obj;
 				return vec;
 			}
 			
 			f_in.close();
 			obj_in.close();
 		} 
 		catch (FileNotFoundException e) 
 		{
 			// do nothing, just return the empty vector
 			return vec;
 		}
 		catch (IOException e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 		catch (ClassNotFoundException e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return vec;
 	}
 	
 	/**
 	 * Checks and saves a found torrent to the harddisk
 	 * @param name Name of the torrent
 	 * @param minSize Minimum size for the contents of the torrent
 	 * @param maxSize Maximum size for the contents of the torrent
 	 * @param location Folder where ted has to store the torrent
 	 * @param config Current TedConfig
 	 * @throws Exception
 	 */
 		
 	/**
 	 * Saves the config to the harddrive
 	 * @param tc TedConfig we have to save
 	 */
 	public void SaveConfig()
 	{
 		try
 		{
 			File file = new File(CONFIG_FILE);
 			FileWriter fw = new FileWriter(file);
 			
 			TedLogDialog t = TedLogDialog.getInstance();
 						
 			// write all the settings in new lines to config.ted
 			fw.append("refresh=" + TedConfig.getRefreshTime() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			fw.append("directory=" + TedConfig.getDirectory()  + "\n");	 //$NON-NLS-1$ //$NON-NLS-2$
 			fw.append("opentorrent=" + TedConfig.isOpenTorrent() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			fw.append("checkversion=" + TedConfig.isCheckVersion()  + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			fw.append("showerrors=" + TedConfig.isShowErrors()  + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			fw.append("showhurray=" + TedConfig.isShowHurray()  + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			fw.append("windowwidth=" + TedConfig.getWidth()  + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			fw.append("windowheight=" + TedConfig.getHeight()  + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			fw.append("windowx=" + TedConfig.getX()  + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			fw.append("windowy=" + TedConfig.getY()  + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			fw.append("startminimized=" + TedConfig.isStartMinimized() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			fw.append("timeoutsecs=" + TedConfig.getTimeOutInSecs() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			fw.append("seedersetting=" + TedConfig.getSeederSetting() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			fw.append("rssversion=" + TedConfig.getRSSVersion() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			fw.append("rssupdate=" + TedConfig.getAutoUpdateFeedList() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			fw.append("rssadjust=" + TedConfig.getAutoAdjustFeeds() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			fw.append("locale_language=" + TedConfig.getLanguage() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			fw.append("locale_country=" + TedConfig.getCountry() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			fw.append("parse_at_start=" + TedConfig.isParseAtStart() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			fw.append("add_tray=" + TedConfig.isAddSysTray() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			fw.append("downloadcompressed=" + TedConfig.getDoNotDownloadCompressed() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			fw.append("filterextensions=" + TedConfig.getFilterExtensions() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			fw.append("loglines=" + t.getLines() + "\n"); //$NON-NLS-1$
 			fw.append("allowlogging=" + TedConfig.isAllowLogging() + "\n"); //$NON-NLS-1$
 			fw.append("logtofile=" + TedConfig.isLogToFile() + "\n"); //$NON-NLS-1$
 			fw.append("oddrowcolor=" +  TedConfig.getOddRowColor().getRGB() + "\n");
 			fw.append("evenrowcolor=" + TedConfig.getEvenRowColor().getRGB() + "\n");
 			fw.append("timezoneoffset=" + TedConfig.getTimeZoneOffset() + "\n");
 			fw.append("sorttype=" + TedConfig.getSortType() + "\n");
 			fw.append("sortdirection=" + TedConfig.getSortDirection() + "\n");
 			fw.append("autoschedule=" + TedConfig.isUseAutoSchedule());	
 			
 			fw.close();
 		}
 		catch (Exception e)
 		{
             TedLog.error(e, "Config File writing error"); //$NON-NLS-1$
 		}		
 	}
 	
 	/**
 	 * Reads a TedConfig from the harddrive
 	 * @return TedConfig read from the harddrive
 	 * @throws FileNotFoundException
 	 */
 	public void GetConfig () throws FileNotFoundException
 	{
 		try
 		{
 			// check application folder for all config / shows files
 			
 			File userdir_config_file = new File(CONFIG_FILE);
 			// if config does not exist, copy from current dir & delete original
 			if (!userdir_config_file.exists())
 			{
 				// if config does not exist in current dir either: new ted
 				File config_file = new File ("config.ted"); //$NON-NLS-1$
 				
 				if (!config_file.exists())
 				{
 					// new ted: show config dialog at startup
 					FileNotFoundException e = new FileNotFoundException();
 					throw e; 
 				}
 				else
 				{
 					// copy config_file and delete afterwards
 					try
 					{
 						copyFile(config_file, userdir_config_file);
 						if (!config_file.delete())
 						{
 							TedLog.error("Error deleting config file from original ted dir"); //$NON-NLS-1$
 						}
 					}
 					catch (Exception e)
 					{
 						TedLog.error(e, "Error copying config file to user directory"); //$NON-NLS-1$
 					}
 					
 				}
 			}
 
             TedLog.debug("Open config file " + userdir_config_file.getAbsolutePath()); //$NON-NLS-1$
 			FileReader fr = new FileReader(userdir_config_file);
 			BufferedReader br = new BufferedReader(fr);
 			
 			String tempLanguage = ""; //$NON-NLS-1$
 			String tempCountry = ""; //$NON-NLS-1$
 			
 			String line = null;
 			while((line=br.readLine()) != null)
 			{
 				int seperatorIndex = line.indexOf('=');
 				
 				// Get the name of this config item.
 				String configItem = line.substring(0, seperatorIndex);
 				
 				// Get the value for the item. Just retrieve the rest of the
 				// string (available tokens) so also values with an '=' in 
 				// them are correctly used.
 				String configItemValue = line.substring(seperatorIndex + 1);
 
 				if(configItem.equals("refresh")) //$NON-NLS-1$
 				{
 					TedConfig.setRefreshTime(Integer.parseInt(configItemValue));
 				}
 				else if(configItem.equals("directory")) //$NON-NLS-1$
 				{
 					String s = configItemValue;
 					File f = new File(s);
 					
 					if(f.isDirectory())
 					{
 						TedConfig.setDirectory(s);
 					}
 					else
 					{
 						JOptionPane.showMessageDialog(null, Lang.getString("TedConfigDialog.DialogSelectDirectory")); //$NON-NLS-1$
 					}
 				}
 				else if(configItem.equals("opentorrent")) //$NON-NLS-1$
 				{
 					TedConfig.setOpenTorrent(Boolean.parseBoolean(configItemValue));
 				}
 				else if(configItem.equals("checkversion")) //$NON-NLS-1$
 				{
 					TedConfig.setCheckVersion(Boolean.parseBoolean(configItemValue));
 				}
 				else if(configItem.equals("showerrors")) //$NON-NLS-1$
 				{
 					TedConfig.setShowErrors(Boolean.parseBoolean(configItemValue));
 				}
 				else if(configItem.equals("showhurray")) //$NON-NLS-1$
 				{
 					TedConfig.setShowHurray(Boolean.parseBoolean(configItemValue));
 				}
 				else if(configItem.equals("windowwidth")) //$NON-NLS-1$
 				{
 					TedConfig.setWidth(Integer.parseInt(configItemValue));
 				}
 				else if(configItem.equals("windowheight")) //$NON-NLS-1$
 				{
 					TedConfig.setHeight(Integer.parseInt(configItemValue));
 				}
 				else if(configItem.equals("windowx")) //$NON-NLS-1$
 				{
 					TedConfig.setX(Integer.parseInt(configItemValue));
 				}
 				else if(configItem.equals("windowy")) //$NON-NLS-1$
 				{
 					TedConfig.setY(Integer.parseInt(configItemValue));
 				}
 				else if(configItem.equals("startminimized")) //$NON-NLS-1$
 				{
 					TedConfig.setStartMinimized(Boolean.parseBoolean(configItemValue));
 				}
 				else if(configItem.equals("rssversion")) //$NON-NLS-1$
 				{
 					TedConfig.setRSSVersion(Integer.parseInt(configItemValue));
 				}
 				else if(configItem.equals("rssupdate")) //$NON-NLS-1$
 				{
 					TedConfig.setAutoUpdateFeedList(Integer.parseInt(configItemValue));
 				}
 				else if(configItem.equals("rssadjust")) //$NON-NLS-1$
 				{
 					TedConfig.setAutoAdjustFeeds(Integer.parseInt(configItemValue));
 				}
 				else if(configItem.equals("timeoutsecs")) //$NON-NLS-1$
 				{
 					TedConfig.setTimeOutInSecs(Integer.parseInt(configItemValue));
 				}
 				else if(configItem.equals("seedersetting")) //$NON-NLS-1$
 				{
 					TedConfig.setSeederSetting(Integer.parseInt(configItemValue));
 				}
 				else if(configItem.equals("locale_language")) //$NON-NLS-1$
 				{
 					tempLanguage = configItemValue;
 				}
 				else if(configItem.equals("locale_country")) //$NON-NLS-1$
 				{
 					tempCountry = configItemValue;
 				}
 				else if(configItem.equals("parse_at_start")) //$NON-NLS-1$
 				{
 					TedConfig.setParseAtStart(Boolean.parseBoolean(configItemValue));
 				}
 				else if(configItem.equals("add_tray")) //$NON-NLS-1$
 				{
 					TedConfig.setAddSysTray(Boolean.parseBoolean(configItemValue));
 				}
 				else if(configItem.equals("downloadcompressed")) //$NON-NLS-1$
 				{
 					TedConfig.setDoNotDownloadCompressed(Boolean.parseBoolean(configItemValue));
 				}
 				else if(configItem.equals("filterextensions")) //$NON-NLS-1$
 				{
 					TedConfig.setFilterExtensions(configItemValue);
 				}	
 				else if(configItem.equals("loglines")) //$NON-NLS-1$
 				{
 					TedLogDialog t = TedLogDialog.getInstance();
 					t.setMaxLines(Integer.parseInt(configItemValue));
 				}
 				else if(configItem.equals("allowlogging")) //$NON-NLS-1$
 				{
 					TedConfig.setAllowLogging(Boolean.parseBoolean(configItemValue));
 				}
 				else if(configItem.equals("logtofile")) //$NON-NLS-1$
 				{
 					TedConfig.setLogToFile(Boolean.parseBoolean(configItemValue));
 				}
 				else if(configItem.equals("oddrowcolor"))
 				{
 					String s = configItemValue;
 					Color color = new Color(Integer.parseInt(s));
 					TedConfig.setOddRowColor(color);
 				}
 				else if(configItem.equals("evenrowcolor"))
 				{
 					Color color = new Color(Integer.parseInt(configItemValue));
 					TedConfig.setEvenRowColor(color);
 				}
 				else if(configItem.equals("timezoneoffset"))
 				{
 					TedConfig.setTimeZoneOffset(Integer.parseInt(configItemValue));
 				}
 				else if(configItem.equals("autoschedule"))
 				{
 					TedConfig.setUseAutoSchedule(Boolean.parseBoolean(configItemValue));
 				}
 				else if(configItem.equals("sorttype"))
 				{
 					TedConfig.setSortType(Integer.parseInt(configItemValue));
 				}
 				else if(configItem.equals("sortdirection"))
 				{
 					TedConfig.setSortDirection(Integer.parseInt(configItemValue));
 				}
 			}
 			
 			TedConfig.setLocale(tempCountry, tempLanguage);
 		
 			br.close();
 			fr.close();
 			
 		}
 		catch (FileNotFoundException e)
 		{
 			// Config file not available
 			TedLog.error(e, "Configfile config.ted not available"); //$NON-NLS-1$
 			throw e;
 		}
 		catch (Exception e)
 		{
             TedLog.error(e, "Error reading configfile config.ted"); //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * Reads version.txt from the ted website and returns the current version of ted
 	 * @param d Running version of ted
 	 * @return Current version of ted
 	 */
 	public double checkNewTed(double d) 
 	{
 		try
 		{
 			TedLog.debug("Checking for new version of ted..."); //$NON-NLS-1$
 			URL url = new URL(versionUrl); //$NON-NLS-1$
 		    String line;
 		    StringTokenizer tokenizer;
 		    String token;
 
 		    BufferedReader data = this.makeBufferedReader(url, TedConfig.getTimeOutInSecs());
 		      	
 		    while ((line = data.readLine()) != null) 
 			{
 		    	tokenizer = new StringTokenizer(line, "="); //$NON-NLS-1$
 		    	token = tokenizer.nextToken();
 		    	if (token.equals("version")) //$NON-NLS-1$
 				{
 		    		return Double.parseDouble(tokenizer.nextToken());
 				}
 			}
 		    
 		    data.close();
 		}
 		catch (Exception e)
 		{
 			TedLog.error(e, "Error checking the version of ted"); //$NON-NLS-1$
 		}
 		return d;
 	}
 	
 	/**
 	 * @return The current XML version as published on ted's website
 	 */
 	private int getXMLVersion()
 	{
 		try
 		{
 			TedLog.debug(Lang.getString("TedIO.Checking")); //$NON-NLS-1$
 			URL url = new URL(versionUrl); //$NON-NLS-1$
 		    String line;
 		    StringTokenizer tokenizer;
 		    String token;
 
 		    BufferedReader data = this.makeBufferedReader(url, TedConfig.getTimeOutInSecs());
 		      	
 		    while ((line = data.readLine()) != null) 
 			{
 		    	tokenizer = new StringTokenizer(line, "="); //$NON-NLS-1$
 		    	token = tokenizer.nextToken();
 		    	if (token.equals("show_xml_version")) //$NON-NLS-1$
 				{
 		    		return Integer.parseInt(tokenizer.nextToken());
 				}
 			}
 		    
 		    data.close();
 		}
 		catch (Exception e)
 		{
 			TedLog.error(e, Lang.getString("TedIO.ErrorChecking")); //$NON-NLS-1$
 		}
 		return -1;
 	}
 
 	/**
 	 * Check whether there is a new version of shows.xml
 	 * Downloads it and updates shows
 	 * @param main
 	 * @param showresult Whether the user wants to see the result of the check
 	 */
 	public void checkNewXMLFile(TedMainDialog main, boolean showresult, TedTable mainTable)
 	{		
 		// check the website if there is a new version available
 		int version = TedConfig.getRSSVersion();
 		int onlineversion = this.getXMLVersion();
 		
 		// if there is a new version
 		if(onlineversion > version)
 		{				
 			// Always download the new XML file.
 			if (TedConfig.isAutoUpdateFeedList())
 			{
 				// download the XML file
 				downloadXML();
 				
 				// update the shows (if the user wants to).
 				updateShows(main, mainTable);				
 			}
 			else if (TedConfig.askAutoUpdateFeedList() || showresult)
 			{
 				// Ask user for confirmation if we have to.
 				// The downloadXML function is called from within this window.
 				String message = "<html><body>"+Lang.getString("TedIO.DialogNewPredefinedShows1")+ " " + onlineversion + Lang.getString("TedIO.DialogNewPredefinedShows2")+"</body></html>"; //$NON-NLS-1$ //$NON-NLS-2$;
 				String title =  Lang.getString("TedIO.DialogNewPredefinedShowsHeader");
 					
 				new TedUpdateWindow(title,
 									message,
 									"http://www.ted.nu/wiki/index.php/Show_list_changes",
 									"DownloadXml",
 									main);
 			}
 		}
 		else if(showresult)
 		{
 			JOptionPane.showMessageDialog(null,
 	                Lang.getString("TedIO.DialogMostRecentShows1")+ " " + version + Lang.getString("TedIO.DialogMostRecentShows2"), //$NON-NLS-1$ //$NON-NLS-2$
 	                Lang.getString("TedIO.DialogMostRecentShowsHeader"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$
 		}
 
 	}
 	
 	public void updateShows(TedMainDialog main, TedTable mainTable)
 	{
 		int rows = mainTable.getRowCount();
 		
 		// check if the user wants us to update the shows
 		int answer = -1;
 		if(rows != 0)
 		{
 			if(TedConfig.askAutoAdjustFeeds())
 			{
 				String message = Lang.getString("TedIO.DialogUpdateShows1")+ "\n" +//$NON-NLS-1$
                 				 Lang.getString("TedIO.DialogUpdateShows2") + "\n" + //$NON-NLS-1$
                 				 Lang.getString("TedIO.DialogUpdateShows3");
 				String title =	Lang.getString("TedIO.DialogUpdateShowsHeader"); //$NON-NLS-1$
 
 				answer = TimedOptionPane.showTimedOptionPane(null, message, title, "", 30000, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION, null, Lang.getAlwaysYesNoNeverLocale(), Lang.getAlwaysYesNoNeverLocale()[0]);
 				
 				if (answer == 0)
 				{
 					// The user clicked the always button, so store it in the configuration.
 					TedConfig.setAutoAdjustFeeds(TedConfig.ALWAYS);
 					this.SaveConfig();
 				}
 				else if (answer == 3)
 				{
 					// Do the same for the never button.
 					TedConfig.setAutoAdjustFeeds(TedConfig.NEVER);
 					this.SaveConfig();
 				}
 				// For the yes/no option nothing has to be done as when the user sees this message
 				// dialog the configuration is already correctly set on "ask".
 			}
 			
 			if(TedConfig.isAutoAdjustFeeds() || answer == JOptionPane.YES_OPTION)
 			{
 				// adjust the feeds
 				this.UpdateShow(main, true, mainTable);
 			}	
 		}
 	}
 	
 	/**
 	 * Update the feeds with the correct urls
 	 * User defined feeds will not be adjusted
 	 * @param main
 	 */
 	public void UpdateShow(TedMainDialog main, boolean AutoUpdate, TedTable mainTable)
 	{
 		String s;
 		String location;
 		int returnVal;
 		
 		if(!AutoUpdate)
 		{
 			location = XML_SHOWS_FILE;
 			File standardFile = new File(location);
 		    JFileChooser chooser = new JFileChooser();
 		    TedExampleFileFilter filter = new TedExampleFileFilter();
 		    filter.addExtension("xml"); //$NON-NLS-1$
 		    filter.setDescription("XML-file"); //$NON-NLS-1$
 		    chooser.setFileFilter(filter);
 		    chooser.setSelectedFile(standardFile);
 		    chooser.setCurrentDirectory(standardFile);
 		    chooser.setDialogTitle(Lang.getString("TedIO.ChooseSync")); //$NON-NLS-1$
 		    
 		    returnVal = chooser.showOpenDialog(chooser);
 		    
 		    location = chooser.getSelectedFile().getName();
 		    
 	    	if(!location.endsWith(".xml")) //$NON-NLS-1$
 	    		location += ".xml"; //$NON-NLS-1$
 		}
 		else
 		{
 			location = TedIO.XML_SHOWS_FILE;
 			returnVal = JFileChooser.APPROVE_OPTION;
 		}
 		
 	    if(returnVal == JFileChooser.APPROVE_OPTION) 
 	    {
 	    	int rows = mainTable.getRowCount();
 	
 			TedXMLParser parser = new TedXMLParser();
 			Element el = parser.readXMLFromFile(location);
 			
 			
 			for(int i=0; i<rows; i++)
 			{		
 				TedSerie serie = mainTable.getSerieAt(i);
 						
 				if(serie!=null)
 				{
 					TedSerie XMLserie = parser.getSerie(el, serie.getName());
 					serie.AutoFillInPresets(XMLserie);
 					
 					// add auto-generated search based feeds to the show
 					// do this after AutoFillInPresets, 'cause that will reset the feeds
 					// of the serie
 					Vector<FeedPopupItem> items = new Vector<FeedPopupItem>();
 					items = parser.getAutoFeedLocations(el);
 					serie.generateFeedLocations(items);
 				}
 			}
 			if(!AutoUpdate)
 			{
 				s = Lang.getString("TedIO.ShowsSynced"); //$NON-NLS-1$
 			}
 			else
 			{
 				s = Lang.getString("TedIO.ShowsSyncedWithNew"); //$NON-NLS-1$
 			}
 			TedLog.debug(s);
 			
 			if(!AutoUpdate)
 			{
 				JOptionPane.showMessageDialog(null, s, null, 1);
 			}
 	    }
 	}
 	
 
 	/**
 	 * Translate url from feed into usable torrent download url
 	 * @param uri URL to translate
 	 * @param title Torrent title
 	 * @return
 	 */
 	public String translateUrl(String uri, String title, int timeOut) 
 	{
 		try
 		{
 			TedLog.debug(Lang.getString("TedIO.TranslatingUrl") + uri); //$NON-NLS-1$
 			URL url = new URL("http://ted.sourceforge.net/urltranslator.php?url=" + uri + "&sTitle=" + URLEncoder.encode(title, "UTF-8")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		    String line;
 		      
 		    BufferedReader data = this.makeBufferedReader(url, timeOut);
 		      	
 		    while ((line = data.readLine()) != null) 
 			{		    	
 	    		String torrentUrl = line;
 	    		if (torrentUrl.equals("null")) //$NON-NLS-1$
 	    		{
 	    			TedLog.debug(Lang.getString("TedIO.UnableTranslate")); //$NON-NLS-1$
 	    			return null;
 	    		}
 	    		else
 	    		{
 	    			TedLog.debug(Lang.getString("TedIO.UrlTranslated") + torrentUrl); //$NON-NLS-1$
 	    			return torrentUrl;
 		    	}
 			}
 			
 		    data.close();
 		}
 		catch (Exception e)
 		{
             TedLog.error(e, Lang.getString("TedIO.TranslateError")); //$NON-NLS-1$
 			return null;
 		}
 		return null;
 	}
 	
 	/**
 	 * Download the XML file from internet with the show definitions
 	 * @param main
 	 */
 	public void downloadXML()
 	{		
 		try 
 		{
 			TedLog.debug("Downloading new show definitions XML"); //$NON-NLS-1$
 			// open connection to the XML file
 			URL url = new URL(this.XMLurl);
 			BufferedReader br = this.makeBufferedReader(url, TedConfig.getTimeOutInSecs());
 			
 			// write the xml file
 			FileWriter fw  = new FileWriter(XML_SHOWS_FILE);
 			
 			String line;	
 			while((line = br.readLine()) != null)
 			{
 				fw.write(line + "\n"); //$NON-NLS-1$
 			}
 			fw.close();
 			br.close();
 			
 			// get the version of the XML file
 			TedXMLParser parser = new TedXMLParser();
 			Element el = parser.readXMLFromFile(XML_SHOWS_FILE);
 			int onlineVersion = parser.getVersion(el);
 			
 			// set the version and save the config
 			TedConfig.setRSSVersion(onlineVersion);
 			this.SaveConfig();
 		}
 		catch (MalformedURLException e) 
 		{
 			TedLog.debug("Showdefinitions XML file cannot be found: MalformedURL"); //$NON-NLS-1$
 		} catch (IOException e) 
 		{
 			TedLog.debug("Showdefinitions XML file cannot be read"); //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * Downloads a torrent to the user selected location and launches
 	 * torrentclient if user wants to
 	 * @param url URL of torrent to download
 	 * @param name FileName to save torrent to (without directory)
 	 * @param config TedConfig containing usersettings
 	 * @throws Exception
 	 */
 	public void downloadTorrent(URL url, String name) throws Exception
 	{
 		try
 		{
 			// remove strange tokens from name string so torrent can be opened by client
     		TedLog.debug(Lang.getString("TedIO.DownloadingTorrent") + url + Lang.getString("TedIO.Name") + name); //$NON-NLS-1$ //$NON-NLS-2$
     		// remove weird characters and spaces that can cause problems while
 			// opening the torrent
 			name = name.replaceAll("[/:&*?|\"\\\\]", "");
 			name = name.replaceAll(" ()", ".");
          
             //create output torrent file
             String loc = TedConfig.getDirectory() + File.separator  + name + ".torrent"; //$NON-NLS-1$
 			File outputFile = new File(loc); 
 		
     		//file already exists
     		int i = 1;
     		while(outputFile.exists())
     		{
     			loc  = TedConfig.getDirectory() + File.separator + name + "-" + i + ".torrent"; //$NON-NLS-1$ //$NON-NLS-2$
     			outputFile = new File(loc);
     			i++;
     		}
     		    		
     		URLConnection urlc2 = null;
 
     		urlc2 = url.openConnection();
     		urlc2.setConnectTimeout(1000*TedConfig.getTimeOutInSecs());
 			int length = urlc2.getContentLength();
 			InputStream in = urlc2.getInputStream();
 			
 			// incredible ugly hack to retrieve torrents from isohunt
 			if(length==-1)
 			{
 				length = 250000;
 			}
 			
             BufferedInputStream bis = new BufferedInputStream(in);
             FileOutputStream    bos = new FileOutputStream(outputFile);   
 			
             byte[] buff = new byte[length];
             int bytesRead;
 
             while(-1 != (bytesRead = bis.read(buff, 0, buff.length))) 
             {
             	bos.write(buff, 0, bytesRead);
             }
             
             in.close();
             bis.close();
             bos.close();            	
             
            // open the torrent by default program
             if (TedConfig.isOpenTorrent())
 			{
         		TedLog.debug(Lang.getString("TedIO.OpenningTorrent")); //$NON-NLS-1$
         		this.openFile(loc);
     		}
 		}
 		catch(Exception e)
 		{
 			throw e;
 		}
 		
 	}
 
 	public void openFile(String loc)
 	{
 		try
 		{
 			if (TedSystemInfo.osIsWindows())
 	    	{
 	        	String [] open = { "cmd" , "/C", loc};   //$NON-NLS-1$ //$NON-NLS-2$
 	        
 	        	Runtime.getRuntime().exec(open);			
 			}
 			else if (TedSystemInfo.osIsMac()) 
 			{
 				String[] args = new String[] { "open", loc};
 				Runtime.getRuntime().exec(args);
 			}
 			else if (TedSystemInfo.osIsLinux())
 			{
 				Runtime.getRuntime().exec( new String [] {"gnome-open", loc} ); //$NON-NLS-1$
 			}
 		}
 		catch (IOException e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 		/*catch (ClassNotFoundException e) 
 		{
 			
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SecurityException e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvocationTargetException e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}*/
 	}
 	
 	public boolean checkForShowsXML()
 	{
 		// Checks if the show xml file is present on the user system.
 		// If not, alert the user.
 		File showXML =new File(XML_SHOWS_FILE);
 	    
 	    if(!showXML.exists())
 	    {
 	    	// Alert Mac users
 	    	if(TedSystemInfo.osIsMac())
 	    	{
 	    		// what if growl isn't supported?
 	    	    GrowlMessenger gm = new GrowlMessenger();
 	    		gm.displayError(Lang.getString("TedGeneral.Error"),
	    						Lang.getString("TedIO.ShowsFileNotPresent2"));
 	        }
 	        
 	    	// Alert Windows users
 	    	if(TedSystemInfo.osSupportsBalloon())
 	    	{
 	    		// by balloon?
 	    	    PopupMessenger pm = new PopupMessenger(null);
 	    		pm.displayError(Lang.getString("TedGeneral.Error"),
								Lang.getString("TedIO.ShowsFileNotPresent2"));
 	        }
 	    	
 	    	return false;
 	    }
 	    else
 	    {
 	    	return true;
 	    }
 	}
 	
 
 	public void ExportShows(TedMainDialog main)
 	{
 		JFileChooser chooser = new JFileChooser();
 		TedFileFilter filter = new TedFileFilter();
 	    chooser.setFileFilter(filter);
 				
 		int returnVal = chooser.showSaveDialog(main);
 		if(returnVal == JFileChooser.APPROVE_OPTION)
 		{				
 			try 
 			{
 				String fileOut = chooser.getSelectedFile().getCanonicalPath();
 				
 				// Files should always have the .properties extension.
 				if(!fileOut.endsWith(".ted"))
 				{
 					fileOut += ".ted";
 				}
 				
 				FileChannel inChannel  = new FileInputStream(TedIO.SHOWS_FILE).getChannel();
 		        FileChannel outChannel = new FileOutputStream(fileOut).getChannel();
 		        
 		        try 
 		        {
 		            inChannel.transferTo(0, inChannel.size(), outChannel);
 		        } 
 		        catch (IOException e) 
 		        {
 		            throw e;
 		        }
 		        finally 
 		        {
 		            if (inChannel != null) inChannel.close();
 		            if (outChannel != null) outChannel.close();
 		        }
 		    } 
 			catch (IOException e) 
 			{
 				TedLog.error(e.toString());
 		    }
 		}
 	}
 	
 	public void ImportShows(TedMainDialog main)
 	{
 		JFileChooser chooser = new JFileChooser();
 		TedFileFilter filter = new TedFileFilter();
 	    chooser.setFileFilter(filter);
 				
 		int returnVal = chooser.showOpenDialog(main);
 		if(returnVal == JFileChooser.APPROVE_OPTION)
 		{				
 			try 
 			{
 				String fileIn = chooser.getSelectedFile().getCanonicalPath();
 								
 				FileChannel inChannel  = new FileInputStream(fileIn).getChannel();
 		        FileChannel outChannel = new FileOutputStream(TedIO.SHOWS_FILE).getChannel();
 		        
 		        try 
 		        {
 		            inChannel.transferTo(0, inChannel.size(), outChannel);
 		        } 
 		        catch (IOException e) 
 		        {
 		            throw e;
 		        }
 		        finally 
 		        {
 		            if (inChannel != null) inChannel.close();
 		            if (outChannel != null) outChannel.close();
 		        }
 		    } 
 			catch (IOException e) 
 			{
 				TedLog.error(e.toString());
 		    }
 		}
 	}
 	
 	/****************************************************
 	 * PRIVATE METHODS
 	 ****************************************************/
 	
 	private static void copyFile(File in, File out) throws Exception 
 	{
 	    FileInputStream fis  = new FileInputStream(in);
 	    FileOutputStream fos = new FileOutputStream(out);
 	    byte[] buf = new byte[1024];
 	    int i = 0;
 	    while((i=fis.read(buf))!=-1) 
 	    {
 	    	fos.write(buf, 0, i);
 	    }
 	    fis.close();
 	    fos.close();
 	}
 	
 	private BufferedReader makeBufferedReader(URL url, int timeOutInSecs) throws IOException
 	{
 		URLConnection conn = url.openConnection();
 	    
 	    conn.setConnectTimeout(1000 * timeOutInSecs);
 	      
 	    BufferedReader br = new BufferedReader(new InputStreamReader(
 	    		  		conn.getInputStream()));
 	    return br;
 	}
 	
 	class TedFileFilter extends FileFilter
 	{
 		public boolean accept(File f) 
 		{
 			return f.toString().toLowerCase().endsWith(".ted");
 		}
 
 		public String getDescription() 
 		{
 			return "show definitions";
 		}		
 	}
 }
