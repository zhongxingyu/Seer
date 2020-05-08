 package de.electricdynamite.pasty;
 
 /*
  *    Copyright 2012 Philipp Geschke
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import org.json.JSONArray;
 import org.json.JSONException;
import org.json.JSONObject;

 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.support.v4.content.AsyncTaskLoader;
 import android.util.Log;
 
 public class PastyLoader extends AsyncTaskLoader<PastyLoader.PastyResponse> {
     private static final String TAG = PastyLoader.class.getName();
     
     // Delta to determine if our cached response is old
 	private static final String CACHEFILE = "ClipboardCache.json";
 
 	private PastyClient client;
 	private PastyPreferencesProvider prefs;
 	private Context context;
 	private ConnectivityManager mConnMgr;
 	
 	private boolean isOnline = false;
 	private boolean firstLoad = true;
 	private boolean permitCache = true;
     
     public static final int TASK_CLIPBOARD_FETCH = 0xA1;
     public static final int TASK_ITEM_ADD = 0xB1;
     public static final int TASK_ITEM_DELETE = 0xB2;
     
     public static final Boolean CACHE_PERMITTED = true;
     public static final Boolean CACHE_DENIED = false;
     
     private int taskId = 0x0;
     
     
     
     public static class PastyResponse {
         private JSONArray mClipboard;
         private PastyException mException;
         private int resultSource;
         public boolean isFinal = false;
         public static final int SOURCE_MEM = 0x1;
         public static final int SOURCE_CACHE = 0x2;
         public static final int SOURCE_NETWORK = 0x3;
 		public boolean hasException = false;
 
 		public PastyResponse() {
         }
         
         public PastyResponse(JSONArray clipboard, int resultSource, boolean isFinal) {
         	this.mClipboard = clipboard;
         	this.resultSource = resultSource;
         	this.isFinal = isFinal;
         }
         
         public PastyResponse(JSONArray clipboard, int resultSource) {
         	this.mClipboard = clipboard;
         	this.resultSource = resultSource;
         }
         
         public PastyResponse(PastyException e) {
         	this.mException = e;
         	this.hasException  = true;
         }
         
         public JSONArray getClipboard() {
             return mClipboard;
         }
         
         public int getResultSource() {
             return resultSource;
         }
         
         public PastyException getException() {
         	return this.mException;
         }
     }
     
 	private PastyResponse mCachePastyResponse;
     
         
     public PastyLoader(Context context, int taskId, Bundle args) {
         super(context);
         this.context = context;
     	// Restore preferences
    	 	this.prefs = new PastyPreferencesProvider(context);
         // Create a PastyClient
     	client = new PastyClient(prefs.getRESTBaseURL(), true);
     	client.setUsername(prefs.getUsername());
     	client.setPassword(prefs.getPassword());
         this.taskId = taskId;
         this.permitCache = args.getBoolean("permitCache", PastyLoader.CACHE_PERMITTED);
     }
 
 	@Override
     public PastyResponse loadInBackground() {
 		if (PastySharedStatics.devMode) Log.d(TAG, "loadInBackground() called");
         try {
             // At the very least we always need an action.
             if (taskId == 0x0) {
                 Log.e(TAG, "No taskId provided.");
                 return new PastyResponse(); 
             }
             switch(taskId) {
             case TASK_CLIPBOARD_FETCH:
             	JSONArray clipboard = client.getClipboard();
             	cacheClipboard(clipboard);
         		if (PastySharedStatics.devMode) Log.d(TAG, "Delivered result from network");
             	return new PastyResponse(clipboard, PastyResponse.SOURCE_NETWORK, true);
             case TASK_ITEM_ADD:
             	break;
             case TASK_ITEM_DELETE:
             	break;
             default:
             	return new PastyResponse();
             }            
             // Request was null if we get here, so let's just send our empty RESTResponse like usual.
             return new PastyResponse();
         } 
         catch (PastyException e) {
         	return new PastyResponse(e);
         }
     }
     
 	@Override
     public void deliverResult(PastyResponse response) {
 		// Store our PastyResponse as cached result
 		mCachePastyResponse = response;
         super.deliverResult(response);
     }
     
     @Override
     protected void onStartLoading() {
     	if(mConnMgr == null) {
     		mConnMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE); 
     	}		
     	NetworkInfo networkInfo = mConnMgr.getActiveNetworkInfo();
     	if (networkInfo != null && networkInfo.isConnected()) {
     		this.isOnline = true;
     	} else {
     		this.isOnline = false;
     	}
     	networkInfo = null;
     	if (mCachePastyResponse != null && permitCache) {
     		// Instantly return a cached version
     		if (PastySharedStatics.devMode) Log.d(TAG, "Delivered result from memory");
     		super.deliverResult(mCachePastyResponse);
     	} else if(firstLoad && permitCache) {
     		JSONArray jsonCache = getCachedClipboard();
     		if(jsonCache != null) {
     			// Got clipboard from device cache
     			if (PastySharedStatics.devMode) Log.d(TAG, "Delivered result from cache");
         		if(!isOnline) {
         			deliverResult(new PastyResponse(jsonCache, PastyResponse.SOURCE_CACHE, true));
         		}
     			deliverResult(new PastyResponse(jsonCache, PastyResponse.SOURCE_CACHE));	
     			firstLoad = false;
     		} else {
     			if(!isOnline) {
     				PastyException e = new PastyException(PastyException.ERROR_NO_CACHE_EXCEPTION);
         			deliverResult(new PastyResponse(e));    				
     			}
     		}
     	}
     	
     	if(isOnline) {
     		forceLoad();
     	}
     }
     
     @Override
     protected void onStopLoading() {
         // This prevents the AsyncTask backing this
         // loader from completing if it is currently running.
         cancelLoad();
     }
     
     @Override
     protected void onReset() {
         super.onReset();
         
         // Stop the Loader if it is currently running.
         onStopLoading();
         
         // Reset cache
         mCachePastyResponse = null;
     }
     
     protected JSONArray getCachedClipboard() {
     	File mDeviceCacheFile = new File(
                 context.getCacheDir(), CACHEFILE);
 
         try {
         	BufferedReader reader = new BufferedReader(new FileReader(mDeviceCacheFile));
             String line, results = "";
             while( ( line = reader.readLine() ) != null)
             {
                 results += line;
             }
             reader.close();
 			JSONArray jsonCache;
 			try {
 				jsonCache = new JSONArray(results);
 				return jsonCache;
 			} catch (JSONException e) {
 				Log.e(TAG, "getCachedClipboard(): Invalid JSON in cache file");
 			}
 
         } catch (IOException e) {
         	Log.i(TAG, "getCachedClipboard(): Cache file not existing");
         } 
         return null;
     }
     
     private void cacheClipboard(JSONArray clipboard) {
     	File mDeviceCacheFile = new File(
                 context.getCacheDir(), CACHEFILE);
 
         try {
         	mDeviceCacheFile.createNewFile();
             FileWriter fw = new FileWriter(mDeviceCacheFile);
             BufferedWriter bw = new BufferedWriter(fw);
             bw.write(clipboard.toString());
             bw.newLine();
             bw.close();
             if (PastySharedStatics.devMode) Log.d(TAG, "Saved result to cache");
 
         } catch (IOException e) {
         	Log.e(TAG, "cacheClipboard(): Could not create cache file");
         }
     }
 }
