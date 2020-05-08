 package com.example.mastermind;
 
 import java.util.ArrayList;
import java.util.Random;
 
 import android.app.Activity;
 import android.content.Context;
 import android.view.View;
 import android.view.ViewGroup;
import android.widget.ImageView;
 import android.widget.Toast;
 
 public class Game {
 	private Context context;
 	private GameActivity activity;
 	private ArrayList<Round> rounds;
 	private Boolean solved = false;
 	private Boolean gameOver = false;
     private ButtonColorManager bcm;
     private GameOptions gameOptions;
     private AnswerCode answer;
     private GuessCode guess;
 
 	public Game(Context c) {
 		setContext(c);
 		setActivity(c);
 		bcm = new ButtonColorManager(context); // TODO probably remove this class
 		gameOptions = new GameOptions(context);
 		answer = new AnswerCode(gameOptions.getGuessLength(), gameOptions.isAllowDupes(), gameOptions.isAllowBlanks());
 		guess = new GuessCode(gameOptions.getGuessLength());
 		solved = false;
 		gameOver = false;
 	}
 	public AnswerCode getAnswer() {
 		return answer;
 	}
 	public void setAnswer(AnswerCode answer) {
 		this.answer = answer;
 	}
 	public GuessCode getGuess() {
 		return guess;
 	}
 	public void setGuess(GuessCode guess) {
 		this.guess = guess;
 	}
 	public ArrayList<Round> getRounds() {
 		return rounds;
 	}
 	public void setRounds(ArrayList<Round> rounds) {
 		this.rounds = rounds;
 	}
 	public void setActivity(GameActivity activity) {
 		this.activity = activity;
 	}
 	public Context getContext() {
 		return context;
 	}
 	public void setContext(Context context) {
 		this.context = context;
 	}
 	public Activity getActivity() {
 		return activity;
 	}
 	public void setActivity(Context c) {
 		this.activity = (GameActivity) c;
 	}
 	public ButtonColorManager getBcm() {
 		return bcm;
 	}
 
 	public void setBcm(ButtonColorManager bcm) {
 		this.bcm = bcm;
 	}	
 	public GameOptions getGameOptions() {
 		return gameOptions;
 	}
 
 	public void setGameOptions(GameOptions gameOptions) {
 		this.gameOptions = gameOptions;
 	}
 
 	public Boolean getSolved() {
 		return solved;
 	}
 
 	public void setSolved(Boolean solved) {
 		this.solved = solved;
 	}
 
 	public Boolean getGameOver() {
 		return gameOver;
 	}
 
 	public void setGameOver(Boolean gameOver) {
 		this.gameOver = gameOver;
 	}
 	
 	public Round getCurrentRound() {
 		Round currentRound;
 		try {
 			currentRound = rounds.get(rounds.size()-1);
 		} catch (IndexOutOfBoundsException e) {
 			currentRound = null;
 		}
 
 		return currentRound;
 	}
 	
 	public Round getPreviousRound() {
 		Round round;
 		try {
 			round = rounds.get(rounds.size()-2);
 		} catch (IndexOutOfBoundsException e) {
 			round = null;
 		}
 
 		return round;
 	}
 	
 	// game utilities
 	private int firstAvailablePosition() { //TODO: encapsulate in correct place
 		int rInt = -1; // -1 means nothing is available
 		// get the current guess row
 		ArrayList<Guess> guesses = getCurrentRound().getGuessRow().getGuesses();
 		for (int i = 0; i < guesses.size(); i++){
 			if (guesses.get(i).isSet()) {
 				continue;
 			} else {
 				rInt = i;
 				break;
 			}
 		}
 		return rInt;
 	}
 	
 	// this is for apk<11
 	public void choose(Integer colorViewId) {
 		if (solved || gameOver) { // Button to play again
 			Toast.makeText(context, "Game over. Play again?", Toast.LENGTH_LONG).show();
 		} else if (guess.isFull()) {
 			Toast.makeText(context, "You've filled up your guess. Hit the submit button.", Toast.LENGTH_LONG).show();
 		} else {
 			// we always allow duplicates in the guesses
 			// TODO any way to get this on evaluate?
 			String colorName = bcm.getColorNameFromViewId(colorViewId);
 			// Get the first available position, put it there
 			int currentPosition = firstAvailablePosition();
 			guess.insert(currentPosition, colorName);
 			// color the current guess position (UI)
 			View currentChoice = getCurrentRound().getGuessRow().getGuesses().get(currentPosition).getGuessView();
 			int colorId = bcm.getColorIdFromViewId(colorViewId);
 			bcm.setBackground(currentChoice, colorId);
 		}
 	}
 	
 	// this is for apk>=11
 	public void choose(Integer colorViewId, Integer guessPositionId) {
 		if (solved || gameOver) { // Button to play again
 			Toast.makeText(context, "Game over. Play again?", Toast.LENGTH_LONG).show();
 		} else if (guess.isFull()) {
 			Toast.makeText(context, "You've filled up your guess. Hit the submit button.", Toast.LENGTH_LONG).show();
 		} else {
 			// we always allow duplicates in the guesses
 			String colorName = bcm.getColorNameFromViewId(colorViewId);
 			GameActivity parent = (GameActivity) context;
 			int i;
 			for (i=1; i<=gameOptions.getGuessLength();i++) {
 				ViewGroup round = (ViewGroup) parent.findViewById(R.id.game_round1);
 				if (guessPositionId == round.getChildAt(i-1).getId()) {
 					break;
 				}
 			}
 			int guessPosition = i-1;
 			guess.insert(guessPosition, colorName);
 		}
 	}
 	
 	public void evaluate() {
 		int i, j, k;
 		ArrayList<String> answerCopy = new ArrayList<String>(answer.getCode());
 		
 		if (solved||gameOver) {
 			//shouldn't happen because submit button should disappear
 			return;
 		}
 		
 		// process all the possible blacks first
 		for (i=0; i<answerCopy.size(); i++) {
 			if (guess.getCode().get(i) == answerCopy.get(i)) {
 				getCurrentRound().getReply().getClues().get(i).setClueType(ClueType.Black);
 				getCurrentRound().getReply().getClues().get(i).setBackgroundResource();
 				guess.getCode().remove(i);
 				answerCopy.remove(i); // TODO: make sure loop still checks right size()
 			}
 		}
 
 		// then process any remaining whites
 		for (j=0; j<answerCopy.size(); j++) {
 			String colorToFind = guess.getCode().get(i);
 			for (k=0; k<answerCopy.size(); k++) {
 				if (answerCopy.get(k) == colorToFind) {
 					getCurrentRound().getReply().getClues().get(i).setClueType(ClueType.White);
 					getCurrentRound().getReply().getClues().get(i).setBackgroundResource();
 					//answerCopy[k] = "";
 					//TODO what if i remove from here too?
 					answerCopy.remove(k);
 					break;
 				}
 			}
 		}
 	}	
 }
