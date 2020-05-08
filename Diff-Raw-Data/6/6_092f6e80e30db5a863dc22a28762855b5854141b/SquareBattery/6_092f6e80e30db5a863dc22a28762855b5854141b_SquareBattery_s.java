 /*
  * Copyright (C) 2012 Sven Dawitz for the CyanogenMod Project
  * Copyright (C) 2013 The Collective
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
 
 package com.android.systemui.statusbar.policy;
 
 import android.view.ViewGroup.LayoutParams;
 import android.content.BroadcastReceiver;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.res.Resources;
 import android.database.ContentObserver;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Align;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.os.BatteryManager;
 import android.os.Handler;
 import android.os.UserHandle;
 import android.provider.Settings;
 import android.util.AttributeSet;
 import android.view.View;
 import android.widget.ImageView;
 
 import com.android.systemui.R;
 
 /***
  * Note about SquareBattery Implementation:
  *
  * Unfortunately, we cannot use BatteryController here,
  * since communication between controller and this view is not possible without
  * huge changes. As a result, this Class is doing everything by itself,
  * monitoring battery level and battery settings.
  */
 
 public class SquareBattery extends ImageView {
     private Handler mHandler;
     private Context mContext;
     private BatteryReceiver mBatteryReceiver = null;
 
     // state variables
     private boolean mAttached;      // whether or not attached to a window
     private boolean mActivated;     // whether or not activated due to system settings
     private boolean mPercentage;    // whether or not to show percentage number
     private boolean mBatteryPlugged;// whether or not battery is currently plugged
 	private int     mBatteryColor;
 	private int     mBatteryFColor;
     private int     mBatteryStatus; // current battery status
     private int     mLevel;         // current battery level
     private int     mAnimOffset;    // current level of charging animation
     private boolean mIsAnimating;   // stores charge-animation status to reliably remove callbacks
 
     private int     mSquareHeight;    
 	private int     mSquareWidth;
     private RectF   mRectLeft;      // contains the precalculated rect used in drawArc(), derived from mSquareHeight
     private Float   mTextLeftX;     // precalculated x position for drawText() to appear centered
     private Float   mTextY;         // precalculated y position for drawText() to appear vertical-centered
     private Float   cLevel;
 
     // quiet a lot of paint variables. helps to move cpu-usage from actual drawing to initialization
     private Paint   mPaintFont;
 	private Paint   mPaintBatt;
 	private Paint   mPaintTip;
     private Paint   mPaintGray;
     private Paint   mPaintSystem;
     private Paint   mPaintRed;
 
     // runnable to invalidate view via mHandler.postDelayed() call
     private final Runnable mInvalidate = new Runnable() {
         public void run() {
             if(mActivated && mAttached) {
                 invalidate();
             }
         }
     };
 
     // observes changes in system battery settings and enables/disables view accordingly
     class SettingsObserver extends ContentObserver {
         SettingsObserver(Handler handler) {
             super(handler);
         }
 
         public void observe() {
             ContentResolver resolver = mContext.getContentResolver();
             resolver.registerContentObserver(Settings.System.getUriFor(
                     Settings.System.STATUS_BAR_BATTERY), false, this);
             onChange(true);
 			
 			resolver.registerContentObserver(Settings.System
                     .getUriFor(Settings.System.STATUSBAR_BATTERY_COLOR), true,
                     this);
 			onChange(true);	
 			
 			resolver.registerContentObserver(Settings.System
                     .getUriFor(Settings.System.STATUSBAR_BATTERY_FCOLOR), true,
                     this);
 			onChange(true);	
         }
 
         @Override
         public void onChange(boolean selfChange) {
 		
 		    int mDefaultColor = mContext.getResources().getColor(R.color.circle_battery_mod);
 		    int mDefaultFColor = mContext.getResources().getColor(R.color.circle_battery_font);
 			
 		    mBatteryColor = Settings.System.getIntForUser(mContext.getContentResolver(),
                 Settings.System.STATUSBAR_BATTERY_COLOR, mDefaultColor, UserHandle.USER_CURRENT);
 				
 			mBatteryFColor = Settings.System.getIntForUser(mContext.getContentResolver(),
                 Settings.System.STATUSBAR_BATTERY_FCOLOR, mDefaultFColor, UserHandle.USER_CURRENT);	
 		
             int batteryStyle = (Settings.System.getIntForUser(mContext.getContentResolver(),
                     Settings.System.STATUS_BAR_BATTERY, 0, UserHandle.USER_CURRENT));
 
             mActivated = (batteryStyle == BatteryController.BATTERY_STYLE_SQUARE || batteryStyle == BatteryController.BATTERY_STYLE_SQUARE_PERCENT);
             mPercentage = (batteryStyle == BatteryController.BATTERY_STYLE_SQUARE_PERCENT);
 
             setVisibility(mActivated && isBatteryPresent() ? View.VISIBLE : View.GONE);
             if (mBatteryReceiver != null) {
                 mBatteryReceiver.updateRegistration();
             }
 
             if (mActivated && mAttached) {
                 invalidate();
             }
         }
     }
 
     // keeps track of current battery level and charger-plugged-state
     class BatteryReceiver extends BroadcastReceiver {
         private boolean mIsRegistered = false;
 
         public BatteryReceiver(Context context) {
         }
 
         @Override
         public void onReceive(Context context, Intent intent) {
             final String action = intent.getAction();
             if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                 onBatteryStatusChange(intent);
 
                 int visibility = mActivated && isBatteryPresent() ? View.VISIBLE : View.GONE;
                 if (getVisibility() != visibility) {
                     setVisibility(visibility);
                 }
 
                 if (mActivated && mAttached) {
                     LayoutParams l = getLayoutParams();
                     l.width = mSquareWidth + getPaddingLeft();
                     setLayoutParams(l);
 
                     invalidate();
                 }
             }
         }
 
         private void registerSelf() {
             if (!mIsRegistered) {
                 mIsRegistered = true;
 
                 IntentFilter filter = new IntentFilter();
                 filter.addAction(Intent.ACTION_BATTERY_CHANGED);
                 mContext.registerReceiver(mBatteryReceiver, filter);
             }
         }
 
         private void unregisterSelf() {
             if (mIsRegistered) {
                 mIsRegistered = false;
                 mContext.unregisterReceiver(this);
             }
         }
 
         private void updateRegistration() {
             if (mActivated && mAttached) {
                 registerSelf();
             } else {
                 unregisterSelf();
             }
         }
     }
 
     /***
      * Start of SquareBattery implementation
      */
     public SquareBattery(Context context) {
         this(context, null);
     }
 
     public SquareBattery(Context context, AttributeSet attrs) {
         this(context, attrs, 0);
     }
 
     public SquareBattery(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
 
         mContext = context;
         mHandler = new Handler();
 
         SettingsObserver settingsObserver = new SettingsObserver(mHandler);
         settingsObserver.observe();
         mBatteryReceiver = new BatteryReceiver(mContext);
 
         // initialize and setup all paint variables
         // stroke width is later set in initSizeBasedStuff()
         Resources res = getResources();
 
         mPaintFont = new Paint();
         mPaintFont.setAntiAlias(true);
         mPaintFont.setDither(true);
         mPaintFont.setStyle(Paint.Style.STROKE);
 		
         mPaintBatt = new Paint();
         mPaintBatt.setAntiAlias(true);
         mPaintBatt.setDither(true);
         mPaintBatt.setStyle(Paint.Style.FILL);
 		
         mPaintGray = new Paint(mPaintFont);
 		mPaintTip = new Paint(mPaintBatt);
         mPaintSystem = new Paint(mPaintBatt);
         mPaintRed = new Paint(mPaintBatt);
 
         mPaintGray.setStrokeWidth(1);
         mPaintSystem.setStrokeWidth(0);
         mPaintRed.setStrokeWidth(0);
         mPaintTip.setStrokeWidth(1);
         mPaintFont.setColor(mBatteryFColor);
         mPaintSystem.setColor(mBatteryColor);
         // could not find the darker definition anywhere in resources
         // do not want to use static 0x404040 color value. would break theming.
         mPaintGray.setColor(res.getColor(R.color.darker_gray));
 	    mPaintTip.setColor(res.getColor(R.color.darker_gray));	
         mPaintRed.setColor(res.getColor(R.color.holo_red_light));
 
         // font needs some extra settings
         mPaintFont.setTextAlign(Align.CENTER);
         mPaintFont.setFakeBoldText(true);
     }
 
     protected int getLevel() {
         return mLevel;
     }
 
     protected int getBatteryStatus() {
         return mBatteryStatus;
     }
 
     protected boolean isBatteryPlugged() {
         return mBatteryPlugged;
     }
 
     protected boolean isBatteryPresent() {
         // the battery widget always is shown.
         return true;
     }
 
     private boolean isBatteryStatusUnknown() {
         return getBatteryStatus() == BatteryManager.BATTERY_STATUS_UNKNOWN;
     }
 
     private boolean isBatteryStatusCharging() {
         return getBatteryStatus() == BatteryManager.BATTERY_STATUS_CHARGING;
     }
 
     protected void onBatteryStatusChange(Intent intent) {
         mLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
         mBatteryPlugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0;
         mBatteryStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS,
                                             BatteryManager.BATTERY_STATUS_UNKNOWN);
     }
 
     @Override
     protected void onAttachedToWindow() {
         super.onAttachedToWindow();
         if (!mAttached) {
             mAttached = true;
             mBatteryReceiver.updateRegistration();
             mHandler.postDelayed(mInvalidate, 250);
         }
     }
 
     @Override
     protected void onDetachedFromWindow() {
         super.onDetachedFromWindow();
         if (mAttached) {
             mAttached = false;
             mBatteryReceiver.updateRegistration();
             mRectLeft = null; // makes sure, size based variables get
                                 // recalculated on next attach
             mSquareHeight = 0;    // makes sure, mSquareHeight is reread from icons on
             mSquareWidth = 0;                  // next attach
         }
     }
 
     @Override
     protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
         if (mSquareHeight == 0) {
             initSizeMeasureIconHeight();
         }
 		if (mSquareWidth == 0) {
             initSizeMeasureIconWidth();
         }
 
         setMeasuredDimension(mSquareWidth + getPaddingLeft(), mSquareHeight);
     }
 
     protected void drawSquare(Canvas canvas, int level, int animOffset, float textX, RectF dRect) {
         Paint usePaint = mPaintSystem;
         int internalLevel = level;
         boolean unknownStatus = isBatteryStatusUnknown();
         // turn red at 14% - same level android battery warning appears
         if (unknownStatus) {
             usePaint = mPaintGray;
             internalLevel = 100; // Draw all the square;
         } else if (internalLevel <= 14) {
             usePaint = mPaintRed;
         }

      
 	 
         // Battery level display is calculated by subtracting the battery height by the
 		// percentage of the battery level of the battery height minus the battery tip.	 
         cLevel = dRect.bottom - (dRect.bottom * (internalLevel * 0.01f)) + (mSquareHeight / 10.5f);
          
 		canvas.drawRect(dRect.left + (mSquareWidth / 4f), dRect.top, dRect.right - (mSquareWidth / 4f), dRect.top + (mSquareHeight / 10.5f), mPaintTip);
        
 		canvas.drawRect(dRect.left, dRect.top + (mSquareHeight / 10.5f), dRect.right, dRect.bottom, mPaintGray);
 		if (cLevel <= dRect.top + (mSquareHeight / 10.5f)) {
 		cLevel = dRect.top + (mSquareHeight / 10.5f);
 		} else if (cLevel >= dRect.bottom - (dRect.bottom * 0.15f)) {
 		cLevel = dRect.bottom - (dRect.bottom * 0.15f);
 		}
 		
 		
 		if (mIsAnimating == true) {
         canvas.drawRect(dRect.left + 1, animOffset, dRect.right - 1, dRect.bottom - 1, usePaint);
 		} else {
 		canvas.drawRect(dRect.left + 1, cLevel, dRect.right - 1, dRect.bottom - 1, usePaint);
 		}
 		
 	;
 		// if chosen by options, draw percentage text in the middle
         // always skip percentage when 100, so layout doesnt break
         if (unknownStatus) {
             mPaintFont.setColor(usePaint.getColor());
             canvas.drawText("?", textX, mTextY, mPaintFont);
         } else if (internalLevel < 100 && mPercentage) {
             mPaintFont.setColor(mBatteryFColor);
             canvas.drawText(Integer.toString(internalLevel), textX, mTextY, mPaintFont);
         }
 
     }
 
     @Override
     protected void onDraw(Canvas canvas) {
         if (mRectLeft == null) {
             initSizeBasedStuff();
         }
 
         updateChargeAnim();
 
         drawSquare(canvas,
                    getLevel(),
                    (isBatteryStatusCharging() ? mAnimOffset : 0), mTextLeftX, mRectLeft);
     }
 
     /***
      * updates the animation counter
      * cares for timed callbacks to continue animation cycles
      * uses mInvalidate for delayed invalidate() callbacks
      */
     private void updateChargeAnim() {
         if (!isBatteryStatusCharging() || getLevel() >= 97) {
             if (mIsAnimating) {
                 mIsAnimating = false;
                 mAnimOffset = 0;
                 mHandler.removeCallbacks(mInvalidate);
             }
             return;
         }
 
         mIsAnimating = true;
 
         if (mAnimOffset <= mRectTop + (mSquareHeight / 10.5f) + 1) {
             mAnimOffset = Math.round(cLevel) + 1;
         } else {
             mAnimOffset = mAnimOffset - 1;
         }
 
         mHandler.removeCallbacks(mInvalidate);
         mHandler.postDelayed(mInvalidate, 50);
     }
 
     /***
      * initializes all size dependent variables
      * sets stroke width and text size of all involved paints
      * YES! i think the method name is appropriate
      */
     private void initSizeBasedStuff() {
         if (mSquareHeight == 0) {
             initSizeMeasureIconHeight();
         }
 		if (mSquareWidth == 0) {
             initSizeMeasureIconWidth();
         }
 
         mPaintFont.setTextSize(mSquareHeight / 2f);
 
         float strokeWidth = mSquareHeight / 6.5f;
         mPaintRed.setStrokeWidth(strokeWidth);
         mPaintSystem.setStrokeWidth(strokeWidth);
         mPaintGray.setStrokeWidth(strokeWidth / 3.5f);
        
         int pLeft = getPaddingLeft();
         mRectLeft = new RectF(pLeft + strokeWidth / 2.0f, 0 + strokeWidth / 2.0f, mSquareWidth
                 - strokeWidth / 2.0f + pLeft, mSquareHeight - strokeWidth / 2.0f);
 
         // calculate Y position for text
         Rect bounds = new Rect();
         mPaintFont.getTextBounds("99", 0, "99".length(), bounds);
         mTextLeftX = mSquareWidth / 2.0f + getPaddingLeft() - 1;
         
         mTextY = mSquareHeight / 2.0f + (bounds.bottom - bounds.top) / 2.0f;
 
         // force new measurement for wrap-content xml tag
         onMeasure(0, 0);
     }
 
     /***
      * we need to measure the size of the square battery by checking another
      * resource. unfortunately, those resources have transparent/empty borders
      * so we have to count the used pixel manually and deduct the size from
      * it. quite complicated, but the only way to fit properly into the
      * statusbar for all resolutions. We also need a static image so that the
 	 * image will always be the same and people can theme the normal images 
 	 * and leave this one alone it will only be used to get the proper size
      */
     private void initSizeMeasureIconHeight() {
         final Bitmap measure = BitmapFactory.decodeResource(getResources(),
                 com.android.systemui.R.drawable.battery_template);
         final int x = measure.getWidth() / 2;
 
         mSquareHeight = 0;
         for (int y = 0; y < measure.getHeight(); y++) {
             int alpha = Color.alpha(measure.getPixel(x, y));
             if (alpha > 5) {
                 mSquareHeight++;
             }
         }
     }
 	 private void initSizeMeasureIconWidth() {
         final Bitmap measure = BitmapFactory.decodeResource(getResources(),
                 com.android.systemui.R.drawable.battery_template);
         final int y = measure.getHeight() / 2;
 
         mSquareWidth = 0;
         for (int x = 0; x < measure.getWidth(); x++) {
             int alpha = Color.alpha(measure.getPixel(x, y));
             if (alpha > 5) {
                 mSquareWidth++;
             }
         }
     }
 	
 	
 }
