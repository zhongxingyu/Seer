 package edu.voicelabs.vst;
 
 import android.app.Activity;
 import android.os.Bundle;
import android.media.MediaPlayer;
 
 public class TutorialVideo extends Activity {
 	
 	
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		setContentView(R.layout.tutorial_movie);
 		
		MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.tutorial_intro);
		mediaPlayer.start();
 	}
 }
