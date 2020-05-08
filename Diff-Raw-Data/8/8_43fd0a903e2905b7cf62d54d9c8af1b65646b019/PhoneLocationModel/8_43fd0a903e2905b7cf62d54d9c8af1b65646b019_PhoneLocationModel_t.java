 package org.jimhopp.GPSvsNetwork.model;
 
 import org.jimhopp.GPSvsNetwork.provider.LocationContentProvider;
 import org.jimhopp.GPSvsNetwork.provider.LocationsContentProvider;
 
 import android.content.ContentProvider;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 
 public class PhoneLocationModel {
 	
 	private Context ctxt;
 	
 	public PhoneLocationModel(LocationManager lm, Context ctxt) {
 		this.ctxt = ctxt;
 		LocationListener locationGPS = new LocationListener() {
 			@Override
 			public void onLocationChanged(Location location) { updateGPS(location); }
 			@Override
 			public void onProviderDisabled(String providerName) { }
 			@Override
 			public void onProviderEnabled(String providerName) { }
 			@Override
 			public void onStatusChanged(String providerName, int providerStatus,
 					Bundle extras) { }
         };
         updateGPS(lm.getLastKnownLocation(LocationManager.GPS_PROVIDER));
 
         lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000l, Float.valueOf("10.0"),
         	locationGPS);
         
 		LocationListener locationNetwork = new LocationListener() {
 			@Override
 			public void onLocationChanged(Location location) { updateNetwork(location); }
 			@Override
 			public void onProviderDisabled(String providerName) { }
 			@Override
 			public void onProviderEnabled(String providerName) { }
 			@Override
 			public void onStatusChanged(String providerName, int providerStatus,
 					Bundle extras) { }
         };
         updateNetwork(lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
 
         lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30000l, Float.valueOf("10.0"),
         	locationNetwork);
 	}
 	
 	public Location getLocation(String path) { 
 		Cursor cursor = ctxt.getContentResolver().query(
 				Uri.withAppendedPath(LocationContentProvider.LOCATIONS_URI, "/" + path),      //uri                                   //selection, we want all rows 
 				null,                                       //projections
 				null,                                       //select stmt
 				null,                                       //selection args
 				null);  									//sort order
 		Log.i(this.getClass().getSimpleName(), "getLocation(): got " + cursor.getCount() 
 				+ " rows for " + path);
 		Location loc = null;
 		if (cursor.moveToLast()) {
 		    loc = new Location("GPS");
 		    loc.setAccuracy(cursor.getFloat(cursor.getColumnIndex(LocationsContentProvider.ACCURACY_COL)));
 		    loc.setLatitude(cursor.getDouble(cursor.getColumnIndex(LocationsContentProvider.LAT_COL)));
 		    loc.setLongitude(cursor.getDouble(cursor.getColumnIndex(LocationsContentProvider.LON_COL)));
 		    loc.setTime(cursor.getLong(cursor.getColumnIndex(LocationsContentProvider.TIME_COL)));
 		}
 		cursor.close();
 		return loc; 
 	}
 	
 	public Location getGPSLocation() { 
 		return getLocation(LocationContentProvider.LAST_LOCATION_GPS); 
 	}
 	
 	public Location getNetworkLocation() { 
 		return getLocation(LocationContentProvider.LAST_LOCATION_NETWORK);
 	}
 	
 	void updateGPS(Location loc) {
 		recordLocation(loc, LocationContentProvider.GPS);
 	}
 	
 	void updateNetwork(Location loc) {
 		recordLocation(loc, LocationContentProvider.NETWORK);
 	}
 	
 	void recordLocation(Location loc, String tag) {
		if (loc == null) {
			Log.i(this.getClass().getSimpleName(), "loc is null for tag " + tag);
			return;
		}
		if (Math.abs(loc.getLatitude()) < 0.1 && Math.abs(loc.getLongitude()) < 0.1 && loc.getAccuracy() > 1e6) {
			Log.i(this.getClass().getSimpleName(), "loc is empty for tag " + tag);
			return;
		}
 		ContentValues map = new ContentValues(); 
 		map.put(LocationsContentProvider.TIME_COL, loc != null ? loc.getTime() : System.currentTimeMillis()); 
 		map.put(LocationsContentProvider.TYPE_COL, tag);
 		map.put(LocationsContentProvider.LAT_COL, loc != null ? loc.getLatitude() : 0);
 		map.put(LocationsContentProvider.LON_COL, loc != null ? loc.getLongitude() : 0);
 		map.put(LocationsContentProvider.ACCURACY_COL, loc != null ? loc.getAccuracy(): 10000000.0);
 		Log.i(this.getClass().getSimpleName(), "calling content provider to insert " + map.toString());
 		Uri url = ctxt.getContentResolver().insert(LocationContentProvider.LOCATIONS_URI, map);
 		Log.i(this.getClass().getSimpleName(), "inserted URL " + url.toString());
 	}
 
 	public String dumpLocations() {
 		try {
 			Cursor cursor = ctxt.getContentResolver().query(
 					Uri.withAppendedPath(LocationContentProvider.LOCATIONS_URI, "/all"),      //uri                                   //selection, we want all rows 
 					null,                                       //projections
 					null,                                       //group by
 					null,                                       //having
 					null);  									//sort order
 			Log.i(this.getClass().getSimpleName(), "dumpLocations(): got " 
 			     + cursor.getCount() + " rows and " + cursor.getColumnCount() + " columns");
 			
 			int nCols = cursor.getColumnCount();
 			StringBuilder strbuf = new StringBuilder("Locations: " + cursor.getCount() 
 					+ " rows\n| ");
 			boolean more = cursor.moveToNext();
 			for (int i=0;i<nCols;i++) {
 				strbuf.append(cursor.getColumnName(i) + " |");
 			}
 			while (more) {
 				strbuf.append("\n| ");
 				for (int i=0;i<nCols;i++) {
 					if (LocationsContentProvider.ALL_COLS[i][1].startsWith("INTEGER")) {
 						strbuf.append(cursor.getLong(i));
 					} else if (LocationsContentProvider.ALL_COLS[i][1] == "REAL") {
 						strbuf.append(cursor.getFloat(i));
 					} else if (LocationsContentProvider.ALL_COLS[i][1] == "TEXT") {
 						strbuf.append(cursor.getString(i));
 					} else {
 						strbuf.append("(null or blob)");
 					}
 					strbuf.append(" | ");
 				}
 				more = cursor.moveToNext();
 			}
 			cursor.close();
 			return strbuf.toString();
 		} catch (SQLException e) {
 			Log.e("Error trying to dump locations", e.toString());
 			return "Error trying to dump locations " + e.toString();
 		}
 	}
 }
