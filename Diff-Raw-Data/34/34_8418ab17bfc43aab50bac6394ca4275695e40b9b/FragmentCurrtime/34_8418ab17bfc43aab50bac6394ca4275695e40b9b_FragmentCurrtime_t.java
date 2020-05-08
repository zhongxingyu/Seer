 package com.example.demofragments02;
 
 import android.annotation.SuppressLint;
 import android.app.Fragment;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.TextView;
 
 /**
  * 
  * @author http://developer.android.com/guide/components/fragments.html
  *
  */
 @SuppressLint("NewApi")
 public class FragmentCurrtime extends Fragment {
	public void updateDetail(){
 		// get current time
 		String currtime = String.valueOf(System.currentTimeMillis());
 
 		// print data
 		if( this.isInLayout() ){
			setText(currtime);
 		}
 	}
 	
 	public void setText( String item ){
FIXME cast not possible, at click
 		TextView view = (TextView) getView().findViewById(R.id.textview_currtime);
 		view.setText(item);
 	}
 	
 	@Override
 	public View onCreateView(
 			LayoutInflater inflater
 			, ViewGroup container
 			, Bundle savedInstancesState ){
 		
 		View view = inflater.inflate( 
 				R.layout.fragment_currtime
 				, container
 				, false );
 		
		
		// set up button
 		Button button = (Button) view.findViewById( R.id.button_update );
 		button.setOnClickListener( new View.OnClickListener(){
 			@Override
 			public void onClick( View v ){
 				updateDetail();
 			}
 		});
 		
 		return view;
 	}
//*/
 }
