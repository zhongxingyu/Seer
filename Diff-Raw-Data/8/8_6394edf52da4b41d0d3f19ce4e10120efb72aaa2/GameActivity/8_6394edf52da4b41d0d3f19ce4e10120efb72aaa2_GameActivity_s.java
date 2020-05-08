 package com.paperplanez;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 
 public class GameActivity extends Activity {
     /** Called when the activity is first created. */
 	
	static int width, heigh, balloons;
 	static long start_time;
 	boolean game_over = false;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         SharedPreferences prefs = getSharedPreferences("com.paperplanez", Context.MODE_PRIVATE);
        heigh = prefs.getInt("width", 600);
         width = prefs.getInt("width", 800);
        balloons = prefs.getInt("ballons", 10);
         
         while (!game_over) {
         	start_time = System.nanoTime();
         	
         	//TODO Physics
         	
         	
         	
         	while (System.nanoTime() - start_time < 40000);
         }
         
         
     }
 }
