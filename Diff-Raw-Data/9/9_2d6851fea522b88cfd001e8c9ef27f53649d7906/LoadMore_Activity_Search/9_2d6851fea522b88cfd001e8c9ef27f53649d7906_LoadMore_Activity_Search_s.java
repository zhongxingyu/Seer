 package com.wwyz.loltv.loadMore;
 
 import android.content.Intent;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.widget.SearchView;
 import com.wwyz.loltv.R;
 import com.wwyz.loltv.R.drawable;
 
 public class LoadMore_Activity_Search extends LoadMore_Activity_Base
 		implements SearchView.OnQueryTextListener {
 
 	protected String queryHint = "Search";
 	protected String mQuery;
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 
 		SearchView searchView = new SearchView(this.getSupportActionBar()
 				.getThemedContext());
 		searchView.setQueryHint(queryHint);
 		searchView.setOnQueryTextListener(this);
 
 		menu.add(0, 20, 0, "Search")
				.setIcon(R.drawable.ic_search)
 				.setActionView(searchView)
 				.setShowAsAction(
 						MenuItem.SHOW_AS_ACTION_IF_ROOM
 								| MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
 
 		menu.add(0, 0, 0, "Refresh")
 				.setIcon(R.drawable.ic_refresh)
 				.setShowAsAction(
 						MenuItem.SHOW_AS_ACTION_IF_ROOM
 								| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
 		return true;
 
 	}
 
 	@Override
 	public boolean onQueryTextSubmit(String query) {
 //		Toast.makeText(this, "You are in: " + queryHint, Toast.LENGTH_LONG)
 //				.show();
 		return true;
 	}
 
 	@Override
 	public boolean onQueryTextChange(String newText) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 	
 }
