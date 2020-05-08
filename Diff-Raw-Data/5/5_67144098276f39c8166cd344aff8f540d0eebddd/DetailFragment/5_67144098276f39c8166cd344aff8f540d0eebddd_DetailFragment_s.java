 package csci498.gnanda.lunchList;
 
 
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.EditText;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class DetailFragment extends Fragment {
 	
 	private static final String TAKE_OUT = "take_out";
 	private static final String SIT_DOWN = "sit_down";
 	private static final String ARG_REST_ID = "csci498.gnanda.lunchList.ARG_REST_ID";
 	private EditText name;
 	private EditText address;
 	private EditText phone;
 	private EditText notes;
 	private EditText feed;
 	private RadioGroup types;
 	private RestaurantHelper helper;
 	private String restaurantId;		
 	private TextView location;
 	private LocationManager locMgr;
 	private double latitude = 0.0d;
 	private double longitude = 0.0d;
 		
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		setHasOptionsMenu(true);		
 	}
 	
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		
 		locMgr = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
 		getWidgetsFromXML();
 		
 		Bundle args = getArguments();
 		if (args != null) {
 			loadRestaurant(args.getString(ARG_REST_ID));
 		}
 	}
 	
 	public void loadRestaurant(String restaurantId) {
 		this.restaurantId = restaurantId;
 		
 		if (restaurantId != null) {
 			load();
 		}
 	}
 	
 	public static DetailFragment newInstance(long id) {
 		DetailFragment result = new DetailFragment();
 		Bundle args = new Bundle();
 		
 		args.putString(ARG_REST_ID, String.valueOf(id));
 		result.setArguments(args);
 		
 		return result;
 	}
 
 	@Override
 	public void onPause() {
 		save();
 		helper.close();
 		locMgr.removeUpdates(onLocationChange);
 		super.onPause();
 	}
 	
 	public RestaurantHelper getHelper() {
 		if (helper == null) {
 			helper = new RestaurantHelper(getActivity());
 		}
 		return helper;
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.detail_form, container, false);
 	}
 	
 	@Override
 	public void onPrepareOptionsMenu(Menu menu) {
 		if (restaurantId == null) {
 			menu.findItem(R.id.location).setEnabled(false);
 			menu.findItem(R.id.map).setEnabled(false);
 		}
 	}
 	
 	private void getWidgetsFromXML() {
 		name = (EditText) getView().findViewById(R.id.name);
 		address = (EditText) getView().findViewById(R.id.addr);
 		phone = (EditText) getView().findViewById(R.id.phone);
 		types = (RadioGroup) getView().findViewById(R.id.types);
 		notes = (EditText) getView().findViewById(R.id.notes);
 		feed = (EditText) getView().findViewById(R.id.feed);
 		location = (TextView) getView().findViewById(R.id.location);
 	}
 	
 	private void load() {
 		Cursor c = getHelper().getById(restaurantId);
 		
 		c.moveToFirst();
 		name.setText(getHelper().getName(c));
 		address.setText(getHelper().getAddress(c));
 		notes.setText(getHelper().getNotes(c));
 		feed.setText(getHelper().getFeed(c));
 		
 		if (getHelper().getType(c).equals(SIT_DOWN)) {
 			types.check(R.id.sit_down);
 		}
 		else if (getHelper().getType(c).equals(TAKE_OUT)) {
 			types.check(R.id.take_out);
 		}
 		else {
 			types.check(R.id.delivery);
 		}
 		
 		latitude = getHelper().getLatitude(c);
 		longitude = getHelper().getLongitude(c);
 		
 		location.setText(getLocationOutput(getHelper().getLatitude(c), getHelper().getLongitude(c)));
 		c.close();
 	}
 	
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		inflater.inflate(R.menu.details_options, menu);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == R.id.feed) {
 			if(isNetworkAvailable()) {
 				Intent i = new Intent(getActivity(), FeedActivity.class);
 				i.putExtra(FeedActivity.FEED_URL, feed.getText().toString());
 				startActivity(i);
 			}
 			else {
 				Toast.makeText(getActivity(), R.string.internet_not_available, Toast.LENGTH_LONG).show();
 			}
 			return true;
 		}
 		else if (item.getItemId() == R.id.location) {
 			locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, onLocationChange);
 			return true;
 		}
 		else if (item.getItemId() == R.id.map) {
 			Intent i = new Intent(getActivity(), RestaurantMap.class);
 			
 			i.putExtra(RestaurantMap.EXTRA_LATITUDE, latitude);
 			i.putExtra(RestaurantMap.EXTRA_LONGITUDE, longitude);
 			i.putExtra(RestaurantMap.EXTRA_NAME, name.getText().toString());
 			
 			startActivity(i);
 			return true;
 		}
 		else if (item.getItemId() == R.id.help) {
 			startActivity(new Intent(getActivity(), HelpPage.class));
 		}
 		
 		return super.onOptionsItemSelected(item);
 	}
 	
 	private LocationListener onLocationChange = new LocationListener() {
 		
 		@Override
 		public void onLocationChanged(Location fix) {
 			getHelper().updateLocation(restaurantId, fix.getLatitude(), fix.getLongitude());
 			location.setText(getLocationOutput(fix.getLatitude(), fix.getLongitude()));
 			locMgr.removeUpdates(onLocationChange);
 			Toast.makeText(getActivity(), R.string.location_saved, Toast.LENGTH_LONG).show();
 		}
 		
 		@Override
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 			// Not used		
 		}
 		
 		@Override
 		public void onProviderEnabled(String provider) {
 			// Not used				
 		}
 		
 		@Override
 		public void onProviderDisabled(String provider) {
 			// Not used			
 		}		
 
 	};
 	
 	private String getLocationOutput(Object latitude, Object longitude) {
 		return String.valueOf(latitude) + ", " + String.valueOf(longitude);
 	}
 	
 	private boolean isNetworkAvailable() {
 		ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo info = cm.getActiveNetworkInfo();
 		return info != null;
 	}
 
 	private void save() {
 		String type = null;
 
 		switch (types.getCheckedRadioButtonId()) {
 		case R.id.sit_down:
 			type = SIT_DOWN;
 			break;
 		case R.id.take_out:
 			type = TAKE_OUT;
 			break;
 		case R.id.delivery:
 			type = "delivery";
 			break;
 		}
 
 		if (restaurantId == null) {
			getHelper().insert(name.getText().toString(), address.getText().toString(), type, notes.getText().toString(), feed.getText().toString(), phone.toString());
 		}
 		else {
			getHelper().update(restaurantId, name.getText().toString(), address.getText().toString(), type, notes.getText().toString(), feed.getText().toString(), phone.toString());
 		}
 	}
 	
 }
