 package com.akkie.tortoisegit;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.TextView;
 
 /** local master branch */
 public class TortoiseGit extends Activity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         //setContentView(R.layout.main);
 
         TextView tView = new TextView(this);
         //tView.setText(getString(R.string.app_name) + ", 開始");
         //tView.setText(getString(R.string.app_name) + ", 開始2");
 
         //tView.setText(getString(R.string.app_name) + ", 開始3");
         tView.setText(getString(R.string.app_name) + ", from MAC");
         tView.setText(getString(R.string.app_name) + ", from MAC2");
         tView.setText(getString(R.string.app_name) + ", from 5/29 MAC3");
         tView.setText(getString(R.string.app_name) + ", from 5/29 MAC5");
         
         tView.setText(getString(R.string.app_name) + ", from 5/29 MAC7");
         
         //local master branch 18:36
         setContentView(tView);
     }
}
