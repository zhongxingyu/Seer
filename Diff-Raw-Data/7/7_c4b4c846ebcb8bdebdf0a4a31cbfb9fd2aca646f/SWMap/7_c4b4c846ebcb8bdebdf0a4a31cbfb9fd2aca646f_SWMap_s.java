 package com.run.stepway;
 
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Application;
 import android.location.Location;
 import android.os.Handler;
 import android.os.Message;
 import android.text.format.Time;
 import android.util.Log;
 
 import com.baidu.mapapi.BMapManager;
 import com.baidu.mapapi.GeoPoint;
 import com.baidu.mapapi.LocationListener;
 import com.baidu.mapapi.MKLocationManager;
 import com.baidu.mapapi.MapActivity;
 import com.baidu.mapapi.MapController;
 import com.baidu.mapapi.MapView;
 import com.baidu.mapapi.MyLocationOverlay;
 
 public class SWMap {
 
 	static BMapManager mBMapMan = null;
 	
 	static double DEF_PI = 3.14159265359; // PI
 	static double DEF_2PI= 6.28318530712; // 2*PI
 	static double DEF_PI180= 0.01745329252; // PI/180.0
 	static double DEF_R =6370693.5; // radius of earth
 	
 	MapView mMapView = null;
 	MapController mMapController = null;
 	LocationListener mLocationListener = null;
 	MyLocationOverlay mMyloc = null;
 	boolean mLocated = false;
 	boolean mRuning = false;
 	ArrayList<GeoPoint> mTrackPoints = new ArrayList<GeoPoint>();
 	float mSpeed = 0.0f;
 	float mDistance = 0.0f;
 
 	Time mStartTime = new Time(); 
 	Time mEndTime = new Time(); 
 	
 	SWHandler mHandler = null;
 	
 	Timer mTimer = new Timer( );
 
 	TimerTask mTask = new TimerTask( ) {
 		public void run ( ) {
 			Message message = new Message( );
 			message.what = SWHandler.REFRESH;
 			mHandler.sendMessage(message);
 		}
 	};
 	
 	static SWMap mMap = null;
 	
 	static SWMap GetInstance(){
 		if(mMap == null){
 			mMap = new SWMap();
 		}
 		return mMap;
 	}
 	
 	public SWMap(){
 		
 	}
 
 	public void init(Application app){
         mBMapMan = new BMapManager(app);
         mBMapMan.init("702F895FB6970D5DC38E3C9AC3C91A240800A7A9", null);
         mBMapMan.getLocationManager().setNotifyInternal(1, 1);
 	}
 	
 	public void destroy(){
 		if (mBMapMan != null) {
 			mBMapMan.destroy();
 			mBMapMan = null;
 		}
 	}
 	
 	public void onCreate(MapActivity mapActivity, MapView mapview){
 		mBMapMan.start();
 		mapActivity.initMapActivity(mBMapMan);
          
         mMapView = mapview;
         mMapView.setBuiltInZoomControls(true);  //õſؼ
          
         mMapController = mMapView.getController();  // õmMapViewĿȨ,ƺƽƺ
         GeoPoint point = new GeoPoint((int) (39.915 * 1E6),
                 (int) (116.404 * 1E6));  //øľγȹһGeoPointλ΢ ( * 1E6)
 //        mMapController.setCenter(point);  //õͼĵ
 //        mMapController.setZoom(15);    //õͼzoom
         
         mLocationListener = new LocationListener(){
 
 			@Override
 			public void onLocationChanged(Location location) {
 				if(location != null && location.getProvider().compareTo("gps") == 0){
 					String str = location.getProvider();
 					mLocated = true;
 					if(isRuning()){
 				        GeoPoint point = new GeoPoint((int) (location.getLatitude() * 1E6),
 				                (int) (location.getLongitude() * 1E6));
 				        if(!mTrackPoints.isEmpty()){
 				        	GeoPoint pointLast = mTrackPoints.get(mTrackPoints.size()-1);
 				        	mDistance += GetDistance(location.getLongitude(),location.getLatitude(),
 				        			pointLast.getLongitudeE6()/1E6,pointLast.getLatitudeE6()/1E6);
 				        }
 				        mTrackPoints.add(point);
 				        
 				        mSpeed = location.getSpeed();
 						Message msg = new Message();
 						msg.what = SWHandler.REFRESH;
 						mHandler.sendMessage(msg);
 					}
 			       // mMapController.setCenter(point);
 				}else{
 					mLocated = false;
 				}
 			}
         };
         
         mMyloc = new SWTrackOverlay(mapActivity, mMapView);
         mMyloc.enableMyLocation(); // öλ
         //mMyloc.enableCompass();    // ָ
         mMapView.getOverlays().add(mMyloc);
 	}
 
 	public void onDestroy() {
 //	    if (mBMapMan != null) {
 //	        mBMapMan.destroy();
 //	        mBMapMan = null;
 //	    }
 	}
 
 	public void onPause() {
 	    if (mBMapMan != null) {
 	        mBMapMan.stop();
 	        closeGps();
 	    }
 	}
 
 	public void onResume() {
 	    if (mBMapMan != null) {
 	        mBMapMan.start();
 	        openGps();
 	    }
 	}
 
 	public boolean isSatellite(){
 		return mMapView.isSatellite();
 	}
 	
 	public void setSatellite(boolean bSatellite){
 		mMapView.setSatellite(bSatellite); 
 	}
 	
 	public boolean isLocated(){
 		return mLocated;
 	}
 	
 	public void openGps(){
 		mBMapMan.getLocationManager().enableProvider(MKLocationManager.MK_GPS_PROVIDER);
 	}
 
 	public void closeGps(){
 		mBMapMan.getLocationManager().disableProvider(MKLocationManager.MK_GPS_PROVIDER);
 	}
 	
 	public void goCurPos(){
 		Location location = mBMapMan.getLocationManager().getLocationInfo();
 		if(location != null){
 			GeoPoint point = new GeoPoint((int) (location.getLatitude() * 1E6),
 	                (int) (location.getLongitude() * 1E6));
 			mMapController.setCenter(point);
 		}
 	}
 	
 	public boolean isRuning(){
 		return mRuning;
 	}
 	
 	public void startRun(){
 		mTrackPoints.clear();
 		//mMapController.setZoom(15);
 		goCurPos();
 		mBMapMan.getLocationManager().requestLocationUpdates(mLocationListener);
 		mStartTime.setToNow();
 		mEndTime.setToNow();
 		
 		mTimer.schedule(mTask,0,1000);
 		mRuning = true;
 		
 //		mTrackPoints.add(new GeoPoint((int) (39.915 * 1E6),
 //      (int) (116.404 * 1E6)));
 //
 //		mTrackPoints.add(new GeoPoint((int) (39.935 * 1E6),
 //      (int) (116.404 * 1E6)));
 //
 //		mTrackPoints.add(new GeoPoint((int) (39.915 * 1E6),
 //      (int) (116.304 * 1E6)));
 	}
 	
 	public void stopRun(){
 		
 		mRuning = false;
 		mTimer.cancel();
 		mBMapMan.getLocationManager().removeUpdates(mLocationListener);
 		mSpeed = 0.0f;
 		mDistance = 0.0f;
 		mEndTime.setToNow();
 	}
 	
 	public ArrayList<GeoPoint> getTrackPoints(){
 		ArrayList<GeoPoint> trackPoints = new ArrayList<GeoPoint>();
 		trackPoints.addAll(mTrackPoints);
 		return trackPoints;
 	}
 	
 	public float getSpeed(){
 		return mSpeed;
 	}
 	
 	public float getDistance(){
 		return mDistance;
 	}
 	
 	public Time getRunTime(){
 		Time runTime = new Time();
 		if(isRuning()){
 			Time curTime = new Time(); 
 			curTime.setToNow();
 			long millis = curTime.toMillis(false)-mStartTime.toMillis(false);
			runTime.set(millis);
 		}else{
 			long millis = mEndTime.toMillis(false)-mStartTime.toMillis(false);
			runTime.set(millis);
 		}
 		return runTime;
 	}
 	
 	public void setHandler(SWHandler handler){
 		mHandler = handler;
 	}
 	
 	public double GetDistance(double lon1, double lat1, double lon2, double lat2)
 	{
 		double ew1, ns1, ew2, ns2;
 
 		double distance;
 
 		// ǶתΪ
 		ew1 = lon1 * DEF_PI180;
 		ns1 = lat1 * DEF_PI180;
 		ew2 = lon2 * DEF_PI180;
 		ns2 = lat2 * DEF_PI180;
 
 		// ԲӻеĽ()
 		distance = Math.sin(ns1) * Math.sin(ns2) + Math.cos(ns1) * Math.cos(ns2) * Math.cos(ew1 - ew2);
 
 		// [-1..1]Χڣ
 		if (distance > 1.0)
 			distance = 1.0;
 		else if (distance < -1.0)
 			 distance = -1.0;
 
 		// Բӻ
 		distance = DEF_R * Math.acos(distance);
 		
 		return distance;
 	}
 }
