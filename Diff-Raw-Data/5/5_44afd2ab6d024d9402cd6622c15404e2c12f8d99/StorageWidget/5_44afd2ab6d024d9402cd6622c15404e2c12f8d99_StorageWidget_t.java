 package com.mridang.storage;
 
import android.content.Intent;
 import android.os.Environment;
 import android.os.StatFs;
import android.provider.Settings;
 import android.text.format.Formatter;
 import android.util.Log;
 
 import com.bugsense.trace.BugSenseHandler;
 import com.google.android.apps.dashclock.api.DashClockExtension;
 import com.google.android.apps.dashclock.api.ExtensionData;
 
 /*
  * This class is the main class that provides the widget
  */
 public class StorageWidget extends DashClockExtension {
 
 	/*
 	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onCreate()
 	 */
 	public void onCreate() {
 
 		super.onCreate();
 		Log.d("StorageWidget", "Created");
 		BugSenseHandler.initAndStartSession(this, "904d6d13");
 
 	}
 
 	/*
 	 * This calculates the amount of memory on the device
 	 * 
 	 * @returns   The amount of total memory
 	 */
 	public Long getTotalMemory() {
 		
 		return getInternalTotalMemory() + getExternalTotalMemory();
 
 	}
 	
 	/*
 	 * This calculates the amount of internal memory on the device
 	 * 
 	 * @returns   The amount of internal memory
 	 */
 	public Long getInternalTotalMemory() {
 		
 		StatFs sfsInternal = new StatFs(Environment.getRootDirectory().getAbsolutePath());
 		return Long.valueOf(sfsInternal.getBlockCount()) * Long.valueOf(sfsInternal.getBlockSize());
 
 	}
 	
 	/*
 	 * This calculates the amount of external memory on the device
 	 * 
 	 * @returns   The amount of external memory
 	 */
 	public Long getExternalTotalMemory() {
 		
 		try {
 
 			StatFs sfsExternal = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
 			return Long.valueOf(sfsExternal.getBlockCount()) * Long.valueOf(sfsExternal.getBlockSize());
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 			return 0L;
 		}
 
 	}
 	
 	/*
 	 * @see
 	 * com.google.android.apps.dashclock.api.DashClockExtension#onUpdateData
 	 * (int)
 	 */
 	@Override
 	protected void onUpdateData(int arg0) {
 
 		setUpdateWhenScreenOn(true);
 
 		Log.d("StorageWidget", "Checking device storage");
 		ExtensionData edtInformation = new ExtensionData();
 		edtInformation.visible(true);
 
 		try {
 
 			Log.v("StorageWidget", "Internal: " + Formatter.formatFileSize(getApplicationContext(), getInternalTotalMemory()));
 			if (getInternalTotalMemory() > 0L) {
 				edtInformation
 						.expandedBody((edtInformation.expandedBody() == null ? ""
 								: edtInformation.expandedBody() + "\n")
 								+ String.format(getString(R.string.internal),
 										Formatter.formatFileSize(
 												getApplicationContext(),
 												getInternalFreeMemory()),
 										Formatter.formatFileSize(
 												getApplicationContext(),
 												getInternalTotalMemory())));
 			}
 
 			edtInformation.status(String.format(getString(R.string.available_space), (int) (0.5d + (double) getFreeMemory() * 100 / (double) getTotalMemory())));
			edtInformation.clickIntent(new Intent(Settings.ACTION_WIFI_SETTINGS));
 			
 			Log.v("StorageWidget", "External: " + Formatter.formatFileSize(getApplicationContext(), getExternalTotalMemory()));
 			if (getExternalTotalMemory() > 0L) {
 				edtInformation
 						.expandedBody((edtInformation.expandedBody() == null ? ""
 								: edtInformation.expandedBody() + "\n")
 								+ String.format(getString(R.string.external),
 										Formatter.formatFileSize(
 												getApplicationContext(),
 												getExternalFreeMemory()),
 										Formatter.formatFileSize(
 												getApplicationContext(),
 												getExternalTotalMemory())));
 			}
 
 		} catch (Exception e) {
 			Log.e("StorageWidget", "Encountered an error", e);
 			BugSenseHandler.sendException(e);
 		}
 
 		edtInformation.icon(R.drawable.ic_dashclock);
 		publishUpdate(edtInformation);
 		Log.d("StorageWidget", "Done");
 
 	}
 
 	/*
 	 * This calculates the amount of external free memory on the device
 	 * 
 	 * @returns   The amount of free external memory
 	 */
 	public Long getExternalFreeMemory() {
 		
 		try {
 		
 			StatFs sfsExternal = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
 			return Long.valueOf(sfsExternal.getAvailableBlocks()) * Long.valueOf(sfsExternal.getBlockSize());
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 			return 0L;
 		}
 
 	}
 
 	/*
 	 * This calculates the amount of internal free memory on the device
 	 * 
 	 * @returns   The amount of free internal memory
 	 */
 	public Long getInternalFreeMemory() {
 		
 		StatFs sfsInternal = new StatFs(Environment.getRootDirectory().getAbsolutePath());
 		return Long.valueOf(sfsInternal.getAvailableBlocks()) * Long.valueOf(sfsInternal.getBlockSize());
 
 	}
 	
 	/*
 	 * This calculates the amount of free memory on the device
 	 * 
 	 * @returns   The amount of total free memory
 	 */
 	public Long getFreeMemory() {
 		
 		return getInternalFreeMemory() + getExternalFreeMemory();
 
 	}
 	
 	/*
 	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onDestroy()
 	 */
 	public void onDestroy() {
 
 		super.onDestroy();
 		Log.d("StorageWidget", "Destroyed");
 		BugSenseHandler.closeSession(this);
 
 	}
 
 }
