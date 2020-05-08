 package com.cse5236groupthirteen;
 
 import java.util.Date;
 import java.util.List;
 
 import com.cse5236groupthirteen.utilities.MenuItem;
 import com.cse5236groupthirteen.utilities.ParseHelper;
 import com.cse5236groupthirteen.utilities.Restaurant;
 import com.cse5236groupthirteen.utilities.Submission;
 import com.cse5236groupthirteen.utilities.YumHelper;
 import com.cse5236groupthirteen.utilities.YumViewActivity;
 import com.parse.FindCallback;
 import com.parse.ParseException;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 
 import android.os.Bundle;
 import android.content.Intent;
 
 import android.text.Html;
 
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class RestaurantViewActivity extends YumViewActivity implements OnClickListener, OnItemClickListener {
 
 	private TextView txtvw_restaurantName;
 	private TextView txtvw_restaurantAddress;
 	private TextView txtvw_restaurantPhone;
 	private TextView txtvw_restaurantWebsite;
 	private TextView txtvw_restaurantRating;
 	private Button btn_callMenuViewActivity;
 	private ListView lstvw_recentSubmissions;
 	private Button btn_callSubmissionViewActivity;
 	
 	private Restaurant selectedRestaurant;
 	
 	private ArrayAdapter<Submission> listAdapter;
 	
 	
 		
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_restaurant_view);
 		
 		// set up buttons
 		btn_callMenuViewActivity = (Button)findViewById(R.id.btn_restview_callmenuviewactivity);
 		btn_callMenuViewActivity.setOnClickListener(this);
 		
 		btn_callSubmissionViewActivity = (Button)findViewById(R.id.btn_restview_callsubmissionviewactivity);
 		btn_callSubmissionViewActivity.setOnClickListener(this);
 				
 		// load selected restaurant information
 		Bundle b = getIntent().getExtras();
 		if (b != null) {
 			String selectedRestaurantId = b.getString(Restaurant.R_UUID);
 			loadRestaurant(selectedRestaurantId);
 		} else {
 			String errmsg = "There was an error passing Restaurant information from HomeView";
 			YumHelper.handleError(this, errmsg);
 		}
 		
 		// set up reviews box
 		listAdapter = new ArrayAdapter<Submission>(this, android.R.layout.simple_list_item_1);
 		lstvw_recentSubmissions = (ListView)findViewById(R.id.lstvw_restview_submissionsummary);
 		lstvw_recentSubmissions.setAdapter(listAdapter);
 		lstvw_recentSubmissions.setOnItemClickListener(this);
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		if (selectedRestaurant != null) {
 			loadSubmissions(selectedRestaurant.getRestaurantId());
 			populateUI();
 		}
 	}
 	
 	@Override
 	public void onShake() {
 		loadSubmissions(selectedRestaurant.getRestaurantId());
 	}
 	
 	private void populateUI() {
 		
 		this.setTitle(selectedRestaurant.getName() + " Information");
 		
 		txtvw_restaurantName = (TextView) findViewById(R.id.txtvw_restview_restaurantname);
 		txtvw_restaurantName.setText(selectedRestaurant.getName());
 		
 		txtvw_restaurantAddress = (TextView) findViewById(R.id.txtvw_restview_restaurantaddress);
 		txtvw_restaurantAddress.setText(selectedRestaurant.getFullAddress());
 		
 		txtvw_restaurantPhone = (TextView) findViewById(R.id.txtvw_restview_restaurantphonenumber);
 		txtvw_restaurantPhone.setText(selectedRestaurant.getPhoneNumber());
 		
 		txtvw_restaurantWebsite = (TextView) findViewById(R.id.txtvw_restview_restaurantwebsite);
 		txtvw_restaurantWebsite.setText(Html.fromHtml(selectedRestaurant.getWebsite()));
 		
 		
 		
 		if (doesRestaurantHaveMenu(selectedRestaurant.getRestaurantId())) {
 			btn_callMenuViewActivity.setEnabled(true);
 		} else {
 			btn_callMenuViewActivity.setEnabled(false);
 		}
 	}
 	
 	private boolean doesRestaurantHaveMenu(String restaurantId) {
 		
 		ParseQuery query = new ParseQuery(ParseHelper.CLASS_MENUITEMS);
 		query.whereEqualTo(MenuItem.MI_RESTID, restaurantId);
 		try {
 			int count = query.count();
 			if (count > 0) {
 				return true;
 			} else {
 				return false;
 			}
 		} catch (ParseException e) {
 			// error occurred
 			String errmsg = "There was an error loading data from Parse";
 			YumHelper.handleException(this, e, errmsg);
 			return false;
 		}
 		
 	}
 
 	private void loadRestaurant(String restaurantId) {
 		
 		ParseQuery query = new ParseQuery(ParseHelper.CLASS_RESTAURANTS);
 		query.whereEqualTo(Restaurant.R_UUID, restaurantId);
 		showProgress();
 		query.findInBackground(new FindCallback() {
 
 			@Override
 			public void done(List<ParseObject> objects, ParseException e) {
 				dismissProgress();
 				if (e == null) {
 					if (objects.size() == 1) {
 						// id is unique and there should only be one result
 						ParseObject po = objects.get(0);
 						selectedRestaurant = new Restaurant(po);
 						loadSubmissions(selectedRestaurant.getRestaurantId());
 						populateUI();
 					} else {
 						String errmsg = "Parse returned multiple objects";
 						YumHelper.handleException(getParent(), e, errmsg);
 					}
 				} else {
 					String errmsg = "There was an error loading data from Parse";
 					YumHelper.handleException(getParent(), e, errmsg);
 				}
 				
 			}
 			
 		});
 		
 	}
 
 	private void loadSubmissions(String restaurantId) {
 		
 		ParseQuery query = new ParseQuery(ParseHelper.CLASS_SUBMISSIONS);
 		query.whereEqualTo(Submission.S_RESTID, restaurantId);
 		query.orderByDescending("createdAt");
 		query.setLimit(5);
 		
 		showProgress();
 		
 		query.findInBackground(new FindCallback() {
 			
 			public void done(List<ParseObject> objects, ParseException e) {
 				dismissProgress();
 				if (e == null) {
 					
 					listAdapter.clear();
 					for(ParseObject po: objects) {
 						Submission s = new Submission(po);
 						listAdapter.add(s);
 					}
 					listAdapter.notifyDataSetChanged();
 					
 				} else {
 					String errmsg = "There was an error loading data from Parse";
 					YumHelper.handleException(getParent(), e, errmsg);
 				}
 			}
 			
 		});
 		
		txtvw_restaurantRating = (TextView) findViewById(R.id.txtvw_restview_restaurantrating);
		switch (calculateRestaurantRating()) {
		case 1:
			txtvw_restaurantRating.setText("Recently it has been :)");
			break;
		case 0:
			txtvw_restaurantRating.setText("Recently it has been :|");
			break;
		case -1:
			txtvw_restaurantRating.setText("Recently it has been :(");
			break;
		case -2:
			txtvw_restaurantRating.setText("Recently it has been (?)");
			break;
		}
		
 	}
 	
 	private int calculateRestaurantRating() {
 		
 		// for now we will find the mean average of the ratings that are shown in the summary box
 		
 		double average = 0.0;
 		int count = listAdapter.getCount();
 		if (count == 0) {
 			return -2;
 		}
 		
 		for (int i = 0; i < count; i++) {
 			average += listAdapter.getItem(i).getRating();
 		}
 		average /= count;
 		
 		// return based on arbitrary bounds
 		// -1 to -1/3 is sad
 		// -1/3 to 1/3 is okay
 		// 1/3 to 1 is happy
 		
 		if (average >= 0.333) {
 			return 1;
 		} else if (average <= -0.333) {
 			return -1;
 		} else {
 			return 0;
 		}
 		
 	}
 
 	@Override
 	public void onClick(View v) {
 		
 		switch (v.getId()) {
 	
 		case R.id.btn_restview_callmenuviewactivity:
 			Intent intentMenu = new Intent(RestaurantViewActivity.this, MenuViewActivity.class);
 			intentMenu.putExtra(Restaurant.R_UUID, selectedRestaurant.getRestaurantId());
 			intentMenu.putExtra(Restaurant.R_NAME, selectedRestaurant.getName());
 			startActivity(intentMenu);
 			break;
 		case R.id.lstvw_restview_submissionsummary:
 			Intent intentHistory = new Intent(RestaurantViewActivity.this, HistoryViewActivity.class);
 			intentHistory.putExtra(Restaurant.R_UUID, selectedRestaurant.getRestaurantId());
 			intentHistory.putExtra(Restaurant.R_NAME, selectedRestaurant.getName());
 			startActivity(intentHistory);
 			break;
 		case R.id.btn_restview_callsubmissionviewactivity:
 			Intent intentSubmission = new Intent(RestaurantViewActivity.this, SubmissionViewActivity.class);
 			intentSubmission.putExtra(Restaurant.R_UUID, selectedRestaurant.getRestaurantId());
 			intentSubmission.putExtra(Restaurant.R_NAME, selectedRestaurant.getName());
 			intentSubmission.putExtra("StartTime", new Date());
 			startActivity(intentSubmission);
 			break;
 		}
 		
 	}
 
 
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 		onClick(findViewById(R.id.lstvw_restview_submissionsummary));	
 	}
 	
 	
 
 }
