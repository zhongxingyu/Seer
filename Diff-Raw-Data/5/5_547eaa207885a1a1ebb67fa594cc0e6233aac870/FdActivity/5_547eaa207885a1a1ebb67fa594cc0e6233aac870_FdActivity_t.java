 package org.on.puz.photobombsquad;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.SimpleDateFormat;
 
 import org.opencv.android.BaseLoaderCallback;
 import org.opencv.android.CameraBridgeViewBase;
 import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
 import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
 import org.opencv.android.LoaderCallbackInterface;
 import org.opencv.android.OpenCVLoader;
 import org.opencv.android.Utils;
 import org.opencv.core.Core;
 import org.opencv.core.Mat;
 import org.opencv.core.Rect;
 import org.opencv.core.Scalar;
 import org.opencv.samples.facedetect.DetectionBasedTracker;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.Config;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.ImageView;
 
 public class FdActivity extends Activity implements CvCameraViewListener2{//, OnClickListener {
 
     private static final String    TAG                 = "OCVSample::Activity";
     private static final Scalar    FACE_GOOD_COLOR     = new Scalar(  0, 255, 0, 255),
     							   FACE_BAD_COLOR      = new Scalar(255,   0, 0, 255);
 
     private MenuItem               mItemFace50;
     private MenuItem               mItemFace40;
     private MenuItem               mItemFace30;
     private MenuItem               mItemFace20;
 
     private Mat                    mRgba;
     private Mat                    mGray;
     private File                   mCascadeFile;
     private DetectionBasedTracker  mNativeDetector;
 
     private float                  mRelativeFaceSize   = 0.2f;
     private int                    mAbsoluteFaceSize   = 0;
 
     private CameraBridgeViewBase   mOpenCvCameraView;
     
     private FaceTracker			   mTracker;
 
     private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
         @Override
         public void onManagerConnected(int status) {
             switch (status) {
                 case LoaderCallbackInterface.SUCCESS:
                 {
                     Log.i(TAG, "OpenCV loaded successfully");
 
                     // Load native library after(!) OpenCV initialization
                     System.loadLibrary("detection_based_tracker");
 
                     try {
                         // load cascade file from application resources
                         InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                         File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                         mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                         FileOutputStream os = new FileOutputStream(mCascadeFile);
 
                         byte[] buffer = new byte[4096];
                         int bytesRead;
                         while ((bytesRead = is.read(buffer)) != -1) {
                             os.write(buffer, 0, bytesRead);
                         }
                         is.close();
                         os.close();
 
                         mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);
                         
                         mTracker = new FaceTracker(mNativeDetector, 20*20, 20, 1, 1.5);
 
                         cascadeDir.delete();
 
                     } catch (IOException e) {
                         e.printStackTrace();
                         Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                     }
 
                     mOpenCvCameraView.enableView();
                 } break;
                 default:
                 {
                     super.onManagerConnected(status);
                 } break;
             }
         }
     };
 
     public FdActivity() {
         Log.i(TAG, "Instantiated new " + this.getClass());
     }
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         
         Log.i(TAG, "called onCreate");
         super.onCreate(savedInstanceState);
         getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 
         //findViewById(R.id.button_capture).setOnClickListener(this);        
         setContentView(R.layout.face_detect_surface_view);
         
         mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
         mOpenCvCameraView.setCvCameraViewListener(this);
     }
 
     @Override
     public void onPause()
     {
         super.onPause();
         if (mOpenCvCameraView != null)
             mOpenCvCameraView.disableView();
     }
 
     @Override
     public void onResume()
     {
         super.onResume();
         OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
     }
 
     public void onDestroy() {
         super.onDestroy();
         mOpenCvCameraView.disableView();
     }
 
     public void onCameraViewStarted(int width, int height) {
         mGray = new Mat();
         mRgba = new Mat();
     }
 
     public void onCameraViewStopped() {
         mGray.release();
         mRgba.release();
     }
 
     public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
 
         mRgba = inputFrame.rgba();
         mGray = inputFrame.gray();
 
         if (mAbsoluteFaceSize == 0) {
             int height = mGray.rows();
             if (Math.round(height * mRelativeFaceSize) > 0) {
                 mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
             }
             mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
         }
         
         mTracker.addFaceSet(mGray, System.currentTimeMillis());
 
         for (Rect face:mTracker.goodFaces())
             Core.rectangle(mRgba, face.tl(), face.br(), FACE_GOOD_COLOR, 3);
         for (Rect face:mTracker.badFaces())
             Core.rectangle(mRgba, face.tl(), face.br(), FACE_BAD_COLOR, 3);
 
         return mRgba;
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         Log.i(TAG, "called onCreateOptionsMenu");
         mItemFace50 = menu.add("Face size 50%");
         mItemFace40 = menu.add("Face size 40%");
         mItemFace30 = menu.add("Face size 30%");
         mItemFace20 = menu.add("Face size 20%");
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
         if (item == mItemFace50)
             setMinFaceSize(0.5f);
         else if (item == mItemFace40)
             setMinFaceSize(0.4f);
         else if (item == mItemFace30)
             setMinFaceSize(0.3f);
         else if (item == mItemFace20)
             setMinFaceSize(0.2f);
         return true;
     }
 
     private void setMinFaceSize(float faceSize) {
         mRelativeFaceSize = faceSize;
         mAbsoluteFaceSize = 0;
     }
    public void capturePhoto(View v) {
         Bitmap bmp = Bitmap.createBitmap(mRgba.width(), mRgba.height(), Config.ARGB_8888);
        Utils.matToBitmap(mRgba, bmp);
         ImageView tv1 = new ImageView(this);
         tv1.setImageBitmap(bmp);
         setContentView(tv1);
         try {
             
             String fDate = new SimpleDateFormat("yyyymmddhhmmss").format(new java.util.Date());
             File picDir = new File( Environment.getExternalStorageDirectory().toString()+File.separator + "BombDiffuser");
             if (! picDir.exists()){
                 picDir.mkdirs();
                 if (! picDir.mkdirs()){
                     Log.d("SavePicture", "failed to create directory");
                     return;
                 }
             }
             ByteArrayOutputStream bytes = new ByteArrayOutputStream();
             bmp.compress(Bitmap.CompressFormat.PNG, 90, bytes);
             
             File file = new File(picDir.getPath().toString() + File.separator  + "picture"+ fDate + ".png");
             Log.i(TAG, picDir.getPath().toString() + File.separator + "picture"+ fDate + ".png");
             file.createNewFile();
             FileOutputStream out = new FileOutputStream(file);
             out.write(bytes.toByteArray());
 
            
            out.flush();
            out.close();
 
         } catch (Exception e) {
                e.printStackTrace();
         }
     
     }
 
       
    
 }
