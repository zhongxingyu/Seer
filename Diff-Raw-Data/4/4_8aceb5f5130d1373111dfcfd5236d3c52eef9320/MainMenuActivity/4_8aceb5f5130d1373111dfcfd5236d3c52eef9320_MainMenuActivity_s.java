 package com.group7.dragonwars;
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.res.AssetManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 public class MainMenuActivity extends Activity implements OnClickListener {
 
     private Button btnBattle, btnStats, btnQuit;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.activity_main_menu);
         this.btnBattle = (Button)this.findViewById(R.id.btnBattle);
         this.btnBattle.setOnClickListener(this);
 
         this.btnStats = (Button)this.findViewById(R.id.btnStats);
         this.btnStats.setOnClickListener(this);
 
         this.btnQuit = (Button)this.findViewById(R.id.btnQuit);
         this.btnQuit.setOnClickListener(this);
 
     }
 
     @Override
     protected void onStart() {
         // app lifecycle
         // 1. onCreate
         // 2. onStart
         // 3. onResume (can interact now)
         // 4. onPause (could -> onResume) (state not lost)
         // 5. onStop (could -> onStart) (state not lost)
         // 6. onDestroy (state lost)
         //Toast.makeText(getApplicationContext(), "If the user had a game in progress when they quit the app (without properly saving and exiting), we will offer to resume it now (we are nice like that)", Toast.LENGTH_LONG).show();
         super.onStart();
 
         AssetManager ass = getAssets();
         try {
             String[] files = ass.list("maps");
             for (int i = 0; i < files.length; ++i) {
                 Log.v(null, "File " + i + " " + files[i]);
             }
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 
     @Override
     public void onClick(View v) {
         if (v == this.btnBattle) {
             Intent intent = new Intent(this, MapSelectActivity.class);
             startActivity(intent);
             finish();
         }
 
         if (v == this.btnStats) {
             setContentView(R.layout.loading_screen);
             Intent intent = new Intent(this, StatisticsActivity.class);
             startActivity(intent);
         }
 
         if (v == this.btnQuit) {
             System.exit(0);
         }
     }
 }
