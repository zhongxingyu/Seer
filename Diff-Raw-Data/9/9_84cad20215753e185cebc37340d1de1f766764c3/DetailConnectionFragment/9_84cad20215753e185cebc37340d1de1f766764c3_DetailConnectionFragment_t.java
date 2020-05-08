 package com.oldcherokee.ftpeekr.fragments;
 
 import com.oldcherokee.ftpeekr.R;
 
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 public class DetailConnectionFragment extends android.app.Fragment {
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	    Log.e("Test", "hello");
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 	    super.onActivityCreated(savedInstanceState);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
 	    View view = inflater.inflate(R.layout.details, container, false);
 	    return view;
 	}
 
 	public void setText(String item) {
	    TextView view = (TextView) getView().findViewById(R.id.details_text);
 	    view.setText(item);
 	}
 }
