 package com.glowman.spaceunit;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.DialogInterface.OnClickListener;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Looper;
 import android.os.Vibrator;
 import android.util.Log;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
 import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
 import com.glowman.spaceunit.core.GPGSActivity;
 import com.glowman.spaceunit.data.GooglePlayData;
 import com.google.android.gms.games.Player;
 
 public class MainAndroid extends GPGSActivity implements OnClickListener {
 	// request codes we use when invoking an external activity
 	public final int RC_RESOLVE = 5000, RC_UNUSED = 5001;
 	
 	// tag for debug logging
     final boolean ENABLE_DEBUG = false;
     final String TAG = "ProBIGI";
 	
     public ProgressDialog progress;
     private AlertDialog _greetDialog;
     
 	@Override
 	public void onCreate (Bundle savedInstanceState) {
 		enableDebugLog(ENABLE_DEBUG, TAG);
 		super.onCreate(savedInstanceState);
 
 		AppVibrator.init((Vibrator) getSystemService(Context.VIBRATOR_SERVICE));
 
 		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
 		config.useAccelerometer = false;
 		config.useCompass = false;
 		config.useWakelock = true;
 		config.useGL20 = false;
 		
 		GooglePlayData.game = this;
 		GooglePlayData.gameHelper = super.mHelper;
 		GooglePlayData.gamesClient = super.getGamesClient();
 		
 		
 		//this.showAlert("Wanna game?", "Sign in with Google");
 		//beginUserInit();
 		//TODO доработать приветствие // ага, чтоб работало на эмуляторе ((
 		/*
 		Log.d("hz", "build product : " + Build.PRODUCT);
 		if (!"google_sdk".equals( Build.PRODUCT ) && !mHelper.isSignedIn()) {
 			this.mHelper.showGreetAlert();
 		}
 		*/
 		
 		progress = new ProgressDialog(this);
 		progress.setMessage("Loading...");
 		progress.show();
 		
 		initialize(new Main(), config);
 	}
 	
 	public void initGooglePlay() {
 		progress.dismiss();
 		progress = null;
 		
 		Log.d("hz", "build product : " + Build.PRODUCT);
 		if (!"google_sdk".equals( Build.PRODUCT ) && !mHelper.isSignedIn()) {
 			this.runOnUiThread(new Runnable() {
 				public void run() {
 					showGreetAlert();
 				}
 			});
 		}
 	}
 	
     public void showGreetAlert() {
     	new AlertDialog.Builder(this).setTitle("Welcome!")
         .setMessage("Sign in with Google to earn achievements and submit scores to leaderboards.")
         .setNeutralButton(android.R.string.ok, this).create().show();
     }
 
 	@Override
 	public void onClick(DialogInterface dialog, int which) {
 		beginUserInit();
 	}
 	
 	public void beginUserInit() {
 		try {
 			super.beginUserInitiatedSignIn();
 		}
 		catch (Error e) {
 			Log.e("hz", e.getMessage());
 		}
 	}
 	
 	@Override
 	public void onSignInFailed() {
 		Gdx.app.log("Google Play onSignInFailed", "Oh NOOO! ");
 	}
 
 	@Override
 	public void onSignInSucceeded() {
 		Gdx.app.log("Google Play onSignInSucceeded", "FUCK YEAH!");
 		
 		Player p = GooglePlayData.gamesClient.getCurrentPlayer();
 		if (p != null) {
 			Gdx.app.log("Google Play onSignInSucceeded", "Welcome " + p.getDisplayName());
 		}
 	}
 	
 	public void showURLIntent() {
 		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://glowmanstudio.ru/"));
 		this.startActivity(browserIntent);
 	}
 	
 }
