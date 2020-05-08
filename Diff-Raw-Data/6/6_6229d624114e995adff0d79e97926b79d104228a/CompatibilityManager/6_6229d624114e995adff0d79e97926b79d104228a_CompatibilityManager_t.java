 package org.jboss.pressgang.ccms.server.webdav.managers;
 
 import javax.annotation.Nullable;
 import javax.enterprise.context.ApplicationScoped;
 import javax.validation.constraints.NotNull;
 import java.util.*;
 import java.util.concurrent.TimeUnit;
 
 import com.google.common.cache.Cache;
 import com.google.common.cache.CacheBuilder;
 import com.google.common.cache.LoadingCache;
 import org.jboss.pressgang.ccms.server.webdav.constants.WebDavConstants;
 
 /**
  * There are a number of cases where working with database fields is not the same as working with files. This class
  * provides work around for these edge cases.
  *
  * Database fields can not be deleted, but when they are exposed as files, some applications expect to be able to delete
  * them (e.g. Kate will delete the file it is editing, and then check to make sure it is deleted before saving any changes).
  * This manager simply keeps a track of delete requests, and shows a file as being deleted for a short period of time,
  * or until it is "created" again.
  *
  * Saving content will quite often update the content as part of the save process. e.g. Topic's XML will be formatted
  * and the title changed upon save. This results in annoying messages in text editors about updated files, and usually
  * a second or two after the save was completed (with plenty of time to start typing again; changes that will be lost when the
  * file is reloaded).
  *
  * The following workaround has been implemented:
  *
  * 1.   Text editor saves XML
  * 2.   WebDAV saves XML as formatted text in the database
  * 3.   Text editor gets XML
  * 4.   a) If the text in the database is the same as the result of the save in step 1, the original, unformatted text is returned.
  *         The text editor does not see that the XML was actually formatted when it was saved.
  *      b) If the text in the database is different, it is returned as is.
  *
  */
 @ApplicationScoped
 public class CompatibilityManager {
     /**
      * Used to override the last modified date for resources that were deleted and then created.
      */
     final Cache<ResourceData, Date> createdResources = CacheBuilder.newBuilder()
             .expireAfterWrite(WebDavConstants.DELETE_WINDOW, TimeUnit.SECONDS)
             .build();
     /**
      * Used to identify resources that were marked as deleted.
      */
     final Cache<ResourceData, Date> deletedResources = CacheBuilder.newBuilder()
             .expireAfterWrite(WebDavConstants.DELETE_WINDOW, TimeUnit.SECONDS)
             .build();
     /**
      * Used to return the unformatted contents of a file.
      */
     final Cache<ResourceData, DataCache> databaseCache = CacheBuilder.newBuilder()
             .expireAfterWrite(WebDavConstants.DELETE_WINDOW, TimeUnit.SECONDS)
             .build();
 
     public CompatibilityManager() {
 
     }
 
     /**
      * @param resourceType      The type of resource
      * @param remoteAddress     The clients remote address
      * @param id                The id of the resource
      * @return true if this resource is supposed to be deleted, and false otherwise
      */
     synchronized public boolean isDeleted(@NotNull final ResourceTypes resourceType, @NotNull final String remoteAddress, @NotNull final Integer id) {
 
         final ResourceData resourceData = new ResourceData(resourceType, remoteAddress, id);
         final Date deletedDate = deletedResources.getIfPresent(resourceData);
         return deletedDate != null;
     }
 
     /**
      * Marks the resource as being deleted.
      * @param resourceType      The type of resource
      * @param remoteAddress     The clients remote address
      * @param id                The id of the resource
      */
     synchronized public void delete(@NotNull final ResourceTypes resourceType, @NotNull final String remoteAddress,
             @NotNull final Integer id) {
         final ResourceData resourceData = new ResourceData(resourceType, remoteAddress, id);
         deletedResources.put(resourceData, new Date());
 
         /* The data and creation date caches are cleared */
         databaseCache.invalidate(resourceData);
         createdResources.invalidate(resourceData);
     }
 
     /**
      * Marks the resource as being not deleted if it was previously marked as deleted.
      * @param resourceType          The type of resource
      * @param remoteAddress         The clients remote address
      * @param id                    The id of the resource
      */
     synchronized public void create(@NotNull final ResourceTypes resourceType, @NotNull final String remoteAddress,
             @NotNull final Integer id) {
 
         final ResourceData resourceData = new ResourceData(resourceType, remoteAddress, id);
         final boolean deletedDateExists = deletedResources.getIfPresent(resourceData) != null;
        final boolean clearCacheRequired = databaseCache.getIfPresent(resourceData) != null;
 
         if (deletedDateExists) {
             deletedResources.invalidate(resourceData);
         }

        if (clearCacheRequired) {
            databaseCache.invalidate(resourceData);
        }
     }
 
     /**
      * Saves the uploaded data in a cache against the data that was actually saved in the database. This cache can then
      * be used to return the uploaded data, so the client doesn't see the formatting going on in the background.
      * @param resourceType      The type of resource
      * @param remoteAddress     The clients remote address
      * @param id                The id of the resource
      * @param databaseData      The value that was eventually put in the database (after formatting, validation etc)
      * @param originalData      The value that was saved to the database
      */
     synchronized public void put(@NotNull final ResourceTypes resourceType, @NotNull final String remoteAddress, @NotNull final Integer id, @NotNull final byte[] databaseData, @NotNull final byte[] originalData) {
         final ResourceData resourceData = new ResourceData(resourceType, remoteAddress, id);
         final DataCache dataCache = new DataCache(originalData, databaseData, Calendar.getInstance());
         databaseCache.put(resourceData, dataCache);
     }
 
     /**
      * When returning data for a file, we try to send back what the client sent us, regardless of any formatting that
      * may have taken place when the data was actually saved in the database. This is to prevent "The file has changed"
      * messages after a save has taken place.
      *
      * @param resourceType      The type of resource
      * @param remoteAddress     The clients remote address
      * @param id                The id of the resource
      * @param databaseData      The current value of the data from the database
      * @return The originally uploaded data if it is in the cache, or the database data if the cache is not valid
      */
     synchronized public byte[] get(@NotNull final ResourceTypes resourceType, @NotNull final String remoteAddress, @NotNull final Integer id, @NotNull final byte[] databaseData) {
 
         final ResourceData resourceData = new ResourceData(resourceType, remoteAddress, id);
 
         final DataCache dataCache = databaseCache.getIfPresent(resourceData);
 
         if (dataCache == null) {
             return databaseData;
         }
 
         /*
             If the database data does not equal what was saved in response to the original data, clear the cache
             and return the database text.
          */
         if (!Arrays.equals(databaseData, dataCache.getDatabase())) {
             databaseCache.invalidate(resourceData);
             return databaseData;
         }
 
         /*
             If the cache has expired, clear the cache and return the database text.
             Now done by Guava cache.
          */
         /*final Calendar window = Calendar.getInstance();
         window.add(Calendar.SECOND, -WebDavConstants.DELETE_WINDOW);
 
         if (dataCache.getTime().before(window)) {
             databaseCache.invalidate(resourceData);
             return databaseData;
         }*/
 
         /*
             If we have arrived at this point the client has requested database text that is
             equal to the text created by a write by this client.
 
             In this situation we say that the text in the database is equivalent to the
             text that was submitted for saving, even if there was some reformatting done on
             what was submitted in order to generate what was saved.
          */
         return dataCache.getOriginal();
     }
 
 }
