 package net.smach.harga;
 
 import java.io.*;
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 
 import org.apache.cordova.*;
 
 public class MainActivity extends DroidGap {
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         super.loadUrl("file:///android_asset/www/index.html");
     }
}
