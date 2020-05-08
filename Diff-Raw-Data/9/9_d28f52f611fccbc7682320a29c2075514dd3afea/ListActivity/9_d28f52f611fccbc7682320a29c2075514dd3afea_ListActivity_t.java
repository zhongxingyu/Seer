 package se.chalmers.krogkollen.list;
 
 import android.app.ActionBar;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.ViewPager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.ListView;
 import android.widget.Toast;
 import se.chalmers.krogkollen.R;
 import se.chalmers.krogkollen.adapter.TabsPagerAdapter;
 import se.chalmers.krogkollen.map.IMapView;
 import se.chalmers.krogkollen.map.MapActivity;
 import se.chalmers.krogkollen.map.MapPresenter;
 
 
 /**
  * Activity for the list view. This shows a list that is sorted by a few default values, that the user can chose between.
  * Such as distance, queue-time or favorites.
  *
  */
 public class ListActivity extends FragmentActivity implements IListView{
 
 	private ViewPager viewPager;
     private IMapView mapView; // TODO this is never used?
     private TabsPagerAdapter mAdapter;
     private ActionBar actionBar;
     
     // Tab titles
    private String[] tabs;
     private IListPresenter presenter;
     private ListView list;  // TODO this is never used?
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_list);
         
        tabs = new String[] {	getString(R.string.list_tab_name_queue_time), 
								getString(R.string.list_tab_name_distance), 
								getString(R.string.list_tab_name_favorites)};
        
         // TODO is this still a test?
         //Detta är bara för TEST 
         viewPager = (ViewPager) findViewById(R.id.pager);
         actionBar = getActionBar();
         mAdapter = new TabsPagerAdapter(getSupportFragmentManager());
 
         viewPager.setAdapter(mAdapter);
         actionBar.setHomeButtonEnabled(false);
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
         presenter = new ListPresenter(this);
         
         // Adding Tabs
         for (String tab_name : tabs) {
             actionBar.addTab(actionBar.newTab().setText(tab_name)
                     .setTabListener(this.presenter));
         }
 
         viewPager.setOnPageChangeListener(presenter);
 
         // Remove the default logo icon and add our list icon.
         ActionBar actionBar = getActionBar();
         actionBar.setIcon(R.drawable.map_icon);
         actionBar.setDisplayHomeAsUpEnabled(true);
     }
 
     // Start the activity in a local method to keep the right context.
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
 
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.list, menu);
 
         return true;
     }
 
     @Override
     public void setActionBarSelectedNavigationItem(int pos){
         actionBar.setSelectedNavigationItem(pos);
     }
     
     @Override
     public void setViewPagerCurrentItem(int pos){
         viewPager.setCurrentItem(pos);
     }
 
     @Override
     public void navigate(Class<?> destination) {
         Intent intent = new Intent(this, destination);
         startActivity(intent);
     }
 
     @Override
     public void navigate(Class<?> destination, Bundle extras) {
         Intent intent = new Intent(this, destination);
         intent.putExtra(MapActivity.MARKER_PUB_ID, extras.getString(MapPresenter.MAP_PRESENTER_KEY));
         startActivity(intent);
     }
 
     @Override
 	public void showErrorMessage(String message) {
     	CharSequence text = message;
     	int duration = Toast.LENGTH_LONG;
 
     	Toast toast = Toast.makeText(this, text, duration);
     	toast.show();
 	}
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.refresh_info:
                 //TODO implement
                 //new RefreshTask().execute();
                 break;
             case R.id.search:
                 //TODO implement
                // this.onSearch();
                 break;
 
             case android.R.id.home:
                 this.navigate(MapActivity.class);
                 break;
         }
         return super.onOptionsItemSelected(item);
     }
 }
