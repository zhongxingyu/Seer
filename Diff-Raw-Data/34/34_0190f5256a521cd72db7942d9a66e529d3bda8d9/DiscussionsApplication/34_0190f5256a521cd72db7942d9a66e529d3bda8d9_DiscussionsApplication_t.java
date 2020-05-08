 /*
  * Copyright 2012 sloboda-studio.com
  */
 package com.slobodastudio.discussions;
 
import android.annotation.TargetApi;
 import android.app.Application;
 import android.os.StrictMode;
 
 import com.bugsense.trace.BugSenseHandler;
 
 public class DiscussionsApplication extends Application {
 
	@TargetApi(9)
 	@Override
 	public void onCreate() {
 
 		super.onCreate();
 		if (ApplicationConstants.DEV_MODE) {
 			StrictMode
 					.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
 			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
 		} else {
 			BugSenseHandler.setup(this, ApplicationConstants.BUG_SENSE_API_KEY);
 		}
 	}
 }
