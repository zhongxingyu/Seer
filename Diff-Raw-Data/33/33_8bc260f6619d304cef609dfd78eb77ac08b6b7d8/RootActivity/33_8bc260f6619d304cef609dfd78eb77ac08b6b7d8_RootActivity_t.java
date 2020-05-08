 package com.vanderbilt.people.finder;
 
 import com.vanderbilt.people.finder.Provider.Constants;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.util.Log;
 
 public class RootActivity extends Activity 
 {
 	private static final String TAG = "RootActivity";
 	private static final int INIT_TAG = 1;
 	
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		
 	    if (UserData.needsInitialization(getApplicationContext()))
 	    {
 	    	Intent i = new Intent(Settings.ACTION_ADD_ACCOUNT);
 	    	i.putExtra(Settings.EXTRA_AUTHORITIES, new String[] { Constants.AUTHORITY });
 	    	Log.v(TAG, "launching startup activity");
 	    	Log.v(TAG, i.toString());
 	    	startActivityForResult(i, INIT_TAG);
 	    }
 	    else
 	    {
 	    	Log.v(TAG, "starting MainActivity");
 	    	startActivity(new Intent(this, MainActivity.class));
 	    	finish();
 	    }
 	}
 	
 	protected void onActivityResult(int requestCode, int resultCode, Intent data)
     {
     	if (requestCode == INIT_TAG)
     	{
     		if (resultCode == RESULT_OK)
     		{
     			Log.v(TAG, "starting MainActivity");
     			startActivity(new Intent(this, MainActivity.class));
     			finish();
     		}
     	}
     	
 //    	Log.e(TAG, "Main activity couldn't be started!");
     }
 }
