 package cs169.project.thepantry;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.EditText;
 import android.widget.ListView;
 
 public class SearchResultsActivity extends BasicMenuActivity {
 
 	ArrayList<SearchMatch> matches;
 	SearchResultAdapter srAdapter;
 	SearchModel sm = new SearchModel();
 	ListView listView;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_search_results);
 		
 		EditText searchText = (EditText) findViewById(R.id.search_text);
 		searchText.setText((String)getIntent().getStringExtra("currentSearch"));
 		
 		SearchResult result = (SearchResult)getIntent().getExtras().getSerializable("result");
 		matches = result.matches;
 		
 		listView = (ListView) findViewById(R.id.resultList);
 		srAdapter = new SearchResultAdapter(this, matches);          
 		listView.setAdapter(srAdapter);
 		srAdapter.notifyDataSetChanged();
 		
 		//when a search result item is clicked
 		listView.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 			    // When clicked
 				if (isOnline()){
 		    		SearchCriteria searchcriteria = new SearchCriteria("recipe", (String)view.getTag());
 		    		new SearchTask(getApplicationContext()).execute(searchcriteria);
 				}
 			}
 		});
 	}
 	
 	public void search(View view) throws Exception {
 		EditText searchText = (EditText) findViewById(R.id.search_text);
     	String search = searchText.getText().toString();
     	if (isOnline()) {
     		SearchCriteria searchcriteria = new SearchCriteria("search", search);
     		new SearchTask(getApplicationContext()).execute(searchcriteria);
     	}
 	}
 	
 public class SearchTask extends AsyncTask<SearchCriteria, String, Storage> {
 		
 		String type;
 		String q;
 		Context context;
 		
 		public SearchTask(Context context) {
 		    	this.context = context;
 		}
 		
 		@Override
 		protected Storage doInBackground(SearchCriteria... sc) {
 			this.type = sc[0].type;
 			this.q = sc[0].q;
 			return sm.search(sc[0]);
 		}
 		
 		@Override
 		protected void onPostExecute(Storage result) {
		if (result != null) {
 			if (this.type == "search") {
 				if (srAdapter.values.size() == 0) {
 					matches = ((SearchResult)result).matches;
 					srAdapter = new SearchResultAdapter(SearchResultsActivity.this, matches);   
 					listView.setAdapter(srAdapter);
 					listView.bringToFront();
 				}
 				else {
 					srAdapter.values = ((SearchResult)result).matches;
 					srAdapter.notifyDataSetChanged();
 				}
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
