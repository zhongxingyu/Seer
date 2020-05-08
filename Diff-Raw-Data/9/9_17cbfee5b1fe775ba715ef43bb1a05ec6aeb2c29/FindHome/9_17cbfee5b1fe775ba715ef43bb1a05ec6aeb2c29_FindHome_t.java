 package com.trydish.find;
 
 import java.util.List;
 import java.util.Locale;
 
 import android.app.Fragment;
 import android.app.FragmentTransaction;
 import android.content.Context;
 import android.location.Address;
 import android.location.Criteria;
 import android.location.Geocoder;
 import android.location.Location;
 import android.location.LocationManager;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.GridView;
 import android.widget.ImageButton;
 import android.widget.PopupMenu;
 import android.widget.PopupWindow;
 import android.widget.Spinner;
 
 import com.trydish.main.R;
 
 
 
 public class FindHome extends Fragment implements OnClickListener {
 
 	//Implements OnClickListener so that we don't have to define onClick methods in the LoginHome
 	//PopupWindow usage modeled after http://www.mobilemancer.com/2011/01/08/popup-window-in-android/ 
 
 	//Keep track of what distance user has selected from drop down menu. Saving now b/c likely later passed to other function 
 	private String searchDistance = "1 mile";
 	private static Context context;
 	private View myView;
 	private LocationManager manager;
 	private Location location;
 	private double latitude;
 	private double longitude;
 	private PopupWindow pop;
 
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.activity_find_home,
 				container, false);
 
 		myView = view;
 		context = view.getContext();
 		//Note we have to call findViewById on the view b/c we are not in an Activity
 		Spinner distanceSpinner = (Spinner) view.findViewById(R.id.distance_spinner);
 		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(view.getContext(), R.array.distance_array, android.R.layout.simple_spinner_item);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		distanceSpinner.setAdapter(adapter);
 
 		//Create a new OnItemSelectedListener for the Spinner using anonymous class to define necessary methods
 		OnItemSelectedListener listener = new OnItemSelectedListener() {
 
 			//comment
 			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
 				String itemSelected = (String) parent.getItemAtPosition(pos);
 				searchDistance = itemSelected;
 				Log.d("FindHome", searchDistance);
 				//note that when this tab is selected, this method is executed
 			}
 
 			//comment
 			public void onNothingSelected(AdapterView<?> parent) {
 
 			}
 		};
 		//Set the spinners listener
 		distanceSpinner.setOnItemSelectedListener(listener);
 
 		//Grab the buttons and set their onClickListeners to be this Fragment
 		ImageButton ib = (ImageButton) view.findViewById(R.id.search);
 		Button b = (Button) view.findViewById(R.id.my_location);
 		ib.setOnClickListener(this);
 		b.setOnClickListener(this);
 
 		GridView gridview = (GridView) view.findViewById(R.id.food_images);
 		gridview.setAdapter(new ImageAdapter(view.getContext()));
 
 		gridview.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
 				Fragment view_dish = new ViewDish();
 				FragmentTransaction trans = getFragmentManager().beginTransaction();
 
 				trans.replace(((ViewGroup) myView.getParent()).getId(), view_dish);
 				trans.addToBackStack(null);
 				trans.commit();
 			}
 		});
 
 		//Hide keyboard
 		getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
 
 		//define the behavior of the "DONE" key on the keyboard
 		/*
 	    EditText et = (EditText) myView.findViewById(R.id.search_box);
 	    et.setOnEditorActionListener(new EditText.OnEditorActionListener() {
 	        @Override
 	        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 	            if (actionId == EditorInfo.IME_ACTION_DONE){
 	                    //Do your stuff here
 	            		Log.d("FindHome","DONE pressed");
 	            		donePressed();
 	            		return true;
 	                } else {
 	                	return false;
 	                }
 	            } }); 
 		 */
 
 		//
 		manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
 		setLocation();
 
 		return view;
 
 	}
 
 	public void setLocation() {
 		String providerName = manager.getBestProvider(new Criteria(), true);
 		location = manager.getLastKnownLocation(providerName);
 
 		Button location_button = (Button) myView.findViewById(R.id.my_location);
 		location_button.setText("My Location");
 
 		if (location == null) {
 			location_button.setText("GPS Off");
 		} else {
 			latitude = location.getLatitude();
 			longitude = location.getLongitude();
 
 			try {
 				ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
 				NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
 				boolean isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnected(); 
 				
 				if (isConnected) {
 					Geocoder myLocation = new Geocoder(context.getApplicationContext(), Locale.getDefault());
 					List<Address> myList = myLocation.getFromLocation(latitude, longitude, 1);
 					int address_lines = myList.get(0).getMaxAddressLineIndex();
 
 					if (address_lines >= 1) {
 						String address = myList.get(0).getAddressLine(address_lines - 1);
 						String city = address.split(",", 2)[0];
 						location_button.setText(city);
 					}
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	//Called when search icon is clicked
 	public void searchClicked(View v) {
 		//EditText et = (EditText) myView.findViewById(R.id.search_box);
 		//et.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));
 		//hideSoftKeyboard(myView);
 		Log.d("Find Home", "search clicked");
 		InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
 		EditText et = (EditText) myView.findViewById(R.id.search_box);
 		imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
 	}
 
 	//Called when the My Location button is clicked to change location
 	//PopupWindow usage modeled after http://www.mobilemancer.com/2011/01/08/popup-window-in-android/ 
 	public void myLocationClicked(View v) {
 		Log.d("Find Home", "location clicked");
 		//Add the popup window
 		LayoutInflater inflater = LayoutInflater.from(context);
 		View popup_menu = inflater.inflate(R.layout.location_popup, (ViewGroup) myView.findViewById(R.id.location_popup));
 		pop = new PopupWindow(popup_menu, 700, 300, true);
 		pop.showAtLocation(popup_menu, Gravity.NO_GRAVITY, 50, 160);
 		//grab button and set onClickListener to be able to dismiss the window
 		Button b = (Button) popup_menu.findViewById(R.id.change_my_location);
 
 		//set b's OnClickListener to allow dismissing popup
 		b.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				//do something
 				}
 		});
 		
		Button c = (Button) popup_menu.findViewById(R.id.cancel_change_my_location);
 		c.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				//get rid of popup window
 				pop.dismiss();
 			}
 		});
 		
 		
 	}
 
 	
 	//Decides which method to call based on which button is clicked. Again, this is needed because by default buttons 
 	//onClickListener is not the Fragment
 	@Override
 	public void onClick(View arg0) {
 		switch(arg0.getId()) {
 		//search button clicked
 		case R.id.search:
 			searchClicked(arg0);
 			break;
 			//my location button clicked
 		case R.id.my_location:
 			myLocationClicked(arg0);
 			break;
 		}
 
 	}
 
 	public void donePressed() {
 		searchClicked(myView);
 	}
 
 }
