 package com.escoand.android.wceu;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import android.annotation.SuppressLint;
 import android.app.ActionBar;
 import android.app.ActionBar.OnNavigationListener;
 import android.app.ActionBar.Tab;
 import android.app.ActionBar.TabListener;
 import android.app.FragmentTransaction;
 import android.app.ListFragment;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.graphics.Point;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.view.Display;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.escoand.android.wceu.CategoryDialog.CategoryDialogListener;
 import com.handmark.pulltorefresh.library.PullToRefreshBase;
 import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
 import com.handmark.pulltorefresh.library.PullToRefreshListView;
 
 public class NewsListFragment extends ListFragment implements
 		CategoryDialogListener {
 
 	private NewsDatabase dbNews;
 	private EventsDatabase dbEvents;
 	private String displayType = "news";
 	private String displayFilter = "";
 
 	public NewsListFragment() {
 		super();
 	}
 
 	/* create fragment */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		setHasOptionsMenu(true);
 		super.onCreate(savedInstanceState);
 	}
 
 	/* create fragment view */
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.list, container, false);
 	}
 
 	/* fragment created */
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 
 		/* load settings */
 		if (savedInstanceState != null) {
 			displayType = savedInstanceState.getString("displayType");
 			displayFilter = savedInstanceState.getString("displayFilter");
 		}
 
 		/* data */
 		dbNews = new NewsDatabase(getActivity());
 		dbEvents = new EventsDatabase(getActivity());
 		setListAdapter(new NewsListAdapter(getActivity()));
 
 		/* action bar */
 		ActionBar actionBar = getActivity().getActionBar();
 		actionBar.setDisplayShowTitleEnabled(false);
 
 		TabListener tabl = new TabListener() {
 			@Override
 			public void onTabUnselected(Tab tab, FragmentTransaction ft) {
 			}
 
 			@Override
 			public void onTabSelected(Tab tab, FragmentTransaction ft) {
 				if (tab.getText().equals(getString(R.string.menuNews)))
 					displayType = "news";
 				else if (tab.getText().equals(getString(R.string.menuEvents)))
 					displayType = "events";
 				refreshDisplay();
 			}
 
 			@Override
 			public void onTabReselected(Tab tab, FragmentTransaction ft) {
 				onTabSelected(tab, ft);
 			}
 		};
 
 		/* tabs */
 		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
 			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 			actionBar.addTab(actionBar.newTab().setText(R.string.menuNews)
 					.setTabListener(tabl));
 			actionBar.addTab(actionBar.newTab().setText(R.string.menuEvents)
 					.setTabListener(tabl));
 			if (displayType.equals("events"))
 				actionBar.selectTab(actionBar.getTabAt(1));
 		}
 
 		/* dropdown */
 		else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
 			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
 			actionBar.setListNavigationCallbacks(ArrayAdapter
 					.createFromResource(getActivity(), R.array.listSpinner,
 							android.R.layout.simple_spinner_dropdown_item),
 					new OnNavigationListener() {
 						@Override
 						public boolean onNavigationItemSelected(int pos, long id) {
 							if (pos == 0)
 								displayType = "news";
 							else if (pos == 1)
 								displayType = "events";
 							refreshDisplay();
 							return true;
 						}
 					});
 			if (displayType.equals("events"))
 				actionBar.setSelectedNavigationItem(1);
 		}
 
 		/* banner */
 		if (getActivity().findViewById(R.id.banner) != null)
 			getActivity().findViewById(R.id.banner).setOnClickListener(
 					new OnClickListener() {
 						@Override
 						public void onClick(View v) {
 							CategoryDialog diag = new CategoryDialog();
							diag.show(
									getActivity().getSupportFragmentManager(),
									"");
 						}
 					});
 
 		/* list */
 		PullToRefreshListView list = (PullToRefreshListView) getActivity()
 				.findViewById(R.id.listNews);
 		list.setEmptyView(getActivity().findViewById(R.id.listEmpty));
 		list.setAdapter(getListAdapter());
 		list.setShowIndicator(true);
 		list.setOnRefreshListener(new OnRefreshListener<ListView>() {
 			@Override
 			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
 				refreshData();
 			}
 		});
 		refreshDisplay();
 
 		if (getActivity().findViewById(R.id.mainArticle) != null) {
 			getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
 		}
 
 		super.onActivityCreated(savedInstanceState);
 	}
 
 	/* fragment stopped */
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		outState.putString("displayType", displayType);
 		outState.putString("displayFilter", displayFilter);
 		super.onSaveInstanceState(outState);
 	}
 
 	/* create option menu */
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		inflater.inflate(R.menu.main, menu);
 		super.onCreateOptionsMenu(menu, inflater);
 	}
 
 	/* option item selected */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 
 		/* category */
 		case R.id.menuRegion:
 			CategoryDialog diag = new CategoryDialog();
 			diag.setListener(this);
 			diag.show(getFragmentManager(), "");
 			break;
 
 		/* contact */
 		case R.id.menuContact:
 			Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
 					"mailto", "centraloffice@worldsceunion.org", null));
 			startActivity(Intent.createChooser(intent,
 					getString(R.string.messageEMail)));
 			break;
 		}
 
 		return super.onOptionsItemSelected(item);
 	}
 
 	/* category selected */
 	@Override
 	public void onCategorySelected(String category) {
 		displayFilter = category;
 		refreshDisplay();
 	}
 
 	/* list item selected */
 	@SuppressLint("SimpleDateFormat")
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 
 		/* open url */
 		if (v.getTag() instanceof String) {
 			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse((String) v
 					.getTag()));
 			startActivity(intent);
 		}
 
 		/* open date */
 		else if (v.getTag() instanceof Date) {
 
 			/* open in fragment */
 			if (getActivity().findViewById(R.id.mainArticle) != null) {
 				ArticleFragment frag = new ArticleFragment();
 				Bundle args = new Bundle();
 				args.putString("date", new SimpleDateFormat(
 						NewsDatabase.DATE_FORMAT).format(v.getTag()));
 				frag.setArguments(args);
 				getFragmentManager().beginTransaction()
 						.replace(R.id.mainArticle, frag).commit();
 			}
 
 			/* open in activity */
 			else {
 				Intent intent = new Intent(v.getContext(),
 						ArticleActivity.class);
 				intent.putExtra("date", new SimpleDateFormat(
 						NewsDatabase.DATE_FORMAT).format(v.getTag()));
 				startActivity(intent);
 			}
 		}
 
 		super.onListItemClick(l, v, position, id);
 	}
 
 	@SuppressWarnings("deprecation")
 	@SuppressLint("NewApi")
 	public void refreshDisplay() {
 		final NewsListAdapter adp = (NewsListAdapter) getListAdapter();
 		final ImageView banner = (ImageView) getActivity().findViewById(
 				R.id.banner);
 
 		/* news */
 		if (displayType.equals("news")) {
 			adp.changeCursor(dbNews.getList());
 			if (displayFilter == null || displayFilter.equals(""))
 				adp.changeCursor(dbNews.getList());
 			else {
 				adp.changeCursor(dbNews.getList(NewsDatabase.COLUMN_CATEGORY
 						+ "=?", new String[] { displayFilter }));
 			}
 		}
 
 		/* events */
 		else if (displayType.equals("events")) {
 			adp.changeCursor(dbEvents.getList());
 			if (displayFilter == null || displayFilter.equals(""))
 				adp.changeCursor(dbEvents.getList());
 			else {
 				adp.changeCursor(dbEvents.getList(NewsDatabase.COLUMN_CATEGORY
 						+ "=?", new String[] { displayFilter }));
 			}
 		}
 
 		/* banner */
 		if (banner != null) {
 
 			/* banner image */
 			if (displayFilter.equals("africa"))
 				banner.setImageResource(R.drawable.banner_africa);
 			else if (displayFilter.equals("america"))
 				banner.setImageResource(R.drawable.banner_america);
 			else if (displayFilter.equals("asia"))
 				banner.setImageResource(R.drawable.banner_asia);
 			else if (displayFilter.equals("auspac"))
 				banner.setImageResource(R.drawable.banner_auspac);
 			else if (displayFilter.equals("europe"))
 				banner.setImageResource(R.drawable.banner_europe);
 			else if (banner != null)
 				banner.setImageResource(R.drawable.banner_wceu);
 
 			/* banner size */
 			int width = 0;
 			Display d = ((WindowManager) getActivity().getSystemService(
 					Context.WINDOW_SERVICE)).getDefaultDisplay();
 			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 				Point size = new Point();
 				d.getSize(size);
 				width = size.x;
 			} else {
 				width = d.getWidth();
 			}
 			banner.getLayoutParams().height = (int) ((double) width
 					/ (double) banner.getDrawable().getIntrinsicWidth() * (double) banner
 					.getDrawable().getIntrinsicHeight());
 		}
 	}
 
 	public void refreshData() {
 		new AsyncTask<Void, Void, Boolean>() {
 			@Override
 			protected Boolean doInBackground(Void... params) {
 				return RefreshHandler.refreshAll(getActivity(), dbNews,
 						dbEvents);
 			}
 
 			@Override
 			protected void onPostExecute(Boolean result) {
 
 				/* show error */
 				if (!result)
 					Toast.makeText(getActivity(),
 							getString(R.string.messageIOException),
 							Toast.LENGTH_LONG).show();
 
 				/* refresh listing */
 				refreshDisplay();
 				((PullToRefreshListView) getActivity().findViewById(
 						R.id.listNews)).onRefreshComplete();
 
 				super.onPostExecute(result);
 			}
 		}.execute();
 	}
 }
