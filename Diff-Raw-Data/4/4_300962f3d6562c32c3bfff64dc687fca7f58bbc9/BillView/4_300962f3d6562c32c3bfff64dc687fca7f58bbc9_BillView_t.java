 /*
  * Copyright (C) 2012 Jimmy Theis. Licensed under the MIT License:
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.jetheis.android.makeitrain;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 public class BillView extends SurfaceView implements SurfaceHolder.Callback {
 
     class BillThread extends Thread {
 
         private SurfaceHolder mSurfaceHolder;
         private Context mContext;
         private Resources mResources;
         private SharedPreferences mPrefs;
 
         private Bitmap mBillImage;
 
         private boolean mRunning = false;
         private boolean mFlinging = false;
 
         private int mBillY;
         private int mBillHeight;
         private int mFlingVelocity;
 
         private int mImageResource;
         private int mDenomination;
         private int mTotalSpent;
 
         public BillThread(SurfaceHolder surfaceHolder, Context context, Handler handler,
                 int imageResource, int denomination) {
             mSurfaceHolder = surfaceHolder;
             mContext = context;
 
             mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
             mTotalSpent = mPrefs.getInt(context.getString(R.string.pref_total_spent), 0);
 
             mResources = context.getResources();
 
             setImageResource(imageResource);
             setDenomination(denomination);
 
             mBillHeight = mBillImage.getHeight();
         }
 
         public boolean isRunning() {
             return mRunning;
         }
 
         public void setIsRunning(boolean isRunning) {
             mRunning = isRunning;
         }
 
         public boolean isFlinging() {
             return mFlinging;
         }
 
         public void setIsFlinging(boolean isFlinging) {
             mFlinging = isFlinging;
         }
 
         public int getDenomination() {
             return mDenomination;
         }
 
         public void setDenomination(int denomination) {
             mDenomination = denomination;
         }
 
         public int getImageResource() {
             return mImageResource;
         }
 
         public void setImageResource(int imageResource) {
             mImageResource = imageResource;
             mBillImage = BitmapFactory.decodeResource(mResources, mImageResource);
         }
 
         private void drawOn(Canvas canvas) {
            if (canvas == null) {
                return;
            }

             if (isFlinging() && mBillY > -mBillHeight) {
                 mBillY -= mFlingVelocity;
             } else if (isFlinging()) {
                 mBillY = 0;
                 setIsFlinging(false);
                 mTotalSpent = mPrefs.getInt(mResources.getString(R.string.pref_total_spent), 0);
                 mTotalSpent += getDenomination();
 
                 Editor editor = mPrefs.edit();
                 editor.putInt(mContext.getString(R.string.pref_total_spent), mTotalSpent);
                 editor.commit();
             }
             canvas.drawBitmap(mBillImage, 0, 0, null);
             canvas.drawBitmap(mBillImage, 0, mBillY, null);
         }
 
         public void updateBillPostion(int y) {
             mBillY = y;
         }
 
         public void initiateFling(int velocity) {
             mFlingVelocity = velocity;
             mFlinging = true;
         }
 
         public void run() {
             Canvas canvas = null;
 
             while (mRunning) {
                 try {
                     canvas = mSurfaceHolder.lockCanvas();
                     drawOn(canvas);
                 } finally {
                     if (canvas != null) {
                         mSurfaceHolder.unlockCanvasAndPost(canvas);
                     }
                 }
             }
         }
     }
 
     private BillThread mThread;
     private boolean mDragging = false;
 
     private Context mContext;
 
     private int mDragYOffset;
     private int mLastMoveY = 0;
     private int mVelocity = 0;
 
     public BillView(Context context, AttributeSet attrs) {
         super(context, attrs);
 
         SurfaceHolder holder = getHolder();
         holder.addCallback(this);
 
         mContext = context;
 
         mThread = new BillThread(holder, context, null, R.drawable.bill_1_left,
                 R.string.denomination_1);
 
         setFocusable(true);
     }
 
     @Override
     public boolean onTouchEvent(MotionEvent event) {
         super.onTouchEvent(event);
 
         if (mThread.isFlinging()) {
             return false;
         }
 
         switch (event.getAction()) {
 
         case MotionEvent.ACTION_DOWN:
             mDragging = true;
             mDragYOffset = (int) event.getY();
             return true;
 
         case MotionEvent.ACTION_MOVE:
             if (mDragging) {
                 int newY = (int) event.getY() - mDragYOffset;
                 mVelocity = Math.min(Math.max(mLastMoveY - newY, 80), 150);
                 mLastMoveY = newY;
                 mThread.updateBillPostion(Math.min(0, newY));
             }
             return true;
 
         case MotionEvent.ACTION_UP:
             mDragging = false;
             mThread.initiateFling(mVelocity);
             return true;
         }
 
         return false;
     }
 
     public BillThread getThread() {
         return mThread;
     }
 
     public int getDenomination() {
         return mThread.getDenomination();
     }
 
     public void setDenomination(int denomination) {
         mThread.setDenomination(denomination);
     }
 
     public int getImageResource() {
         return mThread.getImageResource();
     }
 
     public void setImageResource(int imageResource) {
         mThread.setImageResource(imageResource);
     }
 
     public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
         // TODO Auto-generated method stub
     }
 
     public void surfaceCreated(SurfaceHolder holder) {
         try {
             mThread.setIsRunning(true);
             mThread.start();
         } catch (IllegalThreadStateException e) {
             mThread = new BillThread(holder, mContext, null, getImageResource(), getDenomination());
             mThread.setIsRunning(true);
             mThread.start();
         }
     }
 
     public void surfaceDestroyed(SurfaceHolder holder) {
         boolean retry = true;
         mThread.setIsRunning(false);
         while (retry) {
             try {
                 mThread.join();
                 retry = false;
             } catch (InterruptedException e) {
                 // Just eat it and try again
             }
         }
     }
 }
