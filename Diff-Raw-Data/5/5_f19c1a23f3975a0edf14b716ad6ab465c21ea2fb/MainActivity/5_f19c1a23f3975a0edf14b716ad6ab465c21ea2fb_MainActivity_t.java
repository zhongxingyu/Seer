 package org.dushuba.android;
 
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.widget.Toast;
 
 import org.apache.cordova.DroidGap;
 
 public class MainActivity extends DroidGap
 {
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
        super.setStringProperty("errorUrl", "file:///android_asset/www/error.html");
        super.init();
 		super.setIntegerProperty("splashscreen", R.drawable.dushuba_flash);
        super.loadUrl("file:///android_asset/www/index.html");
     }
     
 	private long exitTime = 0;
 	
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
 			if (System.currentTimeMillis() - exitTime > 2000) {
 				Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
 				exitTime = System.currentTimeMillis();
 			}
 			else {
 				android.os.Process.killProcess(android.os.Process.myPid());
 			}
 			return true;
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 }
