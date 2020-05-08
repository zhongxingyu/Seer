 package com.antsapps.triples;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.ActivityInfo;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.DialogFragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.Toast;
 import android.widget.ViewSwitcher;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.antsapps.triples.backend.Application;
 import com.antsapps.triples.backend.Game;
 import com.antsapps.triples.backend.Game.GameState;
 import com.antsapps.triples.backend.Game.OnUpdateGameStateListener;
 import com.antsapps.triples.cardsview.CardsView;
 import com.google.android.gms.games.GamesClient;
 import com.google.android.gms.games.leaderboard.LeaderboardVariant;
 import com.google.android.gms.games.leaderboard.OnScoreSubmittedListener;
 import com.google.android.gms.games.leaderboard.SubmitScoreResult;
 
 public class GameActivity extends SherlockFragmentActivity implements
     OnUpdateGameStateListener, GameHelper.GameHelperListener {
 
   public class SignInDialogFragment extends DialogFragment {
     @Override
     public Dialog onCreateDialog(Bundle savedInstanceState) {
       // Use the Builder class for convenient dialog construction
       AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
       // Get the layout inflater
       LayoutInflater inflater = getActivity().getLayoutInflater();
 
       // Inflate and set the layout for the dialog
       // Pass null as the parent view because its going in the dialog layout
       View view = inflater.inflate(R.layout.signin_dialog, null);
       view.findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
           mHelper.beginUserInitiatedSignIn();
           SignInDialogFragment.this.dismiss();
         }
       });
       builder.setView(view);
       // Create the AlertDialog object and return it
       return builder.create();
     }
   }
 
   private Game mGame;
   private ViewSwitcher mViewSwitcher;
   private CardsView mCardsView;
   private GameState mGameState;
   private StatusBar mStatusBar;
   private Application mApplication;
 
   private GameHelper mHelper;
 
   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.main);
     mApplication = Application.getInstance(getApplication());
 
     if (getIntent().hasExtra(Game.ID_TAG)) {
       // We are being created from the game list.
       mGame = mApplication.getGame(getIntent().getLongExtra(Game.ID_TAG, 0));
     } else if (savedInstanceState != null) {
       // We are being restored
       mGame = mApplication.getGame(savedInstanceState.getLong(Game.ID_TAG));
     } else {
       throw new IllegalArgumentException(
           "No savedInstanceState or intent containing key");
     }
 
     mGame.addOnUpdateGameStateListener(this);
     GameState originalGameState = mGame.getGameState();
 
     mStatusBar = (StatusBar) findViewById(R.id.status_bar);
     mGame.setOnTimerTickListener(mStatusBar);
     mGame.addOnUpdateCardsInPlayListener(mStatusBar);
 
     mCardsView = (CardsView) findViewById(R.id.cards_view);
     mCardsView.setOnValidTripleSelectedListener(mGame);
     mGame.addOnUpdateCardsInPlayListener(mCardsView);
 
     mViewSwitcher = (ViewSwitcher) findViewById(R.id.view_switcher);
 
     ActionBar actionBar = getSupportActionBar();
     actionBar.setDisplayHomeAsUpEnabled(true);
 
     mGame.begin();
 
     if (originalGameState == GameState.STARTING) {
       mCardsView.shouldSlideIn();
     }
 
     mHelper = new GameHelper(this);
     mHelper.setup(this, GameHelper.CLIENT_PLUS | GameHelper.CLIENT_GAMES);
   }
 
   @Override
   protected void onStart() {
     super.onStart();
     mHelper.onStart(this);
   }
 
   @Override
   protected void onStop() {
     super.onStop();
     mHelper.onStop();
   }
 
 
   @Override
   public boolean onPrepareOptionsMenu(Menu menu) {
     super.onPrepareOptionsMenu(menu);
     menu.findItem(R.id.pause).setVisible(mGameState == GameState.ACTIVE);
     menu.findItem(R.id.play).setVisible(mGameState == GameState.PAUSED);
     return true;
   }
 
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
     MenuInflater inflater = getSupportMenuInflater();
     inflater.inflate(R.menu.game, menu);
     return true;
   }
 
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
     // Handle item selection
     switch (item.getItemId()) {
       case R.id.pause:
         mGame.pause();
         return true;
       case R.id.play:
         mGame.resume();
         return true;
       case R.id.help:
         Intent helpIntent = new Intent(getBaseContext(), HelpActivity.class);
         startActivity(helpIntent);
         return true;
       case R.id.settings:
         Intent settingsIntent = new Intent(getBaseContext(),
             SettingsActivity.class);
         startActivity(settingsIntent);
         return true;
       case android.R.id.home:
         // app icon in action bar clicked; go up one level
         Intent intent = new Intent(this, GameListActivity.class);
         intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
         startActivity(intent);
         finish();
         return true;
       default:
         return super.onOptionsItemSelected(item);
     }
   }
 
   @Override
   protected void onResume() {
     super.onResume();
     mGame.resumeFromLifecycle();
 
     SharedPreferences sharedPref = PreferenceManager
         .getDefaultSharedPreferences(this);
     if (sharedPref.getBoolean(getString(R.string.pref_screen_lock), true)) {
       getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
     }
 
     if (sharedPref.getBoolean(getString(R.string.pref_orientation_lock), false)) {
       String orientation = sharedPref.getString(
           getString(R.string.pref_orientation),
           "portrait");
       if (orientation.equals("portrait")) {
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
       } else if (orientation.equals("landscape")) {
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
       }
     } else {
       setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
     }
     updateViewSwitcher();
   }
 
   @Override
   protected void onPause() {
     super.onPause();
     mApplication.saveGame(mGame);
     mGame.pauseFromLifecycle();
     getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
     updateViewSwitcher();
   }
 
   @Override
   protected void onSaveInstanceState(Bundle outState) {
     outState.putLong(Game.ID_TAG, mGame.getId());
   }
 
   @Override
   protected void onDestroy() {
     mGame.removeOnUpdateCardsInPlayListener(mCardsView);
 
     mGame.removeOnUpdateCardsInPlayListener(mStatusBar);
     mGame.setOnTimerTickListener(null);
 
     mGame.removeOnUpdateGameStateListener(this);
 
     super.onDestroy();
   }
 
   @Override
   public void onUpdateGameState(GameState state) {
     mGameState = state;
 
     updateViewSwitcher();
 
     if (mGameState == GameState.COMPLETED) {
       mCardsView.setAlpha(0.5f);
     }
 
     invalidateOptionsMenu();
   }
 
   @Override
   public void gameFinished() {
     Log.i("GameActivity", "game finished");
     if (mHelper.isSignedIn()) {
       submitScore();
     } else {
       DialogFragment dialog = new SignInDialogFragment();
       dialog.show(getSupportFragmentManager(), "SignInDialogFragment");
     }
   }
 
   private void submitScore() {
     if (mGameState != GameState.COMPLETED) {
       return;
     }
     mHelper.getGamesClient().submitScoreImmediate(new OnScoreSubmittedListener() {
       @Override
       public void onScoreSubmitted(int status, SubmitScoreResult submitScoreResult) {
         String message = null;
         switch (status) {
           case GamesClient.STATUS_OK:
             if (submitScoreResult.getScoreResult(LeaderboardVariant.TIME_SPAN_ALL_TIME).newBest) {
               message = "Congratulations! That's your best score ever.";
             } else if (submitScoreResult.getScoreResult(LeaderboardVariant.TIME_SPAN_WEEKLY).newBest) {
               message = "Well Done! That's your best score this week.";
             } else if (submitScoreResult.getScoreResult(LeaderboardVariant.TIME_SPAN_DAILY).newBest) {
               message = "Nice! That's your best score today.";
             } else {
              message = "You've done better today - keep trying!";
             }
             break;
           case GamesClient.STATUS_NETWORK_ERROR_OPERATION_DEFERRED :
             message = "Score will be submitted when next connected.";
            break;
           default:
             message = "Score could not be submitted";
            break;
         }
         Toast.makeText(GameActivity.this, message, Toast.LENGTH_LONG).show();
       }
     }, GamesServices.Leaderboard.CLASSIC, mGame.getTimeElapsed());
   }
 
   private void updateViewSwitcher() {
     int childToDisplay = 0;
     if(mGameState == GameState.PAUSED || !mGame.getActivityLifecycleActive()) {
       childToDisplay = 1;
     } else {
       childToDisplay = 0;
     }
     if (mViewSwitcher.getDisplayedChild() != childToDisplay) {
       mViewSwitcher.setDisplayedChild(childToDisplay);
     }
   }
 
   @Override
   public void onSignInFailed() {
 
   }
 
   @Override
   public void onSignInSucceeded() {
     if (mGameState == GameState.COMPLETED) {
       submitScore();
     }
   }
 
   @Override
   public void onSignOut() {
 
   }
 }
