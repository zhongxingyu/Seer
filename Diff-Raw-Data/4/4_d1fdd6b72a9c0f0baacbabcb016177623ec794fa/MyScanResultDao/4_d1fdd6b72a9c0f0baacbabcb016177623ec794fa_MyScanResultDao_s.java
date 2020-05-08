 package info.eigenein.openwifi.persistence;
 
 import android.content.*;
 import android.database.*;
 import android.database.sqlite.*;
 import android.location.*;
 import android.net.wifi.*;
 import android.text.*;
 import android.util.*;
 import info.eigenein.openwifi.helpers.location.*;
 
 import java.util.*;
 
 public class MyScanResultDao extends BaseDao {
     private static final String LOG_TAG = MyScanResultDao.class.getCanonicalName();
 
     private static final int PAGE_SIZE = 4096;
 
     public MyScanResultDao(final SQLiteDatabase database) {
         super(database);
     }
 
     @Override
     public void onCreate(final SQLiteDatabase database) {
         database.execSQL(
                 "CREATE TABLE `my_scan_results` (" +
                         "`id` INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "`accuracy` FLOAT NOT NULL, " +
                         "`latitude` INTEGER NOT NULL, " +
                         "`longitude` INTEGER NOT NULL, " +
                         "`timestamp` BIGINT NOT NULL, " +
                         "`synced` SMALLINT NOT NULL, " +
                         "`own` SMALLINT NOT NULL, " +
                         "`bssid` VARCHAR NOT NULL, " +
                         "`ssid` VARCHAR NOT NULL);"
         );
         database.execSQL(
                 "CREATE INDEX `idx_my_scan_results_latitude` " +
                         "ON `my_scan_results` (`latitude`);");
         database.execSQL(
                 "CREATE INDEX `idx_my_scan_results_synced` " +
                         "ON `my_scan_results` (`synced`);");
         database.execSQL(
                 "CREATE INDEX `idx_my_scan_results_bssid` " +
                         "ON `my_scan_results` (`bssid`);");
     }
 
     /**
      * Gets the results within the specified area.
      */
     public Collection<MyScanResult> queryByLocation(
                 final CancellationToken cancellationToken,
                 final double minLatitude,
                 final double minLongitude,
                 final double maxLatitude,
                 final double maxLongitude) {
         Log.d(LOG_TAG + ".queryByLocation", String.format(
                 "[minLat=%s, minLon=%s, maxLat=%s, maxLon=%s]",
                 minLatitude,
                 minLongitude,
                 maxLatitude,
                 maxLongitude));
         final long startTimeMillis = System.currentTimeMillis();
         // Initialize the collection.
         final Collection<MyScanResult> results = new ArrayList<MyScanResult>();
         // Paging.
         int offset = 0;
         while (true) {
             // Test the cancellation token.
             if (cancellationToken.isCancelled()) {
                 Log.d(LOG_TAG + ".queryByLocation", "Cancelled.");
                 return null;
             }
             // Run query.
             Log.d(LOG_TAG + ".queryByLocation", String.format("Reading page at %s ...", offset));
             final Cursor cursor = database.rawQuery(
                     "SELECT id, accuracy, latitude, longitude, timestamp, synced, own, bssid, ssid " +
                             "FROM my_scan_results " +
                             "WHERE (latitude BETWEEN ? AND ?) AND (longitude BETWEEN ? AND ?) " +
                             "LIMIT ? OFFSET ?;",
                     new String[] {
                             Integer.toString(L.toE6(minLatitude)),
                             Integer.toString(L.toE6(maxLatitude)),
                             Integer.toString(L.toE6(minLongitude)),
                             Integer.toString(L.toE6(maxLongitude)),
                             Integer.toString(PAGE_SIZE),
                             Integer.toString(offset)
                     }
             );
             if (cursor.getCount() == 0) {
                 break;
             }
             // Read results.
             read(cursor, results);
             // Move.
             offset += PAGE_SIZE;
         }
         Log.d(LOG_TAG + ".queryByLocation", String.format(
                 "Got %d results in %sms.",
                 results.size(),
                 System.currentTimeMillis() - startTimeMillis
         ));
         // Return the results.
         return results;
     }
 
     /**
      * Gets the unsynced results.
      */
     public List<MyScanResult> queryUnsynced(final int limit) {
         // Initialize the collection.
         final List<MyScanResult> results = new ArrayList<MyScanResult>();
         // Run the query.
         final Cursor cursor = database.rawQuery(
                 "SELECT id, accuracy, latitude, longitude, timestamp, synced, own, bssid, ssid " +
                         "FROM my_scan_results " +
                         "WHERE NOT synced " +
                         "LIMIT ?;",
                 new String[] { Integer.toString(L.toE6(limit)) }
         );
         // Read results.
         read(cursor, results);
         // Return the results.
         return results;
     }
 
     /**
      * Gets the scan results by the specified BSSID from the newest to the oldest.
      */
     public List<MyScanResult> queryNewestByBssid(final String bssid) {
         final List<MyScanResult> results = new ArrayList<MyScanResult>();
         final Cursor cursor = database.rawQuery(
                 "SELECT id, accuracy, latitude, longitude, timestamp, synced, own, bssid, ssid " +
                         "FROM my_scan_results " +
                         "WHERE bssid = ? " +
                         "ORDER BY timestamp DESC;",
                 new String[] { bssid }
         );
         read(cursor, results);
         return results;
     }
 
     /**
      * Inserts the results.
      */
     public void insert(final Location location, final Collection<ScanResult> results) {
         if (results.isEmpty()) {
             return;
         }
         for (final ScanResult scanResult : results) {
             final ContentValues values = new ContentValues();
             values.put("accuracy", location.getAccuracy());
            values.put("latitude", location.getLatitude());
            values.put("longitude", location.getLongitude());
             values.put("timestamp", location.getTime());
             values.put("synced", false);
             values.put("own", true);
             values.put("bssid", scanResult.BSSID);
             values.put("ssid", scanResult.SSID);
             database.insert("my_scan_results", null, values);
         }
         Log.d(LOG_TAG + ".insert(location, results)", String.format("Inserted %s results.", results.size()));
     }
 
     /**
      * Inserts the results.
      */
     public void insert(final Collection<MyScanResult> results) {
         // Check the parameters.
         if (results.isEmpty()) {
             return;
         }
         final long startTimeMillis = System.currentTimeMillis();
         // Initialize the helper.
         final DatabaseUtils.InsertHelper insertHelper =
                 new DatabaseUtils.InsertHelper(database, "my_scan_results");
         final int accuracyIndex = insertHelper.getColumnIndex("accuracy");
         final int latitudeIndex = insertHelper.getColumnIndex("latitude");
         final int longitudeIndex = insertHelper.getColumnIndex("longitude");
         final int timestampIndex = insertHelper.getColumnIndex("timestamp");
         final int syncedIndex = insertHelper.getColumnIndex("synced");
         final int ownIndex = insertHelper.getColumnIndex("own");
         final int bssidIndex = insertHelper.getColumnIndex("bssid");
         final int ssidIndex = insertHelper.getColumnIndex("ssid");
         // Perform inserting.
         try {
             for (final MyScanResult result : results) {
                 insertHelper.prepareForInsert();
                 // Put the values.
                 insertHelper.bind(accuracyIndex, result.getAccuracy());
                 insertHelper.bind(latitudeIndex, result.getLatitudeE6());
                 insertHelper.bind(longitudeIndex, result.getLongitudeE6());
                 insertHelper.bind(timestampIndex, result.getTimestamp());
                 insertHelper.bind(syncedIndex, result.isSynced());
                 insertHelper.bind(ownIndex, result.isOwn());
                 insertHelper.bind(bssidIndex, result.getBssid());
                 insertHelper.bind(ssidIndex, result.getSsid());
                 // Insert the result.
                 insertHelper.execute();
             }
         } finally {
             insertHelper.close();
         }
         // Done.
         Log.d(LOG_TAG + ".insert(results)", String.format(
                 "Inserted %d results in %sms.",
                 results.size(),
                 System.currentTimeMillis() - startTimeMillis
         ));
     }
 
     /**
      * Sets the synced flag on the specified results.
      */
     public void setSynced(final Collection<MyScanResult> results) {
         final StringBuilder idsStringBuilder = new StringBuilder();
         for (final MyScanResult result : results) {
             if (idsStringBuilder.length() != 0) {
                 idsStringBuilder.append(", ");
             }
             idsStringBuilder.append(result.getId());
         }
         if (idsStringBuilder.length() != 0) {
             final String ids = idsStringBuilder.toString();
             Log.d(LOG_TAG + ".setSynced", ids);
             database.execSQL(String.format("UPDATE my_scan_results SET synced = 1 WHERE id IN (%s);", ids));
         }
     }
 
     /**
      * Gets the total scan result count.
      */
     public long getCount() {
         return DatabaseUtils.queryNumEntries(database, "my_scan_results");
     }
 
     public long getUniqueBssidCount() {
         return getUniqueCount("bssid");
     }
 
     public long getUniqueSsidCount() {
         return getUniqueCount("ssid");
     }
 
     /**
      * Deletes the scan results with specified IDs.
      */
     public void delete(final Collection<Long> ids) {
         database.execSQL(String.format(
                 "DELETE FROM my_scan_results WHERE id in (%s);",
                 TextUtils.join(", ", ids)));
     }
 
     private long getUniqueCount(final String columnName) {
         final Cursor cursor = database.rawQuery(
                 String.format("SELECT COUNT(DISTINCT %s) FROM my_scan_results;", columnName),
                 new String[0]);
         try {
             cursor.moveToFirst();
             return cursor.getLong(0);
         } finally {
             cursor.close();
         }
     }
 
     /**
      * Reads the results from the cursor.
      */
     private static void read(final Cursor cursor, final Collection<MyScanResult> results) {
         Log.d(LOG_TAG + ".read", String.format("Read %s rows.", cursor.getCount()));
         try {
             while (cursor.moveToNext()) {
                 results.add(read(cursor));
             }
         } finally {
             cursor.close();
         }
     }
 
     /**
      * Reads the result from the cursor.
      */
     private static MyScanResult read(final Cursor cursor) {
         final MyScanResult result = new MyScanResult();
         result.setId(cursor.getLong(0));
         result.setAccuracy(cursor.getFloat(1));
         result.setLatitudeE6(cursor.getInt(2));
         result.setLongitudeE6(cursor.getInt(3));
         result.setTimestamp(cursor.getLong(4));
         result.setSynced(cursor.getInt(5) != 0);
         result.setOwn(cursor.getInt(6) != 0);
         result.setBssid(cursor.getString(7));
         result.setSsid(cursor.getString(8));
         return result;
     }
 }
