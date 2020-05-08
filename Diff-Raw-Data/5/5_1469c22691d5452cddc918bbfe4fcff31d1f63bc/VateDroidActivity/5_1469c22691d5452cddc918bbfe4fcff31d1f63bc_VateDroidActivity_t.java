 package com.vatedroid;
 
 import android.app.Activity;
 import android.os.Bundle;
import android.util.Log;
 
 public class VateDroidActivity extends Activity
 {
    // native instance method declaration
    private native String bottleThrower(String name, String code);

     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
     }
 }
