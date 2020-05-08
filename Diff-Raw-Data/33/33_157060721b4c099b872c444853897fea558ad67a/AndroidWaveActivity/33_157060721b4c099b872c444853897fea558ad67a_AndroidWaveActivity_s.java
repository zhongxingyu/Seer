 package edu.berkeley.androidwave.waveui;
 
 import edu.berkeley.androidwave.R;
 import edu.berkeley.androidwave.waverecipe.WaveRecipeAuthorization;
 import edu.berkeley.androidwave.waveservice.WaveService;
 
 import android.app.ListActivity;
 import android.app.AlertDialog;
 import android.content.ActivityNotFoundException;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import java.util.ArrayList;
 import java.util.Date;
 
 /**
  * AndroidWaveActivity
  *
  * This is the primary launch Activity of the Wave UI, which should be
  * displayed when the user launches the Wave app on their phone. It should
  * bind to the WaveService over the private local binding.
  */
 public class AndroidWaveActivity extends ListActivity {
     
     private final String TAG = AndroidWaveActivity.class.getSimpleName();
 
     private static final int REQUEST_CODE_EDIT_RECIPE = 1;
 
     protected WaveService mService;
     protected boolean mBound = false;
     
    protected ArrayList<WaveRecipeAuthorization> authorizations;
     protected AndroidWaveActivityListAdapter listAdapter;
     
     // UI outlets
     protected ListView listView;
     protected TextView emptyView;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         
         // connect the UI outlets
         listView = getListView();
         emptyView = (TextView) listView.getEmptyView();
 
         // placeholder empty authorizations array
         authorizations = new ArrayList<WaveRecipeAuthorization>();
         // set up the ListAdapter
         listAdapter = new AndroidWaveActivityListAdapter(this, R.layout.main_authorization_cell, authorizations);
         setListAdapter(listAdapter);
 
         // connect to WaveService
         Intent sIntent = new Intent(Intent.ACTION_MAIN);
         sIntent.setClass(this, WaveService.class);
         try {
             bindService(sIntent, mConnection, Context.BIND_AUTO_CREATE);
         } catch (Exception e) {
             Toast.makeText(AndroidWaveActivity.this, "Exception encountered, see log.", Toast.LENGTH_SHORT).show();
             Log.w(TAG, "Exception encountered in onCreate()", e);
         }
     }
 
     @Override
     protected void onStop() {
         super.onStop();
         
         if (mBound) {
             unbindService(mConnection);
             mBound = false;
         }
     }
     
     protected void reloadAuthorizations() {
         Log.d(TAG, "reloadAuthorizations()");
         
         authorizations = mService.recipeAuthorizations();
         
         Runnable r = new Runnable() {
             @Override
             public void run() {
                 // TODO: something cleaner than clearing then re-adding
                 listAdapter.clear();
                 if (authorizations.size() > 0) {
                     Date now = new Date();
                     for (WaveRecipeAuthorization a : authorizations) {
                         if (a.validForDate(now)) {
                             listAdapter.add(a);
                         }
                     }
                 }
                 listAdapter.notifyDataSetChanged();
             }
         };
         
         runOnUiThread(r);
     }
     
     protected void afterBind() {
         Log.d(TAG, "afterBind()");
         // populate the list of authorizations
         reloadAuthorizations();
     }
     
     @Override
     public void onListItemClick(ListView l, View v, int position, long id) {
         WaveRecipeAuthorization clickedAuth = listAdapter.getItem(position);
         
         Intent i = new Intent(Intent.ACTION_EDIT);
         i.setPackage("edu.berkeley.androidwave");
         i.putExtra(ViewRecipeAuthorizationActivity.RECIPE_ID_EXTRA, clickedAuth.getRecipe().getId());
         i.putExtra(ViewRecipeAuthorizationActivity.CLIENT_NAME_EXTRA, clickedAuth.getRecipeClientName());
         try {
             startActivityForResult(i, REQUEST_CODE_EDIT_RECIPE);
         } catch (ActivityNotFoundException anfe) {
             Toast.makeText(AndroidWaveActivity.this, "Error launching Wave UI", Toast.LENGTH_SHORT).show();
             Log.w(TAG, "ActivityNotFoundException raised while starting Recipe Edit UI", anfe);
         }
     }
     
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (requestCode == REQUEST_CODE_EDIT_RECIPE) {
             if (resultCode == RESULT_OK) {
                 // Edit was confirmed
                 Toast.makeText(AndroidWaveActivity.this, "Edit confirmed", Toast.LENGTH_SHORT).show();
                 try {
                     Thread.sleep(500);
                 } catch (InterruptedException ie) {}
                 reloadAuthorizations();
             } else {
                 // Edit was cancelled
                 Toast.makeText(AndroidWaveActivity.this, "Edit cancelled.", Toast.LENGTH_SHORT).show();
             }
         }
     }
     
     /**
      * Create the menu, which allows the user to export and empty the app's
      * data storage database
      */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.manager_options_menu, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch  (item.getItemId()) {
             case R.id.reset_wave:
                 return resetAndroidWave();
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
     
     /**
      * Resets AndroidWave to freshly installed state.  All authorizations,
      * cached recipes, and client keys are erased.
      */
     private boolean resetAndroidWave() {
         AlertDialog.Builder builder = new AlertDialog.Builder(AndroidWaveActivity.this);
         builder.setMessage("Are you sure you want to fully reset AndroidWave?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mService.reset();
         
                        reloadAuthorizations();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing
                    }
                });
         AlertDialog alert = builder.show();
         return true;
     }
     
     /** Defines callbacks for service binding, passed to bindService() */
     private ServiceConnection mConnection = new ServiceConnection() {
         @Override
         public void onServiceConnected(ComponentName className,
                 IBinder service) {
             // We've bound to LocalService, cast the IBinder and get LocalService instance
             mService = ((WaveService.LocalBinder)service).getService();
             mBound = true;
             afterBind();
         }
 
         @Override
         public void onServiceDisconnected(ComponentName className) {
             mBound = false;
         }
     };
 }
