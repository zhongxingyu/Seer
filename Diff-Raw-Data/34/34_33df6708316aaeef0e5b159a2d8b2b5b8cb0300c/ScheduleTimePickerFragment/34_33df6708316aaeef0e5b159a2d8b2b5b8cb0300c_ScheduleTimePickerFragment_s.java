 package org.minyanmate.minyanmate.dialogs;
 
 import android.app.Dialog;
 import android.app.TimePickerDialog;
 import android.content.ContentValues;
 import android.os.Bundle;
 import android.widget.TimePicker;
 
 import org.minyanmate.minyanmate.contentprovider.MinyanMateContentProvider;
 import org.minyanmate.minyanmate.database.MinyanSchedulesTable;
 
 /**
  * A DialogFragment with a Time Picker, used to update the time of a schedule
  * when it calls back to onTimeSet
  */
 public class ScheduleTimePickerFragment extends AbstractSchedulePickerDialog {
 	
 
 	
 	@Override
 	public Dialog onCreateDialog(Bundle savedInstanceState) {
         return super.onCreateDialog(savedInstanceState);
 	}
 	
 	/**
 	 * The callback from {@link TimePickerDialog.OnTimeSetListener}, pushes
 	 * changes back to the {@link MinyanMateContentProvider}.
 	 */
 	@Override
 	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 
         if (ignoreTimeSet)
             return;
 		
 		ContentValues values = new ContentValues();
 		values.put(MinyanSchedulesTable.COLUMN_PRAYER_HOUR, hourOfDay);
 		values.put(MinyanSchedulesTable.COLUMN_PRAYER_MIN, minute);
 		
 		saveSelection(view.getContext(), hourOfDay, minute, 
 				schedule.getSchedulingWindowLength(), values);
 
 	}
 	
 
 	
 }
