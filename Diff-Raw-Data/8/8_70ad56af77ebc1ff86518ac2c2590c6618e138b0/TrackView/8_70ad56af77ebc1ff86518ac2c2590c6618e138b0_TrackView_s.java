 package com.teamluper.luper;
 /*
  * Copyright (C) 2011 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.graphics.Shader;
 import android.graphics.drawable.BitmapDrawable;
 import android.media.MediaPlayer;
 import android.media.MediaRecorder;
 import android.os.Environment;
 import android.os.Looper;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.*;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Random;
 
 import com.androidlearner.widget.DragThing;
 import com.androidlearner.widget.DragThingPlayhead;
 import com.googlecode.androidannotations.annotations.Background;
 import com.googlecode.androidannotations.annotations.EView;
 import com.googlecode.androidannotations.annotations.UiThread;
 import com.teamluper.luper.AudioRecorderTestActivity.PlayButton;
 import com.teamluper.luper.AudioRecorderTestActivity.playTrackButton;
 import android.os.Bundle;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.androidlearner.widget.DragThingPlayhead;
 import com.googlecode.androidannotations.annotations.EActivity;
 
 /**
  * A custom view for a color chip for an event that can be drawn differently
  * according to the event's status.
  *
  */
 @EView
 public class TrackView extends RelativeLayout {
   private static final String LOG_TAG = "TrackView";
   public static final float PIXELS_PER_MILLISECOND = 0.3f;
   public static final int LEFT_MARGIN = 100;
 
   DragThing deMovingTxt;
   int [] paramz;
 
   private String lastRecordedFileName = null;
   private AudioFile lastRecordedFile = null;
 
   private RecordButton mRecordButton = null;
   private MediaRecorder mRecorder = null;
 
   private PlayButton   mPlayButton = null;
   private MediaPlayer   mPlayer = null;
 
   private Button mBrowseButton;
   private TextView fileSelected;
   private DragThingPlayhead playhead;
   private int MediaFetchResultCode = 11;
 
   private SQLiteDataSource dataSource;
 
   private Clip newClip;
   private Clip preparedClip = null;
   Track associated;
   public int startTimeSetter;
 
   private LuperProjectEditorActivity editorActivity;
 
   //the track that will be associated with this TrackView
   //Track associated;
 
   //constructor
   public TrackView(LuperProjectEditorActivity context, Track track, SQLiteDataSource dataSource) {
     super(context);
     editorActivity = context;
     playhead = (DragThingPlayhead)editorActivity.findViewById(1337);
     associated = track;
     this.dataSource = dataSource;
     deMovingTxt = (DragThing) findViewById(R.id.detext);
     init();
   }
 
 
   //set a click listener for the buttons that will activate promptDialog() when clicked
   OnClickListener addClipClicker = new OnClickListener(){
     public void onClick(View v){
       addClipPromptDialog(); // later on this will be the time corresponding to where in the empty timeline we touched.
       // TODO
     }
   };
   //TODO: Implement the listener for the play button, so we can play a track
   OnClickListener playClicker = new OnClickListener(){
     public void onClick(View v){
       startPlayingTrack(); //need track startPlayback but track class + audio hook-up not working yet; this does work though
     }
   };
 
   //listener for the browse button in the record or browse alertdialog
   OnClickListener browseClicker = new OnClickListener(){
     public void onClick(View v){
       browsePromptDialog();
     }
   };
 
   public void render() {
     init();
   }
 
   @UiThread
   public void init(){
     mPlayer = new MediaPlayer();
     //LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
     //        LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
     //layoutParams.setMargins(5, 0, 5, 0);
     //this.setLayoutParams(layoutParams);
 
 //		add a linear layout to the left side that will have a playtrack button
 //		as well as a button to add a clip to this track
 
     if(this.getChildCount() != 0) this.removeAllViews();
 
     LinearLayout trackControl = new LinearLayout(this.getContext());
     trackControl.setOrientation(LinearLayout.VERTICAL);
 
     //wai doesnt this werrrkk???
     //deMovingTxt = (DragThing) findViewById(R.id.detext);
     //this.addView(deMovingTxt);
 
 //		create the addClipButton then set its image to add and add it to the trackControl
 
     ImageButton addClipButton = new ImageButton(this.getContext());
     addClipButton.setImageResource(R.drawable.add);
     addClipButton.setOnClickListener(addClipClicker);
     addClipButton.setBackgroundColor(Color.parseColor("#f5f5f5"));
     trackControl.addView(addClipButton);
 
 //		create the playButton then set its image to play and add it to the trackControl
     ImageButton playButton = new ImageButton(this.getContext());
     playButton.setImageResource(R.drawable.play);
     playButton.setOnClickListener(playClicker);
     playButton.setBackgroundColor(Color.parseColor("#f5f5f5"));
     trackControl.addView(playButton);
 
     //trackControl.setBackgroundColor(Color.parseColor("#000000"));
 
 
     this.addView(trackControl);
 //		testing...
 
     //Clip clip1 = new Clip(); clip1.begin = 100; clip1.end = 4000; clip1.duration = 3900;
     //Clip clip2 = new Clip(); clip2.begin = 0; clip2.end = 450; clip2.duration = 450;
 
     ColorChipButton chip;
     //this.associated.putClip(clip1);
     //this.associated.putClip(clip2);
     for(int i = 0; i < this.associated.clips.size(); i++){
 
       //System.out.println("Here " + this.associated.getClips().get(i).begin);
       Clip c = this.associated.getClips().get(i);
       chip = new ColorChipButton(this.getContext(), c);
       c.addAssociatedView(chip);
       System.out.println("Chips x pos " + chip.associated.begin);
       this.addView(chip);
       LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
         ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
       this.setLayoutParams(params);
       Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.line);
       BitmapDrawable bitmapDrawable = new BitmapDrawable(bmp);
       bitmapDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
       //this.setBackgroundDrawable(bitmapDrawable);
       //this.setBackgroundColor(Color.parseColor("#e2dfd8"));
       //this.setBackgroundColor(Color.TRANSPARENT);
       //this.addView(deMovingTxt);
     }
   }
 
 
   //Creates the dialog for record or browse, for adding clips to tracks,
   public void addClipPromptDialog(){
     //our custom layout for inside the dialog
     LinearLayout custom = new LinearLayout(this.getContext());
     custom.setOrientation(LinearLayout.VERTICAL);
 
     LinearLayout ll = new LinearLayout(this.getContext());
     mRecordButton = new RecordButton(this.getContext());
     ll.addView(mRecordButton,
         new LinearLayout.LayoutParams(
             ViewGroup.LayoutParams.WRAP_CONTENT,
             ViewGroup.LayoutParams.WRAP_CONTENT,
             0));
 
     LinearLayout ll2 = new LinearLayout(this.getContext());
     mBrowseButton = new Button(this.getContext());
     mBrowseButton.setText("Browse");
     mBrowseButton.setOnClickListener(browseClicker);
     fileSelected = new AutoCompleteTextView(this.getContext());
     fileSelected.setHint("Select a File");
     ll2.addView(mBrowseButton,
         new LinearLayout.LayoutParams(
             ViewGroup.LayoutParams.WRAP_CONTENT,
             ViewGroup.LayoutParams.WRAP_CONTENT,
             0));
     ll2.addView(fileSelected,
         new LinearLayout.LayoutParams(
             ViewGroup.LayoutParams.FILL_PARENT,
             ViewGroup.LayoutParams.FILL_PARENT,
             0));
 
     custom.addView(ll,
         new LinearLayout.LayoutParams(
             ViewGroup.LayoutParams.WRAP_CONTENT,
             ViewGroup.LayoutParams.WRAP_CONTENT,
             0));
     custom.addView(ll2,
         new LinearLayout.LayoutParams(
             ViewGroup.LayoutParams.MATCH_PARENT,
             ViewGroup.LayoutParams.WRAP_CONTENT,
             0));
 
     //final int finalStartTime = startTime;
     final Track finalTrack = associated;
 
     new AlertDialog.Builder(getContext())
       .setTitle("Record or Browse?")
       .setView(custom)
       .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
         public void onClick(DialogInterface dialog, int whichButton) {
           //want it to pass a new clip back to the editor panel and add it to the screen
           //startTimePrompt(finalStartTime);
           startTimePrompt();
         }
       })
       .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
         public void onClick(DialogInterface dialog, int whichButton) {
           // Do nothing.
         }
       })
       .show();
   }
 
   //creates a dialog for browsing a list of the user's audiofiles, for adding clips to a track with a file youve already
   //recorded.
   public void browsePromptDialog(){
     System.out.println("Here");
     final AudioFile[] audioFilesByUser = this.dataSource.getAudioFilesByUserId(this.dataSource.getActiveUser().getId());
     final String[] fileNames = new String[audioFilesByUser.length];
     for(int i = 0; i < audioFilesByUser.length; i++){
       fileNames[i] = audioFilesByUser[i].getClientFilePath();
     }
     new AlertDialog.Builder(getContext())
         .setTitle("Select Audio File for New Clip Homie")
         .setItems(fileNames, new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int which) {
             // The 'which' argument contains the index position
             // of the selected item
             lastRecordedFile = audioFilesByUser[which];
             fileSelected.setText(lastRecordedFile.getClientFilePath());
           }
         })
         .show();
   }
 
   public void setStart(int time){
     this.startTimeSetter = time;
   }
 
   public boolean isNumeric(String s) {
     return s.matches("[-+]?\\d*\\.?\\d+");
   }
 
   public void startTimePrompt(){
     DialogFactory.prompt(getContext(),"Enter a start time or add it to the end of the track by entering nothing.","",
         new Lambda.StringCallback() {
           public void go(String value) {
             if(value.isEmpty()){
               int val = associated.findLastClipTime()+25;
               clipMaker(associated, lastRecordedFile, val);
             }
             else if(isNumeric(value)) {
               int val = Integer.parseInt(value);
               clipMaker(associated, lastRecordedFile, val);
             } else {
               DialogFactory.alert(getContext(),"Oops!","That isn't a valid start time.");
             }
           }
         }
     );
   }
 
   public void clipMaker(Track track, AudioFile file, int startTime) {
     Random rnd = new Random();
     int newColor = Color.argb(168, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
     Clip newClip = dataSource.createClip(track, file, startTime, newColor);
     newClip.parentTrack = track;
     track.clips.add(newClip);
     ColorChipButton newButton = new ColorChipButton(this.getContext(), newClip);
     newClip.addAssociatedView(newButton);
     this.addView(newButton);
     this.invalidateSafely();
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
 
   private void onRecord(boolean start) {
 
     if (start) {
       startRecording();
     } else {
       stopRecording();
     }
   }
 
   private void startRecording() {
     //Sets the name of the file when you start recording as opposed to when you click "Audio Record Test" from the main screen
     lastRecordedFileName = Environment.getExternalStorageDirectory()+"/LuperApp/Clips";
     lastRecordedFileName += "/clip_" + System.currentTimeMillis() +".3gp";
 
     mRecorder = new MediaRecorder();
     mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
     mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
     mRecorder.setOutputFile(lastRecordedFileName);
 
     mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
 
     try {
       mRecorder.prepare();
     } catch (IOException e) {
       Log.e(LOG_TAG, "prepare() failed", e);
     }
 
     mRecorder.start();
   }
 
   private void stopRecording() {
     mRecorder.stop();
     mRecorder.release();
 
     lastRecordedFile = dataSource.createAudioFile(dataSource.getActiveUser(), lastRecordedFileName);
     lastRecordedFile.setReadyOnClient(true);
 
     lastRecordedFile.setDurationMS(lastRecordedFile.calcDuration());
 
     fileSelected.setText(lastRecordedFileName);
     mRecorder = null;
   }
     /*private void startPlaying() {
 
         mPlayer = new MediaPlayer();
         try {
             mPlayer.setDataSource(lastRecordedFileName);
             mPlayer.prepare();
             //playFrom(1000);
             mPlayer.start();
           editorActivity.playhead.moveHead(mPlayer);
         } catch (IOException e) {
             Log.e(LOG_TAG, "prepare() failed1");
         }
     }
 
    private void stopPlaying() {
       if(mPlayer != null) {
         mPlayer.release();
         mPlayer = null;
       }
     }*/
 
   @Background
   public void playPreparedClip() {
     playPreparedClip(null);
   }
 
   // later on will also take a startTime parameter (current playhead time)
   @Background
   public void playPreparedClip(final Clip optionalNextClip) {
     try {
       final Track t = associated;
       mPlayer.start();
       mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
         public void onCompletion(MediaPlayer mp) {
          if(optionalNextClip == null) {
             t.prepareNextClip(t.nextClip.getStartTime()+10);
          } else {
             t.trackView.prepareClip(optionalNextClip);
           }
         }
       });
     } catch(Exception e) {
       //handle interrupted exceptions in a different way
       Log.e(LOG_TAG, "CLIP PREPARE FAILED", e);
     }
   }
 
   @UiThread
   public void startPlayingTrack() {
     startPlayingTrackInBackground();
   }
 
   @Background // doesn't actually run them in parallel - might be related to the sleep call in track startPlayback
   public void startPlayingTrackInBackground() {
     //mPlayer = new MediaPlayer();
     ArrayList<Clip> clips = associated.getClips();
     for(int i=0; i<clips.size(); i++) {
       Clip c = clips.get(i);
       prepareClip(c);
       try {
         Thread.sleep(c.getDurationMS());
       } catch(InterruptedException e) {
         Log.e("luper", "THREAD.SLEEP WAS INTERRUPTED WHILE PLAYING INDIVIDUAL TRACK");
       }
       playPreparedClip(i+1 < clips.size() ? clips.get(i+1) : null);
     }
   }
 
   @Background
   public void stopPlaying() {
     if(mPlayer.isPlaying()) mPlayer.stop();
   }
 
   @Background
   public void prepareClip(Clip c) {
     this.preparedClip = c;
     if(mPlayer != null) {
       mPlayer.reset();
     } else {
       mPlayer = new MediaPlayer();
     }
     // mPlayer is now idle
     String clipFileName = c.getAudioFile().getClientFilePath();
     try {
       mPlayer.setDataSource(clipFileName);
       mPlayer.prepare();
       // TODO make an onPrepared and check if we're behind playhead
     } catch(Exception e) {
       //handle interrupted exceptions in a different way
       Log.e(LOG_TAG, "CLIP PREPARE FAILED", e);
     }
   }
 
   public void invalidateSafely() {
     this.requestLayout();
     if (Looper.myLooper() != null && Looper.myLooper() == Looper.getMainLooper()) {
       // we're in the main-thread / UI Thread.
       this.invalidate();
     } else {
       // we're in a background thread.
       this.postInvalidate();
     }
   }
 
   private void stopPlayingTrack() {
     if(mPlayer != null) {
       mPlayer.release();
       mPlayer = null;
     }
   }
 
 
 }
 
 
