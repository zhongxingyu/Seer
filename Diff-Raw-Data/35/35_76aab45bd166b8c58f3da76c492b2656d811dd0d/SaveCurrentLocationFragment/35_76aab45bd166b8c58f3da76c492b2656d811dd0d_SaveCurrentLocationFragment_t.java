 package com.charlesmadere.android.fishnspots;
 
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.content.Context;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 
 public class SaveCurrentLocationFragment extends DialogFragment
 {
 
 
 	private TextView textView_header;
 	private EditText editText_name;
 	private EditText editText_latitude;
 	private EditText editText_longitude;
 	private Button button_saveCurrentLocation;
 
 
 	/**
 	 * Indicates whether or not a location search is currently running.
 	 */
 	private boolean locationSearchIsRunning = false;
 
 
 	private LocationManager locationManager;
 	private LocationListener locationListener;
 
 
 	/**
 	 * 
 	 */
 	private SaveCurrentLocationListeners listeners;
 
 
 	/**
 	 * 
 	 */
 	public interface SaveCurrentLocationListeners
 	{
 
 
 		/**
		 * @return
		 * 
		 */
		public boolean isDeviceSmall();


		/**
 		 * 
 		 * 
 		 * @param location
 		 * 
 		 */
 		public void onLocationSave(final Location location);
 
 
 	}
 
 
 
 
 	@Override
 	public void onCreate(final Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setHasOptionsMenu(true);
 	}
 
 
 	@Override
 	public Dialog onCreateDialog(final Bundle savedInstanceState)
 	{
 		final Dialog dialog = super.onCreateDialog(savedInstanceState);
 		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
 
 		return dialog;
 	}
 
 
 	@Override
 	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
 	{
 		return inflater.inflate(R.layout.save_current_location_fragment, container, false);
 	}
 
 
 	@Override
 	public void onActivityCreated(final Bundle savedInstanceState)
 	{
 		super.onActivityCreated(savedInstanceState);
 
 		findViews();
 
 		final TextWatcher textWatcher = new TextWatcher()
 		{
 			@Override
 			public void afterTextChanged(final Editable s)
 			{
 				findViews();
 
 				if (editText_name.length() >= 1 && editText_latitude.length() >= 1 && editText_longitude.length() >= 1)
 				{
 					button_saveCurrentLocation.setEnabled(true);
 				}
 				else
 				{
 					button_saveCurrentLocation.setEnabled(false);
 				}
 			}
 
 
 			@Override
 			public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after)
 			{
 
 			}
 
 
 			@Override
 			public void onTextChanged(final CharSequence s, final int start, final int before, final int count)
 			{
 
 			}
 		};
 
 		editText_name.addTextChangedListener(textWatcher);
 		editText_latitude.addTextChangedListener(textWatcher);
 		editText_longitude.addTextChangedListener(textWatcher);
 
 		locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
 		locationListener = new LocationListener()
 		{
 			@Override
 			public void onLocationChanged(final Location location)
 			{
 				locationSearchIsRunning = false;
 				locationManager.removeUpdates(locationListener);
 
 				findViews();
 
 				editText_latitude.setText(String.valueOf(location.getLatitude()));
 				editText_longitude.setText(String.valueOf(location.getLongitude()));
 
 				flushViews();
 
 				button_saveCurrentLocation.setOnClickListener(new View.OnClickListener()
 				{
 					@Override
 					public void onClick(final View v)
 					{
 						listeners.onLocationSave(location);
 					}
 				});
 			}
 
 
 			@Override
 			public void onProviderDisabled(final String provider)
 			{
 				
 			}
 
 
 			@Override
 			public void onProviderEnabled(final String provider)
 			{
 				
 			}
 
 
 			@Override
 			public void onStatusChanged(final String provider, final int status, final Bundle extras)
 			{
 				
 			}
 		};
 
 		refreshLocation();
 	}
 
 
 	@Override
 	public void onAttach(final Activity activity)
 	// This makes sure that the Activity containing this Fragment has
 	// implemented the callback interface. If the callback interface has not
 	// been implemented, an exception is thrown.
 	{
 		super.onAttach(activity);
 
 		try
 		{
 			listeners = (SaveCurrentLocationListeners) activity;
 		}
 		catch (final ClassCastException e)
 		{
 			throw new ClassCastException(activity.toString() + " must implement listeners!");
 		}
 	}
 
 
 	@Override
 	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater)
 	{
 		menu.removeItem(R.id.location_list_fragment_menu_save_current_location);

		if (listeners.isDeviceSmall())
		{
			inflater.inflate(R.menu.save_current_location_fragment, menu);
		}

 		super.onCreateOptionsMenu(menu, inflater);
 	}
 
 
 	@Override
 	public boolean onOptionsItemSelected(final MenuItem item)
 	{
 		switch (item.getItemId())
 		{
 			case android.R.id.home:
 				getActivity().onBackPressed();
 				break;
 
 			case R.id.save_current_location_fragment_menu_refresh:
 				refreshLocation();
 				break;
 
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 
 		return true;
 	}
 
 
 	@Override
 	public void onPrepareOptionsMenu(final Menu menu)
 	{
		final MenuItem refresh = menu.findItem(R.id.save_current_location_fragment_menu_refresh);

		if (refresh != null)
		{
			refresh.setEnabled(!locationSearchIsRunning);
		}
 	}
 
 
 
 
 	/**
 	 * Checks to see if all of the layout items that we're using from this
 	 * class's layout have been found (and are not null). If any single one has
 	 * not been found (and is therefore null), then this method will find every
 	 * single layout item.
 	 */
 	private void findViews()
 	{
 		if (textView_header == null || editText_name == null || editText_latitude == null
 			|| editText_longitude == null || button_saveCurrentLocation == null)
 		{
 			final View view = getView();
 
 			textView_header = (TextView) view.findViewById(R.id.save_current_location_fragment_textview_header);
 			editText_name = (EditText) view.findViewById(R.id.save_current_location_fragment_edittext_name);
 			editText_latitude = (EditText) view.findViewById(R.id.save_current_location_fragment_edittext_latitude);
 			editText_longitude = (EditText) view.findViewById(R.id.save_current_location_fragment_edittext_longitude);
 			button_saveCurrentLocation = (Button) view.findViewById(R.id.save_current_location_fragment_button_save_current_location);
 		}
 	}
 
 
 	/**
 	 * Enables or disables the EditText layout items that we're using in this
 	 * class's layout. If we're currently searching for the phone's location,
 	 * then this will disable the EditText layout items. If we're not currently
 	 * searching for the phone's location, then this will enable the EditText
 	 * layout items. After all of this is done, the menu is invalidated so that
 	 * it can be rebuilt.
 	 */
 	private void flushViews()
 	{
 		if (locationSearchIsRunning)
 		{
 			textView_header.setText(R.string.searching_for_location);
 
 			editText_latitude.setText(null);
 			editText_latitude.setEnabled(false);
 
 			editText_longitude.setText(null);
 			editText_longitude.setEnabled(false);
 		}
 		else
 		{
 			textView_header.setText(R.string.location_found);
 
 			editText_latitude.setEnabled(true);
 			editText_longitude.setEnabled(true);
 		}
 
 		getActivity().invalidateOptionsMenu();
 	}
 
 
 	/**
 	 * If the location search is running, then this method will do nothing. If
 	 * the location search is not running, then this will start the location
 	 * search.
 	 */
 	private void refreshLocation()
 	{
 		if (!locationSearchIsRunning)
 		{
 			locationSearchIsRunning = true;
 
 			findViews();
 			flushViews();
 
 			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
 		}
 	}
 
 
 }
