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
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.io.Reader;
 import java.io.Writer;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.Iterator;
 import java.util.Collection;
 import java.util.zip.GZIPInputStream;
 
 import org.clapper.curn.util.Util;
 import org.clapper.curn.parser.RSSParser;
 import org.clapper.curn.parser.RSSParserException;
 import org.clapper.curn.parser.RSSChannel;
 import org.clapper.curn.parser.RSSItem;
 
 import org.clapper.util.io.FileUtil;
 import org.clapper.util.logging.Logger;
 import org.clapper.util.regex.RegexUtil;
 import org.clapper.util.regex.RegexException;
 import org.clapper.util.text.TextUtil;
 
 class FeedDownloadThread extends Thread
 {
     /*----------------------------------------------------------------------*\
                              Private Constants
     \*----------------------------------------------------------------------*/ 
     
    private final static String HTTP_CONTENT_TYPE_CHARSET_FIELD = "charset=";
    private final static int    HTTP_CONTENT_TYPE_CHARSET_FIELD_LEN =
                                       HTTP_CONTENT_TYPE_CHARSET_FIELD.length();
 
     /*----------------------------------------------------------------------*\
                            Private Instance Data
     \*----------------------------------------------------------------------*/ 
 
     private Logger         log             = null;
     private String         id              = null;
     private ConfigFile     configuration   = null;
     private RSSParser      rssParser       = null;
     private FeedCache      cache           = null;
     private RegexUtil      regexUtil       = new RegexUtil();
     private List           feedQueue       = null;
     private FeedException  exception       = null;
 
     /*----------------------------------------------------------------------*\
                                Inner Classes
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
 
         public int hashCode()
         {
             return super.hashCode();
         }
 
         public boolean equals (Object o)
         {
             return (o instanceof ItemComparator);
         }
     }
 
     /**
      * Encapsulates information about a downloaded feed.
      */
     private class DownloadedTempFile
     {
         File    file;
         String  encoding;
         int     bytesDownloaded;
 
         DownloadedTempFile (File   tempFile,
                             String encoding,
                             int    bytesDownloaded)
         {
             this.file = tempFile;
             this.encoding = encoding;
             this.bytesDownloaded = bytesDownloaded;
         }
     }
 
     /*----------------------------------------------------------------------*\
                                 Constructor
     \*----------------------------------------------------------------------*/ 
 
     /**
      * Create a new <tt>FeedDownloadThread</tt> object to download feeds.
      *
      * @param threadId   the unique identifier for the thread, for log messages
      * @param parser     the RSS parser to use
      * @param feedCache  the feed cache to save cache data to
      * @param configFile the parsed configuration file
      * @param feedQueue  list of feeds to be processed. This list must contain
      *                   contain <tt>FeedInfo</tt> objects. The list is assumed
      *                   to be shared across multiple threads, and should be
      *                   thread safe.
      */
     FeedDownloadThread (String     threadId,
                         RSSParser  parser,
                         FeedCache  feedCache,
                         ConfigFile configFile,
                         List       feedQueue)
     {
 
         this.id = threadId;
 
         String name = "FeedDownloadThread-" + this.id;
 
         super.setName (name);
         this.log = new Logger (name);
         this.configuration = configFile;
         this.rssParser = parser;
         this.cache = feedCache;
         this.feedQueue = feedQueue;
 
         //setPriority (getPriority() + 1);
     }
 
     /*----------------------------------------------------------------------*\
                               Public Methods
     \*----------------------------------------------------------------------*/ 
 
     /**
      * Run the thread. Pulls the next <tt>FeedInfo</tt> object from the
      * feed queue (the list passed to the constructor) and processes it.
      * The thread stops running when it has finished downloading a feed and
      * it finds that the feed queue is empty.
      */
     public void run()
     {
         log.info ("Thread is alive at priority " + getPriority());
 
         for (;;)
         {
             FeedInfo feed = null;
 
             log.debug ("Checking feed queue.");
 
             synchronized (feedQueue)
             {
                 if (feedQueue.size() > 0)
                     feed = (FeedInfo) feedQueue.remove (0);
             }
 
             if (feed == null)
             {
                 log.info ("Queue of feeds is empty. Nothing left to do.");
                 break;
             }
 
             processFeed (feed);
         }
 
         log.debug ("Thread is finishing.");
     }
 
     /*----------------------------------------------------------------------*\
                           Package-visible Methods
     \*----------------------------------------------------------------------*/ 
 
     /**
      * Processes the specified feed. This method is called by {@link #run}.
      * It's also intended to be called directly, when <i>curn</i> is
      * running in non-threaded mode. Once this method returns, use the
      * {@link #errorOccurred} method to determine whether a feed-processing
      * error occurred, and the {@link #getException} method to receive the
      * exception if an error did occur. (If an error does occur, this method
      * logs it regardless.)
      *
      * @param feed  The <tt>FeedInfo</tt> object for the feed to be processed
      *
      * @throws FeedException error processing feed
      *
      * @see #errorOccurred
      * @see #getException
      */
     void processFeed (FeedInfo feed)
     {
         this.exception = null;
 
         try
         {
             log.info ("Processing feed: " + feed.getURL().toString());
             feed.setParsedChannelData (handleFeed (feed,
                                                    rssParser,
                                                    configuration));
         }
 
         catch (FeedException ex)
         {
             log.error ("", ex);
             this.exception = ex;
         }
     }
 
     /**
      * Determine whether an error occurred during processing of the most
      * recent feed. If an error did occur, you can use {@link #getException}
      * to get the corresponding exception.
      *
      * @return <tt>true</tt> if an error occurred while processing the last
      *         feed, <tt>false</tt> if no error occurred
      *
      * @see #processFeed
      * @see #getException
      */
     boolean errorOccurred()
     {
         return (this.exception != null);
     }
 
     /**
      * If an error occurred during processing of the most recent feed,
      * this method will return the exception associated with the error.
      *
      * @return the exception associated with the most recent error, or
      *         null if no error has occurred
      *
      * @see #processFeed
      * @see #errorOccurred
      */
     FeedException getException()
     {
         return this.exception;
     }
 
     /*----------------------------------------------------------------------*\
                               Private Methods
     \*----------------------------------------------------------------------*/ 
 
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
      * @throws FeedException  error
      */
     private RSSChannel handleFeed (FeedInfo   feedInfo,
                                    RSSParser  parser,
                                    ConfigFile configuration)
         throws FeedException
     {
         URL         feedURL = feedInfo.getURL();
         String      feedURLString = feedURL.toString();
         RSSChannel  channel = null;
         String      s;
 
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
 
             setIfModifiedSinceHeader (conn, feedInfo, cache);
 
             // If the config allows us to transfer gzipped content, then
             // set that header, too.
 
             setGzipHeader (conn, configuration);
 
             // Set the user-agent header.
 
             conn.setRequestProperty ("User-Agent", feedInfo.getUserAgent());
 
             // If the feed has actually changed, process it.
 
             if (! feedHasChanged (conn, feedInfo, cache))
             {
                 log.info ("Feed has not changed. Skipping it.");
             }
 
             else
             {
                 log.debug ("Feed may have changed. "
                          + "Downloading and processing it.");
 
 
                 // Download the feed to a file. We'll parse the file.
 
                 DownloadedTempFile tempFile = downloadFeed (conn, feedInfo);
 
                 if (tempFile.bytesDownloaded == 0)
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
                         s = ((tempFile.encoding == null) ? "default"
                                                          : tempFile.encoding);
 
                         String saveAsEncoding = feedInfo.getSaveAsEncoding();
                         log.debug ("Copying temporary file \""
                                  + tempFile.file.getPath()
                                  + "\" (encoding="
                                  + s
                                  + ") to \""
                                  + saveAsFile.getPath()
                                  + "\" (encoding="
                                  + saveAsEncoding
                                  + ")");
                         FileUtil.copyTextFile (tempFile.file,
                                                tempFile.encoding,
                                                saveAsFile,
                                                saveAsEncoding);
                     }
 
                     if (parser == null)
                     {
                         log.debug ("No RSS parser. Skipping XML parse phase.");
                     }
 
                     else if (feedInfo.saveOnly())
                     {
                         log.debug ("Feed is marked 'save only'. Skipping "
                                  + "XML parse phase.");
                     }
 
                     else
                     {
                         if (feedInfo.getPreparseEditCommands() != null)
                             handlePreparseEdit (feedInfo, tempFile);
 
                         log.debug ("Using RSS parser "
                                  + parser.getClass().getName()
                                  + " to parse the feed.");
 
                         InputStream is = new FileInputStream (tempFile.file);
                         channel = parser.parseRSSFeed (is, tempFile.encoding);
                         is.close();
 
                         processChannelItems (channel, feedInfo);
                         if (channel.getItems().size() == 0)
                             channel = null;
                     }
                 }
 
                 tempFile.file.delete();
             }
         }
 
         catch (RegexException ex)
         {
             throw new FeedException (feedInfo, ex);
         }
 
         catch (MalformedURLException ex)
         {
             throw new FeedException (feedInfo, ex);
         }
 
         catch (RSSParserException ex)
         {
             throw new FeedException (feedInfo, ex);
         }
 
         catch (IOException ex)
         {
             throw new FeedException (feedInfo, ex);
         }
 
         return channel;
     }
 
     /**
      * Download a feed.
      *
      * @param conn     the <tt>URLConnection</tt> for the feed
      * @param feedInfo the <tt>FeedInfo</tt> object for the feed
      *
      * @return the <tt>DownloadedTempFile</tt> object that captures the
      *         details about the downloaded file
      */
     private DownloadedTempFile downloadFeed (URLConnection conn,
                                              FeedInfo      feedInfo)
         throws IOException
     {
         URL feedURL = feedInfo.getURL();
         String feedURLString = feedURL.toString();
         int totalBytes = 0;
         File tempFile = createTempXMLFile();
 
         log.debug ("Downloading \""
                  + feedURLString
                  + "\" to file \""
                  + tempFile.getPath());
 
         InputStream urlStream = getURLInputStream (conn);
         Reader      reader;
         Writer      writer;
 
         // Determine the character set encoding to use.
 
         String protocol = feedURL.getProtocol();
         String encoding = null;
 
         if (protocol.equals ("http") || protocol.equals("https"))
         {
             String contentTypeHeader = conn.getContentType();
 
             if (contentTypeHeader != null)
             {
                 encoding = contentTypeCharSet (contentTypeHeader);
                 log.debug ("HTTP server says encoding for \""
                          + feedURLString
                          + "\" is \""
                          + ((encoding == null) ? "<null>" : encoding)
                          + "\"");
             }
         }
 
         else if (protocol.equals ("file"))
         {
             // Assume the same default encoding used by "SaveAsEncoding",
             // unless explicitly specified.
 
             encoding = FeedInfo.DEFAULT_SAVE_AS_ENCODING;
             log.debug ("Default encoding for \""
                      + feedURLString
                      + "\" is \""
                      + encoding
                      + "\"");
         }
 
         // Set the forced encoding, if specified. Note: This is done after
         // we check the HTTP encoding, so we can log any discrepancies
         // between the config-specified encoding and the HTTP
         // server-specified encoding.
 
         String forcedEncoding = feedInfo.getForcedCharacterEncoding();
         if (forcedEncoding != null)
         {
             log.debug ("URL \""
                      + feedURLString
                      + "\": Forcing encoding to be \""
                      + forcedEncoding
                      + "\"");
             encoding = forcedEncoding;
         }
 
         if (encoding != null)
         {
             log.debug ("Encoding is \"" + encoding + "\"");
             reader = new InputStreamReader (urlStream, encoding);
             writer = new OutputStreamWriter (new FileOutputStream (tempFile),
                                              encoding);
             /*
             // Cheat by writing an encoding line to the temp file.
             writer.write ("<?xml version=\"1.0\" encoding=\""
                         + encoding
                         + "\"> ");
             */
         }
 
         else
         {
             InputStreamReader isr = new InputStreamReader (urlStream);
             reader = isr;
             writer = new FileWriter (tempFile);
             log.debug ("No encoding for \""
                      + feedURLString
                      + "\". Using VM default of \""
                      + isr.getEncoding()
                      + "\"");
         }
         
         totalBytes = FileUtil.copyReader (reader, writer);
         log.debug ("Total bytes downloaded: " + totalBytes);
         writer.close();
         urlStream.close();
 
         // It's possible for totalBytes to be zero if, for instance, the
         // use of the If-Modified-Since header caused an HTTP server to
         // return no content.
 
         return new DownloadedTempFile (tempFile, encoding, totalBytes);
     }
 
     /**
      * Given a content-type header, extract the character set information.
      *
      * @param contentType  the content type header
      *
      * @return the character set, or null if not available
      */
     private String contentTypeCharSet (String contentType)
     {
         String result = null;
         String[] fields = TextUtil.split (contentType, "; \t");
 
 
         for (int i = 0; i < fields.length; i++)
         {
             if (fields[i].startsWith (HTTP_CONTENT_TYPE_CHARSET_FIELD) &&
                 (fields[i].length() > HTTP_CONTENT_TYPE_CHARSET_FIELD_LEN))
             {
                 result = fields[i].substring
                                          (HTTP_CONTENT_TYPE_CHARSET_FIELD_LEN);
                 break;
             }
         }
 
         return result;
     }
 
     /**
      * Get the input stream for a URL. Handles compressed data.
      *
      * @param conn the <tt>URLConnection</tt> to process
      *
      * @return the <tt>InputStream</tt>
      */
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
 
     /**
      * Conditionally set the header that requests a compressed (gzipped)
      * feed. Must be called on a <tt>URLConnection</tt> before the
      * <tt>InputStream</tt> is retrieved.
      *
      * @param conn          the <tt>URLConnection</tt> on which to set the
      *                      header
      * @param configuration the parsed configuration, which indicates whether
      *                      or not to request gzipping of downloaded data
      */
     private void setGzipHeader (URLConnection conn, ConfigFile configuration)
     {
         if (configuration.retrieveFeedsWithGzip())
         {
             log.debug ("Setting header \"Accept-Encoding\" to \"gzip\"");
             conn.setRequestProperty ("Accept-Encoding", "gzip");
         }
     }
 
     /**
      * Conditionally set the header that "If-Modified-Since" header for a
      * feed. Must be called on a <tt>URLConnection</tt> before the
      * <tt>InputStream</tt> is retrieved. Uses the feed cache to set the
      * value.
      *
      * @param conn     the <tt>URLConnection</tt> on which to set the
      *                 header
      * @param feedInfo the information on the feed
      * @param cache    the cache
      */
     private void setIfModifiedSinceHeader (URLConnection conn,
                                            FeedInfo      feedInfo,
                                            FeedCache     cache)
     {
         long     lastSeen = 0;
         boolean  hasChanged = false;
         URL      feedURL = feedInfo.getURL();
 
         if (cache != null)
         {
             FeedCacheEntry entry = cache.getItemByURL (feedURL);
 
             if (entry != null)
             {
                 lastSeen = entry.getTimestamp();
 
                 if (lastSeen > 0)
                 {
                     if (log.isDebugEnabled())
                     {
                         log.debug ("Setting If-Modified-Since header for "
                                  + "feed \""
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
     }
 
     /**
      * Query the appropriate URL connection headers to determine whether
      * the remote server thinks feed data has changed since the last time
      * the feed was downloaded. Must be called on a <tt>URLConnection</tt>
      * after the <tt>InputStream</tt> is retrieved. Uses the feed cache to
      * set the value.
      *
      * @param conn     the <tt>URLConnection</tt> whose headers are to be
      *                 checked
      * @param feedInfo the information on the feed
      * @param cache    the cache
      */
     private boolean feedHasChanged (URLConnection conn,
                                     FeedInfo      feedInfo,
                                     FeedCache     cache)
         throws IOException
     {
         long     lastSeen = 0;
         long     lastModified = 0;
         boolean  hasChanged = false;
         URL      feedURL = feedInfo.getURL();
 
         if (cache != null)
         {
             FeedCacheEntry entry = cache.getItemByURL (feedURL);
 
             if (entry != null)
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
 
 
     /**
      * Apply one or more preparse edit commands to a downloaded, but not yet
      * parsed, feed XML file.
      *
      * @param feedInfo    the FeedInfo data
      * @param tempFile    the downloaded temp file. This method modifies
      *                    this parameter
      *
      * @throws RegexException encountered a bad regular expression
      * @throws IOException    failed to open a file
      */
     private void handlePreparseEdit (FeedInfo           feedInfo,
                                      DownloadedTempFile tempFile)
         throws RegexException,
                IOException
     {
         // Allocate a new temp file
 
         File outputFile = createTempXMLFile();
         BufferedReader in;
         PrintWriter out;
 
         if (tempFile.encoding != null)
         {
             in = new BufferedReader
                      (new InputStreamReader
                          (new FileInputStream (tempFile.file),
                           tempFile.encoding));
             out = new PrintWriter
                       (new OutputStreamWriter
                           (new FileOutputStream (outputFile),
                            tempFile.encoding));
         }
 
         else
         {
             in  = new BufferedReader (new FileReader (tempFile.file));
             out = new PrintWriter (new FileWriter (outputFile));
         }
 
         try
         {
             String[] editCommands = feedInfo.getPreparseEditCommands();
             String   line;
             int      lineNumber = 0;
 
             while ((line = in.readLine()) != null)
             {
                 lineNumber++;
                 for (int i = 0; i < editCommands.length; i++)
                 {
                     if (log.isDebugEnabled() && (lineNumber == 1))
                     {
                         log.debug ("Applying edit command \""
                                  + editCommands[i]
                                  + "\" to XML file, line "
                                  + lineNumber);
                     }
 
                     line = regexUtil.substitute (editCommands[i], line);
                 }
 
                 out.println (line);
             }
 
             out.flush();
             out.close();
 
             tempFile.file.delete();
             tempFile.file = outputFile;
         }
 
         finally
         {
             in.close();
             out.close();
         }
     }
 
     /**
      * Process all the items for a channel.
      *
      * @param channel   the channel
      * @param feedInfo  the feed information for the channel
      *
      * @throws RSSParserException    parser exception
      * @throws MalformedURLException bad URL
      * @throws RegexException        substitution error
      */
     private void processChannelItems (RSSChannel  channel,
                                       FeedInfo    feedInfo)
         throws RSSParserException,
                MalformedURLException,
                RegexException
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
                 {
                     log.debug ("Before editing, item URL=" + sURL);
                     sURL = regexUtil.substitute (editCmd, sURL);
                     log.debug ("After editing, item URL=" + sURL);
                 }
 
                 itemURL = new URL (sURL);
             }
 
             // Normalize the URL and save it.
 
             itemURL = Util.normalizeURL (itemURL);
             item.setLink (itemURL);
 
             // Skip it if it's cached.
 
             String itemID = item.getID();
             log.debug ("Item link: " + itemURL);
             log.debug ("Item ID: " + ((itemID == null) ? "<null>" : itemID));
             if (cache != null)
             {
                 if ((itemID != null) && (cache.containsID (itemID)))
                 {
                     log.debug ("Skipping cached ID \""
                              + itemID
                              + "\" (item URL \""
                              + itemURL.toString()
                              + "\")");
                     it.remove();
                 }
 
                 else if (cache.containsURL (itemURL))
                 {
                     log .debug ("Skipping cached URL \""
                              + itemURL.toString()
                              + "\")");
                     it.remove();
                 }
             }
         }
 
         // Add all the items to the cache, and adjust whatever items are to
         // be adjusted.
 
         if (items.size() > 0)
         {
             for (it = items.iterator(); it.hasNext(); )
             {
                 RSSItem item = (RSSItem) it.next();
 
                 if (cache != null)
                 {
                     log.debug ("Cacheing URL: " + item.getLink().toString());
                     cache.addToCache (item.getID(),
                                       item.getLink(),
                                       feedInfo);
                 }
             }
         }
 
         // If we're to ignore items with duplicate titles, now is the time
         // to do it. It must be done AFTER caching, to be sure we don't show
         // the weeded-out duplicates during the next run.
 
         if (feedInfo.ignoreItemsWithDuplicateTitles())
             items = pruneDuplicateTitles (items);
 
         // Change the channel's items to the ones that are left.
 
         channel.setItems (items);
     }
 
     /**
      * Sort downloaded items according to the sort criteria for the feed
      *
      * @param items    the downloaded items
      * @param feedInfo info about the feed, used to determine the desired
      *                 sort criteria
      *
      * @return a new <tt>Collection</tt> of the same items, possibly sorted
      */
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
 
     /**
      * Prune all items with duplicate titles.
      *
      * @param items the list of items
      *
      * @return a new <tt>Collection</tt> of items, possibly pruned
      */
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
 
     /**
      * Create a temporary file for XML content.
      *
      * @return the temp file
      *
      * @throws IOException error creating temporary file
      */
     private File createTempXMLFile()
         throws IOException
     {
         File f = File.createTempFile ("curn", ".xml", null);
         f.deleteOnExit();
         return f;
     }
 }
