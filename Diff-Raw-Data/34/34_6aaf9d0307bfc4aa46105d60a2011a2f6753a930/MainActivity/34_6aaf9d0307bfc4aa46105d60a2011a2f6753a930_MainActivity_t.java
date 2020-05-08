 package com.sinaapp.ovilia;
 
 import android.os.Bundle;
 import android.view.Menu;
import android.view.WindowManager;

 import com.phonegap.*;
 import com.sinaapp.ovilia.fishbonecollector.R;
 
 public class MainActivity extends DroidGap {
	
	private JsHelper jsHelper;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        super.init();
        
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        jsHelper = new JsHelper(this);
        super.appView.addJavascriptInterface(jsHelper, "JsHelper");
        
         super.loadUrl("file:///android_asset/www/index.html");
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 }
