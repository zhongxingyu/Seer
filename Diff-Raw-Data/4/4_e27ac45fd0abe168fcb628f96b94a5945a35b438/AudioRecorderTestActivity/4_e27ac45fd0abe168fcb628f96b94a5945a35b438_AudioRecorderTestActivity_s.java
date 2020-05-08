 // AudioRecorderTest.java
 // ----------------------
 
 // this is an artifact from our Alpha release, and this code will likely end up
 // somewhere else, particularly in whatever recording module we develop.
 
 /*
  * The application needs to have the permission to write to external storage
  * if the output file is written to the external storage, and also the
  * permission to record audio. These permissions must be set in the
  * application's AndroidManifest.xml file, with something like:
  *
  * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
  * <uses-permission android:name="android.permission.RECORD_AUDIO" />
  *
  */
 package com.teamluper.luper;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.MediaRecorder;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.*;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.googlecode.androidannotations.annotations.Background;
 import com.googlecode.androidannotations.annotations.EActivity;
 
 import java.io.File;
 import java.io.IOException;
 
 @EActivity
 public class AudioRecorderTestActivity extends SherlockActivity
 {
     private static final String LOG_TAG = "AudioRecorderTest";
     private static String mFileName = null;
 
     private RecordButton mRecordButton = null;
     private MediaRecorder mRecorder = null;
 
     private PlayButton   mPlayButton = null;
     private MediaPlayer   mPlayer = null;
 
     private Button mBrowseButton = null;
     private TextView fileSelected;
     private Button mAddToTrackButton = null;
 
     private playTrackButton mPlayTrackButton = null;
 
     private AudioManager audioManager;
 
     private int MediaFetchResultCode = 11;
 
     private Track playBackTest = new Track ();
 
     private void onRecord(boolean start) {
 
         if (start) {
             startRecording();
         } else {
             stopRecording();
         }
     }
 
     private void onPlay(boolean start) {
         if (start) {
             startPlaying();
         } else {
             stopPlaying();
         }
     }
 
     private void onPlayTrack(boolean start) {
         if (start) {
             startPlayingTrack();
         } else {
             stopPlayingTrack();
         }
     }
 
     private void startPlaying() {

         mPlayer = new MediaPlayer();
         try {
             mPlayer.setDataSource(mFileName);
             mPlayer.prepare();
             mPlayer.start();
         } catch (IOException e) {
             Log.e(LOG_TAG, "prepare() failed1");
         }
     }
 
     private void stopPlaying() {
       if(mPlayer != null) {
         mPlayer.release();
         mPlayer = null;
       }
     }
 
     @Background
     public void startPlayingTrack() {
 
     	int i=0;
     	while(playBackTest!=null && i!=playBackTest.size())
     	{
 	        mPlayer = new MediaPlayer();
 	        mFileName=playBackTest.clips.get(i).name;
 	        try
 	        {
 	            mPlayer.setDataSource(mFileName);
 	            mPlayer.prepare();
 	            Thread.sleep(playBackTest.clips.get(i).getDuration());
 	            mPlayer.start();
 	            i++;
 	        } catch (Exception e) {
 	        	//handle interrupted exceptions in a different way
 	            Log.e(LOG_TAG, "prepare() failed1");
 	        }
     	}
     }
     
     //Used to initialize a playhead from a specific point - literally just a sleep call on the thread in question
     public void playFrom(int ms)
     {
     		try {
 				Thread.sleep(ms);
 			} catch (Exception e) {
 				//handle interrupted exceptions in a different way
 				Log.e(LOG_TAG, "sleep() failed1");
 			}
     }
 
     private void stopPlayingTrack() {
       if(mPlayer != null) {
         mPlayer.release();
         mPlayer = null;
       }
     }
 
     private void startRecording() {
     	//Sets the name of the file when you start recording as opposed to when you click "Audio Record Test" from the main screen
         mFileName = Environment.getExternalStorageDirectory()+"/LuperApp/Clips";
         mFileName += "/clip_" + System.currentTimeMillis() +".3gp";
 
         mRecorder = new MediaRecorder();
         //System.out.println("here");
         mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
         //System.out.println("and here");
         mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
         mRecorder.setOutputFile(mFileName);
 
         mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
 
         try {
             mRecorder.prepare();
         } catch (IOException e) {
         	System.out.println(e.toString());
             Log.e(LOG_TAG, "prepare() failed2");
         }
 
         mRecorder.start();
     }
 
     private void stopRecording() {
         mRecorder.stop();
         mRecorder.release();
 
         Clip newClip = new Clip(mFileName);
 
         try {
 			newClip.getDuration();
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 
         //playBackTest.putClip(newClip);
         //alertDialog("Clip Created! The clip's length is: " + newClip.duration + "(ms). The tracks size is " + playBackTest.size() + " and it's name in the track is ..." + playBackTest.clips.get(0).name);
 
         DialogFactory.alert(this,"Clip Created! The clip's length is: "
           + newClip.duration + "(ms) the clip's name is: " + newClip.name);
 
         fileSelected.setText(mFileName);
         mRecorder = null;
     }
 
     class RecordButton extends Button {
         boolean mStartRecording = true;
 
         OnClickListener clicker = new OnClickListener() {
             public void onClick(View v) {
                 onRecord(mStartRecording);
                 if (mStartRecording) {
                     setText("Stop recording");
                 } else {
                     setText("Start recording");
                 }
                 mStartRecording = !mStartRecording;
             }
         };
 
         public RecordButton(Context ctx) {
             super(ctx);
             setText("Start recording");
             setOnClickListener(clicker);
         }
     }
 
     class PlayButton extends Button {
         boolean mStartPlaying = true;
 
         OnClickListener clicker = new OnClickListener() {
             public void onClick(View v) {
                 onPlay(mStartPlaying);
                 if (mStartPlaying) {
                     setText("Stop playing");
                 } else {
                     setText("Start playing");
                 }
                 mStartPlaying = !mStartPlaying;
             }
         };
 
         public PlayButton(Context ctx) {
             super(ctx);
             setText("Start playing");
             setOnClickListener(clicker);
         }
     }
     class playTrackButton extends Button {
         boolean mStartPlayingTrack = true;
 
         OnClickListener clicker = new OnClickListener() {
             public void onClick(View v) {
                 onPlayTrack(mStartPlayingTrack);
                 if (mStartPlayingTrack) {
                     setText("Stop Track");
                 } else {
                     setText("Start Track");
                 }
                 mStartPlayingTrack = !mStartPlayingTrack;
             }
         };
 
         public playTrackButton(Context ctx) {
             super(ctx);
             setText("Start Track");
             setOnClickListener(clicker);
         }
     }
  
     public AudioRecorderTestActivity() {
         mFileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/LuperApp/";
         //System.out.println(mFileName);
         mFileName += System.currentTimeMillis() + ".3gp";
     }
 
     @Override
     public void onCreate(Bundle icicle) {
         super.onCreate(icicle);
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
         //base will hold the other linear layouts that hold buttons and whatnot
         LinearLayout base = new LinearLayout(this);
         //sets the orientation to be vertical
         base.setOrientation(LinearLayout.VERTICAL);
 
         //ll holds the start recording, play, and browse buttons
         LinearLayout ll = new LinearLayout(this);
         //create and add the record button
         mRecordButton = new RecordButton(this);
         ll.addView(mRecordButton,
             new LinearLayout.LayoutParams(
                 ViewGroup.LayoutParams.WRAP_CONTENT,
                 ViewGroup.LayoutParams.WRAP_CONTENT,
                 0));
         //create and add the play button
         mPlayButton = new PlayButton(this);
         ll.addView(mPlayButton,
             new LinearLayout.LayoutParams(
                 ViewGroup.LayoutParams.WRAP_CONTENT,
                 ViewGroup.LayoutParams.WRAP_CONTENT,
                 0));
 
         //ll2 holds the textbox to display current file to play
         LinearLayout ll2 = new LinearLayout(this);
         //create and add the browse button, set the button text to "Browse"
         mBrowseButton = new Button(this);
         mBrowseButton.setText("Browse");
         ll2.addView(mBrowseButton,
                 new LinearLayout.LayoutParams(
                         ViewGroup.LayoutParams.WRAP_CONTENT,
                         ViewGroup.LayoutParams.WRAP_CONTENT,
                         0));
         //create the button to add a clip to the track, set the button text to
         //"Add to Track", then add the button to ll2
         mAddToTrackButton = new Button(this);
         mAddToTrackButton.setText("Add to Track");
         ll2.addView(mAddToTrackButton,
                 new LinearLayout.LayoutParams(
                         ViewGroup.LayoutParams.WRAP_CONTENT,
                         ViewGroup.LayoutParams.WRAP_CONTENT,
                         0));
         //create and add the file selected text field, then set the initial text
         //to be "Select a File"
         fileSelected = new AutoCompleteTextView(this);
         fileSelected.setHint("Select a File");
         ll2.addView(fileSelected,
                 new LinearLayout.LayoutParams(
                         ViewGroup.LayoutParams.FILL_PARENT,
                         ViewGroup.LayoutParams.FILL_PARENT,
                         0));
 
         //ll3 will hold the volume bar
         LinearLayout ll3 = new LinearLayout(this);
         //create and add the volume bar
         SeekBar volBar = new SeekBar(this);
         ll3.addView(volBar,
                 new LinearLayout.LayoutParams(
                         ViewGroup.LayoutParams.MATCH_PARENT,
                         ViewGroup.LayoutParams.WRAP_CONTENT,
                         0));
 
         //ll4 holds the textbox to display current file to play
         //create and add the play track button
         LinearLayout ll4 = new LinearLayout(this);
         mPlayTrackButton = new playTrackButton(this);
         ll4.addView(mPlayTrackButton,
                 new LinearLayout.LayoutParams(
                         ViewGroup.LayoutParams.WRAP_CONTENT,
                         ViewGroup.LayoutParams.WRAP_CONTENT,
                         0));
 
         //add ll to base
         base.addView(ll,
                 new LinearLayout.LayoutParams(
                         ViewGroup.LayoutParams.WRAP_CONTENT,
                         ViewGroup.LayoutParams.WRAP_CONTENT,
                         0));
         //add ll2 to base
         base.addView(ll2,
                 new LinearLayout.LayoutParams(
                         ViewGroup.LayoutParams.MATCH_PARENT,
                         ViewGroup.LayoutParams.WRAP_CONTENT,
                         0));
         //add ll4 to base
         base.addView(ll4,
                 new LinearLayout.LayoutParams(
                         ViewGroup.LayoutParams.MATCH_PARENT,
                         ViewGroup.LayoutParams.WRAP_CONTENT,
                         0));
         //add ll3 to base
         base.addView(ll3,
                 new LinearLayout.LayoutParams(
                         ViewGroup.LayoutParams.MATCH_PARENT,
                         ViewGroup.LayoutParams.WRAP_CONTENT,
                         0));
         //switched ll4 and ll3 so that the volume bar would be the bottom most thing in the view
         //sets the context view to be base
         setContentView(base);
 
         //starts a new activity/intent that activates the FileSelector activity
         mBrowseButton.setOnClickListener(new View.OnClickListener() {
         	   @Override
         	   public void onClick(View v) {
         	    Intent intent = new Intent(AudioRecorderTestActivity.this, FileSelectorActivity.class);
         	    AudioRecorderTestActivity.this.startActivityForResult(intent, MediaFetchResultCode);
         	   }
         	  });
 
         mAddToTrackButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if(mFileName != null){
 					Clip newClip = new Clip(mFileName);
 					playBackTest.putClip(newClip);
 				}
 			}
 		});
 
         //what we need to get the volume bar to work
         audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
         int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
         int curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
         volBar.setMax(maxVolume);
         volBar.setProgress(curVolume);
         volBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
 			public void onStopTrackingTouch(SeekBar seekBar) {}
 			public void onStartTrackingTouch(SeekBar seekBar) {}
 
 			public void onProgressChanged(SeekBar seekBar, int progress,
 					boolean fromUser) {
 				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
 			}
 		});
     }
 
     //returns the correct file when you select one from the file browser
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     	  super.onActivityResult(requestCode, resultCode, data);
     	  if (requestCode == MediaFetchResultCode) {
     	   if (resultCode == RESULT_OK) {
     	    mFileName = data.getStringExtra("fileChosen");
     	    File file = new File(mFileName);
     	    fileSelected.setText(file.getName());
     	    Toast.makeText(getApplicationContext(),
     	      "You have selected:" + file.getName(), Toast.LENGTH_LONG).show();
     	   }
     	  }
     	 }
 
     @Override
     public void onPause() {
         super.onPause();
         if (mRecorder != null) {
             mRecorder.release();
             mRecorder = null;
         }
 
         if (mPlayer != null) {
             mPlayer.release();
             mPlayer = null;
         }
     }
 }
