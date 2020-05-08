 /**
  * 
  */
 package com.kulkarni.gasmileagecalculator;
 
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
import android.widget.TextView;
 
 /**
  * @author Amey Kulkarni
  *
  */
 public class FillupFragment extends Fragment {
 
 	/**
 	 * 
 	 */
 	public FillupFragment() {
 		// TODO Auto-generated constructor stub
 	}
 	
 	@Override
 	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		View rootView = inflater.inflate(R.layout.fillup_fragment, container, false);
		
		TextView dateTextView = (TextView) rootView.findViewById(R.id.textView_fillup_date);
		dateTextView.setText(getTodaysDate());
 		return rootView;
 	}
 
	
	private CharSequence getTodaysDate () {
		Date date = new Date ();
		SimpleDateFormat sdf = (SimpleDateFormat) DateFormat.getDateInstance();
		
		return sdf.format(date);
	}
 }
