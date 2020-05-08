 // 
 //  WaveService.java
 //  AndroidWaveProject
 //  
 //  Created by Philip Kuryloski on 2010-12-15.
 //  Copyright 2010 University of California, Berkeley. All rights reserved.
 // 
 
 package edu.berkeley.androidwave.waveservice;
 
 import edu.berkeley.androidwave.waveclient.*;
 import edu.berkeley.androidwave.waveexception.WaveRecipeNotCachedException;
 import edu.berkeley.androidwave.waveexception.SensorNotAvailableException;
 import edu.berkeley.androidwave.waveservice.sensorengine.SensorEngine;
 import edu.berkeley.androidwave.waveservice.sensorengine.WaveRecipeOutputListener;
 
 import android.app.Service;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Binder;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.util.Log;
 import java.io.*;
 import java.security.DigestOutputStream;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.*;
 import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.StatusLine;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import edu.berkeley.androidwave.waverecipe.*;
 
 /**
  * WaveService
  * 
  * This is the primary Wave component, the Wave Service.  It encapsulates all
  * of the main functionality of the wave system, managing sensors, recipes,
  * and synchronization.
  *
  * It provides two primary interfaces, a public interface via AIDL for Wave
  * Client applications, and a private local interface for the WaveUI
  * Application.
  */
 public class WaveService extends Service implements WaveRecipeOutputListener {
     
     private static final String TAG = WaveService.class.getSimpleName();
     
     public static final String ACTION_WAVE_SERVICE = "edu.berkeley.androidwave.intent.action.WAVE_SERVICE";
     public static final String ACTION_REQUEST_RECIPE_AUTHORIZE = "edu.berkeley.androidwave.intent.action.AUTHORIZE";
     public static final String RECIPE_ID_EXTRA = "recipe_id";
     public static final String CLIENT_KEY_EXTRA = "client_key";
     public static final String WAVERECIPE_CACHE_DIR = "waverecipes/cache";
     
     protected RecipeDbHelper databaseHelper;
     
     // Name in these maps refers to the client's package name
     protected Map<String, String> clientKeyNameMap;
     protected Map<String, String> clientNameKeyMap;
     protected ArrayList<WaveRecipeAuthorization> authorizations;
     
     // The SensorEngine
     protected SensorEngine sensorEngine;
     protected Map<WaveRecipeAuthorization, IWaveRecipeOutputDataListener> listenerMap;
     
     private DownloadRecipeTask downloadRecipeTask;
     
     protected void throwNotImplemented() {
         String className = getClass().getName();
         String methodName = Thread.currentThread().getStackTrace()[3].getMethodName();
         throw new RuntimeException(className+"#"+methodName+" not implemented yet!");
     }
     
     @Override
     public void onCreate() {
         Log.d(TAG, "onCreate()");
         
         try {
             // get the SensorEngine
             SensorEngine.init(this);
             sensorEngine = SensorEngine.getInstance();
             listenerMap = new HashMap<WaveRecipeAuthorization, IWaveRecipeOutputDataListener>();
             
             /**
              * open up our sqlite database and restore the saved authorizations
              */
             databaseHelper = new RecipeDbHelper(this);
         
             // load the client auth map (and create the inverse map)
             clientKeyNameMap = databaseHelper.loadClientKeyNameMap();
             clientNameKeyMap = new HashMap<String, String>();
             for (Map.Entry<String, String> entry : clientKeyNameMap.entrySet()) {
                 clientNameKeyMap.put(entry.getValue(), entry.getKey());
             }
         
             authorizations = databaseHelper.loadAuthorizations(this); // the waveservice is passed so that we can load recipes
         } catch (Exception e) {
             Log.d(TAG, "Exception encountered in onCreate()", e);
         }
     }
     
     @Override
     public IBinder onBind(Intent intent) {
         if (intent.getAction().equals(ACTION_WAVE_SERVICE)) {
             return mPublicBinder;
         }
         if (intent.getAction().equals(Intent.ACTION_MAIN)) {
             return mUIBinder;
         }
         return null;
     }
     
     /**
      * The UI service is a local binding only as there is only one UI app
      */
     private final IBinder mUIBinder = new LocalBinder();
     
     /**
      * The associated local binder
      */
     public class LocalBinder extends Binder {
         public WaveService getService() {
             return WaveService.this;
         }
     }
     
     @Override
     public void onDestroy() {
         // the databaseHelper may not be set up if the service doesn't fully start up
         if (databaseHelper != null) {
             databaseHelper.closeDatabase();
         }
     }
     
     /**
      * UI (LOCAL) METHODS
      */
     
     /**
     * recipeAuthorizations
      * 
     * The list of authorizations outstanding
     * 
     * @see WaveRecipeAuthorization
      */
    public List<WaveRecipeAuthorization> recipeAuthorizations() {
         return authorizations;
     }
     
     
     /**
      * DownloadRecipeTask private inner class
      * 
      * an AsyncTask subclass, used for downloading a recipe on a background
      * thread
      */
     private class DownloadRecipeTask extends AsyncTask<Object, Void, File> {
         protected RecipeRetrievalResponder rrResponder;
         protected String recipeId;
         
         private String statusText;
         
         protected File doInBackground(Object... args) {
             rrResponder = (RecipeRetrievalResponder) args[0];
             recipeId = (String) args[1];
             String path = (String) args[2];
             File file = (File) args[3];
             
             Log.d(TAG, "About to download recipe from "+path);
             
             try {
                 HttpGet get = new HttpGet(path);
                 HttpResponse response = new DefaultHttpClient().execute(get);
                 HttpEntity entity = response.getEntity();
                 
                 // log the headers
                 Log.d(TAG, "Response Details:");
                 Header[] allHeaders = response.getAllHeaders();
                 for (int i=0; i<allHeaders.length; i++) {
                     Header h = allHeaders[i];
                     if (h.getName().equals("Content-Type")) {
                         if (!h.getValue().equals("text/plain")) {
                             Log.w(TAG, "\tContent-Type is not text/plain, check that your internet connection is properly configured");
                         }
                     }
                     Log.d(TAG, "\t"+h.getName()+": "+h.getValue());
                 }
                 
                 StatusLine statusLine = response.getStatusLine();
                 Log.d(TAG, "\t\trequest complete, status => "+statusLine.getReasonPhrase());
                 if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                     FileOutputStream fos = new FileOutputStream(file);
                     MessageDigest md = MessageDigest.getInstance("SHA1");
                     DigestOutputStream dos = new DigestOutputStream(fos, md);
                     entity.writeTo(fos);
                     dos.close();
                     fos.close();
                     Log.d(TAG, "\t\tdownloaded file has SHA1 "+byteArray2Hex(dos.getMessageDigest().digest()));
                     entity.consumeContent();
                 }
                 statusText = statusLine.getReasonPhrase();
             } catch (NoSuchAlgorithmException nsae) {
                 Log.d(TAG, "NoSuchAlgorithmException during recipe download", nsae);
                 statusText = "NoSuchAlgorithmException";
             } catch (FileNotFoundException fnfe) {
                 Log.d(TAG, "FileNotFoundException during recipe download", fnfe);
                 statusText = "FileNotFoundException";
             } catch (IOException ioe) {
                 Log.d(TAG, "IOException during recipe download", ioe);
                 statusText = "Could not connect to server";
             }
             
             return file;
         }
         
         protected void onPostExecute(File result) {
             if (result.exists()) {
                 rrResponder.handleRetrievalFinished(recipeId, result);
             } else {
                 rrResponder.handleRetrievalFailed(recipeId, statusText);
             }
         }
         
         protected void onCancelled(File result) {
             rrResponder.handleRetrievalFailed(recipeId, "Cancelled by user.");
         }
     }
     
     /**
      * beginRetrieveRecipeForID
      * 
      * this should contact a recipe server, validate (jar has valid sig only)
      * and cache it
      * TODO: store recipe server url in app preferences
      * TODO: clean up the AsyncTask args
      */
     public void beginRetrieveRecipeForId(String id, RecipeRetrievalResponder r) {
         
         String recipeHost = "http://moteserver1.eecs.berkeley.edu/recipes/";
         
         String recipePath = recipeHost + id;
         
         File cacheFile = recipeCacheFileForId(id);
         
         // save the task so it can be cancelled
         downloadRecipeTask = new DownloadRecipeTask();
         downloadRecipeTask.execute(r, id, recipePath, cacheFile);
     }
     
     /**
      * cancelRetrieveRecipeForId
      */
     public void cancelRetrieveRecipeForId(String id) {
         downloadRecipeTask.cancel(true);
     }
     
     private boolean clearRecipeCache() {
         Log.d(TAG, "clearRecipeCache()");
         
         String[] c = WAVERECIPE_CACHE_DIR.split(File.separator, 2);
         // create the storage directory
         File filesDir = getDir(c[0], Context.MODE_PRIVATE);
         // make necessary children directories
         File parent;
         if (c.length > 1) {
             parent = new File(filesDir, c[1]);
             parent.mkdirs();
         } else {
             parent = filesDir;
         }
         assert parent.exists() : parent;
         assert parent.isDirectory() : parent;
         
         // now empty the parent
         return deleteRecursive(parent);
     }
     
     private boolean deleteRecursive(File f) {
         if (f.isDirectory()) {
             boolean deletedAll = true;
             for (File child : f.listFiles()) {
                 deletedAll &= deleteRecursive(child);
             }
             return deletedAll;
         } else {
             return f.delete();
         }
     }
     
     /**
      * recipeCacheFileForId
      */
     public File recipeCacheFileForId(String id) {
         String[] c = WAVERECIPE_CACHE_DIR.split(File.separator, 2);
         // create the storage directory
         File filesDir = getDir(c[0], Context.MODE_PRIVATE);
         // make necessary children directories
         File parent;
         if (c.length > 1) {
             parent = new File(filesDir, c[1]);
             parent.mkdirs();
         } else {
             parent = filesDir;
         }
         assert parent.exists() : parent;
         
         File recipeFile = new File(parent, id+".waverecipe");
         return recipeFile;
     }
     
     /**
      * get a recipe by its ID.  In this form, a recipe is delivered
      * immediately, or an exception is thrown
      */
     protected WaveRecipe getRecipeForId(String id)
             throws Exception {
         File f = recipeCacheFileForId(id);
         if (!f.exists()) {
             throw new WaveRecipeNotCachedException(id);
         }
         return WaveRecipe.createFromDisk(this, f);
     }
     
     /**
      * permitClientNameKeyPair
      */
     public synchronized boolean permitClientNameKeyPair(String packageName, String key) {
         // first, see if we have a stored key for that packageName
         if (clientKeyNameMap.containsValue(packageName)) {
             Log.d(TAG, "Checking key for previously registered package "+packageName);
             // see if the proposed key is correct
             if (clientKeyNameMap.containsKey(key) && clientKeyNameMap.get(key).equals(packageName)) {
                 return true;
             } else {
                 return false;
             }
         } else if (clientKeyNameMap.containsKey(key)) {
             Log.d(TAG, ""+packageName+" attempted use of another package's key. Removing key.");
             // we must make sure the secret key is not already in use. If a
             // conflict is detected, this means that a new client randomly
             // chose a key matching that of another client.  The safest thing
             // to do is to revoke the existing entry and fail the new one,
             // otherwise the new client would know it discovered a secret key.
             // another possibly would be to make the stored key a secure hash
             // of the client name and it's key, to avoid collisions of this
             // sort.
             if (!databaseHelper.removeClientKeyEntry(key)) {
                 Log.w(TAG, "Failed to remove conflicting client key from database");
             }
             clientKeyNameMap.remove(key);
         } else if (databaseHelper.storeClientKeyNameEntry(key, packageName)) {
             Log.w(TAG, "Storing new key for "+packageName);
             clientKeyNameMap.put(key, packageName);
             return true;
         }
         return false;
     }
     
     /**
      * saveAuthorization
      * 
      * save an authorization.  Note that we don't check the validity of the
      * client key, because this is a private interface method.
      */
     public synchronized boolean saveAuthorization(WaveRecipeAuthorization auth) {
         try {
             if (databaseHelper.insertOrUpdateAuthorization(auth)) {
                 if (!authorizations.contains(auth)) {
                     authorizations.add(auth);
                 }
                 Date now = new Date();
                 if (!auth.validForDate(now)) {
                     // this is a deauthorization
                     boolean didUnschedule = sensorEngine.descheduleAuthorization(auth);
                     Log.d(TAG, "sensorEngine.descheduleAuthorization("+auth+") => "+didUnschedule);
                 }
                 return true;
             }
         } catch (Exception e) {
             Log.w(TAG, "Exception raised while saving authorization", e);
         }
         return false;
     }
     
     /**
      * getAuthorization
      * 
      * Used by the ViewRecipeAuthorizationActivity to call up an authorization
      * object
      */
     public synchronized WaveRecipeAuthorization getAuthorization(String recipeId, ComponentName clientName) {
         // first look for a full name match
         for (WaveRecipeAuthorization auth : authorizations) {
             if (auth.getRecipe().getId().equals(recipeId)) {
                 if (clientName.equals(auth.getRecipeClientName())) {
                     return auth;
                 }
             }
         }
         // then look for a package match
         return getAuthorization(recipeId, clientName.getPackageName());
     }
     
     /**
      * alternate getAuthorization matching on recipeId and client package name
      */
     protected WaveRecipeAuthorization getAuthorization(String recipeId, String clientPackageName) {
         for (WaveRecipeAuthorization auth : authorizations) {
             if (auth.getRecipe().getId().equals(recipeId)) {
                 if (clientPackageName.equals(auth.getRecipeClientName().getPackageName())) {
                     return auth;
                 }
             }
         }
         return null;
     }
     
     public synchronized void reset() {
         Log.d(TAG, "begin reset()");
         
         // NOTE: resetting the listener map my result in some strang log messages
         listenerMap = new HashMap<WaveRecipeAuthorization, IWaveRecipeOutputDataListener>();
         
         this.resetDatabase();
         
         // now clear the recipe cache
         this.clearRecipeCache();
         
         Log.d(TAG, "end reset()");
     }
     
     /**
      * resets the authorization system to a clean install state.  Database
      * info and caches are wiped.
      */
     protected synchronized void resetDatabase() {
         Log.d(TAG, "begin resetDatabase()");
         
         databaseHelper.emptyDatabase();
         
         clientKeyNameMap = new HashMap<String, String>();
         clientNameKeyMap = new HashMap<String, String>();
         authorizations = new ArrayList<WaveRecipeAuthorization>();
         
         Log.d(TAG, "end resetDatabase()");
     }
     
     
     /**
      * WaveRecipeOutputListener methods
      */
     public void receiveDataForAuthorization(long time, Map<String, Double> values, WaveRecipeAuthorization authorization) {
         // forward the data through IPC to the listener
         IWaveRecipeOutputDataListener destination = listenerMap.get(authorization);
         // TODO: spawn a thread to write data to the listener (although maybe we don't need to, as ipc already goes through its own thread)
         if (destination != null) {
             try {
                 // repackage the WaveRecipeOutputData as a ParcelableWaveRecipeOutputData
                 // Log.v(TAG, "receiveDataForAuthorization: data => "+data);
                 ParcelableWaveRecipeOutputData dataImpl = new ParcelableWaveRecipeOutputData(time, values);
                 destination.receiveWaveRecipeOutputData(dataImpl);
             } catch (RemoteException re) {
                 Log.d(TAG, "RemoteException in receiveDataForAuthorization, connection to client must have been dropped.", re);
                 boolean didUnschedule = sensorEngine.descheduleAuthorization(authorization);
                 Log.d(TAG, "sensorEngine.descheduleAuthorization("+authorization+") => "+didUnschedule);
             } catch (Exception e) {
                 Log.d(TAG, "Exception in receiveDataForAuthorization("+time+", "+values+", "+authorization+")");
             }
         } else {
             Log.d(TAG, "could not look up destination in receiveDataForAuthorization");
             boolean didUnschedule = sensorEngine.descheduleAuthorization(authorization);
             Log.d(TAG, "sensorEngine.descheduleAuthorization("+authorization+") => "+didUnschedule);
         }
     }
     
     
     /**
      * WAVESERVICE PUBLIC METHODS
      * 
      * @see IWaveServicePublic
      */
     private final IWaveServicePublic.Stub mPublicBinder = new IWaveServicePublic.Stub() {
         
         /**
          * NOTE: These methods have not been refactored, because it is an
          * apparent limit of AIDL such that we cannot call methods from
          * outside of this inner class (it might also be possible that we
          * cannot even call one method from another inside of this Stub()
          * implementation).
          */
         
         /**
          * isAuthorized
          */
         public boolean isAuthorized(String key, String recipeId) {
             // Log.d(TAG, "mPublicBinder.isAuthorized(\""+key+"\", \""+recipeId+"\")");
             
             try {
                 // check the validity of the key
                 if (clientKeyNameMap.containsKey(key)) {
                     // recall the package name for the given key
                     // this is a valid key
                     String clientPackageName = clientKeyNameMap.get(key);
                 
                     for (WaveRecipeAuthorization auth : authorizations) {
                         if (auth.getRecipe().getId().equals(recipeId)) {
                             if (clientPackageName.equals(auth.getRecipeClientName().getPackageName())) {
                                 if (auth.validForDate(new Date())) {
                                     return true;
                                 } else {
                                     Log.d(TAG, "isAuthorized called for revoked recipe (for recipeId="+recipeId+")");
                                 }
                             }
                         }
                     }
                 } else {
                     Log.d(TAG, "isAuthorized called using invalid key (for recipeId="+recipeId+")");
                 }
             } catch (Exception e) {
                 Log.d(TAG, "Exception in isAuthorized", e);
                 throw new RuntimeException(e);
             }
             return false;
         }
 
         /**
          * retrieveAuthorization
          */
         public WaveRecipeAuthorizationInfo retrieveAuthorizationInfo(String key, String recipeId) {
             // Log.d(TAG, "mPublicBinder.retrieveAuthorizationInfo(\""+key+"\", \""+recipeId+"\")");
 
             try {
                 // check the validity of the key
                 if (clientKeyNameMap.containsKey(key)) {
                     // recall the package name for the given key
                     // this is a valid key
                     String clientPackageName = clientKeyNameMap.get(key);
 
                     for (WaveRecipeAuthorization auth : authorizations) {
                         if (auth.getRecipe().getId().equals(recipeId)) {
                             if (clientPackageName.equals(auth.getRecipeClientName().getPackageName())) {
                                 if (auth.validForDate(new Date())) {
                                     return auth.asInfo();
                                 } else {
                                     Log.d(TAG, "retrieveAuthorizationInfo called for revoked recipe (for recipeId="+recipeId+")");
                                 }
                             }
                         }
                     }
                 } else {
                     Log.d(TAG, "retrieveAuthorizationInfo called using invalid key (for recipeId="+recipeId+")");
                 }
             } catch (Exception e) {
                 Log.d(TAG, "Exception in retrieveAuthorizationInfo", e);
                 throw new RuntimeException(e);
             }
             return null;
         }
 
         /**
          * getAuthorizationIntent
          *
          * TODO: avoid the use of the key, and determine the caller of the IPC dynamically
          */
         public Intent getAuthorizationIntent(String recipeId, String key) {
             // Log.d(TAG, "mPublicBinder.getAuthorizationIntent(\""+recipeId+"\", \""+key+"\")");
 
             Intent authIntent = new Intent(ACTION_REQUEST_RECIPE_AUTHORIZE);
             authIntent.putExtra(RECIPE_ID_EXTRA, recipeId);
             authIntent.putExtra(CLIENT_KEY_EXTRA, key);
             return authIntent;
         }
 
         /**
          * registerRecipeOutputListener
          */
         public boolean registerRecipeOutputListener(String key, String recipeId, IWaveRecipeOutputDataListener listener) {
             /**
              * this means we need to merge the authorization for this recipeId
              * and client into the scheduled sampling of sensors
              * we need to then make sure that the recipe is calculated at the
              * appropriate interval to fulfill this recipe
              * 
              * The current recipe format is such that we should call
              * ingestSensorData on it at the authorized rate (or lower).
              * It is up to the recipe to buffer and calculate. Possibly,
              * ingestSensorData should provide timestamped data to aid the
              * recipe.  The recipe reports output as necessary, which then
              * feeds through to the listener set here.  To keep the recipe
              * from blocking, we should invoke ingestSensorData from a
              * separate thread.  It should be up to the recipe to respond
              * appropriately.
              * 
              * So, what this means, is that we need to schedule the underlying
              * sensor at a rate sufficient for the recipe+authorizations that
              * require it, and make sure that ingestSensorData is called on
              * the appropriate recipe algorithm instance (one instance for
              * each authorization), invoked in separate threads.  We also need
              * to link the callbacks up to the listener here.  This method
              * more or less instantiates an instance of the authenticates the
              * call to the sensor engine.
              */
             
             try {
                 // check the validity of the key
                 if (clientKeyNameMap.containsKey(key)) {
                     // recall the package name for the given key
                     // this is a valid key
                     String clientPackageName = clientKeyNameMap.get(key);
                     // look up the authorization
                     for (WaveRecipeAuthorization auth : authorizations) {
                         if (auth.getRecipe().getId().equals(recipeId)) {
                             if (clientPackageName.equals(auth.getRecipeClientName().getPackageName())) {
                                 if (auth.validForDate(new Date())) {
                                     // authorization was found and is valid
                                     // check if already registered
                                     if (listenerMap.containsKey(auth)) {
                                         // listener already registered
                                         Log.d(TAG, "registerRecipeOutputListener called when already registered registered (for recipeId="+recipeId+")");
                                         return false;
                                     } else {
                                         listenerMap.put(auth, listener);
                                         // Warning: passing WaveService.this could possibly mess with AIDL.  Not sure right now.
                                         boolean didSchedule;
                                         try {
                                             didSchedule = sensorEngine.scheduleAuthorization(auth, WaveService.this);
                                         } catch (SensorNotAvailableException snae) {
                                             Log.d(TAG, "Exception while scheduling authorization", snae);
                                             didSchedule = false;
                                         }
                                         if (!didSchedule) {
                                             listenerMap.remove(auth);
                                         }
                                         if (didSchedule) {
                                             Log.d(TAG, "registered output listener for recipeId="+recipeId);
                                         }
                                         return didSchedule;
                                     }
                                 } else {
                                     Log.d(TAG, "registerRecipeOutputListener called for revoked recipe (for recipeId="+recipeId+")");
                                 }
                             }
                         }
                     }
                 } else {
                     Log.d(TAG, "registerRecipeOutputListener called using invalid key (for recipeId="+recipeId+")");
                 }
             } catch (Exception e) {
                 Log.d(TAG, "Exception in registerRecipeOutputListener", e);
                 throw new RuntimeException(e);
             }
             return false;
         }
 
         /**
         * unregisterRecipeOutputListener
         */
         public boolean unregisterRecipeOutputListener(String key, String recipeId) {
             try {
                 // check the validity of the key
                 if (clientKeyNameMap.containsKey(key)) {
                     // recall the package name for the given key
                     // this is a valid key
                     String clientPackageName = clientKeyNameMap.get(key);
                     // look up the authorization
                     for (WaveRecipeAuthorization auth : authorizations) {
                         if (auth.getRecipe().getId().equals(recipeId)) {
                             if (clientPackageName.equals(auth.getRecipeClientName().getPackageName())) {
                                 if (auth.validForDate(new Date())) {
                                     // authorization was found and is valid
                                     if (listenerMap.containsKey(auth)) {
                                         listenerMap.remove(auth);
                                         Log.d(TAG, "unregistered output listener for recipeId="+recipeId);
                                         return true;
                                     } else {
                                         Log.d(TAG, "unregisterRecipeOutputListener called when nothing was registered (for recipeId="+recipeId+")");
                                         return false;
                                     }
                                 } else {
                                     Log.d(TAG, "unregisterRecipeOutputListener called for revoked recipe (for recipeId="+recipeId+")");
                                 }
                             }
                         }
                     }
                 } else {
                     Log.d(TAG, "unregisterRecipeOutputListener called using invalid key (for recipeId="+recipeId+")");
                 }
             } catch (Exception e) {
                 Log.d(TAG, "Exception in unregisterRecipeOutputListener", e);
                 throw new RuntimeException(e);
             }
             return false;
         }
     };
     
     /**
      * http://www.javablogging.com/sha1-and-md5-checksums-in-java/
      */
     private static String byteArray2Hex(byte[] hash) {
         Formatter formatter = new Formatter();
         for (byte b : hash) {
             formatter.format("%02x", b);
         }
         return formatter.toString();
     }
 }
