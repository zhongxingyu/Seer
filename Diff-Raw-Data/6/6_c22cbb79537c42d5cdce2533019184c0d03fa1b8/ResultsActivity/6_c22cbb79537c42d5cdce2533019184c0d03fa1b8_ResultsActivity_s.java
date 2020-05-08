 package revminer.ui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import revminer.common.Restaurant;
 import revminer.common.RestaurantAttribute;
 import revminer.common.RestaurantReviewCategory;
 import revminer.common.RestaurantReviews;
 import revminer.common.SearchHistory;
 import revminer.service.RevminerClient;
 import revminer.service.SearchListener;
 import revminer.service.SearchResultEvent;
 import revminer.service.SearchResultListener;
 import revminer.ui.R;
 import android.app.Activity;
 import android.app.ListActivity;
 import android.app.TabActivity;
 import android.content.Context;
 import android.os.Bundle;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 public class ResultsActivity extends ListActivity {
 	
 	private ResultsAdapter adapter;
 	
 	private class ResultsAdapter extends ArrayAdapter<Restaurant> implements SearchResultListener, OnItemClickListener {
 	
 	    private List<Restaurant> items;
 	
 	    public ResultsAdapter(Context context, int textViewResourceId, List<Restaurant> list) {
 	            super(context, textViewResourceId, list);
 	            this.items = list;
 	    }
 	
 	    @Override
 	    public View getView(int position, View convertView, ViewGroup parent) {
 	            View view = convertView;
 	            if (view == null) {
 	                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 	                view = vi.inflate(R.layout.result_item, null);
 	            }
 	            Restaurant r = items.get(position);
 	            if (r != null) {
 	                    TextView restaurantName = (TextView) view.findViewById(R.id.restaurantName);
 	                    
 //	                    TextView polarity1 = (TextView) view.findViewById(R.id.polarity1);
 //	                    TextView polarity2 = (TextView) view.findViewById(R.id.polarity2);
 //	                    TextView polarity3 = (TextView) view.findViewById(R.id.polarity3);
 //	                    TextView polarity4 = (TextView) view.findViewById(R.id.polarity4);
 //	                    TextView polarity5 = (TextView) view.findViewById(R.id.polarity5);
 	                    TextView distance = (TextView) view.findViewById(R.id.distance);
 	                    
 	                    Log.d("results.getView", r.getName());
 	                    
 	                    restaurantName.setText(r.getName());
 //	                    
 //	            		REVIEW_FOOD,
 //	            		REVIEW_SERVICE,
 //	            		REVIEW_DECORE,
 //	            		REVIEW_OVERALL,
 //	            		REVIEW_OTHER,
 	                    
 	                    //////RestaurantReviewCategory reviewCategory = r.getReviews().getReviewCategory(RestaurantReviewCategory.ReviewCategory.REVIEW_FOOD);
 	                    
 	                    
 	                    
 	                    // TODO: update width of each polarity box
 	                    // TODO: actually calculate the distance!
 	                    distance.setText("0.3");
                 }
 	            return view;
 	    }
 
 		public void onSearchResults(SearchResultEvent e) {
 	    	items.clear();
 	    	
 	    	if (e.hasError()) {
 	    		Log.d("results.onresults", "got error results");
 	    		return;
 	    	}
 	    	
 	    	StringBuilder sb = new StringBuilder();
 	    	sb.append("got ").append(e.getResturants().size()).append(" results");
 	    	Log.d("results.onresults", sb.toString());
 	    	
 	    	// make our own copy of the results
	    	items = new ArrayList<Restaurant>(e.getResturants());
	    	
	    	if (items.size() > 0)
	    		Log.d("results.sampleresult", items.get(0).getName());
 	    	
 	        notifyDataSetChanged();
 		}
 		
 		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 			if (position > items.size())
 				return;
 
 			RevminerClient.Client().sendSearchQuery(items.get(position).getName());
 		}
   }
 	
 	
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.results);
     
     Log.d("results.oncreate", "hello");
     
 	// initialize results
     List<Restaurant> results;
     SearchResultEvent e = RevminerClient.Client().getLastSearchResultEvent();
     if (e == null || e.hasError()) {
     	results = new ArrayList<Restaurant>();
     } else {
     	results = new ArrayList<Restaurant>(e.getResturants());
     }
 //    results.add(new Restaurant("one"));
 //    results.add(new Restaurant("two"));
 //    results.add(new Restaurant("three"));
 //    results.add(new Restaurant("four"));
 //    results.add(new Restaurant("five"));
 //    results.add(new Restaurant("six"));
     
     
     
     adapter = new ResultsAdapter(this, R.layout.result_item, results);
     setListAdapter(adapter);
     RevminerClient.Client().addSearchResultListener(adapter);
     getListView().setOnItemClickListener(adapter);
     
   }
   
 //  public void showTab() {
 //	  ((TabActivity)this.getParent()).getTabHost().setCurrentTabByTag("Results");
 //  }
 }
