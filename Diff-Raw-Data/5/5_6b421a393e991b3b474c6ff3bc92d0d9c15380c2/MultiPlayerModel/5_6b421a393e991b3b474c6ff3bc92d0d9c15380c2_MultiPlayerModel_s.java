 package com.example.zootypers.core;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import android.util.Log;
 
 import com.example.zootypers.util.EmptyQueueException;
 import com.example.zootypers.util.InternalErrorException;
 import com.example.zootypers.util.InternetConnectionException;
 import com.example.zootypers.util.States;
 import com.parse.ParseException;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 import com.parse.RefreshCallback;
 
 /** 
  * 
  * The Model class for Multi-Player store a list of words for the UI to display.
  * It keeps track of word and letter the user has typed and updates the view accordingly.
  * 
  * @author winglam, nhlien93, dyxliang
  * 
  */
 public class MultiPlayerModel extends PlayerModel {
 
 	// timer set for 15 sec to wait before giving up in queue
 	private static final int QUEUE_TIMEOUT = 15000;
 
 	// timer set to 5 sec to wait for getting opponents score
 	private static final int SCORE_TIMEOUT = 5000;
 
 	// timer set to 1/2 sec to wait between checks
 	private static final int RECHECK_TIME = 500;
 
 	// total number of words in wordLists on Parse database
 	private static final int TOTAL_WORDS = 709;
   private static final int TOTAL_WORDS_LATIN = 342;
 
 	// size of the list to get from the Parse database
 	private static final int LIST_SIZE = 100;
 
 
 	// the object on the database that has all info of the match
 	// on the Parse database
 	private ParseObject match;
 
 	// username of the player which was passed in
 	private String name;
 
 	// the ID of the animal the user picked
 	private int animalID;
 
 	// maps the info to the correct key (between if the player was p1 or p2)
 	private Map<String, String> info;
 
 	/**
 	 * Constructs a new SinglePlayerModel that takes in the ID of an animal and background,
 	 * and also what the difficulty level is. The constructor will initialize the words list
 	 * and fills in what words the view should display on the screen.
 	 * 
 	 * @param wordsDis, the number of words being displayed on the screen
 	 * @param uname, the username of the user
 	 * @param animalID, the int ID of a animal that is selected by the user
 	 */
 	public MultiPlayerModel(int wordsDis, String uname, int animalName) {
 		super(wordsDis);
 		this.animalID = animalName;
 		this.name = uname;
 		this.info = new HashMap<String, String>();
 	}
 
 	/**
 	 * Begins matchmaking the user by trying to find an opponent on the Parse
 	 * database.
 	 * 
 	 * @throws InternetConnectionException if disconnected from Internet
 	 * @throws EmptyQueueException if no opponent is found on database
 	 * @throws InternalErrorException if there was an internal error
 	 */  
 	public void beginMatchMaking() throws InternetConnectionException, 
 	EmptyQueueException, InternalErrorException {
 		Log.i("Multiplayer", "user begins waiting in queue");
 		// if an opponent is waiting in the database to play
 		if (findOpponent()) {
 			setInfo(false);
 			try {
 				match.put("p2name", name);
 				match.put("p2animal", animalID);
 				match.put("p2score", 0);
 				match.put("p2finished", false);
 				match.save();
 			} catch (ParseException e) {
 				Log.e("Multiplayer", "parse error while joining a match", e);
 				throw new InternetConnectionException();
 			}
 			// if no opponent was added then create a match and wait for an opponent to join
 		} else {
 			addToQueue();
 			if (!checkStatus()) {
 				Log.w("Multiplayer", "timed out waiting for an opponent");
 				throw new EmptyQueueException();
 			}
 		}
 	}
 
 	/*
 	 * checks whether user can be matched to an opponent
 	 * returns true if matched, false otherwise
 	 */
 	private boolean findOpponent() throws InternetConnectionException {
 		try {
 			checkInternet();
 			ParseQuery query = new ParseQuery("Matches");
 			query.whereEqualTo("p2name", "");
 			query.whereNotEqualTo("p1name", name);
 			match = query.getFirst();
 			Log.i("Multiplayer", "matched has been found for player");
 			return true;
 		} catch (ParseException e) {
 			e.fillInStackTrace();
 			Log.i("Multiplayer", "no matches are found");
 			return false;
 		} catch (NullPointerException e) {
 			Log.e("Multiplayer", "unable to connect to internet", e);
 			throw new InternetConnectionException();
 		}
 	}
 
 	/*
 	 * matches the correct key to the correct key on the
 	 * Parse database depending on of the user is player 1 or 2
 	 */
 	private void setInfo(boolean isPOne) {
 		if (isPOne) {
 			info.put("name", "p1name");
 			info.put("animal", "p1animal");
 			info.put("score", "p1score");
 			info.put("finished", "p1finished");
 			info.put("oname", "p2name");
 			info.put("oanimal", "p2animal");
 			info.put("oscore", "p2score");
 			info.put("ofinished", "p2finished");
 		} else {
 			info.put("name", "p2name");
 			info.put("animal", "p2animal");
 			info.put("score", "p2score");
 			info.put("finished", "p2finished");
 			info.put("oname", "p1name");
 			info.put("oanimal", "p1animal");
 			info.put("oscore", "p1score");
 			info.put("ofinished", "p1finished");
 		}
 	}
 
 	/*
 	 * Creates a new match online and adds the user to queue
 	 * as player 1. Match doesn't start until another opponent joins the match
 	 */
 	private void addToQueue() throws InternetConnectionException {
 		// sets the starting word index on the database to a random integer
 		Log.i("Multiplayer", "creating match for player");
 		
 		final int randy;
 		if (Locale.getDefault().getDisplayLanguage().equals("italiano")) {
 		    randy = (int) (Math.random() * (TOTAL_WORDS_LATIN));
 		} else {
 		    randy = (int) (Math.random() * (TOTAL_WORDS));
 		}
 		try {
 		    setInfo(true);
 		    match = new ParseObject("Matches");
 		    match.put("p1name", name);
 		    match.put("p1animal", animalID);
 		    match.put("p1score", 0);
 		    match.put("p1finished", false);
 		    match.put("p2name", "");
 		    match.put("wordIndex", randy);
 		    match.save();
 		} catch (ParseException e) {
 		    Log.e("Multiplayer", "parse error while creating a match", e);
 		    throw new InternetConnectionException();
 		}
 	}
 
 	/*
 	 * Checks online to see if an opponent has added thems
 	 * elves to the match
 	 * as player 2. there is a time limit to how long the user will wait to
 	 * find an opponent before model gives up.
 	 */
 	private boolean checkStatus() throws InternetConnectionException, InternalErrorException {
 		Log.i("Multiplayer", "begin waiting in queue");
 		long starttime = System.currentTimeMillis();
 		long endtime = starttime + QUEUE_TIMEOUT;
 		while(System.currentTimeMillis() < endtime) {
 			try {
 				match.refresh();    
 				checkIfInMatch();
 				if (!match.getString(info.get("oname")).equals("")) {
 					return true;
 				}
 				Thread.sleep(RECHECK_TIME);
 			} catch (ParseException e) {
 				Log.e("Multiplayer", "parse error while checking for a match", e);
 				throw new InternetConnectionException();
 			} catch (InterruptedException e) {
 				Log.e("Multiplayer", "thread error while sleeping", e);
 				throw new InternalErrorException();
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Using the starting index of the match from online, make the words list array
 	 * with LIST_SIZE words from the Parse database
 	 * 
 	 * @throws InternetConnectionException if disconnected from Internet
 	 * @throws InternalErrorException 
 	 */  
 	public void setWordsList() throws InternetConnectionException, InternalErrorException {
 		Log.i("Multiplayer", "getting words list from parse");
 
 		List<ParseObject> wordObjects = null;
 		try {
 			checkInternet();
 			checkIfInMatch();
 			ParseQuery query; 
			if (Locale.getDefault().getDisplayLanguage().equals("français")) {
 			  query = new ParseQuery("WordsListLatin");
 			} else {
 			  query = new ParseQuery("WordList");
 			}
 			query.setSkip(match.getInt("wordIndex"));
 			query.setLimit(LIST_SIZE);
 			wordObjects= query.find();
 			// if not enough words were in the query than get more words
 			if (wordObjects.size() < LIST_SIZE) {
 			  ParseQuery query2;
		     if (Locale.getDefault().getDisplayLanguage().equals("français")) {
 		        query2 = new ParseQuery("WordsListLatin");
 		      } else {
 		        query2 = new ParseQuery("WordList");
 		      }
 				query2.setLimit(LIST_SIZE - wordObjects.size());
 				wordObjects.addAll(query2.find());
 			}
 		} catch (ParseException e) {
 			Log.e("Multiplayer", "error getting words list from parse", e);
 			throw new InternetConnectionException();
 		}  catch (NullPointerException e) {
 			Log.e("Multiplayer", "unable to connect to internet", e);
 			throw new InternetConnectionException();
 		}
 		// changing words from parse objects into a list of strings.
 		wordsList = new ArrayList<String>();
 		for (ParseObject o : wordObjects) {
 			wordsList.add(o.getString("word"));
 		}
 	}
 
 	/*
 	 * Checks if the user is still in the match they were originally a part of
 	 */
 	private void checkIfInMatch() throws InternalErrorException {
 		if (!match.getString(info.get("name")).equals(name)) {
 			Log.e("Multiplayer", 
 					"got replaced by someone else, concurrency error");
 			throw new InternalErrorException();
 		}
 	}
 
 	/**
 	 * gets the opponent's animal ID from the Parse database
 	 * 
 	 * @throws InternetConnectionException if disconnected from Internet
 	 * @return the int ID of a animal that is selected by the user's opponent
 	 * @throws InternalErrorException 
 	 */  
 	public int getOpponentAnimal() throws InternetConnectionException, InternalErrorException {
 		Log.i("Multiplayer", "getting opponent's animal");
 		try {
 			match.refresh();
 			checkIfInMatch();
 		} catch (ParseException e) {
 			Log.e("Multiplayer", "error getting opponent's animal", e);
 			throw new InternetConnectionException();
 		}
 		return match.getInt(info.get("oanimal"));
 	}
 
 	/**
 	 * The typedLetter method handles what words and letter the user has
 	 * typed so far and notify the view to highlight typed letter or fetch 
 	 * a new word from the wordsList for the view to display accordingly.
 	 * 
 	 * @param letter, the letter that the user typed on the Android soft-keyboard
 	 */
 	public final void typedLetter(final char letter) {
 		// currently not locked on to a word
 		if (currWordIndex == -1) {
 			for (int i = 0; i < wordsDisplayed.length; i++) {
 				// if any of the first character in wordsDisplayed matched letter
 				if (wordsList.get(wordsDisplayed[i]).charAt(0) == letter) {
 					currWordIndex = i;
 					currLetterIndex = 1;
 					setChanged();
 					notifyObservers(States.update.HIGHLIGHT);
 					Log.i("Multiplayer", "typed the letter: " + letter);
 					return;
 				}
 			}
 			// locked on to a word being typed (letter == the index of current letter index in the word)
 		} else if (wordsList.get(wordsDisplayed[currWordIndex]).charAt(currLetterIndex) == letter) {
 
 			// store length of current word
 			int wordLen = wordsList.get(wordsDisplayed[currWordIndex]).trim().length();
 			Log.i("Multiplayer", "typed the letter: " + letter);
 
 			// word is completed after final letter is typed
 			if ((currLetterIndex + 1) >= wordLen) {
 				int curScore = match.getInt(info.get("score"));
 				curScore += wordLen;
 				Log.i("Multiplayer", "completed the word: " + 
 						wordsList.get(wordsDisplayed[currWordIndex]) 
 						+ "\nscore increased to: " + curScore);
 
 				match.put(info.get("score"), curScore);
 				match.saveInBackground();
 				updateWordsDisplayed();
 				currLetterIndex = -1;
 				currWordIndex = -1;
 			} else {
 				currLetterIndex += 1;
 				setChanged();
 				notifyObservers(States.update.HIGHLIGHT);
 			}
 			return;
 		}
 
 		// wrong letter typed
 		Log.i("Multiplayer", "typed the wrong letter: " + letter);
 		setChanged();
 		notifyObservers(States.update.WRONG_LETTER);
 	}
 
 	/**
 	 * sets the user to being done with the match
 	 * @throws InternetConnectionException 
 	 *
 	 */
 	public final void setUserFinish() throws InternetConnectionException {
 		Log.i("Multiplayer", "player set to finished");
 		try {
 			checkInternet();
 			match.put(info.get("finished"), true);
 			match.save();
 		} catch (ParseException e) {
 			if ("p1name".equals(info.get("name"))) {
 				Log.w("Multiplayer", "parse error setting user to finish");
 				throw new InternetConnectionException();
 			} 
 		} catch (NullPointerException e) {
 			Log.w("Multiplayer", "unable to connect to internet");
 			throw new InternetConnectionException();
 		}
 	}
 
 	/**
 	 * checks to see if the opponent is finished for a maximum of 
 	 * SCORE_TIMEOUT milliseconds. If the opponent is finished then
 	 * return true, false otherwise
 	 * 
 	 * @throws InternetConnectionException if disconnected from Internet
 	 * @throws InternalErrorException if there was an internal error
 	 * @return true if opponent is done, false otherwise
 	 */
 	public final boolean isOpponentFinished() throws 
 	InternetConnectionException, InternalErrorException {
 		Log.i("Multiplayer", "waiting on opponent to finish");
 		long starttime = System.currentTimeMillis();
 		long endtime = starttime + SCORE_TIMEOUT;
 		while(System.currentTimeMillis() < endtime) {
 			try {
 				match.refresh();
 				checkIfInMatch();
 				Thread.sleep(RECHECK_TIME);
 			} catch (ParseException e) {
 				if ("p1name".equals(info.get("name"))) {
 					Log.w("Multiplayer", 
 							"parse error while checking if opponent is finished or not");
 					throw new InternetConnectionException();
 				}
 				return false;
 			} catch (InterruptedException e) {
 				Log.e("Multiplayer", "thread error while sleeping", e);
 				throw new InternalErrorException();
 			}
 			if (match.getBoolean(info.get("ofinished"))) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * deletes the match from the database if this user is player 1
 	 * @throws InternetConnectionException 
 	 */
 	public void deleteUser() throws InternetConnectionException {
 		try {
 			checkInternet();
 			if ("p1name".equals(info.get("name"))) {
 				Log.i("Multiplayer", "deleted match from database");
 				match.delete();
 			}
 		} catch (ParseException e) {
 			Log.e("Multiplayer", "error deleting match from parse", e);
 		}
 	}
 
 	private void checkInternet() throws InternetConnectionException, ParseException {
 		try {
 			ParseQuery query = new ParseQuery("Matches");
 			query.count();
 		} catch (NullPointerException e) {
 			Log.w("Multiplayer", "unable to connect to internet");
 			throw new InternetConnectionException();
 		}
 	}
 
 	/**
 	 * refreshes the match in a background thread and notifies the
 	 * UI to update the opponent score after the refresh is done.
 	 * 
 	 * @modifies this
 	 */
 	public void refreshInBackground() {
 		match.refreshInBackground(new RefreshCallback() {
 			public void done(ParseObject object, ParseException e) {
 				if (e == null) {
 					setChanged();
 					notifyObservers(States.update.OPPONENT_SCORE);
 				}
 			}
 		});
 	}
 
 	/**
 	 * @return user name of the user's opponent
 	 */
 	public final String getOpponentName() {
 		return match.getString(info.get("oname"));
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public final int getScore() {
 		return match.getInt(info.get("score"));
 	}
 
 	/**
 	 * @return current score of the user's opponent
 	 */
 	public final int getOpponentScore() {
 		return match.getInt(info.get("oscore"));
 	}
 
 	/**
 	 * @return the words list of all the words
 	 */
 	public final List<String> getWordsList() {
 		return wordsList;
 	}
 }
