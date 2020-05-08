 /*******************************************************
  * @Title: SettingsActivity.java
  * @Package org.shangjiyu.twidere.extension.translaetor
  * @Description: TODO(显示设置页面)
  * @author shangjiyu
  * @date 2013-10-10 下午1:55:28
  * @version V1.0
  ********************************************************/
 
 package org.shangjiyu.twidere.extension.translator;
 
 import org.shangjiyu.twidere.extension.translator.R;
 
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.preference.PreferenceActivity;
 import android.widget.Toast;
 
 /********************************************************
  * @ClassName: SettingsActivity
  * @Description: TODO(设置页面)
  * @author shangjiyu
  * @date 2013-10-10 下午1:55:28
  */
 
 public class SettingsActivity extends PreferenceActivity implements
 		OnSharedPreferenceChangeListener {
 
 	private SharedPreferences sharedPreferences;
 	
 	@SuppressWarnings("deprecation")
 	@Override
 	protected void onCreate(final Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
 		addPreferencesFromResource(R.xml.settings);
 		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
 	}
 	
 	
 	/********************************************************
 	 *Title: onSharedPreferenceChanged
 	 *Description: 
 	 * @param sharedPreferences
 	 * @param key
 	 * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener#onSharedPreferenceChanged(android.content.SharedPreferences, java.lang.String)
 	 *********************************************************/
 	
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
 			String key) {
 		// TODO Auto-generated method stub
 		if (sharedPreferences.getBoolean(getString(R.string.baidu_translate_api_checkbox), false)) {
 			String baiduAPIKey = sharedPreferences.getString(getString(R.string.baidu_client_id_value), null);
 			if ( baiduAPIKey != null && baiduAPIKey.length() == 24) {
 				BDTranslate.BDTRANSLATEKEY_STRING = baiduAPIKey;
 			}else {
				Toast.makeText(SettingsActivity.this, "incorrec baidu app key!please reinpiut", Toast.LENGTH_LONG).show();
 			}
 		}else if (sharedPreferences.getBoolean(getString(R.string.microsoft_translate_api_checkbox), false)) {
			String bingAPIkey = sharedPreferences.getString(getString(R.string.microsoft_client_secret_value), null);
 			if (bingAPIkey != null && bingAPIkey.length() == 44) {
 				MSTranslate.CLIEND_SECRET_STRING = bingAPIkey;
 			}else {
				Toast.makeText(SettingsActivity.this, "incorrec Bing API Secret!please reinpiut", Toast.LENGTH_LONG).show();
 			}
 		}
 	}
 	
 
 }
 
