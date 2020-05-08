 package ch.unibe.ese.shopnote.activities;
 
 import java.util.Locale;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.content.res.Configuration;
 import android.graphics.Color;
 import android.graphics.drawable.ColorDrawable;
 import android.os.Bundle;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceFragment;
 import android.preference.PreferenceManager;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import ch.unibe.ese.shopnote.R;
 
 /**
  *	Activity to change the settings of the app
  */
 public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {	
 	
 	@Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 		// Show the Up button in the action bar.
 		setupActionBar();
 		
 		// set Listener
 		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
 		sharedPrefs.registerOnSharedPreferenceChangeListener(this);
 		
         // Display the fragment as the main content.
         getFragmentManager().beginTransaction()
                 .replace(android.R.id.content, new SettingsFragment())
                 .commit();
        
         //Set color theme for settings
         updateColorSettings();
        
         setTitle(this.getString(R.string.title_activity_settings));
     }
 	
 	/**
 	 * Set up the {@link android.app.ActionBar}.
 	 */
 	private void setupActionBar() {
 
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.options, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			Intent intent = new Intent(this, HomeActivity.class);
 	        startActivity(intent);
 			return true;
 		}
 		
 		return super.onOptionsItemSelected(item);
 	}
 
 	/**
 	 * If user changes a option, this function is called to initialize all need steps which are required by the options
 	 */
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
 		if (key.equals("language")) {
 			updateLanguage(sharedPreferences);
 		}
 		else if(key.equals("color_setting")) {
 			
 		}   
 		
 		// refresh theme
         finish();
         startActivity(getIntent());
 	}
 
 
 	private void updateLanguage(SharedPreferences sharedPreferences) {
 		String newLanguage = sharedPreferences.getString("language", null);
 		String languages[] = getResources().getStringArray(R.array.choosable_languages_keys);
 		Configuration config = new Configuration();
 		
 		Locale locale = config.locale;	
 		if(newLanguage.equals(languages[0])) 
 			locale = Locale.ENGLISH; 
 		else if(newLanguage.equals(languages[1])) 
 			locale = Locale.GERMANY;
 		else if(newLanguage.equals(languages[2])) 
 			locale = Locale.FRANCE;
 		
 		Locale.setDefault(locale);
 		config.locale = locale;
 		getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
 	}
 	
 	private void updateColorSettings() {
 		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
 		String colorString = sharedPref.getString("color_setting", "white");
 		String colorChoosable[] = getResources().getStringArray(R.array.default_color_choice_names);
 		String colors[] = getResources().getStringArray(R.array.default_color_choice_white);
 
 		if(colorString.equals(colorChoosable[1]))
 			colors = getResources().getStringArray(R.array.default_color_choice_dark);
 		else if(colorString.equals(colorChoosable[2]))
 			colors = getResources().getStringArray(R.array.default_color_choice_chocolate);
 		else if(colorString.equals(colorChoosable[3]))
 			colors = getResources().getStringArray(R.array.default_color_choice_barbie);
 		if(colors == null) throw new IllegalStateException();
 		
 		int backgroundColor = Color.parseColor(colors[0]);
 		int titleBarColor = Color.parseColor(colors[2]);
 		int textColor = Color.parseColor(colors[3]);
 		int listViewDividerColor = Color.parseColor(colors[6]);
 		
 		getListView().setBackgroundColor(backgroundColor);
 		getListView().setDivider(new ColorDrawable(listViewDividerColor));
 		
 		getActionBar().setBackgroundDrawable(new ColorDrawable(titleBarColor));
 	}
 	
 	
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 	    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
 	        Intent intent = new Intent(this, HomeActivity.class);
 	        startActivity(intent);
 	    }
 	    return super.onKeyDown(keyCode, event);
 	}

	
	public void onResume() {
		super.onResume();
	}
 	
 }
 
 class SettingsFragment extends PreferenceFragment {
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         // Load the preferences from an XML resource
         addPreferencesFromResource(R.xml.preferences);
         
     }
 }
