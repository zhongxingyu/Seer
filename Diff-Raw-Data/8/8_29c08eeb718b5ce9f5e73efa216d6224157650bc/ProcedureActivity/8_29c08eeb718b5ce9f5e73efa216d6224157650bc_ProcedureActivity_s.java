 package edu.cmu.hcii.novo.kadarbra;
 
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
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.animation.AnimationUtils;
 import android.widget.Button;
 import android.widget.FrameLayout;
 import android.widget.TextView;
 import edu.cmu.hcii.novo.kadarbra.page.AnnotationPage;
 import edu.cmu.hcii.novo.kadarbra.page.CoverPage;
 import edu.cmu.hcii.novo.kadarbra.page.ExecNotesPage;
 import edu.cmu.hcii.novo.kadarbra.page.GroundPage;
 import edu.cmu.hcii.novo.kadarbra.page.NavigationPage;
 import edu.cmu.hcii.novo.kadarbra.page.PageAdapter;
 import edu.cmu.hcii.novo.kadarbra.page.StepPage;
 import edu.cmu.hcii.novo.kadarbra.page.StepPageScrollView;
 import edu.cmu.hcii.novo.kadarbra.page.StowagePage;
 import edu.cmu.hcii.novo.kadarbra.structure.ExecNote;
 import edu.cmu.hcii.novo.kadarbra.structure.Procedure;
 import edu.cmu.hcii.novo.kadarbra.structure.Step;
 
 public class ProcedureActivity extends Activity {
 	private static final String TAG = "ProcedureActivity";	// used for logging purposes
 	public final static String CURRENT_STEP = "edu.cmu.hcii.novo.kadarbra.CURRENT_STEP";
 
 	public final static int PREPARE_PAGES = 3; // number of pages in prepare stage (before steps are shown)
 	public final static int OPEN_MENU = 0; // startActivityForResult call identifier
 	
 	private Procedure procedure;
 	private ViewPager viewPager;
 	private Breadcrumb breadcrumb;
 	private StepPreviewWidget stepPreviewWidget;
 	private DataUpdateReceiver dataUpdateReceiver;
 	
 	private List<Integer> procedureIndex;
 	
 	private Map<String, Animation> menuAnimations;
 	private View drawerContent;
 	private final String openTag = "_open";
 	private final String closeTag = "_close";
 	private final String cycleTag = "_cycle";
 	private final String cascadeTag = "_cascade";
 	private final int delay = 50;
 	
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		
 		Intent intent = getIntent();
 		procedure = (Procedure)intent.getSerializableExtra(MainActivity.PROCEDURE);
 		
 		setContentView(R.layout.activity_procedure);
 		
		initBreadcrumb();
 		initStepPreviewWidget();
 		initMenu();
 		initViewPager();
 		
 		procedureIndex = getPageIndex();
 	}
 	
 	
 	
 	/**
 	 * Called when child activity returns some result
 	 */
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 				
 		/**
 		 * For messages sent by the menu
 		 */
 		if (requestCode == OPEN_MENU){
 			if (resultCode == Activity.RESULT_OK){
 				/**
 				 * For navigate commands
 				 */
 				if (data.getStringExtra("command").equals("navigate")){
 					final int navigate = data.getIntExtra("navigate",0);
 					Log.v(TAG,""+navigate);
 					
 					if (navigate != 0){
 						runOnUiThread(new Runnable() {
 		            	      public void run() { 
 		            	    	  int page = procedureIndex.get(navigate);
 		            	    	  viewPager.setCurrentItem(page,true);
 		            	      }
 		            	});
 					}
 					
 				}
 			}
 		}
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
 	    //edu.cmu.hcii.novo.kadarbra.MainApp.setCurrentActivity(this);
 	    Log.v(TAG, "onResume");
 	
 	    if (dataUpdateReceiver == null) 
 	    	dataUpdateReceiver = new DataUpdateReceiver();
 	    IntentFilter intentFilter = new IntentFilter("command");
 	    registerReceiver(dataUpdateReceiver, intentFilter);
 	}
 
 	
 	
 	// The activity is paused
 	@Override
 	protected void onPause(){
 		super.onPause();
 		//clearReferences();
 		Log.v(TAG, "onPause");
 		if (dataUpdateReceiver != null) 
 			unregisterReceiver(dataUpdateReceiver);
 	}
 
 	
 	
 	// The activity is no longer visible (it is now "stopped")
 	@Override
 	protected void onStop() {
 	    super.onStop();
 	    //clearReferences();
 	    Log.v(TAG, "onStop");
 	}
 	
 	
 
 	// The activity is about to be destroyed.
 	@Override
 	protected void onDestroy() {
 	    super.onDestroy();
 	    //clearReferences();
 	    Log.v(TAG, "onDestroy");
 	}
 
 	
 	
 	// initializes the Breadcrumb (currently just step numbers)
 	private void initBreadcrumb(){
 		breadcrumb = (Breadcrumb) findViewById(R.id.breadcrumb);
 		breadcrumb.setTotalSteps(viewPager.getAdapter().getCount());
 		breadcrumb.setCurrentStep(1);
 		//breadcrumb.setVisibility(View.INVISIBLE);
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
 				View drawer = (View) findViewById(R.id.menuDrawer);
 				
 				if (drawer.getVisibility() == View.VISIBLE) {
 					closeMenu();
 					
 				} else {
 					View bg = (View) findViewById(R.id.menuBackground);
 					
 					if (bg.getVisibility() == View.VISIBLE) {
 						closeMenu();
 					} else {
 						openMenu();
 					}
 					
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
 		addMenuAnimation(curId + openTag, R.anim.menu_enter, 0, new AnimationListener() {
 
 			@Override
 			public void onAnimationEnd(Animation arg0) {
 				//Open the menu.  Run each menu item's open animation and set their visibility to VISIBLE.
 				runMenuItemAnimations(openTag, View.VISIBLE);
 			}
 
 			@Override
 			public void onAnimationRepeat(Animation arg0) {}
 
 			@Override
 			public void onAnimationStart(Animation arg0) {}
 			
 		});
 		addMenuAnimation(curId + closeTag, R.anim.menu_exit, 0, null);
 		
 		//Menu button animations
 		curId = findViewById(R.id.navButton).getId();
 		addMenuAnimation(curId + openTag, R.anim.menu_enter, delay, null);
 		addMenuAnimation(curId + closeTag, R.anim.menu_exit, delay*4, new AnimationListener() {
 
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				//Start the menu background's close animation
 				View bg = (View) findViewById(R.id.menuBackground);
 				
 				bg.startAnimation(menuAnimations.get(bg.getId() + closeTag));
 				bg.setVisibility(View.GONE);
 			}
 
 			@Override
 			public void onAnimationRepeat(Animation animation) {}
 
 			@Override
 			public void onAnimationStart(Animation animation) {}
 			
 		});
 		
 		curId = findViewById(R.id.stowageButton).getId();
 		addMenuAnimation(curId + openTag, R.anim.menu_enter, delay*2, null);
 		addMenuAnimation(curId + closeTag, R.anim.menu_exit, delay*3, null);
 		
 		curId = findViewById(R.id.annotationButton).getId();
 		addMenuAnimation(curId + openTag, R.anim.menu_enter, delay*3, null);
 		addMenuAnimation(curId + closeTag, R.anim.menu_exit, delay*2, null);
 		
 		curId = findViewById(R.id.groundButton).getId();
 		addMenuAnimation(curId + openTag, R.anim.menu_enter, delay*4, null);
 		addMenuAnimation(curId + closeTag, R.anim.menu_exit, delay, null);
 		
 		//Drawer animations
 		curId = findViewById(R.id.menuDrawer).getId();
 		addMenuAnimation(curId + openTag, R.anim.menu_drawer_enter, 0, new AnimationListener() {
 
 			@Override
 			public void onAnimationEnd(Animation animation) {}
 
 			@Override
 			public void onAnimationRepeat(Animation animation) {}
 
 			@Override
 			public void onAnimationStart(Animation animation) {
 				if (drawerContent != null) {
 					((FrameLayout)findViewById(R.id.menuDrawer)).addView(drawerContent);
 					drawerContent = null;
 				}
 			}
 			
 		});
 		addMenuAnimation(curId + closeTag, R.anim.menu_drawer_exit, 0, new AnimationListener() {
 
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				FrameLayout drawer = (FrameLayout)findViewById(R.id.menuDrawer);
 				drawer.removeAllViews();
 			}
 
 			@Override
 			public void onAnimationRepeat(Animation animation) {}
 
 			@Override
 			public void onAnimationStart(Animation animation) {}
 			
 		});
 		addMenuAnimation(curId + cycleTag, R.anim.menu_drawer_exit, 0, new AnimationListener() {
 
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				FrameLayout drawer = (FrameLayout)findViewById(R.id.menuDrawer);
 				drawer.removeAllViews();
 				drawer.startAnimation(menuAnimations.get(drawer.getId() + openTag));
 			}
 
 			@Override
 			public void onAnimationRepeat(Animation animation) {}
 
 			@Override
 			public void onAnimationStart(Animation animation) {}
 			
 		});
 		addMenuAnimation(curId + closeTag + cascadeTag, R.anim.menu_drawer_exit, 0, new AnimationListener() {
 
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				FrameLayout drawer = (FrameLayout)findViewById(R.id.menuDrawer);
 				drawer.removeAllViews();
 				//Run each menu item's close animation and set their visibility to GONE.
 				runMenuItemAnimations(closeTag, View.GONE);
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
 	 * Open the menu.  Run the menu background's open animation and set
 	 * it's visibility to VISIBLE.  This automatically opens the itmes.
 	 */
 	private void openMenu() {
 		View bg = (View) findViewById(R.id.menuBackground);
 		
 		bg.startAnimation(menuAnimations.get(bg.getId() + openTag));
 		bg.setVisibility(View.VISIBLE);
 	}
 	
 	
 	
 	/**
 	 * Close the menu.  If the drawer is open, close it via it's cascading close.  
 	 * Otherwise, run each item's close animation and set their visibility to GONE.
 	 * Then make sure to make all buttons unselected.
 	 */
 	private void closeMenu() {
 		View drawer = (View) findViewById(R.id.menuDrawer);
 		
 		if (drawer.getVisibility() == View.VISIBLE) {
 			drawer.startAnimation(menuAnimations.get(drawer.getId() + closeTag + cascadeTag));
 			drawer.setVisibility(View.GONE);
 			
 		} else {
 			runMenuItemAnimations(closeTag, View.GONE);
 		}
 		
 		clearMenuSelection();
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
 		
 		overviewButton.startAnimation(menuAnimations.get(overviewButton.getId() + tag));
 		overviewButton.setVisibility(visibility);
 		
 		stowButton.startAnimation(menuAnimations.get(stowButton.getId() + tag));
 		stowButton.setVisibility(visibility);
 		
 		annotateButton.startAnimation(menuAnimations.get(annotateButton.getId() + tag));
 		annotateButton.setVisibility(visibility);
 		
 		groundButton.startAnimation(menuAnimations.get(groundButton.getId() + tag));
 		groundButton.setVisibility(visibility);
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
 				Log.v("viewPager","onPageSelected "+arg0);
 				breadcrumb.setCurrentStep(arg0+1); // updates breadcrumb when a new page is selected
 				stepPreviewWidget.setCurrentStep(procedure,arg0);
 				
 				/*if (!(viewPager.getChildAt(viewPager.getCurrentItem()).getClass() == StepPage.class)) {
 
 					Log.v(TAG, "Removing breadcrumb");
 					breadcrumb.setVisibility(View.INVISIBLE);
 				} else {
 					Log.v(TAG, "Removing breadcrumb");
 					breadcrumb.setVisibility(View.VISIBLE);
 				}*/
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
 		
 		for (int i = 0; i < procedure.getNumSteps(); i++){
 			result.addAll(setupStepPage(procedure.getStep(i), null));
 		}
 		
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
 	private List<ViewGroup> setupStepPage(Step step, Step parent) {
 		List<ViewGroup> result = new ArrayList<ViewGroup>();
 		
 		//TODO This won't work for more than 2 levels
 		String fullStepNumber = (parent != null ? parent.getNumber() + "." : "")  + 
 				step.getNumber();
 		
 		int execNoteIndex = getExecNoteIndex(fullStepNumber);
 		if (execNoteIndex > -1) step.setExecNote(procedure.getExecNotes().get(execNoteIndex));
 		
 		//If there are substeps, don't add the parent step, only the children
 		if (step.getNumSubsteps() > 0) {
 			for (int i = 0; i < step.getNumSubsteps(); i++) {
 				result.addAll(setupStepPage(step.getSubstep(i), step));
 			}
 		} else {
 			result.add(new StepPage(this, step, parent));
 		}
 		
 		return result;
 	}
 
 	
 
     /*
     private void clearReferences() {
     	Activity currActivity = edu.cmu.hcii.novo.kadarbra.MainApp.getCurrentActivity();
     	if (currActivity != null && currActivity.equals(this))
     		edu.cmu.hcii.novo.kadarbra.MainApp.setCurrentActivity(null);
     }
     */
     
     
     
 	/**
 	 * Listens to broadcast messages
 	 *  
 	 * @author Chris
 	 *
 	 */
     private class DataUpdateReceiver extends BroadcastReceiver {
     	@Override
         public void onReceive(Context context, Intent intent) {
        	Log.v(TAG, "on receive: " +intent.getAction());
 
         	if (intent.getAction().equals("command")) {
             	Bundle b = intent.getExtras();
             	String msg = b.getString("msg");
             	
             	Log.v(TAG, msg);
             	handleCommand(msg);
            	
           }
           
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
 	 * Gets index of pages, where each item in the returned list corresponds to a starting page of a step
 	 * @return pageIndex
 	 */ 
     private ArrayList<Integer> getPageIndex(){
     	ArrayList<Integer> index = new ArrayList<Integer>();
     	List<Step> steps = procedure.getSteps();
     	
     	int stepCounter = 0;
     	for (int i = 0; i < steps.size()+1; i++) {
     		if (i==0){
     			stepCounter += PREPARE_PAGES;
     			index.add(stepCounter);
     		}else{
 	    		Step s = steps.get(i-1);
 				int substeps = s.getNumSubsteps();
 				index.add(stepCounter);
 				if (substeps > 0)
 					stepCounter += substeps;
 				else
 					stepCounter++;
     		}
     	}
     	
 		return index;
     	
     }
     
     
 
     /**
 	 * Get the index of the procedure step currently being viewed.
 	 * 
 	 * @return the index of the current procedure step
 	 */
 	private int getCurrentStep() {
 		if (viewPager.getCurrentItem()>=PREPARE_PAGES) {
 			
 			// Iterates through the procedureIndex to see which higher level step section this current page is in
 			for (int i = 0; i < procedureIndex.size()-1; i++){
 				int page = procedureIndex.get(i);
 				int nextSectionStart = procedureIndex.get(i+1);
 	    		
 				if (viewPager.getCurrentItem() >= page && 
 	    			viewPager.getCurrentItem() < nextSectionStart) {
 	    			return i-1;
 	    		
 				} else if (viewPager.getCurrentItem() >= nextSectionStart) {
 	    			return i;
 	    		}
 	    	}
 			
 		} 
 	
 		return -1;
 	}
 
 	
 	
 	/**
      * All commands are handled here
      * @param command 
      */
     private void handleCommand(String command){
     	if (command.equals("back")) {
     		prevPage();
     	} else if (command.equals("next")) {
     		nextPage();
     	} else if (command.equals("down")) {
     		scrollDown();
     	} else if (command.equals("up")) {
     		scrollUp();
     	} else if (command.equals("menu")) {
     		openMenu();
     	}
     }
 	    
     
     
 	/**
 	 * Goes to previous page in viewPager    
 	 */
     private void prevPage(){
     	if (viewPager.getCurrentItem()>0)
     		viewPager.setCurrentItem(viewPager.getCurrentItem()-1,true);
     	else
     		finish();
     }
     
     
     
     /**
      * Goes to next page in viewPager
      */
     private void nextPage(){
     	if (viewPager.getCurrentItem()<viewPager.getChildCount());
     		viewPager.setCurrentItem(viewPager.getCurrentItem()+1,true);
     }
     
     
     
     /**
      * Scrolls the current StepPageScrollView down
      */
     private void scrollDown(){
     	((StepPageScrollView)(viewPager.findViewWithTag(viewPager.getCurrentItem()))).scrollDown();
     }
 
     
     
     /**
      * Scrolls the current StepPageScrollView up
      */
     private void scrollUp(){
     	((StepPageScrollView)(viewPager.findViewWithTag(viewPager.getCurrentItem()))).scrollUp();
     }
     
     
     
     /**
 	 * The onclick method for all menu buttons.  Handles the drawer movment and 
 	 * population.
 	 * 
 	 * @param v
 	 */
 	public void menuSelect(View v) {
 		FrameLayout drawer = (FrameLayout)findViewById(R.id.menuDrawer);
 		
 		//If I hit the same menu button
 		if (v.isSelected()) {
 			drawer.startAnimation(menuAnimations.get(drawer.getId() + closeTag));
 			drawer.setVisibility(View.GONE);
 			v.setSelected(false);
 		} else {
 			
 			//Setup the new menu content
 			switch (v.getId()) {
 				case R.id.navButton :
 					drawerContent = new NavigationPage(v.getContext(), procedure.getSteps(), getCurrentStep());
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
 			if (drawer.getVisibility() != View.GONE) {
 				//change drawer
 				drawer.startAnimation(menuAnimations.get(drawer.getId() + cycleTag));
 				clearMenuSelection();
 		    //If the drawer is closed
 			} else {
 				//open the drawer
 				drawer.startAnimation(menuAnimations.get(drawer.getId() + openTag));
 				drawer.setVisibility(View.VISIBLE);
 			}
 		    
 			v.setSelected(true);
 		}
 	}
 }
