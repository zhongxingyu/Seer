 /*
     This file is part of dgAlert Classic.
 
     dgAlert Classic is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     dgAlert Classic is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with dgAlert Classic.  If not, see <http://www.gnu.org/licenses/>.
 */
 package com.blau.android.screenon;
 
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.os.Bundle;
 import android.preference.DialogPreference;
 import android.preference.PreferenceActivity;
 
 import com.blau.android.screenon.preferences.EmailDialogPreference;
import com.mikedg.android.shared.splashnotes.SplashNotes;
 
 public class DgAlertClassicConfigActivity extends PreferenceActivity {
 	private String version;
 	public static final String TAG = "dgAlert";
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		addPreferencesFromResource(R.xml.preferences);
 		SplashNotes.showSplashNotes(this, R.string.release_notes);
 		
 		//Try and find app version number
 		version = "";
 		PackageManager pm = this.getPackageManager();
 		try {
 			//Get version number, not sure if there is a better way to do this
 			version = " v" +
 			pm.getPackageInfo(
 				DgAlertClassicConfigActivity.class.getPackage().getName(), 0).versionName;
 		} catch (NameNotFoundException e) {
 			//No need to do anything here if it fails
 			//e.printStackTrace();
 		}
 		
 		DialogPreference aboutPref =
 			(DialogPreference) findPreference(getString(R.string.pref_about_key));
 		aboutPref.setDialogTitle(getString(R.string.app_name) + version);
 		aboutPref.setDialogLayoutResource(R.layout.about);
 		
 		EmailDialogPreference emailPref = (EmailDialogPreference) findPreference(getString(R.string.pref_sendemail_key));
 		emailPref.setVersion(version);
 
 		
 //		ContentResolver myCR = getContentResolver();
 //		long timestamp = Long.valueOf("1234635824");
 //		long threadId = 40;
 //		int id = 0;
 //		Log.v("trying to find message to delete: thread_id = " + threadId + ", date = " + timestamp);
 //		Cursor cursor =
 //			myCR.query(
 //						ContentUris.withAppendedId(DgAlertClassicUtils.CONVERSATION_CONTENT_URI, threadId),
 //						new String[] { "_id", "date", "thread_id" },
 //						"thread_id=" + threadId + " and " + "date=" + timestamp,
 //						//null,
 //						null, "date desc");
 //		if (cursor != null) {
 //			Log.v("cursor was not null");
 //			try {
 //				if (cursor.moveToFirst()) {
 //					// for (int i = 0; i < cursor.getColumnNames().length; i++) {
 //					// Log.v("Column: " + cursor.getColumnNames()[i]);
 //					//							
 //					// }
 //					// Log.v("_id = " + cursor.getInt(0));
 //					// Log.v("date = " + cursor.getLong(1) + " (timestamp = " +
 //					// timestamp + ")");
 //					// Log.v("threadId = " + cursor.getInt(2));
 //					id = cursor.getInt(0);
 //					long date = cursor.getLong(1);
 //					long thread = cursor.getLong(2);
 //					Log.v("id = " + id);
 //					Log.v("date = " + date);
 //					Log.v("thread = " + thread);
 //				}
 //			} finally {
 //				cursor.close();
 //			}
 //		}
 
 		// Preference inboxPref = (Preference)
 		// findPreference(getString(R.string.pref_inbox_app_key));
 		// Intent inboxChooseIntent = new Intent();
 		// inboxChooseIntent.setAction(Intent.ACTION_PICK_ACTIVITY);
 		//		
 		// Intent filterIntent = new Intent(Intent.ACTION_MAIN);
 		// filterIntent.addCategory(Intent.CATEGORY_LAUNCHER);
 		
 	// inboxChooseIntent.putExtra(Intent.EXTRA_INTENT, filterIntent);
 		// inboxChooseIntent.addCategory(Intent.CATEGORY_LAUNCHER);
 		// <action android:name="android.intent.action.MAIN" />
 		// <category android:name="android.intent.category.LAUNCHER" />
 
 		// inboxPref.setIntent(inboxChooseIntent);
 
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();		
 	}
 }
