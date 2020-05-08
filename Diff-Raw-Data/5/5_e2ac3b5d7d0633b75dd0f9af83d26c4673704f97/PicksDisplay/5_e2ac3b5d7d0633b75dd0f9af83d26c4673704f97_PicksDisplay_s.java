 package com.theluvexchange.android;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.graphics.Typeface;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.SpannableString;
 import android.text.TextUtils.TruncateAt;
 import android.text.style.UnderlineSpan;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.RatingBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * @author Niranjan Singh
  * 
  *         Activity to set up the custom list view of PicksDisplay
  * 
  * 
  */
 
 public class PicksDisplay extends Activity {
 
 	private TheLuvExchange application = null;
 	private List<Pick> picksList = null;
 	private User user;
 	private City city;
 	ListView listViewRestaurants;
 	TextView textViewPopularity;
 	TextView textViewRating;
 	TextView textViewLatest;
 	String itemClicked;
 	private int type;
 
 	private Activity activity = this;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.picks);
 
 		itemClicked = getIntent().getStringExtra("MenuSelected");
 
 		TextView textViewPickTitle = (TextView) findViewById(R.id.textViewPickTitle);
 
 		application = (TheLuvExchange) this.getApplication();
 		picksList = new ArrayList<Pick>();
 
 		listViewRestaurants = (ListView) findViewById(R.id.picksList);
 
 		user = application.getUser();
 		city = application.getCity();
 
 		textViewPopularity = (TextView) findViewById(R.id.textViewPopularity);
 		textViewRating = (TextView) findViewById(R.id.textViewRating);
 		textViewLatest = (TextView) findViewById(R.id.textViewLatest);
 
 		// By default, the list is sorted by Popularity
 		textViewPopularity.setTypeface(null, Typeface.BOLD);
 		textViewRating.setTypeface(null, Typeface.NORMAL);
 		textViewLatest.setTypeface(null, Typeface.NORMAL);
 
 		// Set the title
 		textViewPickTitle.setText(itemClicked);
 
 		if (itemClicked.equalsIgnoreCase("Things To Do")) {
 			type = WebService.THINGS_TO_DO;
 			// Call the WebService.getThings() method to populate the cities
 			// list. Default sorting- Popularity
 			picksList.addAll(WebService.getThings(user, city));
  
 		} else if (itemClicked.equalsIgnoreCase("Restaurants & Clubs")) {
 			type = WebService.RESTAURANTS;
 			// Call the WebService.getRestaurants() method to populate the cities
 			// list. Default sorting- Popularity
 			picksList.addAll(WebService.getRestaurants(user, city));
 		} else if (itemClicked.equalsIgnoreCase("Airport Eats")) {
 			type = WebService.AIRPORT_EATS;
 			// Call the WebService.getAirportEats() method to populate the cities
 			// list. Default sorting- Popularity
 			picksList.addAll(WebService.getAirportEats(user, city));
 		}
 		
 		// Set Header to selected city name
 		TextView cityName = (TextView) findViewById(R.id.header);
 		cityName.setText(city.getName());
 
 		RestaurantAdapter restaurantAdapter = new RestaurantAdapter();
 		listViewRestaurants.setAdapter(restaurantAdapter);
 		listViewRestaurants.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
 
 		// Listener to handle click of an Item in the List
 		listViewRestaurants.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 
 				// Intent to start PickComments activity
 				Intent intent = new Intent(activity, PickComments.class);
 
 				// Pass Pick to the PickComments activity
 				intent.putExtra("Pick", picksList.get(position));
 
 				TextView textViewPickTitle = (TextView) findViewById(R.id.textViewPickTitle);
 				intent.putExtra("Title", textViewPickTitle.getText());
 				intent.putExtra("MenuSelected", itemClicked);
 
 				startActivity(intent);
 
 			}
 		});
 
 		textViewRating.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				picksList.clear();
 
 				// Call the WebService.getRestaurants() method to populate the
 				// cities
 				// Sorted by rating
 				picksList.addAll(WebService.getRestaurants(user, city,
 						"rating_avg", "desc"));
 
 				// Refresh list view
 				listViewRestaurants.invalidateViews();
 				textViewPopularity.setTypeface(null, Typeface.NORMAL);
 				textViewRating.setTypeface(null, Typeface.BOLD);
 				textViewLatest.setTypeface(null, Typeface.NORMAL);
 
 				// Toast.makeText(activity, "rating clicked",
 				// Toast.LENGTH_LONG).show();
 			}
 		});
 
 		textViewPopularity.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				picksList.clear();
 
 				// Call the WebService.getRestaurants() method to populate the
 				// cities
 				// Sorted by default popularity
 				picksList.addAll(WebService.getRestaurants(user, city));
 
 				// Refresh list view
 				listViewRestaurants.invalidateViews();
 				textViewPopularity.setTypeface(null, Typeface.BOLD);
 				textViewRating.setTypeface(null, Typeface.NORMAL);
 				textViewLatest.setTypeface(null, Typeface.NORMAL);
 
 				// Toast.makeText(activity, "popularity clicked",
 				// Toast.LENGTH_LONG).show();
 			}
 		});
 
 		textViewLatest.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				picksList.clear();
 
 				// Call the WebService.getRestaurants() method to populate the
 				// cities
 				// Sorted by created
 				picksList.addAll(WebService.getRestaurants(user, city,
 						"created", "desc"));
 
 				// Refresh list view
 				listViewRestaurants.invalidateViews();
 				textViewPopularity.setTypeface(null, Typeface.NORMAL);
 				textViewRating.setTypeface(null, Typeface.NORMAL);
 				textViewLatest.setTypeface(null, Typeface.BOLD);
 
 				// Toast.makeText(activity, "Latest clicked",
 				// Toast.LENGTH_LONG).show();
 			}
 		});
 		
 		Button addPlaceButton = (Button)findViewById(R.id.btnAddPlace);
 		addPlaceButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				Intent intent = new Intent(activity, AddLocation.class);
 				intent.putExtra("type", type);
 				startActivity(intent);
 			}
 		});
 	}
 
 	private class RestaurantAdapter extends ArrayAdapter<Pick> {
 
 		public RestaurantAdapter() {
 			super(PicksDisplay.this, R.layout.pickrow, picksList);
 
 		}
 
 		private LayoutInflater layoutInflater = getLayoutInflater();
 
 		public View getView(int position, View convertView, ViewGroup parent) {
 
 			// If the row was already created before, we'll receive it here
 			View row = convertView;
 
 			// a ViewHolder keeps references to children views to avoid
 			// unnecessary calls
 			// to findById() on each row
 			ViewHolder myViewHolder = null;
 
 			// If this row wasn't created before, we'll create it here
 			if (row == null) {
 
 				// inflater will be used to create Views from the things_row
 				// layout
 				row = layoutInflater.inflate(R.layout.pickrow, null);
 
 				myViewHolder = new ViewHolder(row);
 
 				row.setTag(myViewHolder);
 
 			} else {
 				myViewHolder = (ViewHolder) row.getTag();
 			}
 
 			myViewHolder.populateFrom(picksList.get(position));
 
 
 			return row;
 
 		}
 
 		class ViewHolder {
 			TextView textViewNumber = null;
 			TextView textViewName = null;
 			TextView textViewAddress = null;
 			TextView textViewPhoneNumber = null;
 			TextView textViewVoteCount = null;
 			RatingBar rating = null;
 			Button buttonAddVote = null;
 			Button buttonMap = null;
 			Pick currentPick;
 
 			public ViewHolder(View row) {
 				textViewNumber = (TextView) row
 						.findViewById(R.id.textViewPickSerialNumber);
 				textViewAddress = (TextView) row
 						.findViewById(R.id.textViewPickAddress);
 				textViewName = (TextView) row
 						.findViewById(R.id.textViewPickName);
 				textViewName.setEllipsize(TruncateAt.END);
 				textViewPhoneNumber = (TextView) row
 						.findViewById(R.id.textViewPickPhoneNumber);
 				textViewVoteCount = (TextView) row
 						.findViewById(R.id.textViewVoteCount);
 
 				rating = (RatingBar) row.findViewById(R.id.ratingBarPicks);
 				
 				buttonAddVote = (Button) row.findViewById(R.id.AddVoteButton);
 				buttonMap = (Button) row.findViewById(R.id.MapButton);
 				
 
 			}
 
 			public void populateFrom(Pick pick) {
 
 				currentPick = pick;
 				
 				if(itemClicked.equalsIgnoreCase("Airport Eats")){
 					textViewAddress.setText(city.getAirport());
 				} else {
 					textViewAddress.setText(pick.getAddress());
 				}
 				
 				
 				textViewName.setText(pick.getName());
 				textViewPhoneNumber.setText(pick.getPhone());
 				textViewNumber.setText(Integer.toString(pick
 						.getSerialNumber()) + ".");
 				textViewVoteCount.setText(pick.getRatingCount());
				rating.setRating(Integer.parseInt(pick.getRatingAverage()));
 				
 //				// To underline the phone number text view
 //				SpannableString content = new SpannableString(pick.getPhone());
 //				content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
 //				textViewPhoneNumber.setText(content);
 
 				// Add action listener to dial phone number when clicked
 				textViewPhoneNumber.setOnClickListener(new OnClickListener() {
 					
 					public void onClick(View v) {
 
 						try {
 		                    Intent callIntent = new Intent(Intent.ACTION_CALL);
 		                    callIntent.setData(Uri.parse("tel:"+currentPick.getPhone()));
 		                    startActivity(callIntent);
 		                } catch (Exception exception) {
 		                    Log.e("Calling a Phone Number", "Call failed", exception);
 		                }
 
 					}
 				});
 				
 				
 				// Set focusable to false else list view click listener will not work
 				buttonAddVote.setFocusable(false);
 				buttonMap.setFocusable(false);
 				
 				
 				
 				buttonAddVote.setOnClickListener(new OnClickListener() {
 					
 					public void onClick(View v) {
 						// Intent to start PickVote activity
 						Intent intent = new Intent(activity, PickVote.class);
 						
 						// Pass Pick to the PickVote activity
 						intent.putExtra("Pick",  currentPick);
 
 						intent.putExtra("Title", getIntent().getCharSequenceExtra("Title"));
 
 
 						startActivity(intent);
 					}
 				});
 				
 				buttonMap.setOnClickListener(new OnClickListener() {
 					public void onClick(View view) {
 						String latitude = currentPick.getLatitude();
 						String longitude = currentPick.getLongitude();
 
 						String url = "geo:";
 						String address = currentPick.getAddress();
 						if (latitude == null || longitude == null || latitude.trim().equals("") || longitude.trim().equals("")) {
 							if (address == null || address.trim().equals("")) {
 								Toast.makeText(activity, "No valid address, map forwarding to center of city.",
 										Toast.LENGTH_SHORT).show();
 								url = String.format(url + "%s,%s", city.getLat(), city.getLongitude());
 							} else {
 								url = String.format(url + "%s,%s?q=%s", city.getLat(), city.getLongitude(), Uri.encode(address));
 							}
 						} else {
 							if (address == null || address.trim().equals("")) {
 								Toast.makeText(activity, "No valid address, map centering on place location.",
 										Toast.LENGTH_SHORT).show();
 								url = String.format(url + "%s,%s", currentPick.getLatitude(), currentPick.getLongitude());
 							} else {
 								url = String.format(url + "%s,%s?q=%s", currentPick.getLatitude(), currentPick.getLongitude(), Uri.encode(address));
 							}
 						}
 						Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
 								Uri.parse(url));
 						startActivity(intent);
 
 					}
 
 				});
 
 			
 				
 			}
 
 		}
 
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.options_menu_other, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		switch (item.getItemId()) {
 		case R.id.itemAbout:
 
 			break;
 		case R.id.itemLogout:
 
 			
 			SharedPreferences savedUser = getPreferences(MODE_PRIVATE);
 			Editor editor = savedUser.edit();
 			
 			User user = application.getUser();
 			user.save(editor, false);
 			application.setUser(null); 
 			application.setCity(null);
 			
 //			editor.clear();
 //			editor.commit();
 			
 			startActivity(new Intent(activity, Login.class));
 
 			break;
 		case R.id.itemChangeCity:
 
 			Intent intent = new Intent(activity, Login.class);
 
 			// Pass Pick to the Login activity to display the cities pop up
 			intent.putExtra("ShowCity", true);
 			startActivity(intent);
 			
 			break;
 			
 		case R.id.itemMainMenu:
 			
 			startActivity(new Intent(activity, CityMenu.class));
 			break;
 		}
 		return false;
 	}
 
 }
