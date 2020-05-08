 package com.malcom.library.android;
 
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 
 import com.malcom.library.android.module.campaign.MCMCampaignAdapter;
 import com.malcom.library.android.module.campaign.MCMCampaignNotifiedDelegate;
 import com.malcom.library.android.module.core.MCMCoreAdapter;
 import com.malcom.library.android.module.notifications.EnvironmentType;
 import com.malcom.library.android.module.stats.MCMStats;
 
 import java.util.Hashtable;
 import java.util.List;
 
 /**
  * Malcom Android Library.
  *
  * NOTE
  *
  * 	Malcom Android Library needs the following permissions:
  *
  * 	<pre>
  * 		<uses-permission android:name="android.permission.INTERNET" />
  *		<uses-permission android:name="android.permission.READ_PHONE_STATE" />
  *		<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
  *		<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
  *		<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
  *		<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
  * 	</pre>
  *
  * Please, see documentation on https://github.com/MyMalcom/malcom-lib-android/ for more information.
  *
  * @author	Malcom Ventures, S.L.
  * @since	2012
  */
 public class MalcomLib {
 
     /**
      * Initializes Malcom with the app values
      * @param context
      * @param uuid      the app identifier (you can get it from your Malcom App configuration page)
      * @param secretKey the app secret key (you can get it from your Malcom App configuration page)
      */
     public static void initMalcom(Context context, String uuid, String secretKey){
         MCMCoreAdapter.getInstance().initMalcom(context,uuid,secretKey);
     }
 
     /**
      * Loads the Malcom configuration
      * @param activity The activity where the configuration will be loaded
      */
     public static void loadConfiguration(Activity activity){
         MCMCoreAdapter.getInstance().moduleConfigurationActivate(activity);
     }
 
     /**
      * Gets a advanced configuration value from Malcom
      * @param key Malcom value key
      * @param defaultValue value by default
      * @return
      */
     public static String getConfiguredProperty(String key, String defaultValue){
         String value;
         try {
             value = MCMCoreAdapter.getInstance().moduleConfigurationGetProperty(key);
         } catch (Exception e){
             value = defaultValue;
         }
 
         return value;
     }
 
     /**
      * Starts the tracking of a event
      * @param eventName the name of the event
      */
     public static void startEvent(String eventName){
        MalcomLib.startEvent(eventName, null, true);
     }
 
     /**
      * Starts the tracking of a event
      * @param eventName the name of the event
      * @param params parameters to have more information about the event
      * @param timeSession true if you want to track the event duration
      */
     public static void startEvent(String eventName, Hashtable<String,Object> params, boolean timeSession){
         MCMCoreAdapter.getInstance().moduleStatsStartBeaconWithName(eventName, timeSession, params);
     }
 
     /**
      * Stops the tracking of the event
      * @param eventName the name of the event
      */
     public static void endEvent(String eventName){
         MCMCoreAdapter.getInstance().moduleStatsEndBeaconWithName(eventName);
     }
 
     /**
      * Identifies the user
      * @param name			Name of the user app
      * @param mail 			mail of the user app
      */
     public static void identifyUser(String name, String mail){
         MCMCoreAdapter.getInstance().moduleStatsIdentifyUser(name, mail);
     }
 
     /**
      * Identifies the user
      * @param name			Name of the user app
      * @param mail 			mail of the user app
      * @param params		SubBeacon parameters with optional additional info
      */
     public static void identifyUser(String name, String mail, Hashtable<String,Object> params){
         MCMCoreAdapter.getInstance().moduleStatsIdentifyUser(name, mail, params);
     }
 
     /**
      * Identifies a revenue
      * @param name			Name of product
      * @param SKU 			Code of product
      * @param price			Price of the single product
      * @param currencyCode	International code for currency (EUR, USD)
      * @param amount 		Total purchase amount
      */
     public static void registerRevenue(String name, String SKU, float price, String currencyCode, int amount){
         MCMCoreAdapter.getInstance().moduleStatsRegisterRevenue(name, SKU, price, currencyCode, amount);
     }
 
     /**
      * Get the list of all tags from Malcom lib
      * @return
      */
     public static List<String> getTags(){
         return MCMStats.getTags();
     }
 
     /**
      * Adds a tag to Malcom lib
      * @param tagName
      */
     public static void addTag(String tagName){
         MCMStats.addTag(tagName);
     }
 
     /**
      * Removes a tag from Malcom lib
      * @param tagName
      */
     public static void removeTag(String tagName){
         MCMStats.removeTag(tagName);
     }
 
 
     /**
      * Sets the user meta data to Malcom lib
      * @param userMetadata
      */
     public static void setUserMetadata(String userMetadata){
         MCMCoreAdapter.getInstance().moduleStatsSetUserMetadata(userMetadata);
     }
 
     /**
      * Gets the user meta data from Malcom lib
      * @return
      */
     public static String getUserMetadata(){
         return MCMCoreAdapter.getInstance().moduleStatsGetUserMetadata();
     }
 
     /**
      * Sets the sender identifier from GCM
      * @param senderId
      */
     public static void setSenderId(String senderId) {
         MCMCoreAdapter.getInstance().setSenderId(senderId);
     }
 
     /**
      * Registers the device with GCM and Malcom push notification system
      * @param	context
      * @param	title		Title for the notification
      * @param 	clazz		Class to call when clicking in the notification
      */
     public static void notificationsRegister(Context context, String title, Class<?> clazz){
         MalcomLib.notificationsRegister(context, EnvironmentType.PRODUCTION, title, true, clazz);
     }
 
     /**
      * Registers the device with GCM and Malcom push notification system
      * @param	context
      * @param	environment Destination environment. See @EnvironmentType
      * @param	title		Title for the notification
      * @param   showAlert   Set false for no show alert whe the notification is opened
      * @param 	clazz		Class to call when clicking in the notification
      */
     public static void notificationsRegister(Context context, EnvironmentType environment, String title, Boolean showAlert, Class<?> clazz){
         MCMCoreAdapter.getInstance().moduleNotificationsRegister(context, environment, title, showAlert, clazz);
     }
 
     /**
      * Un-registers the device from Malcom notifications
      * @param context
      */
     public static void notificationsUnregister(Context context){
         MCMCoreAdapter.getInstance().moduleNotificationsUnregister(context);
     }
 
     /**
      * Check if there are notifications to be shown
      */
     public static void checkForNewNotifications(Activity activity) {
         MCMCoreAdapter.getInstance().moduleNotificationsCheckForNewNotifications(activity);
     }
 
     /**
      * Adds a cross-selling campaign to the specified activity
      * @param activity where the banner will be placed
      */
     public static void addCampaignCrossSelling(Activity activity){
         MalcomLib.addCampaignCrossSelling(activity, MCMCampaignAdapter.CAMPAIGN_DEFAULT_DURATION, null, null);
     }
 
     /**
      * Adds a cross-selling campaign to the specified activity
      * @param activity where the banner will be placed
      * @param duration indicating the time that is going to be shown the banner in seconds (0 for always visible).
      * @param delegate delegate for handling the performing of the banners
      * @param loadingImgResId the local image id that will be placed while the remote image is loading
      */
     public static void addCampaignCrossSelling(Activity activity, int duration, MCMCampaignNotifiedDelegate delegate, Integer loadingImgResId){
         MCMCoreAdapter.getInstance().moduleCampaignAddCrossSelling(activity, duration, delegate, loadingImgResId);
     }
 
     /**
      * Requests all the available cross selling campaigns for the app and calls the receiver
      * with the banner views to let the developer places them
      * @param activity the context where the request is made
      * @param receiver the interface that will be called with the retrieved data
      */
     public static void requestCampaignsCrossSelling(Activity activity,MCMCampaignAdapter.RequestCampaignReceiver receiver){
         MCMCoreAdapter.getInstance().moduleCampaignRequestCrossSelling(activity, receiver);
     }
 
     /**
      * Adds a promotion campaign to the specified activity.
      * @param activity where the banner will be placed
      */
     public static void addCampaignPromotion(Activity activity){
         MalcomLib.addCampaignPromotion(activity, MCMCampaignAdapter.CAMPAIGN_DEFAULT_DURATION, null, null);
     }
 
     /**
      * Adds a promotion campaign to the specified activity.
      * @param activity where the banner will be placed
      * @param duration indicating the time that is going to be shown the banner in seconds (0 for always visible).
      * @param delegate delegate for handling the performing of the banners
      * @param loadingImgResId the local image id that will be placed while the remote image is loading
      */
     public static void addCampaignPromotion(Activity activity, int duration, MCMCampaignNotifiedDelegate delegate, Integer loadingImgResId){
         MCMCoreAdapter.getInstance().moduleCampaignAddPromotion(activity, duration, delegate, loadingImgResId);
     }
 
     /**
      * Requests all the available promotion campaigns for the app and calls the receiver
      * with the banner views to let the developer places them
      * @param activity the context where the request is made
      * @param receiver the interface that will be called with the retrieved data
      */
     public static void requestCampaignsPromotion(Activity activity,MCMCampaignAdapter.RequestCampaignReceiver receiver){
         MCMCoreAdapter.getInstance().moduleCampaignRequestPromotion(activity, receiver);
     }
 
     /**
      * Adds a RateMyApp alert based on the server params
      * @param activity where the alert will be shown
      * @param delegate for handle the campaign behaviour
      */
     public static void addCampaignRateMyApp(Activity activity,MCMCampaignNotifiedDelegate delegate) {
         MCMCoreAdapter.getInstance().moduleCampaignAddRateMyApp(activity, delegate);
     }
 }
