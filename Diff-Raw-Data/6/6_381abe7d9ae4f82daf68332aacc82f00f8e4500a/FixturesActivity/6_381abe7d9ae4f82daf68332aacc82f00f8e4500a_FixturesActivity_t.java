 package com.icedwater.sleague;
 
 import android.app.Activity;
 import android.os.Bundle;
 
 import android.widget.Button;
 import android.view.View;
 import android.content.Intent;
 
 public class FixturesActivity extends Activity
 {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
        setContentView(R.layout.fixtures);

         Button btnMain = (Button) findViewById(R.id.btn_BackMain);
         btnMain.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                finish();
             }
         });
     }
 
 }
