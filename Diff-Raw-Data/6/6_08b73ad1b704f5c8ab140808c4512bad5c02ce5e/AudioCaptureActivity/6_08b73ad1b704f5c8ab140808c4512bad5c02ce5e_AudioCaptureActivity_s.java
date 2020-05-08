 /*
  * This file is part of TaskMan
  *
  * Copyright (C) 2012 Jed Barlow, Mark Galloway, Taylor Lloyd, Braeden Petruk
  *
  * TaskMan is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * TaskMan is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with TaskMan.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package ca.cmput301.team13.taskman;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.ByteBuffer;
 import java.nio.ShortBuffer;
 
 import utils.Notifications;
 import android.content.Intent;
 import android.media.MediaRecorder;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class AudioCaptureActivity extends FulfillmentActivity implements OnClickListener {
 
     private Uri audioFileUri;
     private static final int COLLECTION_ACTIVITY_REQUEST_CODE = 300;
     private MediaRecorder recorder;
     private boolean recording;
     private String fileName;
     private boolean audioSelected = false;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.audio_fulfillment);
         
         //setup our listeners
         ((Button)findViewById(R.id.record_button)).setOnClickListener(this);
         ((Button)findViewById(R.id.collection_button)).setOnClickListener(this);
         ((Button)findViewById(R.id.save_button)).setOnClickListener(this);
         ((Button)findViewById(R.id.cancel_button)).setOnClickListener(this);        
     }
 
     @Override
     public void onResume() {
         super.onResume();
         //make sure the recorder is null and booleans are false
         recorder = null;
         recording = false;
         audioSelected = false;
         ((TextView) findViewById(R.id.audio_view)).setText("Choose audio from your collection or record one now.");
     }
 
     @Override
     public void onPause() {
         super.onPause();
         //release the recorder if needed
         if(recorder != null){
             recorder.release();
             recorder = null;
             ((Button)findViewById(R.id.record_button)).setText("Start Recording");
         }
     }
     
     @Override
     public void onStop() {
         super.onStop();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.audio_fulfillment, menu);
         return true;
     }
 
     /**
      * Delegates action based on which listener has been clicked
      * @param source 
      */
     public void onClick(View source) {
         if(source.equals(findViewById(R.id.record_button))) {
             if(recording == false){
                 recording = true;
                 startRecording();
                 ((Button)findViewById(R.id.record_button)).setText("Stop Recording");
             } else {
                 recording = false;
                 recorder.stop();
                 audioSelected = true;
                 recorder.release();
                 
                 ((TextView) findViewById(R.id.audio_view)).setText("Recording stopped.");
                 ((Button)findViewById(R.id.record_button)).setText("Record Again");
             }
         }
         else if(source.equals(findViewById(R.id.collection_button))) {
             audioFromCollection();
         }
     }
     
     /**
      * Send the taken/selected audio to our parent and exit the Activity
      */
     public void save() {
     	short[] audioShorts = null;
         //test audio has been selected.
         if (audioSelected) {
             //Get audio from collection
             if(audioFileUri != null) {
             	audioShorts = getAudioShort(audioFileUri.getPath());
             //Get audio from the recorder
             } else if(fileName != null) {
             	audioShorts = getAudioShort(fileName);
             }
             
             //Return to the Task Viewer
             if(audioShorts != null) {
             	successful = true;
             	fulfillment.setAudio(audioShorts);
             } else {
             	successful = false;
             }
             finish();
         } else {
             Notifications.showToast(getApplicationContext(), "No Audio selected");
         }
     }
     
     /**
      * Creates a short array from audio data stored at the given file path
      * @param path		The path to the audio file
      * @return			The short[] representing the audio data
      */
     public short[] getAudioShort(String path) {
     	File audioFile;
     	FileInputStream audioStream = null;
     	byte[] audioBytes = null;
     	short[] audioShorts = null;
     	audioFile = new File(path);
     	//If audio of some kind was generated, attempt to convert it and pass it back to the Task Viewer
         if(audioFile != null) {
 			try {
 				audioStream = new FileInputStream(audioFile);
 				audioBytes = new byte[(int)audioFile.length()];
 				audioStream.read(audioBytes);
 				audioStream.close();
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
         }
         //Do the conversion
         if(audioBytes != null) {
         	ByteBuffer audioByteBuffer = ByteBuffer.wrap(audioBytes);
         	audioByteBuffer.rewind();
         	ShortBuffer audioShortBuffer = ((ByteBuffer)audioByteBuffer.rewind()).asShortBuffer();
         	audioShorts = audioShortBuffer.array();
         }
         return audioShorts;
     }
     
     /**
      * Cancel the Activity
      */
     public void cancel() {
         successful = false;
         finish();
     }
     
     /**
      * Takes the user to the built-in audio selector where an existing audio file can
      * be selected for use
      */
     private void audioFromCollection() {
         Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
         intent.setType("audio/*");
         startActivityForResult(Intent.createChooser(intent,"Select Audio "), COLLECTION_ACTIVITY_REQUEST_CODE);
     }
 
     /**
      * Sets the file path for the new audio file, initialises and starts the MediaRecorder
      * @see android.media.MediaRecorder
      */
     private void startRecording() {
         //set a file path for the new audio
         String folder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/tmp";
         File folderF = new File(folder);
         if (!folderF.exists()) {
             folderF.mkdir();
         }
         fileName = folder + "/" + String.valueOf(System.currentTimeMillis()) + ".3gp";
 
         //instantiate the MediaRecorder
         recorder = new MediaRecorder();
         recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
         recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
         recorder.setOutputFile(fileName);
         recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
 
         try {
             recorder.prepare();
         } catch (IOException e) {
             e.printStackTrace();
         }
         //start the recorder
         recorder.start();
         ((TextView) findViewById(R.id.audio_view)).setText("Recording...");
     }
     
     /**
      * Handles the result condition and/or returned data from the Android built-in 
      * Audio Content selection. 
      * 
      * @param requestCode specifies which type of activity we are returning from
      * @param resultCode signifies the success or fail of the intent
      * @param data The data returned from the intent
      */
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (requestCode == COLLECTION_ACTIVITY_REQUEST_CODE) {
             if (resultCode == RESULT_OK) {
                 Notifications.showToast(getApplicationContext(), "Audio Selection Successful");
                 audioSelected = true;
                 
                 audioFileUri = data.getData();
                 InputStream audioStream = null;
                 try {
                     audioStream = getContentResolver().openInputStream(audioFileUri);
                 } catch (FileNotFoundException e) {
                     e.printStackTrace();
                 }
 
                 //TODO: need to figure out what to do with audio file here
                 
             } else if (resultCode == RESULT_CANCELED) {
                 //Audio selection was cancelled
                 Notifications.showToast(getApplicationContext(), "Audio Selection Cancelled");
             } else {
                 //Audio selection had an error
                 Notifications.showToast(getApplicationContext(), "Audio Selection Error" + resultCode);
             }
         }
     }
 }
