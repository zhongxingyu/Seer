 package cc.rainwave.android;
 
 import cc.rainwave.android.adapters.ElectionListAdapter;
 import cc.rainwave.android.adapters.StationListAdapter;
 import cc.rainwave.android.api.Session;
 import cc.rainwave.android.api.types.RainwaveException;
 import cc.rainwave.android.api.types.RainwaveResponse;
 import cc.rainwave.android.api.types.RatingResult;
 import cc.rainwave.android.api.types.Song;
 import cc.rainwave.android.api.types.Station;
 import cc.rainwave.android.views.HorizontalRatingBar;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.RatingBar;
 import android.widget.SlidingDrawer;
 import android.widget.TextView;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 
 import com.android.music.TouchInterceptor;
 import com.google.android.apps.iosched.ui.widget.Workspace;
 
 /**
  * This is the primary activity for this application. It announces
  * which song is playing, handles ratings, and also elections.
  * @author pkilgo
  *
  */
 public class NowPlayingActivity extends Activity {
     /** Debug tag */
 	private static final String TAG = "NowPlaying";
 	
 	/** This is the last response from the last schedule sync */
 	private RainwaveResponse mOrganizer;
 	
 	/** This manages our connection with the Rainwave server */
 	private Session mSession;
 	
 	/** AsyncTask for schedule syncs */
 	private FetchInfo mFetchInfo;
 	
 	/** AsyncTask for song ratings */
 	private RateTask mRateTask;
 	
 	/** AsycnTask for song timer */
 	private SongCountdownTask mSongCountdownTask;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setup();
         setContentView(R.layout.activity_main);
         setListeners();
     }
     
     @Override
     public void onStart() {
         super.onStart();
     }
     
     /**
      * Our strategy here is to attempt to re-initialize the
      * app as much as possible. This helps us to catch preference
      * changes, and to not have lingering song data lying around.
      */
     @Override
     public void onResume() {
         super.onResume();
         initializeSession();
         initSchedules();
     }
 
     /**
      * We also want to stop our threads as much as possible, as they
      * should solely run in the foreground.
      */
     public void onPause() {
     	super.onPause();
     	stopTasks();
     }
     
     public void onStop() {
     	super.onStop();
     }
     
     public void onDestroy() {
     	super.onDestroy();
     }
 
     /**
      * Dialog manufacturer.
      */
     public Dialog onCreateDialog(int id) {
     	AlertDialog.Builder builder = new AlertDialog.Builder(this);
     	
     	switch(id) {
     	    
     	case DIALOG_STATION_PICKER:
     		Station stations[] = mOrganizer.getStations();
     		
     		final ListView listView = new ListView(this);
     		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 				@Override
 				public void onItemClick(AdapterView<?> parent, View view,
 						int index, long id) {
 					Station s = (Station) listView.getItemAtPosition(index);
 					mSession.setStation(s.id);
 					NowPlayingActivity.this.dismissDialog(DIALOG_STATION_PICKER);
 					refresh();
 				}
 			});
     		
     		listView.setAdapter(new StationListAdapter(this, stations));
     		
     		return builder.setTitle(R.string.label_pickStation)
     			.setNegativeButton(R.string.label_cancel, null)
     			.setView(listView)
     			.create();
     		
     	default:
     	    // Assume the number must be a string resource id.
     		return builder.setTitle(R.string.label_error)
     				.setMessage(id)
     				.setPositiveButton(R.string.label_ok, null)
     				.create();
     	}
     }
     
     private void setup() {
     	getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
     	Rainwave.forceCompatibility(this);
     }
     
     /**
      * Sets up listeners for this activity.
      */
     private void setListeners() {
     	// The rating dialog should show up if the Song rating view is clicked.    	
     	findViewById(R.id.np_songRating).setOnTouchListener(
     	new OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent e) {
 				Workspace w = (Workspace) findViewById(R.id.np_workspace);
				HorizontalRatingBar b = (HorizontalRatingBar) findViewById(R.id.np_songRating);
 				
 				if(mOrganizer == null || !mOrganizer.isTunedIn() || !mSession.isAuthenticated()) {
 					if(e.getAction() == MotionEvent.ACTION_DOWN) {
 						w.lockCurrentScreen();
						b.setLabel(R.string.msg_tuneInFirst);
 					}
 					else if(e.getAction() == MotionEvent.ACTION_UP) {
 						w.unlockCurrentScreen();
						b.setLabel(R.string.label_song);
 					}
 					return true;
 				}
 				
 				
 				HorizontalRatingBar hrb = (HorizontalRatingBar) v;
 				switch(e.getAction()) {
 				case MotionEvent.ACTION_DOWN:
 					w.lockCurrentScreen();
 				case MotionEvent.ACTION_MOVE:
 				case MotionEvent.ACTION_UP:
 					float rating = hrb.snapPositionToMinorIncrement(e.getX());
 					rating = Math.max(1.0f, Math.min(rating, 5.0f));
 					hrb.setPrimaryValue(rating);
 					
 					if(e.getAction() == MotionEvent.ACTION_UP) {
 						w.unlockCurrentScreen();
 						RateTask t = new RateTask();
 						Song s = mOrganizer.getCurrentSong();
 						t.execute(s.song_id, rating);
 					}
 				}
 				return true;
 			}
     	});
     	
 
     	final ListView election = (ListView) findViewById(R.id.np_electionList);
     	election.setOnItemClickListener(new AdapterView.OnItemClickListener() {
     		public void onItemClick(AdapterView parent, View v, int i, long id) {
     			if(mOrganizer.isTunedIn() && mSession.isAuthenticated()) {
     				((ElectionListAdapter) election.getAdapter()).startCountdown(i);
     			}
     			else {
     				showDialog(R.string.msg_tunedInVote);
     			}
     		}
 		});
     }
     
     /**
      * Stops ALL AsyncTasks and removes
      * all references to them.
      */
     private void stopTasks() {
     	if(mFetchInfo != null) {
     		mFetchInfo.cancel(true);
     		mFetchInfo = null;
     	}
     	
     	if(mRateTask != null) {
     		mRateTask.cancel(true);
     		mRateTask = null;
     	}
     	
     	if(mSongCountdownTask != null) {
     		mSongCountdownTask.cancel(true);
     		mSongCountdownTask = null;
     	}
     }
     
     /**
      * Stops all running tasks and re-initializes
      * the schedule.
      */
     private void refresh() {
     	stopTasks();
     	fetchSchedules(true);
     }
     
     /**
      * Performs an initial (e.g., non-longpoll) fetch
      * of our song info.
      */
     private void initSchedules() {
         fetchSchedules(true);
     }
     
     /**
      * Performs a long-polling synchronous update
      * of our song info.
      */
     private void syncSchedules() {
         fetchSchedules(false);
     }
     
     /**
      * Performs an update of song info.
      * @param init flag to indicate this
      *   is an initial (non-long-poll) fetch.
      */
     private void fetchSchedules(boolean init) {
         // Some really bad thing happened and we don't
         // have a connection at all.
         if(mSession == null) {
         	Rainwave.showError(NowPlayingActivity.this, R.string.msg_sessionError);
             return;
         }
         
         if(mFetchInfo == null) {
             mFetchInfo = new FetchInfo();
             mFetchInfo.execute(init);
         }
     }
     
     /**
      * Starts AsyncTask for song countdown.
      * @param endTime the UTC time to stop counting
      */
     private void startCountdown(long endTime) {
     	if(mSongCountdownTask != null) {
     		mSongCountdownTask.cancel(true);
     	}
     	
     	mSongCountdownTask = new SongCountdownTask();
     	mSongCountdownTask.execute(endTime);
     }
     
     /**
      * Sets the vote drawer to opened or closed.
      * @param state, true for open, false for closed
      */
     private void setDrawerState(boolean state) {
     	boolean pref = Rainwave.getAutoShowElectionFlag(this);
     	if(!pref) return;
     	SlidingDrawer v = (SlidingDrawer) this.findViewById(R.id.np_drawer);
     	if(state && !v.isOpened()) {
     		v.animateOpen();
     	}
     	else if(v.isOpened()) {
     		v.animateClose();
     	}
     }
     
     /** Shows the menu */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menu, menu);
 		return true;
 	}
 
 	/** Responds to menu selection */
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Intent i;
 		switch(item.getItemId()) {
 		    
 		// Start RainwavePreferenceActivity.
 		case R.id.menu_preferences:
 			i = new Intent(this, RainwavePreferenceActivity.class);
 			startActivity(i);
 			break;
 			
 		case R.id.menu_tuneIn:
 			int stationId = mSession.getStationId();
 			Station s = mOrganizer.getStation(stationId);
 			i = new Intent(Intent.ACTION_VIEW);
 			i.setDataAndType(Uri.parse(s.stream), "audio/*");
 			startActivity(i);
 			break;
 			
 		case R.id.menu_pickStation:
 			showDialog(DIALOG_STATION_PICKER);
 			break;
 			
 		case R.id.menu_refresh:
 			refresh();
 			break;
 		}
 		
 		return false;
 	}
     
 	/**
 	 * Destroys any existing Session and creates
 	 * a new Session object for us to use, pulling
 	 * the user_id and key attributes from the default
 	 * Preference store.
 	 */
     private void initializeSession() {
         try {
         	// TODO: Maybe ASK the user before we override?
         	handleIntent(getIntent());
             mSession = Session.makeSession(this);
         } catch (IOException e) {
             Rainwave.showError(this, e);
         }
     }
     
     /**
      * Handles stuff included from the intent. Called once each
      * time a Session is initialized.
      * @param i the intent to handle
      * @return true if no error occurred, false if there was some error handling the URL.
      */
     private boolean handleIntent(Intent i) {
     	if(i == null) {
     		return true;
     	}
     	Bundle b = i.getExtras();
     	Uri uri = i.getData();
     	
     	// No uri? No need to handle.
     	if(uri == null){
     		return true;
     	}
     	
     	boolean handled = (b != null) && b.getBoolean(HANDLED_URI, false);
     	
     	if(handled) {
     		return true;
     	}
     	
     	i.putExtra(HANDLED_URI, true);
     	boolean ok = Rainwave.setPreferencesFromUri(this, uri);
     	
     	if(!ok) {
     		Rainwave.showError(this, R.string.msg_invalidUrl);
     	}
     	
     	return ok;
     }
     
     /**
      * Executes when a schedule sync finished.
      * @param response the response the server issued
      */
     private void onScheduleSync(RainwaveResponse response) {
     	// Updates title, album, and artists.
     	updateSongInfo(response.getCurrentSong());
     	
     	// Updates song, album ratings.
     	setRatings(response.getCurrentSong());
     	
     	// Updates election info.
     	updateElection(response);
     	
     	// Updates tuned in state.
     	updateTunedIn(response);
     }
     
     private void updateTunedIn(RainwaveResponse response) {
     	long end = response.getEndTime();
     	long utc = System.currentTimeMillis() / 1000;
     	updateTitle(response, (int) (end - utc));
     }
     
     private void updateTimer(int seconds) {
     	updateTitle(mOrganizer, seconds);
     }
     
     private void updateTitle(RainwaveResponse response, int seconds) {
     	seconds = Math.max(0, seconds);
     	int minutes = seconds / 60;
     	seconds %= 60;
     	
     	Resources r = getResources();
     	int id = mSession.getStationId();
     	String stationName = mOrganizer.getStationName(id);
     	String title = (stationName != null) ? stationName : r.getString(R.string.app_name);
     	String state = r.getString(R.string.label_nottunedin);
     	if(response.isTunedIn()) {
     		state = r.getString(R.string.label_tunedin);
     	}
     	
     	// Update thread-safe since this method may be called by AsyncTask.
     	dispatchTitleUpdate(String.format("[%d:%02d] %s (%s)", minutes, seconds, title, state));
     }
     
     private void updateElection(RainwaveResponse response) {
     	ElectionListAdapter adapter = new ElectionListAdapter(this,mSession,response.getElection());
     	((ListView)findViewById(R.id.np_electionList))
     	   .setAdapter(adapter);
     	
     	// Set vote deadline for when the song ends.
     	adapter.setDeadline(response.getEndTime());
     	
     	// Open the drawer if the user can vote.
     	boolean canVote = !response.hasVoteResult() && response.isTunedIn();
     	setDrawerState(canVote);
     	
     	// Set the vote listener for th list adapter.
     	adapter.setOnVoteHandler(mHandler);
     	
     	if(response.hasVoteResult()) {
     		adapter.markVoted(response.getPastVote());
     	}
     }
     
     /**
      * Updates the song title, album title, and
      * artists in the user interface.
      * @param current the current song that's playing.
      */
     private void updateSongInfo(Song current) {
     	((TextView) findViewById(R.id.np_songTitle)).setText(current.song_title);
     	((TextView) findViewById(R.id.np_albumTitle)).setText(current.album_name);
     	((TextView) findViewById(R.id.np_artist)).setText(current.collapseArtists());
     	
     	ImageView accent = (ImageView)findViewById(R.id.np_accent);
     	TextView requestor = (TextView)findViewById(R.id.np_requestor);
     	Resources r = getResources();
     	
     	if(current.isRequest()) {
     		accent.setImageResource(R.drawable.accent_song_hilight);
     		requestor.setVisibility(View.VISIBLE);
     		requestor.setText(String.format(r.getString(R.string.label_requestor), current.song_requestor));
     	}
     	else {
     		accent.setImageResource(R.drawable.accent_song);
     		requestor.setVisibility(View.GONE);
     	}
     }
     
     /**
      * Updates the song and album ratings.
      * @param current the current song playing
      */
     private void setRatings(Song current) {
     	((HorizontalRatingBar) findViewById(R.id.np_songRating))
     	   .setBothValues(current.song_rating_user, current.song_rating_avg);
     	
     	((HorizontalRatingBar) findViewById(R.id.np_albumRating))
  	       .setBothValues(current.album_rating_user, current.album_rating_avg);
     }
     
     /**
      * Executes when a "rate song" request has finished.
      * @param result the result the server issued
      */
     private void onRateSong(RatingResult result) {
         mOrganizer.updateSongRatings(result);
         setRatings(mOrganizer.getCurrentSong());
     }
     
     /**
      * Sets the album art to the provided Bitmap, or
      * a default image if art is null.
      * @param art desired album art
      */
     private void updateAlbumArt(Bitmap art) {
         if(art == null) {
             Log.e(TAG, "Error fetching album art.");
             Rainwave.showError(this, R.string.msg_albumArtError);
             art = BitmapFactory.decodeResource(getResources(), R.drawable.noart);
         }
         
         ((ImageView) findViewById(R.id.np_albumArt)).setImageBitmap(art);
     }
     
     /**
      * AsyncTask for submitting a rating for a song.
      * Expects two arguments to <code>execute(Object...params)</code>,
      * which are song_id (int), and rating (float).
      * @author pkilgo
      *
      */
     protected class RateTask extends AsyncTask<Object, Integer, RatingResult> {
 		@Override
 		protected RatingResult doInBackground(Object ... params) {
 			Log.d(TAG, "Submitting a rating...");
 			int songId = (Integer) params[0];
 			float rating = (Float) params[1];
 			try {
 				return mSession.rateSong(songId, rating);
 			} catch (IOException e) {
 				Log.e(TAG, "IO error: " + e.getMessage());
                 Rainwave.showError(NowPlayingActivity.this, e);
 			} catch (RainwaveException e) {
 				Log.e(TAG, "API error: " + e.getMessage());
 				Rainwave.showError(NowPlayingActivity.this, e);
 			}
 			return null;
 		}
 		
 		protected void onPostExecute(RatingResult result) {
 			Log.d(TAG, "Rating task ended.");
 			mRateTask = null;
 			if(result == null) return;
 			onRateSong(result);
 		}
     }
     
     /**
      * Fetches the now playing info.
      * Expects one argument to <code>execute(Object...params)</code> which
      * is the flag to indicate if this is an initializing (e.g., non-longpoll)
      * fetch of the schedule data.
      * @author pkilgo
      *
      */
     protected class FetchInfo extends AsyncTask<Boolean, Integer, Bundle> {
         private String TAG = "Unnamed";
         private boolean mInit = false;
 
         @Override
         protected Bundle doInBackground(Boolean ... flags) {
             mInit = flags[0];
             
             if(mInit) {
             	dispatchThrobberVisibility(true);
             }
             
             TAG = (mInit) ? "InitialPoll" : "UpdatePoll";
         	Log.d(TAG, "Fetching a schedule");
         	
             Bundle b = new Bundle();
             try {
                 RainwaveResponse organizer =
                         (mInit)
                         	? (mSession.isAuthenticated())
                         			? mSession.syncInit()
                         			: mSession.asyncGet()
                         	: mSession.syncGet(mOrganizer);
             	
             	if(mInit) {
             		Station stations[] = mSession.getStations();
             		organizer.setStations(stations);
             	}
                 
                 b.putParcelable(SCHEDULE, organizer);
                 
                 if(!organizer.hasError()) {
                     Song song = organizer.getCurrentSong();
                     Bitmap art = mSession.fetchAlbumArt(song.album_art);
                     b.putParcelable(ART, art);
                 }
                 
                 return b;
             } catch (IOException e) {
                 Log.e(TAG, "IOException occured: " + e);
                 Rainwave.showError(NowPlayingActivity.this, e);
                 return null;
             } catch (RainwaveException e) {
             	Log.e(TAG, "API error: " + e.getMessage());
             	Rainwave.showError(NowPlayingActivity.this, e);
             	return null;
             }
             
         }
         
         protected void onPostExecute(Bundle result) {
             super.onPostExecute(result);
             
             dispatchThrobberVisibility(false);
             
             mFetchInfo = null;
             
             // Was there an IO failure?
             if(result == null) {
                 mFetchInfo = null;
             	return;
             }
             
             if(mOrganizer == null) {
             	mOrganizer = result.getParcelable(SCHEDULE);
             }
             else {
             	RainwaveResponse tmp = result.getParcelable(SCHEDULE);
             	mOrganizer.receiveUpdates(tmp);
             }
             
             // Callback for schedule sync.
             onScheduleSync(mOrganizer);
             updateAlbumArt( (Bitmap) result.getParcelable(ART) );
             
             if(mSession.isAuthenticated()) {
                 syncSchedules();
             }
             
             startCountdown(mOrganizer.getEndTime());
             
             Log.d(TAG, "Exiting successfully.");
         }
     }
     
     protected class SongCountdownTask extends AsyncTask<Long, Integer, Boolean> {
         private String TAG = "Unnamed";
 
         @Override
         protected Boolean doInBackground(Long ... params) {
         	long stopTime = params[0];
 
         	long utc = System.currentTimeMillis() / 1000;
         	
         	while(utc < stopTime) {
         		try {
 					Thread.sleep(1000);
 				} catch (InterruptedException e) {
 					return false;
 				}
         		utc = System.currentTimeMillis() / 1000;
         		updateTimer((int) (stopTime - utc));
         	}
         	return true;
         }
         
         protected void onPostExecute(Boolean result) {
             super.onPostExecute(result);
         }
     }
     
     private Handler mHandler = new Handler() {
     	public void handleMessage(Message msg) {
     		Bundle data = msg.getData();
     		switch(msg.what) {
     		case HANDLER_SET_INDETERMINATE:
     			setProgressBarIndeterminateVisibility( data.getBoolean(BOOL_STATUS) );
     			break;
     			
     		case UPDATE_TITLE:
     			setTitle( data.getString(STRING_TITLE) );
     			break;
     			
     		case ElectionListAdapter.CODE_VOTED:
     			if(msg.arg1 == ElectionListAdapter.CODE_SUCCESS) {
     				setDrawerState(false);
     			}
     			break;
     		}
     	}
     };
     
     private void dispatchThrobberVisibility(boolean state) {
     	Message msg = mHandler.obtainMessage(HANDLER_SET_INDETERMINATE);
     	Bundle data = msg.getData();
     	data.putBoolean(BOOL_STATUS, state);
     	msg.sendToTarget();
     }
     
     private void dispatchTitleUpdate(String title) {
     	Message msg = mHandler.obtainMessage(UPDATE_TITLE);
     	Bundle data = msg.getData();
     	data.putString(STRING_TITLE, title);
     	msg.sendToTarget();
     }
     
     /** Handler codes */
     private static final int
     	UPDATE_TITLE = 0x71713,
     	HANDLER_SET_INDETERMINATE = 0x1D373;
     
     /** Handler keys */
     private static final String
     	BOOL_STATUS = "bool_status";
     
     /** Handler keys*/
     private static final String
     	STRING_TITLE = "string_title";
     
     /** Dialog identifiers */
     public static final int
     	DIALOG_STATION_PICKER = 0xb1c7;
     
     /** Bundle constants */
     public static final String
     	HANDLED_URI = "handled-uri",
         SCHEDULE = "schedule",
         ART = "art";
 }
