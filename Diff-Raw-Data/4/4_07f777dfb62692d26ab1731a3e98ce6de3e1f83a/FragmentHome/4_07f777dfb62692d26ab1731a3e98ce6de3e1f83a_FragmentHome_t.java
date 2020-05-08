 package com.pk.addits.fragment;
 
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Handler.Callback;
 import android.os.Message;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AbsListView;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.FrameLayout;
 import android.widget.GridView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.SearchView;
 
 import com.pk.addits.R;
 import com.pk.addits.activity.ActivityMain;
 import com.pk.addits.adapter.FeedAdapter;
 import com.pk.addits.data.Data;
 import com.pk.addits.model.Article;
 
 public class FragmentHome extends Fragment
 {
 	private SharedPreferences prefs;
 	private boolean adsEnabled;
 	private LinearLayout ad;
 	
 	static GridView grid;
 	static ListView list;
 	static FrameLayout frame;
 	static FeedAdapter adapter;
 	static Context cntxt;
 	static int scrollPosition;
 	static int topOffset;
 	
 	static int currentSlide;
 	static Timer timer;
 	Handler timeHandler;
 	long startTime;
 	
 	static Fragment fragSlide;
 	static FragmentManager fm;
 	static Integer currentSlideID;
 	
 	private static boolean isLandscape;
 	
 	private static int returnCount; // To prevent a StackOverflowError
 	
 	
 	public static FragmentHome newInstance(int lastScrollPosition, int lastTopOffset)
 	{
 		FragmentHome f = new FragmentHome();
 		Bundle bdl = new Bundle();
 		
 		bdl.putInt("Last Scroll Position", lastScrollPosition);
 		bdl.putInt("Last Top Offset", lastTopOffset);
 		f.setArguments(bdl);
 		
 		return f;
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
 	{
 		View view = inflater.inflate(R.layout.fragment_home, container, false);
 		setHasOptionsMenu(true);
 		
 		prefs = getActivity().getSharedPreferences(Data.PREFS_TAG, 0);
 		adsEnabled = prefs.getBoolean(Data.PREF_TAG_ADS_ENABLED, true);
 		cntxt = getActivity();
 		isLandscape = getActivity().getResources().getBoolean(R.bool.isLandscape);
 		adapter = new FeedAdapter(getActivity(), ActivityMain.articleList);
 		ad = (LinearLayout) view.findViewById(R.id.ad);
 		 
 		
 		if (isLandscape)
 		{
 			grid = (GridView) view.findViewById(R.id.GridView);
 			grid.setAdapter(adapter);
 		}
 		else
 		{
 			list = (ListView) view.findViewById(R.id.ListView);
 			list.setDividerHeight(0);
 			View header = (View) inflater.inflate(R.layout.header, list, false);
 			AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, Data.getHeightByPercent(getActivity(), 0.35));
 			// list.setParallaxHeader(header);
 			list.addHeaderView(header, null, true);
 			header.setLayoutParams(layoutParams);
 			list.setAdapter(adapter);
		}
 		
 		currentSlide = 1;
 		currentSlideID = 1;
 		scrollPosition = 0;
 		topOffset = 0;
 		returnCount = 0;
 		
 		Bundle args = getArguments();
 		if (args != null)
 		{
 			scrollPosition = args.getInt("Last Scroll Position", 0);
 			topOffset = args.getInt("Last Top Offset", 0);
 		}
 		
 		if (!adsEnabled)
 			ad.setVisibility(View.GONE);
 		
 		return view;
 	}
 	
 	@Override
 	public void onStart()
 	{
 		super.onStart();
 		fm = getChildFragmentManager();
 		
 		timer = new Timer();
 		timeHandler = new Handler(new Callback()
 		{
 			@Override
 			public boolean handleMessage(Message msg)
 			{
 				if (!isLandscape && ActivityMain.articleList != null)
 					populateSlide();
 				
 				return false;
 			}
 		});
 		updateState();
 		
 		if (!isLandscape)
 		{
 			timer.schedule(new firstTask(), 5000, 7000);
 			list.setOnItemClickListener(new OnItemClickListener()
 			{
 				@Override
 				public void onItemClick(AdapterView<?> arg0, View view, int position, long index)
 				{
 					if (position > 0)
 					{
 						Article article = ActivityMain.articleList.get(position - 1);
 						ActivityMain.callArticle(article, list.getFirstVisiblePosition(), (list.getChildAt(0) == null) ? 0 : list.getChildAt(0).getTop());
 					}
 				}
 			});
 		}
 		else
 		{
 			grid.setOnItemClickListener(new OnItemClickListener()
 			{
 				@Override
 				public void onItemClick(AdapterView<?> arg0, View view, int position, long index)
 				{
 					Article article = ActivityMain.articleList.get(position);
 					ActivityMain.callArticle(article, 0, 0);
 				}
 			});
 		}
 	}
 	
 	@Override
 	public void onPause()
 	{
 		super.onPause();
 		
 		timer.cancel();
 		timer.purge();
 	}
 	
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
 	{
 		menu.clear();
 		inflater.inflate(R.menu.home, menu);
 		
 		SearchView searchView = (SearchView) menu.findItem(R.id.Search_Label).getActionView();
 	
 		
 		
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		switch (item.getItemId())
 		{
 			case R.id.Website_Label:
 				Intent i = new Intent(Intent.ACTION_VIEW);
 				i.setData(Uri.parse(Data.MAIN_URL));
 				startActivity(i);
 				return true;
 			case R.id.Settings_Label:
 				ActivityMain.callSettings();
 				return true;
 			default:
 				
 				return super.onOptionsItemSelected(item);
 		}
 	}
 	
 	public static void updateState()
 	{
 		if (ActivityMain.articleList != null)
 		{
 			adapter.notifyDataSetChanged();
 			if (!isLandscape)
 			{
 				list.setSelectionFromTop(scrollPosition, topOffset);
 				populateSlide();
 			}
 		}
 	}
 	
 	public static void populateSlide()
 	{
 		int sID = 0;
 		String sTitle = "";
 		String sAuthor = "";
 		String sDate = "";
 		String sImage = "";
 		String sCategory = "";
 		
 		if (ActivityMain.articleList != null && ActivityMain.articleList.size() > 0)
 		{
 			Random generator = new Random();
 			int r = generator.nextInt(ActivityMain.articleList.size());
 			sID = r;
 			sTitle = ActivityMain.articleList.get(r).getTitle();
 			sAuthor = ActivityMain.articleList.get(r).getAuthor();
 			sDate = Data.parseDate(cntxt, ActivityMain.articleList.get(r).getDate());
 			sImage = ActivityMain.articleList.get(r).getImage();
 			sCategory = ActivityMain.articleList.get(r).getCategory();
 			currentSlideID = ActivityMain.articleList.get(r).getID();
 			
 			if (sImage.length() < 1 && returnCount < 5)
 			{
 				returnCount++;
 				populateSlide();
 				return;
 			}
 		}
 		
 		if (!sCategory.equalsIgnoreCase("DAILY SAVER"))
 			fragSlide = FragmentSlider.newInstance(sID, sTitle, sAuthor, sDate, sImage);
 		else if (returnCount < 5)
 		{
 			returnCount++;
 			populateSlide();
 			return;
 		}
 		
 		returnCount = 0;
 		
 		if (!isLandscape)
 		{
 			FragmentTransaction trans = fm.beginTransaction();
 			trans.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_out, R.anim.fade_in);
 			trans.replace(R.id.slider_content, fragSlide);
 			trans.commit();
 		}
 	}
 	
 	class firstTask extends TimerTask
 	{
 		@Override
 		public void run()
 		{
 			timeHandler.sendEmptyMessage(0);
 		}
 	};
 }
