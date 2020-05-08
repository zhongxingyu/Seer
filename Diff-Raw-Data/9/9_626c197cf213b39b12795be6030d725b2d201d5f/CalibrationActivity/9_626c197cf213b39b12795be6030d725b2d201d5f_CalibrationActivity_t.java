 package com.github.matt.williams.optometrist;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.WindowManager;
 
 public class CalibrationActivity extends Activity implements CalibrationView.OnCalibrationCompleteListener {
 
     private Preferences mPreferences;
     private CalibrationView mCameraView;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         mPreferences = new Preferences(this);
         Intent intent = getIntent();
         if (Intents.isActionCalibrate(intent) &&
            !Intents.isExtraForce(intent) &&
             mPreferences.hasMatrix()) {
             onCalibrationComplete(mPreferences.getMatrix());
         } else {
             setContentView(R.layout.activity_calibration);
             mCameraView = (CalibrationView)findViewById(R.id.calibrationView);
             mCameraView.setMatrix(mPreferences.getMatrix());
             mCameraView.requestFocus();
             mCameraView.setOnCalibrationCompleteListener(this);
             getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
         }
     }
 
     @Override
     protected void onNewIntent(Intent intent) {
         if (Intents.isActionCalibrate(intent) &&
             Intents.isExtraForce(intent) &&
             mPreferences.hasMatrix()) {
             onCalibrationComplete(mPreferences.getMatrix());
         } else {
             super.onNewIntent(intent);
         }
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         mCameraView.onResume();
     }
 
     @Override
     protected void onPause() {
         mCameraView.onPause();
         super.onPause();
     }
 
     @Override
     public void onCalibrationComplete(float[] matrix) {
         mPreferences.setMatrix(matrix);
         Intent intent = new Intent();
         Intents.setExtraMatrix(intent, matrix);
         setResult(Activity.RESULT_OK, intent);
         finish();
     }
 }
