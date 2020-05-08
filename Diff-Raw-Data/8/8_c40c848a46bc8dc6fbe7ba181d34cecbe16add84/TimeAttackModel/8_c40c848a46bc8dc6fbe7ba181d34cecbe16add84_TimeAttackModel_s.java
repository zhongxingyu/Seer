 package com.chalmers.speedtype.model;
 
 import android.view.KeyEvent;
 
 import com.chalmers.speedtype.R;
 
 public class TimeAttackModel extends GameModel {
 
 	private static final int LAYOUT_ID = R.layout.time_attack_layout;
 	private static final int VIEW_ID = R.id.time_attack_view;
 	
 	private static final int UPDATE_FREQUENCY = 50;
 
 	private int timeLeft = 10000;
 	private boolean correctInput;
 	private long speedRewardTimeStart;
 	private long lastUpdateMillis;
 
 	private PowerUp speedRewardPowerUp;
 
 	public TimeAttackModel() {
 		super();
 		correctInput = false;
 	}
 
 	protected void setTimeLeft(int timeLeft) {
 		this.timeLeft = timeLeft;
 	}
 
 	public int getTimeLeft() {
 		return timeLeft;
 	}
 
 	@Override
 	public int getLayoutId() {
 		return LAYOUT_ID;
 	}
 
 	@Override
 	public int getViewId() {
 		return VIEW_ID;
 	}
 
 	private void setCorrectInputReport(boolean b) {
 		correctInput = b;
 	}
 
 	public boolean correctInputReport() {
 		if (correctInput) {
 			return true;
 		} else {
 			setCorrectInputReport(true);
 			return false;
 		}
 	}
 
 	@Override
 	public void onInput(KeyEvent event) {
 		correctInput = true;
 		char inputChar = Character.toLowerCase((char) event.getUnicodeChar());
 		if (activeWord.charAt(currentCharPos) == inputChar) {
 			onCorrectChar();
 			if (isWordComplete()) {
 				onCorrectWord();
 			} else {
 				incCurrentCharPos();
 			}
 		} else {
 			onIncorrectChar();
 		}
 	}
 
 	protected void onCorrectChar() {
 		super.onCorrectChar();
 		incScore(1);
 	}
 
 	protected void onCorrectWord() {
 		super.onCorrectWord();
 		if (isFastEnough()) {
 			speedRewardPowerUp = new SpeedRewardPowerUp(this);
 			speedRewardPowerUp.usePowerUp();
 		}
 		speedRewardTimeStart = System.currentTimeMillis();
 		setTimeLeft(timeLeft + 1000 * activeWord.length());
 		updateWord();
 	}
 
 	protected void onIncorrectChar() {
 		super.onIncorrectChar();
 		setCorrectInputReport(false);
 	}
 
 	@Override
 	public boolean isContinuous() {
 		return true;
 	}
 
 	@Override
 	public boolean isSensorDependent() {
 		return false;
 	}
 
 	public boolean isFastEnough() {
 		return System.currentTimeMillis() - speedRewardTimeStart < activeWord
 				.length() * 1000 ? true : false;
 	}
 
 	@Override
 	public void update() {
 		if (System.currentTimeMillis() - lastUpdateMillis > UPDATE_FREQUENCY) {
			setTimeLeft((int) (timeLeft - (System.currentTimeMillis() - lastUpdateMillis)));
			lastUpdateMillis = System.currentTimeMillis();
			
 			listener.propertyChange(null);
 		}
 	}
 }
