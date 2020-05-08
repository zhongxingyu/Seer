 /**
  * 
  */
 package story.book;
 
 import java.util.ArrayList;
 
 import android.annotation.TargetApi;
 import android.app.ActionBar;
 import android.app.Fragment;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.ArrayAdapter;
 import android.widget.TabHost;
 
 /**
  * StoryFragmentReadActivity is an interface for users to read a story
  * fragment (left tab), as well as an interface for users to add 
  * annotations (right tab) on the story fragment which is currently being read.
  * 
  * @author jsurya
  *
  */
 @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
 public class StoryFragmentReadActivity extends FragmentActivity implements StoryView<Story> {
 	 // Declare Tab Variable
     ActionBar.Tab Tab1, Tab2;
     Fragment readingTab1;
     Fragment annotationsTab2;
     ActionBar actionBar;
     StoryReadController SRC;
     ArrayList<Illustration> illustrations;
     
     static StoryFragment SF = null;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.story_fragment_read_activity);
 		
 		SRC = new StoryReadController();
 		SF = SRC.getStartingFragment();
		SF.notifyViews();
 		String title = SF.getFragmentTitle();
 		illustrations = SF.getIllustrations();
 		
 		readingTab1 = new ReadingFragment();
 		annotationsTab2 = new AnnotationFragment();
 		actionBar = getActionBar();
 		actionBar.setTitle(title);
 		 
         // Create Actionbar Tabs
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
  
         // Set Tab Icon and Titles
         Tab1 = actionBar.newTab().setText("Story");
         Tab2 = actionBar.newTab().setText("Annotations");
  
         // Set Tab Listeners
         Tab1.setTabListener(new TabListener(readingTab1));
         Tab2.setTabListener(new TabListener(annotationsTab2));
         
         // Add tabs to action bar
         actionBar.addTab(Tab1);
         actionBar.addTab(Tab2);
 
 	}
 	
 	@Override
 	public void onStart() {
 		super.onStart();
 
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    // Inflate the menu items for use in the action bar
 	    MenuInflater inflater = getMenuInflater();
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
 	public void update(Story model) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public StoryReadController getController() {
 		return SRC;
 	}
 
 }
