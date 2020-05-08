 package org.geometerplus.fbreader.widget;
 
 import java.util.Set;
 
 import android.annotation.TargetApi;

 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Build;
 
 public class SharedPreferencesManager {
 	public static SharedPreferences getSharedPreferences(Context context, int appWidgetId) {
 		final String sharedPreferensesName = PreferenceKey.SHARED_PREFERENCES + appWidgetId;
 		return context.getSharedPreferences(sharedPreferensesName, Context.MODE_PRIVATE);
 	}
 	
 	public static int getType(Context context, int appWidgetId) {
 		return getSharedPreferences(context, appWidgetId).getInt(PreferenceKey.TYPE, -1);
 	}
 	
 	public static String getString(Context context, String key, int appWidgetId) {
 		return getSharedPreferences(context, appWidgetId).getString(key, null);
 	}
 
 	public static boolean getBoolean(Context context, String key, int appWidgetId) {
 		return getSharedPreferences(context, appWidgetId).getBoolean(key, false);
 	}
 
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	public static Set<String> getBooksSet(Context context, int appWidgetId) {
 		return getSharedPreferences(context, appWidgetId).getStringSet(PreferenceKey.BOOKS, null);
 	}
 
 	public static void deleteSharedPreferences(Context context, int appWidgetId) {
 		getSharedPreferences(context, appWidgetId).edit().clear().commit();
 	}
 }
