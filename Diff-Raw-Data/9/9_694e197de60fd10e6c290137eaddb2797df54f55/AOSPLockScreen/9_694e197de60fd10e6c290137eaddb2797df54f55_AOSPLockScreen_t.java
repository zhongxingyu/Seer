 package com.mohammadag.aosplockscreen;
 
 import static de.robv.android.xposed.XposedHelpers.findClass;
 import static de.robv.android.xposed.XposedHelpers.getObjectField;
 
 import java.lang.reflect.Constructor;
 
 import de.robv.android.xposed.IXposedHookLoadPackage;
 import de.robv.android.xposed.XC_MethodHook;
 import de.robv.android.xposed.XC_MethodReplacement;
 import de.robv.android.xposed.XSharedPreferences;
 import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
 import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
 
 public class AOSPLockScreen implements IXposedHookLoadPackage {
 
 	public static XSharedPreferences prefs;
 	
 	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
 		if (!lpparam.packageName.equals("android"))
 			return;
 		
 		prefs = new XSharedPreferences(AOSPLockScreen.class.getPackage().getName());
 		
 		XposedHelpers.findAndHookMethod("com.android.internal.policy.impl.LockScreen", lpparam.classLoader,
				"shouldEnableMenuKey", new XC_MethodHook() {
    		@Override
    		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
    			param.setResult(false);
    		}
		});
 		
 		Class<?> lockPatternUtilsClass = findClass("com.android.internal.widget.LockPatternUtils", lpparam.classLoader);
 		Class<?> keyguardUpdateMonitorClass = findClass("com.android.internal.policy.impl.KeyguardUpdateMonitor", lpparam.classLoader);
 		Class<?> keyguardScreenCallbackClass = findClass("com.android.internal.policy.impl.KeyguardScreenCallback", lpparam.classLoader);
 		Class<?> contextClass = findClass("android.content.Context", lpparam.classLoader);
 		Class<?> configurationClass = findClass("android.content.res.Configuration", lpparam.classLoader);
 		
 		Class<?> aospLockScreenClass = findClass("com.android.internal.policy.impl.LockScreen", lpparam.classLoader);
 		Class<?> circleLockScreenClass = findClass("com.android.internal.policy.impl.sec.CircleLockScreen", lpparam.classLoader);
 		
 		final Constructor<?> aospLockScreen = XposedHelpers.findConstructorBestMatch(aospLockScreenClass, contextClass, configurationClass,
 				lockPatternUtilsClass, keyguardUpdateMonitorClass, keyguardScreenCallbackClass);
 		
 		final Constructor<?> circleLockScreen = XposedHelpers.findConstructorBestMatch(circleLockScreenClass, contextClass, configurationClass,
 				lockPatternUtilsClass, keyguardUpdateMonitorClass, keyguardScreenCallbackClass);
 		
 		XposedHelpers.findAndHookMethod("com.android.internal.policy.impl.LockPatternKeyguardView",
 				lpparam.classLoader, "createLockScreen", new XC_MethodHook() {
 			
     		@Override
     		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
     			Object lockView = null;
     			if (prefs.getBoolean("aosp-lockscreen", true)) {
     				lockView = aospLockScreen.newInstance(getObjectField(param.thisObject, "mContext"),
     		    	    getObjectField(param.thisObject, "mConfiguration"), getObjectField(param.thisObject, "mLockPatternUtils"),
     		    	    getObjectField(param.thisObject, "mUpdateMonitor"), getObjectField(param.thisObject, "mKeyguardScreenCallback"));
     			} else {
     				lockView = circleLockScreen.newInstance(getObjectField(param.thisObject, "mContext"),
         		    	    getObjectField(param.thisObject, "mConfiguration"), getObjectField(param.thisObject, "mLockPatternUtils"),
         		    	    getObjectField(param.thisObject, "mUpdateMonitor"), getObjectField(param.thisObject, "mKeyguardScreenCallback"));
     			}
     		    XposedHelpers.callMethod(param.thisObject, "initializeTransportControlView", lockView);
     		    param.setResult(lockView);
     		}
 		});
 	}
 	
 }
