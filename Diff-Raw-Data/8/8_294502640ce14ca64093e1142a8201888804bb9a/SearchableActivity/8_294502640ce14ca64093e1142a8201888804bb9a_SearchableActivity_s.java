 package com.atlast.MegaLike;
 
 import java.util.Arrays;
 import java.util.Collections;
 
 import com.actionbarsherlock.app.SherlockListActivity;
 import com.atlast.MegaLike.Lib.Extra;
 import com.atlast.MegaLike.Lib.FacebookData;
 
 import android.app.SearchManager;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class SearchableActivity extends SherlockListActivity {
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		String item = (String) getListAdapter().getItem(position);
 		saveCurrentUserId(item);
 		finish();
 	}
 
 	private void saveCurrentUserId(int id) {
 		Extra.CURRENT_USER_ID = id;
 	}
 	
 	private void saveCurrentUserId(String data) {
		if (data.equals("Rasto")) {
 			saveCurrentUserId(2);
		} else if (data.equals("Mato")) {
 			saveCurrentUserId(3);
		} else if (data.equals("Shaan")) {
 			saveCurrentUserId(4);
 		} else {
 			saveCurrentUserId(1);
 		}
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		handleIntent(getIntent());
 	}
 
 	@Override
 	protected void onNewIntent(Intent intent) {
 		setIntent(intent);
 		handleIntent(intent);
 	}
 
 	private void handleIntent(Intent intent) {
 		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
 			String query = intent.getStringExtra(SearchManager.QUERY);
 			doMySearch(query);
 		} else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
 			// Handle a suggestions click (because the suggestions all use ACTION_VIEW
 			String data = intent.getDataString();
 			saveCurrentUserId(data);
 			finish();
 		}
 	}
 
 	private void doMySearch(String query) {
 		String[] values = new String[] { "Tuan", "Rasto", "Mato", "Shaan", "WebOS", "Ubuntu", "Windows7", "Max OS X", "Linux", "OS/2" };
 		Collections.shuffle(Arrays.asList(values));
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, FacebookData.getInstance().getMatches(query));
 		setListAdapter(adapter);
 	}
 }
