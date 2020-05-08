 package edu.usf.PIE.tylar.MirrorMe.avatar;
 
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Calendar;
 import java.util.TimeZone;
 
 import android.content.SharedPreferences;
 import android.content.res.AssetManager;
 import android.content.res.Resources;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.SystemClock;
 import android.service.wallpaper.WallpaperService;
 import android.text.format.Time;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 
 /*
  * This animated wallpaper draws a virtual avatar animation from png images saved on the sd card
  */
 public class avatarWallpaper extends WallpaperService {
 
 	public static final String SHARED_PREFS_NAME="avatarsettings";
     private final Handler mHandler = new Handler();
     
     @Override
     public void onCreate() {
     	SetDirectory();
     	super.onCreate();
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
     }
 
     @Override
     public Engine onCreateEngine() {
         return new DrawEngine();
     }
 
     class DrawEngine extends Engine 
     	implements SharedPreferences.OnSharedPreferenceChangeListener {
 
     	//vars for logging
     	long visibilityStart;
     	boolean keepLogs = true;
     	
     	//set up the file directory for saving data and retrieving sprites
     	String extStorageDirectory = Environment.getExternalStorageDirectory()+"/MirrorMe";
         File fileDirectory = new File (extStorageDirectory);
     	
         //vars for the avatar
     	long lastFrameChange = 0;		//last frame update [ms]
         Resources r = getResources();
         avatarObject theAvatar = new avatarObject(r, 3, "sleeping");		//create new avatar
         String selectorMethod = "Constant";
         long lastActivityChange = 0;	//last time activity level was changed [ms]
         long deltaActivityChange = 60*60*1000;	//desired time between activity level updates [ms]
         int bedTime = 23;
         int wakeTime = 5;
         boolean activeOnEvens = true;
         
         //vars for canvas
         private final Paint mPaint = new Paint();
         private float mCenterX;
         private float mCenterY;
         private float mHeight;
         private float mWidth;
         
         //vars for touchPoint circle
         private float mTouchX = -1;
         private float mTouchY = -1;
 
     	//vars for offset based on home screen location
         private float mOffset;
         
         //vars for frame rate
         private long mStartTime;	//time of app start
         int lastActivityLevelChangeDay;
         String lastActivityLevel = "active";
         private long lastTime = 0;	//time measurement for calculating deltaT and thus fps
         private float desiredFPS = 5;
         private float[] lastFPS = {0,0,0,0,0,0,0,0,0,0};	//saved past 10 fps measurements
         
         
         private final Runnable mDrawViz = new Runnable() {
             public void run() {
                 //if past bedTime and before wakeTime, sleep
                 int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                 //Log.v("Avatars4Change Avatar sleep clock", "current hour:" + currentHour);
                 if(currentHour >= bedTime || currentHour < wakeTime){
                 	//draw sleeping
                 	theAvatar.setActivityLevel("sleeping");
                 } else {	//awake
                 	int today = Time.getJulianDay(System.currentTimeMillis(), TimeZone.getDefault().getRawOffset()); 	//(new Time()).toMillis(false)
 	            	//set active or passive, depending on even or odd julian day
 	            	if(today%2 == 0){	//if today is even
 	            		if(activeOnEvens){
 	            			theAvatar.setActivityLevel("active");
 	            		}else{
 	            			theAvatar.setActivityLevel("passive");
 	            		}
 	            	}else{	//today is odd
	            		if(activeOnEvens){	//if active on odd days
 	            			theAvatar.setActivityLevel("active");
 	            		}else{
 	            			theAvatar.setActivityLevel("passive");
 	            		}
 	            	}
 	            	//check for enough time to change animation
 	        		long now = SystemClock.elapsedRealtime();		//TODO: ensure that this works even if phone switched off. 
 	                if((now - lastActivityChange) > deltaActivityChange){		//if time elapsed > desired time
 	                	theAvatar.randomActivity(theAvatar.getActivityLevel());
 	               	 	lastActivityChange = now;
 	                }
                 }
                 drawFrame();//draw next frame
             }
         };
         
         private boolean mVisible;
         
         private SharedPreferences mPrefs;
         
         DrawEngine() {
             // Create a Paint to draw on
             final Paint paint = mPaint;
             paint.setColor(Color.GRAY); //0xffffffff);
             paint.setAntiAlias(true);
             paint.setStrokeWidth(2);
             paint.setStrokeCap(Paint.Cap.ROUND);
             paint.setStyle(Paint.Style.STROKE);
 
             mStartTime = System.currentTimeMillis();	//set app start time
             lastActivityLevelChangeDay = Time.getJulianDay(mStartTime, TimeZone.getDefault().getRawOffset()); 	//initialize to app start
             mPrefs = avatarWallpaper.this.getSharedPreferences(SHARED_PREFS_NAME, 0);	//load settings
             //register reciever for changed settings:
             mPrefs.registerOnSharedPreferenceChangeListener(this);
             onSharedPreferenceChanged(mPrefs, null);
         }
         
 		@Override
 		public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
 			Log.d("MirrorMe Avatar", "adjusting " + key + " preference");
 			if(! (key == null)){	//skip if null
 				if(key.equals("RealismLevel")){
 					theAvatar.setRealismLevel(Integer.parseInt(prefs.getString(key, Integer.toString(theAvatar.getRealismLevel()))));
 				}
 				if (key.equals("CurrentActivity")){
 					theAvatar.setActivityName(prefs.getString(key, "inBed"));
 					lastActivityChange = SystemClock.elapsedRealtime();
 				}
 				if (key.equals("ActivityLevelSelector")){
 					selectorMethod = prefs.getString(key, selectorMethod);
 				}
 				if (key.equals("ResetLogs")){
 					keepLogs = !prefs.getBoolean(key, keepLogs);
 					//Log.d("MirrorMe Avatar", "keepLogs=" + String.valueOf(keepLogs));
 				}
 				if(key.equals("activeOnEvens")){
 					activeOnEvens = prefs.getBoolean(key, activeOnEvens);
 				}
 			}
 		}
 
         @Override
         public void onCreate(SurfaceHolder surfaceHolder) {
             super.onCreate(surfaceHolder);
             // By default we don't get touch events, so enable them.
             setTouchEventsEnabled(true);
         }
 
         @Override
         public void onDestroy() {
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
                 	Log.v("MirrorMe Avatar", "creation of directory fails, already exists?");
                 }
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
 						Log.d("MirrorMe Avatar", "New dataLog file has been created");
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
 					Log.d("MirrorMe Avatar", visibleTime + " ms of time added to file");
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
 
         @Override
         public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
             super.onSurfaceChanged(holder, format, width, height);
             // store the center of the surface, so we can draw in the right spot
             mCenterX = width/2.0f;
             mCenterY = height/2.0f;
             mHeight = height;
             mWidth = width;
             //TODO: MOVE THIS:
             float hScaler = mHeight/theAvatar.maxH();
             float wScaler = mWidth/theAvatar.maxW();
             if(wScaler < hScaler){
             	theAvatar.setScaler(wScaler);
             } else theAvatar.setScaler(hScaler);
             
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
 
         @Override
         public void onOffsetsChanged(float xOffset, float yOffset,
                 float xStep, float yStep, int xPixels, int yPixels) {
             mOffset = xOffset;
             drawFrame();
         }
 
         /*
          * Store the position of the touch event so we can use it for drawing later
          */
         @Override
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
                 	drawBG(c);
                     //drawTouchPoint(c);
                     drawAvatar(c);
                     //drawFPS(c);
                 }
             } finally {
                 if (c != null) holder.unlockCanvasAndPost(c);
             }
 
             // Reschedule the next redraw
             mHandler.removeCallbacks(mDrawViz);
             if (mVisible) {
                 mHandler.postDelayed(mDrawViz, 1000 / 25);
             }
         }
 
         /*
          * Draw a circle around the current touch point, if any.
          */
         void drawTouchPoint(Canvas c) {
             if (mTouchX >=0 && mTouchY >= 0) {
                 c.drawCircle(mTouchX, mTouchY, 80, mPaint);
             }
         }
         
         /*draw avatar*/
         void drawAvatar(Canvas c) {
         	//determine if enough time has passed to move to next frame
         	long now = SystemClock.elapsedRealtime();
              if(((float)(now - lastFrameChange)) > (((float)1000)/desiredFPS)){		//if total ms elapsed > desired ms elapsed
             	 theAvatar.nextFrame();
             	 lastFrameChange = now;
              } //else display same as last loop
              
         	c.translate(mCenterX, mCenterY);
         	theAvatar.drawAvatar(c,mCenterX*2,mCenterY*2);
             c.restore();
         }
         
         
         /*background */
         void drawBG(Canvas c){
         	//CALCULATE BACKGROUND LOCATION BASED ON OFFSET:
         	//float yrot = (0.5f - mOffset) * 2.0f;
         	//TODO: replace solid color background with image
             //background
             c.drawColor(Color.DKGRAY); //(0xff000000);
             //return canvas to default location
             //c.restore();
         }
         
         void drawFPS(Canvas c){
         	//calculate current frame rate
             long thisTime = System.currentTimeMillis();
             long elapsedTime = thisTime-lastTime;
             lastTime = thisTime;
             float FPSsum = 0;
             for(int i = 9; i > 0; i--){
             	lastFPS[i] = lastFPS[i-1];
             	FPSsum += lastFPS[i];
             }
         	lastFPS[0] = (float)1000 / ((float)elapsedTime); // 1 frame / <ms passed> * 1000ms/s = frame/s
         	FPSsum += lastFPS[0];
         	float fps = FPSsum/(float)10;	// 10 is # of saved previous FPS measures
         	//draw the frame rate to the screen
         	mPaint.setColor(Color.BLACK); 
         	mPaint.setTextSize(20); 
         	c.drawText("virtual FPS: " + desiredFPS + "    actual FPS: " + fps, 10, 100, mPaint); 
         }
     }
     
     /**
      * -- Check to see if the sdCard is mounted and create a directory w/in it
      * ========================================================================
      **/
     private void SetDirectory() {
         if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
 
             String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
 
             File txtDirectory = new File(extStorageDirectory);
             // Create
             // a
             // File
             // object
             // for
             // the
             // parent
             // directory
             txtDirectory.mkdirs();// Have the object build the directory
             // structure, if needed.
             CopyAssets(extStorageDirectory); // Then run the method to copy the file.
 
         } else if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED_READ_ONLY)) {
         	Log.e("MirrorMe Asset Copier", "SD card is missing");
             //AlertsAndDialogs.sdCardMissing(this);//Or use your own method ie: Toast
         }
 
     }
 
     /**
      * -- Copy the file from the assets folder to the sdCard
      * ===========================================================
      **/
     private void CopyAssets(String extStorageDir) {
         copier("MirrorMe",extStorageDir);
     }
     
     private void copier(String inDir, String extStorageDir){
     	AssetManager assetManager = getAssets();
         String[] files = null;
         Log.v("MirrorMe Avatar", "copying files in " + inDir);
         try {
             files = assetManager.list(inDir);
         } catch (IOException e) {
             Log.e("MirrorMe asset listing", e.getMessage());
         }
         String prefix = inDir;
     	if(!inDir.equals("")){
     		prefix += "/";
     	}
         for (int i = 0; i < files.length; i++) {
             InputStream in = null;
             OutputStream out = null;
             String fileName = files[i];
             try {
                 in = assetManager.open(prefix + fileName);
             } catch(Exception e){	//failed file open means listing is a directory
             	Log.v("MirrorMe Avatar", files[i] + " is directory");
             	copier(prefix + fileName,extStorageDir);	//add dir name to prefix
             	continue;
             }
             //implied else
             Log.v("MirrorMe Avatar", files[i] + " is file");
             
             File fDir = new File (extStorageDir + "/" + prefix);	//file object for mkdirs
             fDir.mkdirs();	//create directory
 
             try{	//copy the file
                 out = new FileOutputStream(extStorageDir + "/" + prefix + files[i]);
                 copyFile(in, out);
                 in.close();
                 in = null;
                 out.flush();
                 out.close();
                 out = null;
             } catch (Exception e) {
                 Log.e("MirrorMe copyfile", e.getMessage());
             }
         }
     }
 
     private void copyFile(InputStream in, OutputStream out) throws IOException {
         byte[] buffer = new byte[1024];
         int read;
         while ((read = in.read(buffer)) != -1) {
             out.write(buffer, 0, read);
         }
     }
 
 }
