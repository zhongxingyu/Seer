 /*
  * Copyright (C) 2007 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package hpc.idcc.cameraapp;
 
 import hpc.idcc.cameraapp.SavePhotoAsyncTask.SavePhotoListener;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Intent;
 import android.hardware.Camera;
 import android.hardware.Camera.PictureCallback;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.text.TextUtils;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 // ----------------------------------------------------------------------
 
 public class CameraPreviewActivity extends Activity implements OnClickListener, SavePhotoListener  {
     private CameraPreviewSurfaceView mPreview;
     private Camera mCamera;
     private int numberOfCameras;
     private int cameraCurrentlyLocked;
 
     // The first rear facing camera
     private int defaultCameraId;
 
     private ImageView mConfirmButton;
     private ImageView mCancelButton;
     private ImageView imageviewTakePhoto;
     private byte[] mPhotoData;
     private String fileUri;
     
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         // Hide the window title.
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
        fileUri = ((Uri) getIntent().getParcelableExtra(MediaStore.EXTRA_OUTPUT)).toString();
        if (TextUtils.isEmpty(fileUri)) {
             fileUri = PictureFiles.getOutputMediaFile(PictureFiles.MEDIA_TYPE_IMAGE).getPath();
         }
         
         // Create a RelativeLayout container that will hold a SurfaceView,
         // and set it as the content of our activity.
         setContentView(R.layout.activity_camera_preview);
         mPreview = (CameraPreviewSurfaceView) findViewById(R.id.camera_preview_surfaceview);
 
         imageviewTakePhoto = (ImageView) findViewById(R.id.camera_preview_take_photo);
         imageviewTakePhoto.setOnClickListener(this);
         
         mConfirmButton = (ImageView) findViewById(R.id.camera_preview_confirm);
         mConfirmButton.setOnClickListener(this);
         
         mCancelButton = (ImageView) findViewById(R.id.camera_preview_cancel);
         mCancelButton.setOnClickListener(this);
         
         // Find the total number of cameras available
         numberOfCameras = Camera.getNumberOfCameras();
         
         if (numberOfCameras < 1) {
             Toast.makeText(this, "sorry, no camera", Toast.LENGTH_SHORT).show();
             finish();
         } 
             
         defaultCameraId = 0;
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
         // Open the default i.e. the first rear facing camera.
         mCamera = Camera.open(defaultCameraId);
         cameraCurrentlyLocked = defaultCameraId;
         mPreview.setCamera(mCamera);
     }
 
     @Override
     protected void onPause() {
         super.onPause();
 
         // Because the Camera object is a shared resource, it's very
         // important to release it when the activity is paused.
         if (mCamera != null) {
             mPreview.setCamera(null);
             mCamera.release();
             mCamera = null;
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
 
         // Inflate our menu which can gather user input for switching camera
         return false;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         switch (item.getItemId()) {
         case R.id.switch_cam:
             // check for availability of multiple cameras
             if (numberOfCameras == 1) {
                 AlertDialog.Builder builder = new AlertDialog.Builder(this);
                 builder.setMessage(this.getString(R.string.camera_alert))
                        .setNeutralButton("Close", null);
                 AlertDialog alert = builder.create();
                 alert.show();
                 return true;
             }
 
             // OK, we have multiple cameras.
             // Release this camera -> cameraCurrentlyLocked
             if (mCamera != null) {
                 mCamera.stopPreview();
                 mPreview.setCamera(null);
                 mCamera.release();
                 mCamera = null;
             }
 
             // Acquire the next camera and request Preview to reconfigure
             // parameters.
             mCamera = Camera
                     .open((cameraCurrentlyLocked + 1) % numberOfCameras);
             cameraCurrentlyLocked = (cameraCurrentlyLocked + 1)
                     % numberOfCameras;
 
             // Start the preview
             mCamera.startPreview();
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
     
     private PictureCallback mPicture = new PictureCallback() {
 
         @Override
         public void onPictureTaken(byte[] data, Camera camera) {
             mPhotoData = data;
             mConfirmButton.setVisibility(View.VISIBLE);
             mCancelButton.setVisibility(View.VISIBLE);
         }
     };
 
     @Override
     public void onClick(View v) {
         switch (v.getId()) {
         case R.id.camera_preview_take_photo:
             if (mCamera != null)
                 mCamera.takePicture(null, null, mPicture);
             break;
         case R.id.camera_preview_confirm:
             SavePhotoAsyncTask mAsyncTask = new SavePhotoAsyncTask(this, 
                     Uri.parse(fileUri));
             mAsyncTask.execute(mPhotoData);
             break;
         case R.id.camera_preview_cancel:
             mPhotoData = null;
             mConfirmButton.setVisibility(View.GONE);
             mCancelButton.setVisibility(View.GONE);
             mCamera.startPreview();
         default:
             break;
         }
     }
 
     @Override
     public void onSaveFinish(Throwable e, Uri uri) {
         Intent intent = new Intent();
         if (e == null) {
             intent.setData(uri);
             intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
             setResult(RESULT_OK, intent);
         } else {
             setResult(RESULT_CANCELED, intent);
             Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
         }
         finish();
     }
 }
