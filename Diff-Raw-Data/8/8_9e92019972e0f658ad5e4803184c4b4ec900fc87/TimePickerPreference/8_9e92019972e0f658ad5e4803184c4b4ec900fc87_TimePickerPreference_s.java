 package com.dgsd.android.ShiftTracker.View;
 
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.preference.DialogPreference;
 import android.text.format.Time;
 import android.util.AttributeSet;
 import android.view.View;
 import android.widget.TimePicker;
 
import java.util.TimeZone;

 /**
  * A preference type that allows a user to choose a time
 
  */
 public class TimePickerPreference extends DialogPreference implements
         TimePicker.OnTimeChangedListener {
 
     private long mCurrentValue;
 
     private Time mTime;
 
     private TimePicker mTimePicker;
 
     /**
      * @param context
      * @param attrs
      */
     public TimePickerPreference(Context context, AttributeSet attrs) {
         super(context, attrs);
         mTime = new Time();
         mTime.setToNow();
         initialize();
     }
 
     /**
      * @param context
      * @param attrs
      * @param defStyle
      */
     public TimePickerPreference(Context context, AttributeSet attrs,
                                 int defStyle) {
         super(context, attrs, defStyle);
         initialize();
     }
 
     /**
      * Initialize this preference
      */
     private void initialize() {
         setPersistent(true);
     }
 
     /*
     * (non-Javadoc)
     *
     * @see android.preference.DialogPreference#onCreateDialogView()
     */
     @Override
     protected View onCreateDialogView() {
 
         mTimePicker = new TimePicker(getContext());
         mTimePicker.setOnTimeChangedListener(this);
 
         int h = getHour();
         int m = getMinute();
         if (h >= 0 && m >= 0) {
             mTimePicker.setCurrentHour(h);
             mTimePicker.setCurrentMinute(m);
         }
 
         return mTimePicker;
     }
 
     /*
     * (non-Javadoc)
     *
     * @see
     * android.widget.TimePicker.OnTimeChangedListener#onTimeChanged(android
     * .widget.TimePicker, int, int)
     */
 
     public void onTimeChanged(TimePicker view, int hour, int minute) {
         mTime.hour = hour;
         mTime.minute = minute;
         mTime.second = 0;
 
         final long millis = mTime.toMillis(true);
         persistLong(millis);
         callChangeListener(millis);
     }
 
     @Override
     protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
         if(restorePersistedValue) {
             mCurrentValue = getPersistedLong(mCurrentValue);
             return;
         }
 
         if(defaultValue == null)
             return;
 
         String defValAsString = defaultValue.toString();
 
         try {
             mCurrentValue = Long.valueOf(defValAsString);
             //The default value is in GMT, convert it to timezone
             mTime.timezone = "GMT";
             mTime.set(mCurrentValue);
             int hour = mTime.hour;
             int min = mTime.minute;
 
             mTime = new Time();
             mTime.hour = hour;
             mTime.minute = min;
             mTime.second = 0;
 
             mCurrentValue = mTime.toMillis(true);
         } catch (Exception e) {
             //O well..
 
             mTime.hour = 9;
             mTime.minute = 0;
             mTime.second = 0;
 
             mCurrentValue = mTime.toMillis(true);
         }
     }
 
     @Override
     protected void onBindDialogView(View view) {
         super.onBindDialogView(view);
 
         mTime.set(mCurrentValue);
         mTime.normalize(true);
         mTimePicker.setCurrentHour(mTime.hour);
         mTimePicker.setCurrentMinute(mTime.minute);
     }
 
     @Override
     protected Object onGetDefaultValue(TypedArray a, int index) {
         return a.getString(index);
     }
 
     /**
      * Get the hour value (in 24 hour time)
      *
      * @return The hour value, will be 0 to 23 (inclusive)
      */
     private int getHour() {
         long millis = getPersistedLong(mCurrentValue);
         mTime.set(millis);
         mTime.normalize(true);
 
         return mTime.hour;
     }
 
     /**
      * Get the minute value
      *
      * @return the minute value, will be 0 to 59 (inclusive)
      */
     private int getMinute() {
         long millis = getPersistedLong(mCurrentValue);
         mTime.set(millis);
         mTime.normalize(true);
 
         return mTime.minute;
     }
 }
