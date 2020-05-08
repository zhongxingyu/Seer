 package com.kyview;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Random;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationManager;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.telephony.TelephonyManager;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.WindowManager;
 
 import com.kuaiyou.adfill.util.Utils;
 import com.kyview.AdViewTargeting.RunMode;
 import com.kyview.AdViewTargeting.UpdateMode;
 import com.kyview.adapters.AdViewAdapter;
 import com.kyview.base64.Crypts;
 import com.kyview.obj.Extra;
 import com.kyview.obj.Ration;
 import com.kyview.util.AdViewUtil;
 
 public class AdViewManager {
 	public String keyAdView;
 
 	private Extra extra;
 	private List<Ration> rationsList;
 	// private List<Ration> rationsList_pri;
 	private double totalWeight = 0;
 	private WeakReference<Context> contextReference;
 
 	// Default config expire timeout is 20 minutes.
 	public static int configExpireTimeout = 2 * 600;
 
 	Iterator<Ration> rollovers;
 	Iterator<Ration> rollover_pri;
 
 	public int mSimulator = 0;
 
 	private final static String PREFS_STRING_CONFIG = "config";
 	private boolean youmiInit = true;
 	private boolean bGotNetConfig = false;
 	public static long mLastConfigTime = 0;
 
 	public static final long CONFIG_SERVER_LIMIT_MSTIME = 60 * 5 * 1000; // ms.
 
 	public boolean bLocationForeign = false;
 	public String mLocation = "";
 
 	public int width;
 	public int height;
 	public String mDeviceid = "";
 
 	public AdViewManager(WeakReference<Context> contextReference,
 			String keyAdView) {
 		Log.i("Android", "Creating weivda reganam...");
 		this.contextReference = contextReference;
 		this.keyAdView = keyAdView;
 		mDeviceid = getDeviceID((Context) contextReference.get());
 		WindowManager WM = (WindowManager) ((Context) contextReference.get())
 				.getSystemService("window");
 		width = WM.getDefaultDisplay().getWidth();
 		height = WM.getDefaultDisplay().getHeight();
 		if (width > height) {
 			int temp = width;
 			width = height;
 			height = temp;
 		}
 
 		mSimulator = isSimulator();
 
 		bLocationForeign = isLocateForeign();
 		if (bLocationForeign == false)
 			mLocation = "china";
 		else
 			mLocation = "foreign";
 	}
 
 	public void setYoumiInit(boolean flag) {
 		youmiInit = flag;
 	}
 
 	public boolean getYoumiInit() {
 		return youmiInit;
 	}
 
 	public synchronized Extra getExtra() {
 		if (totalWeight <= 0) {
 			return null;
 		} else {
 			return this.extra;
 		}
 	}
 
 	public int getConfigExpiereTimeout() {
 		return configExpireTimeout;
 	}
 
 	private Ration afRation() {
 		Ration ration = new Ration();
 		ration.type = 997;
 		return ration;
 	}
 
 	public synchronized Ration getRation() {
 		Random random = new Random();
 
 		double r = random.nextDouble() * totalWeight;
 		double s = 0;
 
 		Iterator<Ration> it = this.rationsList.iterator();
 		Ration ration = null;
 		while (it.hasNext()) {
 			ration = it.next();
 			s += ration.weight;
 
 			if (s >= r) {
 				break;
 			}
 		}
 
 		return ration;
 	}
 
 	public synchronized Ration getRollover() {
 		if (this.rollovers == null)
 			return null;
 		Ration ration = null;
 		if (this.rollovers.hasNext())
 			ration = this.rollovers.next();
 		return ration;
 	}
 
 	public Ration getRollover_pri() {
 		int max = 100000000;
 		if (this.rollover_pri == null) {
 			return null;
 		}
 
 		Ration ration = null;
 		Ration ration_pri = null;
 		while (this.rollover_pri.hasNext()) {
 
 			ration = this.rollover_pri.next();
 			if (ration.priority < max) {
 				ration_pri = ration;
 				max = ration.priority;
 			}
 
 		}
 
 		return ration_pri;
 	}
 
 	public synchronized void resetRollover() {
 		Class<?> clazz = null;
 		try {
			clazz = Class.forName("com.kuaiyou.adfill.util.Utils");
 			if (null != clazz && AdViewLayout.isadFill)
 				Utils.resetList(AdViewUtil.adfill_count,
 						AdViewUtil.common_count, AdViewUtil.adfill_precent);
 		} catch (ClassNotFoundException e) {
 		} finally {
 			this.rollovers = this.rationsList.iterator();
 		}
 	}
 
 	private String getLocalConfig(String sdkkey) {
 		if (sdkkey == null || sdkkey.length() == 0) {
 			return null;
 		}
 
 		Context context = contextReference.get();
 		InputStream is;
 		String filename = sdkkey + ".txt";
 		String localconfig = null;
 
 		try {
 			is = context.getAssets().open(filename);
 			localconfig = convertStreamToString(is);
 			is.close();
 		} catch (Exception e) {
 			AdViewUtil.logError("", e);
 			return null;
 		}
 
 		AdViewUtil.logInfo("localconfig=" + localconfig);
 		return localconfig;
 	}
 
 	public boolean needUpdateConfig() {
 		return AdViewTargeting.getUpdateMode() == AdViewTargeting.UpdateMode.EVERYTIME ? true
 				: (!bGotNetConfig || (System.currentTimeMillis()
 						- mLastConfigTime >= configExpireTimeout * 1000));
 	}
 
 	public void fetchConfig(AdViewLayout adViewLayout) {
 		String jsonString = actFetchConfig(AdViewTargeting.getUpdateMode() == UpdateMode.EVERYTIME);
 
 		if ((jsonString == null || jsonString.length() == 0)
 				&& (AdViewTargeting.getUpdateMode() != UpdateMode.EVERYTIME)) {
 			jsonString = actFetchConfig(true);
 
 			if (jsonString == null || jsonString.length() == 0) {
 				jsonString = getLocalConfig(this.keyAdView);
 				parseOfflineConfigurationString(adViewLayout, jsonString);
 				return;
 			}
 		}
 
 		parseConfigurationString(adViewLayout, jsonString);
 	}
 
 	public static String performGetContent(String url) {
 		String contentStr = null;
 		HttpClient httpClient = new DefaultHttpClient();
 		HttpGet httpGet = new HttpGet(url);
 
 		HttpResponse httpResponse;
 		try {
 			httpResponse = httpClient.execute(httpGet);
 
 			if (httpResponse.getStatusLine().getStatusCode() == 200) {
 				HttpEntity entity = httpResponse.getEntity();
 
 				if (entity != null) {
 					InputStream inputStream = entity.getContent();
 					contentStr = convertStreamToString(inputStream);
 				}
 			}
 		} catch (ClientProtocolException e) {
 			AdViewUtil.logError("", e);
 		} catch (IOException e) {
 			AdViewUtil.logError("", e);
 		} finally {
 			httpClient.getConnectionManager().shutdown();
 		}
 		return contentStr;
 	}
 
 	private String actFetchConfig(boolean bForceFromServer) {
 		Context context = contextReference.get();
 		if (null == context)
 			return null;
 		SharedPreferences adViewPrefs = context.getSharedPreferences(keyAdView,
 				Context.MODE_PRIVATE);
 
 		String jsonString = "";
 		if (!bForceFromServer) {
 			jsonString = adViewPrefs.getString(PREFS_STRING_CONFIG, null);
 			mLastConfigTime = adViewPrefs.getLong(
 					AdViewUtil.PREFS_STRING_TIMESTAMP, 0);
 			return jsonString;
 		}
 
 		String url = String.format(AdViewUtil.urlConfig, this.keyAdView,
 				AdViewLayout.appVersion, mSimulator, mLocation,
 				AdViewUtil.currentSecond(), AdViewUtil.VERSION);
 		// AdViewUtil.logInfo(url);
 		jsonString = performGetContent(url);
 
 		if (null != jsonString && jsonString.length() > 0) {
 			if (checkConfigurationString(jsonString) == true) {
 				mLastConfigTime = System.currentTimeMillis();
 				// parseConfigurationString(jsonString);
 				// if(this.rationsList.size() > 0)
 				{
 					SharedPreferences.Editor editor = adViewPrefs.edit();
 					editor.putString(PREFS_STRING_CONFIG, jsonString);
 					editor.putLong(AdViewUtil.PREFS_STRING_TIMESTAMP,
 							System.currentTimeMillis());
 					editor.commit();
 				}
 				bGotNetConfig = true;
 			} else {
 				jsonString = "";
 			}
 		} else {
 			jsonString = "";
 		}
 
 		return jsonString;
 	}
 
 	public void fetchConfigFromServer(AdViewLayout adViewLayout) {
 		if (bGotNetConfig
 				&& System.currentTimeMillis() - mLastConfigTime < CONFIG_SERVER_LIMIT_MSTIME) {
 			// if last server fetch time is limited in some minutes (like 5),
 			// won't fetch again.
 			return;
 		}
 
 		String jsonString = actFetchConfig(true);
 		if (null != jsonString && jsonString.length() > 0)
 			parseConfigurationString(adViewLayout, jsonString);
 	}
 
 	private static String convertStreamToString(InputStream is) {
 		BufferedReader reader = new BufferedReader(new InputStreamReader(is),
 				8192);
 		StringBuilder sb = new StringBuilder();
 
 		String line = null;
 		try {
 			while ((line = reader.readLine()) != null) {
 				sb.append(line + "\n");
 			}
 		} catch (IOException e) {
 
 			return null;
 		} finally {
 			try {
 				is.close();
 			} catch (IOException e) {
 
 				return null;
 			}
 		}
 		return sb.toString();
 	}
 
 	private boolean checkConfigurationString(String jsonString) {
 		boolean ret = false;
 		try {
 			JSONObject json = new JSONObject(jsonString);
 			if (json.has("extra") && json.has("rations"))
 				ret = true;
 		} catch (JSONException e) {
 			AdViewUtil.logError("", e);
 			ret = false;
 		} catch (NullPointerException e) {
 			AdViewUtil.logError("", e);
 			ret = false;
 		}
 		AdViewUtil.logInfo("ret=" + ret);
 		return ret;
 	}
 
 	private void parseConfiguration(AdViewLayout adViewLayout, String jsonString) {
 		AdViewUtil.logInfo(jsonString);
 		String afp = null;
 		try {
 			JSONObject json = new JSONObject(jsonString);
 			AdViewUtil.configVer = json.optInt("version", 0);
 			if (json.has("adFill"))
 				if (json.getString("adFill").equals("0"))
 					AdViewLayout.isadFill = false;
 				else
 					AdViewLayout.isadFill = true;
 			else
 				AdViewLayout.isadFill = false;
 			parseExtraJson(json.getJSONObject("extra"));
 			parseRationsJson(json.getJSONArray("rations"));
 
 			if (json.has("afp")) {
 				afp = json.getString("afp");
 				afp = Crypts.xorMapDecrypt(afp);
 				AdViewUtil.adfill_precent = (Double.parseDouble(afp) / 100.0D);
 			}
 		} catch (JSONException e) {
 			this.extra = new Extra();
 		} catch (NullPointerException e) {
 			this.extra = new Extra();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private void parseConfigurationString(AdViewLayout adViewLayout,
 			String jsonString) {
 		parseConfiguration(adViewLayout, jsonString);
 	}
 
 	private void parseOfflineConfigurationString(AdViewLayout adViewLayout,
 			String jsonString) {
 		String afp = null;
 		try {
 			JSONObject json = new JSONObject(jsonString);
 
 			if (bLocationForeign == false) {
 				json = json.getJSONObject("china_cfg");
 			} else {
 				json = json.getJSONObject("foreign_cfg");
 			}
 			if (json.has("adFill"))
 				if (json.getString("adFill").equals("0"))
 					AdViewLayout.isadFill = false;
 				else
 					AdViewLayout.isadFill = true;
 			else
 				AdViewLayout.isadFill = false;
 
 			AdViewUtil.configVer = json.optInt("version", 0);
 			parseExtraJson(json.getJSONObject("extra"));
 			parseRationsJson(json.getJSONArray("rations"));
 
 			if (json.has("afp")) {
 				afp = json.getString("afp");
 				afp = Crypts.xorMapDecrypt(afp);
 				AdViewUtil.adfill_precent = (Double.parseDouble(afp) / 100.0D);
 			}
 		} catch (JSONException e) {
 			parseConfiguration(adViewLayout, jsonString);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private synchronized void parseExtraJson(JSONObject json) {
 		Extra extra = new Extra();
 
 		try {
 			extra.cycleTime = json.getInt("cycle_time");
 			extra.locationOn = json.getInt("loacation_on");
 			extra.transition = json.getInt("transition");
 			extra.report = json.getString("report");
 
 			AdViewUtil.initConfigUrls(extra.report);
 
 			JSONObject backgroundColor = json
 					.getJSONObject("background_color_rgb");
 			extra.bgRed = backgroundColor.getInt("red");
 			extra.bgGreen = backgroundColor.getInt("green");
 			extra.bgBlue = backgroundColor.getInt("blue");
 			extra.bgAlpha = backgroundColor.getInt("alpha") * 255;
 
 			JSONObject textColor = json.getJSONObject("text_color_rgb");
 			extra.fgRed = textColor.getInt("red");
 			extra.fgGreen = textColor.getInt("green");
 			extra.fgBlue = textColor.getInt("blue");
 			extra.fgAlpha = textColor.getInt("alpha") * 255;
 		}
 
 		catch (JSONException e) {
 
 		}
 		this.extra = extra;
 	}
 
 	public synchronized List<Ration> getRationList() {
 		return this.rationsList;
 	}
 
 	public synchronized Ration getadFillRation() {
 		Ration ration = null;
 		for (int i = 0; i < this.rationsList.size(); i++) {
 			if (rationsList.get(i).type == 28) {
 				ration = rationsList.get(i);
 				break;
 			}
 		}
 		return ration;
 	}
 
 	public synchronized Ration getAdFill() {
 		return afRation();
 	}
 
 	private synchronized void parseRationsJson(JSONArray json) {
 		List<Ration> rationsList = new ArrayList<Ration>();
 		// List<Ration> rationsList_pri =new ArrayList<Ration>();
 		List<Ration> rationsList_ex = new ArrayList<Ration>();
 		// List<Ration> rationsList_pri_ex =new ArrayList<Ration>();
 		double totalweight = 0;
 		double totalweight_ex = 0;
 
 		// this.totalWeight = 0;
 
 		try {
 			int i;
 			for (i = 0; i < json.length(); i++) {
 				JSONObject jsonRation = json.getJSONObject(i);
 				if (jsonRation == null) {
 					continue;
 				}
 
 				Class<? extends AdViewAdapter> adapterClass = AdViewAdRegistry
 						.getInstance().adapterClassForAdType(
 								jsonRation.getInt("type"));
 
 				if (null == adapterClass) {
 					AdViewUtil.logInfo("don't include ad="
 							+ jsonRation.getInt("type"));
 					continue;
 				}
 
 				Ration ration = new Ration();
 
 				ration.nid = jsonRation.getString("nid");
 				ration.type = jsonRation.getInt("type");
 				ration.name = jsonRation.getString("nname");
 				ration.weight = jsonRation.getInt("weight");
 				ration.priority = jsonRation.getInt("priority");
 				ration.key = jsonRation.getString("key");
 				ration.key2 = jsonRation.optString("key2");
 				ration.key3 = jsonRation.optString("key3");
 				ration.type2 = jsonRation.optInt("type2");
 				ration.logo = jsonRation.optString("logo");
 
 				rationsList.add(ration);
 				totalweight += ration.weight;
 			}
 		} catch (JSONException e) {
 
 		}
 		// rationsList.add(getAdFill());
 		// if set the location optimizing
 		/*
 		 * if(this.extra.locationOn == 0){ //if the location is in China
 		 * if(isLocateForeign() == false){ if(rationsList.size() <= 0){
 		 * rationsList = rationsList_ex; totalweight = totalweight_ex; } //
 		 * if(rationsList_pri.size() <= 0){ // rationsList_pri =
 		 * rationsList_pri_ex; // } }else{ if(rationsList_ex.size() > 0){
 		 * rationsList = rationsList_ex; totalweight = totalweight_ex;
 		 * //rationsList_pri = rationsList_pri_ex; } } }else
 		 */
 		{
 			rationsList.addAll(rationsList_ex);
 
 			totalweight += totalweight_ex;
 			// rationsList_pri.addAll(rationsList_pri_ex);
 		}
 
 		Collections.sort(rationsList);
 		this.rationsList = rationsList;
 		// only for test
 		// AdViewLayout.isadFill = true;
 		if (AdViewLayout.isadFill) {
 			Class<?> clazz = null;
 			try {
				clazz = Class.forName("com.kuaiyou.adfill.util.Utils");
 				if (null != clazz)
 					if (this.rationsList.isEmpty()) {
 						this.rationsList.add(0, afRation());
 						totalweight = 100.0;
 					} else
 						this.rationsList
 								.add(rationsList.size() - 1, afRation());
 			} catch (ClassNotFoundException e) {
 			} finally {
 				this.rollovers = this.rationsList.iterator();
 			}
 		}
 
 		this.rollovers = this.rationsList.iterator();
 		this.totalWeight = totalweight;
 		/*
 		 * Collections.sort(rationsList_pri);
 		 * this.rationsList_pri=rationsList_pri;
 		 * this.rollover_pri=rationsList_pri.iterator();
 		 */
 
 	}
 
 	public boolean isLocateForeign() {
 		Context context = contextReference.get();
 		if (context == null) {
 			return false;
 		}
 
 		TelephonyManager tm = (TelephonyManager) context
 				.getSystemService(Context.TELEPHONY_SERVICE);
 
 		String imei = tm.getDeviceId();
 		if ((imei == null) || (imei.equals("000000000000000"))) {
 			AdViewUtil.logInfo("There is no imei, or run in emulator");
 			return false;
 		} else {
 			AdViewUtil.logInfo("run in device, imei=" + imei);
 		}
 
 		String countryCodeDefault = Locale.getDefault().getCountry()
 				.toLowerCase();
 		String countryCodeNetwork = tm.getNetworkCountryIso().toLowerCase();
 		String locale = Locale.getDefault().toString();
 
 		if (AdViewTargeting.getRunMode() == RunMode.TEST) {
 			AdViewUtil.logInfo("run in device, imei=" + imei + "\n"
 					+ "countryCodeDefault=" + countryCodeDefault + "\n"
 					+ "countryCodeNetwork=" + countryCodeNetwork + "\n'"
 					+ "locale=" + locale);
 		}
 
 		if (countryCodeNetwork != null && countryCodeNetwork.length() > 0) {
 			if (countryCodeNetwork.compareTo("cn") == 0)
 				return false;
 			else
 				return true;
 		}
 
 		if (countryCodeDefault != null && countryCodeDefault.length() > 0) {
 			if (countryCodeDefault.compareTo("cn") == 0)
 				return false;
 			else
 				return true;
 		}
 
 		try {
 			String serviceName = "location";
 			LocationManager locationManager = (LocationManager) context
 					.getSystemService(serviceName);
 			Criteria criteria = new Criteria();
 			criteria.setAccuracy(1);
 			criteria.setAltitudeRequired(false);
 			criteria.setBearingRequired(false);
 			criteria.setCostAllowed(true);
 			criteria.setPowerRequirement(1);
 
 			String provider = locationManager.getBestProvider(criteria, true);
 			Location location = null;
 
 			if (provider != null && provider.length() > 0) {
 				AdViewUtil.logInfo("provider=" + provider + "\n" + provider
 						+ " enable ="
 						+ locationManager.isProviderEnabled(provider));
 				location = locationManager.getLastKnownLocation(provider);
 			}
 
 			if (location != null) {
 				AdViewUtil.logInfo("location != null");
 				double lat = location.getLatitude();
 				double lng = location.getLongitude();
 				String locationString = (lat + "," + lng);
 				AdViewUtil.logInfo("locationString=" + locationString);
 			} else
 				AdViewUtil.logInfo("location == null");
 		} catch (Exception e) {
 			AdViewUtil.logError("", e);
 		}
 
 		return false;
 	}
 
 	private int isSimulator() {
 		Context context = contextReference.get();
 		if (context == null) {
 			return 0;
 		}
 
 		TelephonyManager tm = (TelephonyManager) context
 				.getSystemService(Context.TELEPHONY_SERVICE);
 		int ret;
 
 		String imei = tm.getDeviceId();
 		if ((imei == null) || (imei.equals("000000000000000"))) {
 			ret = 1;
 		} else {
 			ret = 0;
 		}
 
 		AdViewUtil.logInfo("isSimulator, ret=" + ret);
 		return ret;
 	}
 
 	public static String getIDByMAC(Context context) {
 		String str = null;
 		try {
 			WifiManager localWifiManager = (WifiManager) context
 					.getSystemService("wifi");
 			WifiInfo localWifiInfo = localWifiManager.getConnectionInfo();
 			str = localWifiInfo.getMacAddress();
 		} catch (Exception localException) {
 			AdViewUtil
 					.logError(
 							"Could not read MAC, forget to include ACCESS_WIFI_STATE permission?",
 							localException);
 		}
 		return str;
 	}
 
 	public static String getDeviceID(Context context) {
 		TelephonyManager tm = (TelephonyManager) context
 				.getSystemService("phone");
 		StringBuffer tmDevice = new StringBuffer();
 		try {
 			String imei = tm.getDeviceId();
 			if (imei == null) {
 				tmDevice.append("000000000000000");
 			} else {
 				tmDevice.append(imei);
 			}
 
 			while (tmDevice.length() < 15) {
 				tmDevice.append("0");
 			}
 
 			tmDevice.append(":");
 			String macAdd = getIDByMAC(context);
 			if (!TextUtils.isEmpty(macAdd))
 				macAdd = macAdd.replace(":", "");
 			else {
 				macAdd = "000000000000";
 			}
 			tmDevice.append(macAdd);
 			tmDevice.append(":");
 			while (tmDevice.length() < 32)
 				tmDevice.append("0");
 		} catch (Exception localException) {
 			localException.printStackTrace();
 			AdViewUtil.logInfo("Failed to take mac as IMEI");
 		}
 		return tmDevice.toString();
 	}
 }
