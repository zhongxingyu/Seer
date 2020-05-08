 package net.learn2develop.Activity101;
 
 import net.learn2develop.R;
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Window;
 
// page 38
 public class Activity101Activity extends Activity {
   String tag = "Lifecycle";
 
   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
 
     // ---hides the title bar---
     requestWindowFeature(Window.FEATURE_NO_TITLE);
 
     setContentView(R.layout.activity_activity101);
     Log.d(tag, "In the onCreate() event");
   }
 
   public void onStart() {
     super.onStart();
     Log.d(tag, "In the onStart() event");
   }
 
   public void onRestart() {
     super.onRestart();
     Log.d(tag, "In the onRestart() event");
   }
 
   public void onResume() {
     super.onResume();
     Log.d(tag, "In the onResume() event");
   }
 
   public void onPause() {
     super.onPause();
     Log.d(tag, "In the onPause() event");
   }
 
   public void onStop() {
     super.onStop();
     Log.d(tag, "In the onStop() event");
   }
 
   public void onDestroy() {
     Log.d(tag, "In the onDestroy() event");
   }
 }
