 package cc.rainwave.android;
 
 import java.io.IOException;
 
 import cc.rainwave.android.api.Session;
 import cc.rainwave.android.api.types.RainwaveException;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.res.Resources;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.widget.Toast;
 
 public class Rainwave {
 	public static boolean putIntPreference(Context ctx, String name, int value) {
 		SharedPreferences prefs = getPreferences(ctx);
 		Editor editor = prefs.edit();
 		editor.putInt(name, value);
 		return editor.commit();
 	}
 	
 	public static boolean putStringPreference(Context ctx, String name, String value) {
 		SharedPreferences prefs = getPreferences(ctx);
 		Editor editor = prefs.edit();
 		editor.putString(name, value);
 		return editor.commit();
 	}
 	
 	public static boolean putBoolPreference(Context ctx, String name, boolean value) {
 		SharedPreferences prefs = getPreferences(ctx);
 		Editor editor = prefs.edit();
 		editor.putBoolean(name, value);
 		return editor.commit();
 	}
 	
 	public static boolean getAutoShowElectionFlag(Context ctx) {
 		return getBoolPref(ctx, PREF_AUTOSHOW_ELECTION, true);
 	}
 	
     public static String getUrl(Context ctx) {
     	return getStringPref(ctx,PREFS_URL,API_URL);
     }
     
     public static String getUserId(Context ctx) {
     	return getStringPref(ctx,PREFS_USERID,null);
     }
     
     public static boolean putUserId(Context ctx, String value) {
     	return putStringPreference(ctx, PREFS_USERID, value);
     }
     
     public static String getKey(Context ctx) {
         return getStringPref(ctx,PREFS_KEY,null);
     }
     
     public static boolean putKey(Context ctx, String value) {
     	return putStringPreference(ctx, PREFS_KEY, value);
     }
     
     public static int getLastStation(Context ctx, int defValue) {
     	return getIntPref(ctx, PREFS_LASTSTATION, defValue);
     }
     
     public static boolean putLastStation(Context ctx, int value) {
     	return putIntPreference(ctx, PREFS_LASTSTATION, value);
     }
     
     public static boolean getBoolPref(Context ctx, String key, boolean defValue) {
     	return getPreferences(ctx).getBoolean(key, defValue);
     }
     
     public static String getStringPref(Context ctx, String key, String defValue) {
     	return getPreferences(ctx).getString(key, defValue);
     }
     
     public static int getIntPref(Context ctx, String key, int defValue) {
     	return getPreferences(ctx).getInt(key, defValue);
     }
     
     public static SharedPreferences getPreferences(Context ctx) {
         return PreferenceManager.getDefaultSharedPreferences(ctx);
     }
     
     public static void showError(Context ctx, RainwaveException e) {
     	showError(ctx, e.getCode(), e.getMessage());
     }
     
     public static void showError(Context ctx, IOException e) {
     	showError(ctx, 1, e.getMessage());
     }
     
     public static void showError(Context ctx, int resId) {
     	Resources r = ctx.getResources();
     	showError(ctx, 1, r.getString(resId));
     }
     
     public static void showError(Context ctx, int code, String msg) {
     	Message m = ERROR_QUEUE.obtainMessage(code, ctx);
     	Bundle data = m.getData();
     	data.putString("text", msg);
     	m.sendToTarget();
     }
     
     public static void forceCompatibility(Context ctx) {
     	SharedPreferences prefs = getPreferences(ctx);
     	forceType(prefs, PREFS_URL, PrefType.STRING);
     	forceType(prefs, PREFS_USERID, PrefType.STRING);
     	forceType(prefs, PREFS_KEY, PrefType.STRING);
     	forceType(prefs, PREFS_LASTSTATION, PrefType.INT);
     }
     
     public static boolean verifyUserInfo(String userInfo) {
     	boolean seenColon = false;
     	boolean validUserId = false;
     	boolean validKey = false;
     	for(int i = 0; i < userInfo.length(); i++) {
     		char c = Character.toUpperCase(userInfo.charAt(i));
     		
     		if(!seenColon && c == ':') {
     			seenColon = true;
     		}
     		
     		// If we haven't seen the colon yet, make return false if it isn't a digit, OR
     		// if we have seen the colon, make sure it's not a digit AND it's not a hexadecimal digit.
     		else if((!seenColon && !Character.isDigit(c)) ||
     		  (seenColon && (!Character.isDigit(c) && (c < 'A' || c > 'F')))) {
     			return false;
     		}
 
     		// We saw a valid user id or key, based on whether or not
     		// we've seen the colon yet.
     		else if(seenColon) {
     			validKey = true;
     		}
     		else {
     			validUserId = true;
     		}
     	}
     	return validUserId && validKey && seenColon;
     }
     
     public static boolean setPreferencesFromUri(Context ctx, Uri uri) {
     	// Probably it is good practice to check the scheme
     	// before we read data from the Uri.
     	String scheme = uri.getScheme();
     	if(!scheme.equals(Rainwave.SCHEME)) {
     		return false;
     	}
     	
     	boolean result = true;
     	
     	String userInfo = uri.getUserInfo();
     	result &= Rainwave.setPreferencesFromUserInfo(ctx, userInfo);
     	
     	// TODO: Handle the hostname.
     	
     	String path = uri.getPath();
     	result &= Rainwave.setPreferencesFromPath(ctx, path);
     	
     	return result;
     }
     
     private static boolean setPreferencesFromUserInfo(Context ctx, String userInfo) {
     	if(userInfo == null) return true;
     	if(!Rainwave.verifyUserInfo(userInfo)) return false;
     	
     	String userId = Rainwave.extractUserId(userInfo);
 		String key = Rainwave.extractKey(userInfo);
 		
 		boolean result = true;
 		result &= Rainwave.putUserId(ctx, userId);
 		result &= Rainwave.putKey(ctx, key);
 		return result;
     }
     
     private static boolean setPreferencesFromPath(Context ctx, String path) {
     	if(path == null || path.length() < 2) return true;
     	
     	// Chop of leading '/'
     	path = path.substring(1);
     	
     	// Chop off trailing '/'
     	if(path.charAt(path.length()-1)  == '/') {
     		path = path.substring(0, path.length() - 1);
     	}
     	
    	// Stop here if no path provided.
    	if(path.length() == 0) {
    		return true;
    	}
    	
     	// Only numbers allowed!
     	if(path.matches("[0-9]+") == false) {
     		return false;
     	}
     	
     	int sid = Integer.parseInt(path);
     	Rainwave.putLastStation(ctx, sid);
     	return true;
     }
     
     public static String extractUserId(String userInfo) {
     	String userId = userInfo.split(":")[0];
     	return userId.substring(0, Math.min(userId.length(), USERID_MAX));
     }
     
     public static String extractKey(String userInfo) {
     	String key = userInfo.split(":")[1];
     	return key.substring(0, Math.min(key.length(), KEY_MAX));
     }
     
     private static boolean forceType(SharedPreferences prefs, String key, PrefType type) {
     	if(!prefs.contains(key))
     		return false;
     	
     	try {
 	    	switch(type) {
 	    	case STRING: prefs.getString(key, null); break;
 	    	case LONG: prefs.getLong(key, 0l); break;
 	    	case INT: prefs.getInt(key, 0); break;
 	    	case FLOAT: prefs.getFloat(key, 0f); break;
 	    	case BOOL: prefs.getBoolean(key, false); break;
 	    	}
     	}
     	catch (ClassCastException e) {
     		// Delete it.
     		Editor edit = prefs.edit();
     		edit.remove(key);
     		edit.commit();
     		return true;
     	}
     	return false;
     }
     
     private static final Handler ERROR_QUEUE = new Handler() {
     	public void handleMessage(Message msg) {
     		Bundle data = msg.getData();
     		Context ctx = (Context) msg.obj;
     		String text = data.getString("text");
     		Toast.makeText(ctx, text, Toast.LENGTH_LONG).show();
     	}
     };
     
     private enum PrefType { STRING, BOOL, INT, LONG, FLOAT };
     
     public static final int
     	USERID_MAX = 10,
     	KEY_MAX = 10;
     
     public static final String
     	SCHEME = "rw",
     	API_URL = "http://rainwave.cc",
         PREFS_URL = "pref_url",
         PREFS_USERID = "pref_userId",
         PREFS_LASTSTATION = "pref_lastStation",
         PREF_IMPORT = "import_qr",
         PREF_AUTOSHOW_ELECTION = "pref_autoshow_elections",
         PREFS_KEY = "pref_key";
 }
