 /***
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may
  * not use this file except in compliance with the License. You may obtain
  * a copy of the License at
  * http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  */
 package de.mangelow.slideitloud;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.preference.CheckBoxPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceCategory;
 import android.preference.PreferenceScreen;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.util.Log;
 import android.view.Window;
 
 
 public class Config extends PreferenceActivity {
 
 	private final static String TAG = "SIL";
	private final static boolean D = false;
 
 	private Context context;
 	private Helper mHelper = new Helper();
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		if(D)Log.d(TAG, "onCreate");
 
 		if(Build.VERSION.SDK_INT<Build.VERSION_CODES.HONEYCOMB)requestWindowFeature(Window.FEATURE_LEFT_ICON);
 
 		super.onCreate(savedInstanceState);
 		context = getApplicationContext();			
 
 	}
 	@SuppressWarnings("deprecation")
 	@Override
 	public void onResume() {
 		super.onResume();
 		if(D)Log.d(TAG, "onResume");
 
 		if(!mHelper.isHardwareKeyboardAvailable(context)) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(Config.this);
 			builder.setTitle(context.getResources().getString(R.string.no_hardwarekeyboard_titel));
 			builder.setMessage(context.getResources().getString(R.string.no_hardwarekeyboard_msg));
 			builder.setIcon(android.R.drawable.ic_dialog_alert);
 			builder.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {  
 				public void onClick(DialogInterface dialog, int which) {
 					finish();
 					return;  
 				} });  			
 			builder.create().show();
 		}
 		else {
 			setPreferenceScreen(createPreferences());
 		}
 		
 		if(Build.VERSION.SDK_INT<Build.VERSION_CODES.HONEYCOMB)getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,R.drawable.icon);
 	}
 	@SuppressWarnings("deprecation")
 	private PreferenceScreen createPreferences() {
 		if(D)Log.d(TAG, "createPreferences");
 
 		final boolean enabled = mHelper.loadBooleanPref(context, "enabled", mHelper.ENABLED);
 		final boolean autoanswer = mHelper.loadBooleanPref(context, "autoanswer", mHelper.AUTOANSWER);
 		final boolean autohangup = mHelper.loadBooleanPref(context, "autohangup", mHelper.AUTOHANGUP);
 
 		// Categories
 		PreferenceScreen root = getPreferenceManager().createPreferenceScreen(context);		
 		final PreferenceCategory pc_status = new PreferenceCategory(context);
 		pc_status.setTitle("Status");
 		final PreferenceCategory pc_extras = new PreferenceCategory(context);
 		pc_extras.setTitle("Extras");
 
 		// Preferences
 		CheckBoxPreference cbp_enableservice = new CheckBoxPreference(context);
 		final Preference p_service = new Preference(context);
 		final CheckBoxPreference cbp_autoanswer = new CheckBoxPreference(context);
 		final CheckBoxPreference cbp_autohangup = new CheckBoxPreference(context);
 
 		//
 
 		cbp_enableservice.setTitle(getString(R.string.enable));
 		cbp_enableservice.setChecked(enabled);
 		cbp_enableservice.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
 			public boolean onPreferenceChange(Preference p, Object o) {
 				boolean newvalue = Boolean.parseBoolean(o.toString());
 
 				Intent serviceIntent = new Intent(context,Service.class);
 
 				if(newvalue) {
 					if(D)Log.d(TAG, "Enable Service");					
 					startService(serviceIntent);
 				}
 				else {
 					if(D)Log.d(TAG, "Disable Service");					
 					stopService(serviceIntent);
 				}
 
 				mHelper.saveBooleanPref(context, "enabled", newvalue);				
 				p_service.setEnabled(newvalue);
 				p_service.setTitle(getString(R.string.service_running));
 				p_service.setSummary(getString(R.string.service_running_summary));
 				if(!mHelper.isMyServiceRunning(context)) {
 					p_service.setTitle(getString(R.string.service_not_running));
 					p_service.setSummary(getString(R.string.service_running_summary));
 				}
 				cbp_autoanswer.setEnabled(newvalue);
 				cbp_autohangup.setEnabled(newvalue);
 				return true;
 			}
 		});
 
 		p_service.setEnabled(enabled);		
 		p_service.setTitle(getString(R.string.service_running));
 		p_service.setSummary(getString(R.string.service_running_summary));
 		if(!mHelper.isMyServiceRunning(context)) {
 			p_service.setTitle(getString(R.string.service_not_running));
 			p_service.setSummary(getString(R.string.service_not_running_summary));
 		}
 		p_service.setOnPreferenceClickListener(new OnPreferenceClickListener() {
 			public boolean onPreferenceClick(Preference preference) {				
 				Intent serviceIntent = new Intent(context,Service.class);				
 				if(mHelper.isMyServiceRunning(context)) {
 					stopService(serviceIntent);
 					p_service.setTitle(getString(R.string.service_not_running));
 					if(enabled)p_service.setSummary(getString(R.string.service_not_running_summary));
 				}
 				else {
 					startService(serviceIntent);
 					p_service.setTitle(getString(R.string.service_running));
 					if(enabled)p_service.setSummary(getString(R.string.service_running_summary));
 				}
 				return false;
 			}
 		});		
 
 		//
 
 		cbp_autoanswer.setEnabled(enabled);
 		cbp_autoanswer.setTitle(getString(R.string.autoanswer));
 		cbp_autoanswer.setSummary(getString(R.string.autoanswer_summary));
 		cbp_autoanswer.setChecked(autoanswer);
 		cbp_autoanswer.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
 			public boolean onPreferenceChange(Preference p, Object o) {
 				boolean newvalue = Boolean.parseBoolean(o.toString());
 				mHelper.saveBooleanPref(context, "autoanswer", newvalue);				
 				return true;
 			}
 		});
 
 		cbp_autohangup.setEnabled(enabled);
 		cbp_autohangup.setTitle(getString(R.string.autohangup));
 		cbp_autohangup.setSummary(getString(R.string.autohangup_summary));
 		cbp_autohangup.setChecked(autohangup);
 		cbp_autohangup.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
 			public boolean onPreferenceChange(Preference p, Object o) {
 				boolean newvalue = Boolean.parseBoolean(o.toString());
 				mHelper.saveBooleanPref(context, "autohangup", newvalue);				
 				return true;
 			}
 		});
 
 		//
 
 		root.addPreference(cbp_enableservice);
 
 		root.addPreference(pc_status);
 		pc_status.addPreference(p_service);
 
 		root.addPreference(pc_extras);
 		pc_extras.addPreference(cbp_autoanswer);
 		pc_extras.addPreference(cbp_autohangup);
 
 		return root;
 	}
 
 }
