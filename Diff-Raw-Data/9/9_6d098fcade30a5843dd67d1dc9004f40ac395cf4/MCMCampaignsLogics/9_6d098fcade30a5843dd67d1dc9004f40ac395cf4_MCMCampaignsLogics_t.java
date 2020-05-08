 package com.malcom.library.android.module.campaign;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.util.Log;
 
 import com.malcom.library.android.MCMDefines;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Random;
 
 /**
  * Created with IntelliJ IDEA.
  * User: PedroDuran
  * Date: 09/05/13
  * Time: 15:45
  * To change this template use File | Settings | File Templates.
  */
 public class MCMCampaignsLogics {
 
     /**
      * Method that filter the promotion campaign items from arrayList
      *
      * @param campaignsArray the arrayList with all the MCMCampaignDTO retrieved from server
      * @return ArrayList<MCMCampaignDTO> with promotion campaign items
      */
     public static ArrayList<MCMCampaignDTO> getFilteredCampaigns(ArrayList<MCMCampaignDTO> campaignsArray, MCMCampaignDTO.CampaignType type) {
 
         Iterator iterator = campaignsArray.iterator();
         ArrayList<MCMCampaignDTO> resultArray = new ArrayList<MCMCampaignDTO>();
 
         while (iterator.hasNext()) {
             MCMCampaignDTO currentCampaign = (MCMCampaignDTO) iterator.next();
             if (currentCampaign.getType() == type) {
                 resultArray.add(currentCampaign);
             }
         }
 
         return resultArray;
     }
 
     /**
      * Method that gets randomly weighted a campaign to serve.
      *
      * @return MCMCampaignDTO campaign selected.
      * @since 1.0.1
      */
     public static MCMCampaignDTO getCampaignPerWeight(ArrayList<MCMCampaignDTO> campaignsArray) {
         ArrayList<Integer> weightedArray = new ArrayList<Integer>();
 
         //generates the array to random weighted selection
         for (int i = 0; i < campaignsArray.size(); i++) {
             MCMCampaignDTO campaignModel = campaignsArray.get(i);
             //adds to the weighted array as ids as weight has
             for (int j = 0; j < campaignModel.getWeight(); j++) {
                 weightedArray.add(i);
             }
         }
 
         //generates random number
         int selection = 0;
         if (weightedArray.size() > 1) {
             selection = new Random().nextInt(weightedArray.size() - 1);
         }
 
         //gets the random position and gets the id written on it. It will be one of the campaigns
         MCMCampaignDTO selectedCampaignModel = campaignsArray.get(weightedArray.get(selection));
 
         return selectedCampaignModel;
 
     }
 
     private static final String ATTR_TIMES_BEFORE_REMINDING = "TIMES_BEFORE_REMINDING";
     private static final String ATTR_DAYS_UNTIL_PROMT = "DAYS_UNTIL_PROMT";
 
     private static final String RATE_MY_APP_PREFERENCES = "RateMyAppPreferences";
     private static final String NOT_SHOW_AGAIN = "DontShowAgain";
     private static final String SESSIONS_SINCE_LAST_DIALOG = "SessionsSinceLastDialog";
     private static final String DATE_LAST_DIALOG_ms = "DateLastDialog";                    //In miliseconds
 
     public static boolean shouldShowDialog(Context context, MCMCampaignDTO campaignDTO) {
         //By default should show the dialog
         boolean shouldShowDialog = true;
 
         //Check if the campaign was stored on SharedPreferences
         SharedPreferences preferences = getRateMyAppSharedPreferences(context);
 
         //If the campaign was already there, check the parameters
         //Otherwise should show the dialog
         if (preferences.getBoolean(campaignDTO.getCampaignId(),false)) {
 
             //Promotion limits
             int sessionLimit = Integer.parseInt(campaignDTO.getClientLimitFeature(ATTR_TIMES_BEFORE_REMINDING));
             int daysLimit = Integer.parseInt(campaignDTO.getClientLimitFeature(ATTR_DAYS_UNTIL_PROMT));
 
             //Check the client limits and "notshowagain"
            int sessionsSinceLastDialog = preferences.getInt(SESSIONS_SINCE_LAST_DIALOG,0);
             int daysSinceLastDialog = getDaysFromDateInMilliseconds(preferences.getLong(DATE_LAST_DIALOG_ms, System.currentTimeMillis()));
 
             boolean notShowAgain = preferences.getBoolean(NOT_SHOW_AGAIN,false);
             boolean notShouldShowDialog = (sessionsSinceLastDialog < sessionLimit) && (daysSinceLastDialog < daysLimit);
 
             if (notShowAgain || notShouldShowDialog) {
                 shouldShowDialog = false;
             }
 
         } else {
             clearRateMyAppControlParameters(context);
         }
 
         return shouldShowDialog;
     }
 
     public static void updateRateDialogSession(Context context, MCMCampaignDTO campaignDTO) {
         SharedPreferences prefs = getRateMyAppSharedPreferences(context);
         SharedPreferences.Editor editor = getRateMyAppPreferencesEditor(context);
 
         //Update the campaignId on preferences
         if (!prefs.getBoolean(campaignDTO.getCampaignId(),false)) {
             editor.putBoolean(campaignDTO.getCampaignId(),true);
         }
 
         //Update the sessions number
        int formerSessions = prefs.getInt(SESSIONS_SINCE_LAST_DIALOG,-1); //To get 0 sessions the first time
         editor.putInt(SESSIONS_SINCE_LAST_DIALOG,formerSessions+1);
 
         editor.commit();
     }
 
     public static void updateRateDialogDate(Context context) {
         updateRateDialog(context, true);
     }
 
     public static void updateRateDialogDontShowAgain(Context context) {
         updateRateDialog(context,false);
     }
 
     private static void updateRateDialog(Context context, boolean showAgain) {
         SharedPreferences.Editor editor = getRateMyAppPreferencesEditor(context);
 
         //If it is necessary show the dialog again, update the control parameters
         if (showAgain) {
             editor.putLong(DATE_LAST_DIALOG_ms, System.currentTimeMillis());
             //Reset the session number
             editor.putInt(SESSIONS_SINCE_LAST_DIALOG,1);
 
         } else { //Otherwise set the NOT_SHOW_AGAIN parameter to true
             editor.putBoolean(NOT_SHOW_AGAIN,true);
         }
 
         editor.commit();
     }
 
     private static void clearRateMyAppControlParameters(Context context) {
         SharedPreferences.Editor editor = getRateMyAppPreferencesEditor(context);
 
         editor.clear();
         editor.commit();
     }
 
     private static SharedPreferences getRateMyAppSharedPreferences(Context context) {
         return context.getSharedPreferences(RATE_MY_APP_PREFERENCES, 0);
     }
 
     private static SharedPreferences.Editor getRateMyAppPreferencesEditor(Context context) {
         return getRateMyAppSharedPreferences(context).edit();
     }
 
     private static int getDaysFromDateInMilliseconds(long millisecondsDate) {
 
         long currentDate = System.currentTimeMillis();
 
         int days = (int) ((millisecondsDate - currentDate) / (24 * 60 * 60 * 1000));
 
         return days;
     }
 }
