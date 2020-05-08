 package com.t3hh4xx0r.addons;
 
 
 import java.io.BufferedInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.HttpURLConnection;
 
 import android.app.AlertDialog;
 import android.app.DownloadManager;
 import android.app.DownloadManager.Query;
 import android.app.DownloadManager.Request;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.content.IntentFilter;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Build;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Looper;
 import android.os.Message;
 import android.preference.Preference;
 import android.preference.PreferenceCategory;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 import android.preference.PreferenceScreen;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import android.util.DisplayMetrics;
 
 import android.util.Log;
 import android.util.Slog;
 
 public class Addons extends PreferenceActivity {
 	public static String DATE = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss").format(new Date());
 
 	private boolean DBG = true;
 	
 	private String TAG = "Addons";
 	//Constants for addons, ties to android:key value in addons.xml
         private static final String GOOGLE_APPS = "google_apps_addon";
 	private static final String SBC1 = "sbc_1";
 	private static final String OMFGBMECHACFS = "omfgbmechacfs";
 	private static final String OMFGBMECHABFS = "omfgbmechabfs";
 	private static final String OMFGBMECHAKANGBANG = "omfgbmechakangbang";
 	private static final String OMFT = "omft";
         public static final String EXTENDEDCMD = "/cache/recovery/extendedcommand";
 
 	public static String CWM_DOWNLOAD_DIR = "/sdcard/t3hh4xx0r/downloads/";
 	public static String DOWNLOAD_DIR = "/mnt/sdcard/t3hh4xx0r/downloads/";
 
 	private static String OUTPUT_NAME;
 	private static String DOWNLOAD_URL;
 
 	private static final int DOWNLOAD_ADDON = 0;
 	private static final int FLASH_ADDON = 1;
 	private static final int INSTALL_ADDON = 2;
 	
         private long enqueue;
 
         private DownloadManager dm;
 
 	private Preference mGoogleApps;
 	private Preference mSBC1;
 	private Preference mOMFGBmechaKernelCfs;
 	private Preference mOMFGBmechaKernelBfs;
 	private Preference mOMFGBmechaKernelKangBang;
 	private Preference mOMFT;
 
 	private boolean mAddonIsFlashable;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
                 super.onCreate(savedInstanceState);
 
                 addPreferencesFromResource(R.xml.addons);
 
                 PreferenceScreen prefSet = getPreferenceScreen();
 
 	        mGoogleApps = prefSet.findPreference(GOOGLE_APPS);
 		mSBC1 = prefSet.findPreference(SBC1);
 	        if ((!Build.MODEL.equals("Incredible"))) {
 			PreferenceCategory kernelCategory = (PreferenceCategory) findPreference("kernel_category");
 		        kernelCategory.removePreference(mSBC1);
 		}
 		mOMFGBmechaKernelCfs = prefSet.findPreference(OMFGBMECHACFS);
 		if ((!Build.MODEL.equals("Thunderbolt"))) {
 			PreferenceCategory kernelCategory = (PreferenceCategory) findPreference("kernel_category");
 			kernelCategory.removePreference(mOMFGBmechaKernelCfs);
 		}
 		mOMFGBmechaKernelBfs = prefSet.findPreference(OMFGBMECHABFS);
 		if ((!Build.MODEL.equals("Thunderbolt"))) {
 			PreferenceCategory kernelCategory = (PreferenceCategory) findPreference("kernel_category");
 			kernelCategory.removePreference(mOMFGBmechaKernelBfs);
 		}
 		mOMFGBmechaKernelKangBang = prefSet.findPreference(OMFGBMECHAKANGBANG);
 		if ((!Build.MODEL.equals("Thunderbolt"))) {
 			PreferenceCategory kernelCategory = (PreferenceCategory) findPreference("kernel_category");
 			kernelCategory.removePreference(mOMFGBmechaKernelKangBang);
 		}
 
                 mOMFT = prefSet.findPreference(OMFT);
 		switch (getResources().getDisplayMetrics().densityDpi) {
 		case DisplayMetrics.DENSITY_MEDIUM:
                         PreferenceCategory appsCategory = (PreferenceCategory) findPreference("apps_category");
                         appsCategory.removePreference(mOMFT);
 		break;
 		}
 		
 		BroadcastReceiver onComplete=new BroadcastReceiver() {
         	    public void onReceive(Context context, Intent intent) {
 			checkFileStatus();
 	            }
     		};
 
 		registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
 	}
 		
 	private boolean isSdCardPresent(){
 		return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
 	}
 	
 	private boolean isSdCardWriteable(){
 		return !Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState());
 	}
 
 	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
      		boolean value;
      		if(this.isSdCardPresent() && this.isSdCardWriteable()){
 		        if (preference == mGoogleApps) {
 				DOWNLOAD_URL = "http://r2doesinc.bitsurge.net/GAPPS.zip";
 				OUTPUT_NAME = "Gapps.zip";
 				mAddonIsFlashable = true;
 			} else if (preference == mSBC1) {
                                 OUTPUT_NAME = "OMFGBk-sbc_1.zip";
                                 DOWNLOAD_URL = "http://r2doesinc.bitsurge.net/Addons/OMFGBk-sbc-1.zip";
                                 mAddonIsFlashable = true;
 			} else if (preference == mOMFGBmechaKernelCfs) {
 				OUTPUT_NAME = "OMFGB-Drod_Cfs.zip";
 				DOWNLOAD_URL = "http://r2doesinc.bitsurge.net/Addons/OMFGB-Drod_Cfs.zip";
 				mAddonIsFlashable = true;
 			} else if (preference == mOMFGBmechaKernelBfs) {
 				OUTPUT_NAME = "OMFGB-Drod_Bfs.zip";
 				DOWNLOAD_URL = "http://r2doesinc.bitsurge.net/Addons/OMFGB-Drod_Bfs.zip";
 				mAddonIsFlashable = true;
 			} else if (preference == mOMFGBmechaKernelKangBang) {
 				OUTPUT_NAME = "OMFGB-Drod_KangBang.zip";
 				DOWNLOAD_URL = "http://r2doesinc.bitsurge.net/Addons/OMFGB-Drod_KangBang.zip";
 				mAddonIsFlashable = true;
                         } else if (preference == mOMFT) {
                                 OUTPUT_NAME = "OMFT.apk";
                                 DOWNLOAD_URL = "http://r2doesinc.bitsurge.net/Addons/OMFT.apk";
                                 mAddonIsFlashable = false;
 
 			}
 
 			checkFileStatus();
      		}
 			return true;
 	}
 
 	private Handler handler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			switch(msg.what){
 			case FLASH_ADDON:
 				flashPackage();
 				break;
 			case INSTALL_ADDON:
 				installPackage();
 				break;
                         case DOWNLOAD_ADDON:
                                 downloadPackage();
                                 break;
 			}
 			return;
 		}
 	};
 
 	public void flashPackage() {
 
 		Thread cmdThread = new Thread(){
 			@Override
 			public void run() {
 
                         File updateDirectory = new File ("/cache/recovery/");
                         if (!updateDirectory.isDirectory()) {
                                 updateDirectory.mkdir();
                         }
 				Looper.prepare();
 
 				try{Thread.sleep(1000);}catch(InterruptedException e){ }
 
 				final Runtime run = Runtime.getRuntime();
 				DataOutputStream out = null;
 				Process p = null;
 
 				try {
 	                                p = run.exec("su");
 					out = new DataOutputStream(p.getOutputStream());
 					out.writeBytes("busybox echo 'rm -r /data/dalvik-cache' > " + EXTENDEDCMD + "\n");
                                         out.writeBytes("busybox echo 'rm -r /cache/dalvik-cache' > " + EXTENDEDCMD + "\n");
 					out.writeBytes("busybox echo 'install_zip(\"" + CWM_DOWNLOAD_DIR + OUTPUT_NAME +"\");' > " + EXTENDEDCMD + "\n");
                                         out.writeBytes("reboot recovery\n");
 					out.flush();
 				} catch (IOException e) {
 					e.printStackTrace();
 					return;
 				}
 			}
 		};
 		cmdThread.start();
 	}
 
 	public void installPackage() {
 	
 	Intent intent = new Intent(Intent.ACTION_VIEW);
 	intent.setDataAndType(Uri.fromFile(new File(DOWNLOAD_DIR + OUTPUT_NAME)), "application/vnd.android.package-archive");
 	startActivity(intent);
 	}
 
 	public void downloadPackage() {
 
 	Slog.d(TAG, "Download for " + OUTPUT_NAME + " started.");
 
 	File downloadDir = new File (DOWNLOAD_DIR);
 	if (!downloadDir.isDirectory()) {
 	    downloadDir.mkdir();
 	}
 
         DownloadManager mDownloadManager = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);  
 
         Uri uri = Uri.parse(DOWNLOAD_URL);  
           
         DownloadManager.Request mRequest =  new  DownloadManager.Request (uri);  
         mRequest.setTitle ("T3hh4xx0r Addons");  
         mRequest.setDescription (OUTPUT_NAME);  
 
 	File file = new File(DOWNLOAD_DIR + OUTPUT_NAME);  
 	mRequest.setDestinationUri(Uri.fromFile(file));
 
         mRequest.setShowRunningNotification(true);  
         mRequest.setVisibleInDownloadsUi(true);  
           
         long downloadId = mDownloadManager.enqueue(mRequest);
 	}
 
 	public void checkFileStatus() {
 	File f = new File (DOWNLOAD_DIR + OUTPUT_NAME);
     	    if (f.exists()) {
 		    Slog.d(TAG, "File is found");
	    	    if (mAddonIsFlashable) {
			flashAlertBox();
                    } else {
   	            handler.sendEmptyMessage(INSTALL_ADDON);
                     }
   	    } else {
 		   Slog.d(TAG, "File not found, starting DL.");
 		    handler.sendEmptyMessage(DOWNLOAD_ADDON);
 	    }
 	}
 
 	private void log(String msg) {
 	    Log.d(TAG, msg);
   	}
 	
 	public void flashAlertBox() {
 	AlertDialog dialog = new AlertDialog.Builder(Addons.this).create();
 	   dialog.setTitle("T3hh4xx0r Addons");
            dialog.setMessage("About to flash " + OUTPUT_NAME + "!");
 	   dialog.setCancelable(false);
 
 	   dialog.setButton("Ok", new DialogInterface.OnClickListener() {
         	public void onClick(DialogInterface dialog, int whichButton) {
  		    handler.sendEmptyMessage(FLASH_ADDON);
     		}
 	   });
  	dialog.show();
 	}
 }
