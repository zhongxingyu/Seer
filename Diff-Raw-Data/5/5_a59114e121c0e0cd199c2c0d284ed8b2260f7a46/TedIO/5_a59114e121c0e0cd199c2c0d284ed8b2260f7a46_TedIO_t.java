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
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.nio.channels.FileChannel;
 import java.util.Properties;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.Vector;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.filechooser.FileFilter;
 
 import org.w3c.dom.Element;
 
 import ted.headless.DaemonLog;
 import ted.ui.TimedOptionPane;
 import ted.ui.editshowdialog.FeedPopupItem;
 import ted.ui.messaging.GrowlMessenger;
 import ted.ui.messaging.PopupMessenger;
 import ted.tools.StringEncrypter;
 
 /**
  * TED: Torrent Episode Downloader (2005 - 2006)
  * 
  * This class does all the file reading / writing for ted
  * 
  * @author Roel
  * @author Joost
  * 
  *         ted License: This file is part of ted. ted and all of it's parts are
  *         licensed under GNU General Public License (GPL) version 2.0
  * 
  *         for more details see:
  *         http://en.wikipedia.org/wiki/GNU_General_Public_License
  * 
  */
 public class TedIO
 {
     private String XMLurl = "http://ted.sourceforge.net/shows_clean.xml"; //$NON-NLS-1$
     private String versionUrl = "http://ted.sourceforge.net/version.txt";
 
     private static String CONFIG_FILE = TedSystemInfo.getUserDirectory() + "config.ted"; //$NON-NLS-1$
     public static String SHOWS_FILE = TedSystemInfo.getUserDirectory() + "shows.ted"; //$NON-NLS-1$
     public static String XML_SHOWS_FILE = TedSystemInfo.getUserDirectory() + "shows_clean.xml"; //$NON-NLS-1$
     
     private Lock savingShows = new ReentrantLock();
     
     private static TedIO ioSingleton = null;
     
     private String encryptionKey = "123456789012345678901234567890";
     
 
     /****************************************************
      * CONSTRUCTORS
      ****************************************************/
 
 	private TedIO()
 	{
 	}
 	
 	// Handle multi threading problems. Only allow one singleton to be made.
     private synchronized static void createInstance() 
     {
         if (ioSingleton == null) 
         {
         	ioSingleton = new TedIO();
         }
     }
  
     public static TedIO getInstance() 
     {
         if (ioSingleton == null) 
         {
         	createInstance();
         }
         
         return ioSingleton;
     }
     
     // Prevent cloning.
     public Object clone() throws CloneNotSupportedException 
     {
     	throw new CloneNotSupportedException();
     }
     
     /****************************************************
      * PUBLIC METHODS
      ****************************************************/
     /**
      * Saves the current shows of ted to the harddrive
      * 
      * @param series
      *            Current vector of shows in ted
      */
     public void SaveShows(Vector series)
     {
     	if (!savingShows.tryLock())
     	{
         	// Do this check first to prevent two processes writing in the
         	// file at the same time. As the saves are so close together
         	// it (probably) doesn't matter that the second save is cancelled
         	// by this.
     		return;
     	}
     	
 		try
 		{
 		    // Write to disk with FileOutputStream
 		    FileOutputStream f_out = new FileOutputStream(SHOWS_FILE);
 	
 		    // Write object with ObjectOutputStream
 		    ObjectOutputStream obj_out = new ObjectOutputStream(f_out);
 	
 		    // Write object out to disk
 		    obj_out.writeObject(series);
 	
 		    f_out.close();
 		    obj_out.close();
 		} 
 		catch (Exception e)
 		{
 		    TedLog.error(e, "Shows File writing error"); //$NON-NLS-1$
 		}
 		finally
 		{
 			savingShows.unlock();
 		}
     }
 
     /**
      * Reads the shows from the harddisk
      * 
      * @return Shows saved on the harddisk
      */
     public Vector GetShows()
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
 				File shows_file = new File("shows.ted"); //$NON-NLS-1$
 		
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
 		    ObjectInputStream obj_in = new ObjectInputStream(f_in);
 	
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
      * 
      * @param name
      *            Name of the torrent
      * @param minSize
      *            Minimum size for the contents of the torrent
      * @param maxSize
      *            Maximum size for the contents of the torrent
      * @param location
      *            Folder where ted has to store the torrent
      * @param config
      *            Current TedConfig.getInstance()
      * @throws Exception
      */
 
     /**
      * Saves the config to the harddrive
      * 
      * @param tc
      *            TedConfig.getInstance() we have to save
      */
     public void SaveConfig()
     {
 		try
 		{
 		    File file = new File(CONFIG_FILE);
 		    FileWriter fw = new FileWriter(file);	    
 	
 		    // write all the settings in new lines to config.ted
 		    fw.append("refresh=" + TedConfig.getInstance().getRefreshTime() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 		    fw.append("directory=" + TedConfig.getInstance().getDirectory() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 		    fw.append("opentorrent=" + TedConfig.getInstance().isOpenTorrent() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 		    fw.append("checkversion=" + TedConfig.getInstance().isCheckVersion() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 		    fw.append("showerrors=" + TedConfig.getInstance().isShowErrors() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 		    fw.append("showhurray=" + TedConfig.getInstance().isShowHurray() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 		    fw.append("windowwidth=" + TedConfig.getInstance().getWidth() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 		    fw.append("windowheight=" + TedConfig.getInstance().getHeight() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 		    fw.append("windowx=" + TedConfig.getInstance().getX() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 		    fw.append("windowy=" + TedConfig.getInstance().getY() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 		    fw.append("startminimized=" + TedConfig.getInstance().isStartMinimized() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 		    fw.append("timeoutsecs=" + TedConfig.getInstance().getTimeOutInSecs() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 		    fw.append("seedersetting=" + TedConfig.getInstance().getSeederSetting() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 		    fw.append("rssversion=" + TedConfig.getInstance().getRSSVersion() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 		    fw.append("rssupdate=" + TedConfig.getInstance().getAutoUpdateFeedList() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 		    fw.append("rssadjust=" + TedConfig.getInstance().getAutoAdjustFeeds() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 		    fw.append("locale_language=" + TedConfig.getInstance().getLanguage() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 		    fw.append("locale_country=" + TedConfig.getInstance().getCountry() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 		    fw.append("parse_at_start=" + TedConfig.getInstance().isParseAtStart() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 		    fw.append("add_tray=" + TedConfig.getInstance().isAddSysTray() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 		    fw.append("downloadcompressed=" + TedConfig.getInstance().getDoNotDownloadCompressed() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 		    fw.append("filterextensions=" + TedConfig.getInstance().getFilterExtensions() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 		    if (!TedSystemInfo.isHeadless())
 		    {		
 	        	    TedLogDialog t = TedLogDialog.getInstance();
 	        	    fw.append("loglines=" + t.getLines() + "\n"); //$NON-NLS-1$
 		    }
 		    fw.append("allowlogging=" + TedConfig.getInstance().isAllowLogging() + "\n"); //$NON-NLS-1$
 		    fw.append("logtofile=" + TedConfig.getInstance().isLogToFile() + "\n"); //$NON-NLS-1$
 		    fw.append("oddrowcolor=" + TedConfig.getInstance().getOddRowColor().getRGB() + "\n");
 		    fw.append("evenrowcolor=" + TedConfig.getInstance().getEvenRowColor().getRGB() + "\n");
 		    fw.append("timezoneoffset=" + TedConfig.getInstance().getTimeZoneOffset() + "\n");
 		    fw.append("sorttype=" + TedConfig.getInstance().getSortType() + "\n");
 		    fw.append("sortdirection=" + TedConfig.getInstance().getSortDirection() + "\n");
 		    fw.append("autoschedule=" + TedConfig.getInstance().isUseAutoSchedule() + "\n");
 		    fw.append("hdkeywords=" + TedConfig.getInstance().getHDKeywords() + "\n");
 		    fw.append("hdpreference=" + TedConfig.getInstance().isHDDownloadPreference() + "\n");
 	
 		    fw.append("useProxy=" + TedConfig.getInstance().getUseProxy() + "\n");
 		    fw.append("useProxyAuth=" + TedConfig.getInstance().getUseProxyAuth() + "\n");
 		    fw.append("proxyUsername=" + TedConfig.getInstance().getProxyUsername() + "\n");
 		    fw.append("proxyHost=" + TedConfig.getInstance().getProxyHost() + "\n");
 		    fw.append("proxyPort=" + TedConfig.getInstance().getProxyPort() + "\n");
 		    
 		    String encryptedString = "";
 		    String proxyPassword = TedConfig.getInstance().getProxyPassword(); 
 		    if (proxyPassword != null && !proxyPassword.equals(""))
 		    {
 		    	StringEncrypter encrypter = new StringEncrypter("DESede", encryptionKey);
 		    	encryptedString = encrypter.encrypt(TedConfig.getInstance().getProxyPassword());
 		    }
 	    	fw.append("proxyPassword=" + encryptedString + "\n");
 		    
 		    fw.append("filterPrivateTrackers=" + TedConfig.getInstance().isFilterPrivateTrackers());
 		    fw.close();
 		} 
 		catch (Exception e)
 		{
 		    TedLog.error(e, "Config File writing error"); //$NON-NLS-1$
 		}
     }
 
     /**
      * Reads a TedConfig.getInstance() from the harddrive
      * 
      * @return TedConfig.getInstance() read from the harddrive
      * @throws FileNotFoundException
      */
     public void GetConfig() throws FileNotFoundException
     {
 		try
 		{
 		    // check application folder for all config / shows files	
 		    File userdir_config_file = new File(CONFIG_FILE);
 		    
 		    // if config does not exist, copy from current dir & delete original
 		    if (!userdir_config_file.exists())
 		    {
 				// if config does not exist in current dir either: new ted
 				File config_file = new File("config.ted"); //$NON-NLS-1$
 		
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
 		    while ((line = br.readLine()) != null)
 		    {
 				int seperatorIndex = line.indexOf('=');
 		
 				// Get the name of this config item.
 				String configItem = line.substring(0, seperatorIndex);
 		
 				// Get the value for the item. Just retrieve the rest of the
 				// string (available tokens) so also values with an '=' in
 				// them are correctly used.
 				String configItemValue = line.substring(seperatorIndex + 1);
 				
 				// Nothing set for this config item.
 				if (configItemValue == null || configItemValue.equals(""))
 				{
 					// Go to the next line.
 					continue;
 				}
 		
 				if (configItem.equals("refresh")) //$NON-NLS-1$
 				{
 				    TedConfig.getInstance().setRefreshTime(Integer.parseInt(configItemValue));
 				} 
 				else if (configItem.equals("directory")) //$NON-NLS-1$
 				{
 				    String s = configItemValue;
 				    File f = new File(s);
 		
 				    boolean directoryAvailable = false;
 				    // Create the directory if it doesn't exist.
 				    if (!f.exists())
 				    {
 				    	directoryAvailable = f.mkdir();
 				    } 
 				    else
 				    {
 				    	directoryAvailable = true;
 				    }
 		
 				    if (directoryAvailable)
 				    {
 				    	TedConfig.getInstance().setDirectory(s);
 				    }
 				    else
 				    {
 						if (TedSystemInfo.isHeadless())
 						{
 						    DaemonLog.error(Lang.getString("TedConfig.getInstance()Dialog.DialogSelectDirectory")); //$NON-NLS-1$)
 						}
 						else
 						{
 						    JOptionPane.showMessageDialog(null, Lang.getString("TedConfig.getInstance()Dialog.DialogSelectDirectory")); //$NON-NLS-1$
 						}
 				    }
 				} 
 				else if (configItem.equals("opentorrent")) //$NON-NLS-1$
 				{
 				    TedConfig.getInstance().setOpenTorrent(Boolean.parseBoolean(configItemValue));
 				} 
 				else if (configItem.equals("checkversion")) //$NON-NLS-1$
 				{
 				    TedConfig.getInstance().setCheckVersion(Boolean.parseBoolean(configItemValue));
 				} 
 				else if (configItem.equals("showerrors")) //$NON-NLS-1$
 				{
 				    TedConfig.getInstance().setShowErrors(Boolean.parseBoolean(configItemValue));
 				} 
 				else if (configItem.equals("showhurray")) //$NON-NLS-1$
 				{
 				    TedConfig.getInstance().setShowHurray(Boolean.parseBoolean(configItemValue));
 				} 
 				else if (configItem.equals("windowwidth")) //$NON-NLS-1$
 				{
 				    TedConfig.getInstance().setWidth(Integer.parseInt(configItemValue));
 				} 
 				else if (configItem.equals("windowheight")) //$NON-NLS-1$
 				{
 				    TedConfig.getInstance().setHeight(Integer.parseInt(configItemValue));
 				} 
 				else if (configItem.equals("windowx")) //$NON-NLS-1$
 				{
 				    TedConfig.getInstance().setX(Integer.parseInt(configItemValue));
 				} 
 				else if (configItem.equals("windowy")) //$NON-NLS-1$
 				{
 				    TedConfig.getInstance().setY(Integer.parseInt(configItemValue));
 				} 
 				else if (configItem.equals("startminimized")) //$NON-NLS-1$
 				{
 				    TedConfig.getInstance().setStartMinimized(Boolean.parseBoolean(configItemValue));
 				} 
 				else if (configItem.equals("rssversion")) //$NON-NLS-1$
 				{
 				    TedConfig.getInstance().setRSSVersion(Integer.parseInt(configItemValue));
 				} 
 				else if (configItem.equals("rssupdate")) //$NON-NLS-1$
 				{
 				    TedConfig.getInstance().setAutoUpdateFeedList(Integer.parseInt(configItemValue));
 				} 
 				else if (configItem.equals("rssadjust")) //$NON-NLS-1$
 				{
 				    TedConfig.getInstance().setAutoAdjustFeeds(Integer.parseInt(configItemValue));
 				} 
 				else if (configItem.equals("timeoutsecs")) //$NON-NLS-1$
 				{
 				    TedConfig.getInstance().setTimeOutInSecs(Integer.parseInt(configItemValue));
 				} 
 				else if (configItem.equals("seedersetting")) //$NON-NLS-1$
 				{
 				    TedConfig.getInstance().setSeederSetting(Integer.parseInt(configItemValue));
 				} 
 				else if (configItem.equals("locale_language")) //$NON-NLS-1$
 				{
 				    tempLanguage = configItemValue;
 				} 
 				else if (configItem.equals("locale_country")) //$NON-NLS-1$
 				{
 				    tempCountry = configItemValue;
 				} 
 				else if (configItem.equals("parse_at_start")) //$NON-NLS-1$
 				{
 				    TedConfig.getInstance().setParseAtStart(Boolean.parseBoolean(configItemValue));
 				} 
 				else if (configItem.equals("add_tray")) //$NON-NLS-1$
 				{
 				    TedConfig.getInstance().setAddSysTray(Boolean.parseBoolean(configItemValue));
 				} 
 				else if (configItem.equals("downloadcompressed")) //$NON-NLS-1$
 				{
 				    TedConfig.getInstance().setDoNotDownloadCompressed(Boolean.parseBoolean(configItemValue));
 				} 
 				else if (configItem.equals("filterextensions")) //$NON-NLS-1$
 				{
 				    TedConfig.getInstance().setFilterExtensions(configItemValue);
 				}
 				else if (configItem.equals("loglines") && !TedSystemInfo.isHeadless()) //$NON-NLS-1$
 				{
 				    TedLogDialog t = TedLogDialog.getInstance();
 				    t.setMaxLines(Integer.parseInt(configItemValue));
 				}
 				else if (configItem.equals("allowlogging")) //$NON-NLS-1$
 				{
 				    TedConfig.getInstance().setAllowLogging(Boolean.parseBoolean(configItemValue));
 				}
 				else if (configItem.equals("logtofile")) //$NON-NLS-1$
 				{
 				    TedConfig.getInstance().setLogToFile(Boolean.parseBoolean(configItemValue));
 				}
 				else if (configItem.equals("oddrowcolor") && !TedSystemInfo.isHeadless())
 				{
 				    String s = configItemValue;
 				    Color color = new Color(Integer.parseInt(s));
 				    TedConfig.getInstance().setOddRowColor(color);
 				}
 				else if (configItem.equals("evenrowcolor") && !TedSystemInfo.isHeadless())
 				{
 				    Color color = new Color(Integer.parseInt(configItemValue));
 				    TedConfig.getInstance().setEvenRowColor(color);
 				}
 				else if (configItem.equals("timezoneoffset"))
 				{
 				    TedConfig.getInstance().setTimeZoneOffset(Integer.parseInt(configItemValue));
 				}
 				else if (configItem.equals("autoschedule"))
 				{
 				    TedConfig.getInstance().setUseAutoSchedule(Boolean.parseBoolean(configItemValue));
 				}
 				else if (configItem.equals("sorttype"))
 				{
 				    TedConfig.getInstance().setSortType(Integer.parseInt(configItemValue));
 				}
 				else if (configItem.equals("sortdirection"))
 				{
 				    TedConfig.getInstance().setSortDirection(Integer.parseInt(configItemValue));
 				}
 				else if (configItem.equals("hdkeywords"))
 				{
 				    TedConfig.getInstance().setHDKeywords(configItemValue);
 				}
 				else if (configItem.equals("hdpreference"))
 				{
 				    TedConfig.getInstance().setHDDownloadPreference(Boolean.parseBoolean(configItemValue));
 				}
 				else if (configItem.equals("useProxy"))
 				{
 				    TedConfig.getInstance().setUseProxy(Boolean.parseBoolean(configItemValue));
 				}
 				else if (configItem.equals("useProxyAuth"))
 				{
 				    TedConfig.getInstance().setUseProxyAuth(Boolean.parseBoolean(configItemValue));
 				}
 				else if (configItem.equals("proxyPassword"))
 				{
 					String decryptedString   = "";					
 					String encryptedPassword = configItemValue;
 					
 					if (!encryptedPassword.equals(""))
 					{
 						StringEncrypter encrypter = new StringEncrypter("DESede", encryptionKey);
 						decryptedString = encrypter.decrypt(encryptedPassword);
 					}
 					
 				    TedConfig.getInstance().setProxyPassword(decryptedString);
 				}
 				else if (configItem.equals("proxyUsername"))
 				{
 				    TedConfig.getInstance().setProxyUsername(configItemValue);
 				}
 				else if (configItem.equals("proxyHost"))
 				{
 				    TedConfig.getInstance().setProxyHost(configItemValue);
 				}
 				else if (configItem.equals("proxyPort"))
 				{
 				    TedConfig.getInstance().setProxyPort(configItemValue);
 				}	
 				else if (configItem.equals("filterPrivateTrackers"))
 				{
 					TedConfig.getInstance().setFilterPrivateTrackers(Boolean.parseBoolean(configItemValue));
 				}
 		    }
 	
 		    TedConfig.getInstance().setLocale(tempCountry, tempLanguage);
 		    TedConfig.getInstance().setPrivateTrackers(getPrivateTrackersFromXml());
 	
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
 		    e.printStackTrace();
 		}
     }
 
     /**
      * Reads version.txt from the ted website and returns the current version of
      * ted
      * 
      * @param d
      *            Running version of ted
      * @return Current version of ted
      */
     public double checkNewTed(double d)
     {
 		try
 		{
 		    TedLog.debug(Lang.getString("TedMain.CheckingNewTed")); //$NON-NLS-1$
 		    URL url = new URL(versionUrl); //$NON-NLS-1$
 		    String line;
 		    StringTokenizer tokenizer;
 		    String token;
 	
 		    BufferedReader data = this.makeBufferedReader(url, TedConfig.getInstance().getTimeOutInSecs());
 	
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
 		    TedLog.error(e, Lang.getString("TedMain.CheckingError")); //$NON-NLS-1$
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
 	
 		    BufferedReader data = this.makeBufferedReader(url, TedConfig.getInstance().getTimeOutInSecs());
 	
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
      * Check whether there is a new version of shows.xml Downloads it and
      * updates shows
      * 
      * @param main
      * @param showresult
      *            Whether the user wants to see the result of the check
      */
     public void checkNewXMLFile(TedMainDialog main, boolean showresult, TedTable mainTable)
     {
 		// check the website if there is a new version available
 		int version = TedConfig.getInstance().getRSSVersion();
 		int onlineversion = this.getXMLVersion();
 	
 		// if there is a new version
 		if (onlineversion > version)
 		{
 			// Information for in the info/update panel.
 			String messageUpdate = "<html><body>" 
 								 + Lang.getString("TedIO.DialogNewPredefinedShows1") 
 								 + " " 
 								 + onlineversion 
 								 + Lang.getString("TedIO.DialogNewPredefinedShows2") 
 								 + "</body></html>";
 			
 			String messageInfo = "<html><body>" 
 				               + Lang.getString("TedIO.DialogNewPredefinedShowsInfo") 
 				               + " " + onlineversion + ")."
 				               + "</body></html>";
 			
 			String title = Lang.getString("TedIO.DialogNewPredefinedShowsHeader");
 	
 		    // Always download the new XML file.
 		    if (TedConfig.getInstance().isAutoUpdateFeedList())
 		    {
 		    	// download the XML file
 				downloadXML();
 				
 				// update the shows (if the user wants to).
 				updateShows(main, mainTable);	
 
 				new TedUpdateWindow(title, 
 									messageInfo, 
 									"http://ted.sourceforge.net/newshowsinfo.php", 
 									Lang.getString("TedGeneral.ButtonOk"), 
 									main);
 		    } 
 		    else if (TedConfig.getInstance().askAutoUpdateFeedList() || showresult)
 		    {
 				// Ask user for confirmation if we have to.
 				// The downloadXML function is called from within this window.
 				new TedUpdateWindow(title, 
 									messageUpdate, 
 									"http://ted.sourceforge.net/newshowsinfo.php", 
 									"DownloadXml", 
 									Lang.getString("TedGeneral.ButtonDownload"), 
 									Lang.getString("TedGeneral.ButtonLater"), 
 									main);
 		    }
 		} 
 		else if (showresult)
 		{
 		    JOptionPane.showMessageDialog(null, Lang.getString("TedIO.DialogMostRecentShows1") + " " + version + Lang.getString("TedIO.DialogMostRecentShows2"), //$NON-NLS-1$ //$NON-NLS-2$
 			    Lang.getString("TedIO.DialogMostRecentShowsHeader"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$
 		}
     }
 
     public void updateShows(TedMainDialog main, TedTable mainTable)
     {
 		int rows = mainTable.getRowCount();
 	
 		// check if the user wants us to update the shows
 		int answer = -1;
 		if (rows != 0)
 		{
 		    if (TedConfig.getInstance().askAutoAdjustFeeds())
 		    {
 				String message = Lang.getString("TedIO.DialogUpdateShows1") + "\n" + //$NON-NLS-1$
 					Lang.getString("TedIO.DialogUpdateShows2") + "\n" + //$NON-NLS-1$
 					Lang.getString("TedIO.DialogUpdateShows3");
 				String title = Lang.getString("TedIO.DialogUpdateShowsHeader"); //$NON-NLS-1$
 		
 				answer = TimedOptionPane.showTimedOptionPane(null, message, title, "", 30000, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION, null, Lang.getAlwaysYesNoNeverLocale(), Lang.getAlwaysYesNoNeverLocale()[0]);
 		
 				if (answer == 0)
 				{
 				    // The user clicked the always button, so store it in the
 				    // configuration.
 				    TedConfig.getInstance().setAutoAdjustFeeds(TedConfig.getInstance().ALWAYS);
 				    this.SaveConfig();
 				} 
 				else if (answer == 3)
 				{
 				    // Do the same for the never button.
 				    TedConfig.getInstance().setAutoAdjustFeeds(TedConfig.getInstance().NEVER);
 				    this.SaveConfig();
 				}
 				// For the yes/no option nothing has to be done as when the user
 				// sees this message
 				// dialog the configuration is already correctly set on "ask".
 		    }
 	
 		    if (TedConfig.getInstance().isAutoAdjustFeeds() || answer == JOptionPane.YES_OPTION)
 			{
 				// adjust the feeds
 				this.UpdateShow(main, true, mainTable);
 		    }
 		}
     }
 
     /**
      * Update the feeds with the correct urls User defined feeds will not be
      * adjusted
      * 
      * @param main
      */
     public void UpdateShow(TedMainDialog main, boolean AutoUpdate, TedTable mainTable)
     {
 		String s;
 		String location;
 		int returnVal;
 	
 		if (!AutoUpdate)
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
 	
 		    location = chooser.getSelectedFile().getAbsolutePath();
 	
 		    if (!location.endsWith(".xml")) //$NON-NLS-1$
 		    {
 		    	location += ".xml"; //$NON-NLS-1$
 		    }
 		} 
 		else
 		{
 		    location = TedIO.XML_SHOWS_FILE;
 		    returnVal = JFileChooser.APPROVE_OPTION;
 		}
 
 		if (returnVal == JFileChooser.APPROVE_OPTION)
 		{
 		    int rows = mainTable.getRowCount();
 	
 		    TedXMLParser parser = new TedXMLParser();
 		    Element el = parser.readXMLFromFile(location);
 	
 		    for (int i = 0; i < rows; i++)
 		    {
 				TedSerie serie = mainTable.getSerieAt(i);
 		
 				if (serie != null)
 				{
 				    TedSerie XMLserie = parser.getSerie(el, serie.getName());
 				    serie.AutoFillInPresets(XMLserie);
 		
 				    // add auto-generated search based feeds to the show
 				    // do this after AutoFillInPresets, 'cause that will reset
 				    // the feeds
 				    // of the serie
 				    Vector<FeedPopupItem> items = new Vector<FeedPopupItem>();
 				    items = parser.getAutoFeedLocations(el);
 				    serie.generateFeedLocations(items);
 				}
 		    }
 		    
 		    if (!AutoUpdate)
 		    {
 		    	s = Lang.getString("TedIO.ShowsSynced"); //$NON-NLS-1$
 		    } 
 		    else
 		    {
 		    	s = Lang.getString("TedIO.ShowsSyncedWithNew"); //$NON-NLS-1$
 		    }
 		    TedLog.debug(s);
 	
 		    if (!AutoUpdate)
 		    {
 		    	JOptionPane.showMessageDialog(null, s, null, 1);
 		    }
 		}
     }
 
     /**
      * Translate url from feed into usable torrent download url
      * 
      * @param uri
      *            URL to translate
      * @param title
      *            Torrent title
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
      * 
      * @param main
      */
     public void downloadXML()
     {
 		try
 		{
 		    TedLog.debug("Downloading new show definitions XML"); //$NON-NLS-1$
 		    // open connection to the XML file
 		    URL url = new URL(this.XMLurl);
 		    BufferedReader br = this.makeBufferedReader(url, TedConfig.getInstance().getTimeOutInSecs());
 	
 		    // write the xml file
 		    FileWriter fw = new FileWriter(XML_SHOWS_FILE);
 	
 		    String line;
 		    while ((line = br.readLine()) != null)
 		    {
 		    	fw.write(line + "\n"); //$NON-NLS-1$
 		    }
 		    fw.close();
 		    br.close();
 	
 		    // Get the version of the XML file
 		    TedXMLParser parser = new TedXMLParser();
 		    Element xmlFile = parser.readXMLFromFile(XML_SHOWS_FILE);
 		    int onlineVersion = parser.getVersion(xmlFile);
 	
 		    // Also get the HD keywords (if available).
 		    String hdKeywords = parser.getHDKeywords(xmlFile);
 		    	
 		    // Save the config.
 		    TedConfig.getInstance().setRSSVersion(onlineVersion);
 		    TedConfig.getInstance().setHDKeywords(hdKeywords);
 	
 		    this.SaveConfig();
 		} 
 		catch (MalformedURLException e)
 		{
 		    TedLog.debug("Showdefinitions XML file cannot be found: MalformedURL"); //$NON-NLS-1$
 		} 
 		catch (IOException e)
 		{
 		    TedLog.debug("Showdefinitions XML file cannot be read"); //$NON-NLS-1$
 		}
     }
     
     public Set<String> getPrivateTrackersFromXml()
     {
 	    TedXMLParser parser = new TedXMLParser();
 	    Element xmlFile = parser.readXMLFromFile(XML_SHOWS_FILE);
 
 	    // Get the private trackers.
 	    Set<String> privateTrackers = parser.getPrivateTrackers(xmlFile);
 	    
 	    // And save them in the config.
 	    return privateTrackers;
     }
 
     /**
      * Downloads a torrent to the user selected location and launches
      * torrentclient if user wants to
      * 
      * @param url
      *            URL of torrent to download
      * @param name
      *            FileName to save torrent to (without directory)
      * @param config
      *            TedConfig.getInstance() containing usersettings
      * @throws Exception
      */
     public void downloadTorrent(URL url, String name) throws Exception
     {
 		try
 		{
 		    // remove strange tokens from name string so torrent can be opened
 		    // by client
 		    TedLog.debug(Lang.getString("TedIO.DownloadingTorrent") + url + Lang.getString("TedIO.Name") + name); //$NON-NLS-1$ //$NON-NLS-2$
 		    // remove weird characters and spaces that can cause problems while
 		    // opening the torrent
 		    name = name.replaceAll("[\\[\\]/:&*?|\"\\\\]", "");
 		    name = name.replaceAll(" ()", ".");
 	
 		    // create output torrent file
 		    String loc = TedConfig.getInstance().getDirectory() + File.separator + name + ".torrent"; //$NON-NLS-1$
 		    File outputFile = new File(loc);
 	
 		    // file already exists
 		    int i = 1;
 		    while (outputFile.exists())
 		    {
 				loc = TedConfig.getInstance().getDirectory() + File.separator + name + "-" + i + ".torrent"; //$NON-NLS-1$ //$NON-NLS-2$
 				outputFile = new File(loc);
 				i++;
 		    }
 	
 		    URLConnection urlc2 = null;
		    urlc2 = TedIO.makeUrlConnection(url, TedConfig.getInstance().getTimeOutInSecs());
		    
 		    int length = urlc2.getContentLength();
 		    InputStream in = urlc2.getInputStream();
 	
 		    // incredible ugly hack to retrieve torrents from isohunt
 		    if (length == -1)
 		    {
 		    	length = 250000;
 		    }
 	
 		    BufferedInputStream bis = new BufferedInputStream(in);
 		    FileOutputStream bos = new FileOutputStream(outputFile);
 	
 		    byte[] buff = new byte[length];
 		    int bytesRead;
 	
 		    while (-1 != (bytesRead = bis.read(buff, 0, buff.length)))
 		    {
 		    	bos.write(buff, 0, bytesRead);
 		    }
 	
 		    TedLog.debug("downloaded and saved torrent to " + loc);
 	
 		    in.close();
 		    bis.close();
 		    bos.close();
 	
 		    // open the torrent by default program
 		    if (TedConfig.getInstance().isOpenTorrent() && !TedSystemInfo.isHeadless())
 		    {
 		    	TedLog.debug(Lang.getString("TedIO.OpenningTorrent")); //$NON-NLS-1$
 		    	this.openFile(loc);
 		    }
 		} 
 		catch (Exception e)
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
 		    	String[] open =
 		    	{ "cmd", "/C", loc }; //$NON-NLS-1$ //$NON-NLS-2$
 	
 		    	Runtime.getRuntime().exec(open);
 		    } 
 		    else if (TedSystemInfo.osIsMac())
 		    {
 		    	String[] args = new String[]
 		    	{ "open", loc };
 		    	Runtime.getRuntime().exec(args);
 		    } 
 		    else if (TedSystemInfo.osIsLinux())
 		    {
 		    	Runtime.getRuntime().exec(new String[]
 		    	{ "gnome-open", loc }); //$NON-NLS-1$
 		    }
 		}
 		catch (IOException e)
 		{
 		    // TODO Auto-generated catch block
 		    e.printStackTrace();
 		}
 	/*
 	 * catch (ClassNotFoundException e) {
 	 * 
 	 * // TODO Auto-generated catch block e.printStackTrace(); } catch
 	 * (SecurityException e) { // TODO Auto-generated catch block
 	 * e.printStackTrace(); } catch (NoSuchMethodException e) { // TODO
 	 * Auto-generated catch block e.printStackTrace(); } catch
 	 * (IllegalArgumentException e) { // TODO Auto-generated catch block
 	 * e.printStackTrace(); } catch (IllegalAccessException e) { // TODO
 	 * Auto-generated catch block e.printStackTrace(); } catch
 	 * (InvocationTargetException e) { // TODO Auto-generated catch block
 	 * e.printStackTrace(); }
 	 */
     }
 
     public boolean checkForShowsXML()
     {
 		// Checks if the show xml file is present on the user system.
 		// If not, alert the user.
 		File showXML = new File(XML_SHOWS_FILE);
 	
 		if (!showXML.exists())
 		{
 		    // Alert Mac users
 		    if (TedSystemInfo.osIsMac())
 		    {
 				// what if growl isn't supported?
 				GrowlMessenger gm = new GrowlMessenger();
 				gm.displayError(Lang.getString("TedGeneral.Error"), Lang.getString("TedIO.ShowsFileNotPresent2"));
 		    }
 	
 		    // Alert Windows users
 		    if (TedSystemInfo.osSupportsBalloon())
 		    {
 				// by balloon?
 				PopupMessenger pm = new PopupMessenger(null);
 				pm.displayError(Lang.getString("TedGeneral.Error"), Lang.getString("TedIO.ShowsFileNotPresent2"));
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
 		if (returnVal == JFileChooser.APPROVE_OPTION)
 		{
 		    try
 		    {
 		    	String fileOut = chooser.getSelectedFile().getCanonicalPath();
 	
 				// Files should always have the .properties extension.
 				if (!fileOut.endsWith(".ted"))
 				{
 				    fileOut += ".ted";
 				}
 		
 				FileChannel inChannel = new FileInputStream(TedIO.SHOWS_FILE).getChannel();
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
 				    if (inChannel != null)
 				    	inChannel.close();
 				    
 				    if (outChannel != null)
 				    	outChannel.close();
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
 		if (returnVal == JFileChooser.APPROVE_OPTION)
 		{
 		    try
 		    {
 				String fileIn = chooser.getSelectedFile().getCanonicalPath();
 		
 				FileChannel inChannel = new FileInputStream(fileIn).getChannel();
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
 				    if (inChannel != null)
 				    	inChannel.close();
 				    
 				    if (outChannel != null)
 				    	outChannel.close();
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
 		FileInputStream fis = new FileInputStream(in);
 		FileOutputStream fos = new FileOutputStream(out);
 		byte[] buf = new byte[1024];
 		int i = 0;
 		while ((i = fis.read(buf)) != -1)
 		{
 		    fos.write(buf, 0, i);
 		}
 		fis.close();
 		fos.close();
 	}
 
     public static URLConnection makeUrlConnection(URL url) throws IOException
     {
 	return makeUrlConnection(url, TedConfig.getInstance().getTimeOutInSecs());
     }
     
     public static URLConnection makeUrlConnection(URL url, int timeOutInSecs) throws IOException
     {
 		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 		Properties systemProperties = System.getProperties();
 		if (TedConfig.getInstance().getUseProxy())
 		{
 			System.getProperties().put( "proxySet", "true");
 		    systemProperties.setProperty("http.proxyHost", TedConfig.getInstance().getProxyHost());
 		    systemProperties.setProperty("http.proxyPort", TedConfig.getInstance().getProxyPort());
 	
 		    if (TedConfig.getInstance().getUseProxyAuth())
 		    {    	
 		    	conn.setRequestProperty("Proxy-Authorization", TedConfig.getInstance().getProxyAuthentication());
 		    } 
 		} 
 		else
 		{
 			System.getProperties().put( "proxySet", "false");
 		    systemProperties.setProperty("http.proxyHost", "");
 		    systemProperties.setProperty("http.proxyPort", "");
 		}
 	
 		conn.setConnectTimeout(timeOutInSecs * 1000);
 	
 		return conn;
     }
 
     public static BufferedInputStream makeBufferedInputStream(URL url, int timeOutInSecs) throws IOException
     {
 		BufferedInputStream stream = new BufferedInputStream(TedIO.makeUrlConnection(url, timeOutInSecs).getInputStream());
 	
 		return stream;
     }
 
     public static BufferedReader makeBufferedReader(URL url, int timeOutInSecs) throws IOException
     {
     	return new BufferedReader(new InputStreamReader(TedIO.makeBufferedInputStream(url, timeOutInSecs)));
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
