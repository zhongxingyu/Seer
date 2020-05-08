 package com.msx7.gps;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.IBinder;
 
 import com.google.gson.Gson;
 
 /**
  * 
  * 经度范围 -180°~180° 纬度范围 -90°~90°<br/>
  * 1、使用百度定位，应添加如下 定位服务 和 声明使用权限<br/>
  * <br/>
  * <code>
  * &lt;service android:name="com.baidu.location.f" android:enabled="true" android:process=":remote"/&gt; <br/>
  * </code>
  * 
  * <code>
  * &lt;uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/ &gt;  <br/>
  * &lt;uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/ &gt;  <br/>
  * &lt;uses-permission  android:name="android.permission.ACCESS_WIFI_STATE"/ &gt;  <br/>
  * &lt;uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/ &gt;  <br/>
  * &lt;uses-permission android:name="android.permission.CHANGE_WIFI_STATE"/ &gt; <br/> 
  * &lt;uses-permission android:name="android.permission.READ_PHONE_STATE"/ &gt;  <br/>
  * &lt;uses-permission  android:name="android.permission.WRITE_EXTERNAL_STORAGE"/ &gt;  <br/>
  * &lt;uses-permission android:name="android.permission.INTERNET" /&gt; <br/>
  * &lt;uses-permission  android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS"/&gt;<br/>  
  * &lt;uses-permission android:name="android.permission.READ_LOGS"/ &gt;   <br/>
  * </code><br/>
  * 2、使用谷歌定位、系统定位，你应该申明 精准定位/粗略定位的权限<br/>
  * <code>
  * &lt;uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/&gt;<br/>
  * &lt;uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/&gt;
  * </code><br/>
  * <br/>
  * 3、在你的Androidmanifest.xml中申明此service，应当添加action {@link #ACTION_SERVICE}<br/>
  * <br/>
  * 4、你可以接口{@link #ACTION_SEND_LOCATION}的广播来接受定位的信息，使用如下方式来获得定位信息<br/>
  * <br/>
  * <code>
  * 		String json = intent.getStringExtra(PARAM_LOCATION);<br/>
  * 		Msx7Location location=new Gson().fromJson(json, Msx7Location.class);
  * </code><br/>
  * <br/>
  * 5、启动方式:<br/>
  * <code>
  * 		Intent intent=new Intent(LocationServices.ACTION_SERVICE);<br/>
  * 		intent.putExtra(FLAG_GPS_TYPE, GPS_GOOGLE|GPS_BAIDU);<br/>
  * 		//TODO:添加其他参数，详情查看 {@link GoogleGPS}、{@link BaiduGPS}、{@link SystemGPS}等对应注释<br/>
  * 		startService(intent);<br/>
  * 		</code><br/><br/>
  * 6、扩展<br/>
  * 		实现此{@link LocationServices#loadOthers()}方法，加载其他定义的IGPS服务<br/>
  * @author Msx7
  * 
  */
 public class LocationServices extends Service {
 	public static final String ACTION_SEND_LOCATION = "com.msx7.gps.send_location";
 	public static final String ACTION_SERVICE = "com.msx7.gps.locationservices";
 	public static final String PARAM_LOCATION = "param_location";
 	public static final int GPS_SYSTEM = 0x0001;
 	public static final int GPS_GOOGLE = GPS_SYSTEM << 1;
 	public static final int GPS_BAIDU = GPS_SYSTEM << 2;
 	public static final String FLAG_GPS_TYPE = "gpsType";
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		int type = intent.getIntExtra(FLAG_GPS_TYPE, GPS_SYSTEM);
 		startGPS(type, intent);
 		return super.onStartCommand(intent, flags, startId);
 
 	}
 
 	private IGPS startGPS(int type, Intent data) {
 		IGPS gps = null;
 		for (IGPS _gps : getIGPSList()) {
 			if (_gps.doFilter(this, data, type)) {
 				gps = _gps;
 				gps.setLocationUpdate(upadate);
 				break;
 			}
 		}
 		return gps;
 	}
 
 	LocationUpadate upadate = new LocationUpadate() {
 
 		@Override
 		public void updateLocation(Msx7Location location) {
 			Intent intent = new Intent(ACTION_SEND_LOCATION);
 			intent.putExtra(PARAM_LOCATION, new Gson().toJson(location));
 
 			sendBroadcast(intent);
 		}
 	};
 
 	@Override
 	public IBinder onBind(Intent intent) {
 
 		return null;
 	}
 
 	public List<IGPS> getIGPSList() {
 		List<IGPS> gps = new ArrayList<IGPS>();
 		gps.add(new GoogleGPS());
 		gps.add(new BaiduGPS());
 		gps.add(new SystemGPS());
 		List<IGPS> _gps = loadOthers();
 		if (_gps != null)
 			gps.addAll(_gps);
 		return gps;
 	}
 
 	protected List<IGPS> loadOthers(){
 		return null;
 	}
 	public static interface LocationUpadate {
 		public void updateLocation(Msx7Location location);
 	}
 }
