 /*****************************************************************************
  *  PhraseCraze is a family friendly word game for mobile phones.
  *  Copyright (C) 2011 Siramix Team
  *  
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  ****************************************************************************/
 package com.siramix.phrasecraze;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Application;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 /**
  * This activity class is responsible for summarizing the turn and the hand-off
  * into the next turn or game end.
  * 
  * @author Siramix Labs
  */
 public class TurnSummary extends Activity {
   /**
    * logging tag
    */
   public static String TAG = "TurnSummary";
 
   static final int DIALOG_GAMEOVER_ID = 0;
   
   // Request code for AssignPoints activity result
   static final int ASSIGNPOINTS_REQUEST_CODE = 1;
 
   // Create a therad for updating the playcount for each card
   private Thread mUpdateThread;
   
   private LinkedList<Card> mCardList;
   private List<ImageView> mCardViewList;
   private List<View> mCardLineList;
 
   /**
    * Watches the button that handles hand-off to the next turn activity.
    */
   private final OnClickListener mNextRoundListener = new OnClickListener() {
     public void onClick(View v) {
       if (PhraseCrazeApplication.DEBUG) {
         Log.d(TAG, "NextTurnListener OnClick()");
       }
       PhraseCrazeApplication application = (PhraseCrazeApplication) TurnSummary.this
           .getApplication();
       GameManager gm = application.getGameManager();
 
       if (gm.isGameOver()) {
         gm.endGame();
         startActivity(new Intent(getApplication().getString(
             R.string.IntentEndGame), getIntent().getData()));
       } else {
         gm.nextRound();
         Intent clearStackIntent = new Intent(getApplication().getString(
             R.string.IntentTurn), getIntent().getData());
         clearStackIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
         startActivity(clearStackIntent);
       }
 
     }
   }; // End NextTurnListener
   
   /**
    * Watches the button that handles assigning team points.
    */
   private final OnClickListener mAssignPointsListener = new OnClickListener() {
     public void onClick(View v) {
       if (PhraseCrazeApplication.DEBUG) {
         Log.d(TAG, "AssignPoints OnClick()");
       }
       Intent intent = new Intent(getApplication().getString(
               R.string.IntentAssignPoints), getIntent().getData());
 
       startActivityForResult(intent, ASSIGNPOINTS_REQUEST_CODE);
     }
   };
 
 
   /**
    * Initializes the activity to display the results of the turn.
    */
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     if (PhraseCrazeApplication.DEBUG) {
       Log.d(TAG, "onCreate()");
     }
     
 
     // Force volume controls to affect Media volume
     setVolumeControlStream(AudioManager.STREAM_MUSIC);
 
     // Setup the view
     this.setContentView(R.layout.turnsummary);
 
     PhraseCrazeApplication application = (PhraseCrazeApplication) this
         .getApplication();
     GameManager game = application.getGameManager();
 
     // Automatically add in scores only in automatic scoring
     if( game.isAssistedScoringEnabled())
     {
     	game.setAutoAssignedRoundScores();
     }
     // Populate and display list of cards
     ScrollView list = (ScrollView) findViewById(R.id.TurnSummary_CardList);
     LinearLayout layout = new LinearLayout(this.getBaseContext());
     layout.setOrientation(LinearLayout.VERTICAL);
 
     // Iterate through all completed cards and set layout accordingly
     mCardViewList = new LinkedList<ImageView>();
     mCardLineList = new LinkedList<View>();
     mCardList = game.getCurrentCards();
     // While incrementing, build a string for updating the playcount
     String idstring = "";
     Card card = null;
     int count = 0;
 
     for (Iterator<Card> it = mCardList.iterator(); it.hasNext();) {
       card = it.next();
       // Build our string of ids for updating playcount
       if (it.hasNext()) {
         idstring += card.getId() + ", ";        
       }
       else {
         idstring += card.getId();
       }
 
       LinearLayout line = (LinearLayout) LinearLayout.inflate(
           this.getBaseContext(), R.layout.turnsumrow, layout);
       RelativeLayout realLine = (RelativeLayout) line.getChildAt(count);
       // Make every line alternating color
       if (count % 2 == 0) {
         View background = (View) realLine.getChildAt(0);
         background.setBackgroundResource(R.color.genericBG_trim);
       }
 
       // Set Title
       TextView cardTitle = (TextView) realLine.getChildAt(1);
       cardTitle.setText(card.getTitle());
 
       // Set Row end background according to team (if assisted, otherwise mark it neutral)
       ImageView rowBG = (ImageView) realLine.getChildAt(2);
       if( game.isAssistedScoringEnabled())
       {
     	  rowBG.setImageResource(card.getCreditedTeam().getTurnSumRowEndPiece());
       }
       else
       {
     	  rowBG.setImageResource(R.drawable.turnsum_row_end_noteam);
       }
       mCardViewList.add(rowBG);
       
       // Set Row end icon according to right wrong skip status
       ImageView cardIcon = (ImageView) realLine.getChildAt(3);
       cardIcon.setImageResource(card.getRowEndDrawableId());
       
       mCardViewList.add(cardIcon);
       mCardLineList.add(realLine);
       count++;
     }
     list.addView(layout);
     
     // Update our database in a thread to avoid crashes on fast next clicks and slow db writes
     final String ids = idstring;
     mUpdateThread = new Thread(new Runnable() {
       public void run() {
       Deck.DeckOpenHelper helper = new Deck.DeckOpenHelper(
           TurnSummary.this);      
       helper.updatePlaydate(mCardList);
       helper.close();
         }
       });
     mUpdateThread.start();
     
     // TODO This should be in a thread, but I'm not sure how to access the game from inside the thread
     game.fillCacheIfLow();
     
     // Force Scrollview to the bottom, since the top really doesn't matter in Phrasecraze 
     list.post(new Runnable() {
     	public void run() {
     		((ScrollView) TurnSummary.this.findViewById(R.id.TurnSummary_CardList)).fullScroll(ScrollView.FOCUS_DOWN);
     	}
     });
 
     // Update scoring team display
     TextView stoppedTeam = (TextView) this
         .findViewById(R.id.TurnSummary_StoppedOn_Team);
     if( game.isAssistedScoringEnabled())
     {
     	stoppedTeam.setText(game.getBuzzedTeam().getName());
     	stoppedTeam.setTextColor(this.getResources().getColor(game.getBuzzedTeam().getPrimaryColor()));
     }
     else
     {
     	// Hide Stopped Team views in Free Play 
     	stoppedTeam.setVisibility(View.INVISIBLE);
     	((TextView) this.findViewById(R.id.TurnSummary_StoppedOn)).setVisibility(View.INVISIBLE);
     }
     
     // Set fonts
     Typeface antonFont = Typeface.createFromAsset(getAssets(),
         "fonts/Anton.ttf");
     TextView scoreTitle = (TextView) findViewById(R.id.TurnSummary_ScoreboardTitle);
     scoreTitle.setTypeface(antonFont);
     TextView resultsTitle = (TextView) findViewById(R.id.TurnSummary_Title);
     resultsTitle.setTypeface(antonFont);
     stoppedTeam.setTypeface(antonFont);
     
     // Update the scoreboard views
     updateScoreViews();
 
     // Bind Next button
     Button playGameButton = (Button) this
         .findViewById(R.id.TurnSummary_NextTurn);
     playGameButton.setOnClickListener(mNextRoundListener);
     
     // Bind Assign Points button
     Button assignPointsButton = (Button) this
         .findViewById(R.id.TurnSummary_AssignPoints);
     assignPointsButton.setOnClickListener(mAssignPointsListener);
 
     // Handle Activity changes for final turn
     refreshButtons();
     
     // Set the score limit display
     TextView scoreLimit = (TextView) findViewById(R.id.TurnSummary_ScoreLimit);
     scoreLimit.setText(getString(R.string.turnsummary_scorelimit, game.getScoreLimit()));
     
     
   }
   
   /**
    * Refreshes the buttons to reflect new behaviors based on 
    * team scores.
    */
   private void refreshButtons()
   {
 	  PhraseCrazeApplication application = (PhraseCrazeApplication) this
 			  .getApplication();
 	  GameManager game = application.getGameManager();
 
 	  Button playGameButton = (Button) this
 			  .findViewById(R.id.TurnSummary_NextTurn);
 
 	  // Handle activity changes for final turn
 	  if ( game.isGameOver() ) {
 		  // Indicate the game is going to end
 		  playGameButton.setText(this.getString(R.string.turnsummary_results));
 	  }
 	  else
 		  playGameButton.setText(this.getString(R.string.turnsummary_nextbutton));
   }
   
   /**
    * This function is called when the AssignPoints activity finishes.
    * It adds in the supplied points to each team's total score.
    */
   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     if (requestCode == ASSIGNPOINTS_REQUEST_CODE &&
         resultCode == Activity.RESULT_OK &&
         data.getExtras() != null) {
       
       // Get team scores from the dialog
       int[] teamScores = data.getIntArrayExtra(getString(R.string.assignedPointsBundleKey));
 
       // Assign scores to teams
       PhraseCrazeApplication application = (PhraseCrazeApplication) this
           .getApplication();
       GameManager game = application.getGameManager();
       game.setNewRoundScores(teamScores);
       
       this.updateScoreViews();
       
       // Buttons could change based on new scores
       refreshButtons();
     }
     super.onActivityResult(requestCode, resultCode, data);
   }
 
   /**
    * Creates the menu items for the options menu
    */
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
     if (PhraseCrazeApplication.DEBUG) {
       Log.d(TAG, "onCreateOptionsMenu()");
     }
     menu.add(0, R.string.menu_EndGame, 0, "End Game");
     menu.add(0, R.string.menu_Rules, 0, "Rules");
 
     return true;
   }
 
   /**
    * Handle menu clicks
    */
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
     if (PhraseCrazeApplication.DEBUG) {
       Log.d(TAG, "onOptionsItemSelected()");
     }
     SoundManager sm = SoundManager.getInstance(this.getBaseContext());
     // Handle item selection
     switch (item.getItemId()) {
     case R.string.menu_EndGame:
       // Play confirmation sound
       sm.playSound(SoundManager.Sound.CONFIRM);
       this.showDialog(DIALOG_GAMEOVER_ID);
       return true;
     case R.string.menu_Rules:
       // Play confirmation sound
       sm.playSound(SoundManager.Sound.CONFIRM);
       startActivity(new Intent(
           getApplication().getString(R.string.IntentRules), getIntent()
               .getData()));
       return true;
     default:
       return super.onOptionsItemSelected(item);
     }
   }  
   
   /**
    * Handle creation of dialogs used in TurnSummary
    */
   @Override
   protected Dialog onCreateDialog(int id) {
     if (PhraseCrazeApplication.DEBUG) {
       Log.d(TAG, "onCreateDialog(" + id + ")");
     }
     Dialog dialog = null;
     AlertDialog.Builder builder = null;
 
     switch (id) {
     case DIALOG_GAMEOVER_ID:
       builder = new AlertDialog.Builder(this);
       builder.setMessage("Are you sure you want to end the current game?")
           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
               // Play confirmation sound
               SoundManager sm = SoundManager.getInstance(TurnSummary.this
                   .getBaseContext());
               sm.playSound(SoundManager.Sound.CONFIRM);
               PhraseCrazeApplication application = (PhraseCrazeApplication) TurnSummary.this
                   .getApplication();
               GameManager gm = application.getGameManager();
               gm.endGame();
               startActivity(new Intent(getString(R.string.IntentEndGame),
                   getIntent().getData()));
             }
           }).setNegativeButton("No", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
               // Play confirmation sound
               SoundManager sm = SoundManager.getInstance(TurnSummary.this
                   .getBaseContext());
               sm.playSound(SoundManager.Sound.CONFIRM);
               dialog.cancel();
             }
           });
       dialog = builder.create();
       break;
     default:
       dialog = null;
     }
     return dialog;
   }
 
   /**
    * Update the views to display the proper scores for the current round
    */
   private void updateScoreViews() {
     if (PhraseCrazeApplication.DEBUG) {
       Log.d(TAG, "UpdateScoreViews()");
     }
     PhraseCrazeApplication application = (PhraseCrazeApplication) this
         .getApplication();
     GameManager game = application.getGameManager();
     List<Team> teams = game.getTeams();
 
     // References to Scoreboard team Groups
     final int[] TEAM_SCORE_GROUPS = new int[] { R.id.TurnSummary_Scores_TeamA,
         R.id.TurnSummary_Scores_TeamB};
 
     ScoreboardRowLayout row;
     // Setup score displays. Iterate through all team groups, setting scores for
     // teams that played
     // and disabling the group for teams that did not play
     for (int i = 0; i < TEAM_SCORE_GROUPS.length; i++) {
       row = (ScoreboardRowLayout) this.findViewById(TEAM_SCORE_GROUPS[i]);
       if (i >= teams.size()) {
         // Gray out rows for teams that didn't play
         row.setActiveness(false);
       } else {
         // Show teams that played, and set their rank
         row.setTeam(teams.get(i));
         row.setActiveness(true);
       }
     }
   }
 
   /**
    * Resume the activity when it comes to the foreground. If the calling Intent
    * bundles a new card index and state the card in question is update
    * accordingly.
    */
   @Override
   protected void onResume() {
 
     super.onResume();
 
   }
 
   /**
    * Start tracking the back button so we can properly handle catching it in the
    * onKeyUp
    */
   @Override
   public boolean onKeyDown(int keyCode, KeyEvent event) {
     // Handle the back button
     if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
       event.startTracking();
       return true;
     }
 
     return super.onKeyDown(keyCode, event);
   }
 
   /**
    * Do not allow the user to go back with the back button from this activity
    * (the turn is over)
    */
   @Override
   public boolean onKeyUp(int keyCode, KeyEvent event) {
     // Make back do nothing on key-up instead of climb the action stack
     if (keyCode == KeyEvent.KEYCODE_BACK && event.isTracking()
         && !event.isCanceled()) {
       return true;
     }
 
     return super.onKeyUp(keyCode, event);
   }
 }
