 package com.cs371m.austinrecycle;
 
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo.State;
 import android.os.Bundle;
 import android.os.Handler;
 import android.provider.Settings;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 
 public class SplashScreenActivity extends Activity {
 
 	private final static String TAG = "SplashScreenActivity";
 	private static int TIME_OUT = 3000;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_splash_screen);
 		
 		new Handler().postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				// Check if it is connecting to Internet
 				ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 				State mobile = conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
 				State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
				if(mobile.equals(State.DISCONNECTED) || mobile.equals(State.DISCONNECTING)
						|| wifi.equals(State.DISCONNECTED) || wifi.equals(State.DISCONNECTING)) {
 					AlertDialog.Builder connectionDialogBuilder = new AlertDialog.Builder(SplashScreenActivity.this);
 					connectionDialogBuilder.setTitle("Connection error");
 					connectionDialogBuilder.setMessage(	"This app requires Internet connection.\n" +
 																				"Please make sure you are connected to the Internet.");
 					connectionDialogBuilder.setNegativeButton("Try again", new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							Log.d(TAG, "Try again");
 							SplashScreenActivity.this.recreate();
 						}
 					});
 					connectionDialogBuilder.setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							Log.d(TAG, "Settings");
 							startActivity(new Intent(Settings.ACTION_SETTINGS));
 							SplashScreenActivity.this.recreate();
 						}
 					});
 					
 					AlertDialog connectionDialog = connectionDialogBuilder.create();
 					connectionDialog.show();
 				}
 				else {
 					Log.d(TAG, "Connected to internet");
 					Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
 					SplashScreenActivity.this.startActivity(intent);
 					SplashScreenActivity.this.finish();
 				}
 			}
 		}, TIME_OUT);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.splash_screen, menu);
 		return true;
 	}
 }
