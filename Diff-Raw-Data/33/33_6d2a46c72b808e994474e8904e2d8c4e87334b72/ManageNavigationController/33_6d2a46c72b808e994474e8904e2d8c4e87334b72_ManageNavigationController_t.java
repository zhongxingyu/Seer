 package gov.va.ptsd.ptsdcoach.activities;
 
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
 
 import com.flurry.android.FlurryAgent;
 import com.openmhealth.ohmage.campaigns.va.ptsd_explorer.PostExerciseSudsEvent;
 import com.openmhealth.ohmage.campaigns.va.ptsd_explorer.PreExerciseSudsEvent;
 import com.openmhealth.ohmage.campaigns.va.ptsd_explorer.ToolAbortedEvent;
 import com.openmhealth.ohmage.core.EventLog;
 
 import gov.va.ptsd.ptsdcoach.ContentDBHelper;
 import gov.va.ptsd.ptsdcoach.PTSDCoach;
 import gov.va.ptsd.ptsdcoach.Util;
 import gov.va.ptsd.ptsdcoach.content.Content;
 import gov.va.ptsd.ptsdcoach.controllers.ContentViewController;
 import gov.va.ptsd.ptsdcoach.controllers.ContentViewControllerBase;
 import gov.va.ptsd.ptsdcoach.controllers.SUDSController;

