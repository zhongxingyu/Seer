 package com.coffeeandpower;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 
 import android.app.Application;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.telephony.TelephonyManager;
 import android.text.Html;
 import android.util.Log;
 
 import com.coffeeandpower.urbanairship.IntentReceiver;
 import com.coffeeandpower.utils.HttpUtil;
 import com.urbanairship.UAirship;
 import com.urbanairship.push.PushManager;
 import com.urbanairship.push.PushPreferences;
 
 public class AppCAP extends Application {
 
 	// Google maps api key for debug Kep:
 	// 0PV0Dp_6Dj6PkG_8xJqiTbSPxXwq2XEiEqXkO_Q
 	// Google maps api key for debug Tengai home:
 	// 0PV0Dp_6Dj6M_WBuUrThj9-fW3btGy9kxl83wgQ
 
 	public static final String TAG = "CoffeeAndPower";
 
 	private static final String TAG_USER_EMAIL = "tag_user_email";
 	private static final String TAG_USER_EMAIL_PASSWORD = "tag_user_email_password";
 	private static final String TAG_USER_LINKEDIN_TOKEN = "tag_user_linkedin_token";
 	private static final String TAG_USER_LINKEDIN_TOKEN_SECRET = "tag_user_linkedin_token_secret";
 	private static final String TAG_USER_LINKEDIN_ID = "tag_user_linkedin_id";
 	private static final String TAG_USER_PHOT_URL = "tag_user_photo_url";
 	private static final String TAG_USER_PHOT_LARGE_URL = "tag_user_photo_large_url";
 	private static final String TAG_LOGGED_IN_USER_ID = "tag_logged_in_user_id";
 	private static final String TAG_LOGGED_IN_USER_NICKNAME = "tag_logged_in_user_nickname";
 	private static final String TAG_USER_COORDINATES = "tag_user_coordinates";
 	private static final String TAG_IS_USER_CHECKED_IN = "tag_is_user_checked_in";
 	private static final String TAG_SHOULD_FINISH_ACTIVITY_MAP = "tag_sgould_finish_activity_map";
 	private static final String TAG_SHOULD_START_LOG_IN = "tag_sgould_start_log_in";
 	private static final String TAG_COOKIE_STRING = "tag_cookie_string";
 	private static final String TAG_METRIC_SYSTEM = "tag_metric_system";
 	private static final String TAG_PUSH_DISTANCE = "tag_push_distance";
 	private static final String TAG_START_LOGIN_PAGE_FROM_CONTACTS = "tag_start_login_page_from_contacts";
 	private static final String TAG_IS_LOGGED_IN = "tag_is_logged_in";
 	private static final String TAG_SCREEN_WIDTH = "tag_screen_width";
 	private static final String TAG_FIRST_START = "tag_first_start";
 	private static final String TAG_INFO_DIALOG = "tag_info_dialog";
 
 	// Notification settings
 	private static final String TAG_NOTIFICATION_FROM = "tag_notification_from";
 	private static final String TAG_NOTIFICATION_TOGGLE = "tag_notification_toggle";
 
 	// public static final String URL_WEB_SERVICE = "https://candp.me/"; //
 	// production
 	public static final String URL_WEB_SERVICE = "https://staging.candp.me/"; // staging
 	public static final String URL_FOURSQUARE = "https://api.foursquare.com/v2/venues/search?oauth_token=BCG410DXRKXSBRWUNM1PPQFSLEFQ5ND4HOUTTTWYUB1PXYC4&v=20120302";
 	public static final String FOURSQUARE_OAUTH = "BCG410DXRKXSBRWUNM1PPQFSLEFQ5ND4HOUTTTWYUB1PXYC4";
 	public static final String URL_FUNDS = "http://www.coffeeandpower.com/m/?ios#addFundsiPhone";
 	public static final String URL_LOGIN = "login.php";
 	public static final String URL_LOGOUT = "logout.php";
 	public static final String URL_SIGNUP = "signup.php";
 	public static final String URL_API = "api.php";
 
 	// Activity codes
 	public static final int ACT_CHECK_IN = 1888;
 	public static final int ACT_QUIT = 1333;
 
 	// Http return codes
 	public static final int HTTP_ERROR = 1403;
 	public static final int HTTP_REQUEST_SUCCEEDED = 1404;
 	public static final int ERROR_SUCCEEDED_SHOW_MESS = 1407;
 
 	private static AppCAP instance;
 
 	private HttpUtil http;
 
 	public AppCAP() {
 		instance = this;
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 
 		this.http = new HttpUtil();
 
 		UAirship.takeOff(this);
 
 		PushManager.enablePush();
 		PushManager.shared().setIntentReceiver(IntentReceiver.class);
 
 		PushPreferences prefs = PushManager.shared().getPreferences();
 		Log.d("LOG", "Found APID: " + prefs.getPushId());
 
 		// Get country code for metrics/imperial units
 		TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
 		Log.d("LOG", "Locale: " + tm.getSimCountryIso());
 		if (tm.getSimCountryIso() != null && !tm.getSimCountryIso().equals("")) {
 			if (tm.getSimCountryIso().contains("US") || tm.getSimCountryIso().contains("us") || tm.getSimCountryIso().contains("usa")
 					|| tm.getSimCountryIso().contains("um")) {
 				setMetricsSys(false);
 			} else {
 				setMetricsSys(true);
 			}
 		} else {
 			setMetricsSys(false);
 		}
 	}
 
 	public void onStop() {
 		UAirship.land();
 	}
 
 	public static HttpUtil getConnection() {
 		return instance.http;
 	}
 
 	private static SharedPreferences getSharedPreferences() {
 		return instance.getSharedPreferences(AppCAP.TAG, MODE_PRIVATE);
 	}
 
 	public static boolean isFirstStart() {
 		return getSharedPreferences().getBoolean(TAG_FIRST_START, true);
 	}
 
 	public static void setNotFirstStart() {
 		getSharedPreferences().edit().putBoolean(TAG_FIRST_START, false).commit();
 	}
 
 	public static boolean shouldShowInfoDialog() {
 		return getSharedPreferences().getBoolean(TAG_INFO_DIALOG, true);
 	}
 
 	public static void dontShowInfoDialog() {
 		getSharedPreferences().edit().putBoolean(TAG_INFO_DIALOG, false).commit();
 	}
 
 	public static boolean isMetrics() {
 		return getSharedPreferences().getBoolean(TAG_METRIC_SYSTEM, false);
 	}
 
 	private void setMetricsSys(boolean set) {
 		getSharedPreferences().edit().putBoolean(TAG_METRIC_SYSTEM, set).commit();
 	}
 
 	public static void setUserEmail(String email) {
 		getSharedPreferences().edit().putString(TAG_USER_EMAIL, email).commit();
 	}
 
 	public static String getUserEmail() {
 		return getSharedPreferences().getString(TAG_USER_EMAIL, "");
 	}
 
 	public static void setUserEmailPassword(String pass) {
 		getSharedPreferences().edit().putString(TAG_USER_EMAIL_PASSWORD, pass).commit();
 	}
 
 	public static String getUserEmailPassword() {
 		return getSharedPreferences().getString(TAG_USER_EMAIL_PASSWORD, "");
 	}
 
 	public static String cleanResponseString(String data) {
 		String retS = data;
 		data = Html.fromHtml(data).toString();
 
 		try {
 			retS = URLDecoder.decode(data, "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
		} catch (IllegalArgumentException e){
			e.printStackTrace();
 		}
 		return retS;
 	}
 
 	public static void setLocalUserPhotoURL(String url) {
 		getSharedPreferences().edit().putString(TAG_USER_PHOT_URL, url).commit();
 	}
 
 	public static String getLocalUserPhotoURL() {
 		return getSharedPreferences().getString(TAG_USER_PHOT_URL, "");
 	}
 
 	public static void setLocalUserPhotoLargeURL(String url) {
 		getSharedPreferences().edit().putString(TAG_USER_PHOT_LARGE_URL, url).commit();
 	}
 
 	public static String getLocalUserPhotoLargeURL() {
 		return getSharedPreferences().getString(TAG_USER_PHOT_LARGE_URL, "");
 	}
 
 	public static String setUserLinkedInToken() {
 		return getSharedPreferences().getString(TAG_USER_LINKEDIN_TOKEN, "");
 	}
 
 	public static String setUserLinkedInTokenSecret() {
 		return getSharedPreferences().getString(TAG_USER_LINKEDIN_TOKEN_SECRET, "");
 	}
 
 	public static String setUserLinkedInID() {
 		return getSharedPreferences().getString(TAG_USER_LINKEDIN_ID, "");
 	}
 
 	public static void setUserLinkedInDetails(String token, String tokenSecret, String id) {
 		getSharedPreferences().edit().putString(TAG_USER_LINKEDIN_ID, id).commit();
 		getSharedPreferences().edit().putString(TAG_USER_LINKEDIN_TOKEN, token).commit();
 		getSharedPreferences().edit().putString(TAG_USER_LINKEDIN_TOKEN_SECRET, tokenSecret).commit();
 	}
 
 	public static String getUserLinkedInID() {
 		return getSharedPreferences().getString(TAG_USER_LINKEDIN_ID, "");
 	}
 
 	public static String getUserLinkedInToken() {
 		return getSharedPreferences().getString(TAG_USER_LINKEDIN_TOKEN, "");
 	}
 
 	public static String getUserLinkedInTokenSecret() {
 		return getSharedPreferences().getString(TAG_USER_LINKEDIN_TOKEN_SECRET, "");
 	}
 
 	public static void setLoggedInUserId(int userId) {
 		getSharedPreferences().edit().putInt(TAG_LOGGED_IN_USER_ID, userId).commit();
 	}
 
 	public static int getLoggedInUserId() {
 		return getSharedPreferences().getInt(TAG_LOGGED_IN_USER_ID, 0);
 	}
 
 	public static void setLoggedInUserNickname(String nickname) {
 		getSharedPreferences().edit().putString(TAG_LOGGED_IN_USER_NICKNAME, nickname).commit();
 	}
 
 	public static String getLoggedInUserNickname() {
 		return getSharedPreferences().getString(TAG_LOGGED_IN_USER_NICKNAME, "");
 	}
 
 	public static void setUserCoordinates(double[] data) {
 		getSharedPreferences().edit().putFloat(TAG_USER_COORDINATES + "sw_lat", (float) data[0]).commit();
 		getSharedPreferences().edit().putFloat(TAG_USER_COORDINATES + "sw_lng", (float) data[1]).commit();
 		getSharedPreferences().edit().putFloat(TAG_USER_COORDINATES + "ne_lat", (float) data[2]).commit();
 		getSharedPreferences().edit().putFloat(TAG_USER_COORDINATES + "ne_lng", (float) data[3]).commit();
 		getSharedPreferences().edit().putFloat(TAG_USER_COORDINATES + "user_lat", (float) data[4]).commit();
 		getSharedPreferences().edit().putFloat(TAG_USER_COORDINATES + "user_lng", (float) data[5]).commit();
 	}
 
 	/**
 	 * data[0] = sw_lat; data[1] = sw_lng; data[2] = ne_lat; data[3] =
 	 * ne_lng; data[4] = user_lat; data[5] = user_lng;
 	 */
 	public static double[] getUserCoordinates() {
 		double[] data = new double[6];
 		data[0] = (double) getSharedPreferences().getFloat(TAG_USER_COORDINATES + "sw_lat", 0);
 		data[1] = (double) getSharedPreferences().getFloat(TAG_USER_COORDINATES + "sw_lng", 0);
 		data[2] = (double) getSharedPreferences().getFloat(TAG_USER_COORDINATES + "ne_lat", 0);
 		data[3] = (double) getSharedPreferences().getFloat(TAG_USER_COORDINATES + "ne_lng", 0);
 		data[4] = (double) getSharedPreferences().getFloat(TAG_USER_COORDINATES + "user_lat", 0);
 		data[5] = (double) getSharedPreferences().getFloat(TAG_USER_COORDINATES + "user_lng", 0);
 		return data;
 	}
 
 	public static boolean isUserCheckedIn() {
 		return getSharedPreferences().getBoolean(TAG_IS_USER_CHECKED_IN, false);
 	}
 
 	public static void setUserCheckedIn(boolean set) {
 		getSharedPreferences().edit().putBoolean(TAG_IS_USER_CHECKED_IN, set).commit();
 	}
 
 	public static boolean shouldFinishActivities() {
 		return getSharedPreferences().getBoolean(TAG_SHOULD_FINISH_ACTIVITY_MAP, false);
 	}
 
 	public static void setShouldFinishActivities(boolean set) {
 		getSharedPreferences().edit().putBoolean(TAG_SHOULD_FINISH_ACTIVITY_MAP, set).commit();
 	}
 
 	public static boolean shouldStartLogIn() {
 		return getSharedPreferences().getBoolean(TAG_SHOULD_START_LOG_IN, false);
 	}
 
 	public static void setShouldStartLogIn(boolean set) {
 		getSharedPreferences().edit().putBoolean(TAG_SHOULD_START_LOG_IN, set).commit();
 	}
 
 	public static boolean isStartingLoginPageFromContacts() {
 		return getSharedPreferences().getBoolean(TAG_START_LOGIN_PAGE_FROM_CONTACTS, false);
 	}
 
 	public static void setStartLoginPageFromContacts(boolean set) {
 		getSharedPreferences().edit().putBoolean(TAG_START_LOGIN_PAGE_FROM_CONTACTS, set).commit();
 	}
 
 	public static String getCookieString() {
 		return getSharedPreferences().getString(TAG_COOKIE_STRING, "");
 	}
 
 	public static void setCookieString(String cookie) {
 		getSharedPreferences().edit().putString(TAG_COOKIE_STRING, cookie).commit();
 	}
 
 	public static void setLoggedIn(boolean set) {
 		getSharedPreferences().edit().putBoolean(TAG_IS_LOGGED_IN, set).commit();
 	}
 
 	public static boolean isLoggedIn() {
 		return getSharedPreferences().getBoolean(TAG_IS_LOGGED_IN, false);
 	}
 
 	public static void setPushDistance(String dist) {
 		getSharedPreferences().edit().putString(TAG_PUSH_DISTANCE, dist).commit();
 	}
 
 	public static String getPushDistance() {
 		return getSharedPreferences().getString(TAG_PUSH_DISTANCE, "city");
 	}
 
 	public static void setNotificationFrom(String from) {
 		getSharedPreferences().edit().putString(TAG_NOTIFICATION_FROM, from).commit();
 	}
 
 	public static void setNotificationToggle(boolean res) {
 		getSharedPreferences().edit().putBoolean(TAG_NOTIFICATION_TOGGLE, res).commit();
 	}
 
 	public static String getNotificationFrom() {
 		return getSharedPreferences().getString(TAG_NOTIFICATION_FROM, "in city");
 	}
 
 	public static boolean getNotificationToggle() {
 		return getSharedPreferences().getBoolean(TAG_NOTIFICATION_TOGGLE, false);
 	}
 
 	public static int getScreenWidth() {
 		return getSharedPreferences().getInt(TAG_SCREEN_WIDTH, 480);
 	}
 
 	public static void saveScreenWidth(int screenWidth) {
 		getSharedPreferences().edit().putInt(TAG_SCREEN_WIDTH, screenWidth).commit();
 	}
 
 	public static void logInFile(String data) {
 		try {
 			FileOutputStream fOut = instance.openFileOutput("big_log.txt", MODE_WORLD_READABLE);
 			OutputStreamWriter osw = new OutputStreamWriter(fOut);
 			osw.write(data);
 			osw.flush();
 			osw.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
