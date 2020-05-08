 package ted;
 
 import java.awt.HeadlessException;
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.Serializable;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Vector;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.JOptionPane;
 
 import net.sf.torrentsniffer.bencoding.BencodingException;
 import net.sf.torrentsniffer.torrent.TorrentException;
 import net.sf.torrentsniffer.torrent.TorrentFile;
 import net.sf.torrentsniffer.torrent.TorrentImpl;
 import net.sf.torrentsniffer.torrent.TorrentInfo;
 import net.sf.torrentsniffer.torrent.TorrentState;
 import ted.datastructures.DailyDate;
 import ted.datastructures.SeasonEpisode;
 import ted.infrastructure.ITedMain;
 
 import com.sun.syndication.feed.synd.SyndContent;
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.sun.syndication.io.SyndFeedInput;
 import com.sun.syndication.io.XmlReader;
 
 /**
  * TED: Torrent Episode Downloader (2005 - 2006)
  * 
  * The parser checks one entire show for new episodes
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
 public class TedParser extends Thread implements Serializable{
 	/****************************************************
 	 * GLOBAL VARIABLES
 	 ****************************************************/
 	private ITedMain tMainDialog;
 	private boolean foundTorrent;
 	private TedParserKeywordChecker tPKeyChecker = new TedParserKeywordChecker();
 	private TedParserDateChecker tPDateChecker = new TedParserDateChecker();
 	private TorrentImpl bestTorrent;
 	private TorrentInfo bestTorrentInfo;
 	private URL bestTorrentUrl = null;
 	private Vector<DailyDate> dailyItems;
 	private SyndFeed[] feedsData = null;
 	private int totalNumberOfFeedItems = 0;
 	private TedSerie currentSerie;
 
 	private int checkedTorrents = 0;
 	private int foundTorrents = 0;
 	private int bestTorrentSeeders = 0;
 
 	private int itemNr = 0;
 	private int bestItemNr = 0;
 
 	private String[][] parseLogInfo;
 	private boolean forceParce = false;
 	private boolean stopGetItems = false;
 
 	/****************************************************
 	 * CONSTRUCTORS
 	 ****************************************************/
 	/**
 	 * Creates a new tedparser
 	 * 
 	 * @param serie
 	 *            Serie to parse
 	 * @param main
 	 *            TedMainDialog to report to
 	 */
 	TedParser(TedSerie serie, ITedMain main)
 	{
 		this.tMainDialog = main;
 		this.currentSerie = serie;
 
 		// reset globals
 		foundTorrent = false;
 
 		this.bestTorrent = null;
 		this.bestTorrentInfo = null;
 		this.bestTorrentUrl = null;
 		this.feedsData = null;
 		totalNumberOfFeedItems = 0;
 		bestTorrentSeeders = 0;
 
 		this.dailyItems = new Vector();
 	}
 
 	public TedParser()
 	{
 
 	}
 
 	public TedParser(TedSerie serie, ITedMain mainDialog, boolean forceParse)
 	{
 		this.tMainDialog = mainDialog;
 		this.currentSerie = serie;
 
 		// reset globals
 		foundTorrent = false;
 
 		this.bestTorrent = null;
 		this.bestTorrentInfo = null;
 		this.bestTorrentUrl = null;
 		this.feedsData = null;
 		totalNumberOfFeedItems = 0;
 		bestTorrentSeeders = 0;
 
 		this.dailyItems = new Vector();
 		this.forceParce = forceParse;
 	}
 
 	public void run()
 	{
 		// check if episode schedule needs an update
 		this.currentSerie.isEpisodeScheduleAvailableWithUpdate();
 
 		if (this.currentSerie.isCheck() || this.forceParce)
 		{
 			this.currentSerie.setActivity(TedSerie.IS_PARSING);
 			// load xml feeds into memory
 			this.loadFeeds(currentSerie, tMainDialog);
 			// parse the feeds for new episodes
 			this.parseFeeds(currentSerie, tMainDialog);
 			this.currentSerie.setActivity(TedSerie.IS_IDLE);
 
 			// clear memory
 			this.feedsData = null;
 			this.parseLogInfo = null;
 			// call garbage collector to cleanup dirt
 			Runtime.getRuntime().gc();
 		}
 	}
 
 	/**
 	 * Loads all the data from the feeds of the serie into a global array. This
 	 * to prevent the data from loaded everytime the episode number is increased
 	 * 
 	 * @param serie
 	 *            A tedserie
 	 * @param main
 	 *            A ted maindialog
 	 */
 	private void loadFeeds(TedSerie serie, ITedMain main)
 	{
 		if (feedsData != null)
 		{
 			// feeds already loaded;
 			// do not load them again
 			return;
 		}
 
 		// Set up the rss parser
 		SyndFeedInput input = new SyndFeedInput();
 
 		Vector feeds = serie.getFeeds();
 		TedSerieFeed currentFeed;
 
 		feedsData = new SyndFeed[feeds.size()];
 		for (int i = 0; i < feeds.size(); i++)
 		{
 			if (main.getStopParsing())
 			{
 				return;
 			}
 			// progress++;
 			currentFeed = (TedSerieFeed) feeds.get(i);
 			try 
 			{
 				URLConnection urlc;
 				URL feedURL = new URL(currentFeed.getUrl());
 
 				TedLog.debug("Loading feed from " + serie.getName() + " URL: " + feedURL); //$NON-NLS-1$ //$NON-NLS-2$
 				serie.setStatusString(Lang.getString("TedParser.StatusLoading") + " " + feedURL, tMainDialog); //$NON-NLS-1$
 
 				// Read in the feed.
 				BufferedInputStream stream = TedIO.makeBufferedInputStream(feedURL, TedConfig.getInstance().getTimeOutInSecs());
 
 				XmlReader xmlReader = new XmlReader(stream);
 				SyndFeed feed = input.build(xmlReader);
 
 				feedsData[i] = feed;
 				// This call can throw a nullpointer exception if no items are
 				// present in the feed, catched below
 				totalNumberOfFeedItems += feed.getEntries().size();
 
 				// clear memory
 				feed = null;
 			} 
 			catch (MalformedURLException e)
 			{
 				String message = Lang.getString("TedParser.ErrorNotValidURL1") + " " + serie.getName() + ". " + Lang.getString("TedParser.ErrorNotValidURL2") + "\n" + currentFeed.getUrl(); //$NON-NLS-1$
 				tMainDialog.displayError(Lang.getString("TedParser.ErrorHeader"), message, ""); //$NON-NLS-1$ //$NON-NLS-2$
 				feedsData[i] = null;
 			} 
 			catch (IOException e)
 			{
 				String message = Lang.getString("TedParser.Error404Feed1")
 						+ " "
 						+ serie.getName()
 						+ "\n" + Lang.getString("TedParser.Error404Feed2") + " " + currentFeed.getUrl(); //$NON-NLS-1$ //$NON-NLS-2$
 				tMainDialog.displayError(Lang
 						.getString("TedParser.ErrorHeader"), message, ""); //$NON-NLS-1$ //$NON-NLS-2$
 				feedsData[i] = null;
 			} 
 			catch (NullPointerException e)
 			{
 				// no items in the feed
 				String message = Lang.getString("TedParser.NoFeedItems1")
 						+ serie.getName()
 						+ "\n" + Lang.getString("TedParser.NoFeedItems1") + currentFeed.getUrl(); //$NON-NLS-1$ //$NON-NLS-2$
 				tMainDialog.displayError(Lang
 						.getString("TedParser.ErrorHeader"), message, ""); //$NON-NLS-1$ //$NON-NLS-2$
 				feedsData[i] = null;
 			} 
 			catch (Exception e)
 			{
 				TedLog.error(e,
 							 Lang.getString("TedParser.UnknownException") + " (" + currentFeed.getUrl() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
 				feedsData[i] = null;
 			}
 		}
 		return;
 	}
 
 	/**
 	 * Parse the feeds of a serie for new episodes
 	 * 
 	 * @param serie
 	 *            Serie to be parsed
 	 * @param main
 	 *            Tedmaindialog
 	 */
 	private void parseFeeds(TedSerie serie, ITedMain main)
 	{
 		// reset globals
 		foundTorrent = false;
 
 		this.bestTorrent = null;
 		this.bestTorrentInfo = null;
 		this.bestTorrentUrl = null;
 		bestTorrentSeeders = 0;
 		this.itemNr = 0;
 		this.bestItemNr = 0;
 
 		parseLogInfo = new String[this.totalNumberOfFeedItems + 1][2];
 
 		parseLogInfo[0][0] = "\n" + Lang.getString("TedLog.ParseResults") + " "
 				+ currentSerie.getName() + " - "
 				+ currentSerie.getSearchForString();
 
 		Vector feeds = serie.getFeeds();
 
 		serie.setProgress(0, tMainDialog);
 
 		double progress = 0;
 		int itemProgress = 0;
 		double progressPerItem = 100.0 / this.totalNumberOfFeedItems;
 		this.checkedTorrents = 0;
 		SyndFeed feed;
 
 		for (int i = 0; i < feedsData.length; i++)
 		{
 			if (main.getStopParsing())
 			{
 				return;
 			}
 
 			feed = feedsData[i];
 
 			if (feed != null)
 			{
 				List<SyndEntry> items = feed.getEntries();
 
 				// walk through the different entries until we find a desired
 				// episode
 				// we walk in the wrong direction to get older torrents first
 				// (older = more seeders)
 				for (SyndEntry item : items)
 				{
 					if (main.getStopParsing())
 					{
 						return;
 					}
 					progress += progressPerItem;
 					itemProgress++;
 					
 					serie.setProgress((int) Math.abs(progress), tMainDialog);
 					serie.setStatusString(Lang.getString("TedParser.StatusCheckingItem") + " " + itemProgress + "/" + totalNumberOfFeedItems, tMainDialog); //$NON-NLS-1$ //$NON-NLS-2$
 
 					if (serie.isDaily || this.continueParsing())
 					{
 						if (titleMatchesKeywords(item, serie))
 						{
 							if (!serie.isDaily)
 							{
 								this.ParseItem(item, serie, feed.getTitle());
 							} 
 							else
 							{
 								// retrieve date from string
 								DailyDate date = getDailyDateFromItem(item);
 								// if date isn't found or date of item is older
 								// than latest downloaded item
 								// don't download item
 								if (   date != null
 									&& ((((TedDailySerie) serie).getLatestDownloadDateInMillis()) <= date.getDate().getTimeInMillis()))
 								{
 									this.addDailyItem(item, serie);
 								}
 							}
 						}
 					} 
 					else
 					{
 						i = feeds.size();
 					}
 				}
 
 				items = null;
 			}
 		}
 		try 
 		{
 			if (!serie.isDaily)
 			{
 				this.downloadBest(serie);
 			}
 			else
 			{
 				this.downloadBestDaily(serie);
 			}
 
 			serie.setProgress(100, tMainDialog);
 		} 
 		catch (Exception e)
 		{
 			// error while downloading best torrent
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * @return Whether the parser should continue parsing considering the found
 	 *         torrent and the user settings
 	 */
 	private boolean continueParsing()
 	{
 		if (TedConfig.getInstance().getSeederSetting() == TedConfig.DOWNLOADMOSTSEEDERS)
 		{
 			return true;
 		}
 		else if (TedConfig.getInstance().getSeederSetting() == TedConfig.DOWNLOADMINIMUMSEEDERS && this.bestTorrentUrl == null)
 		{
 			return true;
 		}
 		return false;
 	}
 
 	/****************************************************
 	 * LOCAL METHODS
 	 ****************************************************/
 	/**
 	 * Parse one item from the RSS feed. Checks the contents, finds episode and
 	 * seaon numbers and takes specific actions (like saving the torrent or
 	 * alerting the user)
 	 * 
 	 * @param item
 	 *            Item to parse
 	 * @param serie
 	 *            Serie this item is from
 	 * @param source
 	 *            Source of the Feed
 	 * @throws FileSizeException
 	 * @throws HeadlessException
 	 */
 	private void ParseItem(SyndEntry item, TedSerie serie, String source)
 	{
 		int season = 0;
 		int episode = 0;
 		String sTitle = item.getTitle().toString();
 
 		// check the episode and season
 		SeasonEpisode se = getSeasonEpisodeFromItem(item, serie, source, false);
 		if (se != null)
 		{
 			season = se.getSeason();
 			episode = se.getEpisode();
 		}
 		// used if user wants to get the latest season/episode from the feed
 		// limit of 50 as maximum episode/season number
 
 		// if the season is the current season and episode is the next episode
 		// or if the season is the next season and episode is the first episode
 		// also download all from feed is so selected
 
 		// translate the url from the feed to a download url
 		String torrentUrl = item.getLink().toString();
 
 		// Do we have found the correct episode?
 		boolean foundCorrectEpisode = (season == serie.getCurrentSeason() && episode == serie.getCurrentEpisode());
 
 		// Some extra check for double episode. Make sure that the next episode
 		// number is also in the string of the torrent file.
 		if (foundCorrectEpisode && serie.currentEpisodeSS.isDouble())
 		{
 			foundCorrectEpisode = sTitle.contains("" + (episode + 1));
 		}
 
 		if (foundCorrectEpisode)
 		{
 			torrentUrl = TedIO.getInstance().translateUrl(torrentUrl, sTitle, TedConfig.getInstance().getTimeOutInSecs());
 
 			TedLog.debug(Lang.getString("TedSerie.Checking") + " " + sTitle); //$NON-NLS-1$
 			serie.setStatusString(Lang.getString("TedSerie.Checking") + " " + sTitle, tMainDialog); //$NON-NLS-1$
 			tMainDialog.repaint();
 
 			if (torrentUrl == null)
 			{
 				tMainDialog.displayError(Lang.getString("TedParser.ErrorHeader"),
 										 Lang.getString("TedParser.ErrorNotSupportedFeed1") + " " + serie.getName() + " " + Lang.getString("TedParser.ErrorNotSupportedFeed2") + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 										 Lang.getString("TedParser.ErrorNotSupportedFeed3"), Lang.getString("TedParser.ErrorNotSupportedFeed4") + " " + source); //$NON-NLS-1$ //$NON-NLS-2$
 			} 
 			else
 			{
 				// save found torrent to file
 
 				try 
 				{
 					this.checkIfBest(torrentUrl, serie, item);
 				} 
 				catch (Exception e)
 				{
 					TedLog.error(e, Lang.getString("TedLog.ErrorTorrent")); //$NON-NLS-1$
 				}
 			}
 		} 
 		else if (season == serie.getCurrentSeason() + 1 && episode == 1)
 		{
 			TedLog.debug(Lang.getString("TedParser.FoundNextSeason"));
 			torrentUrl = TedIO.getInstance().translateUrl(torrentUrl, 
 														  sTitle, 
 														  TedConfig.getInstance().getTimeOutInSecs());
 
 			// make connection with torrent
 			URL url;
 			try 
 			{
 				url = new URL(torrentUrl);
 			} 
 			catch (Exception e)
 			{
 				String message = Lang.getString("TedParser.ErrorWhileChecking1") + " " + torrentUrl + " " + Lang.getString("TedParser.ErrorWhileChecking2") + serie.getName(); //$NON-NLS-1$ //$NON-NLS-2$
 				tMainDialog.displayError(Lang.getString("TedParser.ErrorHeader"), message, "Exception"); //$NON-NLS-1$ //$NON-NLS-2$
 				
 				e.printStackTrace();
 				return;
 			}
 			int seedersFromRSS = getSeedersFromRSS(item);
 
 			// Only load the torrent when the seeders found in the RSS entry are 0 or
 			// larger than the minimum number of seeders
 			if (seedersFromRSS <= 0 || seedersFromRSS >= serie.getMinNumOfSeeders())
 			{
 				TedLog.debug(Lang.getString("TedLog.LoadingTorrent")); //$NON-NLS-1$
 				TorrentImpl torrent = new TorrentImpl(url, TedConfig.getInstance().getTimeOutInSecs());
 	
 				// check size and amount of seeders to filter out fakes
 				boolean correctSize = true;
 				try 
 				{
 					hasCorrectSize(torrent, serie, item);
 				} 
 				catch (FileSizeException e)
 				{
 					correctSize = false;
 				}
 	
 				if (hasEnoughSeeders(torrent, serie, seedersFromRSS) && correctSize)
 				{
 					// we found a new season, does the user wants to download it?
 					//if we are headless, yes indeed
 	
 					int answer = JOptionPane.NO_OPTION;
 					if (TedSystemInfo.isHeadless())
 					{
 						answer = JOptionPane.YES_OPTION;
 					}
 					else
 					{
 						if ( TedConfig.getInstance().isDownloadNewSeason())
 						{					
 		
 							// ask user if he wants to download new season
 							answer = JOptionPane
 									.showOptionDialog(
 											null,
 											Lang
 													.getString("TedParser.DialogNewSeason1") + " " + season + " " + Lang.getString("TedParser.DialogNewSeason2") + " " + serie.getName() + "." //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 													+ "\n" + Lang.getString("TedParser.DialiogNewSeason3") + " " + season + "?" //$NON-NLS-1$ //$NON-NLS-2$
 													+ "\n"
 													+ Lang
 															.getString("TedParser.DialogNewSeason4")
 													+ " "
 													+ (season - 1)
 													+ "\n" + Lang.getString("TedParser.DialogNewSeason5"), //$NON-NLS-1$ //$NON-NLS-2$
 											Lang
 													.getString("TedParser.DialogNewSeasonHeader") + serie.getName(), //$NON-NLS-1$
 											JOptionPane.YES_NO_OPTION,
 											JOptionPane.QUESTION_MESSAGE, null, Lang
 													.getYesNoLocale(), Lang
 													.getYesNoLocale()[0]);
 						}
 	
 						if (answer == JOptionPane.YES_OPTION)
 						{
 							// download new season
 							SeasonEpisode nextSeason = new SeasonEpisode(season, 1);
 							serie.setCurrentEpisode(nextSeason);
 							tMainDialog.saveShows();
 	
 							// only the season and episode are changed, no torrent
 							// has actually
 							// been downloaded
 							foundTorrent = false;
 						} else
 						{
 							// remember the preference of the user
 							TedConfig.getInstance().setDownloadNewSeason(false);
 						}
 					}
 				}
 			}
 		}
 
 	}
 
 	/**
 	 * @param item
 	 * @return Number of seeders in item if present. If not present: -1.
 	 */
 	private int getSeedersFromRSS(SyndEntry item) 
 	{
 		int numberOfSeeders = -1;
 		String urlInFeed = item.getLink();
 		if (urlInFeed.contains("mininova"))
 		{
 			numberOfSeeders = getMininovaDescriptionSeeders(item);
 		}
 		else if (urlInFeed.contains("btjunkie"))
 		{
 			numberOfSeeders = getBtJunkieDescriptionSeeders(item.getTitle());
 		}
 		
 		TedLog.debug("Seeders from RSS:" + " " + numberOfSeeders);
 		
 		return numberOfSeeders;
 	}
 
 	/**
 	 * If the torrent of this satifies serie filters add it to the download
 	 * array of the daily serie
 	 * 
 	 * @param item
 	 *            the item which has to be checked
 	 * @param serie
 	 *            the daily serie which filters the torrents has to satisfy
 	 */
 	private void addDailyItem(SyndEntry item, TedSerie serie)
 	{
 		String sTitle = item.getTitle().toString();
 		String torrentUrl = item.getLink().toString();
 		torrentUrl = TedIO.getInstance().translateUrl(torrentUrl, sTitle, TedConfig.getInstance().getTimeOutInSecs());
 
 		this.bestTorrent = null;
 		this.bestTorrentInfo = null;
 		this.bestTorrentUrl = null;
 		this.bestTorrentSeeders = 0;
 
 		// check seeders, size and keyword filters
 		checkIfBest(torrentUrl, serie, item);
 
 		// itemNr is updated in checkIfBest
 		parseLogInfo[itemNr][0] = torrentUrl;
 
 		// if torrent satifies check if we havn't found a better torrent
 		// with the same date
 		if (this.bestTorrentUrl != null)
 		{
 			DailyDate dd = getDailyDateFromItem(item);
 			dd.setSeeders(bestTorrentSeeders);
 			dd.setUrl(bestTorrentUrl);
 
 			checkIfBestDaily(dd);
 		}
 	}
 
 	/**
 	 * Check if there isn't a better item already available selected for
 	 * download
 	 * 
 	 * @param dd
 	 *            the DailyDate which has to be compared to the other items in
 	 *            the download array
 	 */
 	private void checkIfBestDaily(DailyDate newItem)
 	{
 		// for all daily show episodes which are selected for download
 		for (int i = 0; i < dailyItems.size(); i++) 
 		{
 			DailyDate existingItem = (DailyDate) dailyItems.get(i);
 
 			// see if there is already a selected daily with the same date as
 			// the new DD
 			if (existingItem.getDate().getTimeInMillis() == newItem.getDate().getTimeInMillis())
 			{
 				// if the new DD is has more seeders replace the old selected daily
 				if (existingItem.getSeeders() < newItem.getSeeders())
 				{
 					dailyItems.remove(i);
 					dailyItems.add(newItem);
 
 					int placeInLog = findPlaceInLog(existingItem.getUrl().toString());
 					if (placeInLog != 0)
 					{
 						parseLogInfo[placeInLog][1] = Lang
 								.getString("TedMainMenuBar.File")
 								+ " "
 								+ itemNr
 								+ " "
 								+ Lang.getString("TedLog.FoundBetterTorrent");
 
 					}
 
 					parseLogInfo[itemNr][1] = Lang.getString("TedLog.BestTorrent");
 
 					return;
 				} 
 				else
 				{
 					// as for every date there is only one selected daily you
 					// can stop
 					for (int j = 0; j < this.totalNumberOfFeedItems + 1; j++)
 					{
 						// existing one is better so log this
 						int placeInLog = findPlaceInLog(existingItem.getUrl().toString());
 
 						parseLogInfo[itemNr][1] = Lang
 								.getString("TedMainMenuBar.File")
 								+ " "
 								+ placeInLog
 								+ " "
 								+ Lang.getString("TedLog.FoundBetterTorrent");
 					}
 					return;
 				}
 			}
 		}
 
 		parseLogInfo[itemNr][1] = Lang.getString("TedLog.BestTorrent");
 
 		dailyItems.add(newItem);
 	}
 
 	/**
 	 * Checks if torrent satisfies user size and seeder settings and if its
 	 * better than the current selected best torrent
 	 * 
 	 * @param torrentUrl
 	 *            URL of torrent to check
 	 * @param serie
 	 *            TedSerie that torrent belongs to
 	 */
 	private void checkIfBest(String torrentUrl, TedSerie serie, SyndEntry item)
 	{
 		// make url
 		URL url;
 		TorrentImpl torrent;
 		TorrentInfo torrentInfo;
 
 		this.checkedTorrents++;
 
 		itemNr++;
 		parseLogInfo[itemNr][0] = torrentUrl.toString();
 
 		try 
 		{
 			url = new URL(torrentUrl);
 		} 
 		catch (Exception e)
 		{
 			String message = Lang.getString("TedParser.ErrorWhileChecking1") + " " + torrentUrl + " " + Lang.getString("TedParser.ErrorWhileChecking2") + serie.getName(); //$NON-NLS-1$ //$NON-NLS-2$
 			tMainDialog	.displayError(Lang.getString("TedParser.ErrorHeader"), message, "Exception"); //$NON-NLS-1$ //$NON-NLS-2$
 
 			parseLogInfo[itemNr][1] = Lang.getString("TedLog.ErrorTorrentInfo");
 
 			return;
 		}
 
 		// download torrent info
 		try 
 		{
 			int seedersFromRSS = getSeedersFromRSS(item);
 			// Only load the torrent when the seeders found in the RSS entry are 0 or
 			// larger than the minimum number of seeders
 			if (seedersFromRSS <= 0 || seedersFromRSS >= serie.getMinNumOfSeeders())
 			{
 			
 				TedLog.debug(Lang.getString("TedLog.LoadingTorrent")); //$NON-NLS-1$
 				torrent = new TorrentImpl(url, TedConfig.getInstance().getTimeOutInSecs());
 				// get torrent info (for size)
 				torrentInfo = torrent.getInfo();
 				
 				// First see if we�re dealing with a private tracker.
 				String trackerUrl = torrent.getTracker().getAnnounce();
 				if (TedConfig.getInstance().isFilterPrivateTrackers()
 				 && TedConfig.getInstance().isPrivateTracker(trackerUrl))
 				{
 					parseLogInfo[itemNr][1] = Lang.getString("TedLog.ErrorPrivateTracker") + " (" + trackerUrl + ")";
 					return;
 				}
 				
 				// Check for the size of the file.
 				try 
 				{
 					hasCorrectSize(torrent, serie, item);
 				} 
 				catch (FileSizeException e)
 				{
 					parseLogInfo[itemNr][1] = Lang.getString("TedLog.ErrorFileSize") + " (" + e.size + ")";
 					throw e;
 				}
 	
 				// if the user does not want to download compressed files
 				if (TedConfig.getInstance().getDoNotDownloadCompressed())
 				{
 					// and the torrent contains compressed files
 					if (this.containsCompressedFiles(torrent))
 					{
 						// reject it
 						parseLogInfo[itemNr][1] = Lang.getString("TedLog.ErrorCompressedFiles");
 						return;
 					}
 				}
 	
 				// get torrent state (for seeders)
 				try 
 				{
 					int torrentSeeders = 0;
 					// only get the torrent state when minimum seeders > 0
 					if (seedersFromRSS > 0)
 					{
 						torrentSeeders = seedersFromRSS;
 					}
 					else if (   serie.getMinNumOfSeeders() > 0 
 						|| TedConfig.getInstance().getSeederSetting() == TedConfig.DOWNLOADMOSTSEEDERS)
 					{
 						torrentSeeders = getSeedersFromTorrent(torrent, serie);
 					}
 	
 					// compare with best
 					// if more seeders than best and more seeders than min seeders
 					if (   (this.bestTorrentUrl == null || (torrentSeeders > this.bestTorrentSeeders))
 						&& torrentSeeders >= serie.getMinNumOfSeeders())
 					{
 						// print seeders
 						TedLog.debug("Found new best torrent! (" + torrentSeeders + " seeders)"); //$NON-NLS-1$ //$NON-NLS-2$
 						
 						// current is best
 						this.bestTorrentUrl = url;
 						this.bestTorrentInfo = torrentInfo;
 						this.bestTorrentSeeders = torrentSeeders;
 						this.bestTorrent = torrent;
 	
 						if (!serie.isDaily)
 						{
 							if (bestItemNr != 0)
 							{
 								// the message for the old best torrent is
 								// changed...
 								parseLogInfo[bestItemNr][1] = Lang.getString("TedMainMenuBar.File")
 															+ " "
 															+ itemNr
 															+ " "
 															+ Lang.getString("TedLog.FoundBetterTorrent");
 							}
 	
 							// and the new best torrent gets his own message...
 							parseLogInfo[itemNr][1] = Lang.getString("TedLog.BestTorrent");
 							this.bestItemNr = itemNr;
 						}
 					} 
 					else
 					{
 						parseLogInfo[itemNr][1] = Lang.getString("TedLog.ErrorSeeders")	+ " (" + torrentSeeders + ")";
 						TedLog.debug("Torrent has not enough seeders (" + torrentSeeders + ")"); //$NON-NLS-1$ //$NON-NLS-2$
 					}
 				} 
 				catch (Exception e)
 				{
 					parseLogInfo[itemNr][1] = Lang.getString("TedLog.ErrorTorrentInfo");
 	
 					if (e.getMessage().contains("bencoding"))
 					{
 						parseLogInfo[itemNr][1] = Lang.getString("TedLog.BencodingError");
 					}
 	
 					TedLog.error(e,	"Error getting trackerstate for torrent " + torrentInfo.getName()); //$NON-NLS-1$
 				}
 			}
			else
			{
				parseLogInfo[itemNr][1] = Lang.getString("TedLog.ErrorSeeders")	+ " (" + seedersFromRSS + ")";
				TedLog.debug("Torrent has not enough seeders (" + seedersFromRSS + ")"); //$NON-NLS-1$ //$NON-NLS-2$
			}
 		}
 		// TODO: catch all exceptions here and determine logging
 		catch (BencodingException e)
 		{
 			// error reading torrentinfo or info from tracker
 			if (e.getMessage().startsWith("Unknown object")) 
 			{
 				tMainDialog	.displayError(
 								Lang.getString("TedParser.ErrorHeader"),
 								Lang.getString("TedParser.ErrorWhileChecking1")
 										+ torrentUrl
 										+ "."
 										+ "\n" + Lang.getString("TedParser.ErrorBencoding"), "Exception"); //$NON-NLS-1$ //$NON-NLS-2$
 			} else
 			{
 				tMainDialog	.displayError(Lang.getString("TedParser.ErrorHeader"), e.getMessage(), "Exception"); //$NON-NLS-1$ //$NON-NLS-2$
 			}
 
 			parseLogInfo[itemNr][1] = Lang.getString("TedLog.BencodingError");
 		} 
 		catch (TorrentException e)
 		{
 			// error reading torrentinfo or info from tracker
 			// tMainDialog.displayError("ted Error!", e.getMessage(),
 			// "Exception");
 			TedLog.error(e, e.getLocalizedMessage());
 
 			parseLogInfo[itemNr][1] = Lang.getString("TedLog.ErrorTorrentInfo");
 
 			return;
 
 		} 
 		catch (FileSizeException e) 
 		{
 			// torrent contents too small
 			return;
 		} 
 		catch (RuntimeException e)
 		{
 			// happens when scraping tracker for torrent seeder information
 			e.printStackTrace();
 			TedLog.error(e, e.getLocalizedMessage());
 
 			parseLogInfo[itemNr][1] = Lang.getString("TedLog.ErrorTorrentInfo");
 
 			return;
 		} 
 		catch (Exception e)
 		{
 			String message = Lang.getString("TedParser.ErrorDownloadingContent1")
 						   + " "
 						   + torrentUrl
 						   + " " + Lang.getString("TedParser.ErrorDownloadingContent2") + " " + serie.getName(); //$NON-NLS-1$ //$NON-NLS-2$
 			tMainDialog.displayError(Lang.getString("TedParser.ErrorHeader"), message, "Exception"); //$NON-NLS-1$ //$NON-NLS-2$
 			TedLog.error(e, e.getLocalizedMessage());
 			e.printStackTrace();
 
 			parseLogInfo[itemNr][1] = Lang.getString("TedParser.ErrorDownloadingContent1");
 		}
 	}
 
 	/**
 	 * Checks if torrent satisfies user size
 	 * 
 	 * @param torrent
 	 *            torrentImpl of torrent to check
 	 * @param serie
 	 *            TedSerie that torrent belongs to
 	 * @return Returns if the torrent has the correct size
 	 */
 	private boolean hasCorrectSize(TorrentImpl torrent, 
 								   TedSerie    serie,
 								   SyndEntry   rssItem)	throws FileSizeException 
 	{
 		int minSize = serie.getMinSize();
 		int maxSize = serie.getMaxSize();
 
 		// If current episode is a double episode update the max size
 		// by a factor two.
 		if (serie.isDoubleEpisode())
 		{
 			maxSize *= 2;
 		}
 
 		int sizeMB = 0;
 		sizeMB = getRssDescriptionSize(rssItem);
 		
 		// Size not found in description, try the torrent file itself.
 		if (sizeMB == 0)
 		{
 			// get torrent info (for size)
 			TorrentInfo torrentInfo = torrent.getInfo();
 			// first check if size is between min and max
 			// convert bytes to MB
 			double byteSize = 9.5367431 * Math.pow(10, -7);
 			sizeMB = (int) Math.round(torrentInfo.getLength() * byteSize);
 		}
 
 		// the size of the file(s) is zero. Not useful and probably not even a
 		// torrent
 		if (sizeMB == 0)
 		{
 			TedLog.debug(Lang.getString("TedParser.FileSizeSmall") + " (" + sizeMB + ")"); //$NON-NLS-1$ //$NON-NLS-2$
 			FileSizeException e = new FileSizeException();
 			e.size = 0;
 			throw e;
 		}
 
 		// the size is smaller than the minimum size or larger than the maximum
 		// size
 		if ((minSize != 0 && minSize >= sizeMB)
 		 || (maxSize != 0 && maxSize <= sizeMB))
 		{
 			// print error
 			if (sizeMB > maxSize)
 			{
 				TedLog.debug(Lang.getString("TedParser.FileSizeLarge") + " (" + sizeMB + " mb)"); //$NON-NLS-1$ //$NON-NLS-2$
 			} 
 			else if (sizeMB < minSize)
 			{
 				TedLog.debug(Lang.getString("TedParser.FileSizeSmall") + " (" + sizeMB + " mb)"); //$NON-NLS-1$ //$NON-NLS-2$
 			}
 			
 			// throw exception
 			FileSizeException e = new FileSizeException();
 			e.size = sizeMB;
 			throw e;
 		} 
 		else
 		{
 			TedLog.debug(Lang.getString("TedParser.FileSizeOK") + " (" + sizeMB + " mb)"); //$NON-NLS-1$ //$NON-NLS-2$
 			return true;
 		}
 	}
 
 	/**
 	 * Checks if torrent satisfies seeder settings
 	 * 
 	 * @param torrent
 	 *            torrentImpl of torrent to check
 	 * @param serie
 	 *            TedSerie that torrent belongs to
 	 * @return Returns if the torrent has enough seeders
 	 */
 	private boolean hasEnoughSeeders(TorrentImpl torrent, TedSerie serie, int seedersFromRSS)
 	{
 		// Get torrent state (containing seeders/leechers)
 		try 
 		{
 			int numberOfSeeders = seedersFromRSS;
 			if (seedersFromRSS == 0)
 			{
 				numberOfSeeders = getSeedersFromTorrent(torrent, serie);
 			}
 			// Return if the found number of seeders is large enough.
 			return (numberOfSeeders >= serie.getMinNumOfSeeders());
 		} 
 		catch (Exception e)
 		{
 			return false;
 		}
 	}
 	
 	private int getSeedersFromTorrent(TorrentImpl torrent, TedSerie serie)
 	{
 		// First try to get the number of the torrent.
 		int numberOfSeeders = 0;
 		TorrentState torrentState = torrent.getState(TedConfig.getInstance().getTimeOutInSecs());
 		numberOfSeeders = torrentState.getComplete();
 		
 		TedLog.debug("Seeders from torrent:" + " " + numberOfSeeders); //$NON-NLS-1$ //$NON-NLS-2$
 
 		return numberOfSeeders;
 	}
 
 	private boolean titleMatchesKeywords(SyndEntry item, TedSerie serie)
 	{
 		String torrentTitle = item.getTitle().toString();
 		boolean matchesNormalKeywords = tPKeyChecker.checkKeywords(
 				torrentTitle, serie.getKeywords());
 
 		boolean matchesHDKeywords = true;
 		if (serie.isDownloadInHD())
 		{
 			matchesHDKeywords = tPKeyChecker.checkKeywords(torrentTitle,
 					TedConfig.getInstance().getHDKeywords());
 		}
 
 		return matchesHDKeywords && matchesNormalKeywords;
 	}
 
 	/**
 	 * Checks if torrent contains compressed files
 	 * 
 	 * @param torrent
 	 *            torrentImpl of torrent to check
 	 * @return Returns if the torrents contains a compressed file
 	 */
 	private boolean containsCompressedFiles(TorrentImpl torrent)
 	{
 		TorrentInfo torrentInfo = torrent.getInfo();
 		TorrentFile[] files;
 
 		// check if the torrent contains multiple files
 		if (!torrentInfo.isSingleFile())
 		{
 			files = torrentInfo.getMultiFile();
 			String name, type;
 
 			// check all files if any of them is a compressed file
 			for (int i = 0; i < files.length; i++)
 			{
 				name = files[i].getPath().toString();
 				type = name.substring(name.length() - 3);
 
 				if (isCompressedFile(type))
 				{
 					// found a compressed file
 					TedLog.debug(Lang.getString("TedParser.CompressedFiles"));
 					return true;
 				}
 			}
 
 			// no compressed files found
 			return false;
 		} else
 		{
 			// check to see if the single file is compressed
 			if (isCompressedFile(torrentInfo.getName().substring(
 					torrentInfo.getName().length() - 3)))
 			{
 				TedLog.debug(Lang.getString("TedParser.CompressedFiles"));
 				return true;
 			} else
 			{
 				return false;
 			}
 		}
 	}
 
 	/**
 	 * Checks if the given extension is that of a compressed file
 	 * 
 	 * @param extension
 	 *            The extension of the file to check
 	 * @return Returns if the extension is that of a compressed file
 	 */
 	private boolean isCompressedFile(String extension)
 	{
 		String extensions = TedConfig.getInstance().getFilterExtensions();
 		if (extensions.contains(extension))
 			return true;
 		else
 			return false;
 	}
 
 	/**
 	 * For the given serie download at most the max number of shows the user has
 	 * set up from the download array
 	 * 
 	 * @param serie
 	 *            the serie for which the items have to be downloaded
 	 */
 	private void downloadBestDaily(TedSerie serie) throws Exception {
 		Collections.sort(dailyItems);
 		TedDailySerie dailySerie = (TedDailySerie) serie;
 
 		// if 0 is selected download everything, otherwise the given number
 		int maxDownloads = Math.min(dailyItems.size(), dailySerie
 				.getMaxDownloads());
 		if (maxDownloads == 0)
 		{
 			maxDownloads = dailyItems.size();
 		}
 
 		// For correctly updating to the new date.
 		boolean firstDownload = true;
 
 		DailyDate dd;
 		for (int i = 0; i < dailyItems.size(); i++)
 		{
 			// get the current daily date
 			dd = (DailyDate) dailyItems.get(i);
 
 			// get url
 			this.bestTorrentUrl = dd.getUrl();
 
 			// if we want to download this daily
 			if (i < maxDownloads)
 			{
 				try {
 					downloadBest(dailySerie, dd);
 				} catch (Exception e)
 				{
 					throw e;
 				}
 
 				// Add one day, to search for next episode
 				// Do this only for the first download. As the shows are sorted
 				// the first download is the one with the latest date. You want
 				// the show to be put on the date after the downloaded episode
 				// with the latest date.
 				if (firstDownload)
 				{
 					dailySerie.goToNextEpisode(dd);
 					firstDownload = false;
 				}
 			} else
 			{
 				parseLogInfo[findPlaceInLog(this.bestTorrentUrl.toString())][1] = Lang
 						.getString("TedLog.TooOld")
 						+ " " + maxDownloads;
 			}
 		}
 
 		TedLog.simpleLog(generateLogMessage());
 		serie.setStatusString(this.generateOverviewMessage(), tMainDialog);
 	}
 
 	/**
 	 * Downloads the currently best torrent to the user set location Announces
 	 * download via balloon if succesful
 	 * 
 	 * @param serie
 	 *            Current serie the best torrent belongs to
 	 * @throws Exception
 	 */
 	private void downloadBest(TedSerie serie) throws Exception 
 	{
 		foundTorrent = false;
 
 		if (this.bestTorrentUrl != null)
 		{
 			int season = serie.getCurrentSeason();
 			int episode = serie.getCurrentEpisode();
 
 			// download torrent
 			String fileName;
 
 			fileName = serie.getName() + "-s" + season + "_e" + episode; //$NON-NLS-1$ //$NON-NLS-2$
 
 			try 
 			{
 				TedIO.getInstance().downloadTorrent(this.bestTorrentUrl, fileName);
 			} 
 			catch (Exception e)
 			{
 				throw e;
 			}
 
 			this.foundTorrents++;
 
 			// announce to user and update serie
 			// everything went okay, notify user and save the changes
 			String message;
 
 			message = Lang.getString("TedParser.BalloonFoundTorrent1") + " "
 					+ season + " "
 					+ Lang.getString("TedParser.BalloonFoundTorrent2") + " "
 					+ episode + " ";
 
 			if (serie.isDoubleEpisode())
 			{
 				message += " & " + (episode + 1) + " ";
 			}
 
 			message += Lang.getString("TedParser.BalloonFoundTorrent3") + " " + serie.getName(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 
 			tMainDialog.displayHurray(
 				Lang.getString("TedParser.BallonFoundTorrentHeader"), message, "Download succesful"); //$NON-NLS-1$ //$NON-NLS-2$
 
 			// increase the season/episode based on the schedule
 			serie.goToNextEpisode();
 
 			// save the shows
 			tMainDialog.saveShows();
 
 			// Write log message
 			TedLog.simpleLog(generateLogMessage());
 
 			if (serie.isCheck())
 			{
 				// search for more torrents if the status is check or pause
 				parseFeeds(serie, tMainDialog);
 			}
 		} 
 		else
 		{
 			TedLog.simpleLog(generateLogMessage());
 			serie.setStatusString(generateOverviewMessage(), tMainDialog);
 		}
 
 		this.bestTorrentUrl = null;
 	}
 
 	/**
 	 * Downloads the currently best torrent to the userset location of this
 	 * daily show Announces download via balloon if succesful
 	 * 
 	 * @param serie
 	 *            a TedDailySerie
 	 * @param dd
 	 *            the DailyDate containing the download url
 	 * @throws Exception
 	 */
 	private void downloadBest(TedDailySerie serie, DailyDate dd)
 			throws Exception {
 		foundTorrent = false;
 		if (this.bestTorrentUrl != null)
 		{
 			// download torrent
 			String fileName;
 
 			// make filename containing the date
 			fileName = serie.getName() + "-" + dd.getYear() + "_"
 					+ (dd.getMonth() + 1) + "_" + dd.getDay();
 
 			try 
 			{
 				TedIO.getInstance().downloadTorrent(this.bestTorrentUrl, fileName);
 			} 
 			catch (Exception e)
 			{
 				throw e;
 			}
 
 			this.foundTorrents++;
 
 			// announce to user and update serie
 			// everything went okay, notify user and save the changes
 			String message;
 
 			message = Lang.getString("TedParser.BalloonFoundDailyTorrent1")
 					+ " "
 					+ dd.getFormattedEpisodeDate()
 					+ " "
 					+ Lang.getString("TedParser.BalloonFoundDailyTorrent2") + " " + serie.getName(); //$NON-NLS-1$
 
 			tMainDialog
 					.displayHurray(
 							Lang
 									.getString("TedParser.BallonFoundTorrentHeader"), message, "Download succesful"); //$NON-NLS-1$ //$NON-NLS-2$
 
 			foundTorrent = true;
 
 			// save the shows
 			tMainDialog.saveShows();
 		} else
 		{
 			serie.setStatusString(generateOverviewMessage(), tMainDialog);
 		}
 
 		this.bestTorrentUrl = null;
 	}
 
 	private String generateOverviewMessage()
 	{
 		String message = "";
 
 		if (this.foundTorrents == 1)
 		{
 			message = Lang.getString("TedParser.StatusFoundTorrentForPrevious");
 		} else if (this.foundTorrents > 1) 
 		{
 			message = Lang
 					.getString("TedParser.StatusFoundTorrentsForMultipleEpisodes1")
 					+ " "
 					+ this.foundTorrents
 					+ " "
 					+ Lang
 							.getString("TedParser.StatusFoundTorrentsForMultipleEpisodes2");
 		} else if (this.checkedTorrents == 1)
 		{
 			message = Lang
 					.getString("TedParser.StatusFound1ButDidNotDownload1")
 					+ " "
 					+ this.checkedTorrents
 					+ " "
 					+ Lang
 							.getString("TedParser.StatusFound1ButDidNotDownload2");
 		} else if (this.checkedTorrents > 1)
 		{
 			message = Lang.getString("TedParser.StatusFoundButDidNotDownload1")
 					+ " " + this.checkedTorrents + " "
 					+ Lang.getString("TedParser.StatusFoundButDidNotDownload2");
 		} else
 		{
 			message = Lang.getString("TedParser.StatusFoundNoTorrents");
 		}
 
 		return message;
 	}
 
 	private String generateLogMessage()
 	{
 		String logMessage = parseLogInfo[0][0] + "\n---------------\n";
 
 		if (checkedTorrents > 0)
 		{
 			logMessage += Lang.getString("TedLog.Found") + "\n";
 			for (int i = 1; i < parseLogInfo.length; i++)
 			{
 				if (parseLogInfo[i][0] != null)
 					logMessage += i + ". " + parseLogInfo[i][0] + "\n";
 			}
 			logMessage += "---------------\n";
 
 			logMessage += Lang.getString("TedLog.Rejected") + "\n";
 			for (int i = 1; i < parseLogInfo.length; i++)
 			{
 				if (parseLogInfo[i][0] != null
 						&& parseLogInfo[i][1] != Lang
 								.getString("TedLog.BestTorrent"))
 					logMessage += i + ". " + parseLogInfo[i][1] + "\n";
 			}
 			logMessage += "---------------\n";
 
 			boolean foundBest = false;
 			logMessage += Lang.getString("TedLog.Downloaded") + "\n";
 			for (int i = 1; i < parseLogInfo.length; i++)
 			{
 				if (parseLogInfo[i][1] == Lang.getString("TedLog.BestTorrent"))
 				{
 					logMessage += i + ". " + parseLogInfo[i][0] + "\n";
 					foundBest = true;
 				}
 			}
 
 			if (!foundBest)
 			{
 				logMessage += Lang.getString("TedLog.NoneDownloaded") + "\n";
 			}
 		} else
 		{
 			logMessage += Lang.getString("TedLog.Found") + "\n";
 			logMessage += Lang.getString("TedLog.NoneFound") + "\n";
 		}
 
 		logMessage += "\n------------------------------------\n";
 
 		return logMessage;
 	}
 
 	private int findPlaceInLog(String url)
 	{
 		for (int j = 0; j < this.totalNumberOfFeedItems + 1; j++)
 		{
 			if (parseLogInfo[j][0].equals(url))
 			{
 				return j;
 			}
 		}
 
 		return 0;
 	}
 
 	/****************************************************
 	 * GETTERS & SETTERS
 	 ****************************************************/
 
 	/**
 	 * Returns vector with all available seasons and episodes from the feeds of
 	 * the serie
 	 * 
 	 * @param serie
 	 */
 	public Vector getItems(TedSerie serie)
 	{
 		Vector items = new Vector();
 
 		SyndFeedInput sfi = new SyndFeedInput();
 
 		Vector<TedSerieFeed> feeds = serie.getFeeds();
 		TedSerieFeed currentFeed;
 
 		for (int i = 0; i < feeds.size() && !stopGetItems; i++)
 		{
 			currentFeed = (TedSerieFeed) feeds.get(i);
 			try {
 				URLConnection urlc;
 				URL feedURL = new URL(currentFeed.getUrl());
 
 				TedLog.debug("Loading feed from " + serie.getName() + " URL: "
 						+ feedURL);
 
 				SyndFeed feed = sfi.build(new XmlReader(feedURL));
 
 				List<SyndEntry> entries = feed.getEntries();
 
 				for (int j = entries.size() - 1; j >= 0 && !stopGetItems; j--)
 				{
 					SyndEntry item = (SyndEntry) entries.get(j);
 
 					SeasonEpisode se = null;
 					DailyDate dd = null;
 					if (!serie.isDaily)
 						se = this.getSeasonEpisodeFromItem(item, serie, feed
 								.getTitle(), true);
 					else
 						dd = this.getDailyDateFromItem(item);
 
 					if (se != null)
 					{
 						items.add(se);
 					}
 
 					if (dd != null)
 					{
 						items.add(dd);
 					}
 					// }
 				}
 
 				// clear memory
 				entries = null;
 			} catch (MalformedURLException e)
 			{
 				String message = Lang.getString("TedParser.ErrorNotValidURL1") + " " + serie.getName() + ". " + Lang.getString("TedParser.ErrorNotValidURL2") + //$NON-NLS-1$ //$NON-NLS-2$
 						"\n" + currentFeed.getUrl();
 				; //$NON-NLS-1$
 			} catch (IOException e)
 			{
 				String message = Lang.getString("TedParser.Error404Feed1")
 						+ " "
 						+ serie.getName()
 						+ "\n" + Lang.getString("TedParser.Error404Feed2") + " " + currentFeed.getUrl(); //$NON-NLS-1$ //$NON-NLS-2$
 			} catch (NullPointerException e)
 			{
 				// no items in the feed
 				String message = Lang.getString("TedParser.NoFeedItems1")
 						+ serie.getName()
 						+ "\n" + Lang.getString("TedParser.NoFeedItems1") + currentFeed.getUrl(); //$NON-NLS-1$ //$NON-NLS-2$
 			} catch (Exception e)
 			{
 			}
 
 		}
 
 		stopGetItems = false;
 
 		if (serie.isDaily)
 			return removeDoublesDD(items);
 		else
 			return removeDoublesSE(items);
 
 	}
 
 	private DailyDate getDailyDateFromItem(SyndEntry item)
 	{
 		DailyDate dd = new DailyDate();
 
 		dd.setPublishDate(this.tPDateChecker.getItemDate(item));
 
 		String title = item.getTitle().toString().toLowerCase();
 
 		// Match title on the following format:
 		// #-#-# (where # stands for one or more integers)
 		// (#.#.# and # # # are also checked for)
 		String sMatch = "((\\d)+)(-|\\.| )((\\d)+)(-|\\.| )((\\d)+)";
 		Pattern pDate = Pattern.compile(sMatch);
 		Matcher mDate = pDate.matcher(title);
 
 		if (mDate.find())
 		{
 			String match = mDate.group();
 			String split[] = match.split("(-|\\.| )");
 
 			// Check for different patterns
 			// If value > 999 (e.g. > 2000) that's the year
 			int firstItem = Integer.parseInt(split[0]);
 			int secondItem = Integer.parseInt(split[1]);
 			int thirdItem = Integer.parseInt(split[2]);
 
 			// yyyy-mm-dd
 			if (firstItem > 999)
 			{
 				dd.setYear(firstItem);
 				dd.setMonth(secondItem - 1);
 				dd.setDay(thirdItem);
 			} 
 			else
 			{
 				if (thirdItem < 999)
 					thirdItem += 2000;
 
 				dd.setYear(thirdItem);
 
 				// dd-mm-yyyy
 				// dd-mm-yy
 				if (firstItem > 12)
 				{
 					dd.setDay(firstItem);
 					dd.setMonth(secondItem - 1);
 				}
 				// mm-dd-yyyy
 				// mm-dd-yy
 				else if (secondItem > 12)
 				{
 					dd.setDay(secondItem);
 					dd.setMonth(firstItem - 1);
 				} else
 				{
 					// it's unclear if the first or the second item
 					// represents the day (or month).
 					// try to find out by looking at the publish date
 					// of the torrent.
 					DailyDate monthFirstDate = new DailyDate(secondItem,
 															 firstItem - 1, 
 															 thirdItem);
 					
 					DailyDate dayFirstDate = new DailyDate(firstItem,
 														   secondItem - 1, 
 														   thirdItem);
 					
 					long monthMillis = monthFirstDate.getDate().getTimeInMillis();
 					long dayMillis = dayFirstDate.getDate().getTimeInMillis();
 					long pdMillis = dd.getPublishDate().getTime();
 					
 					boolean monthFirst = false;
 
 					// both have a date larger than publish date
 					if (monthMillis > pdMillis && dayMillis > pdMillis)
 					{
 						// none of the dates is valid
 						dd.setYear(0);
 					}
 					// month date is larger than publish date
 					else if (monthMillis > pdMillis)
 					{
 						monthFirst = false;
 					}
 					// day date is larger than publish date
 					else if (dayMillis > pdMillis)
 					{
 						monthFirst = true;
 					}
 					// month date is nearest to the publsh date
 					// so first item is probably the month
 					else if (pdMillis - monthMillis < pdMillis - dayMillis)
 					{
 						monthFirst = true;
 					}
 
 					// set date, year was already set
 					if (monthFirst) 
 					{
 						dd.setDay(secondItem);
 						dd.setMonth(firstItem - 1);
 					} else 
 					{
 						dd.setDay(firstItem);
 						dd.setMonth(secondItem - 1);
 					}
 				}
 			}
 		} 
 		else
 		{
 			// Start looking for another pattern.
 			// Jan 1st 2008
 			sMatch = "((\\w)+)\\p{Blank}((\\d)+)((\\w)+)\\p{Blank}((\\d)+)";
 			pDate = Pattern.compile(sMatch);
 			mDate = pDate.matcher(title);
 
 			if (mDate.find())
 			{
 				String match = mDate.group();
 				String split[] = match.split("\\p{Blank}");
 
 				int month = this.tPDateChecker.getMonth(split[0]);
 
 				// We've found the month
 				if (month != -1)
 				{
 					String dayString = split[1];
 					int day = -1;
 
 					// If the string has size 1 or 2
 					if (dayString.length() <= 2 && dayString.length() != 0)
 					{
 						// only integer day available in input.
 						day = Integer.parseInt(dayString);
 					}
 					// Or when the string has size 3 or 4
 					else if (dayString.length() <= 4)
 					{
 						// remove 'st', 'nd', 'th' from name.
 						day = Integer.parseInt(split[1].substring(0, (split[1]
 								.length() - 2)));
 					}
 					else
 					{
 						// not well formed input.
 						return null;
 					}
 
 					// get year
 					int year = Integer.parseInt(split[2]);
 
 					// small check if we've a correct year
 					if (year > 999)
 					{
 						dd.setYear(year);
 						dd.setMonth(month);
 						dd.setDay(day);
 					}
 				}
 			}
 
 		}
 
 		if (dd.getYear() != 0)
 			return dd;
 		else
 			return null;
 	}
 
 	private SeasonEpisode getSeasonEpisodeFromItem(SyndEntry item,
 			TedSerie serie, String text, boolean checkLatest)
 	{
 		SeasonEpisode se = new SeasonEpisode();
 
 		se.setPublishDate(this.tPDateChecker.getItemDate(item));
 
 		String sTitle = item.getTitle().toString();
 		String sTitle_lower = sTitle.toLowerCase();
 		String eTitle = sTitle_lower;
 		String xTitle = sTitle_lower;
 
 		String sMatch = "(season|s)(\\W)*"; //$NON-NLS-1$
 		// TODO: ep with a '.' don't work?
 		String eMatch = "(episode|ep|e)(\\W)*"; //$NON-NLS-1$
 		String xMatch = "x"; //$NON-NLS-1$
 
 		// make 2 patterns, one to match season and one to match episode
 		Pattern pSeason = Pattern.compile(sMatch + "(\\d)+"); //$NON-NLS-1$
 		Pattern pEpisode = Pattern.compile(eMatch + "(\\d)+"); //$NON-NLS-1$
 		Pattern pX = Pattern.compile("(\\d)+" + xMatch + "(\\d)+"); //$NON-NLS-1$ //$NON-NLS-2$
 		Pattern pNum = Pattern.compile("(\\d){3,4}"); //$NON-NLS-1$
 
 		// match the patterns to the title
 		Matcher mSeason = pSeason.matcher(sTitle_lower);
 		Matcher mEpisode = pEpisode.matcher(eTitle);
 		Matcher mX = pX.matcher(xTitle);
 		Matcher mNum = pNum.matcher(xTitle);
 
 		// if they both are found in the title
 		if (mSeason.find() && mEpisode.find())
 		{
 			// get the substrings that matched
 			String sSeason = mSeason.group();
 			String sEpisode = mEpisode.group();
 
 			// replace all found episode strings or seasonstrings with one
 			// letter so we dont confuse them
 			// sSeason.replaceAll(sMatch, "s");
 			// sEpisode.replaceAll(eMatch, "e");
 
 			// split the title to get the integers
 			String[] splitSeason = sSeason.split(sMatch);
 			String[] splitEpisode = sEpisode.split(eMatch);
 
 			// parse the integers
 			try {
 				se.setSeason(Integer.parseInt(splitSeason[1]));
 				se.setEpisode(Integer.parseInt(splitEpisode[1]));
 			} catch (Exception e)
 			{
 				TedLog.error(e, Lang.getString("TedParser.ErrorParsing1")
 						+ " (" + splitSeason[1] + ") "
 						+ Lang.getString("TedParser.ErrorParsing2")
 						+ " (" + splitEpisode[1] + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 				return null;
 			}
 		} else if (mX.find())
 		{
 			String x = mX.group();
 
 			String[] splitX = x.split(xMatch);
 
 			try {
 				se.setSeason(Integer.parseInt(splitX[0]));
 				se.setEpisode(Integer.parseInt(splitX[1]));
 			} catch (Exception e)
 			{
 				TedLog.error(e, Lang.getString("TedParser.ErrorParsing1")
 						+ " (" + splitX[0] + ") "
 						+ Lang.getString("TedParser.ErrorParsing2")
 						+ " (" + splitX[2] + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 				return null;
 			}
 		} else if (mNum.find() && !checkLatest)
 		{
 			String x = mNum.group();
 			if (x.length() == 3)
 			{
 				// first number is season, following two are episode
 				se.setSeason(Integer.parseInt(x.substring(0, 1)));
 				se.setEpisode(Integer.parseInt(x.substring(1, 3)));
 
 			} else if (x.length() == 4)
 			{
 				// first two numbers are season, second two numbers episode
 				se.setSeason(Integer.parseInt(x.substring(0, 2)));
 				se.setEpisode(Integer.parseInt(x.substring(2, 4)));
 			}
 
 		} else
 		{
 			// nothing found
 			return null;
 		}
 
 		if (!(serie.getName().equals("" + se.getSeason()))
 				&& se.getSeason() < 50 && se.getEpisode() < 50)
 		{
 			return se;
 		} else
 		{
 			return null;
 		}
 	}
 
 	/**
 	 * Returns a vector where all the doubles from the parameter vector are
 	 * removed
 	 * 
 	 * @param seasonEpisodes
 	 * @return
 	 */
 	private Vector<SeasonEpisode> removeDoublesSE(
 			Vector<SeasonEpisode> seasonEpisodes)
 			{
 		// sort the seasons and episodes in ascending order
 		Collections.sort(seasonEpisodes);
 
 		// create new empty vector
 		Vector<SeasonEpisode> singleVector = new Vector<SeasonEpisode>();
 
 		if (seasonEpisodes.size() > 0)
 		{
 			// get the first
 			SeasonEpisode currentSE = seasonEpisodes.get(0);
 
 			currentSE.setQuality(1);
 
 			// loop through all the season and episodes to remove any doubles
 			for (int i = 1; i < seasonEpisodes.size(); i++)
 			{
 				// get the next
 				SeasonEpisode se = seasonEpisodes.get(i);
 
 				// if it is the same as the current
 				if (se.compareTo(currentSE) == 0)
 				{
 					// add one to the quality
 					currentSE.setQuality(currentSE.getQuality() + 1);
 
 					// set the correct publishdate
 					if (se.getPublishDate().compareTo(
 							currentSE.getPublishDate()) < 0)
 					{
 						currentSE.setPublishDate(se.getPublishDate());
 					}
 				} else 
 				{
 					// new current, so add the previous current to the vector
 					singleVector.add(currentSE);
 					currentSE = se;
 					currentSE.setQuality(1);
 				}
 			}
 
 			// add the last to the vector
 			singleVector.add(currentSE);
 		}
 
 		return singleVector;
 	}
 
 	private Vector<DailyDate> removeDoublesDD(Vector<DailyDate> dailyDates)
 	{
 		Collections.sort(dailyDates);
 
 		Vector<DailyDate> singleVector = new Vector<DailyDate>();
 
 		if (dailyDates.size() > 0)
 		{
 			DailyDate currentSE = dailyDates.get(0);
 
 			currentSE.setQuality(1);
 
 			for (int i = 1; i < dailyDates.size(); i++)
 			{
 				DailyDate se = dailyDates.get(i);
 
 				if (se.compareTo(currentSE) == 0)
 				{
 					currentSE.setQuality(currentSE.getQuality() + 1);
 
 					if (se.getPublishDate().compareTo(
 							currentSE.getPublishDate()) < 0)
 					{
 						currentSE.setPublishDate(se.getPublishDate());
 					}
 				} else {
 					singleVector.add(currentSE);
 					currentSE = se;
 					currentSE.setQuality(1);
 				}
 
 			}
 
 			// add the last to the vector
 			singleVector.add(currentSE);
 		}
 
 		return singleVector;
 	}
 
 	/**
 	 * Interrupt the GetItems() function
 	 */
 	public void stopGetItems()
 	{
 		stopGetItems = true;
 	}
 	
 	private int getMininovaDescriptionSeeders(SyndEntry rssItem)
 	{
 		// See if there is a description.
 		SyndContent rssContent = rssItem.getDescription();
 		if (rssContent == null)
 		{
 			// No description available.
 			return -1;
 		}
 		
 		// Retrieve the description.
 		String description = rssContent.getValue().toLowerCase();
 		
 		// Search for the seeders in the description.
 		Pattern seedersPattern = Pattern.compile("((-*)(\\d+))(.+)(seeds)");
 		Matcher seedersMatcher = seedersPattern.matcher(description);
 		
 		// First see if the seeders can be found in the description.
 		int nrOfSeeders = 0;
 		if (seedersMatcher.find())
 		{
 			// Retrieve seeders.
 			// Group 0 is the entire matched string. Group 1 is the number of seeders.
 			String matchedString = seedersMatcher.group(1);
 			
 			try
 			{
 				// Retrieve the number from the string.
 				nrOfSeeders = Integer.parseInt(matchedString);
 			}
 			catch (Exception e)
 			{
 				// Return if it has failed.
 				return -1;
 			}
 			
 			if (nrOfSeeders < 0)
 			{
 				// Seeders are not available in the description.
 				return -1;
 			}				
 		}
 		else
 		{
 			// Seeders not found.
 			return -1;
 		}
 				
 		return nrOfSeeders;
 	}
 	
 	private int getBtJunkieDescriptionSeeders(String rssItemTitle)
 	{
 		if (rssItemTitle.equals(""))
 		{
 			return -1;
 		}
 		
 		Pattern seedersPattern = Pattern.compile("(\\[(\\d+)/(\\d+)\\])");
 		Matcher seedersMatcher = seedersPattern.matcher(rssItemTitle);
 		
 		int nrOfSeeders = 0;
 		if (seedersMatcher.find())
 		{
 			String matchedString = seedersMatcher.group(2);			
 			
 			try
 			{
 				// Retrieve the number from the string.
 				nrOfSeeders = Integer.parseInt(matchedString);
 			}
 			catch (Exception e)
 			{
 				// Return if it has failed.
 				return -1;
 			}
 			
 			if (nrOfSeeders < 0)
 			{
 				// Seeders are not available in the description.
 				return -1;
 			}				
 		}
 		else
 		{
 			// Seeders not found.
 			return -1;
 		}
 				
 		return nrOfSeeders;
 	}
 	
 	private int getRssDescriptionSize(SyndEntry rssItem)
 	{
 		// See if there is a description.
 		SyndContent rssContent = rssItem.getDescription();
 		if (rssContent == null)
 		{
 			// No description available.
 			return 0;
 		}
 		
 		// Retrieve the description.
 		String description = rssContent.getValue().toLowerCase();
 				
 		Pattern sizePattern = Pattern.compile("(size: )(\\d+(.\\d+)*)(.*)((megabyte|mb)|(gigabyte|gb))");
 		Matcher sizeMatcher = sizePattern.matcher(description);
 	
 		// Find the file size in the description.
 		int fileSize = 0; 
 		if (sizeMatcher.find())
 		{
 			// Retrieve the file size.
 			String fileSizeMatch = sizeMatcher.group(2);
 			
 			// If the file size is in GB we've to multiply it by 1024
 			// to get it in MB.
 			String kindOfBytes = sizeMatcher.group(5);
 			
 			// Use this temporary double to store sizes of 1.5gb
 			// Cast it to an int before returning the size (after muliplying it by 1024).
 			double fileSizeDouble = 0;
 			try
 			{
 				// Retrieve the number from the string.
 				fileSizeDouble = Double.parseDouble(fileSizeMatch);
 			}
 			catch (Exception e)
 			{
 				// Return if it has failed.
 				return 0;
 			}
 			
 			if (kindOfBytes.contains("gigabyte") || kindOfBytes.equals("gb"))
 			{
 				fileSizeDouble *= 1024;
 			}
 			
 			fileSize = (int) fileSizeDouble;
 		}
 		else
 		{
 			// Size not found
 			return 0;
 		}
 				
 		return fileSize;
 	}
 }
