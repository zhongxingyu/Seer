 package com.example.testapplication;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.content.Intent;
 
 
 public class MainActivity extends Activity {
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
     }
 

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

     /** Called when the user clicks the Music Player button */
     public void startMusicPlayer(View view) {
         Intent intent = new Intent(this, MyMediaPlayerActivity.class);
         startActivity(intent);
     }
 
     /** Called when the user clicks the About button */
     public void startAbout(View view) {
         Intent intent = new Intent(this, About.class);
         startActivity(intent);
     }
 
     /** Called when the user clicks the Camera button */
     public void startCamera(View view) {
         Intent intent = new Intent(this, PhotoIntentActivity.class);
         startActivity(intent);
     }
 
     /** Called when the user clicks the Website button */
     public void startWebsite(View view) {
         Intent intent = new Intent(this, WebActivity.class);
         startActivity(intent);
     }
     
 }
