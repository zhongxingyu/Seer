 /**
  * Chess Clock App
  * https://github.com/japtar10101/Chess-Clock-App
  * 
  * The MIT License
  * 
  * Copyright (c) 2011 Taro Omiya
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package com.japtar.chessclock.models;
 
 import java.security.InvalidParameterException;
 
 import android.content.SharedPreferences;
 
 import com.japtar.chessclock.Global;
 import com.japtar.chessclock.enums.DelayMode;
 import com.japtar.chessclock.enums.TimerCondition;
 
 /**
  * Model indicating the game's current (or paused) conditions.
  * @author japtar10101
  */
 public class GameStateModel implements SaveStateModel {
 	/* ===========================================================
 	 * Listeners
 	 * =========================================================== */
 	/**
 	 * Listener for when a player's time increases based on game rules
 	 * @author japtar10101
 	 */
 	public interface OnTimeIncreasedListener {
 		public void onTimeIncreased(boolean leftPlayersTime, TimeModel increase);
 	}
 	
 	/* ===========================================================
 	 * Constants
 	 * =========================================================== */
 	// == Stored key values ==
 	// == Conditionals ==
 	/** The saved key value for who's turn it is */
 	public static final String KEY_LEFT_PLAYERS_TURN = "leftPlayersTurn";
 	/** The saved key value for whether the left player is white or not */
 	public static final String KEY_LEFT_IS_WHITE = "leftIsWhite";
 	/** The saved key value for the timer condition */
 	public static final String KEY_TIMER_CONDITION = "timerCondition";
 	
 	// == Player's time ==
 	/** The saved key value for left player's time */
 	public static final String KEY_LEFT_PLAYER = "leftPlayersTime";
 	/** The saved key value for right player's time */
 	public static final String KEY_RIGHT_PLAYER = "rightPlayersTime";
 	
 	// == Player's delay time ==
 	/** The saved key value for delay time */
 	public static final String KEY_LEFT_DELAY_TIME = "gameDelayTime";
 	/** The saved key value for delay time */
 	public static final String KEY_RIGHT_DELAY_TIME = "rightPlayerDelayTime";
 	
 	// == Number of Player's moves ==
 	/** The saved key value for number of moves */
 	public static final String KEY_LEFT_NUM_MOVES = "leftNumMoves";
 	/** The saved key value for number of moves */
 	public static final String KEY_RIGHT_NUM_MOVES = "rightNumMoves";
 	
 	/* ===========================================================
 	 * Members
 	 * =========================================================== */
 	/** prepend string for delay time */
 	private String mDelayPrependString = null;
 	
 	// == Conditionals ==
 	/** Flag indicating whose turn it is */
 	public boolean leftPlayersTurn = false;
 	/** Flag indicating whose turn it is */
 	public boolean leftIsWhite = false;
 	/** The current timer's condition */
 	public byte timerCondition = TimerCondition.STARTING;
 	
 	// == Player's time ==
 	/** Left player's time */
 	private final TimeModel mLeftPlayersTime = new TimeModel();
 	/** Right player's time */
 	private final TimeModel mRightPlayersTime = new TimeModel();
 	
 	// == Player's delay time ==
 	/** Time of delay */
 	private final TimeModel mLeftPlayerDelayTime = new TimeModel();
 	/** Time of delay */
 	private final TimeModel mRightPlayerDelayTime = new TimeModel();
 	
 	// == Player's moves ==
 	/** Number of moves the left player made */
 	public int numLeftPlayerMoves = 0;
 	/** Number of moves the right player made */
 	public int numRightPlayerMoves = 0;
 	
 	// == Listener ==
 	private OnTimeIncreasedListener mListener = null;
 	
 	/* ===========================================================
 	 * Override Methods
 	 * =========================================================== */
 	/**
 	 * Saves the current settings to a SharedPreferences
 	 * @param saveState the SharedPreferences to save this app's options
 	 * @see SharedPreferences
 	 */
 	@Override
 	public void saveSettings(final SharedPreferences saveState) throws
 			IllegalArgumentException {
 		// Make sure the parameter isn't null
 		if(saveState == null) {
 			// If so, complain
 			throw new IllegalArgumentException("saveState cannot be null");
 		}
 		
 		// Grab the editor, and start saving
 		final SharedPreferences.Editor saveEditor = saveState.edit();
 		
 		// First, save all the time variables
 		mLeftPlayersTime.saveTime(saveEditor, KEY_LEFT_PLAYER);
 		mRightPlayersTime.saveTime(saveEditor, KEY_RIGHT_PLAYER);
 		mLeftPlayerDelayTime.saveTime(saveEditor, KEY_LEFT_DELAY_TIME);
 		mRightPlayerDelayTime.saveTime(saveEditor, KEY_RIGHT_DELAY_TIME);
 		
 		// Who's turn it is
 		saveEditor.putBoolean(KEY_LEFT_PLAYERS_TURN, leftPlayersTurn);
 		saveEditor.putBoolean(KEY_LEFT_IS_WHITE, leftIsWhite);
 		
 		// The value of timerCondition
 		saveEditor.putInt(KEY_TIMER_CONDITION, timerCondition);
 		
 		// Number of moves for each player
 		saveEditor.putInt(KEY_LEFT_NUM_MOVES, numLeftPlayerMoves);
 		saveEditor.putInt(KEY_RIGHT_NUM_MOVES, numRightPlayerMoves);
 		
 		// Write it in!
 		saveEditor.commit();
 	}
 	
 	/**
 	 * Saves the current settings to a SharedPreferences
 	 * @param savedState the SharedPreferences to save this app's options
 	 * @see SharedPreferences
 	 * @param savedState shared Preference to save to
 	 */
 	@Override
 	public void recallSettings(final SharedPreferences savedState) throws
 			InvalidParameterException, IllegalArgumentException {
 		
 		// Make sure the parameter isn't null
 		if(savedState == null) {
 			// If so, complain
 			throw new IllegalArgumentException("savedState cannot be null");
 		}
 		
 		// Recall the attributes from the preference
 		// First, the time variables
 		mLeftPlayersTime.recallTime(savedState, KEY_LEFT_PLAYER,
 				OptionsModel.DEFAULT_TIME_LIMIT);
 		mRightPlayersTime.recallTime(savedState, KEY_RIGHT_PLAYER,
 				OptionsModel.DEFAULT_TIME_LIMIT);
 		mLeftPlayerDelayTime.recallTime(savedState, KEY_LEFT_DELAY_TIME,
 				OptionsModel.DEFAULT_DELAY_TIME);
 		
 		// For backward compatibility, if this value is not recalled,
 		// set it the same delay time as the left player
 		mRightPlayerDelayTime.recallTime(savedState, KEY_RIGHT_DELAY_TIME,
 				TimeModel.totalSeconds(mLeftPlayerDelayTime));
 		
 		// Who's turn it is...
 		leftPlayersTurn = savedState.getBoolean(KEY_LEFT_PLAYERS_TURN, true);
 		leftIsWhite = savedState.getBoolean(KEY_LEFT_IS_WHITE, true);
 		
 		// Number of moves for each player
 		numLeftPlayerMoves = savedState.getInt(KEY_LEFT_NUM_MOVES, 0);
 		numRightPlayerMoves = savedState.getInt(KEY_RIGHT_NUM_MOVES, 0);
 		
 		// Lastly, the game's state
 		timerCondition = TimeModel.intToByte(
 				savedState.getInt(KEY_TIMER_CONDITION, 0));
 		if(timerCondition >= TimerCondition.NUM_CONDITIONS) {
 			timerCondition = TimerCondition.STARTING;
 		}
 	}
 	
 	/* ===========================================================
 	 * Public Methods
 	 * =========================================================== */
 	/**
 	 * TODO: add a description
 	 * @param listener
 	 */
 	public void setTimeIncrementListener(final OnTimeIncreasedListener listener) {
 		mListener = listener;
 	}
 	
 	/**
 	 * Decrements time.
 	 * @param numSeconds number of seconds to decrement
 	 * @return true if either player's time is up.
 	 */
 	public boolean decrementTime(final int numSeconds) {
 		boolean toReturn = false;
 		
 		// Change the delay behavior based on mode
 		switch(Global.OPTIONS.delayMode) {
 			case DelayMode.BASIC:
 				toReturn = decrementBasicDelay(numSeconds);
 				break;
 			case DelayMode.HOUR_GLASS:
 				toReturn = decrementHourGlass(numSeconds);
 				break;
 			case DelayMode.BRONSTEIN:
 				toReturn = decrementBronstein(numSeconds);
 				break;
 			default:
 				toReturn = decrementFischer(numSeconds);
 				break;
 		}
 		return toReturn;
 	}
 	
 	/**
 	 * TODO: add a description
 	 * @param leftPlayerIsNext
 	 */
 	public void switchTurns(final boolean leftPlayerIsNext) {
 		// If clicked by right/left player button,
 		// update the current player
 		leftPlayersTurn = leftPlayerIsNext;
 		
 		if(leftPlayersTurn) {
 			// It's left player's turn
 			// Increment right player's move count
 			++numRightPlayerMoves;
 		} else {
 			// It's left player's turn
 			// Increment right player's move count
 			++numLeftPlayerMoves;
 		}
 		
 		// Determine which player's time to increment
 		TimeModel increment = null, delayTime = null;
 		boolean increaseLeftPlayersTime = false;
 		switch(Global.OPTIONS.delayMode) {
 			case DelayMode.FISCHER:
 				
 				// Set incrementTime to the next player's time
 				increaseLeftPlayersTime = leftPlayersTurn;
 				increment = mRightPlayersTime;
 				delayTime = mRightPlayerDelayTime;
 				if(increaseLeftPlayersTime) {
 					increment = mLeftPlayersTime;
 					delayTime = mLeftPlayerDelayTime;
 				}
 				break;
 			case DelayMode.FISCHER_AFTER:
 			case DelayMode.BRONSTEIN:
 				
 				// Make sure the game is actually running first
 				// (this prevents the first player from gaining time)
 				if(timerCondition == TimerCondition.RUNNING) {
 					
 					// Set incrementTime to the last player's time
 					increaseLeftPlayersTime = !leftPlayersTurn;
 					increment = mRightPlayersTime;
 					delayTime = mRightPlayerDelayTime;
 					if(increaseLeftPlayersTime) {
 						increment = mLeftPlayersTime;
 						delayTime = mLeftPlayerDelayTime;
 					}
 				}
 				break;
 		}
 		
 		// Increment the time
 		if((increment != null) && (delayTime != null)) {
 			increment.incrementTime(delayTime);
 			
 			// Call the listener event
 			if(mListener != null) {
 				mListener.onTimeIncreased(
 						increaseLeftPlayersTime, delayTime);
 			}
			
			// Revert the delay
			if(Global.OPTIONS.delayMode == DelayMode.BRONSTEIN) {
				this.resetDelay();
			}
 		}
 	}
 	
 	/**
 	 * Resets the internal game time to option's settings
 	 */
 	public void resetTime() {
 		// Determine which time corresponds to which player
 		final TimeModel leftTime, rightTime;
 		if(leftIsWhite) {
 			leftTime = Global.OPTIONS.savedWhiteTimeLimit;
 			rightTime = Global.OPTIONS.savedBlackTimeLimit;
 		} else {
 			leftTime = Global.OPTIONS.savedBlackTimeLimit;
 			rightTime = Global.OPTIONS.savedWhiteTimeLimit;
 		}
 		
 		// Revert the move count to 0
 		numLeftPlayerMoves = 0;
 		numRightPlayerMoves = 0;
 		
 		// Revert all the time to Option's settings
 		mLeftPlayersTime.setTime(leftTime);
 		mRightPlayersTime.setTime(rightTime);
 		this.resetDelay();
 	}
 	
 	/**
 	 * Resets the internal delay time to option's settings
 	 */
 	public void resetDelay() {
 		if(Global.OPTIONS.delayMode == DelayMode.BRONSTEIN) {
 			// Set both delay to 0
 			mLeftPlayerDelayTime.setTime(0, 0);
 			mRightPlayerDelayTime.setTime(0, 0);
 		} else {
 			// Determine which time corresponds to which player
 			final TimeModel leftTime, rightTime;
 			if(leftIsWhite) {
 				leftTime = Global.OPTIONS.savedWhiteDelayTime;
 				rightTime = Global.OPTIONS.savedBlackDelayTime;
 			} else {
 				leftTime = Global.OPTIONS.savedBlackDelayTime;
 				rightTime = Global.OPTIONS.savedWhiteDelayTime;
 			}
 			
 			// Set the delay time
 			mLeftPlayerDelayTime.setTime(leftTime);
 			mRightPlayerDelayTime.setTime(rightTime);
 		}
 	}
 	
 	/**
 	 * @return left player's current time, in text
 	 */
 	public String leftPlayerTime() {
 		return mLeftPlayersTime.toString();
 	}
 	
 	/**
 	 * @return right player's current time, in text
 	 */
 	public String rightPlayerTime() {
 		return mRightPlayersTime.toString();
 	}
 	
 	/**
 	 * @return If the delay time is zero, returns null.
 	 * Otherwise, returns the current delay time, in text.
 	 */
 	public String delayTime() {
 		// Check if the right mode is on.  If not, return null.
 		if(Global.OPTIONS.delayMode != DelayMode.BASIC) {
 			return null;
 		}
 		
 		// Get the delay time for the current player
 		TimeModel delayTime = mRightPlayerDelayTime;
 		if(leftPlayersTurn) {
 			delayTime = mLeftPlayerDelayTime;
 		}
 		
 		// If this time is 0, return null
 		if(delayTime.isTimeZero()) {
 			return null;
 		}
 		
 		// Calculate the total seconds to delay
 		final Integer seconds = delayTime.getMinutes() * 60 +
 				delayTime.getSeconds();
 		
 		// Prepend the string, if available
 		if(mDelayPrependString != null) {
 			return mDelayPrependString + ' ' + seconds.toString();
 		} else {
 			return seconds.toString();
 		}
 	}
 	
 	/**
 	 * @return A default delay label string
 	 */
 	public String defaultDelayLabelString() {
 		// Prepend the string, if available
 		if(mDelayPrependString != null) {
 			return mDelayPrependString + " 0";
 		} else {
 			return "0";
 		}
 	}
 	
 	/* ===========================================================
 	 * Setters
 	 * =========================================================== */
 	/**
 	 * @param prepend sets {@link mDelayPrependString}
 	 */
 	public void setDelayPrependString(final String prepend) {
 		mDelayPrependString = prepend;
 	}
 	
 	/* ===========================================================
 	 * Private/Protected Methods
 	 * =========================================================== */
 	
 	/**
 	 * TODO: add a description
 	 * @param numSeconds
 	 * @return
 	 */
 	private boolean decrementBronstein(final int numSeconds) {
 		boolean toReturn = false;
 		// Figure out which time to decrement
 		final TimeModel updateDelay, maximumDelay, updateTime;
 		if(leftPlayersTurn) {
 			// It's left players turn.  Grab left player's specs.
 			updateTime = mLeftPlayersTime;
 			updateDelay = mLeftPlayerDelayTime;
 			
 			// Check if the left player is white
 			if(leftIsWhite) {
 				// If so, grab its max delay value
 				maximumDelay = Global.OPTIONS.savedWhiteDelayTime;
 			} else {
 				// If so, grab its max delay value
 				maximumDelay = Global.OPTIONS.savedBlackDelayTime;
 			}
 		} else {
 			// It's right players turn.  Grab left player's specs.
 			updateTime = mRightPlayersTime;
 			updateDelay = mRightPlayerDelayTime;
 			
 			// Check if the left player is black
 			if(!leftIsWhite) {
 				// If so, grab the right player's max delay value
 				maximumDelay = Global.OPTIONS.savedWhiteDelayTime;
 			} else {
 				// If so, grab the right player's max delay value
 				maximumDelay = Global.OPTIONS.savedBlackDelayTime;
 			}
 		}
 		
 		// Decrement time from player
 		for(int second = 0; second < numSeconds; ++second) {
 			
 			// Decrementing the player's time
 			if(updateTime.decrementASecond()) {
 				
 				// If that succeeded, and the player's delay time is less than
 				// the maximum limit, increment the delay time
 				if(updateDelay.compareTo(maximumDelay) < 0) {
 					updateDelay.incrementASecond();
 				}
 			} else {
 				
 				// If that failed, times up!
 				toReturn = true;
 				break;
 			}
 		}
 		return toReturn;
 	}
 	
 	/**
 	 * TODO: add a description
 	 * @param numSeconds
 	 * @return
 	 */
 	private boolean decrementFischer(final int numSeconds) {
 		boolean toReturn = false;
 		// Figure out which time to decrement
 		TimeModel updateTime = mRightPlayersTime;
 		if(leftPlayersTurn) {
 			updateTime = mLeftPlayersTime;
 		}
 		
 		// Decrement time from player
 		for(int second = 0; second < numSeconds; ++second) {
 			
 			// Try decrementing the player's time
 			if(!updateTime.decrementASecond()) {
 				
 				// If that failed, times up!
 				toReturn = true;
 				break;
 			}
 		}
 		return toReturn;
 	}
 	
 	/**
 	 * TODO: add a description
 	 * @param numSeconds
 	 * @return
 	 */
 	private boolean decrementHourGlass(final int numSeconds) {
 		boolean toReturn = false;
 		// Figure out which time to decrement, and which to increment
 		final TimeModel downTime, upTime;
 		if(leftPlayersTurn) {
 			downTime = mLeftPlayersTime;
 			upTime = mRightPlayersTime;
 		} else {
 			downTime = mRightPlayersTime;
 			upTime = mLeftPlayersTime;
 		}
 		
 		// Decrement time from delay or player
 		for(int second = 0; second < numSeconds; ++second) {
 				
 			// Increment the opposing player's time
 			upTime.incrementASecond();
 			
 			// Try decrementing the player's time
 			if(!downTime.decrementASecond()) {
 				
 				// If that failed, times up!
 				toReturn = true;
 				break;
 			}
 		}
 		return toReturn;
 	}
 	
 	/**
 	 * TODO: add a description
 	 * @param numSeconds
 	 * @return
 	 */
 	private boolean decrementBasicDelay(final int numSeconds) {
 		boolean toReturn = false;
 		// Figure out which time to decrement
 		TimeModel updateTime = mRightPlayersTime;
 		TimeModel delayTime = mRightPlayerDelayTime;
 		if(leftPlayersTurn) {
 			updateTime = mLeftPlayersTime;
 			delayTime = mLeftPlayerDelayTime;
 		}
 		
 		// Decrement time from delay or player
 		for(int second = 0; second < numSeconds; ++second) {
 			
 			// First, attempt to decrement time from delay
 			if(!delayTime.decrementASecond()) {
 				
 				// If failed, try decrementing the player's time
 				if(!updateTime.decrementASecond()) {
 					
 					// If that failed, times up!
 					toReturn = true;
 					break;
 				}
 			}
 		}
 		return toReturn;
 	}
 }
