 package edu.plymouth.psumobile;
 
 import android.os.Bundle;
import org.apache.cordova.*;
 
 public class App extends DroidGap {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         // Set a property to load the splash image before loading the URL
         super.setIntegerProperty("splashscreen", R.drawable.splash);
         
         // Load the local asset url.
        // The second parameter is the amount of time that should pass before the URL is actually loaded... effectively the splash screen show time
         // Uncomment the OS-specific folder (symlink)
         super.loadUrl("file:///android_asset/www/index.html", 750);				// Unix like / Mac OS X
         //super.loadUrl("file:///android_asset/www_windows/index.html", 750); 	// Windows
     }
 }
