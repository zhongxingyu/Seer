 package com.oakonell.chaotictactoe;
 
 import java.util.Random;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentTransaction;
 import android.view.View;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.google.ads.Ad;
 import com.google.ads.AdListener;
 import com.google.ads.AdRequest;
 import com.google.ads.AdRequest.ErrorCode;
 import com.google.ads.AdView;
 import com.google.ads.InterstitialAd;
 import com.google.analytics.tracking.android.EasyTracker;
 import com.google.android.gms.games.GamesActivityResultCodes;
 import com.google.android.gms.games.GamesClient;
 import com.google.android.gms.games.multiplayer.Participant;
 import com.oakonell.chaotictactoe.googleapi.BaseGameActivity;
 import com.oakonell.chaotictactoe.googleapi.GameHelper;
 import com.oakonell.chaotictactoe.model.Cell;
 import com.oakonell.chaotictactoe.model.Marker;
 import com.oakonell.chaotictactoe.ui.game.GameFragment;
 import com.oakonell.chaotictactoe.ui.game.SoundManager;
 import com.oakonell.chaotictactoe.ui.menu.MenuFragment;
 import com.oakonell.utils.Utils;
 import com.oakonell.utils.activity.AppLaunchUtils;
 
 public class MainActivity extends BaseGameActivity {
 	public static final String FRAG_TAG_GAME = "game";
 	private static final String FRAG_TAG_MENU = "menu";
 
 	// Request codes for the UIs that we show with startActivityForResult:
 	public final static int RC_UNUSED = 1;
 	// online play request codes
 	public final static int RC_SELECT_PLAYERS = 10000;
 	public final static int RC_INVITATION_INBOX = 10001;
 	public final static int RC_WAITING_ROOM = 10002;
 
 	private RoomListener roomListener;
 	private InterstitialAd mInterstitialAd;
 	private AdView mAdView;
 	private SoundManager soundManager;
 
 	@Override
 	protected void onActivityResult(int request, int response, Intent data) {
 		super.onActivityResult(request, response, data);
 		if (request == RC_WAITING_ROOM) {
 			// TODO currently specially launched from listener, with access to
 			// activity only
 			getMenuFragment().onActivityResult(request, response, data);
 		} else if (request == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
 			getRoomListener().leaveRoom();
 		}
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Utils.enableStrictMode();
 		setContentView(R.layout.main_activity);
 
 		initializeAds();
 
 		initializeSoundManager();
 
 		AppLaunchUtils.appLaunched(this, null);
 
 		final ActionBar ab = getSupportActionBar();
 		ab.setDisplayHomeAsUpEnabled(false);
 		ab.setDisplayUseLogoEnabled(true);
 		ab.setDisplayShowTitleEnabled(true);
 
 		Fragment menuFrag = getSupportFragmentManager().findFragmentByTag(
 				FRAG_TAG_MENU);
 		if (menuFrag == null) {
 			menuFrag = new MenuFragment();
 			FragmentTransaction transaction = getSupportFragmentManager()
 					.beginTransaction();
 			transaction.add(R.id.main_frame, menuFrag, FRAG_TAG_MENU);
 			transaction.commit();
 		}
 
 		setSignInMessages(getString(R.string.signing_in),
 				getString(R.string.signing_out));
 	}
 
 	private void initializeSoundManager() {
 		soundManager = new SoundManager(this);
 		soundManager.addSound(Sounds.PLAY_X, R.raw.play_x_sounds_882_solemn);
 		soundManager.addSound(Sounds.PLAY_O, R.raw.play_o_sounds_913_served);
 		soundManager.addSound(Sounds.FUSE,
 				R.raw.fuse_burning_soundbible_com_1372982430);
 		soundManager.addSound(Sounds.REMOVE_MARKER,
 				R.raw.bomb_soundbible_com_891110113);
 		soundManager.addSound(Sounds.INVALID_MOVE,
 				R.raw.invalid_move_metal_gong_dianakc_109711828);
 		soundManager.addSound(Sounds.CHAT_RECIEVED,
 				R.raw.chat_received_sounds_954_all_eyes_on_me);
 		soundManager.addSound(Sounds.INVITE_RECEIVED,
 				R.raw.invite_received_sounds_1044_inquisitiveness);
 		soundManager.addSound(Sounds.DICE_ROLL,
 				R.raw.wheel_roll_single_275807_sounddogs__ca);
 		soundManager.addSound(Sounds.GAME_LOST,
 				R.raw.game_lost_sad_trombone_joe_lamb_665429450);
 		soundManager.addSound(Sounds.GAME_WON,
 				R.raw.game_won_small_crowd_applause_yannick_lemieux_1268806408);
 		soundManager.addSound(Sounds.GAME_DRAW, R.raw.game_draw_clong_1);
 	}
 
 	private void initializeAds() {
 		initializeInterstitialAd();
 
 		// initialize banner ad
 		mAdView = (AdView) findViewById(R.id.adView);
 		mAdView.loadAd(createAdRequest());
 	}
 
 	private void initializeInterstitialAd() {
 		mInterstitialAd = new InterstitialAd(MainActivity.this, getResources()
 				.getString(R.string.admob_id));
 		mInterstitialAd.loadAd(createAdRequest());
 	}
 
 	private AdRequest createAdRequest() {
 		return new AdRequest();
 	}
 
 	@Override
 	public void onSignInFailed() {
		getMenuFragment().onSignInFailed();
 	}
 
 	@Override
 	public void signOut() {
 		getMenuFragment().signOut();
 		super.signOut();
 	}
 
 	@Override
 	public void onSignInSucceeded() {
 		getMenuFragment().onSignInSucceeded();
 
 		ChaoTicTacToe app = (ChaoTicTacToe) getApplication();
 
 		Intent settingsIntent = getGamesClient().getSettingsIntent();
 		app.setSettingsIntent(settingsIntent);
 
 		Achievements achievements = app.getAchievements();
 		if (achievements.hasPending()) {
 			achievements.pushToGoogle(getGameHelper(), this);
 		}
 
 		// if we received an invite via notification, accept it; otherwise, go
 		// to main screen
 		String invitationId = getInvitationId();
 		if (invitationId != null) {
 			getMenuFragment().acceptInviteToRoom(invitationId);
 			return;
 		}
 	}
 
 	@Override
 	public GameHelper getGameHelper() {
 		return super.getGameHelper();
 	}
 
 	@Override
 	public GamesClient getGamesClient() {
 		return super.getGamesClient();
 	}
 
 	public MenuFragment getMenuFragment() {
 		return (MenuFragment) getSupportFragmentManager().findFragmentByTag(
 				FRAG_TAG_MENU);
 	}
 
 	public GameFragment getGameFragment() {
 		return (GameFragment) getSupportFragmentManager().findFragmentByTag(
 				FRAG_TAG_GAME);
 	}
 
 	public void gameEnded() {
 		possiblyShowInterstitialAd();
 		getMenuFragment().setActive();
 		roomListener = null;
 		mAdView.setVisibility(View.VISIBLE);
 	}
 
 	public void hideAd() {
 		mAdView.setVisibility(View.GONE);
 	}
 
 	public RoomListener getRoomListener() {
 		return roomListener;
 	}
 
 	public void setRoomListener(RoomListener roomListener) {
 		this.roomListener = roomListener;
 	}
 
 	@Override
 	public void onBackPressed() {
 		if (getGameFragment() != null && getGameFragment().isVisible()) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setTitle(R.string.leave_game_title);
 			builder.setMessage(R.string.leave_game_message);
 			builder.setPositiveButton(R.string.yes, new OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					dialog.dismiss();
 
 					if (roomListener != null) {
 						roomListener.leaveRoom();
 					}
 					getGameFragment().leaveGame();
 					// MainActivity.super.onBackPressed();
 
 				}
 			});
 			builder.setNegativeButton(R.string.no, new OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					dialog.dismiss();
 				}
 			});
 			builder.show();
 			return;
 		}
 		super.onBackPressed();
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		EasyTracker.getInstance().activityStart(this);
 	}
 
 	@Override
 	public void onStop() {
 		super.onStop();
 		EasyTracker.getInstance().activityStop(this);
 	}
 
 	@Override
 	protected void onPause() {
 		// mAdView.pause();
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		// mAdView.resume();
 	}
 
 	@Override
 	protected void onDestroy() {
 		mAdView.destroy();
 		soundManager.release();
 		super.onDestroy();
 	}
 
 	public void possiblyShowInterstitialAd() {
 		// show an ad with some probability (~50%?)
 		Random random = new Random();
 		if (random.nextInt(10) > 5)
 			return;
 
 		if (mInterstitialAd.isReady()) {
 			mInterstitialAd.show();
 			mInterstitialAd.setAdListener(new AdListener() {
 
 				@Override
 				public void onReceiveAd(Ad arg0) {
 					// do nothing special
 				}
 
 				@Override
 				public void onPresentScreen(Ad arg0) {
 					// do nothing special
 				}
 
 				@Override
 				public void onLeaveApplication(Ad arg0) {
 					// do nothing special
 				}
 
 				@Override
 				public void onFailedToReceiveAd(Ad arg0, ErrorCode arg1) {
 					// do nothing special
 				}
 
 				@Override
 				public void onDismissScreen(Ad arg0) {
 					// do nothing special
 					initializeInterstitialAd();
 				}
 			});
 		}
 	}
 
 	public int playSound(Sounds sound) {
 		return soundManager.playSound(sound);
 	}
 
 	public int playSound(Sounds sound, boolean loop) {
 		return soundManager.playSound(sound, loop);
 	}
 
 	public void stopSound(int streamId) {
 		soundManager.stopSound(streamId);
 	}
 
 	public void onDisconnectedFromRoom() {
 		if (getGameFragment() != null) {
 			getGameFragment().onDisconnectedFromRoom();
 		}
 	}
 
 	public void opponentWillPlayAgain() {
 		getGameFragment().opponentWillPlayAgain();
 	}
 
 	public void opponentWillNotPlayAgain() {
 		getGameFragment().opponentWillNotPlayAgain();
 	}
 
 	public void opponentLeft() {
 		if (getGameFragment() != null) {
 			getGameFragment().opponentLeft();
 		}
 	}
 
 	public void opponentInChat() {
 		getGameFragment().opponentInChat();
 	}
 
 	public void opponentClosedChat() {
 		getGameFragment().opponentClosedChat();
 	}
 
 	public void onlineMoveReceived(Marker marker, Cell cell) {
 		getGameFragment().onlineMakeMove(marker, cell);
 	}
 
 	public void messageRecieved(Participant opponentParticipant, String string) {
 		getGameFragment().messageRecieved(opponentParticipant, string);
 	}
 
 }
