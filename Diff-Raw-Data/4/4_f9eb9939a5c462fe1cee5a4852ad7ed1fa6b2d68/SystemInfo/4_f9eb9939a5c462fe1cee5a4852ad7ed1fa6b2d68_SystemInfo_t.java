 package com.joy.launcher2.util;
 
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.util.Enumeration;
 import java.util.Locale;
 
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.os.Build;
 import android.telephony.TelephonyManager;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.WindowManager;
 
 import com.baidu.location.BDLocation;
 import com.baidu.location.BDLocationListener;
 import com.baidu.location.LocationClient;
 import com.baidu.location.LocationClientOption;
 import com.joy.launcher2.LauncherApplication;
 
 /**
  *  本机信息（id，mac，IME, sd operate，network status ）
  * @author wanghao
  */
 public class SystemInfo {
 
 	String TAG = "SystemInfo";
 	/**
 	 * 渠道号
 	 */
 	public static String channel;
 	/**
 	 * IMEI号
 	 */
 	public static String imei;
 	/**
 	 * IMSI号
 	 */
 	public static String imsi;
 	/**
 	 * 操作系统版本
 	 */
 	public static String os;
 	/**
 	 * 所在省份
 	 */
 	public static String province;
 	/**
 	 * 所在城市
 	 */
 	public static String city;
 	/**
 	 * IP地址
 	 */
 	public static String ip;
 	/**
 	 * 短信中心
 	 */
 //	public static String sms;
 	/**
 	 * 屏幕分辨率
 	 */
 	public static String display;
 	/**
 	 * 手机制造商家
 	 */
 	public static String product;
 	/**
 	 * 手机品牌
 	 */
 	public static String brand;
 	/**
 	 * 手机型号
 	 */
 	public static String model;
 	/**
 	 * 语言 (0 未知 1 简体中文 2 繁体中文 3 英文)
 	 */
 	public static String language;
 	/**
 	 * 运营商(0 未知 1 移动 2 联通 3 电信)
 	 */
 	public static Integer operators;
 	/**
 	 * 网络型号（0 未知 1 wifi 2G 3G 4G）
 	 */
 	public static Integer network;
 	/**
 	 * 桌面版本
 	 */
 	public static Integer vcode;
 	/**
 	 * 桌面版本
 	 */
 	public static String vname;
 	/**
 	 * mac
 	 */
 	public static String mac;
 	/**
 	 * 主板
 	 */
 	public static String board;
 	/**
 	 * cpu指令集
 	 */
 	public static String abi;
 	/**
 	 * 设备参数
 	 */
 	public static String device;
 	/**
 	 * 修订版本列表
 	 */
 	public static String id;
 	/**
 	 * 
 	 */
 	public static String mf;
 	/**
 	 * 描述build的标签
 	 */
 	public static String tags;
 	/**
 	 * builder类型
 	 */
 	public static String type;
 	/**
 	 * user
 	 */
 	public static String user;
 
 	/**
 	 * 设备ID
 	 */
 	public static String deviceid;
 
 	private LocationInfo locInfo;
 	
 //	private SmsCenter msmCenter;
 
 	static SystemInfo mSystemInfo;
 	private SystemInfo(){
 		locInfo = new LocationInfo();
 //		msmCenter = new SmsCenter();
 		initSystemInfo();
 	}
 
 	public static SystemInfo getInstance(){
 		if(mSystemInfo == null){
 			mSystemInfo = new SystemInfo();
 		}
 		return mSystemInfo;
 	}
 	public void setPushListenner(PushListenner pushListenner){
 		locInfo.setPushListenner(pushListenner);
 	}
 	public void initSystemInfo() {
 
 		channel = "1001";
 		vcode = Util.getVersionCode(LauncherApplication.mContext);
 		vname = "my_launcher";
 
 		brand = Build.BRAND;
 		model = Build.MODEL;
 		board = Build.BOARD;
 		abi = Build.CPU_ABI;
 		device = Build.DEVICE;
 		id = Build.ID;
 		tags = Build.TAGS;
 		type = Build.TYPE;
 		user = Build.USER;
 		product  = Build.PRODUCT;
 		mf = Build.MANUFACTURER;
 		os = Build.VERSION.RELEASE;
 		
 		imei = getImei();
 		imsi = getImsi();
 
 		mac = getMac();
 		language = getLanguage();
 		network = getNetwork();
 		display = getDisplay();
 		operators = getOperators();
 		ip = getLocalIpAddress();
 		
 		
 //		sms = msmCenter.getSmsCenter();
 		
		province = "";
		city = "";
 		
 		
 		if (SystemInfo.imei == null||SystemInfo.imei.equals("")) {
 			SystemInfo.imei = "1234567890";
 		}
 		if (SystemInfo.imsi == null||SystemInfo.imsi.equals("")) {
 			SystemInfo.imsi = "1234567890";
 		}
 		
 		deviceid = getDeviceID();
 		locInfo.getLocationInfo();
 	}
 
 	/**
 	 * 获取IMEI标识
 	 * 
 	 * @param context
 	 * @return
 	 */
 	private String getImei() {
 		TelephonyManager tm = (TelephonyManager) LauncherApplication.mContext.getSystemService(Context.TELEPHONY_SERVICE);
 		return tm.getDeviceId();
 	}
 
 	/**
 	 * 
 	 * @param imsi
 	 * @return 0 未知 1 移动 2 联通 3 电信
 	 */
 	private int getOperators() {
 		String imsi = getImsi();
 		int operator = 0;
 		if (imsi != null) {
 			if (imsi.startsWith("46000") || imsi.startsWith("46002")) {
 				// 因为移动网络编号46000下的IMSI已经用完，所以虚拟了一个46002编号，134/159号段使用了此编号
 				// 中国移动
 				operator = 1;
 			} else if (imsi.startsWith("46001")) {
 				// 中国联通
 				operator = 2;
 			} else if (imsi.startsWith("46003")) {
 				// 中国电信
 				operator = 3;
 			} else {
 				operator = 0;
 			}
 		}
 		return operator;
 	}
 
 	/**
 	 * 获取网络类型
 	 * 
 	 * @return 0 未知 1 wifi 2G 3G 4G
 	 */
 	private int getNetwork() {
 
 		ConnectivityManager connectMgr = (ConnectivityManager) LauncherApplication.mContext
 				.getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo info = connectMgr.getActiveNetworkInfo();
 		if (info == null) {
 			return 0;// 未知
 		}
 
 		int type = info.getType();
 		if (type == ConnectivityManager.TYPE_WIFI) {
 			return 1; // wifi
 		}
 		int substype = info.getSubtype();
 		switch (substype) {
 		case TelephonyManager.NETWORK_TYPE_GPRS:
 		case TelephonyManager.NETWORK_TYPE_EDGE:
 		case TelephonyManager.NETWORK_TYPE_CDMA:
 		case TelephonyManager.NETWORK_TYPE_1xRTT:
 		case TelephonyManager.NETWORK_TYPE_IDEN:
 			return 2;// 2G
 		case TelephonyManager.NETWORK_TYPE_UMTS:
 		case TelephonyManager.NETWORK_TYPE_EVDO_0:
 		case TelephonyManager.NETWORK_TYPE_EVDO_A:
 		case TelephonyManager.NETWORK_TYPE_HSDPA:
 		case TelephonyManager.NETWORK_TYPE_HSUPA:
 		case TelephonyManager.NETWORK_TYPE_HSPA:
 		case TelephonyManager.NETWORK_TYPE_EVDO_B:
 		case TelephonyManager.NETWORK_TYPE_EHRPD:
 		case TelephonyManager.NETWORK_TYPE_HSPAP:
 			return 3;// 3G
 		case TelephonyManager.NETWORK_TYPE_LTE:
 			return 4;// 4G
 		default:
 			return 0;// 未知
 		}
 	}
 
 	/**
 	 * 获取手机分辨率
 	 * @return
 	 */
 	private String getDisplay() {
 		DisplayMetrics dm = new DisplayMetrics();
 		WindowManager mWm = (WindowManager) LauncherApplication.mContext
 				.getSystemService(Context.WINDOW_SERVICE);
 		mWm.getDefaultDisplay().getMetrics(dm);
 		String display = dm.widthPixels + "*" + dm.heightPixels;
 		return display;
 	}
 
 	/**
 	 * 获取手机当前语言类型
 	 * @return
 	 */
 	private String getLanguage() {
 		
 		String language = Locale.getDefault().getLanguage();
 //		 0 未知 1 简体中文 2 繁体中文 3 英文',
 //		if (language.equals("zh")) {
 //			return 1;
 //		} else if (language.equals("zh-rCN")) {
 //			return 2;
 //		} else if (language.equals("en")) {
 //			return 3;
 //		}
 		return language;
 	}
 
 	/**
 	 * 获取ip号
 	 * @return
 	 */
 	public String getLocalIpAddress() {
 	     try {
 	         for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
 	             NetworkInterface intf = en.nextElement();
 	             for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
 	                 InetAddress inetAddress = enumIpAddr.nextElement();
 	                 if (!inetAddress.isLoopbackAddress()) {
 	                     return inetAddress.getHostAddress().toString();
 	                 }
 	             }
 	         }
 	     } catch (SocketException ex) {
 	         Log.i(TAG, ex.toString());
 	     }
 	     return null;
 	}
 	/**
 	 * 获取mac号
 	 * @return
 	 */
 	private String getMac() {
 		WifiManager wifi = (WifiManager) LauncherApplication.mContext
 				.getSystemService(Context.WIFI_SERVICE);
 		WifiInfo info = wifi.getConnectionInfo();
 		return info.getMacAddress();
 	}
 
 	/**
 	 * 获取IMSI标识
 	 * 
 	 * @param context
 	 * @return
 	 */
 	private String getImsi() {
 		TelephonyManager tm = (TelephonyManager) LauncherApplication.mContext
 				.getSystemService(Context.TELEPHONY_SERVICE);
 		return tm.getSubscriberId();
 	}
 
 	/**
 	 * 获取手机号码
 	 * 
 	 * @param context
 	 * @return
 	 */
 	private String getPhone(Context context) {
 		TelephonyManager tm = (TelephonyManager) context
 				.getSystemService(Context.TELEPHONY_SERVICE);
 		return tm.getLine1Number();
 	}
 
 	/**
 	 * 获取设备号
 	 * 
 	 * @return
 	 */
 	private String getDeviceID() {
 		if (deviceid == null) {
 
 			StringBuffer sb = new StringBuffer(200);
 			sb.append(mac).append(imei).append(board).append(brand).append(abi)
 					.append(device).append(display).append(id).append(mf)
 					.append(model).append(product).append(tags).append(type)
 					.append(user);
 			deviceid = Util.md5Encode(sb.toString());
 			Log.i(TAG, "2: "+deviceid);
 		}
 		
 		return deviceid;
 	}
 
 	/**
 	 * 获取地址信息
 	 * 
 	 * @author wanghao
 	 * 
 	 */
 	private class LocationInfo {
 
 		private LocationClient mLocationClient = null;
 		private MyLocationListenner myListener = new MyLocationListenner();
 		private PushListenner mPushListenner= null;
 		public LocationInfo() {
 			mLocationClient = new LocationClient(
 					LauncherApplication.mContext.getApplicationContext());
 			mLocationClient.registerLocationListener(myListener);
 			
 		}
 		public void setPushListenner(PushListenner l){
 			mPushListenner = l;
 		}
  
 		private void getLocationInfo() {
 			LocationClientOption option = new LocationClientOption();
 			option.setOpenGps(true);
 			option.setCoorType("bd09ll"); // 设置坐标类型
 			option.setAddrType("all"); // 设置地址信息，仅设置为“all”时有地址信息，默认无地址信息
 //			option.setScanSpan(1000);
 			option.disableCache(true);// 禁止启用缓存定位
 			mLocationClient.setLocOption(option);
 			mLocationClient.start();
 //			try {
 //				Thread.sleep(3000);
 //			} catch (InterruptedException e) {
 //				// TODO Auto-generated catch block
 //				e.printStackTrace();
 //			}
 //			mLocationClient.stop();
 
 		}
 
 		/**
 		 * 监听函数
 		 */
 		private class MyLocationListenner implements BDLocationListener {
 			@Override
 			public void onReceiveLocation(BDLocation location) {
 				if (location == null)
 					return;
 				StringBuffer sb = new StringBuffer(256);
 				if (location.getCity() == null) {
 					int type = mLocationClient.requestLocation();
 				}
 
 				province = location.getProvince();
 				city = location.getCity();
 				mLocationClient.stop();
 				
 				if (mPushListenner != null) {
 					if (province != null||city!= null) {
 						mPushListenner.onReceiveCompleted();
 					}else{
 						mPushListenner.onReceiveFailed();
 					}
 					
 				}
 				Log.i(TAG,
 						location.getLatitude() + " | "
 								+ location.getLongitude() + " | "
 								+ location.getRadius() + " | "
 								+ location.getProvince() + " | "
 								+ location.getCity() + " | "
 								+ location.getDistrict() + " | "
 								+ location.getAddrStr());
 			}
 
 			public void onReceivePoi(BDLocation poiLocation) {
 				if (poiLocation == null) {
 					return;
 				}
 				province = poiLocation.getProvince();
 				city = poiLocation.getCity();
 				Log.i(TAG,
 						poiLocation.getLatitude() + " | "
 								+ poiLocation.getLongitude() + " | "
 								+ poiLocation.getRadius() + " | "
 								+ poiLocation.getProvince() + " | "
 								+ poiLocation.getCity() + " | "
 								+ poiLocation.getDistrict() + " | "
 								+ poiLocation.getAddrStr());
 			}
 		}
 	}
 	public interface PushListenner{
 		public void onReceiveCompleted();
 		public void onReceiveFailed();
 	}
 	@Override
 	public String toString() {
 		// TODO Auto-generated method stub
 		return "channel: "+channel+"--imei: "+imei+"--imsi: "+imsi+"--os: "+os
 				+"--province: "+province+"--city: "+city+"--ip: "+ip
 				+"--display: "+display+"--product: "+product
 				+"--brand: "+brand+"--model: "+model+"--language: "+language
 				+"--operators: "+operators+"--network: "+network+"--vcode: "+vcode
 				+"--vname: "+vname+"--mac: "+mac+"--board: "+board
 				+"--abi: "+abi+"--device: "+device+"--id: "+id
 				+"--mf: "+mf+"--tags: "+tags+"--type: "+type
 				+"--user: "+user;
 	}
 }
