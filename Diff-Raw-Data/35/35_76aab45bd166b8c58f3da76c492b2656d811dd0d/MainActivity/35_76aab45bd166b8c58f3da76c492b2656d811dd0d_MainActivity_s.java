 package com.charlesmadere.android.fishnspots;
 
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 import android.location.Location;
 import android.os.Bundle;
 
 
 /**
  * This class is the app's entry point.
  */
 public class MainActivity extends Activity implements
 	LocationListFragment.LocationListFragmentListeners,
 	SaveCurrentLocationFragment.SaveCurrentLocationListeners
 {
 
 
 	private LocationListFragment locationListFragment;
 	private SaveCurrentLocationFragment saveCurrentLocationFragment;
 
 
 
 
 	@Override
 	protected void onCreate(final Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main_activity);
 
 		final FragmentManager fManager = getFragmentManager();
 		final FragmentTransaction fTransaction = fManager.beginTransaction();
 
 		if (isDeviceLarge())
 		{
 			locationListFragment = (LocationListFragment) fManager.findFragmentById(R.id.main_activity_fragment_location_list_fragment);
 		}
 		else
 		{
 			locationListFragment = new LocationListFragment();
 			fTransaction.add(R.id.main_activity_container, locationListFragment);
 		}
 
 		fTransaction.commit();
 	}
 
 
 	@Override
 	public void onBackPressed()
 	{
 		if (!isDeviceLarge() && saveCurrentLocationFragment != null && saveCurrentLocationFragment.isVisible())
 		{
 			final ActionBar actionBar = getActionBar();
 			actionBar.setDisplayHomeAsUpEnabled(false);
 			actionBar.setTitle(R.string.fish_n_spots);
 		}
 
 		super.onBackPressed();
 	}
 
 
 
 
 	/**
 	 * Checks to see what size screen this device has. This method will return
 	 * true if the device has a large screen, and as such said device should be
 	 * using the dual pane, fragment based, layout stuff.
 	 * 
 	 * @return
 	 * Returns true if this device has a large screen.
 	 */
 	private boolean isDeviceLarge()
 	{
 		return findViewById(R.id.main_activity_container) == null;
 	}
 
 
 
 
 	@Override
 	public void onClickSaveCurrentLocation()
 	{
 		saveCurrentLocationFragment = new SaveCurrentLocationFragment();
 		final FragmentManager fManager = getFragmentManager();
 
 		if (isDeviceLarge())
 		{
 			saveCurrentLocationFragment.show(fManager, null);
 		}
 		else
 		{
 			final FragmentTransaction fTransaction = fManager.beginTransaction();
 			fTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
 			fTransaction.hide(locationListFragment);
 			fTransaction.add(R.id.main_activity_container, saveCurrentLocationFragment);
 			fTransaction.addToBackStack(null);
 			fTransaction.commit();
 
 			final ActionBar actionBar = getActionBar();
 			actionBar.setDisplayHomeAsUpEnabled(true);
 			actionBar.setTitle(R.string.save_current_location);
 		}
 	}
 
 
 	@Override
 	public void onLocationSave(final Location location)
 	{
 		if (saveCurrentLocationFragment != null && saveCurrentLocationFragment.isVisible())
 		{
 			final FragmentManager fManager = getFragmentManager();
 			fManager.popBackStack();
 
 			final FragmentTransaction fTransaction = fManager.beginTransaction();
 			fTransaction.remove(saveCurrentLocationFragment);
 			fTransaction.commit();
 		}
 	}
 
 
 }
