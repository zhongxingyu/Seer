 package com.darkrockstudios.apps.tminus.fragments;
 
 import android.animation.Animator;
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.provider.CalendarContract;
 import android.provider.CalendarContract.Events;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ShareActionProvider;
 import android.widget.TextView;
 
 import com.android.volley.toolbox.ImageLoader;
 import com.android.volley.toolbox.NetworkImageView;
 import com.darkrockstudios.apps.tminus.R;
 import com.darkrockstudios.apps.tminus.R.id;
 import com.darkrockstudios.apps.tminus.RocketDetailUpdateService;
 import com.darkrockstudios.apps.tminus.TMinusApplication;
 import com.darkrockstudios.apps.tminus.database.RocketDetail;
 import com.darkrockstudios.apps.tminus.launchlibrary.Launch;
 import com.darkrockstudios.apps.tminus.loaders.LaunchLoader;
 import com.darkrockstudios.apps.tminus.loaders.LaunchLoader.Listener;
 import com.darkrockstudios.apps.tminus.loaders.RocketDetailLoader;
 import com.darkrockstudios.apps.tminus.misc.Preferences;
 import com.darkrockstudios.apps.tminus.misc.Utilities;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.concurrent.TimeUnit;
 
 /**
  * A fragment representing a single Launch detail screen.
  * This fragment is either contained in a {@link com.darkrockstudios.apps.tminus.LaunchListActivity}
  * in two-pane mode (on tablets) or a {@link com.darkrockstudios.apps.tminus.LaunchDetailActivity}
  * on handsets.
  */
 public class LaunchDetailFragment extends Fragment implements Listener, RocketDetailLoader.Listener, Utilities.ZoomAnimationHandler
 {
 	public static final String TAG =
 			LaunchDetailFragment.class.getSimpleName();
 	public static final  String ARG_ITEM_ID                 = "item_id";
 	private static final String LOCATION_FRAGMENT_TAG       = "LocationFragmentTag";
 	private static final String ROCKET_FRAGMENT_TAG         = "RocketFragmentTag";
 	private static final long   DISPLAY_COUNTDOWN_THRESHOLD = TimeUnit.DAYS.toMillis( 2 );
 	private ShareActionProvider        m_shareActionProvider;
 	private Launch                     m_launchItem;
 	private RocketDetail               m_rocketDetail;
 	private TimeReceiver               m_timeReceiver;
 	private NetworkImageView           m_rocketImage;
 	private NetworkImageView m_rocketImageExpanded;
 	private ViewGroup                  m_locationContainer;
 	private ViewGroup                  m_rocketContainer;
 	private View             m_containerView;
 	private View                       m_contentView;
 	private View                       m_progressBar;
 	private View                       m_countDownContainer;
 	private View                       m_rocketDetailButton;
 	private RocketDetailUpdateReceiver m_rocketDetailUpdateReceiver;
 
 	private Animator m_currentAnimator;
 	private int      m_shortAnimationDuration;
 
 	/**
 	 * Mandatory empty constructor for the fragment manager to instantiate the
 	 * fragment (e.g. upon screen orientation changes).
 	 */
 	public LaunchDetailFragment()
 	{
 	}
 
 	public static LaunchDetailFragment newInstance( int launchId )
 	{
 		Bundle arguments = new Bundle();
 		arguments.putInt( LaunchDetailFragment.ARG_ITEM_ID, launchId );
 		LaunchDetailFragment fragment = new LaunchDetailFragment();
 		fragment.setArguments( arguments );
 		return fragment;
 	}
 
 	@Override
 	public void onCreate( Bundle savedInstanceState )
 	{
 		super.onCreate( savedInstanceState );
 
 		setHasOptionsMenu( true );
 
 		m_shortAnimationDuration =
 				getResources().getInteger( android.R.integer.config_shortAnimTime );
 	}
 
 	@Override
 	public void onResume()
 	{
 		super.onResume();
 
 		handleCountDownContainer();
 	}
 
 	@Override
 	public void onAttach( Activity activity )
 	{
 		super.onAttach( activity );
 
 		m_timeReceiver = new TimeReceiver();
 		IntentFilter intentFilter = new IntentFilter( Intent.ACTION_TIME_TICK );
 
 		activity.registerReceiver( m_timeReceiver, intentFilter );
 
 		m_rocketDetailUpdateReceiver = new RocketDetailUpdateReceiver();
 		IntentFilter m_rocketDetailUpdateIntentFilter = new IntentFilter();
 		m_rocketDetailUpdateIntentFilter
 				.addAction( RocketDetailUpdateService.ACTION_ROCKET_DETAIL_UPDATED );
 		m_rocketDetailUpdateIntentFilter
 				.addAction( RocketDetailUpdateService.ACTION_ROCKET_DETAIL_UPDATE_FAILED );
 		activity.registerReceiver( m_rocketDetailUpdateReceiver, m_rocketDetailUpdateIntentFilter );
 	}
 
 	@Override
 	public void onDetach()
 	{
 		super.onDetach();
 
 		Activity activity = getActivity();
 		activity.unregisterReceiver( m_timeReceiver );
 
 		m_timeReceiver = null;
 
 		activity.unregisterReceiver( m_rocketDetailUpdateReceiver );
 		m_rocketDetailUpdateReceiver = null;
 	}
 
 	@Override
 	public View onCreateView( LayoutInflater inflater, ViewGroup container,
 	                          Bundle savedInstanceState )
 	{
 		View rootView = inflater.inflate( R.layout.fragment_launch_detail, container, false );
 
 		if( rootView != null )
 		{
 			m_containerView = rootView.findViewById( id.LAUNCHDETAIL_container_view );
 			m_contentView = rootView.findViewById( R.id.LAUNCHDETAIL_content_view );
 			m_progressBar = rootView.findViewById( R.id.progressBar );
 			m_countDownContainer =
 					rootView.findViewById( R.id.LAUNCHDETAIL_imminent_launch_container );
 			m_countDownContainer.setVisibility( View.GONE );
 			m_rocketDetailButton = rootView.findViewById( R.id.LAUNCHDETAIL_rocket_detail_button );
 			m_rocketImage =
 					(NetworkImageView) rootView.findViewById( R.id.LAUNCHDETAIL_mission_image );
 			m_rocketImageExpanded = (NetworkImageView) rootView
 					                                           .findViewById( R.id.LAUNCHDETAIL_expanded_rocket_image );
 			m_locationContainer =
 					(ViewGroup) rootView.findViewById( id.LAUNCHDETAIL_location_container );
 			m_rocketContainer =
 					(ViewGroup) rootView.findViewById( id.LAUNCHDETAIL_rocket_container );
 
 			m_rocketImage.setErrorImageResId( R.drawable.launch_detail_no_rocket_image );
 			m_rocketImage.setDefaultImageResId( R.drawable.launch_detail_no_rocket_image );
 
 			loadLaunch();
 		}
 
 		return rootView;
 	}
 
 	@Override
 	public void onCreateOptionsMenu( Menu menu, MenuInflater inflater )
 	{
 		inflater.inflate( R.menu.launch_detail, menu );
 
 		MenuItem item = menu.findItem( R.id.menu_item_share );
 		if( item != null )
 		{
 			m_shareActionProvider = (ShareActionProvider) item.getActionProvider();
 		}
 		updateShareIntent();
 
 		super.onCreateOptionsMenu( menu, inflater );
 	}
 
 	@Override
 	public boolean onOptionsItemSelected( MenuItem item )
 	{
 		final boolean handled;
 
 		switch( item.getItemId() )
 		{
 			case id.menu_item_add_to_calendar:
 				addLaunchToCalendar();
 				handled = true;
 				break;
 			default:
 				handled = super.onOptionsItemSelected( item );
 		}
 
 		return handled;
 	}
 
 	private void addLaunchToCalendar()
 	{
 		if( m_launchItem != null )
 		{
 			final String title = getString( R.string.CALENDAR_event_title, m_launchItem.name );
 			final String description =
 					getString( R.string.CALENDAR_event_description, m_launchItem.rocket.name,
 					           m_launchItem.rocket.configuration );
 
 			Intent intent = new Intent( Intent.ACTION_INSERT )
 					                .setData( Events.CONTENT_URI )
 					                .putExtra( CalendarContract.EXTRA_EVENT_BEGIN_TIME, m_launchItem.net.getTime() )
 					                .putExtra( CalendarContract.EXTRA_EVENT_END_TIME,
 					                           m_launchItem.windowend.getTime() )
 					                .putExtra( Events.TITLE, title )
 					                .putExtra( Events.DESCRIPTION, description )
 					                .putExtra( Events.EVENT_LOCATION, m_launchItem.location.name )
 					                .putExtra( Events.AVAILABILITY, Events.AVAILABILITY_BUSY );
 			startActivity( intent );
 		}
 	}
 
 	public int getLaunchId()
 	{
 		int launchId = -1;
 
 		final Bundle arguments = getArguments();
 		if( arguments != null && arguments.containsKey( ARG_ITEM_ID ) )
 		{
 			launchId = arguments.getInt( ARG_ITEM_ID );
 		}
 
 		return launchId;
 	}
 
 	private void showContent()
 	{
 		if( m_contentView != null && m_rocketImage != null && m_progressBar != null )
 		{
 			m_contentView.setVisibility( View.VISIBLE );
 			m_rocketImage.setVisibility( View.VISIBLE );
 			m_progressBar.setVisibility( View.GONE );
 		}
 	}
 
 	private void showLoading()
 	{
 		if( m_contentView != null && m_rocketImage != null && m_progressBar != null )
 		{
 			m_contentView.setVisibility( View.GONE );
 			m_rocketImage.setVisibility( View.GONE );
 			m_progressBar.setVisibility( View.VISIBLE );
 		}
 	}
 
 	private void updateViews()
 	{
 		if( m_launchItem != null && isAdded() )
 		{
 			final View rootView = getView();
 
 			final TextView name =
 					(TextView) rootView.findViewById( R.id.LAUNCHDETAIL_mission_name );
 			name.setText( m_launchItem.name );
 
 			final TextView description =
 					(TextView) rootView.findViewById( R.id.LAUNCHDETAIL_mission_description );
 			if( m_launchItem.mission != null )
 			{
 				description.setText( m_launchItem.mission.description );
 			}
 			else
 			{
 				description.setText( R.string.LAUNCHDETAIL_no_mission_details );
 			}
 
 			final TextView status = (TextView) rootView.findViewById( R.id.LAUNCHDETAIL_status );
 			status.setText( Utilities.getStatusText( m_launchItem, rootView.getContext() ) );
 
 			final TextView location =
 					(TextView) rootView.findViewById( R.id.LAUNCHDETAIL_location );
 			location.setText( m_launchItem.location.name );
 			int flagResourceId = Utilities
 					                     .getFlagResource( m_launchItem.location.locInfo.countrycode );
 			location.setCompoundDrawablesWithIntrinsicBounds( 0, 0, flagResourceId, 0 );
 
 			final TextView windowLabel =
 					(TextView) rootView.findViewById( R.id.LAUNCHDETAIL_window_length );
 			final TextView windowLength =
 					(TextView) rootView.findViewById( R.id.LAUNCHDETAIL_window_length );
 
 			if( m_launchItem.windowend != null )
 			{
 				final long windowLengthMs =
 						m_launchItem.windowend.getTime() - m_launchItem.windowstart.getTime();
 				windowLength.setText( Utilities.getFormattedTime( windowLengthMs ) );
 
 				windowLabel.setVisibility( View.VISIBLE );
 				windowLength.setVisibility( View.VISIBLE );
 			}
 			else
 			{
 				windowLabel.setVisibility( View.INVISIBLE );
 				windowLength.setVisibility( View.INVISIBLE );
 			}
 
 			final TextView rocketName =
 					(TextView) rootView.findViewById( id.LAUNCHDETAIL_rocket_name );
 			if( rocketName != null )
 			{
 				rocketName.setText( m_launchItem.rocket.name );
 			}
 
 			if( m_rocketDetail != null && m_rocketDetail.imageUrl != null )
 			{
 				ImageLoader imageLoader = new ImageLoader( TMinusApplication.getRequestQueue(),
 				                                           TMinusApplication.getBitmapCache() );
 				m_rocketImage.setImageUrl( m_rocketDetail.imageUrl, imageLoader );
 				m_rocketImageExpanded.setImageUrl( m_rocketDetail.imageUrl, imageLoader );
 			}
 
 			final TextView netView1 = (TextView) rootView.findViewById( R.id.launch_detail_net_1 );
 			final TextView netView2 = (TextView) rootView.findViewById( R.id.launch_detail_net_2 );
 			final TextView netView3 = (TextView) rootView.findViewById( R.id.launch_detail_net_3 );
 
 			SimpleDateFormat monthDay = new SimpleDateFormat( "MMM dd" );
 			SimpleDateFormat year = new SimpleDateFormat( "yyyy" );
 			SimpleDateFormat time = new SimpleDateFormat( "HH:mm" );
 
 			netView1.setText( monthDay.format( m_launchItem.net ) );
 			netView2.setText( year.format( m_launchItem.net ) );
 			netView3.setText( time.format( m_launchItem.net ) );
 
 			updateTimeViews();
 			handleCountDownContainer();
 		}
 	}
 
 	public void updateTimeViews()
 	{
 		if( m_launchItem != null )
 		{
 			final View rootView = getView();
 
 			final TextView timeRemaining =
 					(TextView) rootView.findViewById( R.id.LAUNCHDETAIL_time_remaining );
 			final Date now = new Date();
 
 			final long totalMsLeft = m_launchItem.windowstart.getTime() - now.getTime();
 			timeRemaining.setText( Utilities.getFormattedTime( totalMsLeft ) );
 
 			handleCountDownContainer();
 		}
 	}
 
 	public void loadLaunch()
 	{
 		final int launchId = getLaunchId();
 		if( launchId >= 0 )
 		{
 			showLoading();
 
 			LaunchLoader loader = new LaunchLoader( getActivity(), this );
 			loader.execute( launchId );
 		}
 	}
 
 	private void handleCountDownContainer()
 	{
 		boolean alwaysShow = false;
 
 		final Activity activity = getActivity();
 		if( activity != null )
 		{
 			final SharedPreferences preferences =
 					PreferenceManager.getDefaultSharedPreferences( activity );
 			alwaysShow = preferences.getBoolean( Preferences.KEY_ALWAYS_SHOW_COUNT_DOWN, false );
 		}
 
 		if( m_launchItem != null )
 		{
 			final Date thresholdDate =
 					new Date( m_launchItem.net.getTime() - DISPLAY_COUNTDOWN_THRESHOLD );
 			if( thresholdDate.before( new Date() ) || alwaysShow )
 			{
 				m_countDownContainer.setVisibility( View.VISIBLE );
 			}
 			else
 			{
 				m_countDownContainer.setVisibility( View.GONE );
 			}
 		}
 	}
 
 	private void updateShareIntent()
 	{
		if( m_launchItem != null && m_shareActionProvider != null && isAdded() )
 		{
 			Intent intent = new Intent( android.content.Intent.ACTION_SEND );
 			intent.setType( "text/plain" );
 			intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET );
 
 			// Add data to the intent, the receiving app will decide what to do with it.
 			intent.putExtra( Intent.EXTRA_SUBJECT, "Upcoming Space Launch: " + m_launchItem.name );
 			intent.putExtra( Intent.EXTRA_TEXT, generateShareBody() );
 
 			m_shareActionProvider.setShareIntent( intent );
 		}
 	}
 
 	private String generateShareBody()
 	{
 		String body = "";
 
		if( m_launchItem != null && isAdded() )
 		{
 			final String missionDescription;
 			if( m_launchItem.mission != null )
 			{
 				missionDescription = m_launchItem.mission.description;
 			}
 			else
 			{
 				missionDescription = getString( R.string.LAUNCHDETAIL_share_no_mission );
 			}
 
 			body = getString( R.string.LAUNCHDETAIL_share_details, missionDescription,
 			                  m_launchItem.location.name, m_launchItem.net );
 		}
 
 		return body;
 	}
 
 	@Override
 	public void launchLoaded( Launch launch )
 	{
 		m_launchItem = launch;
 
 		if( m_rocketDetailButton != null )
 		{
 			m_rocketDetailButton.setTag( m_launchItem.rocket );
 		}
 
 		loadRocketDetails();
 
 		updateViews();
 		updateShareIntent();
 		showContent();
 
 		// TODO: Better tablet layout detection
 		final boolean isTabletLayout = (m_rocketContainer != null);
 
 		if( m_locationContainer != null )
 		{
 			LocationDetailFragment locationDetailFragment = LocationDetailFragment
 					                                                .newInstance( m_launchItem.location.id, isTabletLayout );
 			getChildFragmentManager().beginTransaction()
 					.add( R.id.LAUNCHDETAIL_location_container, locationDetailFragment,
 					      LOCATION_FRAGMENT_TAG )
 					.commit();
 		}
 
 		if( m_rocketContainer != null )
 		{
 			RocketDetailFragment rocketDetailFragment =
 					RocketDetailFragment.newInstance( m_launchItem.rocket.id );
 			getChildFragmentManager().beginTransaction()
 					.add( R.id.LAUNCHDETAIL_rocket_container, rocketDetailFragment,
 					      ROCKET_FRAGMENT_TAG ).commit();
 		}
 	}
 
 	private void loadRocketDetails()
 	{
 		Activity activity = getActivity();
 		if( activity != null && m_launchItem != null )
 		{
 			RocketDetailLoader detailLoader = new RocketDetailLoader( activity, this );
 			detailLoader.execute( m_launchItem.rocket.id );
 		}
 	}
 
 	@Override
 	public void rocketDetailLoaded( RocketDetail rocketDetail )
 	{
 		m_rocketDetail = rocketDetail;
 
 		updateViews();
 	}
 
 	@Override
 	public void rocketDetailMissing( int rocketId )
 	{
 		final Activity activity = getActivity();
 		if( activity != null && m_launchItem != null )
 		{
 			fetchRocketDetails();
 		}
 	}
 
 	private void fetchRocketDetails()
 	{
 		final Activity activity = getActivity();
 		if( activity != null && m_launchItem != null )
 		{
 			Intent intent = new Intent( activity, RocketDetailUpdateService.class );
 			intent.putExtra( RocketDetailUpdateService.EXTRA_ROCKET_ID, m_launchItem.rocket.id );
 			activity.startService( intent );
 		}
 	}
 
 	public void zoomRocketImage()
 	{
 		// If we don't have rocket info yet, don't bother zooming
 		if( m_rocketDetail != null && m_rocketDetail.imageUrl != null )
 		{
 			Utilities.zoomImage( m_rocketImage, m_rocketImageExpanded, m_containerView, this,
 			                     m_shortAnimationDuration );
 		}
 	}
 
 	@Override
 	public void setCurrentAnimator( Animator animator )
 	{
 		m_currentAnimator = animator;
 	}
 
 	@Override
 	public Animator getCurrentAnimator()
 	{
 		return m_currentAnimator;
 	}
 
 	private class TimeReceiver extends BroadcastReceiver
 	{
 		@Override
 		public void onReceive( Context context, Intent intent )
 		{
 			updateTimeViews();
 		}
 	}
 
 	private class RocketDetailUpdateReceiver extends BroadcastReceiver
 	{
 		@Override
 		public void onReceive( Context context, Intent intent )
 		{
 			final Activity activity = getActivity();
 			if( activity != null && isAdded() )
 			{
 				if( RocketDetailUpdateService.ACTION_ROCKET_DETAIL_UPDATED
 				                             .equals( intent.getAction() ) )
 				{
 					Log.i( TAG,
 					       "Received Rocket Detail update SUCCESS broadcast, will update the UI now." );
 
 					final int rocketId =
 							intent.getIntExtra( RocketDetailUpdateService.EXTRA_ROCKET_ID, -1 );
 					if( rocketId > 0 )
 					{
 						Log.i( TAG, "Rocket Detail fetch completely successfully for rocket id: " +
 						            rocketId );
 
 						RocketDetailLoader detailLoader =
 								new RocketDetailLoader( activity, LaunchDetailFragment.this );
 						detailLoader.execute( rocketId );
 
 						activity.setProgressBarIndeterminateVisibility( false );
 					}
 
 				}
 				else if( RocketDetailUpdateService.ACTION_ROCKET_DETAIL_UPDATE_FAILED
 				                                  .equals( intent.getAction() ) )
 				{
 					Log.w( TAG, "Received Rocket Detail update FAILURE broadcast." );
 
 					final int rocketId =
 							intent.getIntExtra( RocketDetailUpdateService.EXTRA_ROCKET_ID, -1 );
 					if( rocketId > 0 )
 					{
 						Log.w( TAG,
 						       "Rocket Detail fetch completely failed for rocket id: " + rocketId );
 					}
 
 					// TODO: set failure image here
 
 					activity.setProgressBarIndeterminateVisibility( false );
 				}
 			}
 		}
 	}
 }
