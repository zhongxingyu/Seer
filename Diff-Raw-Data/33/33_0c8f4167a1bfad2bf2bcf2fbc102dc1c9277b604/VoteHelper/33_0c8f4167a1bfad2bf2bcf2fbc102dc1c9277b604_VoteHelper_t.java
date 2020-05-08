 /**
  * 
  */
 package de.questmaster.tudmensa;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Calendar;
 
 import android.os.Bundle;
 
 /**
  * @author Daniel
  *
  */
 public class VoteHelper extends Thread {
 
 	private final static int MODE_GET_VOTES = 0;
 	private final static int MODE_SET_VOTES = 1;
	private static final String mURLlocation = "http://www.questmaster.de/tumensa/";
 	
 	private Bundle mDialogData = null;
 	private MealsDbAdapter mDbHelper;
 	private int mModeOfOperation = MODE_GET_VOTES;
 	private MensaMeals mParent = null;
 	private Calendar mToday;
 	
 	/**
 	 * @param bundle
 	 */
 	public VoteHelper(MensaMeals parent, MealsDbAdapter dbHelper, Calendar curDate) {
 		mDbHelper = dbHelper;
 		mParent = parent;
 		mToday = curDate;
 		
 		mModeOfOperation = MODE_GET_VOTES;
 	}
 
 	/**
 	 * @param bundle
 	 */
 	public VoteHelper(Bundle bundle) {
 		mDialogData = bundle;
 		mModeOfOperation = MODE_SET_VOTES;
 	}
 
 	@Override
 	public void run() {
 		switch (mModeOfOperation) {
 		case MODE_GET_VOTES:
			// FIXME: get dates in DB from today on
 			
			// FIXME: read votes data
 			
			// FIXME: store votes in DB
 			
 			// DEBUG test
//			long id = mDbHelper.fetchMealId("stadtmitte", "20110209", "Bistro", 0);
 //			mDbHelper.updateMealExternalVotes(id, (float) 3.5, (float) 4.0, (float) 1.5, 1, 1, 1);
 			
 			// update list
 			mParent.mHandler.sendEmptyMessage(1);
 			break;
 		case MODE_SET_VOTES:
 			try {
 				String visual = String.valueOf(mDialogData.getBoolean(MensaMeals.VOTE_DIALOG_VISUAL_CHANGE_ID) ? mDialogData.getFloat(MensaMeals.VOTE_DIALOG_VISUAL_ID) : 0);
 				String price = String.valueOf(mDialogData.getBoolean(MensaMeals.VOTE_DIALOG_PRICE_CHANGE_ID) ? mDialogData.getFloat(MensaMeals.VOTE_DIALOG_PRICE_ID) : 0);
 				String taste = String.valueOf(mDialogData.getBoolean(MensaMeals.VOTE_DIALOG_TASTE_CHANGE_ID) ? mDialogData.getFloat(MensaMeals.VOTE_DIALOG_TASTE_ID) : 0);
 				
				URL voteURL = new URL(String.format(mURLlocation + "mealcounter.php?mealid=%s&date=%s&vote1=%s&vote2=%s&vote3=%s",
 						mDialogData.getString(MensaMeals.VOTE_DIALOG_MEAL_SCRIPT_ID), 
 						mDialogData.getString(MensaMeals.VOTE_DIALOG_DATE_ID), 
 						visual, price, taste));
 				
 				URLConnection uc = voteURL.openConnection();
 
 				uc.connect();
 				uc.getContent();
 				
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}			
 		}
 	}
 
 }
