 package org.gotext;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 
 public class AboutActivity extends Activity {
 	
 	 @Override
 	    public void onCreate(Bundle savedInstanceState) {
 	        super.onCreate(savedInstanceState);
 	        setContentView(R.layout.about);
 	        
 	 }
 	 
 	   public void onClick(View view) {
 	    	 switch (view.getId()) {
 	    	 case R.id.buttonInfo:
 	    		 finish();
 	             break;
 	    	 }
 	    }
 	 
 
 }
