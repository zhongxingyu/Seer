 package com.charlesmadere.android.classygames;
 
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.res.AssetManager;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.charlesmadere.android.classygames.models.Game;
 import com.charlesmadere.android.classygames.models.Person;
 import com.charlesmadere.android.classygames.utilities.FacebookUtilities;
 import com.charlesmadere.android.classygames.utilities.TypefaceUtilities;
 import com.charlesmadere.android.classygames.utilities.Utilities;
 import com.nostra13.universalimageloader.core.ImageLoader;
 
 
 public class ConfirmGameFragment extends SherlockFragment
 {
 
 
 	public final static String KEY_FRIEND_ID = "KEY_FRIEND_ID";
 	public final static String KEY_FRIEND_NAME = "KEY_FRIEND_NAME";
 
 
 
 
 	/**
 	 * The AlertDialog that is shown whenever the user decides to play a game
 	 * against the chosen friend. This AlertDialog asks the user to choose
 	 * which game they want to play.
 	 */
 	private AlertDialog alertDialog;
 
 
 	private AssetManager assetManager;
 
 
 	/**
 	 * The LayoutInflater object that was passed in to this Fragment in the
 	 * onCreateView() method.
 	 */
 	private LayoutInflater inflater;
 
 
 	/**
 	 * The Person object that will be shown on this Fragment's layout. The user
 	 * must confirm whether or not they want to play against this friend.
 	 */
 	private Person friend;
 
 
 
 
 	/**
 	 * Object that allows us to run any of the methods that are defined in the
 	 * ConfirmGameFragmentListeners interface.
 	 */
 	private ConfirmGameFragmentListeners listeners;
 
 
 	/**
 	 * A bunch of listener methods for this Fragment.
 	 */
 	public interface ConfirmGameFragmentListeners
 	{
 
 
 		/**
 		 * This is fired during this Fragment's onCreateOptionsMenu() method.
 		 * Checks to see if the current device is considered by Android to be
 		 * small. This be basically every phone.
 		 * 
 		 * @return
 		 * Returns true if the current device is small.
 		 */
 		public boolean isDeviceSmall();
 
 
 		/**
 		 * This is fired in the event that an error was detected with some of
 		 * the data needed to instantiate a game.
 		 */
 		public void onDataError();
 
 
 		/**
 		 * This is fired in the event that the current device's user clicks the
 		 * "Start Game!" button. This means that they've decided to play a game
 		 * against the chosen friend.
 		 * 
 		 * @param friend
 		 * The Facebook friend that the user has decided to play against.
 		 *
 		 * @param whichGame
 		 * Which game that the user decided to play. This could be checkers,
 		 * chess...
 		 */
 		public void onGameConfirm(final Person friend, final byte whichGame);
 
 
 		/**
 		 * This is fired in the event that the current device's user clicks the
 		 * "Nevermind..." button. This means that they've decided that they'd
 		 * rather not play against the chosen friend.
 		 */
 		public void onGameDeny();
 
 
 	}
 
 
 
 
 	@Override
 	public void onCreate(final Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setHasOptionsMenu(true);
 	}
 
 
 	@Override
 	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
 	{
 		this.inflater = inflater;
 		final Bundle arguments = getArguments();
 
 		if (arguments != null && !arguments.isEmpty())
 		{
 			final long friendId = arguments.getLong(KEY_FRIEND_ID);
 			final String friendName = arguments.getString(KEY_FRIEND_NAME);
 
 			if (Person.isIdAndNameValid(friendId, friendName))
 			{
 				friend = new Person(friendId, friendName);
 			}
 			else
 			{
 				listeners.onDataError();
 			}
 		}
 
 		return inflater.inflate(R.layout.confirm_game_fragment, null);
 	}
 
 
 	@Override
 	public void onActivityCreated(final Bundle savedInstanceState)
 	{
 		super.onActivityCreated(savedInstanceState);
 
 		final View view = getView();
 		final AssetManager assetManager = getAssetManager();
 
 		final TextView friendsName = (TextView) view.findViewById(R.id.confirm_game_fragment_friend_name);
 		friendsName.setText(friend.getName());
 		TypefaceUtilities.applyTypefaceSnellRoundhand(assetManager, friendsName);
 
 		final TextView description = (TextView) view.findViewById(R.id.confirm_game_fragment_description);
 		description.setText(getString(R.string.are_you_sure_that_you_want_to_start_a_game_with_x, friend.getName()));
 
 		final Button confirm = (Button) view.findViewById(R.id.confirm_game_fragment_button_confirm);
 		TypefaceUtilities.applyTypefaceBlueHighway(assetManager, confirm);
 
 		confirm.setOnClickListener(new OnClickListener()
 		{
 			@Override
 			public void onClick(final View v)
 			{
 				getAlertDialog().show();
 			}
 		});
 
 		final Button deny = (Button) view.findViewById(R.id.confirm_game_fragment_button_deny);
 		TypefaceUtilities.applyTypefaceBlueHighway(assetManager, deny);
 
 		deny.setOnClickListener(new OnClickListener()
 		{
 			@Override
 			public void onClick(final View v)
 			{
 				listeners.onGameDeny();
 			}
 		});
 
 		final Context context = getSherlockActivity();
 		final ImageLoader imageLoader = Utilities.getImageLoader(context);
 		final ImageView profilePicture = (ImageView) view.findViewById(R.id.confirm_game_fragment_friend_profile_picture);
 		imageLoader.displayImage(FacebookUtilities.getFriendsPictureLarge(context, friend.getId()), profilePicture);
 	}
 
 
 	@Override
 	public void onAttach(final Activity activity)
 	// This makes sure that the Activity containing this Fragment has
 	// implemented the callback interface. If the callback interface has not
 	// been implemented, an exception is thrown.
 	{
 		super.onAttach(activity);
 
 		try
 		{
 			listeners = (ConfirmGameFragmentListeners) activity;
 		}
 		catch (final ClassCastException e)
 		{
 			throw new ClassCastException(activity.toString() + " must implement listeners!");
 		}
 	}
 
 
 	@Override
 	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater)
 	{
 		if (listeners.isDeviceSmall())
 		{
 			menu.removeItem(R.id.friends_list_fragment_menu_refresh);
 		}
 
 		menu.removeItem(R.id.friends_list_fragment_menu_search);
 
 		super.onCreateOptionsMenu(menu, inflater);
 	}
 
 
 
 
 	/**
 	 * Builds the AlertDialog that asks the user which game they'd like to play
 	 * against their friend.
 	 *
 	 * @return
 	 * Returns the AlertDialog completely built and ready to go. Simply use its
 	 * show() method to draw it onto the screen.
 	 */
 	private AlertDialog getAlertDialog()
 	{
 		if (alertDialog == null)
 		{
 			final View dialogView = inflater.inflate(R.layout.choose_which_game_dialog, null);
 			final AssetManager assetManager = getAssetManager();
 
 			final Button checkers = (Button) dialogView.findViewById(R.id.choose_which_game_dialog_button_checkers);
 			TypefaceUtilities.applyTypefaceBlueHighway(assetManager, checkers);
 
 			checkers.setOnClickListener(new OnClickListener()
 			{
 				@Override
 				public void onClick(final View v)
 				{
 					listeners.onGameConfirm(friend, Game.WHICH_GAME_CHECKERS);
 				}
 			});
 
 			final Button chess = (Button) dialogView.findViewById(R.id.choose_which_game_dialog_button_chess);
 			TypefaceUtilities.applyTypefaceBlueHighway(assetManager, chess);
 
 			chess.setOnClickListener(new OnClickListener()
 			{
 				@Override
 				public void onClick(final View v)
 				{
 					listeners.onGameConfirm(friend, Game.WHICH_GAME_CHESS);
 				}
 			});
 
 			alertDialog = new AlertDialog.Builder(getSherlockActivity())
 				.setView(dialogView)
 				.create();
 		}
 
 		return alertDialog;
 	}
 
 
 	/**
 	 * Retrieves a usable AssetManager object.
 	 *
 	 * @return
 	 * Returns a ready-to-use AssetManager object.
 	 */
 	private AssetManager getAssetManager()
 	{
 		if (assetManager == null)
 		{
 			assetManager = getResources().getAssets();
 		}
 
 		return assetManager;
 	}
 
 
 }
