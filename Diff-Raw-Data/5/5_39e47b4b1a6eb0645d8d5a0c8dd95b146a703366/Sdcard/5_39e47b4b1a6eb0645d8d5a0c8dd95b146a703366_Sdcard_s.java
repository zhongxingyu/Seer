 package edu.usf.eng.pie.avatars4change.storager;
 
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Typeface;
 import android.os.Environment;
 import android.util.Log;
 import android.widget.Toast;
 import edu.usf.eng.pie.avatars4change.wallpaper.Layer_Background;
 import edu.usf.eng.pie.avatars4change.wallpaper.avatarWallpaper;
 
 public class Sdcard {
 	private static final String TAG = "storager.Sdcard";
 	
 	private static final long DELAY_TIME = 5000;
 	
 	private static Context CONTEXT = null;
 //	private static Handler delayHandle;
 	
 	public static void onStart(){
 		//Sdcard.setupCardReceiver();
 	}
 	
 	// returns outputStream for visibility logging file
 	// boolean 'append' specifies if data should be appended or if a new file should be created
 	public static DataOutputStream getVisibilityLog(Context contx, boolean append){
     	//set up the file directory for saving data and retrieving sprites
     	String extStorageDirectory = Sdcard.getFileDir(contx);
     	File   fileDirectory       = new File (extStorageDirectory);
     	
         File dataLogFile = new File(fileDirectory, "dataLog.txt");	//create file
         FileOutputStream outStream = null;
 		DataOutputStream dataOut = null;
 
         if(!fileDirectory.mkdirs()){	//create if directory not exist
         	//if creation of directory fails
         	Log.v(TAG, "creation of directory '"+ fileDirectory +"' fails, already exists?");
         }
 
 		if(append){
 			try {
 				outStream = new FileOutputStream(dataLogFile, true);	//append
 			} catch (FileNotFoundException e) {
 				Sdcard.waitForReady(contx);
 				try {
 					outStream = new FileOutputStream(dataLogFile, true);
 				} catch (FileNotFoundException E){
 					E.printStackTrace();
 				}
 			}
 			dataOut = new DataOutputStream(outStream);
         } else { //do not append
         	try {
         		outStream = new FileOutputStream(dataLogFile, false);	
 				dataOut = new DataOutputStream(outStream);
 				//print header on data file
 				try {
 					dataOut.writeBytes("StartVisible,EndVisible,ViewTime,animationName\n");
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				Log.d(TAG, "New dataLog file has been created");
 				avatarWallpaper.keepLogs = true;	//change the flag so that logs won't be reset every time
 			} catch (FileNotFoundException e) {
 				// TODO
 				e.printStackTrace();
 			}
         }
 		return dataOut;
 	}
 	
 	// tells the user that the sdcard is not reachable.
 	private static void missingCardError(Context context){
 		Log.e(TAG,"attempt to access SDcard when no SDcard present!");
 
 		Toast ERRmessage = Toast.makeText(context, "your avatar needs the SDcard!", Toast.LENGTH_SHORT);
 		ERRmessage.show();
 		}
 	
 	// tells the user that the sdcard is not reachable.
 	// extra canvas argument used to print error to canvas.
     private static void missingCardError(Context context, Canvas c){
     	missingCardError(context);
 		if (c != null){	// show error on the canvas if given
 			c.save();
 			Layer_Background.draw(c);
 		    Paint mPaint = new Paint();
 		    mPaint.setColor(Color.BLACK); 
 			mPaint.setTextSize(30); 
 			//mPaint.setStrokeWidth(2);
 			mPaint.setTypeface(Typeface.DEFAULT);
 			c.drawText("cannot detect SD card", -90, 90, mPaint); 
 			c.restore();
 		}
     }
 
 	// method for getting file dir when context is not available...
 	@SuppressLint("SdCardPath")
 	public static String getFileDir(){
 		if (CONTEXT.equals(null)) {
 			Log.e(TAG,"cannot get file dir without a context, and unfortunately I don't have one stored...");
 			String guess = null;
 			try{
 	    		Log.v(TAG,"trying to find sdcard location...");
 				guess = Environment.getExternalStorageDirectory().getPath()+"Android/data/edu.usf.eng.pie.avatars4change/files/";
 			} catch (NullPointerException e){
 	    		Log.v(TAG,"that didn't work... making hardcoded guess about file dir...");
 	    		guess = "/mnt/sdcard/Android/data/edu.usf.eng.pie.avatars4change/files/";
 			}
 	    	Log.v(TAG,"fDir=?="+guess);
 			return guess;
 		}else{
 			Log.v(TAG,"using previously used context to guess fileDir");
 			String guess = getFileDir(CONTEXT);
 	    	Log.v(TAG,"fDir=?="+guess);
 			return guess;
 		}
 	}
 
 	// method for getting program file dir
 	public static String getFileDir(Context context){
 		CONTEXT = context; //store the given context in case we need it later
 		//hang here until storage is ready
 		waitForReady(context);
 		try{
 	    	//return storage location
 	    	String result = context.getExternalFilesDir(null).toString()+"/";	//TODO: implement this
 	    	Log.v(TAG,"fDir="+result);
 	    	return result;
 		} catch (NullPointerException e){
 			Log.e(TAG,"sdCard found, but cannot get internal storage for unknown reason");
 			try {
 				Thread.sleep(DELAY_TIME);
 				missingCardError(context);
 			} catch (InterruptedException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 			return getFileDir(context);
 		}
 	
 	}
 
 	//returns true if external storage is readable and writable
 	// further utility is available using commented out items
 	public static boolean storageReady(){
 		//boolean mExternalStorageAvailable = false;
 		//boolean mExternalStorageWriteable = false;
 		String state = Environment.getExternalStorageState();
 		Log.d(TAG,"storageState="+state);
 		if (Environment.MEDIA_MOUNTED.equals(state)) {
 		    // We can read and write the media
 		//    mExternalStorageAvailable = mExternalStorageWriteable = true;
 		    return true;
 		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
 		    // We can only read the media
 		//    mExternalStorageAvailable = true;
 		//    mExternalStorageWriteable = false;
 		    return false;
 		} else {
 		    // Something else is wrong. It may be one of many other states, but all we need
 		    //  to know is we can neither read nor write
 		//    mExternalStorageAvailable = mExternalStorageWriteable = false;
 		    return false;
 		}
 	}
 
 	//schedules delayed run
 	public static void waitForReady(final Context context){
 		if(storageReady()){
 			return;
 		} else {
 			missingCardError(context);
 			try {
 				Thread.sleep(DELAY_TIME);
				missingCardError(context);
 			} catch (InterruptedException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 //			try{
 //	    		delayHandle.postDelayed(new Runnable() {
 //					@Override
 //					public void run() {
 //						 waitForReady(context);
 //					}
 //				},DELAY_TIME);
 //			}catch(NullPointerException e){
 //				Log.e(TAG,"handler for noSDcardDelay not found; creating new");
 //				delayHandle = new Handler();
 //				waitForReady(context); //restart the function
 //			}
 		}
 	}
 	
 	//schedules delayed run
 	public static void waitForReady(final Context context, final Canvas canv){
 		if(storageReady()){
 			return;
 		} else {
 			missingCardError(context,canv);
 			try {
 				Thread.sleep(DELAY_TIME);
				missingCardError(context);
 			} catch (InterruptedException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 //			try{
 //	    		delayHandle.postDelayed(new Runnable() {
 //					@Override
 //					public void run() {
 //						 waitForReady(context,canv);
 //					}
 //				},DELAY_TIME);
 //			}catch(NullPointerException e){
 //				Log.e(TAG,"handler for noSDcardDelay not found; creating new");
 //				delayHandle = new Handler();
 //				waitForReady(context); //restart the function
 //			}
 		}
 	}
   
 /* This is the alternate method for defining Sdcard.isPresent which uses a broadcast receiver.
  * Testing showed that this wasn't reliable, so the more clumsy way is used, but this is retained 
  * in case we want to switch back.
 
     public static boolean sdPresent = false;
 
 	public static void setupCardReceiver(){
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
      */
 }
