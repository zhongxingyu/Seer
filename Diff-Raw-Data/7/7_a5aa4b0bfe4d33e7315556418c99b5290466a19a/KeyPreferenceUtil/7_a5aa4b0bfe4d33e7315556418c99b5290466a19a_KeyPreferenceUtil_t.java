 package mobi.monaca.framework.plugin.innovationplus;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 
 public class KeyPreferenceUtil {
 	private static final String PREF = "ipp_auth_pref";
 	private static final String KEY_AUTH ="ipp_auth_key";
 
	private static String currentApplicationId = null;
 
 	private KeyPreferenceUtil() {};
 
 	/**
 	 * get last saved auth key.
 	 * @param context
 	 * @return if no auth key saved, return empty String
 	 */
 	public static String getAuthKey(Context context) {
 		SharedPreferences pref = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
 		return pref.getString(KEY_AUTH, "");
 	}
 
 	/**
 	 * save auth key to preference. old key is overridden
 	 * @param context
 	 * @param authKey
 	 */
 	public static void saveAuthKey(Context context, String authKey) {
 		SharedPreferences pref = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
 		pref.edit().putString(KEY_AUTH, authKey).commit();
 	}
 
 	/**
 	 * clear saved auth key.
 	 * @param context
 	 */
 	public static void removeAuthKey(Context context) {
 		SharedPreferences pref = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
 		pref.edit().clear().commit();
 	}
 
 	/**
 	 * used by debugger
 	 * @param id
 	 */
 	public static void setApplicationId(String id) {
 		currentApplicationId = id;
 	}
 
 	public static String getApplicationId(Context context) {
 		try {
 			ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			String idInManifest = (appInfo.metaData != null ? appInfo.metaData.getString("application_id") : null);
			return currentApplicationId == null ? idInManifest : currentApplicationId;
 		} catch (NameNotFoundException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 }
