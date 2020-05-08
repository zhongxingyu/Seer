 /*
  * Copyright (C) <2013>  <Minhal Syed>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 
 package c301.AdventureBook;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.SearchManager;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.SearchView;
 import android.widget.TextView;
 import android.widget.Toast;
 import c301.AdventureBook.Models.Story;
 
 import com.example.adventurebook.R;
 
 
 /**
  * This is the offline library activity. This activity's main purpose is to
  * provide a graphical user interface to show all the local stories that are 
  * present in the phone's memory to the user. 
  * 
  * @author Minhal Syed - Main Creator
  * @author Justin Hoy - Minor Editor
  *
  */
 
 public class OfflineLibraryActivity extends Activity {
 	
 	private static final int ACTIVITY_EDIT_STORY = 0;
 
 	ArrayList<Story> offlineStoryLibrary;
 	ArrayAdapter<Story> adapter;
 	FileLoader fLoader;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		offlineStoryLibrary = new ArrayList<Story>();
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.offline_library);
 
 		fLoader = new FileLoader(this);
 		offlineStoryLibrary = fLoader.loadAllStoryFiles();
 		
 		populateListView();
 	}
 	
 	protected void onResume(){
 		super.onResume();
 		offlineStoryLibrary = fLoader.loadAllStoryFiles();
 		
 		populateListView();
 	}
 
 	/**
 	 * Starts the create story activity to create a new story.
 	 */
 	public void launchNewStoryActivity(View v) {
 		Intent i = new Intent(this, CreateStoryActivity.class);
 		startActivity(i);
 	}
 
 	/**
 	 * Starts the online library activity to view the online library.
 	 */
 	public void launchOnlineLibraryActivity(View v) {
 		Intent i = new Intent(this, OnlineLibraryActivity.class);
 		// startActivity(i);
 	}
 
 	/**
 	 * Populate the library's list view with all the available stories.
 	 */
 	private void populateListView() {
 		// Tutorial from : https://www.youtube.com/watch?v=4HkfDObzjXk
 
 		final ListView offlineLV = (ListView) findViewById(R.id.offline_library_listView);
 		adapter = new CustomAdapter();
 		offlineLV.setAdapter(adapter);
 		registerForContextMenu(offlineLV);
 
 		// tutorial used =
 		// http://stackoverflow.com/questions/9097723/adding-a-onclicklistener-to-listview-android
 		// When Clicked on the list item, we can return a story.
 		offlineLV.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 
 				// This is the story object that is returned when a list item is
 				// clicked.
 				Story story = (Story) offlineLV.getItemAtPosition(position);
 				Toast.makeText(getBaseContext(), story.getTitle(),
 						Toast.LENGTH_SHORT).show();
 				viewStory(story);
 			}
 		});
 
 	}
 
 	/**
 	 * Create options menu in action bar
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.library_options_menu, menu);
 
 		// Associate searchable configuration with the SearchView
 		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
 		final SearchView searchView = (SearchView) menu.findItem(R.id.search)
 				.getActionView();
 
 		final SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
 			@Override
 			public boolean onQueryTextChange(String newText) {
 				// Do something
 
 				String keyword = searchView.getQuery().toString().toLowerCase();
 		        offlineStoryLibrary = fLoader.loadStoryFileWithKeyword(keyword);
 
 		        populateListView();
 				return true;
 			}
 
 			@Override
 			public boolean onQueryTextSubmit(String query) {
 				// Do something
 				return true;
 			}
 		};
 
 		searchView.setOnQueryTextListener(queryTextListener);
 
 		return true;
 	}
 
 	/**
 	 * Create long click menu
 	 */
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		// TODO Auto-generated method stub
 		super.onCreateContextMenu(menu, v, menuInfo);
 		menu.add("Publish Online");
 		menu.add("Edit Story");
 		menu.add("Delete Story");
 
 		View thisItem = v;
 		// TextView titleText = (TextView) v.findViewById(R.id.titleTV);
 		// this.title = (String) titleText.getText();
 
 		// I am trying to see if I can get the story object directly, rather
 		// than reloading it.
 		// tutorial from:
 		// http://stackoverflow.com/questions/2321332/detecting-which-selected-item-in-a-listview-spawned-the-contextmenu-android
 
 		// Get the info on which item was selected
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
 
 		// Retrieve the item that was clicked on
 		Object item = adapter.getItem(info.position);
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		// super.onContextItemSelected(item);
 
 		// Here's how you can get the correct item in onContextItemSelected()
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
 				.getMenuInfo();
 
 		// Get the Story that is clicked on the listView.
 		Story storyClicked = adapter.getItem(info.position);
 
 		if (item.getTitle() == "Publish Online") {
 			// Do Publish Story Function
 			Toast.makeText(this, "Publish " + storyClicked.getTitle(),
 					Toast.LENGTH_LONG).show();
 		} else if (item.getTitle() == "Edit Story") {
 
 			Intent i = new Intent(this, EditStoryActivity.class);
 			Bundle bundle = new Bundle();
 			bundle.putSerializable("someStory", storyClicked);
 			i.putExtras(bundle);
 			startActivityForResult(i, ACTIVITY_EDIT_STORY);
 			// Toast.makeText(this, "Edit " + storyClicked.getTitle(),
 			// Toast.LENGTH_LONG)
 			// .show();
 		} else if (item.getTitle() == "Delete Story") {
 			// Do Delete Story Function
 
 			fLoader.deleteStory(storyClicked);
 
 			fLoader = new FileLoader(this);
 			offlineStoryLibrary = fLoader.loadAllStoryFiles();
 			
 			populateListView();
 
 			Toast.makeText(this, "Deleted Story!", Toast.LENGTH_LONG).show();
 		}
 		return true;
 	}
 
 	private void viewStory(Story story) {
 		Intent intent = new Intent(this, ViewPageActivity.class);
 		intent.putExtra("someStory", story);
 		startActivity(intent);
 	}
 	
 	private class CustomAdapter extends ArrayAdapter<Story> {
 
 		public CustomAdapter() {
 			super(OfflineLibraryActivity.this, R.layout.offline_library_row,
 					offlineStoryLibrary);
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			// Tutorial used from: https://www.youtube.com/watch?v=WRANgDgM2Zg
 
 			// Make sure we have a view to work with (may have been given null)
 
 			View itemView = convertView;
 			if (itemView == null) {
 				itemView = getLayoutInflater().inflate(
 						R.layout.offline_library_row, parent, false);
 			}
 
 			Story currentStory = offlineStoryLibrary.get(position);
 
 			// Fill the view
 			ImageView imageView = (ImageView) itemView
 					.findViewById(R.id.storyThumbnailView);
 
 			if (currentStory.getImagePath() == null) {
 				imageView.setImageResource(R.drawable.default_image);
 			} else {
 				imageView.setImageBitmap(BitmapFactory.decodeFile(currentStory
 						.getImagePath()));
 			}
 			TextView titleText = (TextView) itemView.findViewById(R.id.titleTV);
 			titleText.setText(currentStory.getTitle());
 
 			TextView authorText = (TextView) itemView
 					.findViewById(R.id.authorTV);
 			authorText.setText(currentStory.getAuthor());
 
 			TextView dateText = (TextView) itemView
 					.findViewById(R.id.dateCreatedTV);
 			dateText.setText(currentStory.getDate());
 
 			return itemView;
 		}
 	}
 	
 }
