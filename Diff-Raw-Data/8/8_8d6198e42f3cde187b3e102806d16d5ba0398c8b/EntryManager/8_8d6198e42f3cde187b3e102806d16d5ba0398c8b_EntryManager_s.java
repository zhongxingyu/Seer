 package mobisocial.rectacular.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import mobisocial.rectacular.model.MEntry.EntryType;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteStatement;
 
 public class EntryManager extends ManagerBase {
     private static final String[] STANDARD_FIELDS = {
         MEntry.COL_ID,
         MEntry.COL_TYPE,
         MEntry.COL_NAME,
         MEntry.COL_OWNED,
         MEntry.COL_COUNT,
         MEntry.COL_FOLLOWING_COUNT,
         MEntry.COL_THUMBNAIL
     };
     
     private static final int _id = 0;
     private static final int type = 1;
     private static final int name = 2;
     private static final int owned = 3;
     private static final int count = 4;
     private static final int followingCount = 5;
     private static final int thumbnail = 6;
 
     private SQLiteStatement sqlInsertEntry;
     private SQLiteStatement sqlUpdateEntryCount;
     private SQLiteStatement sqlUpdateEntryThumbnail;
     private SQLiteStatement sqlUpdateEntryOwned;
 
     public EntryManager(SQLiteDatabase db) {
         super(db);
     }
     
     public EntryManager(SQLiteOpenHelper databaseSource) {
         super(databaseSource);
     }
     
     /**
      * Insert a new entry into the database
      * @param entry A valid MEntry object
      */
     public void insertEntry(MEntry entry) {
         SQLiteDatabase db = initializeDatabase();
         if (sqlInsertEntry == null) {
             synchronized(this) {
                 if (sqlInsertEntry == null) {
                     StringBuilder sql = new StringBuilder()
                         .append("INSERT INTO ").append(MEntry.TABLE)
                         .append("(")
                         .append(MEntry.COL_TYPE).append(",")
                         .append(MEntry.COL_NAME).append(",")
                         .append(MEntry.COL_OWNED).append(",")
                         .append(MEntry.COL_COUNT).append(",")
                         .append(MEntry.COL_FOLLOWING_COUNT).append(",")
                         .append(MEntry.COL_THUMBNAIL)
                         .append(") VALUES (?,?,?,?,?,?)");
                     sqlInsertEntry = db.compileStatement(sql.toString());
                 }
             }
         }
         synchronized(sqlInsertEntry) {
             bindField(sqlInsertEntry, type, entry.type.ordinal());
             bindField(sqlInsertEntry, name, entry.name);
             bindField(sqlInsertEntry, owned, entry.owned);
             bindField(sqlInsertEntry, count, 0); // inserted entries always have 0 owners
             bindField(sqlInsertEntry, followingCount, 0); // inserted entries always have 0 owners
             bindField(sqlInsertEntry, thumbnail, entry.thumbnail);
             entry.id = sqlInsertEntry.executeInsert();
         }
     }
     
     /**
      * Increment the number of times the entry has been seen
     * @param entry MEntry object with valid id
      * @param addOne Whether or not to add to the total count
      * @param addFollowing Whether or not to add to the following count
      */
     public void updateCount(long entryId, boolean addOne, boolean addFollowing) {
         if (!addOne && !addFollowing) return;
         SQLiteDatabase db = initializeDatabase();
         if (sqlUpdateEntryCount == null) {
             synchronized(this) {
                 if (sqlUpdateEntryCount == null) {
                     StringBuilder sql = new StringBuilder()
                         .append("UPDATE ").append(MEntry.TABLE)
                         .append(" SET ")
                         .append(MEntry.COL_COUNT).append("=")
                         .append(MEntry.COL_COUNT).append("+?,")
                         .append(MEntry.COL_FOLLOWING_COUNT).append("=")
                         .append(MEntry.COL_FOLLOWING_COUNT).append("+?")
                         .append(" WHERE ").append(MEntry.COL_ID).append("=?");
                     sqlUpdateEntryCount = db.compileStatement(sql.toString());
                 }
             }
         }
         synchronized(sqlUpdateEntryCount) {
             bindField(sqlUpdateEntryCount, 1, addOne ? 1 : 0);
             bindField(sqlUpdateEntryCount, 2, addFollowing ? 1 : 0);
             bindField(sqlUpdateEntryCount, 3, entryId);
             sqlUpdateEntryCount.executeUpdateDelete();
         }
     }
     
     /**
      * Add a thumbnail to an entry
      * @param entry MEntry object containing a valid thumbnail
      */
     public void updateThumbnail(MEntry entry) {
         SQLiteDatabase db = initializeDatabase();
         if (sqlUpdateEntryThumbnail == null) {
             synchronized(this) {
                 if (sqlUpdateEntryThumbnail == null) {
                     StringBuilder sql = new StringBuilder()
                         .append("UPDATE ").append(MEntry.TABLE)
                         .append(" SET ")
                         .append(MEntry.COL_THUMBNAIL).append("=?")
                         .append(" WHERE ").append(MEntry.COL_ID).append("=?");
                     sqlUpdateEntryThumbnail = db.compileStatement(sql.toString());
                 }
             }
         }
         synchronized(sqlUpdateEntryThumbnail) {
             bindField(sqlUpdateEntryThumbnail, 1, entry.thumbnail);
             bindField(sqlUpdateEntryThumbnail, 2, entry.id);
             sqlUpdateEntryThumbnail.executeUpdateDelete();
         }
     }
     
     /**
      * Set ownership flag for an entry
      * @param entry MEntry object
      */
     public void updateOwned(MEntry entry) {
         SQLiteDatabase db = initializeDatabase();
         if (sqlUpdateEntryOwned == null) {
             synchronized(this) {
                 if (sqlUpdateEntryOwned == null) {
                     StringBuilder sql = new StringBuilder()
                         .append("UPDATE ").append(MEntry.TABLE)
                         .append(" SET ")
                         .append(MEntry.COL_OWNED).append("=?")
                         .append(" WHERE ").append(MEntry.COL_ID).append("=?");
                     sqlUpdateEntryOwned = db.compileStatement(sql.toString());
                 }
             }
         }
         synchronized(sqlUpdateEntryOwned) {
             bindField(sqlUpdateEntryOwned, 1, entry.owned);
             bindField(sqlUpdateEntryOwned, 2, entry.id);
             sqlUpdateEntryOwned.executeUpdateDelete();
         }
     }
     
     /**
      * Insert an entry, or update it if it already exists
      * @param type EntryType of the entry
      * @param name String name of the entry
      * @param incrementCt true to add to the count, false otherwise
     * @param incrementFct true to add to the following count, false otherwise
      * @param setOwned Whether or not to set as owned
     * @return
      */
     public MEntry ensureEntry(
             EntryType type, String name,
             boolean incrementCt, boolean incrementFCt, boolean setOwned) {
         SQLiteDatabase db = initializeDatabase();
         db.beginTransaction();
         try {
             MEntry entry = getEntry(type, name);
             if (entry != null) {
                 updateCount(entry.id, incrementCt, incrementFCt);
                 if (setOwned && !entry.owned) {
                     entry.owned = true;
                     updateOwned(entry);
                 }
             } else {
                 entry = new MEntry();
                 entry.type = type;
                 entry.name = name;
                 entry.owned = setOwned;
                 entry.count = 0L;
                 insertEntry(entry);
             }
             db.setTransactionSuccessful();
             return entry;
         } finally {
             db.endTransaction();
         }
     }
     
     /**
      * Get a single entry of a given type and name
      * @param type Type of the entry
      * @param name Unique name of the entry
      * @return MEntry object
      */
     public MEntry getEntry(EntryType type, String name) {
         SQLiteDatabase db = initializeDatabase();
         String table = MEntry.TABLE;
         String selection = MEntry.COL_TYPE + "=? AND " + MEntry.COL_NAME + "=?";
         String[] selectionArgs = new String[]{ Integer.toString(type.ordinal()), name };
         String groupBy = null, having = null, orderBy = null;
         Cursor c = db.query(table, STANDARD_FIELDS, selection, selectionArgs, groupBy, having, orderBy);
         try {
             if (c.moveToFirst()) {
                 return fillInStandardFields(c);
             } else {
                 return null;
             }
         } finally {
             c.close();
         }
     }
     
     /**
      * Get all entries of a given type
      * @param type EntryType of the desired type
      * @return List of MEntry objects
      */
     public List<MEntry> getEntries(EntryType type) {
         SQLiteDatabase db = initializeDatabase();
         String table = MEntry.TABLE;
         String selection = MEntry.COL_TYPE + "=?";
         String[] selectionArgs = new String[]{ Integer.toString(type.ordinal()) };
         String groupBy = null, having = null;
         String orderBy = MEntry.COL_COUNT + " DESC";
         Cursor c = db.query(table, STANDARD_FIELDS, selection, selectionArgs, groupBy, having, orderBy);
         try {
             List<MEntry> entries = new ArrayList<MEntry>();
             while (c.moveToNext()) {
                 entries.add(fillInStandardFields(c));
             }
             return entries;
         } finally {
             c.close();
         }
     }
     
     /**
      * Get the entities of a given type most common amongst followed users.
      * @param type The type of entry desired
      * @param lim The maximum number to fetch (null if no limit)
      * @return List of MEntry objects
      */
     public List<MEntry> getTopEntries(EntryType type, Long lim) {
         SQLiteDatabase db = initializeDatabase();
         String table = MEntry.TABLE;
         String selection = MEntry.COL_TYPE + "=?";
         String[] selectionArgs = new String[]{ Integer.toString(type.ordinal()) };
         String groupBy = null, having = null;
         String orderBy = MEntry.COL_COUNT + " DESC";
         String limit = null;
         if (lim != null) {
             limit = lim.toString();
         }
         Cursor c = db.query(table, STANDARD_FIELDS, selection, selectionArgs, groupBy, having, orderBy, limit);
         try {
             List<MEntry> entries = new ArrayList<MEntry>();
             while (c.moveToNext()) {
                 entries.add(fillInStandardFields(c));
             }
             return entries;
         } finally {
             c.close();
         }
     }
     
     /**
      * Like getTopEntries but for non-owned entries
      * @param type EntryType of the entry
      * @param lim Maximum number to return
      * @return List of MEntry objects
      */
     public List<MEntry> getDiscoveredTopEntries(EntryType type, Long lim) {
         SQLiteDatabase db = initializeDatabase();
         String table = MEntry.TABLE;
         String selection = MEntry.COL_TYPE + "=? AND " + MEntry.COL_OWNED + "=?";
         String[] selectionArgs = new String[]{
                 Integer.toString(type.ordinal()),
                 Long.toString(0L)
         };
         String groupBy = null, having = null;
         String orderBy = MEntry.COL_COUNT + " DESC";
         String limit = null;
         if (lim != null) {
             limit = lim.toString();
         }
         Cursor c = db.query(table, STANDARD_FIELDS, selection, selectionArgs, groupBy, having, orderBy, limit);
         try {
             List<MEntry> entries = new ArrayList<MEntry>();
             while (c.moveToNext()) {
                 entries.add(fillInStandardFields(c));
             }
             return entries;
         } finally {
             c.close();
         }
     }
     
     /**
      * Convert a database cursor into an MEntry object
      * @param c A valid Cursor from a database query
      * @return MEntry object
      */
     private MEntry fillInStandardFields(Cursor c) {
         MEntry entry = new MEntry();
         entry.id = c.getLong(_id);
         entry.type = EntryType.values()[(int) c.getLong(type)];
         entry.name = c.getString(name);
         entry.owned = (c.getLong(owned) == 1) ? true : false;
         entry.count = c.getLong(count);
         entry.followingCount = c.getLong(followingCount);
         if (!c.isNull(thumbnail)) {
             entry.thumbnail = c.getBlob(thumbnail);
         }
         return entry;
     }
 
     @Override
     public void close() {
         if (sqlInsertEntry != null) {
             sqlInsertEntry.close();
         }
         if (sqlUpdateEntryCount != null) {
             sqlUpdateEntryCount.close();
         }
         if (sqlUpdateEntryThumbnail != null) {
             sqlUpdateEntryThumbnail.close();
         }
         if (sqlUpdateEntryOwned != null) {
             sqlUpdateEntryOwned.close();
         }
     }
 
 }
