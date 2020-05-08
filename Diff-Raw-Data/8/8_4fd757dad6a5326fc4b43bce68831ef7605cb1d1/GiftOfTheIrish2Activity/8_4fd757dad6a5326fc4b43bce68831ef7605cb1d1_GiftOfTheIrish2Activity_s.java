 package org.coderdojo.giftoftheirish;
 
 import org.coderdojo.giftoftheirish.application.GiftOfTheIrishApplication;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.IntentFilter;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 
 /**
  * GiftOfTheIrish2Activity
  * 
  * This activity provides the user interface functionality when the user clicks on the icon.  
  * <ul>
  * 		<li> Sets up the broadcast receiver for the  Sms Watcher </li>
  * 		<li> Provides the button functionality </li>
  * 		<li> Provides Talk On/Off descision to the application </li>
  * 		<li> Handles create and destory to insure the app is closed properly </li>
  * </ul>
  * 
  * @author Noel King
  *
  */
 public class GiftOfTheIrish2Activity extends Activity {
 	
 	/**Created statically as we only ever want one instance of this sms watcher available */
 	private BroadcastReceiver watchForSms;
 	
 	/** Application context used to hold application state */
 	private GiftOfTheIrishApplication application;
 	
 	/** Determines whether the broadcast received is registered in this activity*/
 	private boolean isRegistered = false;
 	
 	/** SMS intent filter */
 	private IntentFilter smsIntent;
 	
 	/** 
 	 * Called when the activity is first created. 
 	 * */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         //app going to start talking now
         setUpApplicationToTalk();
         
         //Sets up watch for Sms 
         watchForSms = new SmsWatcher();
         
         //add the sms Intent
         smsIntent = new IntentFilter();
         smsIntent.addAction("android.provider.Telephony.SMS_RECEIVED");
     }
     
     /** 
      * Sets up the application to talk 
      * */
     private void setUpApplicationToTalk() {
     	 //get the application so can be used by the app
         application = (GiftOfTheIrishApplication)getApplicationContext();
     }
     
     /**
      * Turns speach on / off for this app
      * @param speakOutLoud - true will shout the text messages out
      */
     private void appTalk(boolean speakOutLoud) {
     	application.setReadSmsOutLoud(speakOutLoud);
     }
     
     /**
      * On restart of app then retrigger the sms receiver
      */
     public void onResume()
     {
       super.onResume();
       
       //on return from reading an SMS out what should the app do
       if(application.isReadSmsOutLoud()) {
 	      startWatcher(getCurrentFocus());
       } else {
     	  stopWatcher(getCurrentFocus());
       }
     }
 
     /**
      * Stop sms when you hit the button
      * @param view
      */
     public void smsActionWatch(View view) {
     	if(application.isReadSmsOutLoud())
     		stopWatcher(view);
     	else 
     		startWatcher(view);
     }
     
     /**
     * Handles the starting of the watchers
      * @param view
      */
     private void startWatcher(View view) {
    	handleButton("Stop Lisening", Color.RED);
 	    registerReceiver(watchForSms, smsIntent); 
 	    isRegistered = true;
 	    application.setReadSmsOutLoud(true);
     }
     
     /**
      * Insures no sms read when app not running
      */
     public void onDestroy() {
     	super.onDestroy();
     	stopWatcher(getCurrentFocus());
     }
     
     /**
      * Stops the sms watcher
      */
     private void stopWatcher(View view) {
       	 
     	 //un register
     	 if(isRegistered) {
     	   unregisterReceiver(watchForSms);
     	   isRegistered = false;
     	 }
     	 //set the text and color
      	 handleButton("Start Lisening", Color.GREEN);
       	 application.setReadSmsOutLoud(false);
     }
     
     /**
      * Handles the new color and text of the button on change
      * @param text
      * @param color
      */
     private void handleButton(String text, int color) {
     	  Button appButton = (Button)findViewById(R.id.appButton);
       	  appButton.setText(text);
       	  appButton.setTextColor(color);
     }
 	
 }
