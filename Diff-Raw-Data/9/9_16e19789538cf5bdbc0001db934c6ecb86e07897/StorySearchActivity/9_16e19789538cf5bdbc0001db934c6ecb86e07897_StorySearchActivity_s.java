 
 package ualberta.g12.adventurecreator.views;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.SearchManager;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ListView;
 
 import ualberta.g12.adventurecreator.R;
 import ualberta.g12.adventurecreator.data.AdventureCreator;
 import ualberta.g12.adventurecreator.data.Fragment;
 import ualberta.g12.adventurecreator.data.Story;
 import ualberta.g12.adventurecreator.data.StoryList;
 import ualberta.g12.adventurecreator.tasks.TryPublishStoryTask;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 public class StorySearchActivity extends Activity implements LView<StoryList>, OnItemClickListener {
 
     private List<Story> stories;
     private static StoryList storyList;
     private ListView listView;
     private StoryListArrayAdapter adapter;
     private static boolean isAuthor = false;
     private String query;
 
     private static final String TAG = "StorySearchActivity";
     private static final boolean DEBUG = true;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_story_search);
         // Show the Up button in the action bar.
         setupActionBar();
 
         // Get a storyList instance from the application
         storyList = AdventureCreator.getStoryList();
 
         // Load our local stories from the storyListModel
         stories = new ArrayList<Story>(storyList.getAllStories());
 
         setupListView();
 
         // see who started us
         handleIntent(getIntent());
     }
 
     private void setupListView() {
         // Set up listView
         listView = (ListView) findViewById(R.id.story_search_activity_listview);
         adapter = new StoryListArrayAdapter(this, R.layout.listview_story_list, stories);
         listView.setAdapter(adapter);
         listView.setOnItemClickListener(this);
 
         listView.setOnItemLongClickListener(new OnItemLongClickListener() {
 
             @Override
             public boolean onItemLongClick(AdapterView<?> parent, View view, final int position,
                     long id) {
                 AlertDialog.Builder builder = new AlertDialog.Builder(StorySearchActivity.this);
                 builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         tryPublishStory(position);
                     }
                 });
                 builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialgo, int which) {
                         // Cancel the dialog
                     }
                 });
                 builder.setTitle("Do you want to publish this story");
                 AlertDialog dialog = builder.create();
                 dialog.show();
                 // We don't want the onItemClickListener to go
                 return true;
             }
 
         });
     }
 
     private void tryPublishStory(int position) {
         Story s = stories.get(position);
         TryPublishStoryTask tryPublishTask = new TryPublishStoryTask(this);
         tryPublishTask.execute(new Story[] {
                 s
         });
     }
 
     @Override
     protected void onNewIntent(Intent i) {
         setIntent(i);
         handleIntent(i);
     }
 
     private void handleIntent(Intent i) {
         // Set up the isAuthor flag
         isAuthor = i.getBooleanExtra(MainActivity.IS_AUTHOR_FLAG, false);
 
         /*
          * If it's null we don't have a search turn and don't have to change
          */
         if (i.getStringExtra(SearchManager.QUERY) != null) {
             Log.d(TAG, "There was some Search stuff");
             String query = i.getStringExtra(SearchManager.QUERY).toLowerCase(Locale.CANADA);
             if (DEBUG)
                 Log.d(TAG, String.format("Search query was: %s", query));
             // If we are here we want to reload the ListView with the searched
             // things
             this.query = query;
             performSearch();
         }
     }
 
     private void performSearch() {
         if (this.stories != null && this.query != null) {
             if (DEBUG)
                 Log.d(TAG, String.format("Searching for query: %s", this.query));
 
             List<Story> sl = new ArrayList<Story>();
             sl.addAll(stories);
             stories.clear();
             for (Story s : sl) {
                 if (s.getTitle() == null || s.getAuthor() == null) {
                     // Do nothing this is a story with a null title or
                     // author
                 } else {
                     if (s.getTitle().toLowerCase(Locale.CANADA).contains(this.query)
                             || s.getAuthor().toLowerCase(Locale.CANADA).contains(this.query)) {
                         if (DEBUG)
                             Log.d(TAG, "Adding a story");
                         stories.add(s);
                     }
                 }
             }
 
             if (DEBUG)
                 Log.d(TAG, String.format("SL: %d, stories: %d", sl.size(), stories.size()));
 
             if (DEBUG)
                 Log.d(TAG, "About to notify adapater");
             adapter.notifyDataSetChanged();
         }
     }
 
     @SuppressWarnings("unchecked")
     @Override
     protected void onStart() {
         super.onStart();
 
         // Add ourself to the storyList Model
         storyList.addView(this);
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public void onDestroy() {
         super.onDestroy();
         // Remove ourselves from the storylist model
         storyList.deleteView(this);
     }
 
     /**
      * Set up the {@link android.app.ActionBar}.
      */
     private void setupActionBar() {
 
         getActionBar().setDisplayHomeAsUpEnabled(true);
 
     }
 
     @Override
     public void onResume() {
         super.onResume();
         SharedPreferences settings = getPreferences(MODE_PRIVATE);
         isAuthor = settings.getBoolean(MainActivity.IS_AUTHOR_FLAG, false);
     }
 
     @Override
     public void onPause() {
         super.onPause();
 
         SharedPreferences settings = getPreferences(MODE_PRIVATE);
         SharedPreferences.Editor editor = settings.edit();
         editor.putBoolean(MainActivity.IS_AUTHOR_FLAG, isAuthor);
 
         editor.commit();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.story_search, menu);
         return true;
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         menu.findItem(R.id.menu_check_box_author).setChecked(isAuthor);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 // This ID represents the Home or Up button. In the case of this
                 // activity, the Up button is shown. Use NavUtils to allow users
                 // to navigate up one level in the application structure. For
                 // more details, see the Navigation pattern on Android Design:
                 //
                 // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                 //
                 NavUtils.navigateUpFromSameTask(this);
                 return true;
             case R.id.menu_check_box_author:
                 boolean alreadyChecked = item.isChecked();
                 item.setChecked(!alreadyChecked);
 
                 isAuthor = item.isChecked();
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
         Intent i;
         if (isAuthor) {
             i = new Intent(this, StoryEditActivity.class);
             i.putExtra("Mode", "Edit");
             i.putExtra("StoryPos", pos);
             startActivity(i);
         } else {
             /*
              * Needs get the story from our list of stories as this will be
              * different than the list of stories that our StoryList singleton
              * has.
              */
            Story story = stories.get(pos);
            int fragPos = story.getStartFragPos();

            Fragment goToFrag = story.getFragmentAtPos(fragPos);

             i = new Intent(this, FragmentViewActivity.class);
            i.putExtra("Fragment", goToFrag);
             startActivity(i);
 
         }
     }
 
     @Override
     public void update(StoryList model) {
         // Reload our stories from StoryList Model
         stories = model.getAllStories();
 
         // Notify our listview adapter that our array has changed
         adapter.notifyDataSetChanged();
 
         // Perform search if there has been a previous search
         performSearch();
     }
 
 }
