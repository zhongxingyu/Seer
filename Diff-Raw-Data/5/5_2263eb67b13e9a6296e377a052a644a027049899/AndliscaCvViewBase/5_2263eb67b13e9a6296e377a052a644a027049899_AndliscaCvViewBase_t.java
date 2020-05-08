 package com.mash.andlisca;
 
 import java.util.List;
 
 import org.opencv.core.Size;
 import org.opencv.highgui.VideoCapture;
 import org.opencv.highgui.Highgui;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 public abstract class AndliscaCvViewBase extends SurfaceView implements SurfaceHolder.Callback, Runnable {
     private static final String TAG = "Sample::SurfaceView";
 
     private SurfaceHolder       mHolder;
     private VideoCapture        mCvCamera;
     private FpsMeter            mFps;
     private int                 mFrameWidth;
     private int                 mFrameHeight;    
 
     public AndliscaCvViewBase(Context context) {
         super(context);
         mHolder = getHolder();
         mHolder.addCallback(this);
         mFps = new FpsMeter();
         Log.i(TAG, "Instantiated new " + this.getClass());
     }
     
     public int getFrameWidth() {
         return mFrameWidth;
     }
 
     public int getFrameHeight() {
         return mFrameHeight;
     }    
 
     public void surfaceChanged(SurfaceHolder _holder, int format, int width, int height) {
         Log.i(TAG, "surfaceCreated");
         synchronized (this) {
             if (mCvCamera != null && mCvCamera.isOpened()) {
                 Log.i(TAG, "before mCvCamera.getSupportedPreviewSizes()");
                 List<Size> sizes = mCvCamera.getSupportedPreviewSizes();
                 Log.i(TAG, "after mCvCamera.getSupportedPreviewSizes()");
                 mFrameWidth = width;
                 mFrameHeight = height;
 
                 // selecting optimal camera preview size
                 {
                     double minDiff = Double.MAX_VALUE;
                     for (Size size : sizes) {
                         if (Math.abs(size.height - height) < minDiff) {
                             mFrameWidth = (int) size.width;
                             mFrameHeight = (int) size.height;
                             minDiff = Math.abs(size.height - height);
                         }
                     }
                 }
 
                 mCvCamera.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, mFrameWidth);
                 mCvCamera.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, mFrameHeight);
             }
         }
     }
 
     public void surfaceCreated(SurfaceHolder holder) {
         Log.i(TAG, "surfaceCreated");
         mCvCamera = new VideoCapture(Highgui.CV_CAP_ANDROID);
         if (mCvCamera.isOpened()) {
             (new Thread(this)).start();
         } else {
             mCvCamera.release();
             mCvCamera = null;             
             Log.e(TAG, "Failed to open native camera");
         }
     }
 
     public void surfaceDestroyed(SurfaceHolder holder) {
         Log.i(TAG, "surfaceDestroyed");
         if (mCvCamera != null) {
             synchronized (this) {
                 mCvCamera.release();
                 mCvCamera = null;             
             }
         }
     }
 
     protected abstract Bitmap processFrame(VideoCapture capture);
 
     public void run() {
         Log.i(TAG, "Starting processing thread");
         mFps.init();
 
         while (true) {
             Bitmap bmp = null;
 
             synchronized (this) {
                 if (mCvCamera == null)
                     break;
 
                 if (!mCvCamera.grab()) {
                     Log.e(TAG, "mCvCamera.grab() failed");
                     break;
                 }
 
                 bmp = processFrame(mCvCamera);
 
                 mFps.measure();
             }
 
             if (bmp != null) {
                 Canvas canvas = mHolder.lockCanvas();
                 if (canvas != null) {
                 	canvas.rotate(90, canvas.getWidth()/2, canvas.getHeight()/2);
                     canvas.drawBitmap(bmp, (canvas.getWidth() - bmp.getWidth()) / 2, (canvas.getHeight() - bmp.getHeight()) / 2, null);
                     canvas.rotate(-90, canvas.getWidth()/2, canvas.getHeight()/2);
                     mFps.draw(canvas, canvas.getWidth()/2+20, canvas.getHeight()-25);
                     mHolder.unlockCanvasAndPost(canvas);
                 }
                 bmp.recycle();
             }
         }
 
         Log.i(TAG, "Finishing processing thread");
     }
 }
