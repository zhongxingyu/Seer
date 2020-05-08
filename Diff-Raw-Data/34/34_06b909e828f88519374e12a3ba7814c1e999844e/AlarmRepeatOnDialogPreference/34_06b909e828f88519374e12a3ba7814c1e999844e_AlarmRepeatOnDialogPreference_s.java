 
 package org.startsmall.alarmclockplus.preference;
 
 import org.startsmall.alarmclockplus.Alarms;
 import android.app.Dialog;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.preference.DialogPreference;
 import android.util.AttributeSet;
 import android.util.Log;
 
 import java.util.Arrays;
 import java.text.DateFormatSymbols;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 
 public class AlarmRepeatOnDialogPreference extends DialogPreference
     implements DialogInterface.OnMultiChoiceClickListener {
 
     public interface OnRepeatWeekdaysSetListener {
         public void onRepeatWeekdaysSet(Alarms.RepeatWeekdays weekdays);
     }
 
     private static final String TAG = "AlarmRepeatOnDialogPreference";
     private Alarms.RepeatWeekdays mRepeatWeekdays = new Alarms.RepeatWeekdays();
     private OnRepeatWeekdaysSetListener mListener;
 
     public AlarmRepeatOnDialogPreference(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
     }
 
     public AlarmRepeatOnDialogPreference(Context context, AttributeSet attrs) {
         super(context, attrs);
     }
 
     public void setOnRepeatWeekdaysSetListener(OnRepeatWeekdaysSetListener listener) {
         mListener = listener;
     }
 
     @Override
     protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
         SimpleDateFormat dateFormat =
             (SimpleDateFormat)DateFormat.getDateInstance(DateFormat.LONG);
         DateFormatSymbols dateFormatSymbols = dateFormat.getDateFormatSymbols();
        CharSequence[] weekdays = dateFormatSymbols.getWeekdays();

        Log.d(TAG, "======> '" + Arrays.deepToString(weekdays) + "'");

         builder.setMultiChoiceItems(weekdays, null /* no days are checked */, this);

        mRepeatWeekdays.reset();
     }
 
     public void onClick(DialogInterface dialog, int which, boolean isChecked) {
         int day = which + 1;
         if(isChecked) {
             mRepeatWeekdays.addDay(day);
         } else {
             mRepeatWeekdays.removeDay(day);
         }
     }
 
     public void onClick(DialogInterface dialog, int which) {
         switch(which) {
         case DialogInterface.BUTTON_POSITIVE:
             if(mListener != null) {
                 mListener.onRepeatWeekdaysSet(mRepeatWeekdays);
             }
             dialog.dismiss();
             break;
         default:
             dialog.cancel();
             break;
         }
     }
 }
