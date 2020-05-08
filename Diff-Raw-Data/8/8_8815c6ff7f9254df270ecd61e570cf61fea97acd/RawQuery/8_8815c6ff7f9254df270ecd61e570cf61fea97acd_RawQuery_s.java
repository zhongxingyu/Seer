 package kajman.ssid.model;
 
 import kajman.ssid.model.entity.Wifi;
 import android.content.Context;
 import android.database.Cursor;
 
 public class RawQuery extends DbModel {
 
	// select count(*) as number, a.ssid from wifi as a join (select _id,
 	// bssid, ssid from wifi group by bssid) as b on a._id = b._id group by
	// a.ssid having number >1 order by number;
 	private static final String TOP_NAMES = String
			.format("select count(*) as n, a.%s from %s as a "
 					+ "join (select %s, %s, %s from %s group by %s) as b "
 					+ "on a.%s = b.%s group by a.%s having n > 1 order by n desc",
 					Wifi.Columns.SSID, Wifi.TABLE_NAME, Wifi.Columns._ID,
 					Wifi.Columns.BSSID, Wifi.Columns.SSID, Wifi.TABLE_NAME,
 					Wifi.Columns.BSSID, Wifi.Columns._ID, Wifi.Columns._ID,
 					Wifi.Columns.SSID);
 
 	private static final String WIFIS = String.format("select %s, %s from %s",
 			Wifi.Columns.BSSID, Wifi.Columns.SSID, Wifi.TABLE_NAME);
 	private static final String WIFIS_BY_BSSID = WIFIS
 			+ String.format(" order by %s", Wifi.Columns.BSSID);
 	private static final String WIFIS_BY_SSID = WIFIS
 			+ String.format(" order by %s", Wifi.Columns.SSID);
 
 	// select count(*) as n, channel/5-481 as c from wifi group by c order by c;
 	private static final String CHANNELS = String
 			.format("select count(*) as n, %s/5-481 as c from %s group by c order by c",
 					Wifi.Columns.CHANNEL, Wifi.TABLE_NAME);
 	// select count(*) as n, number from (select count(*) as number from wifi as
 	// a join (select _id, bssid, ssid from wifi group by bssid) as b on a._id =
 	// b._id group by a.scan_number having number > 0 order by number) group by
 	// number;
 	private static final String RECORDS_BY_SCAN = String.format(
 			"select count(*) as n, number from "
 					+ "(select count(*) as number from %s as a join "
 					+ "(select %s, %s, %s from %s group by %s) as b " +
 					"on a.%s = b.%s group by a.%s having number > 0 order by number) " +
 					"group by number", Wifi.TABLE_NAME,
 					Wifi.Columns._ID, Wifi.Columns.BSSID, Wifi.Columns.SSID, Wifi.TABLE_NAME, Wifi.Columns.BSSID,
 					Wifi.Columns._ID,Wifi.Columns._ID,Wifi.Columns.SCAN_NUMBER);
 
 	public RawQuery(Context context) {
 		super(context);
 	}
 
 	private Cursor fetch(String query) {
 		return getDb().rawQuery(query, null);
 	}
 
 	public Cursor fetchTopNames() {
 		return fetch(TOP_NAMES);
 	}
 
 	public Cursor fetchWifisByName() {
 		return fetch(WIFIS_BY_SSID);
 	}
 
 	public Cursor fetchWifisByBSSID() {
 		return fetch(WIFIS_BY_BSSID);
 	}
 
 	public Cursor fetchByChannels() {
 		return fetch(CHANNELS);
 	}
 	
 	public Cursor fetchRecordsByScanNumner(){
 		return fetch(RECORDS_BY_SCAN);
 	}
 
 	public String toString(Cursor cursor) {
 		if (cursor != null) {
 			StringBuilder stringBuilder = new StringBuilder();
 			String[] names = cursor.getColumnNames();
 			stringBuilder
 					.append("Records fetched: " + cursor.getCount() + "\n");
 			for (String s : names) {
 				stringBuilder.append(s + "\t\t");
 			}
 			stringBuilder.append("\n");
 			while (cursor.moveToNext()) {
 				for (String s : names) {
 					stringBuilder.append(cursor.getString(cursor
 							.getColumnIndex(s)) + "\t\t");
 				}
 				stringBuilder.append("\n");
 			}
 			cursor.close();
 			return stringBuilder.toString();
 		} else {
 			return "No data to display";
 		}
 	}
 }
