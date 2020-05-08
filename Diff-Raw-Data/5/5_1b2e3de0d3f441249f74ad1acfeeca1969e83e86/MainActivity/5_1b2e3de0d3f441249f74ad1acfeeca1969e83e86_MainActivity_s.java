 /*
  * Copyright (C) 2013 asksven
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.asksven.mytrack;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.asksven.android.common.utils.DateUtils;
 import com.asksven.mytrack.LocationService;
 import com.asksven.mytrack.PreferencesActivity;
 import com.asksven.mytrack.ReadmeActivity;
 import com.asksven.mytrack.utils.Configuration;
 import com.asksven.mytrack.utils.Constants;
 import com.google.ads.AdRequest;
 import com.google.ads.AdView;
 
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.content.pm.PackageInfo;
 import android.util.Log;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.Spinner;
 import android.widget.TableLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends SherlockActivity
 
 {
 	
 	/**
 	 * The logging TAG
 	 */
 	private static final String TAG = "MainActivity";
 	
 	/**
 	 * a progess dialog to be used for long running tasks
 	 */
 	ProgressDialog m_progressDialog;
 	
 	/** The event receiver for updated from the service */
 	private ConnectionUpdateReceiver m_connectionUpdateReceiver;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 	
 		AdView adView = (AdView)this.findViewById(R.id.adView);
 	    adView.loadAd(new AdRequest());
 	    
         try
         {
         	TextView versionTextView = (TextView) findViewById(R.id.textViewVersion);
         	TextView nameTextView = (TextView) findViewById(R.id.textViewName);
         	TextView hintTextView = (TextView) findViewById(R.id.textViewHint);
         	
         	if (Configuration.isFullVersion(this))
         	{
 	    		nameTextView.setText("My Track full");
 	    		hintTextView.setText("");
 	    		Log.i(TAG, "full version was detected");
         	}
         	PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
         	versionTextView.setText(pinfo.versionName);
         }
         catch (Exception e)
         {
         	Log.e(TAG, "An error occured retrieveing the version info: " + e.getMessage());
         }
         
         	
 
         // Show release notes when first starting a new version
 		String strLastRelease	= sharedPrefs.getString("last_release", "0");
 		String strCurrentRelease = "";
 
 		try
 		{
 			PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
 			
 	    	strCurrentRelease = Integer.toString(pinfo.versionCode);
 		}
 		catch (Exception e)
 		{
 			// nop strCurrentRelease is set to ""
 		}
 
 		if (!strLastRelease.equals(strCurrentRelease))
     	{
     		// show the readme
 	    	Intent intentReleaseNotes = new Intent(this, ReadmeActivity.class);
 	    	intentReleaseNotes.putExtra("filename", "readme.html");
 	        this.startActivity(intentReleaseNotes);
 	        
 	        // save the current release to properties so that the dialog won't be shown till next version
 	        SharedPreferences.Editor editor = sharedPrefs.edit();
 	        editor.putString("last_release", strCurrentRelease);
 	        editor.commit();
     	}
 		
 		// update the enabled stats
     	CheckBox enabledCheckBox = (CheckBox) findViewById(R.id.checkBoxEnabled);
 		boolean bEnabled	= sharedPrefs.getBoolean("enabled", true);
 		enabledCheckBox.setChecked(bEnabled);
 		
   	}
 
 
 	/* Request updates at startup */
 	@Override
 	protected void onResume()
 	{
 		super.onResume();
     	
 		startService();
 		
 		// set up the listener for connection status changes 
 		if (m_connectionUpdateReceiver == null)
 		{
 			m_connectionUpdateReceiver = new ConnectionUpdateReceiver();
 		}
 		IntentFilter intentFilter = new IntentFilter(Constants.getInstance(this).BROADCAST_STATUS_CHANGED);
 		registerReceiver(m_connectionUpdateReceiver, intentFilter);
 		
 		// update the status
 		this.updateStatus();
 	}
 
 	/* Remove the event listener updates when Activity is paused */
 	@Override
 	protected void onPause()
 	{
 		super.onPause();
 		
 		// unregister event listener
 		if (m_connectionUpdateReceiver != null)
 		{
 			unregisterReceiver(m_connectionUpdateReceiver);
 		}
 	}
 	
 
 	/** 
      * Add menu items
      * 
      * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
      */
     public boolean onCreateOptionsMenu(Menu menu)
     {  
     	MenuInflater inflater = getSupportMenuInflater();
         inflater.inflate(R.menu.mainmenu, menu);
         return true;
     }
     
     /** 
      * Define menu action
      * 
      * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
      */
     public boolean onOptionsItemSelected(MenuItem item)
     {  
         switch (item.getItemId())
         {  
 	        case R.id.preferences:  
 	        	Intent intentPrefs = new Intent(this, PreferencesActivity.class);
 	            this.startActivity(intentPrefs);
 	        	break;	
 	        case R.id.quick_dialog:
 	        	showQuickDialog(this);
 	        	break;
 	        case R.id.release_notes:
             	// Release notes
             	Intent intentReleaseNotes = new Intent(this, ReadmeActivity.class);
             	intentReleaseNotes.putExtra("filename", "readme.html");
                 this.startActivity(intentReleaseNotes);
             	break;
 	        case R.id.credits:
             	// Release notes
             	Intent intentCredits = new Intent(this, CreditsActivity.class);
                 this.startActivity(intentCredits);
             	break;
             	
         }
         
         return true;
     }
 
     /**
      * Handler when a checkbox on the layout was checked/unchecked
      * @param view
      */
     public void onCheckboxClicked(View view)
     {
         // Is the view now checked?
         boolean checked = ((CheckBox) view).isChecked();
         
         // Check which checkbox was clicked
         switch(view.getId())
         {
             case R.id.checkBoxEnabled:
                 // save the current release to properties so that the dialog won't be shown till next version
         		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
     	        SharedPreferences.Editor editor = sharedPrefs.edit();
     	        editor.putBoolean("enabled", checked);
     	        editor.commit();
                 break;
         }
     }
     
     private void updateStatus()
     {
 	    // Set the connection state
 		TextView statusTextView = (TextView) findViewById(R.id.textViewStatus);
 		LocationService myService = LocationService.getInstance();
 		if (myService != null)
 		{
 			statusTextView.setText(myService.getStatus());
 		}
 		else
 		{
 			statusTextView.setText(Constants.getInstance(this).STATUS_SERVICE_NOT_STARTED);
 		}
 				
 	    final TableLayout statusLayout = (TableLayout) findViewById(R.id.layoutStatus);
 
     	if (myService != null)
     	{
     		myService.setStatus(myService.getStatus());
     	}
 	    
 		// update the status info
 		TextView tvMode = (TextView) findViewById(R.id.textViewMode);
 		TextView tvRemaining 	= (TextView) findViewById(R.id.textViewRemaing);
 		TextView tvAccuracy 	= (TextView) findViewById(R.id.textViewValueAccuracy);
 		TextView tvInterval 	= (TextView) findViewById(R.id.textViewValueInterval);
 		TextView tvUpdated 		= (TextView) findViewById(R.id.textViewValueUpdated);
 		
 		LocationService service = LocationService.getInstance();
 		if (service != null)
 		{
 			// mode
 			if (!service.isQuickChangeRunning())
 			{
 				tvMode.setText(getString(R.string.layout_main_mode_normal));
 				tvRemaining.setText("-");
 			}
 			else
 			{
 				tvMode.setText(getString(R.string.layout_main_mode_quick));
 				tvRemaining.setText(DateUtils.formatDurationLong(service.getUntil() - System.currentTimeMillis()));
 			}
 			
 			tvAccuracy.setText(String.valueOf(service.getAccuracy() + " m"));
 			tvInterval.setText(DateUtils.formatDurationLong(service.getInterval()) );
 			
 			long updated = service.getUpdated();
 			long since = service.getUpdated(); 
 			if (updated != 0)
 			{
 				tvUpdated.setText(DateUtils.formatShort(since));
 			}
 			else
 			{
 				tvUpdated.setText("-");
 			}
 		}
 		else
 		{
 			tvMode.setText("-");
 			tvAccuracy.setText("-");
 			tvInterval.setText("-");
 			tvUpdated.setText("-");
 			tvRemaining.setText("-");
 
 		}
     }
 
 	/** 
      * Starts the service 
      */
 	private void startService()
 	{
 		if( !LocationService.isServiceRunning(this) )
 		{
 			Intent i = new Intent();
 			i.setClassName( "com.asksven.mytrack", LocationService.SERVICE_NAME );
 			startService( i );
 			Log.i(getClass().getSimpleName(), "startService()");
 		}
 	}
 	
 	
 
 	private class ConnectionUpdateReceiver extends BroadcastReceiver
 	{
 	    @Override
 	    public void onReceive(Context context, Intent intent)
 	    {
 	        if (intent.getAction().equals(Constants.getInstance(MainActivity.this).BROADCAST_STATUS_CHANGED))
 	        {
 	        	TextView statusTextView = (TextView) findViewById(R.id.textViewStatus);
 	        	LocationService myService = LocationService.getInstance();
 	        	if (myService != null)
 	        	{
 	        		String address = myService.getAddress();
 	        		if (!address.equals(""))
 	        		{
 	        			statusTextView.setText(myService.getStatus() + ": " + address);
 	        		}
 	        		else
 	        		{ 
 	        			statusTextView.setText(myService.getStatus());
 	        		}
 	        	}
 	        	else
 	        	{
 	        		statusTextView.setText(Constants.getInstance(MainActivity.this).STATUS_SERVICE_NOT_STARTED);
 	        	}
 	        }
 	    }
 	}
 	
 	/**
 	 * Shows a dialog to capture the quick action parameters
 	 * @param context
 	 */
 	private void showQuickDialog(Context context)
 	{
     	final Dialog dialog = new Dialog(context);
 
     	dialog.setContentView(R.layout.quick_action_dialog);
     	dialog.setTitle(getString(R.string.dialog_quick_settings_title));
 
     	// configure first spinner
 		final Spinner spinnerInterval = (Spinner) dialog.findViewById(R.id.spinnerInterval);
 		
 		ArrayAdapter spinnerIntervalAdapter = ArrayAdapter.createFromResource(
 	            this, R.array.quickIntervalLabels, android.R.layout.simple_spinner_item);
 		spinnerIntervalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 	    
 		spinnerInterval.setAdapter(spinnerIntervalAdapter);
 
 		// configure second spinner
 		final Spinner spinnerAccuracy = (Spinner) dialog.findViewById(R.id.spinnerAccuracy);
 		
 		ArrayAdapter spinnerAccuracyAdapter = ArrayAdapter.createFromResource(
 	            this, R.array.quickAccuracyLabels, android.R.layout.simple_spinner_item);
 		spinnerAccuracyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 	    
 		spinnerAccuracy.setAdapter(spinnerAccuracyAdapter);
 
 		// configure third spinner
 		final Spinner spinnerDuration = (Spinner) dialog.findViewById(R.id.spinnerDuration);
 		
 		ArrayAdapter spinnerDurationAdapter = ArrayAdapter.createFromResource(
 	            this, R.array.quickDurationLabels, android.R.layout.simple_spinner_item);
 		spinnerDurationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 	    
 		spinnerDuration.setAdapter(spinnerDurationAdapter);
 		
 		Button buttonOk 	= (Button) dialog.findViewById(R.id.ButtonOk);
 		Button buttonCancel = (Button) dialog.findViewById(R.id.ButtonCancel);
 		Button buttonReset 	= (Button) dialog.findViewById(R.id.ButtonReset);
 		
 		// Check if we already have a qhick change running
 		LocationService myService = LocationService.getInstance();
     	if (myService != null)
     	{
     		if (myService.isQuickChangeRunning())
     		{
     			// disable all except reset
     			spinnerAccuracy.setEnabled(false);
     			spinnerInterval.setEnabled(false);
     			spinnerDuration.setEnabled(false);
     			
     			buttonOk.setEnabled(false);
     			buttonCancel.setEnabled(true);
     			buttonReset.setEnabled(true);
     			buttonReset.setOnClickListener( new Button.OnClickListener()
     			 {
     			     @Override
     			     public void onClick(View v)
     			     {
     			    	 LocationService.getInstance().resetQuickChange();
     			    	 
     			         dialog.dismiss();
     			         updateStatus();
     			     }
     			 });
 				buttonCancel.setOnClickListener(new Button.OnClickListener()
 				{
 					@Override
 					public void onClick(View v)
 					{
 						// do nothing
 						dialog.dismiss();
 					}
 				});
     			
     			// set selections
     			spinnerAccuracy.setSelection(myService.getAccuracyIndex());
     			spinnerInterval.setSelection(myService.getIntervalIndex());
     			spinnerDuration.setSelection(myService.getDurationIndex());
     			
     		}
     		else
     		{
     			// disable reset
     			spinnerAccuracy.setEnabled(true);
     			spinnerInterval.setEnabled(true);
     			spinnerDuration.setEnabled(true);
 
     			buttonOk.setEnabled(true);
     			buttonCancel.setEnabled(true);
     			buttonReset.setEnabled(false);
 
     			// set selection from prefs
     	    	
     	    	int iAccuracy = 0;
     	    	int iInterval = 0;
     	    	int iDuration = 0;
     	    	try
     	    	{
     	    		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
     	        	
     	    		iAccuracy = Integer.valueOf(sharedPrefs.getString("quick_update_accuracy", "0"));
     	    		iInterval = Integer.valueOf(sharedPrefs.getString("quick_update_interval", "0"));
     	    		iDuration = Integer.valueOf(sharedPrefs.getString("quick_update_duration", "0"));
     	    	}
     	    	catch (Exception e)
     	    	{
     	    		Log.e(TAG, "An error occured while reading quick action preferences");
     	    	}
 
     			spinnerAccuracy.setSelection(iAccuracy);
     			spinnerInterval.setSelection(iInterval);
     			spinnerDuration.setSelection(iDuration);
 
     			buttonOk.setOnClickListener( new Button.OnClickListener()
     			{
     				
 					@Override
 					public void onClick(View v)
 					{
 						LocationService.getInstance()
 							.setQuickChange(
 									spinnerInterval.getSelectedItemPosition(),
 									spinnerAccuracy.getSelectedItemPosition(),
 									spinnerDuration.getSelectedItemPosition());
 						
 			    		updateStatus();
 
 						dialog.dismiss();
 					}
 				});
 				buttonCancel.setOnClickListener(new Button.OnClickListener()
 				{
 					@Override
 					public void onClick(View v)
 					{
 						// do nothing
 						dialog.dismiss();
 					}
 				});
 
     		}
 
     		dialog.show();
     	}
     	else
     	{
     		Toast.makeText(this, Constants.getInstance(this).STATUS_SERVICE_NOT_STARTED, Toast.LENGTH_SHORT).show();
     	}
 	}
 	
 	
 	
     public void openURL( String inURL )
     {
         Intent browse = new Intent( Intent.ACTION_VIEW , Uri.parse( inURL ) );
 
         startActivity( browse );
     }
 
 }
