 package com.utai.gitdemo1;
 
 import android.app.Activity;
import android.content.Intent;
 import android.os.Bundle;
import android.view.View;
 
 public class GitDemo1Activity extends Activity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
     }
    public void doActivity(View view){
    	Intent intent = new Intent();
    	intent.setClass(this,Second.class);
    	startActivity(intent);
    }
 }
