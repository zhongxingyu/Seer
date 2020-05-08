 /* CMPUT301F13T06-Adventure Club: A choose-your-own-adventure story platform
  * Copyright (C) 2013 Alexander Cheung, Jessica Surya, Vina Nguyen, Anthony Ou,
  * Nancy Pham-Nguyen
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package story.book.view;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import story.book.view.R;
 import story.book.controller.StoryCreationController;
 import story.book.model.Story;
 import story.book.model.StoryFragment;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.DialogFragment;
 import android.app.SearchManager;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.SearchView.OnQueryTextListener;
 import android.widget.SearchView;
 
 /**
  * StoryFragmentListActivity displays all story fragments contained
  * in the story that is currently open. Selecting a story fragment
  * will allow user to:
  * 		1. Edit the story fragment
  * 		2. Set the story fragment as the starting fragment
  * 		3. Delete the story fragment from the story
  * 
  * @author Jessica Surya
  * @author Vina Nguyen
  */
 public class StoryFragmentListActivity extends Activity implements StoryView, RequestingActivity {
 	ActionBar actionBar;
 	ArrayList<StoryFragment> SFL;
 	ArrayAdapter<StoryFragment> adapter;
 	StoryCreationController SCC;
 
 	int pos;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		setContentView(R.layout.story_fragment_read_activity);
 		SCC = new StoryCreationController();
 		SFL = new ArrayList<StoryFragment>();
 		
 		SCC.getStory().addView(this);
 		
 		return;
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();			
 		Log.d(String.valueOf(
 				SFL.isEmpty()), "DEBUG: SFL is empty");
 		getFragmentTitles();
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		Story s = SCC.getStory();
 		SCC.saveStory();
 		s.deleteView(this);
 		
 		for (StoryFragment f: SFL) {
 			f.deleteView(this);
 		}
 	}
 	
 	/**
 	 * getFragmentTitles() loads fragment titles from the story using the
 	 * <code>StoryCreationController</code> and displays them in the list
 	 * by calling <code>updateFragmentList()</code> method
 	 */
 	private void getFragmentTitles() {
 		SFL = new ArrayList<StoryFragment>();
 		HashMap<Integer, StoryFragment> map = SCC.getFragments();
 		for (Integer key : map.keySet()){
 			StoryFragment f = map.get(key);
 			SFL.add(f);
 			f.addView(this);
 		}
 		updateFragmentList();
 	}
 	
 	/**
 	 * updateFragmentList() displays a story fragment titles in the list
 	 */
 	private void updateFragmentList() {
 		
 		String title = SCC.getStory().getStoryInfo().getTitle();
 		actionBar = getActionBar();
 		actionBar.setTitle(title);
 
 		adapter = new ArrayAdapter<StoryFragment>(this, android.R.layout.simple_list_item_1,
 				SFL);
 
 		ListView listview = new ListView(this);
 
 		listview.setBackgroundColor(Color.WHITE);
 		listview.setAdapter(adapter);
 		setContentView(listview);
 
 		registerForContextMenu(listview);
 		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView parent, View v, int position, long id) {
 				pos = position;
 				Intent i = new Intent(parent.getContext(), StoryFragmentEditActivity.class);
 				i.putExtra("FID", SFL.get(pos).getFragmentID());
 				startActivity(i);
 			}
 
 		});
 	}
 	
 	/**
 	 * editFragment() passes the FragmentID from the selected story fragment in the list
 	 * and starts a new <code>StoryFragmentEditActivity</code> for edit the contents of 
 	 * the fragment
 	 * 
 	 * @param FragmentID (FID) which will be passed to <code>StoryFragmentEditActivity</code>
 	 */
 	private void editFragment(int FID) {
 		Intent i = new Intent(this, StoryFragmentEditActivity.class);
 		i.putExtra("FID", FID);
 		startActivity(i);
 	}
 	
 	/**
 	 * changeFragmentTitle() adds a new fragment to the current story.
 	 */
 	private void changeFragmentTitle() {
 		DialogFragment newFragment = new RequestTextDialog();
 		((RequestTextDialog)newFragment).setParent(this);
 		((RequestTextDialog)newFragment).setHeader(this.getString(R.string.add_fragment_title));
 		((RequestTextDialog)newFragment).setWarning(this.getString(R.string.bad_frag_title_msg));
 		newFragment.show(getFragmentManager(), "addFragment");
 	}
 
 	public void onUserSelectValue(String title) {
 		if (title != null) {
 			if (pos != - 1) {
 				//Update fragment title
 				SCC.changeFragmentTitle(SFL.get(pos), title);
 			} else
 			{
 				//Create fragment with this title
 				StoryFragment fragment = SCC.newFragment(title);
 
 				//Open fragment for editing
 				editFragment(fragment.getFragmentID());
 			}
 		}
 	}
 	/**
 	 * doMySearch() takes a query from the search bar, passes it to
 	 * <code>StoryCreationController</code>, and display the set of results 
 	 * returned by the controller in the listView
 	 * 
 	 * @param query returned by SearchView (search bar)
 	 */
 	private void doMySearch(String query) {
 		SFL = new ArrayList<StoryFragment>();
 		//show the list with just the search results
 		HashMap<Integer, StoryFragment> map = SCC.searchFragments(query);
 		for (Integer key : map.keySet()){
 			SFL.add(map.get(key));
 		}
 		updateFragmentList();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu items for use in the action bar
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.fragment_list_menu, menu);
 		inflater.inflate(R.menu.standard_menu, menu);
 
 		// Get the SearchView and set the searchable configuration
 		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
 		searchView.setSubmitButtonEnabled(true);
 		// Assumes current activity is the searchable activity
 		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		searchView.setIconifiedByDefault(true); // Do not iconify the widget; expand it by default
 		
 		searchView.setOnQueryTextListener(new OnQueryTextListener() {
 			@Override
 			public boolean onQueryTextSubmit(String query) {
 				Log.d(String.valueOf(query), "DEBUG: Query entered");
 				doMySearch(query);
 				return true;
 			}
 
 			@Override
 			public boolean onQueryTextChange(String newText) {
 				if (newText.isEmpty()) {
 					Log.d("Close", "DEBUG: Search closed");
 					SFL = new ArrayList<StoryFragment>();
 					getFragmentTitles();
 					return true;
 				}
 				else {
 					return false;
 				}
 			}
 		});
 		
 		return super.onCreateOptionsMenu(menu);
 	}
 	
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		Intent intent;
 		switch (item.getItemId()) {
 		case R.id.title_activity_dashboard:
 			intent = new Intent(this, Dashboard.class);
 			startActivity(intent);
 			finish();
 			return true;
 		case R.id.add_fragment:
 			pos = -1;
 			changeFragmentTitle();
 			return true;
 		case R.id.publish:
 			if (StoryApplication.checkInternetConnected()) {
 				SCC.publishStory();
 			} else {
 				SimpleWarningDialog.getWarningDialog(this.getString(R.string.no_internet), this);
 			}
 			return true;
 		case R.id.change_info:
 			intent = new Intent(this, EditStoryInformationActivity.class);
 			startActivity(intent);
 			return true;
 		default:
 			
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		menu.setHeaderTitle("Select an Option:");
 		menu.add(0, v.getId(), 1, "Edit");  
 		menu.add(0, v.getId(), 2, "Change Fragment Title");  
 		menu.add(0, v.getId(), 3, "Set as Starting Story Fragment"); 
 		menu.add(0, v.getId(), 4, "Delete");
 		menu.add(0, v.getId(), 5, "Cancel");
 	}
 
 	@Override  
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 		pos = info.position;
 
 		switch (item.getOrder()) {
 		case 1:
 			// Edit story fragment
 			editFragment(SFL.get(pos).getFragmentID());
 			break;
 
 		case 2:
 			// Edit story fragment title
 			changeFragmentTitle();
 			break;
 
 		case 3:
 			// Set as starting story fragment
 			SCC.setStartingFragment(SFL.get(pos).getFragmentID());
 			break;
 		case 4:
 			//Delete
 			int FID = SFL.get(pos).getFragmentID();
 			if (FID == SCC.getStartingFragment()) {
 				SimpleWarningDialog.getWarningDialog(this.getString(R.string.bad_frag_delete_msg), this);
 			} else {
 				SCC.deleteFragment(FID);
 			}
 			getFragmentTitles();
 			break;
 		case 5:
 			// Cancel options
 			return false;
 		}
 
 		return true; 
 
 	}
 
 	@Override
 	public void update(Object model) {
 		// TODO Auto-generated method stub
 		getFragmentTitles(); 
 	}
 }
