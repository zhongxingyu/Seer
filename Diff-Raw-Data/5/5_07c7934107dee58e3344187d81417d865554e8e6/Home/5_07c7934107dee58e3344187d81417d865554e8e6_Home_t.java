 package com.lftechnology.childtutor;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 
 public class Home extends Activity implements OnClickListener {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_home);
 
         // Add event handlers for buttons
        View lesson1Button = findViewById(R.id.button1);
         lesson1Button.setOnClickListener(this);
        View lesson2Button = findViewById(R.id.button2);
         lesson2Button.setOnClickListener(this);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.home, menu);
         return true;
     }
 
     @Override
     public void onClick(View v) {
         switch (v.getId()) {
 
         case R.id.lesson1:
             // call lessen1
             break;
 
         case R.id.lesson2:
             // call lesson2
             break;
 
         }
     }
 
 }
