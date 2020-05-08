 package com.megginson.sloopsql;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.DialogFragment;
 import android.app.Fragment;
 import android.app.FragmentTransaction;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.ViewGroup;
 import com.megginson.sloopsql.R;
 import java.util.ArrayList;
 
 /**
  * Main container activity for the UI.
  *
  * This activity manages the various fragments that make up the app.
  */
 public class MainActivity extends Activity implements TableListFragment.Listener
 {
 
 	//
 	// Internal state
 	//
 
 	/**
 	 * Serial counter for query tabs
 	 */
 	private int mQueryCounter = 0;
 
 
 	//
 	// Activity lifecycle methods
 	//
 
 	/**
 	 * Lifecycle event: activity first created
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.main);
 
 		// Specify that tabs should be displayed in the action bar.
 		getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 	}
 
 	/**
 	 * Lifecycle event: activity finally destroyed.
 	 */
 	@Override
 	protected void onDestroy()
 	{
 		super.onDestroy();
 	}
 
 	/**
 	 * Lifecycle event: Android wants us to save the instance state.
 	 */
 	@Override
 	protected void onSaveInstanceState(Bundle savedInstanceState)
 	{
 		super.onSaveInstanceState(savedInstanceState);
 		save_tabs(savedInstanceState);
 	}
 
 	/**
 	 * Lifecycle event: Android wants us to restore the instance state.
 	 */
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState)
 	{
 		super.onRestoreInstanceState(savedInstanceState);
 		restore_tabs(savedInstanceState);
 	}
 
 	/**
 	 * Lifecycle event: Android is creating the options menu.
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		super.onCreateOptionsMenu(menu);
 
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.main_menu, menu);
 		return true;		
 	}
 
 	/**
 	 * Lifecycle event: Android is preparing the options menu.
 	 */
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu)
 	{
 		super.onPrepareOptionsMenu(menu);
 
 		// Hide the close tab item if there are no tabs open
 		menu.findItem(R.id.item_close_tab).setVisible(getActionBar().getTabCount() > 0);
 
 		return true;		
 	}
 
 	/**
 	 * Lifecycle event: user has selected a menu item.
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		super.onOptionsItemSelected(item);
 
 		// Handle item selection
 		switch (item.getItemId())
 		{
 			case R.id.item_add_query:
				do_add_query_tab(null, null);
 				return true;
 			case R.id.item_close_tab:
 				do_close_current_tab();
 				return true;
 			case R.id.item_list_tables:
 				do_list_tables();
 				return true;
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 
 	//
 	// Listener callbacks for fragments.
 	//
 
 	/**
 	 * The user has selected a table in the table list dialog.
 	 */
 	@Override
 	public void onTableSelected(String tableName)
 	{
		do_add_query_tab(tableName, "select * from " + tableName);
 	}
 
 
 	//
 	// Callbacks for actions a user has performed in the UI
 	//
 
 	/**
 	 * Action: add a new query tab
 	 */
	private void do_add_query_tab(String title, String queryText)
 	{
		if (title == null)
		{
			title = "Query " + (mQueryCounter + 1);
		}
 		ActionBar.Tab tab = 
			add_fragment_tab(title, QueryFragment.newInstance(queryText));
 		tab.select();
 		mQueryCounter++;
 	}
 
 	/**
 	 * Action: close the current tab.
 	 */
 	private void do_close_current_tab()
 	{
 		ActionBar.Tab currentTab = getActionBar().getSelectedTab();
 		if (currentTab != null)
 		{
 			getActionBar().removeTab(currentTab);
 			invalidateOptionsMenu();
 		}
 	}
 
 	/**
 	 * Action: list tables in this database.
 	 */
 	private void do_list_tables()
 	{
 		FragmentTransaction ft = getFragmentManager().beginTransaction();
 		Fragment prev = getFragmentManager().findFragmentByTag("dialog");
 		if (prev != null)
 		{
 			ft.remove(prev);
 		}
 		ft.addToBackStack(null);
 
 		// Create and show the dialog.
 		DialogFragment newFragment = new TableListFragment();
 		newFragment.show(ft, "dialog");
 	}
 
 
 	//
 	// Internal utility methods
 	//
 
 	/**
 	 * Restore navigation tab state from a bundle.
 	 *
 	 * Android will use this information to recreate an activity after e.g.
 	 * an orientation change.
 	 *
 	 * @param savedInstanceState the bundle from which to restore the state.
 	 * @see #save_tabs(Bundle)
 	 */
 	private void restore_tabs(Bundle savedInstanceState)
 	{
 		((ViewGroup)findViewById(R.id.fragment_container)).removeAllViews();
 		mQueryCounter = savedInstanceState.getInt("queryCounter");
 		int selectedTabIndex = savedInstanceState.getInt("selectedTabIndex");
 		ArrayList<Parcelable> fragmentStates = savedInstanceState.getParcelableArrayList("fragmentStates");
 		ArrayList<String> tabTitles = savedInstanceState.getStringArrayList("tabTitles");
 
 		if (tabTitles != null && fragmentStates != null)
 		{
 			for (int i = 0; i < tabTitles.size() && i < fragmentStates.size(); i++)
 			{
 				String tabTitle = tabTitles.get(i);
 				Fragment.SavedState fragmentState = (Fragment.SavedState)fragmentStates.get(i);
 				Fragment fragment = new QueryFragment();
 				fragment.setInitialSavedState(fragmentState);
 				add_fragment_tab(tabTitle, fragment);
 			}
 			if (selectedTabIndex > -1)
 			{
 				getActionBar().setSelectedNavigationItem(selectedTabIndex);
 			}
 		}
 	}
 
 	/**
 	 * Save navigation tab state to a bundle.
 	 *
 	 * This saved information lets us restore after e.g. an orientation
 	 * change.
 	 *
 	 * We will save the tab titles and the tags of the associated fragments, 
 	 * all of which must have called {@link Fragment#setRetainState(boolean)}
 	 * with true in their {@link Fragment#onActivityCreated} methods to tell
 	 * Android to preserve their internal state after the parent activity
 	 * terminates.
 	 *
 	 * @param savedInstanceState the bundle to which to save the state.
 	 * @see #restore_tabs(Bundle)
 	 */
 	private void save_tabs(Bundle savedInstanceState)
 	{
 		ArrayList<String> tabTitles = new ArrayList<String>();
 		ArrayList<Parcelable> fragmentStates = new ArrayList<Parcelable>();
 
 		for (int i = 0; i < getActionBar().getTabCount(); i++)
 		{
 			ActionBar.Tab tab = getActionBar().getTabAt(i);
 			TabListener listener = (TabListener)tab.getTag();
 			String tabTitle = tab.getText().toString();
 			Fragment fragment = listener.getFragment();
 
 			Fragment.SavedState fragmentState = getFragmentManager().saveFragmentInstanceState(fragment);
 			tabTitles.add(tabTitle);
 			fragmentStates.add(fragmentState);
 		}
 
 		savedInstanceState.putInt("queryCounter", mQueryCounter);
 		savedInstanceState.putInt("selectedTabIndex", getActionBar().getSelectedNavigationIndex());
 		savedInstanceState.putStringArrayList("tabTitles", tabTitles);
 		savedInstanceState.putParcelableArrayList("fragmentStates", fragmentStates);
 	}
 
 	/**
 	 * Create and add a new action bar tab.
 	 *
 	 * This method adds a new {@link TabListener} to the fragment, which
 	 * we can retrieve using {@link Fragment#getTag} (not to be confused
 	 * with the tag in the {@link FragmentManager}).
 	 *
 	 * This method automatically refreshes the options menu.
 	 *
 	 * @param label The tab label/title.
 	 * @param fragment The {@link Fragment} associated with the tab.
 	 * @return The new tab.
 	 */
 	private ActionBar.Tab add_fragment_tab(String label, Fragment fragment)
 	{
 		TabListener listener = new TabListener(this, R.id.fragment_container, fragment);
 		ActionBar.Tab tab = getActionBar().newTab()
 			.setText(label)
 			.setTabListener(listener);
 		// there's no getTabListener() method, so save a copy here
 		tab.setTag(listener);
 		getActionBar().addTab(tab);
 		invalidateOptionsMenu();
 		return tab;
 	}
 
 }
