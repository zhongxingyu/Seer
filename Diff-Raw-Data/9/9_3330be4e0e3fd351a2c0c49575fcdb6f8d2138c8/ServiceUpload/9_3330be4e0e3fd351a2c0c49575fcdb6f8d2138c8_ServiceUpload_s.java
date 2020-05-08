 //
 // Copyright (C) 2013, Maxim Osipov
 //
 // All rights reserved.
 //
 // Redistribution and use in source and binary forms, with or without modification,
 // are permitted provided that the following conditions are met:
 //
 //  - Redistributions of source code must retain the above copyright notice, this
 //    list of conditions and the following disclaimer.
 //  - Redistributions in binary form must reproduce the above copyright notice,
 //    this list of conditions and the following disclaimer in the documentation
 //    and/or other materials provided with the distribution.
 //  - Neither the name of the University of Oxford nor the names of its
 //    contributors may be used to endorse or promote products derived from this
 //    software without specific prior written permission.
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 // ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 // WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 // IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 // INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 // BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 // DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 // LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 // OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 //
 
 package com.ibme.android.actopsy;
 
 import com.ibme.android.actopsy.R;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.util.ArrayList;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.HandlerThread;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.Process;
 import android.preference.PreferenceManager;
 import android.text.TextUtils;
 
 // In order to start the service the application should run from the phone memory.
 // This one wakes up every 24 hours and attempts to upload data (if upload is enabled)
 // or just removes old files.
 public class ServiceUpload extends Service implements OnSharedPreferenceChangeListener {
 
 	private static final String TAG = "ActopsyUploadService";
 
 	private long mDeleteAge; // in milliseconds
 	private String mUserID;
 	private String mUserPass;
 	private boolean mUpload;
 	private boolean mUploadDisabled;
 
 	ConnectivityManager mConnectivityManager;
 	ConnectivityReceiver mConnectivityReceiver;
 	AlarmManager mAlarmManager;
 	AlarmReceiver mAlarmReceiver;
 
 	@Override
 	public void onCreate() {
 		// TODO: Are we actually running in this thread???
 		HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
 		thread.start();
 
 		// android.os.Debug.waitForDebugger();
 
 		long ts = System.currentTimeMillis();
 
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		int localStorage = Integer.valueOf(prefs.getString("editLocalStorage", "10")); 
 		mDeleteAge = localStorage*ClassConsts.MILLIDAY;
 		mUserID = prefs.getString("editUserID", "");
 		mUserPass = prefs.getString("editUserPass", "");
 		mUpload = prefs.getBoolean("checkboxShare", false);
 		mUploadDisabled = false;
 
 		// Prepare for connectivity tracking
 		mConnectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
 		mConnectivityReceiver = new ConnectivityReceiver();
 		registerReceiver(mConnectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
 
 		// Prepare for periodic activation
 		mAlarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
 		mAlarmReceiver = new AlarmReceiver();
 		Intent intent = new Intent(ClassConsts.UPLOAD_ALARM);
 		PendingIntent pintent = PendingIntent.getBroadcast(this, 0, intent, 0);
 		registerReceiver(mAlarmReceiver, new IntentFilter(ClassConsts.UPLOAD_ALARM));
 		mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, ts + 1000, ClassConsts.UPLOAD_PERIOD, pintent);
 	}
 
 	private File[] getFiles(final String mask) {
 		File[] names = new File[0];
 		File root = Environment.getExternalStorageDirectory();
 		// TODO: Fix it to check and wait for storage
 		try {
 			if (root.canRead()){
 				File folder = new File(root, ClassConsts.FILES_ROOT);
 				if (folder.exists()) {
 					// Build list of zip files from sdcard
 					names = folder.listFiles(new FilenameFilter() {
 						public boolean accept(File dir, String name) {
 							return name.toLowerCase().matches(mask);
 						}
 					});
 				}
 			} else {
 				new ClassEvents(TAG, "ERROR", "SD card not readable");
 			}
 		} catch (Exception e) {
 			new ClassEvents(TAG, "ERROR", "Could get files " + e.getMessage());
 		}
 		return names;
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		new ClassEvents(TAG, "INFO", "Started");
 
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this); 
 		prefs.registerOnSharedPreferenceChangeListener(this);
 		return START_STICKY;
 	}
 
 	@Override
 	public void onDestroy() {
 		unregisterReceiver(mConnectivityReceiver);
 		unregisterReceiver(mAlarmReceiver);
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this); 
 		prefs.unregisterOnSharedPreferenceChangeListener(this);
 
 		new ClassEvents(TAG, "INFO", "Destroyed");
 	}
 
 	// Upload files if connectivity is available
 	public class ConnectivityReceiver extends BroadcastReceiver {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			NetworkInfo ni = mConnectivityManager.getActiveNetworkInfo();
 			if (ni != null && ni.isConnected() && ni.getType() == ConnectivityManager.TYPE_WIFI) {
 				new ClassEvents(TAG, "INFO", "WiFi on");
 
 				// TODO: Testing only!!!
 				// File[] f = getFiles(".*json");
 				// new ClassEvents(TAG, "INFO", "Uploading TC " + f[0]);
				// new TaskUploaderTC(context).execute(System.currentTimeMillis() - ClassConsts.MILLIDAY);
 
 				// Upload allowed, enabled and configured
 				if (mUpload && !mUploadDisabled && !TextUtils.isEmpty(mUserID) && !TextUtils.isEmpty(mUserPass)) {
 					new ClassEvents(TAG, "INFO", "Upload on");
 					// We receive multiple identical CONNECTIVITY_CHANGE events and this leads
 					// to multiple upload attempts where only one is successful and others waste
 					// traffic. Temporary (for one hour) disabling uploads helps to prevent it.
 					mUploadDisabled = true;
 					File[] files = getFiles(".*zip$");
 					if (files != null) {
 						for (int i = 0; i < files.length; i++) {
 							new TaskUploaderIBME(context).execute(files[i]);
 						}
 					}
 				} else {
 					new ClassEvents(TAG, "INFO", "Upload off");
 				}
 			} else {
 				// TODO: Stop uploads?
 				new ClassEvents(TAG, "INFO", "WiFi off");
 			}
 		}
 	}
 
 	// Remove old files
 	public class AlarmReceiver extends BroadcastReceiver {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			File[] files;
 			long now = System.currentTimeMillis();
 
 			// Delete old files
 			files = getFiles(".*");
 			for (int i = 0; i < files.length; i ++) {
 				long age = files[i].lastModified();
 				if (age + mDeleteAge < now) {
 					if (files[i].delete()) {
 						new ClassEvents(TAG, "INFO", "Deleted " + files[i].getName());
 					} else {
 						new ClassEvents(TAG, "ERROR", "Delete failed " + files[i].getName());
 					}
 				}
 			}
 
 			// Archive files older then 2 days 
 			files = getFiles(".*csv$");
 			for (int i = 0; i < files.length; i ++) {
 				long age = files[i].lastModified();
 				if (age + 2*ClassConsts.MILLIDAY < now) {
 					new TaskZipper().execute(files[i].getName());
 				}
 			}
 
 			// Re-enable upload
 			mUploadDisabled = false;
 		}
 	}
 
 	///////////////////////////////////////////////////////////////////////////
 	// Messages interface (unused)
 	///////////////////////////////////////////////////////////////////////////
 	public static final int MSG_REGISTER_CLIENT = 1;
 	public static final int MSG_UNREGISTER_CLIENT = 2;
 
 	private ArrayList<Messenger> mClients = new ArrayList<Messenger>();
 	final Messenger mMessenger = new Messenger(new IncomingHandler());
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return mMessenger.getBinder();
 	}
 
 	class IncomingHandler extends Handler {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case MSG_REGISTER_CLIENT:
 				mClients.add(msg.replyTo);
 				break;
 			case MSG_UNREGISTER_CLIENT:
 				mClients.remove(msg.replyTo);
 				break;
 			default:
 				super.handleMessage(msg);
 			}
 		}
 	}
 
 	///////////////////////////////////////////////////////////////////////////
 	// Shared preference change listener
 	///////////////////////////////////////////////////////////////////////////
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
 		if (key.equals("editLocalStorage")) {
 			int localStorage = Integer.valueOf(sharedPreferences.getString(key, "10")); 
 			if (localStorage > 0) {
 				mDeleteAge = localStorage*ClassConsts.MILLIDAY;
 			}
 		} else if (key.equals("editUserID")) {
 			mUserID = sharedPreferences.getString("editUserID", "");
 		} else if (key.equals("editUserPass")) {
 			mUserPass = sharedPreferences.getString("editUserPass", "");
 		} else if (key.equals("checkboxShare")) {
 			mUpload = sharedPreferences.getBoolean("checkboxShare", false);
 		}
 	}
 }
