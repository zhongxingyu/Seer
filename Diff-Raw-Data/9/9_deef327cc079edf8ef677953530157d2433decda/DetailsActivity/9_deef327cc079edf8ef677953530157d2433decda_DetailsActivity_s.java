 package rs.pedjaapps.tvshowtracker;
 
 import android.app.*;
 import android.content.*;
 import android.graphics.*;
 import android.net.*;
 import android.os.*;
 import android.preference.*;
 import android.support.v4.app.*;
 import android.support.v4.view.*;
 import android.view.*;
 import android.widget.*;
 import com.nostra13.universalimageloader.core.*;
 import java.util.*;
 import rs.pedjaapps.tvshowtracker.adapter.*;
 import rs.pedjaapps.tvshowtracker.model.*;
 import rs.pedjaapps.tvshowtracker.utils.*;
 
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 
 public class DetailsActivity extends BaseActivity
 {
 
 	SectionsPagerAdapter mSectionsPagerAdapter;
 	ViewPager mViewPager;
 	static int seriesId;
 	static String profile;
 	static List<Actor> actors;
 	static List<EpisodeItem> episodes;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		setContentView(R.layout.activity_details);
 
 		// Show the Up button in the action bar.
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 
 		seriesId = getIntent().getIntExtra("seriesId", 0);
 		profile = getIntent().getStringExtra("profile");
 		// Create the adapter that will return a fragment for each of the three
 		// primary sections of the app.
 		new Loader().execute();
 
 	}
 
 	public class Loader extends AsyncTask<String, Void, Void>
 	{
 
 		@Override
 		protected Void doInBackground(String... args)
 		{
 			actors = db.getAllActors(seriesId + "", profile);
 			episodes = db.getAllEpisodes(seriesId + "", profile);
			Collections.reverse(episodes);
 			return null;
 		}
 
 		@Override
 		protected void onPreExecute()
 		{
 			setProgressBarIndeterminateVisibility(true);
 		}
 
 		@Override
 		protected void onPostExecute(Void result)
 		{
 			setProgressBarIndeterminateVisibility(false);
 			setupViewPager();
 		}
 	}
 	
 	private void setupViewPager()
 	{
 		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
 		mViewPager = (ViewPager) findViewById(R.id.pager);
 		mViewPager.setAdapter(mSectionsPagerAdapter);
 		new Handler().post(new Runnable(){
 
 				public void run()
 				{
 					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(DetailsActivity.this);
 					int page = Integer.parseInt(prefs.getString("details_def_page", "1"));
 					mViewPager.setCurrentItem(page, true);
 				}
 			});
 		mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
 			{
 
 				@Override
 				public void onPageScrollStateChanged(int arg0)
 				{
 					// TODO Auto-generated method stub
 
 				}
 
 				@Override
 				public void onPageScrolled(int arg0, float arg1, int arg2)
 				{
 					// TODO Auto-generated method stub
 
 				}
 
 				@Override
 				public void onPageSelected(int position)
 				{
 					if (position == 1)
 					{
 						OverviewFragment.setProgress();
 					}
 
 				}
 			});
 			//mSectionsPagerAdapter.notifyDataSetChanged();
 			//mViewPager.invalidate();
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 
 		menu.add(0, 0, 0, "Download Banner").setShowAsAction(
 				MenuItem.SHOW_AS_ACTION_NEVER);
 		/*
 		 * menu.add(0, 1, 1, "Download Header")
 		 * .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
 		 */
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		switch (item.getItemId())
 		{
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
 		case 0:
 			startActivity(new Intent(this, BannerActivity.class)
 					.putExtra("seriesId", seriesId + "")
 					.putExtra("type", "banner").putExtra("profile", profile));
 			return true;
 		case 1:
 			startActivity(new Intent(this, BannerActivity.class).putExtra(
 					"seriesId", seriesId + "").putExtra("type", "fanart"));
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	/**
 	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 	 * one of the sections/tabs/pages.
 	 */
 	public class SectionsPagerAdapter extends FragmentPagerAdapter
 	{
 
 		public SectionsPagerAdapter(FragmentManager fm)
 		{
 			super(fm);
 		}
 
 		@Override
 		public Fragment getItem(int position)
 		{
 			// getItem is called to instantiate the fragment for the given page.
 			// Return a DummySectionFragment (defined as a static inner class
 			// below) with the page number as its lone argument.
 			Fragment fragment = null;
 			switch (position)
 			{
 			case 0:
 				fragment = new ActorsFragment();
 				break;
 			case 1:
 				fragment = new OverviewFragment();
 				break;
 			case 2:
 				fragment = new EpisodesFragment();
 				break;
 			}
 			return fragment;
 		}
 
 		@Override
 		public int getCount()
 		{
 			// Show 3 total pages.
 			return 3;
 		}
 
 		@Override
 		public CharSequence getPageTitle(int position)
 		{
 			Locale l = Locale.getDefault();
 			switch (position)
 			{
 			case 0:
 				return getString(R.string.title_actors).toUpperCase(l);
 			case 1:
 				return getString(R.string.title_overview).toUpperCase(l);
 			case 2:
 				return getString(R.string.title_episodes).toUpperCase(l);
 			}
 			return null;
 		}
 	}
 
 	public static class ActorsFragment extends Fragment
 	{
 
 		public static ActorsAdapter adapter;
 
 		public ActorsFragment()
 		{
 		}
 
 		@Override
 		public View onCreateView(LayoutInflater inflater, ViewGroup container,
 				Bundle savedInstanceState)
 		{
 			View rootView = inflater.inflate(R.layout.details_actors,
 					container, false);
 			adapter = new ActorsAdapter(this.getActivity(),
 					R.layout.details_actors_row);
 			ListView list = (ListView) rootView.findViewById(R.id.list);
 			for (Actor a : actors)
 			{
 
 				adapter.add(new Actor(a.getActorId(), a.getName(), a.getRole(),
 						a.getImage(), profile, seriesId+""));
 
 			}
 
 			list.setAdapter(adapter);
 
 			return rootView;
 		}
 	}
 
 	public static class EpisodesFragment extends Fragment
 	{
 
 		public static EpisodesAdapter adapter;
 
 		public EpisodesFragment()
 		{
 		}
 
 		@Override
 		public View onCreateView(LayoutInflater inflater, ViewGroup container,
 				Bundle savedInstanceState)
 		{
 			View rootView = inflater.inflate(R.layout.details_episodes,
 					container, false);
 			adapter = new EpisodesAdapter(this.getActivity(), seriesId + "", db);
 			ListView list = (ListView) rootView.findViewById(R.id.list);
 			
 			List<Integer> seasons = new ArrayList<Integer>();
 			for (EpisodeItem i : episodes)
 			{
 				if (i.getSeason() != 0)
 				{
 					if (!seasons.contains(i.getSeason()))
 					{
 						adapter.add(new EpisodeSection(i.getSeason()));
 						seasons.add(i.getSeason());
 					}
 					adapter.add(new EpisodeItem(i.getEpisodeName(), i
 							.getEpisode(), i.getSeason(), i.getFirstAired(), i
 							.getImdbId(), i.getOverview(), i.getRating(), i
 							.isWatched(), i.getEpisodeId(), profile, seriesId+""));
 				}
 			}
 
 			list.setAdapter(adapter);
 			list.setOnItemClickListener(new AdapterView.OnItemClickListener()
 			{
 
 				@Override
 				public void onItemClick(AdapterView<?> arg0, View arg1,
 						int position, long arg3)
 				{
 
 					if (adapter.getItem(position) instanceof EpisodeItem)
 					{
 						AlertDialog.Builder builder = new AlertDialog.Builder(
 								getActivity());
 
 						builder.setTitle(((EpisodeItem) adapter
 								.getItem(position)).getEpisodeName());
 						String plot = ((EpisodeItem) adapter
 							.getItem(position)).getOverview();
 						builder.setMessage(plot.length() > 0 ? plot : "Plot not available");
 
 						builder.setPositiveButton(
 								getResources().getString(android.R.string.ok),
 								new DialogInterface.OnClickListener()
 								{
 									@Override
 									public void onClick(DialogInterface dialog,
 											int which)
 									{
 
 									}
 								});
 
 						AlertDialog alert = builder.create();
 
 						alert.show();
 					}
 				}
 
 			});
 			return rootView;
 		}
 	}
 
 	public static class OverviewFragment extends Fragment
 	{
 		static ProgressBar prgWatched;
 		static TextView watchedText;
 
 		public OverviewFragment()
 		{
 		}
 
 		private static void setProgress()
 		{
 			final List<EpisodeItem> episodes = db.getAllEpisodes(seriesId + "",
 					profile);
 			int episodeCount = db.getEpisodesCount(seriesId + "", profile);
 			int watched = 0;
 			for (EpisodeItem e : episodes)
 			{
 				if (e.isWatched())
 				{
 					watched++;
 				}
 			}
 			prgWatched.setProgress((int) ((double) watched
 					/ (double) episodeCount * 100.0));
 			watchedText.setText(watched + "/" + episodeCount);
 		}
 
 		@Override
 		public View onCreateView(LayoutInflater inflater, ViewGroup container,
 				Bundle savedInstanceState)
 		{
 			View rootView = inflater.inflate(R.layout.datails_overview,
 					container, false);
 			ImageView header = (ImageView) rootView
 					.findViewById(R.id.imgSeriesHeader);
 			TextView name = (TextView) rootView
 					.findViewById(R.id.txtSeriesName);
 			TextView status = (TextView) rootView
 					.findViewById(R.id.txtSeriesStatus);
 			TextView rating = (TextView) rootView
 					.findViewById(R.id.txtSeriesRating);
 			prgWatched = (ProgressBar) rootView.findViewById(R.id.pgrWatched);
 			watchedText = (TextView) rootView.findViewById(R.id.txtWatched);
 			TextView lastAiredHeader = (TextView) rootView
 					.findViewById(R.id.txtLastAired);
 			TextView lastAiredTitle = (TextView) rootView
 					.findViewById(R.id.txtLastAiredTitle);
 			TextView lastAiredEpisode = (TextView) rootView
 					.findViewById(R.id.txtLastAiredEpisodeNumber);
 			TextView nextTitle = (TextView) rootView
 					.findViewById(R.id.txtNextEpisodeTitle);
 			TextView nextHeader = (TextView) rootView
 					.findViewById(R.id.txtUpcomingEpisodes);
 			TextView nextEpisode = (TextView) rootView
 					.findViewById(R.id.txtNextEpisodeNumber);
 			TextView showEnded = (TextView) rootView
 					.findViewById(R.id.txtShowEnded);
 			TextView summary = (TextView) rootView
 					.findViewById(R.id.txtSummary);
 			Button imdb = (Button) rootView.findViewById(R.id.btnOpenImdb);
 			RelativeLayout lastAiredLayout = (RelativeLayout) rootView
 					.findViewById(R.id.rllHeader1);
 			RelativeLayout nextLayout = (RelativeLayout) rootView
 					.findViewById(R.id.rllHeader2);
 			final Show s = db.getShow(seriesId + "", profile);
 			getActivity().getActionBar().setTitle(
 					s.getSeriesName());
 			getActivity().getActionBar().setSubtitle(
 					s.getNetwork());
 			final List<EpisodeItem> episodes = db.getAllEpisodes(seriesId + "",
 					profile);
 			DisplayImageOptions options = new DisplayImageOptions.Builder()
 					.showStubImage(R.drawable.noimage_large)
 					.showImageForEmptyUri(R.drawable.noimage_large)
 					.showImageOnFail(R.drawable.noimage_large).cacheInMemory()
 					.bitmapConfig(Bitmap.Config.RGB_565).build();
 			ImageLoader imageLoader = ImageLoader.getInstance();
 			imageLoader
 					.displayImage("file://" + s.getFanart(), header, options);
 
 			name.setText(s.getSeriesName());
 			status.setText(s.getStatus().toUpperCase());
 			rating.setText(s.getRating() + "");
 			summary.setText(s.getOverview());
 
 			setProgress();
 			final int lePos = getLastAiredEpisodePosition(episodes);
 			int nePos = getNextEpisodePosition(episodes);
 
 			if (lePos != -1)
 			{
 				lastAiredTitle.setText(episodes.get(lePos).getEpisodeName());
 				lastAiredEpisode.setText(EpisodesAdapter.episode(episodes
 						.get(lePos))[0]);
 			}
 			else
 			{
 				lastAiredHeader.setVisibility(View.GONE);
 				lastAiredLayout.setVisibility(View.GONE);
 			}
 
 			if (nePos != -1)
 			{
 				nextTitle.setText(episodes.get(nePos).getEpisodeName());
 				nextEpisode
 						.setText(EpisodesAdapter.episode(episodes.get(nePos))[0]);
 			}
 			else
 			{
 				nextHeader.setVisibility(View.GONE);
 				nextLayout.setVisibility(View.GONE);
 			}
 			if (s.getStatus().equals("Ended"))
 			{
 				showEnded.setVisibility(View.VISIBLE);
 			}
 			imdb.setOnClickListener(new View.OnClickListener()
 			{
 
 				@Override
 				public void onClick(View v)
 				{
 					Uri uri = Uri.parse("http://www.imdb.com/title/"
 							+ s.getImdbId());
 					Intent intent = new Intent(Intent.ACTION_VIEW, uri);
 					startActivity(intent);
 
 				}
 			});
 			return rootView;
 		}
 	}
 
 	private static int getLastAiredEpisodePosition(
 			List<EpisodeItem> episodeItems)
 	{
 		int n = episodeItems.size() - 1;
 		for (int i = episodeItems.size() - 1; i > 0; i--)
 		{
 			// for(int i = 0; i<episodeItems.size(); i++){
 			try
 			{
 				Date firstAired = Constants.df.parse(episodeItems.get(i)
 						.getFirstAired());
 				if (new Date().after(firstAired))
 				{
 					return n;
 				}
 				else
 				{
 					n--;
 				}
 
 			}
 			catch (Exception ex)
 			{
 				n--;
 			}
 		}
 		return -1;
 	}
 
 	private static int getNextEpisodePosition(List<EpisodeItem> episodeItems)
 	{
 		int n = 0;
 
 		for (EpisodeItem episodeItem : episodeItems)
 		{
 			try
 			{
 				Date firstAired = Constants.df.parse(episodeItem
 						.getFirstAired());
 				if (new Date().before(firstAired)
 						|| (new Date().getTime() / (1000 * 60 * 60 * 24)) == (firstAired
 								.getTime() / (1000 * 60 * 60 * 24)))
 				{
 					return n;
 				}
 				else
 				{
 					n++;
 				}
 
 			}
 			catch (Exception ex)
 			{
 				n++;
 			}
 		}
 		return -1;
 	}
 	
 	public void onDestroy()
 	{
 		super.onDestroy();
 	}
 }
