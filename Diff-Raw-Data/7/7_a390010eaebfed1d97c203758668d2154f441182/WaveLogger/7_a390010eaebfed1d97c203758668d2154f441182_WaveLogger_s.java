 package edu.berkeley.androidwave.wavelogger;
 
 import edu.berkeley.androidwave.waveclient.*;
 import edu.berkeley.androidwave.wavelogger.service.WaveLoggerService;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ActivityNotFoundException;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 import java.io.File;
 import java.util.ArrayList;
 
 public class WaveLogger extends Activity {
     
     private static final String TAG = WaveLogger.class.getSimpleName();
 
     private static final String ACTION_WAVE_SERVICE = "edu.berkeley.androidwave.intent.action.WAVE_SERVICE";
     private static final String ACTION_DID_AUTHORIZE = "edu.berkeley.androidwave.intent.action.DID_AUTHORIZE";
     private static final String ACTION_DID_DENY = "edu.berkeley.androidwave.intent.action.DID_DENY";
     private static final int REQUEST_CODE_AUTH = 1;
 
     public static final String ACCEL_RECIPE_ID = "edu.berkeley.waverecipe.passthrough.AccelerometerPassThrough";
     public static final String LOC_RECIPE_ID = "edu.berkeley.waverecipe.passthrough.LocationPassThrough";
 
     // we use the package name, so clones on the WaveLogger (with different package names)
     // can simultaneously connect to the WaveService
     private String API_KEY;
     
     private IWaveServicePublic mWaveService;
     private boolean mBound;
     
     protected DbHelper databaseHelper;
 
     protected Button accelButton;
     protected Button locButton;
     protected Button startButton;
     protected Button stopButton;
     protected TextView messageTextView;
     
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         // set the API KEY
         API_KEY = this.getPackageName();
         
         // connect UI Outlets
         accelButton = (Button) findViewById(R.id.accel_button);
         locButton = (Button) findViewById(R.id.loc_button);
         startButton = (Button) findViewById(R.id.start_button);
         stopButton = (Button) findViewById(R.id.stop_button);
         
         // configure UI state
         accelButton.setEnabled(false);
         locButton.setEnabled(false);
         startButton.setEnabled(false);
         stopButton.setEnabled(true);
         
         stopButton.setOnClickListener(stopButtonListener);
         
         // open the database
         databaseHelper = new DbHelper(this);
         
         // connect to the service
         Intent i = new Intent(ACTION_WAVE_SERVICE);
         if (bindService(i, mConnection, Context.BIND_AUTO_CREATE)) {
             mBound = true;
             Toast.makeText(this, "Connected to WaveService", Toast.LENGTH_SHORT).show();
         } else {
             Log.d(TAG, "Could not bind with "+i);
             Toast.makeText(this, "Could not connect to the WaveService!", Toast.LENGTH_SHORT).show();
             messageTextView.setText("ERROR:\n\nFailed to bind to the WaveService.\n\nIs AndroidWave installed on this device?\n\nPlease address this issue and restart this Application.");
         }
     }
     
     @Override
     protected void onStop() {
         super.onStop();
         
         databaseHelper.closeDatabase();
         
         if (mBound) {
             unbindService(mConnection);
             mBound = false;
         }
     }
     
     /**
      * Handle result from authorization intent
      */
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (requestCode == REQUEST_CODE_AUTH) {
             if (resultCode == RESULT_OK) {
                 if (data.getAction().equals(ACTION_DID_AUTHORIZE)) {
                     Toast.makeText(WaveLogger.this, "Authorization Successful!", Toast.LENGTH_SHORT).show();
                     checkAuthorizations();
                 } else {
                     Toast.makeText(WaveLogger.this, "Authorization Denied!", Toast.LENGTH_SHORT).show();
                 }
             } else {
                 Toast.makeText(WaveLogger.this, "Authorization process canceled.", Toast.LENGTH_SHORT).show();
             }
         }
     }
     
     protected void checkAuthorizations() {
         try {
             boolean authorizedAtLeastOne = false;
             
             if (mWaveService.isAuthorized(API_KEY, ACCEL_RECIPE_ID)) {
                 WaveRecipeAuthorizationInfo authInfo = mWaveService.retrieveAuthorizationInfo(API_KEY, ACCEL_RECIPE_ID);
                 accelButton.setText(String.format("Accelerometer authorized at %5f Hz, %5f precision", authInfo.outputMaxRate, authInfo.outputMaxPrecision));
                 accelButton.setEnabled(false);
             } else {
                 accelButton.setOnClickListener(accelButtonListener);
                 accelButton.setEnabled(true);
                authorizedAtLeastOne = true;
             }
             
             if (mWaveService.isAuthorized(API_KEY, LOC_RECIPE_ID)) {
                 WaveRecipeAuthorizationInfo authInfo = mWaveService.retrieveAuthorizationInfo(API_KEY, LOC_RECIPE_ID);
                 accelButton.setText(String.format("Location authorized at %5f Hz, %5f precision", authInfo.outputMaxRate, authInfo.outputMaxPrecision));
                 locButton.setEnabled(false);
             } else {
                 locButton.setOnClickListener(locButtonListener);
                 locButton.setEnabled(true);
                authorizedAtLeastOne = true;
             }
             
             if (authorizedAtLeastOne) {
                 startButton.setOnClickListener(startButtonListener);
                 startButton.setEnabled(true);
             }
         } catch (RemoteException re) {
             Log.d(TAG, "lost connection to the service", re);
         }
     }
     
     /**
      * Continue setup after binding to WaveService
      */
     protected void afterBind() {
         checkAuthorizations();
     }
     
     /**
      * OnClickListener(s)
      */
     private OnClickListener accelButtonListener = new OnClickListener() {
         public void onClick(View v) {
             try {
                 // get an auth intent from the service
                 Intent i = mWaveService.getAuthorizationIntent(ACCEL_RECIPE_ID, API_KEY);
             
                 // then run it looking for a result
                 try {
                     startActivityForResult(i, REQUEST_CODE_AUTH);
                 } catch (ActivityNotFoundException anfe) {
                     anfe.printStackTrace();
                     Toast.makeText(WaveLogger.this, "Error launching authorization UI", Toast.LENGTH_SHORT).show();
                 }
             } catch (RemoteException e) {
                 Log.d(TAG, "lost connection to the service");
             }
         }
     };
     
     private OnClickListener locButtonListener = new OnClickListener() {
         public void onClick(View v) {
             try {
                 // get an auth intent from the service
                 Intent i = mWaveService.getAuthorizationIntent(LOC_RECIPE_ID, API_KEY);
             
                 // then run it looking for a result
                 try {
                     startActivityForResult(i, REQUEST_CODE_AUTH);
                 } catch (ActivityNotFoundException anfe) {
                     anfe.printStackTrace();
                     Toast.makeText(WaveLogger.this, "Error launching authorization UI", Toast.LENGTH_SHORT).show();
                 }
             } catch (RemoteException e) {
                 Log.d(TAG, "lost connection to the service");
             }
         }
     };
     
     private OnClickListener startButtonListener = new OnClickListener() {
         public void onClick(View v) {
             // we should already be authorized for at least one recipe for
             // this button to be enabled
             
             ArrayList<String> requestedIds = new ArrayList<String>(2);
             
             try {
                 if (mWaveService.isAuthorized(API_KEY, ACCEL_RECIPE_ID)) {
                     requestedIds.add(ACCEL_RECIPE_ID);
                 }
                 if (mWaveService.isAuthorized(API_KEY, LOC_RECIPE_ID)) {
                     requestedIds.add(LOC_RECIPE_ID);
                 }
             } catch (RemoteException e) {
                 Log.d(TAG, "lost connection to the service");
             }
             
             Intent waveLoggerServiceIntent = new Intent(Intent.ACTION_MAIN);
             waveLoggerServiceIntent.setClass(WaveLogger.this, WaveLoggerService.class);
             waveLoggerServiceIntent.putStringArrayListExtra(WaveLoggerService.RECIPE_IDS_EXTRA, requestedIds);
             
             startService(waveLoggerServiceIntent);
         }
     };
     
     private OnClickListener stopButtonListener = new OnClickListener() {
         public void onClick(View v) {
             Intent waveLoggerServiceIntent = new Intent(Intent.ACTION_MAIN);
             waveLoggerServiceIntent.setClass(WaveLogger.this, WaveLoggerService.class);
             stopService(waveLoggerServiceIntent);
         }
     };
     
     /**
      * Create the menu, which allows the user to export and empty the app's
      * data storage database
      */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.options_menu, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch  (item.getItemId()) {
             case R.id.export_database:
                 return exportDatabase();
             case R.id.empty_database:
                 return emptyDatabase();
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
     
     protected boolean exportDatabase() {
         File f = databaseHelper.writeContentsToSdCard();
         
         String message;
         if (f != null) {
             message = "Data has been exported to "+f.getName();
         } else {
             message = "Errors were encountered during export";
         }
         AlertDialog.Builder builder = new AlertDialog.Builder(WaveLogger.this);
         builder.setMessage(message)
                .setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // nothing
                    }
                });
         AlertDialog alert = builder.show();
         return (f != null);
     }
     
     protected boolean emptyDatabase() {
         AlertDialog.Builder builder = new AlertDialog.Builder(WaveLogger.this);
         builder.setMessage("Are you sure you want to delete all previously logged data?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        databaseHelper.emptyDatabase();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d(TAG, "emptyDatabase() cancelled.");
                    }
                });
         AlertDialog alert = builder.show();
         return true;
     }
     
     /**
      * ServiceConnection subclass
      */
     private ServiceConnection mConnection = new ServiceConnection() {
         public void onServiceConnected(ComponentName className, IBinder service) {
             mWaveService = IWaveServicePublic.Stub.asInterface(service);
             afterBind();
         }
         
         public void onServiceDisconnected(ComponentName className) {
             mWaveService = null;
         }
     };
 }
