 package com.charlesmadere.android.classygames;
 
 
 import android.app.NotificationManager;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.charlesmadere.android.classygames.models.Game;
 import com.charlesmadere.android.classygames.models.Person;
 import com.charlesmadere.android.classygames.settings.SettingsActivity;
 import com.charlesmadere.android.classygames.utilities.Utilities;
 
 
 public class GameFragmentActivity extends SherlockFragmentActivity implements
 	GamesListFragment.Listeners,
 	GenericGameFragment.Listeners,
 	MyStatsDialogFragment.Listeners
 {
 
 
 	private final static String LOG_TAG = Utilities.LOG_TAG + " - GameFragmentActivity";
 
 
 	public final static int RESULT_CODE_FINISH = MainActivity.GAME_FRAGMENT_ACTIVITY_REQUEST_CODE_FINISH;
 	public final static int NEW_GAME_FRAGMENT_ACTIVITY_REQUEST_CODE_FRIEND_SELECTED = 16;
 
 
 	public final static String BUNDLE_DATA_GAME_ID = "BUNDLE_DATA_GAME_ID";
 	public final static String BUNDLE_DATA_WHICH_GAME = "BUNDLE_DATA_WHICH_GAME";
 	public final static String BUNDLE_DATA_PERSON_OPPONENT_ID = "BUNDLE_PERSON_OPPONENT_ID";
 	public final static String BUNDLE_DATA_PERSON_OPPONENT_NAME = "BUNDLE_PERSON_OPPONENT_NAME";
 
 	private final static String KEY_ACTION_BAR_TITLE = "KEY_ACTION_BAR_TITLE";
 
 
 
 
 	private EmptyGameFragment emptyGameFragment;
 	private GamesListFragment gamesListFragment;
 	private GenericGameFragment genericGameFragment;
 	private MyStatsDialogFragment myStatsDialogFragment;
 
 
 
 
 	@Override
 	protected void onCreate(final Bundle savedInstanceState)
 	// I'm sorry that this method is so crazy. I really am. But there's a bunch
 	// of different things that it does, so it's necessary. Check the comments
 	// throughout the method to follow along.
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.game_fragment_activity);
 		setResult(RESULT_CODE_FINISH);
 		Utilities.setActionBar(this, R.string.games_list, false);
 
 		final FragmentManager fManager = getSupportFragmentManager();
 
 		if (savedInstanceState == null)
 		// Checks to see if the savedInstanceState object is null. If this
 		// object is null then we're not rebuilding this FragmentActivity due
 		// to an orientation change. This means that this FragmentActivity is
 		// completely fresh and brand new.
 		{
 			final FragmentTransaction fTransaction = fManager.beginTransaction();
 
 			if (isDeviceLarge())
 			// Checks to see if this is a large device. If this is a large
 			// device then we will load in the multi pane layout.
 			{
 				emptyGameFragment = new EmptyGameFragment();
 				fTransaction.add(R.id.game_fragment_activity_fragment_game, emptyGameFragment);
 
 				gamesListFragment = (GamesListFragment) fManager.findFragmentById(R.id.game_fragment_activity_fragment_games_list_fragment);
 			}
 			else
 			// This is a small device. We will load in the single pane layout.
 			{
 				gamesListFragment = new GamesListFragment();
 				fTransaction.add(R.id.game_fragment_activity_container, gamesListFragment);
 			}
 
 			fTransaction.commit();
 		}
 		else
 		// The savedInstanceState object is not null. This means that the
 		// Android device probably just went through an orientation change.
 		// We're going to recover this FragmentActivity from the data stored
 		// in the savedInstanceState object.
 		{
 			// Read in the previously stored title for the Action Bar. If the
 			// title was not found, then the R.string.games_list String will be
 			// loaded in and used instead.
			final CharSequence actionBarTitle = savedInstanceState.getCharSequence(KEY_ACTION_BAR_TITLE,
				getString(R.string.games_list));
 
 			if (isDeviceLarge())
 			// Checks to see if this is a large device. If this is a large
 			// device then we will load in the multi pane layout.
 			{
 				try
 				{
 					emptyGameFragment = (EmptyGameFragment) fManager.findFragmentById(R.id.game_fragment_activity_fragment_game);
 				}
 				catch (final ClassCastException e)
 				{
 					genericGameFragment = (GenericGameFragment) fManager.findFragmentById(R.id.game_fragment_activity_fragment_game);
 					Utilities.setActionBar(this, actionBarTitle, true);
 				}
 			}
 			else
 			// This is a small device. We will load in the single pane layout.
 			{
 				try
 				{
 					gamesListFragment = (GamesListFragment) fManager.findFragmentById(R.id.game_fragment_activity_container);
 				}
 				catch (final ClassCastException e)
 				{
 					genericGameFragment = (GenericGameFragment) fManager.findFragmentById(R.id.game_fragment_activity_container);
 					Utilities.setActionBar(this, actionBarTitle, true);
 				}
 			}
 		}
 
 		// Checks to see if the reason that this class is running is because
 		// the user tapped a Classy Games notification in their notifications
 		// bar.
 		checkIfNotificationWasTapped();
 	}
 
 
 	@Override
 	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data)
 	// This method will be run when the NewGameFragmentActivity returns some
 	// data back. That data pertains to who the current Android user wants to
 	// create a game against.
 	{
 		super.onActivityResult(requestCode, resultCode, data);
 
 		if (resultCode == NewGameFragmentActivity.RESULT_CODE_FRIEND_SELECTED)
 		// Check the result code as returned from NewGameFragmentActivity. If a
 		// friend was selected, then this means that the current Android user
 		// wants to start a new game against that selected user.
 		{
 			// Retrieve the data as returned from NewGameFragmentActivity. It
 			// could be malformed, we'll check for that below.
 			final Bundle extras = data.getExtras();
 
 			if (extras != null && !extras.isEmpty())
 			// Ensure that the returned data is not totally garbled.
 			{
 				final long personId = extras.getLong(NewGameFragmentActivity.BUNDLE_FRIEND_ID);
 				final String personName = extras.getString(NewGameFragmentActivity.BUNDLE_FRIEND_NAME);
 				final byte whichGame = extras.getByte(NewGameFragmentActivity.BUNDLE_WHICH_GAME);
 
 				if (Game.isWhichGameValid(whichGame) && Person.isIdAndNameValid(personId, personName))
 				// Ensure that we received proper data from
 				// NewGameFragmentActivity.
 				{
 					final Person friend = new Person(personId, personName);
 					final Game game = new Game(friend, whichGame);
 					onGameSelected(game);
 				}
 				else
 				{
 					Log.e(LOG_TAG, "onActivityResult() received game data that was malformed.");
 				}
 			}
 		}
 	}
 
 
 	@Override
 	public void onBackPressed()
 	{
 		if (gamesListFragment != null && gamesListFragment.isAnAsyncTaskRunning())
 		{
 			gamesListFragment.cancelRunningAnyAsyncTask();
 		}
 		else if (genericGameFragment != null && genericGameFragment.isAnAsyncTaskRunning())
 		{
 			genericGameFragment.cancelRunningAnyAsyncTask();
 		}
 		else if (myStatsDialogFragment != null && myStatsDialogFragment.isServerTaskRunning())
 		{
 			myStatsDialogFragment.cancelRunningServerTask();
 		}
 		else
 		{
 			Utilities.setActionBar(this, R.string.games_list, false);
 			super.onBackPressed();
 		}
 	}
 
 
 	@Override
 	public boolean onCreateOptionsMenu(final Menu menu)
 	{
 		final MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.game_fragment_activity, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 
 	@Override
 	protected void onDestroy()
 	{
 		if (gamesListFragment != null)
 		{
 			gamesListFragment.cancelRunningAnyAsyncTask();
 		}
 
 		if (genericGameFragment != null)
 		{
 			genericGameFragment.cancelRunningAnyAsyncTask();
 		}
 
 		if (myStatsDialogFragment != null)
 		{
 			myStatsDialogFragment.cancelRunningServerTask();
 		}
 
 		super.onDestroy();
 	}
 
 
 	@Override
 	public boolean onOptionsItemSelected(final MenuItem item)
 	{
 		switch (item.getItemId())
 		{
 			case android.R.id.home:
 				onBackPressed();
 				break;
 
 			case R.id.game_fragment_activity_menu_about:
 				startActivity(new Intent(this, AboutActivity.class));
 				break;
 
 			case R.id.game_fragment_activity_menu_my_stats:
 				final FragmentManager fManager = getSupportFragmentManager();
 				final FragmentTransaction fTransaction = fManager.beginTransaction();
 				fTransaction.addToBackStack(null);
 				myStatsDialogFragment = new MyStatsDialogFragment();
 				myStatsDialogFragment.show(fTransaction, null);
 				break;
 
 			case R.id.game_fragment_activity_menu_new_game:
 				if (isDeviceLarge() && genericGameFragment != null && genericGameFragment.isVisible())
 				{
 					onBackPressed();
 				}
 
 				startActivityForResult(new Intent(this, NewGameFragmentActivity.class), NEW_GAME_FRAGMENT_ACTIVITY_REQUEST_CODE_FRIEND_SELECTED);
 				break;
 
 			case R.id.game_fragment_activity_menu_settings:
 				startActivity(new Intent(this, SettingsActivity.class));
 				break;
 
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 
 		return true;
 	}
 
 
 	@Override
 	protected void onSaveInstanceState(final Bundle outState)
 	{
 		final CharSequence actionBarTitle = getSupportActionBar().getTitle();
 		outState.putCharSequence(KEY_ACTION_BAR_TITLE, actionBarTitle);
 
 		super.onSaveInstanceState(outState);
 	}
 
 
 
 
 	/**
 	 * Checks to see if the reason that the user is currently at this class
 	 * (GameFragmentActivity) is because they tapped a notification in their
 	 * notifications bar. If that turns out to be the reason, then this method
 	 * will load in the game that the notification pertains to.
 	 */
 	private void checkIfNotificationWasTapped()
 	{
 		// Retrieve an Intent that was given to this class. It's possible that
 		// no Intent was given to this class (in this case the resulting value
 		// should be null). We need to do this because when someone taps a
 		// Classy Games notification in their notifications bar, that action
 		// triggers an Intent to be sent into this method. That Intent will
 		// then include a bunch of information about the tapped notification.
 		final Intent intent = getIntent();
 
 		if (intent != null)
 		// Check to make sure that this Intent object is not null. If it is not
 		// null then we know that the user clicked a Classy Games notification
 		// and was then sent here.
 		{
 			final String gameId = intent.getStringExtra(BUNDLE_DATA_GAME_ID);
 			final byte whichGame = intent.getByteExtra(BUNDLE_DATA_WHICH_GAME, (byte) 0);
 			final long personId = intent.getLongExtra(BUNDLE_DATA_PERSON_OPPONENT_ID, (long) 0);
 			final String personName = intent.getStringExtra(BUNDLE_DATA_PERSON_OPPONENT_NAME);
 
 			if (Game.isIdValid(gameId) && Game.isWhichGameValid(whichGame)
 					&& Person.isIdAndNameValid(personId, personName))
 			// Check the data gathered from the Intent. If a single piece of
 			// this data is found to be invalid, then we will not act upon the
 			// tapped notification.
 			{
 				final Game game = new Game(new Person(personId, personName), whichGame, gameId);
 				onGameSelected(game);
 			}
 		}
 	}
 
 
 	/**
 	 * Ensures that the gamesListFragment Fragment is not null.
 	 */
 	private void getGamesListFragment()
 	{
 		if (gamesListFragment == null)
 		{
 			final FragmentManager fManager = getSupportFragmentManager();
 
 			if (isDeviceLarge())
 			{
 				gamesListFragment = (GamesListFragment) fManager.findFragmentById(R.id.game_fragment_activity_fragment_games_list_fragment);
 			}
 			else
 			{
 				try
 				{
 					gamesListFragment = (GamesListFragment) fManager.findFragmentById(R.id.game_fragment_activity_container);
 				}
 				catch (final ClassCastException e)
 				{
                     Log.e(LOG_TAG, "ClassCastException in getGamesListFragment()!", e);
 				}
 			}
 		}
 	}
 
 
 	/**
 	 * Checks to see what size screen this device has. This method will return
 	 * true if the device has a large screen, and as such said device should be
 	 * using the dual pane, fragment based, layout stuff.
 	 * 
 	 * @return
 	 * Returns true if this device has a large screen.
 	 */
 	private boolean isDeviceLarge()
 	{
 		return findViewById(R.id.game_fragment_activity_container) == null;
 	}
 
 
 
 
 	@Override
 	public void onGameSelected(final Game game)
 	{
 		if (isDeviceLarge() || (genericGameFragment == null || !genericGameFragment.isVisible()))
 		// This statement will validate as true if this Android device is large
 		// OR if EITHER of the following two statements are two: the
 		// genericGameFragment variable is null, OR the genericGameFragment
 		// variable is not visible.
 		{
 			if (genericGameFragment != null && genericGameFragment.isVisible())
 			// If the genericGameFragment variable is both not null and is
 			// visible. This means that a game is currently open and showing.
 			// We want to remove that game before proceeding with the showing
 			// of the newly selected game.
 			{
 				// Trigger a press of the back button. This will remove the
 				// game that is currently showing.
 				onBackPressed();
 			}
 
 			// Create a new Bundle object and place a bunch of data into it.
 			// This Bundle will be passed into the GenericGameFragment that
 			// is about to be created. This Bundle tells the new
 			// GenericGameFragment details about the game that it needs to
 			// display.
 			final Bundle arguments = new Bundle();
 			arguments.putString(GenericGameFragment.KEY_GAME_ID, game.getId());
 			arguments.putByte(GenericGameFragment.KEY_WHICH_GAME, game.getWhichGame());
 			arguments.putLong(GenericGameFragment.KEY_PERSON_ID, game.getPerson().getId());
 			arguments.putString(GenericGameFragment.KEY_PERSON_NAME, game.getPerson().getName());
 
 			if (game.isGameCheckers())
 			{
 				genericGameFragment = new CheckersGameFragment();
 			}
 			else if (game.isGameChess())
 			{
 				genericGameFragment = new ChessGameFragment();
 			}
 
 			// give the newly created GenericGameFragments the Bundle that we
 			// created just a few lines above this one
 			genericGameFragment.setArguments(arguments);
 
 			final FragmentTransaction fTransaction = getSupportFragmentManager().beginTransaction();
 			fTransaction.addToBackStack(null);
 
 			if (isDeviceLarge())
 			{
 				fTransaction.replace(R.id.game_fragment_activity_fragment_game, genericGameFragment);
 			}
 			else
 			{
 				fTransaction.add(R.id.game_fragment_activity_container, genericGameFragment);
 			}
 
 			fTransaction.commit();
 
 			if (game.isGameCheckers())
 			{
 				Utilities.setActionBar(this, getString(R.string.checkers_with_x, game.getPerson().getName()), true);
 			}
 			else if (game.isGameChess())
 			{
 				Utilities.setActionBar(this, getString(R.string.chess_with_x, game.getPerson().getName()), true);
 			}
 		}
 	}
 
 
 	@Override
 	public void onRefreshSelected()
 	{
 		if (isDeviceLarge() && genericGameFragment != null && genericGameFragment.isVisible())
 		{
 			onBackPressed();
 		}
 
 		((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();
 
 		getGamesListFragment();
 		gamesListFragment.refreshGamesList();
 	}
 
 
 	@Override
 	public boolean isDeviceSmall()
 	{
 		return !isDeviceLarge();
 	}
 
 
 	@Override
 	public void onGetGameCancelled()
 	{
 		onBackPressed();
 	}
 
 
 	@Override
 	public void onGetGameDataError()
 	{
 		Utilities.easyToastAndLogError(this, R.string.couldnt_create_the_game_as_malformed_data_was_detected);
 		onBackPressed();
 	}
 
 
 	@Override
 	public void onGetStatsDataError()
 	{
 		Utilities.easyToastAndLogError(this, R.string.we_couldnt_get_your_stats_data_there_was_a_server_error);
 		onBackPressed();
 	}
 
 
 	@Override
 	public void onServerApiTaskFinished()
 	{
 		final FragmentManager fManager = getSupportFragmentManager();
 		fManager.popBackStack();
 		final FragmentTransaction fTransaction = fManager.beginTransaction();
 
 		if (isDeviceLarge())
 		{
 			if (emptyGameFragment == null)
 			{
 				emptyGameFragment = new EmptyGameFragment();
 			}
 
 			fTransaction.replace(R.id.game_fragment_activity_fragment_game, emptyGameFragment);
 		}
 		else
 		{
 			fTransaction.remove(genericGameFragment);
 		}
 
 		fTransaction.commit();
 		fManager.executePendingTransactions();
 		Utilities.setActionBar(this, R.string.games_list, false);
 
 		onRefreshSelected();
 	}
 
 
 }
