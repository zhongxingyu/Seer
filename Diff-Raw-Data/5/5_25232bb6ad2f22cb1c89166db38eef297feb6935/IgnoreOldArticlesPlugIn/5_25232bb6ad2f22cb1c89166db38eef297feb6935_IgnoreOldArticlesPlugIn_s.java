 /*---------------------------------------------------------------------------*\
   $Id: IgnoreOldArticlesPlugIn.java 6688 2007-04-17 22:52:19Z bmc $
   ---------------------------------------------------------------------------
   This software is released under a BSD-style license:
 
   Copyright (c) 2004-2007 Brian M. Clapper. All rights reserved.
 
   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions are
   met:
 
   1.  Redistributions of source code must retain the above copyright notice,
       this list of conditions and the following disclaimer.
 
   2.  The end-user documentation included with the redistribution, if any,
       must include the following acknowlegement:
 
         "This product includes software developed by Brian M. Clapper
         (bmc@clapper.org, http://www.clapper.org/bmc/). That software is
         copyright (c) 2004-2007 Brian M. Clapper."
 
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
 
 import java.text.ParseException;
 import java.util.Date;
 import org.clapper.curn.CurnConfig;
 import org.clapper.curn.CurnException;
 import org.clapper.curn.FeedInfo;
 import org.clapper.curn.FeedConfigItemPlugIn;
 import org.clapper.curn.PostFeedParsePlugIn;
 import org.clapper.curn.MainConfigItemPlugIn;
 import org.clapper.curn.parser.RSSChannel;
 import org.clapper.curn.parser.RSSItem;
 
 import org.clapper.util.classutil.ClassUtil;
 import org.clapper.util.config.ConfigurationException;
 import org.clapper.util.logging.Logger;
 
 import java.util.HashMap;
 import java.util.Map;
 import org.clapper.util.misc.Duration;
 
 /**
  * The <tt>IgnoreOldArticlesPlugIn</tt> provides a way to ignore articles that
  * are older than a certain interval. It can be applied globally and overridden
  * on a per-feed basis, or it can be applied only for selected feeds. This
  * plug-in is especially useful when:
  *
  * <ul>
  *   <li> You've added a new feed, but you don't want to see every article
  *        ever published by that site.
  *   <li> A feed has been out of commission for awhile and is coming back online.
  *   <li> A feed's URL changes (which looks like a new feed to <i>curn</i>).
  * </ul>
  *
  * Age is specified using natural language strings that express durations.
  * Examples:
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
 * {@link http://www.clapper.org/software/java/util/ org.clapper.util} library's
 * {@link http://www.clapper.org/software/java/util/javadocs/util/api/org/clapper/util/misc/Duration.html Duration}
  * class to parse the age/duration values. See that class for more details.</p>
  *
  * <p>For this plug-in to work for a feed, the feed's articles must be tagged
  * with valid, parseable dates. If an article does not have a date, then the
  * current date is assumed.</p>n
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
  *     <td><tt>IgnoreArticlesOlderThan</tt></td>
  *     <td>Global default specifying how an article must be before this plug-in
  *         will discard it. Applies to all feeds that don't explicitly override
  *         this parameter.</td>
  *     <td>None. (Articles aren't suppressed by age.)</td>
  *   </tr>
  *   <tr valign="top">
  *     <td><tt>[Feed<i>xxx</i>]</tt></td>
  *     <td><tt>IgnoreArticlesOlderThan</tt></td>
  *     <td>Per-feed parameter specifying how an article must be before this
  *         plug-in will discard it.</td>
  *     <td>The global <tt>IgnoreArticlesOlderThan</tt> setting. If there is
  *         no global setting, then the default is not to suppress articles
  *         based on age.</td>
  *   </tr>
  * </table>
  *
  * @version <tt>$Revision: 6688 $</tt>
  */
 public class IgnoreOldArticlesPlugIn
     implements MainConfigItemPlugIn,
                FeedConfigItemPlugIn,
                PostFeedParsePlugIn
 {
     /*----------------------------------------------------------------------*\
                              Private Constants
     \*----------------------------------------------------------------------*/
 
     public static final String VAR_IGNORE_OLD_ARTICLES =
         "IgnoreArticlesOlderThan";
 
     /*----------------------------------------------------------------------*\
                                Inner Classes
     \*----------------------------------------------------------------------*/
 
     /*----------------------------------------------------------------------*\
                             Private Data Items
     \*----------------------------------------------------------------------*/
 
     /**
      * Feed save data, by feed
      */
     private Map<FeedInfo,Duration> perFeedSetting =
         new HashMap<FeedInfo,Duration>();
 
     /**
      * The global default
      */
     private Duration globalDefault = null;
 
     /**
      * For log messages
      */
     private static final Logger log = new Logger (IgnoreOldArticlesPlugIn.class);
 
     /*----------------------------------------------------------------------*\
                                 Constructor
     \*----------------------------------------------------------------------*/
 
     /**
      * Default constructor (required).
      */
     public IgnoreOldArticlesPlugIn()
     {
         // Nothing to do
     }
 
     /*----------------------------------------------------------------------*\
                Public Methods Required by *PlugIn Interfaces
     \*----------------------------------------------------------------------*/
 
     /**
      * Get a displayable name for the plug-in.
      *
      * @return the name
      */
     public String getPlugInName()
     {
         return "Ignore Old Articles";
     }
 
     /**
      * Get the sort key for this plug-in.
      *
      * @return the sort key string.
      */
     public String getPlugInSortKey()
     {
         return ClassUtil.getShortClassName (getClass().getName());
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
             if (paramName.equals (VAR_IGNORE_OLD_ARTICLES))
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
             if (paramName.equals (VAR_IGNORE_OLD_ARTICLES))
             {
                 try
                 {
                     String sDuration = config.getConfigurationValue(sectionName,
                                                                     paramName);
                     Duration duration = new Duration(sDuration);
                     perFeedSetting.put(feedInfo, duration);
                     if (log.isDebugEnabled())
                     {
                         log.debug("[" + sectionName + "] " + paramName + "=" +
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
                                           RSSChannel channel)
         throws CurnException
     {
         Duration duration = perFeedSetting.get(feedInfo);
         if (duration == null)
             duration = globalDefault;
 
         if (duration != null)
         {
             String feedURL = feedInfo.getURL().toString();
             String sDuration = log.isDebugEnabled() ? duration.format() : null;
             long durationMillis = duration.getDuration();
 
             log.debug("Ignoring all articles in " + feedURL + " older than " +
                       sDuration);
 
             long now = System.currentTimeMillis();
             for (RSSItem item : channel.getItems())
             {
                 Date itemDate = item.getPublicationDate();
                 if (itemDate == null)
                 {
                     log.debug("Item " + item.getURL() + " has no date. " +
                               "Assuming it's current. NOT ignoring it.");
                     continue;
                 }
 
                 long itemDateMillis = itemDate.getTime();
                 long itemDateAgeMillis = now - itemDateMillis;
 
                 // Account for articles dated in the future. (There's no
                 // reason some doofus feed couldn't do that. And then there's
                 // always machine clock-skew.)
 
                 if (itemDateAgeMillis < 0)
                     itemDateAgeMillis = 0;
 
                 // Is the feed older than permitted?
 
                 if (itemDateAgeMillis > durationMillis)
                 {
                     log.info("In feed " + feedURL + ", article " +
                              item.getURL() + " is dated " + itemDate +
                              ", which is older than " + sDuration + ". " +
                              "Suppressing article.");
                     channel.removeItem(item);
                 }
             }
         }
 
         return true;
     }
 }
