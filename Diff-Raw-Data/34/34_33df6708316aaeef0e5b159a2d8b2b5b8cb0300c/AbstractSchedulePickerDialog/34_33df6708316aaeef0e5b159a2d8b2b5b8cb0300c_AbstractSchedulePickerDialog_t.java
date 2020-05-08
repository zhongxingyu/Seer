 package org.minyanmate.minyanmate.dialogs;
 
 import android.app.Dialog;
 import android.app.TimePickerDialog;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.text.format.DateFormat;
import android.util.Log;
 import android.widget.Toast;
 
 import org.minyanmate.minyanmate.contentprovider.MinyanMateContentProvider;
 import org.minyanmate.minyanmate.database.MinyanSchedulesTable;
 import org.minyanmate.minyanmate.models.MinyanSchedule;
 
 import java.util.concurrent.TimeUnit;
 
 public abstract class AbstractSchedulePickerDialog extends DialogFragment 
 implements TimePickerDialog.OnTimeSetListener {
 
     boolean ignoreTimeSet = true;
 
 	private int id;
 	int minute;
 	int hour;
 	MinyanSchedule schedule;
 
     @Override
     public Dialog onCreateDialog(Bundle savedInstanceState) {
 
         final TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), this, hour, minute,
                 (this instanceof ScheduleWindowPickerFragent ?
                        true : DateFormat.is24HourFormat(getActivity())));
 
         timePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Set",
                 new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int i) {
                         ignoreTimeSet = false;
                        Log.d("Inside TimePickerDialog for all SDK's", "SDK Version: " + Build.VERSION.SDK_INT);
                        Log.d("Inside TimePickerDialog", "Device Manufactorer: " + Build.MANUFACTURER);
                        // doesn't seem to work on Samsung device ?
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH || "samsung".equalsIgnoreCase(Build.MANUFACTURER)) {
                            Log.d("Inside TimePickerDialog for Pre-ICS or Samsungs", "SDK Version: " + Build.VERSION.SDK_INT);
                             timePickerDialog.onClick(dialog, i);
                         }
                     }
                 });
         timePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                 new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i) {
                         ignoreTimeSet = true;
                         dialogInterface.cancel();
                     }
                 });
 
         return timePickerDialog;
     }
 
 	public void initialize(int id, int hour, int minute, MinyanSchedule sched) {
 		this.minute = minute;
 		this.hour = hour;
 		this.id = id;
 		this.schedule = sched;
 	}
 	
 	/**
 	 * This method checks the validity of the insert operation against the adjacent minyans
 	 * scheduled to possibly occur. It checks both forwards and backwards.
 	 * @param context of the calling view
 	 * @param thisScheduleHour is the desired hour (0-23) of the minyan
 	 * @param thisScheduleMinute is the desired minute of the minyan 
 	 * @param thisScheduleWindow is the desired scheduling window length of the minyan
 	 * @param contentValues contains the updated columns 
 	 */
 	void saveSelection(Context context, int thisScheduleHour, int thisScheduleMinute,
 			long thisScheduleWindow, ContentValues contentValues) {
 		
 		// ABS( thisID - _id ) = 1 finds immediately adjacent schedules
 		
 			Cursor adjacentSchedules = context.getContentResolver().query(
 					MinyanMateContentProvider.CONTENT_URI_SCHEDULES,
 					null, "ABS(" + id + "- " + MinyanSchedulesTable.COLUMN_SCHEDULE_ID + ")=1" , null,
 					MinyanSchedulesTable.COLUMN_SCHEDULE_ID + " ASC");
 			
 			long thisWindowLength = thisScheduleWindow; // assume constant
 			long thisScheduleStartTime = TimeUnit.HOURS.toMillis(thisScheduleHour) + TimeUnit.MINUTES.toMillis(thisScheduleMinute);
 			long thisScheduleEndTime = thisScheduleStartTime +TimeUnit.MINUTES.toMillis(30); // 30 minutes later
 			
 			if (id == 1 && adjacentSchedules.getCount() == 1 // if first event, only check forwards 
 					&& adjacentSchedules.moveToFirst()) {
 				
 				MinyanSchedule nextSched = MinyanSchedule.schedFromCursor(adjacentSchedules);
 				long nextWindowLength = nextSched.getSchedulingWindowLength();
 				long nextSchedStartTime = TimeUnit.HOURS.toMillis(nextSched.getHour()) + TimeUnit.MINUTES.toMillis(nextSched.getMinute());
 				
 				if ( thisScheduleEndTime + nextWindowLength < nextSchedStartTime) { // no need to check day-wrap-around
 					context.getContentResolver().update(
 							Uri.parse(MinyanMateContentProvider.CONTENT_URI_SCHEDULES + "/" + id),
 							contentValues,
 //								MinyanTimesTable.COLUMN_ID + "=?", new String[] { String.valueOf(childPrayer.getId()) }
 							null, null
 							);
 				} else 
 					Toast.makeText(context, "Failed to save! May be another minyan " +
 							"too soon afterwards!", Toast.LENGTH_SHORT).show();
 				
 			} else if (adjacentSchedules.getCount() == 1     // else assume last event and only check backwards
 					&& adjacentSchedules.moveToFirst()) {
 				
 				MinyanSchedule previousSched = MinyanSchedule.schedFromCursor(adjacentSchedules);
 				
 				long prevSchedEndTime = TimeUnit.HOURS.toMillis(previousSched.getHour()) + TimeUnit.MINUTES.toMillis(previousSched.getMinute());
 				
 				if ( prevSchedEndTime + thisWindowLength < thisScheduleStartTime ) { // no need to check day-wrap-around
 					context.getContentResolver().update(
 							Uri.parse(MinyanMateContentProvider.CONTENT_URI_SCHEDULES + "/" + id),
 							contentValues,
 //								MinyanTimesTable.COLUMN_ID + "=?", new String[] { String.valueOf(childPrayer.getId()) }
 							null, null
 							);
 				} else 
 					Toast.makeText(context, "Failed to save! May be another minyan " +
 							"scheduled too recently to this one!", Toast.LENGTH_SHORT).show();
 				
 			} else { /* otherwise assume somewhere in between and check both forwards and backwards
 					  * being very careful about day-wrap-around - need to check both whether end/start of 
 					  * day and if so wrap around, and if not don't
 					  */
 				
 				adjacentSchedules.moveToFirst();			
 				MinyanSchedule previousSched = MinyanSchedule.schedFromCursor(adjacentSchedules);
 				
 				long prevSchedEndTime = TimeUnit.HOURS.toMillis(previousSched.getHour()) + TimeUnit.MINUTES.toMillis(previousSched.getMinute());
 				
 				adjacentSchedules.moveToNext();
 				MinyanSchedule nextSched = MinyanSchedule.schedFromCursor(adjacentSchedules);
 				
 				long nextWindowLength = nextSched.getSchedulingWindowLength();
 				long nextSchedStartTime = TimeUnit.HOURS.toMillis(nextSched.getHour()) + TimeUnit.MINUTES.toMillis(nextSched.getMinute());
 				
 				boolean previousMinyanCanWrapDay = (previousSched.getPrayerNum() == 3 && schedule.getPrayerNum() == 1);
 				boolean thisMinyanCanWrapDay = (schedule.getPrayerNum() == 3 && nextSched.getPrayerNum() == 1);
 				
 				/*
 				 * If this minyan is at the beginning of the day, the previous minyan is at the end of the day, 
 				 * can "wrap around" to today. To make sure that absolute time references are preserved, add 24 hours.
 				 */
 				
 				thisScheduleStartTime += (previousMinyanCanWrapDay ? TimeUnit.DAYS.toMillis(1) : 0);
 				
 				/*
 				 * If this minyan is at the end of the day, the next minyan is at the beginning of the day, and this
 				 * can "wrap around" to tomorrow. To make sure that absolute time references are preserved, add 24 hours.
 				 */
 				
 				nextSchedStartTime += (thisMinyanCanWrapDay ? TimeUnit.DAYS.toMillis(1) : 0);
 				
 				if ( prevSchedEndTime + thisWindowLength < thisScheduleStartTime) {
 					
 					if ( thisScheduleEndTime + nextWindowLength < nextSchedStartTime) {
 						context.getContentResolver().update(
 								Uri.parse(MinyanMateContentProvider.CONTENT_URI_SCHEDULES + "/" + id),
 								contentValues,
 //									MinyanTimesTable.COLUMN_ID + "=?", new String[] { String.valueOf(childPrayer.getId()) }
 								null, null
 								);
 						
 					} else 
 						Toast.makeText(context, "Failed to save! May be another minyan " +
 								"too soon afterwards!", Toast.LENGTH_SHORT).show();
 
 				} else
 					Toast.makeText(context, "Failed to save! May be another minyan " +
 							"scheduled too recently to this one!", Toast.LENGTH_SHORT).show();
 			}
 	}
 }
