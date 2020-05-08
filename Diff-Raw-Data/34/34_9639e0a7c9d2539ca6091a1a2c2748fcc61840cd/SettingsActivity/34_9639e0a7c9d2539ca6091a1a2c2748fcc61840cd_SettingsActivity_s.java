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
		
 	}
 	
 
 }
 
