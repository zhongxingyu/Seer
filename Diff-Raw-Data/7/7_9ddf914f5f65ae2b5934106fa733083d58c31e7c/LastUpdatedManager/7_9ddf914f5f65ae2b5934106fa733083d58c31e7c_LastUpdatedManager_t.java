 package com.ouchadam.fang.presentation.item;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 
 import com.ouchadam.fang.FangCalendar;
 import com.ouchadam.fang.NovodaCalendar;
 
 public class LastUpdatedManager {
 
     private static final String PREFERENECE_LAST_UPDATED = "LastUpdated";
     private static final String KEY_LAST_UPDATED = "lastUpdated";
     private static final String KEY_SEEN = "seen";
 
     private final SharedPreferences preferences;
 
     public static LastUpdatedManager from(Context context) {
         SharedPreferences preferences = context.getSharedPreferences(PREFERENECE_LAST_UPDATED, Context.MODE_PRIVATE);
         return new LastUpdatedManager(preferences);
     }
 
     private LastUpdatedManager(SharedPreferences preferences) {
         this.preferences = preferences;
     }
 
     public void setLastUpdated() {
         preferences.edit().putLong(KEY_LAST_UPDATED, System.currentTimeMillis()).apply();
         setSeen(false);
     }
 
     public void setSeen(boolean hasSeen) {
         preferences.edit().putBoolean(KEY_SEEN, hasSeen).apply();
     }
 
     public boolean getSeen() {
         return preferences.getBoolean(KEY_SEEN, false);
     }
 
     public long getLastUpdatedMs() {
         return preferences.getLong(KEY_LAST_UPDATED, 0l);
     }
 
     public boolean canShow() {
        if (hasValidLastUpdatedMs() && !getSeen()) {
             return true;
         }
         return false;
     }
 
    private boolean hasValidLastUpdatedMs() {
        return getHour(getLastUpdatedMs()) > 5 && getLastUpdatedMs() != 0L;
    }

     private long getHour(long time) {
         return new FangCalendar(time).getHoursDifference();
     }
 
 }
