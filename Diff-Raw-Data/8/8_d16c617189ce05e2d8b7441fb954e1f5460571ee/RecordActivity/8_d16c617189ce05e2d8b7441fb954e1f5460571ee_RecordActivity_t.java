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
 import android.os.SystemClock;
 import android.provider.Settings;
 import android.speech.tts.TextToSpeech;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.Chronometer;
 import android.widget.TextView;
 
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.patdivillyfitness.runcoach.R;
 
 public class RecordActivity extends LoggerMap implements TextToSpeech.OnInitListener
 {
    private boolean isGPSEnabled;
    private LocationManager locationManager;
    private TextToSpeech tts;
    private TextView recordingTextView;
    private Button startStopBtn;
    private Button pauseResumeBtn;
    private static final String TAG = "PDFRun";
    private boolean firstLocationFound = false;
    private boolean started = false;
    private boolean startBtn = true;
    private boolean pauseBtn = true;
    private static final String INSTANCE_TIME = "time";
    private static final String INSTANCE_SPEED = "speed";
    private static final String INSTANCE_DISTANCE = "distance";
 //   private Chronometer myChronometer;
 
    @Override
    protected void onCreate(Bundle load)
    {
       this.layout = R.layout.activity_record;
       locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
       tts = new TextToSpeech(this, this);
       super.onCreate(load);
       recordingTextView = (TextView) findViewById(R.id.recording);
       startStopBtn = (Button) findViewById(R.id.startStopBtn);
       pauseResumeBtn = (Button) findViewById(R.id.pauseResumeBtn);
 //      myChronometer = (Chronometer) findViewById(R.id.chronometer);
 
       mServiceConnected = new Runnable()
          {
             @Override
             public void run()
             {
                updateBlankingBehavior();
                loggingState = mLoggerServiceManager.getLoggingState();               
                Log.d("PDFRun", "Record logging state: " + mLoggerServiceManager.getLoggingState());
                switch (loggingState)
                {
                   case Constants.STOPPED:
                      recordingTextView.setText("Stopped");
                      RecordActivity.this.setTitle(RecordActivity.this.getString(R.string.application_name));
                      pauseResumeBtn.setText("Pause");
                      pauseResumeBtn.setEnabled(false);
                      pauseBtn = true;
                      startStopBtn.setText("Start");
                      startBtn = true;
 //                     myChronometer.stop();
                      break;
                   case Constants.LOGGING:
                      if (started)
                         recordingTextView.setText("Searching, walk slowly until recording starts.");
                      else{
                         recordingTextView.setText("Recording");                                              
                      }
                      
                      startStopBtn.setText("Stop");
                      startBtn = false;
                      pauseResumeBtn.setText("Pause");
                      pauseResumeBtn.setEnabled(true);
                      pauseBtn = true;
                      break;
                   case Constants.PAUSED:
                      recordingTextView.setText("Paused");
                      pauseResumeBtn.setText("Resume");
                      pauseBtn = false;
                      startStopBtn.setText("Stop");
                      startBtn = false;
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
       if (started && !firstLocationFound)
       {
          Log.d(TAG, "First Location Found");
          recordingTextView.setText("Recording");
 //         myChronometer.start();
 //         myChronometer.setBase(SystemClock.elapsedRealtime());  
          speakOut();
          firstLocationFound = true;
       }
       super.updateTrackNumbers();
    }
 
    public void trackRun(View view)
    {
       checkGPSAndOpenControls();
    }
 
    public void startStopRun(View view)
    {
       Log.d(TAG, "startStopRun");
       if (startBtn)
          checkGPSAndControlRecording(com.patdivillyfitness.runcoach.Constants.START);
       else
          checkGPSAndControlRecording(com.patdivillyfitness.runcoach.Constants.STOP);
    }
 
    public void pauseResumeRun(View view)
    {
       Log.d(TAG, "pauseResumeRun");
       if (pauseBtn)
          checkGPSAndControlRecording(com.patdivillyfitness.runcoach.Constants.PAUSE);
       else
          checkGPSAndControlRecording(com.patdivillyfitness.runcoach.Constants.RESUME);
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
                detailsIntent.putExtra(com.patdivillyfitness.runcoach.Constants.FROM_RECORDING_EXTRA, true);
                startActivity(detailsIntent);
             }
             else if (intent.getBooleanExtra(com.patdivillyfitness.runcoach.Constants.TRACKING_STARTED, false))
             {
                started = true;
             }
          }
       }
       else if (requestCode == 100)
       {
          //checkGPSAndOpenControls();
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
 
    //   @Override
    //   public boolean onKeyDown(int keycode, KeyEvent e)
    //   {
    //      switch (keycode)
    //      {
    //         case KeyEvent.KEYCODE_MENU:
    //            checkGPSAndOpenControls();
    //            return true;
    //      }
    //
    //      return super.onKeyDown(keycode, e);
    //   }
 
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
          alertGPSDisabled();
       }
       else
       {
          Intent intent = new Intent(this, ControlTracking.class);
          startActivityForResult(intent, MENU_TRACKING);
       }
    }
 
    private void checkGPSAndControlRecording(int action)
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
          alertGPSDisabled();
       }
       else
       {
          Intent intent = new Intent(this, ControlTracking.class);
          intent.putExtra(com.patdivillyfitness.runcoach.Constants.CONTROL_EXTRA, action);
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
      if (load != null)
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
 
    private void alertGPSDisabled()
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
 
    @Override
    protected void onSaveInstanceState(Bundle save)
    {
       super.onSaveInstanceState(save);
       save.putString(INSTANCE_SPEED, mCurrentSpeed);
       save.putString(INSTANCE_DISTANCE, mCurrentDistance);
       save.putString(INSTANCE_TIME, mElapsedTime);
    }
 
 }
