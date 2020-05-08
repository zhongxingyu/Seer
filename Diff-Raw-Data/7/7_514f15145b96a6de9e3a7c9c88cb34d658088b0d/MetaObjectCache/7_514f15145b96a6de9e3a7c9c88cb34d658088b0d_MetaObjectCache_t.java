 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package Sirius.navigator.tools;
 
 import Sirius.navigator.connection.SessionManager;
import Sirius.navigator.connection.proxy.ConnectionProxy;
 import Sirius.navigator.exception.ConnectionException;
 
 import Sirius.server.middleware.types.MetaObject;
 
 import org.apache.log4j.Logger;
 
 import java.lang.ref.SoftReference;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 /**
  * DOCUMENT ME!
  *
  * @author   therter
  * @version  $Revision$, $Date$
  */
 public class MetaObjectCache {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final transient Logger LOG = Logger.getLogger(MetaObjectCache.class);
 
     //~ Instance fields --------------------------------------------------------
 
     private final transient ReentrantReadWriteLock rwLock;
     // we're soft-referencing the whole array so that we can be sure that the whole array will be collected and not only
     // single items of the array. if there is a need for additional cache access methods we'll have to change the data
     // store anyway
     private final transient Map<Integer, SoftReference<MetaObject[]>> cache;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new MetaSearchCache object.
      */
     private MetaObjectCache() {
         rwLock = new ReentrantReadWriteLock();
         cache = new HashMap<Integer, SoftReference<MetaObject[]>>();
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static MetaObjectCache getInstance() {
         return LazyInitialiser.INSTANCE;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param       query  DOCUMENT ME!
      *
      * @return      DOCUMENT ME!
      *
      * @deprecated  use {@link #getMetaObjectsByQuery(java.lang.String)} instead
      */
     public MetaObject[] get(final String query) {
         try {
             rwLock.readLock().lock();
 
             final SoftReference<MetaObject[]> objs = cache.get(query.intern().hashCode());
 
             return (objs == null) ? null : objs.get();
         } finally {
             rwLock.readLock().unlock();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param       query  DOCUMENT ME!
      * @param       value  DOCUMENT ME!
      *
      * @deprecated  altering cache entries manually is highly discouraged
      */
     public void put(final String query, final MetaObject[] value) {
         try {
             rwLock.writeLock().lock();
 
             cache.put(query.intern().hashCode(), new SoftReference<MetaObject[]>(value));
         } finally {
             rwLock.writeLock().unlock();
         }
     }
 
     /**
      * Completely wipes the cache content.
      */
     public void clearCache() {
         try {
             rwLock.writeLock().lock();
 
             cache.clear();
         } finally {
             rwLock.writeLock().unlock();
         }
     }
 
     /**
      * Wipes the cache content for the given query and returns the cache's original content.
      *
      * @param   query  the query for which the cache shall be cleared
      *
      * @return  DOCUMENT ME!
      */
     public MetaObject[] clearCache(final String query) {
         try {
             rwLock.writeLock().lock();
            
            final SoftReference<MetaObject[]> objs = cache.put(query.intern().hashCode(), null);
 
            return objs == null ? null : objs.get();
         } finally {
             rwLock.writeLock().unlock();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param       query  DOCUMENT ME!
      *
      * @return      DOCUMENT ME!
      *
      * @deprecated  use {@link #getMetaObjectsByQuery(java.lang.String)} instead
      */
     public MetaObject[] getMetaObjectByQuery(final String query) {
         try {
             return getMetaObjectsByQuery(query, false);
         } catch (final CacheException e) {
             // this is only to maintain compliance with the previous API behaviour
             LOG.warn("exception in cache, returning empty array", e); // NOI18N
 
             return new MetaObject[0];
         }
     }
 
     /**
      * Get {@link MetaObject}s by query using the very same query string as one would request them from the server. This
      * operation simply calls {@link #getMetaObjectsByQuery(java.lang.String, boolean)} with the given query and <code>
      * false</code> for force reload. It throws an exception to indicate any errors so that it can maintain compliance
      * with a call to {@link ConnectionProxy#getMetaObjectByQuery(java.lang.String, int)} with regards to its return
      * value.
      *
      * @param   query  the query to get the <code>MetaObject</code>s for
      *
      * @return  an array of <code>MetaObject</code>s as they would have been returned from
      *          ConnectionProxy#getMetaObjectByQuery(java.lang.String, int) if used directly
      *
      * @throws  CacheException  if any error occurs, e.g. the server is not reachable if the cache is empty
      *
      * @see     #getMetaObjectsByQuery(java.lang.String, boolean)
      */
     public MetaObject[] getMetaObjectsByQuery(final String query) throws CacheException {
         return getMetaObjectsByQuery(query, false);
     }
 
     /**
      * Get {@link MetaObject}s by query using the very same query string as one would request them from the server. This
      * operation supports forcing of a reload so that callers are assured to receive a current result as it would have
      * been returned from {@link ConnectionProxy#getMetaObjectByQuery(java.lang.String, int)}. It throws an exception to
      * indicate any errors so that it can maintain compliance with a call to
      * {@link ConnectionProxy#getMetaObjectByQuery(java.lang.String, int)} with regards to its return value.
      *
      * @param   query        the query to get the <code>MetaObject</code>s for
      * @param   forceReload  force a reload of the <code>MetaObject</code>s if they have already been cached
      *
      * @return  an array of <code>MetaObject</code>s as they would have been returned from
      *          {@link ConnectionProxy#getMetaObjectByQuery(java.lang.String, int)} if used directly
      *
      * @throws  CacheException  if any error occurs, e.g. the server is not reachable if the cache is empty or was
      *                          forced to reload
      *
      * @see     ConnectionProxy#getMetaObjectByQuery(java.lang.String, int)
      */
     public MetaObject[] getMetaObjectsByQuery(final String query, final boolean forceReload) throws CacheException {
         if (query == null) {
             return null;
         }
 
         final String iQuery = query.intern();
         final Integer qHash = iQuery.hashCode();
         MetaObject[] cachedObjects = null;
         Lock lock = null;
         try {
             lock = rwLock.readLock();
             lock.lock();
 
             SoftReference<MetaObject[]> objs = cache.get(qHash);
             cachedObjects = (objs == null) ? null : objs.get();
 
             if ((cachedObjects == null) || forceReload) {
                 lock.unlock();
                 lock = rwLock.writeLock();
                 lock.lock();
 
                 final boolean wasEmpty = cachedObjects == null;
 
                 // somebody may have aquired the write lock in the meantime and we're late
                 objs = cache.get(qHash);
                 cachedObjects = (objs == null) ? null : objs.get();
 
                 // if this is the case we truly have to load it because there are no objects or there have already been
                 // objects in the cache but a reload is forced. in case of the write lock was aquired in the mean time
                 // and the cache has been filled by another thread and the forceReload is true, we don't want to reload
                 // (because it has already been done by the other thread)
                 if ((cachedObjects == null) || (!wasEmpty && forceReload)) {
                     try {
                         if (LOG.isDebugEnabled()) {
                             LOG.debug("loading metaobjects: " // NOI18N
                                         + "[query=" + iQuery // NOI18N
                                         + "|qHash=" + qHash // NOI18N
                                         + "|cachedObjects=" + cachedObjects // NOI18N
                                         + "|wasEmpty=" + wasEmpty // NOI18N
                                         + "|forceReload=" + forceReload // NOI18N
                                         + "]");         // NOI18N
                         }
 
                         cachedObjects = SessionManager.getProxy().getMetaObjectByQuery(iQuery, 0);
                         cache.put(qHash, new SoftReference<MetaObject[]>(cachedObjects));
                     } catch (final ConnectionException ex) {
                         final String message = "cannot fetch meta objects for query: " + iQuery; // NOI18N
                         LOG.error(message, ex);
                         throw new CacheException(iQuery, message, ex);
                     }
                 }
             }
         } finally {
             if (lock != null) {
                 lock.unlock();
             }
         }
 
         return cachedObjects;
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private static final class LazyInitialiser {
 
         //~ Static fields/initializers -----------------------------------------
 
         private static final MetaObjectCache INSTANCE = new MetaObjectCache();
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new LazyInitialiser object.
          */
         private LazyInitialiser() {
         }
     }
 }
