 package it.skb.carsharing.ui;
 
 import java.util.Calendar;
 
 import android.annotation.SuppressLint;
 import android.app.DatePickerDialog;
 import android.app.DatePickerDialog.OnDateSetListener;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.os.Bundle;
 import android.widget.DatePicker;
 
 @SuppressLint("ValidFragment")
 public class DatePickerFragment extends DialogFragment implements OnDateSetListener {
 	
 	private MainActivity ma;
 	private TimePickerFragment timePickerFragment;
 	
 	public DatePickerFragment(MainActivity ma) {
 		this.ma = ma;
 		this.timePickerFragment = new TimePickerFragment(ma);
 	}
 
 	@Override
 	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		ma.setDate(getTag(), year, monthOfYear, dayOfMonth);
		timePickerFragment.show(getFragmentManager(), getTag());				
 	}
 
 	@Override
     public Dialog onCreateDialog(Bundle savedInstanceState) {
         final Calendar c = Calendar.getInstance();
         int year = c.get(Calendar.YEAR);
         int month = c.get(Calendar.MONTH);
         int day = c.get(Calendar.DAY_OF_MONTH);
 
         return new DatePickerDialog(getActivity(), this, year, month, day);
     }
 }
