 package com.example.mastermind;
 
import java.util.ArrayList;

import android.os.Build;
import android.os.Bundle;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.Toast;
 
 public class GameActivity extends Activity {
 	
     private Game game;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_game);
 
 		// Initialize the game
 		game = new Game(this);
 		
 		// Set visibilities based on prefs
 		Button blankButton = (Button) findViewById(R.id.blankButton);
 		if (!game.getGameOptions().isAllowBlanks()) {
 			blankButton.setVisibility(View.GONE);
 		}
 		
 		createNewRound();
 		attachEvents();
 	}
 	
 	private void createNewRound() {
 		ViewGroup roundsViewGroup = (ViewGroup) findViewById(R.id.gameLayout_rounds);
 		if (roundsViewGroup.getChildCount() < game.getGameOptions().getNumGuesses()) {
 			View roundView = LayoutInflater.from(getBaseContext()).inflate(R.layout.round, roundsViewGroup, false);
 			roundsViewGroup.addView(roundView);
 			
 			// object management
 			Round newRound = new Round(roundView, game.getRounds().size()+1);
 			game.getRounds().add(newRound);
 			game.setGuess(new GuessCode(game.getGameOptions().getGuessLength()));
 			
 			// don't display submit button until guesses are complete
 			roundView.findViewById(R.id.game_round1_guessRow_submit).setVisibility(View.GONE);
 		}
 		return;
 	}
 	
 	private void attachEvents() {
 		attachEventsToColorChoices();
 		attachEventsToGuessRow();
 	}
 	
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void attachEventsToColorChoices() {
 		// Set event listeners for color choice buttons
 		LinearLayout llchoices = (LinearLayout) findViewById(R.id.gameLayout_choices);
 		if (android.os.Build.VERSION.SDK_INT >= 11) {
 		    // Use APIs supported by API level 11 (Android 3.0) and up
 			for (int i = 0; i < llchoices.getChildCount(); i++){
 				llchoices.getChildAt(i).setOnLongClickListener(new MyLongClickListener());
 		    }
 		} else {
 		    // Do something different to support older versions
 			for (int i = 0; i < llchoices.getChildCount(); i++){
 		        llchoices.getChildAt(i).setOnClickListener(new OnClickListener() { 
 		        	
 		        	@Override
 		            public void onClick(View v) {
 		                myOnClick(v);
 		            }
 		        	
 		        });
 		    }
 		}
 	}
 	
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void attachEventsToGuessRow() {
 		Round currentRound = game.getCurrentRound();
 		Round prevRound = game.getPreviousRound();
 		
 		if (prevRound != null) {
 			// remove the submit button
 			prevRound.getGuessRow().getGuessRowView().findViewById(R.id.game_round1_guessRow_submit).setVisibility(View.GONE);
 			// detach events if possible
 			for(int i = 0; i < prevRound.getGuessRow().getGuesses().size(); ++i) {
 				View nextChild = prevRound.getGuessRow().getGuesses().get(i).getGuessView();
 				nextChild.setOnClickListener(null);
 			}
 		}
 		if (currentRound != null) {
 			// attach events
 			// Current guess row's colors can be removed by clicking on them
 			for (int i = 0; i < currentRound.getGuessRow().getGuesses().size(); i++) {
 				currentRound.getGuessRow().getGuesses().get(i).getGuessView().setOnClickListener(new OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						myOnClick(v);
 					}
 				});
 			}
 			// Current guess row's choices also have drag listeners for api>=11
 			if (android.os.Build.VERSION.SDK_INT >= 11) {
 				for (int j = 0; j < currentRound.getGuessRow().getGuesses().size(); j++) {
 					currentRound.getGuessRow().getGuesses().get(j).getGuessView().setOnDragListener(new MyDragListener());
 				}
 			}
 		}
 		return;
 	}
 	
 	protected void myOnClick(View v) {
 		//detect the view that was "clicked"
         switch(v.getId()) {
         	case R.id.game_round1_guessRow_submit:
         		game.evaluate();
         		displayResult();
         		if (game.getSolved() || game.getGameOver()) {
         		} else {
         			createNewRound();
         			attachEventsToGuessRow();
         		}
         		break;
         	case R.id.game_round1_guessRow_guess1:
         		resetGuess(v, 0);
         		break;
         	case R.id.game_round1_guessRow_guess2:
         		resetGuess(v, 1);
         		break;
         	case R.id.game_round1_guessRow_guess3:
         		resetGuess(v, 2);
         		break;
         	case R.id.game_round1_guessRow_guess4:	
         		resetGuess(v, 3);
         		break;
         	case R.id.redButton:
         	case R.id.orangeButton:
         	case R.id.yellowButton:
         	case R.id.greenButton:
         	case R.id.blueButton:
         	case R.id.violetButton:
         	case R.id.blankButton:
         		chooseColor(v);
         }
 		return;
 	}
 	
 	public void displayResult() {
 		Round currentRound = game.getCurrentRound();
 		View currentRoundView;
 		
 		if (currentRound != null) {
 			currentRoundView = currentRound.getRoundView();
 			RelativeLayout rlresults = (RelativeLayout) currentRoundView.findViewById(R.id.game_round1_reply);
 		
 			int b, w;
 			for (b = 0; b < currentRound.getReply().getClues(ClueType.Black); b++) {
 				// add a black dot
 				ImageView resultImg = (ImageView) rlresults.getChildAt(b);
 				resultImg.setBackgroundResource(R.raw.blackcircle);
 			}
 	
 			for (w = b; w < currentRound.getReply().getClues(ClueType.White) + currentRound.getReply().getClues(ClueType.Black); w++) {
 				// add a white dot
 				ImageView resultImg = (ImageView) rlresults.getChildAt(w);
 				resultImg.setBackgroundResource(R.raw.whitecircle);
 			}
 	
 			if (4 == currentRound.getReply().getClues(ClueType.Black)) {
 				game.setSolved(true);
 				//display the solved hint
 				Toast.makeText(getApplicationContext(), "You won! Play again?", Toast.LENGTH_LONG).show();
 			} else if (game.getGameOptions().getNumGuesses()<=game.getRounds().size()) {
 				game.setGameOver(true);
 				//display the gameover hint
 				Toast.makeText(getApplicationContext(), "Game over. Play again?", Toast.LENGTH_LONG).show();
 			}
 			if (game.getSolved() || game.getGameOver()) {
 				currentRoundView.findViewById(R.id.game_round1_guessRow_submit).setVisibility(View.GONE);
 			}
 		}
 	}
 
 	private void resetGuess(View v, int position) {
 		// TODO has a lot of implications
 		// also how do you then populate the missing one?
 		// therefore, need to implement the user choice position
 		
 		// Visuals
 		v.getBackground().clearColorFilter();
 		v.invalidate();
 		
 		// Object Management
		String[] newGuesses = new String[game.getGameOptions().getGuessLength()];
		for (int i = 0; i < game.getGuesses().length; i++) {
			if (i == position) {
				// reset this one
				newGuesses[i] = "";
			} else {
				// copy the original
				newGuesses[i] = game.getGuesses()[i];
			}
		}
		game.setGuesses(newGuesses);
		game.setChoice(game.getChoice()-1);
		
 		// Attach drag listener since it was detached before
 		if (android.os.Build.VERSION.SDK_INT >= 11) {
 			v.setOnDragListener(new MyDragListener());
 		}
 	}
 	
 	// **********************************************************
 	// this is used by apk<11
 	// **********************************************************
 	private void chooseColor(View v) {
 		// in the current row, assigns the first available choice to v's color
 		// TODO later maybe allow user to put the color in the position they specify
 		// Automatically populate the first available position
 		// To select a position, touch the position first then the color
 		game.choose(v.getId());
 		toggleSubmitButton();
 	}
 	
 	// this is used by apk>=11
 	public void chooseColor(Integer colorViewId, Integer guessPositionId) {
 		game.choose(colorViewId, guessPositionId);
 		toggleSubmitButton();
 	}
 
 	private void toggleSubmitButton() {
		// decide to show the submit button
		if (game.getChoice()==game.getGameOptions().getGuessLength()) { //TODO different way of figuring out choice
 			if (game.getCurrentRound() != null) {
 				game.getCurrentRound().getRoundView().findViewById(R.id.game_round1_guessRow_submit).setVisibility(View.VISIBLE);
 			}
 		}
 	}
 	
 	public Game getGame() {
 		return game;
 	}
 
 	public void setGame(Game game) {
 		this.game = game;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.menu_game, menu);
 		return true;
 	}
 	
 	@Override
     public boolean onOptionsItemSelected (MenuItem item) {
     	
     	switch (item.getItemId()) {
 	    	case R.id.menu_saveGame:
 	    	    saveGame();
 	    	    return true;
 	    	case R.id.menu_exitGame:
 	    	    finish();
 	    	    return true;
 	    	default:
 	    	    return super.onOptionsItemSelected(item);
     	}
     }
 	
 	private void saveGame() {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
