 package com.patdivillyfitness.runcoach.activity;
 
 import java.util.Locale;
 
 import nl.sogeti.android.gpstracker.actions.ControlTracking;
 import nl.sogeti.android.gpstracker.db.GPStracking.Tracks;
 import nl.sogeti.android.gpstracker.util.Constants;
 import nl.sogeti.android.gpstracker.viewer.LoggerMap;
 import android.app.AlertDialog;
 import android.content.ContentResolver;
 import android.content.ContentUris;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.ContentObserver;
 import android.database.Cursor;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.provider.Settings;
 import android.speech.tts.TextToSpeech;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.TextView;
 
import com.google.android.maps.GeoPoint;
 import com.patdivillyfitness.runcoach.R;
 
 public class RecordActivity extends LoggerMap implements TextToSpeech.OnInitListener
 {
    private boolean isGPSEnabled;
    private LocationManager locationManager;
    private TextToSpeech tts;
    private TextView recordingTextView;
    private static final String TAG = "PDFRun";
    private boolean firstLocationFound=false;
    private boolean started=false;
    private static final String INSTANCE_TIME = "time";
    private static final String INSTANCE_SPEED = "speed";
    private static final String INSTANCE_DISTANCE = "distance";
 
    @Override
    protected void onCreate(Bundle load)
    {
       this.layout = R.layout.activity_record;
       locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
       tts = new TextToSpeech(this, this);
       recordingTextView = (TextView) findViewById(R.id.recording);
       super.onCreate(load);
       mServiceConnected = new Runnable()
       {
          @Override
          public void run()
          {
             updateBlankingBehavior();
             loggingState=mLoggerServiceManager.getLoggingState();
             Log.d("PDFRun", "Record logging state: " + mLoggerServiceManager.getLoggingState());
             if (recordingTextView==null)
                recordingTextView = (TextView) findViewById(R.id.recording);
             switch(loggingState){
                case Constants.STOPPED:
                   recordingTextView.setText("Stopped");
                   RecordActivity.this.setTitle(RecordActivity.this.getString(R.string.application_name));
                   break;
                case Constants.LOGGING:
                   if (started)
                      recordingTextView.setText("Waiting For Satelites");
                   else
                      recordingTextView.setText("Recording");
                   break;
                case Constants.PAUSED:
                   recordingTextView.setText("Paused");
                   break;
                default:
                   Log.d(TAG, "unknown logging state");
                   recordingTextView.setText("No");
                   break;
             }
          }
       };
       mSegmentWaypointsObserver = new ContentObserver(new Handler())
       {
          @Override
          public void onChange(boolean selfUpdate)
          {
             if (!selfUpdate)
             {
                Log.d(TAG, "RA update track numbers");
                RecordActivity.this.updateTrackNumbers();            
             }
             else
             {
                Log.w(TAG, "mSegmentWaypointsObserver skipping change on " + mLastSegment);
             }
          }
       };
    }
 
    @Override
    protected void updateTrackNumbers()
    {
       if(started&&!firstLocationFound){
          Log.d(TAG, "First Location Found");
          recordingTextView.setText("Recording");
          speakOut();
       }
       super.updateTrackNumbers();
    }
    public void trackRun(View view)
    {
       checkGPSAndOpenControls();
    }
 
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
       super.onActivityResult(requestCode, resultCode, intent);
       if (requestCode == MENU_TRACKING)
       {
          if (resultCode == RESULT_OK)
          {
             if (intent.getBooleanExtra(com.patdivillyfitness.runcoach.Constants.TRACKING_STOPPED, false))
             {
                Log.d("PDFRun", "Stopped tracking run");
                Uri trackUri = ContentUris.withAppendedId(Tracks.CONTENT_URI, mTrackId);
                Intent detailsIntent = new Intent(this, RunDetailsActivity.class);
                detailsIntent.setData(trackUri);
                startActivity(detailsIntent);
             } else if (intent.getBooleanExtra(com.patdivillyfitness.runcoach.Constants.TRACKING_STARTED, false)){
                started=true;
             }
          }
       }
       else if (requestCode == 100)
       {
         checkGPSAndOpenControls();
       }
    }
 
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
       return true;
    }
 
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
       return true;
    }
 
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
       return true;
    }
 
    @Override
    protected void updateTitleBar()
    {
       ContentResolver resolver = this.getContentResolver();
       Cursor trackCursor = null;
       try
       {
          trackCursor = resolver.query(ContentUris.withAppendedId(Tracks.CONTENT_URI, this.mTrackId), new String[] { Tracks.NAME }, null, null, null);
          if (trackCursor != null && trackCursor.moveToLast())
          {
             String trackName = trackCursor.getString(0);
             this.setTitle(trackName);
          }
       }
       finally
       {
          if (trackCursor != null)
          {
             trackCursor.close();
          }
       }
    }
 
    @Override
    public boolean onKeyDown(int keycode, KeyEvent e)
    {
       switch (keycode)
       {
          case KeyEvent.KEYCODE_MENU:
             checkGPSAndOpenControls();
             return true;
       }
 
       return super.onKeyDown(keycode, e);
    }
 
    @Override
    public void onDestroy()
    {
       if (tts != null)
       {
          tts.stop();
          tts.shutdown();
       }
       super.onDestroy();
    }
 
    @Override
    public void onInit(int status)
    {
 
       if (status == TextToSpeech.SUCCESS)
       {
 
          int result = tts.setLanguage(Locale.UK);
 
          if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
          {
             Log.e("TTS", "This Language is not supported");
          }
 //         else
 //         {
 //            speakOut();
 //         }
 
       }
       else
       {
          Log.e("TTS", "Initilization Failed!");
       }
 
    }
 
    private void speakOut()
    {
       tts.speak("Go", TextToSpeech.QUEUE_FLUSH, null);
    }
 
    private void checkGPSAndOpenControls()
    {
       try
       {
          isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
       }
       catch (Exception ex)
       {
       }
 
       if (!isGPSEnabled)
       {
          AlertDialog.Builder builder = new AlertDialog.Builder(this);
          builder.setMessage("Your GPS module is disabled. Would you like to enable it ?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener()
             {
                public void onClick(DialogInterface dialog, int id)
                {
 
                   //Sent user to GPS settings screen
                   final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                   startActivityForResult(intent, 100);
 
                   dialog.dismiss();
 
                }
             }).setNegativeButton("No", new DialogInterface.OnClickListener()
             {
                public void onClick(DialogInterface dialog, int id)
                {
                   dialog.cancel();
                }
             });
          AlertDialog alert = builder.create();
          alert.show();
       }
       else
       {
          Intent intent = new Intent(this, ControlTracking.class);
          startActivityForResult(intent, MENU_TRACKING);
       }
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle load)
    {
       if (load != null)
       {
          super.onRestoreInstanceState(load);
       }
       if (load != null) // 1st method: track from a previous instance of this activity
       {
          if (load.containsKey(INSTANCE_SPEED))
          {
             mLastGPSSpeedView.setText(load.getString(INSTANCE_SPEED));
          }
          if (load.containsKey(INSTANCE_DISTANCE))
          {
             mDistanceView.setText(load.getString(INSTANCE_DISTANCE));
          }
          if (load.containsKey(INSTANCE_TIME))
          {
             mElapsedTimeView.setText(load.getString(INSTANCE_TIME));
          }
       }
    }
 
    @Override
    protected void onSaveInstanceState(Bundle save)
    {
       super.onSaveInstanceState(save);
       save.putString(INSTANCE_SPEED, mCurrentSpeed);
       save.putString(INSTANCE_DISTANCE, mCurrentDistance);
       save.putString(INSTANCE_TIME, mElapsedTime);
    }
 
 }
