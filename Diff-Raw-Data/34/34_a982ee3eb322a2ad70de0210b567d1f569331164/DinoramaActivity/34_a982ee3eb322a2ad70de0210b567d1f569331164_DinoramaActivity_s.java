 package com.amazon.dinorama;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.view.View;
 
 public class DinoramaActivity extends Activity {
     
 	MediaPlayer song;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main_menu);
         /** Called when the activity is first created. */
         song = MediaPlayer.create(DinoramaActivity.this, R.raw.menu_music);
         song.setLooping(true);
         song.start();
     }
     
     public void pickArcadeMode(View view){
     	Intent myIntent = new Intent(DinoramaActivity.this, CharacterSelectActivity.class);
     	myIntent.putExtra("game_mode", "arcade"); //Optional parameters
     	song.stop();
     	DinoramaActivity.this.startActivity(myIntent);
     }
     
     public void pickCampaignMode(View view){
     	Intent myIntent = new Intent(DinoramaActivity.this, CharacterSelectActivity.class);
     	myIntent.putExtra("game_mode", "campaign"); //Optional parameters
     	song.stop();
     	DinoramaActivity.this.startActivity(myIntent);
     }
 }
