 package com.openfridge;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListAdapter;
 import android.widget.ListView;
import android.widget.ScrollView;
 import android.widget.Toast;
 
 public class ExpirationListActivity extends Activity {
 	private static final int ROW_HEIGHT = 100;
 	private Intent expire, itemEdit;
 	// Tag for debug log
 	private static final String DEBUG_TAG = "Openfridge";
	private ScrollView sv;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
         expire = new Intent(this, ExpireActivity.class);
         itemEdit = new Intent(this, ItemEditActivity.class);
 
 		Log.d(DEBUG_TAG, "checkpoint");
 
 		// This code gets the xml and sets up the SAX Parser
 		
 		try {
 			MainMenuActivity.client.reloadFoods();
 		} catch (Exception e) {
 			// For debugging
 			e.printStackTrace();
 		}
 		setContentView(R.layout.expiration_list);
 		
 		/*
 		// Setup the Listview
 		List<FridgeFood> good = Arrays.asList(new FridgeFood("2011-10-28",
 				"Milk", "2011-11-01", "2", "2011-10-28", "1"), new FridgeFood(
 				"2011-10-28", "Eggs", "2011-11-01", "2", "2011-10-28", "1"),
 				new FridgeFood("2011-10-28", "Leftovers", "2011-11-01", "2",
 						"2011-10-28", "1"), new FridgeFood("2011-10-28",
 						"Kale", "2011-11-01", "2", "2011-10-28", "1"),
 				new FridgeFood("2011-10-28", "Beef", "2011-11-01", "2",
 						"2011-10-28", "1")
 
 		);
 		List<FridgeFood> nearly = Arrays.asList(new FridgeFood("2011-10-28",
 				"Leftovers", "2011-11-01", "1", "2011-10-28", "1"));
 		List<FridgeFood> expired = Collections.<FridgeFood> emptyList();
 		*/
 		
 		//Log.d(DEBUG_TAG, "number of good items: " + good.size());
 		
 		initFridgeFoodListView(R.id.pastLV, MainMenuActivity.client.getExpiredFoods(),
 				new PastFridgeItemClickListener());
 		initFridgeFoodListView(R.id.nearLV, MainMenuActivity.client.getNearFoods(),
 				new PastFridgeItemClickListener());
 		initFridgeFoodListView(R.id.goodLV, MainMenuActivity.client.getGoodFoods(),
 				new PastFridgeItemClickListener());
		
		// Correct Scroll Location
		sv = (ScrollView) findViewById(R.id.scrollV);
		sv.post(new Runnable() {
		    @Override
		    public void run() {
		        sv.scrollTo(0, 0);
		    } 
		});

 	}
 
 	private void initFridgeFoodListView(int viewId, List<FridgeFood> foods,
 			OnItemClickListener listener) {
 		ListView listView = (ListView) findViewById(viewId);
 		listView.setTextFilterEnabled(true);
 		listView.setAdapter(new ArrayAdapter<FridgeFood>(this,
 				android.R.layout.simple_list_item_1, foods));
 		listView.setOnItemClickListener(listener);
 
 		// Make items not focusable to avoid listitem / button conflicts
 		listView.setItemsCanFocus(false);
 
 		ListAdapter listAdapter = listView.getAdapter();
 
 		int rows = listAdapter.getCount();
 		// int height = android.R.attr.listPreferredItemHeight * rows;
 		// for some reason listPreferredItemHeight didn't have a reasonable
 		// value...
 		int height = ROW_HEIGHT * rows;
 
 		ViewGroup.LayoutParams params = listView.getLayoutParams();
 		params.height = height;
 		listView.setLayoutParams(params);
 		listView.requestLayout();
 	}
 	
 	public void loadItemEdit(View view) {
         startActivity(itemEdit);
     }
 
 	private class PastFridgeItemClickListener implements OnItemClickListener {
 	    public void onItemClick(AdapterView<?> parent, View view, int position, 
 	                            long id) {
 	        Log.d("Fridge debug", "list item clicked");
 	        if (parent.getClass() == ListView.class) {
 	            ListView parentList = (ListView) parent;
 	            
 	            FridgeFood food = (FridgeFood) parentList.getItemAtPosition(position);
 	            
 	            Toast.makeText(parentList.getContext(), 
 	                    "Expiration Date: " + food.getExpirationDateString(), 
 	                    Toast.LENGTH_SHORT).show();
 
 	            startActivity(expire);
 	            
 	            FridgeFoodDataClient client = new FridgeFoodDataClient();
 	            try {
 	                client.postFood(food);
 	            } catch (Exception e) {
 	                Toast.makeText(parentList.getContext(), 
 	                        "Connection error occurred", Toast.LENGTH_SHORT);
 	            }
 	        }
 	    }
 		
 	}
 }
