 package com.dunksoftware.seminoletix;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 public class MainActivity extends Activity {
 
 	Button mMoveOn;
 	/* This page can host a splash screen of some sort.
 	 * In about two seconds or so, it can navigate to the login page
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		/*
 		 * To get a handle on an UI element, you must first set
 		 * the appropriate id name in the xml file for the element
 		 * Next, you must use the findViewById function and downcast the
 		 * the return value to the appropriate class. Oh and don't forget
 		 * to declare the variable (I'm pretty sure that you guys knew that
 		 * though)
 		 */
 		mMoveOn = (Button)findViewById(R.id.UI_ButtonMoveOn);
 		
 		/*
 		 * Two way to set event handlers in android. 
 		 * YES, android does support the anonymous inner class
 		 * route (My Favorite). This is mostly meant for a one time
 		 * usage of the function. (Only used for one button)
 		 * 
 		 *  Second route is to declare and define a OnClickListener
 		 *  variable's functions
 		 */
 		mMoveOn.setOnClickListener(new OnClickListener() {
 			Intent nextActivityIntent;
 			
 			@Override
 			public void onClick(View arg0) {
 				nextActivityIntent = new Intent();
 				
 				/*
 				 * First parameter is the current content that is shooting
 				 * off the navigation method. (I believe this allows Android 
 				 * to have it's internal breadcrumb trail so you can hit the back
 				 * button to get to the most previous screen(activity)
 				 * 
 				 * In MOST cases, this will simply be
 				 * <CurrentClass>.this; We'll get to the special cases later
 				 * 
 				 * The second param is what class( or in this case, Activity's class)
 				 * you want to navigate to. <DesiredClass>.class
 				 * This wields a few special cases too.
 				 * 
 				 * Intents are the secret storage containers that are used to transport
 				 * generic data to the next activity <IntentName>.putExtra(name, value)
 				 * Kind of like a map or set
 				 */
				nextActivityIntent.setClass(MainActivity.this, LoginActivity.class);
				//nextActivityIntent.setClass(MainActivity.this, ListActivity.class);
 				
 				// Now that the navigation data is set, let's fire off
 				startActivity(nextActivityIntent);
 				
 			}
 		});
 		
 		// Second way
 		
 		/* Begin inner class MyOnClick */
 		class MyOnClick implements OnClickListener
 		{
 			// can declare variables if needed
 			Intent nextActivityIntent;
 			
 			public MyOnClick() {
 				nextActivityIntent = new Intent();
 			}
 			
 			@Override
 			public void onClick(View v) {
 				nextActivityIntent.setClass(MainActivity.this, LoginActivity.class);
 				startActivity(nextActivityIntent);
 			}		
 			
 		} /* End of inner class MyOnClick */
 		
 		/** Usage of class implementing OnClickListener interface */
 		//mMoveOn.setOnClickListener(new MyOnClick());
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 }
