 package edu.usf.eng.pie.avatars4change.wallpaper;
 
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import ly.count.android.api.Countly;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.SystemClock;
 import android.service.wallpaper.WallpaperService;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.widget.Toast;
 import edu.usf.eng.pie.avatars4change.avatar.Avatar;
 import edu.usf.eng.pie.avatars4change.avatar.Location;
 import edu.usf.eng.pie.avatars4change.myrunsdatacollectorlite.Globals;
 import edu.usf.eng.pie.avatars4change.userData.userData;
 import edu.usf.eng.pie.avatars4change.wallpaper.Layer_Main;
 
 // This animated wallpaper draws a virtual avatar animation from png images saved on the sd card
  
 public class avatarWallpaper extends WallpaperService {
 	private static final String TAG                    = "avatarWallpaper";	//for logs
 	public static final String SHARED_PREFS_NAME="avatar_settings";
     private final Handler mHandler              = new Handler();
     private final String[] mLabels              = {"still", "walking", "running"};
     public static float desiredFPS              = 10;
     public static Context mContext;	//this is needed for countly wifi check
     public static boolean wifiOnly              = false;	//enable if program should only use wifi
     public static boolean sdPresent             = false;
     
     public static Avatar    theAvatar;
     private static SharedPreferences mPrefs;
 
 	//vars for background visibility logging
 	long    visibilityStart;
 	public static boolean keepLogs              = true;
 	public static long lastLogTime = 0;
     
     @Override
     public void onCreate() {
     	super.onCreate();
     	mContext = getApplicationContext();//TODO: this should not be used, but instead passed around or found with getContext()
     	sdPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
 		//delay if no sdCard
 		while(!sdPresent){
 			Log.d(TAG,"waiting for sdCard...");
 			Toast.makeText(mContext, "avatarWallpaper searching for sdCard", Toast.LENGTH_SHORT).show();
 	    	sdPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
 			try {
 				Thread.sleep(750);		//TODO: wow, this is ugly...
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
        mPrefs   = avatarWallpaper.this.getSharedPreferences(SHARED_PREFS_NAME, 0);	//load settings

         Log.d(TAG,"application context =" + getApplicationContext().toString());
         //set up the avatar
         theAvatar = new Avatar(new Location(0,0,0,300,0), 3,"sleeping", getApplicationContext());		//create new avatar
         loadPrefs();
     	
     	//set up countly:
     	String appKey        = "301238f5cbf557a6d4f80d4bb19b97b3da3a22ca";
     	String serverURL     = "http://testSubDomain.socialvinesolutions.com";
     	Countly.sharedInstance().init(mContext, serverURL, appKey);
     	
         //start up countly
     	Countly.sharedInstance().onStart();// in onStart.
     	
     	//setup the PA collector service:
     	Intent mServiceIntent = new Intent(mContext, edu.usf.eng.pie.avatars4change.myrunsdatacollectorlite.ServiceSensors.class);
  		int activityId = Globals.SERVICE_TASK_TYPE_CLASSIFY;	//TODO: ?
  		String label = mLabels[activityId];
  		Bundle extras = new Bundle();
  		extras.putString("label", label);
  		extras.putString("type", "collecting");
  		mServiceIntent.putExtras(extras);
  		Log.v(TAG, "starting SensorService");
  		startService(mServiceIntent); 
     	
  		//check for first time run (by looking for files)
  		boolean firstTime;
  		File file = new File(userData.getFileDir(getApplicationContext()), "dataLog.txt" );
  		if (file.exists()) {
  			firstTime = false;
  		}else{
  			firstTime = true;
  		}
  		if(firstTime){
  			Log.v(TAG,"running 1st time setup");
 	    	//run intial setup activity
  			Intent i = new Intent(getApplicationContext(), edu.usf.eng.pie.avatars4change.wallpaper.AvatarWallpaperSetup.class);
  			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
  			startActivity(i);
  		} //else assume that everything is in working order
  		
  		//register sdCard connect receiver
  		IntentFilter conFilter = new IntentFilter (Intent.ACTION_MEDIA_MOUNTED); 
  		conFilter.addDataScheme("file"); 
  		registerReceiver(this.SDconnReceiver, new IntentFilter(conFilter));
  		//register sdCard remove receiver
  		IntentFilter remFilter = new IntentFilter (Intent.ACTION_MEDIA_MOUNTED); 
  		remFilter.addDataScheme("file"); 
  		registerReceiver(this.SDremovReceiver, new IntentFilter(remFilter));
     }
     
     //SD card connected receiver
     private BroadcastReceiver SDconnReceiver = new BroadcastReceiver(){
         @Override
         public void onReceive(Context arg0, Intent intent) {
         sdPresent = true;
         }
      }; 
      //SD card removed receiver
      private BroadcastReceiver SDremovReceiver = new BroadcastReceiver(){
          @Override
          public void onReceive(Context arg0, Intent intent) {
          sdPresent = false;
          }
       }; 
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         //shut down countly
        	Countly.sharedInstance().onStop(); // in onStop.
     }
 
     @Override
     public Engine onCreateEngine() {
 		return new DrawEngine();
     }
     
     public static void loadPrefs(){
 		Log.d(TAG, "loading preferences");
         String key;
 		key="RealismLevel";
 			theAvatar.setRealismLevel(Integer.parseInt(mPrefs.getString(key, Integer.toString(theAvatar.getRealismLevel()))));
 			Log.d(TAG, "RealismLevel:"+theAvatar.getRealismLevel());
 		
 		key="CurrentActivity";
 			theAvatar.setActivityName(mPrefs.getString(key, theAvatar.getActivityName()));
 			theAvatar.lastActivityChange = SystemClock.elapsedRealtime();
 			Log.d(TAG, "CurrentActivity:"+theAvatar.getActivityName());
 		
 		key="behaviorSelector";
 			theAvatar.behaviorSelectorMethod = mPrefs.getString(key, theAvatar.behaviorSelectorMethod);
 			Log.d(TAG, "behaviorSelector:"+theAvatar.behaviorSelectorMethod);
 		
 		key="ResetLogs";
 			keepLogs = !mPrefs.getBoolean(key, keepLogs);
 			//Log.d(TAG, "keepLogs=" + String.valueOf(keepLogs));
 			Log.d(TAG, "keepLogs?:"+keepLogs);
 			
 		key="activeOnEvens";
 			sceneBehaviors.activeOnEvens = mPrefs.getBoolean(key, sceneBehaviors.activeOnEvens);
 			Log.d(TAG,"activeOnEvens:"+sceneBehaviors.activeOnEvens);
 			
 		key="UID";
 			userData.USERID = mPrefs.getString(key,userData.USERID);
 			Log.d(TAG,"UID:"+userData.USERID);
 		
 		key="wifiOnly";
 			wifiOnly = mPrefs.getBoolean(key,wifiOnly);
 			Log.d(TAG,"wifiOnly:"+wifiOnly);
 			
 		key="scale";
 			theAvatar.scaler = Float.parseFloat(mPrefs.getString(key, "1.0f"));
 			Log.d(TAG, "RealismLevel:"+theAvatar.getRealismLevel());
     }
 
     // All parts needed to draw the output go in this function
     class DrawEngine extends Engine {
     	
     	//set up the file directory for saving data and retrieving sprites
     	String extStorageDirectory = userData.getFileDir(getApplicationContext());
     	File   fileDirectory       = new File (extStorageDirectory);
     	
         //vars for the avatar
         Resources r                = getResources();
         
         //vars for canvas
         private float mCenterX;
         private float mCenterY;
        private float mHeight;
        private float mWidth;
         
 //        //vars for touchPoint circle
 //        private float mTouchX = -1;
 //        private float mTouchY = -1;
 
     	//vars for offset based on home screen location
 
 //        private float mOffset;
         
         private final Runnable mDrawViz = new Runnable() {
         	private void updateSceneBehavior(){
         		sceneBehaviors.runBehavior(getApplicationContext(),theAvatar);
         	}
         	
             public void run() {
             		updateSceneBehavior();
                 	drawFrame();                	
             }
         };
             
         private boolean mVisible;
         
         DrawEngine() {
 
 //            mStartTime = System.currentTimeMillis();	//set app start time
 //            lastActivityLevelChangeDay = Time.getJulianDay(mStartTime, TimeZone.getDefault().getRawOffset()); 	//initialize to app start
                         
 //            //register reciever for changed settings:
 //            mPrefs.registerOnSharedPreferenceChangeListener(this);
 //            onSharedPreferenceChanged(mPrefs, null);
         }
 
         @Override
         public void onCreate(SurfaceHolder surfaceHolder) {
             super.onCreate(surfaceHolder);
             //// By default we don't get touch events, so enable them.
             //setTouchEventsEnabled(true);
 
 
             //load the preferences
             loadPrefs();
             
             //set up the scene
             Layer_Main.setup(theAvatar);
             
         }
 
         @Override
         public void onDestroy() {
         	//TODO: save prefs here
             super.onDestroy();
             mHandler.removeCallbacks(mDrawViz);
         }
 
         @Override
         public void onVisibilityChanged(boolean visible) {
             mVisible = visible;
             if (visible) {
      			drawFrame();
                 visibilityStart = System.currentTimeMillis();
             } else {
                 mHandler.removeCallbacks(mDrawViz);
                 Long visibilityEnd = System.currentTimeMillis();
                 long visibleTime = visibilityEnd - visibilityStart;
                 File dataLogFile = new File(fileDirectory, "dataLog.txt");	//create file
                 
                 if(!fileDirectory.mkdirs()){	//create if directory not exist
                 	//if creation of directory fails
                 	Log.v(TAG, "creation of directory '"+ fileDirectory +"' fails, already exists?");
                 }
                 
             	Boolean SDpresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
             	if(SDpresent){
 	                //create or open dataLog file:
 	                FileOutputStream dataFileOut = null;
 	                if(keepLogs){
 						try {
 							dataFileOut = new FileOutputStream(dataLogFile, true);	//append
 						} catch (FileNotFoundException e) {
 							// TODO
 							e.printStackTrace();
 						}
 	                } else {
 	                	try {
 							dataFileOut = new FileOutputStream(dataLogFile, false);	//do not append
 							DataOutputStream dataOut = new DataOutputStream(dataFileOut);
 							//print header on data file
 							try {
 								dataOut.writeBytes("StartVisible,EndVisible,ViewTime,animationName\n");
 							} catch (IOException e) {
 								// TODO Auto-generated catch block
 								e.printStackTrace();
 							}
 							Log.d(TAG, "New dataLog file has been created");
 							keepLogs = true;
 						} catch (FileNotFoundException e) {
 							// TODO
 							e.printStackTrace();
 						}
 	                }
 					DataOutputStream dataOut = new DataOutputStream(dataFileOut);
 					//write time viewed to file
 	                try {
 						dataOut.writeBytes(String.valueOf(visibilityStart)+","+String.valueOf(visibilityEnd)+","+String.valueOf(visibilityEnd-visibilityStart)+
 								"," + theAvatar.getActivityName() + "\n");
 						Log.d(TAG, visibleTime + " ms of time added to file");
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 	                try {
 						dataOut.close();
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
             	}
             }
         }
 
         @Override
         public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
             super.onSurfaceChanged(holder, format, width, height);
             // store the center of the surface, so we can draw in the right spot
             mCenterX = width/2.0f;
             mCenterY = height/2.0f;
             /*
              * size is now constant
             mHeight = height;
             mWidth = width; 
             int s = Math.round(Math.min(mHeight,mWidth)*0.8f);
         	//Log.d(TAG,"onSurfaceChanged makes avatar size " + theAvatar.);
             theAvatar.setSize(s);
             */
             
             drawFrame();
         }
 
         @Override
         public void onSurfaceCreated(SurfaceHolder holder) {
             super.onSurfaceCreated(holder);
         }
 
         @Override
         public void onSurfaceDestroyed(SurfaceHolder holder) {
             super.onSurfaceDestroyed(holder);
             mVisible = false;
             mHandler.removeCallbacks(mDrawViz);
         }
 
         /*
         @Override
         public void onOffsetsChanged(float xOffset, float yOffset,
                 float xStep, float yStep, int xPixels, int yPixels) {
             mOffset = xOffset;
         }
         */
 
         /*
          * Store the position of the touch event so we can use it for drawing later
          */
 /*        @Override
         public void onTouchEvent(MotionEvent event) {
             if (event.getAction() == MotionEvent.ACTION_MOVE) {
                 mTouchX = event.getX();
                 mTouchY = event.getY();
             } else {
                 mTouchX = -1;
                 mTouchY = -1;
             }
             super.onTouchEvent(event);
         }
         */
 
         /*
          * Draw one frame of the animation. This method gets called repeatedly
          * by posting a delayed Runnable. You can do any drawing you want in
          * here.
          */
         void drawFrame() {
             final SurfaceHolder holder = getSurfaceHolder();
 
             Canvas c = null;
             try {
                 c = holder.lockCanvas();
                 if (c != null) {
                 	sdPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
                 	if(sdPresent){
 				Layer_Main.nextFrame();
 	
 	                	c.save();
 	                	c.translate(mCenterX, mCenterY);
 	                	Layer_Main.draw(c, holder.getSurfaceFrame(),theAvatar);
 	                	c.restore();
                 	}else{//SDcard not present
                 		c.save();
                 		Layer_Background.draw(c);
 	                	c.translate(mCenterX, mCenterY);
                 	    Paint mPaint = new Paint();
                 	    mPaint.setColor(Color.BLACK); 
                 		mPaint.setTextSize(30); 
                 		//mPaint.setStrokeWidth(2);
                 		mPaint.setTypeface(Typeface.DEFAULT);
                 		c.drawText("cannot detect SD card", -90, 90, mPaint); 
                 		c.restore();
                 	}
                 }
             } finally {
                 if (c != null) holder.unlockCanvasAndPost(c);
             }
 
             // Reschedule the next redraw
             mHandler.removeCallbacks(mDrawViz);
             if (mVisible) {
                 mHandler.postDelayed(mDrawViz, Math.round( 1000 / desiredFPS ));
             }
         }
 
     }
 }
