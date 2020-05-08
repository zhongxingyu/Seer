 package cc.openframeworks.androidFerns;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Window;
 import cc.openframeworks.OFAndroid;
 
 
 public class OFActivity extends Activity{
 
 	@Override
     public void onCreate(Bundle savedInstanceState)
     { 
         super.onCreate(savedInstanceState);
         String packageName = getPackageName();
 
         ofApp = new OFAndroid(packageName,this);
     }
 	
 	@Override
 	public void onDetachedFromWindow() {
 	}
 	
     @Override
     protected void onPause() {
         super.onPause();
         ofApp.pause();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         ofApp.resume();
     }
     
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         OFAndroid.onKeyDown(keyCode);
         return super.onKeyDown(keyCode, event);
     }
     
     @Override
     public boolean onKeyUp(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)) {
            if( OFAndroid.onBackPressed() ) return true;
            else return super.onKeyUp(keyCode, event);
         }
         
         OFAndroid.onKeyUp(keyCode);
         return super.onKeyUp(keyCode, event);
     }
 
 	
 	OFAndroid ofApp;
 }
 
 
 
