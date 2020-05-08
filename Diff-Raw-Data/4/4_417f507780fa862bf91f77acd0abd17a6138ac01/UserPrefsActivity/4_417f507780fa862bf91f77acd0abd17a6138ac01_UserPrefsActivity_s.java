 package com.seawolfsanctuary.tmt;
 
 import java.io.File;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.CheckBox;
 import android.widget.Toast;
 
 public class UserPrefsActivity extends Activity {
 
 	public static final String APP_PREFS = "Global";
 
 	CheckBox chk_CompleteFromStn;
 	CheckBox chk_CompleteToStn;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.user_prefs_activity);
 		SharedPreferences settings = getSharedPreferences(APP_PREFS,
 				MODE_PRIVATE);
 
 		chk_CompleteFromStn = (CheckBox) findViewById(R.id.chk_CompleteFromStation);
 		chk_CompleteFromStn.setChecked(settings.getBoolean(
 				"CompleteFromStation", true));
 
 		chk_CompleteToStn = (CheckBox) findViewById(R.id.chk_CompleteToStation);
 		chk_CompleteToStn.setChecked(settings.getBoolean("CompleteToStation",
 				true));
 	}
 
 	public void clearCrashReports(View v) {
 		File appFiles = getFilesDir(); // actually directory
 		File[] listOfFiles = appFiles.listFiles();
 		System.out.println("Found " + listOfFiles.length + " file(s) in "
 				+ appFiles.getAbsolutePath());
 
 		if (listOfFiles.length == 0) {
 			Toast.makeText(getBaseContext(), "No previous reports found.",
 					Toast.LENGTH_SHORT).show();
 		} else {
 			String fileInList;
 			int filesRemoved = 0;
 			for (File file : listOfFiles) {
 				if (file.isFile()) {
 					fileInList = file.getName();
 					if (fileInList.endsWith(".stacktrace")) {
 						System.out.println("Removing: " + fileInList);
 						file.delete();
 						filesRemoved += 1;
 					}
 				}
 			}
 			Toast.makeText(getBaseContext(),
 					"" + filesRemoved + " previous reports removed.",
 					Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	public void chk_CompleteFromStation(View v) {
 		Editor editor = getSharedPreferences(UserPrefsActivity.APP_PREFS,
 				MODE_PRIVATE).edit();
 		chk_CompleteFromStn = (CheckBox) findViewById(R.id.chk_CompleteFromStation);
 		editor.putBoolean("CompleteFromStation",
 				chk_CompleteFromStn.isChecked());
 		editor.commit();
 	}
 
 	public void chk_CompleteToStation(View v) {
 		Editor editor = getSharedPreferences(UserPrefsActivity.APP_PREFS,
 				MODE_PRIVATE).edit();
 		chk_CompleteToStn = (CheckBox) findViewById(R.id.chk_CompleteToStation);
 		editor.putBoolean("CompleteToStation", chk_CompleteToStn.isChecked());
 		editor.commit();
 	}
 }
