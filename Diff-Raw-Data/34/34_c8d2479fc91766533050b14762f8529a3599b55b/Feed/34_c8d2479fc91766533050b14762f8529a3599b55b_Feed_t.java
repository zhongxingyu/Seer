 package newsrack.archiver;
 
 import newsrack.GlobalConstants;
 import newsrack.util.IOUtils;
 import newsrack.util.StringUtils;
 import newsrack.database.DB_Interface;
 import newsrack.database.NewsItem;
 import newsrack.util.URLCanonicalizer;
 
 import java.lang.String;
 import java.net.URL;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Date;
 import java.io.File;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.io.FileOutputStream;
 
 import com.sun.syndication.io.WireFeedInput;
 import com.sun.syndication.io.XmlReader;
 import com.sun.syndication.feed.WireFeed;
 import com.sun.syndication.feed.rss.Channel;
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.sun.syndication.feed.synd.SyndFeedImpl;
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndContent;
 import com.sun.syndication.feed.synd.SyndContentImpl;
 
 import org.apache.commons.digester.*;
 import org.apache.commons.digester.xmlrules.*;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class Feed implements java.io.Serializable
 {
 // ############### STATIC FIELDS AND METHODS ############
    private static Log _log = LogFactory.getLog(Feed.class);
 
 		// number of retries for connecting to a server
 	public static final short NUM_RETRIES = 2;
 
    // NOTE: For the same _feedTag, there can be multiple Source objects.
    // because different users might refer to the feeds using different tags and/or names.
 
 	private static final HashMap<URL, File> _rssFeedCache = new HashMap<URL, File>();
 
       // Sets that specify for which feeds / domains, the cached text for
       // news articles cannot be displayed
    private static Set<String> _feedsWithoutCachedTextDisplay;
    private static Set<String> _domainsWithoutCachedTextDisplay;
 
 	private static DB_Interface _db; 		// Interface to the underlying database -- just a cached value
 	private static String _newsArchiveDir;	// Global news archive where all downloaded news is archived
 
       // Default value for MAX_DESC_SIZE
    private static int MAX_DESC_SIZE = 2048;
 
       // Read in a file that specifies for which feeds / domains,
       // cached text cannot be displayed!
    public static void init(DB_Interface db)
    {
 		_db = db;
 		_newsArchiveDir = GlobalConstants.getGlobalNewsArchive();
       MAX_DESC_SIZE = Integer.parseInt(GlobalConstants.getProperty("rss.max_description_size"));
       readCachedTextDisplayRules();
    }
 
 	public static Feed getFeed(String feedUrl, String tag, String feedName)
 	{
 		Feed f = new Feed(_db.getUniqueFeedTag(feedUrl, tag, feedName), feedName, feedUrl);
 		f._id = Long.parseLong(f._feedTag.substring(0, f._feedTag.indexOf('.')));
 		f.setCachedTextDisplayFlag();
 			// @TODO fix this ... by default, setting cacheable flag to true
 		f._cacheableFlag = true;
 
 		return f;
 	}
 
 	public static Collection<Feed> getActiveFeeds()
 	{
 		return _db.getAllActiveFeeds();
 	}
 
    /**
     * public because the xml digester requires the methods to be public
     */
    public static void recordFeed(String feed)
    {
          // Normalize URL
       feed = normalizeURL(feed);
       _feedsWithoutCachedTextDisplay.add(feed);
      _log.debug("Got " + feed);
    }
 
    /**
     * public because the xml digester requires the methods to be public
     */
    public static void recordDomain(String domain)
    {
          // Normalize URL
       domain = normalizeURL(domain);
       _domainsWithoutCachedTextDisplay.add(domain);
      _log.debug("Got " + domain);
    }
 
    private static void readCachedTextDisplayRules()
    {
          // Allocate tables
       _feedsWithoutCachedTextDisplay   = new HashSet<String>();
       _domainsWithoutCachedTextDisplay = new HashSet<String>();
 
          // Read the file
 		String    rulesFile     = "feeds.without.cachedtext.display.digester.rules.xml";
 		URL       digesterRules = Feed.class.getClassLoader().getResource(rulesFile);
 		boolean   info = _log.isInfoEnabled();
 		if (info) _log.info("rules file name is  " + rulesFile + "\nrules file URL  is  " + digesterRules);
       try {
 		   URL  inputFile = Source.class.getClassLoader().getResource("cachedtext.nodisplay.xml");
          File input     = new File(inputFile.toURI());
 			if (info) _log.info("rules - " + digesterRules + "\ninput - " + inputFile);
 			if (input.exists()) {
 				Digester d = DigesterLoader.createDigester(digesterRules);
                // The digester API needs an object on the stack even for calling
                // static methods .. so, just adding a dummy method there!
             d.push(new Feed());
             d.parse(input);
 			}
 			else {
 				if (info) _log.info("Did not find the file " + inputFile);
 			}
       }
 		catch (Exception exc) {
          exc.printStackTrace();
       }
    }
 
    public static void refreshCachedTextDisplayRules()
    {
 		readCachedTextDisplayRules();
 		List<Feed> allFeeds = _db.getAllFeeds();
 		for (Feed f: allFeeds) {
 			f.setCachedTextDisplayFlag();
 			_db.updateFeedCacheability(f);
 		}
    }
 
    private static String normalizeURL(String url)
    {
          // Make sure all urls begin with "http://"
       if (!url.startsWith("http://"))
          url = "http://" + url;
 
          // If there is a "http://www." at the beginning,
          // get rid of the "www."
       if (url.startsWith("http://www."))
 			url = "http://" + url.substring(11);
 
       return url;
    }
 
    /**
     * Process the description element to make sure it
     * is restricted to under 'MAX_DESC_SIZE' ... when doing that
     * strip the text of HTML tags ...
     */
    private static String processDescription(SyndEntry se)
    {
       String desc    = (se.getDescription() == null) ? "" : se.getDescription().getValue();
       int    descLen = desc.length();
       if (descLen > MAX_DESC_SIZE) {
          SyndContent newDesc = new SyndContentImpl();
          newDesc.setType("text/plain");
          try {
             StringBuffer sbText = HTMLFilter.getFilteredTextFromString(desc);
             int          k      = sbText.lastIndexOf(" ", MAX_DESC_SIZE - 5);
             String       ndv    = StringUtils.filterForXMLOutput(sbText.substring(0, k)) + " ...";
             newDesc.setValue(ndv);
             if (_log.isDebugEnabled()) {
                _log.debug("#### ORIG - " + desc);
                _log.debug("#### Processed - " + sbText);
                _log.debug("#### k - " + k); 
                _log.debug("#### NEW - " + ndv);
             }
             if (_log.isInfoEnabled()) _log.info("### Shortened description from " + descLen + " characters to " + MAX_DESC_SIZE + " charactors");
          }
          catch (Exception e) {
             newDesc.setValue(se.getTitle());
          }
          se.setDescription(newDesc);
       }
       return desc;
    }
 
 // ############### NON-STATIC FIELDS AND METHODS ############
 	/* 256 bytes for most => 4 feeds per KB => 4000 feeds per MB RAM */
 
 	public Long   _id;		 // System-assigned unique id (int) for this feed
 	public String _feedTag;	 // System-assigned unique id (String) for this feed
 		// INVARIANT: "_feedTag" has "_id" at the beginning!
 	public String _feedUrl;	 // URL of the feed, if any exists.  null if none exists
 	public String _feedName; // Display name for the feed
 
    private boolean _cacheableFlag;
    private boolean _cachedTextDisplayFlag;
 
 	public  Feed() { } /* Default constructor */
 
 	public Feed(Long key, String tag, String name, String url)
 	{
 		_id       = key;
 		_feedTag  = tag;
 		_feedUrl  = url;
 		_feedName = name.trim();	/* trim white space at the beginning and end */
 	}
 
 	public Feed(String tag, String name, String url)
 	{
 		_feedTag  = tag;
 		_feedUrl  = url;
 		_feedName = name.trim();	/* trim white space at the beginning and end */
 	}
 
 	public boolean equals(Object o) { return (o != null) && (o instanceof Feed) && _feedUrl.equals(((Feed)o)._feedUrl); }
 
 	public int hashCode() { return _feedUrl.hashCode(); }
 
 	public Long    getKey()    { return _id; }
 	public String  getTag()    { return _feedTag; }
 	public String  getName()   { return _feedName; }
 	public String  getUrl()    { return _feedUrl; }
 	public boolean getCacheableFlag() { return _cacheableFlag; }
 	public boolean getCachedTextDisplayFlag() { return _cachedTextDisplayFlag; }
 
 	public void setCacheableFlag(boolean flag) { _cacheableFlag = flag; }
 	public void setShowCachedTextDisplayFlag(boolean flag) { _cachedTextDisplayFlag = flag; }
 
    /** 
     * For this news source, decide whether the cached news text has to be displayed or not 
     */
    private void setCachedTextDisplayFlag()
    {
          // Global default
       if (GlobalConstants.isFalse("cached.links.display")) {
          _cachedTextDisplayFlag = false;
          return;
       }
 
       String f = normalizeURL(_feedUrl);
       if (_log.isDebugEnabled())_log.debug("Checking feed turn off for: " + f);
       if (_feedsWithoutCachedTextDisplay.contains(f)) {
          _cachedTextDisplayFlag = false;
          if (_log.isInfoEnabled())_log.info("Turning off cached text display for (feed) " + _feedUrl);
       }
       else {
             // Get the domain name!
          f = f.substring(0, f.indexOf("/", 7));
          if (_log.isDebugEnabled()) _log.debug("Checking domain turn off for: " + f);
          if (_domainsWithoutCachedTextDisplay.contains(f)) {
             _cachedTextDisplayFlag = false;
             if (_log.isInfoEnabled())_log.info("Turning off cached text display for (domain) " + _feedUrl);
          }
       }
    }
 
 	public Collection<NewsItem> getDownloadedNews()
 	{
 		return _db.getDownloadedNews(this);
 	}
 
 	/**
 	 * Read the feed, store it locally, and download all the news items referenced in the feed.
 	 */
 	public void download() throws Exception
 	{
 		if (_log.isInfoEnabled()) _log.info("reading rss feed " + _feedUrl);
 
 			// 1. Download the feed and write it to the feed file in the output dir
 			//    or if it has previously been downloaded, get access to the file
 			//    This block is synchronized to prevent multiple threads
 	 		//    from downloading the same feed at the same time!
 		boolean downloaded = true;
 		URL     u          = new URL(_feedUrl);
 		String rssFeedBase = "rss." + StringUtils.getBaseFileName(u.toString()); // Add "rss." prefix to prevent clash with "index.xml" index file names
 		File   rssFeedFile;
 		synchronized (this) {
 			InputStream is = IOUtils.getURLInputStream(u, NUM_RETRIES); // FIXME: Note that info about HTTP encoding is lost here!
 			if (is == null) {
 					// We will get a null value ONLY IF the feed has not changed since the previous download.
 				rssFeedFile = _rssFeedCache.get(u);
 				if (rssFeedFile == null) {
 					_log.error("Could not open RSS url, and there is no local cached copy either");
 					return;
 				}
 				downloaded = false;
 			}
 			else {
 					// Download the feed into a temporary file and record the location in the cache
 					// NOTE: 
 					// 1. Before we download and parse the feed, we won't know its final location!
 					// 2. We cannot use the "WireFeedOutput" to output the feeds directly to the location
 					//    after parsing, because if the input feeds do not conform to the standard
 					//    (Hindustan Times, Times Now), then, the output modules that are strict in
 					//    enforcing standards will fail!
 					// Hence the dance of storing in a temporary file and moving it to its final location.
 				rssFeedFile = _db.getTempFilePath(rssFeedBase);
 				FileOutputStream fos = new FileOutputStream(rssFeedFile);
 				IOUtils.copyInputToOutput(is, fos);
 				fos.close();
 				is.close(); // close the HTTP connection AFTER building the feed
 
 					// FIXME: Why not just store the feed in the cache
 					// as opposed to just the file path of the cached feed??
 				_rssFeedCache.put(u, rssFeedFile);
 			}
 		}
 
 			// 2. Open the feed for parsing, parse it and build a wire feed and close it!
 		XmlReader r  = new XmlReader(rssFeedFile);
 		WireFeed  wf = null;
 		try {
 			wf = (new WireFeedInput()).build(r);
 		}
 		catch (com.sun.syndication.io.ParsingFeedException e) {
 			r.close();
 				// FIXME: Caught trailing character exception!
 				// This can happen for feeds like "The Telegraph"
 			_log.error("For feed " + _feedUrl + ", found trailing content ... getting rid of it and re-parsing!");
 
 				// Read the file content into a string, get rid of trailing content, and overwrite the file
 			IOUtils.writeFile(rssFeedFile, IOUtils.readFile(rssFeedFile).replaceAll("(?s)</rss>.*", "</rss>"));
 
 				// Retry building the wirefeed!
 			r  = new XmlReader(rssFeedFile);
 			wf = (new WireFeedInput()).build(r);
 		}
 
 			// 2b. Handle some special cases when the feed is a RSS feed
 			//     and it has a 'lastBuildDate' but no 'pubDate'.  This is because
 			//     when Rome builds a 'SyndFeed' object, it normalizes information 
 			//     and throws away the 'lastBuildDate' information! 
 			//     This is a problem for RSS 0.91, 0.92, 0.93, 0.94, and 2.0
 			// REFER http://wiki.java.net/bin/view/Javawsxml/Rome05DateMapping
 		Date   rssPubDate = null;
 		String wfType = wf.getFeedType();
 		String baseUrl = "";
 		if (wfType.startsWith("rss")) {
 				// Check if there is 'lastBuildDate' but no 'pubDate' (BBC feeds?)
 			Channel ch = (Channel)wf;
 			baseUrl = ch.getLink();
 			Date lbd = ch.getLastBuildDate();
 			Date pd  = ch.getPubDate();
 			if ((pd == null) && (lbd != null))
 				rssPubDate = lbd;
 			if (_log.isInfoEnabled()) {
 				_log.info("RSS pd  - " + pd);
 				_log.info("RSS lbd - " + lbd);
 			}
 		}
 
 			// 3. Convert the wire feed to a syndfeed!
 		SyndFeed f = new SyndFeedImpl(wf);
 		if (rssPubDate == null)
 			rssPubDate = f.getPublishedDate();
 		if (_log.isInfoEnabled()) _log.info("Publishing date : " + rssPubDate);
 		if (rssPubDate == null) {
 			if (_log.isErrorEnabled()) _log.error("ERROR: For feed " + _feedUrl + "; Publishing date : null .. using today's date");
 			rssPubDate = new Date();
 		}
 
 			// 4. Move the feed to its final location!
 		if (downloaded) {
 			File finalRssFeedFile = new File(_newsArchiveDir + File.separator 
 														+ _db.getArchiveDirForIndexFiles(this, rssPubDate) + File.separator + rssFeedBase);
 
 				// The rename will probably fail in the rare case when one thread has reached here and trying to move the file 
 				// whereas another thread has simultaneously opened the file in its temporary location (2. above) to build the feed
 				// FIXME: It is not worth the effort to try and prevent this .. simultaneous access to the same feed from multiple
 				// threads is expected to be really rare and even then, the rss feed file is being stored only for archival access 
 				// and it is unlikely that these archived feeds will ever be accessed!
 			if (rssFeedFile.renameTo(finalRssFeedFile)) {
 				_rssFeedCache.put(u, finalRssFeedFile);
 			}
 			else {
 				if (_log.isErrorEnabled()) _log.error("ERROR: For feed " + _feedUrl + "; Failed to rename " + rssFeedFile + " to " + finalRssFeedFile);
 			}
 		}
 
 			// 5. Inform the DB before news downloading  
 		_db.initializeNewsDownload(this, rssPubDate);
 
 			// 6. Process the news items in the feed
 		Iterator items = f.getEntries().iterator();
 		while (items.hasNext()) {
 				// 7. Process a news item
 			SyndEntry se = (SyndEntry)items.next();
 
 				// 7a. Try getting published date of the news item 
 				// If no item date, default is the pub date of the RSS feed
 			Date itemDate = se.getPublishedDate();
 			Date niDate   = (itemDate != null) ? itemDate: rssPubDate;
 
 				// 7b. Spit out some debug information
 			if (_log.isInfoEnabled()) {
 				StringBuffer sb = new StringBuffer();
 				sb.append("\ntitle     - " + se.getTitle());
 				sb.append("\nlink      - " + se.getLink());
 				sb.append("\nITEM date - " + itemDate);
 /**
 				sb.append("\nauthor    - " + se.getAuthor());
 //				sb.append("\nNI   date - " + niDate);
             processDescription(se);
 				if (se.getDescription() != null)
 					sb.append("\ndesc      - " + se.getDescription());
 				sb.append("\n--------------------");
 **/
 				_log.info(sb);
 			}
 
 				// 7c. Download the news item
 			if (se.getLink() != null) {
 					// For some bad RSS feeds (Doordarshan), there exist initial
 					// <item> objects that have all their entries set to null!
 					// Hence the above check!
 				NewsItem ni = downloadNewsItem(baseUrl, se, niDate);
 				if (ni != null) {
 					String title = se.getTitle();
 					String auth  = se.getAuthor();
 					String desc  = (se.getDescription() == null) ? null : se.getDescription().getValue();
 					ni.setTitle((title != null) ? title.trim() : null);
 					ni.setAuthor((auth != null) ? auth.trim() : null);
 					ni.setDescription((desc != null) ? desc.trim() : null);
 
 						// 7d. Record The news item with the DB
 					_db.recordDownloadedNewsItem(this, ni);
 				}
 			}
 		}
 
 			// 8. Inform the DB after news downloading  
 		_db.finalizeNewsDownload(this);
 	}
 
 	private NewsItem downloadNewsItem(String baseUrl, SyndEntry se,  Date date)
 	{
 	/* Download the nEws item identified by the URL 'u' and create
 	 * (1) local copy of the article, and 
 	 * (2) a filtered version of the same article 
 	 */
 
 		PrintWriter filtPw = null;
 		PrintWriter origPw = null;
 		NewsItem    ni     = null;
 		try {
 				// 1a. Find the url and attempt to bypass redirects and forwarding urls/scripts
          String origURL = URLCanonicalizer.cleanup(baseUrl, se.getLink());
 
 				// 1b. Canonicalize it so that we can catch duplicate urls more easily! 
 			String canonicalUrl = URLCanonicalizer.canonicalize(origURL);
 			if (_log.isInfoEnabled()) _log.info("URL :" + canonicalUrl);
 
 				// 2. Check if the article has already been downloaded previously
 			ni = _db.getNewsItemFromURL(canonicalUrl);
 			if (ni != null) {
 				if (_log.isInfoEnabled()) _log.info("PREVIOUSLY DOWNLOADED: FOUND AT " + ni.getLocalCopyPath());
 				return ni;
 			}
 
 				// 3. Else, create a new item.  NOTE: This won't be stored to the db yet!
 			ni = _db.createNewsItem(canonicalUrl, this, date);
 
             // IMPORTANT: For purposes of downloading, use the original unfiltered URL!
 				// 3a. Get appropriate output streams
 				// 3b. Filter the article using the HTML filter (while getting original text)
 				// 3c. Store the original text onto disk
 				// 3d. Close streams
 			filtPw = _db.getWriterForFilteredArticle(ni);
 			origPw = _db.getWriterForOrigArticle(ni);
 			try {
             if ((filtPw != null) && (origPw != null)) {
                boolean done = false;
                int numTries = 0;
                do {
                   numTries++;
 						HTMLFilter hf = new HTMLFilter(origURL, filtPw, true);
 						hf.run();
 						String origText = hf.getOrigHtml();
                      // Null implies there was an error downloading the url
                   if (origText != null) {
 							String newUrl = hf.getUrl();	// Record the "final" url after going through redirects!
 							if (!newUrl.equals(canonicalUrl))
 								_log.info("TEST: orig - " + canonicalUrl + "; new - " + newUrl);
                      origPw.println(origText);
                      done = true;
                   }
                   else {
                      _log.info("Error downloading from url: " + origURL + " Retrying (max 3 times) once more after 5 seconds!");
                      StringUtils.sleep(5);
                   }
                } while (!done && (numTries < 3));
             }
             else {
                _log.info("Ignoring! There already exists a downloaded file for url: " + origURL);
             }
 			}
 			catch (Exception e) {
 					// Delete the file for this article -- otherwise, it will
 					// trigger a false hit in the archive later on!
 				if (filtPw != null)
 					_db.deleteFilteredArticle(ni);
 				throw e;
 			}
 
 				// close the files
 			if (origPw != null) origPw.close();
 			if (filtPw != null) filtPw.close();
 
             // 4e. After a download, sleep for 1 second to prevent bombarding the 
             //     remote server with downloads
          StringUtils.sleep(1);
 
 				// 5. Create the news item and return it!
 			return ni;
 		}
 		catch (Exception e) {
 			if (origPw != null) origPw.close();
 			if (filtPw != null) filtPw.close();
 			if (_log.isInfoEnabled()) _log.info(" ... FAILED!");
 			_log.error("Exception downloading news item : " + se.getLink().trim(), e);
 
 			return null;
 		}
 	}
 }
