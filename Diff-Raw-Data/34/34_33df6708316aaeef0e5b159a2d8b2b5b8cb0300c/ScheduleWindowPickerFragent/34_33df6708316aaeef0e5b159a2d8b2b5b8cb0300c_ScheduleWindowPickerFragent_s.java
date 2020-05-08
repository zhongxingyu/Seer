 package org.minyanmate.minyanmate.dialogs;
 
 import android.app.Dialog;
 import android.app.TimePickerDialog;
 import android.content.ContentValues;
 import android.os.Bundle;
 import android.widget.TimePicker;
 
 import org.minyanmate.minyanmate.contentprovider.MinyanMateContentProvider;
 import org.minyanmate.minyanmate.database.MinyanSchedulesTable;
 
 import java.util.concurrent.TimeUnit;
 
 public class ScheduleWindowPickerFragent extends AbstractSchedulePickerDialog {
 
 
 	@Override
 	public Dialog onCreateDialog(Bundle savedInstanceState) {
 		
 		TimePickerDialog dialog = (TimePickerDialog) super.onCreateDialog(savedInstanceState);
 		
 		dialog.setTitle("Set scheduling period length");
 		return dialog;
 	}
 	
 	/**
 	 * The callback from {@link ScheduleWindowPickerFragent.OnTimeSetListener}, pushes
 	 * changes back to the {@link MinyanMateContentProvider}.
 	 */
 	@Override
 	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

         if (ignoreTimeSet)
             return;
 
 		/* 
 		 * Need to check both ahead and behind. Can't move this schedule too far forward
 		 * or it would interfere with the next event, and can't move this schedule too far
 		 * backwards or it would interfere with the previous event.
 		 */
 		
 		ContentValues values = new ContentValues();
 		long windowLength = TimeUnit.HOURS.toMillis(hourOfDay) + TimeUnit.MINUTES.toMillis(minute);
 		
 		values.put(MinyanSchedulesTable.COLUMN_SCHEDULE_WINDOW, windowLength);
 		
 		saveSelection(view.getContext(), schedule.getHour(), schedule.getMinute(), 
 				windowLength, values);
 	}
 
 }
