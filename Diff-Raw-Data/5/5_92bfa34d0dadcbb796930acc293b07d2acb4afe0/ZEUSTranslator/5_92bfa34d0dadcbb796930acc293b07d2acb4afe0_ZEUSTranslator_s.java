 package com.alvaroag.zeusTranslator;
 
 import java.util.HashMap;
 
 import android.inputmethodservice.InputMethodService;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 
 public class ZEUSTranslator extends InputMethodService {
 	private static final HashMap<Integer,Integer> zeusTranslation;
 
 	static {
 		zeusTranslation = new HashMap<Integer,Integer>();
 
 		// Multiple Mappings for the MENU key.
 		// Not sure which is the correct one
 		zeusTranslation.put(229	,	KeyEvent.KEYCODE_MENU);
 		zeusTranslation.put(139	,	KeyEvent.KEYCODE_MENU);
 		zeusTranslation.put(59	,	KeyEvent.KEYCODE_MENU);
 
 		// The BACK key
 		zeusTranslation.put(158	,	KeyEvent.KEYCODE_BACK);
 
 		// The ZEUS DPAD is mapped with the IYOKAN DPAD
 		zeusTranslation.put(103	,	KeyEvent.KEYCODE_DPAD_UP);
 		zeusTranslation.put(106	,	KeyEvent.KEYCODE_DPAD_RIGHT);
 		zeusTranslation.put(108	,	KeyEvent.KEYCODE_DPAD_DOWN);
 		zeusTranslation.put(105	,	KeyEvent.KEYCODE_DPAD_LEFT);
 
 		// START & SELECT are mapped with ENTER & BACKSPACE, respectively
 		zeusTranslation.put(28	,	KeyEvent.KEYCODE_BUTTON_START);
 		zeusTranslation.put(14	,	KeyEvent.KEYCODE_BUTTON_SELECT);
 
 		// L1 & L2 are mapped with ' & Q, respectively
 		zeusTranslation.put(16	,	KeyEvent.KEYCODE_BUTTON_L1);
 		zeusTranslation.put(40	,	KeyEvent.KEYCODE_BUTTON_L2);
 
 		// PS Buttons are mapped with some letters
 		zeusTranslation.put(17	,	KeyEvent.KEYCODE_BUTTON_Y);	// W - Triangle
 		zeusTranslation.put(30	,	KeyEvent.KEYCODE_BUTTON_X);	// A - Square
 		zeusTranslation.put(32	,	KeyEvent.KEYCODE_BUTTON_B);	// D - Circle
 		zeusTranslation.put(44	,	KeyEvent.KEYCODE_BUTTON_A);	// Z - X
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 	}
 
 	@Override
 	public void onInitializeInterface() {
 	}
 
 	@Override
 	public View onCreateInputView() {
 		return null;
     }
 
 	@Override
 	public View onCreateCandidatesView() {
 		return null;
 	}
 
 	@Override
 	public void onStartInput(EditorInfo attribute,boolean restarting) {
 		super.onStartInput(attribute,restarting);
 	}
 
 	@Override
 	public void onFinishInput() {
 		super.onFinishInput();
 	}
 
 	@Override
 	public void onStartInputView(EditorInfo attribute,boolean restarting) {
 		super.onStartInputView(attribute,restarting);
     }
 
 	@Override
 	public boolean onKeyDown(int keyCode,KeyEvent event) {
 		if (zeusTranslation.containsKey(event.getScanCode())) {
 			int finalKeyCode = zeusTranslation.get(event.getScanCode());
 			getCurrentInputConnection().sendKeyEvent(new KeyEvent(0,finalKeyCode));
 			return true;
 		} else 
 			return super.onKeyDown(keyCode,event);
 	}
 
 	@Override
 	public boolean onKeyUp(int keyCode,KeyEvent event) {
 		if (zeusTranslation.containsKey(event.getScanCode())) {
 			int finalKeyCode = zeusTranslation.get(event.getScanCode());
 			getCurrentInputConnection().sendKeyEvent(new KeyEvent(1,finalKeyCode));
 			return true;
 		} else 
 			return super.onKeyUp(keyCode,event);
 	}
 }
