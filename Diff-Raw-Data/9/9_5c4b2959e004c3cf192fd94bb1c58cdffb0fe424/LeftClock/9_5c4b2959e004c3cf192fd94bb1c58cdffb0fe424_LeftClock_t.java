 /*
  * Copyright (C) 2006 The Android Open Source Project
  * Patched by Sven Dawitz; Copyright (C) 2011 CyanogenMod Project
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
 
 package com.android.systemui.statusbar.clocks;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.TimeZone;
 import java.util.ArrayList;
 
 import android.content.BroadcastReceiver;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.database.ContentObserver;
 import android.os.Handler;
 import android.provider.Settings;
 import android.text.Spannable;
 import android.text.SpannableStringBuilder;
 import android.text.format.DateFormat;
 import android.text.style.CharacterStyle;
 import android.text.style.RelativeSizeSpan;
 import android.util.AttributeSet;
 import android.view.View;
 import android.util.TypedValue;
 import android.content.res.Resources;
 import android.util.DisplayMetrics;
 import android.widget.TextView;
 
 import com.android.internal.R;
 
 /**
  * This widget display an analogic clock with two hands for hours and
  * minutes.
  */
 public class LeftClock extends TextView {
     private boolean mAttached;
     private Calendar mCalendar;
     private String mClockFormatString;
     private SimpleDateFormat mClockFormat;
 
     private static final int AM_PM_STYLE_NORMAL  = 0;
     private static final int AM_PM_STYLE_SMALL   = 1;
     private static final int AM_PM_STYLE_GONE    = 2;
 
     private static int AM_PM_STYLE = AM_PM_STYLE_GONE;
 
     private static final int WEEKDAY_STYLE_NORMAL = 0;
     private static final int WEEKDAY_STYLE_SMALL  = 1;
     private static final int WEEKDAY_STYLE_GONE   = 2;
 
     private int WEEKDAY_STYLE = WEEKDAY_STYLE_GONE;
 
     private int mAmPmStyle;
     private int mWeekdayStyle;
     private boolean mShowLeftClock;
     private boolean mShowClocker;
     private int mClockColor;
     private int mCarrierSize;
 
     Handler mHandler;
 
     class SettingsObserver extends ContentObserver {
         SettingsObserver(Handler handler) {
             super(handler);
         }
 
         void observe() {
             ContentResolver resolver = mContext.getContentResolver();
             resolver.registerContentObserver(Settings.System.getUriFor(
                     Settings.System.STATUS_BAR_AM_PM), false, this);
             resolver.registerContentObserver(Settings.System.getUriFor(
                     Settings.System.STATUS_BAR_CLOCK), false, this);
             resolver.registerContentObserver(Settings.System.getUriFor(
                     Settings.System.STATUS_BAR_WEEKDAY), false, this);
             resolver.registerContentObserver(Settings.System.getUriFor(
                     Settings.System.STATUS_BAR_CLOCKCOLOR), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.STATUSBAR_CLOCK_FONT_SIZE), false, this);
         }
 
         @Override public void onChange(boolean selfChange) {
             updateSettings();
         }
     }
 
     public LeftClock(Context context) {
         this(context, null);
     }
 
     public LeftClock(Context context, AttributeSet attrs) {
         this(context, attrs, 0);
     }
 
     public LeftClock(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
 
         mHandler = new Handler();
         SettingsObserver settingsObserver = new SettingsObserver(mHandler);
         settingsObserver.observe();
 
         updateSettings();
     }
 
     @Override
     protected void onAttachedToWindow() {
         super.onAttachedToWindow();
 
         if (!mAttached) {
             mAttached = true;
             IntentFilter filter = new IntentFilter();
 
             filter.addAction(Intent.ACTION_TIME_TICK);
             filter.addAction(Intent.ACTION_TIME_CHANGED);
             filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
             filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
 
             getContext().registerReceiver(mIntentReceiver, filter, null, getHandler());
         }
 
         // NOTE: It's safe to do these after registering the receiver since the receiver always runs
         // in the main thread, therefore the receiver can't run before this method returns.
 
         // The time zone may have changed while the receiver wasn't registered, so update the Time
         mCalendar = Calendar.getInstance(TimeZone.getDefault());
 
         // Make sure we update to the current time
         updateLeftClock();
     }
 
     @Override
     protected void onDetachedFromWindow() {
         super.onDetachedFromWindow();
         if (mAttached) {
             getContext().unregisterReceiver(mIntentReceiver);
             mAttached = false;
         }
     }
 
     private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             String action = intent.getAction();
             if (action.equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                 String tz = intent.getStringExtra("time-zone");
                 mCalendar = Calendar.getInstance(TimeZone.getTimeZone(tz));
                 if (mClockFormat != null) {
                     mClockFormat.setTimeZone(mCalendar.getTimeZone());
                 }
             }
             updateLeftClock();
         }
     };
 
     final void updateLeftClock() {
         mCalendar.setTimeInMillis(System.currentTimeMillis());
         setTextColor(mClockColor);
         setText(getSmallTime());
         setTextSize(mCarrierSize);
 
     }
 
     private final CharSequence getSmallTime() {
         Context context = getContext();
         boolean b24 = DateFormat.is24HourFormat(context);
         int res;
 
         if (b24 && AM_PM_STYLE == AM_PM_STYLE_GONE) {
             res = R.string.twenty_four_hour_time_format;
         } else {
             res = R.string.twelve_hour_time_format;
         }
 
         final char MAGIC1 = '\uEF00';
         final char MAGIC2 = '\uEF01';
 
         SimpleDateFormat sdf;
         String format = context.getString(res);
         if (!format.equals(mClockFormatString)) {
             /*
              * Search for an unquoted "a" in the format string, so we can
              * add dummy characters around it to let us find it again after
              * formatting and change its size.
              */
             if (AM_PM_STYLE != AM_PM_STYLE_NORMAL) {
                 int a = -1;
                 boolean quoted = false;
                 for (int i = 0; i < format.length(); i++) {
                     char c = format.charAt(i);
 
                     if (c == '\'') {
                         quoted = !quoted;
                     }
                     if (!quoted && c == 'a') {
                         a = i;
                         break;
                     }
                 }
 
                 if (a >= 0) {
                     // Move a back so any whitespace before AM/PM is also in the alternate size.
                     final int b = a;
                     while (a > 0 && Character.isWhitespace(format.charAt(a-1))) {
                         a--;
                     }
                     format = format.substring(0, a) + MAGIC1 + format.substring(a, b)
                         + "a" + MAGIC2 + format.substring(b + 1);
                 }
             }
 
             mClockFormat = sdf = new SimpleDateFormat(format);
             mClockFormatString = format;
         } else {
             sdf = mClockFormat;
         }
         String result = sdf.format(mCalendar.getTime());
 
         String currentDay = null;
 
         Calendar calendar = Calendar.getInstance();
         int day = calendar.get(Calendar.DAY_OF_WEEK);
 
         if (WEEKDAY_STYLE != WEEKDAY_STYLE_GONE) {
             currentDay = getDay(day);
             result = currentDay + result;
         }
 
         SpannableStringBuilder formatted = new SpannableStringBuilder(result);
 
         if (AM_PM_STYLE != AM_PM_STYLE_NORMAL) {
             int magic1 = result.indexOf(MAGIC1);
             int magic2 = result.indexOf(MAGIC2);
             if (magic1 >= 0 && magic2 > magic1) {
                 if (AM_PM_STYLE == AM_PM_STYLE_GONE) {
                     formatted.delete(magic1, magic2+1);
                 } else {
                     if (AM_PM_STYLE == AM_PM_STYLE_SMALL) {
                         CharacterStyle style = new RelativeSizeSpan(0.7f);
                         formatted.setSpan(style, magic1, magic2,
                                           Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                     }
                     formatted.delete(magic2, magic2 + 1);
                     formatted.delete(magic1, magic1 + 1);
                 }
             }
         }
 
         if (WEEKDAY_STYLE != WEEKDAY_STYLE_NORMAL) {
             if (currentDay != null) {
                 if (WEEKDAY_STYLE == WEEKDAY_STYLE_GONE) {
                     formatted.delete(result.indexOf(currentDay), result.lastIndexOf(currentDay)+currentDay.length());
                 } else {
                     if (WEEKDAY_STYLE == WEEKDAY_STYLE_SMALL) {
                         CharacterStyle style = new RelativeSizeSpan(0.7f);
                         formatted.setSpan(style, result.indexOf(currentDay), result.lastIndexOf(currentDay)+currentDay.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                     }
                 }
             }
         }
 
         return formatted;
     }
 
     private String getDay(int today) {
         String currentDay = null;
         switch (today) {
             case 1:
                 currentDay = mContext.getResources().getString(R.string.day_of_week_medium_sunday);
             break;
             case 2:
                 currentDay = mContext.getResources().getString(R.string.day_of_week_medium_monday);
             break;
             case 3:
                 currentDay = mContext.getResources().getString(R.string.day_of_week_medium_tuesday);
             break;
             case 4:
                 currentDay = mContext.getResources().getString(R.string.day_of_week_medium_wednesday);
             break;
             case 5:
                 currentDay = mContext.getResources().getString(R.string.day_of_week_medium_thursday);
             break;
             case 6:
                 currentDay = mContext.getResources().getString(R.string.day_of_week_medium_friday);
             break;
             case 7:
                 currentDay = mContext.getResources().getString(R.string.day_of_week_medium_saturday);
             break;
         }
         return currentDay.toUpperCase() + " ";
     }
 
     public void VisibilityChecks(boolean show) {
         mShowClocker = show;
         updateSettings();
     }
 
     private void updateSettings(){
         ContentResolver resolver = mContext.getContentResolver();
 
         int mCColor = mClockColor;
         int defValuesColor = mContext.getResources().getInteger(com.android.internal.R.color.color_default_cyanmobile);
         mAmPmStyle = (Settings.System.getInt(resolver,
                 Settings.System.STATUS_BAR_AM_PM, 2));
         mWeekdayStyle = (Settings.System.getInt(resolver,
             Settings.System.STATUS_BAR_WEEKDAY, 2));
         mClockColor = (Settings.System.getInt(resolver,
                 Settings.System.STATUS_BAR_CLOCKCOLOR, defValuesColor));
         int mCarrierSizeval = Settings.System.getInt(resolver,
                 Settings.System.STATUSBAR_CLOCK_FONT_SIZE, 11);
         int CarrierSizepx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mCarrierSizeval, mContext.getResources().getDisplayMetrics());
         mCarrierSize = CarrierSizepx;
 
         if ((mAmPmStyle != AM_PM_STYLE) || (mCColor != mClockColor)) {
             AM_PM_STYLE = mAmPmStyle;
             mClockFormatString = "";
 
             if (mAttached) {
                 updateLeftClock();
             }
         }
 
         if (mWeekdayStyle != WEEKDAY_STYLE) {
             WEEKDAY_STYLE = mWeekdayStyle;
             mClockFormatString = "";
 
             if (mAttached) {
                updateLeftClock();
             }
         }
 
         mShowLeftClock = (Settings.System.getInt(resolver,
                 Settings.System.STATUS_BAR_CLOCK, 1) == 3);
 
         if(mShowLeftClock)
             setVisibility(mShowClocker ? View.VISIBLE : View.GONE);
         else
             setVisibility(View.GONE);
     }
 }
 
