 package fr.insalyon.pyp.tools;
 
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.util.Enumeration;
 import java.util.Locale;
 import java.util.logging.Level;
 import android.content.Context;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.location.Location;
 import android.location.LocationManager;
 import android.net.ConnectivityManager;
 import android.provider.Settings.Secure;
 import android.telephony.TelephonyManager;
 
 public class TerminalInfo {
 
 	public static final String WIFI = "WIFI";
 	private static Context context = PYPContext.getContext();
 
 	/**
 	 * @return - true if connected to the internet, false otherwise
 	 */
 	public static boolean isConnected() {
 		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
 
 		if (connectivityManager.getActiveNetworkInfo() != null) {
 			AppTools.log("Connectivity informations : " + connectivityManager.getActiveNetworkInfo().toString(), Level.FINER);
 			return true;
 		} else {
 			AppTools.log("Connectivity informations : no connection", Level.FINER);
 			return false;
 		}
 
 	}
 
 	/**
 	 * @return - true if connected to the mobile network, false otherwise
 	 */
 	public static boolean isMobileConnected() {
 		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
 		final android.net.NetworkInfo mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
 
 		if (mobile.isAvailable()) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * returns the Connection type of the terminal
 	 * 
 	 * @return
 	 */
 	public static String getConnectionType() {
 
 		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
 		if (connectivityManager.getActiveNetworkInfo() != null) {
 			String typeName = connectivityManager.getActiveNetworkInfo().getTypeName();
 			if (typeName != "WIFI") {
 				typeName = "gsm";
 			}
 			return typeName;
 		} else {
 			return "";
 		}
 	}
 
 	/**
 	 * returns the app's version
 	 * 
 	 * @return
 	 */
 	public static String getAppVersion() {
 		PackageInfo pckInfo;
 		try {
 			pckInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
 			return pckInfo.versionName;
 		} catch (NameNotFoundException e) {
 			// cannot happen as we use the 'getPackageName' function
 		}
 		AppTools.log("Package name not found - Returning default app version", Level.SEVERE);
 		return "1.0"; // default
 	}
 
 	/**
 	 * returns the app's version
 	 * 
 	 * @return
 	 */
 	public static int getAppVersionCode() {
 		PackageInfo pckInfo;
 		try {
 			pckInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
 			return pckInfo.versionCode;
 		} catch (NameNotFoundException e) {
 			// cannot happen as we use the 'getPackageName' function
 		}
 		AppTools.log("Package name not found - Returning default app version", Level.SEVERE);
 		return 1; // default
 	}
 
 	/**
 	 * returns the terminal name
 	 * 
 	 * @return
 	 */
 	public static String getTerminalName() {
 		String model = android.os.Build.MODEL;
 		model = model.replaceAll(" ", "");
 		return model;
 	}
 
 	/**
 	 * returns the OS name
 	 * 
 	 * @return
 	 */
 	public static String getOSName() {
 		String osName = "android";// System.getProperty("os.name");
 		return osName;
 	}
 
 	/**
 	 * returns the OS name
 	 * 
 	 * @return
 	 */
 	public static String getOSNameCamelCase() {
 		String osName = "Android";// System.getProperty("os.name");
 		return osName;
 	}
 
 	/**
 	 * returns the OS version
 	 * 
 	 * @return
 	 */
 	public static String getOSVersion() {
 		String osVersion = android.os.Build.VERSION.RELEASE;
 		return osVersion;
 	}
 
 	/**
 	 * returns the terminal manufacturer
 	 * 
 	 * @return
 	 */
 	public static String getTerminalManufacturer() {
 		return android.os.Build.MANUFACTURER;
 	}
 
 	/**
 	 * @return the IP Address
 	 * @throws SGMobileException
 	 */
 	public static String getLocalIpAddress() {
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
 			AppTools.error(ex.toString());
 		}
 		return "0.0.0.0";
 	}
 
 	/**
 	 * returns the Android ID (i.e. UDID)
 	 * 
 	 * @param context
 	 * @return
 	 */
 	public static String getAndroidID() {
 		String androidId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
 		if (androidId == null) {
 
 			// in case we run use the imei number instead of the android id
 			TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
 			String uid = telephonyManager.getDeviceId();
 			androidId = uid;
 		}
 		if (androidId == null) {
 			// Android Emulator
 			androidId = "5ae6f56c54379e878db478823161f000";
 		}
 		return androidId;
 	}
 
 	/**
 	 * 
 	 * @return the user current locale
 	 */
 	public static String getDeviceLang() {
 		return Locale.getDefault().toString();
 	}
 	
 	public static Location getPosition(){
 		LocationManager mlocManager = (LocationManager) PYPContext.getContext().getSystemService(Context.LOCATION_SERVICE);
		Location l = mlocManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);	
		if (l==null)
			l = mlocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (l==null)
			l = mlocManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
		return l;
 	}
 	
 
 }
