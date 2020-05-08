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
 import android.support.v4.app.NavUtils;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.Chronometer;
 import android.widget.TextView;
 
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.patdivillyfitness.runcoach.PDFChronometer;
 import com.patdivillyfitness.runcoach.R;
 
 public class RecordActivity extends LoggerMap
 {
    private boolean isGPSEnabled;
    private LocationManager locationManager;
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
 
    private PDFChronometer myChronometer;
 
    @Override
    protected void onCreate(Bundle load)
    {
       this.layout = R.layout.activity_record;
       locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
       super.onCreate(load);
       recordingTextView = (TextView) findViewById(R.id.recording);
       startStopBtn = (Button) findViewById(R.id.startStopBtn);
       pauseResumeBtn = (Button) findViewById(R.id.pauseResumeBtn);
       myChronometer = (PDFChronometer) findViewById(R.id.chronometer);
 
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
                      recordingTextView.setText(RecordActivity.this.getString(R.string.recording_stopped));
                      RecordActivity.this.setTitle(RecordActivity.this.getString(R.string.application_name));
                      pauseResumeBtn.setText(RecordActivity.this.getString(R.string.pause));
                      pauseResumeBtn.setEnabled(false);
                      pauseBtn = true;
                      startStopBtn.setText(RecordActivity.this.getString(R.string.start));
                      startBtn = true;
                      //                     myChronometer.stop();
                      break;
                   case Constants.LOGGING:
                      if (started)
                         recordingTextView.setText(RecordActivity.this.getString(R.string.recording_searching));
                      else
                      {
                         recordingTextView.setText(RecordActivity.this.getString(R.string.recording));
                      }
 
                      startStopBtn.setText(RecordActivity.this.getString(R.string.stop));
                      startBtn = false;
                      pauseResumeBtn.setText(RecordActivity.this.getString(R.string.pause));
                      pauseResumeBtn.setEnabled(true);
                      pauseBtn = true;
                      break;
                   case Constants.PAUSED:
                      recordingTextView.setText(RecordActivity.this.getString(R.string.recording_paused));
                      pauseResumeBtn.setText(RecordActivity.this.getString(R.string.resume));
                      pauseBtn = false;
                      startStopBtn.setText(RecordActivity.this.getString(R.string.stop));
                      startBtn = false;
                      break;
                   default:
                      Log.d(TAG, "unknown logging state");
                      recordingTextView.setText(RecordActivity.this.getString(R.string.no));
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
       getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
 
    @Override
    protected void updateTrackNumbers()
    {
       if (started && !firstLocationFound)
       {
          Log.d(TAG, "First Location Found");
          recordingTextView.setText(RecordActivity.this.getString(R.string.recording));
          myChronometer.setMsElapsed(0);
          myChronometer.start();  
          firstLocationFound = true;
      } else
          myChronometer.setMsElapsed(mLoggerServiceManager.getElapsedTime());
       super.updateTrackNumbers();
    }
 
    public void trackRun(View view)
    {
       checkGPSAndOpenControls();
    }
 
    public void startStopRun(View view)
    {
       Log.d(TAG, "startStopRun");
       firstLocationFound = false;
       started = false;
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
       switch (item.getItemId())
       {
          case android.R.id.home:
             // This ID represents the Home or Up button. In the case of this
             // activity, the Up button is shown. Use NavUtils to allow users
             // to navigate up one level in the application structure. For
             // more details, see the Navigation pattern on Android Design:
             //
             // http://developer.android.com/design/patterns/navigation.html#up-vs-back
             //
             NavUtils.navigateUpFromSameTask(this);
             return true;
       }
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
          if(action==com.patdivillyfitness.runcoach.Constants.STOP){
             myChronometer.setMsElapsed(0);
             myChronometer.stop();
             myChronometer.setText(R.string.zero_time);
             mDistanceView.setText(R.string.empty_detail);
             mLastGPSSpeedView.setText(R.string.empty_detail);
             mElapsedTimeView.setText(R.string.empty_time);
          }
       }
    }
 
    @Override
    protected void onRestoreInstanceState(Bundle load)
    {
       Log.d(TAG, "onRestoreInstanceState");
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
       builder.setMessage(RecordActivity.this.getString(R.string.gps_disabled_text)).setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener()
          {
             public void onClick(DialogInterface dialog, int id)
             {
 
                //Sent user to GPS settings screen
                final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent, 100);
 
                dialog.dismiss();
 
             }
          }).setNegativeButton(RecordActivity.this.getString(R.string.no), new DialogInterface.OnClickListener()
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
