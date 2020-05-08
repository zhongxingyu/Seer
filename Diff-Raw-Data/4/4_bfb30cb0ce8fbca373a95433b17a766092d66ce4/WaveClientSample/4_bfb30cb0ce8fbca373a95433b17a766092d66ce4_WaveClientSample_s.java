 package edu.berkeley.waveclientsample;
 
import edu.berkeley.androidwave.waveservice.IWaveServicePublic;
import edu.berkeley.androidwave.waveservice.IWaveRecipeOutputDataListener;
 
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class WaveClientSample extends Activity
 {
     private IWaveServicePublic mWaveService;
     private boolean mBound;
     
     Button authRequestButton;
     TextView messageTextView;
     
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         authRequestButton = (Button) findViewById(R.id.auth_request_button);
         messageTextView = (TextView) findViewById(R.id.message_textview);
         
         // connect to the service
         
         // check if we are authorized for the recipe and update the UI
         //  - if we are already authorized, let the user switch to the WaveUI
         //    to deauthorize
         //  - if we are not authorized, let the user request it
         
         Intent i = new Intent(Intent.ACTION_MAIN);
         i.setComponent(new ComponentName("edu.berkeley.androidwave.waveservice", "WaveService"));
         if (bindService(i, mConnection, Context.BIND_AUTO_CREATE)) {
             mBound = true;
         } else {
             Log.d(getClass().getSimpleName(), "Could not bind with "+i);
             messageTextView.setText("Failed to bind to the WaveService using Intent "+i);
         }
     }
     
     @Override
     protected void onStop() {
         super.onStop();
         if (mBound) {
             unbindService(mConnection);
             mBound = false;
         }
     }
     
     private OnClickListener authRequestListener = new OnClickListener() {
         public void onClick(View v) {
             // do something
         }
     };
     
     private ServiceConnection mConnection = new ServiceConnection() {
         public void onServiceConnected(ComponentName className, IBinder service) {
             mWaveService = IWaveServicePublic.Stub.asInterface(service);
             
             mWaveService.registerRecipeOutputListener(outputListener, true);
         }
         
         public void onServiceDisconnected(ComponentName className) {
             mWaveService = null;
         }
     };
     
     private IWaveRecipeOutputDataListener outputListener = new IWaveRecipeOutputDataListener.Stub() {
         public void receiveWaveRecipeOutputData(WaveRecipeOutputDataImpl wrOutput) {
             // update the log text
         }
     };
 }
