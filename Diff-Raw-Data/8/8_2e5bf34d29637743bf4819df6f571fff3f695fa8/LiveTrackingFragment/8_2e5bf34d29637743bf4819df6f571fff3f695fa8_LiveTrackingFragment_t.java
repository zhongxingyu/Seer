 //*****************************************************************************
 // This file is part of bluetrack.
 //
 // bluetrack is free software: you can redistribute it and/or modify
 // it under the terms of the GNU General Public License as published by
 // the Free Software Foundation, either version 3 of the License, or
 // (at your option) any later version.
 //
 // bluetrack is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 //
 // You should have received a copy of the GNU General Public License
 // along with bluetrack.  If not, see <http://www.gnu.org/licenses/>.
 //*****************************************************************************
 
 package org.jonblack.bluetrack.activities;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.jonblack.bluetrack.R;
 import org.jonblack.bluetrack.adapters.LiveTrackingCursorAdapter;
 import org.jonblack.bluetrack.services.BluetoothLogService;
 import org.jonblack.bluetrack.storage.DeviceDiscoveryTable;
 import org.jonblack.bluetrack.storage.DeviceTable;
 import org.jonblack.bluetrack.storage.SessionTable;
 
 import android.app.Activity;
 import android.app.ListFragment;
 import android.app.LoaderManager;
 import android.bluetooth.BluetoothAdapter;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.CursorLoader;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.Loader;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class LiveTrackingFragment extends ListFragment
                                   implements LoaderManager.LoaderCallbacks<Cursor>
 {
   private static final String TAG = "LiveTrackingFragment";
   
   /**
    * Request code for handling calls to enable bluetooth."
    */
   private static final int REQUEST_ENABLE_BT = 1;
   
   /**
    * Intent used to communicate with the BluetoothLogService.
    */
   private Intent mBluetoothLogServiceIntent;
   
   /**
    * Whether or not tracking is in progress.
    */
   private boolean mTracking = false;
   
   /**
    * Id of this tracking session.
    */
   private long mSessionId = -1;
   
   /**
    * LiveTrackingCursorAdapter used by the list view to get data.
    */
   private LiveTrackingCursorAdapter mAdapter;
   
   /**
    * Receiver used when bluetooth device status has changed.
    */
   private final BroadcastReceiver mBtStateChangedReceiver = new BroadcastReceiver() {
     @Override
     public void onReceive(Context context, Intent intent)
     {
       // Get the action that prompted the broadcast
       String action = intent.getAction();
       if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED))
       {
         final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
         if (state == BluetoothAdapter.STATE_TURNING_OFF ||
             state == BluetoothAdapter.STATE_OFF)
         {
           stopBluetoothLogService();
         }
       }
     }
   };
   
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState)
   {
     super.onCreateView(inflater, container, savedInstanceState);
     
     setHasOptionsMenu(true);
     
     // Create an return the custom ListView layout which also displays an
     // 'empty list' message.
     return inflater.inflate(R.layout.live_tracking_list, null);
   }
   
   @Override
   public void onDestroyView()
   {
     super.onDestroyView();
     
     // Unregister the broadcast receiver for finding bluetooth devices.
     getActivity().unregisterReceiver(mBtStateChangedReceiver);
   }
   
   @Override
   public void onActivityCreated(Bundle savedInstanceState)
   {
     Log.d(TAG, "onActivityCreated");
     
     super.onActivityCreated(savedInstanceState);
     
     // Restore state
     if (savedInstanceState != null)
     {
       mSessionId = savedInstanceState.getLong("sessionId");
       mTracking = savedInstanceState.getBoolean("tracking");
       
       Log.v(TAG, String.format("Restoring state: mSessionId=%d mTracking=%s",
                                mSessionId, mTracking));
       
       if (mTracking)
       {
         // This should get the loader that was already created.
         getLoaderManager().initLoader(0, null, this);
       }
     }
     
     // Register broadcast receiver for bluetooth status changes
     getActivity().registerReceiver(this.mBtStateChangedReceiver,
                                    new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
     
     // Configure the ListView adapter, which will connect to the database.
     mAdapter = new LiveTrackingCursorAdapter(getActivity(), null, 0);
     setListAdapter(mAdapter);
   }
   
   @Override
   public Loader<Cursor> onCreateLoader(int id, Bundle args)
   {
     Log.d(TAG, "onCreateLoader");
     
     // Session id must have been set
     assert(mSessionId != -1);
     
     // Now create and return a CursorLoader that will take care of
     // creating a Cursor for the data being displayed.
     return new CursorLoader(getActivity(),
                             DeviceDiscoveryTable.CONTENT_URI,
                             new String[] {DeviceTable.COL_ID,
                                           "name",
                                           "mac_address",
                                           "rssi",
                                           "major_class"},
                             "device_discovery.session_id = ?",
                             new String[] {Long.toString(mSessionId)},
                             null);
   }
 
   @Override
   public void onLoadFinished(Loader<Cursor> loader, Cursor data)
   {
     Log.d(TAG, "onLoadFinished");
     
     // Fixes #9
     // When the tabs are changed and the cursor is re-initialised, the old
     // loader is re-used, which still has data, causing this function to be
     // called even though tracking might be disabled. This results in the list
     // being udpated with the results of the previous scan.
     //
     // Only update when tracking is in progress.
     if (mTracking)
     {
       // Swap the new cursor in. (The framework will take care of closing the
       // old cursor once we return.)
       mAdapter.swapCursor(data);
     }
   }
 
   @Override
   public void onLoaderReset(Loader<Cursor> loader)
   {
     Log.d(TAG, "onLoaderReset");
     
     // This is called when the last Cursor provided to onLoadFinished()
     // above is about to be closed.  We need to make sure we are no
     // longer using it.
     mAdapter.swapCursor(null);
   }
   
   /**
    * Starts the BluetoothLogService. This will continue to run until
    * stopBluetoothLogService() is called.
    */
   private void startBluetoothLogService()
   {
     // Create a new tracking session
     addSession();
     assert(mSessionId != -1);
     
     // Start the service
     Log.d(TAG, "Starting BluetoothLogService.");
     mBluetoothLogServiceIntent = new Intent(getActivity(), BluetoothLogService.class);
     mBluetoothLogServiceIntent.putExtra("sessionId", mSessionId);
     ComponentName cn = getActivity().startService(mBluetoothLogServiceIntent);
     if (cn != null)
     {
       Log.d(TAG, "Using already running service.");
     }
     
     // Toast starting the service
     Toast toast = Toast.makeText(getActivity(),
                                  getString(R.string.toast_tracking_started),
                                  Toast.LENGTH_SHORT);
     toast.show();
     
     // Cause the action bar menu to be updated so the button text can change.
     getActivity().invalidateOptionsMenu();
     
     // Prepare the loader. Either re-connect with an existing one, or start a
     // new one.
     getLoaderManager().initLoader(0, null, this);
     
     // Set the empty list view text
     TextView tv = (TextView) getActivity().findViewById(android.R.id.empty);
     tv.setText(R.string.live_tracking_on_list_empty);
     
     mTracking = true;
   }
   
   /**
    * Stops the BluetoothLogService.
    */
   private void stopBluetoothLogService()
   {
     // Stop service if it's running.
     if (mTracking)
     {
       Log.d(TAG, "Stopping BluetoothLogService.");
       getActivity().stopService(mBluetoothLogServiceIntent);
       
       Toast toast = Toast.makeText(getActivity(),
                                    getString(R.string.toast_tracking_stopped),
                                    Toast.LENGTH_SHORT);
       toast.show();
     }
     
     // Cause the action bar menu to be updated so the button text can change.
     getActivity().invalidateOptionsMenu();
     
     // Set the empty list view text
     TextView tv = (TextView) getActivity().findViewById(android.R.id.empty);
     tv.setText(R.string.live_tracking_off_list_empty);
     
     // Update the session
     finalizeSession();
     
    // Destroy the loader. This is no longer needed.
    getLoaderManager(). destroyLoader(0);
    
     mTracking = false;
   }
   
   @Override
   public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
   {
     inflater.inflate(R.menu.action_bar, menu);
   }
   
   @Override
   public void onPrepareOptionsMenu(Menu menu)
   {
     // Set the button text depending on the service state.
     MenuItem menuItem = menu.findItem(R.id.menu_toggle_tracking);
     if (mTracking)
     {
       menuItem.setTitle(getString(R.string.ab_stop_tracking));
     }
     else
     {
       menuItem.setTitle(getString(R.string.ab_start_tracking));
     }
   }
   
   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
     // TODO: Bit odd that the LiveTrackingFragment is responsible for this.
     switch (item.getItemId())
     {
     case R.id.menu_toggle_tracking:
       if (mTracking)
       {
         stopBluetoothLogService();
       }
       else
       {
         // Check that bluetooth is enabled. If it isn't, ask the user to
         // enable it. If the user enables it, the BluetoothLogService will be
         // started in the callback.
         BluetoothAdapter localBtAdapter = BluetoothAdapter.getDefaultAdapter();
         assert(localBtAdapter != null);
         
         if (!localBtAdapter.isEnabled())
         {
           Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
           startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
         }
         else
         {
           startBluetoothLogService();
         }
       }
       break;
     case R.id.menu_settings:
       Intent intent = new Intent(getActivity(), SettingsActivity.class);
       startActivity(intent);
       break;
     default:
       assert(false);
     }
     
     return true;
   }
   
   @Override
   public void onSaveInstanceState(Bundle outState)
   {
     super.onSaveInstanceState(outState);
     
     outState.putLong("sessionId", mSessionId);
     outState.putBoolean("tracking", mTracking);
   }
   
   /**
    * Adds a tracking session to the database.
    */
   private void addSession()
   {
     Log.i(TAG, "Adding session.");
     
     SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
     Date date = new Date();
     
     ContentValues values = new ContentValues();
     values.put("start_date_time", dateFormat.format(date));
     Uri uri = getActivity().getContentResolver().insert(SessionTable.CONTENT_URI, values);
     
     mSessionId = ContentUris.parseId(uri);
     
     assert(mSessionId != -1);
   }
   
   /**
    * Finalizes the tracking session by setting the end_date_time field.
    */
   private void finalizeSession()
   {
     Log.i(TAG, "Updating session end time.");
     
     SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
     Date date = new Date();
     
     ContentValues values = new ContentValues();
     values.put("end_date_time", dateFormat.format(date));
     Uri uri = Uri.withAppendedPath(SessionTable.CONTENT_URI,
                                    Long.toString(mSessionId));
     int updated_rows = getActivity().getContentResolver().update(uri, values,
                                                                  null, null);
     assert(updated_rows == 1);
   }
   
   /**
    * @see android.app.Activity.onActivityResult
    */
   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data)
   {
     // Handle result for starting "enable bluetooth" intent.
     if (requestCode == REQUEST_ENABLE_BT)
     {
       if (resultCode == Activity.RESULT_OK)
       {
         Log.d(TAG, "Bluetooth enabled.");
         startBluetoothLogService();
       }
       else
       {
         Log.d(TAG, "Bluetooth not enabled.");
       }
     }
   }
 }
