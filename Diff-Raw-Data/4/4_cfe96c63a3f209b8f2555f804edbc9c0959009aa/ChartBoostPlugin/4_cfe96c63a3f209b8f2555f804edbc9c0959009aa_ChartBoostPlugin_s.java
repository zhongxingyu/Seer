 package com.tealeaf.plugin.plugins;
 
 import com.tealeaf.logger;
 import com.tealeaf.plugin.IPlugin;
 import java.io.*;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.chartboost.sdk.*;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.Context;
 import android.util.Log;
 import android.os.Bundle;
 import android.content.pm.ActivityInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 
 public class ChartBoostPlugin implements IPlugin {
 
 	private Chartboost cb;
 	private Activity mActivity;
 
 	private class PluginDelegate implements ChartboostDelegate {
 		@Override
 		public void didCacheMoreApps() {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public void didClickInterstitial(String arg0) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public void didClickMoreApps() {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public void didCloseInterstitial(String arg0) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public void didCloseMoreApps() {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public void didDismissInterstitial(String arg0) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public void didDismissMoreApps() {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public void didFailToLoadInterstitial(String arg0) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public void didFailToLoadMoreApps() {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public void didShowInterstitial(String arg0) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public void didShowMoreApps() {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public boolean shouldDisplayInterstitial(String arg0) {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean shouldDisplayLoadingViewForMoreApps() {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean shouldDisplayMoreApps() {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean shouldRequestInterstitial(String arg0) {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean shouldRequestInterstitialsInFirstSession() {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean shouldRequestMoreApps() {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public void didCacheInterstitial(String arg0) {
 			// TODO Auto-generated method stub
 			
 		};
 	}
 
 	public ChartBoostPlugin() {
 
 	}
 
 	public void onCreateApplication(Context applicationContext) {
 	
 	}
 
 	public void onCreate(Activity activity, Bundle savedInstanceState) {
 		this.mActivity = activity;
 
         PackageManager manager = activity.getPackageManager();
         String appID = "", appSignature = "";
         try {
             Bundle meta = manager.getApplicationInfo(activity.getPackageName(), PackageManager.GET_META_DATA).metaData;
             if (meta != null) {
                appID = meta.getString("CHARTBOOST_APP_ID");
                appSignature = meta.getString("CHARTBOOST_APP_SIGNATURE");
             }
         } catch (Exception e) {
             android.util.Log.d("EXCEPTION", "" + e.getMessage());
         }
 
 		logger.log("{chartboost} Initializing from manifest with AppID=", appID, "and signature=", appSignature);
 
 		this.cb = Chartboost.sharedChartboost();
 		this.cb.onCreate(activity, appID, appSignature, null);
 		this.cb.startSession();
 	}
 
 	public void onResume() {
 	
 	}
 
 	public void onStart() {
 		this.cb.onStart(mActivity);
 	}
 
 	public void onPause() {
 	
 	}
 
 	public void onStop() {
 		this.cb.onStop(mActivity);
 	}
 
 	public void onDestroy() {
 		this.cb.onDestroy(mActivity);
 	}
 
 	public void onNewIntent(Intent intent) {
 	
 	}
 
 	public void setInstallReferrer(String referrer) {
 	
 	}
 
 	public void onActivityResult(Integer request, Integer result, Intent data) {
 	
 	}
 
 	public boolean consumeOnBackPressed() {
 		return true;
 	}
 
 	public void onBackPressed() {
 	}
 
 }
