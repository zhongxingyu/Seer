 package com.unsignedlab.contasigarette;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.TextView;
 import org.joda.time.*;
 
 
 public class MainActivity extends Activity {
 
     private int mNCy = 0;
     private TextView mTitle;
 
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         //retrive old mNCY
         SharedPreferences prefs = this.getSharedPreferences("DATABASE", 0);
         this.mNCy = prefs.getInt("daily", -1);
         DateMidnight mDm = new DateMidnight();
         int cDate = prefs.getInt("daily_date", -1);
 
         if(mDm.dayOfYear().get() > cDate || cDate == -1){
             SharedPreferences.Editor editor = prefs.edit();
             editor.putInt("daily_date", mDm.getDayOfYear());
             editor.putInt(String.valueOf(cDate), mNCy);
             mNCy = 0;
             editor.commit();
         }
 
        this.mTitle = (TextView)findViewById(R.id.title);
         this.mTitle.setText("Oggi hai fumato "+String.valueOf(mNCy)+ " "+((mNCy != 1) ? "sigarette" : "sigaretta"));
 
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     public void addCygarette(View arg0){
         this.mNCy++;
         this.mTitle.setText("Oggi hai fumato "+String.valueOf(mNCy)+ " "+((mNCy != 1) ? "sigarette" : "sigaretta"));
     }
 
     public void gotoGraph(View arg0){
 
         //save current cygarette in db
         SharedPreferences prefs = getSharedPreferences("DATABASE", 0);
 
         SharedPreferences.Editor editor = prefs.edit();
         editor.putInt("daily", this.mNCy);
         editor.commit();
 
         Intent mIntent = new Intent(arg0.getContext(), GraphActivity.class);
         startActivityForResult(mIntent, 0);
     }
 
     @Override
     public void onStop(){
         super.onStop();
 
         SharedPreferences prefs = getSharedPreferences("DATABASE", 0);
 
         SharedPreferences.Editor editor = prefs.edit();
         editor.putInt("daily", this.mNCy);
         editor.commit();
 
     }
 }
