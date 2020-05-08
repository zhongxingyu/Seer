 /*---------------------------------------------------------------------------*\
   $Id$
   ---------------------------------------------------------------------------
   This software is released under a Berkeley-style license:
 
   Copyright (c) 2004 Brian M. Clapper. All rights reserved.
 
   Redistribution and use in source and binary forms are permitted provided
   that: (1) source distributions retain this entire copyright notice and
   comment; and (2) modifications made to the software are prominently
   mentioned, and a copy of the original software (or a pointer to its
   location) are included. The name of the author may not be used to endorse
   or promote products derived from this software without specific prior
   written permission.
 
   THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR IMPLIED
   WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF
   MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 
   Effectively, this means you can do what you want with the software except
   remove this notice or take advantage of the author's name. If you modify
   the software and redistribute your modified version, you must indicate that
   your version is a modification of the original, and you must provide either
   a pointer to or a copy of the original.
 \*---------------------------------------------------------------------------*/
 
 package org.clapper.curn;
 
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.File;
 import java.io.InputStream;
 import java.io.FileInputStream;
 import java.io.OutputStream;
 import java.io.FileOutputStream;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.Comparator;
 import java.util.TreeSet;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.Iterator;
 import java.util.Collection;
 import java.util.ResourceBundle;
 import java.util.zip.GZIPInputStream;
 
 import java.text.MessageFormat;
 
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.MalformedURLException;
 
 import org.clapper.curn.parser.RSSParserFactory;
 import org.clapper.curn.parser.RSSParser;
 import org.clapper.curn.parser.RSSParserException;
 import org.clapper.curn.parser.RSSChannel;
 import org.clapper.curn.parser.RSSItem;
 
 import org.clapper.util.io.FileUtils;
 import org.clapper.util.misc.Logger;
 import org.clapper.util.config.ConfigurationException;
 
 import org.apache.oro.text.perl.Perl5Util;
 
 /**
  * <p><i>curn</i>: Curiously Uncomplicated RSS Notifier.</p>
  *
  * <p><i>curn</i> is an RSS reader. It scans a configured set of URLs, each
  * one representing an RSS feed, and summarizes the results in an
  * easy-to-read text format. <i>curn</i> keeps track of URLs it's seen
  * before, using an on-disk cache; when using the cache, it will suppress
  * displaying URLs it has already reported (though that behavior can be
  * disabled). <i>curn</i> can be extended to use any RSS parser; its
  * built-in RSS parser, the
  * {@link org.clapper.curn.parser.minirss.MiniRSSParser MiniRSSParser}
  * class, can handle files in
  * {@link <a href="http://www.atomenabled.org/developers/">Atom</a>}
  * format (0.3) and RSS formats
  * {@link <a target="_top" href="http://backend.userland.com/rss091">0.91</a>},
  * 0.92,
  * {@link <a target="_top" href="http://web.resource.org/rss/1.0/">1.0</a>} and
  * {@link <a target="_top" href="http://blogs.law.harvard.edu/tech/rss">2.0</a>}.</p>
  *
  * <p>The <tt>Curn</tt> class represents the API entry point into the
 * <i>curn</i> processing. Any program can invoke this class's static
 * {@link #processRSSFeeds} method to process a configuration file; in
 * practice, most people use the existing <tt>Tool</tt> command-line
  * program.</p>
  *
  * @version <tt>$Revision$</tt>
  */
 public class Curn
 {
     /*----------------------------------------------------------------------*\
                              Private Constants
     \*----------------------------------------------------------------------*/
 
     private static final String EMAIL_HANDLER_CLASS =
                             "org.clapper.curn.email.EmailOutputHandlerImpl";
 
     private static final String BUNDLE_NAME = "org.clapper.curn.Curn";
 
     /*----------------------------------------------------------------------*\
                               Private Classes
     \*----------------------------------------------------------------------*/
 
     private class ItemComparator implements Comparator
     {
         private Date  now = new Date();
         private int   sortBy;
 
         ItemComparator (int sortBy)
         {
             this.sortBy = sortBy;
         }
 
         public int compare (Object o1, Object o2)
         {
             int      cmp = 0;
             RSSItem  i1  = (RSSItem) o1;
             RSSItem  i2  = (RSSItem) o2;
 
             switch (sortBy)
             {
                 case FeedInfo.SORT_BY_TITLE:
                     String title1 = i1.getTitle();
                     if (title1 == null)
                         title1 = "";
 
                     String title2 = i2.getTitle();
                     if (title2 == null)
                         title2 = "";
 
                     cmp = title1.compareToIgnoreCase (title2);
                     break;
 
                 case FeedInfo.SORT_BY_TIME:
                     Date time1 = i1.getPublicationDate();
                     if (time1 == null)
                         time1 = now;
 
                     Date time2 = i2.getPublicationDate();
                     if (time2 == null)
                         time2 = now;
 
                     cmp = time1.compareTo (time2);
                     break;
 
                 default:
                     cmp = -1;
                     break;
             }
 
             return cmp;
         }
 
         public boolean equals (Object o)
         {
             return (o instanceof ItemComparator);
         }
     }
 
     /*----------------------------------------------------------------------*\
                            Private Instance Data
     \*----------------------------------------------------------------------*/
 
     private ConfigFile      config           = null;
     private boolean         useCache         = true;
     private FeedCache       cache            = null;
     private Date            currentTime      = new Date();
     private Collection      outputHandlers   = new ArrayList();
     private Collection      emailAddresses   = new ArrayList();
     private Perl5Util       perl5Util        = new Perl5Util();
     private ResourceBundle  bundle           = null;
 
     /**
      * For log messages
      */
     private static Logger log = new Logger (Curn.class);
 
     /*----------------------------------------------------------------------*\
                                 Constructor
     \*----------------------------------------------------------------------*/
 
     public Curn (ConfigFile config)
     {
         this.config = config;
     }
 
     /*----------------------------------------------------------------------*\
                               Public Methods
     \*----------------------------------------------------------------------*/
 
     /**
      * Set the cache's notion of the current time. This method will change
      * the time used when reading and pruning the cache from the current time
      * to the specified time. This method must be called before
      * <tt>processRSSFeeds()</tt>.
      *
      * @param newTime  the time to pretend is the current time
      */
     public void setCurrentTime (Date newTime)
     {
         this.currentTime = newTime;
     }
 
     /**
      * Read the RSS feeds specified in a parsed configuration, writing them
      * to the output handler(s) specified in the configuration.
      *
      * @param configuration   the parsed configuration
      * @param emailAddresses  a collection of (string) email addresses to
      *                        receive the output, or null (or empty collection)
      *                        for none.
      * @param useCache        whether or not to use the cache
      *
      * @throws IOException              unable to open or read a required file
      * @throws ConfigurationException   error in configuration file
      * @throws RSSParserException       error parsing XML feed(s)
      * @throws CurnException          any other error
      */
     public void processRSSFeeds (ConfigFile configuration,
                                  Collection emailAddresses,
                                  boolean    useCache)
         throws IOException,
                ConfigurationException,
                RSSParserException,
                CurnException
     {
         Iterator            it;
         String              parserClassName;
         RSSParser           parser = null;
         Collection          channels;
         OutputStreamWriter  out;
 
         loadOutputHandlers (configuration, emailAddresses);
 
         if (useCache && (configuration.getCacheFile() != null))
         {
             cache = new FeedCache (configuration);
             cache.setCurrentTime (currentTime);
             cache.loadCache();
         }
 
         if (outputHandlers.size() == 0)
         {
             log.debug ("No output handlers. Skipping XML parse phase.");
         }
 
         else
         {
             // There are output handlers. We'll need a parser.
 
             parserClassName = configuration.getRSSParserClassName();
             log.info ("Getting parser \"" + parserClassName + "\"");
             parser = RSSParserFactory.getRSSParser (parserClassName);
         }
 
         channels = new ArrayList();
 
         Collection feeds = configuration.getFeeds();
         if (feeds.size() == 0)
             throw new ConfigurationException ("No configured RSS feed URLs.");
 
         for (it = configuration.getFeeds().iterator(); it.hasNext(); )
         {
             FeedInfo feedInfo = (FeedInfo) it.next();
             if (! feedInfo.feedIsEnabled())
             {
                 log.info ("Skipping disabled feed: " + feedInfo.getURL());
             }
 
             else
             {
                 RSSChannel channel = checkFeed (feedInfo,
                                                 parser,
                                                 configuration);
                 if (channel != null)
                     channels.add (new ChannelFeedInfo (feedInfo, channel));
             }
         }
 
         if (channels.size() > 0)
             displayChannels (channels);
 
         if ((cache != null) && configuration.mustUpdateCache())
             cache.saveCache();
     }
 
     /*----------------------------------------------------------------------*\
                               Private Methods
     \*----------------------------------------------------------------------*/
 
     private void loadOutputHandlers (ConfigFile configuration,
                                      Collection emailAddresses)
         throws ConfigurationException,
                CurnException
     {
         String         className;
         OutputHandler  handler;
         Iterator       it;
 
         if (configuration.totalOutputHandlers() > 0)
         {
             for (it = configuration.getOutputHandlerClassNames().iterator();
                  it.hasNext(); )
             {
                 className = (String) it.next();
                 handler   = OutputHandlerFactory.getOutputHandler (className);
                 outputHandlers.add (handler);
             }
 
             // If there are email addresses, then attempt to load the email
             // handler, and wrap the other output handlers inside it.
 
             if ((emailAddresses != null) && (emailAddresses.size() > 0))
             {
                 EmailOutputHandler emailHandler;
 
                 emailHandler = (EmailOutputHandler)
                                       OutputHandlerFactory.getOutputHandler
                                                    (EMAIL_HANDLER_CLASS);
 
                 // Place all the other handlers inside the EmailOutputHandler
 
                 for (it = outputHandlers.iterator(); it.hasNext(); )
                     emailHandler.addOutputHandler ((OutputHandler) it.next());
 
                 // Add the email addresses to the handler
 
                 for (it = emailAddresses.iterator(); it.hasNext(); )
                     emailHandler.addRecipient ((String) it.next());
 
                 // Clear the existing set of output handlers and replace it
                 // with the email handler.
 
                 outputHandlers.clear();
                 outputHandlers.add (emailHandler);
             }
         }
     }
 
     /**
      * <p>Check and possibly process a feed. If XML parsing is disabled and
      * the feed has no "save as" setting, this method just returns (since
      * it's pointless to do anything if both of those conditions aren't
      * met). Otherwise, this method will open a URL connection to the feed,
      * determine whether the feed has changed, download it if it has, parse
      * it (if required), and save it (if the feed has a "save as"
      * setting).</p>
      *
      * <p>If there are no output handlers, then there's no need to waste
      * time parsing the feeds' XML; the caller is responsible for detecting
      * that condition and passing a null <tt>parser</tt> parameter into
      * this method in that case.</p>
      *
      * @param feedInfo      the info about the feed
      * @param parser        the RSS parser to use, or null if parsing is to
      *                      be skipped
      * @param configuration the parsed configuration data
      *
      * @return the <tt>RSSChannel</tt> representing the parsed feed, if
      *         parsing was enabled; otherwise, null.
      *
      * @throws RSSParserException parser error
      */
     private RSSChannel checkFeed (FeedInfo   feedInfo,
                                   RSSParser  parser,
                                   ConfigFile configuration)
         throws RSSParserException
     {
         URL         feedURL = feedInfo.getURL();
         RSSChannel  channel = null;
 
         if ((parser == null) && (feedInfo.getSaveAsFile() == null))
         {
             log.debug ("Feed "
                      + feedURL.toString()
                      + ": RSS parser is null and there's no save file. "
                      + "There's no sense processing this feed.");
         }
 
         else
         {
             channel = processFeed (feedInfo, parser, configuration);
         }
 
         return channel;
     }
 
     /**
      * Actually processes a feed. This method is called by checkFeed()
      * after checkFeed() determines that there's a reason to try to download
      * the feed (i.e., the feed has a "save as" setting, and/or parsing is
      * desired.
      * @param feedInfo      the info about the feed
      * @param parser        the RSS parser to use, or null if parsing is to
      *                      be skipped
      * @param configuration the parsed configuration data
      *
      * @return the <tt>RSSChannel</tt> representing the parsed feed, if
      *         parsing was enabled; otherwise, null.
      *
      * @throws RSSParserException parser error
      */
     private RSSChannel processFeed (FeedInfo   feedInfo,
                                     RSSParser  parser,
                                     ConfigFile configuration)
         throws RSSParserException
     {
         URL         feedURL = feedInfo.getURL();
         String      feedURLString = feedURL.toString();
         RSSChannel  channel = null;
 
         try
         {
             log.info ("Checking for new data from RSS feed " + feedURLString);
 
             // Open the connection.
 
             URLConnection conn = feedURL.openConnection();
 
             // Don't download the channel if it hasn't been modified since
             // we last checked it. We set the If-Modified-Since header, to
             // tell the web server not to return the content if it's not
             // newer than what we saw before. However, as a double-check
             // (for web servers that ignore the header), we also check the
             // Last-Modified header, if any, that's returned; if it's not
             // newer, we don't bother to parse and process the returned
             // XML.
 
             setIfModifiedSinceHeader (conn, feedInfo);
 
             // If the config allows us to transfer gzipped content, then
             // set that header, too.
 
             setGzipHeader (conn, configuration);
 
             // If the feed has actually changed, process it.
 
             if (! feedHasChanged (conn, feedInfo))
             {
                 log.info ("Feed has not changed. Skipping it.");
             }
 
             else
             {
                 log.debug ("Feed may have changed. "
                          + "Downloading and processing it.");
 
                 if (cache != null)
                 {
                     cache.addToCache (feedInfo.getCacheKey(),
                                       feedURL,
                                       feedInfo);
                 }
 
                 InputStream is = getURLInputStream (conn);
 
                 // Download the feed to a file. We'll parse the file.
 
                 File temp = File.createTempFile ("curn", "xml", null);
                 temp.deleteOnExit();
 
                 int totalBytes = downloadFeed (is, feedURLString, temp);
                 is.close();
 
                 if (totalBytes == 0)
                 {
                     log.debug ("Feed \""
                              + feedURLString
                              + "\" returned no data.");
                 }
 
                 else
                 {
                     File saveAsFile = feedInfo.getSaveAsFile();
 
                     if (saveAsFile != null)
                     {
                         log.debug ("Copying temporary file \""
                                  + temp.getPath()
                                  + "\" to \""
                                  + saveAsFile.getPath()
                                  + "\"");
                         FileUtils.copyFile (temp, saveAsFile);
                     }
 
                     if (parser == null)
                         log.debug ("No RSS parser. Skipping XML parse phase.");
                     else
                     {
                         log.debug ("Using RSS parser "
                                  + parser.getClass().getName()
                                  + " to parse the feed.");
 
                         is = new FileInputStream (temp);
                         channel = parser.parseRSSFeed (is);
                         is.close();
 
                         processChannelItems (channel, feedInfo);
                         if (channel.getItems().size() == 0)
                             channel = null;
                     }
                 }
 
                 temp.delete();
             }
         }
 
         catch (MalformedURLException ex)
         {
             log.error ("", ex);
         }
 
         catch (RSSParserException ex)
         {
             log.error ("RSS parse exception: ", ex); 
         }
 
         catch (IOException ex)
         {
             log.error ("", ex);
         }
 
         return channel;
     }
 
     private int downloadFeed (InputStream urlStream, String feedURL, File file)
         throws IOException
     {
         int totalBytes = 0;
 
         log.debug ("Downloading \""
                  + feedURL
                  + "\" to file \""
                  + file.getPath());
         OutputStream os = new FileOutputStream (file);
         totalBytes = FileUtils.copyStream (urlStream, os);
         os.close();
 
         // It's possible for totalBytes to be zero if, for instance, the
         // use of the If-Modified-Since header caused an HTTP server to
         // return no content.
 
         return totalBytes;
     }
 
     private InputStream getURLInputStream (URLConnection conn)
         throws IOException
     {
         InputStream is = conn.getInputStream();
         String ce = conn.getHeaderField ("content-encoding");
 
         if (ce != null)
         {
             String urlString = conn.getURL().toString();
 
             log.debug ("URL \""
                      + urlString
                      + "\" -> Content-Encoding: "
                      + ce);
             if (ce.indexOf ("gzip") != -1)
             {
                 log.debug ("URL \""
                          + urlString
                          + "\" is compressed. Using GZIPInputStream.");
                 is = new GZIPInputStream (is);
             }
         }
 
         return is;
     }
 
     private void setGzipHeader (URLConnection conn, ConfigFile configuration)
     {
         if (configuration.retrieveFeedsWithGzip())
         {
             log.debug ("Setting header \"Accept-Encoding\" to \"gzip\"");
             conn.setRequestProperty ("Accept-Encoding", "gzip");
         }
     }
 
     private void setIfModifiedSinceHeader (URLConnection conn,
                                            FeedInfo      feedInfo)
     {
         long     lastSeen = 0;
         boolean  hasChanged = false;
         String   cacheKey = feedInfo.getCacheKey();
         URL      feedURL = feedInfo.getURL();
 
         if ((cache != null) && (cache.contains (cacheKey)))
         {
             FeedCacheEntry entry = (FeedCacheEntry) cache.getItem (cacheKey);
             lastSeen = entry.getTimestamp();
 
             if (lastSeen > 0)
             {
                 if (log.isDebugEnabled())
                 {
                     log.debug ("Setting If-Modified-Since header for feed \""
                              + feedURL.toString()
                              + "\" to: "
                              + String.valueOf (lastSeen)
                              + " ("
                              + new Date (lastSeen).toString()
                              + ")");
                 }
 
                 conn.setIfModifiedSince (lastSeen);
             }
         }
     }
 
     private boolean feedHasChanged (URLConnection conn, FeedInfo feedInfo)
         throws IOException
     {
         long     lastSeen = 0;
         long     lastModified = 0;
         boolean  hasChanged = false;
         String   cacheKey = feedInfo.getCacheKey();
         URL      feedURL = feedInfo.getURL();
 
         if ((cache != null) && (cache.contains (cacheKey)))
         {
             FeedCacheEntry entry = (FeedCacheEntry) cache.getItem (cacheKey);
             lastSeen = entry.getTimestamp();
         }
 
         if (lastSeen == 0)
         {
             log.debug ("Feed \""
                      + feedURL.toString()
                      + "\" has no recorded last-seen time.");
             hasChanged = true;
         }
 
         else if ((lastModified = conn.getLastModified()) == 0)
         {
             log.debug ("Feed \""
                      + feedURL.toString()
                      + "\" provides no last-modified time.");
             hasChanged = true;
         }
 
         else if (lastSeen >= lastModified)
         {
             log.debug ("Feed \""
                      + feedURL.toString()
                      + "\" has Last-Modified time of "
                      + new Date (lastModified).toString()
                      + ", which is not newer than last-seen time of "
                      + new Date (lastSeen).toString()
                      + ". Feed has no new data.");
         }
 
         else
         {
             log.debug ("Feed \""
                      + feedURL.toString()
                      + "\" has Last-Modified time of "
                      + new Date (lastModified).toString()
                      + ", which is newer than last-seen time of "
                      + new Date (lastSeen).toString()
                      + ". Feed might have new data.");
             hasChanged = true;
         }
 
         return hasChanged;
     }
 
     private void processChannelItems (RSSChannel  channel,
                                       FeedInfo    feedInfo)
         throws RSSParserException,
                MalformedURLException
     {
         Collection  items;
         Iterator    it;
         String      titleOverride = feedInfo.getTitleOverride();
         boolean     pruneURLs = feedInfo.pruneURLs();
         String      editCmd = feedInfo.getItemURLEditCommand();
         String      channelName = channel.getLink().toString();
 
         if (titleOverride != null)
             channel.setTitle (titleOverride);
 
         if (editCmd != null)
         {
             log.debug ("Channel \""
                      + channelName
                      + "\": Edit command is: "
                      + editCmd);
         }
 
         items = sortChannelItems (channel.getItems(), feedInfo);
 
         // First, weed out the ones we don't care about.
 
         log.info ("Channel \""
                 + channelName
                 + "\": "
                 + String.valueOf (items.size())
                 + " total items");
         for (it = items.iterator(); it.hasNext(); )
         {
             RSSItem item = (RSSItem) it.next();
             URL itemURL = item.getLink();
 
             if (itemURL == null)
             {
                 log.debug ("Skipping item with null URL.");
                 it.remove();
                 continue;
             }
 
             if (pruneURLs || (editCmd != null))
             {
                 // Prune the URL of its parameters, if configured for this
                 // site. This must be done before checking the cache, because
                 // the pruned URLs are what end up in the cache.
 
                 String sURL = itemURL.toExternalForm();
 
                 if (pruneURLs)
                 {
                     int i = sURL.indexOf ("?");
 
                     if (i != -1)
                         sURL = sURL.substring (0, i);
                 }
 
                 if (editCmd != null)
                     sURL = perl5Util.substitute (editCmd, sURL);
 
                 itemURL = new URL (sURL);
             }
 
             // Normalize the URL and save it.
 
             item.setLink (Util.normalizeURL (itemURL));
 
             // Skip it if it's cached.
 
             log.debug ("Item link: " + itemURL);
             log.debug ("Item ID: " + item.getUniqueID());
             log.debug ("Item key: " + item.getCacheKey());
             if ((cache != null) && cache.contains (item.getCacheKey()))
             {
                 log.debug ("Skipping cached URL: " + itemURL.toString());
                 it.remove();
                 continue;
             }
         }
 
         // Add all the items to the cache.
 
         if (items.size() > 0)
         {
             for (it = items.iterator(); it.hasNext(); )
             {
                 RSSItem item = (RSSItem) it.next();
 
                 log.debug ("Cacheing URL: " + item.getLink().toString());
                 if (cache != null)
                 {
                     cache.addToCache (item.getCacheKey(),
                                       item.getLink(),
                                       feedInfo);
                 }
             }
         }
 
         // If we're to ignore items with duplicate titles, now is the time
         // to do it. It must be done AFTER cacheing, to be sure we don't show
         // the weeded-out duplicates during the next run.
 
         if (feedInfo.ignoreItemsWithDuplicateTitles())
             items = pruneDuplicateTitles (items);
 
         // Change the channel's items to the ones that are left.
 
         channel.setItems (items);
     }
 
     private Collection sortChannelItems (Collection items, FeedInfo feedInfo)
     {
         Collection result = items;
         int        total  = items.size();
 
         if (total > 0)
         {
             int sortBy = feedInfo.getSortBy();
 
             switch (sortBy)
             {
                 case FeedInfo.SORT_BY_NONE:
                     break;
 
                 case FeedInfo.SORT_BY_TITLE:
                 case FeedInfo.SORT_BY_TIME:
 
                     // Can't just use a TreeSet, with a Comparator, because
                     // then items with the same title will be weeded out.
 
                     Object[] array = items.toArray();
                     Arrays.sort (array, new ItemComparator (sortBy));
                     result = Arrays.asList (array);
                 break;
 
             default:
                 throw new IllegalStateException ("Bad FeedInfo.getSortBy() "
                                                + "value of "
                                                + String.valueOf (sortBy));
             }
         }
 
         return result;
     }
 
     private Collection pruneDuplicateTitles (Collection items)
     {
         Set         titlesSeen = new HashSet();
         Collection  result     = new ArrayList();
 
         for (Iterator it = items.iterator(); it.hasNext(); )
         {
             RSSItem item  = (RSSItem) it.next();
             String  title = item.getTitle().toLowerCase();
 
             if (title == null)
                 title = "";
 
             if (! titlesSeen.contains (title))
             {
                 result.add (item);
                 titlesSeen.add (title);
             }
         }
 
         return result;
     }
 
     private void displayChannels (Collection channels)
         throws CurnException,
                ConfigurationException
     {
         OutputHandler firstOutput = null;
 
         // Dump the output to each output handler
 
         for (Iterator itHandler = outputHandlers.iterator();
              itHandler.hasNext(); )
         {
             OutputHandler handler;
 
             handler = (OutputHandler) itHandler.next();
 
             log.info ("Initializing output handler "
                     + handler.getClass().getName());
             handler.init (config);
 
             for (Iterator itChannel = channels.iterator();
                  itChannel.hasNext(); )
             {
                 ChannelFeedInfo cfi = (ChannelFeedInfo) itChannel.next();
                 handler.displayChannel (cfi.getChannel(), cfi.getFeedInfo());
             }
 
             handler.flush();
 
             if ((firstOutput == null) && (handler.hasGeneratedOutput()))
                 firstOutput = handler;
         }
 
         // If we're not emailing the output, then dump the output from the
         // first handler to the screen.
 
         if (emailAddresses.size() == 0)
         {
             if (firstOutput == null)
                 log.info ("None of the output handlers produced output.");
 
             else
             {
                 InputStream output = firstOutput.getGeneratedOutput();
 
                 try
                 {
                     FileUtils.copyStream (output, System.out);
                     output.close();
                 }
 
                 catch (IOException ex)
                 {
                     throw new CurnException ("Failed to copy output from "
                                            + "handler "
                                            + firstOutput.getClass().getName()
                                            + " to standard output.",
                                              ex);
                 }
             }
         }
     }
 }
