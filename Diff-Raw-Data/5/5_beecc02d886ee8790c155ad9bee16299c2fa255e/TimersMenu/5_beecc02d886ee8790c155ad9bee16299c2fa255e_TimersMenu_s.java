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
 package com.japtar.chessclock.menus;
 
 import android.content.Context;
 import android.media.AudioManager;
 import android.media.Ringtone;
 import android.media.RingtoneManager;
 import android.os.Handler;
 import android.view.HapticFeedbackConstants;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.view.animation.Animation.AnimationListener;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
 import com.japtar.chessclock.Global;
 import com.japtar.chessclock.MainActivity;
 import com.japtar.chessclock.R;
 import com.japtar.chessclock.enums.TimerCondition;
 import com.japtar.chessclock.gui.OutlinedTextView;
 import com.japtar.chessclock.models.GameStateModel;
 import com.japtar.chessclock.models.TimeModel;
 
 /**
  * Menu for Timer
  * @author japtar10101
  */
 public class TimersMenu implements MenuInterface,
 		View.OnClickListener, View.OnTouchListener,
 		GameStateModel.OnTimeIncreasedListener {
 	/* ===========================================================
 	 * Members
 	 * =========================================================== */
 	/** The main activities class */
 	private final MainActivity mParentActivity;
 
 	// == Timer ==
 	/** The handler running {@link mTask} */
 	private final Handler mTimer;
 	/** Task that decrements the timer */
 	private final DecrementTimerTask mTask;
 	
 	// == Sub-Menus ==
 	/** Helper class taking care of the Start Menu functionality */
 	private final SubMenu mStartMenu;
 	/** Helper class taking care of the Pause Menu functionality */
 	private final SubMenu mPauseMenu;
 	/** Helper class taking care of the Times-Up Menu functionality */
 	private final SubMenu mTimesUpMenu;
 	
 	// == Buttons ==
 	/** Left player's button */
 	private ImageButton mLeftButton = null;
 	/** Right player's button */
 	private ImageButton mRightButton = null;
 	/** Pause button */
 	private Button mPauseButton = null;
 		
 	// == Labels ==
 	/** Label indicating delay */
 	private TextView mDelayLabel = null;
 	/** Left player's label indicating remaining time */
 	private OutlinedTextView mLeftLabel = null;
 	/** Right player's label indicating remaining time */
 	private OutlinedTextView mRightLabel = null;
 	/** Left player's label indicating time increase */
 	private OutlinedTextView mLeftIncreaseLabel = null;
 	/** Right player's label indicating time increase */
 	private OutlinedTextView mRightIncreaseLabel = null;
 	/** Left player's label indicating move count */
 	private OutlinedTextView mLeftMoveLabel = null;
 	/** Right player's label indicating move count */
 	private OutlinedTextView mRightMoveLabel = null;
 	
 	// == Animations ==
 	/** Shows the delay label */
 	private Animation mShowAnimation = null;
 	/** Hides the delay label */
 	private Animation mHideAnimation = null;
 	/** Hides the delay label */
 	private Animation mLeftIncreaseAnimation = null;
 	private Animation mRightIncreaseAnimation = null;
 	private final AnimationListener mShowDelayLabel;
 	private final AnimationListener mHideDelayLabel;
 	private final AnimationListener mIncreaseLeftLabel;
 	private final AnimationListener mIncreaseRightLabel;
 	
 	// == Misc. ==
 	/** Sound of alarm */
 	private Ringtone mRingtone = null;
 	private int mMoveStringLength = 0;
 	private final StringBuffer mStringGenerator = new StringBuffer();
 	
 	/* ===========================================================
 	 * Constructors
 	 * =========================================================== */
 	/**
 	 * @param parent the menu's parent activity
 	 */
 	public TimersMenu(final MainActivity parent) {
 		// Setup activity
 		mParentActivity = parent;
 		
 		// Setup the timer-related stuff
 		mTimer = new Handler();
 		mTask = new DecrementTimerTask(this, mTimer);
 		
 		// Setup the pause menu
 		mPauseMenu = new PauseSubMenu(mParentActivity, this);
 		mStartMenu = new StartSubMenu(mParentActivity, this);
 		mTimesUpMenu = new TimesUpSubMenu(mParentActivity, this);
 		
 		// Setup animation listeners
 		mShowDelayLabel = new AnimationListener() {
 			@Override
 			public void onAnimationStart(Animation animation) {
 				if(mDelayLabel != null) {
 					mDelayLabel.setVisibility(View.VISIBLE);
 				}
 			}
 			@Override
 			public void onAnimationEnd(Animation animation) { }
 			@Override
 			public void onAnimationRepeat(Animation animation) { }
 		};
 		mHideDelayLabel = new AnimationListener() {
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				if(mDelayLabel != null) {
 					mDelayLabel.setVisibility(View.INVISIBLE);
 				}
 			}
 			@Override
 			public void onAnimationStart(Animation animation) { }
 			@Override
 			public void onAnimationRepeat(Animation animation) { }
 		};
 		mIncreaseLeftLabel = new AnimationListener() {
 			@Override
 			public void onAnimationStart(Animation animation) {
 				if(mLeftIncreaseLabel != null) {
 					mLeftIncreaseLabel.setVisibility(View.VISIBLE);
 				}
 			}
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				if(mLeftIncreaseLabel != null) {
 					mLeftIncreaseLabel.setVisibility(View.INVISIBLE);
 				}
 			}
 			@Override
 			public void onAnimationRepeat(Animation animation) { }
 		};
 		mIncreaseRightLabel = new AnimationListener() {
 			@Override
 			public void onAnimationStart(Animation animation) {
 				if(mRightIncreaseLabel != null) {
 					mRightIncreaseLabel.setVisibility(View.VISIBLE);
 				}
 			}
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				if(mRightIncreaseLabel != null) {
 					mRightIncreaseLabel.setVisibility(View.INVISIBLE);
 				}
 			}
 			@Override
 			public void onAnimationRepeat(Animation animation) { }
 		};
 	}
 
 	/* ===========================================================
 	 * Overrides
 	 * =========================================================== */
 	/**
 	 * Updates the player's turns, based on the button clicked
 	 * @see android.view.View.OnClickListener#onClick(android.view.View)
 	 */
 	@Override
 	public void onClick(final View v) {
 		// Stop the handler
 		mTimer.removeCallbacks(mTask);
 		
 		if(v != null) {
 			// Check the sub-menus first.
 			// If any are shown, have them update the layouts
 			if(mStartMenu.isMenuShown()) {
 				mStartMenu.onClick(v);
 			} else if(mPauseMenu.isMenuShown()) {
 				mPauseMenu.onClick(v);
 			} else if(mTimesUpMenu.isMenuShown()) {
 				mTimesUpMenu.onClick(v);
 			}
 			
 			// Check if the pause button was click
 			else if(v.equals(mPauseButton)) {
 				// We're pausing the game
 				this.paused();
 			} else {
 				// Check which game button was pressed
 				final boolean leftPlayersTurn = v.equals(mRightButton);
 				if(leftPlayersTurn || v.equals(mLeftButton)) {
 					// Update the player's turn
 					this.updatePlayersTurn(leftPlayersTurn);
 					
 					// Resume the game
 					this.resume();
 				}
 			}
 		}
 	}
 	
 	/**
 	 * @param arg0
 	 * @param arg1
 	 */
 	@Override
 	public boolean onTouch(final View v, final MotionEvent event) {
 		// Make sure if the vibration is enabled or not
 		if(Global.OPTIONS.enableVibrate && (v != null) &&
 				(event.getAction() == MotionEvent.ACTION_DOWN)) {
 			// Play haptic feedback on mouse down
 			v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
 		}
 		return false;
 	}
 	
 	/**
 	 * Resumes the timer if paused, or starts it over.
 	 */
 	public void setupMenu() {
 		// First, setup the UI
 		mParentActivity.setContentView(R.layout.main);
 		
 		// Stop the handler
 		mTimer.removeCallbacks(mTask);
 
 		// == Load up all the member variables ==
 		this.setupMemberVariables();
 		
 		// == Setup the member variables ==
 		
 		// Update the haptic feedback
 		mLeftButton.setHapticFeedbackEnabled(Global.OPTIONS.enableVibrate);
 		mRightButton.setHapticFeedbackEnabled(Global.OPTIONS.enableVibrate);
 		
 		// Update the outlined text position
 		final int leftWidth = mLeftButton.getWidth();
 		final int rightWidth = mRightButton.getWidth();
 		mLeftLabel.setWidth(leftWidth);
 		mRightLabel.setWidth(rightWidth);
 		mLeftIncreaseLabel.setWidth(leftWidth);
 		mRightIncreaseLabel.setWidth(rightWidth);
 		mLeftMoveLabel.setWidth(leftWidth);
 		mRightMoveLabel.setWidth(rightWidth);
 		
 		// Update the text size on everything
 		mLeftLabel.setTextSize(MainActivity.msTextSize);
 		mRightLabel.setTextSize(MainActivity.msTextSize);
 		mPauseButton.setTextSize(MainActivity.msTextSize * 0.7f);
 		mDelayLabel.setTextSize(MainActivity.msTextSize * 0.7f);
 		mLeftIncreaseLabel.setTextSize(MainActivity.msTextSize * 0.6f);
 		mRightIncreaseLabel.setTextSize(MainActivity.msTextSize * 0.6f);
 		mLeftMoveLabel.setTextSize(MainActivity.msTextSize * 0.4f);
 		mRightMoveLabel.setTextSize(MainActivity.msTextSize * 0.4f);
 		
 		// Update the sub menu
 		mStartMenu.setupMenu();
 		mPauseMenu.setupMenu();
 		mTimesUpMenu.setupMenu();
 		
 		// Update the animations
 		mShowAnimation.setAnimationListener(mShowDelayLabel);
 		mHideAnimation.setAnimationListener(mHideDelayLabel);
 		mLeftIncreaseAnimation.setAnimationListener(mIncreaseLeftLabel);
 		mRightIncreaseAnimation.setAnimationListener(mIncreaseRightLabel);
 		
 		// == Determine the game state to jump to ==
 		Global.GAME_STATE.setTimeIncrementListener(this);
 		
 		// Determine the condition to begin this game at
 		switch(Global.GAME_STATE.timerCondition) {
 			case TimerCondition.TIMES_UP:
 			case TimerCondition.STARTING:
 				// Run the startup function
 				this.startup();
 				break;
 			default:
 				// Figure out which color to set each player
 				if(Global.GAME_STATE.leftIsWhite) {
 					mLeftButton.setImageResource(R.drawable.white_button);
 					mRightButton.setImageResource(R.drawable.black_button);
 				} else {
 					mLeftButton.setImageResource(R.drawable.black_button);
 					mRightButton.setImageResource(R.drawable.white_button);
 				}
 				
 				// Run the pause function
 				this.paused();
 				break;
 		}
 	}
 	
 	/**
 	 * TODO: add a description
 	 * @see com.japtar.chessclock.models.GameStateModel.OnTimeIncreasedListener#onTimeIncreased(boolean, com.japtar.chessclock.models.TimeModel)
 	 */
 	public void onTimeIncreased(final boolean leftPlayersTime, final TimeModel increase) {
 		// Show the label only if there's some time to increment
 		if(!increase.isTimeZero()) {
 			// Determine which label to animate
 			final TextView animateLabel;
 			final Animation animation;
 			if(leftPlayersTime) {
 				animation = mLeftIncreaseAnimation;
 				animateLabel = mLeftIncreaseLabel;
 			} else {
 				animation = mRightIncreaseAnimation;
 				animateLabel = mRightIncreaseLabel;
 			}
 			
 			// Update this label's text, and start its animation
 			animateLabel.setText('+' + increase.toString());
 			animateLabel.startAnimation(animation);
 		}
 	}
 	
 	/* ===========================================================
 	 * Public Methods
 	 * =========================================================== */
 	/**
 	 * Pauses the timer, unless the time is up.
 	 */
 	public void exitMenu() {
 		// Stop the time handler
 		mTimer.removeCallbacks(mTask);
 		if(mRingtone != null) {
 			mRingtone.stop();
 		}
 		
 		// Set the option's state
 		switch(Global.GAME_STATE.timerCondition) {
 			case TimerCondition.TIMES_UP:
 			case TimerCondition.STARTING:
 				Global.GAME_STATE.timerCondition = TimerCondition.STARTING;
 				break;
 			default:
 				Global.GAME_STATE.timerCondition = TimerCondition.PAUSE;
 				break;
 		}
 	}
 	
 	/* ===========================================================
 	 * Package-space Methods
 	 * =========================================================== */
 	/**
 	 * Updates the text on {@link mLeftButton}, {@link mRightButton},
 	 * and {@link mDelayLabel}
 	 */
 	void updateButtonAndLabelText() {
 		// Update button texts
 		mLeftLabel.setText(Global.GAME_STATE.leftPlayerTime());
 		mRightLabel.setText(Global.GAME_STATE.rightPlayerTime());
 		
 		// Update the delay label's text or visibility
 		if((Global.GAME_STATE.timerCondition == TimerCondition.RUNNING) ||
 				(Global.GAME_STATE.timerCondition == TimerCondition.PAUSE)) {
 			// Update the delay label's text or visibility
 			String delayText = Global.GAME_STATE.delayTime();
 			if(delayText == null) {
 				// If no text is provided, play the hide animation
 				if(mDelayLabel.getVisibility() == View.VISIBLE) {
 					mDelayLabel.startAnimation(mHideAnimation);
 				}
 				// Update the delay text
 				delayText = Global.GAME_STATE.defaultDelayLabelString();
			} else if(mDelayLabel.getVisibility() == View.INVISIBLE) {
 				// If text IS provided, play the show-label animation
 				mDelayLabel.startAnimation(mShowAnimation);
 			}
 			
 			// Update the move counts
 			if(Global.OPTIONS.displayMoveCount) {
 				// Hide the move count
 				mLeftMoveLabel.setVisibility(View.VISIBLE);
 				mRightMoveLabel.setVisibility(View.VISIBLE);
 				
 				// Update Left player's move count texts
 				mStringGenerator.delete(mMoveStringLength, mStringGenerator.length());
 				mStringGenerator.append(Global.GAME_STATE.numLeftPlayerMoves);
 				mLeftMoveLabel.setText(mStringGenerator.toString());
 				
 				// Update Left player's move count texts
 				mStringGenerator.delete(mMoveStringLength, mStringGenerator.length());
 				mStringGenerator.append(Global.GAME_STATE.numRightPlayerMoves);
 				mRightMoveLabel.setText(mStringGenerator.toString());
 			}
 			
 			// Update the text
 			mDelayLabel.setText(delayText);
 		}
 	}
 	
 	/**
 	 * TODO: add a description
 	 * @param v
 	 * @param buttonId
 	 * @return
 	 */
 	Button getButton(final View v, final int buttonId) {
 		// Find a view based on ID
 		final View foundView = v.findViewById(buttonId);
 		return this.convertToButton(foundView);
 	}
 	
 	/**
 	 * Indicate the game just started
 	 */
 	void startup() {
 		// Stop the ringtone
 		if(mRingtone != null) {
 			mRingtone.stop();
 		}
 
 		// Set the condition to time up
 		Global.GAME_STATE.timerCondition = TimerCondition.STARTING;
 		
 		// Make both buttons display a neutral piece
 		mLeftButton.setImageResource(R.drawable.neutral_button);
 		mRightButton.setImageResource(R.drawable.neutral_button);
 		
 		// Show the start menu
 		mStartMenu.showMenu();
 		
 		// Do whatever else is necessary
 		this.changeConditionSetup();
 		
 		// Hide the delay label
 		mDelayLabel.setVisibility(View.INVISIBLE);
 		
 		// Hide the increment label
 		mLeftIncreaseLabel.setVisibility(View.INVISIBLE);
 		mRightIncreaseLabel.setVisibility(View.INVISIBLE);
 		
 		// Hide the move count
 		mLeftMoveLabel.setVisibility(View.INVISIBLE);
 		mRightMoveLabel.setVisibility(View.INVISIBLE);
 	}
 	
 	/**
 	 * Indicate the game is paused
 	 */
 	void paused() {
 		// Stop the handler
 		mTimer.removeCallbacks(mTask);
 
 		// Set the condition to time up
 		Global.GAME_STATE.timerCondition = TimerCondition.PAUSE;
 		
 		// Show the pause menu
 		mPauseMenu.showMenu();
 		
 		// Hide the delay label if at zero
 		if(Global.GAME_STATE.delayTime() == null) {
 			mDelayLabel.setVisibility(View.INVISIBLE);
 		} else {
 			mDelayLabel.setVisibility(View.VISIBLE);
 		}
 		
 		// Do whatever else is necessary
 		this.changeConditionSetup();
 	}
 	
 	/**
 	 * Indicate the time is up
 	 */
 	void timesUp() {
 		// Set the condition to time up
 		Global.GAME_STATE.timerCondition = TimerCondition.TIMES_UP;
 				
 		// Check the volume and ringer
 		if(mRingtone != null) {
 			final AudioManager audioManager = (AudioManager)
 				mParentActivity.getSystemService(Context.AUDIO_SERVICE);
 			if(audioManager.getStreamVolume(AudioManager.STREAM_ALARM) > 0) {
 				// If they're valid, play it as an alarm
 				mRingtone.setStreamType(AudioManager.STREAM_ALARM);
 				mRingtone.play();
 			}
 		}
 		
 		// Show the time-up menu
 		mTimesUpMenu.showMenu();
 		
 		// Do whatever else is necessary
 		this.changeConditionSetup();
 		
 		// Hide the delay label
 		mDelayLabel.setVisibility(View.INVISIBLE);
 	}
 	
 	/**
 	 * Updates layout based on which button is pressed
 	 * @param buttonPressed the button that was pressed
 	 */
 	void updatePlayersTurn(final boolean leftPlayersTurn) {
 		// Check if this button press starts the game
 		if(Global.GAME_STATE.timerCondition == TimerCondition.STARTING) {
 			// Set which play is white
 			Global.GAME_STATE.leftIsWhite = !leftPlayersTurn;
 			
 			// Reset the time
 			Global.GAME_STATE.resetTime();
 			
 			// Update the color of the game buttons
 			final int leftButtonID, rightButtonID;
 			if(Global.GAME_STATE.leftIsWhite) {
 				leftButtonID = R.drawable.white_button;
 				rightButtonID = R.drawable.black_button;
 			} else {
 				leftButtonID = R.drawable.black_button;
 				rightButtonID = R.drawable.white_button;
 			}
 			mLeftButton.setImageResource(leftButtonID);
 			mRightButton.setImageResource(rightButtonID);
 		}
 		
 		// Reset the delay time
 		Global.GAME_STATE.switchTurns(leftPlayersTurn);
 		
 		// Set the timer to running
 		Global.GAME_STATE.timerCondition = TimerCondition.RUNNING;
 		
 		// Update the Delay label
 		this.updateButtonAndLabelText();
 		
 		// Play the click sound
 		if(Global.OPTIONS.enableClick) {
 			mParentActivity.playSound();
 		}
 	}
 	/**
 	 * Updates layout based on which button is pressed
 	 * @param buttonPressed the button that was pressed
 	 */
 	void resume() {
 		// If just starting, update the labels
 		if(Global.GAME_STATE.timerCondition == TimerCondition.STARTING) {
 			// Update to the time text
 			this.updateButtonAndLabelText();
 		}
 		Global.GAME_STATE.timerCondition = TimerCondition.RUNNING;
 		
 		// Enable only one button
 		mLeftButton.setEnabled(Global.GAME_STATE.leftPlayersTurn);
 		mRightButton.setEnabled(!Global.GAME_STATE.leftPlayersTurn);
 		
 		// Make the pause button visible
 		mPauseButton.setVisibility(View.VISIBLE);
 		
 		// Start the timer
 		mTask.reset();
 		mTimer.postDelayed(mTask, 1000);
 	}
 	
 	/* ===========================================================
 	 * Private/Protected Methods
 	 * =========================================================== */	
 	/**
 	 * Grabs all the member variables from the parent activity
 	 */
 	private void setupMemberVariables() {
 		// Grab the label
 		mDelayLabel = (TextView) mParentActivity.findViewById(R.id.labelDelay);
 		mLeftLabel = (OutlinedTextView)
 			mParentActivity.findViewById(R.id.labelLeftTime);
 		mRightLabel = (OutlinedTextView)
 			mParentActivity.findViewById(R.id.labelRightTime);
 		mLeftIncreaseLabel = (OutlinedTextView)
 			mParentActivity.findViewById(R.id.labelLeftIncreaseTime);
 		mRightIncreaseLabel = (OutlinedTextView)
 			mParentActivity.findViewById(R.id.labelRightIncreaseTime);
 		mLeftMoveLabel = (OutlinedTextView)
 			mParentActivity.findViewById(R.id.labelLeftMoveCount);
 		mRightMoveLabel = (OutlinedTextView)
 			mParentActivity.findViewById(R.id.labelRightMoveCount);
 		
 		// Grab the buttons
 		mLeftButton = this.getImageButton(R.id.buttonLeftTime);
 		mRightButton = this.getImageButton(R.id.buttonRightTime);
 		mPauseButton = this.getButton(R.id.buttonPause);
 				
 		// Get the ringtone
 		mRingtone = null;
 		if(Global.OPTIONS.alarmUri != null) {
 			mRingtone = RingtoneManager.getRingtone(mParentActivity,
 					Global.OPTIONS.alarmUri);
 		}
 		
 		// Get animations
 		mShowAnimation = AnimationUtils.loadAnimation(mParentActivity,
 				R.anim.show_delay_label);
 		mHideAnimation = AnimationUtils.loadAnimation(mParentActivity,
 				R.anim.hide_delay_label);
 		mLeftIncreaseAnimation = AnimationUtils.loadAnimation(mParentActivity,
 				R.anim.increment_label);
 		mRightIncreaseAnimation = AnimationUtils.loadAnimation(mParentActivity,
 				R.anim.increment_label);
 		
 		// Get strings
 		final String moveText = mParentActivity.getString(R.string.moveLabelText);
 		mStringGenerator.delete(0, mStringGenerator.length());
 		mStringGenerator.append(moveText);
 		mStringGenerator.append(":");
 		mMoveStringLength = mStringGenerator.length();
 	}
 	
 	/**
 	 * TODO: add a description
 	 */
 	private void changeConditionSetup() {
 		// Update the buttons/labels text
 		this.updateButtonAndLabelText();
 		
 		// Disable game buttons, hide pause
 		mLeftButton.setEnabled(false);
 		mRightButton.setEnabled(false);
 		mPauseButton.setVisibility(View.INVISIBLE);	
 	}
 	
 	/**
 	 * TODO: add a description
 	 * @param buttonId
 	 * @return
 	 */
 	private ImageButton getImageButton(final int buttonId) {
 		// Return value
 		ImageButton toReturn = null;
 		
 		// Find a view based on ID
 		final View foundView = mParentActivity.findViewById(buttonId);
 		if(foundView instanceof ImageButton) {
 			toReturn = (ImageButton) foundView;
 			toReturn.setOnClickListener(this);
 			toReturn.setOnTouchListener(this);
 		}
 		return toReturn;
 	}
 	
 	/**
 	 * TODO: add a description
 	 * @param buttonId
 	 * @return
 	 */
 	private Button getButton(final int buttonId) {
 		// Find a view based on ID
 		final View foundView = mParentActivity.findViewById(buttonId);
 		return this.convertToButton(foundView);
 	}
 
 	/**
 	 * TODO: add a description
 	 * @param v
 	 * @return
 	 */
 	private Button convertToButton(final View v) {
 		// Return value
 		Button toReturn = null;
 		if(v instanceof Button) {
 			toReturn = (Button) v;
 			toReturn.setOnClickListener(this);
 			toReturn.setOnTouchListener(this);
 		}
 		return toReturn;
 	}
 }
