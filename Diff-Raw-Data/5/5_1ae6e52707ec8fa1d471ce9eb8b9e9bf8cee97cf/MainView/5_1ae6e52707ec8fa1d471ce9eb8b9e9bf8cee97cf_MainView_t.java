 /*
  * Copyright (C) 2011 Ron Huang
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 
 package com.example.vistroller.dualshock;
 
 import android.view.View;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.util.AttributeSet;
 import android.view.KeyEvent;
 import android.util.Log;
 import android.os.Handler;
 
 
 public class MainView extends View {
     private Paint mPaint;
     private Paint mSolidPaint;
 
     private String mShowOnScreen;
 
     private static final String[] mButtonLabels = {
         "Left", "Right", "Square", "Crose",
         "Up", "Down", "Triangle", "Circle",
         "Select", "Start",
         "L1", "L2", "L3",
         "R1", "R2", "R3"
     };
 
     private int mCount;
     private static final String[] mSpinLabels = {"-", "\\", "/"};
 
     private int mProgress;
     private boolean mShowProgress;
 
     // Log tag
     private static final String TAG = "MainView";
 
 
     public MainView(Context context) {
         super(context);
 
         init();
     }
 
 
     public MainView(Context context, AttributeSet attrs) {
         super(context, attrs);
 
         init();
     }
 
 
     public MainView(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
 
         init();
     }
 
 
     private void init() {
         // Initialize paint
         mPaint = new Paint();
         mPaint.setDither(true);
         mPaint.setColor(0xFFFFFF00);
         mPaint.setStyle(Paint.Style.STROKE);
         mPaint.setStrokeJoin(Paint.Join.ROUND);
         mPaint.setStrokeCap(Paint.Cap.ROUND);
         mPaint.setStrokeWidth(1);
         mPaint.setTextSize(30.0f);
 
         mSolidPaint = new Paint(mPaint);
         mSolidPaint.setStyle(Paint.Style.FILL);
     }
 
 
     protected void setProgress(int progress) {
         mProgress = progress;
         mShowProgress = true;
 
         Log.d(TAG, String.format("setProgress:%d", progress));
 
         invalidate();
 
         if (mProgress >= 100) {
             Handler handler = new Handler();
             handler.postDelayed(
                 new Runnable() {
                     public void run() {
                         mShowProgress = false;
                         invalidate();
                     }
                 }, 500);
         }
     }
 
 
     @Override
     protected void onDraw(Canvas canvas) {
         if (null != mShowOnScreen)
             canvas.drawText(mShowOnScreen, 200.0f, 200.0f, mSolidPaint);
 
         if (mShowProgress) {
             int blockSize = 40;
             int gapSize = 10;
             int blockCount = 10;
 
             float y = (getHeight() - blockSize) / 2f;
             float x = (getWidth() - blockSize * blockCount - gapSize * (blockCount - 1)) / 2f;
 
             for (int i = 0; i < blockCount; i++) {
                 float left = x + i * (blockSize + gapSize);
                 if (i * 100 <= mProgress * blockCount)
                     canvas.drawRect(left, y, left + blockSize, y + blockSize, mSolidPaint);
                 else
                     canvas.drawRect(left, y, left + blockSize, y + blockSize, mPaint);
             }
         }
     }
 
 
     private String getButtonLabel(int keyCode) {
         int index = keyCode - KeyEvent.KEYCODE_A;
 
         if (index < 0 || index >= mButtonLabels.length) {
             // unrecognized key code
             return "";
         }
 
         return mButtonLabels[index];
     }
 
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         Log.d(TAG, "MainView::onKeyDown: " + keyCode);
 
         mShowOnScreen = String.format("[%s] %s",
                                      mSpinLabels[mCount % mSpinLabels.length],
                                       getButtonLabel(keyCode));
        mCount++;
 
         invalidate();
 
         return false;
     }
 }
