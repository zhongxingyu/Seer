 package com.andriod.tailorassist;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.EditText;
 
 public class MeasurmentFragment extends Fragment {
 	String caption;
 	public MeasurmentFragment(String caption){
 		this.caption = "Enter "+caption+" Details";
 //EditText measurementField;
 //    	
 //    	measurementField = (EditText)getView().findViewById(R.id.editText_shirtMeasurement);
 //    	measurementField.setHint(caption);
 	}
 	 @Override
 	    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 	        // Inflate the layout for this fragment
	        return inflater.inflate(R.layout.fragment_measurments, container, false);
 	    }
 	public String getCaption() {
 		return caption;
 	}
 	public void setCaption(String caption) {
 		this.caption = caption;
 	}
 	 
 	
 }
