 package com.madisp.bad.demo;
 
 import android.app.Activity;
 import android.app.FragmentTransaction;
 import android.os.Bundle;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.FrameLayout;
 
 /**
  * Created with IntelliJ IDEA.
  * User: madis
  * Date: 3/20/13
  * Time: 8:44 AM
  */
 public class MainActivity extends Activity {
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		View v = new FrameLayout(this);
 		v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
 		v.setId(R.id.mainContainer);
 		setContentView(v);
 
		if (savedInstanceState == null) {
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.add(R.id.mainContainer, new ListFragment());
			ft.commit();
		}
 	}
 }
