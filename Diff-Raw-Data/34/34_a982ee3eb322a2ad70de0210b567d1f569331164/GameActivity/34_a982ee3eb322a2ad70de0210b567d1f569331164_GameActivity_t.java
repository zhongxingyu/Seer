 package com.amazon.dinorama;
 
 import android.app.Activity;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 
 public class GameActivity extends Activity {
 
 	MediaPlayer song;
 	
 	protected GameView gameView;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         gameView = new GameView(this);
         setContentView(gameView);
         
         song = MediaPlayer.create(GameActivity.this, R.raw.game_mode);
         song.setLooping(true);
         song.start();
 
     }
     
     @Override
     protected void onPause() {
     	super.onPause();
     	gameView.activate(false);
    	song.stop();
     }
     
     @Override
     protected void onStart() {
     	super.onStart();
     	gameView.activate(true);
     }
     
     @Override
     protected void onResume() {
     	super.onResume();
     	gameView.activate(true);
     }
     
     @Override
     protected void onRestart() {
     	super.onRestart();
     	gameView.activate(true);
     }
 }
