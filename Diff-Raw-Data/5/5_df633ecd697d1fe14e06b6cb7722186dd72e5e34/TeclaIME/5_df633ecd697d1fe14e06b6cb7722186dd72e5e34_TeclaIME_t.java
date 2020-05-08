 package com.android.tecla.addon;
 
 import ca.idi.tecla.sdk.SwitchEvent;
 import ca.idrc.tecla.framework.TeclaStatic;
 
 import com.android.inputmethod.keyboard.KeyboardSwitcher;
 import android.content.Intent;
 import com.android.inputmethod.keyboard.KeyboardView;
 
 import android.inputmethodservice.InputMethodService;
 import android.os.Handler;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.os.Message;
 import android.view.inputmethod.EditorInfo;
 
 public class TeclaIME extends InputMethodService {
 
 	/**
 	 * Tag used for logging in the whole framework
 	 */
 	public static final String CLASS_TAG = "TeclaIME";
 
 	private static final int MSG_IMESCAN_SETUP = 0x2244;
 	
 //	private static TeclaIME sInstance;
 	
 	private Handler mHandler = new Handler() {
 
 		@Override
 		public void handleMessage(Message msg) {
 			if(msg.what == MSG_IMESCAN_SETUP) {
 				KeyboardView kbv = KeyboardSwitcher.getInstance().getKeyboardView();
 				boolean kb_ready = IMEAdapter.setKeyboardView(kbv);
 				if(!kb_ready) {
 					++ msg.arg1;
 					if(msg.arg1 < 10) {
 						mHandler.sendMessageDelayed(msg, 250);
 					}
 				} else {
 					IMEAdapter.initialScanHighlighted();
 				}
 			}
 //			if(msg.what == MSG_SHIELD_KEYEVENT_TIMEOUT) {
 //				TeclaStatic.logD(CLASS_TAG, "Shield Timeout expired!");
 //				cancelShieldKeyTimeout();
 ////				if(mShieldKeyCount < TOTAL_SHIELD_KEY_COUNT) {
 ////					// Flush out ALL buffered keys!
 ////					for (byte i=0; i < (mShieldKeyCount); i++) {
 ////						sInstance.keyDownUp(mShieldKeyBuff[1]);
 ////					}
 ////				}
 //				mShieldKeyCount = 0;				
 //			}
 /*			if(msg.what == MSG_REQUEST_SHOW_IME) {
 				showWindow(true);
 				updateInputViewShown();
 			}*/
 			super.handleMessage(msg);
 		}
 		
 	};
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 //		sInstance = this;
 		TeclaApp.setIMEInstance(this);
 		TeclaStatic.logD(CLASS_TAG, "Created " + TeclaIME.class.getName());
 	}
 	
 	@Override
 	public void onStartInputView(EditorInfo info, boolean restarting) {
 		if(TeclaApp.getInstance().isTeclaA11yServiceRunning()
 				&& TeclaApp.overlay.isVisible()) {
 			Message msg = new Message();
 			msg.what = MSG_IMESCAN_SETUP;
 			msg.arg1 = 0;
 			mHandler.sendMessageDelayed(msg, 250);				
 			TeclaApp.overlay.hide();
 		}
 		
 		super.onStartInputView(info, restarting);
 		TeclaApp.persistence.setIMEShowing(true);
 	}
 
 	@Override
 	public void onFinishInputView(boolean finishingInput) {
 		IMEAdapter.setKeyboardView(null);
 		TeclaApp.persistence.setIMEShowing(false);
 		//if(TeclaApp.persistence.shouldShowHUD()
 				//&& !TeclaApp.overlay.isVisible()) {
 			TeclaApp.overlay.showPreviewHUD();
 			TeclaApp.overlay.show();
 //		}
 			
 		super.onFinishInputView(finishingInput);
 	}
 	
 	/* (non-Javadoc)
 	 * @see android.inputmethodservice.InputMethodService#onKeyDown(int, android.view.KeyEvent)
 	 */
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 //		if ((isShieldCodeHeader(keyCode) && mShieldKeyCount == 0) ||
 //				(isShieldCodeHeader(keyCode) && mShieldKeyCount < 4) ||
 //				(isShieldCodeEvent(keyCode) && mShieldKeyCount == 4)) {
 //			resetShieldKeyTimeout();
 //			mShieldKeyCount++;
 //			return true;
 //		}
 //		expireDelayedShieldKeyTimeout(0);
 //		super.onKeyDown(keyCode, event);
 		return false;
 	}
 	
 	/* (non-Javadoc)
 	 * @see android.inputmethodservice.InputMethodService#onKeyUp(int, android.view.KeyEvent)
 	 */
 	@Override
 	public boolean onKeyUp(int keyCode, KeyEvent event) {
 //		if (isShieldCodeHeader(keyCode) && mShieldKeyCount < 4) {
 //			resetShieldKeyTimeout();
 //			mShieldKeyCount++;
 //			return true;
 //		}
 //		if (isShieldCodeEvent(keyCode) && mShieldKeyCount == 5) {
 //			cancelShieldKeyTimeout();
 //			mShieldKeyCount = 0;
 //			sendTeclaSwitchEvent(keyCode);
 //			return true;
 //		}
 //		expireDelayedShieldKeyTimeout(0);
 //		super.onKeyUp(keyCode, event);
 		return false;
 	}
 
 	public void pressHomeKey() {
 		Intent home = new Intent(Intent.ACTION_MAIN);
 		home.addCategory(Intent.CATEGORY_HOME);
 		home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		getApplicationContext().startActivity(home);
 	}
 	
 	public void pressBackKey() {
 		keyDownUp(KeyEvent.KEYCODE_BACK);
 		IMEAdapter.setKeyboardView(null);
 	}
 
 	public void sendKey(int keycode) {
 		keyDownUp(keycode);
 	}
 	
 	/**
 	 * Helper to send a key down / key up pair to the current editor.
 	 */
 	private void keyDownUp(int keyEventCode) {
 		getCurrentInputConnection().sendKeyEvent(
 				new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
 		getCurrentInputConnection().sendKeyEvent(
 				new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
 	}	
 	
 	/* (non-Javadoc)
 	 * @see android.inputmethodservice.InputMethodService#onEvaluateInputViewShown()
 	 */
 //	@Override
 //	public boolean onEvaluateInputViewShown() {
 //		if (TeclaApp.persistence.isFullscreenEnabled()) return true;
 //		return super.onEvaluateInputViewShown();
 //	}
 
 //	private boolean isShieldCode(int keyCode) {
 //		return (isShieldCodeHeader(keyCode) || isShieldCodeEvent(keyCode));
 //	}
 	
 //	private boolean isShieldCodeHeader(int keyCode) {
 //		switch(keyCode) {
 //		case KEYCODE_SHIELD_HEADER1:
 //		case KEYCODE_SHIELD_HEADER2:
 //			return true;
 //		}
 //		return false;
 //	}
 	
 //	private boolean isShieldCodeEvent(int keyCode) {
 //		switch(keyCode) {
 //		case KEYCODE_SHIELD_SP1:
 //		case KEYCODE_SHIELD_SP2:
 //		case KEYCODE_SHIELD_J1:
 //		case KEYCODE_SHIELD_J2:
 //		case KEYCODE_SHIELD_J3:
 //		case KEYCODE_SHIELD_J4:
 //		case KEYCODE_SHIELD_ALLUP:
 //			return true;
 //		}
 //		return false;
 //	}
 	
 //	private void resetShieldKeyTimeout() {
 //		expireDelayedShieldKeyTimeout(SHIELD_KEYEVENT_TIMEOUT); // FIXME: Huge latency!
 //	}
 
 //	private void cancelShieldKeyTimeout () {
 //		mHandler.removeMessages(MSG_SHIELD_KEYEVENT_TIMEOUT);
 //	}
 
 //	private void expireDelayedShieldKeyTimeout (int delay) {
 //		cancelShieldKeyTimeout();
 //		Message msg = new Message();
 //		msg.what = MSG_SHIELD_KEYEVENT_TIMEOUT;
 //		msg.arg1 = 0;
 //		mHandler.sendMessageDelayed(msg, delay);
 //	}
 
 //	private void sendTeclaSwitchEvent(int keyCode) {
 //		
 //		switch (keyCode) {
 //		case KEYCODE_SHIELD_SP1:
 //		case KEYCODE_SHIELD_SP2:
 //		case KEYCODE_SHIELD_J1:
 //		case KEYCODE_SHIELD_J2:
 //		case KEYCODE_SHIELD_J3:
 //		case KEYCODE_SHIELD_J4:
 //			// switch E1 down
 //			TeclaApp.a11yservice.injectSwitchEvent(
 //					new SwitchEvent(SwitchEvent.MASK_SWITCH_E1, 0)); //Primary switch pressed
 //			break;
 //		case KEYCODE_SHIELD_ALLUP:
 //			TeclaApp.a11yservice.injectSwitchEvent(
 //					new SwitchEvent(0,0)); //Switches released
 //			break;
 //		}
 //		
 //	}
 
 //	private static final int KEYCODE_SHIELD_HEADER1 = 59;
 //	private static final int KEYCODE_SHIELD_HEADER2 = 10;
 //	private static final int KEYCODE_SHIELD_SP1 = 124;
 //	private static final int KEYCODE_SHIELD_SP2 = 122;
 //	private static final int KEYCODE_SHIELD_J1 = 92;
 //	private static final int KEYCODE_SHIELD_J2 = 112;
 //	private static final int KEYCODE_SHIELD_J3 = 123;
 //	private static final int KEYCODE_SHIELD_J4 = 93;
 //	private static final int KEYCODE_SHIELD_ALLUP = 7;
 
 //	private static final int TOTAL_SHIELD_KEY_COUNT = 6;
 //	private static final int SHIELD_KEYEVENT_TIMEOUT = 200;  //milliseconds
 //	private static final int MSG_REQUEST_SHOW_IME = 0x7755;
 	
 //	private static final int MSG_SHIELD_KEYEVENT_TIMEOUT = 0x4466;
 	
 	//private int[] mShieldKeyBuff = new int[TOTAL_SHIELD_KEY_COUNT];
 	//private int mShieldKeyCount = 0;
 
 }
