 package com.example.demofragments02;
 
 import android.os.Bundle;
 import android.view.Menu;
 import android.app.Activity;
 
 /**
  * 
  * @author Lothar Rubusch
  */
 public class MainActivity extends Activity {
 	@Override
 	public void onCreate(Bundle savedInstancesState){
 		super.onCreate(savedInstancesState);
		setContentView(R.layout.fragment_main);
 /*		
 		FragmentCurrtime fragment = (FragmentCurrtime) getFragmentManager().findFragmentById(R.id.fragment_currtime);
 		if( null != fragment && fragment.isInLayout()){
 			fragment.setText("foo");
 		}
*/		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 };
 
