 package net.eledge.android.eu.europeana.gui.activity;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.eledge.android.eu.europeana.Config;
 import net.eledge.android.eu.europeana.R;
 import net.eledge.android.eu.europeana.gui.adaptor.FacetsAdaptor;
 import net.eledge.android.eu.europeana.gui.enums.FacetItemType;
 import net.eledge.android.eu.europeana.gui.enums.FacetType;
 import net.eledge.android.eu.europeana.gui.fragments.SearchResultsFragment;
 import net.eledge.android.eu.europeana.gui.model.FacetItem;
 import net.eledge.android.eu.europeana.search.SearchController;
 import net.eledge.android.eu.europeana.search.listeners.SearchTaskListener;
 import net.eledge.android.eu.europeana.search.model.SearchResult;
 import net.eledge.android.eu.europeana.search.model.searchresults.Facet;
 import android.app.SearchManager;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.os.StrictMode;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.view.GravityCompat;
 import android.support.v4.widget.DrawerLayout;
 import android.text.TextUtils;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.SearchView;
 import android.widget.ShareActionProvider;
 
 public class SearchActivity extends FragmentActivity implements SearchTaskListener {
 
 	private ShareActionProvider mShareActionProvider;
 
 	private SearchResultsFragment mSearchFragment;
 	private DrawerLayout mDrawerLayout;
 	private ListView mFacetsList;
 	private ActionBarDrawerToggle mDrawerToggle;
 	private FacetsAdaptor mFacetsAdaptor;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_search);
 
 		mFacetsList = (ListView) findViewById(R.id.drawer_facets);
 		mFacetsAdaptor = new FacetsAdaptor(this, new ArrayList<FacetItem>());
 
 		mFacetsList.setAdapter(mFacetsAdaptor);
 		mFacetsList.setOnItemClickListener(new DrawerItemClickListener());
 
 		// enable ActionBar app icon to behave as action to toggle nav drawer
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 		getActionBar().setHomeButtonEnabled(true);
 
		View view = findViewById(R.id.layout_activity_search);
		if ((view != null ) && view instanceof DrawerLayout) {
			mDrawerLayout = (DrawerLayout) view;
 			mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
 			mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer,
 					R.string.drawer_facets_open, R.string.drawer_facets_close) {
 				public void onDrawerClosed(View view) {
 					// getActionBar().setTitle(mTitle);
 					invalidateOptionsMenu();
 				}
 
 				public void onDrawerOpened(View drawerView) {
 					// getActionBar().setTitle(mDrawerTitle);
 					invalidateOptionsMenu();
 				}
 			};
 			mDrawerLayout.setDrawerListener(mDrawerToggle);
 		}
 
 		if (Config.DEBUGMODE) {
 			StrictMode.enableDefaults();
 		}
 		handleIntent(getIntent());
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.search, menu);
 		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
 		SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
 		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
 
 		MenuItem share = menu.findItem(R.id.action_share);
 		// Fetch and store ShareActionProvider
 		mShareActionProvider = (ShareActionProvider) share.getActionProvider();
 		if (mShareActionProvider != null) {
 			mShareActionProvider.setShareIntent(createShareIntent());
 		}
 
 		return true;
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		if (mDrawerLayout != null) {
 			boolean drawerOpen = mDrawerLayout.isDrawerOpen(mFacetsList);
 			menu.findItem(R.id.action_search).setVisible(!drawerOpen);
 			menu.findItem(R.id.action_share).setVisible(!drawerOpen);
 		}
 		return super.onPrepareOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if ((mDrawerLayout != null) && mDrawerToggle.onOptionsItemSelected(item)) {
 			return true;
 		}
 		switch (item.getItemId()) {
 
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	protected void onPostCreate(Bundle savedInstanceState) {
 		super.onPostCreate(savedInstanceState);
 		if (mDrawerLayout != null) {
 			mDrawerToggle.syncState();
 		}
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 		if (mDrawerLayout != null) {
 			mDrawerToggle.syncState();
 		}
 	}
 
 	@Override
 	protected void onNewIntent(Intent intent) {
 		setIntent(intent);
 		handleIntent(intent);
 	}
 
 	@Override
 	public void onSearchStart() {
 		// TODO Search animation
 	}
 
 	@Override
 	public void onSearchError(String message) {
 		// TODO Report error
 	}
 
 	@Override
 	public void onSearchFinish(SearchResult results) {
 		FragmentManager fragmentManager = getSupportFragmentManager();
 		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
 		if (mSearchFragment == null) {
 			mSearchFragment = new SearchResultsFragment();
 		}
 		fragmentTransaction.replace(R.id.content_frame, mSearchFragment);
 		fragmentTransaction.commit();
 	}
 
 	@Override
 	public void onSearchFacetsUpdate(List<Facet> facets) {
 		if (facets != null) {
 			List<FacetItem> facetlist = new ArrayList<FacetItem>();
 			for (Facet facet : facets) {
 				FacetType type = FacetType.safeValueOf(facet.name);
 				if (type != null) {
 					FacetItem item = new FacetItem();
 					item.itemType = FacetItemType.CATEGORY;
 					item.facetType = type;
 					item.facet = facet.name;
 					facetlist.add(item);
 				}
 			}
 			mFacetsAdaptor.clear();
 			mFacetsAdaptor.addAll(facetlist);
 			mFacetsAdaptor.notifyDataSetChanged();
 		}
 	}
 
 	private Intent createShareIntent() {
 		Intent shareIntent = new Intent(Intent.ACTION_SEND);
 		shareIntent.setType("text/plain");
 		shareIntent.putExtra(Intent.EXTRA_TEXT, SearchController.getInstance().getPortalUrl());
 		shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Check out this search on Europeana.eu!");
 		return shareIntent;
 	}
 
 	private void handleIntent(Intent intent) {
 		String query = null;
 		if (intent != null) {
 			if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
 				query = intent.getStringExtra(SearchManager.QUERY);
 			} else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
 				query = intent.getDataString();
 			} else {
 				onSearchRequested();
 			}
 			if (!TextUtils.isEmpty(query)) {
 				SearchController.getInstance().newSearch(this, query);
 			}
 		}
 	}
 
 	/* The click listner for ListView in the navigation drawer */
 	private class DrawerItemClickListener implements ListView.OnItemClickListener {
 		@Override
 		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 			// selectItem(position);
 		}
 	}
 
 }
