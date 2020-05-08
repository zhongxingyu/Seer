 package com.example.newspinproj;
 
 import Model.DataContainer;
 import Model.User;
 import android.annotation.TargetApi;
 import android.app.ActionBar;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends FragmentActivity implements
 		ActionBar.OnNavigationListener {
 
 	/**
 	 * The serialization (saved instance state) Bundle key representing the
 	 * current dropdown position.
 	 */
 	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
 	private String curgroup;
 	private TabFragment tabFragment = new TabFragment();
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		@SuppressWarnings("unused")
 		DataContainer dataContainer = new DataContainer();
 		
 		tabFragment = new TabFragment();
 		getSupportFragmentManager().beginTransaction()
 				.add(R.id.container, tabFragment).commit();
 		
		this.firstData();
 		
 
 		try {
 			Thread.sleep(1500);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 		// Set up the action bar to show a dropdown list.
 		final ActionBar actionBar = getActionBar();
 		actionBar.setDisplayShowTitleEnabled(false);
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
 
 		// Set up the dropdown list navigation in the action bar.
 		actionBar.setListNavigationCallbacks(
 		// Specify a SpinnerAdapter to populate the dropdown list.
 				new ArrayAdapter<String>(getActionBarThemedContextCompat(),
 						android.R.layout.simple_list_item_1,
 						android.R.id.text1, new String[] {
 								getString(R.string.title_section4),
 								getString(R.string.title_section5),
 								getString(R.string.title_section6),
 								getString(R.string.title_section7), 
 								getString(R.string.title_section8),}), this);
 	}
 
 	/**
 	 * Backward-compatible version of {@link ActionBar#getThemedContext()} that
 	 * simply returns the {@link android.app.Activity} if
 	 * <code>getThemedContext</code> is unavailable.
 	 */
 	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
 	private Context getActionBarThemedContextCompat() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
 			return getActionBar().getThemedContext();
 		} else {
 			return this;
 		}
 	}
 
 	@Override
 	public void onRestoreInstanceState(Bundle savedInstanceState) {
 		// Restore the previously serialized current dropdown position.
 		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
 			getActionBar().setSelectedNavigationItem(
 					savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
 		}
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		// Serialize the current dropdown position.
 		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
 				.getSelectedNavigationIndex());
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onNavigationItemSelected(int position, long id) {
 		/*
 		 * // When the given dropdown item is selected, show its contents in the
 		 * // container view. TabFragment fragment = new TabFragment(); Bundle
 		 * args = new Bundle();
 		 * args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
 		 * fragment.setArguments(args);
 		 * getSupportFragmentManager().beginTransaction()
 		 * .replace(R.id.container, fragment).commit(); return true;
 		 */
 		curgroup = "" + position;
 		String[] groups = new String[] {"All groups", "No6", "SH", "789", "Life"};
 		tabFragment.changeData(groups[position]);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.menu_refresh:
 			refreshData();
 			return true;
 		case R.id.menu_add:
 			Toast.makeText(getApplicationContext(), "Create task", Toast.LENGTH_SHORT)
 					.show();
 			openTaskCreation();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	private void openTaskCreation(){
 		
 		Intent intent = new Intent(MainActivity.this, TaskAssignent.class);
         Bundle bundle = new Bundle();
         bundle.putString("group", curgroup);
         intent.putExtra("groupinfo", bundle);
         startActivity(intent);
 	}
 	
	private void firstData(){
		User u = new User("Niki", "mary", "sally", 234);
 		u.getTasks();
 	}
 	
 	private void refreshData() {
 	
 		this.recreate();
 	}
 	
 	public void redrawData() {
 		// TODO: 
 	}
 	/**
 	 * A dummy fragment representing a section of the app, but that simply
 	 * displays dummy text.
 	 */
 	public static class DummySectionFragment extends Fragment {
 		/**
 		 * The fragment argument representing the section number for this
 		 * fragment.
 		 */
 		public static final String ARG_SECTION_NUMBER = "section_number";
 
 		public DummySectionFragment() {
 		}
 
 		@Override
 		public View onCreateView(LayoutInflater inflater, ViewGroup container,
 				Bundle savedInstanceState) {
 			View rootView = inflater.inflate(R.layout.fragment_main_dummy,
 					container, false);
 			TextView dummyTextView = (TextView) rootView
 					.findViewById(R.id.section_label);
 			dummyTextView.setText(Integer.toString(getArguments().getInt(
 					ARG_SECTION_NUMBER)));
 
 			return rootView;
 		}
 	}
 
 }
