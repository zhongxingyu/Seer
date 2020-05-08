 /*---------------------------------------------------------------------------*\
   $Id$
   ---------------------------------------------------------------------------
   This software is released under a Berkeley-style license:
 
   Copyright (c) 2004-2006 Brian M. Clapper. All rights reserved.
 
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
 
 import java.net.URL;
 import java.io.File;
 
 import org.clapper.curn.parser.RSSChannel;
 
 /**
  * <p>Contains data for one feed (or site). The data about the feed
  * comes from the configuration file. The feed itself comes from parsing
  * the RSS data.</p>
  *
  * @see CurnConfig
  *
  * @version <tt>$Revision$</tt>
  */
 public class FeedInfo
 {
     /*----------------------------------------------------------------------*\
                              Public Constants
     \*----------------------------------------------------------------------*/
 
     /**
      * Default encoding for "save as" file.
      */
     public static final String DEFAULT_SAVE_AS_ENCODING = "utf-8";
 
     /*----------------------------------------------------------------------*\
                              Private Constants
     \*----------------------------------------------------------------------*/
 
     /*----------------------------------------------------------------------*\
                             Private Data Items
     \*----------------------------------------------------------------------*/
 
     private boolean     summaryOnly           = false;
     private boolean     enabled               = true;
     private int         daysToCache           = 0;
     private String      titleOverride         = null;
     private URL         siteURL               = null;
     private boolean     ignoreDuplicateTitles = false;
     private RSSChannel  parsedChannelData     = null;
     private String      forcedEncoding        = null;
 
     /*----------------------------------------------------------------------*\
                                 Constructor
     \*----------------------------------------------------------------------*/
 
     /**
      * Default constructor.
      * 
      * @param siteURL  the main URL for the site's RSS feed. This constructor
      *                 normalizes the URL.
     * @see CurnUtil#normalizeURL
      */
     public FeedInfo (URL siteURL)
     {
         this.siteURL = CurnUtil.normalizeURL (siteURL);
     }
 
     /*----------------------------------------------------------------------*\
                               Public Methods
     \*----------------------------------------------------------------------*/
 
     /**
      * Get the hash code for this feed
      *
      * @return the hash code
      */
     public int hashCode()
     {
         return getURL().hashCode();
     }
 
     /**
      * Determine whether this <tt>FeedInfo</tt> object is equivalent to
      * another one, based on the URL.
      *
      * @param obj  the other object
      *
      * @return <tt>true</tt> if <tt>obj</tt> is a <tt>FeedInfo</tt> object
      *         that specifies the same URL, <tt>false</tt> otherwise
      */
     public boolean equals (Object obj)
     {
         boolean eq = false;
 
         if (obj instanceof FeedInfo)
             eq = this.siteURL.equals (((FeedInfo) obj).siteURL);
 
         return eq;
     }
 
     /**
      * Get the main RSS URL for the site.
      * 
      * @return the site's main RSS URL, guaranteed to be normalized
     * @see CurnUtil#normalizeURL
      */
     public URL getURL()
     {
         return siteURL;
     }
 
     /**
      * Get the number of days that URLs from this site are to be cached.
      *
      * @return the number of days to cache URLs from this site.
      *
      * @see #setDaysToCache
      */
     public int getDaysToCache()
     {
         return daysToCache;
     }
 
     /**
      * Get the number of milliseconds that URLs from this site are to be
      * cached. This is a convenience front-end to <tt>getDaysToCache()</tt>.
      *
      * @return the number of milliseconds to cache URLs from this site
      *
      * @see #getDaysToCache
      * @see #setDaysToCache
      */
     public long getMillisecondsToCache()
     {
         long days = (long) getDaysToCache();
         return days * 25 * 60 * 60 * 1000;
     }
    
     /**
      * Set the "days to cache" value.
      *
      * @param cacheDays  new value
      *
      * @see #getDaysToCache
      * @see #getMillisecondsToCache
      */
     public void setDaysToCache (int cacheDays)
     {
         this.daysToCache = cacheDays;
     }
 
     /**
      * Get the forced character set encoding for this feed. If this
      * parameter is set, <i>curn</i> will ignore the character set encoding
      * advertised by the remote server (if any), and use the character set
      * specified by this configuration item instead. This is useful in the
      * following cases:
      *
      * <ul>
      *   <li>the remote HTTP server doesn't supply an HTTP Content-Encoding
      *       header, and the local (Java) default encoding doesn't match
      *       the document's encoding
      *   <li>the remote HTTP server supplies the wrong encoding
      * </ul>
      *
      * @return the forced character set encoding, or null if not configured
      */
     public String getForcedCharacterEncoding()
     {
         return forcedEncoding;
     }
      
     /**
      * Get the parsed channel data for this feed. This field is set by the
      * main processing logic and does not come from the configuration.
      *
      * @return the <tt>RSSChannel</tt> object representing the current
      *         parsed data from this feed, or null if the data has not been
      *         parsed yet.
      *
      * @see #setParsedChannelData
      * @see Curn
      * @see RSSChannel
      */
     public RSSChannel getParsedChannelData()
     {
         return parsedChannelData;
     } 
 
     /*----------------------------------------------------------------------*\
                           Package-visible Methods
     \*----------------------------------------------------------------------*/
 
     /**
      * Set the parsed channel data for this feed. This field is set by the
      * main processing logic and does not come from the configuration.
      *
      * @param channel the <tt>RSSChannel</tt> object representing the current
      *                parsed data from this feed, or null if not set
      *
      * @see #getParsedChannelData
      * @see Curn
      * @see RSSChannel
      */
     void setParsedChannelData (RSSChannel channel)
     {
         this.parsedChannelData = channel;
     }
     /*
      * Set the forced character set encoding for this feed.
      *
      * @param encoding the encoding
      *
      * @see #getForcedCharacterEncoding
      */
     public void setForcedCharacterEncoding (String encoding)
     {
         this.forcedEncoding = encoding;
     }
 }
