 package se.chalmers.h_sektionen;
 
 import se.chalmers.h_sektionen.utils.MenuItems;
 import android.os.Build;
 import android.os.Bundle;
 import android.annotation.TargetApi;
 import android.content.Context;
 import android.support.v4.widget.DrawerLayout;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.FrameLayout;
 import android.widget.ListView;
 
 import com.parse.Parse;
 import com.parse.ParseAnalytics;
 import com.parse.ParseInstallation;
 import com.parse.PushService;
 
 import android.support.v7.app.ActionBar;
 import android.support.v7.app.ActionBarActivity;
 
 public class MainActivity extends ActionBarActivity {
     private String[] menuTitles;
     private DrawerLayout mDrawerLayout;
     private ListView mDrawerList;
     private FrameLayout frameLayout;
     	
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);        
         setupActionBar();
         
         menuTitles = getResources().getStringArray(R.array.menu_titles);
         mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
         mDrawerList = (ListView) findViewById(R.id.left_drawer);
         
         frameLayout = (FrameLayout) findViewById(R.id.content_frame);
         
 
         // Set the adapter for the list view
         mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, menuTitles));
         // Set the list's click listener
         mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
 		
 		//Parse.com Add your Parse API keys
 		Parse.initialize(this, "f4nb9heDlUu0uBmPiOJlYCXxlNnHftMkoBRkurLN", 
 				"y7raMOFCv6mkDLm953GFBuRI6P3XAzDYtvbgzmm4");
 		
 		//Parse.com inform the Parse Push Service that it is ready for notifications.
 		PushService.setDefaultPushCallback(this, MainActivity.class);
 		ParseInstallation.getCurrentInstallation().saveInBackground();
 		
 		//Parse.com track statistics around application opens
 		ParseAnalytics.trackAppOpened(getIntent());
 	}
 	
     private class DrawerItemClickListener implements ListView.OnItemClickListener {
         @Override
         public void onItemClick(AdapterView parent, View view, int position, long id) {
 //            Intent intent = new Intent(parent.getContext(), LunchActivity.class);
 //        	startActivity(intent);
         	frameLayout.removeAllViews();
         	LayoutInflater inflater = getLayoutInflater();
         	
         	switch (position) {
         	case MenuItems.NEWS:
         		frameLayout.addView(inflater.inflate(R.layout.activity_main, null));
         		break;
         	case MenuItems.LUNCH:
         		frameLayout.addView(inflater.inflate(R.layout.view_lunch, null));
         		break;
         	case MenuItems.PUB:
         		frameLayout.addView(inflater.inflate(R.layout.view_pub, null));
         		break;
         	case MenuItems.INFO:
         		frameLayout.addView(inflater.inflate(R.layout.view_info, null));
         		break;
         	case MenuItems.EVENTS:
         		frameLayout.addView(inflater.inflate(R.layout.view_events, null));
         		break;
         	case MenuItems.VOTE:
         		frameLayout.addView(inflater.inflate(R.layout.view_vote, null));
         		break;
         	case MenuItems.SUGGEST:
         		frameLayout.addView(inflater.inflate(R.layout.view_suggest, null));
         		break;	
         	default:
         		return;
         	}
         	
         	mDrawerLayout.closeDrawer(Gravity.LEFT);
         }
     }
 	
     
     private void setupActionBar() {
     	ActionBar ab = getSupportActionBar();
     	ab.setDisplayShowCustomEnabled(true);
     	ab.setDisplayShowTitleEnabled(false);
     	ab.setIcon(R.drawable.ic_action_overflow);
     	
     	LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     	View v = inflator.inflate(R.layout.action_bar_title, null);
 
//    	Om vi vill ha nån schysst font till titlen tydligen
 //    	TextView titleTV = (TextView) v.findViewById(R.id.title);
 //    	Typeface font = Typeface.createFromAsset(getAssets(), "fonts/your_custom_font.ttf");
 //    	titleTV.setTypeface(font);
     	
     	ab.setCustomView(v);
     	
     	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
     		enableHomeButton();
     	} else {
     		ab.setHomeButtonEnabled(true);
     	}
     }
     @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
     private void enableHomeButton() {
     	getActionBar().setHomeButtonEnabled(true);
     }
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		
 		 switch(item.getItemId()) {		 	
 		 	case android.R.id.home:
 		 		openMenu();
 		 		return true;
 		 	default:
 		 		return super.onOptionsItemSelected(item);
 		 }
 	}
 	
 	private void openMenu() {
 		if(mDrawerLayout.isDrawerOpen(Gravity.LEFT))
 			mDrawerLayout.closeDrawer(Gravity.LEFT);
 		else
 			mDrawerLayout.openDrawer(Gravity.LEFT);
 	}
 
 }
