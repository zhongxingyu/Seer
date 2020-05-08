 package com.gingbear.githubtest;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Locale;
 
 import android.content.Context;
 import android.location.Address;
 import android.location.Criteria;
 import android.location.Geocoder;
 import android.location.GpsStatus;
 import android.location.GpsSatellite;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.location.Location;
 import android.location.LocationProvider;
 import android.os.Bundle;
 import android.app.Activity;
 
 public class CustomLocation  implements LocationListener, GpsStatus.Listener{
 	private static CustomLocation instance;
     private LocationManager mLocationManager;
 	    CustomLocation(){
 
 	    }; 
 	   public static synchronized CustomLocation getInstance(){
 	     if(instance == null){ 
 	       instance = new CustomLocation();
 //		   mBroadcastReceiver = new BatteryReceiver(instance);
 	     }
 	     return instance;
 	   }
 	public void check(Activity activity){
 		//LocationManagerの取得
 		LocationManager locationManager = (LocationManager)activity.getSystemService(Context.LOCATION_SERVICE);
 		//GPSから現在地の情報を取得
 		Location myLocate = locationManager.getLastKnownLocation("gps");
 	}
 	public void create(Activity activity, LocationListener listener){
 
         // LocationManagerオブジェクトの生成
 		mLocationManager = (LocationManager) activity.getSystemService(Activity.LOCATION_SERVICE);
  
         // ローケーション取得条件の設定
         Criteria criteria = new Criteria();
         criteria.setAccuracy(Criteria.ACCURACY_COARSE);
         criteria.setPowerRequirement(Criteria.POWER_LOW);
         criteria.setSpeedRequired(false);
         criteria.setAltitudeRequired(false);
         criteria.setBearingRequired(false);
         criteria.setCostAllowed(false);
  
         mLocationManager.requestLocationUpdates(1000, 1, criteria, listener, activity.getMainLooper());
 
         mLocationManager.requestLocationUpdates(
             LocationManager.GPS_PROVIDER,
 //            LocationManager.NETWORK_PROVIDER,
             0,
             0,
             listener);
         
 
 //(1) LocationManagerの取得
 //LocationManager locMgr  = (LocationManager)getSystemService(LOCATION_SERVICE);
 
 //(2) 最適なLocationProviderを選択
 //Criteria criteria = new Criteria();
 String provider = mLocationManager.getBestProvider(criteria, true);
 
 //(3) 最後にわかっている位置情報を取得
 Location location = mLocationManager.getLastKnownLocation(provider);
 long minTime = 15000;
 float minDistance = (float) 1.0; 
 mLocationManager.requestLocationUpdates(provider, minTime, minDistance, listener);
 
 //とすると、minTime[msec]以上の間隔で、minDistance[m]以上の変化があれば情報が取得される事になります。
 
 
 Context context = activity.getApplicationContext();
 //(5) 経度、緯度、高度等の情報から住所情報への変換
 //Geocoder geocoder = new Geocoder(context, Locale.JAPAN);
 //List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 5);
 //(6) GPSの状態取得/衛星情報の取得
 GpsStatus gpsStat = mLocationManager.getGpsStatus(null);
 Iterable<GpsSatellite> satellites = gpsStat.getSatellites();
 	}
 	
 
 	//座標を住所のStringへ変換
 	public String point2address(double latitude, double longitude, Context context) throws IOException{
		String tag = "ReverseGeocode";
 		String string = new String();
 
 		//geocoedrの実体化
 		CustomLog.d(tag, "Start point2adress");
 		Geocoder geocoder = new Geocoder(context, Locale.JAPAN);
 		List<Address> list_address = geocoder.getFromLocation(latitude, longitude, 5);	//引数末尾は返す検索結果数
 
 		//ジオコーディングに成功したらStringへ
 		if (!list_address.isEmpty()){
 
 			Address address = list_address.get(0);
 			StringBuffer strbuf = new StringBuffer();
 
 			//adressをStringへ
 			String buf;
 			for (int i = 0; (buf = address.getAddressLine(i)) != null; i++){
 				CustomLog.d(tag, "loop no."+i);
 				strbuf.append("address.getAddressLine("+i+"):"+buf+"\n");
 			}
 
 			string = strbuf.toString();
 
 		}
 
 		//失敗（Listが空だったら）
 		else {
 			CustomLog.d(tag, "Fail Geocoding");
 		}
 
 		CustomLog.d(tag, string);
 		return string;
 	}
 	
 	
 	
 	public void end(LocationListener listener){
 		mLocationManager.removeUpdates(listener);
 	}
 	String latitude = "";
 	String longitude = "";
 	String accuracy = "";
 	String altitude = "";
 	String time = "";
 	String speed = "";
 	String bearing = "";
 	Location mLocation;
 	public void onLocationChanged(Location location) {
 		//位置情報が変更された場合に呼び出される
 		mLocation = location;
 		latitude = String.valueOf(location.getLatitude());
 		longitude = String.valueOf(location.getLongitude());
 		accuracy = String.valueOf(location.getAccuracy());
 		altitude = String.valueOf(location.getAltitude());
 		time = String.valueOf(location.getTime());
 		speed = String.valueOf(location.getSpeed());
 		bearing = String.valueOf(location.getBearing());
        CustomLog.v("----------", "----------");
        CustomLog.v("Latitude（緯度）", String.valueOf(location.getLatitude()));
        CustomLog.v("Longitude（経度）", String.valueOf(location.getLongitude()));
        CustomLog.v("Accuracy（精度）", String.valueOf(location.getAccuracy()));
        CustomLog.v("Altitude（標高）", String.valueOf(location.getAltitude()));
        CustomLog.v("Time", String.valueOf(location.getTime()));
        CustomLog.v("Speed", String.valueOf(location.getSpeed()));
        CustomLog.v("Bearing", String.valueOf(location.getBearing()));
 		
 	}
 
 	public void onProviderDisabled(String provider) {
 		// LocationProviderが有効になった場合に呼び出される
 		
 	}
 
 	public void onProviderEnabled(String provider) {
 		// LocationProviderが無効になった場合に呼び出される
 		
 	}
 
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		//LocationProviderの状態が変更された場合に呼び出される
         switch (status) {
         case LocationProvider.AVAILABLE:
         	CustomLog.v("Status", "AVAILABLE");
             break;
         case LocationProvider.OUT_OF_SERVICE:
         	CustomLog.v("Status", "OUT_OF_SERVICE");
             break;
         case LocationProvider.TEMPORARILY_UNAVAILABLE:
         	CustomLog.v("Status", "TEMPORARILY_UNAVAILABLE");
             break;
         }
 		
 	}
 	public void onGpsStatusChanged(int event) {
 	    if (event == GpsStatus.GPS_EVENT_FIRST_FIX) {
 	      	CustomLog.v("GpsStatus", "GPS_EVENT_FIRST_FIX");
 	    } else if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
 	      	CustomLog.v("GpsStatus", "GPS_EVENT_SATELLITE_STATUS");
 	    } else if (event == GpsStatus.GPS_EVENT_STARTED) {
 	      	CustomLog.v("GpsStatus", "GPS_EVENT_STARTED");
 	    } else if (event == GpsStatus.GPS_EVENT_STOPPED) {
 	    	CustomLog.v("GpsStatus", "GPS_EVENT_STOPPED");
 	    }
 		
 	}
 }
