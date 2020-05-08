 /*---------------------------------------------------------------------------*\
   $Id$
 \*---------------------------------------------------------------------------*/
 
 package org.clapper.rssget;
 
 import java.io.File;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.Serializable;
 
 import java.net.URL;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Date;
 import java.util.Iterator;
 
 /**
  * Defines the in-memory format of the <i>rssget</i> cache, and provides
  * methods for saving and restoring the cache.
  *
  * @see rssget
  * @see org.clapper.rssget.parser.RSSChannel
  *
  * @version <tt>$Revision$</tt>
  */
 public class RSSGetCache implements Serializable
 {
     /*----------------------------------------------------------------------*\
                            Private Instance Data
     \*----------------------------------------------------------------------*/
 
     /**
      * Where to emit verbose messages
      */
     private VerboseMessagesHandler vh;
 
     /**
      * The configuration
      */
     private RSSGetConfiguration config;
 
     /**
      * The actual cache
      */
     private Map cacheMap;
 
     /**
      * Whether or not the cache has been modified since saved or loaded
      */
     private boolean modified = false;
 
     /**
      * Current time
      */
     private long currentTime = System.currentTimeMillis();
 
     /*----------------------------------------------------------------------*\
                                 Constructor
     \*----------------------------------------------------------------------*/
 
     /**
      * Construct a new, empty cache object.
      *
      * @param verboseHandler  the verbose messages handler to use
      * @param config          the <i>rssget</i> configuration
      */
     RSSGetCache (VerboseMessagesHandler vh,
                  RSSGetConfiguration    config)
     {
         this.vh     = vh;
         this.config = config;
     }
 
     /*----------------------------------------------------------------------*\
                               Public Methods
     \*----------------------------------------------------------------------*/
 
     /**
      * Load the cache from the file specified in the configuration. If the
      * file doesn't exist, this method quietly returns.
      *
      * @throws IOException  unable to read cache
      */
     public void loadCache()
         throws IOException
     {
         File cacheFile = config.getCacheFile();
 
         vh.verbose (3, "Reading cache from \"" + cacheFile.getPath() + "\"");
 
         if (! cacheFile.exists())
         {
             vh.verbose (2,
                         "Cache \""
                       + cacheFile.getPath()
                       + "\" doesn't exist.");
             this.cacheMap = new HashMap();
         }
 
         else
         {
             ObjectInputStream objIn = new ObjectInputStream
                                            (new FileInputStream (cacheFile));
 
             try
             {
                 this.cacheMap = (Map) objIn.readObject();
                 pruneCache();
                 modified = false;
             }
 
             catch (ClassNotFoundException ex)
             {
                 throw new IOException (ex.toString());
             }
 
             finally
             {
                 objIn.close();
             }
         }
     }
 
     /**
      * Attempt to save the cache back to disk. Does nothing if the cache
      * hasn't been modified since it was saved.
      *
      * @throws IOException  unable to write cache
      */
     public void saveCache()
         throws IOException
     {
         if (this.modified)
         {
             File cacheFile = config.getCacheFile();
 
             ObjectOutputStream objOut = new ObjectOutputStream
                                            (new FileOutputStream (cacheFile));
 
             vh.verbose (3, "Saving cache to \"" + cacheFile.getPath() + "\"");
             objOut.writeObject (cacheMap);
             objOut.close();
             this.modified = false;
         }
     }
 
     /**
      * Determine whether a given URL is cached.
      *
      * @param url  the URL to check. The URL should already have
      *             been normalized.
      *
      * @return <tt>true</tt> if cached, <tt>false</tt> if not
      *
     * @see Util#normalizeURL(URL)
      */
     public boolean containsURL (URL url)
     {
         String key = url.toExternalForm();
         vh.verbose (3,
                     "Cache contains \""
                   + key
                   + "\"? "
                   + cacheMap.containsKey (key));
         return cacheMap.containsKey (key);
     }
 
     /**
      * Get an item from the cache.
      *
      * @param url  the URL, which must be normalized.
      *
      * @return the corresponding <tt>RSSCacheEntry</tt> object, or null if
      *         not found
      *
     * @see Util#normalizeURL(URL)
      */
     public RSSCacheEntry getItem (URL url)
     {
         return (RSSCacheEntry) cacheMap.get (url.toExternalForm());
     }
 
     /**
      * Add (or replace) a cached URL.
      *
      * @param url        the URL to cache. May be an individual item URL, or
      *                   the URL for an entire feed.
      * @param parentFeed the associated feed
      *
      * @see Util#normalizeURL
      */
     public void addToCache (URL url, RSSFeedInfo parentFeed)
     {
         URL parentURL = parentFeed.getURL();
         RSSCacheEntry entry = new RSSCacheEntry (parentURL,
                                                  url,
                                                  System.currentTimeMillis());
 
         vh.verbose (3,
                     "Adding cache entry for URL \""
                   + entry.getEntryURL().toExternalForm()
                   + "\". Channel URL: \""
                   + entry.getChannelURL().toExternalForm()
                   + "\"");
         cacheMap.put (url.toExternalForm(), entry);
         modified = true;
     }
 
     /**
      * Set the cache's notion of the current time, which affects how elements
      * are pruned when loaded from the cache. Only meaningful if set before
      * the <tt>loadCache()</tt> method is called. If this method is never
      * called, then the cache uses the current time.
      *
      * @param datetime  the time to use
      */
     public void setCurrentTime (Date datetime)
     {
         this.currentTime = datetime.getTime();
     }
 
     /*----------------------------------------------------------------------*\
                               Private Methods
     \*----------------------------------------------------------------------*/
 
     /**
      * Prune the loaded cache of out-of-date data.
      */
     private void pruneCache()
     {
         vh.verbose (3,
                     "Cache's notion of current time: "
                   + new Date (currentTime));
 
         for (Iterator itKeys = cacheMap.keySet().iterator();
              itKeys.hasNext(); )
         {
             String itemUrlString = (String) itKeys.next();
             RSSCacheEntry entry = (RSSCacheEntry) cacheMap.get (itemUrlString);
             URL channelURL = entry.getChannelURL();
 
             vh.verbose (3, "Checking cached URL \"" + itemUrlString + "\"");
             vh.verbose (3, "    Channel URL: " + channelURL.toString());
 
             RSSFeedInfo feedInfo = config.getFeedInfoFor (channelURL);
 
             if (feedInfo == null)
             {
                 // Cached URL no longer corresponds to a configured site
                 // URL. Kill it.
 
                 vh.verbose (2,
                             "Cached URL \""
                           + itemUrlString
                           + "\", with base URL \""
                           + channelURL.toString()
                           + "\" no longer corresponds to a configured feed. "
                           + "tossing it.");
                 itKeys.remove();
             }
 
             else
             {
                 long timestamp  = entry.getTimestamp();
                 long maxCacheMS = feedInfo.getMillisecondsToCache();
                 long expires    = timestamp + maxCacheMS;
 
                 vh.verbose (3,
                             "\tcached on:  "
                           + new Date (timestamp).toString());
                 vh.verbose (3,
                             "\tcache days: " + feedInfo.getDaysToCache());
                 vh.verbose (3,
                             "\tcache ms:   " + maxCacheMS);
                 vh.verbose (3, "\texpires: " + new Date (expires).toString());
 
                 if (timestamp > currentTime)
                 {
                     vh.verbose (2,
                                 "Cache time for URL \""
                               + itemUrlString
                               + "\" is in the future, relative to cache's "
                               + "notion of current time. Deleting cache "
                               + "entry.");
                     itKeys.remove();
                 }
 
                 else if (expires < currentTime)
                 {
                     vh.verbose (2,
                                 "Cache time for URL \""
                               + itemUrlString
                               + "\" has expired. Deleting cache entry.");
                     itKeys.remove();
                 }
             }
         }
     }
 }
