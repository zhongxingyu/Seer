 package com.ouchadam.fang.presentation.controller;
 
 import android.app.Activity;
 import android.content.res.Resources;
 import android.text.TextUtils;
 
 import com.bugsense.trace.BugSenseHandler;
 import com.ouchadam.fang.R;
 
 public class BugsenseDelegate {
 
     private static final String INVALID = "invalidkey";
     private final Resources resources;
 
     public BugsenseDelegate(Resources resources) {
         this.resources = resources;
     }
 
     public void init(Activity activity) {
         String bugsenseKey = getApiKey();
        if (hasValidBugsenseKey(bugsenseKey)) {
             BugSenseHandler.initAndStartSession(activity, bugsenseKey);
         }
     }
 
    private boolean hasValidBugsenseKey(String bugsenseKey) {
        return !INVALID.equals(bugsenseKey);
     }
 
     private String getApiKey() {
         try {
             String apiKey = resources.getString(R.string.bugsense_key);
             if (TextUtils.isEmpty(apiKey)) {
                 return INVALID;
             }
             return apiKey;
         } catch (Resources.NotFoundException e) {
             return INVALID;
         }
     }
 
 }
