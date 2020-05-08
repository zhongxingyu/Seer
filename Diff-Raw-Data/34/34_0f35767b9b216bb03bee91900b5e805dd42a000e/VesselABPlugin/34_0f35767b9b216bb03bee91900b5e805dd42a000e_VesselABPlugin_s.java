 package com.vessel.abplugin;
 
 import org.apache.cordova.CallbackContext;
 import org.apache.cordova.CordovaPlugin;
import org.json.JSONObject;
 import org.json.JSONArray;
 import org.json.JSONException;
 
import android.app.Activity;
 import android.util.Log;
import android.content.Intent;
 
 import com.vessel.VesselSDK;
 import com.vessel.VesselAB;
 import com.vessel.enums.VesselEnums.TestVariation;
 import com.vessel.interfaces.ABListener;
 
 
 public class VesselABPlugin extends CordovaPlugin {
     CallbackContext callbackContext;
 
     @Override
     public boolean execute(String action, JSONArray args, CallbackContext _callbackContext) throws JSONException {
         try {
             callbackContext = _callbackContext;
             Log.d("VesselSDK", "Action "+action);
 
             if ("initialize".equals(action)) {
                String SECRET_KEY = args.getString(0);
 
                VesselSDK.initialize(cordova.getActivity().getApplicationContext(), SECRET_KEY);
                callbackContext.success();
                return true;
 
             }else if ("reloadTest".equals(action)) {
                 VesselAB.reloadTest();
                 callbackContext.success();
                 return true;
             } else if ("pause".equals(action)) {
                 VesselAB.onPause();
                 callbackContext.success();
                 return true;
             } else if ("resume".equals(action)) {
                 VesselAB.onResume();
                 callbackContext.success();
                 return true;
             } else if ("startSession".equals(action)) {
                 String sessionName = args.getString(0);
                 VesselAB.startSession(sessionName);
                 callbackContext.success();
                 return true;
             } else if ("endSession".equals(action)) {
                 String sessionName = args.getString(0);
                 VesselAB.endSession(sessionName);
                 callbackContext.success();
                 return true;
             }else if ("checkPointVisited".equals(action)) {
                 String checkpointName = args.getString(0);
                 VesselAB.checkPointVisited(checkpointName);
                 callbackContext.success();
                 return true;
             }else if ("setABListener".equals(action)) {
                 this.cordova.setActivityResultCallback(this);
                 this.cordova.getThreadPool().execute(new Runnable() {
 
                     @Override
                     public void run() {
                        VesselAB.setABListener(new ABListener() {
 
                             @Override
                            public void testNotAvailable(TestVariation arg0) {
                                callbackContext.success(arg0.toString());
                             }
 
                             @Override
                             public void testLoading() {
                             }
 
                             @Override
                            public void testLoaded(String testName, TestVariation testVariation) {
                                 callbackContext.success(testVariation.toString());

                             }
                         });
 
                     }
                 });
                 return true;
             }else if("getValue".equals(action)){
                 String key = args.getString(0);
                 String defaultValue = args.getString(1);
                 String value = VesselAB.getValue(key, defaultValue);
                 callbackContext.success(value);
                 return true;
             }
             return false;
         } catch(Exception e) {
             e.printStackTrace();
             callbackContext.error(e.getMessage());
             return false;
         } 
     }
 }
