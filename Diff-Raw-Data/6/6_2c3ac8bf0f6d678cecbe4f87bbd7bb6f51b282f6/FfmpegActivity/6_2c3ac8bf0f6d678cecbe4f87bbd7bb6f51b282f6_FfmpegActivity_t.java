 package com.iasa;
 
 import android.app.Activity;
 import android.os.Bundle;
 
 import android.content.Context;
 import android.content.pm.ActivityInfo;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.View;
 import android.view.MotionEvent;
 
 import com.iasa.GlView;
 
 public class FfmpegActivity extends Activity implements View.OnClickListener, View.OnLongClickListener {
     /**
      * Called when the activity is first created.
      */
     static NativeVideo mf = new NativeVideo();
 
     private GlView     mGlView;
     
    @Override protected void onCreate(Bundle icicle) {
     super.onCreate(icicle);
     
     Intent intent = new Intent(this, FileDialog.class);
     mGlView = new GlView(getApplication());
     mGlView.setOnClickListener(this);
 	mGlView.setOnLongClickListener(this);
 
 
     startActivityForResult (intent, 0);
 
 
     }
 
     @Override protected void onPause() {
         super.onPause();
         mGlView.onPause();
     }
 
     @Override protected void onResume() {
         super.onResume();
         mGlView.onResume();
     }
     
     @Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		
 		String readResultFile = FileDialogOptions.readResultFile(data);
 		
 		mf.Open(readResultFile);
 		mf.Start();
 		
 		setContentView(mGlView);
     }
     
     public void onClick(View v) {
 		mf.Pause();
 	}
 	
     public boolean onLongClick(View v) {
 	
 		mf.Pause();
 		return true;
 	}
 }
 
