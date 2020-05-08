 /**
 *	MovieDetailsActivity.java
 *
 *	@author Robin Andersson
 *	@copyright (c) 2012 Robin Andersson
 *	@license MIT
 */
 
 package se.chalmers.watchme.ui;
 
 import java.util.Calendar;

 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.widget.DatePicker;
 
 
 /**
  * Class for showing the Android date-picker and returning the selected date
  */
 public class DatePickerFragment extends DialogFragment
 								implements DatePickerDialog.OnDateSetListener {
 	
 	private Calendar pickedDate;
 	private DatePickerListener datePickerListener;
 	
 	/**
 	 * Safety check so that the acitivity that called the date picker is
 	 * implementing the required DatePickerListener interface.
 	 *  
 	 *  @throws ClassCastException Is thrown if the activity that called the datepicker
 	 *  does not implement DatePickerListener interface
 	 */
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 		
 		try {
 			datePickerListener = (DatePickerListener) activity;
 		}
 		
 		catch (ClassCastException e) {
 			throw new ClassCastException(activity.toString() +
 					" must implement DatePickerListener");
 		}
 	}
 	
 	@Override
     public Dialog onCreateDialog(Bundle savedInstanceState) {
 		
         // Use the current date as the default date in the picker
         pickedDate = Calendar.getInstance();
         int year = pickedDate.get(Calendar.YEAR);
         int month = pickedDate.get(Calendar.MONTH);
         int day = pickedDate.get(Calendar.DAY_OF_MONTH);
 
         // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
     }
 	
 
 	/**
 	 * Callback method for when the user sets a date.
 	 * 
 	 * Calls the appropriate method to set the date on the activity that
 	 * initialized the date picker.
 	 */
 	public void onDateSet(DatePicker view, int year, int month,
 			int day) {
 		
 		//TODO Is there a way around changing from Calendar to ints repeatedly?
 		pickedDate.set(year, month, day);
 		
 		datePickerListener.setDate(pickedDate);
 	}
 	
 	/**
 	 * Interface for sending the date back to the activity that initialized it.
 	 * Observe - that activity must implement this interface. 
 	 */
 	public interface DatePickerListener {
 		
 		/**
 		 * Sets the date of the activity that initialized the date picker.
 		 * @param releaseDate The movie's release date
 		 */
 		public void setDate(Calendar pickedDate); 
 	}
 	
 }
