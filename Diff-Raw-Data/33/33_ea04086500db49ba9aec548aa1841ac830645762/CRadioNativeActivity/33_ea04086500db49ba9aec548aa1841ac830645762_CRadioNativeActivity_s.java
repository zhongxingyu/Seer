 package com.senselessweb.cradionative;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.speech.RecognizerIntent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 
 import com.senselessweb.cradionative.radio.Radio;
 
 public class CRadioNativeActivity extends Activity 
 {
 	
 	private Radio radio = null;
 	
 	private BroadcastReceiver broadcastReceiver;
 	
     /** 
      * Called when the activity is first created. 
      */
     @Override
     public void onCreate(final Bundle savedInstanceState) 
     {
         super.onCreate(savedInstanceState);
         
         if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
         	this.setContentView(R.layout.main_horizontal);
         else
         	this.setContentView(R.layout.main_vertical);
         
         this.broadcastReceiver = new BroadcastReceiver() {
 			
 			@Override
 			public void onReceive(final Context context, final Intent intent)
 			{
 				if (!this.isInitialStickyBroadcast() && intent.getIntExtra("state", -1) == 0)
 					CRadioNativeActivity.this.radio.stop();
 			}
 		};
         this.registerReceiver(this.broadcastReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
     }
     
     @Override
     protected void onResume()
     {
     	super.onResume();
         if (this.radio == null)
         	this.radio = new Radio(this);
         else
         	this.radio.onResume();
     }
 
     @Override
     protected void onDestroy()
     {
     	super.onDestroy();
     	this.unregisterReceiver(this.broadcastReceiver);
     }
     
     @Override
     public void onConfigurationChanged(Configuration newConfig)
     {
     	super.onConfigurationChanged(newConfig);
 
         if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) 
             this.setContentView(R.layout.main_horizontal);
         else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
             this.setContentView(R.layout.main_vertical);
     }
     
     /**
      * Is called when any action is performed
      * 
      * @param view The action that triggered this event.
      */
     public void onClick(final View view)
     {
     	switch (view.getId())
     	{
     		case R.id.buttonTogglePlay:
     			this.radio.togglePlayback();
     			break;
     		
     		/* Station presets */	
     		case R.id.buttonStation1:
     			this.radio.playPreset(1);
     			break;
     		case R.id.buttonStation2:
     			this.radio.playPreset(2);
     			break;
     		case R.id.buttonStation3:
     			this.radio.playPreset(3);
     			break;
     		case R.id.buttonStation4:
     			this.radio.playPreset(4);
     			break;
     		case R.id.buttonStation5:
     			this.radio.playPreset(5);
     			break;
     		case R.id.buttonStation6:
     			this.radio.playPreset(6);
     			break;
     			
     		/* Genre and station up/down buttons */
     		case R.id.buttonGenreUp:
     			this.radio.nextGenre();
     			break;
     		case R.id.buttonGenreDown:
     			this.radio.previousGenre();
     			break;
     		case R.id.buttonStationUp:
     			this.radio.nextStation();
     			break;
     		case R.id.buttonStationDown:
     			this.radio.previousStation();
     			break;
     			
     		case R.id.buttonRecogSpeech:
     			this.recognizeSpeech();
     			break;
     	}
     }
     
 
     @Override
     public boolean onCreateOptionsMenu(final Menu menu) 
     {
         final MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu, menu);
         return true;
     }
 
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
     	switch (item.getItemId())
     	{
     		case R.id.menuItemExit: 
     			this.finish();
     			break;
     	}
     	
     	return true;
     }
     
     private void recognizeSpeech()
     {
     	final Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
         intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
         intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Tell me a genre!");
         intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "English");
 
     	this.startActivityForResult(intent, 0);
     }
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data)
     {
     	if (data == null) return;
     	
     	final ArrayList<String> matches = data.getStringArrayListExtra(
                 RecognizerIntent.EXTRA_RESULTS);
     	if (!matches.isEmpty())
     	{
     		final String genre = matches.get(0).substring(0, 1).toUpperCase() + matches.get(0).substring(1);
     		Log.d(CRadioNativeActivity.class.toString(), "Recognized: " + genre);
     		this.radio.playGenre(genre);
     	}
     }
     
     
 }
