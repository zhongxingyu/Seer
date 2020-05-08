 package edu.mit.mobile.android.livingpostcards.util;
 
 import android.accounts.Account;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import edu.mit.mobile.android.livingpostcards.BuildConfig;
 import edu.mit.mobile.android.livingpostcards.auth.Authenticator;
 import edu.mit.mobile.android.livingpostcards.sync.AccountSyncService;
 
 /**
  * Version-specific patches to fix issues that were deployed.
  *
  * @author <a href="mailto:spomeroy@mit.edu">Steve Pomeroy</a>
  *
  */
 public class Patches {
 
     private static final String TAG = Patches.class.getSimpleName();
 
     private static final String PREF_LAST_APPLIED_PATCH = "last_applied_patch";
 
     public static void checkforAndApplyPatches(Context context) {
 
         final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
 
         final int lastAppliedPatch = prefs.getInt(PREF_LAST_APPLIED_PATCH, 0);
 
         final int thisVersion = getAppVersion(context);
 
         if (lastAppliedPatch == thisVersion) {
             return;
         }
 
         if (BuildConfig.DEBUG) {
             Log.d(TAG, "applying any patches for version " + thisVersion);
         }
 
         switch (thisVersion) {
             case 10:
                 patchV10FixLivingPostcardsDomain(context);
                 break;
         }
 
         if (BuildConfig.DEBUG) {
             Log.d(TAG, "Applied all patches.");
         }
 
         new Thread(new Runnable() {
             @Override
             public void run() {
                 prefs.edit().putInt(PREF_LAST_APPLIED_PATCH, thisVersion).commit();
             }
         }).start();
     }
 
     private static int getAppVersion(Context context) {
         PackageInfo pInfo;
         try {
             pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
         } catch (final NameNotFoundException e) {
             Log.e(TAG, "error getting app version", e);
             return 0;
         }
 
         return pInfo.versionCode;
     }
 
     private static void patchV10FixLivingPostcardsDomain(Context context) {
         final Account me = Authenticator.getFirstAccount(context);

        AccountSyncService.setApiUrl(context, me, "http://livingpostcards.org/api/");
     }
 }
