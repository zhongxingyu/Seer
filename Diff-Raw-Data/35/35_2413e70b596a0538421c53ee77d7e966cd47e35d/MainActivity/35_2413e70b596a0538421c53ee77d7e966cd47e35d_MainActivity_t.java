 package com.example.p1;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.app.Application;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		Log.i("MAIN", "made it into p1 MainActivity.");
 		// int versionName =
 		// getPackageManager();getPackageInfo(getPackageName(), 0).versionCode;
 		String version = "?";
 		try {
 			PackageInfo manager = getPackageManager().getPackageInfo(
 					getPackageName(), 0);
 			version = manager.versionName;
 		} catch (NameNotFoundException e) {
 		}
		
		SimpleDateFormat sdf = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
		String theDate = sdf.format(new Date());
		Log.i("MAIN", "theDate:" + theDate);
		
 		Log.i("MAIN", "version " + version);
 		System.out.println("Yo, starting MainActivity.");
 		
 		Globals.delay1 = "abcdefg";
 		
 		Globals g = Globals.getInstance();
 		g.setState("test1");
 		String s = g.getState();
 		
		Utilities.readPlansTasks(getApplicationContext());

 		setContentView(R.layout.activity_main);
 		String t = getTitle().toString();
 		setTitle(t + " version " + version);
 
 		Button startButton = (Button) findViewById(R.id.button1);
 		startButton.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				EditText text1 = (EditText) findViewById(R.id.editText1);
 				String et1 = text1.getText().toString();
 
 				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 				imm.hideSoftInputFromWindow(text1.getWindowToken(), 0);
 
 				Toast.makeText(getApplicationContext(),
						"Greetings, " + et1 + ".", Toast.LENGTH_SHORT)
 						.show();
 			}
 		});
 
 		Button startVend = (Button) findViewById(R.id.btnVend);
 		startVend.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				startVend();
 			}
 		});
 
 		Button startPlan = (Button) findViewById(R.id.btnPlan);
 		startPlan.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				startPlan();
 			}
 		});
 		
 		Button prefBtn = (Button) findViewById(R.id.btnPrefs);
 		prefBtn.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				Intent settingsActivity = new Intent(getBaseContext(),
                         Preferences.class);
 				startActivity(settingsActivity);			}
 		});
 		
 		
 
 		Log.i("MAIN", "about to exit MainActivity.");
 		System.out.println("Yo, exiting MainActivity.");
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is
 		// present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	private void startVend() {
 		Intent workerIntent = new Intent(this, VendActivity.class);
 		startActivity(workerIntent);
 	}
 
 	private void startPlan() {
 		Intent workerIntent = new Intent(this, PlanActivity.class);
 		startActivity(workerIntent);
 	}
 
 	// private void startAnActivity(Intent i, Bundle b) {
 	// Intent workerIntent = new Intent(i, b);
 	// startActivity(workerIntent);
 	// }
 	
 	public class MyApp extends Application {
 		private String myState;
 
 		  public String getState(){
 		    return myState;
 		  }
 		  public void setState(String s){
 		    myState = s;
 		  }
 	}
 	
 }
