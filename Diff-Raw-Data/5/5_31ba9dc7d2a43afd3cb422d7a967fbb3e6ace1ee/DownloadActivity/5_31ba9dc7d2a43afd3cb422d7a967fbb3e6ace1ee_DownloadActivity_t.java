 /**
  * 
  */
 package my.zin.rashidi.android.fugumod;
 
 import static android.app.DownloadManager.ACTION_DOWNLOAD_COMPLETE;
 import static android.app.DownloadManager.COLUMN_REASON;
 import static android.app.DownloadManager.COLUMN_STATUS;
 import static android.app.DownloadManager.ERROR_CANNOT_RESUME;
 import static android.app.DownloadManager.ERROR_DEVICE_NOT_FOUND;
 import static android.app.DownloadManager.ERROR_FILE_ALREADY_EXISTS;
 import static android.app.DownloadManager.ERROR_FILE_ERROR;
 import static android.app.DownloadManager.ERROR_HTTP_DATA_ERROR;
 import static android.app.DownloadManager.ERROR_INSUFFICIENT_SPACE;
 import static android.app.DownloadManager.ERROR_TOO_MANY_REDIRECTS;
 import static android.app.DownloadManager.ERROR_UNHANDLED_HTTP_CODE;
 import static android.app.DownloadManager.ERROR_UNKNOWN;
 import static android.app.DownloadManager.PAUSED_QUEUED_FOR_WIFI;
 import static android.app.DownloadManager.PAUSED_UNKNOWN;
 import static android.app.DownloadManager.PAUSED_WAITING_FOR_NETWORK;
 import static android.app.DownloadManager.PAUSED_WAITING_TO_RETRY;
 import static android.app.DownloadManager.STATUS_FAILED;
 import static android.app.DownloadManager.STATUS_PAUSED;
 import static android.app.DownloadManager.STATUS_PENDING;
 import static android.app.DownloadManager.STATUS_RUNNING;
 import static android.app.DownloadManager.STATUS_SUCCESSFUL;
 import static android.net.Uri.parse;
 import static android.os.Environment.DIRECTORY_DOWNLOADS;
 import static android.preference.PreferenceManager.getDefaultSharedPreferences;
 import static android.widget.Toast.LENGTH_LONG;
 import static android.widget.Toast.makeText;
 import static com.stericson.RootTools.RootTools.debugMode;
 import static com.stericson.RootTools.RootTools.getShell;
 import static com.stericson.RootTools.RootTools.getWorkingToolbox;
 import static java.lang.String.format;
 
 import java.io.File;
 
 import android.app.DownloadManager;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 import com.stericson.RootTools.CommandCapture;
 
 /**
  * @author shidi
  * @version 1.1.1
  * @since 1.1.0
  * 
  * Based on tutorial at http://android-er.blogspot.com/2011/07/check-downloadmanager-status-and-reason.html
  */
 public class DownloadActivity extends FragmentActivity {
 
 	private final String PREFERENCE_RELEASE_ID = "releaseId";
 	private final String DIRECTORY_DOWNLOADS_FULL = format("/%s/%s", "sdcard", DIRECTORY_DOWNLOADS);
 
 	private SharedPreferences preferenceManager;
 	private DownloadManager downloadManager;
 	
 	private	String release;
 	private String targetUrl;
 	
 	@Override
 	protected void onCreate(Bundle arg0) {
 		
 		super.onCreate(arg0);
 		setContentView(R.layout.activity_download);
 		
 		preferenceManager = getDefaultSharedPreferences(this);
 		downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
 		
 		SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.release_ref), 0);
 		
 		targetUrl = sharedPreferences.getString(getString(R.string.target_url), null);
 		release = sharedPreferences.getString(getString(R.string.release_zip), null);
 
 		TextView txtViewRelease = (TextView) findViewById(R.id.textViewRelease);
 		txtViewRelease.setText(release.substring(release.lastIndexOf("_") + 1, release.indexOf(".zip")));
 		
		if (!isFileExists(release)) { requestDownload(release); }
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		unregisterReceiver(downloadReceiver);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		
 		checkDownloadStatus();
 		
 		IntentFilter intentFilter = new IntentFilter(ACTION_DOWNLOAD_COMPLETE);
 		registerReceiver(downloadReceiver, intentFilter);
 	}
 	
 	private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
 		
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			checkDownloadStatus();
 		}
 	};
 	
 	private void checkDownloadStatus() {
 		DownloadManager.Query query = new DownloadManager.Query();
 		long id = preferenceManager.getLong(PREFERENCE_RELEASE_ID, 0);
 		query.setFilterById(id);
 		
 		Cursor cursor = downloadManager.query(query);
 		if (cursor.moveToFirst()) {
 			int columnIndex = cursor.getColumnIndex(COLUMN_STATUS);
 			int status = cursor.getInt(columnIndex);
 			int columnReason = cursor.getColumnIndex(COLUMN_REASON);
 			int reason = cursor.getInt(columnReason);
 			
 			switch (status) {
 			case STATUS_FAILED:
 				String failedReason = "";
 				
 				switch (reason) {
 				case ERROR_CANNOT_RESUME:
 					failedReason = "ERROR_CANNOT_RESUME";
 					break;
 
 				case ERROR_DEVICE_NOT_FOUND:
 					failedReason = "ERROR_DEVICE_NOT_FOUND";
 					break;
 					
 				case ERROR_FILE_ALREADY_EXISTS:
 					failedReason = "ERROR_FILE_ALREADY_EXISTS";
 					break;
 					
 				case ERROR_FILE_ERROR:
 					failedReason = "ERROR_FILE_ERROR";
 					break;
 					
 				case ERROR_HTTP_DATA_ERROR:
 					failedReason = "ERROR_HTTP_DATA_ERROR";
 					break;
 					
 				case ERROR_INSUFFICIENT_SPACE:
 					failedReason = "ERROR_INSUFFICIENT_SPACE";
 					break;
 					
 				case ERROR_TOO_MANY_REDIRECTS:
 					failedReason = "ERROR_TOO_MANY_REDIRECTS";
 					break;
 					
 				case ERROR_UNHANDLED_HTTP_CODE:
 					failedReason = "ERROR_UNHANDLED_HTTP_CODE";
 					break;
 					
 				case ERROR_UNKNOWN:
 					failedReason = "ERROR_UNKNOWN";
 					break;
 				}
 				
 //				displayStatus("FAILED", failedReason);
 				break;
 				
 			case STATUS_PAUSED:
 				String pausedReason = "";
 				
 				switch (reason) {
 				case PAUSED_QUEUED_FOR_WIFI:
 					pausedReason = "PAUSED_QUEUED_FOR_WIFI";
 					break;
 
 				case PAUSED_UNKNOWN:
 					pausedReason = "PAUSED_UNKNOWN";
 					break;
 					
 				case PAUSED_WAITING_FOR_NETWORK:
 					pausedReason = "PAUSED_WAITING_FOR_NETWORK";
 					break;
 					
 				case PAUSED_WAITING_TO_RETRY:
 					pausedReason = "PAUSED_WAITING_FOR_RETRY";
 					break;
 				}
 				
 //				displayStatus("PAUSED", pausedReason);
 				break;
 				
 			case STATUS_PENDING:
 //				displayStatus("PENDING", null);
 				break;
 				
 			case STATUS_RUNNING:
 //				displayStatus("RUNNING", null);
 				break;
 				
 			case STATUS_SUCCESSFUL:
 //				displayStatus("SUCCESSFUL", null);
 				flashImage();
 				break;
 			}
 		}
 	}
 	
 	private void displayStatus(String status, String reason) {
 		
 		if (reason != null) {
 			makeText(this, format("%s: %s", status, reason), LENGTH_LONG).show();
 		} else {
 			makeText(this, format("%s", status), LENGTH_LONG).show();
 		}
 	}
 	
 	private void flashImage() {
 		
 		Button btnFlash = (Button) findViewById(R.id.buttonFlashKernel);
 		btnFlash.setEnabled(true);
 		
 		btnFlash.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				String targetDir = format("%s/%s/", DIRECTORY_DOWNLOADS_FULL, release.replace(".zip", ""));
 				String image = format("%s/%s", targetDir, "boot.img");
 				
 				try {
 					debugMode = true;
 					
 					getShell(true).add(
 							new CommandCapture(0, 
 									format("mkdir %s", targetDir), 
 									format("unzip %s/%s -d %s", DIRECTORY_DOWNLOADS_FULL, release, targetDir),
 									format("%s dd if=%s of=/dev/block/platform/omap/omap_hsmmc.0/by-name/boot", getWorkingToolbox(), image),
 									format("rm -fr %s", targetDir)
 							)).waitForFinish();
 					
 					TextView tvFlashCompleted = (TextView) findViewById(R.id.textViewFlashCompleted);
 					tvFlashCompleted.setText(getString(R.string.flash_completed));
 					
 				} catch (Exception e) {
 					Log.e(getString(R.string.app_name), "Flashing failed: ", e);
 				}
 			}
 		});
 	}
 	
 	private void requestDownload(String filename) {
 		
 		DownloadManager.Request request = new DownloadManager.Request(parse(format("%s/%s", targetUrl, filename)));
 		request.setTitle(format("%s %s", getString(R.string.app_name), "Download"));
 		request.setDescription(release);
 		request.allowScanningByMediaScanner();
 		request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
 		request.setDestinationInExternalPublicDir(DIRECTORY_DOWNLOADS, release);
 		
 		long id = downloadManager.enqueue(request);
 
 		Editor editor = preferenceManager.edit();
 		editor.putLong(PREFERENCE_RELEASE_ID, id);
 		editor.commit();
 	}
 	
 	private boolean isFileExists(String filename) {
 		File file = new File(format("%s/%s", DIRECTORY_DOWNLOADS_FULL, filename));
 		return file.exists();
 	}
 }
