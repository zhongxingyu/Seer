 package com.japtar.chessclock.menus;
 
 import android.view.View;
 import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;
 
import com.japtar.chessclock.Global;
 import com.japtar.chessclock.MainActivity;
 import com.japtar.chessclock.R;
 
 public class StartSubMenu extends SubMenu {
 	/* ===========================================================
 	 * Members
 	 * =========================================================== */
 	
 	// == Buttons ==
 	/** The right button */
 	private Button mRightButton = null;
 	/** The left button */
 	private Button mLeftButton = null;
 	/** The options button */
 	private Button mSettingsButton = null;
 	
 	/* ===========================================================
 	 * Constructors
 	 * =========================================================== */
 	/**
 	 * @see SubMenu#SubMenu(MainActivity, TimersMenu)
 	 */
 	StartSubMenu(final MainActivity activity, final TimersMenu menu) {
 		super(activity, menu);
 	}
 
 	/* ===========================================================
 	 * Override Methods
 	 * =========================================================== */
 	@Override
 	protected int getLayoutId() {
 		return R.layout.sub_start_game;
 	}
 
 	@Override
 	protected void setupLayout(final View v) {
 		// == Load up all the member variables ==
 		
 		// Grab the buttons
 		mRightButton = mParentMenu.getButton(v, R.id.buttonRightStart);
 		mLeftButton = mParentMenu.getButton(v, R.id.buttonLeftStart);
 		mSettingsButton = mParentMenu.getButton(v, R.id.buttonSettings);
 		
 		// == Setup the member variables ==
 
 		// Update the text size on everything
		mRightButton.setTextSize(MainActivity.msTextSize * 0.6f);
		mLeftButton.setTextSize(MainActivity.msTextSize * 0.6f);
 		mSettingsButton.setTextSize(MainActivity.msTextSize * 0.5f);
		
		// Update the margins
		LayoutParams params = (LayoutParams) mRightButton.getLayoutParams();
		params.rightMargin = Global.DISPLAY.widthPixels / 40;
		params.leftMargin = Global.DISPLAY.widthPixels / 40;
		params = (LayoutParams) mLeftButton.getLayoutParams();
		params.rightMargin = Global.DISPLAY.widthPixels / 40;
		params.leftMargin = Global.DISPLAY.widthPixels / 40;
 	}
 
 	@Override
 	public void onClick(final View v) {
 		if(v != null) {
 			// Check which button is clicked
 			if(v.equals(mSettingsButton)) {
 				// Open the options menu
 				mParentActivity.displayOptionsMenu();
 			} else {
 				final boolean leftPlayersTurn = v.equals(mRightButton);
 				if(leftPlayersTurn || v.equals(mLeftButton)) {
 					// Hide this menu
 					this.hideMenu();
 					
 					// Update who's turn it is
 					mParentMenu.updatePlayersTurn(leftPlayersTurn);
 					
 					// Resume the timer
 					mParentMenu.resume();
 				}
 			}
 		}
 	}
 }