import java.util.TreeMap;
 
 public class ManageNavigationController extends NavigationController {
 
 	Content favorites;
 	Content symptom;
 	Content preselectedExercise;
 	MenuItem favoritesItem; 
 	int state;
 	int lastRandomExercise;
 	long lastRandomExerciseCategory;
 	
 	Integer sudsScore;
 	Integer resudsScore;
 	
 	private static final int STATE_SYMPTOM_SELECTION = 1;
 	private static final int STATE_SUDS = 2;
 	private static final int STATE_EXERCISE = 3;
 	private static final int STATE_RESUDS = 4;
 	private static final int STATE_COMPARE = 5;
 	
 	public static final int BUTTON_SKIP_SUDS = 100;
 	public static final int BUTTON_OK_SUDS = 101;
 	public static final int BUTTON_NEW_TOOL = 102;
 	public static final int BUTTON_RETRY_WITH_NEW_TOOL = 110;
 	public static final int BUTTON_DONE_EXERCISE = 103;
 	public static final int BUTTON_NEXT = 104;
 	public static final int BUTTON_DONE_RESUDS = 105;
 	public static final int BUTTON_DONE_ALL = 106;
 
 	public static final int RESULT_FAVORITE_SELECTION = 1000;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		state = STATE_SYMPTOM_SELECTION;
 		favorites = getCurrentContent().getChildByName("@left");
 		lastRandomExercise=-1;
 		lastRandomExerciseCategory=-1;
 	}
 
 	public void symptomOrFavoriteSelected() {
 		state = STATE_SUDS;
 		sudsScore = null;
 		Content c = db.getContentForName("sudsprompt");
 		ContentViewController cv = (ContentViewController)c.createContentView(this);
 		cv.addButton("Skip",BUTTON_SKIP_SUDS);
 		cv.addButton("Next",BUTTON_OK_SUDS);
 		pushView(cv,1);
 	}
 
 	public void exerciseDone() {
 		state = STATE_RESUDS;
 		resudsScore = null;
 		Content c = db.getContentForName("sudsreprompt");
 		ContentViewController cv = (ContentViewController)c.createContentView(this);
 		if (sudsScore == null) {
 			cv.addButton("Try Another Tool",BUTTON_RETRY_WITH_NEW_TOOL);
 			cv.addButton("Done",BUTTON_DONE_ALL);
 		} else {
 			cv.addButton("Next",BUTTON_DONE_RESUDS);
 		}
 		pushView(cv,1);
 	}
 
 	public void distressLevelDone() {
 		state = STATE_EXERCISE;
 		selectExerciseController(false);
 	}
 
 	public void recordSUDS() {
 		SUDSController cv = (SUDSController)getCurrentContentView();
 		sudsScore = cv.getScore();
 
 		TreeMap<String,String> map = new TreeMap<String, String>();
 		map.put("suds",""+sudsScore);
 		FlurryAgent.logEvent("SUDS_READING",map);
 		
 		PreExerciseSudsEvent e = new PreExerciseSudsEvent();
 		e.preExerciseSudsScore = sudsScore;
 		EventLog.log(e);
 	}
 
 	public void recordReSUDS() {
 		SUDSController cv = (SUDSController)getCurrentContentView();
 		resudsScore = cv.getScore();
 
 		TreeMap<String,String> map = new TreeMap<String, String>();
 		map.put("suds",""+sudsScore);
 		map.put("resuds",""+resudsScore);
 		FlurryAgent.logEvent("RESUDS_READING",map);
 
 		PostExerciseSudsEvent e = new PostExerciseSudsEvent();
		e.initialSudsScore = sudsScore != null ? sudsScore : -1;
		e.postExerciseSudsScore = resudsScore != null ? resudsScore : -1;
 		EventLog.log(e);
 	}
 
 	public void compareSUDS() {
 		if ((sudsScore != null) && (resudsScore != null)) {
 			state = STATE_COMPARE;
 			String contentName = "sudssame";
 			if (sudsScore < resudsScore) {
 				contentName = "sudsup";
 			} else if (sudsScore > resudsScore) {
 				contentName = "sudsdown";
 			}
 			Content c = db.getContentForName(contentName);
 			ContentViewController cv = (ContentViewController)c.createContentView(this);
 			cv.addButton("Try Another Tool",BUTTON_RETRY_WITH_NEW_TOOL);
 			cv.addButton("Done",BUTTON_DONE_ALL);
 			pushView(cv,1);
 		} else {
 			state = STATE_SYMPTOM_SELECTION;
 			popToRoot();
 		}
 	}
 
 	public void contentSelected(Content content) {
 		// The content object is the symptom we need to manage
 		symptom = content;
 		preselectedExercise = null;
 		lastRandomExercise=-1;
 		lastRandomExerciseCategory=-1;
 		symptomOrFavoriteSelected();
 	}
 	
 	@Override
 	public void popView() {
 		super.popView();
 		if (stack.size() == 1) state = STATE_SYMPTOM_SELECTION;
 	}
 
 	@Override
 	public void popToRoot() {
 		super.popToRoot();
 		state = STATE_SYMPTOM_SELECTION;
 	}
 
 	@Override
 	public void buttonTapped(int id) {
 		if (state == STATE_SYMPTOM_SELECTION) {
 			super.buttonTapped(id);
 			return;
 		} else if (id == BUTTON_SKIP_SUDS) {
 			distressLevelDone();
 		} else if (id == BUTTON_OK_SUDS) {
 			recordSUDS();
 			if ((sudsScore != null) && (sudsScore.intValue() >= 9)) {
 				state = STATE_EXERCISE;
 				pushViewForContent(ContentDBHelper.instance(this).getContentForName("crisis"));
 				return;
 			}
 			distressLevelDone();
 		} else if (id == BUTTON_NEW_TOOL) {
 			ToolAbortedEvent e = new ToolAbortedEvent();
 			e.toolId = getCurrentContent().uniqueID;
 			e.toolName = getCurrentContent().name;
 			EventLog.log(e);
 			selectExerciseController(true);
 		} else if (id == BUTTON_RETRY_WITH_NEW_TOOL) {
 			selectExerciseController(true);
 		} else if (id == BUTTON_DONE_EXERCISE) {
 			exerciseDone();
 		} else if (id == BUTTON_DONE_RESUDS) {
 			recordReSUDS();
 			compareSUDS();
 		} else if (id == BUTTON_DONE_ALL) {
 			popToRoot();
 		}
 	}
 	
 	static final private int MAX_TRIES = 10;
 	
 	public void selectExerciseController(boolean flipNotPush) {
 		Content exercise = null;
 		ContentViewControllerBase cv = null;
 
 		while (true) {
 			int randPos = -1;
 			exercise = null;
 			
 			if (preselectedExercise != null) {
 				exercise = preselectedExercise;
 			} else {
 				Cursor c = db.sql().rawQuery("select content._id,content.weight,content.parent from content left join symptom_map on content._id=symptom_map.referrer where symptom_map.referree=?", new String[] {""+symptom.id});
 
 				if (c.moveToFirst()) {
 					boolean otherCategoryAvailable = false;
 					float totalWeight = 0;
 
 					while (true) {
 						totalWeight += c.getFloat(1);
 						if (c.getLong(2) != lastRandomExerciseCategory) {
 							otherCategoryAvailable = true;
 						}
 						if (!c.moveToNext()) break;
 					}
 
 					int tryCount = 0;
 					do {
 						do {
 							float chosen = (float)(totalWeight * Math.random());
 
 							float iterWeight = 0;
 							c.moveToFirst();
 							while (true) {
 								float thisWeight = c.getFloat(1);
 								if (iterWeight+thisWeight >= chosen) break;
 								iterWeight += thisWeight;
 								if (!c.moveToNext()) break;
 							}
 
 							randPos = c.getPosition();
 							tryCount++;
 						} while ((randPos == lastRandomExercise) && (tryCount < MAX_TRIES));
 
 						exercise = db.getContentForID(c.getLong(0));
 					} while ((exercise.getParentID() == lastRandomExerciseCategory) && otherCategoryAvailable && (tryCount < MAX_TRIES));
 
 					c.close();
 				}
 			}
 
 			if (exercise == null) break;
 			
 			cv = exercise.createContentView(this);
 			cv.setSelectedContent(exercise);
 
 			String prereq = cv.checkPrerequisites();
 			if (prereq != null) {
 				if (preselectedExercise != null) {
 					AlertDialog.Builder builder = new AlertDialog.Builder(this);
 					builder.setTitle("Can't use this tool");
 					builder.setMessage(prereq);
 					builder.setPositiveButton("Ok", null);
 					builder.show();
 					state = STATE_SYMPTOM_SELECTION;
 					popToRoot();
 					return;
 				}
 				
 				continue;
 			}
 			
 			lastRandomExercise = randPos;
 			lastRandomExerciseCategory = exercise.getParentID();
 			break;
 		}
 	
 		if (exercise == null) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setTitle("Problem finding exercise");
 			builder.setMessage("Sorry, I couldn't find an exercise right now.");
 			builder.setPositiveButton("Ok", null);
 			builder.show();
 			state = STATE_SYMPTOM_SELECTION;
 			popToRoot();
 			return;
 		}
 		
 		Content parent = exercise.getParent();
     	if (parent != null) {
     		String parentUI = parent.getUIDescriptor();
     		if (parentUI != null) {
     			cv = parent.createContentView(this);
     			cv.setSelectedContent(exercise);
     		}
     	}
     	
     	if (flipNotPush) {
     		flipReplaceView(cv,2);
     	} else {
     		pushView(cv);
     	}
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == RESULT_FAVORITE_SELECTION) {
 			if ((data != null) && (data.getData() != null)) {
 				Content c = db.getContentForUri(data.getData());
 				preselectedExercise = c;
 				symptomOrFavoriteSelected();
 			}
 		} else {
 			super.onActivityResult(requestCode, resultCode, data);
 		}
 	}
 	
 	public void gotoFavorites() {
 		Intent intent = new Intent(this,FavoritesListActivity.class);
 		intent.setData(favorites.uriByUniqueID());
 		startActivityForResult(intent,RESULT_FAVORITE_SELECTION);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item == favoritesItem) {
 			gotoFavorites();
 /*
 			ContentViewControllerBase cvc = getCurrentContentView();
 			if (cvc != null) {
 				Content help = cvc.getContent().getHelp();
 				if (help != null) {
 					pushViewForContent(help);
 				}
 			}
 */			
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		favoritesItem = menu.add("Favorites");
 		Drawable d = Util.makeDrawable(this, "thumbsup_option_menu_48.png", true);
 		favoritesItem.setIcon(d);
 		return true;
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		return super.onPrepareOptionsMenu(menu);
 	}
 
 	@Override
 	protected void onPause()
 	{
 		super.onPause();
 		
 		PTSDCoach a = (PTSDCoach)getParent();
 		boolean theVal = a.fromBackground;
 		Log.v("ptsd","testvalue in managenavigationcontroller:" + theVal);
 		
 	}
 	
 }
