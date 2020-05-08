 package com.jacob.patton.bereaconvo;
 
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.text.Html;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockActivity;
 
 
 
 
 public class About extends SherlockActivity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
		
 		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
 		
 		if(settings.getInt("Theme",0) != 0){
 			setTheme(R.style.Theme_BereaBlue);
 		}
		
		super.onCreate(savedInstanceState);
 		setContentView(R.layout.about);
 		
 		// I did this so that it could use bold print with HTML.  
 		TextView res = (TextView)findViewById(R.id.AboutResourcesInfo);
 		res.setText(Html.fromHtml(getString(R.string.resources_info)));
 		TextView help = (TextView)findViewById(R.id.AboutHelpInfo);
 		help.setText(Html.fromHtml(getString(R.string.help_info)));
 		TextView license = (TextView)findViewById(R.id.ApacheLicense);
 		license.setText(Html.fromHtml(getString(R.string.apache_license)));
 		
 	}
 
 	
 
 }
