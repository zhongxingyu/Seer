 package com.malcom.library.android.module.core;
 
 import java.util.Hashtable;
 import java.util.Properties;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.util.Log;
 import android.widget.LinearLayout;
 
 import com.malcom.library.android.MCMDefines;
 import com.malcom.library.android.exceptions.ConfigModuleNotInitializedException;
 import com.malcom.library.android.exceptions.CoreNotInitializedException;
 import com.malcom.library.android.module.ad.MCMAdAdapter;
 import com.malcom.library.android.module.ad.MCMAdEventHandler;
 import com.malcom.library.android.module.campaign.MCMCampaignAdapter;
 import com.malcom.library.android.module.campaign.MCMCampaignDTO;
 import com.malcom.library.android.module.campaign.MCMCampaignNotifiedDelegate;
 import com.malcom.library.android.module.config.MCMConfigManager;
 import com.malcom.library.android.module.notifications.EnvironmentType;
 import com.malcom.library.android.module.notifications.MCMNotificationModule;
 import com.malcom.library.android.module.stats.MCMStats;
 import com.malcom.library.android.module.stats.Subbeacon.SubbeaconType;
 
 /**
  * Malcom Android Library Core Module.
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
 public class MCMCoreAdapter {
 	
 	public static final String SDK_VERSION = "1.0.5";
 	public static final String MALCOM_LIBRARY_PREFERENCES_FILE_NAME = "com.malcom.library.android";
 	
 	public static final String PROPERTIES_MALCOM_BASEURL = "MalcomBaseURL";
 	public static final String PROPERTIES_MALCOM_ADWHIRL_URL = "AdWhirlBaseUrl";
 	public static final String PROPERTIES_MALCOM_APPID = "MalcomAppId";
 	public static final String PROPERTIES_MALCOM_APPSECRETKEY = "MalcomAppSecretKey";
 	public static final String PROPERTIES_MALCOM_ADWHIRLID = "adWhirlId";
 	public static final String PROPERTIES_MALCOM_GCM_SENDERID = "gcmSenderId";
 	public static final String PROPERTIES_MALCOM_APP_NAME = "adAppName";
 	public static final String PROPERTIES_MALCOM_COMPANY_NAME = "adCompanyName";
 	
 	private static final String MALCOM_CONFIG = "MCM_MALCOM_CONFIG";
 	private Context context = null;
 	
 	public static final int CONNECTION_DEFAULT_TIMEOUT = 3000; // 3 sgs.
 	public static final int CONNECTION_DEFAULT_DATA_RECEIVAL_TIMEOUT = 2000; // 2 sgs.
 	
 	public static String SERVER_URL = null;
 	
 	private static MCMCoreAdapter instance = null;
 
 	private static Properties properties;
 	
 	private static boolean coreInitialized = false;
 	public static String applicationPackage = null;
 	
 	//	Ads module: size banner variables
 	
 	public int adWidth = 320;
 	public int adHeight = 52;
 	
 	protected MCMCoreAdapter() {
 	      // Exists only to defeat instantiation.
 	}
 	
 	public static MCMCoreAdapter getInstance() {
 	
 		if(instance == null) {
 			instance = new MCMCoreAdapter();	    
 		}
 		
 		return instance;	   
 	}
 	
 	/**
 	 * 
 	 * @param key
 	 * @return
 	 */
 	public String coreGetProperty(String key) {
 		
 		String result = "";
 		
 		if (coreInitialized) {
 		
 			SharedPreferences prefs = this.context.getSharedPreferences( MALCOM_CONFIG, 0);	
 			result = prefs.getString(key, "");
 		
 		}
 		
 		if (result.equals("")) {
 			Log.e(MCMDefines.LOG_TAG, "You should call first initMalcom with the appId and secretKey");
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * Required. Initialize the Malcom Android LIbrary.
 	 * 
 	 * @param uuid
 	 * @param secretKey
 	 */
 	public void initMalcom(Context context, String uuid, String secretKey) {
 
         Log.d(MCMDefines.LOG_TAG, "initMalcom uuid: "+uuid+" secretKey: "+secretKey);
 		
 		this.context = context;
 		SharedPreferences prefs = context.getSharedPreferences(MALCOM_CONFIG, 0);
 		
 		prefs.edit().putString(PROPERTIES_MALCOM_APPID, uuid).commit();
 		prefs.edit().putString(PROPERTIES_MALCOM_APPSECRETKEY, secretKey).commit();
 		prefs.edit().putString(PROPERTIES_MALCOM_BASEURL, MCMDefines.MALCOM_BASEURL).commit();
 		prefs.edit().putString(PROPERTIES_MALCOM_ADWHIRL_URL, MCMDefines.MALCOM_ADWHIRL_URL).commit();
 		
 		MCMCoreAdapter.applicationPackage = context.getPackageName();
 		
 		coreInitialized = true;
 		
 	}
 	
 	/**
 	 * Gets the android device unique id.
 	 * 
 	 * @param context
 	 * @return
 	 */
 	public String coreGetDeviceId(Context context){
 		return MCMNotificationModule.getInstance().gcmGetDeviceUdid(context);	
 	}
 	
 
 	// MODULES :
 	
 	
 	
 	// --- CONFIGURATION	
 	
 	/**
 	 * This method calls to the configuration module of the library loading the data and executing the config
 	 * if is needed by the "execute" param.
 	 * 
 	 * @param activity		Activity.
 	 * @throws CoreNotInitializedException
 	 */
 	public void moduleConfigurationActivate(Activity activity) throws CoreNotInitializedException{
 				
 		if (coreInitialized) {
 
 			MCMConfigManager.getInstance().createConfig(activity, applicationPackage);
 			
 		}
 		else {
 			
 			Log.e("CORE", "Core has not been initialized, use initMalcom method.");
 			
 		}
 		
 	}
 
     /**
      * This method resturn the value of the specified configuration property. When using this method the configuration
      * module should already loaded by using "useConfigModule()" method.
      *
      * @param key	Property
      * @return      The property value or null if the specified property does not exist or if the property is null
      * @throws      CoreNotInitializedException
      * @throws      ConfigModuleNotInitializedException
      * @deprecated  Use moduleConfigurationGerProperty(String key, ConfigListener listener) instead.
      */
     public String moduleConfigurationGetProperty(String key) throws CoreNotInitializedException, ConfigModuleNotInitializedException{
         if(coreInitialized)
             if(MCMConfigManager.getInstance().isConfigurationLoaded()){
                 return MCMConfigManager.getInstance().getKeyValue(key);
             }else{
                 //TODO: Pedro - Cargamos el fichero de configuración y cuando termina llamamos al callback con el
 
                 if(MCMConfigManager.getInstance().isConfigurationLoading())
                     throw new ConfigModuleNotInitializedException("Config module is being initialized, wait until is fully initialized before call this method.");
                 else
                     throw new ConfigModuleNotInitializedException("Config module has not been initialized, use useConfigModule().");
             }
         else
             throw new CoreNotInitializedException("Core has not been initialized, use initMalcom method.");
     }
 	
 	/**
 	 * This method resturn the value of the specified configuration property. When using this method the configuration
 	 * module should already loaded by using "useConfigModule()" method.
 	 * 
 	 * @param key	Property
 	 * @return      The property value or null if the specified property does not exist or if the property is null
 	 * @throws      CoreNotInitializedException
 	 * @throws      ConfigModuleNotInitializedException
 	 */
 	public void moduleConfigurationGetProperty(String key, ConfigListener listener) throws CoreNotInitializedException, ConfigModuleNotInitializedException{
 		if(coreInitialized)
             MCMConfigManager.getInstance().getProperty(key,listener);
 		else
 			throw new CoreNotInitializedException("Core has not been initialized, use initMalcom method.");
 	}
 	
 	/**
 	 * This method resturn the value of the specified configuration property. When using this method the configuration
 	 * module should already loaded by using "useConfigModule()" method.
 	 * 
 	 * @param key	Property
 	 * @return		The property value or null if the specified property does not exist or if the property is null
 	 * @throws      ConfigModuleNotInitializedException
      * @deprecated  Use moduleConfigurationGerProperty(String key, ConfigListener listener) instead.
 	 */
 	public String moduleConfigurationGetProperty(Activity context, String key) throws ConfigModuleNotInitializedException{
 		
 		return moduleConfigurationGetProperty(key);
 		
 	}
 	
 	/**
 	 * Tells if the configuration module is correctly initialized.
 	 * 
 	 * @return
 	 * @throws CoreNotInitializedException
 	 */
 	public boolean moduleConfigurationIsModuleInitialized() throws CoreNotInitializedException{		
 		return MCMConfigManager.getInstance().isConfigurationLoaded();
 	}
 	
 	public String SDKVersion() {
 		
 		return SDK_VERSION;
 		
 	}
 
     public interface ConfigListener {
         public void onReceivedParameter(String parameter, String value);
     }
 	
 	
 	// --- BEACONS & SUB-BEACONS
 	 
 	/**
 	 * Starts the beacon processing.
 	 * 
 	 * @param context
 	 * @param useLocation	Set to TRUE to also store location data in the beacon.
 	 */
 	public void moduleStatsStartBeacon(Context context, boolean useLocation){
 		
 		MCMStats.initAndStartBeacon(context, properties, useLocation);
 
 	}
 	
 	/**
 	 * Starts a sub-beacon(event) processing with the given name.
 	 * 
 	 * @param beaconName	Name of the sub-beacon (event)
 	 */
 	public void moduleStatsStartBeaconWithName(String beaconName){
 
 		moduleStatsStartBeaconWithName(beaconName, true);
 
 	}
 	
 	/**
 	 * Starts a sub-beacon(event) processing with the given name.
 	 * 
 	 * @param beaconName	Name of the sub-beacon (event)
 	 * @param trackSession
 	 */
 	public void moduleStatsStartBeaconWithName(String beaconName, boolean trackSession){
 		if(coreInitialized){
 			try{
 				MCMStats.getSharedInstance().startSubBeaconWithName(beaconName, SubbeaconType.CUSTOM, new Hashtable<String, Object>(), trackSession);
 			}catch(MCMStats.BeaconException e){
 				Log.e("CORE-STATS", "Error initializing sub-beacon(event) with name '"+beaconName+"' ("+e.getMessage()+")");
 			}
 		}else{
 			throw new CoreNotInitializedException("Stats has not been initialized, use moduleStatsStartBeacon().");
 		}
 	}
 
     /**
      * Starts a sub-beacon(event) processing with the given name and params.
      *
      * @param beaconName	Name of the sub-beacon (event)
      * @param params		SubBeacon parameters
      */
     public void moduleStatsStartBeaconWithName(String beaconName, Hashtable<String, Object> params){
         if(coreInitialized){
             try{
                 MCMStats.getSharedInstance().startSubBeaconWithName(beaconName, SubbeaconType.CUSTOM, params, false);
             }catch(MCMStats.BeaconException e){
                 Log.e("CORE-STATS", "Error initializing sub-beacon(event) with name '"+beaconName+"' ("+e.getMessage()+")");
             }
         }else{
             throw new CoreNotInitializedException("Stats has not been initialized, use moduleStatsStartBeacon().");
         }
     }
 	
 	/**
 	 * Starts a sub-beacon(event) processing with the given name and params.
 	 * 
 	 * @param beaconName	Name of the sub-beacon (event)
 	 * @param params		SubBeacon parameters
 	 * @param trackSession
 	 */
 	public void moduleStatsStartBeaconWithName(String beaconName, boolean trackSession, Hashtable<String, Object> params){
 		if(coreInitialized){
 			try{
 				MCMStats.getSharedInstance().startSubBeaconWithName(beaconName, SubbeaconType.CUSTOM, params, trackSession);
 			}catch(MCMStats.BeaconException e){
 				Log.e("CORE-STATS", "Error initializing sub-beacon(event) with name '"+beaconName+"' ("+e.getMessage()+")");
 			}
 		}else{
 			throw new CoreNotInitializedException("Stats has not been initialized, use moduleStatsStartBeacon().");
 		}
 	}
 	
 	
 	/**
 	 * Starts a sub-beacon(event) to identify the user app.
 	 * 
 	 * @param name			Name of the user app
 	 * @param mail 			mail of the user app
 	 */
 	public void moduleStatsIdentifyUser(String name, String mail){
 		moduleStatsIdentifyUser(name, mail,  new Hashtable<String, Object>());
 	}
 	
 	
 	/**
 	 * Starts a sub-beacon(event) to identify the user app.
 	 * 
 	 * @param name			Name of the user app
 	 * @param mail 			mail of the user app
 	 * @param params		SubBeacon parameters with optional additional info 
 	 */
 	public void moduleStatsIdentifyUser(String name, String mail, Hashtable<String, Object> params){
 		if(coreInitialized){
 			try{
 			    
 				//generates the hashtable for user info
 			    Hashtable<String, Object> userHashtable = new Hashtable<String, Object>();
 			    userHashtable.put("name", name);
 			    userHashtable.put("mail", mail);
 			    userHashtable.putAll(params);
 			    
 			    MCMStats.getSharedInstance().startSubBeaconWithName("app_user", SubbeaconType.SPECIAL, userHashtable, false);
 			    
 			}catch(MCMStats.BeaconException e){
 				Log.e("CORE-STATS", "Error initializing sub-beacon(event) with name '"+"app_user"+"' ("+e.getMessage()+")");
 			}catch (Exception e) {
 				e.printStackTrace();
 			}
 		}else{
 			throw new CoreNotInitializedException("Stats has not been initialized, use moduleStatsStartBeacon().");
 		}
 	}
 
 	
 	/**
 	 * Starts a sub-beacon(event) to identify a revenue.
 	 * 
 	 * @param name			Name of product
 	 * @param SKU 			Code of product
 	 * @param price			Price of the single product
 	 * @param currencyCode	International code for currency (EUR, USD)
 	 * @param amount 		Total purchase amount
 	 */
 	public void moduleStatsRegisterRevenue(String name, String SKU, float price, String currencyCode, int amount){
 		if(coreInitialized){
 			try{
 			    
 			    Hashtable<String, Object> revenueHashtable = new Hashtable<String, Object>();
 			    revenueHashtable.put("name", name);
 			    revenueHashtable.put("SKU", SKU);
 			    revenueHashtable.put("price", String.valueOf(price));
 			    revenueHashtable.put("currencyCode", currencyCode);
 			    revenueHashtable.put("amount", String.valueOf(amount));
 			    
 			    MCMStats.getSharedInstance().startSubBeaconWithName("revenue", SubbeaconType.SPECIAL, revenueHashtable, false);
 			    
 			}catch(MCMStats.BeaconException e){
 				Log.e("CORE-STATS", "Error initializing sub-beacon(event) with name '"+"app_user"+"' ("+e.getMessage()+")");
 			}catch (Exception e) {
 				e.printStackTrace();
 			}
 		}else{
 			throw new CoreNotInitializedException("Stats has not been initialized, use moduleStatsStartBeacon().");
 		}
 	}
 	
 	
 	/**
 	 * Stop the beacon processing and send the resulting beacon data to Malcom.
 	 */
 	public void moduleStatsEndBeacon(){
 		
 		try {
 			MCMStats.getSharedInstance().stopBeacon();
 		}
 		catch(MCMStats.BeaconException e){
 			Log.e("CORE-STATS", "Error stopping beacons ("+e.getMessage()+")");
 		}
 		
 	}
 
 	/**
 	 * Sub-beacons(events) are automatically stopped when general beacon stats are stopped but, optionally,
 	 * a sub-beacon(event) can be stopped with this method.
 	 */
 	public void moduleStatsEndBeaconWithName(String beaconName){
 		try{
 				MCMStats.getSharedInstance().endSubBeaconWithName(beaconName);
 			}catch(MCMStats.BeaconException e){
 				Log.e("CORE-STATS", "Error stopping sub-beacon(event) with name '"+beaconName+"': ("+e.getMessage()+")");
 			}
 		
 	}
 	
 	public void moduleStatsAddTag(String tag) {
 		
 		try {
 			MCMStats.getSharedInstance().addTag(tag);
 		} catch (MCMStats.BeaconException e) {
 			// TODO Auto-generated catch block
 			Log.e("CORE-STATS", "Error add tag': ("+e.getMessage()+")");
 		}
 		
 	}
 	
 	public void moduleStatsRemoveTag(String tag) {
 		
 		try {
 			MCMStats.getSharedInstance().removeTag(tag);
 		} catch (MCMStats.BeaconException e) {
 			// TODO Auto-generated catch block
 			Log.e("CORE-STATS", "Error remove tag': ("+e.getMessage()+")");
 		}
 		
 	}
 	
 	public void moduleStatsSetUserMetadata(String userMetadata) {
 		
 		try {
 			MCMStats.getSharedInstance().setUserMetadata(userMetadata);
 		} catch (MCMStats.BeaconException e) {
 			// TODO Auto-generated catch block
 			Log.e("CORE-STATS", "Error set user metadata': ("+e.getMessage()+")");
 		}
 		
 	}
 	
 	public String moduleStatsGetUserMetadata() {
 		
 		String userMetadata = null;
 		
 		try {
 			userMetadata = MCMStats.getSharedInstance().getUserMetadata();
 		} catch (MCMStats.BeaconException e) {
 			// TODO Auto-generated catch block
 			Log.e("CORE-STATS", "Error get user metadata': ("+e.getMessage()+")");
 		}
 		
 		return userMetadata;
 		
 	}
 	
 	
 	// --- ADS
 
     /**
      *
      * @param context
      * @param layoutAd
      * @param eventHandler
      * @deprecated use Malcom's campaigns module instead.
      */
 	
 	public void moduleAdsActivate(Activity context, LinearLayout layoutAd, MCMAdEventHandler eventHandler){
 		
 		MCMAdAdapter.getInstance().createAds(context, layoutAd, (String)properties.get(PROPERTIES_MALCOM_ADWHIRLID), eventHandler, adWidth, adHeight);
 		
 	}
 	
 	/**
 	 * 
 	 * @param context
 	 * @param layoutAd
      * @deprecated use Malcom's campaigns module instead.
 	 */
 	public void moduleAdsActivate(Activity context, String adsID, LinearLayout layoutAd){
 		
 		MCMAdAdapter.getInstance().createAds(context, layoutAd, adsID, adWidth, adHeight);
 		
 	}
 
 	
 	// --- NOTIFICATIONS
 	
 	public void setSenderId(String senderId) {
 		
 		SharedPreferences prefs = context.getSharedPreferences( MALCOM_CONFIG, 0);
 		
 		prefs.edit().putString(PROPERTIES_MALCOM_GCM_SENDERID, senderId).commit();
 		
 	}
 	
 	/**
 	 * Registers the device with GCM and Malcom push notification system.
 	 * 
 	 * NOTE: 
 	 * The environment is set by looking for the application debug mode,
 	 * if is set to TRUE, the environment will be SANDBOX, otherwise PRODUCTION.
 	 * 
 	 * @param	context
 	 * @param	title		Title for the notification
 	 * @param 	clazz		Class to call when clicking in the notification
 	 */
 	public void moduleNotificationsRegister(Context context, String title, Class<?> clazz){
 		
 		MCMNotificationModule.getInstance().gcmRegisterDevice(context.getApplicationContext(), title, true, clazz);
 		
 	}
 
     /**
      * Registers the device with GCM and Malcom push notification system.
      *
      * @param	context
      * @param	environment Destination environment. See @ENvironmentType.
      * @param	title		Title for the notification
      * @param 	clazz		Class to call when clicking in the notification
      */
     public void moduleNotificationsRegister(Context context, EnvironmentType environment, String title, Class<?> clazz){
 
         MCMNotificationModule.getInstance().gcmRegisterDevice(context, environment, title, true, clazz);
 
     }
 
 	
 	/**
 	 * Registers the device with GCM and Malcom push notification system.
 	 * 
 	 * NOTE: 
 	 * The environment is set by looking for the application debug mode,
 	 * if is set to TRUE, the environment will be SANDBOX, otherwise PRODUCTION.
 	 * 
 	 * @param	context
 	 * @param	title		Title for the notification
 	 * @param 	clazz		Class to call when clicking in the notification
 	 */
 	public void moduleNotificationsRegister(Context context, String title, Boolean showAlert, Class<?> clazz){
 		
 		MCMNotificationModule.getInstance().gcmRegisterDevice(context.getApplicationContext(), title, showAlert, clazz);
 		
 	}
 	
 	/**
 	 * Registers the device with GCM and Malcom push notification system.
 	 * 
 	 * @param	context
 	 * @param	environment Destination environment. See @ENvironmentType. 
 	 * @param	title		Title for the notification
 	 * @param 	clazz		Class to call when clicking in the notification
 	 */
 	public void moduleNotificationsRegister(Context context, EnvironmentType environment, String title, Boolean showAlert, Class<?> clazz){
 		
 		MCMNotificationModule.getInstance().gcmRegisterDevice(context.getApplicationContext(), environment, title, showAlert, clazz);
 		
 	}
 
 	
 	/**
 	 * Un-registers the device from GCM and from Malcom.
 	 * 
 	 * @param context
 	 */
 	public void moduleNotificationsUnregister(Context context){
 		
 		MCMNotificationModule.getInstance().gcmUnregisterDevice(context.getApplicationContext());
 				
 	}
 	
 	/**
 	 * Check if there are notifications to be shown.
 	 */
 	public void moduleNotificationsCheckForNewNotifications(Activity activity) {
 
 		MCMNotificationModule.getInstance().gcmCheckForNewNotification(activity);
 	}
 	
 	/**
 	 * Gets the notification registration token or null if the device is not registered.
 	 * 
 	 * @param context
 	 * @return
 	 */
 	public String moduleNotificationsGetRegistrationToken(Context context){
 		
 		return MCMNotificationModule.getInstance().gcmGetRegistrationToken(context);
 		
 	}
 	
 	//	Campaings
 	
 	/**
 	 * Method that adds the campaigns to the specified activity.  By default, campaigns will last 15 seconds.
 	 * @param activity
     * @deprecated use {@link #moduleCampaignAddCrossSelling(Activity)} instead.
 	 */
 	public void moduleCampaignAddBanner(Activity activity) {
 		
 		moduleCampaignAddBanner(activity, null);
 	}
 	
 	/**
 	 * Method that adds the campaigns to the specified activity. With this method you can use the delegates for handling the performing of the banners.  By default, campaigns will last 15 seconds.
 	 *
      * @deprecated use {@link #moduleCampaignAddCrossSelling(Activity)} instead.
 	 */
 	public void moduleCampaignAddBanner(Activity activity,MCMCampaignNotifiedDelegate delegate) {
 
         moduleCampaignAddCrossSelling(activity, delegate, null);
 		
 	}
 	/**
 	 * Method that sets the duration of the Banner
 	 * @param duration integer indicating the time that is going to be shown the banner in seconds. If the banner is desired to be always on screen you'll need to set to zero the duration.
     * @deprecated use {@link #moduleCampaignAddCrossSelling(Activity)} instead.
 	 */
 	public void moduleCampaignSetBannerDuration(int duration) {
 		
 		MCMCampaignAdapter.getInstance().setDuration(duration);
 	}
 	
 	/**
 	 * Method that removes the current banner shown in activity.
 	 * @param activity
 	 */
 	public void moduleCampaignRemoveCurrentBanner(Activity activity) {
 		
 		MCMCampaignAdapter.getInstance().removeCurrentBanner(activity);
 	}
 
     //	Multitype Campaings
 
     /**
      * Method that adds the cross selling campaign to the specified activity.  By default, campaigns will last 15 seconds.
      * @param activity where the banner will be placed
      */
     public void moduleCampaignAddCrossSelling(Activity activity) {
 
         moduleCampaignAddCrossSelling(activity, null, null);
     }
 
     /**
      * Method that adds the cross selling campaign to the specified activity.  By default, campaigns will last 15 seconds.
      * @param activity where the banner will be placed
      * @param delegate delegate for handling the performing of the banners
      */
     public void moduleCampaignAddCrossSelling(Activity activity,MCMCampaignNotifiedDelegate delegate) {
         moduleCampaignAddCrossSelling(activity, MCMCampaignAdapter.CAMPAIGN_DEFAULT_DURATION,delegate, null);
     }
     public void moduleCampaignAddCrossSelling(Activity activity,MCMCampaignNotifiedDelegate delegate, Integer loadingImgResId) {
         moduleCampaignAddCrossSelling(activity, MCMCampaignAdapter.CAMPAIGN_DEFAULT_DURATION,delegate, loadingImgResId);
     }
 
     /**
      * Method that adds the cross selling campaign to the specified activity.
      * @param activity where the banner will be placed
      * @param duration indicating the time that is going to be shown the banner in seconds (0 for always visible).
      * @param delegate delegate for handling the performing of the banners
      */
     public void moduleCampaignAddCrossSelling(Activity activity,int duration,MCMCampaignNotifiedDelegate delegate) {
         MCMCampaignAdapter.getInstance().addBanner(activity, MCMCampaignDTO.CampaignType.IN_APP_CROSS_SELLING, duration, delegate, null);
     }
     public void moduleCampaignAddCrossSelling(Activity activity,int duration,MCMCampaignNotifiedDelegate delegate, Integer loadingImgResId) {
         MCMCampaignAdapter.getInstance().addBanner(activity, MCMCampaignDTO.CampaignType.IN_APP_CROSS_SELLING, duration, delegate, loadingImgResId);
     }
 
     /**
      * Method that requests all the available cross selling campaigns for the app and calls the receiver
      * with the banner views to let the developer places them
      * @param activity the context where the request is made
      * @param receiver the interface that will be called with the retrieved data
      */
     public void moduleCampaignRequestCrossSelling(Activity activity,MCMCampaignAdapter.RequestCampaignReceiver receiver) {
         MCMCampaignAdapter.getInstance().requestBanner(activity, MCMCampaignDTO.CampaignType.IN_APP_CROSS_SELLING, receiver);
     }
 
     /**
      * Method that adds the promotions campaign to the specified activity.  By default, campaigns will last 15 seconds.
      * @param activity where the banner will be placed
      */
     public void moduleCampaignAddPromotion(Activity activity) {
 
         moduleCampaignAddPromotion(activity, MCMCampaignAdapter.CAMPAIGN_DEFAULT_DURATION, null, null);
     }
 
     /**
      * Method that adds the promotions campaign to the specified activity.  By default, campaigns will last 15 seconds.
      * @param activity where the banner will be placed
      * @param delegate delegate for handling the performing of the banners
      */
     public void moduleCampaignAddPromotion(Activity activity,MCMCampaignNotifiedDelegate delegate) {
         moduleCampaignAddPromotion(activity, MCMCampaignAdapter.CAMPAIGN_DEFAULT_DURATION, delegate, null);
     }
     public void moduleCampaignAddPromotion(Activity activity, int duration, MCMCampaignNotifiedDelegate delegate, Integer loadingImgResId) {
         MCMCampaignAdapter.getInstance().addBanner(activity, MCMCampaignDTO.CampaignType.IN_APP_PROMOTION, duration, delegate, loadingImgResId);
     }
 
     /**
      * Method that requests all the available cross selling campaigns for the app and calls the receiver
      * with the banner views to let the developer places them
      * @param activity the context where the request is made
      * @param receiver the interface that will be called with the retrieved data
      */
     public void moduleCampaignRequestPromotion(Activity activity,MCMCampaignAdapter.RequestCampaignReceiver receiver) {
         MCMCampaignAdapter.getInstance().requestBanner(activity, MCMCampaignDTO.CampaignType.IN_APP_PROMOTION, receiver);
     }
 
     /**
      * Method that add an RateMyApp alert based on the server params
      * @param activity where the alert will be shown
      * @param delegate for handle the campaign behaviour
      */
     public void moduleCampaignAddRateMyApp(Activity activity,MCMCampaignNotifiedDelegate delegate) {
         MCMCampaignAdapter.getInstance().addRateAlert(activity, delegate);
     }
 
 }
