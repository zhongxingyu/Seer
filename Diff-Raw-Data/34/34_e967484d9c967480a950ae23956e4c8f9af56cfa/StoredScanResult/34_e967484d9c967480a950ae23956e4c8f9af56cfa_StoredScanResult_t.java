 package info.eigenein.openwifi.persistency.entities;
 
 import android.net.wifi.ScanResult;
 import android.util.Log;
 import com.j256.ormlite.field.DatabaseField;
 import com.j256.ormlite.table.DatabaseTable;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * Represents a scan result that is stored in the application database.
  */
 @DatabaseTable(tableName = "scan_results")
 public class StoredScanResult {
     private static final String LOG_TAG = StoredScanResult.class.getCanonicalName();
 
     public static final String BSSID = "bssid";
 
     public static final String SSID = "ssid";
 
     public static final String LOCATION_TIMESTAMP = "location_timestamp";
 
     @DatabaseField(columnName = "id", generatedId = true)
     private int id;
 
     @DatabaseField(
             columnName = BSSID,
             canBeNull = false,
             width = 17,
             uniqueIndex = true,
             uniqueIndexName = "scan_results_bssid_location_timestamp_idx")
     private String bssid;
 
     @DatabaseField(columnName = SSID, canBeNull = false, index = true)
     private String ssid;
 
     @DatabaseField(columnName = "capabilities", canBeNull = true)
     private String capabilities;
 
     @DatabaseField(columnName = "level", canBeNull = true)
     private Integer level;
 
     @DatabaseField(
             columnName = LOCATION_TIMESTAMP,
             uniqueIndex = true,
             uniqueIndexName = "scan_results_bssid_location_timestamp_idx",
             foreign = true,
             foreignColumnName = "timestamp")
     private StoredLocation location;
 
     @DatabaseField(columnName = "synced", canBeNull = false, index = true)
     private boolean synced;
 
     public StoredScanResult() {
         // ORMLite needs a no-arg constructor.
     }
 
     public StoredScanResult(ScanResult scanResult, StoredLocation location) {
         this.bssid = scanResult.BSSID;
         this.ssid = scanResult.SSID;
         this.capabilities = scanResult.capabilities;
         this.level = scanResult.level;
         this.location = location;
     }
 
     public int getId() {
         return id;
     }
 
     public String getBssid() {
         return bssid;
     }
 
     public String getSsid() {
         return ssid;
     }
 
     public StoredLocation getLocation() {
         return location;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     public void setLocation(StoredLocation location) {
         this.location = location;
     }
 
     public void setBssid(String bssid) {
         this.bssid = bssid;
     }
 
     public void setSsid(String ssid) {
         this.ssid = ssid;
     }
 
     public void setSynced(boolean synced) {
         this.synced = synced;
     }
 
     public String toString() {
         return String.format("StoredScanResult[bssid=%s, ssid=%s, location=%s]",
                 bssid,
                 ssid,
                 location);
     }
 
     public JSONObject toJsonObject() {
         final JSONObject object = new JSONObject();
         final JSONObject locationObject = new JSONObject();
         try {
             object.put("bssid", bssid);
             object.put("ssid", ssid);
            object.put("ts", location.getTimestamp());
             object.put("acc", location.getAccuracy());
             locationObject.put("lat", location.getLatitude());
             locationObject.put("lon", location.getLongitude());
             object.put("loc", locationObject);
         } catch (JSONException e) {
             Log.e(LOG_TAG, "Could not serialize the scan result: " + this.toString(), e);
         }
         return object;
     }
 }
