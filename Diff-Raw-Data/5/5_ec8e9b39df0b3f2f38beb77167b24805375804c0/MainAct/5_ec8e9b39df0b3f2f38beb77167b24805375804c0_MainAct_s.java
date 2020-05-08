 package com.asksven.ledeffects;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.SubMenu;
 import android.widget.Toast;
 
 
 public class MainAct extends Activity
 {
     private boolean m_bIsStarted;
 	private boolean registered = false;
     
     static final int DIALOG_PREFERENCES_ID 	= 0;
 
     @Override
 	protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 		// tried to fix bug http://code.google.com/p/android/issues/detail?id=3259
 		// by programmatically registering to the event
         if (!registered)
         {
             registerReceiver(new BroadcastHandler(), new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
             registered = true;
         }
 
         // Set UI stuff
         setContentView(R.layout.main); //local_service_binding);
 
                 
         // run the underlying service
         startService();
     }
     
     // Called only the first time the options menu is displayed.
     // Create the menu entries.
     // Menu adds items in the order shown.
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         menu.add("Preferences");
         SubMenu myGroup = menu.addSubMenu("More...");
         myGroup.add("Stop Service");
         myGroup.add("Start Service");
 
         return true;
     }
 
     // handle menu selected
     public boolean onOptionsItemSelected(MenuItem item)
     {
     	if (item.getTitle().equals("Preferences"))
     	{
     		Intent intent = new Intent(this, com.asksven.ledeffects.PreferencesAct.class);
    			startActivityForResult(intent, DIALOG_PREFERENCES_ID);
     		return true;
     	}
     	else if (item.getTitle().equals("Stop Service"))
     	{
     		stopService();
     		return true;
     	}
    		else if (item.getTitle().equals("Start Service"))
    		{
    			startService();
    			return true;
    		}
     	else
     	{
     		return false;
     	}
     }
 
 	private void startService()
 	{
 		if( m_bIsStarted )
 		{
 			Toast.makeText(this, "Service already started", Toast.LENGTH_SHORT).show();
 		}
 		else
 		{
 			Intent i = new Intent();
			i.setClassName( "com.asksven.ledeffects", "com.asksven.ledeffects.Control" );
 			startService( i );
 			Log.i(getClass().getSimpleName(), "startService()");
 			m_bIsStarted = true;
 		}
 	}
 
 	private void stopService()
 	{
 		if( m_bIsStarted )
 		{
 			Intent i = new Intent();
			i.setClassName( "com.asksven.ledeffects", "com.asksven.ledeffects.Control" );
 			stopService( i );
 			Log.i(getClass().getSimpleName(), "stopService()");
 			m_bIsStarted = false;
 		}
 		else
 		{
 			Toast.makeText(this, "Service already started", Toast.LENGTH_SHORT).show();
 		}
 	}   
 }
