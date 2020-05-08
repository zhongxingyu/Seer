 /**
  * 
  */
 package story.book;
 
 import java.util.ArrayList;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.GridView;
 import android.widget.ListView;
 
 /**
  * StoryFragmentListActivity displays all story fragments contained
  * in the story that is currently open. Selecting a story fragment
  * will allow user to:
  * 		1. Edit the story fragment
  * 		2. Set the story fragment as the starting fragment (TODO)
  * 		3. Delete the story fragment from the story (TODO)
  * 
  * @author jsurya
  *
  */
 public class StoryFragmentListActivity extends Activity {
 	ActionBar actionBar;
 	ArrayList<StoryFragment> SFL;
 	ArrayAdapter<StoryFragment> adapter;
 	StoryCreationController SCC;
	StoryApplication SA;
 	
 	int pos;
 	
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		setContentView(R.layout.story_fragment_read_activity);
 		SCC = new StoryCreationController();
 		SFL = SCC.getFragments();
		SA = new StoryApplication();
		String title = SA.getCurrentStory().getStoryInfo().getTitle();
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
 				startActivity(new Intent(v.getContext(), 
 						 StoryFragmentEditActivity.class));
 			}
 
 		});
 
 		return;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    // Inflate the menu items for use in the action bar
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.add_illustration_menu, menu);
 	    inflater.inflate(R.menu.standard_menu, menu);
 	    return super.onCreateOptionsMenu(menu);
 	}
 	
 	public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         switch (item.getItemId()) {
         case R.id.title_activity_dashboard:
             Intent intent = new Intent(this, Dashboard.class);
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
 		 menu.add(0, v.getId(), 2, "Set as Starting Story Fragment"); 
 		 menu.add(0, v.getId(), 3, "Delete");
 		 menu.add(0, v.getId(), 4, "Cancel");
 	}
 	
 	@Override  
 	public boolean onContextItemSelected(MenuItem item) {
 		
 		switch (item.getOrder()) {
 		case 1:
 			// Edit story fragment
 			Intent i = new Intent(this, StoryFragmentEditActivity.class);
 			startActivity(i);
 			break;
 			
 		case 2:
 			// Set as starting story fragment
 
 			break;
 		case 3:
 			//Delete
 			
 			break;
 			
 		case 4:
 			// Cancel options
 			return false;
 		}
 			
 		return true; 
 		
 	}
 }
