 package com.android.tecla.addon;
 
 import android.content.Context;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.WindowManager;
 import ca.idrc.tecla.framework.SimpleOverlay;
 import ca.idrc.tecla.framework.TeclaStatic;
 import ca.idi.tecla.sdk.*;
 
 public class SingleSwitchTouchInterface extends SimpleOverlay {
 
 	/**
 	 * Tag used for logging in the whole framework
 	 */
 	public static final String CLASS_TAG = "SingleSwitchTouchInterface";
 	private static SingleSwitchTouchInterface sInstance;
 
 	public SingleSwitchTouchInterface(Context context) {
 		super(context);
 
 		final WindowManager.LayoutParams params = getParams();
 		params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
 		setParams(params);
 
 		View rView = getRootView();
 		rView.setOnTouchListener(mOverlayTouchListener);
 		rView.setOnLongClickListener(mOverlayLongClickListener);
 		//rView.setOnClickListener(mOverlayClickListener);
 	}
 
 	@Override
 	protected void onShow() {
 		sInstance = this;
 	}
 
 	@Override
 	protected void onHide() {
 		sInstance = null;
 	}
 
 	/**
 	 * Listener for full-screen switch actions
 	 */
 	private View.OnTouchListener mOverlayTouchListener = new View.OnTouchListener() {
 		
 		public boolean onTouch(View v, MotionEvent event) {
 			switch (event.getAction()) {
 			case MotionEvent.ACTION_DOWN:
 				TeclaApp.a11yservice.injectSwitchEvent(
 						new SwitchEvent(SwitchEvent.MASK_SWITCH_E1, 0)); //Primary switch pressed
 				// if (TeclaApp.DEBUG) Log.d(TeclaApp.TAG, CLASS_TAG + "Fullscreen switch down!");
 				break;
 			case MotionEvent.ACTION_UP:
 				TeclaApp.a11yservice.injectSwitchEvent(
 						new SwitchEvent(0,0)); //Switches released
 				// if (TeclaApp.DEBUG) Log.d(TeclaApp.TAG, CLASS_TAG + "Fullscreen switch up!");
 				break;
 			default:
 				break;
 			}
 			return false;
 		}
 	};
 
 	private View.OnClickListener mOverlayClickListener = new View.OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			if(IMEAdapter.isShowingKeyboard()) IMEAdapter.selectScanHighlighted();
 			else TeclaHUDOverlay.selectScanHighlighted();
 				
 		}
 	};	
 
 	private View.OnLongClickListener mOverlayLongClickListener =  new View.OnLongClickListener() {
 
 		@Override
 		public boolean onLongClick(View v) {
 			TeclaStatic.logV(CLASS_TAG, "Long clicked.  ");
 			TeclaApp.persistence.setHUDCancelled(true);
 			TeclaApp.a11yservice.shutdownInfrastructure();
 			return true;
 		}
 	};
 
 }
