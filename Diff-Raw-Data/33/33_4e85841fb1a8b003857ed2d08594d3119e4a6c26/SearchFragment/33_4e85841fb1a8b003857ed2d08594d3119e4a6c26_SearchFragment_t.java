 package com.example.airportstatus.fragments;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.Toast;
 
 import com.example.airportstatus.Airport;
 import com.example.airportstatus.QueryActivity;
 import com.example.airportstatus.R;
 
 public class SearchFragment extends Fragment {
 	
 	Button btnGo;
 	AutoCompleteTextView tvAirportCode;
 	LocationManager locationManager;
 	LocationListener locationListener;
 	SharedPreferences locationPrefs;
 	
 	public static final String AIRPORT_CODE = "airport_code";
 	public static final String PREFS_NAME = "AirportStatusPrefs";
 	public static final String PREFS_LATITUDE = "LAT";
 	public static final String PREFS_LONGITUDE= "LON";
 	
 	@Override
 	public View onCreateView(LayoutInflater inf, ViewGroup parent, Bundle savedInstanceState) {
 		return inf.inflate(R.layout.fragment_search, parent, false);
 	}
 	
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		setupLocationStorage();
         setupLocationListener();
         
         setupButton();
         setupTextView();
 	}
 	
 	private void setupLocationStorage() {
         locationPrefs = getActivity().getSharedPreferences(PREFS_NAME, 0);    	
     }
 	
 	private void setupLocationListener() {
     	locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
     	locationListener = new LocationListener() {
     		@Override
     		public void onLocationChanged(Location location) {
    			if (isAdded()) {
	    			SharedPreferences locationPrefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	    			SharedPreferences.Editor editor = locationPrefs.edit();
	    			editor.putString(PREFS_LATITUDE, String.valueOf(location.getLatitude()));
	    			editor.putString(PREFS_LONGITUDE, String.valueOf(location.getLongitude()));
	    			editor.commit();
    			}
     		}
 
 			@Override
 			public void onProviderDisabled(String provider) {
 				// TODO Auto-generated method stub
 			}
 
 			@Override
 			public void onProviderEnabled(String provider) {
 				// TODO Auto-generated method stub
 			}
 
 			@Override
 			public void onStatusChanged(String provider, int status,
 					Bundle extras) {
 				// TODO Auto-generated method stub
 			}
     	};
     	
     	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 0, locationListener);
     }
 	
 	
     private void setupButton() {
     	btnGo = (Button) getActivity().findViewById(R.id.btnGo);
         btnGo.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
     }
 
     private void setupTextView() {
     	ArrayList<String> codes = new ArrayList<String> (Airport.IATA_CODES.keySet());
     	ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                 android.R.layout.simple_dropdown_item_1line, codes);
     	tvAirportCode = (AutoCompleteTextView) getActivity().findViewById(R.id.tvAirportCode);
     	tvAirportCode.setAdapter(adapter);
     }
     
     public void onSearchBtnClick(View v) {
     	String textEntered = tvAirportCode.getText().toString();
     	String code = Airport.IATA_CODES.get(textEntered);
     	int index = new ArrayList<String>(Airport.IATA_CODES.values()).indexOf(code);
     	if (code != null) {
 	    	Intent i = new Intent(getActivity(), QueryActivity.class);
 	    	i.putExtra(AIRPORT_CODE, code);
 	    	i.putExtra("airport_index", String.valueOf(index));
 	    	startActivity(i);
     	} else {
     		Toast.makeText(getActivity(),  "Could not find airport code " + textEntered, Toast.LENGTH_SHORT).show();
     	}
     }
 
 }
