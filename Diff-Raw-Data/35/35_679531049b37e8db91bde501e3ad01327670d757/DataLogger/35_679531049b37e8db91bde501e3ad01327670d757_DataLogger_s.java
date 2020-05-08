 package org.jdleesmiller.trackjd;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.PrintStream;
 
 import org.jdleesmiller.trackjd.data.AbstractPoint;
 
 import com.loopj.android.http.RequestParams;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 
 /**
  * Generic logging of CSV fragments from multiple sensors. To keep things
  * simple, we just put the data from all the sensors into one big table.
  * 
  * The main advantage of doing it this way is that it's simple: it decouples the
  * logging and uploading logic from the particular sensor types and exactly what
  * we record from them. Once we know more about exactly what we want to collect,
  * we should probably have one table per sensor. The main issues with the
  * current approach are:
  * 
  * 1. We do more work than strictly necessary upon recording each data point,
  * because we convert to CSV. We could instead store some sort of binary blob,
  * or we could break each sensor into its own table; these options would also be
  * more storage-efficient.
  * 
  * 2. We can't easily query the points from the app, so if we're making more
  * complicated decisions on the app based on more than very recent historical
  * data, we'll have to look at some other solution. This could be a problem
  * for on-device mode annotation.
  * 
  * 3. At present, we write every data point as soon as it's recorded, which is
  * not optimal from a power use point of view. We could instead implement a
  * buffering strategy here so that we collect say 100 points and then write them
  * all to disk in a single SQLite transaction.
  */
 public class DataLogger {
 
   public static final String TABLE = "data";
 
   /**
    * Called when initializing the database.
    * 
    * @param db
    */
   public static void createTable(SQLiteDatabase db) {
     db.execSQL("CREATE TABLE " + TABLE + " (" + //
         "id INTEGER PRIMARY KEY AUTOINCREMENT," + //
         "time INTEGER," + //
         "tag TEXT," + //
         "csv TEXT)");
   }
 
   /**
    * Called when recreating the database.
    * 
    * @param db
    */
   public static void dropTable(SQLiteDatabase db) {
     db.execSQL("DROP TABLE IF EXISTS " + TABLE);
   }
 
   private final TrackJDService service;
 
   public DataLogger(TrackJDService service) {
     this.service = service;
   }
 
   /**
    * Store a data point in the database.
    * 
    * @param point
    */
   public void log(AbstractPoint point) {
     SQLiteDatabase db = this.service.getWritableDb();
     if (db != null) {
       ContentValues values = new ContentValues();
       values.put("time", point.getUTCTime());
       values.put("tag", point.getTag());
 
       StringBuilder sb = new StringBuilder();
       point.printCsvData(sb);
       values.put("csv", sb.toString());
 
       db.insert("data", null, values);
     }
   }
 
   /**
    * 
    * @param params
    * 
    * @param maxRecords
    * 
    * @return the id of the last record uploaded, or 0 if no records were
    *         uploaded
    */
   public long addToUpload(RequestParams params, int maxRecords) {
     SQLiteDatabase db = this.service.getReadableDb();
     if (db != null) {
       ByteArrayOutputStream os = new ByteArrayOutputStream();
       PrintStream ps = new PrintStream(os);
       long lastId = -1;
 
       Cursor c = db.rawQuery(//
           "SELECT tag, id, time, csv FROM (" + //
               "  SELECT * FROM " + TABLE + " ORDER BY id ASC LIMIT ?)" + //
               "ORDER BY tag", new String[] { Integer.toString(maxRecords) });
       String currentTag = null;
       while (c.moveToNext()) {
         String tag = c.getString(0);
        if (tag != currentTag) {
           // starting a new file; store the previous file, if any
           if (currentTag != null) {
             ps.flush();
             params.put(currentTag, new ByteArrayInputStream(os.toByteArray()));
             os.reset();
           }
           currentTag = tag;
         }
 
         lastId = c.getLong(1);
 
         ps.print(lastId);
         ps.print(',');
        ps.print(c.getLong(2)); // utc time
         ps.print(',');
        ps.print(c.getString(3)); // utc time
         ps.print('\n');
       }
       return lastId;
     } else {
       return 0;
     }
   }
 
   /**
    * Delete all records with id less than or equal to the given id. Called after
    * addToUpload to clear data that the device has successfully uploaded.
    * 
    * @param lastId
    */
   public void clearLastUpload(long lastId) {
     SQLiteDatabase db = this.service.getWritableDb();
     if (db != null) {
       db.delete(TABLE, "id <= ?", new String[] { Long.toString(lastId) });
     }
   }
 
   /**
    * The number of points currently stored on the device.
    * 
    * @return non-negative
    */
   public int countPoints() {
     SQLiteDatabase db = this.service.getReadableDb();
     if (db != null) {
       Cursor c = db.rawQuery("SELECT COUNT(id) FROM " + TABLE, null);
       if (!c.moveToNext() || c.isNull(0)) {
         return 0;
       } else {
         return c.getInt(0);
       }
     } else {
       return 0;
     }
   }
 }
