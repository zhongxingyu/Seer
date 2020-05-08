 package de.gymbuetz.gsgbapp;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.widget.Toast;
 import de.gymbuetz.gsgbapp.classes.Constants;
 import de.gymbuetz.gsgbapp.classes.DownloadXmlTask;
 import de.gymbuetz.gsgbapp.classes.RepPlan;
 import de.gymbuetz.gsgbapp.classes.TimetableSet;
 
 public class Application extends android.app.Application {
 
 	private RepPlan repPlan;
 	private TimetableSet tts;
 	private String repPlanString;
 	private String timetableString;
 
 	public RepPlan getRepPlan() {
 		return repPlan;
 	}
 
 	public void setRepPlan(RepPlan repPlan) {
 		this.repPlan = repPlan;
 	}
 
 	public TimetableSet getTts() {
 		return tts;
 	}
 
 	public void setTts(TimetableSet tts) {
 		this.tts = tts;
 	}
 
 	public String getRepPlanString() {
 		return repPlanString;
 	}
 
 	public void setRepPlanString(String resultString) {
 		this.repPlanString = resultString;
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 
 		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
 		if (networkInfo != null && networkInfo.isConnected()) {
 			if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("offline_mode", false)) {
 				try {
 					Log.i("Working", "offline mode");
 					repPlanString = loadXmlfromFile(Constants.REPPALN_FILE_NAME);
 					repPlan = RepPlan.parseXml(repPlanString);
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			} else {
 				loadReplacementData();
 			}
 		} else {
 			try {
 				Log.i("Working", "not connected mode");
 				repPlanString = loadXmlfromFile(Constants.REPPALN_FILE_NAME);
 				repPlan = RepPlan.parseXml(repPlanString);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 		File file = new File(getFilesDir(), Constants.TIMETABLE_FILE_NAME);
 		if (!file.exists()) {
 			loadTimetableData();
 		}
 		try {
 			timetableString = loadXmlfromFile(Constants.TIMETABLE_FILE_NAME);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		tts = TimetableSet.parseXml(timetableString, this);
 	}
 
 	private void loadReplacementData() {
 		Log.i("Internet", "Loading repPlan from Server");
 
 		ProgressDialog pd = new ProgressDialog(this);
 		pd.setProgress(0);
 		pd.setTitle(R.string.loading);

 		pd.setOnCancelListener(new OnCancelListener() {
 
 			@Override
 			public void onCancel(DialogInterface dialog) {
 				try {
 					Log.i("Working", "after download");
 					repPlanString = loadXmlfromFile(Constants.REPPALN_FILE_NAME);
 					repPlan = RepPlan.parseXml(repPlanString);
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		});
 		new DownloadXmlTask(this, Constants.REPPLAN_URL, Constants.REPPALN_FILE_NAME, pd).execute();
 	}
 
 	private void loadTimetableData() {
 		ProgressDialog pd = new ProgressDialog(this);
 		pd.setProgress(0);
 		pd.setTitle(R.string.loading);
 
 		pd.setOnCancelListener(new OnCancelListener() {
 
 			@Override
 			public void onCancel(DialogInterface dialog) {
 				try {
 					Log.i("Working", "after download");
 					timetableString = loadXmlfromFile(Constants.REPPALN_FILE_NAME);
 					parseTts();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		});
 
 		new DownloadXmlTask(this, Constants.TIMETABLE_URL, Constants.TIMETABLE_FILE_NAME, pd).execute();
 	}
 
 	protected void parseTts() {
 		tts = TimetableSet.parseXml(timetableString, this);
 	}
 
 	private String loadXmlfromFile(String filename) throws IOException {
 		// Declaration of the file to read
 		Log.i("File", "Loading File " + filename);
 
 		File file = new File(getFilesDir(), filename);
 
 		// try block for reading
 		try {
 			return getStringFromFile(file);
 		} catch (Exception e) {
 			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
 			return "";
 		}
 	}
 
 	public static String getStringFromFile(File fl) throws Exception {
 
 		FileInputStream fin = new FileInputStream(fl);
 		String ret = DownloadXmlTask.convertStreamToString(fin);
 		// Make sure you close all streams.
 		fin.close();
 		return ret;
 	}
 
 }
