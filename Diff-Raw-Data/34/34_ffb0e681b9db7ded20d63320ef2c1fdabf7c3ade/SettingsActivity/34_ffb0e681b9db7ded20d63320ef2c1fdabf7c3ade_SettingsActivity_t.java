 /* The following code was written by Menny Even Danan
  * and is released under the APACHE 2.0 license
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  */
 package com.menny.android.thumbremote.ui;
 
import java.net.InetAddress;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import com.menny.android.thumbremote.R;
 import com.menny.android.thumbremote.RemoteApplication;
 import com.menny.android.thumbremote.ServerAddress;
 import com.menny.android.thumbremote.boxee.BoxeeDiscovererThread;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceScreen;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 import android.widget.Toast;
 
 // See:
 // http://android.git.kernel.org/?p=platform/packages/apps/Settings.git;a=blob;f=src/com/android/settings/wifi/WifiSettings.java;h=cac77e3251fde365f3e32463f4c44712fd0b1944;hb=HEAD
 
 /**
  * Handles preference storage for BoxeeRemote.
  */
 public class SettingsActivity extends PreferenceActivity implements
 		BoxeeDiscovererThread.Receiver,
 		OnPreferenceClickListener, 
 		OnSharedPreferenceChangeListener {
 	
 	private final Preference.OnPreferenceChangeListener numberCheckListener = new Preference.OnPreferenceChangeListener() {
 
 	    @Override
 	    public boolean onPreferenceChange(Preference preference, Object newValue) {
 	        //Check that the string is an integer.
 	        return numberCheck(newValue);
 	    }
 	    
 		private boolean numberCheck(Object newValue) {
 		    if( !newValue.toString().equals("")  &&  newValue.toString().matches("\\d*") ) {
 		        return true;
 		    }
 		    else {
 		        Toast.makeText(SettingsActivity.this.getApplicationContext(), getResources().getString(R.string.is_an_invalid_number, newValue), Toast.LENGTH_SHORT).show();
 		        return false;
 		    }
 		}
 	};
 	/**
 	 * private constants
 	 */
 	private static final int DIALOG_CUSTOM = 1;
 
 	private HashMap<String, ServerAddress> mServers;
 	private PreferenceScreen mServersScreen;
 
 	public SettingsActivity() {
 		mServers = new HashMap<String, ServerAddress>();
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		addPreferencesFromResource(R.layout.preferences);
 
 		mServersScreen = (PreferenceScreen) getPreferenceScreen().findPreference(getText(R.string.settings_key_servers_screen));
 		
		mServersScreen.setSummary(RemoteApplication.getConfig().getServerName());
 		
 		Preference preference = new Preference(this);
 		preference.setTitle(getText(R.string.custom_server));
 		preference.setOrder(1000);
 		preference.setOnPreferenceClickListener(this);
 		mServersScreen.addPreference(preference);
 
 		getPreferenceScreen().findPreference(getText(R.string.settings_key_network_timeout_key)).setOnPreferenceChangeListener(numberCheckListener);
 		
 		BoxeeDiscovererThread discoverer = new BoxeeDiscovererThread(this, this);
 		discoverer.start();
 	}
 
 	@Override
 	protected void onPause() {
		RemoteApplication.getConfig().unlisten(this);
 		super.onPause();
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
		RemoteApplication.getConfig().listen(this);
 	}
 	
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		if (id != DIALOG_CUSTOM)
 			return null;
 
 		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
 		final View layout = inflater.inflate(R.layout.custom_host,
 				(ViewGroup) findViewById(R.id.layoutCustomHost));
		InetAddress hostname = RemoteApplication.getConfig().getHost();
		if (hostname != null)
			((TextView) layout.findViewById(R.id.textAddress)).setText(hostname.toString());
		((TextView) layout.findViewById(R.id.textPort)).setText(new Integer(RemoteApplication.getConfig().getPort()).toString());
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setView(layout);
 		builder.setPositiveButton(android.R.string.ok,
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						String address = ((TextView) layout
 								.findViewById(R.id.textAddress)).getText()
 								.toString();
 
 						int port = Integer.parseInt(((TextView) layout
 								.findViewById(R.id.textPort)).getText()
 								.toString());
 
						RemoteApplication.getConfig().putServer(address, port, "custom", false, true);
 						mServersScreen.getDialog().dismiss();
 					}
 				});
 
 		builder.setNegativeButton(android.R.string.cancel, null);
 		builder.setTitle(R.string.custom_server);
 		builder.setIcon(R.drawable.app_icon);
 
 		return builder.create();
 	}
 
 	@Override
 	public void addAnnouncedServers(ArrayList<ServerAddress> servers) {
 		for (ServerAddress server : servers) {
 			Preference preference = new Preference(this);
 			preference.setOrder(mServers.size());
 			preference.setTitle(server.name());
 			preference.setOnPreferenceClickListener(this);
 			mServersScreen.addPreference(preference);
 			mServers.put(server.name(), server);
 		}
 	}
 
 	@Override
 	public boolean onPreferenceClick(Preference preference) {
 		ServerAddress server = mServers.get(preference.getTitle());
 
 		if (server == null) {
 			showDialog(DIALOG_CUSTOM);
 			return true;
 		}
 
		RemoteApplication.getConfig().putServer(server, false);
 		mServersScreen.getDialog().dismiss();
 
 		return true;
 	}
 
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
 			String key) {
 		if (key.equals(RemoteApplication.getConfig().SERVER_NAME_KEY)) {
			String value = RemoteApplication.getConfig().getServerName();
			Toast.makeText(this.getApplicationContext(), "New server "+value, Toast.LENGTH_SHORT);
 			mServersScreen.setSummary(value);
 		}
 	}
 
 }
