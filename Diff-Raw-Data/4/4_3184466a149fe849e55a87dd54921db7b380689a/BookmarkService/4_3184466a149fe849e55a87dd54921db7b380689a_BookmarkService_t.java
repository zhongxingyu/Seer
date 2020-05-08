 package fr.itinerennes.business.service;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 
 import fr.itinerennes.business.event.IBookmarkModificationListener;
 import fr.itinerennes.database.Columns.BookmarksColumns;
 import fr.itinerennes.database.DatabaseHelper;
 import fr.itinerennes.model.Bookmark;
import fr.itinerennes.utils.IOUtils;
 
 /**
  * A service to manage user bookmarks.
  * 
  * @author Jérémie Huchet
  */
 public final class BookmarkService extends AbstractService implements BookmarksColumns {
 
     /** The event logger. */
     private static final Logger LOGGER = LoggerFactory.getLogger(BookmarkService.class);
 
     /** A list of listeners on bookmarks modifications. */
     private ArrayList<IBookmarkModificationListener> listeners = null;
 
     /**
      * Creates the bookmark service.
      * 
      * @param dbHelper
      *            a database helper
      */
     public BookmarkService(final DatabaseHelper dbHelper) {
 
         super(dbHelper);
     }
 
     /**
      * Adds a listener for modifications make by this service.
      * 
      * @param listener
      *            the listener to bind to this service.
      */
     public void addListener(final IBookmarkModificationListener listener) {
 
         if (null == listener) {
             throw new NullPointerException("Can't bind a null listener");
         }
         if (listeners == null) {
             listeners = new ArrayList<IBookmarkModificationListener>(1);
         }
         listeners.add(listener);
     }
 
     /**
      * Sets a resource starred.
      * 
      * @param type
      *            the type of the resource
      * @param id
      *            the identifier of the resource
      * @param label
      *            the label of the bookmark
      */
     public void setStarred(final String type, final String id, final String label) {
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("setStarred.start - type={}, id={}", type, id);
         }
 
         final ContentValues values = new ContentValues();
         values.put(LABEL, label);
         values.put(TYPE, type);
         values.put(ID, id);
 
         final SQLiteDatabase database = dbHelper.getWritableDatabase();
         // TJHU est ce nécessaire de vérifier qu'on ne va pas faire péter la contrainte
         // unique(type,id) ?
         database.insert(BOOKMARKS_TABLE_NAME, null, values);
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("notifying {} listeners for bookmark addition", null == listeners ? 0
                     : listeners.size());
         }
         if (listeners != null) {
             for (final IBookmarkModificationListener l : listeners) {
                 l.onBookmarkStateChanged(type, id, true);
             }
         }
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("setStarred.end");
         }
     }
 
     /**
      * Unstar a resource.
      * 
      * @param type
      *            the type of the resource
      * @param id
      *            the identifier of the resource
      */
     public void setNotStarred(final String type, final String id) {
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("setNotStarred.start - type={}, id={}", type, id);
         }
 
         final String where = String.format("%s = ? AND %s = ?", TYPE, ID);
         final String[] whereArgs = new String[] { type, id };
 
         final SQLiteDatabase database = dbHelper.getWritableDatabase();
         database.delete(BOOKMARKS_TABLE_NAME, where, whereArgs);
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("notifying {} listeners for bookmark removal", null == listeners ? 0
                     : listeners.size());
         }
         if (listeners != null) {
             for (final IBookmarkModificationListener l : listeners) {
                 l.onBookmarkStateChanged(type, id, false);
             }
         }
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("setNotStarred.end");
         }
     }
 
     /**
      * Returns whether or not a resource is starred.
      * 
      * @param type
      *            the type of the resource
      * @param id
      *            the identifier of the resource
      * @return true if the resource is starred or false
      */
     public boolean isStarred(final String type, final String id) {
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("isStarred.start - type={}, id={}", type, id);
         }
         final SQLiteDatabase database = dbHelper.getWritableDatabase();
         final String selection = String.format("%s = ? AND %s = ?", TYPE, ID);
         final String[] selectionArgs = new String[] { type, id };
         final Cursor c = database.query(BOOKMARKS_TABLE_NAME, new String[] { _ID }, selection,
                 selectionArgs, null, null, null);
 
         final boolean hasResult = c.moveToNext();
         c.close();
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("isStarred.end - isStarred={}", hasResult);
         }
         return hasResult;
     }
 
     /**
      * Gets a bookmark.
      * 
      * @param type
      *            the type of the resource
      * @param id
      *            the identifier of the resource
      * @return the bookmark if it exists or null
      */
     public Bookmark getBookmark(final String type, final String id) {
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("getBookmark.start - type={}, id={}", type, id);
         }
 
         final String selection = String.format("%s = ? AND %s = ?", TYPE, ID);
         final String[] args = new String[] { type, id };
 
         final Cursor c = dbHelper.getWritableDatabase().query(BOOKMARKS_TABLE_NAME,
                 new String[] { LABEL, TYPE, ID }, selection, args, null, null, null);
 
         final Bookmark bm;
         if (c.moveToNext()) {
             bm = new Bookmark();
             bm.setLabel(c.getString(0));
             bm.setType(c.getString(1));
             bm.setId(c.getString(2));
         } else {
             bm = null;
         }
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("getBookmark.end - bookmark={}", bm);
         }
 
        IOUtils.close(c);

         return bm;
     }
 
     /**
      * Gets all bookmarks.
      * 
      * @return all bookmarks
      */
     public List<Bookmark> getAllBookmarks() {
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("getAllBookmarks.start");
         }
 
         final ArrayList<Bookmark> allBookmarks = new ArrayList<Bookmark>();
 
         // retrieve all bookmarks
         final Cursor c = dbHelper.getWritableDatabase().query(BOOKMARKS_TABLE_NAME,
                 new String[] { LABEL, TYPE, ID }, null, null, null, null, null);
 
         while (c.moveToNext()) {
             final Bookmark bm = new Bookmark();
             bm.setLabel(c.getString(0));
             bm.setType(c.getString(1));
             bm.setId(c.getString(2));
             allBookmarks.add(bm);
         }
 
         c.close();
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("getAllBookmarks.end - {} bookmarks", allBookmarks.size());
         }
         return allBookmarks;
     }
 }
