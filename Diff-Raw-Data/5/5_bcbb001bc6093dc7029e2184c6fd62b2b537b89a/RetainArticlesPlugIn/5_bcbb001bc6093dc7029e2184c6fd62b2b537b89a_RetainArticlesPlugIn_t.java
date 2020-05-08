 /*---------------------------------------------------------------------------*\
   $Id$
   ---------------------------------------------------------------------------
   This software is released under a BSD-style license:
 
   Copyright (c) 2007 Brian M. Clapper. All rights reserved.
 
   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions are
   met:
 
   1.  Redistributions of source code must retain the above copyright notice,
       this list of conditions and the following disclaimer.
 
   2.  The end-user documentation included with the redistribution, if any,
       must include the following acknowlegement:
 
         "This product includes software developed by Brian M. Clapper
         (bmc@clapper.org, http://www.clapper.org/bmc/). That software is
         copyright (c) 2007 Brian M. Clapper."
 
       Alternately, this acknowlegement may appear in the software itself,
       if wherever such third-party acknowlegements normally appear.
 
   3.  Neither the names "clapper.org", "clapper.org Java Utility Library",
       nor any of the names of the project contributors may be used to
       endorse or promote products derived from this software without prior
       written permission. For written permission, please contact
       bmc@clapper.org.
 
   4.  Products derived from this software may not be called "clapper.org
       Java Utility Library", nor may "clapper.org" appear in their names
       without prior written permission of Brian M. Clapper.
 
   THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
   WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
   MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
   NO EVENT SHALL BRIAN M. CLAPPER BE LIABLE FOR ANY DIRECT, INDIRECT,
   INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
   NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
   DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
   THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
   (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
   THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 \*---------------------------------------------------------------------------*/
 
 package org.clapper.curn.plugins;
 
 import java.net.URL;
 import java.text.ParseException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import org.clapper.curn.CurnConfig;
 import org.clapper.curn.CurnException;
 import org.clapper.curn.CurnUtil;
 import org.clapper.curn.FeedCache;
 import org.clapper.curn.FeedCacheEntry;
 import org.clapper.curn.FeedConfigItemPlugIn;
 import org.clapper.curn.FeedInfo;
 import org.clapper.curn.ForceFeedDownloadPlugIn;
 import org.clapper.curn.MainConfigItemPlugIn;
 import org.clapper.curn.PostFeedParsePlugIn;
 import org.clapper.curn.parser.RSSChannel;
 import org.clapper.curn.parser.RSSItem;
 import org.clapper.util.classutil.ClassUtil;
 import org.clapper.util.config.ConfigurationException;
 import org.clapper.util.logging.Logger;
 import org.clapper.util.text.Duration;
 
 /**
 * The <tt>RetainArticlesPlugIn</tt> can be used to force articles in a feed
  * (or in all feeds) to be displayed more than once. It looks for a default
  * (main-configuration section) "ShowArticlesFor" parameter, and it permits
  * a per-feed "ShowArticlesFor" parameter to override the default. The
  * configuration parameter takes a time interval, expressed in a
  * natural language string. (The {@link IgnoreOldArticlesPlugIn} class uses
  * the same time interval form.) Examples:
  *
  * <ul>
  *   <li> 3 days
  *   <li> 1 week
  *   <li> 365 days
  *   <li> 12 hours, 30 minutes
  * </ul>
  *
  * Valid interval names (in English) are:
  *
  * <ul>
  *   <li> "millisecond", "milliseconds", "ms"
  *   <li> "second", "seconds", "sec", "secs"
  *   <li> "minutes", "minutes", "min", "mins"
  *   <li> "hour", "hours", "hr", "hrs"
  *   <li> "day", "days"
  *   <li> "week", "weeks"
  * </ul>
  *
  * <p>This plug-in uses the
  * <a href="http://www.clapper.org/software/java/util/">org.clapper.util</a>
 *  library's
  * <a href="http://www.clapper.org/software/java/util/javadocs/util/api/org/clapper/util/misc/Duration.html"><tt>Duration</tt></a>
  * class to parse the age/duration values. See that class for more details.</p>
  *
  *
  * <p>This plug-in intercepts the following configuration parameters.</p>
  *
  * <table border="1">
  *   <tr valign="top" align="left">
  *     <th>Section</th>
  *     <th>Parameter</th>
  *     <th>Meaning</th>
  *     <th>Default</th>
  *   </tr>
  *   <tr valign="top">
  *     <td><tt>[curn]</tt></td>
  *     <td><tt>ShowArticlesFor</tt></td>
  *     <td>Global default specifying how long to retain an article. Applies to
  *         all feeds that don't explicitly override this parameter.</td>
  *     <td>None. (Articles displayed only once.)</td>
  *   </tr>
  *   <tr valign="top">
  *     <td><tt>[Feed<i>xxx</i>]</tt></td>
  *     <td><tt>IgnoreArticlesOlderThan</tt></td>
  *     <td>Per-feed parameter specifying how long to retain an article.</td>
  *     <td>The global <tt>IgnoreArticlesOlderThan</tt> setting. If there is
  *         no global setting, then the default is to display articles only
  *         once.</td>
  *   </tr>
  * </table>
  *
  * <p><b>WARNING</b>: Beware of interactions with the
  * {@link IgnoreOldArticlesPlugIn} class. For instance, if you use
  * "ShowArticlesFor" to show articles for 5 days, but you also use
  * "IgnoreArticlesOlderThan" to discard articles older than 2 days,
  * the "IgnoreArticlesOlderThan parameter takes precedence.
  *
  * @version <tt>$Revision$</tt>
  */
 public class RetainArticlesPlugIn
     implements MainConfigItemPlugIn,
                FeedConfigItemPlugIn,
                ForceFeedDownloadPlugIn,
                PostFeedParsePlugIn
 {
     /*----------------------------------------------------------------------*\
                                Private Constants
     \*----------------------------------------------------------------------*/
 
     private static final String VAR_SHOW_ARTICLES_DURATION = "ShowArticlesFor";
 
     /*----------------------------------------------------------------------*\
                                Private Data Items
     \*----------------------------------------------------------------------*/
 
     /**
      * Feed duration data, by feed URL. This map contains configuration data.
      */
     private Map<URL,Duration> perFeedDuration =
         new HashMap<URL,Duration>();
 
     /**
      * The global default
      */
     private Duration globalDefault = null;
 
     /**
      * For logging
      */
     private static final Logger log = new Logger(RetainArticlesPlugIn.class);
 
     /*----------------------------------------------------------------------*\
                                    Constructor
     \*----------------------------------------------------------------------*/
 
     /**
      * Creates a new instance of <tt>RetainArticlesPlugIn</tt>
      */
     public RetainArticlesPlugIn()
     {
     }
 
     /*----------------------------------------------------------------------*\
                                 Public Methods
     \*----------------------------------------------------------------------*/
 
     /**
      * Get a displayable name for the plug-in.
      *
      * @return the name
      */
     public String getPlugInName()
     {
         return "Retain Articles";
     }
 
     /**
      * Get the sort key for this plug-in.
      *
      * @return the sort key string.
      */
     public String getPlugInSortKey()
     {
         return ClassUtil.getShortClassName(getClass().getName());
     }
 
     /**
      * Initialize the plug-in. This method is called before any of the
      * plug-in methods are called.
      *
      * @throws CurnException on error
      */
     public void initPlugIn()
         throws CurnException
     {
     }
 
     /**
      * Called immediately after <i>curn</i> has read and processed a
      * configuration item in the main [curn] configuration section. All
      * configuration items are passed, one by one, to each loaded plug-in.
      * If a plug-in class is not interested in a particular configuration
      * item, this method should simply return without doing anything. Note
      * that some configuration items may simply be variable assignment;
      * there's no real way to distinguish a variable assignment from a
      * blessed configuration item.
      *
      * @param sectionName  the name of the configuration section where
      *                     the item was found
      * @param paramName    the name of the parameter
      * @param config       the {@link CurnConfig} object
      *
      * @throws CurnException on error
      *
      * @see CurnConfig
      */
     public void runMainConfigItemPlugIn(String     sectionName,
                                         String     paramName,
                                         CurnConfig config)
         throws CurnException
     {
         try
         {
             if (paramName.equals (VAR_SHOW_ARTICLES_DURATION))
             {
                 try
                 {
                     String sDuration = config.getConfigurationValue(sectionName,
                                                                     paramName);
                     globalDefault.parse(sDuration);
                     log.debug("[" + sectionName + "] " + paramName + "=" +
                               globalDefault);
                 }
 
                 catch (ParseException ex)
                 {
                     throw new CurnException("Bad value for configuration " +
                                             "variable \"" + paramName + "\" " +
                                             "in section [" + sectionName + "]",
                                             ex);
                 }
 
             }
         }
 
         catch (ConfigurationException ex)
         {
             throw new CurnException (ex);
         }
     }
 
     /**
      * Called immediately after <i>curn</i> has read and processed a
      * configuration item in a "feed" configuration section. All
      * configuration items are passed, one by one, to each loaded plug-in.
      * If a plug-in class is not interested in a particular configuration
      * item, this method should simply return without doing anything. Note
      * that some configuration items may simply be variable assignment;
      * there's no real way to distinguish a variable assignment from a
      * blessed configuration item.
      *
      * @param sectionName  the name of the configuration section where
      *                     the item was found
      * @param paramName    the name of the parameter
      * @param config       the active configuration
      * @param feedInfo     partially complete <tt>FeedInfo</tt> object
      *                     for the feed. The URL is guaranteed to be
      *                     present, but no other fields are.
      *
      * @return <tt>true</tt> to continue processing the feed,
      *         <tt>false</tt> to skip it
      *
      * @throws CurnException on error
      *
      * @see CurnConfig
      * @see FeedInfo
      * @see FeedInfo#getURL
      */
     public boolean runFeedConfigItemPlugIn(String     sectionName,
                                            String     paramName,
                                            CurnConfig config,
                                            FeedInfo   feedInfo)
         throws CurnException
     {
         try
         {
             if (paramName.equals (VAR_SHOW_ARTICLES_DURATION))
             {
                 try
                 {
                     String sDuration = config.getConfigurationValue(sectionName,
                                                                     paramName);
                     Duration duration = new Duration(sDuration);
                     URL feedURL = CurnUtil.normalizeURL(feedInfo.getURL());
                     perFeedDuration.put(feedURL, duration);
                     if (log.isDebugEnabled())
                     {
                         log.debug("[" + sectionName + "] (" +
                                   feedURL.toString() + ") " + paramName + "=" +
                                   duration + " (" + duration.format() + ")");
                     }
                 }
 
                 catch (ParseException ex)
                 {
                     log.error(ex);
                     throw new CurnException("Bad value for configuration " +
                                             "variable \"" + paramName + "\" " +
                                             "in section [" + sectionName + "]",
                                             ex);
                 }
             }
         }
 
         catch (ConfigurationException ex)
         {
             throw new CurnException (ex);
         }
 
         return true;
     }
     /**
      * This method determines (based on some internal criteria) whether
      * a given feed should be downloaded even if it hasn't changed. If multiple
      * plug-ins implement this interface, then only one needs to return
      * <tt>true</tt> for the feed download to be forced.
      *
      * @param feedInfo  the {@link FeedInfo} object for the feed that
      *                  has been downloaded and parsed.
      * @param feedCache the feed cache, or null if there isn't one
      *
      * @return <tt>true</tt> if the feed should be downloaded and parsed
      *         even if it's not out of date; <tt>false</tt> if <i>curn</i>'s
      *         normal downloading rules should apply.
      *
      * @throws CurnException on error
      */
     public boolean forceFeedDownload(FeedInfo feedInfo, FeedCache feedCache)
         throws CurnException
     {
         URL feedURL = CurnUtil.normalizeURL(feedInfo.getURL());
         Duration duration = perFeedDuration.get(feedURL);
         if (duration == null)
             duration = globalDefault;
 
         return (duration != null);
     }
 
     /**
      * Called immediately after a feed is parsed, but before it is
      * otherwise processed. This method can return <tt>false</tt> to signal
      * <i>curn</i> that the feed should be skipped. For instance, a plug-in
      * that filters on the parsed feed data could use this method to weed
      * out non-matching feeds before they are downloaded. Similarly, a
      * plug-in that edits the parsed data (removing or editing individual
      * items, for instance) could use method to do so.
      *
      * @param feedInfo  the {@link FeedInfo} object for the feed that
      *                  has been downloaded and parsed.
      * @param feedCache the feed cache
      * @param channel   the parsed channel data
      *
      * @return <tt>true</tt> if <i>curn</i> should continue to process the
      *         feed, <tt>false</tt> to skip the feed. A return value of
      *         <tt>false</tt> aborts all further processing on the feed.
      *         In particular, <i>curn</i> will not pass the feed along to
      *         other plug-ins that have yet to be notified of this event.
      *
      * @throws CurnException on error
      *
      * @see RSSChannel
      * @see FeedInfo
      */
     public boolean runPostFeedParsePlugIn(FeedInfo   feedInfo,
                                           FeedCache  feedCache,
                                           RSSChannel channel)
         throws CurnException
     {
         URL feedURL = CurnUtil.normalizeURL(feedInfo.getURL());
         log.debug("Checking parsed feed \"" + feedURL.toString() + "\"");
         Duration duration = perFeedDuration.get(feedURL);
         if (duration == null)
             duration = globalDefault;
 
         if (duration != null)
         {
             String feedURLString = feedURL.toString();
             String sDuration = duration.format();
             long durationMillis = duration.getDuration();
 
             log.debug("Articles in feed " + feedURL + " should be shown for " +
                       sDuration);
 
             long now = System.currentTimeMillis();
             for (RSSItem item : channel.getItems())
             {
                 FeedCacheEntry entry = null;
                 long itemCacheTime = now;
                 if (feedCache != null)
                 {
                     entry = feedCache.getEntryForItem(item);
                     if (entry != null)
                         itemCacheTime = entry.getTimestamp();
                 }
 
                 long itemAge = now - itemCacheTime;
 
                 // Account for articles dated in the future. (There's no
                 // reason some doofus feed couldn't do that. And then there's
                 // always machine clock-skew.)
 
                 if (itemAge < 0)
                     itemAge = 0;
 
                 // Has the item passed the duration to be shown?
 
                 Date cacheDate = new Date(itemCacheTime);
                 if (itemAge > durationMillis)
                 {
                     log.info("In feed " + feedURLString + ", article " +
                              item.getURL() + " was cached " + cacheDate +
                              ", which is more than " + sDuration + ". " +
                              "Suppressing article.");
                     channel.removeItem(item);
                 }
 
                else if (feedCache == null)
                {
                    log.info("No cache. Retaining article by default.");
                }

                 else
                 {
                     entry = feedCache.getEntryForItem(item);
                     if (entry != null)
                     {
                         log.info("In feed " + feedURLString +
                                  ", previously seen article " +
                                   item.getURL() + " was cached " + cacheDate +
                                  ", which is less than " + sDuration + ". " +
                                  "Showing article again.");
                         entry.setSticky(true);
                     }
                 }
             }
         }
 
         return true;
     }
 
     /*----------------------------------------------------------------------*\
                                Protected Methods
     \*----------------------------------------------------------------------*/
 
     /*----------------------------------------------------------------------*\
                                 Private Methods
     \*----------------------------------------------------------------------*/
 }
