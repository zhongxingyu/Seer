 package android.privacy;
 
 import android.app.ActivityManager;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.Signature;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.os.Binder;
 import android.util.Log;
 
 import java.io.File;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 /**
  * PrivacySettingsManager's counterpart running in the system process, which
  * allows write access to /data/
  * @author Svyatoslav Hresyk
  * TODO: add selective contact access management API
  * {@hide}
  */
 public class PrivacySettingsManagerService extends IPrivacySettingsManager.Stub {
 
     private static final String TAG = "PrivacySettingsManagerService";
     
     private static final String WRITE_PRIVACY_SETTINGS = "android.privacy.WRITE_PRIVACY_SETTINGS";
 
     private static final String READ_PRIVACY_SETTINGS = "android.privacy.READ_PRIVACY_SETTINGS";
     
     private static final String MANAGE_PRIVACY_APPLICATIONS = "android.privacy.MANAGE_PRIVACY_APPLICATIONS";
 
     private PrivacyPersistenceAdapter persistenceAdapter;
 
     private Context context;
     
     public static PrivacyFileObserver obs;
     
     private boolean enabled;
     private boolean notificationsEnabled;
     private boolean bootCompleted;
     
     private static final double VERSION = 1.51;
     
     /**
      * @hide - this should be instantiated through Context.getSystemService
      * @param context
      */
     public PrivacySettingsManagerService(Context context) {
         Log.i(TAG, "PrivacySettingsManagerService - initializing for package: " + context.getPackageName() + 
                 " UID: " + Binder.getCallingUid());
         this.context = context;
         
         persistenceAdapter = new PrivacyPersistenceAdapter(context);
         obs = new PrivacyFileObserver("/data/system/privacy", this);
         
         enabled = persistenceAdapter.getValue(PrivacyPersistenceAdapter.SETTING_ENABLED).equals(PrivacyPersistenceAdapter.VALUE_TRUE);
         notificationsEnabled = persistenceAdapter.getValue(PrivacyPersistenceAdapter.SETTING_NOTIFICATIONS_ENABLED).equals(PrivacyPersistenceAdapter.VALUE_TRUE);
         bootCompleted = false;
     }
     
     public PrivacySettings getSettings(String packageName) {
 //        Log.d(TAG, "getSettings - " + packageName);
           if (enabled || context.getPackageName().equals("com.privacy.pdroid") || context.getPackageName().equals("com.privacy.pdroid.Addon") 
 	      || context.getPackageName().equals("com.android.privacy.pdroid.extension"))  //we have to add our addon package here, to get real settings
 //	  if (Binder.getCallingUid() != 1000)
 //            	context.enforceCallingPermission(READ_PRIVACY_SETTINGS, "Requires READ_PRIVACY_SETTINGS");
           return persistenceAdapter.getSettings(packageName, false);
           else return null;
     }
 
     public boolean saveSettings(PrivacySettings settings) {
         Log.d(TAG, "saveSettings - checking if caller (UID: " + Binder.getCallingUid() + ") has sufficient permissions");
         // check permission if not being called by the system process
 	//if(!context.getPackageName().equals("com.privacy.pdroid.Addon")){ //enforce permission, because declaring in manifest doesn't work well -> let my addon package save settings
        	if (Binder.getCallingUid() != 1000)
             		context.enforceCallingPermission(WRITE_PRIVACY_SETTINGS, "Requires WRITE_PRIVACY_SETTINGS");
         			if (!getIsAuthorizedManagerApp(Binder.getCallingPid())) {
         				throw new SecurityException("Application must be authorised to save changes");
         			}
 	//}
         Log.d(TAG, "saveSettings - " + settings);
         boolean result = persistenceAdapter.saveSettings(settings);
         if (result == true) obs.addObserver(settings.getPackageName());
         return result;
     }
     
     public boolean deleteSettings(String packageName) {
 //        Log.d(TAG, "deleteSettings - " + packageName + " UID: " + uid + " " +
 //        		"checking if caller (UID: " + Binder.getCallingUid() + ") has sufficient permissions");
         // check permission if not being called by the system process
 	//if(!context.getPackageName().equals("com.privacy.pdroid.Addon")){//enforce permission, because declaring in manifest doesn't work well -> let my addon package delete settings
        	if (Binder.getCallingUid() != 1000)
             		context.enforceCallingPermission(WRITE_PRIVACY_SETTINGS, "Requires WRITE_PRIVACY_SETTINGS");
 					if (!getIsAuthorizedManagerApp(Binder.getCallingPid())) {
 						throw new SecurityException("Application must be authorised to save changes");
 					}
 	//}
         boolean result = persistenceAdapter.deleteSettings(packageName);
         // update observer if directory exists
         String observePath = PrivacyPersistenceAdapter.SETTINGS_DIRECTORY + "/" + packageName;
         if (new File(observePath).exists() && result == true) {
             obs.addObserver(observePath);
         } else if (result == true) {
             obs.children.remove(observePath);
         }
         return result;
     }
     
     public boolean getIsAuthorizedManagerApp(int pid) {
     	Log.d(TAG, "getIsAuthorizedManagerApp - Getting packages for PID " + Integer.toString(pid));
 
         String packageName = null;
         ActivityManager actMgr = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
 		for(ActivityManager.RunningAppProcessInfo processInfo : actMgr.getRunningAppProcesses()){
 			if(processInfo.pid == pid){
 				packageName = processInfo.processName;
 			}
 		}
 		if (packageName == null) {
 			Log.d(TAG, "getIsAuthorizedManagerApp - Package name could not be obtained");
 			return false;
 		}
 
         return getIsAuthorizedManagerApp(packageName);
   }
     
     public boolean getIsAuthorizedManagerApp(String packageName) {
     	Log.d(TAG, "getIsAuthorizedManagerApp - Running for package " + packageName);
     	PackageManager pkgMgr = context.getPackageManager();
     	if (pkgMgr == null) {
     		Log.d(TAG, "getIsAuthorizedManagerApp - Package manager could not be obtained");
     		return false; 
     	}
     	
     	PackageInfo pkgInfo;
     	try {
 	    	//get the package info so we can get the signatures
 	    	 pkgInfo = pkgMgr.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
     	} catch (NameNotFoundException e) {
     		Log.d(TAG, "getIsAuthorizedManagerApp - Could not get package with name " + packageName);
     		return false;
     	}
     	if (pkgInfo == null) {
     		Log.d(TAG, "getIsAuthorizedManagerApp - Got null back when retrieving packageInfo for " + packageName);
     		return false;
     	}
     	
     	Log.d(TAG, "getIsAuthorizedManagerApp - Retrieving asciiSigs for " + packageName);
 		Set<String> asciiSigs = new HashSet<String>();
 		for (Signature signature : pkgInfo.signatures) {
 			Log.d(TAG, "getIsAuthorizedManagerApp - Found signature " + signature.toCharsString());
 			asciiSigs.add(signature.toCharsString());
 		}
     	
         return persistenceAdapter.getIsAuthorizedManagerApp(new String[] {packageName}, asciiSigs, false);
   }
 
     
     public void authorizeManagerApp(String packageName) {
     	if (Binder.getCallingUid() != 1000)
     		context.enforceCallingPermission(MANAGE_PRIVACY_APPLICATIONS, "Requires MANAGE_PRIVACY_APPLICATIONS");
 
     	PackageManager pkgMgr = context.getPackageManager();
     	if (pkgMgr == null) {
     		Log.d(TAG, "authorizeManagerApp - Package manager could not be obtained");
     		return; 
     	}
     	
     	PackageInfo pkgInfo;
     	try {
 	    	//get the package info so we can get the signatures
 	    	 pkgInfo = pkgMgr.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
     	} catch (NameNotFoundException e) {
     		Log.d(TAG, "authorizeManagerApp - Could not get package with name " + packageName);
     		return;
     	}
     	if (pkgInfo == null) {
     		Log.d(TAG, "authorizeManagerApp - Got null back when retrieving packageInfo for " + packageName);
     		return;
     	}
     	
 		Set<String> asciiSigs = new HashSet<String>();
 		for (Signature signature : pkgInfo.signatures) {
 			asciiSigs.add(signature.toCharsString());
 		}
     	
         persistenceAdapter.authorizeManagerApp(packageName, asciiSigs, false);
   }
     
     public void deauthorizeManagerApp(String packageName) {
     	if (Binder.getCallingUid() != 1000)
     		context.enforceCallingPermission(MANAGE_PRIVACY_APPLICATIONS, "Requires MANAGE_PRIVACY_APPLICATIONS");
 
         persistenceAdapter.deauthorizeManagerApp(packageName, false);
   }
     
     public double getVersion() {
         return VERSION;
     }
     
     public void notification(final String packageName, final byte accessMode, final String dataType, final String output) {
         if (bootCompleted && notificationsEnabled) {
 	    Intent intent = new Intent();
             intent.setAction(PrivacySettingsManager.ACTION_PRIVACY_NOTIFICATION);
             intent.putExtra("packageName", packageName);
             intent.putExtra("uid", PrivacyPersistenceAdapter.DUMMY_UID);
             intent.putExtra("accessMode", accessMode);
             intent.putExtra("dataType", dataType);
             intent.putExtra("output", output);
             context.sendBroadcast(intent);
         }
     }
     
     public void registerObservers() {
         context.enforceCallingPermission(WRITE_PRIVACY_SETTINGS, "Requires WRITE_PRIVACY_SETTINGS");        
         obs = new PrivacyFileObserver("/data/system/privacy", this);
     }
     
     public void addObserver(String packageName) {
         context.enforceCallingPermission(WRITE_PRIVACY_SETTINGS, "Requires WRITE_PRIVACY_SETTINGS");        
         obs.addObserver(packageName);
     }
     
     public boolean purgeSettings() {
         return persistenceAdapter.purgeSettings();
     }
     
     public void setBootCompleted() {
         bootCompleted = true;
     }
     
     public boolean setNotificationsEnabled(boolean enable) {
         String value = enable ? PrivacyPersistenceAdapter.VALUE_TRUE : PrivacyPersistenceAdapter.VALUE_FALSE;
         if (persistenceAdapter.setValue(PrivacyPersistenceAdapter.SETTING_NOTIFICATIONS_ENABLED, value)) {
             this.notificationsEnabled = true;
             this.bootCompleted = true;
             return true;
         } else {
             return false;
         }
     }
     
     public boolean setEnabled(boolean enable) {
         String value = enable ? PrivacyPersistenceAdapter.VALUE_TRUE : PrivacyPersistenceAdapter.VALUE_FALSE;
         if (persistenceAdapter.setValue(PrivacyPersistenceAdapter.SETTING_ENABLED, value)) {
             this.enabled = true;
             return true;
         } else {
             return false;
         }
     }
 }
