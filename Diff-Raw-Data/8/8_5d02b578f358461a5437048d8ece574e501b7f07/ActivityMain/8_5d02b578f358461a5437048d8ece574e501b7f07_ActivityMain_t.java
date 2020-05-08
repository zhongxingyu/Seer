 /**
  * Copyright 2011 Mark Wyszomierski
  */
 package com.foursquare.android.oauth;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 /**
  * https://developer.foursquare.com/docs/oauth.html
  * https://foursquare.com/oauth/
  * 
  * @date May 17, 2011
  * @author Mark Wyszomierski (markww@gmail.com)
  */
 public class ActivityMain extends Activity 
 {
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         Button btn = (Button)findViewById(R.id.button);
         btn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(ActivityMain.this, ActivityWebView.class);
                startActivity(intent);
            }
         });
     }
 }
