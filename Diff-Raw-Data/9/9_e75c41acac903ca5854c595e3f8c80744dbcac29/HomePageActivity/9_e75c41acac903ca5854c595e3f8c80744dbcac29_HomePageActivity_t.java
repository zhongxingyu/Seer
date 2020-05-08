 package cs169.project.thepantry;
 
 import java.util.ArrayList;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.EditText;
 import android.widget.FrameLayout;
 import android.widget.ListView;
 import cs169.project.thepantry.ThePantryContract.Inventory;
 
 public class HomePageActivity extends BasicMenuActivity {
 	
 	ArrayList<SearchMatch> recommendations;
 	public SearchResultAdapter srAdapter;
 	SearchModel sm = new SearchModel();
 	ListView listView;
 	DatabaseModel dm;
 	private static final String DATABASE_NAME = "thepantry";
 	final int NUM_RECOMMENDATIONS = 4;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_home_page);
 		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
 		
 		//setup recommendations listview and adapter
 		recommendations = new ArrayList<SearchMatch>();
 		listView = (ListView) findViewById(R.id.recsList);
 		srAdapter = new SearchResultAdapter(getApplicationContext(), recommendations);          
 		listView.setAdapter(srAdapter);
 		srAdapter.notifyDataSetChanged();
 		
 		//when a search result item (recipe) is clicked
 		listView.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 			    // When clicked
 				if (isOnline()){
 		    		SearchCriteria searchcriteria = new SearchCriteria("recipe", (String)view.getTag());
 		    		new HomeSearchTask(getApplicationContext(), "recipe").execute(searchcriteria);
 				}
 			}
 		});
 		
 		if (isOnline()) {
 			getRecommendations();
 		} else {
 			//TODO: display an "offline" message
 		}
 		
 	}
 	
 	public void search(View view) throws Exception {
 		EditText searchText = (EditText) findViewById(R.id.search_text);
     	String search = searchText.getText().toString();
     	if (isOnline()) {
     		SearchCriteria searchcriteria = new SearchCriteria("search", search);
     		new HomeSearchTask(getApplicationContext(), "search").execute(searchcriteria);
     	}
 	}
 	
 	public void getRecommendations() {		
 		dm = new DatabaseModel(this, DATABASE_NAME);
 		Cursor youHave = dm.findAllItems(Inventory.TABLE_NAME);
 
 		String query = "";
 		SearchCriteria searchcriteria;
 		int numToPick;
 		if (youHave != null && youHave.moveToFirst()) {
 			int numItems = youHave.getCount();
 			// pick a random number between 1-5 or 1-#items to try a combination of items in your inventory to recommend recipes based on
 			if (numItems < 5) {
 				numToPick = (int)(Math.random() * numItems) + 1;
 			} else {
 				numToPick = (int)(Math.random() * 5) + 1;
 			}
 			//pick numToPick inventory items at random and recommend recipes based on them
 			// TODO redo search for < 4 results
 			for (int i = 0; i < numToPick; i++) {
 				int loc = (int)(Math.random() * (numItems));
 				while (loc > 0) {
 					youHave.moveToNext();
 					loc--;
 				}
				query += ", " + youHave.getString(Inventory.ITEMIND);
 				youHave.moveToFirst();
 			}
 			searchcriteria = new SearchCriteria("home", query, NUM_RECOMMENDATIONS);
 		}
 		else {
 			//default
 			searchcriteria = new SearchCriteria("home", "", NUM_RECOMMENDATIONS);
 		}
 		new HomeSearchTask(getApplicationContext(), "home").execute(searchcriteria);
 	}
 	
 	public class HomeSearchTask extends AsyncTask<SearchCriteria, String, Storage> {
 		
 		String type = "";
 		String q;
 		Context context;
 		FrameLayout mFrameOverlay;
 		ProgressDialog progressDialog;
 		
 		public HomeSearchTask(Context context) {
 	    	this.context = context;
 		}
 		
 		public HomeSearchTask(Context context, String type) {
 		    	this.context = context;
 		    	this.type = type;
 		}
 		
 		//show progress wheel
 		@Override
 	    protected void onPreExecute()
 	    {
 			// show the overlay with the progress bar
 			if (this.type == "home") {
 				mFrameOverlay = (FrameLayout)findViewById(R.id.overlay);
 				mFrameOverlay.setVisibility(View.VISIBLE);
 			} else {
 				progressDialog = new ProgressDialog(HomePageActivity.this);
 				progressDialog.setMessage("Loading " + this.type + "...");
 				progressDialog.show();
 			}
 	    };
 		
 		@Override
 		protected Storage doInBackground(SearchCriteria... sc) {
 			this.type = sc[0].type;
 			this.q = sc[0].q;
 			return sm.search(sc[0]);
 		}
 		
 		//update list of recommendations using a SearchResultsAdapter
 		//or open a SearchResultsActivity if a search was made
 		//or open a recipe page if a recipe was selected
 		@Override
 		protected void onPostExecute(Storage result) {
 			
 			//remove the overlay
 			if (this.type == "home") {
 				mFrameOverlay.setVisibility(View.GONE);
 				
 			} else {
 				progressDialog.dismiss();
 			}
 	        
 			if (result != null) {
 				if (this.type == "home") {
 					if (srAdapter.values.size() == 0) {
 						recommendations = ((SearchResult)result).matches;
 						srAdapter = new SearchResultAdapter(HomePageActivity.this, recommendations);   
 						listView.setAdapter(srAdapter);
 					} else {
 						srAdapter.values = ((SearchResult)result).matches; 
 						srAdapter.notifyDataSetChanged();
 					}
 				}
 				else if (this.type == "search") {
 					Intent intent = new Intent(context, SearchResultsActivity.class);
 					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 					intent.putExtra("result", result);
 					intent.putExtra("currentSearch", this.q);
 					startActivity(intent);
 				}
 				else if (this.type == "recipe") {
 					Intent intent = new Intent(context, RecipeActivity.class);
 					intent.putExtra("result", result);
 					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 					startActivity(intent);
 				}
 			}
 			
 		}
 	}
 }
