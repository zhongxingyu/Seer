 package edu.vanderbilt.vm.guide.ui;
 
 import java.util.List;
 
 import android.annotation.TargetApi;
 import android.app.ActionBar;
 import android.app.ActionBar.Tab;
 import android.app.Activity;
 import android.app.FragmentTransaction;
 import android.content.Context;
 import android.content.Intent;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
import android.widget.Toast;
 import edu.vanderbilt.vm.guide.R;
 import edu.vanderbilt.vm.guide.container.Place;
 import edu.vanderbilt.vm.guide.db.GuideDBOpenHelper;
 import edu.vanderbilt.vm.guide.ui.listener.ActivityTabListener;
 import edu.vanderbilt.vm.guide.ui.listener.FragmentTabListener;
 import edu.vanderbilt.vm.guide.util.Geomancer;
 import edu.vanderbilt.vm.guide.util.GlobalState;
 import edu.vanderbilt.vm.guide.util.GuideConstants;
 
 @TargetApi(13)
 public class GuideMain extends Activity {
 	private ActionBar mAction;
 	private Menu mMenu;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_guide_main);
 		setupActionBar();
 		
 		// TODO
 		List<Place> placeList = GlobalState.getPlaceList(this);
 		for (int i = 0; i < 7; i++){
 			GlobalState.getUserAgenda().add(placeList.get(i));
 		}
 		
 		Geomancer.activateGeolocation(this);
 	}
 	// ---------- END onCreate() ---------- //
 	
 	// ---------- BEGIN setup and lifecycle related methods ---------- //
 
 	private void setupActionBar() {
 		mAction = getActionBar();
 		mAction.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 		mAction.setDisplayShowTitleEnabled(true);
 		mAction.setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_bg));
 		
 		Intent myIntent = getIntent();
 		final Integer selection;
 		if (myIntent != null && myIntent.hasExtra("selection")) {
 			selection = (Integer) myIntent.getExtras().get("selection");
 		} else {
 			selection = null;
 		}
 
 		boolean toursSelected = isSelected(3, selection);
 		boolean agendaSelected = isSelected(2, selection);
 		boolean placesSelected = isSelected(1, selection)
 				|| (!toursSelected && !agendaSelected);
 
 		Tab tab = mAction.newTab()
 				.setText("Places")
 				.setTabListener(
 						new FragmentTabListener<PlaceTabFragment>(this,
 								"places", PlaceTabFragment.class));
 		mAction.addTab(tab, 0, placesSelected);
 
 		tab = mAction.newTab()
 				.setText("Agenda")
 				.setTabListener(
 						new FragmentTabListener<AgendaFragment>(this, "agenda",
 								AgendaFragment.class));
 		mAction.addTab(tab, 1, agendaSelected);
 		
 		tab = mAction.newTab()
 				.setText("Tours")
 				.setTabListener(
 						new FragmentTabListener<TourFragment>(this, "tours",
 								TourFragment.class));
 		mAction.addTab(tab, 2, toursSelected);
 		
 		tab = mAction.newTab()
 				.setText("Stats")
 				.setTabListener( //TODO
 						new FragmentTabListener<TourFragment>(this, "stats",
 								TourFragment.class));
 		mAction.addTab(tab, 3, false);
 		
 	}
 	
 	public boolean onCreateOptionsMenu(Menu menu){
 		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.activity_guide_main, menu);
 	    mMenu = menu;
 	    return true;
 	}
 	
 	public boolean onOptionsItemSelected(MenuItem item){
 		
 		switch (item.getItemId()){
 		case R.id.menu_map:
 			Intent i = new Intent(this, ViewMapActivity.class);
 			i.putExtra(GuideConstants.MAP_AGENDA, "");
 			startActivity(i);
 			return true;
 		case R.id.menu_refresh:
			//TODO
			Toast.makeText(this, "Current Location Updated", Toast.LENGTH_SHORT).show();
 			return true;
 		
 		default: return false;
 		}
 	}
 	// ---------- END setup and lifecycle related methods ---------- //
 	
 	private boolean isSelected(int n, Integer selection) {
 		return selection != null && n == selection;
 	}
 	
 	/**
 	 * Adds a little hack to "forward" the user on to the map activity when the
 	 * map tab is clicked. This effectively removes this activity from the back
 	 * stack.
 	 * 
 	 * @author nicholasking
 	 * 
 	 */
 	private class MyActivityTabListener extends ActivityTabListener {
 
 		public MyActivityTabListener(Context packageCtx, Class<?> target) {
 			super(packageCtx, target);
 		}
 
 		@Override
 		public void onTabSelected(Tab tab, FragmentTransaction ft) {
 			super.onTabSelected(tab, ft);
 			finish();
 		}
 
 	}
 	
 	
 }
