 package edu.cmu.hcii.peer;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.animation.AnimationUtils;
 import android.widget.Button;
 import android.widget.FrameLayout;
 import android.widget.LinearLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import edu.cmu.hcii.novo.kadarbra.R;
 import edu.cmu.hcii.peer.AudioFeedbackView.AudioFeedbackThread;
 import edu.cmu.hcii.peer.page.AnnotationPage;
 import edu.cmu.hcii.peer.page.CompletionPage;
 import edu.cmu.hcii.peer.page.CoverPage;
 import edu.cmu.hcii.peer.page.CycleMarkerPage;
 import edu.cmu.hcii.peer.page.CycleSelectPage;
 import edu.cmu.hcii.peer.page.DrawerPageInterface;
 import edu.cmu.hcii.peer.page.ExecNotesPage;
 import edu.cmu.hcii.peer.page.GroundPage;
 import edu.cmu.hcii.peer.page.NavigationPage;
 import edu.cmu.hcii.peer.page.PageAdapter;
 import edu.cmu.hcii.peer.page.StepPage;
 import edu.cmu.hcii.peer.page.StepPageScrollView;
 import edu.cmu.hcii.peer.page.StowagePage;
 import edu.cmu.hcii.peer.structure.Cycle;
 import edu.cmu.hcii.peer.structure.ExecNote;
 import edu.cmu.hcii.peer.structure.Procedure;
 import edu.cmu.hcii.peer.structure.ProcedureItem;
 import edu.cmu.hcii.peer.structure.Step;
 import edu.cmu.hcii.peer.util.FontManager;
 import edu.cmu.hcii.peer.util.FontManager.FontStyle;
 
 /**
  * This activity is the bulk of our application and handles all of the actions within a procedure
  *
  */
 public class ProcedureActivity extends Activity {
 	private static final String TAG = "ProcedureActivity";	// used for logging purposes
 	public final static String CURRENT_STEP = "edu.cmu.hcii.novo.kadarbra.CURRENT_STEP";
 
 	public final static int PREPARE_PAGES = 3; // number of pages in prepare stage (before steps are shown)
 	
 	private Procedure procedure; 					 // current procedure
 	private ViewPager viewPager; 					 // the ViewPager that displays our steps
 	private Breadcrumb breadcrumb; 					 // the progress bar
 	private StepPreviewWidget stepPreviewWidget; 	 // the bottom widget that displays previews of the previous and next steps
 	private DataUpdateReceiver dataUpdateReceiver; 	 // handles broadcast messages 
 	private AudioFeedbackView audioFeedbackView; 	 // visualizes the noise level of audio input
 	private AudioFeedbackThread audioFeedbackThread; // handles the audioFeedbackView
 	
 	private List<StepIndex> stepIndices;	// a list that keeps track of which view page a higher level step is displayed on
 	private int selectedStep = -1;			// the step we are currently viewing
 	
 	private Map<String, Animation> menuAnimations; // stores animations
 	private View drawerContent; // stores drawer content
 	private static final String TAG_OPEN = "_open"; 
 	private static final String TAG_CLOSE = "_close";
 	private static final String TAG_CYCLE = "_cycle";
 	private static final String TAG_CASCADE = "_cascade";
 	private static final int ANIM_DELAY = 25;
 	
 	private long startTime; // for elapsed time
 	private long timerStartTime; // when timer started
 	private long timerDuration; // current time on timer
 
 	private String timerState = TIMER_OFF;
 	private static final String TIMER_ON = "_timerOn";
 	private static final String TIMER_OFF = "_timerOff";
 	private static final String TIMER_RESET = "_timerReset";
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		
 		Intent intent = getIntent();
 		procedure = (Procedure)intent.getSerializableExtra(MainActivity.PROCEDURE);
 		
 		setContentView(R.layout.activity_procedure);
 		initFonts();
 		
 		initStepPreviewWidget();
 		initMenu();
 		initViewPager();
 		initBreadcrumb();
 		initAudioFeedbackView();
 		
 		stepIndices = getPageIndices();
 		
 		initElapsedTime();
 		initTimer();
 	}
 	
 	
 	/**
 	 * Set up the custom fonts on the views that are static to a procedure.
 	 * Mainly, the menu and timer.
 	 */
 	private void initFonts() {
 		FontManager fm = FontManager.getInstance(getAssets());
 		
 		((TextView)findViewById(R.id.menuTitle)).setTypeface(fm.getFont(FontStyle.SELECTABLE));
 		((Button)findViewById(R.id.navButton)).setTypeface(fm.getFont(FontStyle.SELECTABLE));
 		((Button)findViewById(R.id.stowageButton)).setTypeface(fm.getFont(FontStyle.SELECTABLE));
 		((Button)findViewById(R.id.annotationButton)).setTypeface(fm.getFont(FontStyle.SELECTABLE));
 		((Button)findViewById(R.id.groundButton)).setTypeface(fm.getFont(FontStyle.SELECTABLE));
 		
 		((TextView)findViewById(R.id.elapsedTime)).setTypeface(fm.getFont(FontStyle.TIMER));
 		((TextView)findViewById(R.id.elapsedTimeText)).setTypeface(fm.getFont(FontStyle.BODY));
 		((TextView)findViewById(R.id.timer)).setTypeface(fm.getFont(FontStyle.TIMER));
 		
 	}
 
 	
 	
 	// The activity is about to become visible.
 	@Override
 	protected void onStart() {
 	    super.onStart();
 	    Log.v(TAG, "onStart");   
 	}
 
 	
 	
 	// The activity has become visible (it is now "resumed").
 	@Override
 	protected void onResume() {
 	    super.onResume();
 	    Log.v(TAG, "onResume");
 	
 	    if (dataUpdateReceiver == null) 
 	    	dataUpdateReceiver = new DataUpdateReceiver();
         IntentFilter intentFilter = new IntentFilter();
         intentFilter.addAction(MessageHandler.MSG_TYPE_COMMAND);
         intentFilter.addAction(MessageHandler.MSG_TYPE_AUDIO_LEVEL);
         intentFilter.addAction(MessageHandler.MSG_TYPE_AUDIO_BUSY);    
         intentFilter.addAction(MessageHandler.MSG_TYPE_AUDIO_STATE);    
         intentFilter.addAction(MessageHandler.MSG_TYPE_AR_READ);    
 
 	    registerReceiver(dataUpdateReceiver, intentFilter);
 	    
 	    audioFeedbackThread.unpause();
 	}
 
 	
 	
 	// The activity is paused
 	@Override
 	protected void onPause(){
 		super.onPause();
 		audioFeedbackThread.pause();
 		Log.v(TAG, "onPause");
 		if (dataUpdateReceiver != null) 
 			unregisterReceiver(dataUpdateReceiver);
 	}
 
 	
 	
 	// The activity is no longer visible (it is now "stopped")
 	@Override
 	protected void onStop() {
 	    super.onStop();
 	    Log.v(TAG, "onStop");
 	}
 	
 	
 
 	// The activity is about to be destroyed.
 	@Override
 	protected void onDestroy() {
 	    super.onDestroy();
 	    Log.v(TAG, "onDestroy");
 	}
 
 	/**
 	 *  Initializes audio feedback view and drawing thread
 	 */
 	private void initAudioFeedbackView(){
         audioFeedbackView = (AudioFeedbackView) findViewById(R.id.audioFeedbackView);
         audioFeedbackView.bringToFront();
         audioFeedbackThread = audioFeedbackView.getThread();
 	}
 	
 	/**
 	 * Initializes the breadcrumb (currently just step numbers)
 	 */
 	private void initBreadcrumb(){
 		breadcrumb = (Breadcrumb) findViewById(R.id.breadcrumb);
 		breadcrumb.setTotalSteps(viewPager.getAdapter().getCount());
 		breadcrumb.setCurrentStep(1);
 	}
 
 	/**
 	 * Setup the step preview widget.  The thing at the bottom that shows
 	 * the previous and next steps.
 	 */
 	private void initStepPreviewWidget(){
 		stepPreviewWidget = (StepPreviewWidget) findViewById(R.id.stepPreviewWidget);
 		stepPreviewWidget.setCurrentStep(procedure,0);
 	}
 	
 	/**
 	 * Initialize the menu.  Setup the animations along with the onclick method
 	 * for the menu text.
 	 */
 	private void initMenu(){		
 		drawerContent = null;
 		initMenuAnimations();
 		
 		TextView menu = (TextView) findViewById(R.id.menuTitle);
 		menu.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if (v.isSelected()) {
 					closeMenu();
 					v.setSelected(false);
 				} else  {
 					openMenu();
 					v.setSelected(true);
 				}
 			}
 			 
 		 });
 	}
 	
 	
 	
 	/**
 	 * Initialize the menu animations.  All animations are stored in a private
 	 * map.  The key convention is:
 	 *     id + (_enter | _exit | _cycle | _exit_cascade)
 	 */
 	private void initMenuAnimations() {
 		menuAnimations = new HashMap<String, Animation>();		
 		
 		int curId = -1;
 		
 		//Menu background animations
 		curId = findViewById(R.id.menuBackground).getId();
 		addMenuAnimation(curId + TAG_OPEN, R.anim.menu_enter, 0, new AnimationListener() {
 
 			@Override
 			public void onAnimationEnd(Animation arg0) {
 				//Open the menu.  Run each menu item's open animation and set their visibility to VISIBLE.
 				runMenuItemAnimations(TAG_OPEN, View.VISIBLE);
 			}
 
 			@Override
 			public void onAnimationRepeat(Animation arg0) {}
 
 			@Override
 			public void onAnimationStart(Animation arg0) {}
 			
 		});
 		addMenuAnimation(curId + TAG_CLOSE, R.anim.menu_exit, 0, null);
 		
 		//Menu button animations
 		curId = findViewById(R.id.navButton).getId();
 		addMenuAnimation(curId + TAG_OPEN, R.anim.menu_enter, ANIM_DELAY, null);
 		addMenuAnimation(curId + TAG_CLOSE, R.anim.menu_exit, ANIM_DELAY*5, new AnimationListener() {
 
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				//Start the menu background's close animation
 				View bg = (View) findViewById(R.id.menuBackground);
 				
 				bg.startAnimation(menuAnimations.get(bg.getId() + TAG_CLOSE));
 				bg.setVisibility(View.GONE);
 			}
 
 			@Override
 			public void onAnimationRepeat(Animation animation) {}
 
 			@Override
 			public void onAnimationStart(Animation animation) {}
 			
 		});
 		
 		curId = findViewById(R.id.stowageButton).getId();
 		addMenuAnimation(curId + TAG_OPEN, R.anim.menu_enter, ANIM_DELAY*2, null);
 		addMenuAnimation(curId + TAG_CLOSE, R.anim.menu_exit, ANIM_DELAY*4, null);
 		
 		curId = findViewById(R.id.annotationButton).getId();
 		addMenuAnimation(curId + TAG_OPEN, R.anim.menu_enter, ANIM_DELAY*3, null);
 		addMenuAnimation(curId + TAG_CLOSE, R.anim.menu_exit, ANIM_DELAY*3, null);
 		
 		curId = findViewById(R.id.groundButton).getId();
 		addMenuAnimation(curId + TAG_OPEN, R.anim.menu_enter, ANIM_DELAY*4, null);
 		addMenuAnimation(curId + TAG_CLOSE, R.anim.menu_exit, ANIM_DELAY*2, null);
 		
 		curId = findViewById(R.id.elapsedTimeView).getId();
 		addMenuAnimation(curId + TAG_OPEN, R.anim.menu_enter, ANIM_DELAY*5, null);
 		addMenuAnimation(curId + TAG_CLOSE, R.anim.menu_exit, ANIM_DELAY, null);
 		
 		//Drawer animations
 		curId = findViewById(R.id.menuDrawerLayout).getId();
 		addMenuAnimation(curId + TAG_OPEN, R.anim.menu_drawer_enter, 0, new AnimationListener() {
 
 			@Override
 			public void onAnimationEnd(Animation animation) {}
 
 			@Override
 			public void onAnimationRepeat(Animation animation) {}
 
 			@Override
 			public void onAnimationStart(Animation animation) {
 				if (drawerContent != null) {
 					ScrollView sv = ((ScrollView)findViewById(R.id.menuDrawer));
 					if (sv.getChildCount() == 0)
 						sv.addView(drawerContent);
 					drawerContent = null;
 				}
 			}
 			
 		});
 		addMenuAnimation(curId + TAG_CLOSE, R.anim.menu_drawer_exit, 0, new AnimationListener() {
 
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				((ScrollView)findViewById(R.id.menuDrawer)).removeAllViews();
 			}
 
 			@Override
 			public void onAnimationRepeat(Animation animation) {}
 
 			@Override
 			public void onAnimationStart(Animation animation) {}
 			
 		});
 		addMenuAnimation(curId + TAG_CYCLE, R.anim.menu_drawer_exit, 0, new AnimationListener() {
 
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				LinearLayout menuDrawerLayout = (LinearLayout)findViewById(R.id.menuDrawerLayout);
 				ScrollView drawer = (ScrollView)findViewById(R.id.menuDrawer);
 				drawer.removeAllViews();
 				menuDrawerLayout.startAnimation(menuAnimations.get(menuDrawerLayout.getId() + TAG_OPEN));
 			}
 
 			@Override
 			public void onAnimationRepeat(Animation animation) {}
 
 			@Override
 			public void onAnimationStart(Animation animation) {}
 			
 		});
 		addMenuAnimation(curId + TAG_CLOSE + TAG_CASCADE, R.anim.menu_drawer_exit, 0, new AnimationListener() {
 
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				((ScrollView)findViewById(R.id.menuDrawer)).removeAllViews();
 				//Run each menu item's close animation and set their visibility to GONE.
 				runMenuItemAnimations(TAG_CLOSE, View.GONE);
 			}
 
 			@Override
 			public void onAnimationRepeat(Animation animation) {}
 
 			@Override
 			public void onAnimationStart(Animation animation) {}
 			
 		});
 	}
 	
 	
 	
 	/**
 	 * Add an animation to the global animation map.  First inflate the animation then set the
 	 * given parameters.  Then add it with the given key to the map.
 	 * 
 	 * @param key
 	 * @param animationId
 	 * @param delay
 	 * @param listener
 	 */
 	private void addMenuAnimation(String key, int animationId, int delay, AnimationListener listener) {
 		Animation anim = AnimationUtils.loadAnimation(this, animationId);
 		anim.setStartOffset(delay);
 		if (listener != null) anim.setAnimationListener(listener);
 		menuAnimations.put(key, anim);
 	}
 
 	/**
 	 * Gets whether the menu is opened
 	 * @return
 	 */
 	private boolean getMenuVisibility(){
 		View bg = (View) findViewById(R.id.menuBackground);
 		return (bg.getVisibility() == View.VISIBLE);
 	}
 	
 	/**
 	 * 
 	 * @return name of current drawer opened. 
 	 * Returns DrawerPageInterface.DRAWER_NONE if no drawer is currently opened.
 	 */
 	private String getOpenedDrawer(){
 		String currentDrawer = DrawerPageInterface.DRAWER_NONE;
 		ScrollView drawer = (ScrollView)findViewById(R.id.menuDrawer);
 		LinearLayout menuDrawerLayout = (LinearLayout) findViewById(R.id.menuDrawerLayout);
 		
 		if (menuDrawerLayout.getVisibility() == View.VISIBLE){
 			DrawerPageInterface drawerPage = (DrawerPageInterface) drawer.getChildAt(0);
 			if (drawerPage != null)
 				currentDrawer = drawerPage.getDrawerType();
 		}
 		Log.v("currentDrawer",currentDrawer);
 		return currentDrawer;
 	}
 	
 	/**
 	 * Open the menu.  Run the menu background's open animation and set
 	 * it's visibility to VISIBLE.  This automatically opens the items.
 	 */
 	private void openMenu() {
 		View bg = (View) findViewById(R.id.menuBackground);
 		bg.startAnimation(menuAnimations.get(bg.getId() + TAG_OPEN));
 		bg.setVisibility(View.VISIBLE);
 		
 		findViewById(R.id.menuTitle).setSelected(true);
 	}
 	
 	/**
 	 * Opens the drawer
 	 */
 	private void openStepDrawer(View v){
 		ScrollView drawer = ((ScrollView)findViewById(R.id.menuDrawer));
 		LinearLayout menuDrawerLayout = (LinearLayout) findViewById(R.id.menuDrawerLayout);
 
 		if (!getOpenedDrawer().equals(DrawerPageInterface.DRAWER_NAVIGATION)){
 			// do animation
 			drawerContent = v;
 			
 			//If the drawer is open for another menu
 			if (menuDrawerLayout.getVisibility() != View.GONE) {
 				//change drawer
				menuDrawerLayout.startAnimation(menuAnimations.get(menuDrawerLayout.getId() + TAG_CYCLE));
 				clearMenuSelection();
 		    //If the drawer is closed
 			} else {
 				//open the drawer
				menuDrawerLayout.startAnimation(menuAnimations.get(menuDrawerLayout.getId() + TAG_OPEN));
 				menuDrawerLayout.setVisibility(View.VISIBLE);
 			}
 		}else{
 			drawer.removeAllViews();
 			menuDrawerLayout.setVisibility(View.VISIBLE);
 			drawer.addView(v);
 		}
 	}
 	
 	/**
 	 * Close the drawer
 	 */
 	private void closeDrawer(){
 		LinearLayout menuDrawerLayout = (LinearLayout) findViewById(R.id.menuDrawerLayout);
 
 		if (menuDrawerLayout.getVisibility() == View.VISIBLE) {
 			menuDrawerLayout.startAnimation(menuAnimations.get(menuDrawerLayout.getId() + TAG_CLOSE));
 			menuDrawerLayout.setVisibility(View.GONE);
 		}
 		clearMenuSelection();
 	}
 	
 	/**
 	 * Close the menu.  If the drawer is open, close it via it's cascading close.
 	 * Otherwise, run each item's close animation and set their visibility to GONE.
 	 * Then make sure to make all buttons unselected.
 	 */
 	private void closeMenu() {
 		LinearLayout menuDrawerLayout = (LinearLayout) findViewById(R.id.menuDrawerLayout);
 		
 		if (menuDrawerLayout.getVisibility() == View.VISIBLE) {
 			menuDrawerLayout.startAnimation(menuAnimations.get(menuDrawerLayout.getId() + TAG_CLOSE + TAG_CASCADE));
 			menuDrawerLayout.setVisibility(View.GONE);
 			
 		} else {
 			runMenuItemAnimations(TAG_CLOSE, View.GONE);
 		}
 		
 		clearMenuSelection();
 		findViewById(R.id.menuTitle).setSelected(false);
 		
 	}
 	
 	/**
 	 * Run each menu item's animation via the given tag.  Then set their visibility
 	 * to that which was given.
 	 * 
 	 * @param tag
 	 * @param visibility
 	 */
 	private void runMenuItemAnimations(String tag, int visibility) {
 		Button overviewButton = (Button) findViewById(R.id.navButton);
 		Button stowButton = (Button) findViewById(R.id.stowageButton);
 		Button annotateButton = (Button) findViewById(R.id.annotationButton);
 		Button groundButton = (Button) findViewById(R.id.groundButton);
 		View elapsedTime = (View) findViewById(R.id.elapsedTimeView);
 		
 		overviewButton.startAnimation(menuAnimations.get(overviewButton.getId() + tag));
 		overviewButton.setVisibility(visibility);
 		
 		stowButton.startAnimation(menuAnimations.get(stowButton.getId() + tag));
 		stowButton.setVisibility(visibility);
 		
 		annotateButton.startAnimation(menuAnimations.get(annotateButton.getId() + tag));
 		annotateButton.setVisibility(visibility);
 		
 		groundButton.startAnimation(menuAnimations.get(groundButton.getId() + tag));
 		groundButton.setVisibility(visibility);
 
 		elapsedTime.startAnimation(menuAnimations.get(elapsedTime.getId() + tag));
 		elapsedTime.setVisibility(visibility);
 	}
 	
 	
 	
 	/**
 	 * Clears all menu items of their selection values.  Used to keep
 	 * the state correct.
 	 * 
 	 * TODO: Maybe switch these to a set of radio buttons or toggle buttons
 	 */
 	private void clearMenuSelection() {
 		Log.v(TAG, "Clearing all selections");
 		
 		findViewById(R.id.navButton).setSelected(false);
 		findViewById(R.id.stowageButton).setSelected(false);
 		findViewById(R.id.annotationButton).setSelected(false);
 		findViewById(R.id.groundButton).setSelected(false);
 		selectedStep = -1;
 	}
 
 	/**
 	 * Initializes elapsed time view
 	 * Handler handles updates for the elapsed time 
 	 */
 	private void initElapsedTime(){
 		startTime = System.currentTimeMillis();
         
 	    final Handler elapsedTimeHandler = new Handler();
 	    Runnable elapsedTimeRun = new Runnable() {
 	        @Override
 	        public void run() {
 	           long millis = System.currentTimeMillis() - startTime;
 	           int seconds = (int) (millis / 1000);
 	           int minutes = seconds / 60;
 	           seconds     = seconds % 60;
 	           int hours = minutes / 60;
 	           minutes = minutes % 60;
 	           ((TextView) findViewById(R.id.elapsedTime)).setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
 
 	           elapsedTimeHandler.postDelayed(this, 500);
 	        }
 	    };
 	    
 		elapsedTimeHandler.postDelayed(elapsedTimeRun, 0);
 	}
 
 
 	/**
 	 * Sets the timer onClick functionality for testing
 	 */
 	private void initTimer(){
 		((TextView) findViewById(R.id.timer)).setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View arg0) {
 				if (timerState.equals(TIMER_OFF))
 					commandStartTimer();
 				else if (timerState.equals(TIMER_ON))
 					commandStopTimer();
 				
 				if (timerState.equals(TIMER_RESET)){
 					commandStartTimer();
 				}
 			}
 		});
 		
 		((TextView) findViewById(R.id.timer)).setOnLongClickListener(new OnLongClickListener(){
 			@Override
 			public boolean onLongClick(View arg0) {
 				commandResetTimer();
 				return false;
 			}
 		});
 	}
 	
 	/**
 	 * Starts the timer
 	 */
 	private void commandStartTimer(){
 		 timerState = TIMER_ON;
 		 timerStartTime = System.currentTimeMillis();
 		 
 		 final Handler timerHandler = new Handler();
 		 Runnable timerRunner = new Runnable() {
 			@Override
 			public void run() {
 				TextView topTimerTimeText = ((TextView) findViewById(R.id.timer));
 				
 				// Time calculations
 				long millis = System.currentTimeMillis() - timerStartTime + timerDuration;
 				if (timerState.equals(TIMER_OFF)){
 					millis = timerDuration;
 				}else if (timerState.equals(TIMER_RESET))
 					millis = 0;
 				int seconds = (int) (millis / 1000);
 		        int minutes = seconds / 60;
 		        seconds     = seconds % 60;
 		        
 		        // Handles top timer
 		        topTimerTimeText.setText(String.format("%02d:%02d", minutes, seconds));
 		   
 		        // Handles timer if it is on a page
 				if (findViewById(R.id.timerTimeText) != null){
 					 TextView timerTimeText = ((TextView) findViewById(R.id.timerTimeText));
 					 timerTimeText.setText(String.format("%02d:%02d", minutes, seconds));
 					
 					 if(timerState.equals(TIMER_ON)){
 						 TextView tv = (TextView) findViewById(R.id.timerStopText);
 						 tv.setVisibility(View.VISIBLE);
 					 }else if(timerState.equals(TIMER_OFF) || timerState.equals(TIMER_RESET)){
 						 TextView tv = (TextView) findViewById(R.id.timerStopText);
 						 tv.setVisibility(View.GONE);
 					 }
 				}
 				timerHandler.postDelayed(this, 0);
 			}
 
 		 };
 		 timerHandler.postDelayed(timerRunner, 0);
 	}
 	
 	/**
 	 * Resets the timer
 	 */
 	private void commandResetTimer(){
 		 timerDuration = 0;
 		 timerStartTime = System.currentTimeMillis();
 		 timerState = TIMER_RESET;
 	}
 	
 	/**
 	 * Stops/pauses the timer
 	 */
 	private void commandStopTimer(){
 		 timerDuration += System.currentTimeMillis() - timerStartTime;
 		 timerState = TIMER_OFF;
 		 
 	}
 	
 	/**
 	 * Initialize the view pager.  This is the central ui element that 
 	 * allows swiping between step pages.
 	 */
 	private void initViewPager(){
 		viewPager = (ViewPager) findViewById(R.id.viewpager);	// gets the ViewPager UI object from its XML id
 		List<ViewGroup> sp = setupPages();
 		List<ViewGroup> scrollViewPages = setupScrollViewPages(sp);
 		
 		
 		PagerAdapter pagerAdapter = new PageAdapter(this, scrollViewPages); // the PagerAdapter is used to popuplate the ViewPager
 		
 		
 		viewPager.setAdapter(pagerAdapter);
 		viewPager.setCurrentItem(0);
 		
 		
 		// sets a listener for whenever the page in the ViewPager changes.
 		viewPager.setOnPageChangeListener(new OnPageChangeListener(){
 
 			@Override
 			public void onPageScrollStateChanged(int arg0) {
 				//Log.v("viewPager","onPageScrollStateChanged");
 			}
 
 			@Override
 			public void onPageScrolled(int arg0, float arg1, int arg2) {
 				//Log.v("viewPager","onPageScrolled");
 			}
 
 			@Override
 			public void onPageSelected(int arg0) {
 				//Log.v(TAG,"viewPager onPageSelected: "+arg0);
 				breadcrumb.setCurrentStep(arg0+1); // updates breadcrumb when a new page is selected
 				stepPreviewWidget.setCurrentStep(procedure,arg0);
 			
 			}
 			
 		});
 	}
 	
 	
 	
 	/**
 	 * Puts procedure step pages within scroll views
 	 */
 	private List<ViewGroup> setupScrollViewPages(List<ViewGroup> stepPages){
 		List<ViewGroup> result = new ArrayList<ViewGroup>();
 		for (int i = 0; i < stepPages.size(); i++){
 			StepPageScrollView sv = new StepPageScrollView(this,stepPages.get(i));
 			result.add(sv);
 		}
 		
 		return result;
 	}
 	
 	
 	
 	/**
 	 * Setup the procedure steps as a list of step pages.
 	 * 
 	 * @return
 	 */
 	private List<ViewGroup> setupPages() {
 		List<ViewGroup> result = new ArrayList<ViewGroup>();
 		
 		result.add(new CoverPage(this, procedure.getNumber(), procedure.getTitle(), procedure.getObjective(), procedure.getDuration()));
 		
 		result.add(new StowagePage(this, procedure.getStowageItems()));
 		
 		result.add(new ExecNotesPage(this, procedure.getExecNotes()));
 		
 		for (int i = 0; i < procedure.getNumChildren(); i++){
 			result.addAll(setupStepPage(procedure.getChildAt(i), null, 0));
 		}
 		
 		result.add(new CompletionPage(this));
 		
 		return result;
 	}
 	
 	
 	
 	/**
 	 * Setup the given step as a list of step pages.  First checks for
 	 * and sets any execution notes which may exist for the given step.
 	 * Recursively loops through any substeps to get all children.
 	 * 
 	 * TODO: redo how step pages get their parents. this is dumb
 	 * 
 	 * @param step
 	 * @return
 	 */
 	private List<ViewGroup> setupStepPage(ProcedureItem item, ProcedureItem parent, int cycleNum) {
 		List<ViewGroup> result = new ArrayList<ViewGroup>();
 
 		//If a cycle
 		if (item.isCycle()) {
 			Cycle c = (Cycle) item;
 			//Add all the reps
 			for (int i = 0; i < c.getReps(); i++) {
 				result.add(new CycleMarkerPage(this, c, i+1));
 				//Add the steps for each rep
 				for (int j = 0; j < c.getNumChildren(); j++) {
 					//setup the child - parent = null
 					result.addAll(setupStepPage(c.getChild(j), null, i+1));
 				}
 			}
 			
 		//If a step	
 		} else {
 			Step s = (Step) item;
 			Step p = parent == null ? null : (Step) parent;
 			
 			//Setup the execution note for this step
 			int execNoteIndex = getExecNoteIndex(s.getNumber());
 			if (execNoteIndex > -1) s.setExecNote(procedure.getExecNotes().get(execNoteIndex));
 			
 			//If a parent step
 			if (s.getNumChildren() > 0) {
 				for (int i = 0; i < s.getNumChildren(); i++) {
 					//setup the child
 					result.addAll(setupStepPage(s.getChild(i), s, cycleNum));
 				}
 			
 			//Its a leaf step
 			} else {				
 				//setup the step
 				result.add(new StepPage(this, s, p, cycleNum));
 			}
 		}
 		
 		return result;
 	}
 	
 	
 	
 	/**
 	 * Listens to broadcast messages
 	 */
 	private class DataUpdateReceiver extends BroadcastReceiver {
 		@Override
 	    public void onReceive(Context context, Intent intent) {
 	
 	    	if (intent.getAction().equals(MessageHandler.MSG_TYPE_COMMAND)) {
         		handleCommand(intent.getExtras());
         		
 	    	} else if (intent.getAction().equals(MessageHandler.MSG_TYPE_AUDIO_LEVEL)) {
 	    		float rms = Float.parseFloat(intent.getExtras().getString("msg"));
 	    		audioFeedbackView.updateAudioFeedbackView(rms);
 	    		
 	    	} else if (intent.getAction().equals(MessageHandler.MSG_TYPE_AUDIO_BUSY)) {
 	    		boolean busyState = Boolean.parseBoolean(intent.getExtras().getString("msg"));
 	    		audioFeedbackThread.setBusy(busyState);
 	    		Log.v(TAG, "busyState: " + busyState);
 	    		
 	    	} else if (intent.getAction().equals(MessageHandler.MSG_TYPE_AUDIO_STATE)) {
 	    		int state = intent.getExtras().getInt("msg");
 	    		audioFeedbackThread.setState(state);
 	    		//Log.v(TAG, "state: " + state);
 	    		
 	    	} else if (intent.getAction().equals(MessageHandler.MSG_TYPE_AR_READ)) {
 	    		String val = intent.getExtras().getString("msg");
 	    		Log.v(TAG, "Received AR input: " + val);
 	    		commandLogInput(val);
 	    	}
 	    }
 	}
 
 
 
 	/**
      * All commands are handled here.
      * 
      * Convention: 
      * 		extras.getInt("msg") - the command to be run (see MessageHandler class)
      * 		extras.getString("str") - an additional string if needed (e.g. commands for going to a particular step or cycle)
      * 
      */
     private void handleCommand(Bundle extras){
     	int command = extras.getInt("msg");
     	
     	if (command != MessageHandler.COMMAND_NOT_FOUND) {
     		Log.v(TAG, "handleCommand: " + command);
 
     		audioFeedbackThread.setBusy(false);
 		
 			if (command == MessageHandler.COMMAND_BACK) { 					
     			commandBack();
 
 			} else if (command == MessageHandler.COMMAND_NEXT) {  			
 				commandNext();
 				
 			} else if (command == MessageHandler.COMMAND_SCROLL_DOWN) {		
 				commandScrollDown();
 				
 			} else if (command == MessageHandler.COMMAND_SCROLL_UP) {
 				commandScrollUp();
 				
 			} else if (command == MessageHandler.COMMAND_GO_TO_STEP){
 				commandGoToStep(extras);
 				
 			} else if (command == MessageHandler.COMMAND_STEP_NUMBER){
 				commandGoToStep(extras);
 
 			} else if (command == MessageHandler.COMMAND_CYCLE_NUMBER){
 				commandGoToCycle(extras);
 				
 			} else if (command == MessageHandler.COMMAND_MENU){
 				commandMenu();
 		
 			} else if (command == MessageHandler.COMMAND_CLOSE){
 				commandClose();
 			
 			} else if (command == MessageHandler.COMMAND_MENU_OVERVIEW){
 	    		menuSelect(findViewById(R.id.navButton));
 
 			} else if (command == MessageHandler.COMMAND_MENU_STOWAGE) {
 	    		menuSelect(findViewById(R.id.stowageButton));
 	    		
 	    	} else if (command == MessageHandler.COMMAND_MENU_ANNOTATION) {
 	    		menuSelect(findViewById(R.id.annotationButton));
 	    		
 	    	} else if (command == MessageHandler.COMMAND_MENU_GROUND) {
 	    		menuSelect(findViewById(R.id.groundButton));
 	    		
 	    	} else if (command == MessageHandler.COMMAND_TIMER_START){
 	    		commandStartTimer();
 	    		
 	    	} else if (command == MessageHandler.COMMAND_TIMER_STOP){
 	    		commandStopTimer();
 	    		
 	    	} else if (command == MessageHandler.COMMAND_TIMER_RESET){
 	    		commandResetTimer();
 	    		
 	    	} else if (command == MessageHandler.COMMAND_COND_EXPAND){
 		    	commandCondExpand();
 		    	
 		    } else if (command == MessageHandler.COMMAND_COND_COLLAPSE){
 		    	commandCondCollapse();
 		    	
 		    } else if (command == MessageHandler.COMMAND_INPUT) {
 		    	// This is currently being handled with AR
 		    }
     		
     	}
     }
 	
     
     
 	/**
 	 * Handles the back command  
 	 */
     private void commandBack(){
     	Log.v(TAG,"getMenuVisibility: "+ getMenuVisibility() + ", getOpenedDrawer: "+ getOpenedDrawer());
     	
     	/*
     	 * If menu and drawer are closed,
     	 * 		Go to the next previous page.
     	 */
     	if (!getMenuVisibility() && getOpenedDrawer().equals(DrawerPageInterface.DRAWER_NONE)){	
 	    	if (viewPager.getCurrentItem() > 0)
 	    		viewPager.setCurrentItem(viewPager.getCurrentItem()-1,true);
 		}
     	
     	/*
     	 * If menu is open,
     	 * 		Close the menu.
     	 */
     	else if (getMenuVisibility()){
     		closeMenu();
     	}
     	
     	/*
     	 * If menu is not open but drawer is,
     	 * 		Close the drawer.
     	 */
     	else if (!getMenuVisibility() && !getOpenedDrawer().equals(DrawerPageInterface.DRAWER_NONE)){
     		closeDrawer();
     	}
     }
     
     
     
     /**
      * Handles the next command
      */
     private void commandNext(){
     	/*
     	 * If menu and drawer are closed,
     	 * 		Go to the next step page.
     	 */
 		if (!getMenuVisibility() && getOpenedDrawer().equals(DrawerPageInterface.DRAWER_NONE)){	
 	    	if (viewPager.getCurrentItem()<viewPager.getChildCount());
 	    		viewPager.setCurrentItem(viewPager.getCurrentItem()+1,true);
 		}
     }
     
     
     
     /**
      * Handles the scroll down command
      */
     private void commandScrollDown(){
     	/*
     	 * If menu and drawer are closed, 
     	 * 		scroll the step page 
     	 */
 		if (!getMenuVisibility() && getOpenedDrawer().equals(DrawerPageInterface.DRAWER_NONE)){	
 	        StepPageScrollView curPage = (StepPageScrollView) viewPager.findViewWithTag(viewPager.getCurrentItem());    	
 	    	curPage.scrollDown();
 	    	
 	    /*
 	     * If drawer is open, 
 	     * 		scroll the drawer
 	     */
 		}else if (!getOpenedDrawer().equals(DrawerPageInterface.DRAWER_NONE)){
 			scrollDrawerDown();
 		}
     }
 
     
     /**
      * Handles the scroll up command
      */
     private void commandScrollUp(){
     	/*
     	 * If menu and drawer are closed, scroll the step page 
     	 */
 		if (!getMenuVisibility() && getOpenedDrawer().equals(DrawerPageInterface.DRAWER_NONE)){	
 	        StepPageScrollView curPage = (StepPageScrollView) viewPager.findViewWithTag(viewPager.getCurrentItem());    	
 	    	curPage.scrollUp();
 	    	
 	    /*
 	     * If drawer is open, scroll the drawer
 	     */
 		}else if (!getOpenedDrawer().equals(DrawerPageInterface.DRAWER_NONE)){	
 			scrollDrawerUp();
 		}
     }
     
     /**
      * Scrolls current item in drawer down
      */
     private void scrollDrawerDown(){
       	ScrollView drawer = ((ScrollView)findViewById(R.id.menuDrawer));
 	    drawer.smoothScrollBy(0, (int) (drawer.getHeight()*0.6f));
     }
 
     /**
      * Scrolls current item in drawer up
      */
     private void scrollDrawerUp(){
       	ScrollView drawer = ((ScrollView)findViewById(R.id.menuDrawer));
 	    drawer.smoothScrollBy(0, (int) (drawer.getHeight()*-0.6f));
     }
     
     /**
      * Handles the go to step command
      */
     private void commandGoToStep(Bundle extras){
 	    /*
 	     * If the cycle menu is not open, 
 	     * 		handle the step command
 	     */
     	if (!getOpenedDrawer().equals(DrawerPageInterface.DRAWER_CYCLE_SELECT))
     		handleNavigationCommand(extras.getString("str"));
     }
     
     /**
      * Handles the go to cycle command
      */
     private void commandGoToCycle(Bundle extras){
 	    /*
 	     * If the cycle is open, 
 	     * 		handle the command
 	     */
     	if (getOpenedDrawer().equals(DrawerPageInterface.DRAWER_CYCLE_SELECT))
     		handleNavigationCommand(extras.getString("str"));
     }
     
     /*Because of the need to select cycles, we need a branch in
 	  logic.  If the given step number is in a cycle, then we 
 	  need to set the selectedStep variable to keep track of what
 	  was selected and bring up the cycle select menu.  If another
 	  number is then given, then we use the selectedStep variable
 	  to jump to the correct step.  If the step isn't in a cycle,
 	  then we just jump to the first occurrence.
 	*/
     private void handleNavigationCommand(String inputString) {
 		int inputNumber;
 		
 		//TODO this whole thing only works if step numbers are plain integers
 		try{
 			inputNumber = Integer.parseInt(inputString);
 		} catch(NumberFormatException e){
 			inputNumber = -1;
 		}
 		
 		if (inputNumber >= 0){
 			//If the input is on the cycle select page
 			if (selectedStep > -1) {
 				jumpToStep(selectedStep, inputNumber);
 			
 			//Otherwise, it is just a normal nav selection
 			} else {
 				int reps = getStepReps(inputNumber);
 				//If in a cycle, 
 	    		if (reps > 1) {
 	    			selectedStep = inputNumber;
 	    			openStepDrawer(new CycleSelectPage(this, reps, inputNumber));
 	    		} else {
 	    			jumpToStep(inputNumber, 1);
 	    		}	
 			}
 		}
     }
     
     /**
      * Handles menu command
      */
     private void commandMenu(){
     	if (!getMenuVisibility())
     		openMenu();
     	else if (getMenuVisibility())
     		closeMenu();
     }
     
     /**
      * Handles close command
      */
     private void commandClose(){
     	/*
     	 * If menu is closed but drawer is open
     	 * 		close the drawer
     	 */
     	if (!getMenuVisibility() && !getOpenedDrawer().equals(DrawerPageInterface.DRAWER_NONE)){
     		closeDrawer();
     	}else if (getMenuVisibility()){
     		closeMenu();
     	}
     }
     
     /**
      * Expands conditional
      * TODO: make this not slow
      */
     private void commandCondExpand(){
     	// Only if menu and drawer are both closed
 		if (!getMenuVisibility() && getOpenedDrawer().equals(DrawerPageInterface.DRAWER_NONE)){	
 	    	StepPageScrollView curScrollPage = (StepPageScrollView) viewPager.findViewWithTag(viewPager.getCurrentItem());    	
 
 			final TextView consText = (TextView)curScrollPage.findViewById(R.id.consequentText);
 			if (consText != null){
 				final TextView consTitle = (TextView)curScrollPage.findViewById(R.id.consequentMarker);
 				consText.setVisibility(View.VISIBLE);
 				consTitle.setText(R.string.cond_title_shown);
 	        }
 		}
     }
     
     /**
      * Collapses conditional
      * TODO: make this not slow
      */
     private void commandCondCollapse(){
     	// Only if menu and drawer are both closed
     	if (!getMenuVisibility() && getOpenedDrawer().equals(DrawerPageInterface.DRAWER_NONE)){	
 	        StepPageScrollView curScrollPage = (StepPageScrollView) viewPager.findViewWithTag(viewPager.getCurrentItem());    	
 
 	        final TextView consText = (TextView)curScrollPage.findViewById(R.id.consequentText);
 			if (consText != null){
 				final TextView consTitle = (TextView)curScrollPage.findViewById(R.id.consequentMarker);
 				consText.setVisibility(View.GONE);
 				consTitle.setText(R.string.cond_title_hidden);
 	        }
     	}
     }
     
     
     /**
      * Adds the logged input to the current step page.
      */
     private void commandLogInput(String value) {
     	StepPageScrollView curScrollPage = (StepPageScrollView) viewPager.findViewWithTag(viewPager.getCurrentItem());
     	TextView inputText = (TextView)curScrollPage.findViewById(R.id.inputValue);
     	if (inputText != null) {
     		inputText.setText(value);
     		curScrollPage.findViewById(R.id.inputReceived).setVisibility(View.VISIBLE);
     	}
     }
     
     /**
      * Jump to the step at the given index.  Looks up the page
      * to jump to based on the stepIndices list.
      * 
      * TODO: should it be in the UI thread?  Should other methods?
      * 
      * @param stepIndex
      * @param occurence
      */
     private void jumpToStep(int step, int occurrence) {
     	if (step >= 0) {
     		final int index = getPageIndex(step, occurrence);
     		
     		runOnUiThread(new Runnable() {
     			public void run() { 
     				if (getMenuVisibility())
     					closeMenu();
     				else
     					closeDrawer();
     				viewPager.setCurrentItem(index, true);
       	      	}
     		});
     	}
     }
     
     
     
     /**
 	 * The onclick method for all menu buttons.  Handles the drawer movement and 
 	 * population.
 	 * 
 	 * @param v
 	 */
 	public void menuSelect(View v) {
 		LinearLayout menuDrawerLayout = (LinearLayout) findViewById(R.id.menuDrawerLayout);
 		
 		//If I hit the same menu button
 		if (v.isSelected()) {
 			Log.v(TAG,"menuSelect: same menu");
 			menuDrawerLayout.startAnimation(menuAnimations.get(menuDrawerLayout.getId() + TAG_CLOSE));
 			menuDrawerLayout.setVisibility(View.GONE);
 			v.setSelected(false);
 		} else {
 			
 			//Setup the new menu content
 			switch (v.getId()) {
 				case R.id.navButton :
 					drawerContent = new NavigationPage(v.getContext(), procedure.getChildren(), getCurrentStep());
 					break;
 				case R.id.stowageButton:
 					drawerContent = new StowagePage(v.getContext(), procedure.getStowageItems());
 					break;
 				case R.id.annotationButton:
 					drawerContent = new AnnotationPage(v.getContext());
 					break;
 				case R.id.groundButton:
 					drawerContent = new GroundPage(v.getContext());
 					break;
 				default:
 			}
 			
 			//If the drawer is open for another menu
 			if (menuDrawerLayout.getVisibility() != View.GONE) {
 				//change drawer
 				menuDrawerLayout.startAnimation(menuAnimations.get(menuDrawerLayout.getId() + TAG_CYCLE));
 				clearMenuSelection();
 		    //If the drawer is closed
 			} else {
 				//open the drawer
 				menuDrawerLayout.startAnimation(menuAnimations.get(menuDrawerLayout.getId() + TAG_OPEN));
 				menuDrawerLayout.setVisibility(View.VISIBLE);
 			}
 		    
 			v.setSelected(true);
 		}
 	}
 
 	
 	/**
 	 * Get the index of the execution note for the given step number.
 	 * If no execution note exists, return -1.
 	 * 
 	 * @param stepNumber the step number to check for notes
 	 * @return the index of the corresponding execution note
 	 */
 	private int getExecNoteIndex(String stepNumber) {
 		List<ExecNote> notes = procedure.getExecNotes();
 		for (int i = 0; i < notes.size(); i++) {
 			if (stepNumber.equals(notes.get(i).getNumber())) return i;
 		}
 		
 		return -1;
 	}
 
 
 
 	/**
 	 * Returns a list of page indices.  The list represents the overall index
 	 * of where a step starts (inclusive) in regards to their substeps.  This is needed
 	 * to move between the viewpager's flattened list of steps and the actual 
 	 * nested structure of the procedure object.
 	 * 
 	 * Because of cycles, we will have to check for the number of occurrences of 
 	 * a natural index later on when searching this list.
 	 * 
 	 * 
 	 * @return
 	 */ 
 	private List<StepIndex> getPageIndices(){
 		List<StepIndex> result = new ArrayList<StepIndex>();
 		List<ProcedureItem> steps = procedure.getChildren();
 
 		int stepNumber = 0;
 		int curIndex = PREPARE_PAGES;
 		
 		for (int i = 0; i < steps.size(); i++) {
 			
 			if (steps.get(i).isCycle()) {
 				Cycle c = (Cycle) steps.get(i);
 				
 				//Assuming that there are only steps within a cycle
 				for (int j = 0; j < c.getReps(); j++) {					
 					//We need to keep our place outside of the cycle
 					int repStepNumber = stepNumber;
 					//Also, we offset for the cycle marker page
 					curIndex++;
 					
 					for (int k = 0; k < c.getNumChildren(); k++) {						
 						result.add(new StepIndex(++repStepNumber, curIndex));
 						
 						int substeps = c.getChild(k).getNumChildren();
 						curIndex += substeps > 0 ? substeps : 1;
 					}
 					
 					//Account for the steps in the cycle
 					if (j == c.getReps() - 1) stepNumber = repStepNumber;
 				}
 								
 			} else {				
 				result.add(new StepIndex(++stepNumber, curIndex));
 				
 				int substeps = steps.get(i).getNumChildren();
 				curIndex += substeps > 0 ? substeps : 1;
 			}			
 		}
 		
 		Log.v(TAG, "Step Index: " + result.toString());
 		return result;
 	}
 
 
 
 	/**
 	 * Search the pageIndices list for the given occurrence 
 	 * of the given step.  Then return the flat upper bound of
 	 * that step.  
 	 * 
 	 * Remember, the indices list is 1 based and contains the 
 	 * the inclusive start index of that step.
 	 * 
 	 * @param step
 	 * @param occurrence
 	 * @return
 	 */
 	private int getPageIndex(int step, int occurrence) {
 		int counter = 0;
 		for (int i = 0; i < stepIndices.size(); i++) {
 			if (stepIndices.get(i).getNaturalIndex() == step) {
 				counter++;
 				if (counter == occurrence) return stepIndices.get(i).getFlatIndex();
 			}
 		}
 		
 		return 0;
 	}
 	
 	
 	
 	/**
 	 * Get the overall parent step of the one currently being viewed.
 	 * Move through the stepIndices list to see where the currently
 	 * viewed page lies in terms of the overall procedure data structure.
 	 * 
 	 * This is needed to move from the linear indexing of the viewpager 
 	 * and the nested structure of the procedure object.
 	 * 
 	 * Remember, the step number returned is 1 based.
 	 * 
 	 * @return 
 	 */
 	private int getCurrentStep() {
 		int curIndex = viewPager.getCurrentItem();
 
 		if (viewPager.getCurrentItem() >= PREPARE_PAGES) {
 			for (int i = 0; i < stepIndices.size(); i++) {
 				StepIndex si = stepIndices.get(i);
 				
 				if (curIndex >= si.getFlatIndex()) {
 					//If if the last index
 					if (i == stepIndices.size()-1) return si.getNaturalIndex();
 					//If between this index and the start of the next one
 					if (curIndex < stepIndices.get(i+1).getFlatIndex()) return si.getNaturalIndex();
 				}				
 	    	}
 		} 
 	
 		return -1;
 	}
 	
 	
 	
 	//TODO this only goes through the top level of steps
 	private int getStepReps(int stepNumber) {
 		int reps = 0;
 		List<ProcedureItem> steps = procedure.getChildren();
 		
 		for (int i = 0; i < steps.size(); i++) {
 			
 			if (steps.get(i).isCycle()) {
 				Cycle c = (Cycle)steps.get(i);
 				for (int j = 0; j < c.getNumChildren(); j++) {
 					//TODO This breaks for nested cycles
 					if (stepNumber == Integer.parseInt(((Step)c.getChild(j)).getNumber())) {
 						reps += c.getReps();
 						break;
 					}
 				}
 				
 			} else if (stepNumber == Integer.parseInt(((Step)steps.get(i)).getNumber())) {
 				reps++;
 			}
 		}
 		
 		return reps;
 	}
 	
 	
 	
 	/**
 	 * A private class for keeping track of step indexes.
 	 * A pretty basic tuple class.
 	 * 
 	 * @author Chris
 	 *
 	 */
 	private class StepIndex {
 		private int naturalIndex;
 		private int flatIndex;
 		
 		public StepIndex(int naturalIndex, int flatIndex) {
 			this.naturalIndex = naturalIndex;
 			this.flatIndex = flatIndex;
 		}
 		
 		public int getNaturalIndex() {
 			return naturalIndex;
 		}
 		
 		public int getFlatIndex() {
 			return flatIndex;
 		}
 		
 		public String toString() {
 			return "<" + naturalIndex + "," + flatIndex + ">";
 		}
 	}
 	
 
 }
