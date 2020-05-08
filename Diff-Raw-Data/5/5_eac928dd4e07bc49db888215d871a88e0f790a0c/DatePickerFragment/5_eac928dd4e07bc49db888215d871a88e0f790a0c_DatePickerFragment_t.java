 package com.dgsd.android.ShiftTracker.Fragment;
 
 import android.R;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.text.format.Time;
 import android.widget.DatePicker;
 import android.widget.Toast;
 import com.actionbarsherlock.app.SherlockDialogFragment;
 import com.dgsd.android.ShiftTracker.Util.Api;
 import com.dgsd.android.ShiftTracker.Util.TimeUtils;
import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;
 
 public class DatePickerFragment extends SherlockDialogFragment implements DatePickerDialog.OnDateSetListener {
     private static final String KEY_DATE = "_date";
     private static final String KEY_MIN_DATE = "_min";
     private static final String KEY_MAX_DATE = "_max";
     private static final String KEY_TITLE = "_title";
     private static final String KEY_POS_BTN = "_pos_btn";
 
     private int mDate = -1;
     private long mMinDate = Long.MIN_VALUE;
     private long mMaxDate = Long.MAX_VALUE;
     private String mTitle;
     private String mPositiveBtnText;
     private Time mTime = new Time();
 
     private int mLastSelectedJd;
 
     private OnDateSelectedListener mOnDateSelectedListener;
 
     public static DatePickerFragment newInstance(String title, String postiveButton, long min, long max) {
         return newInstance(title, postiveButton, min, max, -1);
     }
 
     public static DatePickerFragment newInstance(String title, String postiveButton, long min, long max, int date) {
         DatePickerFragment frag = new DatePickerFragment();
 
         Bundle args = new Bundle();
         args.putInt(KEY_DATE, date);
         args.putLong(KEY_MIN_DATE, min);
         args.putLong(KEY_MAX_DATE, max);
         args.putString(KEY_TITLE, title);
         args.putString(KEY_POS_BTN, postiveButton);
         frag.setArguments(args);
 
         return frag;
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         final Bundle args = getArguments();
         if(args != null) {
             mDate = args.getInt(KEY_DATE, mDate);
             mMinDate = args.getLong(KEY_MIN_DATE, mMinDate);
             mMaxDate = args.getLong(KEY_MAX_DATE, mMaxDate);
             mTitle = args.getString(KEY_TITLE);
             mPositiveBtnText = args.getString(KEY_POS_BTN);
         }
     }
 
     @Override
     public Dialog onCreateDialog(Bundle savedInstanceState) {
         final Time time = new Time();
 
         if(mDate == -1)
             time.setToNow();
         else
             time.setJulianDay(mDate);
 
 
         mLastSelectedJd = TimeUtils.getJulianDay(time);
         final DatePickerDialog dpd = new DatePickerDialog(getActivity(), this, time.year, time.month, time.monthDay);
         dpd.setTitle(mTitle);
 
         if(Api.isMin(Api.HONEYCOMB)) {
             final DatePicker dp = dpd.getDatePicker();
             dp.setMaxDate(mMaxDate);
             dp.setMinDate(mMinDate);
             dp.setCalendarViewShown(false);
         }
 
         return dpd;
     }
 
     public void setOnDateSelectedListener(OnDateSelectedListener listener) {
         this.mOnDateSelectedListener = listener;
     }
 
     @Override
     public void onDateSet(DatePicker datePicker, int year, int month, int monthDay) {
         //If we're below honeycomb, need to validate the date input
         if(!Api.isMin(Api.HONEYCOMB)) {
             String error = null;
             mTime.set(mMinDate);
             if(mTime.year > year ||
                 (mTime.year >= year && mTime.month > month) ||
                     (mTime.year >= year && mTime.month >= month && mTime.monthDay > monthDay)) {
                 datePicker.updateDate(mTime.year, mTime.month, mTime.monthDay);
                 error = "Date selected is to far in the past";
             }
 
             mTime.set(mMaxDate);
             if(mTime.year < year ||
                 (mTime.year <= year && mTime.month < month) ||
                     (mTime.year <= year && mTime.month <= month && mTime.monthDay < monthDay)) {
                 datePicker.updateDate(mTime.year, mTime.month, mTime.monthDay);
                 error = "Date selected is to far in the future";
             }
 
             if(!TextUtils.isEmpty(error) && getActivity() != null) {
                Crouton.showText(getActivity(), error, Style.INFO);
             }
         }
 
         if(mOnDateSelectedListener != null) {
             mTime.year = datePicker.getYear();
             mTime.month = datePicker.getMonth();
             mTime.monthDay = datePicker.getDayOfMonth();
             mTime.normalize(true);
 
             mOnDateSelectedListener.onDateSelected(TimeUtils.getJulianDay(mTime));
         }
     }
 
     public static interface OnDateSelectedListener {
         public void onDateSelected(int julianDay);
     }
 
 }
