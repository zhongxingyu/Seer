 package lorian.graph.android;
 
 import lorian.graph.WindowSettings;
 import lorian.graph.android.opengl.GraphRenderer;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.widget.TextView;
 import android.annotation.SuppressLint;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.content.DialogInterface.OnCancelListener;
 import android.os.Build;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 
 import com.michaelnovakjr.numberpicker.*;
 
 public class WindowSettingsActivity extends PreferenceActivity implements
 OnTouchListener, OnCancelListener, NumberPickerDialog.OnNumberSetListener,
 SharedPreferences.OnSharedPreferenceChangeListener
 {
 
 	private NumberPickerDialog dialog;
 	int changingViewId = -1;
 	boolean numberDialogOpen = false;
 	
 	public static WindowSettings windowsettings;
 	
 	@SuppressWarnings("deprecation")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
 		setupActionBar();
 		addPreferencesFromResource(R.xml.preferences);
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		
 		prefs.registerOnSharedPreferenceChangeListener(this);
 		
 		this.onSharedPreferenceChanged(prefs, "window_settings_auto_calc_ymin_ymax"); 
 		/*
 		SharedPreferences.Editor edit = prefs.edit();
 		edit.putInt("window_settings_xmin", (int) windowsettings.getXmin());
 		edit.putInt("window_settings_xmax", (int) windowsettings.getXmax());
 		edit.putInt("window_settings_ymin", (int) windowsettings.getYmin());
 		edit.putInt("window_settings_ymax", (int) windowsettings.getYmax());
 		edit.putBoolean("window_settings_grid", windowsettings.gridOn());
 		edit.apply();
 		*/
 	}
 	/*
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_windowsettings);
 		// Show the Up button in the action bar.
 		setupActionBar();
 		
 		
 		this.findViewById(R.id.windowsettings_xmin_value).setOnTouchListener(this);
 		this.findViewById(R.id.windowsettings_xmax_value).setOnTouchListener(this);
 		this.findViewById(R.id.windowsettings_ymin_value).setOnTouchListener(this);
 		this.findViewById(R.id.windowsettings_ymax_value).setOnTouchListener(this);
 		
 	}
 	*/
 	/**
 	 * Set up the {@link android.app.ActionBar}, if the API is available.
 	 */
 	@SuppressLint("NewApi")
 	private void setupActionBar() {
 		if (Build.VERSION.SDK_INT >= 11) {
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		return false;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId())
 		{
 			case android.R.id.home:
 				this.finish();
 				return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public void onNumberSet(int selectedNumber) {
 		((TextView) this.findViewById(changingViewId)).setText(String.valueOf(selectedNumber));
 		numberDialogOpen = false;
 	}
 
 	@Override
 	public void onCancel(DialogInterface dialog) {
 		numberDialogOpen = false;
 		
 	}
 	
 
 	@Override
 	public boolean onTouch(View v, MotionEvent event) {
 		if(numberDialogOpen) return false;
 		changingViewId = v.getId();
 		numberDialogOpen = true;
 		int initialValue = Integer.parseInt(((TextView) this.findViewById(changingViewId)).getText().toString());
 		dialog = new NumberPickerDialog(WindowSettingsActivity.this, -1, 0);
         dialog.setOnNumberSetListener(this);
         dialog.setOnCancelListener(this);
         dialog.getNumberPicker().setRange(Integer.MIN_VALUE, Integer.MAX_VALUE); 
         dialog.getNumberPicker().setCurrent(initialValue);
         dialog.show();
         return true;
 	}
 	@SuppressWarnings("deprecation")
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
 			String key) {
 		
 		Log.d(GraphActivity.TAG, "Updating " + key);
 		
		
 		Preference pref = this.getPreferenceScreen().findPreference(key);
 		if(pref instanceof NumberPickerPreference)
 		{
 			if(key.endsWith("min"))
 				((NumberPickerPreference) pref).notifyValueChanged(sharedPreferences.getInt(key, -8));
 			else
 				((NumberPickerPreference) pref).notifyValueChanged(sharedPreferences.getInt(key, 8));
 		}
 		if(key.endsWith("auto_calc_ymin_ymax"))
 		{
 			boolean value = sharedPreferences.getBoolean(key, true);
 			GraphRenderer.auto_calc_ymin_ymax = value;
 			this.getPreferenceScreen().findPreference("window_settings_ymin").setEnabled(!value);
 			this.getPreferenceScreen().findPreference("window_settings_ymax").setEnabled(!value);
 		}
 
 		else if(key.endsWith("xmin"))
 		{
 			int value = sharedPreferences.getInt(key, -8);
 			windowsettings.setXmin(value);
 		}
 		else if(key.endsWith("xmax"))
 		{
 			int value = sharedPreferences.getInt(key, 8);
 			windowsettings.setXmax(value);
 		}
 		else if(key.endsWith("ymin"))
 		{
 			int value = sharedPreferences.getInt(key, -8);
 			windowsettings.setYmin(value);
 		}
 		else if(key.endsWith("ymax"))
 		{
 			int value = sharedPreferences.getInt(key, 8);
 			windowsettings.setYmax(value);
 		}
 		else if(key.endsWith("grid"))
 		{
 			boolean value = sharedPreferences.getBoolean(key, false);
 			windowsettings.setGrid(value);
 			
 		}
 		GraphRenderer.notifyWindowSettingsChanged();
 		
 	}
 
 	
 
 
 }
