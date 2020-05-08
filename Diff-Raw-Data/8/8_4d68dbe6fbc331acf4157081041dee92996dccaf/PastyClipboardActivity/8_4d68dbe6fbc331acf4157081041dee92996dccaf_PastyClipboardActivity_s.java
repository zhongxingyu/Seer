 package de.electricdynamite.pasty;
 
 /*
  *    Copyright 2012 Philipp Geschke
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.view.Window;
 
 import de.electricdynamite.pasty.AddItemFragment.AddItemFragmentCallbackListener;
 import de.electricdynamite.pasty.ClipboardFragment.PastyClipboardFragmentListener;
 import de.electricdynamite.pasty.PastyAlertDialogFragment.PastyAlertDialogListener;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
import android.util.Log;
 import android.widget.Toast;
 
 public class PastyClipboardActivity extends SherlockFragmentActivity implements PastyAlertDialogListener, PastyClipboardFragmentListener, AddItemFragmentCallbackListener {
 	
 
     private static final String TAG = PastyClipboardActivity.class.toString();
     public String versionName;
     public int versionCode;
 	protected PastyPreferencesProvider prefs;
 	protected FragmentManager mFragmentManager;
 	private static ClipboardFragment mClipboardFragment = new ClipboardFragment();
 	private ConnectivityManager mConnMgr;
 	protected boolean isOnline = false;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
     	try {
 			this.versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
 			this.versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
 		} catch (NameNotFoundException e) {
 			e.printStackTrace();
 		}
 		// Request features
     	requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
     	if(mFragmentManager == null) mFragmentManager = getSupportFragmentManager();
     	if(this.prefs == null) 
     		this.prefs = new PastyPreferencesProvider(getApplication());
         setContentView(R.layout.main);
 
 		if (savedInstanceState == null) {
 		}
     }
     
     
     @Override
     public void onResume() {
     	super.onResume();
     	
     	// Let's get preferences
 		reloadPreferences();
 		
 		checkOnlineState();
 		
     	if(!prefs.getUsername().isEmpty() && !prefs.getPassword().isEmpty()) {
     		// check for the Intent extras
     		if (Intent.ACTION_SEND.equals(getIntent().getAction())) {
     			if(isOnline) {
     				AddItemFragment mAddItemFragment = new AddItemFragment();
     				FragmentTransaction ft = mFragmentManager.beginTransaction();
     				Fragment prev = mFragmentManager.findFragmentByTag("AddItemDialog");
     				if (prev != null) {
     					ft.remove(prev);
     				}
     				// Create and show the dialog.
     				mAddItemFragment.show(ft, "AddItemDialog");
     				ft = null;
     				prev = null;
     				mAddItemFragment = null;
     			} else {
     		    	Toast.makeText(getApplicationContext(), getString(R.string.error_adding_offline), Toast.LENGTH_LONG).show();
     		    	this.finish();
     			}
     		} else {
     			if(!mClipboardFragment.isAdded()) {
     				getSupportFragmentManager().beginTransaction()
     				.replace(R.id.fragment_placeholder, mClipboardFragment)
     				.commit();
     			} else {
     				if(prefs.wasUpdated()) {
     					//Log.d(TAG,"settings were updated, restarting loader");
     					mClipboardFragment.restartLoading();
     				}
     			}
     		}
     	} else {
     		showAlertDialog(PastySharedStatics.DIALOG_CREDENTIALS_NOT_SET);
     	}
     }
     
     @Override
     public void onPause() {
     	super.onPause();
     	FragmentTransaction ft = mFragmentManager.beginTransaction();
         Fragment prev = mFragmentManager.findFragmentByTag("AddItemDialog");
         if (prev != null) {
         	ft.remove(prev);
         	ft.commit();
         	ft = null;
         	prev = null;
         }
     }
 
     protected void showAlertDialog(int id) {
     	Fragment prev = mFragmentManager.findFragmentByTag("AlertFragment"+id);
     	if(prev == null) {
     		PastyAlertDialogFragment AlertDialog = new PastyAlertDialogFragment();
     		AlertDialog.setId(id);
     		AlertDialog.setCancelable(false);
     		AlertDialog.show(mFragmentManager, "AlertFragment"+id);
     	}
     }
     
     public void reloadPreferences() {
     	// Restore preferences
     	prefs.reload();
     }
     
     protected void checkOnlineState() {    	
     	// Check for network connectivity
     	if(mConnMgr == null)
     		mConnMgr = (ConnectivityManager) getApplication().getSystemService(Context.CONNECTIVITY_SERVICE); 
 
     	NetworkInfo networkInfo = mConnMgr.getActiveNetworkInfo();
     	if (networkInfo != null && networkInfo.isConnected()) {
     		setOnline();
     	} else {
     		setOffline();
     	}
     	networkInfo = null;
     }
     
     private void setOnline() {
     	this.isOnline = true;
     	invalidateOptionsMenu();
     }
     
     private void setOffline() {
     	this.isOnline = false;
     	invalidateOptionsMenu();
     }
     
    @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	
         MenuInflater inflater = getSupportMenuInflater();
         inflater.inflate(R.menu.menu, menu);
         return true;
     }
    
    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
 	   if (!isOnline) {
 		   menu.getItem(0).setVisible(false);
 	   } else {
 		   menu.getItem(0).setVisible(true);
 	   }
        return true;
    }
     
 	@Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         switch (item.getItemId()) {
         case R.id.menu_add:
         	checkOnlineState();
         	if(isOnline) {
 	        	FragmentTransaction ft = mFragmentManager.beginTransaction();
 	        	Fragment prev = mFragmentManager.findFragmentByTag("AddItemDialog");
 	        	if (prev != null) {
 	        		ft.remove(prev);
 	        	} 
 	        	// Create and show the dialog.
 	            AddItemFragment mAddItemFragment = new AddItemFragment();
 			    mAddItemFragment.show(ft, "AddItemDialog");
 			    mAddItemFragment = null;
 	        	ft = null;
 		        prev = null;
         	} else {
 		    	Toast.makeText(getApplicationContext(), getString(R.string.error_adding_offline), Toast.LENGTH_LONG).show();
         	}
 		    return true;
         case R.id.menu_reload:
         	checkOnlineState();
         	mClipboardFragment.restartLoading();
 	        return true;
         case R.id.menu_settings:
         	Intent settingsActivity = new Intent(getBaseContext(),
                     PastyPreferencesActivity.class);
         	settingsActivity.putExtra("versionName", this.versionName);
         	settingsActivity.putExtra("versionCode", this.versionCode);
         	startActivity(settingsActivity);
             return true;
         case R.id.menu_about:
         	Intent aboutActivity = new Intent(getBaseContext(),
         			PastyAboutActivity.class);
         	aboutActivity.putExtra("versionName", this.versionName);
         	aboutActivity.putExtra("versionCode", this.versionCode);
         	startActivity(aboutActivity);
 	        return true;
         default:
                 return super.onOptionsItemSelected(item);
                 
         }
     }
 	
 	@Override
 	public void onFinishPastyAlertDialog(int signal) {
 		actOnSignal(signal);
 	}
 
 	private void actOnSignal(int signal) {
 
 		switch(signal) {
 			case PastySharedStatics.SIGNAL_EXIT:
 				this.finish();
 				break;
 			case PastySharedStatics.SIGNAL_ACTIVITY_SETTINGS:
 				Intent settingsActivity = new Intent(getBaseContext(),
 	                    PastyPreferencesActivity.class);
 	        	settingsActivity.putExtra("versionName", versionName);
 	        	settingsActivity.putExtra("versionCode", versionCode);
 	        	startActivity(settingsActivity);
 				break;
 			case PastySharedStatics.SIGNAL_ACTIVITY_ABOUT:
 	        	Intent aboutActivity = new Intent(getBaseContext(),
 	        			PastyAboutActivity.class);
 	        	aboutActivity.putExtra("versionName", this.versionName);
 	        	aboutActivity.putExtra("versionCode", this.versionCode);
 	        	startActivity(aboutActivity);
 				break;
 		}
 	}
 
 	@Override
 	public void onPastyClipboardFragmentSignal(int signal) {
 		actOnSignal(signal);		
 	}
 
 
 	@Override
 	public void onPastyClipboardFragmentSignal(int signal, int dialogId) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	@Override
 	public void onAddItemFragmentCallbackSignal(int signal) {
 		actOnSignal(signal);			
 	}
 
 
 	
 	
     
 }
