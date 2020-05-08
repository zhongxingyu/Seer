 package com.photobox.app;
 
 import com.photobox.R;
 import com.photobox.R.id;
 import com.photobox.R.layout;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 
 public class PhotoboxActivity extends Activity {
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                 WindowManager.LayoutParams.FLAG_FULLSCREEN);
         setContentView(R.layout.main);
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
 
        cleanOldViews(findViewById(R.id.ourCustomView1));
         System.gc();
     }
 
     private void cleanOldViews(View view) {
         if (view.getBackground() != null) {
             view.getBackground().setCallback(null);
         }
         if (view instanceof ViewGroup && (view instanceof AdapterView)) {
             for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                 cleanOldViews(((ViewGroup) view).getChildAt(i));
             }
         ((ViewGroup) view).removeAllViews();
         }
     }
}
