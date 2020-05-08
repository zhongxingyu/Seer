 package com.mridang.cyanight;
 
 import java.util.Random;
 import java.util.Set;
 
 import org.acra.ACRA;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.content.pm.ResolveInfo;
 import android.net.Uri;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 import com.google.android.apps.dashclock.api.DashClockExtension;
 import com.google.android.apps.dashclock.api.ExtensionData;
 
 /**
  * Base extension class that extends the default Dashclock Extension class and add
  * more functionality such as registering intents
  * @author mridang
  */
 @SuppressWarnings("ALL")
 public abstract class ImprovedExtension extends DashClockExtension {
 
 	/**
 	 * Generic broadcast receiver to receive all the registered intent
 	 * @author mridang
 	 */
 	private class DashclockReceiver extends BroadcastReceiver {
 
 		/**
 		 * Receiver method that receives the intent and simply pass it to the
 		 * extension
 		 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
 		 */
 		@Override
 		public void onReceive(Context ctxContext, Intent ittIntent) {
 			ImprovedExtension.this.onReceiveIntent(ctxContext, ittIntent);
 		}
 
 	}
 
 	/**
 	 * The extension data that was previously published so that it can be reused
 	 * when we aren't showing the ads
 	 */
 	private ExtensionData edtPrevious;
 	/**
 	 * The instance of the broadcast receiver is this extension uses intents or
 	 * a receiver is registered
 	 */
 	private DashclockReceiver objReceiver;
 	/**
 	 * The instance of the shared preferences of this extension which is loaded
 	 * when the first setting is read
 	 */
     private SharedPreferences speSettings;
 
 	/*
 	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onCreate()
 	 */
 	public void onCreate() {
 
 		super.onCreate();
 		Log.d(getTag(), "Created");
 		ACRA.init(new AcraApplication(getApplicationContext()));
 
 	}
 
 	/*
 	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onDestroy()
 	 */
 	@Override
 	public void onDestroy() {
 
 		if (objReceiver != null) {
 
 			try {
 
 				Log.d(getTag(), "Unregistered any existing status receivers");
 				unregisterReceiver(objReceiver);
 
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 
 		}
 
		if (getUris() != null) {
			removeAllWatchContentUris();
		}
 		super.onDestroy();
 		Log.d(getTag(), "Destroyed");
 
 	}
 
 	/**
 	 * Abstract method that needs to be overridden to return a list of intents
 	 * that should be registered
 	 * @return A intent filter with the list of intents to register
 	 */
 	protected abstract IntentFilter getIntents();
 
 	/**
 	 * Abstract method that needs to be overridden to return a the logging tag
 	 * @return The logging tag that should be used for logging messages
 	 */
 	protected abstract String getTag();
 
 	/**
 	 * Abstract method that needs to be overridden to return a list of content-uris
 	 * @return A list of content uris to watch
 	 */
 	protected abstract String[] getUris();
 
 	/*
 	 * @see
 	 * com.google.android.apps.dashclock.api.DashClockExtension#onInitialize
 	 * (boolean)
 	 */
 	@Override
 	protected void onInitialize(boolean booReconnect) {
 
 		super.onInitialize(booReconnect);
 		IntentFilter ittIntents = this.getIntents();
 		if (ittIntents != null) {
 
 			if (objReceiver != null) {
 
 				try {
 
 					Log.d(getTag(), "Unregistered any existing status receivers");
 					unregisterReceiver(objReceiver);
 
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 
 			}
 
 			objReceiver = new DashclockReceiver();
 			registerReceiver(objReceiver, ittIntents);
 			Log.d(getTag(), "Registered the status receiver");
 
 		}
 
 		if (this.getUris() != null) {
 			addWatchContentUris(this.getUris());
 		}
 
 	}
 
 	/**
 	 * Method that receives all the intents that are passed to from the broadcast
 	 * receiver. This method needs to be implemented but doesn't need to do anything
 	 * if no intents are needed
 	 * @param ctxContext The current service's context
 	 * @param ittIntent The intent that was received
 	 */
 	protected abstract void onReceiveIntent(Context ctxContext,Intent ittIntent);
 
 	/**
 	 * Helper method that simply tries to publish the previous update that was
 	 * published.
 	 */
 	protected final void doUpdate() {
 		doUpdate(edtPrevious);
 	}
 
 	/**
 	 * Helper method that publishes an update but also checks if the advert message
 	 * needs to be shown.
 	 * @param edtPublish The extension data that needs to be published
 	 */
 	final void doUpdate(ExtensionData edtPublish) {
 
 		edtPrevious = edtPublish;
 		try {
 
             //noinspection DoubleNegation
             if (new Random().nextInt(5) == 0 && !(0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE))) {
 
 				PackageManager mgrPackages = getApplicationContext().getPackageManager();
 
 				try {
 
 					mgrPackages.getPackageInfo("com.mridang.donate", PackageManager.GET_META_DATA);
 
 				} catch (NameNotFoundException e) {
 
 					Integer intExtensions = 0;
 					Intent ittFilter = new Intent("com.google.android.apps.dashclock.Extension");
 					String strPackage;
 
 					for (ResolveInfo info : mgrPackages.queryIntentServices(ittFilter, 0)) {
 
 						strPackage = info.serviceInfo.applicationInfo.packageName;
 						intExtensions = intExtensions + (strPackage.startsWith("com.mridang.") ? 1 : 0);
 
 					}
 
 					if (intExtensions > 1) {
 
 						ExtensionData edtAdvert = new ExtensionData();
 						edtAdvert.visible(true);
 						edtAdvert.clickIntent(new Intent(Intent.ACTION_VIEW).setData(Uri
 								.parse("market:details?id=com.mridang.donate")));
 						edtAdvert.expandedTitle("Please consider a one time purchase to unlock.");
 						edtAdvert
 						.expandedBody("Thank you for using "
 								+ intExtensions
 								+ " extensions of mine. Click this to make a one-time purchase or use just one extension to make this disappear.");
 						setUpdateWhenScreenOn(true);
 						publishUpdate(edtAdvert);
 
 					}
 
 				}
 
 			} else {
 				publishUpdate(edtPrevious);
 			}
 
 		} catch (Exception e) {
 			edtPublish.visible(false);
 			Log.e(getTag(), "Encountered an error", e);
 			ACRA.getErrorReporter().handleSilentException(e);
 		} finally {
 			speSettings = null;
 		}
 
 		Log.d(getTag(), "Done");
 	}
 
 	/*
 	 * @see android.content.res.Resources#getQuantityString(int, int, Object...)
 	 */
     String getQuantityString(Integer intId, Integer intQuantity, Object objArgs) {
 		return getResources().getQuantityString(intId, intQuantity, objArgs);
 	}
 
 	/*
 	 * @see android.preference.PreferenceManager#getDefaultSharedPreferences(Context) 
 	 */
     SharedPreferences getSettings() {
 		return PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see android.content.SharedPreferences#getBoolean(java.lang.String, boolean)
 	 */
 	public boolean getBoolean(String strKey, Boolean booDefault) {
 
 		if (speSettings == null) {
 			speSettings = getSettings();
 		}
 		return speSettings.getBoolean(strKey, booDefault);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see android.content.SharedPreferences#getFloat(java.lang.String, float)
 	 */
 	public float getFloat(String strKey, Float fltDefault) {
 
 		if (speSettings == null) {
 			speSettings = getSettings();
 		}
 		return speSettings.getFloat(strKey, fltDefault);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see android.content.SharedPreferences#getInt(java.lang.String, int)
 	 */
 	public int getInt(String strKey, int intDefault) {
 
 		if (speSettings == null) {
 			speSettings = getSettings();
 		}
 		return speSettings.getInt(strKey, intDefault);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see android.content.SharedPreferences#getLong(java.lang.String, long)
 	 */
 	public long getLong(String strKey, Long lngDefault) {
 
 		if (speSettings == null) {
 			speSettings = getSettings();
 		}
 		return speSettings.getLong(strKey, lngDefault);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see android.content.SharedPreferences#getString(java.lang.String, java.lang.String)
 	 */
 	public String getString(String strKey, String strDefault) {
 
 		if (speSettings == null) {
 			speSettings = getSettings();
 		}
 		return speSettings.getString(strKey, strDefault);
 
 	}
 
     /*
      * (non-Javadoc)
      * @see android.content.SharedPreferences#getStringSet(java.lang.String, java.util.Set)
      */
     public Set<String> getSet(String strKey, Set<String> setDefault) {
 
         if (speSettings == null) {
             speSettings = getSettings();
         }
         return speSettings.getStringSet(strKey, setDefault);
 
     }
 
     /*
      * @see android.content.SharedPreferences#contains(java.lang.String)
      */
     public Boolean hasSetting(String strKey) {
 
         if (speSettings == null) {
             speSettings = getSettings();
         }
         return speSettings.contains(strKey);
 
     }
 
     /*
      * (non-Javadoc)
      * @see  android.content.SharedPreferences.Editor android.content.SharedPreferences#edit()
      */
     public SharedPreferences.Editor getEditor() {
 
         if (speSettings == null) {
             speSettings = getSettings();
         }
         return speSettings.edit();
 
     }
 
 }
