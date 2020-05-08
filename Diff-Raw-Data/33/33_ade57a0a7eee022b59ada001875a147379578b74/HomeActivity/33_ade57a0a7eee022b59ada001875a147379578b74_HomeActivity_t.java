 
 package com.hrw.android.tdd.activity;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.ImageView;
 
import com.hrw.android.tdd.R;
 
 public class HomeActivity extends Activity {
 
     public ImageView mHomeButton;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         setContentView(R.layout.home_layout);
         mHomeButton = (ImageView) findViewById(R.id.home_button);
         super.onCreate(savedInstanceState);
     }
 
 }
