 package com.appointmentmanager;
 
 import java.util.HashMap;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.appointmentmanager.helper.AsyncJSONMain;
 
 public class MainActivity extends Activity{
 
 	static Context context;
 	private static HashMap<String, Object> memoryMap;
 	private final int STATUS_DIALOG = 1;
 	private Dialog dia_status;
 
 	@SuppressWarnings("unchecked")
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.activity_main);
 
 		Log.v("progress", "oncreate");
 
 		//MISC
 		context = this.getApplicationContext();
 		Intent intent = getIntent();
 		memoryMap = (HashMap<String, Object>)intent.getSerializableExtra("memoryMap");
 
 		Log.v("progress", ""+memoryMap);
 		//content
 		if (!isNetworkAvailable())
 			toast("Please connect to the internet to download employee information.");
 		else if (memoryMap == null){
 			new AsyncJSONMain(
 					"http://ienvynails.com/appman/json/meta.php?",
 					"http://ienvynails.com/appman/json/services.php",
 					"http://ienvynails.com/appman/json/employees.php",
 					this).execute();
 		}//end else
 
 	}
 
 	public void onStart() {
 		super.onStart();
 
		toast(""+GetInfo("main", "MINIMUM_APPOINTMENT_TIME"));
 	}
 
 	protected void onPrepareDialog(final int id, final Dialog v) {
 		switch(id) {
 		case STATUS_DIALOG: {
 
 
 			Button butt_cancel = (Button) v.findViewById(R.id.ab_cancel);
 			butt_cancel.setOnClickListener(new OnClickListener() {
 				public void onClick(View v) {
 					dia_status.cancel();
 				}
 			});
 		}break;
 		}
 	}
 
 	protected Dialog onCreateDialog(int id) {
 		switch (id)
 		{
 		case (STATUS_DIALOG): {
 			View v = View.inflate(this, R.layout.dialog_reservation_info, null);
 
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setView(v);
 			dia_status = builder.create();
 			return dia_status;
 		}
 		}		
 		return null;
 	}
 
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	public void goCheckStatus(View v) {
 		//		http://ienvynails.com/appman/json/nextappointment.php?name=Dr.BrightSwagger&phone=715-271-1913
 		showDialog(STATUS_DIALOG);
 	}
 
 	public void goSchedule(View v) {
 		if (!isNetworkAvailable())
 			toast("Please connect to the internet to download employee information.");
 		else {
 			Intent nextScreen = new Intent(context, StaffTimeList.class);
 			nextScreen.putExtra("memoryMap", memoryMap);
 			startActivity(nextScreen);
 		}
 	}
 
 	public void goEmployeeProfiles(View v) {
 		//check if info is got
 		Intent nextScreen = new Intent(context, EmployeeProfiles.class);
 		nextScreen.putExtra("memoryMap", memoryMap);
 		startActivity(nextScreen);
 
 	}
 
 	public void goSettings(View v) {
 		//check if info is got
 		Intent nextScreen = new Intent(context, Settings.class);
 		nextScreen.putExtra("memoryMap", memoryMap);
 		startActivity(nextScreen);
 	}
 
 	public void goNothing(View v) {
 
 	}
 
 	private boolean isNetworkAvailable() {
 		ConnectivityManager connectivityManager 
 		= (ConnectivityManager) getSystemService(context.CONNECTIVITY_SERVICE);
 		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
 		return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
 	}
 
 	public int toDP(int px)
 	{ 	return (int)(px/context.getResources().getDisplayMetrics().density); }
 
 	private int toPX(int dp)
 	{ 	return (int)(dp*context.getResources().getDisplayMetrics().density); }
 
 	private static void toast(String text)
 	{ 	Toast.makeText(context, text, Toast.LENGTH_LONG).show(); }
 
 	public static HashMap<String, Object> getMemoryMap() 
 	{return memoryMap;}
 
 	public static void setMemoryMap(HashMap<String, Object> memoryMap) 
 	{MainActivity.memoryMap = memoryMap;}
 
 	private void SaveInfo(String area, String saveTag, String value) {
 		SharedPreferences sharedPreferences = getSharedPreferences(area,MODE_PRIVATE);
 		SharedPreferences.Editor editor = sharedPreferences.edit();
 		editor.putString(saveTag, value);
 		editor.commit();
 	}
 
 	private String GetInfo(String tag, String which) {	
 		SharedPreferences sharedPreferences = getSharedPreferences(tag,MODE_PRIVATE);
 		String savedStr = sharedPreferences.getString(which, "");
 		return savedStr;
 	}	  	
 }
 
 
