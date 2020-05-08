 package dk.ilios.influencecounter;
 
 import android.app.Application;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 
 public class InfluenceCounterApplication extends Application {
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);	// Initialize if not done already
     	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
     	int influenceChangeGroupingTimer = (int) Float.parseFloat(prefs.getString("history_grouping_timer", "2")) * 1000;
 		
 		GameTracker.initialize(getApplicationContext(), influenceChangeGroupingTimer);
 	}
 }
