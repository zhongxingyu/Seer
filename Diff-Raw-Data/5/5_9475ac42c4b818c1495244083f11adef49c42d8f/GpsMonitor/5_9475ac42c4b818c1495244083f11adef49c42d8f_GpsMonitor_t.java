 package com.parent.management.monitor;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.util.Log;
 
 import com.baidu.location.BDLocation;
 import com.baidu.location.BDLocationListener;
 import com.baidu.location.LocationClient;
 import com.baidu.location.LocationClientOption;
 import com.parent.management.ManagementApplication;
 import com.parent.management.db.ManagementProvider;
 
 public class GpsMonitor extends Monitor {
     private static final String TAG = ManagementApplication.getApplicationTag() + "." +
             GpsMonitor.class.getSimpleName();
     Context mContext = null;
     private LocationClient mLocClient;
     public MyLocationListener myListener = new MyLocationListener();
     
     private BDLocation mCurrentLocation = null;
      
     public GpsMonitor(Context context) {
         super(context);
         mContext = ManagementApplication.getContext();
         mLocClient = new LocationClient(mContext);
         mLocClient.registerLocationListener(myListener);
     }
     
     @Override
     public void startMonitoring() {
         setLocationOption();
         if (!mLocClient.isStarted()) {
             Log.d(TAG, "start locClient");
             mLocClient.start();
         }
         else {
             mLocClient.requestLocation();
         }
         this.monitorStatus = true;
     }
     
     @Override
     public void stopMonitoring() {
         mLocClient.stop();
         this.monitorStatus = false;
     }
 
     private void setLocationOption(){
         LocationClientOption option = new LocationClientOption();
         option.setOpenGps(true); 
         option.setCoorType("bd09ll");
         option.setServiceName("com.baidu.location.service_v2.9");
         option.setPoiExtraInfo(false);   
         option.setAddrType("");
         option.setPriority(LocationClientOption.NetWorkFirst);
         option.setPoiNumber(10);
         option.disableCache(true);      
         mLocClient.setLocOption(option);
     }
 
     public class MyLocationListener implements BDLocationListener {
 
         @Override
         public void onReceiveLocation(BDLocation location) {
             if (location == null)
                 return ;
             StringBuffer sb = new StringBuffer(256);
             sb.append("time : ");
             sb.append(location.getTime());
             sb.append("\nerror code : ");
             sb.append(location.getLocType());
             sb.append("\nlatitude : ");
             sb.append(location.getLatitude());
             sb.append("\nlontitude : ");
             sb.append(location.getLongitude());
             sb.append("\nradius : ");
             sb.append(location.getRadius());
             if (location.getLocType() == BDLocation.TypeGpsLocation){
                 sb.append("\nspeed : ");
                 sb.append(location.getSpeed());
                 sb.append("\nsatellite : ");
                 sb.append(location.getSatelliteNumber());
             } else if (location.getLocType() == BDLocation.TypeNetWorkLocation){
                 sb.append("\naddr : ");
                 sb.append(location.getAddrStr());
             } 
      
             if (mCurrentLocation == null ||
                 (mCurrentLocation != null &&
                  mCurrentLocation.getLatitude() != location.getLatitude() && 
                  mCurrentLocation.getLongitude() != location.getLongitude())) {
                 updateLocation(location);
                 mCurrentLocation = location;
             }
         }
 
         @Override
         public void onReceivePoi(BDLocation poiLocation) {
         }
         
     }
 
     public void updateLocation(BDLocation location) {
         if (location != null) {
             double altitude = location.getAltitude();
             double latidude = location.getLatitude();
             double lontitude = location.getLongitude();
             float speed = location.getSpeed();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Log.v(TAG, location.getTime());
             Date date = null;
             try {
             	date = format.parse(location.getTime());
             } catch (ParseException e) {
             	e.printStackTrace();
             }
             long time = date.getTime();
            Log.v(TAG, format.format(date));
 
             final ContentValues values = new ContentValues();
             values.put(ManagementProvider.Gps.ALTITUDE, altitude);
             values.put(ManagementProvider.Gps.LATIDUDE, latidude);
             values.put(ManagementProvider.Gps.LONGITUDE, lontitude);
             values.put(ManagementProvider.Gps.SPEED, speed);
             values.put(ManagementProvider.Gps.TIME, time);
             
             mContext.getContentResolver().insert(
                     ManagementProvider.Gps.CONTENT_URI, values);
             Log.d(TAG, "insert gps: altitude=" + altitude + ";latidude=" + latidude + ";lontitude=" + lontitude
                     + ";speed=" + speed + ";time=" + time);
         } else {
             Log.e(TAG, "not get Location");
         }
     }
 
     @Override
     public JSONArray extractDataForSend() {
         try {
             JSONArray data = new JSONArray();
 
             String[] GpsProj = new String[] {
                     ManagementProvider.Gps.ALTITUDE,
                     ManagementProvider.Gps.LATIDUDE,
                     ManagementProvider.Gps.LONGITUDE,
                     ManagementProvider.Gps.SPEED,
                     ManagementProvider.Gps.TIME};
             String GpsSel = ManagementProvider.Gps.IS_SENT + " = \""
                     + ManagementProvider.IS_SENT_NO + "\"";
             Cursor gpsCur = mContext.getContentResolver().query(
                     ManagementProvider.Gps.CONTENT_URI,
                     GpsProj, GpsSel, null, null);
 
             if (gpsCur == null) {
                 Log.e(TAG, "open gps table failed");
                 return null;
             }
             if (gpsCur.moveToFirst() && gpsCur.getCount() > 0) {
                 while (gpsCur.isAfterLast() == false) {
                     double alt = gpsCur.getDouble(
                             gpsCur.getColumnIndex(ManagementProvider.Gps.ALTITUDE));
                     double lat = gpsCur.getDouble(
                             gpsCur.getColumnIndex(ManagementProvider.Gps.LATIDUDE));
                     double lon = gpsCur.getDouble(
                             gpsCur.getColumnIndex(ManagementProvider.Gps.LONGITUDE));
                     float spd = gpsCur.getFloat(
                             gpsCur.getColumnIndex(ManagementProvider.Gps.SPEED));
                     long date = gpsCur.getLong(
                             gpsCur.getColumnIndex(ManagementProvider.Gps.TIME));
                     JSONObject raw = new JSONObject();
                     raw.put(ManagementProvider.Gps.ALTITUDE, alt);
                     raw.put(ManagementProvider.Gps.LATIDUDE, lat);
                     raw.put(ManagementProvider.Gps.LONGITUDE, lon);
                     raw.put(ManagementProvider.Gps.SPEED, spd);
                     raw.put(ManagementProvider.Gps.TIME, date);
 
                     data.put(raw);
                     gpsCur.moveToNext();
                 }
             }
             if (null != gpsCur) {
                 gpsCur.close();
             }
             
             Log.v(TAG, "data === " + data.toString());
             
             final ContentValues values = new ContentValues();
             values.put(ManagementProvider.Gps.IS_SENT, ManagementProvider.IS_SENT_YES);
             mContext.getContentResolver().update(
                     ManagementProvider.Gps.CONTENT_URI,
                     values,
                     ManagementProvider.Gps.IS_SENT + "=\"" + ManagementProvider.IS_SENT_NO +"\"",
                     null);
             
             return data;
         } catch (JSONException e) {
             Log.v(TAG, "Json exception:" + e.getMessage());
             e.printStackTrace();
         }
 
         return null;
     }
 
     @Override
     public void updateStatusAfterSend(JSONArray failedList) {
     	if (null != failedList && failedList.length() != 0) {
     		for (int i = 0; i < failedList.length(); ++i) {
     			JSONObject obj = failedList.optJSONObject(i);
     			if (null != obj) {
     				long id = obj.optLong(ManagementProvider.Gps._ID);
     		        final ContentValues values = new ContentValues();
     		        values.put(ManagementProvider.Gps.IS_SENT, ManagementProvider.IS_SENT_NO);
     		        mContext.getContentResolver().update(
     		        		ManagementProvider.Gps.CONTENT_URI,
     		                values,
     		                ManagementProvider.Gps._ID + "=\"" + id +"\"",
     		                null);
     			}
     		}
     	}
         String gpsSel = ManagementProvider.Gps.IS_SENT
         		+ " = \"" + ManagementProvider.IS_SENT_YES + "\"";
     	ManagementApplication.getContext().getContentResolver().delete(
     			ManagementProvider.Gps.CONTENT_URI,
     			gpsSel, null);
     }
    
 }
