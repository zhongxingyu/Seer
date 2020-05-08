 package com.japtar.chessclock.menus;
 
 import android.view.View;
 import android.view.ViewGroup;
 
 import com.japtar.chessclock.MainActivity;
 import com.japtar.chessclock.R;
 
 abstract class SubMenu implements MenuInterface, View.OnClickListener {
 	/* ===========================================================
 	 * Members
 	 * =========================================================== */
 	/** The main activities class */
 	protected final MainActivity mParentActivity;
 	/** The timers menu */
 	protected final TimersMenu mParentMenu;
 	/** Flag indicating if this layout is shown */
 	private boolean mShown = false;
 
 	// == Layouts ==
 	/** The generated sub-menu*/
 	private View mSubMenu = null;
 	/** An overlapping layout from the parent's menu */
 	private ViewGroup mEmbedding = null;
	/** {@link #mEmbedding}'s dimensions */
	private final ViewGroup.LayoutParams mParams =
		new ViewGroup.LayoutParams(0, 0);
 
 	/* ===========================================================
 	 * Constructors
 	 * =========================================================== */
 	/**
 	 * @param activity the activity this menu is running on
 	 * @param menu the parent menu
 	 */
 	SubMenu(final MainActivity activity, final TimersMenu menu) {
 		// Setup variables
 		mParentActivity = activity;
 		mParentMenu = menu;
 	}
 	
 	/* ===========================================================
 	 * Override Methods
 	 * =========================================================== */
 	/**
 	 * Sets up this sub-menu
 	 */
 	public void setupMenu() {
 		// Grab the overlap layout from the parents, first
 		mEmbedding = (ViewGroup) mParentActivity.findViewById(R.id.layoutOverlap);
 		
 		// Inflate the sub-menu
 		mSubMenu = View.inflate(mParentActivity, getLayoutId(), null);
 		
		// Create the 
		mParams.width = mEmbedding.getLayoutParams().width;
		mParams.height = mEmbedding.getLayoutParams().height;
		
 		// Setup the rest
 		this.setupLayout(mSubMenu);
 	}
 		
 	/* ===========================================================
 	 * Abstract Methods
 	 * =========================================================== */
 	/**
 	 * @param v the newly-created view
 	 */
 	protected abstract void setupLayout(final View v);
 	/**
 	 * Gets this layout's id
 	 */
 	protected abstract int getLayoutId();
 	
 	/* ===========================================================
 	 * Public Methods
 	 * =========================================================== */
 	/**
 	 * Sets up this sub-menu
 	 */
 	public void showMenu() {
 		if((mSubMenu != null) && (mEmbedding != null) &&
 				(mEmbedding.getVisibility() == View.INVISIBLE)) {
 			// Add the sub-menu to this layout
			mEmbedding.addView(mSubMenu, mParams);
 			
 			// Make the layout visible
 			mEmbedding.setVisibility(View.VISIBLE);
 			
 			mShown = true;
 		}
 	}
 	
 	/**
 	 * Destroy this sub-menu
 	 */
 	public void hideMenu() {
 		if((mSubMenu != null) && (mEmbedding != null) &&
 				(mEmbedding.getVisibility() == View.VISIBLE)) {
 			// Remove the sub-menu
 			mEmbedding.removeView(mSubMenu);
 			
 			// Make the layout invisible
 			mEmbedding.setVisibility(View.INVISIBLE);
 			
 			mShown = false;
 		}
 	}
 	
 	/**
 	 * @return true if this menu is shown
 	 */
 	public boolean isMenuShown() {
 		// Return the menu's visibility
 		return mShown;
 	}
 }
