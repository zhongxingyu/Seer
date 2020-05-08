 /**
  *	Copyright 2012 Norio bvba
  *
  *	This program is free software: you can redistribute it and/or modify
  *	it under the terms of the GNU General Public License as published by
  *	the Free Software Foundation, either version 3 of the License, or
  *	(at your option) any later version.
  *	
  *	This program is distributed in the hope that it will be useful,
  *	but WITHOUT ANY WARRANTY; without even the implied warranty of
  *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *	GNU General Public License for more details.
  *	
  *	You should have received a copy of the GNU General Public License
  *	along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package be.norio.twunch.android.ui;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import android.app.AlertDialog;
 import android.content.ContentProviderOperation;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.OperationApplicationException;
 import android.database.Cursor;
 import android.graphics.drawable.AnimationDrawable;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.RemoteException;
 import android.provider.BaseColumns;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.widget.ImageView;
 import android.widget.Toast;
 import be.norio.twunch.android.R;
 import be.norio.twunch.android.TwunchApplication;
 import be.norio.twunch.android.provider.TwunchContract;
 import be.norio.twunch.android.provider.TwunchContract.Twunches;
 import be.norio.twunch.android.service.SyncService;
 import be.norio.twunch.android.util.AnalyticsUtils;
 import be.norio.twunch.android.util.PrefsUtils;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.ActionBar.Tab;
 import com.actionbarsherlock.app.ActionBar.TabListener;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.google.android.apps.iosched.util.DetachableResultReceiver;
 import com.google.android.apps.iosched.util.Lists;
 
 public class TwunchListActivity extends BaseActivity implements TabListener {
 
 	TwunchListFragment[] mFragments = new TwunchListFragment[2];
 	private final static String[] SORTS = new String[] { Twunches.SORT_DATE, Twunches.SORT_DISTANCE };
 	private final static String[] PAGES = new String[] { AnalyticsUtils.Pages.TWUNCH_LIST_DATE,
 			AnalyticsUtils.Pages.TWUNCH_LIST_DISTANCE };
 
 	MenuItem refreshMenuItem;
 
 	private DetachableResultReceiver resultReceiver;
 
 	LocationManager locationManager;
 	LocationListener locationListener;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
 		final ActionBar bar = getSupportActionBar();
 		bar.addTab(bar.newTab().setText("By Date").setTabListener(this), false);
 		bar.addTab(bar.newTab().setText("By Distance").setTabListener(this), false);
 
 		bar.setSelectedNavigationItem(PrefsUtils.getLastTab());
 
 		resultReceiver = new DetachableResultReceiver(new Handler());
 		resultReceiver.setReceiver(new SyncResultReceiver());
 
 		// Acquire a reference to the system Location Manager
 		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 
 		// Define a listener that responds to location updates
 		locationListener = new LocationListener() {
 			public void onLocationChanged(Location location) {
 				if (location != null) {
 					new UpdateDistancesTask().execute(location);
 				}
 			}
 
 			public void onStatusChanged(String provider, int status, Bundle extras) {
 				// Do nothing
 			}
 
 			public void onProviderEnabled(String provider) {
 				// Do nothing
 			}
 
 			public void onProviderDisabled(String provider) {
 				// Do nothing
 			}
 		};
 
 	}
 
 	private interface TwunchesQuery {
 
 		String[] PROJECTION = { BaseColumns._ID, Twunches.LATITUDE, Twunches.LONGITUDE };
 
 		int _ID = 0;
 		int LATITUDE = 1;
 		int LONGITUDE = 2;
 	}
 
 	@Override
 	public void onTabSelected(Tab tab, FragmentTransaction ft) {
 		int pos = tab.getPosition();
 		if (mFragments[pos] == null) {
 			mFragments[pos] = new TwunchListFragment();
 			Bundle args = new Bundle();
 			args.putString(TwunchListFragment.EXTRA_SORT, SORTS[pos]);
 			mFragments[pos].setArguments(args);
 		}
 		PrefsUtils.setLastTab(pos);
 		getSupportFragmentManager().beginTransaction().replace(android.R.id.content, mFragments[pos]).commit();
 		AnalyticsUtils.trackPageView(PAGES[pos]);
 	}
 
 	@Override
 	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
 		// Do nothing
 	}
 
 	@Override
 	public void onTabReselected(Tab tab, FragmentTransaction ft) {
 		// Do nothing
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		getSupportMenuInflater().inflate(R.menu.fragment_twunch_list, menu);
 		refreshMenuItem = menu.findItem(R.id.menuRefresh);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menuRefresh:
 			refreshTwunches(true);
 			return true;
 		case R.id.menuMap:
 			startActivity(new Intent(this, TwunchesMapActivity.class));
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	public void refreshTwunches(boolean force) {
 		long lastSync = PrefsUtils.getLastUpdate();
 		long now = (new Date()).getTime();
 		long oneDay = 1000 * 60 * 60 * 24;
 		if (!force && lastSync != 0 && (now - lastSync < oneDay)) {
 			Log.d(TwunchApplication.LOG_TAG, "Not refreshing twunches");
 			return;
 		}
 		if (refreshMenuItem != null) {
 			refreshMenuItem.setActionView(R.layout.actionbar_indeterminate_progress);
 			((AnimationDrawable) ((ImageView) refreshMenuItem.getActionView().findViewById(R.id.refreshing)).getDrawable()).start();
 		}
 		Log.d(TwunchApplication.LOG_TAG, "Refreshing twunches");
 		Intent intent = new Intent(this, SyncService.class);
 		intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, resultReceiver);
 		startService(intent);
 	}
 
 	private class SyncResultReceiver implements DetachableResultReceiver.Receiver {
 
 		@Override
 		public void onReceiveResult(int resultCode, Bundle resultData) {
 			switch (resultCode) {
 			case SyncService.STATUS_RUNNING: {
 				break;
 			}
 			case SyncService.STATUS_FINISHED: {
 				if (refreshMenuItem != null) {
 					if (refreshMenuItem.getActionView() != null) {
 						((AnimationDrawable) ((ImageView) refreshMenuItem.getActionView().findViewById(R.id.refreshing)).getDrawable())
 								.stop();
 					}
 					refreshMenuItem.setActionView(null);
 				}
 				Toast.makeText(TwunchListActivity.this, getString(R.string.download_done), Toast.LENGTH_SHORT).show();
 				String provider = locationManager.getBestProvider(new Criteria(), true);
 				new UpdateDistancesTask().execute(locationManager.getLastKnownLocation(provider));
 				break;
 			}
 			case SyncService.STATUS_ERROR: {
				if (refreshMenuItem != null) {
					if (refreshMenuItem.getActionView() != null) {
						((AnimationDrawable) ((ImageView) refreshMenuItem.getActionView().findViewById(R.id.refreshing)).getDrawable())
								.stop();
					}
					refreshMenuItem.setActionView(null);
				}
 				AlertDialog.Builder builder = new AlertDialog.Builder(TwunchListActivity.this);
 				builder.setMessage(R.string.download_error);
 				builder.setCancelable(false);
 				builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						// Do nothing
 					}
 				});
 				builder.create().show();
 				break;
 			}
 			}
 
 		}
 	}
 
 	class UpdateDistancesTask extends AsyncTask<Location, Void, Void> {
 
 		@Override
 		protected Void doInBackground(Location... locations) {
 			if (locations[0] == null) {
 				return null;
 			}
 			Cursor c = getContentResolver().query(Twunches.CONTENT_URI, TwunchesQuery.PROJECTION, null, null, null);
 			if (!c.moveToFirst()) {
 				c.close();
 				return null;
 			}
 			final ArrayList<ContentProviderOperation> batch = Lists.newArrayList();
 			do {
 				ContentProviderOperation.Builder builder = ContentProviderOperation.newUpdate(Twunches.CONTENT_URI);
 				builder.withSelection(Twunches._ID + "=?", new String[] { Long.toString(c.getLong(TwunchesQuery._ID)) });
 				Location twunchLocation = new Location("");
 				twunchLocation.setLatitude(c.getDouble(TwunchesQuery.LATITUDE));
 				twunchLocation.setLongitude(c.getDouble(TwunchesQuery.LONGITUDE));
 				builder.withValue(Twunches.DISTANCE, (int) locations[0].distanceTo(twunchLocation));
 				batch.add(builder.build());
 			} while (c.moveToNext());
 			c.close();
 			try {
 				getContentResolver().applyBatch(TwunchContract.CONTENT_AUTHORITY, batch);
 			} catch (RemoteException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (OperationApplicationException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			return null;
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		refreshTwunches(false);
 		// Start listening for location updates
 		String provider = locationManager.getBestProvider(new Criteria(), true);
 		if (provider != null) {
 			locationManager.requestLocationUpdates(provider, 300000, 500, locationListener);
 		}
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		locationManager.removeUpdates(locationListener);
 	}
 
 }
