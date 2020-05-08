 package jp.tkgktyk.wimaxhelperforaterm.my;
 
 import jp.tkgktyk.wimaxhelperforaterm.AtermHelper;
 import jp.tkgktyk.wimaxhelperforaterm.R;
 import android.app.Application;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 
 /**
  * An application class provides AtermHelper object to activities and services.
  */
 public class MyApplication extends Application {
 	private AtermHelper _aterm;
 	
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		
 		MyFunc.setContext(this.getApplicationContext());
 		_checkVersion();
 	}
 	
 	private void _checkVersion() {
 		class Version {
 			int major = 0;
 			int minor = 0;
 			int revision = 0;
 			
 			public Version(String version) {
 				if (version != null && version.length() != 0) {
					String[] v = version.split(".");
 					int n = v.length;
 					if (n >= 1)
 						major = Integer.parseInt(v[0]);
 					if (n >= 2)
 						minor = Integer.parseInt(v[1]);
 					if (n >= 3)
 						revision = Integer.parseInt(v[2]);
 				}
 			}
 			
 			public int toInt() {
 				return major*100*100 + minor*100 + revision;
 			}
 		}
 		
 		// get last running version
 		Version last = new Version(MyFunc.getStringPreference(R.string.pref_key_version_name));
 		// current package's version
 		PackageManager pm = this.getPackageManager();
 		String version = null;
 		try {
 			PackageInfo info = pm.getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
 			version = info.versionName;
 		} catch (NameNotFoundException e) {
 			MyLog.e(e.toString());
 		}
 		if (version == null)
 			return;
 		// save current version
 		MyFunc.setStringPreference(R.string.pref_key_version_name, version);
 		Version current = new Version(version);
 		
 		// care of changing version
		if (last.toInt() < new Version("1.1.3").toInt()) {
 			// introduce preferences of router's SSID and versionName.
 			// SSID is saved only when Bluetooth MAC address is changed.
 			// so remove Bluetooth MAC address on preference to save SSID.
 			MyFunc.removePreference(R.string.pref_key_bt_address);
 			// change the method of checking whether the router is active.
 			// in connection with it, need to change the default value of screen_on_wait.
 			// so reset screen_on_wait preference.
 			MyFunc.removePreference(R.string.pref_key_screen_on_wait);
 		}
 	}
 	
 	public AtermHelper getAterm() {
 		if (_aterm == null)
 			_aterm = new AtermHelper(this);
 		return _aterm;
 	}
 	
 	@Override
 	public void onLowMemory() {
 		_aterm = null;
 	}
 }
