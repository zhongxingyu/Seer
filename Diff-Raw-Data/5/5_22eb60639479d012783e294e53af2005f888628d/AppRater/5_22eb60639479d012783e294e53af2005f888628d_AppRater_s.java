 /*
  * Copyright (C) 2012 Friederike Wild <friederike.wild@devmob.de>
  * Created 06.05.2012
  * 
  * https://github.com/friederikewild/DroidAppRater
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package de.devmob.androlib.apprater;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.DialogInterface.OnClickListener;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 
 /**
  * Class to use the app rater component.
  * All available meta keys that can be used to configure the component via the apps AndroidManifest.xml are provided here.
  * 
  * @author Friederike Wild
  */
 public class AppRater
 {
     /** 
      * Meta key to configure the amount of app launches, till the rating dialog should be shown for the first time / next time after postponing.
      *  
      * <meta-data android:name="de.devmob.launch_till_rate" android:value="4" />
      */
     public static final String   META_CONFIG_LAUNCH_BEFORE_RATE = "de.devmob.launch_till_rate";
 
     /** 
      * Meta key to configure the amount of days after installation, till the rating dialog should be shown for the first time / next time after postponing.
      *  
      * <meta-data android:name="de.devmob.days_till_rate" android:value="4" />
      */
     public static final String   META_CONFIG_DAYS_BEFORE_RATE   = "de.devmob.days_till_rate";
 
     /** 
      * Meta key to configure the amount of events after install, till the rating dialog should be shown for the first time / next time after postponing.
      *  
      * <meta-data android:name="de.devmob.events_till_rate" android:value="2" />
      */
     public static final String   META_CONFIG_EVENTS_BEFORE_RATE = "de.devmob.events_till_rate";
 
     /** 
      * Meta key to configure if app rating should log.
      *  
      * <meta-data android:name="de.devmob.verbose" android:value="true" />
      */
     public static final String   META_CONFIG_VERBOSE            = "de.devmob.verbose";
 
     /** Logging tag for the app rater component */
     public static final String   LOG_TAG                        = "devmob_apprater";
 
     /** Flag to turn of the app rater is wanted */
     private static final boolean ENABLE_APPRATER                = true;
 
     /** Default count before the rating dialog should be shown. */
     private static final int     DEFAULT_LAUNCH_BEFORE_RATE     = 4;
 
     /** Default days before the rating dialog should be shown. */
     private static final int     DEFAULT_DAYS_BEFORE_RATE       = 4;
 
     /** Default count of positive events before the rating dialog should be shown. */
     private static final int     DEFAULT_EVENTS_BEFORE_RATE     = 2;
 
     private Context              context;
     /** The optional callback object to be noticed about the chosen dialog option. Past null if not interested. */
     private AppRaterCallback     callbackHandler                = null;
     private AppRaterPreferences  preferences;
 
     public AppRater(Context context)
     {
         this.setContext(context);
     }
 
     public void setContext(Context context)
     {
         this.context = context;
         this.preferences = new AppRaterPreferences(context, shouldLog());
     }
 
     public void invalidateContext()
     {
         this.context = null;
         this.preferences = null;
     }
 
     public void setAppRaterCallback(AppRaterCallback callbackHandler)
     {
         this.callbackHandler = callbackHandler;
     }
 
     /**
      * Method to call from the onCreate method of the first activity that is shown.
      */
     public void checkToShowRatingOnStart()
     {
         if (shouldAppShowRatingOnStart() && ENABLE_APPRATER)
         {
             showAppraterDialog();
         }
     }
 
     /**
      * Method to call from any point during the application when something positive
      * to the user happened.
      * 
      */
     public void checkToShowRatingOnEvent()
     {
         if (shouldAppShowRatingOnEvent() && ENABLE_APPRATER)
         {
             showAppraterDialog();
         }
     }
 
     /**
      * Reset all rater related preferences.
      * This also resets a previous show-never answer.
      */
     public void resetAllStoredPreferences()
     {
         preferences.reset();
     }
 
     /**
      * Reset all rater related preferences.
      * Honors users previous decision to not rate.
      */
     public void resetVotingsIfNotRatingDeclined()
     {
         if (!preferences.isRatingRequestDeclined())
         {
             resetAllStoredPreferences();
         }
     }
 
     /**
      * Get the configured amount of app launches before the rating dialog should be shown.
      * 
      * @return
      */
     private int getConfigLaunchBeforeRateCount()
     {
         int launchBeforeRate = getConfigurationIntOrDefaultValue(META_CONFIG_LAUNCH_BEFORE_RATE, DEFAULT_LAUNCH_BEFORE_RATE);
 
         if (shouldLog())
         {            
             Log.i(AppRater.LOG_TAG, "Devmob AppRater configured to wait for " + launchBeforeRate + " launches.");
         }
 
         return launchBeforeRate;
     }
 
     /**
      * Get the configured amount of days before the rating dialog should be shown.
      * 
      * @return
      */
     private int getConfigDaysBeforeRateCount()
     {
         int daysBeforeRate = getConfigurationIntOrDefaultValue(META_CONFIG_DAYS_BEFORE_RATE, DEFAULT_DAYS_BEFORE_RATE);
 
         if (shouldLog())
         {            
             Log.i(AppRater.LOG_TAG, "Devmob AppRater configured to wait for " + daysBeforeRate + " days.");
         }
 
         return daysBeforeRate;
     }
 
     /**
      * Get the configured amount of positive events before the rating dialog should be shown.
      * 
      * @return
      */
     private int getConfigEventsBeforeRateCount()
     {
         int daysBeforeRate = getConfigurationIntOrDefaultValue(META_CONFIG_EVENTS_BEFORE_RATE, DEFAULT_EVENTS_BEFORE_RATE);
 
         if (shouldLog())
         {            
             Log.i(AppRater.LOG_TAG, "Devmob AppRater configured to wait for " + daysBeforeRate + " positive events.");
         }
 
         return daysBeforeRate;
     }
 
     /**
      * Util method to get a configured int value from the application bundle information defined by the 
      * given key. In case the entry doesn't exist or anyhting goes wrong, the defaultValue is returned.
      * 
      * @param configKey
      * @param defaultValue
      * @return
      */
     private int getConfigurationIntOrDefaultValue(String configKey, int defaultValue)
     {
         int returnValue = defaultValue;
 
         try
         {
             ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
             Bundle aBundle = ai.metaData;
             returnValue = aBundle.getInt(configKey);
             // Check if available
             if (returnValue == 0)
             {                
                 returnValue = defaultValue;
             }
         }
         catch (Exception e)
         {
             // Ignore and reset to default
             returnValue = defaultValue;
         }
 
         return returnValue;
     }
 
     /**
      * Check if the app rating should be shown.
      * Checks the status of the app launches and the previous app rating usage.
      * 
      * @return Flag if the dialog should be shown.
      */
     private boolean shouldAppShowRatingOnStart()
     {
        // Increase count (except when ratingn was declined before)
         if (!preferences.isRatingRequestDeclined())
         {
             preferences.increaseCountOpened();
         }
 
         // No rating case it was already dismissed or rated.
         if (preferences.isRatingRequestDeactivated())
         {
             Log.i(AppRater.LOG_TAG, "AppRater configured to never request rating via dialog (reset after re-install of the app). Checked on start.");
             return false;
         }
 
         // Check if enough days gone by
         long currentTime = System.currentTimeMillis();
         long storedTime = preferences.getStoredStartDate();
         long daysPastSinceStart= ((currentTime - storedTime) / (1000 * 60 * 60 * 24));
 
         if (shouldLog())
         {            
             Log.i(AppRater.LOG_TAG, "AppRater comparison " + daysPastSinceStart + " past ? >= " + getConfigDaysBeforeRateCount());
         }
 
         if (daysPastSinceStart < getConfigDaysBeforeRateCount())
         {
             return false;
         }
 
         // Check the usage
         int countOpened = preferences.getCountOpened();
         if (countOpened % getConfigLaunchBeforeRateCount() == 0)
         {
             return true;
         }
 
         // Fallback is not to show the dialog
         return false;
     }
 
     /**
      * Check if the app rating should be shown.
      * Checks the status of the app events and the previous app rating usage.
      * 
      * @return Flag if the dialog should be shown.
      */
     private boolean shouldAppShowRatingOnEvent()
     {
        // Increase count (except when ratingn was declined before)
         if (!preferences.isRatingRequestDeclined())
         {
             preferences.increaseCountEvents();
         }
         
         // No rating case it was already dismissed or rated.
         if (preferences.isRatingRequestDeactivated())
         {
             Log.i(AppRater.LOG_TAG, "AppRater configured to never request rating via dialog (reset after re-install of the app). Checked on event.");
             return false;
         }
 
         // Check the usage
         int countEvents = preferences.getCountEvents();
         if (countEvents % getConfigEventsBeforeRateCount() == 0)
         {
             return true;
         }
 
         // Fallback is not to show the dialog
         return false;
     }
 
     /**
      * Create and show the app rating dialog. This fetches the package name from the context
      * and uses the texts as given in the locale resources.  
      */
     private void showAppraterDialog()
     {
         AlertDialog.Builder builderInvite = new AlertDialog.Builder(context);
 
         String packageName = "";
         String appName = "";
         try
         {
             // Get the package info manager from the given context
             PackageManager manager = context.getPackageManager();
             PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
 
             // Dynamically read the package name and the application name
             packageName = info.packageName;
             appName = context.getResources().getString(info.applicationInfo.labelRes);
         }
         catch (Exception e)
         {
             // When failing to get the needed information, we ignore the wish to show a rater dialog
             return;
         }
 
         Log.d("Appirater", "PackageName: " + packageName);
 
         
         // TODO (fwild): Add other kinds of links when different stores are supported
         
         // Create the link to the google play store detail page
         final String marketLink = "market://details?id=" + packageName;
 
         if (shouldLog())
         {            
             Log.i(AppRater.LOG_TAG, "Url to link for rating: " + marketLink);
         }
 
         String title = context.getString(R.string.dialog_rate_title, appName);
         builderInvite.setTitle(title);
 
         String message = context.getString(R.string.dialog_rate_message, appName);
         builderInvite.setMessage(message);
         
         String buttonOK = context.getString(R.string.rating_dialog_button_ok);
         String buttonLater = context.getString(R.string.rating_dialog_button_later);
         String buttonNever = context.getString(R.string.rating_dialog_button_never);
         
         builderInvite.setPositiveButton(buttonOK, new DialogInterface.OnClickListener()
         {
             public void onClick(DialogInterface dialog, int id)
             {
                 if (callbackHandler != null)
                 {
                     callbackHandler.processRate();
                 }
 
                 // Mark as never ask for rating again (cause now it was done)
                 preferences.storeRated();
                 
                 // Trigger the rating intent
                 Uri uri = Uri.parse(marketLink);
                 Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                 context.startActivity(intent);
                 dialog.dismiss();
             }
         }).setNeutralButton(buttonLater, new OnClickListener()
         {
             public void onClick(DialogInterface dialog, int which)
             {
                 if (callbackHandler != null)
                 {
                     callbackHandler.processRemindMe();
                 }
                 
                 // Mark as to ask later again
                 preferences.storeToRateLater();
 
                 dialog.dismiss();
             }
         }).setNegativeButton(buttonNever, new OnClickListener()
         {
             public void onClick(DialogInterface dialog, int which)
             {
                 if (callbackHandler != null)
                 {
                     callbackHandler.processNever();
                 }
                 
                 // Mark as never ask for rating again
                 preferences.storeRatingDeclined();
                 
                 dialog.cancel();
             }
         });
         builderInvite.create().show();
     }
 
     /**
      * Check if the app rater component should verbose its logs.
      * 
      * @return Flag if logging is enabled.
      */
     public boolean shouldLog()
     {
         boolean shouldLog = false;
 
         try
         {
             ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
             Bundle aBundle = ai.metaData;
             shouldLog = aBundle.getBoolean(META_CONFIG_VERBOSE);
         }
         catch (Exception e)
         {
             // Ignore and reset to default
             shouldLog = false;
         }
 
         return shouldLog;
     }
 }
