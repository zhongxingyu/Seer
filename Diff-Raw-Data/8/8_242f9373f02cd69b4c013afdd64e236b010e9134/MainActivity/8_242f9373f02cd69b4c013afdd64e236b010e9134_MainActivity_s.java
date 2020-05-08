 package com.mertlebeach.fungifind;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
//
//import com.parse.Parse;
//import com.parse.ParseFacebookUtils;
//import com.parse.ParseObject;
 
 
 public class MainActivity extends Activity {
     
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 //        Parse.initialize(this, "f9nuFRqE5CjHeXL5DxtKourEaDLM62sDkiYGjLzV", "JSCyGA0ovUs894LhEnqEijDCZgK2kgezoWLghk4T");
 //        ParseFacebookUtils.initialize("166475660199140");
 //        
 //        ParseObject testObject = new ParseObject("TestObject");
 //        testObject.put("foo", "bar");
 //        testObject.saveInBackground();
 //        
 
     }
     /** Called when the user clicks the Send button */
     public void getCamara(View view) {
         // Do something in response to button
     	 Intent intent = new Intent(this, CamaraActivity.class);
     	    
     	    startActivity(intent);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
        
         return true;
     }
     
 }
