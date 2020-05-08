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
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.RatingBar;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class PickComments extends Activity {
 
 	private List<Rating> ratingsList = null;
 	private Pick pickSelected = null;
 
 	private TheLuvExchange application = null;
 	private City city;
 
 	TextView textViewRating;
 	TextView textViewLatest;
 	
 	ListView listViewRatings;
 
 	private Activity activity = this;
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.pickcomments);
 
 		application = (TheLuvExchange)this.getApplication();
 		city = application.getCity();
 
 		// Set Header to selected city name
 		TextView cityName = (TextView)findViewById(R.id.header);
 		cityName.setText(city.getName());
 
 		// get the selected Pick passed through the intent 
 		pickSelected = (Pick) getIntent().getSerializableExtra("Pick");
 		
 		textViewRating = (TextView) findViewById(R.id.textViewRating);
 		textViewLatest = (TextView) findViewById(R.id.textViewLatest);
 
 
 		// List containing all the ratings
 		ratingsList  = new ArrayList<Rating>();
 		
 		// By default, the list is sorted by Rating
 		textViewRating.setTypeface(null, Typeface.BOLD);
 		textViewLatest.setTypeface(null, Typeface.NORMAL);
 		ratingsList.addAll(WebService.getRatings(pickSelected, "rating_avg", "desc"));
 				
 
 		listViewRatings = (ListView)findViewById(R.id.pickCommentsList);
 
 		// Set the title to the restaurant name
 		TextView textViewPickCommentTitle = (TextView) findViewById(R.id.textViewPickCommentTitle);
 		textViewPickCommentTitle.setText(pickSelected.getName());
 		
 
 		textViewRating.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				ratingsList.clear();
 
 				// the list is sorted by Rating
 				textViewRating.setTypeface(null, Typeface.BOLD);
 				textViewLatest.setTypeface(null, Typeface.NORMAL);
 				ratingsList.addAll(WebService.getRatings(pickSelected, "viewer_rating", "desc"));
 						
 
 				// Refresh list view
 				listViewRatings.invalidateViews();
 			
 			}
 		});
 		
 		textViewLatest.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				ratingsList.clear();
 
 				// Sorted by Latest
 				textViewRating.setTypeface(null, Typeface.NORMAL);
 				textViewLatest.setTypeface(null, Typeface.BOLD);
 				ratingsList.addAll(WebService.getRatings(pickSelected, "created", "desc"));
 
 				
 				// Refresh list view
 				listViewRatings.invalidateViews();
 				
 			}
 		});
 
 		// Set the discount available text view
 		TextView textViewPickDiscount = (TextView) findViewById(R.id.textViewDiscountBool);
 		if(pickSelected.getDiscounts()==null){
 			textViewPickDiscount.setText("");	// if not specified
 		} else {
 			textViewPickDiscount.setText(pickSelected.getDiscounts().equals("1")?"Yes":"No");
 
 		} 
 
 		// Set the location text view
 		TextView textViewPickLocation = (TextView) findViewById(R.id.textViewPickLocation);
 		textViewPickLocation.setText(pickSelected.getLocation()==null?" ":pickSelected.getLocation());
 
 
 
 
 		listViewRatings.setAdapter(new CommentAdapter());
 		listViewRatings.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
 
 		// Listener to handle click of an Item in the List
 		listViewRatings.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 
 //				// Intent to start PickComments activity
 //				Intent intent = new Intent(activity, PicksDisplay.class);
 //
 //				intent.putExtra("MenuSelected", getIntent().getStringExtra("MenuSelected"));
 //
 //				startActivity(intent);
 				
 				onBackPressed();
 
 			}
 		});
 
 
 		Log.d("restaurant comment", "pick id is - " + getIntent().getExtras().getString("PickID"));
 	}
 
 	private class CommentAdapter extends ArrayAdapter<Rating> {
 
 		public CommentAdapter() {
 			super(activity, R.layout.pickcommentsrow, ratingsList);
 
 		}
 
 		private LayoutInflater layoutInflater = getLayoutInflater();
 
 		public View getView(int position, View convertView, ViewGroup parent) {
 
 			// If the row was already created before, we'll receive it here
 			View row = convertView;
 
 			// a ViewHolder keeps references to children views to avoid unnecessary calls 
 			// to findById() on each row
 			ViewHolder myViewHolder = null;
 
 			// If this row wasn't created before, we'll create it here
 			if(row == null){
 
 
 				// inflater will be used to create Views from the things_row layout
 				row = layoutInflater.inflate(R.layout.pickcommentsrow, null);
 
 				myViewHolder = new ViewHolder(row);
 
 				row.setTag(myViewHolder);
 
 			} else {
 				myViewHolder = (ViewHolder) row.getTag();
 			}
 
 
 			myViewHolder.populateFrom(ratingsList.get(position));
 
 
 			return row;
 
 
 		}
 
 		class ViewHolder {
 			TextView textViewUserName = null;
 			TextView textViewCreated = null;
 			TextView textViewBody = null;
 			RatingBar ratingBar = null;
 
 			public ViewHolder (View row){
 				textViewUserName = (TextView) row.findViewById(R.id.textViewUserName);
 				textViewCreated = (TextView) row.findViewById(R.id.textViewCommentDate);
 				textViewBody = (TextView) row.findViewById(R.id.textViewUserCommentBody);
 				ratingBar = (RatingBar) row.findViewById(R.id.ratingBarViewCommentPick);
 
 
 			}
 
 			public void populateFrom(Rating rating){
 				textViewUserName.setText(rating.getUserName());
 				textViewCreated.setText(rating.getCreated());
 				textViewBody.setText(rating.getBody());
				ratingBar.setRating(Integer.parseInt(rating.getViewerRating()));
 
				//				// if rating is not specified, assign 0
				//				ratingBar.setRating(rating.getRatingAverage()==null?0:Integer.parseInt(rating.getRatingAverage()));
 
 
 
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
 			/*
 			 * Note by: Pranav
 			 * Description: Added call to About class
 			 * --Check call
 			 */
 			Intent i = new Intent(this, AboutApp.class);
 			startActivity(i);
 
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
