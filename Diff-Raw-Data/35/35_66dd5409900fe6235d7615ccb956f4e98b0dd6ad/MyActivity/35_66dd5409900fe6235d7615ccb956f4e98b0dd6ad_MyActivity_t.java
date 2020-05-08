 package com.example.AllTheShares;
 
 import android.app.Activity;
 import android.os.Bundle;
 
 public class MyActivity extends Activity {
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
     }

    public static int add(int i, int j){
        return i+j;
    }
 }
