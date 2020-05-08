 package ted;
 
 import java.awt.Color;
 import java.util.Locale;
 
 import javax.swing.JFileChooser;
 
 
 /**
  * TED: Torrent Episode Downloader (2005 - 2006)
  * 
  * TedConfig stores all the configuration variables of ted
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
 public class TedConfig
 
 {
 	/****************************************************
 	 * GLOBAL VARIABLES
 	 ****************************************************/
 	private static final long serialVersionUID = 2199026019828977965L;
 	public static final int NEVER = 0;
 	public static final int ASK = 1;
 	public static final int ALWAYS = 2;
 	public static final int DOWNLOADMINIMUMSEEDERS = 0;
 	public static final int DOWNLOADMOSTSEEDERS = 1;
 	public static final int SORT_OFF = 0;
 	public static final int SORT_NAME = 1;
 	public static final int SORT_STATUS = 2;
 	public static final int SORT_ASCENDING = 0;
 	public static final int SORT_DESCENDING = 1;
 	
 	// create some default settings
 	private static int RefreshTime = 3600;
 	private static String Directory = "";
 	private static boolean ShowErrors = false;
 	private static boolean ShowHurray = true;
 	private static boolean OpenTorrent = true;
 	private static boolean StartMinimized = false;
 	private static boolean CheckVersion = true;
 	private static boolean downloadNewSeason = true;
 	private static boolean parseAtStart = true;
 	private static int autoUpdateFeedList = ALWAYS;
 	private static int autoAdjustFeeds = ALWAYS;
 	private static int width = 400;
 	private static int height = 550;
 	private static int x = 0;
 	private static int y = 0;
 	private static int RSSVersion = 0;
 	private static int TimeOutInSecs = 10;
 	private static int SeederSetting = 0;
 	private static Locale tedLocale = Locale.getDefault();
 	private static boolean addSysTray = TedSystemInfo.osSupportsTray();
 	private static boolean getCompressed = true;
 	private static String filterExtensions = "zip, rar, r01";
 	private static int timesParsedSinceLastCheck = 0; 
 	private static boolean allowLogging = true;
 	private static boolean logToFile = true;
 	private static int timeZoneOffset = -1;
 	private static boolean useAutoSchedule = true;
 	private static int sortType = SORT_STATUS;
 	private static int sortDirection = SORT_ASCENDING;
 	private static String hdKeywords = "720p & HD";
 	private static boolean hdDownloadPreference = false;
 	
 	
 	private static final Color defaultEvenRowColor = Color.WHITE;
 	private static final Color defaultOddRowColor  = new Color(236,243,254);
 	
 	private static Color evenRowColor     	= Color.WHITE;
 	private static Color oddRowColor      	= new Color(236,243,254);
 	private static Color selectedRowColor 	= new Color(61, 128, 223);  
 	private static Color gridColor 			= new Color(205,205,205);
 
 
 	/****************************************************
 	 * CONSTRUCTORS
 	 ****************************************************/
 	/**
 	 * Creates a TedConfig with some default values
 	 */
 	public TedConfig()
 	{
 	}
 		
 	/****************************************************
 	 * GETTERS & SETTERS
 	 ****************************************************/
 
 	/**
 	 * @return Returns the directory where ted has to save the torrents
 	 */
 	public static String getDirectory() 
 	{
 		if (Directory.equals(""))
 		{
 			// init object
 			Directory = new JFileChooser().getFileSystemView().getDefaultDirectory().getAbsolutePath();
 			String seperator = System.getProperty("file.separator");
 			Directory += seperator + "ted";
 		}
 		return Directory;
 	}
 	/**
 	 * Sets the directory where ted has to save the torrents he downloads
 	 * @param directory
 	 */
 	public static void setDirectory(String directory) 
 	{
 		Directory = directory;
 	}
 	/**
 	 * @return Returns the time (in seconds) between the parser intervals
 	 */
 	public static int getRefreshTime() 
 	{
 		// convert to minutes
 		return RefreshTime;
 	}
 	/**
 	 * Set the time (in seconds) between two parser rounds
 	 * @param refreshTime
 	 */
 	public static void setRefreshTime(int refreshTime) 
 	{
 		// in minutes
 		RefreshTime = refreshTime;
 	}
 
 	/**
 	 * @return Returns if the user wants to see the errors
 	 */
 	public static boolean isShowErrors() 
 	{
 		return ShowErrors;
 	}
 
 	/**
 	 * Set if the user wants to see messages when errors occur
 	 * @param showErrors
 	 */
 	public static void setShowErrors(boolean showErrors) 
 	{
 		ShowErrors = showErrors;
 	}
 
 	/**
 	 * @return Returns if the user wants to see hurray messages
 	 */
 	public static boolean isShowHurray() 
 	{
 		return ShowHurray;
 	}
 
 	/**
 	 * Set if the user wants to see hurray messages
 	 * @param showHurray
 	 */
 	public static void setShowHurray(boolean showHurray) 
 	{
 		ShowHurray = showHurray;
 	}
 
 	/**
 	 * @return Returns if the user wants ted to open downloaded torrents in a default torrent client
 	 */
 	public static boolean isOpenTorrent() 
 	{
 		return OpenTorrent;
 	}
 
 	/**
 	 * Set if the user wants ted to open a downloaded torrent
 	 * @param openTorrent
 	 */
 	public static void setOpenTorrent(boolean openTorrent) 
 	{
 		OpenTorrent = openTorrent;
 	}
 
 	/**
 	 * @return Returns if the user wants ted to check his version at startup
 	 */
 	public static boolean isCheckVersion() 
 	{
 		return CheckVersion;
 	}
 
 	/**
 	 * Set if the user wants ted to check his version at startup
 	 * @param checkVersion
 	 */
 	public static void setCheckVersion(boolean checkVersion) 
 	{
 		CheckVersion = checkVersion;
 	}
 
 	/**
 	 * @return Returns the user wants ted to start minimized
 	 */
 	public static boolean isStartMinimized() 
 	{
 		return StartMinimized;
 	}
 
 	/**
 	 * Set if the user wants ted to start minimized
 	 * @param startMinimized
 	 */
 	public static void setStartMinimized(boolean startMinimized) 
 	{
 		StartMinimized = startMinimized;
 	}
 
 	/**
 	 * @return Returns the stored height of the mainwindow.
 	 */
 	public static int getHeight() 
 	{
 		return height;
 	}
 
 	/**
 	 * @param h The height of the mainwindow to set.
 	 */
 	public static void setHeight(int h) 
 	{
 		height = h;
 	}
 
 	/**
 	 * @return Returns the width of the mainwindow.
 	 */
 	public static int getWidth()
 	{
 		return width;
 	}
 
 	/**
 	 * @param w The width of the mainwindow to set.
 	 */
 	public static void setWidth(int w) 
 	{
 		width = w;
 	}
 
 	/**
 	 * @return Returns the x of the mainwindow.
 	 */
 	public static int getX() 
 	{
 		return x;
 	}
 
 	/**
 	 * @param x_pos The x of the mainwindow to set.
 	 */
 	public static void setX(int x_pos) 
 	{
 		x = x_pos;
 	}
 
 	/**
 	 * @return Returns the y of the mainwindow.
 	 */
 	public static int getY() 
 	{
 		return y;
 	}
 
 	/**
 	 * @param y_pos The y of the mainwindow to set.
 	 */
 	public static void setY(int y_pos) 
 	{
 		y = y_pos;
 	}
 
 	/**
 	 * @return Returns if the user wants to download a new season when ted encounters one
 	 */
 	public static boolean isDownloadNewSeason() 
 	{
 		return downloadNewSeason;
 	}
 
 	/**
 	 * Set if the user wants to download a new season when ted encounters one
 	 * @param download
 	 */
 	public static void setDownloadNewSeason(boolean download) 
 	{
 		downloadNewSeason = download;
 	}
 	
 	/**
 	 * @return Returns the number of the latest downloaded RSS feeds.
 	 */
 	public static int getRSSVersion() 
 	{
 		return RSSVersion;
 	}
 
 	/**
 	 * @param version The RSSVersion of the latest downloaded RSS feeds.
 	 */
 	public static void setRSSVersion(int version) 
 	{
 		RSSVersion = version;
 	}
 
 	/**
 	 * @return If the feeds should be auto-adjusted
 	 */
 	public static boolean isAutoAdjustFeeds() 
 	{
 		return (autoAdjustFeeds == ALWAYS);
 	}
 	/**
 	 * @return If the user wants to be asked before autoadjustement of the feeds
 	 */
 	public static boolean askAutoAdjustFeeds() 
 	{
 		return (autoAdjustFeeds == ASK);
 	}
 
 	/**
 	 * Set the auto-adjustment of feeds
 	 * @param adjust
 	 */
 	public static void setAutoAdjustFeeds(int adjust) 
 	{
 		autoAdjustFeeds = adjust;
 	}
 
 	/**
 	 * @return If the feed list should be auto-updated
 	 */
 	public static boolean isAutoUpdateFeedList() 
 	{
 		return (autoUpdateFeedList == ALWAYS);
 	}
 	
 	/**
 	 * @return If the user wants to be asked before the feedslist is updated
 	 */
 	public static boolean askAutoUpdateFeedList() 
 	{
 		return (autoUpdateFeedList == ASK);
 	}
 
 	/**
 	 * Set the auto-update of the feedlist
 	 * @param update
 	 */
 	public static void setAutoUpdateFeedList(int update) 
 	{
 		autoUpdateFeedList = update;
 	}
 	
 	/**
 	 * @return Auto-update of the feedlist
 	 */
 	public static int getAutoUpdateFeedList()
 	{
 		return autoUpdateFeedList;
 	}
 	
 	/**
 	 * @return If the feeds should be auto-adjusted
 	 */
 	public static int getAutoAdjustFeeds()
 	{
 		return autoAdjustFeeds;
 	}
 
 	/**
 	 * @return Returns the timeOutInSecs.
 	 */
 	public static int getTimeOutInSecs()
 	{
 		return TimeOutInSecs;
 	}
 
 	/**
 	 * @param timeOutInSecs The timeOutInSecs to set.
 	 */
 	public static void setTimeOutInSecs(int timeOutInSecs)
 	{
 		TimeOutInSecs = timeOutInSecs;
 	}
 	
 	/**
 	 * @return Returns the seederSetting.
 	 */
 	public static int getSeederSetting()
 	{
 		return SeederSetting;
 	}
 
 	/**
 	 * @param seederSetting The seederSetting to set.
 	 */
 	public static void setSeederSetting(int seederSetting)
 	{
 		SeederSetting = seederSetting;
 	}
 
 	/**
 	 * @return Returns the current locale.
 	 */
 	public static Locale getLocale()
 	{
 		return tedLocale;
 	}
 	
 	/**
 	 * @return Returns current language code (eg en for english)
 	 */
 	public static String getLanguage()
 	{
 		return tedLocale.getLanguage();
 	}
 	
 	/**
 	 * @return current country code (eg US for United States)
 	 */
 	public static String getCountry()
 	{
 		return tedLocale.getCountry();
 	}
 
 	/**
 	 * @param language The language to set.
 	 */
 	public static void setLocale(Locale language)
 	{
 		tedLocale = language;
 	}
 	
 	/**
 	 * @param country The country
 	 * @param language The language
 	 */
 	public static void setLocale(String country, String language)
 	{
 		tedLocale = new Locale(language, country);
 	}
 	
 	public static void setParseAtStart(boolean b)
 	{
 		parseAtStart = b;
 	}
 	
 	/**
 	 * @return Should ted parse at startup?
 	 */
 	public static boolean isParseAtStart()
 	{
 		return parseAtStart;
 	}
 
 	/**
 	 * @return Should ted add a systray?
 	 */
 	public static boolean isAddSysTray()
 	{
 		return addSysTray;
 	}
 	
 	/**
 	 * Set the add systray setting
 	 * @param b
 	 */
 	public static void setAddSysTray(boolean b)
 	{
 		addSysTray = b;
 	}
 
 	/**
 	 * @return The download torrents with compressed files setting
 	 */
 	public static boolean getDoNotDownloadCompressed() 
 	{
 		return getCompressed;
 	}
 	
 	/**
 	 * Set the download torrents with compressed files settings
 	 * @param b
 	 */
 	public static void setDoNotDownloadCompressed(boolean b)
 	{
 		getCompressed = b;
 	}
 
 	/**
 	 * Set the extensions used in the filtering of torrents with compressed files
 	 * @param text
 	 */
 	public static void setFilterExtensions(String text) 
 	{
 		filterExtensions = text;
 	}
 	
 	/**
 	 * Get the extensions set by the user to filter torrents with compressed files
 	 * @return
 	 */
 	public static String getFilterExtensions()
 	{
 		return filterExtensions;
 	}
 	
 
 	/**
 	 * Get the number of times that ted has searched for new episodes after the last update check
 	 * @return
 	 */
 	public static int getTimesParsedSinceLastCheck() 
 	{
 		return timesParsedSinceLastCheck;
 	}
 
 	/**
 	 * Set the number of times that ted searched for new shows after the last update check
 	 * @param timesParsed
 	 */
 	public static void setTimesParsedSinceLastCheck(int timesParsed) 
 	{
 		timesParsedSinceLastCheck = timesParsed;
 	}
 
 	/**
 	 * Set the log setting
 	 * @param allowLog
 	 */
 	public static void setAllowLogging(boolean allowLog) 
 	{
 		allowLogging = allowLog;
 		TedLog.setAllowLogging(allowLog);
 	}
 
 	/**
 	 * Set the log to file setting
 	 * @param logToFile2
 	 */
 	public static void setLogToFile(boolean logToFile2) 
 	{
 		logToFile = logToFile2;
 		TedLog.setWriteToFile(logToFile2);
 	}
 
 	/**
 	 * @return If ted should keep a log
 	 */
 	public static boolean isAllowLogging() 
 	{
 		return allowLogging;
 	}
 	
 	/**
 	 * @return If ted should write the log to a file
 	 */
 	public static boolean isLogToFile() 
 	{
 		return logToFile;
 	}
 
 	public static Color getEvenRowColor() {
 		return evenRowColor;
 	}
 
 	public static void setEvenRowColor(Color evenRowColor) 
 	{
 		TedConfig.evenRowColor = evenRowColor;
 	}
 
 	public static Color getOddRowColor() 
 	{
 		return oddRowColor;
 	}
 
 	public static void setOddRowColor(Color oddRowColor) 
 	{
 		TedConfig.oddRowColor = oddRowColor;
 	}
 
 	public static Color getSelectedRowColor()
 	{
 		return selectedRowColor;
 	}
 
 	public static void setSelectedRowColor(Color selectedRowColor)
 	{
 		TedConfig.selectedRowColor = selectedRowColor;
 	}
 	
 	public static Color getGridColor()
 	{
 		return gridColor;
 	}
 	
 	public static void restoreDefaultColors()
 	{
 		setEvenRowColor(defaultEvenRowColor);
 		setOddRowColor (defaultOddRowColor );
 	}
 
 	public static void setTimeZoneOffset(int timezoneOffset) 
 	{
 		TedConfig.timeZoneOffset = timezoneOffset;
 	}
 	
 	public static int getTimeZoneOffset()
 	{
 		return TedConfig.timeZoneOffset;
 	}
 
 	public static boolean isUseAutoSchedule() 
 	{
 		return useAutoSchedule;
 	}
 
 	public static void setUseAutoSchedule(boolean useAutoSchedule) 
 	{
 		TedConfig.useAutoSchedule = useAutoSchedule;
 	}
 	
 	/**
 	 * @return The type of field the maintable should be sorted on.
 	 * 0 = no sort, 1 = on name, 2 = on status and airdate
 	 */
 	public static int getSortType()
 	{
 		return sortType;
 	}
 	
 	/**
 	 * @param sortType Type of sort that should be applied to the maintable
 	 */
 	public static void setSortType(int sortType)
 	{
 		TedConfig.sortType = sortType;
 	}
 	
 	/**
 	 * @return The direction of sorting for the maintable
 	 * 0 = ascensing, 1 = descending
 	 */
 	public static int getSortDirection()
 	{
 		return sortDirection;
 	}
 	
 	/**
 	 * @param direction Direction of sort for the maintable
 	 */
 	public static void setSortDirection (int direction)
 	{
 		TedConfig.sortDirection = direction;
 	}
 	
 	public static String getHDKeywords()
 	{
 		return hdKeywords;		
 	}
 	
 	public static void setHDKeywords(String keywords)
 	{
		TedConfig.hdKeywords = keywords;
 	}
 
 	public static void setHDDownloadPreference(boolean hdDownloadPreference) 
 	{
 		TedConfig.hdDownloadPreference = hdDownloadPreference;
 	}
 
 	public static boolean isHDDownloadPreference() 
 	{
 		return hdDownloadPreference;
 	}
 }
