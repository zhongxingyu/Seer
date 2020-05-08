 /*==============================================================================
             Copyright (c) 2012 QUALCOMM Austria Research Center GmbH.
             All Rights Reserved.
             Qualcomm Confidential and Proprietary
             
 @file 
     Formerly ImageTargets.java
 
 @brief
     Based off of the Sample for ImageTargets
 
 ==============================================================================*/
 /*
  * Modified by the augmented reality team of CSCI 240, University of Puget Sound
  *
  * @version 2012.11.10
  *
  * @author Matt Burke
  * 
  */
 
 package edu.pugetsound.vichar.ar;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Vector;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.pm.ActivityInfo;
 import android.content.res.Configuration;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.text.format.Time;
 // import android.support.v4.app.Fragment;
 import android.util.DisplayMetrics;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.SubMenu;
 import android.view.View;
 import android.view.WindowManager;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.graphics.Bitmap;
 import android.graphics.Matrix;
 import android.graphics.drawable.Drawable;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.RemoteException;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View.OnTouchListener;
 import android.view.animation.AlphaAnimation;
 import android.graphics.drawable.BitmapDrawable;
 //Import Fragment dependencies
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 
 import com.qualcomm.QCAR.QCAR;
 
 import edu.pugetsound.vichar.*;
 
 
 /** The main activity for the ARGameActivity. */
 public class ARGameActivity extends FragmentActivity implements OnTouchListener 
 {
     // Application status constants:
     private static final int APPSTATUS_UNINITED         = -1;
     private static final int APPSTATUS_INIT_APP         = 0;
     private static final int APPSTATUS_INIT_QCAR        = 1;
     private static final int APPSTATUS_INIT_TRACKER     = 2;    
     private static final int APPSTATUS_INIT_APP_AR      = 3;
     private static final int APPSTATUS_LOAD_TRACKER     = 4;
     private static final int APPSTATUS_INITED           = 5;
     private static final int APPSTATUS_CAMERA_STOPPED   = 6;
     private static final int APPSTATUS_CAMERA_RUNNING   = 7;
     
     // Name of the native dynamic libraries to load:
     private static final String NATIVE_LIB_AR = "VicharAR";    
     private static final String NATIVE_LIB_QCAR = "QCAR"; 
 
     // Our OpenGL view:
     private QCARSampleGLView mGlView;
     
     // The view to display the sample splash screen:
     private ImageView mSplashScreenView;
     
     // The handler and runnable for the splash screen time out task.
     private Handler mSplashScreenHandler;
     private Runnable mSplashScreenRunnable;    
     
     // The minimum time the splash screen should be visible:
     private static final long MIN_SPLASH_SCREEN_TIME = 2000;    
     
     // The time when the splash screen has become visible:
     long mSplashScreenStartTime = 0;
     
     // Our renderer:
     private ARGameRenderer mRenderer;
     
     // Display size of the device
     private int mScreenWidth = 0;
     private int mScreenHeight = 0;
     
     // The current application status
     private int mAppStatus = APPSTATUS_UNINITED;
     
     // The async tasks to initialize the QCAR SDK 
     private InitQCARTask mInitQCARTask;
     private LoadTrackerTask mLoadTrackerTask;
 
     // An object used for synchronizing QCAR initialization, dataset loading and
     // the Android onDestroy() life cycle event. If the application is destroyed
     // while a data set is still being loaded, then we wait for the loading
     // operation to finish before shutting down QCAR.
     private Object mShutdownLock = new Object();   
     
     // QCAR initialization flags
     private int mQCARFlags = 0;
     
     // The textures we will use for rendering:
     private Vector<Texture> mTextures;
     private int mSplashScreenImageResource = 0;
     
     // The menu item for swapping data sets:
     MenuItem mDataSetMenuItem = null;
     boolean mIsStonesAndChipsDataSetActive  = false;
     
     //JSON namespaces
     private static final String GAME_ENGINE_NAMESPACE = "engine";
     private static final String DEVICES_NAMESPACE = "phones";
     private static final String WEB_NAMESPACE = "web";
     private static final String TURRET_NAMESPACE = "turrets";
     private static final String TURRETBULLET_NAMESPACE = "turretsBullets";
     private static final String FIREBALL_NAMESPACE = "fireballs";
     private static final String MINION_NAMESPACE = "minions";
     private static final String BATTERY_NAMESPACE = "batteries";
     private static final String PLAYER_NAMESPACE = "player";
     private static final String EYEBALL_NAMESPACE = "eyeballs";
     private static final String PLATFORM_NAMESPACE = "platforms";
     private static final String POSITION_NAMESPACE = "position";
     private static final String ROTATION_NAMESPACE = "rotation";
     private String deviceUUID; // Device namespace
     
     //JSON parsing
     public static float[] poseData = new float[70];
     public static boolean updated = false;
     public static final int OBJ_SIZE = 7; 	// the number of array positions to use to represent a game object.
     private int arrayLen = 70;
     
     	
 	// Service Stuff
     private Messenger networkingServiceMessenger = null;
     boolean isBoundToNetworkingService = false;
     private boolean isConnectedToGameServer = false;
     final Messenger mMessenger = new Messenger(new IncomingHandler());
     
     // Twitter
     private boolean activeTwitter = false;
     private float touchTwX;
     private int actionUp = 0;
     private TweetFragment twFrag;
     private View gui;
     
     private static final double MAX_EYELID_TO_SCREEN_RATIO = .25;
     
     private float touchX, touchY;
     
     /** Static initializer block to load native libraries on start-up. */
     static
     {
         loadLibrary(NATIVE_LIB_QCAR);
         loadLibrary(NATIVE_LIB_AR);
     }
     
     /** An async task to initialize QCAR asynchronously. */
     private class InitQCARTask extends AsyncTask<Void, Integer, Boolean>
     {   
         // Initialize with invalid value
         private int mProgressValue = -1;
         
         protected Boolean doInBackground(Void... params)
         {
             // Prevent the onDestroy() method to overlap with initialization:
             synchronized (mShutdownLock)
             {
                 QCAR.setInitParameters(ARGameActivity.this, mQCARFlags);
                 
                 do
                 {
                     // QCAR.init() blocks until an initialization step is complete,
                     // then it proceeds to the next step and reports progress in
                     // percents (0 ... 100%)
                     // If QCAR.init() returns -1, it indicates an error.
                     // Initialization is done when progress has reached 100%.
                     mProgressValue = QCAR.init();
                     
                     // Publish the progress value:
                     publishProgress(mProgressValue);
                     
                     // We check whether the task has been canceled in the meantime
                     // (by calling AsyncTask.cancel(true))
                     // and bail out if it has, thus stopping this thread.
                     // This is necessary as the AsyncTask will run to completion
                     // regardless of the status of the component that started is.
                 } while (!isCancelled() && mProgressValue >= 0 && mProgressValue < 100);
                 
                 return (mProgressValue > 0);                
             }
         }
 
         
         protected void onProgressUpdate(Integer... values)
         {
             // Do something with the progress value "values[0]", e.g. update
             // splash screen, progress bar, etc.
         }
 
         
         protected void onPostExecute(Boolean result)
         {
             // Done initializing QCAR, proceed to next application
             // initialization status:
             if (result)
             {
                 DebugLog.LOGD("InitQCARTask::onPostExecute: QCAR initialization" +
                                                             " successful");
 
                 updateApplicationStatus(APPSTATUS_INIT_TRACKER);
             }
             else
             {
                 // Create dialog box for display error:
                 AlertDialog dialogError = new AlertDialog.Builder(ARGameActivity.this).create();
                 dialogError.setButton(
                 	DialogInterface.BUTTON_POSITIVE,
                     "Close",
                     new DialogInterface.OnClickListener()
                     {
                         public void onClick(DialogInterface dialog, int which)
                         {
                             // Exiting application
                             System.exit(1);
                         }
                     }
                 ); 
                 
                 String logMessage;
 
                 // NOTE: Check if initialization failed because the device is
                 // not supported. At this point the user should be informed
                 // with a message.
                 if (mProgressValue == QCAR.INIT_DEVICE_NOT_SUPPORTED)
                 {
                     logMessage = "Failed to initialize QCAR because this " +
                         "device is not supported.";
                 }
                 else
                 {
                     logMessage = "Failed to initialize QCAR.";
                 }
                 
                 // Log error:
                 DebugLog.LOGE("InitQCARTask::onPostExecute: " + logMessage +
                                 " Exiting.");
                 
                 // Show dialog box with error message:
                 dialogError.setMessage(logMessage);  
                 dialogError.show();
             }
         }
     }
     
     
     /** An async task to load the tracker data asynchronously. */
     private class LoadTrackerTask extends AsyncTask<Void, Integer, Boolean>
     {
         protected Boolean doInBackground(Void... params)
         {
             // Prevent the onDestroy() method to overlap:
             synchronized (mShutdownLock)
             {
                 // Load the tracker data set:
                 return (loadTrackerData() > 0);                
             }
         }
         
         protected void onPostExecute(Boolean result)
         {
             DebugLog.LOGD("LoadTrackerTask::onPostExecute: execution " +
                         (result ? "successful" : "failed"));
             
             if (result)
             {
                 // The stones and chips data set is now active:
                 mIsStonesAndChipsDataSetActive = true;
                 
                 // Done loading the tracker, update application status: 
                 updateApplicationStatus(APPSTATUS_INITED);
             }
             else
             {
                 // Create dialog box for display error:
                 AlertDialog dialogError = new AlertDialog.Builder(ARGameActivity.this).create();
                 dialogError.setButton(
                 	DialogInterface.BUTTON_POSITIVE,
                     "Close",
                     new DialogInterface.OnClickListener()
                     {
                         public void onClick(DialogInterface dialog, int which)
                         {
                             // Exiting application
                             System.exit(1);
                         }
                     }
                 ); 
                 
                 // Show dialog box with error message:
                 dialogError.setMessage("Failed to load tracker data.");  
                 dialogError.show();
             }
         }
     }
 
     private void storeScreenDimensions()
     {
         // Query display dimensions
         DisplayMetrics metrics = new DisplayMetrics();
         getWindowManager().getDefaultDisplay().getMetrics(metrics);
         mScreenWidth = metrics.widthPixels;
         mScreenHeight = metrics.heightPixels;
     }
     
     /**
      * Resizes eyelid overlay bitmaps if they are greater than the defined
      * MAX_EYELID_TO_SCREEN_RATIO. Maintains aspect ratio.
      * If the eyelid overlays do not yet exist, function fails gracefully.
      */
     private void resizeEyelids() {
     	ImageView[] eyelids = new ImageView[4];
     	eyelids[0] = (ImageView) findViewById(R.id.eyelid_top_right);
     	eyelids[1] = (ImageView) findViewById(R.id.eyelid_top_left);
     	eyelids[2] = (ImageView) findViewById(R.id.eyelid_bottom_left);
     	eyelids[3] = (ImageView) findViewById(R.id.eyelid_bottom_right);
     	for(ImageView eyelid : eyelids) {
     		if(eyelid != null) {
     			Drawable drawing = eyelid.getDrawable();
 	    		if (drawing != null) {
 	    			Bitmap bitmap = ((BitmapDrawable) drawing).getBitmap();
 	    			// Get current dimensions
 	    		    int width = bitmap.getWidth();
 	    		    int height = bitmap.getHeight();
 	    		        	    
 	    		    // Determine how much to scale:
 	    		    if(width > MAX_EYELID_TO_SCREEN_RATIO * mScreenWidth
 	    		    	|| height > MAX_EYELID_TO_SCREEN_RATIO * mScreenHeight) 
 	    		    {
 	    		    	double aspectRatio = width / height;
 	    		    	float scale = 1.0f;
 	    		    	if(width > height) {
 	    		    		int newWidth = (int) Math.floor(MAX_EYELID_TO_SCREEN_RATIO * mScreenWidth);
 	    		    		scale = newWidth / ((float) width);
 	    		    	} else {
 	    		    		int newHeight = (int) Math.floor(MAX_EYELID_TO_SCREEN_RATIO * mScreenHeight);
 	    		    		scale = newHeight / ((float) height);
 	    		    	}
 	    		    	
 	    		    	// Create a matrix for the scaling and add the scaling data
 	    		        Matrix matrix = new Matrix();
 	    		        matrix.postScale(scale, scale);
 
 	    		        // Create a new bitmap and convert it to a format understood by the ImageView 
 	    		        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
 	    		        width = scaledBitmap.getWidth(); // re-use
 	    		        height = scaledBitmap.getHeight(); // re-use
 	    		        BitmapDrawable result 
 	    		        	= new BitmapDrawable(getResources(), scaledBitmap);
 	    		      	    		        
 	    		        // Apply the scaled bitmap
 	    		        eyelid.setImageDrawable(result);
 	    		    	
 	    		    }
 	    		} else {
 	    			Log.i("", "Cant get eyelid drawable");
 	    		}
     		} else {
     			Log.i("", "No eyelids!!");
     		}
     	}
     }
 
     
     /** Called when the activity first starts or the user navigates back
      * to an activity. 
      */
     protected void onCreate(Bundle savedInstanceState)
     {
     	DebugLog.LOGD("ARGameActivity::onCreate");
     	super.onCreate(savedInstanceState);
 
     	
     	// Get the UUID we generated when this app was installed
     	deviceUUID = Installation.id(this);
 
     	//the whole screen becomes sensitive to touch
 //    	View gameContainer = (View) findViewById(R.id.game_container);
 //    	gameContainer.setOnTouchListener(this);
     	
         // Set the splash screen image to display during initialization:
     	mSplashScreenImageResource = edu.pugetsound.vichar.R.drawable.splash;
 
     	// Load any sample specific textures:  
     	mTextures = new Vector<Texture>();
     	loadTextures();
 
     	// Query the QCAR initialization flags:
     	mQCARFlags = getInitializationFlags();
 
     	//inflate ui elements
         LayoutInflater inflater = getLayoutInflater();
         gui = inflater.inflate(R.layout.activity_ar, null); 
         Log.d("UI", "inflated ui");
         
     	// Update the application status to start initializing application
     	updateApplicationStatus(APPSTATUS_INIT_APP);    	
     }
     
     private OnTouchListener tweetHandleListener = new OnTouchListener() {
 		public boolean onTouch(View v, MotionEvent me) { 
 			return tweetContainerTouch(v, me);
 		}
        };
     
     private boolean tweetContainerTouch(View v, MotionEvent me)  {
     	View tweetContainer = (View) findViewById(edu.pugetsound.vichar.R.id.tweet_container);
     	//check type of touch action
     	switch (me.getAction()) {
 			case MotionEvent.ACTION_DOWN:
 				actionUp=0;
 				touchTwX = me.getRawX();
 				System.out.println("action down at " + touchTwX);       	
 			case MotionEvent.ACTION_MOVE:
 				actionUp=0;				
 				System.out.println("motion event x " + me.getRawX());				
 				//only execute changes if user hasn't dragged too far right
 				if(me.getRawX()<tweetContainer.getWidth()) {   
 					//calculate motion change
 					float delta = me.getRawX() - touchTwX;  
 					System.out.println("Delta " + delta);
 					touchTwX = me.getRawX();
 	   		
 					//calculate new x coordinate of view
 					System.out.println("right location " + tweetContainer.getRight());
 					//set new params
 					FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) tweetContainer.getLayoutParams();
 					params.leftMargin = params.leftMargin + (int) delta;
 					tweetContainer.setLayoutParams(params);  
 				}
 			case MotionEvent.ACTION_UP:
 				actionUp++;
 				//if this is actually an up action...
 				if(actionUp==2) {
 					System.out.println("------ACTION UP OR CANCEL------");	
 					//if user has dragged half distance of tweet container, snap onscreen
 					if(tweetContainer.getRight() > tweetContainer.getWidth()/2) {
 						snapTwitterOn();
 					//otherwise snap back to beginning
 					} else {		        			
 						 snapTwitterOff();       		
 					}
 				}   		        		
    			}			
         return true;
     }
     
     /**
      * Snaps twitter container onto screen
      * TODO:clear this up. method shouldn't be public, but needs to be accessed from fragment
      */
     public void snapTwitterOn() {
     	View tweetContainer = (View) findViewById(edu.pugetsound.vichar.R.id.tweet_container);
     	FrameLayout.LayoutParams paramsSuccess = (FrameLayout.LayoutParams) tweetContainer.getLayoutParams();
 		paramsSuccess.leftMargin = 0;
 		tweetContainer.setLayoutParams(paramsSuccess);  
     }
     
     /**
      * Snap twitter container off screen
      * TODO:clear this up. method shouldn't be public, but needs to be accessed from fragment
      */
     public void snapTwitterOff()  {
     	View tweetContainer = (View) findViewById(edu.pugetsound.vichar.R.id.tweet_container);
     	FrameLayout.LayoutParams paramsReset = (FrameLayout.LayoutParams) tweetContainer.getLayoutParams();
     	
     	//its difficult to get fragment width, instead take entire width of layout and subtract handle button width
 //    	View twHandle = (View) findViewById(R.id.tweet_frag_button);
 //    	int twFragWidth = tweetContainer.getWidth() - twHandle.getWidth();    	
 		paramsReset.leftMargin = -315;
 		tweetContainer.setLayoutParams(paramsReset);  
     }
        
     /**
      * Looks at current gamestate for twitter challenge
      * @return True if new challenge, false if not
      */
     private void updateTwitterState(JSONObject newState)  {
     	//TODO: based on web API as of 11/17, which is likely to change    	
     	
     	boolean isActive = false;
     	try{
     		JSONObject web = newState.getJSONObject("web");
     		JSONObject twitter = web.getJSONObject("twitter");
     		JSONObject activeVote = twitter.getJSONObject("activeVote");
     		isActive = activeVote.getBoolean("isActive");
     	} catch (JSONException ex) {
     		//TODO:json exception procedures
     		System.out.println(ex);
     		return;
     	}
     	
     	if(isActive==false) {
     		//if a vote has just ended...
     		if(activeTwitter==true)  {    			
     			endTwitter();
     		}
     	} else {
     		//if a vote has just begun
     		if(activeTwitter==false) {    			
 //    			startTwitter(); // @BUG causing a crash when not logged in
     		}
     	}
     }
     
     private boolean checkTwitterLogin() {
     	PreferenceUtility pu = new PreferenceUtility();
     	return pu.returnBoolean(getString(R.string.tw_login_key), false, this);
     }
     
     /**
      * Initializes twitter view for new twitter vote
      */
 	private void startTwitter() {
     	activeTwitter=true;
     	//update prompt...
     	TweetFragment tweetFrag = (TweetFragment) getSupportFragmentManager().findFragmentById(edu.pugetsound.vichar.R.id.twitter_fragment);
         tweetFrag.setPrompt(getString(edu.pugetsound.vichar.R.string.default_twitter_vote));
         //TODO:twitter handle button needs to change
         Button tweetHandle = (Button)findViewById(edu.pugetsound.vichar.R.id.tweet_frag_button);
         
         //set opacity
         AlphaAnimation alpha = new AlphaAnimation(0.50f, 1f);
         alpha.setFillAfter(true);
         tweetHandle.startAnimation(alpha);
         
         
         //deal with deprecated methods calls, ugh     
 //        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
 //            tweetHandle.setBackgroundDrawable(getResources().getDrawable(edu.pugetsound.vichar.R.drawable.twitter_logo));
 //        } else {
 //        	tweetHandle.setBackground(getResources().getDrawable(edu.pugetsound.vichar.R.drawable.twitter_logo));
 //        }       
     }
     
     /**
      * Terminates current twitter vote, changes appropriate UI
      */
 	private void endTwitter() {
     	activeTwitter=false;
     	//update prompt...
     	TweetFragment tweetFrag = (TweetFragment) getSupportFragmentManager().findFragmentById(edu.pugetsound.vichar.R.id.twitter_fragment);
         tweetFrag.setPrompt(getString(edu.pugetsound.vichar.R.string.inactive_twitter));
         
         Button tweetHandle = (Button)findViewById(edu.pugetsound.vichar.R.id.tweet_frag_button);        
         //set opacity
         AlphaAnimation alpha = new AlphaAnimation(1f, 0.50f);
         alpha.setFillAfter(true);
         tweetHandle.startAnimation(alpha);
         
         //deprecated methods again...
 //        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
 //            tweetHandle.setBackgroundDrawable(getResources().getDrawable(edu.pugetsound.vichar.R.drawable.twitter_logo));
 //        } else {
 //        	tweetHandle.setBackground(getResources().getDrawable(edu.pugetsound.vichar.R.drawable.twitter_logo));
 //        }
     }
     
     /**
      * Sends tweet
      * @param view
      */
     public void sendTweet(View view)     {
     	//only post if twitter vote is active
     	activeTwitter = true; //TODO:GET RID OF THIS, just for testing
     	if(activeTwitter)  {
     		TweetFragment tweetFrag = (TweetFragment) getSupportFragmentManager().findFragmentById(edu.pugetsound.vichar.R.id.twitter_fragment);
     		tweetFrag.sendTweet(view);	
     	}
     }
 
     
     /** We want to load specific textures from the APK, which we will later
     use for rendering. */
     private void loadTextures()
     {
     	// UPDATE:: We added these textures for the demo.
     	mTextures.add(Texture.loadTextureFromApk("tower_shell.png", getAssets()));
     	mTextures.add(Texture.loadTextureFromApk("tower_top.png", getAssets()));
     	mTextures.add(Texture.loadTextureFromApk("banana180.jpg", getAssets())); 
     }
     
     
     /** Configure QCAR with the desired version of OpenGL ES. */
     private int getInitializationFlags()
     {
         int flags = 0;
         
         // Query the native code:
         if (getOpenGlEsVersionNative() == 1)
         {
             flags = QCAR.GL_11;
         }
         else
         {
             flags = QCAR.GL_20;
         }
         
         return flags;
     }    
     
     
     /** native method for querying the OpenGL ES version.
      * Returns 1 for OpenGl ES 1.1, returns 2 for OpenGl ES 2.0. */
     public native int getOpenGlEsVersionNative();
     
     /** Native tracker initialization and deinitialization. */
     public native int initTracker();
     public native void deinitTracker();
 
     /** Native functions to load and destroy tracking data. */
     public native int loadTrackerData();
     public native void destroyTrackerData();    
     
     /** Native sample initialization. */
     public native void onQCARInitializedNative();    
         
     /** Native methods for starting and stoping the camera. */ 
     private native void startCamera();
     private native void stopCamera();
     
     /** Native method for setting / updating the projection matrix for AR content rendering */
     private native void setProjectionMatrix();
     
     /** Native method for getting phone location */
     private native float[] getCameraLocation();
     
     // TODO create a flag to indicate whether an target is being tracked, and
     // write a setter function for the native library to call.
 
     protected void onStart() {
     	DebugLog.LOGD("ARGameActivity::onStart");
     	super.onStart();
     	
     	//Bind to the networking service
     	doBindNetworkingService();
     }
     
     protected void onStop() {
     	DebugLog.LOGD("ARGameActivity::onStop");
     	super.onStop();
     	
     	//Bind to the networking service
     	doUnbindNetworkingService();
     }
     
    /** Called when the activity will start interacting with the user.*/
     protected void onResume()
     {
         DebugLog.LOGD("ARGameActivity::onResume");
         super.onResume();
         
         // QCAR-specific resume operation
         QCAR.onResume();
         
         // We may start the camera only if the QCAR SDK has already been 
         // initialized
         if (mAppStatus == APPSTATUS_CAMERA_STOPPED)
         {
             updateApplicationStatus(APPSTATUS_CAMERA_RUNNING);
             
             // Reactivate flash if it was active before pausing the app
             if (mFlash)
             {
                 boolean result = activateFlash(mFlash);
                 DebugLog.LOGI("Turning flash "+(mFlash?"ON":"OFF")+" "+(result?"WORKED":"FAILED")+"!!");
             }
         }
         
         // Resume the GL view:
         if (mGlView != null)
         {
             mGlView.setVisibility(View.VISIBLE);
             mGlView.onResume();
         }        
     }
     
     
     public void onConfigurationChanged(Configuration config)
     {
         DebugLog.LOGD("ARGameActivity::onConfigurationChanged");
         super.onConfigurationChanged(config);
         
         storeScreenDimensions();
         
         // Set projection matrix:
         if (QCAR.isInitialized())
             setProjectionMatrix();
     }
     
     /**
      * Called every time the gameState is updated with the remote game state.
      * This method should call other methods and contain very little logic of 
      * its own. 
      */
     private void onGameStateChange(String stateStr) {
     	
     	pushDeviceState(obtainDeviceState());
     	//DebugLog.LOGI("onGameStateChange:" + stateStr);
     	
     	//System.out.println(stateStr);
     	
     	try {
     		JSONObject gameState = new JSONObject(stateStr);
 
     		//Pull out official namespaces
     		JSONObject engineState = (JSONObject) gameState.get(GAME_ENGINE_NAMESPACE);
     		JSONObject webState = (JSONObject) gameState.get(WEB_NAMESPACE);
     		updateTwitterState(gameState);
     		parseEngineState(engineState);
     		// TODO: Pass the engineState to functions that need to render it
     	} catch(JSONException e) {
     		//shit!
     		e.printStackTrace();
     	}
     }
     
     /**
      * Parse the engineState JSONObject into a float array in ARGameRender.
      * 
      * @throws JSONException
      */
     private void parseEngineState(JSONObject engineState) throws JSONException
     {
     	poseData = new float[arrayLen];
     	int count = 0; 
 
     	//will opt returning null clear the objects?
     	JSONObject turrets = engineState.optJSONObject(TURRET_NAMESPACE);
     	if(turrets != null){
     		count = loadObject(turrets, 1.0f, count, false);
     	}
     	// TODO change type indices
     	JSONObject turretBullets = engineState.optJSONObject(TURRETBULLET_NAMESPACE);
     	if(turretBullets != null){
     		count = loadObject(turretBullets, 1.0f, count, false);
     	}
 
     	JSONObject fireballs = engineState.optJSONObject(FIREBALL_NAMESPACE);
     	if(fireballs != null){
     		count = loadObject(fireballs, 1.0f, count, false);
     	}
 
     	JSONObject minions = engineState.optJSONObject(MINION_NAMESPACE);
     	if(minions != null){
     		count = loadObject(minions, 1.0f, count, false);
     	}
 
     	JSONObject batteries = engineState.optJSONObject(BATTERY_NAMESPACE);
     	if(batteries != null){
     		count = loadObject(batteries, 1.0f, count, true);
     	}
 
     	JSONObject player = engineState.optJSONObject(PLAYER_NAMESPACE);
     	// load player 
     	if(player != null)
     	{
     		DebugLog.LOGI(player.toString());
     	   	if( count + OBJ_SIZE >= arrayLen)
     	   	{
     	   		int newLen = arrayLen * 2;
         		resizeArray(poseData, newLen);
         		arrayLen = newLen;
         	}
     	   	poseData[count++] = 1.0f; // TODO use enums to represent the types of gameobjects.
     		count = parsePosition(player.getJSONObject(POSITION_NAMESPACE), count);
     		count = parseRotaion(player.getJSONObject(ROTATION_NAMESPACE), count);
     		updated = true;
 //    		DebugLog.LOGI( "Parse:" + player.toString());
     	}
     	else DebugLog.LOGI("No Player");
 
     	JSONObject eyeballs = engineState.optJSONObject(EYEBALL_NAMESPACE);
 		count = loadObject(eyeballs, 1.0f, count, false);
 
     	JSONObject platforms = engineState.optJSONObject(PLATFORM_NAMESPACE);
     	// TODO do something with the platforms
     }
 
     /**
      * A helper method to load the object in the array
      * @param type
      * @param typeIndex
      * @param i
      * @return
      * @throws JSONException
      */
    private int loadObject(JSONObject type, float typeIndex, int i, boolean notBattery) throws JSONException
     {
     	Iterator<String> objItr = type.keys();
     	while( objItr.hasNext())
     	{
     		JSONObject obj = type.getJSONObject(objItr.next());
     		if( i + OBJ_SIZE >= arrayLen)
     		{
     			int newLen = arrayLen * 2;
     			resizeArray(poseData, newLen);
     			arrayLen = newLen;
     		}
     		poseData[i++] = typeIndex; // TODO use enums to represent the types of gameobjects.
     		i = parsePosition(obj.getJSONObject(POSITION_NAMESPACE), i);
    		if(notBattery)
     		{
         		i = parseRotaion(obj.getJSONObject(ROTATION_NAMESPACE), i);
     		}
     		updated = true;
     	}
     	return i;
     }
     
     /**
      * loads position JSON data into poseData array.
      * @param xyz
      * @param i
      * @throws JSONException
      */
     private int parsePosition(JSONObject xyz, int i) throws JSONException
     {
     	poseData[i++] = Float.parseFloat(xyz.getString("x"));
     	poseData[i++] = Float.parseFloat(xyz.getString("y"));
     	poseData[i++] = Float.parseFloat(xyz.getString("z"));
     	return i;
     }
     
     /**
      * loads rotation JSON data into poseData array. Designed to be called immediately after parsePosition.
      * @param xyz
      * @param i
      * @throws JSONException
      */
     private int parseRotaion(JSONObject xyz, int i) throws JSONException
     {
     	poseData[i++] = Float.parseFloat(xyz.getString("x"));
     	poseData[i++] = Float.parseFloat(xyz.getString("y"));
     	poseData[i++] = Float.parseFloat(xyz.getString("z"));
     	return i;
     }
 
     /** Called when the system is about to start resuming a previous activity.*/
     protected void onPause()
     {
         DebugLog.LOGD("ARGameActivity::onPause");
         super.onPause();
         
         if (mGlView != null)
         {
             mGlView.setVisibility(View.INVISIBLE);
             mGlView.onPause();
         }
         
         if (mAppStatus == APPSTATUS_CAMERA_RUNNING)
         {
             updateApplicationStatus(APPSTATUS_CAMERA_STOPPED);
         }
         
         // QCAR-specific pause operation
         QCAR.onPause();
     }
     
     
     /** Native function to deinitialize the application.*/
     private native void deinitApplicationNative();
 
     
     /** The final call you receive before your activity is destroyed.*/
     protected void onDestroy()
     {
         DebugLog.LOGD("ARGameActivity::onDestroy");
         super.onDestroy();
         
         // Dismiss the splash screen time out handler:
         if (mSplashScreenHandler != null)
         {
             mSplashScreenHandler.removeCallbacks(mSplashScreenRunnable);
             mSplashScreenRunnable = null;
             mSplashScreenHandler = null;
         }        
         
         // Cancel potentially running tasks
         if (mInitQCARTask != null &&
             mInitQCARTask.getStatus() != InitQCARTask.Status.FINISHED)
         {
             mInitQCARTask.cancel(true);
             mInitQCARTask = null;
         }
         
         if (mLoadTrackerTask != null &&
             mLoadTrackerTask.getStatus() != LoadTrackerTask.Status.FINISHED)
         {
             mLoadTrackerTask.cancel(true);
             mLoadTrackerTask = null;
         }
         
         // Ensure that all asynchronous operations to initialize QCAR and loading
         // the tracker datasets do not overlap:
         synchronized (mShutdownLock) {
             
             // Do application deinitialization in native code
             deinitApplicationNative();
             
             // Unload texture
             mTextures.clear();
             mTextures = null;
             
             // Destroy the tracking data set:
             destroyTrackerData();
             
             // Deinit the tracker:
             deinitTracker();
             
             // Deinitialize QCAR SDK
             QCAR.deinit();   
         }
         
         System.gc();
     }
 
     
     /** NOTE: this method is synchronized because of a potential concurrent
      * access by ARGameActivity::onResume() and InitQCARTask::onPostExecute(). */
     private synchronized void updateApplicationStatus(int appStatus)
     {
         // Exit if there is no change in status
         if (mAppStatus == appStatus)
             return;
 
         // Store new status value      
         mAppStatus = appStatus;
 
         // Execute application state-specific actions
         switch (mAppStatus)
         {
             case APPSTATUS_INIT_APP:
                 // Initialize application elements that do not rely on QCAR
                 // initialization  
                 initApplication();
                 
                 // Proceed to next application initialization status
                 updateApplicationStatus(APPSTATUS_INIT_QCAR);
                 break;
 
             case APPSTATUS_INIT_QCAR:
                 // Initialize QCAR SDK asynchronously to avoid blocking the
                 // main (UI) thread.
                 // This task instance must be created and invoked on the UI
                 // thread and it can be executed only once!
                 try
                 {
                     mInitQCARTask = new InitQCARTask();
                     mInitQCARTask.execute();
                 }
                 catch (Exception e)
                 {
                     DebugLog.LOGE("Initializing QCAR SDK failed");
                 }
                 break;
                 
             case APPSTATUS_INIT_TRACKER:
                 // Initialize the ImageTracker
                 if (initTracker() > 0)
                 {
                     // Proceed to next application initialization status
                     updateApplicationStatus(APPSTATUS_INIT_APP_AR);     
                 }
                 break;
                 
             case APPSTATUS_INIT_APP_AR:
                 // Initialize Augmented Reality-specific application elements
                 // that may rely on the fact that the QCAR SDK has been
                 // already initialized
                 initApplicationAR();
                 
                 // Proceed to next application initialization status
                 updateApplicationStatus(APPSTATUS_LOAD_TRACKER);
                 break;
                 
             case APPSTATUS_LOAD_TRACKER:
                 // Load the tracking data set
                 //
                 // This task instance must be created and invoked on the UI
                 // thread and it can be executed only once!
                 try
                 {
                     mLoadTrackerTask = new LoadTrackerTask();
                     mLoadTrackerTask.execute();
                 }
                 catch (Exception e)
                 {
                     DebugLog.LOGE("Loading tracking data set failed");
                 }
                 break;
                 
             case APPSTATUS_INITED:
                 // Hint to the virtual machine that it would be a good time to
                 // run the garbage collector.
                 //
                 // NOTE: This is only a hint. There is no guarantee that the
                 // garbage collector will actually be run.
                 System.gc();
 
                 // Native post initialization:
                 onQCARInitializedNative();
                 
                 // The elapsed time since the splash screen was visible:
                 long splashScreenTime = System.currentTimeMillis() - 
                                             mSplashScreenStartTime;
                 long newSplashScreenTime = 0;
                 if (splashScreenTime < MIN_SPLASH_SCREEN_TIME)
                 {
                     newSplashScreenTime = MIN_SPLASH_SCREEN_TIME -
                                             splashScreenTime;   
                 }
                 
                 // Request a callback function after a given timeout to dismiss
                 // the splash screen:
                 mSplashScreenHandler = new Handler();
                 mSplashScreenRunnable =
                     new Runnable() {
                         public void run()
                         {
                             // Hide the splash screen
                             mSplashScreenView.setVisibility(View.INVISIBLE);
                             
                             // Activate the renderer
                             mRenderer.mIsActive = true;
     
                             // Now add the GL surface view. It is important
                             // that the OpenGL ES surface view gets added
                             // BEFORE the camera is started and video
                             // background is configured.
                             addContentView(mGlView, new LayoutParams(
                                             LayoutParams.MATCH_PARENT,
                                             LayoutParams.MATCH_PARENT));   
                     	    
                             // Start the camera:
                             updateApplicationStatus(APPSTATUS_CAMERA_RUNNING);
                             
                             //make UI visible
                     	    addContentView(gui, new LayoutParams(
                                     LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));                    	    
                     	    Log.d("UI", "add ui");
                     	    //check if user is logged into twitter
                     	    Boolean twLogin = checkTwitterLogin();
                     	    //if not, don't render twitter container
                     	    if(!twLogin) {
                     	    	View twContainer = findViewById(R.id.tweet_container);
                     	    	twContainer.setVisibility(View.GONE);
                     	    }
                     	    
                     	    //tweet handle touch listener
                             Button tweetHandle = (Button) findViewById(R.id.tweet_frag_button);
                             tweetHandle.setOnTouchListener(tweetHandleListener);
                     	    snapTwitterOff();
                             endTwitter();
                             resizeEyelids();
                         }
                 };
 
                 mSplashScreenHandler.postDelayed(mSplashScreenRunnable,
                                                     newSplashScreenTime);
  
                 break;
                 
             case APPSTATUS_CAMERA_STOPPED:
                 // Call the native function to stop the camera
                 stopCamera();
                 break;
                 
             case APPSTATUS_CAMERA_RUNNING:
                 // Call the native function to start the camera
                 startCamera();
                 setProjectionMatrix();
                 break;
                 
             default:
                 throw new RuntimeException("Invalid application state");
         }  
         //updateUI();
         
     }   
     
     /** Tells native code whether we are in portait or landscape mode */
     private native void setActivityPortraitMode(boolean isPortrait);
     
     
     /** Initialize application GUI elements that are not related to AR. */
     private void initApplication()
     {
         // Set the screen orientation
         //
         // NOTE: It is recommended to set this because of the following reasons:
         //
         //    1.) Before Android 2.2 there is no reliable way to query the
         //        absolute screen orientation from an activity, therefore using 
         //        an undefined orientation is not recommended. Screen 
         //        orientation matching orientation sensor measurements is also
         //        not recommended as every screen orientation change triggers
         //        deinitialization / (re)initialization steps in internal QCAR 
         //        SDK components resulting in unnecessary overhead during 
         //        application run-time.
         //
         //    2.) Android camera drivers seem to always deliver landscape images
         //        thus QCAR SDK components (e.g. camera capturing) need to know 
         //        when we are in portrait mode. Before Android 2.2 there is no 
         //        standard, device-independent way to let the camera driver know 
         //        that we are in portrait mode as each device seems to require a
         //        different combination of settings to rotate camera preview 
         //        frames images to match portrait mode views. Because of this,
         //        we suggest that the activity using the QCAR SDK be locked
         //        to landscape mode if you plan to support Android 2.1 devices
         //        as well. Froyo is fine with both orientations.
         int screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
         
         // Apply screen orientation
         setRequestedOrientation(screenOrientation);
         
         // Pass on screen orientation info to native code
         setActivityPortraitMode(screenOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
         
         storeScreenDimensions();
         
         // As long as this window is visible to the user, keep the device's
         // screen turned on and bright.
         getWindow().setFlags(
             WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
             WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
               
         // Create and add the splash screen view
         mSplashScreenView = new ImageView(this);
         mSplashScreenView.setImageResource(mSplashScreenImageResource);
         addContentView(mSplashScreenView, new LayoutParams(
                         LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
         
         mSplashScreenStartTime = System.currentTimeMillis();       
         
     }
     
     
     /** Native function to initialize the application. */
     private native void initApplicationNative(int width, int height);
 
 
     /** Initializes AR application components. */
     private void initApplicationAR()
     {        
         // Do application initialization in native code (e.g. registering
         // callbacks, etc.)
         initApplicationNative(mScreenWidth, mScreenHeight);
 
         // Create OpenGL ES view:
         int depthSize = 16;
         int stencilSize = 0;
         boolean translucent = QCAR.requiresAlpha();
         
         mGlView = new QCARSampleGLView(this);
         mGlView.init(mQCARFlags, translucent, depthSize, stencilSize);
         
         mRenderer = new ARGameRenderer();
         mGlView.setRenderer(mRenderer);
  
     }
 
     /** Invoked the first time when the options menu is displayed to give
      *  the Activity a chance to populate its Menu with menu items. */
     public boolean onCreateOptionsMenu(Menu menu)
     {
         super.onCreateOptionsMenu(menu);
                         
         mDataSetMenuItem = menu.add("Switch to Vichar dataset");
         menu.add("Toggle flash");
         menu.add("Trigger autofocus");
         
         SubMenu focusModes = menu.addSubMenu("Focus Modes");
         focusModes.add("Normal").setCheckable(true);
         focusModes.add("Continuous Autofocus").setCheckable(true);
         focusModes.add("Infinity").setCheckable(true);
         focusModes.add("Macro Mode").setCheckable(true);
         
         return true;
     }
     
     
     /** Tells native code to switch dataset as soon as possible*/
     private native void switchDatasetAsap();
     
     
     /** Invoked when the user selects an item from the Menu */
     public boolean onOptionsItemSelected(MenuItem item)
     {
         if(item == mDataSetMenuItem)
         {
            switchDatasetAsap();
            mIsStonesAndChipsDataSetActive = !mIsStonesAndChipsDataSetActive;
            if (mIsStonesAndChipsDataSetActive)
            {
                item.setTitle("Switch to FlakesBox dataset");
            }
            else
            {
                item.setTitle("Switch to Vichar dataset");
            }
             
         }
         else if(item.getTitle().equals("Toggle flash"))
         {
             mFlash = !mFlash;
             boolean result = activateFlash(mFlash);
             DebugLog.LOGI("Turning flash "+(mFlash?"ON":"OFF")+" "+(result?"WORKED":"FAILED")+"!!");
         }
         else if(item.getTitle().equals("Trigger autofocus"))
         {
             boolean result = autofocus();
             DebugLog.LOGI("Autofocus requested"+(result?" successfully.":".  Not supported in current mode or on this device."));
         }
         else 
         {
             int arg = -1;
             if(item.getTitle().equals("Normal"))
                 arg = 0;
             if(item.getTitle().equals("Continuous Autofocus"))
                 arg = 1;
             if(item.getTitle().equals("Infinity"))
                 arg = 2;
             if(item.getTitle().equals("Macro Mode"))
                 arg = 3;
             
             if(arg != -1)
             {
                 boolean result = setFocusMode(arg);
                 if (result)
                 {
                     item.setChecked(true);
                     if(checked != null && item != checked)
                         checked.setChecked(false);
                     checked = item;
                 }
                 
                 DebugLog.LOGI("Requested Focus mode "+item.getTitle()+(result?" successfully.":".  Not supported on this device."));
             }
         }
         
         return true;
     }
     
     private MenuItem checked;
     private boolean mFlash = false;
     private native boolean activateFlash(boolean flash);
     private native boolean autofocus();
     private native boolean setFocusMode(int mode);
     
     /** Returns the number of registered textures. */
     public int getTextureCount()
     {
         return mTextures.size();
     }
   
     /** Returns the texture object at the specified index. */
     public Texture getTexture(int i)
     {
         return mTextures.elementAt(i);
     }
    
     /** A helper for loading native libraries stored in "libs/armeabi*". */
     public static boolean loadLibrary(String nLibName)
     {
         try
         {
             System.loadLibrary(nLibName);
             DebugLog.LOGI("Native library lib" + nLibName + ".so loaded");
             return true;
         }
         catch (UnsatisfiedLinkError ulee)
         {
             DebugLog.LOGE("The library lib" + nLibName +
                             ".so could not be loaded");
         }
         catch (SecurityException se)
         {
             DebugLog.LOGE("The library lib" + nLibName +
                             ".so was not allowed to be loaded");
         }
         
         return false;
     }
     
     /**
      * Capture touch events
      * @param v
      * @param event
      * @return
      */
     public boolean onTouch(View v, MotionEvent ev)
     {
     	float dx = 0f;
     	float dy = 0f;
 
     	if(ev.getAction() == MotionEvent.ACTION_MOVE) {
     		dx = ev.getX() - touchX;
     		dy = ev.getY() - touchY;
     	}
     	if(ev.getAction() == MotionEvent.ACTION_DOWN 
     			|| ev.getAction() == MotionEvent.ACTION_MOVE) {
     		// Remember new touch coors
     		touchX = ev.getX();
     		touchY = ev.getY();
     	} else if(ev.getAction() == MotionEvent.ACTION_UP) {
     		// reset values
     		touchX = 0f;
     		touchY = 0f;
     		dx = 0f;
     		dy = 0f;
     	}
     	return true; //Must return true to get move events
     }
     
     /**
      * Returns a blank device state JSONObject
      * Use this method to get a reference to a device state that you can modify
      * and push.
      * 
      * This just makes it extra clear that you are passing snapshots of info
      * to the server. Do not try to get info about the device from a local copy
      * of the device state.
      * 
      * @return
      */
     private JSONObject obtainDeviceState() {
     	return new JSONObject();
     }
     
     /**
      * Wraps a device state snapshot in the deviceUUID and pushes it to the 
      * server.
      * @param deviceState
      */
     private void pushDeviceState(JSONObject deviceState) {
     	try {
     		
     		// get camera's location and rotation from the native code, format it and put it in the JSON object
     		float[] cameraLoc = getCameraLocation();
     		if (cameraLoc[0] == 1.0)
     		{
     			deviceState.put("position", makePositionJSON(cameraLoc[0], cameraLoc[1], cameraLoc[2]));
         		deviceState.put("rotation", makeRotationJSON(cameraLoc[3], cameraLoc[4], cameraLoc[5]));
     		}
     		else
     		{
     			deviceState.put("position", null);
     			deviceState.put("rotation", null);
     		}
     		
     		// Log the position for testing.
     		DebugLog.LOGI("pushDeviceState:" + deviceState.toString());
     		
     		//put current time in JSON
     		long time = System.currentTimeMillis();
     		deviceState.put("lastUpdated", time);
     		deviceState.put("lastUpdatedByDevice", time);
 
     		JSONObject sendState = new JSONObject().put(deviceUUID, deviceState);
     		sendState = new JSONObject().put(DEVICES_NAMESPACE, sendState);
     		pushGameState(sendState);
     	} catch (JSONException e) {
     		
     	}
     		
     }
     
     /**
      * Generally, use the pushDeviceState() function instead of this one.
      * Be careful with this function. It allows you to change anything in the
      * game's root JSON object -- i.e. things in other people's namespaces
      * @param deviceState
      */
     private void pushGameState(JSONObject sendState) {
     	if(isBoundToNetworkingService) {
     		//put in current time
     		try {
 	    		long time = System.currentTimeMillis();
 	    		sendState.put("lastModified", time);
     		} catch (JSONException ex) {
     			//TODO:probably nothing to do in this case
     		}   
     		
     		Bundle b = new Bundle();
     		b.putString("" + NetworkingService.MSG_QUEUE_OUTBOUND_J_STRING, 
     				sendState.toString());
     		Message msg = Message.obtain(null, 
     				NetworkingService.MSG_QUEUE_OUTBOUND_J_STRING);
     		msg.setData(b);
     		try {
     			networkingServiceMessenger.send(msg); //@BUG caught a null pointer exception by pressing the home button from game activity.
     		} catch (RemoteException e) {
                 //TODO handle RemoteException
     			Log.i(this.toString(), "updateRemoteGameState: RemoteException");
             }
     	} else {
     		Log.i(this.toString(),"Not Bound to NetworkingService");
     	}
     }
     
     /**
      * Takes three floats and makes them into a JSON object formatted for position
      * @param x
      * @param y
      * @param z
      * @return returns the position JSONObject
      * @throws JSONException
      */
     private JSONObject makePositionJSON(float x, float y, float z) throws JSONException
     {	
     	JSONObject position = new JSONObject();
     	
     	position.put("x", "" + x);
     	position.put("y", "" + y);
     	position.put("z", "" + z);
     	
     	return position; 	
     }
     
     /**
      * Takes three floats and makes them into a JSON object formatted for rotation (orientation based on Euler Angles).
      * @param xRot
      * @param yRot
      * @param zRot
      * @return 
      * @throws JSONException
      */
     private JSONObject makeRotationJSON(float xRot, float yRot, float zRot) throws JSONException
     {	
     	JSONObject rotation = new JSONObject();
     	
     	rotation.put("x", "" + xRot);
     	rotation.put("y", "" + yRot);
     	rotation.put("z", "" + zRot);
     	
     	return rotation; 	
     }
     
     /**
      * Handles incoming messages from NetworkingService
      * @author DuBious
      *
      */
     class IncomingHandler extends Handler {
         @Override
         public void handleMessage(Message msg) {
             switch (msg.what) {
 	            case NetworkingService.MSG_RET_JSON_STRING_FROM_SERVER:
 	            	Log.d(this.toString(), "Got something from NetworkingService");
 	            	String str = msg.getData().getString("" + NetworkingService.MSG_RET_JSON_STRING_FROM_SERVER);
 	            	//Log.d(this.toString(), str);
 	            	if(str != null) {
 	            		isConnectedToGameServer = true;
 	            		onGameStateChange(str);
 	            	}
 	            	break;
 	            case NetworkingService.MSG_NETWORKING_FAILURE:
 	            	isConnectedToGameServer = false;
 	            	Log.d(this.toString(), "Networking Failure");
 	            	break;
 	            default:
 	                super.handleMessage(msg);
             }
         }
     }
     
     /**
      * Forms the connection between this Activity and the NetworkingService
      */
     private ServiceConnection networkingServiceConnection = new ServiceConnection() 
     { //need this to create the connection to the service
         /**
          * Called when the service is started and bound to this Service Connection
          * @param className The classname of the activity
          * @param service the service's binder object
          */
     	public void onServiceConnected(
         				ComponentName className, 
         				IBinder binder)
         {
     		Log.d(this.toString(),"Start onServiceConnected");
         	// Create a Messenger that references the service
         	networkingServiceMessenger = new Messenger(binder);
         	Log.d(this.toString(),"networkingServiceMessenger: " + networkingServiceMessenger.toString());
         	
         	isBoundToNetworkingService = true;
 	        Log.d(this.toString(),"Bound to HttpSevice");
 	        
 	        try {
 		        Message msg = Message.obtain(null, NetworkingService.MSG_REGISTER_CLIENT);
 	            msg.replyTo = mMessenger;
 	            networkingServiceMessenger.send(msg);
 	        } catch (RemoteException e) {
                 //TODO handle RemoteException
 	        	// The service crashed before we could do anything with it
 	        }
         }
 
     	/**
     	 * Called when the service stops or unbinds itself
     	 * @param className The Classname of the activity
     	 */
         public void onServiceDisconnected(ComponentName className) 
         {
         	isBoundToNetworkingService = false;
         }
     };
     
     /**
      * Binds this activity to the NetworkingService
      */
     private void doBindNetworkingService() {
     	Log.d(this.toString(),"Binding HttpSevice");
         bindService(new Intent(ARGameActivity.this, NetworkingService.class), 
         		networkingServiceConnection, Context.BIND_AUTO_CREATE);
     }
 
     /**
      * Unbinds this activity from the NetworkingService
      */
     private void doUnbindNetworkingService() {
     	// Unregister Messenger
     	if (networkingServiceMessenger != null) {
             try {
                 Message msg = Message.obtain(null, NetworkingService.MSG_UNREGISTER_CLIENT);
                 msg.replyTo = mMessenger;
                 networkingServiceMessenger.send(msg);
                 networkingServiceMessenger = null;
             } catch (RemoteException e) {
                 // There is nothing special we need to do if the service has crashed.
             }
         }
     	// Detach our existing connection.
     	
         unbindService(networkingServiceConnection);
     }
     
     private static float[] resizeArray (float[] oldArray, int newSize) {
     	int oldSize = oldArray.length;
     	float[] newArray = new float[newSize];
     	int preserveSize = Math.min(oldSize, newSize);
     	for(int i=0; i<preserveSize; i++){
     		newArray[i] = oldArray[i];
     	}
     	return newArray; 
     }
 }
 
 
 
