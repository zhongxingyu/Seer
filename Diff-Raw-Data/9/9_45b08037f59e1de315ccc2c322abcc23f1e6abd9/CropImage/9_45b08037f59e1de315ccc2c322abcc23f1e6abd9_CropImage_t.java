 package com.episode6.android.common.ui.image.crop;
 
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
 
 // originally from AOSP Camera code. modified to only do cropping and return 
 // data to caller. Removed saving to file, MediaManager, unneeded options, etc.
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.concurrent.CountDownLatch;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Matrix;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.graphics.Bitmap.CompressFormat;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 
 import com.episode6.android.common.R;
 import com.episode6.android.common.util.ImageUtils;
 
 /**
  * The activity can crop specific region of interest from an image.
  */
 public class CropImage extends MonitoredActivity {
 	
 
 	public static final int ACTIVITY_RESULT_CROP_ERROR = 1752925335;
 	public static final int ACTIVITY_RESULT_IMAGE_LOAD_ERROR = 1752817635;
 	
     public static final String EXTRA_BITMAP_DATA = "data";
     public static final String EXTRA_ASPECT_X = "aspectX";
     public static final String EXTRA_ASPECT_Y = "aspectY";
     public static final String EXTRA_OUTPUT_X = "outputX";
     public static final String EXTRA_OUTPUT_Y = "outputY";
     public static final String EXTRA_SCALE = "scale";
     public static final String EXTRA_SCALE_UP_IF_NEEDED = "scaleUpIfNeeded";
     public static final String EXTRA_SAVE_QUALITY = "saveQuality";
     public static final String EXTRA_OUTPUT_FILE_PATH = "outputFilePath";
     
     protected static final int DIALOG_CROPPING = 834;
     
 //    private static final String TEMP_FILE_PREFIX = "croppedImage";
 //    private static final String TEMP_FILE_SUFFIX = ".jpg";
 
      private static final String TAG = CropImage.class.getSimpleName();
 
     private static final boolean RECYCLE_INPUT = true;
    
    private boolean mIsHighlightViewOnScreen = false;
 
     private int mAspectX, mAspectY;
     private final Handler mHandler = new Handler();
 
     // These options specifiy the output image size and whether we should
     // scale the output to fit it (or just crop it).
     private int mOutputX, mOutputY;
     private boolean mScale;
     private boolean mScaleUp = true;
     private boolean mCircleCrop = false;
     private String mOutputFilePath = null;
     
     private int mSaveQuality;
 
     boolean mSaving; // Whether the "save" button is already clicked.
 
     private CropImageView mImageView;
 
     private Bitmap mBitmap;
     HighlightView mCrop;
     
     private Uri originalBitmapUri;
     private static final int MAX_WIDTH = 400;
     private static final int MAX_HEIGHT = 400;
 
     @Override
     public void onCreate(Bundle icicle) {
         super.onCreate(icicle);
 
         // Make UI fullscreen.
         getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.cropimage);
 
         mImageView = (CropImageView) findViewById(R.id.image);
         mImageView.mContext = this;
 
         // MenuHelper.showStorageToast(this);
 
     }
     
     public void onStart(){
     	super.onStart();
     	
         Intent intent = getIntent();
         Bundle extras = intent.getExtras();
         
 
 
         if (extras != null) {
             mBitmap = (Bitmap) extras.getParcelable(EXTRA_BITMAP_DATA);
             mAspectX = extras.getInt(EXTRA_ASPECT_X);
             mAspectY = extras.getInt(EXTRA_ASPECT_Y);
             mOutputX = extras.getInt(EXTRA_OUTPUT_X);
             mOutputY = extras.getInt(EXTRA_OUTPUT_Y);
             mScale = extras.getBoolean(EXTRA_SCALE, true);
             mScaleUp = extras.getBoolean(EXTRA_SCALE_UP_IF_NEEDED, true);
             mSaveQuality = extras.getInt(EXTRA_SAVE_QUALITY, 80);
             mOutputFilePath = extras.getString(EXTRA_OUTPUT_FILE_PATH);
         }
 
         if (mBitmap == null) {
             InputStream is = null;
             try {
             	originalBitmapUri = intent.getData();
                 ContentResolver cr = getContentResolver();
                 is = cr.openInputStream(originalBitmapUri);
                 mBitmap = BitmapFactory.decodeStream(is);
             } catch (IOException e) {
                 throw new RuntimeException(e);
             } finally {
                 if (is != null) {
                     try {
                         is.close();
                     } catch (IOException ignored) {
                     }
                 }
             }
         }
 
         if (mBitmap == null) {
         	setResult(ACTIVITY_RESULT_IMAGE_LOAD_ERROR);
             finish();
             return;
         }
 
 
         findViewById(R.id.discard).setOnClickListener(
                 new View.OnClickListener() {
                     public void onClick(View v) {
                         setResult(Activity.RESULT_CANCELED);
                         finish();
                     }
                 });
 
         findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 onSaveClicked();
             }
         });
 
         startFaceDetection();
     }
     
     public void onStop(){
     	super.onStop();
     	if(mBitmap!=null){
     		mBitmap.recycle();
     	}
     	
     }
     
     
 
     @Override
 	protected Dialog onCreateDialog(int id) {
 		if (id == DIALOG_CROPPING) {
 			ProgressDialog d = new ProgressDialog(this);
 			d.setMessage(getString(R.string.progress_cropping));
 			d.setIndeterminate(true);
 			d.setCancelable(false);
 			return d;
 		}
 		return super.onCreateDialog(id);
 	}
 
 
 
 	private void startFaceDetection() {
         if (isFinishing()) {
             return;
         }
 
        
         mImageView.setImageBitmapResetBase(mBitmap, true);
 
         startBackgroundJob(this, null, getResources().getString(
                 R.string.running_face_detection), new Runnable() {
             public void run() {
                 final CountDownLatch latch = new CountDownLatch(1);
                 final Bitmap b = mBitmap;
                 mHandler.post(new Runnable() {
                     public void run() {
                         if (b != mBitmap && b != null) {
                             mImageView.setImageBitmapResetBase(b, true);
                             mBitmap.recycle();
                             mBitmap = b;
                         }
                         if (mImageView.getScale() == 1F) {
                             mImageView.center(true, true);
                         }
                         latch.countDown();
                     }
                 });
                 try {
                     latch.await();
                 } catch (InterruptedException e) {
                     throw new RuntimeException(e);
                 }
                 mRunFaceDetection.run();
             }
         }, mHandler);
     }
 
     private static class BackgroundJob extends
             MonitoredActivity.LifeCycleAdapter implements Runnable {
 
         private final MonitoredActivity mActivity;
         private final ProgressDialog mDialog;
         private final Runnable mJob;
         private final Handler mHandler;
         private final Runnable mCleanupRunner = new Runnable() {
             public void run() {
                 mActivity.removeLifeCycleListener(BackgroundJob.this);
                 if (mDialog.getWindow() != null)
                     mDialog.dismiss();
             }
         };
 
         public BackgroundJob(MonitoredActivity activity, Runnable job,
                 ProgressDialog dialog, Handler handler) {
             mActivity = activity;
             mDialog = dialog;
             mJob = job;
             mActivity.addLifeCycleListener(this);
             mHandler = handler;
         }
 
         public void run() {
             try {
                 mJob.run();
             } finally {
                 mHandler.post(mCleanupRunner);
             }
         }
 
         @Override
         public void onActivityDestroyed(MonitoredActivity activity) {
             // We get here only when the onDestroyed being called before
             // the mCleanupRunner. So, run it now and remove it from the queue
             mCleanupRunner.run();
             mHandler.removeCallbacks(mCleanupRunner);
         }
 
         @Override
         public void onActivityStopped(MonitoredActivity activity) {
             mDialog.hide();
         }
 
         @Override
         public void onActivityStarted(MonitoredActivity activity) {
             mDialog.show();
         }
     }
 
     private static void startBackgroundJob(MonitoredActivity activity,
             String title, String message, Runnable job, Handler handler) {
         // Make the progress dialog uncancelable, so that we can gurantee
         // the thread will be done before the activity getting destroyed.
         ProgressDialog dialog = ProgressDialog.show(activity, title, message,
                 true, false);
         new Thread(new BackgroundJob(activity, job, dialog, handler)).start();
     }
 
     Runnable mRunFaceDetection = new Runnable() {
         float mScale = 1F;
         Matrix mImageMatrix;
 
         // Create a default HightlightView if we found no face in the picture.
         private void makeDefault() {
             HighlightView hv = new HighlightView(mImageView);
 
             int width = mBitmap.getWidth();
             int height = mBitmap.getHeight();
 
             Rect imageRect = new Rect(0, 0, width, height);
 
             // make the default size about 4/5 of the width or height
             int cropWidth = Math.min(width, height) * 4 / 5;
             int cropHeight = cropWidth;
 
             if (mAspectX != 0 && mAspectY != 0) {
                 if (mAspectX > mAspectY) {
                     cropHeight = cropWidth * mAspectY / mAspectX;
                 } else {
                     cropWidth = cropHeight * mAspectX / mAspectY;
                 }
             }
 
             int x = (width - cropWidth) / 2;
             int y = (height - cropHeight) / 2;
 
             RectF cropRect = new RectF(x, y, x + cropWidth, y + cropHeight);
             hv.setup(mImageMatrix, imageRect, cropRect, mCircleCrop,
                     mAspectX != 0 && mAspectY != 0);
             mImageView.add(hv);
         }
 
         public void run() {
             mImageMatrix = mImageView.getImageMatrix();
 
             mScale = 1.0F / mScale;
             mHandler.post(new Runnable() {
                 public void run() {
                	if (mImageView.mHighlightViews.size() < 1) {
                		makeDefault();
                	}
 
                     mImageView.invalidate();
                     if (mImageView.mHighlightViews.size() == 1) {
                         mCrop = mImageView.mHighlightViews.get(0);
                         mCrop.setFocus(true);
                     }
                 }
             });
         }
     };
 
     private void onSaveClicked() {
     	
         if (mCrop == null) {
             return;
         }
 
         if (mSaving)
             return;
         mSaving = true;
         
         new CropTask().execute((Void)null);
     }
 
 
     private class CropTask extends AsyncTask<Void, Void, Void> {
     	
     	Uri returnedUri = null;
     	File returnedFile = null;
 
 		@Override
 		protected void onPreExecute() {
 			showDialog(DIALOG_CROPPING);
 			mImageView.clear();
 			mImageView.mHighlightViews.clear();
 			mBitmap.recycle();
 			mBitmap = null;
 			System.gc();
 			super.onPreExecute();
 		}
 		
 		@Override
 		protected Void doInBackground(Void... params) {
 			doCropAndSave();
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Void result) {
 			try {
 				dismissDialog(DIALOG_CROPPING);
 			} catch (Exception e) {
 				//empty
 			}
 			
 	        if (returnedUri == null) {
 	        	setResult(ACTIVITY_RESULT_CROP_ERROR);
 	        } else {
 	        	Intent i = new Intent();
 	        	if (returnedUri != null)
 	        		i.setData(returnedUri);
 	        	else if (returnedFile != null) 
 	        		i.putExtra(EXTRA_OUTPUT_FILE_PATH, returnedFile.getAbsolutePath());
 	        	setResult(RESULT_OK, i);
 	        }
 	        finish();
 			super.onPostExecute(result);
 		}
 
 	    private void doCropAndSave(){	    	
 	    	InputStream input = null;
 	        Bitmap croppedImage;
 	    	int load_scale = 1;
 	    	
 	    	
             try {
 
                 ContentResolver cr = getContentResolver();
                 input = cr.openInputStream(originalBitmapUri);
                 
                 //calc scale to reduce by. scale must be power of 2 (1,2,4,8,16...)
                 load_scale = calculateLoadScale(input);
 
 	        	//Now load image with precalculated scale
 	        	BitmapFactory.Options option_to_load = new BitmapFactory.Options();
 	        	option_to_load.inSampleSize = load_scale;
 	        	((FileInputStream)input).getChannel().position(0); // reset input stream to read again
 	        	mBitmap = BitmapFactory.decodeStream(input, null, option_to_load);
 
 		    	Log.d(TAG, "bitmap HxW: " + mBitmap.getHeight() + "x"+mBitmap.getWidth());
 
             } catch (IOException e) {
             	//TODO: check to see that the returning activity handles and error message
             	
             	setResult(ACTIVITY_RESULT_CROP_ERROR);
             	finish();
             	try{if(input!=null){input.close();}}catch(Exception ignore){}
             } 
             
           
            
 
 
 	        // If the output is required to a specific size, create an new image
 	        // with the cropped image in the center and the extra space filled.
 	        if (mOutputX != 0 && mOutputY != 0 && !mScale) {
 	            // Don't scale the image but instead fill it so it's the
 	            // required dimension
 	            croppedImage = Bitmap.createBitmap(mOutputX, mOutputY,
 	                    Bitmap.Config.RGB_565);
 	            Canvas canvas = new Canvas(croppedImage);
 
 	            Rect srcRect = mCrop.getCropRect();
 	            Rect dstRect = new Rect(0, 0, mOutputX, mOutputY);
 
 	            int dx = (srcRect.width() - dstRect.width()) / 2;
 	            int dy = (srcRect.height() - dstRect.height()) / 2;
 
 	            // If the srcRect is too big, use the center part of it.
 	            srcRect.inset(Math.max(0, dx), Math.max(0, dy));
 
 	            // If the dstRect is too big, use the center part of it.
 	            dstRect.inset(Math.max(0, -dx), Math.max(0, -dy));
 
 	            // Draw the cropped bitmap in the center
 	            canvas.drawBitmap(mBitmap, srcRect, dstRect, null);
 	            
 
 	            mBitmap.recycle();
                 System.gc();
 	        } else {
 	        	
 
 	            Rect r = mCrop.getCropRect();
 	            r.set(r.left/load_scale, r.top/load_scale, r.right/load_scale, r.bottom/load_scale);
 	            int width = r.width();
 	            int height = r.height();
 
 
 	            croppedImage = Bitmap.createBitmap(width, height,
 	                    Bitmap.Config.RGB_565);
 
 	            Canvas canvas = new Canvas(croppedImage);
 	            Rect dstRect = new Rect(0, 0, width, height);
 	            canvas.drawBitmap(mBitmap, r, dstRect, null);
 
 	            mBitmap.recycle();
                 System.gc();
 
 	            // If the required dimension is specified, scale the image.
 	            if (mOutputX != 0 && mOutputY != 0 && mScale) {
 	                croppedImage = transform(new Matrix(), croppedImage, mOutputX,
 	                        mOutputY, mScaleUp, RECYCLE_INPUT);
 	            }
 	        }
 
 	        
 	        try{if(input!=null){input.close();}}catch(Exception ignore){}
 	        
 	        boolean addToGallery = (mOutputFilePath == null);
 
 	        
         	try {
         		
         		
         		if (addToGallery) {
         			returnedUri = ImageUtils.putBitmapIntoGalleryAndGetUri(CropImage.this, croppedImage, true);
         		} else {
         			File tmpFile = new File(mOutputFilePath);
         			FileOutputStream outputStream = new FileOutputStream(tmpFile);
     	        	croppedImage.compress(CompressFormat.JPEG, mSaveQuality, outputStream);
     	        	outputStream.close();
     	        	croppedImage.recycle();
     	        	returnedFile = tmpFile;
         		}
                 System.gc();
         		
         		
         		
 //        		if (addToGallery)
 //        			tmpFile = File.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX, getFilesDir());
 //        		else 
 //        			tmpFile = new File(mOutputFilePath);
 //	        	FileOutputStream outputStream = new FileOutputStream(tmpFile);
 //	        	croppedImage.compress(CompressFormat.JPEG, mSaveQuality, outputStream);
 //	        	outputStream.close();
 //	        	
 //	        	croppedImage.recycle();
 //	        	
 //	        	if (addToGallery)
 //	        		returnedUri = ImageUtils.putImageFileIntoGalleryAndGetUri(CropImage.this, tmpFile, true);
 //	        	else 
 //	        		returnedFile = tmpFile;
 	        	
         	} catch(Exception e) {
         		e.printStackTrace();
         		try {
         			croppedImage.recycle();
         		} catch (Exception e1) {}
         	}
 //        	if (addToGallery && tmpFile != null) {
 //        		tmpFile.delete();
 //        	}
 	    }
 
 		private int calculateLoadScale(InputStream input) {
 			BitmapFactory.Options options_to_get_size = new BitmapFactory.Options();
 			options_to_get_size.inJustDecodeBounds = true;
 			BitmapFactory.decodeStream(input, null, options_to_get_size);
 			int load_scale = 1; // load 100% sized image
 			int width_tmp=options_to_get_size.outWidth;
 			int height_tmp=options_to_get_size.outHeight;
 
 			while(width_tmp/2>MAX_WIDTH && height_tmp/2>MAX_HEIGHT){
 				width_tmp/=2;//load half sized image
 				height_tmp/=2;
 				load_scale*=2;
 			}
 			Log.d(TAG,"load inSampleSize: "+ load_scale);
 			return load_scale;
 		}
     	
     }
     
     private static Bitmap transform(Matrix scaler, Bitmap source,
             int targetWidth, int targetHeight, boolean scaleUp, boolean recycle) {
         int deltaX = source.getWidth() - targetWidth;
         int deltaY = source.getHeight() - targetHeight;
         if (!scaleUp && (deltaX < 0 || deltaY < 0)) {
             /*
              * In this case the bitmap is smaller, at least in one dimension,
              * than the target. Transform it by placing as much of the image as
              * possible into the target and leaving the top/bottom or left/right
              * (or both) black.
              */
             Bitmap b2 = Bitmap.createBitmap(targetWidth, targetHeight,
                     Bitmap.Config.ARGB_8888);
             Canvas c = new Canvas(b2);
 
             int deltaXHalf = Math.max(0, deltaX / 2);
             int deltaYHalf = Math.max(0, deltaY / 2);
             Rect src = new Rect(deltaXHalf, deltaYHalf, deltaXHalf
                     + Math.min(targetWidth, source.getWidth()), deltaYHalf
                     + Math.min(targetHeight, source.getHeight()));
             int dstX = (targetWidth - src.width()) / 2;
             int dstY = (targetHeight - src.height()) / 2;
             Rect dst = new Rect(dstX, dstY, targetWidth - dstX, targetHeight
                     - dstY);
             c.drawBitmap(source, src, dst, null);
             if (recycle) {
                 source.recycle();
             }
             return b2;
         }
         float bitmapWidthF = source.getWidth();
         float bitmapHeightF = source.getHeight();
 
         float bitmapAspect = bitmapWidthF / bitmapHeightF;
         float viewAspect = (float) targetWidth / targetHeight;
 
         if (bitmapAspect > viewAspect) {
             float scale = targetHeight / bitmapHeightF;
             if (scale < .9F || scale > 1F) {
                 scaler.setScale(scale, scale);
             } else {
                 scaler = null;
             }
         } else {
             float scale = targetWidth / bitmapWidthF;
             if (scale < .9F || scale > 1F) {
                 scaler.setScale(scale, scale);
             } else {
                 scaler = null;
             }
         }
 
         Bitmap b1;
         if (scaler != null) {
             // this is used for minithumb and crop, so we want to filter here.
             b1 = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source
                     .getHeight(), scaler, true);
         } else {
             b1 = source;
         }
 
         if (recycle && b1 != source) {
             source.recycle();
         }
 
         int dx1 = Math.max(0, b1.getWidth() - targetWidth);
         int dy1 = Math.max(0, b1.getHeight() - targetHeight);
 
         Bitmap b2 = Bitmap.createBitmap(b1, dx1 / 2, dy1 / 2, targetWidth,
                 targetHeight);
 
         if (b2 != b1) {
             if (recycle || b1 != source) {
                 b1.recycle();
             }
         }
 
         return b2;
     }
 
     @Override
     protected void onPause() {
         super.onPause();
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
     }
 
 }
 
 class CropImageView extends ImageViewTouchBase {
     ArrayList<HighlightView> mHighlightViews = new ArrayList<HighlightView>();
     HighlightView mMotionHighlightView = null;
     float mLastX, mLastY;
     int mMotionEdge;
 
     Context mContext;
 
     @Override
     protected void onLayout(boolean changed, int left, int top, int right,
             int bottom) {
         super.onLayout(changed, left, top, right, bottom);
         if (mBitmapDisplayed.getBitmap() != null) {
             for (HighlightView hv : mHighlightViews) {
                 hv.mMatrix.set(getImageMatrix());
                 hv.invalidate();
                 if (hv.mIsFocused) {
                     centerBasedOnHighlightView(hv);
                 }
             }
         }
     }
 
     public CropImageView(Context context, AttributeSet attrs) {
         super(context, attrs);
     }
 
     @Override
     protected void zoomTo(float scale, float centerX, float centerY) {
         super.zoomTo(scale, centerX, centerY);
         for (HighlightView hv : mHighlightViews) {
             hv.mMatrix.set(getImageMatrix());
             hv.invalidate();
         }
     }
 
     @Override
     protected void zoomIn() {
         super.zoomIn();
         for (HighlightView hv : mHighlightViews) {
             hv.mMatrix.set(getImageMatrix());
             hv.invalidate();
         }
     }
 
     @Override
     protected void zoomOut() {
         super.zoomOut();
         for (HighlightView hv : mHighlightViews) {
             hv.mMatrix.set(getImageMatrix());
             hv.invalidate();
         }
     }
 
     @Override
     protected void postTranslate(float deltaX, float deltaY) {
         super.postTranslate(deltaX, deltaY);
         for (int i = 0; i < mHighlightViews.size(); i++) {
             HighlightView hv = mHighlightViews.get(i);
             hv.mMatrix.postTranslate(deltaX, deltaY);
             hv.invalidate();
         }
     }
 
     @Override
     public boolean onTouchEvent(MotionEvent event) {
         CropImage cropImage = (CropImage) mContext;
         if (cropImage.mSaving) {
             return false;
         }
 
         switch (event.getAction()) {
         case MotionEvent.ACTION_DOWN:
             for (int i = 0; i < mHighlightViews.size(); i++) {
                 HighlightView hv = mHighlightViews.get(i);
                 int edge = hv.getHit(event.getX(), event.getY());
                 if (edge != HighlightView.GROW_NONE) {
                     mMotionEdge = edge;
                     mMotionHighlightView = hv;
                     mLastX = event.getX();
                     mLastY = event.getY();
                     mMotionHighlightView
                             .setMode((edge == HighlightView.MOVE) ? HighlightView.ModifyMode.Move
                                     : HighlightView.ModifyMode.Grow);
                     break;
                 }
             }
             break;
         case MotionEvent.ACTION_UP:
             if (mMotionHighlightView != null) {
                 centerBasedOnHighlightView(mMotionHighlightView);
                 mMotionHighlightView.setMode(HighlightView.ModifyMode.None);
             }
             mMotionHighlightView = null;
             break;
         case MotionEvent.ACTION_MOVE:
             if (mMotionHighlightView != null) {
                 mMotionHighlightView.handleMotion(mMotionEdge, event.getX()
                         - mLastX, event.getY() - mLastY);
                 mLastX = event.getX();
                 mLastY = event.getY();
 
                 if (true) {
                     // This section of code is optional. It has some user
                     // benefit in that moving the crop rectangle against
                     // the edge of the screen causes scrolling but it means
                     // that the crop rectangle is no longer fixed under
                     // the user's finger.
                     ensureVisible(mMotionHighlightView);
                 }
             }
             break;
         }
 
         switch (event.getAction()) {
         case MotionEvent.ACTION_UP:
             center(true, true);
             break;
         case MotionEvent.ACTION_MOVE:
             // if we're not zoomed then there's no point in even allowing
             // the user to move the image around. This call to center puts
             // it back to the normalized location (with false meaning don't
             // animate).
             if (getScale() == 1F) {
                 center(true, true);
             }
             break;
         }
 
         return true;
     }
 
     // Pan the displayed image to make sure the cropping rectangle is visible.
     private void ensureVisible(HighlightView hv) {
         Rect r = hv.mDrawRect;
 
         int panDeltaX1 = Math.max(0, getLeft() - r.left);
         int panDeltaX2 = Math.min(0, getRight() - r.right);
 
         int panDeltaY1 = Math.max(0, getTop() - r.top);
         int panDeltaY2 = Math.min(0, getBottom() - r.bottom);
 
         int panDeltaX = panDeltaX1 != 0 ? panDeltaX1 : panDeltaX2;
         int panDeltaY = panDeltaY1 != 0 ? panDeltaY1 : panDeltaY2;
 
         if (panDeltaX != 0 || panDeltaY != 0) {
             panBy(panDeltaX, panDeltaY);
         }
     }
 
     // If the cropping rectangle's size changed significantly, change the
     // view's center and scale according to the cropping rectangle.
     private void centerBasedOnHighlightView(HighlightView hv) {
         Rect drawRect = hv.mDrawRect;
 
         float width = drawRect.width();
         float height = drawRect.height();
 
         float thisWidth = getWidth();
         float thisHeight = getHeight();
 
         float z1 = thisWidth / width * .6F;
         float z2 = thisHeight / height * .6F;
 
         float zoom = Math.min(z1, z2);
         zoom = zoom * this.getScale();
         zoom = Math.max(1F, zoom);
 
         if ((Math.abs(zoom - getScale()) / zoom) > .1) {
             float[] coordinates = new float[] { hv.mCropRect.centerX(),
                     hv.mCropRect.centerY() };
             getImageMatrix().mapPoints(coordinates);
             zoomTo(zoom, coordinates[0], coordinates[1], 300F);
         }
 
         ensureVisible(hv);
     }
 
     @Override
     protected void onDraw(Canvas canvas) {
     	try {
 	        super.onDraw(canvas);
 	        for (int i = 0; i < mHighlightViews.size(); i++) {
 	            mHighlightViews.get(i).draw(canvas);
 	        }
     	} catch (Exception e) {
     		e.printStackTrace();
     	}
     }
 
     public void add(HighlightView hv) {
         mHighlightViews.add(hv);
         invalidate();
     }
 }
